package com.majorbonghits.moderncompanions.item;

import com.majorbonghits.moderncompanions.core.ModDataComponents;
import com.majorbonghits.moderncompanions.core.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Blank paper-derived item that becomes a configured Alchemist recipe beside a potion. */
public final class AlchemistRecipeItem extends Item {
    public AlchemistRecipeItem(Properties properties) {
        super(properties);
    }

    public static boolean isBlank(ItemStack stack) {
        return stack.is(ModItems.RECIPE.get()) && !stack.has(ModDataComponents.ALCHEMIST_RECIPE.get());
    }

    public static AlchemistRecipeData data(ItemStack stack) {
        return stack.get(ModDataComponents.ALCHEMIST_RECIPE.get());
    }

    @Override
    public Component getName(ItemStack stack) {
        AlchemistRecipeData data = data(stack);
        if (data == null) {
            return Component.translatable("item.modern_companions.recipe.blank").withStyle(ChatFormatting.WHITE);
        }
        return Component.translatable("item.modern_companions.recipe.prefix", potionName(data.potionId()))
                .withStyle(ChatFormatting.AQUA);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (data(stack) != null) {
            tooltip.add(Component.translatable("tooltip.modern_companions.recipe.active")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.modern_companions.recipe.ingredients")
                    .withStyle(ChatFormatting.GRAY));
            Map<ResourceLocation, Integer> ingredientCounts = new LinkedHashMap<>();
            for (ResourceLocation ingredient : data(stack).ingredients()) {
                ingredientCounts.merge(ingredient, 1, Integer::sum);
            }
            ingredientCounts.forEach((ingredientId, count) -> {
                Component ingredientName = BuiltInRegistries.ITEM.getOptional(ingredientId)
                        .map(item -> new ItemStack(item).getHoverName())
                        .orElse(Component.literal(ingredientId.toString()));
                MutableComponent line = Component.literal("  • ").withStyle(ChatFormatting.DARK_GRAY)
                        .append(ingredientName);
                if (count > 1) {
                    line.append(Component.literal(" x" + count).withStyle(ChatFormatting.GRAY));
                }
                tooltip.add(line);
            });
        } else {
            tooltip.add(Component.translatable("tooltip.modern_companions.recipe.blank")
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    private static MutableComponent potionName(ResourceLocation potionId) {
        String path = potionId.getPath();
        boolean strong = path.startsWith("strong_");
        boolean longDuration = path.startsWith("long_");
        String base = strong || longDuration ? path.substring(path.indexOf('_') + 1) : path;
        MutableComponent name = Component.translatable("item.modern_companions.recipe.effect." + base);
        if (strong) name.append(Component.literal(" II"));
        if (longDuration) name.append(Component.literal(" (Long)"));
        return name;
    }
}
