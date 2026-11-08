package com.faust.epiccompat.mixin;
import com.faust.core.runtime.CoreRuntime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import yesman.epicfight.skill.passive.StaminaPillagerSkill;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
/** Restore the shared double-precision pool directly, without a payment or fatigue. */
@Mixin(value=StaminaPillagerSkill.class,remap=false)
public class StaminaPillagerMixin {
    @Shadow protected float regenRate;
    @Redirect(method="lambda$onInitiate$0",at=@At(value="INVOKE",target="Lyesman/epicfight/world/capabilities/entitypatch/player/PlayerPatch;setStamina(F)V"))
    private void restore(PlayerPatch<?> patch,float ignored){
        if(patch.getOriginal() instanceof net.minecraft.server.level.ServerPlayer player){
            var data=CoreRuntime.data(player);var balance=CoreRuntime.balance;
            data.stamina=Math.min(data.maxStamina(balance),data.stamina+Math.min(2,Math.max(0,(data.maxStamina(balance)-data.stamina)*regenRate*0.01)));
            data.updateExhaustion(balance);
        }
    }
}
