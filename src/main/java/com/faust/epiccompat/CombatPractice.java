package com.faust.epiccompat;
import com.faust.core.runtime.CoreRuntime;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
public final class CombatPractice {
    public static final String SHIELD_HITS="faust_epic_shield_hits", DAGGER_HITS="faust_epic_dagger_hits";
    public static final String ROLLS="faust_epic_rolls";
    public static final String JUMPS="faust_epic_jumps";
    public static final String DAMAGE="faust_epic_damage_taken", FISTS="faust_epic_fist_kills", TWO_HAND="faust_epic_two_hand_kills", SWORDS="faust_epic_sword_kills", DUAL="faust_epic_dual_sword_kills";
    public static boolean weaponPracticeNeeded(ServerPlayer player){
        if(!CoreRuntime.active(player))return false;var p=CoreRuntime.data(player);var b=CoreRuntime.balance;
        return p.wantsCounter(FISTS,b)||p.wantsCounter(TWO_HAND,b)||p.wantsCounter(SWORDS,b)||p.wantsCounter(DUAL,b)||p.wantsCounter(SHIELD_HITS,b)||p.wantsCounter(DAGGER_HITS,b);
    }
    public static void add(ServerPlayer player,String counter,double amount){if(!Double.isFinite(amount)||amount<=0)return;CoreRuntime.data(player).recordCounter(counter,amount,CoreRuntime.balance);}
    public record Weapon(boolean fists,boolean swords,boolean dual,boolean twoHand){}
    public static Weapon weapon(ServerPlayer player,ItemStack used){
        var cap=EpicFightCapabilities.getItemStackCapability(used);var category=cap.getWeaponCategory();
        boolean fists=used.isEmpty()||category==CapabilityItem.WeaponCategories.FIST;
        boolean sword=used.getItem() instanceof SwordItem||java.util.Set.of(CapabilityItem.WeaponCategories.SWORD,CapabilityItem.WeaponCategories.GREATSWORD,CapabilityItem.WeaponCategories.LONGSWORD,CapabilityItem.WeaponCategories.TACHI,CapabilityItem.WeaponCategories.UCHIGATANA).contains(category)||category.getParents().contains(CapabilityItem.WeaponCategories.SWORD);
        boolean dual=category==CapabilityItem.WeaponCategories.SWORD&&EpicFightCapabilities.getItemStackCapability(player.getMainHandItem()).getWeaponCategory()==category&&EpicFightCapabilities.getItemStackCapability(player.getOffhandItem()).getWeaponCategory()==category;
        boolean twoHand=EpicFightCapabilities.getUnparameterizedEntityPatch(player,PlayerPatch.class).filter(patch->!used.isEmpty()&&category!=CapabilityItem.WeaponCategories.NOT_WEAPON&&category!=CapabilityItem.WeaponCategories.RANGED&&!cap.getStyle(patch).canUseOffhand()).isPresent();
        return new Weapon(fists,sword,dual,twoHand);
    }
    public static void kill(ServerPlayer player,Weapon weapon){
        if(!CoreRuntime.active(player))return;
        if(weapon.fists())add(player,FISTS,1);if(weapon.swords())add(player,SWORDS,1);if(weapon.dual())add(player,DUAL,1);if(weapon.twoHand())add(player,TWO_HAND,1);
    }
}
