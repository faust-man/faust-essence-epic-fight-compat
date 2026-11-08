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
    @Inject(method="abilityName",at=@At("HEAD"),cancellable=true)
    private static void name(com.faust.core.progression.Balance.Ability ability,CallbackInfoReturnable<net.minecraft.network.chat.Component> cir){var skill=Compat.skill(ability.id());if(skill!=null)cir.setReturnValue(net.minecraft.network.chat.Component.translatable(skill.getTranslationKey()));}
    @Shadow private SkillTrees.Node hovered(double x,double y){throw new AssertionError();}
    @Inject(method="mouseClicked",at=@At("HEAD"),cancellable=true)
    private void select(double x,double y,int button,CallbackInfoReturnable<Boolean> cir){
        if(button!=0||CoreClient.progress==null)return;
        var node=hovered(x,y);if(node==null||!CoreClient.progress.unlocked.contains(node.ability()))return;
        var skill=Compat.skill(node.ability());var mc=Minecraft.getInstance();
        if(skill!=null&&mc.player!=null){mc.setScreen(new SelectSkillScreen(mc.player,skill,(CoreScreen)(Object)this));cir.setReturnValue(true);}
    }
}
