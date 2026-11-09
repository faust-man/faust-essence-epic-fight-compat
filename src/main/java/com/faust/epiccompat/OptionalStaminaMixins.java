package com.faust.epiccompat;

import java.util.*;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.*;

/** Only tree, slot and practice integration applies without Faust Stamina. */
public final class OptionalStaminaMixins implements IMixinConfigPlugin {
    private static final Set<String> RESOURCE=Set.of("StaminaMixin","StaminaPillagerMixin","AttackMixin","CoreAttackMixin","ComboCostMixin","SkillCostMixin","GrapplingCostMixin","ForbiddenMixin","HealthPaymentMixin","HealthStaminaTickMixin","GuardRecoveryMixin","FatigueMilestoneMixin","HudMixin","HealthClientMixin");
    public void onLoad(String pkg){}
    public String getRefMapperConfig(){return null;}
    public boolean shouldApplyMixin(String target,String mixin){return !RESOURCE.contains(mixin.substring(mixin.lastIndexOf('.')+1))||getClass().getClassLoader().getResource("com/faust/stamina/StaminaModel.class")!=null;}
    public void acceptTargets(Set<String> mine,Set<String> others){}
    public List<String> getMixins(){return null;}
    public void preApply(String name,ClassNode node,String mixin,IMixinInfo info){}
    public void postApply(String name,ClassNode node,String mixin,IMixinInfo info){}
}
