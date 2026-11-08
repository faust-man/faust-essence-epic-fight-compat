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
    @Redirect(method="tick",at=@At(value="FIELD",target="Lcom/faust/core/progression/Progress;stamina:D",opcode=180))
    private static double remaining(Progress progress){var p=Forbidden.TICK.get();return p!=null&&Forbidden.mode(p)?Math.max(1e-9,progress.stamina):progress.stamina;}
    @Inject(method="pay",at=@At("HEAD"),cancellable=true)
    private static void health(ServerPlayer player,double cost,CallbackInfoReturnable<Boolean> cir){if(Forbidden.mode(player))cir.setReturnValue(Forbidden.pay(player,cost));}
    @Inject(method="canAct",at=@At("HEAD"),cancellable=true)
    private static void act(ServerPlayer player,CallbackInfoReturnable<Boolean> cir){if(Forbidden.mode(player))cir.setReturnValue(player.getHealth()>0);}
    @WrapMethod(method="tick")
    private static void tick(ServerPlayer player,Operation<Void> original){Forbidden.TICK.set(player);try{original.call(player);}finally{Forbidden.TICK.remove();}}
    @Redirect(method="tick",at=@At(value="INVOKE",target="Lcom/faust/core/progression/Progress;actionsBlocked(Lcom/faust/core/progression/Balance;)Z"))
    private static boolean blocking(Progress progress,Balance balance){var p=Forbidden.TICK.get();return progress.actionsBlocked(balance)&&(p==null||!Forbidden.mode(p));}
    @Redirect(method="tick",at=@At(value="INVOKE",target="Lcom/faust/core/progression/Progress;spend(DLcom/faust/core/progression/Balance;J)D"))
    private static double spend(Progress progress,double cost,Balance balance,long age){var p=Forbidden.TICK.get();if(p!=null&&Forbidden.mode(p)){if(!Forbidden.pay(p,cost))p.setSprinting(false);return 0;}return progress.spend(cost,balance,age);}
    @Redirect(method="tick",at=@At(value="INVOKE",target="Lcom/faust/core/progression/Progress;recover(DZZZZZZLcom/faust/core/progression/Balance;)V"))
    private static void recover(Progress progress,double dt,boolean sleep,boolean campfire,boolean ready,boolean shielding,boolean flying,boolean air,Balance balance){var p=Forbidden.TICK.get();boolean health=p!=null&&Forbidden.mode(p);progress.recover(dt,sleep,campfire,ready||health,shielding&&!health,flying,air,balance);}
}
