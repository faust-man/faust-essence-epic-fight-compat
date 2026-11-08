package com.faust.epiccompat.mixin;
import com.faust.core.runtime.CoreRuntime;
import net.minecraft.server.level.ServerPlayer;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(value=PlayerPatch.class,remap=false)
public class StaminaMixin {
    private PlayerPatch<?> patch(){return (PlayerPatch<?>)(Object)this;}
    @Inject(method="getStamina",at=@At("HEAD"),cancellable=true)
    private void stamina(CallbackInfoReturnable<Float> cir){
        if(patch().getOriginal() instanceof ServerPlayer p)cir.setReturnValue((float)CoreRuntime.data(p).stamina);
        else if(patch().getOriginal().level().isClientSide)com.faust.epiccompat.client.ClientBridge.stamina(patch(),cir,false);
    }
    @Inject(method="getMaxStamina",at=@At("HEAD"),cancellable=true)
    private void maximum(CallbackInfoReturnable<Float> cir){
        if(patch().getOriginal() instanceof ServerPlayer p)cir.setReturnValue((float)CoreRuntime.data(p).maxStamina(CoreRuntime.balance));
        else if(patch().getOriginal().level().isClientSide)com.faust.epiccompat.client.ClientBridge.stamina(patch(),cir,true);
    }
    @Inject(method="hasStamina",at=@At("HEAD"),cancellable=true)
    private void enough(float amount,CallbackInfoReturnable<Boolean> cir){
        if(patch().getOriginal().level().isClientSide&&com.faust.epiccompat.client.ClientBridge.health()&&com.faust.epiccompat.Forbidden.equipped(patch())){cir.setReturnValue(Float.isFinite(amount)&&amount>=0&&patch().getOriginal().getHealth()>amount);return;}
        if(patch().getOriginal() instanceof ServerPlayer p&&com.faust.epiccompat.Forbidden.mode(p)){cir.setReturnValue(com.faust.epiccompat.Forbidden.afford(p,amount));return;}
        if(patch().getOriginal() instanceof ServerPlayer p)cir.setReturnValue(com.faust.epiccompat.Rules.canSpend(CoreRuntime.data(p).stamina,amount,CoreRuntime.data(p).actionsBlocked(CoreRuntime.balance),!CoreRuntime.active(p)||CoreRuntime.balance.stamina.get("enabled")==0));
    }
    @Redirect(method="preTickServer",at=@At(value="INVOKE",target="Lyesman/epicfight/world/capabilities/entitypatch/player/PlayerPatch;setStamina(F)V"))
    private void noRegeneration(PlayerPatch<?> patch,float value){}
    @Inject(method="setStamina",at=@At("HEAD"),cancellable=true)
    private void spend(float value,CallbackInfo ci){
        // Epic Fight regeneration is disabled; only Core regenerates the shared pool.
        if(patch().getOriginal() instanceof ServerPlayer p){
            double cost=CoreRuntime.data(p).stamina-value;
            if(Double.isFinite(cost)&&cost>0.00001){CoreRuntime.pay(p,cost);CoreRuntime.sync(p);}
            else if(Double.isFinite(cost)&&cost< -0.00001){var data=CoreRuntime.data(p);data.stamina=Math.min(value,data.maxStamina(CoreRuntime.balance));data.updateExhaustion(CoreRuntime.balance);CoreRuntime.sync(p);}
        }
        ci.cancel();
    }
}
