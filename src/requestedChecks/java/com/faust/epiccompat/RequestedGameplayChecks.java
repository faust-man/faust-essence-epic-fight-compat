package com.faust.epiccompat;
import com.faust.core.runtime.CoreRuntime;
import com.faust.essence.api.Skill;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.*;
import net.minecraft.world.InteractionHand;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
@net.neoforged.fml.common.EventBusSubscriber(modid="faust_epic_compat")
public final class RequestedGameplayChecks {
 private static void check(boolean ok,String why){if(!ok)throw new AssertionError(why);}
 @net.neoforged.bus.api.SubscribeEvent public static void started(net.neoforged.neoforge.event.server.ServerStartedEvent event){
  try{
   var player=net.neoforged.neoforge.common.util.FakePlayerFactory.get(event.getServer().overworld(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"RequestedChecks"));
   player.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.SURVIVAL);player.setOnGround(true);
   var b=CoreRuntime.balance;var p=CoreRuntime.data(player);p.max(b);var patch=EpicFightCapabilities.getEntityPatch(player,ServerPlayerPatch.class);patch.toEpicFightMode(false);
   player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,200,0));CoreRuntime.tick(player);check(!p.counters.containsKey("speed_2_received"),"Speed I does not satisfy II");
   player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,200,1));player.addEffect(new MobEffectInstance(MobEffects.REGENERATION,200,2));player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,200,1));CoreRuntime.tick(player);
   check(p.counters.get("speed_2_received")==1&&p.counters.get("regeneration_2_received")==1&&p.counters.get("strength_2_received")==1,"real effects satisfy milestones");
   player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.IRON_SWORD));p.fatigue=0;p.stamina=20;p.exhausted=false;
   if(com.faust.core.progression.StaminaServices.present()){Combat.strike(patch,InteractionHand.MAIN_HAND,yesman.epicfight.gameasset.Animations.SWORD_AUTO1.get());double ground=20-p.stamina;p.stamina=20;p.fatigue=0;
   Combat.strike(patch,InteractionHand.MAIN_HAND,yesman.epicfight.gameasset.Animations.SWORD_AIR_SLASH.get());check(Math.abs(20-p.stamina-ground*2)<0.001,"air weapon attack costs double actual modified ground cost");}
   var innate=patch.getSkill(SkillSlots.WEAPON_INNATE);innate.setSkill(yesman.epicfight.registry.entries.EpicFightSkills.LIECHTENAUER.get());innate.setResource(2);p.track(Skill.valueOf("intelligence")).level=10;
   check(Math.abs(AcceleratedThinking.charge(innate,4)-4.4)<0.001,"charge bonus applies to gain only");p.disabled.add(AcceleratedThinking.ID);check(AcceleratedThinking.charge(innate,4)==4,"disabled thinking does not affect charge");
   player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(yesman.epicfight.registry.entries.EpicFightItems.IRON_LONGSWORD.get()));player.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);player.setYRot(0);
   var liecht=yesman.epicfight.registry.entries.EpicFightSkills.LIECHTENAUER.get();innate.setSkill(liecht);innate.activate();
   var states=new yesman.epicfight.api.utils.datastructure.ParameterizedHashMap<yesman.epicfight.api.animation.types.EntityState.StateFactor<?>>();states.put(yesman.epicfight.api.animation.types.EntityState.PHASE_LEVEL,2);patch.updateEntityState(new yesman.epicfight.api.animation.types.EntityState(states));
   var attacker=net.minecraft.world.entity.EntityType.PIG.create(player.serverLevel());attacker.setPos(player.getX(),player.getY(),player.getZ()+2);
   var deflect=liecht.getClass().getDeclaredMethod("lambda$onInitiate$2",yesman.epicfight.skill.SkillContainer.class,yesman.epicfight.api.event.types.entity.TakeDamageEvent.Income.class);deflect.setAccessible(true);
   for(boolean master:new boolean[]{false,true}){if(master)p.disabled.remove(WeaponMastery.LONG);else p.disabled.add(WeaponMastery.LONG);double price=master?0.5:1;
    for(boolean enough:new boolean[]{false,true}){p.stamina=enough?5:price-0.01;p.fatigue=0;p.exhausted=false;if(!com.faust.core.progression.StaminaServices.present())patch.setStamina((float)p.stamina);double before=patch.getStamina();var hit=new yesman.epicfight.api.event.types.entity.TakeDamageEvent.Income(patch,player.damageSources().mobAttack(attacker),2);deflect.invoke(liecht,innate,hit);check(hit.isCanceled()==enough,"deflection requires full stamina payment");check(Math.abs(patch.getStamina()-(enough?before-price:before))<0.001,"exact per-deflection price");}
   }
   p.unlocked.remove("faust_epic_compat:epicfight/roll");p.setCounter("faust_epic_side_jumps",0,b);p.stamina=20;p.exhausted=false;patch.setStamina(20);player.setSprinting(false);player.setYRot(0);player.setDeltaMovement(0.2,0,0);player.jumpFromGround();check(p.counters.get("faust_epic_side_jumps")==1,"actual lateral jump counted");player.setDeltaMovement(0,0,0.2);player.jumpFromGround();check(p.counters.get("faust_epic_side_jumps")==1,"forward jump does not count sideways");
   var enchantPos=player.blockPosition().above(8);var saved=new java.util.HashMap<net.minecraft.core.BlockPos,net.minecraft.world.level.block.state.BlockState>();var world=player.serverLevel();
   try{saved.put(enchantPos,world.getBlockState(enchantPos));world.setBlockAndUpdate(enchantPos,net.minecraft.world.level.block.Blocks.ENCHANTING_TABLE.defaultBlockState());
    for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++)for(int y=0;y<=1;y++){var at=enchantPos.offset(x,y,z);if(x==0&&z==0&&y==0)continue;saved.put(at,world.getBlockState(at));world.setBlockAndUpdate(at,Math.max(Math.abs(x),Math.abs(z))==2?net.minecraft.world.level.block.Blocks.BOOKSHELF.defaultBlockState():net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());}
    p.track(Skill.valueOf("intelligence")).level=20;p.disabled.remove("faustcore:enchanting_power");var menu=new net.minecraft.world.inventory.EnchantmentMenu(99,player.getInventory(),net.minecraft.world.inventory.ContainerLevelAccess.create(world,enchantPos));menu.getSlot(0).set(new ItemStack(Items.IRON_SWORD));menu.slotsChanged(menu.getSlot(0).container);check(menu.costs[2]>=44,"enchanting power exceeds vanilla level30 table cap");p.disabled.add("faustcore:enchanting_power");menu.slotsChanged(menu.getSlot(0).container);check(menu.costs[2]==30,"disabled enchant power retains vanilla cap");
   }finally{for(var e:saved.entrySet())world.setBlockAndUpdate(e.getKey(),e.getValue());}
   var tree=b.trees.stream().filter(t->t.skill()==Skill.valueOf("intelligence")).findFirst().orElseThrow();var thinking=tree.nodes().stream().filter(n->n.ability().equals(AcceleratedThinking.ID)).findFirst().orElseThrow();var revelation=tree.nodes().stream().filter(n->n.ability().endsWith("/revelation")).findFirst().orElseThrow();check(thinking.x()<revelation.x(),"thinking left of revelation");
   var pos=player.blockPosition().above(4);var level=player.serverLevel();var previous=level.getBlockState(pos);
   try{p.unlocked.remove("faustcore:training_intelligence_luck");p.setCounter("world_chests",0,b);level.setBlockAndUpdate(pos,net.minecraft.world.level.block.Blocks.CHEST.defaultBlockState());var chest=(net.minecraft.world.level.block.entity.ChestBlockEntity)level.getBlockEntity(pos);chest.setLootTable(net.minecraft.world.level.storage.loot.BuiltInLootTables.SIMPLE_DUNGEON);chest.unpackLootTable(player);check(p.counters.get("world_chests")==1,"world chest counts first generation");chest.unpackLootTable(player);check(p.counters.get("world_chests")==1,"repeat chest opening does not count");}finally{level.setBlockAndUpdate(pos,previous);}
   var book=new net.minecraft.world.entity.item.ItemEntity(player.level(),player.getX(),player.getY(),player.getZ(),new ItemStack(yesman.epicfight.registry.entries.EpicFightItems.SKILLBOOK.get()));check(!player.serverLevel().addFreshEntity(book),"skill books cannot spawn");
   System.out.println("REQUESTED GAMEPLAY CHECKS PASSED");
  }catch(Throwable failure){failure.printStackTrace();System.out.println("REQUESTED GAMEPLAY CHECKS FAILED");throw new AssertionError(failure);}
  finally{event.getServer().halt(false);}
 }
}
