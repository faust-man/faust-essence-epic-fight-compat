package com.faust.epiccompat;
import com.faust.essence.api.Skill;

import com.faust.core.progression.*;
import java.util.*;
final class RevisionRequirements {
    static Balance.Ability apply(Balance.Ability a){
        var j=Balance.JSON.toJsonTree(a).getAsJsonObject();
        String n=a.id().replace("faust_epic_compat:epicfight/", "");
        int level=switch(n){case "roll","guard"->3;case "bonebreaker","death_harvest"->7;case "adaptive_skin"->12;case "catharsis"->1;case "impact_guard"->10;case "endurance","hypervitality","hyper_vitality"->7;case "berserker"->5;case "vengeance"->7;case "forbidden_strength"->10;default->a.level();};
        List<Balance.SkillRequirement> req=null;
        if(n.equals("swordmaster")||a.id().equals(Combat.SWORD_MASTERY))req=List.of(new Balance.SkillRequirement(Skill.valueOf("strength"),n.equals("swordmaster")?5:10),new Balance.SkillRequirement(Skill.valueOf("dexterity"),5));
        if(Set.of("berserker","vengeance","forbidden_strength").contains(n))req=List.of(new Balance.SkillRequirement(Skill.valueOf("strength"),level),new Balance.SkillRequirement(Skill.valueOf("constitution"),level));
        if(a.id().equals(Combat.FIST_POWER))level=10;
        if(a.id().equals(Combat.MASTERY))level=7;
        if(a.id().equals(Combat.SHIELD_EFFICIENCY))level=7;
        if(a.id().equals(Combat.SHIELD_MASTERY)){level=7;req=List.of(new Balance.SkillRequirement(Skill.valueOf("defense"),7),new Balance.SkillRequirement(Skill.valueOf("strength"),5));}
        if(a.id().equals(WeaponMastery.LONG))req=List.of(new Balance.SkillRequirement(Skill.valueOf("defense"),15),new Balance.SkillRequirement(Skill.valueOf("strength"),10));
        if(n.equals("catharsis")){req=List.of(new Balance.SkillRequirement(Skill.valueOf("constitution"),1),new Balance.SkillRequirement(Skill.valueOf("strength"),1),new Balance.SkillRequirement(Skill.valueOf("dexterity"),1),new Balance.SkillRequirement(Skill.valueOf("defense"),1),new Balance.SkillRequirement(Skill.valueOf("intelligence"),1),new Balance.SkillRequirement(Skill.valueOf("building"),1));j.addProperty("skill","constitution");}
        if(n.equals("death_harvest")){req=List.of(new Balance.SkillRequirement(Skill.valueOf("constitution"),level));j.addProperty("skill","constitution");}
        if(n.equals("adaptive_skin")){req=List.of(new Balance.SkillRequirement(Skill.valueOf("defense"),level));j.addProperty("skill","defense");}
        j.addProperty("level",level);
        if(req!=null)j.add("characteristics",Balance.JSON.toJsonTree(req));
        else if(level!=a.level())j.add("characteristics",Balance.JSON.toJsonTree(List.of(new Balance.SkillRequirement(a.skill(),level))));
        String parent=switch(n){case "swordmaster"->"faust_epic_compat:epicfight/technician";case "endurance","hypervitality","hyper_vitality"->"faustcore:training_constitution_stamina";case "adaptive_skin"->"faustcore:training_defense_armor";case "stamina_pillager"->"faustcore:second_wind";default->null;};
        if(parent!=null)j.addProperty("parent",parent);
        if(a.id().equals(Combat.SHIELD_EFFICIENCY))j.addProperty("value",1);
        return Balance.JSON.fromJson(j,Balance.Ability.class);
    }
}
