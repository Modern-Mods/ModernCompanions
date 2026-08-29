package com.majorbonghits.moderncompanions.entity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Source/resource regression check for companion combat, mana persistence, and firearm cadence. */
public final class CompanionCombatRegressionTest {
    private static final Path COMPANION = Path.of(
            "src/main/java/com/majorbonghits/moderncompanions/entity/AbstractHumanCompanionEntity.java");
    private static final Path BERSERKER = Path.of(
            "src/main/java/com/majorbonghits/moderncompanions/entity/Berserker.java");
    private static final Path VANGUARD = Path.of(
            "src/main/java/com/majorbonghits/moderncompanions/entity/Vanguard.java");
    private static final Path RENDERER = Path.of(
            "src/main/java/com/majorbonghits/moderncompanions/client/renderer/CompanionRenderer.java");
    private static final Path FIREARM_GOAL = Path.of(
            "src/main/java/com/majorbonghits/moderncompanions/entity/ai/FirearmAttackGoal.java");
    private static final Path MAGIC_COMPAT = Path.of(
            "src/main/java/com/majorbonghits/moderncompanions/compat/magic/MagicCastingCompat.java");
    private static final Path PLACEMENT_RECIPE = Path.of(
            "src/main/resources/data/modern_companions/recipe/placement_wand.json");
    private static final Path OLD_PLACEMENT_RECIPE = Path.of(
            "src/main/resources/data/modern_companions/recipes/placement_wand.json");

    private CompanionCombatRegressionTest() {}

    public static void main(String[] args) throws IOException {
        String companion = Files.readString(COMPANION);
        String berserker = Files.readString(BERSERKER);
        String vanguard = Files.readString(VANGUARD);
        String renderer = Files.readString(RENDERER);
        String firearmGoal = Files.readString(FIREARM_GOAL);
        String magicCompat = Files.readString(MAGIC_COMPAT);

        assert companion.contains("hurtCurrentlyUsedShield(float amount)")
                && companion.contains("this.useItem.hurtAndBreak")
                : "companions must damage and break shields when a hit is blocked";
        assert companion.contains("tickCompanionShieldUse();")
                && companion.contains("startUsingItem(InteractionHand.OFF_HAND)")
                : "companions need shared, server-authoritative shield use";
        assert renderer.contains("HumanoidModel.ArmPose.BLOCK")
                : "blocking companions must render the shield block pose";
        assert vanguard.contains("@Override\n    protected void tickCompanionShieldUse()")
                : "Vanguard must retain its custom shield decision logic";
        assert berserker.contains("EquipmentSlot.OFFHAND")
                && berserker.contains("0.8F")
                && berserker.contains("doHurtTargetWithEquipment")
                : "Berserker must make a reduced-damage offhand strike";
        assert !firearmGoal.contains("fireCooldown")
                && firearmGoal.contains("shootTacZ")
                : "TacZ cadence must remain under the native gun operator";

        int spellbookRestore = companion.indexOf("setSpellbookItem(spellbook.getItem(0));");
        int manaClamp = companion.indexOf("this.entityData.set(MANA, bounded(savedMana, getManaMax()));");
        assert spellbookRestore >= 0 && manaClamp >= 0 && spellbookRestore < manaClamp
                : "saved spellbooks must be restored before saved mana is clamped";
        assert magicCompat.contains("ItemAttributeModifiers")
                && magicCompat.contains("getSpellbookItem")
                : "Iron's Spellbooks mana modifiers must be read from the companion spellbook";
        assert magicCompat.contains("getMethod(\"getAttributeModifiers\", slotContextClass")
                && magicCompat.contains("ResourceLocation.class, ItemStack.class)")
                && magicCompat.contains("invoke(spellbook.getItem(), slotContext")
                : "Curios spellbook mana modifiers must use the native three-argument callback";

        assert Files.exists(PLACEMENT_RECIPE)
                && !Files.exists(OLD_PLACEMENT_RECIPE)
                : "the placement wand recipe must use the singular recipe data path";
        String recipe = Files.readString(PLACEMENT_RECIPE);
        assert recipe.contains("\"type\": \"minecraft:crafting_shaped\"")
                && recipe.contains("\"result\": { \"id\": \"modern_companions:placement_wand\", \"count\": 1 }")
                : "the placement wand recipe must use the project's shaped-recipe format";
    }
}
