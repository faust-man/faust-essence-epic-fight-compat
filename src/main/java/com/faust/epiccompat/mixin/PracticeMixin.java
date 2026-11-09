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
        var attacker=source.getDirectEntity() instanceof ServerPlayer p&&source.getEntity()==p&&!source.is(DamageTypeTags.IS_PROJECTILE)?p:null;
        boolean needed=attacker!=null&&CombatPractice.weaponPracticeNeeded(attacker);
        // Capture only if pending practice needs weapon classification.
        var used=source instanceof yesman.epicfight.world.damagesource.EpicFightDamageSource epic?epic.getUsedItem():attacker!=null?attacker.getMainHandItem():net.minecraft.world.item.ItemStack.EMPTY;
        var weapon=needed?used.copy():net.minecraft.world.item.ItemStack.EMPTY;
        var kind=needed?CombatPractice.weapon(attacker,weapon):null;
        if(attacker!=null&&entity!=attacker&&com.faust.core.runtime.CoreRuntime.active(attacker)){
            if(com.faust.core.progression.StaminaServices.present()&&used.isEmpty()&&com.faust.core.runtime.CoreRuntime.data(attacker).enabled(com.faust.epiccompat.Combat.FIST_POWER,com.faust.core.runtime.CoreRuntime.balance))amount+=Math.floor(com.faust.core.runtime.CoreRuntime.data(attacker).track(com.faust.core.progression.Skill.strength).level/10.0);
        }
        boolean exhausted=source.getEntity() instanceof ServerPlayer p&&com.faust.core.runtime.CoreRuntime.data(p).actionsBlocked(com.faust.core.runtime.CoreRuntime.balance);
        boolean result=original.call(source,amount);
        if(result&&kind!=null&&attacker!=null&&entity!=attacker&&com.faust.core.runtime.CoreRuntime.active(attacker)){
            var cat=yesman.epicfight.world.capabilities.EpicFightCapabilities.getItemStackCapability(weapon).getWeaponCategory();
            if(!weapon.isEmpty()&&cat!=yesman.epicfight.world.capabilities.item.CapabilityItem.WeaponCategories.NOT_WEAPON&&attacker.getOffhandItem().canPerformAction(net.neoforged.neoforge.common.ItemAbilities.SHIELD_BLOCK))CombatPractice.add(attacker,CombatPractice.SHIELD_HITS,1);
            if(cat==yesman.epicfight.world.capabilities.item.CapabilityItem.WeaponCategories.DAGGER&&yesman.epicfight.world.capabilities.EpicFightCapabilities.getItemStackCapability(attacker.getOffhandItem()).getWeaponCategory()==cat)CombatPractice.add(attacker,CombatPractice.DAGGER_HITS,1);
        }double lost=Math.max(0,before-entity.getHealth());
        if(entity instanceof ServerPlayer player&&lost>0&&!player.isCreative()&&!player.isSpectator())CombatPractice.add(player,CombatPractice.DAMAGE,lost);
        if(result)com.faust.epiccompat.Milestones.damage(entity,source,lost,before>0&&entity.getHealth()<=0,exhausted);
        if(before>0&&entity.getHealth()<=0&&source.getDirectEntity() instanceof ServerPlayer player&&entity!=player&&kind!=null)CombatPractice.kill(player,kind);
        return result;
    }
}
