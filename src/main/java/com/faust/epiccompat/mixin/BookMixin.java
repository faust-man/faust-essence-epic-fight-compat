package com.faust.epiccompat.mixin;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import yesman.epicfight.world.item.SkillBookItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(SkillBookItem.class)
public class BookMixin {
    @Inject(method="use",at=@At("HEAD"),cancellable=true)
    private void disable(Level level,Player player,InteractionHand hand,CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir){cir.setReturnValue(InteractionResultHolder.fail(player.getItemInHand(hand)));}
}
