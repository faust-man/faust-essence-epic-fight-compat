package com.faust.epiccompat.mixin;
import com.faust.epiccompat.Combat;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import yesman.epicfight.api.animation.types.*;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

@Mixin(value=AttackAnimation.class,remap=false)
public abstract class AttackMixin {
    @Inject(method="begin",at=@At("HEAD"))
    private void reset(LivingEntityPatch<?> patch,org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci){if(patch instanceof PlayerPatch<?> player&&patch.getOriginal() instanceof ServerPlayer)Combat.begin(player);}
    @Shadow protected abstract void hurtCollidingEntities(LivingEntityPatch<?> patch,float previous,float current,EntityState oldState,EntityState state,AttackAnimation.Phase phase);
    @Redirect(method="attackTick",at=@At(value="INVOKE",target="Lyesman/epicfight/world/capabilities/entitypatch/LivingEntityPatch;onStrike(Lyesman/epicfight/api/animation/types/AttackAnimation;Lnet/minecraft/world/InteractionHand;)V"))
    private void pay(LivingEntityPatch<?> patch,AttackAnimation animation,InteractionHand hand){
        if(patch instanceof PlayerPatch<?> player&&patch.getOriginal() instanceof ServerPlayer)Combat.strike(player,hand);
        patch.onStrike(animation,hand);
    }
    @Redirect(method="attackTick",at=@At(value="INVOKE",target="Lyesman/epicfight/api/animation/types/AttackAnimation;hurtCollidingEntities(Lyesman/epicfight/world/capabilities/entitypatch/LivingEntityPatch;FFLyesman/epicfight/api/animation/types/EntityState;Lyesman/epicfight/api/animation/types/EntityState;Lyesman/epicfight/api/animation/types/AttackAnimation$Phase;)V"))
    private void collision(AttackAnimation animation,LivingEntityPatch<?> patch,float previous,float current,EntityState oldState,EntityState state,AttackAnimation.Phase phase){
        if(patch instanceof PlayerPatch<?> player&&patch.getOriginal() instanceof ServerPlayer serverPlayer){
            if(Combat.paid(player))Combat.collide(serverPlayer,()->hurtCollidingEntities(patch,previous,current,oldState,state,phase));
        }else hurtCollidingEntities(patch,previous,current,oldState,state,phase);
    }
}
