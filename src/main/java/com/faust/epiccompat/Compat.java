package com.faust.epiccompat;

import com.faust.core.progression.Balance;
import com.faust.core.runtime.CoreRuntime;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import yesman.epicfight.registry.EpicFightRegistries;
import yesman.epicfight.skill.Skill;
import java.nio.file.*;
import java.util.*;

@Mod("faust_epic_compat")
public final class Compat {
    private static List<Balance.Ability> nodes = List.of();
    public Compat(net.neoforged.bus.api.IEventBus bus,net.neoforged.fml.ModContainer container) {
        if(net.neoforged.fml.loading.FMLEnvironment.dist==net.neoforged.api.distmarker.Dist.CLIENT)Client.register(container);
        var modifiers=net.neoforged.neoforge.registries.DeferredRegister.create(net.neoforged.neoforge.registries.NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS,"faust_epic_compat");
        modifiers.register("remove_books",()->RemoveBooks.CODEC);modifiers.register(bus);
        bus.addListener((net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent event)->{
            var entries=new java.util.HashSet<>(event.getParentEntries());entries.addAll(event.getSearchEntries());
            for(var entry:entries)if(entry.getItem() instanceof yesman.epicfight.world.item.SkillBookItem)event.remove(entry,net.minecraft.world.item.CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        });
        Balance.CONFIG_SAVED=(section,balance)->{
            if(!section.equals("abilities"))return;
            var selected=balance.abilities.stream().filter(a->a.id().startsWith("faust_epic_compat:")).toList();
            try{Path file=FMLPaths.CONFIGDIR.get().resolve("faust-epic-compat/abilities.json");Files.createDirectories(file.getParent());Files.writeString(file,Balance.JSON.toJson(selected)+"\n");nodes=selected;}
            catch(java.io.IOException e){throw new java.io.UncheckedIOException(e);}
        };
        NeoForge.EVENT_BUS.addListener(this::start);
        NeoForge.EVENT_BUS.addListener(this::tick);
    }
    private static class Client {
        static void register(net.neoforged.fml.ModContainer container){EmergencyDodge.CLIENT_EXHAUSTED=()->com.faust.core.client.CoreClient.progress!=null&&com.faust.core.client.CoreClient.balance!=null&&com.faust.core.client.CoreClient.progress.actionsBlocked(com.faust.core.client.CoreClient.balance);WeaponMastery.CLIENT_LONG=()->com.faust.core.client.CoreClient.progress!=null&&com.faust.core.client.CoreClient.balance!=null&&com.faust.core.client.CoreClient.progress.enabled(WeaponMastery.LONG,com.faust.core.client.CoreClient.balance);container.registerExtensionPoint(net.neoforged.neoforge.client.gui.IConfigScreenFactory.class,(net.neoforged.neoforge.client.gui.IConfigScreenFactory)(c,parent)->{prepareConfig();return new com.faust.core.client.CoreConfigScreen(parent,java.util.List.of("abilities","trees","integrations"),"faust_epic_compat");});}
    }
    private static void prepareConfig(){
        if(!nodes.isEmpty())return;
        Path file=FMLPaths.CONFIGDIR.get().resolve("faust-epic-compat/abilities.json");
        try{
            if(Files.exists(file))nodes=Arrays.asList(Balance.JSON.fromJson(Files.readString(file),Balance.Ability[].class));
            else{
                var defaults=new ArrayList<Balance.Ability>();
                for(Skill skill:EpicFightRegistries.SKILL)if(skill.getCategory().learnable())defaults.add(new Balance.Ability(nodeId(skill),branch(skill.getRegistryName().getPath(),skill.getCategory().toString()),5,"hits",0,"","faust_epic_compat:skill",0));
                for(String id:List.of(Combat.MASTERY,Combat.FIST_MASTERY,Combat.SHIELD_MASTERY,Combat.SWORD_MASTERY,Combat.DAGGER_MASTERY,Combat.FIST_POWER,Combat.SHIELD_EFFICIENCY))defaults.add(mastery(id));
                nodes=defaults.stream().map(Compat::relocate).toList();
            }
        }catch(java.io.IOException e){throw new java.io.UncheckedIOException(e);}
    }
    private void tick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player))return;
        WeaponMastery.windBlocked(player);SharedStaminaBonus.refresh(player);WeaponMastery.refreshSpeed(player);
        if(CoreRuntime.active(player)){if(com.faust.core.progression.StaminaServices.present()){Milestones.fatigue(CoreRuntime.data(player));Milestones.peak(CoreRuntime.data(player),"peak_stamina",CoreRuntime.data(player).maxStamina(CoreRuntime.balance));}Milestones.visitEnd(player);}
        if(player.tickCount%5!=0)return;
        yesman.epicfight.world.capabilities.EpicFightCapabilities.getUnparameterizedEntityPatch(player,yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch.class).ifPresent(patch->{
            for(var container:patch.getPlayerSkills().skillContainers){
                var skill=container.getSkill();
                if(skill!=null&&skill.getCategory().learnable()&&(!Combat.allowedSlot(container.getSlot())||!CoreRuntime.data(player).unlocked.contains(nodeId(skill)))){
                    container.setSkill(null);patch.getPlayerSkills().removeLearnedSkill(skill);
                    yesman.epicfight.network.EpicFightNetworkManager.sendToPlayer(new yesman.epicfight.network.server.SPRemoveSkillAndLearn(skill,container.getSlot()),player);
                }
            }
        });
    }
    private void start(ServerAboutToStartEvent event) {
        Path file=FMLPaths.CONFIGDIR.get().resolve("faust-epic-compat/abilities.json");
        try {
            if(Files.exists(file)) nodes=Arrays.asList(Balance.JSON.fromJson(Files.readString(file),Balance.Ability[].class));
            else {
                var defaults=new ArrayList<Balance.Ability>();
                for(Skill skill:EpicFightRegistries.SKILL) if(skill.getCategory().learnable()) {
                    String name=skill.getRegistryName().getPath();
                    var branch=branch(name,skill.getCategory().toString());
                    defaults.add(new Balance.Ability(nodeId(skill),branch,5,"hits",0,"","faust_epic_compat:skill",0));
                }
                nodes=defaults; Files.createDirectories(file.getParent());Files.writeString(file,Balance.JSON.toJson(nodes)+"\n");
            }
            var merged=new ArrayList<>(nodes);
            for(String id:List.of(Combat.MASTERY,Combat.FIST_MASTERY,Combat.SHIELD_MASTERY,Combat.SWORD_MASTERY,Combat.DAGGER_MASTERY,Combat.FIST_POWER,Combat.SHIELD_EFFICIENCY))if(merged.stream().noneMatch(a->a.id().equals(id)))merged.add(mastery(id));
            for(var ability:WeaponMastery.defaults())if(merged.stream().noneMatch(a->a.id().equals(ability.id())))merged.add(ability);
            Path masteryRevision=file.resolveSibling(".weapon-mastery");
            if(!Files.exists(masteryRevision)){
                Files.copy(file,file.resolveSibling("abilities-before-weapon-mastery.json"),StandardCopyOption.REPLACE_EXISTING);
                for(int i=0;i<merged.size();i++)if(merged.get(i).id().endsWith("/technician"))merged.set(i,relocate(merged.get(i)));
            }
            Path combatRevision=file.resolveSibling(".combat-refinement");
            if(!Files.exists(combatRevision)){Files.copy(file,file.resolveSibling("abilities-before-combat-refinement.json"),StandardCopyOption.REPLACE_EXISTING);for(int i=0;i<merged.size();i++)if(merged.get(i).id().equals(WeaponMastery.GREAT)){var json=Balance.JSON.toJsonTree(merged.get(i)).getAsJsonObject();json.addProperty("parent",com.faust.core.progression.StaminaServices.present()?Combat.MASTERY:"");json.addProperty("counter",WeaponMastery.SWINGS);json.addProperty("amount",250);json.add("conditions",new com.google.gson.JsonArray());merged.set(i,Balance.JSON.fromJson(json,Balance.Ability.class));}}
            Path constitutionRevision=file.resolveSibling(".constitution-tune");if(!Files.exists(constitutionRevision)){Files.copy(file,file.resolveSibling("abilities-before-constitution-tune.json"),StandardCopyOption.REPLACE_EXISTING);for(int i=0;i<merged.size();i++)if(java.util.Set.of("faust_epic_compat:epicfight/adaptive_skin","faust_epic_compat:epicfight/catharsis","faust_epic_compat:epicfight/death_harvest").contains(merged.get(i).id()))merged.set(i,relocate(merged.get(i)));}
            Path revision=file.resolveSibling(".requirements-revision");
            if(!Files.exists(revision))for(int i=0;i<merged.size();i++)merged.set(i,relocate(merged.get(i)));
            Path polish=file.resolveSibling(".tree-polish");
            if(!Files.exists(polish)){
                Files.copy(file,file.resolveSibling("abilities-before-tree-polish.json"),StandardCopyOption.REPLACE_EXISTING);
                for(int i=0;i<merged.size();i++)if(merged.get(i).id().endsWith("/bonebreaker"))merged.set(i,relocate(merged.get(i)));
            }
            if(!merged.equals(nodes)){nodes=merged;Files.writeString(file,Balance.JSON.toJson(nodes)+"\n");}
            Files.writeString(polish,"1\n");Files.writeString(masteryRevision,"1\n");Files.writeString(combatRevision,"1\n");Files.writeString(constitutionRevision,"1\n");
            Path followup=file.resolveSibling(".balance-followup");
            if(!Files.exists(followup)){
                for(int i=0;i<nodes.size();i++)if(nodes.get(i).id().equals(Combat.SHIELD_EFFICIENCY)){var j=Balance.JSON.toJsonTree(nodes.get(i)).getAsJsonObject();j.addProperty("value",1);var updated=new ArrayList<>(nodes);updated.set(i,Balance.JSON.fromJson(j,Balance.Ability.class));nodes=updated;}
                Files.writeString(file,Balance.JSON.toJson(nodes)+"\n");Files.writeString(followup,"1\n");
            }
            Files.writeString(revision,"1\n");
            extend(CoreRuntime.balance);
        } catch(Exception ex) { throw new IllegalStateException("Invalid Epic Fight compatibility configuration: "+file,ex); }
    }
    public static com.faust.core.progression.Skill branch(String name,String category) {
        return com.faust.core.progression.Skill.valueOf(Rules.branch(name,category));
    }
    public static boolean isMastery(String id){return List.of(Combat.MASTERY,Combat.FIST_MASTERY,Combat.SHIELD_MASTERY,Combat.SWORD_MASTERY,Combat.DAGGER_MASTERY,Combat.FIST_POWER,Combat.SHIELD_EFFICIENCY).contains(id);}
    public static Balance.Ability mastery(String id){return new Balance.Ability(id,(id.equals(Combat.SHIELD_MASTERY)||id.equals(Combat.SHIELD_EFFICIENCY))?com.faust.core.progression.Skill.defense:id.equals(Combat.DAGGER_MASTERY)?com.faust.core.progression.Skill.dexterity:com.faust.core.progression.Skill.strength,5,"hits",0,id.equals(Combat.SHIELD_MASTERY)?"faustcore:shield_efficiency":"",id.equals(Combat.SHIELD_EFFICIENCY)?"shield_cost_reduction":"faust_epic_compat:mastery",id.equals(Combat.SHIELD_EFFICIENCY)?0.5:0,List.of(),List.of(),List.of(),List.of(),List.of(),"all",0,0,true);}
    public static Balance.Ability relocate(Balance.Ability a){
        var branch=(a.id().equals("faust_epic_compat:epicfight/adaptive_skin")||a.id().equals("faust_epic_compat:epicfight/catharsis")||a.id().equals("faust_epic_compat:epicfight/death_harvest"))?com.faust.core.progression.Skill.constitution:a.id().equals("faust_epic_compat:epicfight/revelation")?com.faust.core.progression.Skill.intelligence:a.skill();
        if(a.id().startsWith("faust_epic_compat:epicfight/")){
            String name=a.id().substring("faust_epic_compat:epicfight/".length());
            if(java.util.Set.of("guard","impact_guard","parrying").contains(name))branch=com.faust.core.progression.Skill.defense;
            if(java.util.Set.of("roll","step","technician","demolition_leap").contains(name))branch=com.faust.core.progression.Skill.dexterity;
        }
        var json=Balance.JSON.toJsonTree(a).getAsJsonObject();
        if(a.id().startsWith("faust_epic_compat:"))json.addProperty("toggleable",isMastery(a.id())||WeaponMastery.custom(a.id()));
        if(branch!=a.skill())json.addProperty("skill",branch.name());
        if(a.id().equals("faust_epic_compat:epicfight/technician")){
            json.addProperty("skill","dexterity");json.addProperty("level",5);
            json.add("characteristics",Balance.JSON.toJsonTree(List.of(new Balance.SkillRequirement(com.faust.core.progression.Skill.dexterity,5))));json.addProperty("counter",WeaponMastery.HITS);json.addProperty("amount",200);json.add("conditions",new com.google.gson.JsonArray());
        }
        if(a.id().equals("faust_epic_compat:epicfight/guard")||a.id().equals("faust_epic_compat:epicfight/impact_guard")){
            json.addProperty("counter","blocks");json.addProperty("amount",a.id().endsWith("/guard")?10:100);json.add("conditions",new com.google.gson.JsonArray());
        }
        if(a.id().equals("faust_epic_compat:epicfight/impact_guard")||a.id().equals("faust_epic_compat:epicfight/parrying"))json.addProperty("parent","faust_epic_compat:epicfight/guard");
        if(a.id().equals("faust_epic_compat:epicfight/parrying")){
            var all=new java.util.LinkedHashSet<>(a.requiresAll());all.add("faust_epic_compat:epicfight/technician");json.add("requiresAll",Balance.JSON.toJsonTree(all));
        }
        if(a.id().equals("faust_epic_compat:epicfight/roll")){
            json.addProperty("level",1);json.addProperty("counter","run_metres");json.addProperty("amount",500);
            json.add("characteristics",new com.google.gson.JsonArray());json.add("conditions",new com.google.gson.JsonArray());
        }
        if(a.id().equals("faust_epic_compat:epicfight/demolition_leap")||a.id().equals("faust_epic_compat:epicfight/phantom_ascent")){
            json.addProperty("skill","dexterity");json.addProperty("level",a.id().endsWith("/demolition_leap")?5:10);
            json.add("characteristics",Balance.JSON.toJsonTree(a.id().endsWith("/demolition_leap")?List.of(new Balance.SkillRequirement(com.faust.core.progression.Skill.dexterity,5),new Balance.SkillRequirement(com.faust.core.progression.Skill.strength,5)):List.of(new Balance.SkillRequirement(com.faust.core.progression.Skill.dexterity,10))));
            json.addProperty("counter",CombatPractice.JUMPS);json.addProperty("amount",100);json.add("conditions",new com.google.gson.JsonArray());
        }
        if(a.id().equals("faust_epic_compat:epicfight/step")){
            json.addProperty("parent","faust_epic_compat:epicfight/roll");json.addProperty("counter",CombatPractice.ROLLS);json.addProperty("amount",100);json.add("conditions",new com.google.gson.JsonArray());
        }
        if(a.id().equals(Combat.SHIELD_MASTERY))json.addProperty("parent",Combat.SHIELD_EFFICIENCY);
        if(a.id().equals(Combat.SHIELD_EFFICIENCY))json.addProperty("parent","faust_epic_compat:epicfight/guard");
        if(a.id().equals(Combat.SWORD_MASTERY))json.addProperty("parent","faust_epic_compat:epicfight/swordmaster");
        if(a.id().equals(Combat.DAGGER_MASTERY))json.addProperty("parent","faust_epic_compat:epicfight/technician");
        if(a.id().equals("faustcore:shield_efficiency")){json.addProperty("value",1);}
        if(a.id().equals(Combat.FIST_POWER)){json.addProperty("parent",Combat.FIST_MASTERY);json.addProperty("counter",CombatPractice.FIST_DAMAGE);json.addProperty("amount",100);json.add("conditions",new com.google.gson.JsonArray());}
        String counter=null;double amount=10;
        if(a.id().equals(Combat.SHIELD_MASTERY)){counter=CombatPractice.SHIELD_HITS;amount=100;}
        if(a.id().equals(Combat.DAGGER_MASTERY)){counter=CombatPractice.DAGGER_HITS;amount=100;}
        if(a.id().equals(Combat.FIST_MASTERY))counter=CombatPractice.FISTS;
        if(a.id().equals(Combat.MASTERY))counter=CombatPractice.TWO_HAND;
        if(a.id().equals("faust_epic_compat:epicfight/swordmaster"))counter=CombatPractice.SWORDS;
        if(a.id().equals(Combat.SWORD_MASTERY))counter=CombatPractice.DUAL;
        if(a.id().equals("faust_epic_compat:epicfight/berserker")){counter=CombatPractice.DAMAGE;amount=40;json.addProperty("parent","");}
        if(a.id().equals("faust_epic_compat:epicfight/vengeance")){counter=CombatPractice.DAMAGE;amount=80;json.addProperty("parent","faust_epic_compat:epicfight/berserker");}
        if(a.id().equals("faust_epic_compat:epicfight/forbidden_strength")){counter=CombatPractice.DAMAGE;amount=120;json.addProperty("parent","faust_epic_compat:epicfight/vengeance");}
        if(counter!=null){json.addProperty("counter",counter);json.addProperty("amount",amount);json.add("conditions",new com.google.gson.JsonArray());}
        return SkillRequirements.apply(Balance.JSON.fromJson(json,Balance.Ability.class));
    }
    public static String icon(String id){
        if(WeaponMastery.custom(id))return id.equals(WeaponMastery.LONG)?"epicfight:iron_longsword":"epicfight:iron_greatsword";
        if(id.startsWith("faust_epic_compat:epicfight/"))return id;
        return "minecraft:"+(id.equals(Combat.SHIELD_EFFICIENCY)?"iron_ingot":id.equals(Combat.SHIELD_MASTERY)?"shield":id.equals(Combat.DAGGER_MASTERY)?"flint":id.equals(Combat.FIST_MASTERY)?"leather":id.equals(Combat.SWORD_MASTERY)?"diamond_sword":"iron_sword");
    }
    public static String nodeId(Skill skill) {return "faust_epic_compat:"+skill.getRegistryName().getNamespace()+"/"+skill.getRegistryName().getPath();}
    public static Skill skill(String id) {
        if(!id.startsWith("faust_epic_compat:"))return null;
        String[] parts=id.substring("faust_epic_compat:".length()).split("/",2);
        return parts.length==2?EpicFightRegistries.SKILL.get(ResourceLocation.fromNamespaceAndPath(parts[0],parts[1])):null;
    }
    public static void extend(Balance balance) {
        for(var ability:WeaponMastery.defaults())if((ability.parent().isEmpty()||balance.abilities.stream().anyMatch(a->a.id().equals(ability.parent())))&&balance.abilities.stream().noneMatch(a->a.id().equals(ability.id())))balance.abilities.add(ability);
        for(var node:nodes)if((com.faust.core.progression.StaminaServices.present()||!isMastery(node.id()))&&balance.abilities.stream().noneMatch(a->a.id().equals(node.id())))balance.abilities.add(node);
        for(int i=0;i<balance.abilities.size();i++)balance.abilities.set(i,relocate(balance.abilities.get(i)));
        for(var configured:nodes)for(int i=0;i<balance.abilities.size();i++)if(balance.abilities.get(i).id().equals(configured.id()))balance.abilities.set(i,configured);
        if(!com.faust.core.progression.StaminaServices.present()){
            var removed=new HashSet<String>();
            for(var a:balance.abilities)if(isMastery(a.id()))removed.add(a.id());
            balance.abilities.removeIf(a->removed.contains(a.id()));
            var staminaDependent=new HashSet<String>();staminaDependent.add("faust_epic_compat:epicfight/endurance");
            boolean changed;
            do{changed=false;for(var node:balance.abilities)if(node.id().startsWith("faust_epic_compat:")&&node.dependencies().stream().anyMatch(id->staminaDependent.contains(id)||removed.contains(id)||id.equals("faustcore:training_constitution_stamina")))changed|=staminaDependent.add(node.id());}while(changed);
            for(int i=0;i<balance.abilities.size();i++){
                var a=com.faust.core.progression.StaminaContent.detach(balance.abilities.get(i),removed);
                // These requirements describe Faust fatigue/exhaustion, absent with native Epic Fight stamina.
                if(a.id().equals("faust_epic_compat:epicfight/emergency_escape")||a.id().equals("faust_epic_compat:epicfight/adrenaline_fiend")){
                    var json=Balance.JSON.toJsonTree(a).getAsJsonObject();json.addProperty("counter","");json.addProperty("amount",0);json.add("conditions",new com.google.gson.JsonArray());
                    a=Balance.JSON.fromJson(json,Balance.Ability.class);
                }
                if(staminaDependent.contains(a.id())&&!WeaponMastery.custom(a.id())){
                    var json=Balance.JSON.toJsonTree(a).getAsJsonObject();json.addProperty("skill","constitution");json.addProperty("level",7);json.add("characteristics",new com.google.gson.JsonArray());json.addProperty("parent","");json.add("requiresAll",new com.google.gson.JsonArray());json.add("requiresAny",new com.google.gson.JsonArray());json.addProperty("requiresCount",0);json.addProperty("counter","skill_levels");json.addProperty("amount",0);json.add("conditions",new com.google.gson.JsonArray());a=Balance.JSON.fromJson(json,Balance.Ability.class);
                }
                balance.abilities.set(i,a);
            }
        }
        // Preserve Core positions; reflow addon nodes into the left branches and compact grid.
        var layouts=new ArrayList<com.faust.core.progression.SkillTrees.Tree>(balance.trees);
        for(var branch:com.faust.core.progression.Skill.values()){
            var tree=layouts.stream().filter(t->t.skill()==branch).findFirst().orElse(null);
            var placed=new ArrayList<com.faust.core.progression.SkillTrees.Node>();
            if(tree!=null)for(var n:tree.nodes()){
                if(n.ability().startsWith("faust_epic_compat:"))continue;
                if(balance.abilities.stream().noneMatch(a->a.id().equals(n.ability())&&a.skill()==branch))continue;
                placed.add(n.ability().startsWith("faust_epic_compat:")?new com.faust.core.progression.SkillTrees.Node(n.ability(),n.x(),n.y(),icon(n.ability())):n);
            }
            if(branch==com.faust.core.progression.Skill.strength&&balance.abilities.stream().anyMatch(a->a.id().equals(WeaponMastery.GREAT)))placed.add(new com.faust.core.progression.SkillTrees.Node(WeaponMastery.GREAT,4,7,icon(WeaponMastery.GREAT)));
            if(branch==com.faust.core.progression.Skill.defense&&balance.abilities.stream().anyMatch(a->a.id().equals(WeaponMastery.LONG)))placed.add(new com.faust.core.progression.SkillTrees.Node(WeaponMastery.LONG,-8,11,icon(WeaponMastery.LONG)));
            var left=List.of("faust_epic_compat:epicfight/swordmaster",Combat.SWORD_MASTERY,Combat.FIST_MASTERY,Combat.MASTERY,"faust_epic_compat:epicfight/berserker","faust_epic_compat:epicfight/vengeance","faust_epic_compat:epicfight/forbidden_strength");
            double leftX=placed.stream().mapToDouble(com.faust.core.progression.SkillTrees.Node::x).min().orElse(0)-4;
            double leftY=placed.stream().mapToDouble(com.faust.core.progression.SkillTrees.Node::y).min().orElse(0);
            if(branch==com.faust.core.progression.Skill.strength)for(int i=0;i<left.size();i++){
                String id=left.get(i);if(balance.abilities.stream().noneMatch(a->a.id().equals(id)&&a.skill()==branch))continue;
                // Weapon pair in the near column; the three-step damage path farther left.
                int column=i<2?0:i<4?1:2;int row=i<2?i:i<4?i-2:i-4;
                placed.add(new com.faust.core.progression.SkillTrees.Node(id,leftX-column*4,leftY+(row==2?7:row*4),icon(id)));
            }
            if(branch==com.faust.core.progression.Skill.defense){
                var guard=(tree==null?placed:tree.nodes()).stream().filter(n->n.ability().equals("faust_epic_compat:epicfight/guard")).findFirst().orElse(null);
                double gx=guard==null?3:guard.x(),gy=guard==null?7:guard.y();
                for(String id:List.of("guard","impact_guard","parrying")){
                    String aid="faust_epic_compat:epicfight/"+id;
                    if(balance.abilities.stream().anyMatch(a->a.id().equals(aid)))placed.add(new com.faust.core.progression.SkillTrees.Node(aid,gx+(id.equals("guard")?0:id.equals("impact_guard")?-3:3),gy+(id.equals("guard")?0:4),icon(aid)));
                }
                if(balance.abilities.stream().anyMatch(a->a.id().equals(Combat.SHIELD_EFFICIENCY)))placed.add(new com.faust.core.progression.SkillTrees.Node(Combat.SHIELD_EFFICIENCY,gx,gy+4,"minecraft:iron_ingot"));
                var economy=placed.stream().filter(n->n.ability().equals(Combat.SHIELD_EFFICIENCY)).findFirst().orElse(null);
                if(economy!=null&&balance.abilities.stream().anyMatch(a->a.id().equals(Combat.SHIELD_MASTERY)))placed.add(new com.faust.core.progression.SkillTrees.Node(Combat.SHIELD_MASTERY,economy.x(),economy.y()+4,icon(Combat.SHIELD_MASTERY)));
            }
            if(branch==com.faust.core.progression.Skill.strength){
                var fist=placed.stream().filter(n->n.ability().equals(Combat.FIST_MASTERY)).findFirst().orElse(null);
                if(fist!=null&&balance.abilities.stream().anyMatch(a->a.id().equals(Combat.FIST_POWER)))placed.add(new com.faust.core.progression.SkillTrees.Node(Combat.FIST_POWER,fist.x(),fist.y()+7,icon(Combat.FIST_POWER)));
            }
            if(branch==com.faust.core.progression.Skill.strength){
                for(int i=0;i<placed.size();i++){var n=placed.get(i);if(n.ability().equals("faustcore:mace_efficiency")){double rootY=placed.stream().filter(a->a.ability().equals("faustcore:training_strength_damage")).mapToDouble(com.faust.core.progression.SkillTrees.Node::y).findFirst().orElse(0);placed.set(i,new com.faust.core.progression.SkillTrees.Node(n.ability(),n.x(),rootY+3,n.icon()));}}
            }
            var missing=balance.abilities.stream().filter(a->a.skill()==branch&&a.id().startsWith("faust_epic_compat:")&&placed.stream().noneMatch(n->n.ability().equals(a.id()))).sorted(Comparator.comparing(Balance.Ability::id)).toList();
            double top=placed.stream().mapToDouble(com.faust.core.progression.SkillTrees.Node::y).max().orElse(-3)+3;
            double gridLeft=branch==com.faust.core.progression.Skill.strength?placed.stream().mapToDouble(com.faust.core.progression.SkillTrees.Node::x).min().orElse(0):0;
            for(int i=0;i<missing.size();i++){
                placed.add(new com.faust.core.progression.SkillTrees.Node(missing.get(i).id(),gridLeft+(i%4)*3,top+(i/4)*3,icon(missing.get(i).id())));
            }
            if(branch==com.faust.core.progression.Skill.strength)swapPositions(placed,Combat.MASTERY,Combat.FIST_POWER);
            if(branch==com.faust.core.progression.Skill.dexterity)alignDexterity(placed);
            if(branch==com.faust.core.progression.Skill.strength){
                for(var pair:List.of(new String[]{"bonebreaker",Combat.FIST_MASTERY},new String[]{"meteor_slam","faustcore:training_strength_damage"})){
                    var anchor=placed.stream().filter(n->n.ability().equals(pair[1])).findFirst().orElse(null);
                    var node=placed.stream().filter(n->n.ability().equals("faust_epic_compat:epicfight/"+pair[0])).findFirst().orElse(null);
                    if(anchor!=null&&node!=null)move(placed,node.ability(),anchor.x(),node.y());
                }
            }
            if(branch==com.faust.core.progression.Skill.strength){
                for(int i=0;i<placed.size();i++){var n=placed.get(i);if(n.ability().endsWith("/vengeance")||n.ability().endsWith("/forbidden_strength"))placed.set(i,new com.faust.core.progression.SkillTrees.Node(n.ability(),n.x(),n.y()-1,n.icon()));}
                var forbidden=placed.stream().filter(n->n.ability().endsWith("/forbidden_strength")).findFirst().orElse(null);
                if(forbidden!=null)move(placed,"faust_epic_compat:epicfight/adrenaline_fiend",forbidden.x(),forbidden.y()+4);
            }
            if(branch==com.faust.core.progression.Skill.defense){
                var parry=placed.stream().filter(n->n.ability().endsWith("/parrying")).findFirst().orElse(null);
                var armor=placed.stream().filter(n->n.ability().equals("faustcore:armor_mastery")).findFirst().orElse(null);
                if(parry!=null&&armor!=null){
                    var shield=placed.stream().filter(n->n.ability().equals(Combat.SHIELD_MASTERY)).findFirst().orElse(null);
                    move(placed,WeaponMastery.LONG,parry.x(),shield==null?parry.y()+4:shield.y());
                    var iron=placed.stream().filter(n->n.ability().equals("faustcore:armor_familiarity")).findFirst().orElse(null);
                    var guardNode=placed.stream().filter(n->n.ability().endsWith("/guard")).findFirst().orElse(null);
                    double vertical=iron==null||guardNode==null?0:iron.y()-guardNode.y();
                    var group=new java.util.HashSet<String>();group.add("faust_epic_compat:epicfight/guard");boolean changed;
                    do{changed=false;for(var a:balance.abilities)if(a.skill()==branch&&a.dependencies().stream().anyMatch(group::contains))changed|=group.add(a.id());}while(changed);
                    double delta=armor.x()-com.faust.core.progression.SkillTrees.NODE_SIZE-com.faust.core.progression.SkillTrees.GRID-parry.x();
                    for(int i=0;i<placed.size();i++){var n=placed.get(i);if(group.contains(n.ability()))placed.set(i,new com.faust.core.progression.SkillTrees.Node(n.ability(),n.x()+delta,n.y()+vertical,n.icon()));}
                }
            }
            if(branch==com.faust.core.progression.Skill.intelligence){
                var cat=placed.stream().filter(n->n.ability().equals("faustcore:cat_scent")).findFirst().orElse(null);
                var enchant=placed.stream().filter(n->n.ability().equals("faustcore:enchanting_power")).findFirst().orElse(null);
                if(cat!=null&&enchant!=null){move(placed,cat.ability(),enchant.x(),enchant.y()+4);move(placed,"faust_epic_compat:epicfight/revelation",-1,enchant.y()+4);}
            }
            if(branch==com.faust.core.progression.Skill.constitution){
                var health=placed.stream().filter(n->n.ability().equals("faustcore:training_constitution_health")).findFirst().orElse(null);
                var recovery=placed.stream().filter(n->n.ability().equals("faustcore:resilient_recovery")).findFirst().orElse(null);
                if(health!=null){move(placed,"faust_epic_compat:epicfight/hypervitality",health.x()-4,health.y());move(placed,"faust_epic_compat:epicfight/hyper_vitality",health.x()-4,health.y());move(placed,"faust_epic_compat:epicfight/endurance",health.x()+4,health.y());move(placed,"faust_epic_compat:epicfight/adaptive_skin",health.x(),health.y()+4);}
                if(health!=null){move(placed,"faustcore:metabolism",health.x()+4,health.y()-4);move(placed,"faustcore:second_wind",health.x()+7,health.y()-4);move(placed,"faustcore:resilient_recovery",health.x()+7,health.y());move(placed,"faust_epic_compat:epicfight/stamina_pillager",health.x()+11,health.y());move(placed,"faust_epic_compat:epicfight/catharsis",health.x()+4,health.y()+4);move(placed,"faust_epic_compat:epicfight/death_harvest",health.x()+7,health.y()+4);}
            }
            if(branch==com.faust.core.progression.Skill.dexterity){
                for(int i=0;i<placed.size();i++){var n=placed.get(i);if(n.ability().equals(Combat.DAGGER_MASTERY))placed.set(i,new com.faust.core.progression.SkillTrees.Node(n.ability(),n.x(),n.y()-1,n.icon()));}
            }
            if(branch==com.faust.core.progression.Skill.constitution){
                for(int i=0;i<placed.size();i++){var n=placed.get(i);if(java.util.Set.of("faustcore:second_wind","faustcore:resilient_recovery","faust_epic_compat:epicfight/stamina_pillager","faust_epic_compat:epicfight/death_harvest").contains(n.ability()))placed.set(i,new com.faust.core.progression.SkillTrees.Node(n.ability(),n.x()+1,n.y(),n.icon()));}
            }
            if(branch==com.faust.core.progression.Skill.strength){
                var meteor=placed.stream().filter(n->n.ability().endsWith("/meteor_slam")).findFirst().orElse(null);
                var paired=placed.stream().filter(n->n.ability().equals(Combat.SWORD_MASTERY)).findFirst().orElse(null);
                if(meteor!=null&&paired!=null){move(placed,"faust_epic_compat:epicfight/adrenaline_fiend",paired.x(),meteor.y());double row=meteor.y();for(int i=0;i<placed.size();i++){var n=placed.get(i);if(n.y()==row)placed.set(i,new com.faust.core.progression.SkillTrees.Node(n.ability(),n.x(),n.y()-1,n.icon()));}}
                for(int i=0;i<placed.size();i++){var n=placed.get(i);if(n.ability().equals(Combat.MASTERY)||n.ability().equals(Combat.FIST_POWER))placed.set(i,new com.faust.core.progression.SkillTrees.Node(n.ability(),n.x(),n.y()-1,n.icon()));}
                var grip=placed.stream().filter(n->n.ability().equals(Combat.MASTERY)).findFirst().orElse(null);
                if(grip!=null&&paired!=null){move(placed,WeaponMastery.GREAT,paired.x(),grip.y());if(grip.y()-paired.y()<3)move(placed,paired.ability(),paired.x(),grip.y()-3);}
            }
            if(branch==com.faust.core.progression.Skill.dexterity){for(int i=0;i<placed.size();i++){var n=placed.get(i);if(n.ability().endsWith("/phantom_ascent"))placed.set(i,new com.faust.core.progression.SkillTrees.Node(n.ability(),n.x(),n.y()-1,n.icon()));}}
            if(branch==com.faust.core.progression.Skill.strength){
                var forbidden=placed.stream().filter(n->n.ability().endsWith("/forbidden_strength")).findFirst().orElse(null);var bone=placed.stream().filter(n->n.ability().endsWith("/bonebreaker")).findFirst().orElse(null);if(forbidden!=null&&bone!=null)move(placed,"faust_epic_compat:epicfight/adrenaline_fiend",forbidden.x(),bone.y());

            }
            if(tree!=null)layouts.remove(tree);
            var finalNodes=com.faust.core.progression.SkillTrees.spaced(placed);
            if(branch==com.faust.core.progression.Skill.strength){var forbidden=finalNodes.stream().filter(n->n.ability().endsWith("/forbidden_strength")).findFirst().orElse(null);var bone=finalNodes.stream().filter(n->n.ability().endsWith("/bonebreaker")).findFirst().orElse(null);if(forbidden!=null&&bone!=null)move(finalNodes,"faust_epic_compat:epicfight/adrenaline_fiend",forbidden.x(),bone.y());}
            if(branch==com.faust.core.progression.Skill.strength){var grip=finalNodes.stream().filter(n->n.ability().equals(WeaponMastery.GREAT)).findFirst().orElse(null);var bone=finalNodes.stream().filter(n->n.ability().endsWith("/bonebreaker")).findFirst().orElse(null);if(grip!=null&&bone!=null)move(finalNodes,"faust_epic_compat:epicfight/emergency_escape",grip.x(),bone.y());}
            layouts.add(new com.faust.core.progression.SkillTrees.Tree(branch,finalNodes));
        }
        balance.trees=layouts;
        balance.validate();
    }

    private static void move(List<com.faust.core.progression.SkillTrees.Node> nodes,String id,double x,double y){
        for(int i=0;i<nodes.size();i++)if(nodes.get(i).ability().equals(id)){var n=nodes.get(i);nodes.set(i,new com.faust.core.progression.SkillTrees.Node(id,x,y,n.icon()));return;}
    }
    private static void alignDexterity(List<com.faust.core.progression.SkillTrees.Node> placed){
        String roll="faust_epic_compat:epicfight/roll",step="faust_epic_compat:epicfight/step",tech="faust_epic_compat:epicfight/technician";
        var dodge=placed.stream().filter(n->n.ability().equals(roll)).findFirst().orElse(null);
        var daggers=placed.stream().filter(n->n.ability().equals(Combat.DAGGER_MASTERY)).findFirst().orElse(null);
        var technician=placed.stream().filter(n->n.ability().equals(tech)).findFirst().orElse(null);
        var jumps=List.of("faust_epic_compat:epicfight/demolition_leap","faust_epic_compat:epicfight/phantom_ascent").stream().filter(id->placed.stream().anyMatch(n->n.ability().equals(id))).toList();
        placed.removeIf(n->jumps.contains(n.ability()));
        boolean hasStep=placed.stream().anyMatch(n->n.ability().equals(step));
        if(dodge!=null){
            placed.removeIf(n->n.ability().equals(roll)||n.ability().equals(step));
            var anchor=placed.stream().filter(n->n.ability().equals("faustcore:cushioned_landing")).findFirst().orElse(null);
            double x=anchor==null?-5:anchor.x(),y=anchor==null?dodge.y():anchor.y()+4;
            while(overlaps(placed,x,y)||hasStep&&overlaps(placed,x,y+3))y+=4;
            placed.add(new com.faust.core.progression.SkillTrees.Node(roll,x,y,icon(roll)));
            if(hasStep)placed.add(new com.faust.core.progression.SkillTrees.Node(step,x,y+3,icon(step)));
        }
        if(daggers!=null&&technician!=null){
            placed.removeIf(n->n.ability().equals(tech)||n.ability().equals(Combat.DAGGER_MASTERY));
            double x=daggers.x(),y=placed.stream().filter(n->n.ability().equals(roll)).mapToDouble(com.faust.core.progression.SkillTrees.Node::y).findFirst().orElse(daggers.y());
            while(overlaps(placed,x,y)||overlaps(placed,x,y+4))y+=4;
            placed.add(new com.faust.core.progression.SkillTrees.Node(tech,x,y,icon(tech)));
            placed.add(new com.faust.core.progression.SkillTrees.Node(Combat.DAGGER_MASTERY,x,y+4,icon(Combat.DAGGER_MASTERY)));
        }
        var bow=placed.stream().filter(n->n.ability().equals("faustcore:bow_mastery")).findFirst().orElse(null);
        double jumpX=bow==null?5:bow.x(),jumpY=bow==null?7:bow.y()+4;
        for(String id:jumps){while(overlaps(placed,jumpX,jumpY))jumpY+=4;placed.add(new com.faust.core.progression.SkillTrees.Node(id,jumpX,jumpY,icon(id)));jumpY+=4;}
    }
    private static void swapPositions(List<com.faust.core.progression.SkillTrees.Node> nodes,String a,String b){
        int ai=-1,bi=-1;for(int i=0;i<nodes.size();i++){if(nodes.get(i).ability().equals(a))ai=i;if(nodes.get(i).ability().equals(b))bi=i;}
        if(ai<0||bi<0)return;var first=nodes.get(ai);var second=nodes.get(bi);
        nodes.set(ai,new com.faust.core.progression.SkillTrees.Node(a,second.x(),second.y(),first.icon()));nodes.set(bi,new com.faust.core.progression.SkillTrees.Node(b,first.x(),first.y(),second.icon()));
    }
    private static boolean overlaps(List<com.faust.core.progression.SkillTrees.Node> nodes,double x,double y){
        return !com.faust.core.progression.SkillTrees.validPosition(new com.faust.core.progression.SkillTrees.Node("faust_epic_compat:layout_probe",x,y,"minecraft:book"),nodes);
    }
}
