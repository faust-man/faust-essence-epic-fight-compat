package com.faust.epiccompat.mixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.DeltaTracker;
import yesman.epicfight.client.gui.BattleModeGui;
@Mixin(value=BattleModeGui.class,remap=false)
public class HudMixin {
    @Inject(method="renderStaminaBar",at=@At("HEAD"),cancellable=true)
    private void hide(GuiGraphics graphics,DeltaTracker delta,CallbackInfo ci){ci.cancel();}
}
