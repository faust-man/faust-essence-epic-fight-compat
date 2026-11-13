package com.faust.epiccompat;

import com.faust.core.progression.Progress;
import com.faust.core.runtime.CoreRuntime;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.*;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.projectile.AbstractArrow;

public final class Milestones {
    private Milestones(){}
    public static void add(Progress p,String key,double amount){p.recordCounter(SkillRequirements.PREFIX+key,amount,CoreRuntime.balance);}
    public static void peak(Progress p,String key,double amount){p.recordMaximum(SkillRequirements.PREFIX+key,amount,CoreRuntime.balance);}
    public static void fatigue(Progress p){peak(p,"peak_fatigue",p.fatigue);}
    public static void visitEnd(ServerPlayer player){
        if(!com.faust.essence.api.EssenceApi.active(player))return;var p=CoreRuntime.data(player);
        if(!p.wantsCounter("faust_epic_visited_end",CoreRuntime.balance))return;
        var advancement=player.getServer().getAdvancements().get(net.minecraft.resources.ResourceLocation.parse("minecraft:story/enter_the_end"));
        if(player.level().dimension()==net.minecraft.world.level.Level.END||advancement!=null&&player.getAdvancements().getOrStartProgress(advancement).isDone())peak(p,"visited_end",1);
    }
    public static void blocked(ServerPlayer player,boolean shield){if(!com.faust.essence.api.EssenceApi.active(player))return;var p=CoreRuntime.data(player);add(p,"any_blocks",1);if(shield)add(p,"shield_blocks",1);}
    public static void damage(LivingEntity target,DamageSource source,double lost,boolean killed,boolean exhaustedAttacker){
        if(lost<=0)return;
        if(target instanceof ServerPlayer player&&!player.isCreative()&&!player.isSpectator()){
            var p=CoreRuntime.data(player);if(p.wantsCounter("faust_epic_peak_damage",CoreRuntime.balance))peak(p,"peak_damage",lost);
            // Total damage uses the existing compat counter, already recorded by PracticeMixin.
            if(p.wantsCounter("faust_epic_fall_damage",CoreRuntime.balance)&&source.is(DamageTypeTags.IS_FALL))add(p,"fall_damage",lost);
            if(p.wantsCounter("faust_epic_melee_damage",CoreRuntime.balance)&&source.getEntity() instanceof LivingEntity&&source.getDirectEntity()==source.getEntity()&&!source.is(DamageTypeTags.IS_PROJECTILE))add(p,"melee_damage",lost);
            if(p.wantsCounter("faust_epic_arrow_damage",CoreRuntime.balance)&&source.getDirectEntity() instanceof AbstractArrow)add(p,"arrow_damage",lost);
            if(p.wantsCounter("faust_epic_fire_damage",CoreRuntime.balance)&&source.is(DamageTypeTags.IS_FIRE))add(p,"fire_damage",lost);
            if(p.wantsCounter("faust_epic_magic_damage",CoreRuntime.balance)&&(source.is(DamageTypes.MAGIC)||source.is(DamageTypes.INDIRECT_MAGIC)||source.is(DamageTypes.WITHER)||source.is(DamageTypes.DRAGON_BREATH)))add(p,"magic_damage",lost);
        }
        if(source.getEntity() instanceof ServerPlayer player&&target!=player&&com.faust.essence.api.EssenceApi.active(player)&&!player.isAlliedTo(target)){
            var p=CoreRuntime.data(player);
            if(p.wantsCounter("faust_epic_heavy_attack",CoreRuntime.balance)&&source.getDirectEntity()==player&&!source.is(DamageTypeTags.IS_PROJECTILE)&&lost>15)add(p,"heavy_attack",1);
            if(killed){
                if(target instanceof ServerPlayer)add(p,"player_kills",1);
                else if(target instanceof Mob){add(p,target instanceof Enemy?"hostile_kills":"peaceful_kills",1);if(exhaustedAttacker)add(p,"exhausted_kills",1);}
            }
        }
    }
    public static void sleep(ServerPlayer player,int roll){
        if(!com.faust.essence.api.EssenceApi.active(player))return;
        var p=CoreRuntime.data(player);if(!p.wantsCounter("faust_epic_sleep_luck",CoreRuntime.balance)||roll!=0)return;add(p,"sleep_luck",1);
        // A successful sleep roll opens Catharsis without requiring a manual purchase.
        String id="faust_epic_compat:epicfight/catharsis";
        if(CoreRuntime.balance.abilities.stream().anyMatch(a->a.id().equals(id)))p.unlock(id,CoreRuntime.balance);

    }
}
