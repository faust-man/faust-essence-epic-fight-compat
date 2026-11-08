package com.faust.epiccompat.client;
import com.faust.core.client.CoreClient;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
public final class ClientBridge {
    private ClientBridge(){}
    public static void stamina(PlayerPatch<?> patch,CallbackInfoReturnable<Float> cir,boolean max){
        if(patch.getOriginal()==Minecraft.getInstance().player&&CoreClient.progress!=null&&CoreClient.balance!=null)
            cir.setReturnValue((float)(max?CoreClient.progress.maxStamina(CoreClient.balance):CoreClient.progress.stamina));
    }
}
