package com.faust.epiccompat;
import com.faust.core.runtime.CoreRuntime;
import com.faust.core.progression.*;
import net.minecraft.world.item.*;
@net.neoforged.fml.common.EventBusSubscriber(modid="faust_epic_compat")
public final class BalanceRevisionRuntime {
    static int checks;
    static void check(boolean v,String why){checks++;if(!v)throw new AssertionError(why);}
    @net.neoforged.bus.api.SubscribeEvent
    public static void started(net.neoforged.neoforge.event.server.ServerStartedEvent event){
        try{
            var player=net.neoforged.neoforge.common.util.FakePlayerFactory.get(event.getServer().overworld(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"BalanceChecks"));
            var p=CoreRuntime.data(player);var b=CoreRuntime.balance;
            check(StaminaServices.present(),"resource provider");
            for(var item:java.util.List.of(Items.IRON_BOOTS,Items.CHAINMAIL_BOOTS,Items.DIAMOND_HELMET,Items.NETHERITE_CHESTPLATE)){
                player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.FEET,new ItemStack(item));check(CoreRuntime.trainsArmor(player),"eligible armor "+item);
            }
            for(var item:java.util.List.of(Items.LEATHER_BOOTS,Items.GOLDEN_BOOTS)){
                player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.FEET,new ItemStack(item));check(!CoreRuntime.trainsArmor(player),"ineligible armor "+item);
            }
            player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.FEET,new ItemStack(Items.IRON_BOOTS));p.stamina=0;p.exhausted=true;
            check(!CoreRuntime.jump(player),"exhausted armor prevents jump");
            p.unlocked.add("faustcore:armor_familiarity");check(CoreRuntime.jump(player),"armor training permits jump");
            p.stamina=10;p.exhausted=false;p.unlocked.clear();check(CoreRuntime.shieldPayment(player,false)&&p.stamina==8,"full shield segment");
            check(CoreRuntime.shieldPayment(player,true)&&p.stamina==7,"half shield segment for arrow");
            p.unlocked.add("faustcore:shield_efficiency");check(CoreRuntime.shieldPayment(player,true)&&p.stamina==7,"arrow cannot restore stamina");
            check(CoreRuntime.shieldPayment(player,false)&&p.stamina==6,"melee discount");
            for(String name:java.util.List.of("roll","guard","impact_guard","swordmaster","endurance","hypervitality","adaptive_skin","stamina_pillager")){
                var a=b.abilities.stream().filter(x->x.id().equals("faust_epic_compat:epicfight/"+name)).findFirst().orElseThrow();
                check(a.level()==(name.equals("roll")||name.equals("guard")?3:name.equals("impact_guard")?10:name.equals("endurance")||name.equals("hypervitality")?7:a.level()),"level "+name);
            }
            var health=b.trees.stream().flatMap(t->t.nodes().stream()).filter(n->n.ability().equals("faustcore:training_constitution_health")).findFirst().orElseThrow();
            var skin=b.trees.stream().flatMap(t->t.nodes().stream()).filter(n->n.ability().endsWith("/adaptive_skin")).findFirst().orElseThrow();
            check(skin.x()==health.x()&&skin.y()>health.y(),"skin below health");
            for(String name:java.util.List.of("endurance","hypervitality")){var a=b.abilities.stream().filter(x->x.id().endsWith("/"+name)).findFirst().orElseThrow();check(a.parent().equals("faustcore:training_constitution_stamina"),"constitution parent");}
            check(yesman.epicfight.world.gamerule.EpicFightGameRules.KEEP_SKILLS.getConfigHolder().getDefault(),"keepSkills defaults true");
            for(String name:java.util.List.of("berserker","vengeance","forbidden_strength")){
                var a=b.abilities.stream().filter(x->x.id().endsWith("/"+name)).findFirst().orElseThrow();int required=name.equals("berserker")?5:name.equals("vengeance")?7:10;
                check(a.skillRequirements().equals(java.util.List.of(new Balance.SkillRequirement(Skill.strength,required),new Balance.SkillRequirement(Skill.constitution,required))),"dual combat requirements");
            }
            for(String name:java.util.List.of("endurance","hypervitality")){var n=b.trees.stream().flatMap(t->t.nodes().stream()).filter(x->x.ability().endsWith("/"+name)).findFirst().orElseThrow();check(n.y()==health.y(),"constitution alignment");}
            var pair=b.abilities.stream().filter(x->x.id().equals(Combat.SWORD_MASTERY)).findFirst().orElseThrow();check(pair.skillRequirements().equals(java.util.List.of(new Balance.SkillRequirement(Skill.strength,10),new Balance.SkillRequirement(Skill.dexterity,5))),"paired swords requirements");
            var master=b.abilities.stream().filter(x->x.id().endsWith("/swordmaster")).findFirst().orElseThrow();check(master.parent().endsWith("/technician"),"swordmaster prerequisite");
            var row=b.trees.stream().filter(t->t.skill()==Skill.constitution).findFirst().orElseThrow().nodes();
            for(String name:java.util.List.of("faustcore:resilient_recovery","faust_epic_compat:epicfight/stamina_pillager")){var n=row.stream().filter(x->x.ability().equals(name)).findFirst().orElseThrow();check(n.y()==health.y()&&n.x()>health.x()+4,"recovery row separated");}
            for(String name:java.util.List.of("catharsis","death_harvest")){var n=row.stream().filter(x->x.ability().endsWith("/"+name)).findFirst().orElseThrow();check(n.y()==skin.y(),"skin row aligned");}
            var harvest=row.stream().filter(n->n.ability().endsWith("/death_harvest")).findFirst().orElseThrow();
            var wind=row.stream().filter(n->n.ability().equals("faustcore:second_wind")).findFirst().orElseThrow();
            var recovery=row.stream().filter(n->n.ability().equals("faustcore:resilient_recovery")).findFirst().orElseThrow();
            var pillager=row.stream().filter(n->n.ability().endsWith("/stamina_pillager")).findFirst().orElseThrow();
            check(wind.x()==harvest.x()&&recovery.x()==harvest.x()&&pillager.x()==SkillTrees.snap(harvest.x()+4),"recovery column aligned");
            p.unlocked.add(Combat.SHIELD_EFFICIENCY);double remaining=p.stamina;check(CoreRuntime.shieldPayment(player,false)&&p.stamina==remaining,"combined discounts clamp at zero");
            for(var left:row)for(var right:row)if(!left.ability().equals(right.ability()))check(Math.abs(left.x()-right.x())>=2||Math.abs(left.y()-right.y())>=2,"constitution nodes do not overlap");
            double xp=p.track(Skill.building).xp;CoreRuntime.stat(player,net.minecraft.stats.Stats.BLOCK_MINED.get(net.minecraft.world.level.block.Blocks.STONE),1);check(p.track(Skill.building).xp==xp,"raw mining stat cannot bypass qualification");
            var intelligence=b.trees.stream().filter(t->t.skill()==Skill.intelligence).findFirst().orElseThrow().nodes();
            var cat=intelligence.stream().filter(n->n.ability().equals("faustcore:cat_scent")).findFirst().orElseThrow();
            var enchant=intelligence.stream().filter(n->n.ability().equals("faustcore:enchanting_power")).findFirst().orElseThrow();
            var revelation=intelligence.stream().filter(n->n.ability().endsWith("/revelation")).findFirst().orElseThrow();
            check(cat.x()==enchant.x()&&cat.y()==SkillTrees.snap(enchant.y()+4)&&cat.y()==revelation.y(),"cat scent alignment");
            var metabolism=row.stream().filter(n->n.ability().equals("faustcore:metabolism")).findFirst().orElseThrow();
            var endurance=row.stream().filter(n->n.ability().endsWith("/endurance")).findFirst().orElseThrow();
            check(metabolism.x()==endurance.x()&&metabolism.y()==wind.y(),"metabolism aligned with endurance and wind");
            var strength=b.trees.stream().filter(t->t.skill()==Skill.strength).findFirst().orElseThrow().nodes();
            for(var columnPair:java.util.List.of(new String[]{"bonebreaker",Combat.FIST_MASTERY},new String[]{"meteor_slam","faustcore:training_strength_damage"})){
                var node=strength.stream().filter(n->n.ability().endsWith("/"+columnPair[0])).findFirst().orElseThrow();var anchor=strength.stream().filter(n->n.ability().equals(columnPair[1])).findFirst().orElseThrow();check(node.x()==anchor.x(),"strength alignment "+columnPair[0]);
            }
            for(var tree:b.trees)for(var node:tree.nodes())check(SkillTrees.validPosition(node,tree.nodes())&&node.x()%SkillTrees.GRID==0&&node.y()%SkillTrees.GRID==0,"grid spacing "+node.ability());
            b.validate();System.out.println("Balance revision server checks passed: "+checks);
        }finally{event.getServer().halt(false);}
    }
}
