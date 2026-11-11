package com.faust.epiccompat.mixin;
import com.faust.epiccompat.CombatPractice;
import com.faust.core.runtime.CoreRuntime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.dodge.DodgeSkill;
@Mixin(value=DodgeSkill.class,remap=false)
public class RollPracticeMixin {
    @Inject(method="executeOnServer",at=@At("TAIL"))
    private void completed(SkillContainer container,net.minecraft.nbt.CompoundTag args,CallbackInfo ci){
        var player=container.getServerExecutor().getOriginal();
        if(CoreRuntime.active(player))CoreRuntime.grant(player,"run",1,1);
        if(container.getSkill().getRegistryName().toString().equals("epicfight:roll")&&CoreRuntime.active(player))CombatPractice.add(player,CombatPractice.ROLLS,1);
    }
}
