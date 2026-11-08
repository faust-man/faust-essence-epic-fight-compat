package com.faust.epiccompat;

import com.faust.core.runtime.CoreRuntime;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import yesman.epicfight.skill.*;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import java.util.*;

/** Server-thread phase payments. Weak keys do not retain disconnected player patches. */
public final class Combat {
    public static final String SHIELD_EFFICIENCY="faust_epic_compat:shield_efficiency";
    public static final String FIST_POWER="faust_epic_compat:fist_power";
    public static final String MASTERY="faust_epic_compat:two_handed_mastery";
    public static final String FIST_MASTERY="faust_epic_compat:fist_mastery";
    public static final String SHIELD_MASTERY="faust_epic_compat:shield_weapon_mastery";
    public static final String SWORD_MASTERY="faust_epic_compat:dual_sword_mastery", DAGGER_MASTERY="faust_epic_compat:dual_dagger_mastery";
    private static final Map<PlayerPatch<?>,Boolean> PAID=new WeakHashMap<>();
    private static final ThreadLocal<ServerPlayer> COLLISION=new ThreadLocal<>();
    private Combat(){}
    public static boolean allowedSlot(SkillSlot slot){return slot.category()!=SkillCategories.PASSIVE||Rules.passiveSlot(slot.toString());}
    public static boolean prepaid(ServerPlayer player){return COLLISION.get()==player;}
    public static void collide(ServerPlayer player,Runnable operation){
        ServerPlayer previous=COLLISION.get();COLLISION.set(player);
        try{operation.run();}finally{if(previous==null)COLLISION.remove();else COLLISION.set(previous);}
    }
    public static boolean paid(PlayerPatch<?> patch){return PAID.getOrDefault(patch,false);}
    public static void begin(PlayerPatch<?> patch){PAID.remove(patch);}
    public static double cost(PlayerPatch<?> patch,InteractionHand hand,com.faust.core.progression.Progress progress,com.faust.core.progression.Balance balance){
        var player=patch.getOriginal();
        var cap=patch.getAdvancedHoldingItemCapability(hand);
        boolean twoHanded=cap.getWeaponCategory()==CapabilityItem.WeaponCategories.GREATSWORD||!cap.getStyle(patch).canUseOffhand();
        boolean spear=cap.getWeaponCategory()==CapabilityItem.WeaponCategories.SPEAR;
        boolean fist=cap.getWeaponCategory()==CapabilityItem.WeaponCategories.FIST||player.getItemInHand(hand).isEmpty();
        var otherHand=hand==InteractionHand.MAIN_HAND?InteractionHand.OFF_HAND:InteractionHand.MAIN_HAND;
        var category=cap.getWeaponCategory();
        boolean shield=category!=CapabilityItem.WeaponCategories.NOT_WEAPON&&category!=CapabilityItem.WeaponCategories.SHIELD&&category!=CapabilityItem.WeaponCategories.RANGED
                &&player.getItemInHand(otherHand).canPerformAction(net.neoforged.neoforge.common.ItemAbilities.SHIELD_BLOCK);
        double cost=Rules.strikeCost(progress.actionCost(player.getItemInHand(hand).is(net.minecraft.world.item.Items.MACE)?"mace":"attack",balance),twoHanded,spear,progress.enabled(MASTERY,balance),fist,progress.enabled(FIST_MASTERY,balance),shield,progress.enabled(SHIELD_MASTERY,balance));
        var other=yesman.epicfight.world.capabilities.EpicFightCapabilities.getItemStackCapability(player.getItemInHand(otherHand)).getWeaponCategory();
        boolean swords=category==CapabilityItem.WeaponCategories.SWORD&&other==category;
        boolean daggers=category==CapabilityItem.WeaponCategories.DAGGER&&other==category;
        return Rules.dualCost(cost,swords||daggers,progress.enabled(swords?SWORD_MASTERY:DAGGER_MASTERY,balance));
    }
    public static boolean canStrike(PlayerPatch<?> patch){
        if(patch.getOriginal() instanceof ServerPlayer p){
            double cost=cost(patch,patch.getPrimaryHand(),CoreRuntime.data(p),CoreRuntime.balance);
            return !CoreRuntime.active(p)||CoreRuntime.balance.stamina.get("enabled")==0||(Forbidden.mode(p)?Forbidden.afford(p,cost):!CoreRuntime.data(p).actionsBlocked(CoreRuntime.balance));
        }
        return com.faust.epiccompat.client.ClientBridge.canStrike(patch);
    }
    public static void strike(PlayerPatch<?> patch,InteractionHand hand){
        if(!(patch.getOriginal() instanceof ServerPlayer player))return;
        double cost=cost(patch,hand,CoreRuntime.data(player),CoreRuntime.balance);
        PAID.put(patch,CoreRuntime.pay(player,cost));
    }
}
