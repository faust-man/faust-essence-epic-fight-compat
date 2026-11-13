package com.faust.epiccompat;
import com.faust.essence.api.Skill;

import com.faust.core.progression.*;
import com.faust.core.runtime.*;
import net.minecraft.world.item.*;
import net.minecraft.world.InteractionHand;
import yesman.epicfight.skill.*;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.damagesource.*;
@net.neoforged.fml.common.EventBusSubscriber(modid="faust_epic_compat")
public final class LongswordShieldRuntime {
 private static void check(boolean v,String why){if(!v)throw new AssertionError(why);}
 @net.neoforged.bus.api.SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.HIGH)
 public static void started(net.neoforged.neoforge.event.server.ServerStartedEvent event) throws Exception {
  var player=net.neoforged.neoforge.common.util.FakePlayerFactory.get(event.getServer().overworld(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"ShieldMasterChecks"));player.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.SURVIVAL);player.setOnGround(true);
  var b=CoreRuntime.balance;var p=CoreRuntime.data(player);p.max(b);p.unlocked.remove(WeaponMastery.LONG);var a=b.abilities.stream().filter(x->x.id().equals(WeaponMastery.LONG)).findFirst().orElseThrow();
  p.setCounter(WeaponMastery.USES,50,b);p.track(com.faust.essence.api.Skill.valueOf("defense")).level=15;p.track(com.faust.essence.api.Skill.valueOf("strength")).level=9;check(!p.canUnlock(a,b),"strength9 cannot learn longsword mastery");p.track(com.faust.essence.api.Skill.valueOf("strength")).level=10;p.track(com.faust.essence.api.Skill.valueOf("defense")).level=14;check(!p.canUnlock(a,b),"defense14 cannot learn longsword mastery");p.track(com.faust.essence.api.Skill.valueOf("defense")).level=15;check(p.unlock(a.id(),b),"defense15 plus strength10 learns mastery");
  var patch=EpicFightCapabilities.getEntityPatch(player,ServerPlayerPatch.class);patch.toEpicFightMode(false);
  player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(yesman.epicfight.registry.entries.EpicFightItems.IRON_LONGSWORD.get()));player.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.SHIELD));
  var skill=WeaponMastery.innate(player,player.getMainHandItem());check(skill==yesman.epicfight.registry.entries.EpicFightSkills.SHARP_STAB.get(),"shield selects native Sharp Stab");check(WeaponMastery.longSword(player,player.getMainHandItem()),"shield style recognized as longsword");
  var source=new EpicFightDamageSource(player.damageSources().playerAttack(player)).setUsedItem(player.getMainHandItem()).setBaseArmorNegation(20).setBaseImpact(3);source.addRuntimeTag(EpicFightDamageTypeTags.WEAPON_INNATE);
  check(source.calculateDamageAgainst(player,null,4)==12&&source.calculateImpact()==9&&source.calculateArmorNegation()==50,"shield innate damage3 impact3 armor50");
  check(source.calculateDamageAgainst(player,null,4)==12,"repeat calculation never compounds multiplier");
  var ordinary=new EpicFightDamageSource(player.damageSources().playerAttack(player)).setUsedItem(player.getMainHandItem()).setBaseArmorNegation(20).setBaseImpact(3);check(ordinary.calculateDamageAgainst(player,null,4)==4&&ordinary.calculateImpact()==3&&ordinary.calculateArmorNegation()==40,"ordinary attack retains existing armor bonus only");
  p.disabled.add(WeaponMastery.LONG);check(source.calculateDamageAgainst(player,null,4)==4&&source.calculateImpact()==3&&source.calculateArmorNegation()==20,"disabled mastery removes shield bonuses");p.disabled.remove(WeaponMastery.LONG);
  p.unlocked.remove(WeaponMastery.LONG);var innate=patch.getSkill(SkillSlots.WEAPON_INNATE);innate.setSkill(skill);innate.setResource(innate.getMaxResource());innate.setStack(1);p.setCounter(WeaponMastery.USES,0,b);check(innate.requestCasting(patch,new net.minecraft.nbt.CompoundTag()),"real shield innate cast");check(p.conditionCount(a,a.actionConditions().getFirst())==1,"shield ability counts toward mastery practice");p.unlocked.add(WeaponMastery.LONG);
  player.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);check(!WeaponMastery.shieldSpecial(source)&&source.calculateDamageAgainst(player,null,4)==4&&source.calculateImpact()==3&&source.calculateArmorNegation()==40,"without shield longsword retains previous behavior");
  player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.IRON_SWORD));player.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.SHIELD));var swordSource=new EpicFightDamageSource(player.damageSources().playerAttack(player)).setUsedItem(player.getMainHandItem()).setBaseImpact(3);swordSource.addRuntimeTag(EpicFightDamageTypeTags.WEAPON_INNATE);check(!WeaponMastery.shieldSpecial(swordSource),"other weapon innate is not boosted");
  var dispatcher=event.getServer().getCommands().getDispatcher();var commandSource=player.createCommandSourceStack().withPermission(2);
  check(dispatcher.execute("essence reset",commandSource)==1,"reset without target succeeds for player");check(CoreRuntime.data(player).unlocked.isEmpty()&&CoreRuntime.data(player).track(com.faust.essence.api.Skill.valueOf("strength")).level==0,"self reset clears progression");
  CoreRuntime.data(player).max(b);check(dispatcher.execute("essence reset @s",commandSource)==1&&CoreRuntime.data(player).unlocked.isEmpty(),"explicit self reset still works");
  boolean denied=false;try{dispatcher.execute("essence reset",event.getServer().createCommandSourceStack());}catch(com.mojang.brigadier.exceptions.CommandSyntaxException expected){denied=true;}check(denied,"console must specify a target");
  CoreRuntime.data(player).max(b);denied=false;try{dispatcher.execute("essence reset",commandSource.withPermission(0));}catch(com.mojang.brigadier.exceptions.CommandSyntaxException expected){denied=true;}check(denied&&!CoreRuntime.data(player).unlocked.isEmpty(),"reset retains operator permission gate");
  System.out.println("Longsword shield and self reset gameplay checks passed");
 }
}
