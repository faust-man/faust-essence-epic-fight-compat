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
        if((Object)this instanceof ServerPlayer player&&CoreRuntime.active(player))CombatPractice.add(player,CombatPractice.JUMPS,1);
    }
}
