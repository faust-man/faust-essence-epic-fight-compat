package com.faust.epiccompat.mixin;
import com.faust.core.runtime.CoreRuntime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.mover.DemolitionLeapSkill;
@Mixin(value=DemolitionLeapSkill.class,remap=false)
public abstract class DemolitionExhaustionMixin {
 @Inject(method="holdTick",at={@At("HEAD"),@At("TAIL")},cancellable=true)
 private void essence$stop(SkillContainer container,CallbackInfo ci){
  var patch=container.getExecutor();
  if(patch.getOriginal() instanceof net.minecraft.server.level.ServerPlayer player&&com.faust.essence.api.EssenceApi.active(player)&&!com.faust.epiccompat.Forbidden.mode(player)&&CoreRuntime.data(player).actionsBlocked(CoreRuntime.balance)&&patch.isHoldingSkill(container.getSkill())){
   container.requestCancel(container.getServerExecutor(),new net.minecraft.nbt.CompoundTag());
   patch.resetHolding();
   patch.playAnimationSynchronized(yesman.epicfight.gameasset.Animations.BIPED_IDLE,0);
   ci.cancel();
  }
 }
}
