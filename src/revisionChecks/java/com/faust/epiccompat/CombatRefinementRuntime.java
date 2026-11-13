package com.faust.epiccompat;
import com.faust.core.runtime.CoreRuntime;
import net.minecraft.world.item.*;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.skill.*;
@net.neoforged.fml.common.EventBusSubscriber(modid="faust_epic_compat")
public final class CombatRefinementRuntime {
 private static void check(boolean ok,String why){if(!ok)throw new AssertionError(why);}
 @net.neoforged.bus.api.SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.HIGH)
 public static void started(net.neoforged.neoforge.event.server.ServerStartedEvent e){
  var player=net.neoforged.neoforge.common.util.FakePlayerFactory.get(e.getServer().overworld(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"RefinementChecks"));player.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.SURVIVAL);player.setOnGround(true);var patch=EpicFightCapabilities.getEntityPatch(player,ServerPlayerPatch.class);var p=CoreRuntime.data(player);var b=CoreRuntime.balance;p.max(b);patch.toEpicFightMode(false);player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(Items.IRON_SWORD));var container=patch.getSkill(SkillSlots.GUARD);
  for(String name:java.util.List.of("guard","parrying","impact_guard")){
   var skill=yesman.epicfight.registry.EpicFightRegistries.SKILL.get(net.minecraft.resources.ResourceLocation.parse("epicfight:"+name));container.setSkill(skill);
   for(int i=0;i<3;i++){p.stamina=10;p.exhausted=false;check(container.requestHold(patch,new net.minecraft.nbt.CompoundTag()),"valid press starts "+name);check(Math.abs(p.stamina-9)<0.001,"each guard press costs exactly1 "+name);patch.resetHolding();}
   p.stamina=0.5;p.exhausted=false;check(!container.requestHold(patch,new net.minecraft.nbt.CompoundTag())&&p.stamina==0.5,"fractional stamina cannot begin guard "+name);
  }
  p.stamina=5;p.exhausted=false;var animation=patch.getAnimator().getPlayerFor(null);animation.setPlayAnimation(yesman.epicfight.gameasset.Animations.GREATSWORD_AUTO1);animation.setElapsedTime(0.1F);Forbidden.TICK.set(player);try{p.secondWind(1,b);check(p.stamina==5,"no Second Wind during attack animation");animation.setPlayAnimation(yesman.epicfight.gameasset.Animations.BIPED_IDLE);p.secondWind(1,b);check(p.stamina>5,"Second Wind returns after animation");}finally{Forbidden.TICK.remove();}
  player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(yesman.epicfight.registry.entries.EpicFightItems.IRON_LONGSWORD.get()));var longSource=new yesman.epicfight.world.damagesource.EpicFightDamageSource(player.damageSources().playerAttack(player)).setUsedItem(player.getMainHandItem()).setBaseArmorNegation(20);check(longSource.calculateArmorNegation()==40,"longsword armor negation2x");p.disabled.add(WeaponMastery.LONG);check(longSource.calculateArmorNegation()==20,"disabled longsword has no bonus");p.disabled.remove(WeaponMastery.LONG);
  player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(yesman.epicfight.registry.entries.EpicFightItems.IRON_GREATSWORD.get()));var speed=player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);var penalty=net.minecraft.resources.ResourceLocation.withDefaultNamespace("greatsword_movement_speed_penalty");speed.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(penalty,-0.02,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE));double penalized=speed.getValue();WeaponMastery.refreshSpeed(player);check(Math.abs(speed.getValue()-penalized-0.02)<0.001,"greatsword item movement penalty cancelled");WeaponMastery.refreshSpeed(player);check(Math.abs(speed.getValue()-penalized-0.02)<0.001,"speed compensation does not accumulate");p.disabled.add(WeaponMastery.GREAT);WeaponMastery.refreshSpeed(player);check(Math.abs(speed.getValue()-penalized)<0.001,"disabling mastery restores weapon penalty");
  var great=b.abilities.stream().filter(a->a.id().equals(WeaponMastery.GREAT)).findFirst().orElseThrow();check(great.amount()==250&&great.parent().equals(Combat.MASTERY),"250 swings and two handed grip dependency");
  System.out.println("Combat refinement gameplay checks passed");
 }
}
