package com.faust.epiccompat.mixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.skill.*;
import yesman.epicfight.skill.common.ComboAttacks;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
@Mixin(value=ComboAttacks.class,remap=false)
public class ComboCostMixin {
    // Basic combos are paid per attack phase, including dash/air attacks and misses.
    @Inject(method="checkConsumption",at=@At("HEAD"),cancellable=true)
    private void phasePayment(SkillContainer container,boolean dash,boolean air,CallbackInfoReturnable<Boolean> cir){cir.setReturnValue(true);}
    @Redirect(method="executeOnServer",at=@At(value="INVOKE",target="Lyesman/epicfight/skill/Skill$Resource$ResourceConsumer;consume(Lyesman/epicfight/skill/SkillContainer;Lyesman/epicfight/world/capabilities/entitypatch/player/ServerPlayerPatch;F)V"))
    private void noDuplicate(Skill.Resource.ResourceConsumer consumer,SkillContainer container,ServerPlayerPatch patch,float amount){}
}
