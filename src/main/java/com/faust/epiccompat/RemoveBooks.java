package com.faust.epiccompat;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.*;
import yesman.epicfight.world.item.SkillBookItem;
public final class RemoveBooks extends LootModifier {
    public static final MapCodec<RemoveBooks> CODEC=RecordCodecBuilder.mapCodec(instance->codecStart(instance).apply(instance,RemoveBooks::new));
    public RemoveBooks(LootItemCondition[] conditions){super(conditions);}
    @Override protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot,LootContext context){loot.removeIf(stack->stack.getItem() instanceof SkillBookItem);return loot;}
    @Override public MapCodec<? extends IGlobalLootModifier> codec(){return CODEC;}
}
