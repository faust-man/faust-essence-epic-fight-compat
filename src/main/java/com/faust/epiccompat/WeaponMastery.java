package com.faust.epiccompat;
import com.faust.core.progression.*;
import com.faust.core.runtime.CoreRuntime;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.damagesource.DamageSource;
import yesman.epicfight.skill.*;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.damagesource.*;
public final class WeaponMastery {
 public static final String LONG="faust_epic_compat:longsword_mastery",GREAT="faust_epic_compat:greatsword_mastery";
 public static final String USES="faust_epic_longsword_uses",SWINGS="faust_epic_greatsword_swings",HITS="faust_epic_technician_hits";
 public static final ThreadLocal<Integer> GUARD_DEPTH=ThreadLocal.withInitial(()->0);
 public static java.util.function.BooleanSupplier CLIENT_LONG=()->false;
 private WeaponMastery(){}
 public static boolean custom(String id){return id.equals(LONG)||id.equals(GREAT);}
 public static java.util.List<Balance.Ability> defaults(){return java.util.List.of(new Balance.Ability(LONG,com.faust.core.progression.Skill.defense,15,USES,50,"faust_epic_compat:epicfight/parrying","faust_epic_compat:weapon_mastery",0),new Balance.Ability(GREAT,com.faust.core.progression.Skill.strength,15,SWINGS,100,"","faust_epic_compat:weapon_mastery",0));}
 public static boolean enabled(Player player,String id){return player instanceof ServerPlayer p?CoreRuntime.active(p)&&CoreRuntime.data(p).enabled(id,CoreRuntime.balance):player.level().isClientSide&&id.equals(LONG)&&CLIENT_LONG.getAsBoolean();}
 public static boolean great(ItemStack stack){var category=EpicFightCapabilities.getItemStackCapability(stack).getWeaponCategory();return !stack.isEmpty()&&(category==CapabilityItem.WeaponCategories.GREATSWORD||category.getParents().contains(CapabilityItem.WeaponCategories.GREATSWORD));}
 public static boolean mastered(DamageSource source){return source instanceof EpicFightDamageSource epic&&source.getEntity() instanceof ServerPlayer player&&enabled(player,GREAT)&&great(epic.getUsedItem().isEmpty()?player.getMainHandItem():epic.getUsedItem());}
 public static boolean pierces(DamageSource source){return mastered(source)&&source.is(EpicFightDamageTypeTags.WEAPON_INNATE);}
 public static void swing(ServerPlayer player){if(CoreRuntime.active(player)&&great(player.getMainHandItem()))CombatPractice.add(player,SWINGS,1);}
 public static void used(SkillContainer container){
  if(!(container.getExecutor().getOriginal() instanceof ServerPlayer p)||!CoreRuntime.active(p)||container.getSlot().category()!=SkillCategories.WEAPON_INNATE)return;
  var category=EpicFightCapabilities.getItemStackCapability(p.getMainHandItem()).getWeaponCategory();
  if(category==CapabilityItem.WeaponCategories.LONGSWORD&&(!(container.getSkill() instanceof yesman.epicfight.skill.weaponinnate.LiechtenauerSkill)||container.isActivated()))CombatPractice.add(p,USES,1);
 }
 public static boolean extendedParry(SkillContainer container){
  if(!container.isActivated()||!enabled(container.getExecutor().getOriginal(),LONG))return false;
  var animationPlayer=container.getExecutor().getAnimator().getPlayerFor(null);var animation=animationPlayer.getRealAnimation().get();
  if(!(animation instanceof yesman.epicfight.api.animation.types.AttackAnimation attack))return false;
  float time=animationPlayer.getElapsedTime();var phase=attack.getPhaseByTime(time);
  return phase!=null&&time>=phase.start&&time<Math.min(phase.end,phase.start+2*(phase.contact-phase.start));
 }
}
