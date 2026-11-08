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
            check(Combat.paid(patch)&&data.stamina==0&&data.exhausted,"last strike consumes fractional reserve and exhausts");
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
            check(Combat.paid(patch)&&data.stamina==0&&data.exhausted,"last shield weapon strike exhausts");
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
            var shieldNode=balance.abilities.stream().filter(a->a.id().equals(Combat.SHIELD_MASTERY)).findFirst().orElseThrow();
            check(shieldNode.all().contains(Combat.SHIELD_EFFICIENCY),"shield combat requires economical shield block");
            var shieldProgress=new com.faust.core.progression.Progress();shieldProgress.track(com.faust.core.progression.Skill.defense).level=20;
            check(!shieldProgress.canUnlock(shieldNode,balance),"shield combat locked without prerequisite");
            shieldProgress.unlocked.add(Combat.SHIELD_EFFICIENCY);shieldProgress.counters.put(CombatPractice.SHIELD_HITS,100.0);check(shieldProgress.canUnlock(shieldNode,balance),"shield combat available after prerequisite");
            shieldProgress.unlock(shieldNode.id(),balance);check(shieldProgress.revoke(Combat.SHIELD_EFFICIENCY,balance).contains(shieldNode.id()),"shield dependency revoked with parent");
            var oldShield=com.faust.core.progression.Balance.JSON.toJsonTree(shieldNode).getAsJsonObject();oldShield.addProperty("parent","");
            var migratedShield=Compat.relocate(com.faust.core.progression.Balance.JSON.fromJson(oldShield,com.faust.core.progression.Balance.Ability.class));
            check(migratedShield.all().contains(Combat.SHIELD_EFFICIENCY),"existing shield config gains prerequisite");
            check(Compat.relocate(migratedShield).equals(migratedShield),"shield dependency migration idempotent");

            var fixedProgress=new com.faust.core.progression.Progress();fixedProgress.max(balance);
            for(var ability:balance.abilities)if(ability.id().startsWith("faust_epic_compat:")){
                check(ability.toggleable()==Compat.isMastery(ability.id()),"custom masteries toggle, Epic Fight nodes equip");
            }
            guardAndRollChecks(balance,player);
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
            check(Combat.canStrike(attached),"fractional reserve allows final strike");Combat.begin(attached);Combat.strike(attached,InteractionHand.MAIN_HAND);check(Combat.paid(attached)&&data.stamina==0&&data.exhausted,"final strike exhausts");check(!Combat.canStrike(attached),"subsequent strike blocked");
            check(attached.getSkill(SkillSlots.PASSIVE1).setSkill(forbidden),"re-equip forbidden");player.setHealth(0.5f);data.exhausted=true;
            vanillaRule.setRuleValue(player.level(),false);
            attached.toVanillaMode(false);
            double unchanged=data.stamina;float health=player.getHealth();double oldFatigue=data.fatigue;
            for(boolean air:new boolean[]{false,true})for(int i=0;i<5;i++)check(CoreRuntime.attack(player,air),"disabled vanilla melee is not billed");
            check(data.stamina==unchanged&&player.getHealth()==health&&data.fatigue==oldFatigue,"disabled vanilla melee never spends stamina health or fatigue");
            vanillaRule.setRuleValue(player.level(),true);
            player.setHealth(20);data.exhausted=false;data.stamina=20;player.setSprinting(false);
            player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);player.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);
            double fists=data.counters.getOrDefault(CombatPractice.FISTS,0.0);
            victim(player).hurt(player.damageSources().playerAttack(player),100);check(data.counters.getOrDefault(CombatPractice.FISTS,0.0)==fists+1,"actual fist kill counted");
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.IRON_SWORD));
            double swords=data.counters.getOrDefault(CombatPractice.SWORDS,0.0);double dual=data.counters.getOrDefault(CombatPractice.DUAL,0.0);
            victim(player).hurt(player.damageSources().playerAttack(player),100);check(data.counters.getOrDefault(CombatPractice.SWORDS,0.0)==swords+1&&data.counters.getOrDefault(CombatPractice.DUAL,0.0)==dual,"single sword kill classified");
            player.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.IRON_SWORD));
            victim(player).hurt(player.damageSources().playerAttack(player),100);check(data.counters.getOrDefault(CombatPractice.DUAL,0.0)==dual+1,"dual sword kill counted");
            player.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(yesman.epicfight.registry.entries.EpicFightItems.IRON_GREATSWORD.get()));
            double two=data.counters.getOrDefault(CombatPractice.TWO_HAND,0.0);victim(player).hurt(player.damageSources().playerAttack(player),100);check(data.counters.getOrDefault(CombatPractice.TWO_HAND,0.0)==two+1,"two-handed kill counted");
            double prior=data.counters.getOrDefault(CombatPractice.SWORDS,0.0);var survivor=victim(player);survivor.setHealth(10);survivor.hurt(player.damageSources().playerAttack(player),1);check(data.counters.getOrDefault(CombatPractice.SWORDS,0.0)==prior,"non-lethal hits do not count as kills");
            var arrow=net.minecraft.world.entity.EntityType.ARROW.create(player.serverLevel());victim(player).hurt(player.damageSources().arrow(arrow,player),100);check(data.counters.getOrDefault(CombatPractice.SWORDS,0.0)==prior,"projectile kills do not count for melee weapons");
            var recipient=new net.neoforged.neoforge.common.util.FakePlayer(player.serverLevel(),new GameProfile(UUID.fromString("64f1e864-a644-448b-bbce-77be313e0fff"),"DamageChecks")){@Override public boolean isInvulnerableTo(net.minecraft.world.damagesource.DamageSource source){return false;}};
            recipient.setHealth(20);recipient.hurt(recipient.damageSources().genericKill(),0.5f);
            check(CoreRuntime.data(recipient).counters.getOrDefault(CombatPractice.DAMAGE,0.0)==0.5,"fractional incoming damage counted");
            player.setHealth(0.5f);data.exhausted=true;
            double residual=data.stamina;check(CoreRuntime.pay(player,1),"lethal health payment accepted");
            check(player.isDeadOrDying()&&player.getHealth()==0&&data.stamina==residual,"health payment kills and preserves stamina");
            check(!Forbidden.pay(player,1),"dead player cannot pay again");
            System.out.println("FAUST COMPAT COMPLETE RUNTIME CHECKS PASSED: "+checks);
        }finally{vanillaRule.setRuleValue(player.level(),oldVanilla);balance.stamina.put("attackCost",oldAttack);event.getServer().halt(false);}
    }
    private static void guardAndRollChecks(com.faust.core.progression.Balance balance,net.minecraft.server.level.ServerPlayer existing){
        String prefix="faust_epic_compat:epicfight/";
        var guard=balance.abilities.stream().filter(a->a.id().equals(prefix+"guard")).findFirst().orElseThrow();
        var impact=balance.abilities.stream().filter(a->a.id().equals(prefix+"impact_guard")).findFirst().orElseThrow();
        var parry=balance.abilities.stream().filter(a->a.id().equals(prefix+"parrying")).findFirst().orElseThrow();
        var step=balance.abilities.stream().filter(a->a.id().equals(prefix+"step")).findFirst().orElseThrow();
        var progress=new com.faust.core.progression.Progress();for(var skill:com.faust.core.progression.Skill.values())progress.track(skill).level=20;
        progress.counters.put("blocks",9.0);check(!progress.canUnlock(guard,balance),"guard needs ten shield blocks");progress.counters.put("blocks",10.0);check(progress.unlock(guard.id(),balance),"ten shield blocks unlock guard");
        progress.counters.put("blocks",99.0);check(!progress.canUnlock(impact,balance),"impact guard needs 100 blocks");progress.counters.put("blocks",100.0);check(progress.canUnlock(impact,balance),"impact guard available at 100 blocks");
        progress.unlocked.remove(guard.id());check(!progress.canUnlock(impact,balance),"impact guard requires learned guard");check(!progress.canUnlock(parry,balance),"parrying requires learned guard");
        progress.unlocked.add(guard.id());check(!progress.canUnlock(parry,balance),"parrying requires technician too");progress.unlocked.add(prefix+"technician");check(progress.canUnlock(parry,balance),"parrying requires both learned skills");
        progress.counters.put(CombatPractice.ROLLS,100.0);check(!progress.canUnlock(step,balance),"step requires learned roll");progress.unlocked.add(prefix+"roll");progress.counters.put(CombatPractice.ROLLS,99.0);check(!progress.canUnlock(step,balance),"step rejects 99 rolls");progress.counters.put(CombatPractice.ROLLS,100.0);check(progress.unlock(step.id(),balance),"step available after 100 rolls");check(progress.revoke(prefix+"roll",balance).contains(step.id()),"revoking roll revokes step");
        for(var ability:java.util.List.of(guard,impact,parry,step))check(Compat.relocate(ability).equals(ability),"new conditions migration idempotent");
        var training=FakePlayerFactory.get(existing.serverLevel(),new GameProfile(UUID.fromString("fb27f059-9775-469d-9f2f-a4c852bc4548"),"GuardRollChecks"));
        var trainingPatch=new ServerPlayerPatch(training);trainingPatch.onConstructed(training);var trainingData=CoreRuntime.data(training);trainingData.max(balance);
        var registry=yesman.epicfight.registry.EpicFightRegistries.SKILL;
        var impactSkill=registry.get(net.minecraft.resources.ResourceLocation.parse("epicfight:impact_guard"));var parrySkill=registry.get(net.minecraft.resources.ResourceLocation.parse("epicfight:parrying"));
        check(impactSkill.getPriorSkill()==null&&parrySkill.getPriorSkill()==null,"equipped guard prior removed for both skills");
        var guardSlot=trainingPatch.getSkill(SkillSlots.GUARD);guardSlot.setSkill(null);check(guardSlot.setSkill(impactSkill),"equip impact guard without equipping guard first");guardSlot.setSkill(null);check(guardSlot.setSkill(parrySkill),"equip parrying without equipping guard first");guardSlot.setSkill(null);
        var roll=registry.get(net.minecraft.resources.ResourceLocation.parse("epicfight:roll"));var stepSkill=registry.get(net.minecraft.resources.ResourceLocation.parse("epicfight:step"));var dodgeSlot=trainingPatch.getSkill(SkillSlots.DODGE);check(dodgeSlot.setSkill(roll),"equip roll for practice");
        var args=new net.minecraft.nbt.CompoundTag();args.putInt("direction",0);args.putFloat("yRot",0);
        double rolls=trainingData.counters.getOrDefault(CombatPractice.ROLLS,0.0);roll.executeOnServer(dodgeSlot,args);check(trainingData.counters.getOrDefault(CombatPractice.ROLLS,0.0)==rolls+1,"actual roll execution counted once");
        dodgeSlot.setSkill(stepSkill);stepSkill.executeOnServer(dodgeSlot,args);check(trainingData.counters.getOrDefault(CombatPractice.ROLLS,0.0)==rolls+1,"step execution does not count as roll");
        dodgeSlot.setSkill(roll);trainingData.unlocked.remove(Compat.nodeId(roll));check(!dodgeSlot.requestCasting(trainingPatch,args),"unlearned roll request denied");
        check(trainingData.counters.getOrDefault(CombatPractice.ROLLS,0.0)==rolls+1,"denied roll attempt does not count");trainingData.unlocked.add(Compat.nodeId(roll));
        var zombie=net.minecraft.world.entity.EntityType.ZOMBIE.create(training.serverLevel());var income=new yesman.epicfight.api.event.types.entity.TakeDamageEvent.Income(trainingPatch,training.damageSources().mobAttack(zombie),2);
        double blocks=trainingData.counters.getOrDefault("blocks",0.0);((yesman.epicfight.skill.guard.GuardSkill)registry.get(net.minecraft.resources.ResourceLocation.parse("epicfight:guard"))).dealEvent(trainingPatch,income,false);
        check(trainingData.counters.getOrDefault("blocks",0.0)==blocks+1,"successful Epic Fight guard counts block");
        check(com.faust.core.progression.Progress.parse(trainingData.json(),balance).counters.getOrDefault(CombatPractice.ROLLS,0.0)==rolls+1,"roll practice survives save/load");
        var attached=yesman.epicfight.world.capabilities.EpicFightCapabilities.getEntityPatch(training,ServerPlayerPatch.class);check(attached!=null,"attached patch for guard recovery");
        for(String name:java.util.List.of("guard","impact_guard")){
            var skill=registry.get(net.minecraft.resources.ResourceLocation.parse("epicfight:"+name));attached.getSkill(SkillSlots.GUARD).setSkill(skill);trainingData.stamina=5;trainingData.fatigue=0;trainingData.exhausted=false;
            check(attached.startSkillHolding((yesman.epicfight.skill.modules.HoldableSkill)skill),"start holding "+name);
            check(GuardRecovery.blocked(training),"guard recovery suppression detected "+name);CoreRuntime.tick(training);
            check(trainingData.stamina==5,"holding prevents regeneration and Second Wind: "+name);
            trainingData.stamina=0.5;trainingData.exhausted=false;
            check(CoreRuntime.shieldPayment(training)&&trainingData.stamina==0&&trainingData.exhausted,"fractional shield block pays remainder");
            check(!CoreRuntime.canAct(training),"exhaustion prevents shield stance");
            check(!((yesman.epicfight.skill.guard.GuardSkill)skill).canExecute(attached.getSkill(SkillSlots.GUARD)),"exhaustion prevents weapon guard");
            check(!GuardRecovery.blocked(training),"held exhausted guard permits recovery");
            trainingData.stamina=5;trainingData.exhausted=false;
            attached.resetHolding();CoreRuntime.tick(training);check(trainingData.stamina>5,"releasing restores regeneration: "+name);
        }
        var pillager=registry.get(net.minecraft.resources.ResourceLocation.parse("epicfight:stamina_pillager"));
        var passive=attached.getSkill(SkillSlots.PASSIVE1);passive.setSkill(pillager);
        trainingData.stamina=0;trainingData.exhausted=true;double fatigue=trainingData.fatigue;
        try{
            var callback=java.util.Arrays.stream(pillager.getClass().getDeclaredMethods()).filter(m->m.getName().equals("lambda$onInitiate$0")).findFirst().orElseThrow();callback.setAccessible(true);
            callback.invoke(pillager,passive,new yesman.epicfight.api.event.types.entity.KillEntityEvent(attached,zombie,training.damageSources().playerAttack(training)));
            check(trainingData.stamina>0&&trainingData.stamina<=2,"stamina pillager restores shared pool after kill");
            check(trainingData.fatigue==fatigue,"pillager recovery does not charge fatigue");
        }catch(ReflectiveOperationException ex){throw new AssertionError(ex);}
        passive.setSkill(null);
        attached.getSkill(SkillSlots.GUARD).setSkill(parrySkill);attached.startSkillHolding((yesman.epicfight.skill.modules.HoldableSkill)parrySkill);check(!GuardRecovery.blocked(training),"parrying unchanged by guard-only recovery rule");attached.resetHolding();

    }
    private static net.minecraft.world.entity.animal.Pig victim(net.minecraft.server.level.ServerPlayer player){var pig=net.minecraft.world.entity.EntityType.PIG.create(player.serverLevel());pig.setHealth(2);return pig;}
}
