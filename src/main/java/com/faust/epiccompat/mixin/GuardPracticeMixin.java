package com.faust.epiccompat.mixin;
import com.faust.core.runtime.CoreRuntime;
import com.faust.epiccompat.CombatPractice;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.skill.guard.GuardSkill;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.api.event.types.entity.TakeDamageEvent;
@Mixin(value=GuardSkill.class,remap=false)
public class GuardPracticeMixin {
    @Inject(method="dealEvent",at=@At("TAIL"))
    private void blocked(PlayerPatch<?> patch,TakeDamageEvent.Income event,boolean advanced,CallbackInfo ci){
        if(patch.getOriginal() instanceof net.minecraft.server.level.ServerPlayer player&&CoreRuntime.active(player)&&event.getDamageSource().getEntity() instanceof net.minecraft.world.entity.monster.Enemy enemy&&!player.isAlliedTo((net.minecraft.world.entity.Entity)enemy))CombatPractice.add(player,"blocks",1);
    }
}
