package com.faust.epiccompat;
import com.faust.core.runtime.*;
import net.minecraft.world.item.*;
@net.neoforged.fml.common.EventBusSubscriber(modid="faust_epic_compat")
public final class ForestRuntimeChecks {
 private static void check(boolean ok,String why){if(!ok)throw new AssertionError(why);}
 @net.neoforged.bus.api.SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.HIGHEST)
 public static void started(net.neoforged.neoforge.event.server.ServerStartedEvent e){
  var level=e.getServer().overworld();var player=net.neoforged.neoforge.common.util.FakePlayerFactory.get(level,new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"ForestChecks"));
  var p=CoreRuntime.data(player);var b=CoreRuntime.balance;p.max(b);p.unlocked.remove("faustcore:cat_scent");p.setCounter("cats_tamed",0,b);
  var cat=new net.minecraft.world.entity.animal.Cat(net.minecraft.world.entity.EntityType.CAT,level);cat.tame(player);
  var ability=b.abilities.stream().filter(a->a.id().equals("faustcore:cat_scent")).findFirst().orElseThrow();check(p.canUnlock(ability,b),"taming cat records practice");
  player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.FEET,ItemStack.EMPTY);
  check(net.minecraft.world.level.block.PowderSnowBlock.canEntityWalkOnPowderSnow(player),"soft foot walks on snow");p.disabled.add("faustcore:soft_foot");
  check(!net.minecraft.world.level.block.PowderSnowBlock.canEntityWalkOnPowderSnow(player),"disabled soft foot sinks");
  new net.minecraft.world.entity.monster.Creeper(net.minecraft.world.entity.EntityType.CREEPER,level);
  var menu=player.inventoryMenu;
  menu.getSlot(1).set(new ItemStack(Items.OAK_SAPLING));menu.slotsChanged(player.getInventory());check(menu.getSlot(0).getItem().is(Items.STICK)&&menu.getSlot(0).getItem().getCount()==2,"sapling yields two sticks");
  menu.getSlot(1).set(new ItemStack(Items.STICK));menu.getSlot(2).set(new ItemStack(Items.STICK));menu.slotsChanged(player.getInventory());check(menu.getSlot(0).getItem().is(FireStarterItem.ITEM.get()),"two sticks yield fire starter");
  menu.getSlot(1).set(new ItemStack(FireStarterItem.ITEM.get()));menu.getSlot(2).set(new ItemStack(Items.OAK_LOG));menu.slotsChanged(player.getInventory());check(menu.getSlot(0).getItem().is(Items.CAMPFIRE),"starter and log yield campfire");
  p.disabled.add("faustcore:fire_starter");menu.slotsChanged(player.getInventory());check(menu.getSlot(0).getItem().isEmpty(),"disabled skill blocks special craft");
  System.out.println("Forest gameplay checks passed");
 }
}
