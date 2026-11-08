package com.faust.epiccompat.mixin;
import com.faust.epiccompat.Combat;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import yesman.epicfight.api.animation.types.grappling.GrapplingAttackAnimation;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
@Mixin(value=GrapplingAttackAnimation.class,remap=false)
public class GrapplingCostMixin {
    @Redirect(method="attackTick",at=@At(value="INVOKE",target="Lyesman/epicfight/world/capabilities/entitypatch/LivingEntityPatch;attack(Lyesman/epicfight/world/damagesource/EpicFightDamageSource;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/InteractionHand;)Lyesman/epicfight/api/utils/AttackResult;"))
    private AttackResult pay(LivingEntityPatch<?> patch,EpicFightDamageSource source,Entity target,InteractionHand hand){
        if(patch instanceof PlayerPatch<?> player&&patch.getOriginal() instanceof ServerPlayer serverPlayer){
            Combat.strike(player,hand);if(!Combat.paid(player))return AttackResult.missed(0);
            var result=new java.util.concurrent.atomic.AtomicReference<AttackResult>();
            Combat.collide(serverPlayer,()->result.set(patch.attack(source,target,hand)));return result.get();
        }
        return patch.attack(source,target,hand);
    }
}
