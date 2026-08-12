package com.majorbonghits.moderncompanions.entity.job;

import com.majorbonghits.moderncompanions.core.TagsInit;
import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.SwordItem;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.Tags;

/** One source of truth for the item a job may visibly use in its main hand. */
public final class JobToolPolicy {
    private JobToolPolicy() {}

    public static boolean isRequired(CompanionJob job) {
        return switch (job) {
            case LUMBERJACK, HUNTER, MINER, FISHER, FARMER -> true;
            case NONE, CHEF -> false;
        };
    }

    public static boolean matches(CompanionJob job, ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        return switch (job) {
            case LUMBERJACK -> stack.getItem() instanceof AxeItem || stack.is(ItemTags.AXES)
                    || stack.is(TagsInit.Items.AXES);
            case MINER -> stack.getItem() instanceof PickaxeItem || stack.is(ItemTags.PICKAXES);
            case FISHER -> stack.getItem() instanceof FishingRodItem
                    || stack.canPerformAction(ItemAbilities.FISHING_ROD_CAST)
                    || stack.is(Tags.Items.TOOLS_FISHING_ROD);
            case FARMER -> stack.getItem() instanceof HoeItem || stack.is(ItemTags.HOES);
            case HUNTER -> stack.getItem() instanceof SwordItem || stack.is(ItemTags.SWORDS)
                    || stack.is(TagsInit.Items.SWORDS) || stack.getItem() instanceof AxeItem
                    || stack.is(ItemTags.AXES) || stack.is(TagsInit.Items.AXES)
                    || stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem;
            case NONE, CHEF -> false;
        };
    }

    public static boolean has(AbstractHumanCompanionEntity companion, CompanionJob job) {
        if (!isRequired(job)) return true;
        if (matches(job, companion.getMainHandItem())) return true;
        for (int slot = 0; slot < companion.getInventory().getContainerSize(); slot++) {
            if (matches(job, companion.getInventory().getItem(slot))) return true;
        }
        return false;
    }

}
