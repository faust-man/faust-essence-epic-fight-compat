package com.faust.epiccompat.client;
import com.faust.essence.api.client.ClientApi;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
public final class ClientBridge {
 private ClientBridge(){}
 public static boolean canStrike(PlayerPatch<?> patch){
  if(ClientApi.snapshot().isEmpty())return false;var player=patch.getOriginal();
  if(player.isCreative()||player.isSpectator()||!ClientApi.resourceEnabled())return true;
  if(health())return player.isAlive()&&player.getHealth()>0;
  return ClientApi.stamina().map(state->!state.exhausted()).orElse(false);
 }
 public static boolean canSpend(float amount){var player=Minecraft.getInstance().player;return ClientApi.stamina().map(state->com.faust.epiccompat.Rules.canSpend(state.current(),amount,state.exhausted(),player!=null&&(player.isCreative()||player.isSpectator()||!ClientApi.resourceEnabled()))).orElse(false);}
 public static boolean health(){var player=Minecraft.getInstance().player;return player!=null&&ClientApi.stamina().map(com.faust.essence.api.ResourceProvider.ResourceState::exhausted).orElse(false)&&yesman.epicfight.world.capabilities.EpicFightCapabilities.getUnparameterizedEntityPatch(player,PlayerPatch.class).filter(com.faust.epiccompat.Forbidden::equipped).isPresent();}
 public static void stamina(PlayerPatch<?> patch,CallbackInfoReturnable<Float> cir,boolean max){if(patch.getOriginal()==Minecraft.getInstance().player)ClientApi.stamina().ifPresent(state->cir.setReturnValue((float)(max?state.maximum():state.current())));}
}
