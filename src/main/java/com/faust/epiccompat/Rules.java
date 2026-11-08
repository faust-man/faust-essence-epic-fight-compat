package com.faust.epiccompat;
import java.util.Locale;
/** Pure policy shared by integration and automatic checks. */
public final class Rules {
    private Rules(){}
    public static double strikeCost(double base,boolean twoHanded,boolean spear,boolean mastery){
        if(!Double.isFinite(base)||base<0)throw new IllegalArgumentException("Invalid attack cost");
        return base*(twoHanded&&!spear&&!mastery?2:1);
    }
    public static double strikeCost(double base,boolean twoHanded,boolean spear,boolean mastery,boolean fist,boolean fistMastery){return fist&&fistMastery?0:strikeCost(base,twoHanded,spear,mastery);}
    public static double strikeCost(double base,boolean twoHanded,boolean spear,boolean mastery,boolean fist,boolean fistMastery,boolean shield,boolean shieldMastery){
        double cost=strikeCost(base,twoHanded,spear,mastery,fist,fistMastery);
        return shield&&!twoHanded&&!fist&&!shieldMastery?cost*2:cost;
    }
    public static boolean passiveSlot(String slot){return java.util.Set.of("PASSIVE1","PASSIVE2","PASSIVE3").contains(slot);}
    public static String branch(String name,String category){
        String key=(name+" "+category).toLowerCase(Locale.ROOT);
        if(key.matches(".*(guard|parry|block|defen).*"))return "defense";
        if(key.matches(".*(roll|step|dodge|mover|jump|sprint|swim|technician).*"))return "dexterity";
        if(key.matches(".*(stamina|recover|endurance|breath|energy|health|regener|vital|harvest|catharsis).*"))return "constitution";
        return "strength";
    }
    public static boolean canSpend(double available,float amount,boolean blocked,boolean exempt){
        return Float.isFinite(amount)&&amount>=0&&(exempt||(!blocked&&available>=amount));
    }
}
