package com.faust.epiccompat;
import com.faust.core.runtime.CoreRuntime;
import com.mojang.authlib.GameProfile;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.skill.*;
import java.util.UUID;

/** Optional in-game checks; excluded from the release JAR. */
@EventBusSubscriber(modid="faust_epic_compat")
public final class RuntimeChecks {
    private static int checks;
    private static void check(boolean value,String label){checks++;if(!value)throw new AssertionError(label);}
    @SubscribeEvent public static void started(ServerStartedEvent event){
        var player=FakePlayerFactory.get(event.getServer().overworld(),new GameProfile(UUID.fromString("9625401d-3174-4f98-bb52-e8002881a51b"),"CompatChecks"));
        var patch=new ServerPlayerPatch(player);
        var data=CoreRuntime.data(player);var balance=CoreRuntime.balance;
        double oldAttack=balance.stamina.get("attackCost");
        try{
            balance.stamina.put("attackCost",1.0);data.stamina=20;data.fatigue=0;data.exhausted=false;
            player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
            Combat.strike(patch,InteractionHand.MAIN_HAND);
            check(Combat.paid(patch)&&Math.abs(data.stamina-19)<0.0001,"bare-hand payment");
            data.unlocked.add(Combat.FIST_MASTERY);
            Combat.strike(patch,InteractionHand.MAIN_HAND);
            check(Combat.paid(patch)&&Math.abs(data.stamina-19)<0.0001,"fist mastery");
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(yesman.epicfight.registry.entries.EpicFightItems.GLOVE.get()));
            Combat.strike(patch,InteractionHand.MAIN_HAND);
            check(Combat.paid(patch)&&Math.abs(data.stamina-19)<0.0001,"glove mastery");
            data.unlocked.remove(Combat.FIST_MASTERY);Combat.strike(patch,InteractionHand.MAIN_HAND);
            check(Combat.paid(patch)&&Math.abs(data.stamina-18)<0.0001,"gloves without mastery cost stamina");
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(yesman.epicfight.registry.entries.EpicFightItems.IRON_GREATSWORD.get()));
            data.stamina=20;Combat.strike(patch,InteractionHand.MAIN_HAND);
            check(Combat.paid(patch)&&Math.abs(data.stamina-18)<0.0001,"greatsword costs double");
            data.unlocked.add(Combat.MASTERY);Combat.strike(patch,InteractionHand.MAIN_HAND);
            check(Combat.paid(patch)&&Math.abs(data.stamina-17)<0.0001,"greatsword mastery normalizes cost");
            data.unlocked.remove(Combat.MASTERY);data.stamina=1;Combat.strike(patch,InteractionHand.MAIN_HAND);
            check(!Combat.paid(patch)&&data.stamina==1,"insufficient double cost is rejected atomically");
            data.stamina=20;data.exhausted=false;
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(yesman.epicfight.registry.entries.EpicFightItems.IRON_SPEAR.get()));
            Combat.strike(patch,InteractionHand.MAIN_HAND);
            check(Combat.paid(patch)&&Math.abs(data.stamina-19)<0.0001,"spear exemption");
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.IRON_SWORD));
            Combat.strike(patch,InteractionHand.MAIN_HAND);
            check(Combat.paid(patch)&&Math.abs(data.stamina-18)<0.0001,"one-handed weapon base cost");
            data.exhausted=true;Combat.strike(patch,InteractionHand.MAIN_HAND);
            check(!Combat.paid(patch),"exhaustion still blocks free attacks");data.exhausted=false;
            for(var slot:SkillSlots.values())if(slot.category()==SkillCategories.PASSIVE)check(Combat.allowedSlot(slot)==(slot==SkillSlots.PASSIVE1||slot==SkillSlots.PASSIVE2||slot==SkillSlots.PASSIVE3),"passive slot gate");
            check(patch.getPlayerSkills().getSkillContainersFor(SkillCategories.PASSIVE).size()==3,"slot selection has three entries");
            double before=data.stamina;
            Combat.collide(player,()->check(CoreRuntime.attack(player,false),"prepaid hit allowed"));
            Combat.collide(player,()->{check(CoreRuntime.attack(player,false),"first area target");check(CoreRuntime.attack(player,false),"second area target");});
            check(data.stamina==before,"prepaid hit not charged twice");check(!Combat.prepaid(player),"payment context cleared");
            try{Combat.collide(player,()->{throw new IllegalStateException("test");});}catch(IllegalStateException expected){}
            check(!Combat.prepaid(player),"context cleared after exception");
            Combat.begin(patch);check(!Combat.paid(patch),"new animation invalidates old payment");
            check(balance.abilities.stream().anyMatch(a->a.id().equals(Combat.MASTERY)),"two-handed node migrated");
            check(balance.abilities.stream().anyMatch(a->a.id().equals(Combat.FIST_MASTERY)),"fist node migrated");
            System.out.println("FAUST COMPAT RUNTIME CHECKS PASSED: "+checks);
        }finally{balance.stamina.put("attackCost",oldAttack);event.getServer().halt(false);}
    }
}
