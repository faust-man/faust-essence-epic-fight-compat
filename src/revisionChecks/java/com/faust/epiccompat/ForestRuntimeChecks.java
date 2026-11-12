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
  menu.getSlot(1).set(new ItemStack(FireStarterItem.ITEM.get()));menu.getSlot(2).set(new ItemStack(Items.OAK_LOG));menu.slotsChanged(player.getInventory());check(menu.getSlot(0).getItem().isEmpty(),"starter and log recipe removed");
  p.disabled.add("faustcore:fire_starter");menu.slotsChanged(player.getInventory());check(menu.getSlot(0).getItem().isEmpty(),"disabled skill blocks special craft");
  menu.getSlot(2).set(ItemStack.EMPTY);menu.getSlot(1).set(new ItemStack(Items.OAK_SAPLING,64));menu.slotsChanged(player.getInventory());
  menu.clicked(0,0,net.minecraft.world.inventory.ClickType.PICKUP,player);
  check(menu.getCarried().is(Items.STICK)&&menu.getCarried().getCount()==2,"single click produces 2 sticks");check(menu.getSlot(1).getItem().getCount()==63,"single click consumes exactly one sapling");menu.setCarried(ItemStack.EMPTY);
  for(int i=0;i<player.getInventory().items.size();i++)player.getInventory().items.set(i,ItemStack.EMPTY);
  menu.getSlot(1).set(new ItemStack(Items.OAK_SAPLING,64));menu.slotsChanged(player.getInventory());menu.clicked(0,0,net.minecraft.world.inventory.ClickType.QUICK_MOVE,player);
  check(menu.getSlot(1).getItem().isEmpty(),"shift craft consumes 64 saplings");int sticks=0;for(var stack:player.getInventory().items)if(stack.is(Items.STICK))sticks+=stack.getCount();check(sticks==128,"64 saplings produce 128 sticks");
  menu.getSlot(1).set(new ItemStack(Items.OAK_SAPLING,64));menu.slotsChanged(player.getInventory());p.disabled.add("faustcore:forester");menu.clicked(0,0,net.minecraft.world.inventory.ClickType.PICKUP,player);check(menu.getCarried().isEmpty()&&menu.getSlot(1).getItem().getCount()==64,"cannot take stale result after disabling forester");p.disabled.remove("faustcore:forester");
  p.unlocked.remove("faustcore:fire_starter");p.setCounter("charcoal_smelted",0,b);var furnace=new net.minecraft.world.inventory.FurnaceResultSlot(player,new net.minecraft.world.SimpleContainer(1),0,0,0);furnace.onTake(player,new ItemStack(Items.CHARCOAL));var fireSkill=b.abilities.stream().filter(a->a.id().equals("faustcore:fire_starter")).findFirst().orElseThrow();check(p.canUnlock(fireSkill,b),"charcoal from furnace unlock requirement");p.unlocked.add(fireSkill.id());
  check(FireStarterItem.ITEM.get().getDefaultMaxStackSize()==1,"starter max stack is one");
  var pos=new net.minecraft.core.BlockPos(1200,100,1200);level.setBlock(pos,net.minecraft.world.level.block.Blocks.OAK_LOG.defaultBlockState(),3);PlacedLogs.get(level).add(pos);check(PlacedLogs.get(level).contains(pos),"placed log tracking");level.setBlock(pos,net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),3);check(!PlacedLogs.get(level).contains(pos),"removed log loses marker");
  player.setPos(1200.5,102,1200.5);player.setXRot(90);player.xRotO=90;player.setYRot(0);player.yRotO=0;p.disabled.remove("faustcore:fire_starter");
  level.setBlock(pos,net.minecraft.world.level.block.Blocks.OAK_LOG.defaultBlockState(),3);PlacedLogs.get(level).add(pos);FireStarterItem.ITEM.get().finishUsingItem(new ItemStack(FireStarterItem.ITEM.get()),level,player);check(level.getBlockState(pos).is(net.minecraft.world.level.block.Blocks.CAMPFIRE),"ignite placed log top into campfire");
  level.setBlock(pos.below(),net.minecraft.world.level.block.Blocks.DIRT.defaultBlockState(),3);
  for(var block:java.util.List.of(net.minecraft.world.level.block.Blocks.SHORT_GRASS,net.minecraft.world.level.block.Blocks.DANDELION,net.minecraft.world.level.block.Blocks.OAK_SAPLING)){
   level.setBlock(pos,block.defaultBlockState(),3);FireStarterItem.ITEM.get().finishUsingItem(new ItemStack(FireStarterItem.ITEM.get()),level,player);check(level.getBlockState(pos).is(net.minecraft.world.level.block.Blocks.FIRE),"replace vegetation with fire "+block);
  }
  var food=player.getFoodData();player.setHealth(10);food.setFoodLevel(20);food.setSaturation(0);p.disabled.remove("faustcore:metabolism");for(int i=0;i<39;i++)food.tick(player);check(player.getHealth()==10,"metabolism waits 40 ticks");food.tick(player);check(player.getHealth()==11,"metabolism heals twice as fast");
  level.addNewPlayer(player);p.unlocked.add("faustcore:cat_scent");var creeper=new net.minecraft.world.entity.monster.Creeper(net.minecraft.world.entity.EntityType.CREEPER,level);
  for(int i=0;i<40;i++){creeper.setPos(player.getX()+4,player.getY(),player.getZ());creeper.setTarget(player);creeper.ignite();creeper.tick();check(!creeper.isIgnited(),"cat scent defuses creeper within five blocks");}
  check(creeper.isAlive()&&creeper.getSwelling(1)<0.1,"protected creeper never reaches explosion");
  player.discard();
  System.out.println("Forest gameplay checks passed");
 }
}
