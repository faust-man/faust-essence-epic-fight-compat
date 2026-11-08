package com.faust.epiccompat.client;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import yesman.epicfight.client.gui.screen.SkillBookScreen;
import yesman.epicfight.skill.*;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.client.CPChangeSkill;
public final class SelectSkillScreen extends SkillBookScreen {
    private final Skill selected;
    public SelectSkillScreen(Player player,Skill skill,Screen parent){super(player,skill,InteractionHand.MAIN_HAND,parent);selected=skill;}
    @Override protected void acquireSkillTo(SkillContainer container){
        // No optimistic mutation: the server validates Core ownership and EF cooldown.
        EpicFightNetworkManager.sendToServer(new CPChangeSkill(container.getSlot(),selected.holder(),-1));
        onClose();
    }
    @Override protected boolean consumesItem(){return false;}
}
