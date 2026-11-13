package com.faust.epiccompat;
import com.faust.essence.api.*;
import com.faust.core.runtime.CoreRuntime;
@net.neoforged.fml.common.EventBusSubscriber(modid="faust_epic_compat")
public final class EssenceApiRuntime {
 static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
 @net.neoforged.bus.api.SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.HIGH)
 public static void started(net.neoforged.neoforge.event.server.ServerStartedEvent event)throws Exception{
  var player=net.neoforged.neoforge.common.util.FakePlayerFactory.get(event.getServer().overworld(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"PublicApiChecks"));player.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.SURVIVAL);
  check(EssenceApi.available(),"Core API backend available");var before=EssenceApi.snapshot(player);var changes=new java.util.concurrent.atomic.AtomicInteger();EssenceApi.onStateChange(change->{if(change.player().equals(player.getUUID()))changes.incrementAndGet();});CoreRuntime.sync(player);
  EssenceApi.grantExperience(player,Skill.valueOf("strength"),10);check(EssenceApi.snapshot(player).tracks().get(Skill.valueOf("strength")).experience()>before.tracks().get(Skill.valueOf("strength")).experience()&&changes.get()==1,"public experience grant and state event");
  check(before.tracks().get(Skill.valueOf("strength")).experience()==0,"snapshot stays detached");
  var denied=new java.util.concurrent.atomic.AtomicBoolean();var thread=new Thread(()->{try{EssenceApi.snapshot(player);}catch(IllegalStateException expected){denied.set(true);}});thread.start();thread.join();check(denied.get(),"worker thread rejected");
  var p=CoreRuntime.data(player);p.stamina=.5;p.exhausted=false;
  check(!Resources.payStamina(player,1,true)&&p.stamina==.5,"exact payment preserves fractional remainder");check(Resources.payStamina(player,1,false)&&p.stamina==0&&p.exhausted,"fractional final payment allowed");
  Resources.stamina().orElseThrow().restore(player,20);check(p.stamina>0&&!p.exhausted,"public resource restoration");
  var skill=EssenceApi.registerSkill("runtime_api:craft");EssenceApi.registerBonus("runtime_api:bonus",(target,value)->{});Extensions.tab(new Extensions.Tab("runtime_api",1,1,false));Extensions.action(new Extensions.Action("runtime_api:use",skill,1,"runtime_api:uses"));Extensions.ability(new Extensions.Ability("runtime_api:ability",java.util.List.of(new Extensions.Requirement(skill,0)),java.util.List.of(new Extensions.Condition("runtime_api:uses",1)),java.util.List.of(),"runtime_api:bonus",1,true,0,0,"minecraft:stone"));
  check(EssenceApi.needsCounter(player,"runtime_api:uses"),"public counter demand");check(EssenceApi.grantAction(player,"runtime_api:use",1,1),"public registered action");check(EssenceApi.unlock(player,"runtime_api:ability")&&EssenceApi.enabled(player,"runtime_api:ability"),"public unlock and enabled query");check(EssenceApi.toggle(player,"runtime_api:ability")&&!EssenceApi.enabled(player,"runtime_api:ability"),"public toggle");
  player.giveExperienceLevels(5);check(EssenceApi.buyCap(player,skill)&&player.experienceLevel==4,"public cap purchase spends correct levels");
  EssenceApi.registerPath("runtime_api:path",target->true);check(EssenceApi.choosePath(player,"runtime_api:path")&&!EssenceApi.choosePath(player,"runtime_api:path"),"public path selection");
  System.out.println("Public Essence API gameplay checks passed");
 }
}
