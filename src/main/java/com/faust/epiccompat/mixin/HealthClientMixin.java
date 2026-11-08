package com.faust.epiccompat.mixin;
import com.faust.core.client.CoreClient;
import com.faust.core.progression.*;
import com.faust.epiccompat.Forbidden;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=CoreClient.class,remap=false)
public class HealthClientMixin {
    @Inject(method="<clinit>",at=@At("TAIL"))
    private static void jump(org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci){var original=com.faust.core.runtime.ClientActions.jumpAllowed;com.faust.core.runtime.ClientActions.jumpAllowed=()->com.faust.epiccompat.client.ClientBridge.health()||original.getAsBoolean();}
    @Inject(method="canSprint",at=@At("HEAD"),cancellable=true)
    private static void sprint(Object entity,CallbackInfoReturnable<Boolean> cir){if(entity==Minecraft.getInstance().player&&com.faust.epiccompat.client.ClientBridge.health())cir.setReturnValue(true);}
    @Redirect(method="tick",at=@At(value="INVOKE",target="Lcom/faust/core/progression/Progress;actionsBlocked(Lcom/faust/core/progression/Balance;)Z"))
    private static boolean blocked(Progress progress,Balance balance){return progress.actionsBlocked(balance)&&!com.faust.epiccompat.client.ClientBridge.health();}
}
