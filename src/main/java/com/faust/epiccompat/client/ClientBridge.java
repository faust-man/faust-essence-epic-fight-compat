package com.faust.epiccompat.client;
import com.faust.core.client.CoreClient;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
public final class ClientBridge {
    public static boolean canStrike(PlayerPatch<?> patch){
        if(CoreClient.progress==null||CoreClient.balance==null)return false;
        var player=patch.getOriginal();if(player.isCreative()||player.isSpectator()||CoreClient.balance.stamina.get("enabled")==0)return true;
        if(health())return player.isAlive()&&player.getHealth()>0;
        return !CoreClient.progress.actionsBlocked(CoreClient.balance);
    }
    public static boolean canSpend(float amount){return CoreClient.progress!=null&&CoreClient.balance!=null&&com.faust.epiccompat.Rules.canSpend(CoreClient.progress.stamina,amount,CoreClient.progress.actionsBlocked(CoreClient.balance),Minecraft.getInstance().player!=null&&(Minecraft.getInstance().player.isCreative()||Minecraft.getInstance().player.isSpectator()||CoreClient.balance.stamina.get("enabled")==0));}
    public static boolean health(){var player=Minecraft.getInstance().player;return player!=null&&CoreClient.progress!=null&&CoreClient.balance!=null&&CoreClient.progress.actionsBlocked(CoreClient.balance)&&yesman.epicfight.world.capabilities.EpicFightCapabilities.getUnparameterizedEntityPatch(player,yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch.class).filter(com.faust.epiccompat.Forbidden::equipped).isPresent();}

    private ClientBridge(){}
    public static void stamina(PlayerPatch<?> patch,CallbackInfoReturnable<Float> cir,boolean max){
        if(patch.getOriginal()==Minecraft.getInstance().player&&CoreClient.progress!=null&&CoreClient.balance!=null)
            cir.setReturnValue((float)(max?CoreClient.progress.maxStamina(CoreClient.balance):CoreClient.progress.stamina));
    }
}
