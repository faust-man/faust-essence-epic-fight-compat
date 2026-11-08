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
    public Compat(net.neoforged.bus.api.IEventBus bus) {
        var modifiers=net.neoforged.neoforge.registries.DeferredRegister.create(net.neoforged.neoforge.registries.NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS,"faust_epic_compat");
        modifiers.register("remove_books",()->RemoveBooks.CODEC);modifiers.register(bus);
        bus.addListener((net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent event)->{
            var entries=new java.util.HashSet<>(event.getParentEntries());entries.addAll(event.getSearchEntries());
            for(var entry:entries)if(entry.getItem() instanceof yesman.epicfight.world.item.SkillBookItem)event.remove(entry,net.minecraft.world.item.CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        });
        NeoForge.EVENT_BUS.addListener(this::start);
        NeoForge.EVENT_BUS.addListener(this::tick);
    }
    private void tick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)||player.tickCount%5!=0)return;
        yesman.epicfight.world.capabilities.EpicFightCapabilities.getUnparameterizedEntityPatch(player,yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch.class).ifPresent(patch->{
            for(var container:patch.getPlayerSkills().skillContainers){
                var skill=container.getSkill();
                if(skill!=null&&skill.getCategory().learnable()&&!CoreRuntime.data(player).unlocked.contains(nodeId(skill))){
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
            extend(CoreRuntime.balance);
        } catch(Exception ex) { throw new IllegalStateException("Invalid Epic Fight compatibility configuration: "+file,ex); }
    }
    public static com.faust.core.progression.Skill branch(String name,String category) {
        return com.faust.core.progression.Skill.valueOf(Rules.branch(name,category));
    }
    public static String nodeId(Skill skill) {return "faust_epic_compat:"+skill.getRegistryName().getNamespace()+"/"+skill.getRegistryName().getPath();}
    public static Skill skill(String id) {
        if(!id.startsWith("faust_epic_compat:"))return null;
        String[] parts=id.substring("faust_epic_compat:".length()).split("/",2);
        return parts.length==2?EpicFightRegistries.SKILL.get(ResourceLocation.fromNamespaceAndPath(parts[0],parts[1])):null;
    }
    public static void extend(Balance balance) {
        for(var node:nodes)if(balance.abilities.stream().noneMatch(a->a.id().equals(node.id())))balance.abilities.add(node);
        // Keep existing/manual positions; pack missing integration nodes into four columns.
        var layouts=new ArrayList<com.faust.core.progression.SkillTrees.Tree>(balance.trees);
        for(var branch:com.faust.core.progression.Skill.values()){
            var tree=layouts.stream().filter(t->t.skill()==branch).findFirst().orElse(null);
            var placed=new ArrayList<com.faust.core.progression.SkillTrees.Node>(tree==null?List.of():tree.nodes());
            var missing=balance.abilities.stream().filter(a->a.skill()==branch&&a.id().startsWith("faust_epic_compat:")&&placed.stream().noneMatch(n->n.ability().equals(a.id()))).sorted(Comparator.comparing(Balance.Ability::id)).toList();
            double top=placed.stream().mapToDouble(com.faust.core.progression.SkillTrees.Node::y).max().orElse(-36)+36;
            for(int i=0;i<missing.size();i++){
                String icon=switch(branch.name()){case "dexterity"->"feather";case "defense"->"shield";case "constitution"->"golden_apple";default->"iron_sword";};
                placed.add(new com.faust.core.progression.SkillTrees.Node(missing.get(i).id(),(i%4)*32,top+(i/4)*32,"minecraft:"+icon));
            }
            if(tree!=null)layouts.remove(tree);
            layouts.add(new com.faust.core.progression.SkillTrees.Tree(branch,placed));
        }
        balance.trees=layouts;
        balance.validate();
    }
}
