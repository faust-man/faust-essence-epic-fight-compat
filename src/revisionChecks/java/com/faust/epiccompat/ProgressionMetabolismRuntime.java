package com.faust.epiccompat;
import com.faust.core.runtime.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.*;
import net.minecraft.core.component.DataComponents;
@net.neoforged.fml.common.EventBusSubscriber(modid="faust_epic_compat")
public final class ProgressionMetabolismRuntime {
 private static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
 @net.neoforged.bus.api.SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.HIGH)
 public static void started(net.neoforged.neoforge.event.server.ServerStartedEvent event)throws Exception{
  var level=event.getServer().overworld();var player=net.neoforged.neoforge.common.util.FakePlayerFactory.get(level,new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"MetabolismChecks"));
  player.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.SURVIVAL);var p=CoreRuntime.data(player);var b=CoreRuntime.balance;p.max(b);
  var original=new MobEffectInstance(MobEffects.POISON,101,0);var changed=Metabolism.potion(player,original);check(changed.getDuration()==50&&changed.getAmplifier()==1&&original.getDuration()==101,"negative potion transformed without mutating source");
  var infinite=new MobEffectInstance(MobEffects.MOVEMENT_SPEED,-1,0);check(Metabolism.potion(player,infinite)==infinite,"infinite unchanged");
  var instant=new MobEffectInstance(MobEffects.HEAL,1,0);check(Metabolism.potion(player,instant)==instant,"instant unchanged");
  b.metabolismExcludedEffects.add("minecraft:poison");check(Metabolism.potion(player,original)==original,"excluded unchanged");b.metabolismExcludedEffects.clear();
  p.disabled.add("faustcore:metabolism");check(Metabolism.potion(player,original)==original,"disabled unchanged");p.disabled.remove("faustcore:metabolism");
  var rule=level.getGameRules().getRule(net.minecraft.world.level.GameRules.RULE_NATURAL_REGENERATION);boolean old=rule.get();rule.set(false,event.getServer());
  try{
   var stack=new ItemStack(Items.POTION);stack.set(DataComponents.POTION_CONTENTS,new PotionContents(java.util.Optional.empty(),java.util.Optional.empty(),java.util.List.of(original)));stack.finishUsingItem(level,player);
   check(player.getEffect(MobEffects.POISON).getDuration()==50&&player.getEffect(MobEffects.POISON).getAmplifier()==1,"actual drink with natural regeneration disabled");player.removeAllEffects();
   player.addEffect(new MobEffectInstance(original));check(player.getEffect(MobEffects.POISON).getDuration()==101&&player.getEffect(MobEffects.POISON).getAmplifier()==0,"non-potion application unchanged");player.removeAllEffects();
   var arrow=new net.minecraft.world.entity.projectile.Arrow(net.minecraft.world.entity.EntityType.ARROW,level);arrow.addEffect(original);
   var hit=arrow.getClass().getDeclaredMethod("doPostHurtEffects",net.minecraft.world.entity.LivingEntity.class);hit.setAccessible(true);hit.invoke(arrow,player);
   check(player.getEffect(MobEffects.POISON).getDuration()==50&&player.getEffect(MobEffects.POISON).getAmplifier()==1,"actual tipped arrow");player.removeAllEffects();
   arrow.addEffect(infinite);hit.invoke(arrow,player);check(player.getEffect(MobEffects.MOVEMENT_SPEED).isInfiniteDuration()&&player.getEffect(MobEffects.MOVEMENT_SPEED).getAmplifier()==0,"infinite arrow unchanged");player.removeAllEffects();
   player.setPos(level.getSharedSpawnPos().getX()+0.5,level.getSharedSpawnPos().getY(),level.getSharedSpawnPos().getZ()+0.5);level.addNewPlayer(player);
   try{
    var splash=new net.minecraft.world.entity.projectile.ThrownPotion(net.minecraft.world.entity.EntityType.POTION,level);splash.setPos(player.position());
    var apply=splash.getClass().getDeclaredMethod("applySplash",Iterable.class,net.minecraft.world.entity.Entity.class);apply.setAccessible(true);apply.invoke(splash,java.util.List.of(original),null);
    check(player.getEffect(MobEffects.POISON)!=null&&player.getEffect(MobEffects.POISON).getDuration()==50&&player.getEffect(MobEffects.POISON).getAmplifier()==1,"actual splash potion");player.removeAllEffects();
    var cloud=new net.minecraft.world.entity.AreaEffectCloud(net.minecraft.world.entity.EntityType.AREA_EFFECT_CLOUD,level);cloud.setPos(player.position());cloud.setRadius(3);cloud.setWaitTime(0);cloud.setPotionContents(new PotionContents(java.util.Optional.empty(),java.util.Optional.empty(),java.util.List.of(original)));cloud.tickCount=5;cloud.tick();
    check(player.getEffect(MobEffects.POISON)!=null&&player.getEffect(MobEffects.POISON).getDuration()==50&&player.getEffect(MobEffects.POISON).getAmplifier()==1,"actual lingering potion");player.removeAllEffects();
   }finally{level.removePlayerImmediately(player,net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);}
  }finally{rule.set(old,event.getServer());}
  var patch=yesman.epicfight.world.capabilities.EpicFightCapabilities.getEntityPatch(player,yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch.class);patch.toEpicFightMode(false);player.setOnGround(true);
  var skill=(yesman.epicfight.skill.mover.DemolitionLeapSkill)yesman.epicfight.registry.EpicFightRegistries.SKILL.get(net.minecraft.resources.ResourceLocation.parse("epicfight:demolition_leap"));var container=patch.getSkill(yesman.epicfight.skill.SkillSlots.MOVER);container.setSkill(skill);
  p.stamina=10;p.exhausted=false;check(patch.startSkillHolding(skill),"start leap charging");skill.holdTick(container);check(patch.isHoldingSkill(skill),"healthy leap keeps charging");
  p.stamina=0.5;p.exhausted=false;patch.setChargingTicks(0);skill.holdTick(container);check(p.exhausted&&!patch.isHoldingAny(),"final leap payment immediately cancels charge");
  p.stamina=5;p.exhausted=true;patch.startSkillHolding(skill);skill.holdTick(container);check(!patch.isHoldingAny(),"exhausted partial recovery cannot freeze charge");
  System.out.println("Progression metabolism and demolition gameplay checks passed");
 }
}
