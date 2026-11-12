package com.faust.epiccompat;

import com.faust.core.progression.Balance;
import com.google.gson.JsonObject;
import java.util.List;

/** Requirements applied to both new and existing addon configuration. */
public final class SkillRequirements {
    private SkillRequirements(){}
    public static final String PREFIX="faust_epic_";
    public static Balance.Ability apply(Balance.Ability ability){
        JsonObject json=Balance.JSON.toJsonTree(ability).getAsJsonObject();
        String id=ability.id();
        String name=id.startsWith("faust_epic_compat:epicfight/")?id.substring("faust_epic_compat:epicfight/".length()):id;
        switch(name){
            case "faust_epic_compat:shield_efficiency" -> conditions(json,"all",0,c("shield_blocks",120));
            case "parrying" -> conditions(json,"all",0,c("any_blocks",50));
            case "meteor_slam" -> conditions(json,"all",0,c("fall_damage",100));
            case "emergency_escape" -> conditions(json,"all",0,c("peak_fatigue",15));
            case "bonebreaker" -> conditions(json,"all",0,c("heavy_attack",10));
            case "adrenaline_fiend" -> conditions(json,"all",0,c("exhausted_kills",1));
            case "death_harvest" -> conditions(json,"at_least",2,c("player_kills",1),c("peaceful_kills",10),c("hostile_kills",100));
            case "endurance" -> {
                json.addProperty("skill","constitution");json.addProperty("level",7);
                json.add("characteristics",Balance.JSON.toJsonTree(List.of()));
                for(String key:List.of("requiresAll","requiresAny","excludes"))json.add(key,Balance.JSON.toJsonTree(List.of()));
                json.addProperty("parent","");json.addProperty("requiresCount",0);conditions(json,"all",0,c("peak_stamina",30));
            }
            case "stamina_pillager" -> conditions(json,"all",0,c("sweet_food",64));
            case "hypervitality" -> conditions(json,"all",0,c("damage_taken",250),c("peak_damage",10));
            case "adaptive_skin" -> conditions(json,"all",0,c("melee_damage",10),c("arrow_damage",10),c("fire_damage",10),c("magic_damage",10));
            case "catharsis" -> conditions(json,"all",0,c("sleep_luck",1));
            case "revelation" -> conditions(json,"all",0,c("visited_end",1));
            default -> {}
        }
        return RevisionRequirements.apply(Balance.JSON.fromJson(json,Balance.Ability.class));
    }
    private static Balance.Condition c(String counter,double amount){return new Balance.Condition(PREFIX+counter,amount);}
    private static void conditions(JsonObject json,String mode,int count,Balance.Condition... values){
        json.addProperty("counter",values[0].counter());json.addProperty("amount",values[0].amount());
        json.add("conditions",Balance.JSON.toJsonTree(values));json.addProperty("conditionMode",mode);json.addProperty("conditionsRequired",count);
    }
}
