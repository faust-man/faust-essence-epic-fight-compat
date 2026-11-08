package com.faust.epiccompat.mixin;

import com.faust.epiccompat.Milestones;
import com.faust.core.runtime.CoreRuntime;
import net.minecraft.server.level.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=CoreRuntime.class,remap=false)
public class MilestoneRuntimeMixin {
    @Inject(method="foodStat",at=@At("RETURN"))
    private static void cake(com.faust.core.progression.Progress progress,net.minecraft.stats.Stat<?> stat,int amount,com.faust.core.progression.Balance balance,org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir){
        if(cir.getReturnValue()&&balance.sweetFoods.getOrDefault("minecraft:cake",0.0)>0)Milestones.add(progress,"sweet_food",amount);
    }
    @Inject(method="food",at=@At("HEAD"))
    private static void food(ServerPlayer player,ItemStack food,CallbackInfo ci){
        if(!CoreRuntime.active(player))return;
        String id=BuiltInRegistries.ITEM.getKey(food.getItem()).toString();
        if(CoreRuntime.balance.sweetFoods.getOrDefault(id,0.0)>0)Milestones.add(CoreRuntime.data(player),"sweet_food",1);
    }
}
