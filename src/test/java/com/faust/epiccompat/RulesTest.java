package com.faust.epiccompat;
public final class RulesTest {
    private static int checks;
    private static void check(boolean result){checks++;if(!result)throw new AssertionError("Check "+checks);}
    public static void main(String[] args){
        check(Rules.branch("roll","DODGE").equals("dexterity"));
        check(Rules.branch("technician","PASSIVE").equals("dexterity"));
        check(Rules.branch("new_custom_skill","MOVER").equals("dexterity"));
        check(Rules.branch("impact_guard","GUARD").equals("defense"));
        check(Rules.branch("stamina_pillager","PASSIVE").equals("constitution"));
        check(Rules.branch("hyper_vitality","PASSIVE").equals("constitution"));
        check(Rules.branch("forbidden_strength","PASSIVE").equals("strength"));
        check(Rules.canSpend(3,3,false,false));
        check(!Rules.canSpend(2,3,false,false));
        check(!Rules.canSpend(10,1,true,false));
        check(Rules.canSpend(0,10,true,true));
        check(!Rules.canSpend(20,Float.NaN,false,false));
        check(!Rules.canSpend(20,Float.POSITIVE_INFINITY,false,true));
        check(!Rules.canSpend(20,-1,false,true));
        check(Rules.canSpend(0,0,false,false));
        for(boolean twoHanded:new boolean[]{false,true})for(boolean spear:new boolean[]{false,true})for(boolean mastery:new boolean[]{false,true}){
            double cost=Rules.strikeCost(1.5,twoHanded,spear,mastery);
            check(cost==(twoHanded&&!spear&&!mastery?3:1.5));
            check(Rules.strikeCost(1.5,twoHanded,spear,mastery,true,true)==0);
            check(Rules.strikeCost(1.5,twoHanded,spear,mastery,false,true)==cost);
            check(Rules.strikeCost(1.5,twoHanded,spear,mastery,true,false)==cost);
        }
        check(Rules.strikeCost(0,true,false,false)==0);
        check(!Rules.canSpend(1,(float)Rules.strikeCost(1,true,false,false),false,false));
        check(Rules.canSpend(1,(float)Rules.strikeCost(1,true,false,true),false,false));
        check(!Rules.canSpend(0,0,true,false));
        for(int i=1;i<=16;i++)check(Rules.passiveSlot("PASSIVE"+i)==(i<=3));
        check(!Rules.passiveSlot("PASSIVE0"));check(!Rules.passiveSlot("CUSTOM_PASSIVE"));
        for(String id:java.util.List.of(Combat.MASTERY,Combat.FIST_MASTERY)){
            var node=Compat.mastery(id);check(node.skill()==com.faust.core.progression.Skill.strength);check(node.level()==5);check(node.amount()==0);
        }
        var balance=new com.faust.core.progression.Balance();
        for(int i=0;i<20;i++)balance.abilities.add(new com.faust.core.progression.Balance.Ability("faust_epic_compat:test/skill_"+i,com.faust.core.progression.Skill.strength,5,"hits",0,"","faust_epic_compat:skill",0));
        var original=java.util.List.copyOf(balance.trees);
        Compat.extend(balance);
        var layout=balance.trees.stream().filter(t->t.skill()==com.faust.core.progression.Skill.strength).findFirst().orElseThrow();
        var added=layout.nodes().stream().filter(n->n.ability().startsWith("faust_epic_compat:")).toList();
        check(added.size()==20);
        check(added.stream().map(com.faust.core.progression.SkillTrees.Node::x).distinct().count()==4);
        check(added.stream().map(com.faust.core.progression.SkillTrees.Node::y).distinct().count()==5);
        check(added.stream().allMatch(n->n.x()>=0&&n.x()+22<=160));
        for(var tree:original)check(balance.trees.stream().filter(t->t.skill()==tree.skill()).findFirst().orElseThrow().nodes().containsAll(tree.nodes()));
        var before=com.faust.core.progression.Balance.JSON.toJson(balance.trees);Compat.extend(balance);
        check(before.equals(com.faust.core.progression.Balance.JSON.toJson(balance.trees)));
        System.out.println("Passed "+checks+" integration policy checks");
    }
}
