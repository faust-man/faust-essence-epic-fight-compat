package com.faust.epiccompat.mixin;
import com.faust.epiccompat.Forbidden;
import com.faust.core.runtime.CoreRuntime;
import com.faust.core.progression.*;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
@Mixin(value=CoreRuntime.class,remap=false)
public class HealthPaymentMixin {
    @Inject(method="jump",at=@At("HEAD"),cancellable=true)
    private static void jump(ServerPlayer player,CallbackInfoReturnable<Boolean> cir){if(Forbidden.mode(player))cir.setReturnValue(!player.isSprinting()||Forbidden.pay(player,CoreRuntime.balance.stamina.get("sprintJumpCost")+CoreRuntime.weight(player)*CoreRuntime.balance.stamina.get("jumpArmorCost")));}
    @Inject(method="pay",at=@At("HEAD"),cancellable=true)
    private static void health(ServerPlayer player,double cost,CallbackInfoReturnable<Boolean> cir){if(Forbidden.mode(player))cir.setReturnValue(Forbidden.pay(player,cost));}
    @Inject(method="canAct",at=@At("HEAD"),cancellable=true)
    private static void act(ServerPlayer player,CallbackInfoReturnable<Boolean> cir){if(Forbidden.mode(player))cir.setReturnValue(player.getHealth()>0);}
    @WrapMethod(method="tick")
    private static void tick(ServerPlayer player,Operation<Void> original){Forbidden.TICK.set(player);try{original.call(player);}finally{Forbidden.TICK.remove();}}

}
