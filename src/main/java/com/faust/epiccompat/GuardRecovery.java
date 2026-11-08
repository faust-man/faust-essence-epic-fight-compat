package com.faust.epiccompat;
import net.minecraft.server.level.ServerPlayer;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
public final class GuardRecovery {
    private GuardRecovery(){}
    public static boolean holding(PlayerPatch<?> patch){
        if(!patch.isHoldingAny())return false;
        String id=patch.getHoldingSkill().asSkill().getRegistryName().toString();
        return id.equals("epicfight:guard")||id.equals("epicfight:impact_guard");
    }
    public static boolean blocked(ServerPlayer player){return EpicFightCapabilities.getUnparameterizedEntityPatch(player,PlayerPatch.class).filter(GuardRecovery::holding).isPresent();}
}
