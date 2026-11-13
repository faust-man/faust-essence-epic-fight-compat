package com.faust.epiccompat.mixin;
@org.spongepowered.asm.mixin.Mixin(net.minecraft.world.entity.player.Player.class)
public abstract class GreatswordShieldMixin {
 @org.spongepowered.asm.mixin.injection.ModifyVariable(method="hurtCurrentlyUsedShield",at=@org.spongepowered.asm.mixin.injection.At("HEAD"),argsOnly=true) private float essence$shieldDamage(float damage){var source=com.faust.core.runtime.CombatContext.DAMAGE.get().peek();return source!=null&&com.faust.epiccompat.WeaponMastery.mastered(source)?damage*2:damage;}
}
