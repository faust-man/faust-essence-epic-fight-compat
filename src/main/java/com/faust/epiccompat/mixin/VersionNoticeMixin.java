package com.faust.epiccompat.mixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.gui.GuiGraphics;
import yesman.epicfight.client.gui.VersionNotifier;
@Mixin(value=VersionNotifier.class,remap=false)
public class VersionNoticeMixin {
    @Inject(method="render",at=@At("HEAD"),cancellable=true)
    private void hide(GuiGraphics graphics,boolean inWorld,CallbackInfo ci){ci.cancel();}
}
