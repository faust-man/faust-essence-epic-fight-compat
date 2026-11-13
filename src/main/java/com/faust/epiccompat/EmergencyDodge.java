package com.faust.epiccompat;
import com.faust.core.runtime.CoreRuntime;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
public final class EmergencyDodge {
 public static java.util.function.BooleanSupplier CLIENT_EXHAUSTED=()->false;
 public static boolean spend(PlayerPatch<?> patch,net.minecraft.nbt.CompoundTag args){
  if(!available(patch))return false;
  if(!patch.isLogicalClient())for(var c:patch.getPlayerSkills().skillContainers)if(c.getSkill() instanceof yesman.epicfight.skill.passive.EmergencyEscapeSkill&&c.getStack()>0&&!c.isDisabled()&&CoreRuntime.data((net.minecraft.server.level.ServerPlayer)patch.getOriginal()).enabled(Compat.nodeId(c.getSkill()),CoreRuntime.balance)){c.getSkill().executeOnServer(c,args);break;}
  return true;
 }
 public static boolean available(PlayerPatch<?> patch){
  boolean exhausted=patch.getOriginal() instanceof net.minecraft.server.level.ServerPlayer p?CoreRuntime.data(p).actionsBlocked(CoreRuntime.balance):CLIENT_EXHAUSTED.getAsBoolean();if(!exhausted)return false;
  for(var container:patch.getPlayerSkills().skillContainers)if(container.getSkill() instanceof yesman.epicfight.skill.passive.EmergencyEscapeSkill&&container.getStack()>0&&!container.isDisabled()){
   if(patch.getOriginal() instanceof net.minecraft.server.level.ServerPlayer p&&!CoreRuntime.data(p).enabled(Compat.nodeId(container.getSkill()),CoreRuntime.balance))continue;return true;
  }return false;
 }
}
