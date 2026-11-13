package com.faust.epiccompat.mixin;
import com.faust.epiccompat.Compat;
import com.faust.core.runtime.CoreRuntime;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.skill.*;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
@Mixin(value=SkillContainer.class,remap=false)
public abstract class ContainerMixin {
    @Inject(method="onReplaceCooldown",at=@At("HEAD"),cancellable=true)
    private void noReplaceWait(CallbackInfoReturnable<Boolean> cir){cir.setReturnValue(false);}
    @Inject(method="getReplaceCooldown",at=@At("HEAD"),cancellable=true)
    private void noReplaceTimer(CallbackInfoReturnable<Integer> cir){cir.setReturnValue(0);}
    @ModifyVariable(method="setReplaceCooldown",at=@At("HEAD"),argsOnly=true)
    private int clearReplaceTimer(int ticks){return 0;}
    @Shadow public abstract PlayerPatch<?> getExecutor();
    @Shadow public abstract Skill getSkill();
    @Shadow public abstract SkillSlot getSlot();
    @Inject(method="canUse",at=@At("HEAD"),cancellable=true)
    private void mayCast(PlayerPatch<?> executor,yesman.epicfight.api.event.types.player.SkillCastEvent event,CallbackInfoReturnable<Boolean> cir){
        if(getSkill() instanceof yesman.epicfight.skill.common.ComboAttacks&&!com.faust.epiccompat.Combat.canStrike(executor)){cir.setReturnValue(false);return;}
        if(executor.getOriginal() instanceof ServerPlayer player){
            var skill=getSkill();
            if(skill!=null&&((skill.getCategory().learnable()&&!com.faust.essence.api.EssenceApi.learned(player,Compat.nodeId(skill)))||(!CoreRuntime.canAct(player)&&skill.getCategory()!=SkillCategories.KNOCKDOWN_WAKEUP&&!(skill.getCategory()==SkillCategories.DODGE&&com.faust.epiccompat.EmergencyDodge.available(executor)))))cir.setReturnValue(false);
        }
    }
    @Inject(method="setSkill(Lyesman/epicfight/skill/Skill;Z)Z",at=@At("HEAD"),cancellable=true)
    private void gate(Skill skill,boolean initialize,CallbackInfoReturnable<Boolean> cir){
        if(skill!=null&&!com.faust.epiccompat.Combat.allowedSlot(getSlot())){cir.setReturnValue(false);return;}
        if(skill!=null&&skill.getCategory().learnable()&&getExecutor()!=null&&getExecutor().getOriginal() instanceof ServerPlayer player&&!com.faust.essence.api.EssenceApi.learned(player,Compat.nodeId(skill)))cir.setReturnValue(false);
    }
}
