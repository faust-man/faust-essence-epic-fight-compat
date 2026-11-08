package com.faust.epiccompat;
import com.faust.core.runtime.CoreRuntime;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.registry.EpicFightRegistries;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
public final class Forbidden {
    public static final ThreadLocal<ServerPlayer> TICK=new ThreadLocal<>();
    public static boolean equipped(PlayerPatch<?> patch){
        var skill=EpicFightRegistries.SKILL.get(ResourceLocation.parse("epicfight:forbidden_strength"));
        return skill!=null&&patch.getSkillContainerFor(skill).filter(c->!c.isDisabled()).isPresent();
    }
    public static boolean mode(ServerPlayer player){
        return CoreRuntime.active(player)&&CoreRuntime.balance.stamina.get("enabled")>0&&CoreRuntime.data(player).actionsBlocked(CoreRuntime.balance)
            &&EpicFightCapabilities.getUnparameterizedEntityPatch(player,PlayerPatch.class).filter(Forbidden::equipped).isPresent();
    }
    public static boolean afford(ServerPlayer player,double cost){return Double.isFinite(cost)&&cost>=0&&player.getHealth()>cost;}
    public static boolean pay(ServerPlayer player,double cost){if(!afford(player,cost))return false;player.setHealth((float)(player.getHealth()-cost));return true;}
}
