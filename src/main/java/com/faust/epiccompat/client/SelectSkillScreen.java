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
    private net.minecraft.client.gui.components.Button unlockButton;
    private boolean learned(){return com.faust.core.client.CoreClient.progress!=null&&com.faust.core.client.CoreClient.progress.unlocked.contains(com.faust.epiccompat.Compat.nodeId(selected));}
    @Override protected void init(){
        super.init();
        var button=getLearnButton();
        unlockButton=addRenderableWidget(net.minecraft.client.gui.components.Button.builder(net.minecraft.network.chat.Component.translatable("faust_epic_compat.unlock"),ignored->{
            if(!learned()&&minecraft!=null&&minecraft.getConnection()!=null)minecraft.getConnection().sendCommand("essence unlock "+com.faust.epiccompat.Compat.nodeId(selected));
        }).bounds(button.getX(),button.getY(),button.getWidth(),button.getHeight()).build());
        refreshMoverButton();
    }
    @Override public void tick(){super.tick();refreshMoverButton();}
    private void refreshMoverButton(){
        if(getLearnButton()==null||unlockButton==null)return;
        boolean learned=learned();
        unlockButton.visible=!learned;
        var progress=com.faust.core.client.CoreClient.progress;var balance=com.faust.core.client.CoreClient.balance;
        var ability=balance==null?null:balance.abilities.stream().filter(a->a.id().equals(com.faust.epiccompat.Compat.nodeId(selected))).findFirst().orElse(null);
        unlockButton.active=!learned&&progress!=null&&ability!=null&&progress.canUnlock(ability,balance);
        getLearnButton().visible=learned;
        if(!learned){getLearnButton().active=false;return;}
        getLearnButton().active=playerpatch.getSkillContainerFor(selected).isEmpty()&&(selected.getPriorSkill()==null||playerpatch.getSkillContainerFor(selected.getPriorSkill()).isPresent());
        if(selected.getCategory()!=SkillCategories.MOVER)return;
        var container=playerpatch.getSkill(SkillSlots.MOVER);
        boolean equipped=container.getSkill()==selected;
        getLearnButton().active=!equipped;
        getLearnButton().setMessage(net.minecraft.network.chat.Component.translatable(equipped?"faust_epic_compat.equipped":"faust_epic_compat.select_skill"));
    }
    @Override protected void acquireSkillTo(SkillContainer container){
        if(!learned()||container.getSlot().category()!=selected.getCategory()||!com.faust.epiccompat.Combat.allowedSlot(container.getSlot()))return;
        // No optimistic mutation: the server validates Core ownership and slot category.
        EpicFightNetworkManager.sendToServer(new CPChangeSkill(container.getSlot(),selected.holder(),-1));
        onClose();
    }
    @Override protected boolean consumesItem(){return false;}
}
