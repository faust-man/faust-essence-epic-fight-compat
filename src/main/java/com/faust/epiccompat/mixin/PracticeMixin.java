package com.faust.epiccompat.mixin;
import com.faust.epiccompat.CombatPractice;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.tags.DamageTypeTags;
import org.spongepowered.asm.mixin.Mixin;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
@Mixin(LivingEntity.class)
public class PracticeMixin {
    @WrapMethod(method="hurt")
    private boolean practice(DamageSource source,float amount,Operation<Boolean> original){
        LivingEntity entity=(LivingEntity)(Object)this;float before=entity.getHealth();
        // Capture the weapon before listeners or death callbacks can change the inventory.
        var weapon=source instanceof yesman.epicfight.world.damagesource.EpicFightDamageSource epic?epic.getUsedItem().copy():source.getEntity() instanceof ServerPlayer player?player.getMainHandItem().copy():net.minecraft.world.item.ItemStack.EMPTY;
        var kind=source.getDirectEntity() instanceof ServerPlayer player&&source.getEntity()==player&&!source.is(DamageTypeTags.IS_PROJECTILE)?CombatPractice.weapon(player,weapon):null;
        boolean result=original.call(source,amount);double lost=Math.max(0,before-entity.getHealth());
        if(entity instanceof ServerPlayer player&&lost>0&&!player.isCreative()&&!player.isSpectator())CombatPractice.add(player,CombatPractice.DAMAGE,lost);
        if(before>0&&entity.getHealth()<=0&&source.getDirectEntity() instanceof ServerPlayer player&&entity!=player&&kind!=null)CombatPractice.kill(player,kind);
        return result;
    }
}
