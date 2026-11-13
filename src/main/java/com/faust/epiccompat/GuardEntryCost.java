package com.faust.epiccompat;
import com.faust.core.runtime.CoreRuntime;
import com.faust.core.progression.StaminaServices;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
public final class GuardEntryCost {
 public static final ThreadLocal<Boolean> PAID=ThreadLocal.withInitial(()->false);
 private GuardEntryCost(){}
 public static boolean pay(PlayerPatch<?> patch){if(!(patch.getOriginal() instanceof net.minecraft.server.level.ServerPlayer p)||!CoreRuntime.active(p))return true;if(StaminaServices.present()&&CoreRuntime.balance.stamina.get("enabled")>0)return CoreRuntime.payStaminaExact(p,1);if(patch.getStamina()<1)return false;patch.setStamina(patch.getStamina()-1);return true;}
}
