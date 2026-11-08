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
        check(Rules.canSpend(2,3,false,false));
        check(!Rules.canSpend(10,1,true,false));
        check(Rules.canSpend(0,10,true,true));
        check(!Rules.canSpend(20,Float.NaN,false,false));
        check(!Rules.canSpend(20,Float.POSITIVE_INFINITY,false,true));
        check(!Rules.canSpend(20,-1,false,true));
        check(Rules.canSpend(0,0,false,false));
        check(Rules.dualCost(1,true,false)==1);check(Rules.dualCost(1,true,true)==0.5);check(Rules.dualCost(1,false,false)==1);
        check(Rules.branch("adaptive_skin","PASSIVE").equals("constitution"));check(Rules.branch("revelation","PASSIVE").equals("intelligence"));
        check(Compat.mastery(Combat.DAGGER_MASTERY).skill()==com.faust.core.progression.Skill.dexterity);
        check(Compat.mastery(Combat.SWORD_MASTERY).skill()==com.faust.core.progression.Skill.strength);
        check(Compat.icon(Combat.SWORD_MASTERY).startsWith("minecraft:"));check(Compat.icon("faust_epic_compat:epicfight/roll").equals("faust_epic_compat:epicfight/roll"));
        check(Rules.strikeCost(1,false,false,false,false,false,true,false)==2);
        check(Rules.strikeCost(1,false,false,false,false,false,true,true)==1);
        check(Rules.strikeCost(1,false,false,true,false,false,true,false)==2);
        check(Rules.strikeCost(1,false,false,false,false,false,false,false)==1);
        check(Rules.strikeCost(1,true,false,false,false,false,true,false)==2);
        check(Rules.strikeCost(1,false,false,false,true,true,true,false)==0);
        check(Rules.strikeCost(1,false,false,false,true,false,true,false)==1);
        check(Rules.strikeCost(1.5,false,false,false,false,false,true,false)==3);
        check(Compat.mastery(Combat.SHIELD_MASTERY).skill()==com.faust.core.progression.Skill.defense);
        check(Compat.mastery(Combat.SHIELD_MASTERY).level()==5);
        for(boolean twoHanded:new boolean[]{false,true})for(boolean spear:new boolean[]{false,true})for(boolean mastery:new boolean[]{false,true}){
            double cost=Rules.strikeCost(1.5,twoHanded,spear,mastery);
            check(cost==(twoHanded&&!spear&&!mastery?3:1.5));
            check(Rules.strikeCost(1.5,twoHanded,spear,mastery,true,true)==0);
            check(Rules.strikeCost(1.5,twoHanded,spear,mastery,false,true)==cost);
            check(Rules.strikeCost(1.5,twoHanded,spear,mastery,true,false)==cost);
        }
        check(Rules.strikeCost(0,true,false,false)==0);
        check(Rules.canSpend(1,(float)Rules.strikeCost(1,true,false,false),false,false));
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
        var gates=new com.faust.core.progression.Balance();
        String tech="faust_epic_compat:epicfight/technician", master="faust_epic_compat:epicfight/swordmaster";
        gates.abilities.add(new com.faust.core.progression.Balance.Ability(tech,com.faust.core.progression.Skill.dexterity,5,"hits",0,"","faust_epic_compat:skill",0));
        gates.abilities.add(new com.faust.core.progression.Balance.Ability(master,com.faust.core.progression.Skill.strength,5,"hits",0,"","faust_epic_compat:skill",0));
        gates.abilities.add(Compat.mastery(Combat.SWORD_MASTERY));
        for(String name:java.util.List.of("berserker","vengeance","forbidden_strength"))gates.abilities.add(new com.faust.core.progression.Balance.Ability("faust_epic_compat:epicfight/"+name,com.faust.core.progression.Skill.strength,5,"hits",0,"","faust_epic_compat:skill",0));
        Compat.extend(gates);
        var technician=gates.abilities.stream().filter(a->a.id().equals(tech)).findFirst().orElseThrow();
        var pair=gates.abilities.stream().filter(a->a.id().equals(Combat.SWORD_MASTERY)).findFirst().orElseThrow();
        var progress=new com.faust.core.progression.Progress();progress.track(com.faust.core.progression.Skill.dexterity).level=5;
        progress.track(com.faust.core.progression.Skill.intelligence).level=4;check(!progress.canUnlock(technician,gates));
        progress.track(com.faust.core.progression.Skill.intelligence).level=5;check(progress.canUnlock(technician,gates));
        check(technician.skill()==com.faust.core.progression.Skill.dexterity);
        progress.track(com.faust.core.progression.Skill.strength).level=5;progress.counters.put(CombatPractice.DUAL,10.0);check(!progress.canUnlock(pair,gates));
        progress.unlocked.add(master);check(progress.canUnlock(pair,gates));
        var tree=gates.trees.stream().filter(t->t.skill()==com.faust.core.progression.Skill.strength).findFirst().orElseThrow();
        var first=tree.nodes().stream().filter(n->n.ability().equals(master)).findFirst().orElseThrow();
        var second=tree.nodes().stream().filter(n->n.ability().equals(pair.id())).findFirst().orElseThrow();
        check(first.x()==second.x()&&second.y()-first.y()==40);
        check(first.x()<original.stream().filter(t->t.skill()==com.faust.core.progression.Skill.strength).findFirst().orElseThrow().nodes().stream().mapToDouble(com.faust.core.progression.SkillTrees.Node::x).min().orElseThrow());
        for(var left:tree.nodes())for(var right:tree.nodes())if(!left.ability().equals(right.ability()))check(Math.abs(left.x()-right.x())>=22||Math.abs(left.y()-right.y())>=22);
        var chain=gates.abilities.stream().filter(a->a.counter().equals(CombatPractice.DAMAGE)).sorted(java.util.Comparator.comparingDouble(com.faust.core.progression.Balance.Ability::amount)).toList();
        check(chain.size()==3);check(chain.get(0).amount()==40&&chain.get(1).amount()==80&&chain.get(2).amount()==120);
        progress.counters.put(CombatPractice.DAMAGE,39.0);check(!progress.canUnlock(chain.get(0),gates));progress.counters.put(CombatPractice.DAMAGE,40.0);check(progress.canUnlock(chain.get(0),gates));
        progress.counters.put(CombatPractice.DAMAGE,120.0);check(!progress.canUnlock(chain.get(2),gates));
        progress.unlocked.add(chain.get(0).id());check(progress.canUnlock(chain.get(1),gates));progress.unlocked.add(chain.get(1).id());check(progress.canUnlock(chain.get(2),gates));
        progress.counters.put(CombatPractice.DUAL,9.0);check(!progress.canUnlock(pair,gates));progress.counters.put(CombatPractice.DUAL,10.0);check(progress.canUnlock(pair,gates));
        for(String id:java.util.List.of(Combat.FIST_MASTERY,Combat.MASTERY,master)){
            var practice=id.equals(master)?gates.abilities.stream().filter(a->a.id().equals(id)).findFirst().orElseThrow():Compat.relocate(Compat.mastery(id));
            check(practice.amount()==10);progress.unlocked.remove(id);progress.counters.put(practice.counter(),9.0);check(!progress.canUnlock(practice,gates));progress.counters.put(practice.counter(),10.0);check(progress.canUnlock(practice,gates));
        }
        String gateLayout=com.faust.core.progression.Balance.JSON.toJson(gates.trees);Compat.extend(gates);check(gateLayout.equals(com.faust.core.progression.Balance.JSON.toJson(gates.trees)));
        for(int i=0;i<4;i++)gates.abilities.add(new com.faust.core.progression.Balance.Ability("faust_epic_compat:test/remaining_"+i,com.faust.core.progression.Skill.strength,5,"hits",0,"","faust_epic_compat:skill",0));
        Compat.extend(gates);var packed=gates.trees.stream().filter(t->t.skill()==com.faust.core.progression.Skill.strength).findFirst().orElseThrow();
        var bottom=packed.nodes().stream().filter(n->n.ability().startsWith("faust_epic_compat:test/remaining_")).toList();
        check(bottom.size()==4&&bottom.stream().map(com.faust.core.progression.SkillTrees.Node::y).distinct().count()==1);
        check(bottom.stream().mapToDouble(com.faust.core.progression.SkillTrees.Node::x).min().orElseThrow()==packed.nodes().stream().mapToDouble(com.faust.core.progression.SkillTrees.Node::x).min().orElseThrow());
        for(var left:packed.nodes())for(var right:packed.nodes())if(!left.ability().equals(right.ability()))check(Math.abs(left.x()-right.x())>=22||Math.abs(left.y()-right.y())>=22);
        check(Compat.relocate(Compat.mastery(Combat.DAGGER_MASTERY)).counter().equals(CombatPractice.DAGGER_HITS));
        check(Compat.relocate(Compat.mastery(Combat.DAGGER_MASTERY)).amount()==10);
        check(Compat.relocate(Compat.mastery(Combat.SHIELD_MASTERY)).counter().equals(CombatPractice.SHIELD_HITS));
        check(Compat.relocate(Compat.mastery(Combat.SHIELD_MASTERY)).amount()==100);
        check(Compat.relocate(Compat.mastery(Combat.FIST_POWER)).parent().equals(Combat.FIST_MASTERY));
        check(Compat.relocate(Compat.mastery(Combat.SHIELD_EFFICIENCY)).parent().equals("faust_epic_compat:epicfight/guard"));
        check(Compat.relocate(Compat.mastery(Combat.SHIELD_EFFICIENCY)).value()==0.5);
        for(String id:java.util.List.of(Combat.FIST_POWER,Combat.FIST_MASTERY,Combat.SHIELD_EFFICIENCY,Combat.SWORD_MASTERY))check(Compat.relocate(Compat.mastery(id)).toggleable());
        check(Rules.canSpend(0.5,1,false,false));check(!Rules.canSpend(0,1,true,false));
        System.out.println("Passed "+checks+" integration policy checks");
    }
}
