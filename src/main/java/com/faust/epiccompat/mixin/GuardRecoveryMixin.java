package com.faust.epiccompat.mixin;
import com.faust.core.runtime.CoreRuntime;
import com.faust.core.progression.*;
import com.faust.epiccompat.GuardRecovery;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
@Mixin(value=CoreRuntime.class,remap=false)
public class GuardRecoveryMixin {
    @WrapOperation(method="tick",at=@At(value="INVOKE",target="Lcom/faust/core/progression/Progress;recover(DZZZZZZZLcom/faust/core/progression/Balance;)V"))
    private static void hold(Progress progress,double dt,boolean sleep,boolean campfire,boolean ready,boolean shield,boolean flying,boolean air,boolean fastSwimming,Balance balance,Operation<Void> original,ServerPlayer player){
        original.call(progress,dt,sleep,campfire,ready,shield||GuardRecovery.blocked(player),flying,air,fastSwimming,balance);
    }
}
