package com.faust.epiccompat;
import com.faust.core.runtime.*;
import com.faust.core.progression.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.*;
import net.minecraft.core.*;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.gameevent.*;
import net.neoforged.neoforge.common.util.FakePlayer;
public final class ExperienceGameplayChecks {
 private static void check(boolean ok,String why){if(!ok)throw new AssertionError(why);}
 private static final class SoundPlayer extends FakePlayer {
  int sounds;
  SoundPlayer(ServerLevel level){super(level,new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"FootstepChecks"));}
  @Override public boolean isInvulnerableTo(net.minecraft.world.damagesource.DamageSource source){return false;}
  @Override public boolean canHarmPlayer(Player other){return true;}
  @Override public void playSound(net.minecraft.sounds.SoundEvent sound,float volume,float pitch){sounds++;}
 }
 public static void run(ServerPlayer attacker) throws Exception {
  var b=CoreRuntime.balance;var p=CoreRuntime.data(attacker);p.max(b);p.track(CoreSkills.intelligence).level=7;
  var victim=new SoundPlayer(attacker.serverLevel());var grace=ServerPlayer.class.getDeclaredField("spawnInvulnerableTime");grace.setAccessible(true);grace.setInt(victim,0);victim.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.SURVIVAL);victim.setHealth(20);
  attacker.experienceLevel=0;attacker.experienceProgress=0;attacker.totalExperience=0;victim.experienceLevel=0;victim.experienceProgress=0;victim.totalExperience=0;victim.giveExperiencePoints(1000);
  // The saved vanilla total is deliberately wrong: the bar is the source of truth.
  victim.totalExperience=99;victim.invulnerableTime=0;check(victim.hurt(attacker.damageSources().playerAttack(attacker),2),"melee hit lands");
  check(ExperienceMath.points(victim.experienceLevel,victim.experienceProgress)==930&&ExperienceMath.points(attacker.experienceLevel,attacker.experienceProgress)==70,"melee conserves experience and ignores stale total");
  int before=ExperienceMath.points(victim.experienceLevel,victim.experienceProgress);victim.hurt(attacker.damageSources().playerAttack(attacker),2);check(ExperienceMath.points(victim.experienceLevel,victim.experienceProgress)==before,"invulnerable hit does not steal");
  var arrow=new net.minecraft.world.entity.projectile.Arrow(attacker.serverLevel(),attacker,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.ARROW),null);victim.invulnerableTime=0;victim.hurt(attacker.damageSources().arrow(arrow,attacker),2);check(ExperienceMath.points(victim.experienceLevel,victim.experienceProgress)==before,"ranged hit does not steal");
  p.disabled.add("faustcore:experience_steal");victim.invulnerableTime=0;victim.hurt(attacker.damageSources().playerAttack(attacker),2);check(ExperienceMath.points(victim.experienceLevel,victim.experienceProgress)==before,"disabled theft does not steal");p.disabled.remove("faustcore:experience_steal");
  var walker=new SoundPlayer(attacker.serverLevel());walker.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.SURVIVAL);walker.setPos(attacker.getX()+8,attacker.getY()+5,attacker.getZ());walker.setOnGround(true);var progress=CoreRuntime.data(walker);progress.max(b);grace.setInt(walker,0);
  var pos=walker.blockPosition().below();var level=walker.serverLevel();var saved=level.getBlockState(pos);GameEventListener listener=null;
  try{
   level.setBlockAndUpdate(pos,Blocks.SOUL_SAND.defaultBlockState());var speed=Entity.class.getDeclaredMethod("getBlockSpeedFactor");speed.setAccessible(true);check(Math.abs((float)speed.invoke(walker)-1)<0.001,"soft foot removes soul sand slowdown");progress.disabled.add("faustcore:soft_foot");check((float)speed.invoke(walker)<1,"disabled soft foot keeps soul sand slowdown");progress.disabled.remove("faustcore:soft_foot");
   walker.setHealth(20);walker.invulnerableTime=0;Blocks.MAGMA_BLOCK.stepOn(level,pos,Blocks.MAGMA_BLOCK.defaultBlockState(),walker);check(walker.getHealth()==20,"soft foot prevents magma damage");progress.disabled.add("faustcore:soft_foot");Blocks.MAGMA_BLOCK.stepOn(level,pos,Blocks.MAGMA_BLOCK.defaultBlockState(),walker);check(walker.getHealth()<20,"disabled soft foot allows magma damage");
   level.setBlockAndUpdate(pos,Blocks.STONE.defaultBlockState());final int[] steps={0};listener=new GameEventListener(){public PositionSource getListenerSource(){return new BlockPositionSource(pos);}public int getListenerRadius(){return 16;}public boolean handleGameEvent(ServerLevel world,Holder<GameEvent> event,GameEvent.Context context,Vec3 origin){if(event.is(GameEvent.STEP))steps[0]++;return true;}};
   level.getChunkAt(pos).getListenerRegistry(SectionPos.blockToSectionCoord(pos.getY())).register(listener);
   var step=Entity.class.getDeclaredMethod("vibrationAndSoundEffectsFromBlock",BlockPos.class,net.minecraft.world.level.block.state.BlockState.class,boolean.class,boolean.class,Vec3.class);step.setAccessible(true);
   progress.disabled.add("faustcore:silent_step");walker.sounds=0;step.invoke(walker,pos,Blocks.STONE.defaultBlockState(),true,true,Vec3.ZERO);check(walker.sounds>0&&steps[0]>0,"ordinary footsteps emit sound and vibration");int sounds=walker.sounds,vibrations=steps[0];progress.disabled.remove("faustcore:silent_step");step.invoke(walker,pos,Blocks.STONE.defaultBlockState(),true,true,Vec3.ZERO);check(walker.sounds==sounds&&steps[0]==vibrations,"silent footsteps emit neither sound nor vibration");
  }finally{if(listener!=null)level.getChunkAt(pos).getListenerRegistry(SectionPos.blockToSectionCoord(pos.getY())).unregister(listener);level.setBlockAndUpdate(pos,saved);}
  var intelligence=b.trees.stream().filter(t->t.skill()==CoreSkills.intelligence).findFirst().orElseThrow().nodes();var enchant=intelligence.stream().filter(n->n.ability().equals("faustcore:enchanting_power")).findFirst().orElseThrow();var theft=intelligence.stream().filter(n->n.ability().equals("faustcore:experience_steal")).findFirst().orElseThrow();var brew=intelligence.stream().filter(n->n.ability().equals("faustcore:potion_duration")).findFirst().orElseThrow();var cat=intelligence.stream().filter(n->n.ability().equals("faustcore:cat_scent")).findFirst().orElseThrow();check(theft.x()==enchant.x()&&theft.y()==enchant.y()+4&&cat.x()==brew.x()&&cat.y()==brew.y()+4,"integrated tree preserves requested positions");
  p.unlocked.remove("faustcore:silent_step");p.unlocked.add("faustcore:soft_foot");p.setCounter("breeze_kills",0,b);
  for(int i=0;i<5;i++){var breeze=EntityType.BREEZE.create(level);breeze.setPos(attacker.position());breeze.die(attacker.damageSources().playerAttack(attacker));breeze.discard();}check(p.counters.get("breeze_kills")==5,"five real breeze death events counted");
  var arena=attacker.blockPosition().above(30);var oldBlocks=new java.util.HashMap<BlockPos,net.minecraft.world.level.block.state.BlockState>();var playerMapField=net.minecraft.server.players.PlayerList.class.getDeclaredField("playersByUUID");playerMapField.setAccessible(true);@SuppressWarnings("unchecked") var playerMap=(java.util.Map<java.util.UUID,ServerPlayer>)playerMapField.get(level.getServer().getPlayerList());var originalPlayer=playerMap.get(attacker.getUUID());
  try{
   for(int x=-6;x<=6;x++)for(int z=-6;z<=6;z++)for(int y=0;y<=4;y++){var at=arena.offset(x,y,z);oldBlocks.put(at,level.getBlockState(at));level.setBlockAndUpdate(at,y==0?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState());}
   var shriekPos=arena.above();level.setBlockAndUpdate(shriekPos,Blocks.SCULK_SHRIEKER.defaultBlockState().setValue(SculkShriekerBlock.CAN_SUMMON,true));var shrieker=(net.minecraft.world.level.block.entity.SculkShriekerBlockEntity)level.getBlockEntity(shriekPos);var type=net.minecraft.world.level.block.entity.SculkShriekerBlockEntity.class;
   var noise=type.getDeclaredMethod("shriek",ServerLevel.class,Entity.class);noise.setAccessible(true);noise.invoke(shrieker,level,attacker);var warning=type.getDeclaredField("warningLevel");warning.setAccessible(true);var summon=type.getDeclaredMethod("trySummonWarden",ServerLevel.class);summon.setAccessible(true);
   playerMap.put(attacker.getUUID(),attacker);p.setCounter("warden_noise",0,b);warning.setInt(shrieker,3);check(!(boolean)summon.invoke(shrieker,level)&&p.counters.get("warden_noise")==0,"warning without spawn does not count");warning.setInt(shrieker,4);check((boolean)summon.invoke(shrieker,level)&&p.counters.get("warden_noise")==1,"actual warden summon credited to noise source");
  }finally{if(originalPlayer==null)playerMap.remove(attacker.getUUID());else playerMap.put(attacker.getUUID(),originalPlayer);for(var w:level.getEntitiesOfClass(net.minecraft.world.entity.monster.warden.Warden.class,new net.minecraft.world.phys.AABB(arena).inflate(12)))w.discard();for(var e:oldBlocks.entrySet())level.setBlockAndUpdate(e.getKey(),e.getValue());}
  System.out.println("EXPERIENCE AND MOVEMENT GAMEPLAY CHECKS PASSED");
 }
}
