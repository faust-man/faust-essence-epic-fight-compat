package com.faust.epiccompat.mixin;
import com.faust.core.progression.Balance;
import com.faust.epiccompat.Compat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.nio.file.Path;
@Mixin(value=Balance.class,remap=false)
public class BalanceMixin {
    @Inject(method="load",at=@At("RETURN"))
    private static void extend(Path path,CallbackInfoReturnable<Balance> cir){Compat.extend(cir.getReturnValue());}
}
