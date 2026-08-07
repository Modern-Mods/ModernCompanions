package com.majorbonghits.moderncompanions.client.screen;

import com.majorbonghits.moderncompanions.entity.personality.CompanionPersonality;
import com.majorbonghits.moderncompanions.item.StoredCompanionItem;
import com.majorbonghits.moderncompanions.menu.TraitReforgingMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Small native-button screen for choosing a primary or secondary Soul Reforging result. */
public class TraitReforgingScreen extends AbstractContainerScreen<TraitReforgingMenu> {
    private static final int PANEL_WIDTH = 316;
    private static final int PANEL_HEIGHT = 164;

    public TraitReforgingScreen(TraitReforgingMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = PANEL_WIDTH;
        imageHeight = PANEL_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        for (int i = 0; i < 3; i++) {
            final int option = i;
            addRenderableWidget(Button.builder(traitLabel(menu.getOption(option)), button -> choose(option))
                    .bounds(leftPos + 10 + option * 102, topPos + 53, 96, 20).build());
            addRenderableWidget(Button.builder(traitLabel(menu.getOption(option)), button -> choose(3 + option))
                    .bounds(leftPos + 10 + option * 102, topPos + 103, 96, 20).build());
        }
    }

    private void choose(int buttonId) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, buttonId);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF25212A);
        graphics.fill(leftPos + 1, topPos + 1, leftPos + imageWidth - 1, topPos + imageHeight - 1, 0xFF3B3443);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, Component.translatable("gui.modern_companions.soul_reforging.title"), 10, 8, 0xFFFFFF, false);
        graphics.drawString(font, Component.translatable("gui.modern_companions.soul_reforging.cost"), 10, 23, 0xD0C6D8, false);
        graphics.drawString(font, Component.translatable("gui.modern_companions.soul_reforging.primary"), 10, 41, 0xFFFFFF, false);
        graphics.drawString(font, Component.translatable("gui.modern_companions.soul_reforging.secondary"), 10, 91, 0xFFFFFF, false);
        String primary = traitLabel(StoredCompanionItem.getTraitId(menu.getSoulGem(), CompanionPersonality.KEY_PRIMARY)).getString();
        String secondary = traitLabel(StoredCompanionItem.getTraitId(menu.getSoulGem(), CompanionPersonality.KEY_SECONDARY)).getString();
        graphics.drawString(font, Component.translatable("gui.modern_companions.soul_reforging.current", primary, secondary), 10, 142, 0xD0C6D8, false);
    }

    private Component traitLabel(String id) {
        if (id == null || id.isBlank()) return Component.translatable("trait.modern_companions.none");
        String full = Component.translatable("trait.modern_companions." + id).getString();
        return Component.literal(full.split("\\R", 2)[0]);
    }
}
