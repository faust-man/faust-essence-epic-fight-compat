package com.faust.epiccompat.mixin;
import com.faust.core.runtime.*;
import com.faust.core.progression.Progress;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Seed Essence ownership before EF copies skill containers through our ownership gate. */
@Mixin(value=yesman.epicfight.api.event.impl.VanillaPlayerEventHooks.class,remap=false)
public abstract class DeathSkillsMixin {
 @Inject(method="onCloned",at=@At("HEAD")) private static void essence$ownership(net.minecraft.world.entity.player.Player oldPlayer,net.minecraft.world.entity.player.Player newPlayer,boolean death,CallbackInfo ci){if(oldPlayer instanceof ServerPlayer previous&&newPlayer instanceof ServerPlayer next)((PlayerData)next).coreData(Progress.parse(CoreRuntime.data(previous).json()));}
}
