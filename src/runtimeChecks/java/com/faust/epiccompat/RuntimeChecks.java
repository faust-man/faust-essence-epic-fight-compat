package com.faust.epiccompat;
import com.faust.core.runtime.CoreRuntime;
import com.mojang.authlib.GameProfile;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.skill.*;
import java.util.UUID;

/** Optional in-game checks; excluded from the release JAR. */
@EventBusSubscriber(modid="faust_epic_compat")
public final class RuntimeChecks {
    private static int checks;
    private static void check(boolean value,String label){checks++;if(!value)throw new AssertionError(label);}
    @SubscribeEvent public static void started(ServerStartedEvent event){
        var player=FakePlayerFactory.get(event.getServer().overworld(),new GameProfile(UUID.fromString("9625401d-3174-4f98-bb52-e8002881a51b"),"CompatChecks"));
        var patch=new ServerPlayerPatch(player);
        var data=CoreRuntime.data(player);var balance=CoreRuntime.balance;
        double oldAttack=balance.stamina.get("attackCost");
        var vanillaRule=yesman.epicfight.world.gamerule.EpicFightGameRules.ALLOW_VANILLA_MELEE;
        boolean oldVanilla=vanillaRule.getRuleValue(player.level());
        try{
            balance.stamina.put("attackCost",1.0);data.stamina=20;data.fatigue=0;data.exhausted=false;
            player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
            vanillaRule.setRuleValue(player.level(),false);
            check(CoreRuntime.attack(player,false)&&data.stamina==20,"disabled vanilla melee has no stamina charge");
            Combat.strike(patch,InteractionHand.MAIN_HAND);
            check(Combat.paid(patch)&&Math.abs(data.stamina-19)<0.0001,"bare-hand payment");
            vanillaRule.setRuleValue(player.level(),true);
            check(CoreRuntime.attack(player,false)&&data.stamina==18,"enabled vanilla melee keeps normal stamina charge");data.stamina=19;
            data.unlocked.add(Combat.FIST_MASTERY);
            Combat.strike(patch,InteractionHand.MAIN_HAND);
            check(Combat.paid(patch)&&Math.abs(data.stamina-19)<0.0001,"fist mastery");
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(yesman.epicfight.registry.entries.EpicFightItems.GLOVE.get()));
            Combat.strike(patch,InteractionHand.MAIN_HAND);
            check(Combat.paid(patch)&&Math.abs(data.stamina-19)<0.0001,"glove mastery");
            data.unlocked.remove(Combat.FIST_MASTERY);Combat.strike(patch,InteractionHand.MAIN_HAND);
            check(Combat.paid(patch)&&Math.abs(data.stamina-18)<0.0001,"gloves without mastery cost stamina");
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(yesman.epicfight.registry.entries.EpicFightItems.IRON_GREATSWORD.get()));
            data.stamina=20;Combat.strike(patch,InteractionHand.MAIN_HAND);
            check(Combat.paid(patch)&&Math.abs(data.stamina-18)<0.0001,"greatsword costs double");
            data.unlocked.add(Combat.MASTERY);Combat.strike(patch,InteractionHand.MAIN_HAND);
            check(Combat.paid(patch)&&Math.abs(data.stamina-17)<0.0001,"greatsword mastery normalizes cost");
            data.unlocked.remove(Combat.MASTERY);data.stamina=1;Combat.strike(patch,InteractionHand.MAIN_HAND);
            check(!Combat.paid(patch)&&data.stamina==1,"insufficient double cost is rejected atomically");
            data.stamina=20;data.exhausted=false;
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(yesman.epicfight.registry.entries.EpicFightItems.IRON_SPEAR.get()));
            Combat.strike(patch,InteractionHand.MAIN_HAND);
            check(Combat.paid(patch)&&Math.abs(data.stamina-19)<0.0001,"spear exemption");
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.IRON_SWORD));
            Combat.strike(patch,InteractionHand.MAIN_HAND);
            check(Combat.paid(patch)&&Math.abs(data.stamina-18)<0.0001,"one-handed weapon base cost");
            player.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.SHIELD));
            Combat.strike(patch,InteractionHand.MAIN_HAND);
            check(Combat.paid(patch)&&Math.abs(data.stamina-16)<0.0001,"sword with shield costs two");
            data.unlocked.add(Combat.MASTERY);Combat.strike(patch,InteractionHand.MAIN_HAND);
            check(Combat.paid(patch)&&Math.abs(data.stamina-14)<0.0001,"strength mastery does not remove shield surcharge");
            data.unlocked.add(Combat.SHIELD_MASTERY);Combat.strike(patch,InteractionHand.MAIN_HAND);
            check(Combat.paid(patch)&&Math.abs(data.stamina-13)<0.0001,"defense mastery removes shield surcharge");
            data.unlocked.remove(Combat.SHIELD_MASTERY);data.stamina=1;Combat.strike(patch,InteractionHand.MAIN_HAND);
            check(!Combat.paid(patch)&&data.stamina==1,"shield surcharge requires full payment");
            data.stamina=20;data.exhausted=false;
            player.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.TORCH));Combat.strike(patch,InteractionHand.MAIN_HAND);
            check(Combat.paid(patch)&&Math.abs(data.stamina-19)<0.0001,"non-shield offhand has no surcharge");
            player.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);
            data.exhausted=true;Combat.strike(patch,InteractionHand.MAIN_HAND);
            check(!Combat.paid(patch),"exhaustion still blocks free attacks");data.exhausted=false;
            for(var slot:SkillSlots.values())if(slot.category()==SkillCategories.PASSIVE)check(Combat.allowedSlot(slot)==(slot==SkillSlots.PASSIVE1||slot==SkillSlots.PASSIVE2||slot==SkillSlots.PASSIVE3),"passive slot gate");
            check(patch.getPlayerSkills().getSkillContainersFor(SkillCategories.PASSIVE).size()==3,"slot selection has three entries");
            double before=data.stamina;
            Combat.collide(player,()->check(CoreRuntime.attack(player,false),"prepaid hit allowed"));
            Combat.collide(player,()->{check(CoreRuntime.attack(player,false),"first area target");check(CoreRuntime.attack(player,false),"second area target");});
            check(data.stamina==before,"prepaid hit not charged twice");check(!Combat.prepaid(player),"payment context cleared");
            try{Combat.collide(player,()->{throw new IllegalStateException("test");});}catch(IllegalStateException expected){}
            check(!Combat.prepaid(player),"context cleared after exception");
            Combat.begin(patch);check(!Combat.paid(patch),"new animation invalidates old payment");
            check(balance.abilities.stream().anyMatch(a->a.id().equals(Combat.MASTERY)),"two-handed node migrated");
            check(balance.abilities.stream().anyMatch(a->a.id().equals(Combat.FIST_MASTERY)),"fist node migrated");
            check(balance.abilities.stream().anyMatch(a->a.id().equals(Combat.SHIELD_MASTERY)&&a.skill()==com.faust.core.progression.Skill.defense),"shield node migrated into defense");
            System.out.println("FAUST COMPAT RUNTIME CHECKS PASSED: "+checks);
            data.stamina=20;data.exhausted=false;
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.IRON_SWORD));player.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.IRON_SWORD));
            Combat.strike(patch,InteractionHand.MAIN_HAND);check(data.stamina==19,"dual sword base cost");
            data.unlocked.add(Combat.SWORD_MASTERY);Combat.strike(patch,InteractionHand.MAIN_HAND);check(data.stamina==18.5,"dual sword mastery");
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(yesman.epicfight.registry.entries.EpicFightItems.IRON_DAGGER.get()));player.setItemInHand(InteractionHand.OFF_HAND,player.getMainHandItem().copy());
            Combat.strike(patch,InteractionHand.MAIN_HAND);check(data.stamina==17.5,"dual dagger base cost independent of sword mastery");
            data.unlocked.add(Combat.DAGGER_MASTERY);Combat.strike(patch,InteractionHand.MAIN_HAND);check(data.stamina==17,"dual dagger mastery");
            var attached=yesman.epicfight.world.capabilities.EpicFightCapabilities.getEntityPatch(player,ServerPlayerPatch.class);
            check(attached!=null,"attached patch available");
            var forbidden=yesman.epicfight.registry.EpicFightRegistries.SKILL.get(net.minecraft.resources.ResourceLocation.parse("epicfight:forbidden_strength"));
            data.unlocked.add(Compat.nodeId(forbidden));check(attached.getSkill(SkillSlots.PASSIVE1).setSkill(forbidden),"equip forbidden strength");
            data.stamina=2;data.exhausted=true;player.setHealth(20);double fatigue=data.fatigue;
            check(Forbidden.mode(player),"forbidden mode during exhaustion");check(CoreRuntime.canAct(player),"can act during exhaustion");
            check(CoreRuntime.pay(player,2)&&player.getHealth()==18&&data.stamina==2,"full 1:1 health payment without spending residual stamina");
            attached.setStamina(attached.getStamina()-3);check(player.getHealth()==15&&data.stamina==2,"EF skill health payment");
            check(data.fatigue==fatigue,"health payments add no fatigue");
            check(Forbidden.afford(player,15),"lethal cost is permitted");
            CoreRuntime.tick(player);check(data.stamina>2,"stamina regenerates while health pays");
            data.stamina=0;data.exhausted=true;player.setOnGround(true);player.setSprinting(true);
            CoreRuntime.stat(player,net.minecraft.stats.Stats.CUSTOM.get(net.minecraft.stats.Stats.SPRINT_ONE_CM),100);
            float beforeRun=player.getHealth();CoreRuntime.tick(player);
            check(player.getHealth()<beforeRun,"sprint at zero stamina pays health");check(data.stamina>0,"sprint at zero stamina still regenerates stamina");
            attached.getSkill(SkillSlots.PASSIVE1).setSkill(null);check(!Forbidden.mode(player),"unequipping restores exhaustion block");
            check(balance.abilities.stream().anyMatch(a->a.id().endsWith("/adaptive_skin")&&a.skill()==com.faust.core.progression.Skill.constitution),"adaptive skin relocated");
            check(balance.abilities.stream().anyMatch(a->a.id().endsWith("/revelation")&&a.skill()==com.faust.core.progression.Skill.intelligence),"revelation relocated");
            data.stamina=0.5;data.exhausted=false;
            var combo=attached.getSkill(SkillSlots.COMBO_ATTACKS);combo.setSkill(yesman.epicfight.registry.entries.EpicFightSkills.COMBO_ATTACKS.get());
            for(int i=0;i<8;i++){check(!Combat.canStrike(attached),"fractional stamina cannot start queued attack");check(!combo.canUse(attached,new yesman.epicfight.api.event.types.player.SkillCastEvent(attached,combo,new net.minecraft.nbt.CompoundTag())),"skill container rejects fractional queued attack");Combat.begin(attached);Combat.strike(attached,InteractionHand.MAIN_HAND);check(!Combat.paid(attached)&&data.stamina==0.5,"fractional repeated strike rejected");}
            check(attached.getSkill(SkillSlots.PASSIVE1).setSkill(forbidden),"re-equip forbidden");player.setHealth(0.5f);data.exhausted=true;
            vanillaRule.setRuleValue(player.level(),false);
            attached.toVanillaMode(false);
            double unchanged=data.stamina;float health=player.getHealth();double oldFatigue=data.fatigue;
            for(boolean air:new boolean[]{false,true})for(int i=0;i<5;i++)check(CoreRuntime.attack(player,air),"disabled vanilla melee is not billed");
            check(data.stamina==unchanged&&player.getHealth()==health&&data.fatigue==oldFatigue,"disabled vanilla melee never spends stamina health or fatigue");
            vanillaRule.setRuleValue(player.level(),true);
            double residual=data.stamina;check(CoreRuntime.pay(player,1),"lethal health payment accepted");
            check(player.isDeadOrDying()&&player.getHealth()==0&&data.stamina==residual,"health payment kills and preserves stamina");
            check(!Forbidden.pay(player,1),"dead player cannot pay again");
            System.out.println("FAUST COMPAT COMPLETE RUNTIME CHECKS PASSED: "+checks);
        }finally{vanillaRule.setRuleValue(player.level(),oldVanilla);balance.stamina.put("attackCost",oldAttack);event.getServer().halt(false);}
    }
}
