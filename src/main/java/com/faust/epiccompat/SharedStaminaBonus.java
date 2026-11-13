package com.faust.epiccompat;
import com.faust.core.runtime.CoreRuntime;
import com.faust.core.progression.StaminaServices;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
public final class SharedStaminaBonus {
 private SharedStaminaBonus(){}
 public static void refresh(ServerPlayer player){
  if(!StaminaServices.present())return;var data=CoreRuntime.data(player);var attribute=player.getAttribute(yesman.epicfight.registry.entries.EpicFightAttributes.MAX_STAMINA);
  double base=data.baseStamina(CoreRuntime.balance)-data.externalStaminaBonus,add=0,multiplyBase=0,multiplyTotal=1;
  if(attribute!=null)for(var modifier:attribute.getModifiers())switch(modifier.operation()){
   case ADD_VALUE -> add+=modifier.amount();case ADD_MULTIPLIED_BASE -> multiplyBase+=modifier.amount();case ADD_MULTIPLIED_TOTAL -> multiplyTotal*=1+modifier.amount();
  }
  double bonus=Math.max(1,(base+add)*(1+multiplyBase)*multiplyTotal)-base;
  if(Math.abs(data.externalStaminaBonus-bonus)>1e-7){data.externalStaminaBonus=bonus;data.stamina=Math.min(data.stamina,data.maxStamina(CoreRuntime.balance));data.revision++;}
 }
}
