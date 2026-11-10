package com.faust.epiccompat;

import com.faust.core.progression.*;
import com.faust.core.runtime.CoreRuntime;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import com.mojang.authlib.GameProfile;
import java.util.*;

/** Actual server events and boundary checks for permanent milestones. */
public final class RequirementChecks {
    private static final class DamagePlayer extends net.neoforged.neoforge.common.util.FakePlayer {
        DamagePlayer(net.minecraft.server.level.ServerLevel level,GameProfile profile){super(level,profile);}
        @Override public boolean isInvulnerableTo(net.minecraft.world.damagesource.DamageSource source){return false;}
        @Override public boolean canHarmPlayer(net.minecraft.world.entity.player.Player target){return true;}
    }
    private static int checks;
    private static void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
    public static void run(Balance b,ServerPlayer owner){
        String prefix="faust_epic_compat:epicfight/";
        for(String name:List.of("meteor_slam","emergency_escape","bonebreaker","adrenaline_fiend","stamina_pillager","hypervitality","adaptive_skin","revelation")){
            var a=b.abilities.stream().filter(n->n.id().equals(prefix+name)).findFirst().orElseThrow();
            var p=new Progress();for(var s:com.faust.core.progression.Skill.values())p.track(s).level=20;
            p.unlocked.addAll(a.dependencies());
            check(!p.canUnlock(a,b),name+" locked without practice");
            for(var c:a.actionConditions())p.setCounter(c.counter(),c.amount(),b);
            check(p.canUnlock(a,b),name+" exact thresholds unlock");
            for(var c:a.actionConditions()){
                p.setCounter(c.counter(),c.amount()-0.01,b);check(!p.canUnlock(a,b),name+" rejects below each threshold");p.setCounter(c.counter(),c.amount(),b);
            }
            check(Compat.relocate(a).equals(a),name+" migration idempotent");
        }
        for(String id:List.of(Combat.SHIELD_EFFICIENCY,prefix+"parrying")){
            var a=b.abilities.stream().filter(n->n.id().equals(id)).findFirst().orElseThrow();var p=new Progress();for(var s:com.faust.core.progression.Skill.values())p.track(s).level=20;p.unlocked.addAll(a.dependencies());
            var c=a.actionConditions().getFirst();p.setCounter(c.counter(),c.amount()-1,b);check(!p.canUnlock(a,b),"block threshold rejects one short");p.setCounter(c.counter(),c.amount(),b);check(p.canUnlock(a,b),"block threshold accepts exact");
        }
        var harvest=b.abilities.stream().filter(a->a.id().equals(prefix+"death_harvest")).findFirst().orElseThrow();
        for(int bits=0;bits<8;bits++){
            var p=new Progress();for(var s:com.faust.core.progression.Skill.values())p.track(s).level=20;p.unlocked.addAll(harvest.dependencies());
            for(int i=0;i<3;i++)if((bits&(1<<i))!=0){var c=harvest.actionConditions().get(i);p.setCounter(c.counter(),c.amount(),b);}
            check(p.canUnlock(harvest,b)==(Integer.bitCount(bits)>=2),"harvest any two of three");
        }
        var endurance=b.abilities.stream().filter(a->a.id().equals(prefix+"endurance")).findFirst().orElseThrow();var p=new Progress();p.track(com.faust.core.progression.Skill.constitution).level=4;check(!p.canUnlock(endurance,b),"endurance requires constitution5");p.track(com.faust.core.progression.Skill.constitution).level=5;check(!p.canUnlock(endurance,b),"endurance requires maximum stamina30");p.setCounter("faust_epic_peak_stamina",29.99,b);check(!p.canUnlock(endurance,b),"below30 rejected");p.setCounter("faust_epic_peak_stamina",30,b);check(p.canUnlock(endurance,b),"maximum30 unlocks");
        var player=new DamagePlayer(owner.serverLevel(),new GameProfile(UUID.fromString("71685d44-faa1-4ee2-b111-88b8a8e3f6cc"),"MilestoneChecks"));var data=CoreRuntime.data(player);
        try{var grace=ServerPlayer.class.getDeclaredField("spawnInvulnerableTime");grace.setAccessible(true);grace.setInt(player,0);}catch(ReflectiveOperationException e){throw new AssertionError(e);}
        data.counters.clear();data.unlocked.clear();data.disabled.clear();data.stamina=20;data.exhausted=false;data.fatigue=15;data.updateExhaustion(b);data.fatigue=0;check(data.counters.get("faust_epic_peak_fatigue")==15,"fatigue peak persists after relief");
        data.unlocked.add("faust_epic_compat:epicfight/guard");data.unlocked.add("faust_epic_compat:epicfight/technician");
        Milestones.blocked(player,false);check(data.counters.getOrDefault("faust_epic_shield_blocks",0.0)==0,"weapon guard cannot count as shield");Milestones.blocked(player,true);check(data.counters.get("faust_epic_any_blocks")==2&&data.counters.get("faust_epic_shield_blocks")==1,"shield and any block are distinct");
        CoreRuntime.food(player,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.APPLE));check(data.counters.get("faust_epic_sweet_food")==1,"configured food counted");CoreRuntime.food(player,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BREAD));check(data.counters.get("faust_epic_sweet_food")==1,"unconfigured food excluded");
        for(int i=0;i<10;i++){player.invulnerableTime=0;player.setHealth(player.getMaxHealth());player.hurt(player.damageSources().fall(),10);}
        check(data.counters.get("faust_epic_fall_damage")==100,"actual lifetime fall damage");check(data.counters.get("faust_epic_peak_damage")==10,"actual single-hit peak");
        player.invulnerableTime=0;player.setHealth(player.getMaxHealth());player.hurt(player.damageSources().magic(),10);check(data.counters.get("faust_epic_magic_damage")==10,"actual magic damage category");
        player.invulnerableTime=0;player.setHealth(player.getMaxHealth());player.hurt(player.damageSources().inFire(),10);check(data.counters.get("faust_epic_fire_damage")==10,"actual fire damage category");
        var zombie=net.minecraft.world.entity.EntityType.ZOMBIE.create(player.serverLevel());zombie.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR).setBaseValue(0);
        player.invulnerableTime=0;player.setHealth(player.getMaxHealth());player.hurt(player.damageSources().mobAttack(zombie),10);check(data.counters.get("faust_epic_melee_damage")==Math.min(10,player.getMaxHealth()-player.getHealth()),"actual melee category");
        var arrow=new net.minecraft.world.entity.projectile.Arrow(net.minecraft.world.entity.EntityType.ARROW,player.serverLevel());player.invulnerableTime=0;player.setHealth(player.getMaxHealth());player.hurt(player.damageSources().arrow(arrow,zombie),10);check(data.counters.get("faust_epic_arrow_damage")==Math.min(10,player.getMaxHealth()-player.getHealth()),"actual arrow category");
        var pig=net.minecraft.world.entity.EntityType.PIG.create(player.serverLevel());pig.setHealth(10);data.exhausted=true;pig.hurt(player.damageSources().playerAttack(player),20);check(data.counters.get("faust_epic_peaceful_kills")==1&&data.counters.get("faust_epic_exhausted_kills")==1,"exhausted peaceful mob kill");
        data.exhausted=false;data.stamina=20;zombie.setHealth(20);zombie.hurt(player.damageSources().playerAttack(player),15);check(data.counters.getOrDefault("faust_epic_heavy_attack",0.0)==0,"exact15 is not greater than15");zombie.invulnerableTime=0;zombie.setHealth(20);zombie.hurt(player.damageSources().playerAttack(player),16);check(data.counters.get("faust_epic_heavy_attack")==1,"attack above15 recorded");
        var cath=b.abilities.stream().filter(a->a.id().equals(prefix+"catharsis")).findFirst().orElseThrow();for(var r:cath.skillRequirements())data.track(r.skill()).level=r.level();data.unlocked.addAll(cath.dependencies());
        player.setHealth(player.getMaxHealth());for(int roll=1;roll<100;roll++)Milestones.sleep(player,roll);check(!data.unlocked.contains(prefix+"catharsis"),"99 sleep outcomes do not grant");Milestones.sleep(player,0);check(data.unlocked.contains(prefix+"catharsis"),"one in100 sleep outcome grants catharsis");
        var endPlayer=FakePlayerFactory.get(owner.getServer().getLevel(net.minecraft.world.level.Level.END),new GameProfile(UUID.fromString("71685d44-faa1-4ee2-b111-88b8a8e3f6cd"),"EndVisitChecks"));Milestones.visitEnd(endPlayer);check(CoreRuntime.data(endPlayer).counters.get("faust_epic_visited_end")==1,"actual End visit recorded");
        long revision=data.revision;player.startSleeping(player.blockPosition());player.stopSleepInBed(true,true);player.stopSleepInBed(true,true);check(data.revision==revision,"completed Catharsis never rolls or mutates progress on future wakes");
        var defense=b.trees.stream().filter(t->t.skill()==com.faust.core.progression.Skill.defense).findFirst().orElseThrow();var economy=defense.nodes().stream().filter(n->n.ability().equals(Combat.SHIELD_EFFICIENCY)).findFirst().orElseThrow();var parry=defense.nodes().stream().filter(n->n.ability().equals(prefix+"parrying")).findFirst().orElseThrow();var impact=defense.nodes().stream().filter(n->n.ability().equals(prefix+"impact_guard")).findFirst().orElseThrow();var coordinated=defense.nodes().stream().filter(n->n.ability().equals(Combat.SHIELD_MASTERY)).findFirst().orElseThrow();check(economy.y()==parry.y()&&economy.y()==impact.y()&&coordinated.y()==economy.y()+40,"shield rows moved up40");check(economy.icon().equals("minecraft:iron_ingot"),"shield economy uses core icon");
        check(Progress.parse(data.json(),b).counters.equals(data.counters),"all counters persist through save/load");
        var dex=b.trees.stream().filter(t->t.skill()==com.faust.core.progression.Skill.dexterity).findFirst().orElseThrow();
        var roll=dex.nodes().stream().filter(n->n.ability().equals(prefix+"roll")).findFirst().orElseThrow();var tech=dex.nodes().stream().filter(n->n.ability().equals(prefix+"technician")).findFirst().orElseThrow();var daggers=dex.nodes().stream().filter(n->n.ability().equals(Combat.DAGGER_MASTERY)).findFirst().orElseThrow();check(roll.y()==tech.y()&&daggers.y()==roll.y()+40,"aligned dodge and technician rows");
        var complete=FakePlayerFactory.get(owner.serverLevel(),new GameProfile(UUID.fromString("71685d44-faa1-4ee2-b111-88b8a8e3f6ce"),"CompletePracticeChecks"));var completed=CoreRuntime.data(complete);completed.max(b);long beforeCompleted=completed.revision;
        check(!CombatPractice.weaponPracticeNeeded(complete),"completed skills skip weapon classification");CombatPractice.add(complete,CombatPractice.ROLLS,1);Milestones.add(completed,"fall_damage",10);check(completed.revision==beforeCompleted,"completed events do not dirty progress");
        var target=net.minecraft.world.entity.EntityType.PIG.create(owner.serverLevel());target.setHealth(10);target.hurt(complete.damageSources().playerAttack(complete),1);check(target.getHealth()==7,"fist power remains functional when all practice tracking is disabled");
        System.out.println("SKILL REQUIREMENT CHECKS PASSED: "+checks);
    }
}
