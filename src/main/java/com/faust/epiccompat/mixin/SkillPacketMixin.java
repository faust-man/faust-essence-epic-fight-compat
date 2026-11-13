package com.faust.epiccompat.mixin;
import com.faust.epiccompat.Compat;
import com.faust.core.runtime.CoreRuntime;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import yesman.epicfight.network.client.CPChangeSkill;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(targets="yesman.epicfight.network.EpicFightServerBoundPayloadHandler",remap=false)
public interface SkillPacketMixin {
    @Inject(method="handleChangeSkill",at=@At("RETURN"))
    private static void confirm(CPChangeSkill data,IPayloadContext context,CallbackInfo ci){
        if(context.player() instanceof ServerPlayer player)
            yesman.epicfight.world.capabilities.EpicFightCapabilities.getUnparameterizedEntityPatch(player,yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch.class).ifPresent(patch->{
                var actual=patch.getSkill(data.skillSlot()).getSkill();
                yesman.epicfight.network.EpicFightNetworkManager.sendToPlayer(new yesman.epicfight.network.server.SPChangeSkill(data.skillSlot(),player.getId(),yesman.epicfight.skill.Skill.holderOrNull(actual)),player);
            });
    }
    @Inject(method="handleChangeSkill",at=@At("HEAD"),cancellable=true)
    private static void authorize(CPChangeSkill data,IPayloadContext context,CallbackInfo ci){
        if(!(context.player() instanceof ServerPlayer player)){ci.cancel();return;}
        if(data.skillBookSlotIndex()!=-1||!data.skillSlot().category().learnable()||!com.faust.epiccompat.Combat.allowedSlot(data.skillSlot())){ci.cancel();return;}
        if(data.skill()==null)return;
        var skill=data.skill().value();
        if(!skill.getCategory().learnable()||data.skillSlot().category()!=skill.getCategory()||!com.faust.essence.api.EssenceApi.learned(player,Compat.nodeId(skill)))ci.cancel();
    }
}
