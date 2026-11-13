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
 public static java.util.List<Balance.Ability> defaults(){return java.util.List.of(new Balance.Ability(LONG,com.faust.core.progression.Skill.defense,15,USES,50,"faust_epic_compat:epicfight/parrying","faust_epic_compat:weapon_mastery",0),new Balance.Ability(GREAT,com.faust.core.progression.Skill.strength,15,SWINGS,250,StaminaServices.present()?Combat.MASTERY:"","faust_epic_compat:weapon_mastery",0));}
 public static boolean enabled(Player player,String id){return player instanceof ServerPlayer p?CoreRuntime.active(p)&&CoreRuntime.data(p).enabled(id,CoreRuntime.balance):player.level().isClientSide&&id.equals(LONG)&&CLIENT_LONG.getAsBoolean();}
 public static yesman.epicfight.skill.Skill innate(Player player,ItemStack stack){return EpicFightCapabilities.getUnparameterizedEntityPatch(player,yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch.class).map(patch->EpicFightCapabilities.getItemStackCapability(stack).getInnateSkill(patch,stack)).orElse(null);}
 public static boolean great(Player player,ItemStack stack){return !stack.isEmpty()&&innate(player,stack) instanceof yesman.epicfight.skill.weaponinnate.SteelWhirlwindSkill;}
 public static boolean longSword(Player player,ItemStack stack){return !stack.isEmpty()&&innate(player,stack) instanceof yesman.epicfight.skill.weaponinnate.LiechtenauerSkill;}
 public static float armorMultiplier(DamageSource source){if(!(source instanceof EpicFightDamageSource epic)||!(source.getEntity() instanceof ServerPlayer player))return 1;var stack=epic.getUsedItem().isEmpty()?player.getMainHandItem():epic.getUsedItem();return great(player,stack)&&enabled(player,GREAT)?1.5F:longSword(player,stack)&&enabled(player,LONG)?2:1;}
 private static final java.util.Map<ServerPlayer,Long> LAST_ATTACK=new java.util.WeakHashMap<>();
 public static void markAttack(ServerPlayer player){LAST_ATTACK.put(player,player.serverLevel().getGameTime());}
 public static boolean windBlocked(ServerPlayer player){long tick=player.serverLevel().getGameTime();if(attacking(player)){LAST_ATTACK.put(player,tick);return true;}var last=LAST_ATTACK.get(player);return last!=null&&tick-last<20;}
 public static boolean attacking(ServerPlayer player){return EpicFightCapabilities.getUnparameterizedEntityPatch(player,yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch.class).map(patch->{var animation=patch.getAnimator().getPlayerFor(null);return animation.getRealAnimation().get() instanceof yesman.epicfight.api.animation.types.AttackAnimation&&!animation.isEnd();}).orElse(false);}
 public static void refreshSpeed(ServerPlayer player){
  var attribute=player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);if(attribute==null)return;double[] amounts={0,0,1};
  if(enabled(player,GREAT)&&great(player,player.getMainHandItem()))player.getMainHandItem().forEachModifier(net.minecraft.world.entity.EquipmentSlot.MAINHAND,(type,modifier)->{var active=attribute.getModifier(modifier.id());if(type.equals(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED)&&active!=null&&active.amount()<0)switch(active.operation()){case ADD_VALUE->amounts[0]-=active.amount();case ADD_MULTIPLIED_BASE->amounts[1]-=active.amount();case ADD_MULTIPLIED_TOTAL->amounts[2]*=1+active.amount();}});
  amounts[2]=amounts[2]>0?1/amounts[2]-1:0;
  for(int i=0;i<3;i++){var id=net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("faust_epic_compat","great_speed_"+i);var old=attribute.getModifier(id);var op=net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.values()[i];if(old!=null&&(Math.abs(old.amount()-amounts[i])>1e-8||old.operation()!=op)){attribute.removeModifier(id);old=null;}if(Math.abs(amounts[i])<1e-8){if(old!=null)attribute.removeModifier(id);}else if(old==null)attribute.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(id,amounts[i],op));}
 }
 public static boolean mastered(DamageSource source){return source instanceof EpicFightDamageSource epic&&source.getEntity() instanceof ServerPlayer player&&enabled(player,GREAT)&&great(player,epic.getUsedItem().isEmpty()?player.getMainHandItem():epic.getUsedItem());}
 public static boolean pierces(DamageSource source){return mastered(source)&&source.is(EpicFightDamageTypeTags.WEAPON_INNATE);}
 public static void swing(ServerPlayer player){if(CoreRuntime.active(player)&&great(player,player.getMainHandItem()))CombatPractice.add(player,SWINGS,1);}
 public static void used(SkillContainer container){
  if(!(container.getExecutor().getOriginal() instanceof ServerPlayer p)||!CoreRuntime.active(p)||container.getSlot().category()!=SkillCategories.WEAPON_INNATE)return;
  var category=EpicFightCapabilities.getItemStackCapability(p.getMainHandItem()).getWeaponCategory();
  if(longSword(p,p.getMainHandItem())&&(!(container.getSkill() instanceof yesman.epicfight.skill.weaponinnate.LiechtenauerSkill)||container.isActivated()))CombatPractice.add(p,USES,1);
 }
 public static boolean extendedParry(SkillContainer container){
  if(!container.isActivated()||!enabled(container.getExecutor().getOriginal(),LONG))return false;
  var animationPlayer=container.getExecutor().getAnimator().getPlayerFor(null);var animation=animationPlayer.getRealAnimation().get();
  if(!(animation instanceof yesman.epicfight.api.animation.types.AttackAnimation attack))return false;
  float time=animationPlayer.getElapsedTime();var phase=attack.getPhaseByTime(time);
  return phase!=null&&time>=phase.start&&time<Math.min(phase.end,phase.start+2*(phase.contact-phase.start));
 }
}
