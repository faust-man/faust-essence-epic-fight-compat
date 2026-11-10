package com.faust.epiccompat;
import com.faust.core.progression.*;
public final class WithoutStaminaTest {
    private static void check(boolean v,String message){if(!v)throw new AssertionError(message);}
    public static void main(String[] args){
        check(!StaminaServices.present(),"provider genuinely absent from classpath");
        Balance b=new Balance();
        b.abilities.add(Compat.mastery(Combat.MASTERY));
        b.abilities.add(Compat.mastery(Combat.FIST_POWER));
        for(String name:java.util.List.of("roll","technician","emergency_escape","adrenaline_fiend"))b.abilities.add(new Balance.Ability("faust_epic_compat:epicfight/"+name,Skill.dexterity,5,"hits",0,"","faust_epic_compat:skill",0));
        Compat.extend(b);b.validate();
        check(b.abilities.stream().noneMatch(a->Compat.isMastery(a.id())),"custom nodes absent from model");
        check(b.trees.stream().flatMap(t->t.nodes().stream()).noneMatch(n->Compat.isMastery(n.ability())),"custom nodes absent from tree");
        check(b.abilities.stream().filter(a->a.id().startsWith("faust_epic_compat:")).count()==4,"only registry skill nodes counted");
        check(b.abilities.stream().anyMatch(a->a.id().equals("faustcore:training_constitution_health")),"ordinary core abilities retained");
        check(b.abilities.stream().filter(a->a.id().endsWith("/emergency_escape")||a.id().endsWith("/adrenaline_fiend")).allMatch(a->a.actionConditions().stream().allMatch(c->c.amount()==0)),"Faust-only practice cannot block native skills");
        var endurance=Compat.relocate(new Balance.Ability("faust_epic_compat:epicfight/endurance",Skill.constitution,5,"hits",0,"","faust_epic_compat:skill",0));
        check(endurance.level()==10&&endurance.actionConditions().stream().allMatch(c->c.amount()==0),"endurance only requires constitution 10");
        var parent="faust_epic_compat:epicfight/endurance";
        b.abilities.add(endurance);
        b.abilities.add(new Balance.Ability("faust_epic_compat:epicfight/test_child",Skill.strength,5,"hits",10,parent,"faust_epic_compat:skill",0));
        b.abilities.add(new Balance.Ability("faust_epic_compat:epicfight/test_grandchild",Skill.strength,5,"hits",10,"faust_epic_compat:epicfight/test_child","faust_epic_compat:skill",0));
        Compat.extend(b);b.validate();
        for(var a:b.abilities)if(a.id().contains("/test_"))check(a.skill()==Skill.constitution&&a.level()==10&&a.dependencies().isEmpty()&&a.amount()==0,"transitive fallback only constitution10");
        System.out.println("Compat without stamina checks passed");
    }
}
