package com.faust.epiccompat.mixin;
import com.faust.epiccompat.Combat;
import java.util.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.skill.*;
import yesman.epicfight.world.capabilities.skill.PlayerSkills;
@Mixin(value=PlayerSkills.class,remap=false)
public class SlotsMixin {
    @Inject(method="getSkillContainersFor",at=@At("RETURN"),cancellable=true)
    private void three(SkillCategory category,CallbackInfoReturnable<Set<SkillContainer>> cir){
        var slots=new LinkedHashSet<SkillContainer>();
        for(var container:cir.getReturnValue())if(Combat.allowedSlot(container.getSlot()))slots.add(container);
        cir.setReturnValue(slots);
    }
}
