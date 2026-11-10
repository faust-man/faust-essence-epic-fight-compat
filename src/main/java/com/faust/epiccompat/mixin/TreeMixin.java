package com.faust.epiccompat.mixin;
import com.faust.core.client.*;
import com.faust.core.progression.SkillTrees;
import com.faust.epiccompat.Compat;
import com.faust.epiccompat.client.SelectSkillScreen;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=CoreScreen.class,remap=false)
public abstract class TreeMixin {
    @Inject(method="icon",at=@At("HEAD"),cancellable=true)
    private static void epicIcon(String id,CallbackInfoReturnable<net.minecraft.world.item.ItemStack> cir){var skill=Compat.skill(id);if(skill!=null){var stack=new net.minecraft.world.item.ItemStack(yesman.epicfight.registry.entries.EpicFightItems.SKILLBOOK.get());yesman.epicfight.world.item.SkillBookItem.setContainingSkill(skill.holder(),stack);cir.setReturnValue(stack);}}
    @Redirect(method="drawTree",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/GuiGraphics;renderItem(Lnet/minecraft/world/item/ItemStack;II)V"))
    private void texture(net.minecraft.client.gui.GuiGraphics graphics,net.minecraft.world.item.ItemStack stack,int x,int y){var skill=yesman.epicfight.world.item.SkillBookItem.getContainSkill(stack);if(skill.isPresent())graphics.blit(skill.get().value().getSkillTexture(),x,y,16,16,0,0,1,1,1,1);else graphics.renderItem(stack,x,y);}
    @Inject(method="abilityName",at=@At("HEAD"),cancellable=true)
    private static void name(com.faust.core.progression.Balance.Ability ability,CallbackInfoReturnable<net.minecraft.network.chat.Component> cir){
        if(Compat.isMastery(ability.id())){cir.setReturnValue(net.minecraft.network.chat.Component.translatable("faust_epic_compat."+ability.id().split(":")[1]));return;}
        var skill=Compat.skill(ability.id());if(skill!=null)cir.setReturnValue(net.minecraft.network.chat.Component.translatable(skill.getTranslationKey()));
    }
    @Inject(method="abilityDescription",at=@At("HEAD"),cancellable=true)
    private static void masteryDescription(com.faust.core.progression.Balance.Ability ability,com.faust.core.progression.Progress progress,com.faust.core.progression.Balance balance,CallbackInfoReturnable<java.util.ArrayList<net.minecraft.network.chat.Component>> cir){
        if(ability.id().equals(com.faust.epiccompat.Combat.SHIELD_EFFICIENCY))return;
        if(Compat.isMastery(ability.id()))cir.setReturnValue(new java.util.ArrayList<>(java.util.List.of(net.minecraft.network.chat.Component.translatable("faust_epic_compat."+ability.id().split(":")[1]+".description"))));
    }
    @Shadow private SkillTrees.Node hovered(double x,double y){throw new AssertionError();}
    @Inject(method="mouseClicked",at=@At("HEAD"),cancellable=true)
    private void select(double x,double y,int button,CallbackInfoReturnable<Boolean> cir){
        if(button!=0||CoreClient.progress==null)return;
        var node=hovered(x,y);if(node==null)return;
        var skill=Compat.skill(node.ability());var mc=Minecraft.getInstance();
        if(skill!=null&&mc.player!=null){mc.setScreen(new SelectSkillScreen(mc.player,skill,(CoreScreen)(Object)this));cir.setReturnValue(true);}
    }
}
