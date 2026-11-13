package com.faust.epiccompat;
import com.faust.core.runtime.CoreRuntime;
import com.faust.core.progression.StaminaServices;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
public final class GuardEntryCost {
 public static final ThreadLocal<Boolean> PAID=ThreadLocal.withInitial(()->false);
 private GuardEntryCost(){}
 public static boolean pay(PlayerPatch<?> patch){if(!(patch.getOriginal() instanceof net.minecraft.server.level.ServerPlayer p)||!com.faust.essence.api.EssenceApi.active(p))return true;if(StaminaServices.present()&&CoreRuntime.balance.stamina.get("enabled")>0)return com.faust.essence.api.Resources.payStamina(p,1,true);if(patch.getStamina()<1)return false;patch.setStamina(patch.getStamina()-1);return true;}
}
