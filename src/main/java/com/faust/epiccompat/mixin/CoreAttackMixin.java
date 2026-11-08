package com.faust.epiccompat.mixin;
import com.faust.core.runtime.CoreRuntime;
import com.faust.epiccompat.Combat;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=CoreRuntime.class,remap=false)
public class CoreAttackMixin {
    @Inject(method="attack",at=@At("HEAD"),cancellable=true)
    private static void prepaid(ServerPlayer player,boolean air,CallbackInfoReturnable<Boolean> cir){
        if(Combat.prepaid(player)){cir.setReturnValue(true);return;}
        // EF phases pay through CoreRuntime.pay; this entry point belongs to vanilla hits/swings.
        if(!yesman.epicfight.world.gamerule.EpicFightGameRules.ALLOW_VANILLA_MELEE.getRuleValue(player.level())){cir.setReturnValue(true);return;}
        if(!air&&com.faust.epiccompat.Forbidden.mode(player)){cir.setReturnValue(com.faust.epiccompat.Forbidden.pay(player,CoreRuntime.data(player).actionCost("attack",CoreRuntime.balance)));return;}
        if(air)yesman.epicfight.world.capabilities.EpicFightCapabilities.getUnparameterizedEntityPatch(player,yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch.class).filter(p->p.isEpicFightMode()).ifPresent(p->cir.setReturnValue(true));
    }
}
