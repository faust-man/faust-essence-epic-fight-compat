package com.faust.epiccompat;
import com.faust.core.progression.*;
import net.minecraft.server.level.ServerPlayer;
import yesman.epicfight.skill.*;
public final class AcceleratedThinking {
 public static final String ID="faust_epic_compat:accelerated_thinking",SLOTS="faust_epic_passive_slots";
 public static Balance.Ability defaults(){return new Balance.Ability(ID,com.faust.essence.api.Skill.valueOf("intelligence"),5,SLOTS,3,"","faust_epic_compat:mastery",0);}
 public static double multiplier(int intelligence,boolean enabled){return enabled?1+Math.max(0,intelligence)*0.02:1;}
 public static float charge(SkillContainer container,float total){
  if(!(container.getExecutor().getOriginal() instanceof ServerPlayer player)||!com.faust.essence.api.EssenceApi.active(player))return total;
  var p=com.faust.core.runtime.CoreRuntime.data(player);float current=container.getResource();
  return current+(float)((total-current)*multiplier(p.track(com.faust.essence.api.Skill.valueOf("intelligence")).level,p.enabled(ID,com.faust.core.runtime.CoreRuntime.balance)));
 }
}
