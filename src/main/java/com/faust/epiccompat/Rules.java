package com.faust.epiccompat;
import java.util.Locale;
/** Pure policy shared by integration and automatic checks. */
public final class Rules {
    private Rules(){}
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
