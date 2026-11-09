package com.faust.epiccompat.mixin;
import com.faust.epiccompat.Forbidden;
import com.faust.core.progression.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(targets="com.faust.stamina.StaminaRuntime",remap=false)
public class HealthStaminaTickMixin {
    @Redirect(method="tick",at=@At(value="FIELD",target="Lcom/faust/core/progression/Progress;stamina:D",opcode=180))
    private static double remaining(Progress progress){var p=Forbidden.TICK.get();return p!=null&&Forbidden.mode(p)?Math.max(1e-9,progress.stamina):progress.stamina;}
    @Redirect(method="tick",at=@At(value="INVOKE",target="Lcom/faust/core/progression/Progress;actionsBlocked(Lcom/faust/core/progression/Balance;)Z"))
    private static boolean blocking(Progress progress,Balance balance){var p=Forbidden.TICK.get();return progress.actionsBlocked(balance)&&(p==null||!Forbidden.mode(p));}
    @Redirect(method="tick",at=@At(value="INVOKE",target="Lcom/faust/core/progression/Progress;spend(DLcom/faust/core/progression/Balance;J)D"))
    private static double spend(Progress progress,double cost,Balance balance,long age){var p=Forbidden.TICK.get();if(p!=null&&Forbidden.mode(p)){if(!Forbidden.pay(p,cost))p.setSprinting(false);return 0;}return progress.spend(cost,balance,age);}
    @Redirect(method="tick",at=@At(value="INVOKE",target="Lcom/faust/core/progression/Progress;recover(DZZZZZZZLcom/faust/core/progression/Balance;)V"))
    private static void recover(Progress progress,double dt,boolean sleep,boolean campfire,boolean ready,boolean shielding,boolean flying,boolean air,boolean fastSwimming,Balance balance){var p=Forbidden.TICK.get();boolean health=p!=null&&Forbidden.mode(p);progress.recover(dt,sleep,campfire,ready||health,shielding&&!health,flying,air,fastSwimming,balance);}
}
