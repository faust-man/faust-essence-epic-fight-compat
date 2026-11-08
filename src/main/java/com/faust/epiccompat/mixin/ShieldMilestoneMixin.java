package com.faust.epiccompat.mixin;
import com.faust.epiccompat.Milestones;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Player.class)
public class ShieldMilestoneMixin {
    @Inject(method="stopSleepInBed",at=@At("HEAD"))
    private void wake(boolean update,boolean setSpawn,CallbackInfo ci){
        if((Object)this instanceof ServerPlayer player&&player.isSleeping())Milestones.sleep(player,player.getRandom().nextInt(100));
    }
    @Inject(method="hurtCurrentlyUsedShield",at=@At("HEAD"))
    private void shield(float damage,CallbackInfo ci){if(damage>0&&(Object)this instanceof ServerPlayer player)Milestones.blocked(player,true);}
}
