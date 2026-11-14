package com.faust.epiccompat.mixin;

import com.faust.core.runtime.CoreRuntime;
import com.faust.epiccompat.CombatPractice;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class JumpPracticeMixin {
    @Inject(method="jumpFromGround",at=@At("TAIL"))
    private void completed(CallbackInfo ci){
        if((Object)this instanceof ServerPlayer player&&com.faust.essence.api.EssenceApi.active(player)){
            CombatPractice.add(player,CombatPractice.JUMPS,1);
            var v=player.getDeltaMovement();double yaw=Math.toRadians(player.getYRot());
            double sideways=v.x*Math.cos(yaw)+v.z*Math.sin(yaw);
            double forward=-v.x*Math.sin(yaw)+v.z*Math.cos(yaw);
            if(Math.abs(sideways)>0.01&&Math.abs(sideways)>=Math.abs(forward))com.faust.epiccompat.Milestones.add(CoreRuntime.data(player),"side_jumps",1);
        }
    }
}
