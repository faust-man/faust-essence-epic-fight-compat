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
            check(CoreRuntime.shieldPayment(player,false)&&p.stamina==7,"melee discount");
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
            b.validate();System.out.println("Balance revision server checks passed: "+checks);
        }finally{event.getServer().halt(false);}
    }
}
