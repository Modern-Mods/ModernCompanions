package com.majorbonghits.moderncompanions.client.screen;

import com.majorbonghits.moderncompanions.ModernCompanions;
import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import com.majorbonghits.moderncompanions.entity.job.CompanionJob;
import com.majorbonghits.moderncompanions.menu.CompanionMenu;
import com.majorbonghits.moderncompanions.network.CompanionActionPayload;
import com.majorbonghits.moderncompanions.network.SetPatrolRadiusPayload;
import com.majorbonghits.moderncompanions.network.ToggleFlagPayload;
import com.majorbonghits.moderncompanions.network.OpenCompanionCuriosPayload;
import com.majorbonghits.moderncompanions.client.screen.job.CompanionJobScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import com.mojang.blaze3d.systems.RenderSystem;
import net.neoforged.fml.ModList;

import java.util.Optional;
import java.util.function.BooleanSupplier;

/**
 * Companion inventory screen styled like the original mod, including sidebar buttons and right-hand stats.
 */
public class CompanionScreen extends AbstractContainerScreen<CompanionMenu> {
    private static final ResourceLocation BG = ResourceLocation.fromNamespaceAndPath(ModernCompanions.MOD_ID, "textures/inventory_stats.png");
    private static final int BG_WIDTH = 345;
    private static final int BG_HEIGHT = 256;
    // Right-hand info panel on inventory_stats.png
    private static final int TOP_STATS_LEFT = 229;
    private static final int TOP_STATS_TOP = 7;
    private static final int TOP_STATS_RIGHT = 327;
    // Attribute block lives in 228,137 to 326,194
    private static final int ATTR_LEFT = 228;
    private static final int ATTR_TOP = 137;
    private static final int ATTR_RIGHT = 326;
    private static final int ATTR_BOTTOM = 194;
    // Wanted food strip shifted down to 228,215 to 327,236
    private static final int FOOD_LEFT = 228;
    private static final int FOOD_TOP = 215;
    private static final int FOOD_RIGHT = 327;
    private static final int FOOD_BOTTOM = 236;

    private Button releaseButton;
    private CompanionButton radiusMinus;
    private CompanionButton radiusPlus;
    private Button curiosButton;
    private Button jobInfoButton;
    private Button journalButton;

    private int sidebarX;

    public CompanionScreen(CompanionMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = BG_WIDTH;
        this.imageHeight = BG_HEIGHT; // draw full texture 1:1; prevents GL wrapping
        this.inventoryLabelY = this.imageHeight - 94;
        this.sidebarX = 174;
    }

    @Override
    protected void init() {
        super.init();
        // Nudge whole GUI down by 1px to align with texture shadow
        this.topPos += 1;
        // Vanilla buttons keep every command legible without the fragile icon atlas.
        int sidebarButtonX = leftPos + sidebarX + 1;
        int actionY = topPos + 5;
        addRenderableWidget(new ToggleButton("Alert", sidebarButtonX, actionY, () -> safeCompanion().map(AbstractHumanCompanionEntity::isAlert).orElse(false), () -> sendToggle("alert")));
        addRenderableWidget(new ToggleButton("Hunting", sidebarButtonX, actionY + 18, () -> safeCompanion().map(AbstractHumanCompanionEntity::isHunting).orElse(false), () -> sendToggle("hunt")));
        addRenderableWidget(Button.builder(Component.literal("Patrol"), b -> sendAction("cycle_orders")).pos(sidebarButtonX, actionY + 36).size(42, 16).build());
        addRenderableWidget(new ToggleButton("Sprint", sidebarButtonX, actionY + 54, () -> safeCompanion().map(AbstractHumanCompanionEntity::isSprintEnabled).orElse(false), () -> sendToggle("sprint")));
        addRenderableWidget(Button.builder(Component.literal("Clear"), b -> sendAction("clear_target")).pos(sidebarButtonX, actionY + 72).size(42, 16).build());
        addRenderableWidget(new ToggleButton("Pickup", sidebarButtonX, actionY + 90, () -> safeCompanion().map(AbstractHumanCompanionEntity::isPickupEnabled).orElse(false), () -> sendToggle("pickup")));

        int radiusY = actionY + 108;
        ResourceLocation radiusTex = ResourceLocation.fromNamespaceAndPath(ModernCompanions.MOD_ID, "textures/gui/radiusbutton.png");
        radiusMinus = addRenderableWidget(new CompanionButton(leftPos + sidebarX + 3, radiusY, 16, 12, 17, 0, 13, radiusTex, () -> adjustRadius(-2)));
        radiusPlus = addRenderableWidget(new CompanionButton(leftPos + sidebarX + 21, radiusY, 16, 12, 0, 0, 13, radiusTex, () -> adjustRadius(2)));

        int jobInfoY = radiusY + 18;
        jobInfoButton = addRenderableWidget(Button.builder(Component.literal("Jobs"), b -> openJobInfo())
                .pos(sidebarButtonX, jobInfoY)
                .size(42, 16)
                .build());

        int journalY = jobInfoY + 18;
        journalButton = addRenderableWidget(Button.builder(Component.translatable("button.modern_companions.journal"), b -> openJournal())
                .pos(sidebarButtonX, journalY)
                .size(42, 16)
                .build());

        int curiosY = journalY + 18;
        if (ModList.get().isLoaded("curios")) {
            curiosButton = addRenderableWidget(Button.builder(Component.literal("Curios"), b -> openCurios())
                    .pos(sidebarButtonX, curiosY)
                    .size(42, 16)
                    .build());
        }

        releaseButton = addRenderableWidget(Button.builder(Component.literal("Release"), b -> {
                    sendAction("release");
                    this.onClose();
                })
                .pos(sidebarButtonX, topPos + 203)
                .size(42, 16)
                .build());
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        gfx.blit(BG, x, y, 0, 0, this.imageWidth, this.imageHeight, BG_WIDTH, BG_HEIGHT);
    }

    @Override
    protected void renderLabels(GuiGraphics gfx, int mouseX, int mouseY) {
        gfx.drawString(this.font, Component.literal("Companion Inventory"), 5, 5, 0x000000, false);
        gfx.drawString(this.font, Component.literal("Inventory"), 5, 130, 0x000000, false);
        // Vanilla labels are replaced with section headers and custom stats at right.
        safeCompanion().ifPresent(companion -> {
            // renderLabels already translates to (leftPos, topPos); use texture-relative coords
            int statsX = TOP_STATS_LEFT + 4;
            int statsWidth = (TOP_STATS_RIGHT - TOP_STATS_LEFT) - 8;
            int y = TOP_STATS_TOP + 2;

            gfx.drawString(this.font, Component.literal("Class").withStyle(ChatFormatting.UNDERLINE), statsX, y, 0x000000, false);
            y += 10;
            String cls = companion.getClassDisplayName();
            gfx.drawString(this.font, Component.literal(cls), statsX, y, 0x000000, false);
            y += 12;

            gfx.drawString(this.font, Component.literal("Health").withStyle(ChatFormatting.UNDERLINE), statsX, y, 0x000000, false);
            y += 10;
            gfx.drawString(this.font, Component.literal(String.format("%.1f / %d", companion.getHealth(), (int) companion.getMaxHealth())), statsX, y, 0x000000, false);
            y += 12;

            float xpFrac = companion.getExperienceProgress();
            int xpNeeded = companion.getXpNeededForNextLevel();
            int xpHave = Math.round(xpFrac * xpNeeded);
            gfx.drawString(this.font, Component.literal("Level " + companion.getExpLvl()), statsX, y, 0x000000, false);
            y += 10;
            int barW = Math.max(60, Math.min(90, statsWidth));
            int barH = 6;
            int filledW = (int) (barW * xpFrac);
            gfx.fill(statsX, y, statsX + barW, y + barH, 0xFF777777);
            gfx.fill(statsX + 1, y + 1, statsX + 1 + filledW, y + barH - 1, 0xFF55AA55);
            y += 10;
            gfx.drawString(this.font, Component.literal(xpHave + "/" + xpNeeded), statsX, y, 0x000000, false);
            y += 12;

            gfx.drawString(this.font, Component.literal("Kills: " + companion.getKillCount()), statsX, y, 0x000000, false);
            y += 12;

            gfx.drawString(this.font, Component.literal("Patrol Radius: " + companion.getPatrolRadius()), statsX, y, 0x000000, false);
            renderAttributes(gfx, companion);
            renderWantedFood(gfx, companion);
        });
    }

    @Override
    public void containerTick() {
        super.containerTick();
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        super.render(gfx, mouseX, mouseY, partialTick);
        this.renderTooltip(gfx, mouseX, mouseY);
    }

    /* ---------- Button actions ---------- */

    private void sendToggle(String flag) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getConnection() == null) return;
        safeCompanion().ifPresent(companion -> {
            boolean newValue = !companion.getFlagValue(flag);
            mc.getConnection().send(new ServerboundCustomPayloadPacket(new ToggleFlagPayload(menu.getCompanionId(), flag, newValue)));
            companion.applyFlag(flag, newValue);
        });
    }

    private void sendAction(String action) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getConnection() == null) return;
        mc.getConnection().send(new ServerboundCustomPayloadPacket(new CompanionActionPayload(menu.getCompanionId(), action)));
    }

    private void adjustRadius(int delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getConnection() == null) return;
        safeCompanion().ifPresent(companion -> {
            int step = hasShiftDown() ? 10 : 2;
            int target = Math.max(2, Math.min(128, companion.getPatrolRadius() + (delta > 0 ? step : -step)));
            mc.getConnection().send(new ServerboundCustomPayloadPacket(new SetPatrolRadiusPayload(menu.getCompanionId(), target)));
            companion.setPatrolRadius(target);
        });
    }

    private void openCurios() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getConnection() == null) return;
        mc.getConnection().send(new ServerboundCustomPayloadPacket(new OpenCompanionCuriosPayload(menu.getCompanionId())));
    }

    private void openJobInfo() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;
        mc.setScreen(new CompanionJobScreen(this, menu.getCompanionId()));
    }

    private void openJournal() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;
        mc.setScreen(new CompanionJournalScreen(this, menu.getCompanionId()));
    }

    private Optional<AbstractHumanCompanionEntity> safeCompanion() {
        AbstractHumanCompanionEntity c = menu.getCompanion();
        if (c == null && this.minecraft != null && this.minecraft.level != null) {
            var e = this.minecraft.level.getEntity(menu.getCompanionId());
            if (e instanceof AbstractHumanCompanionEntity comp) {
                c = comp;
            }
        }
        return Optional.ofNullable(c);
    }

    private class CompanionButton extends Button {
        private final int yTexStart;
        private final int yDiffTex;
        private final ResourceLocation texture;
        private final int xTexStart;

        CompanionButton(int x, int y, int w, int h, int xTexStart, int yTexStart, int yDiffTex, ResourceLocation texture, Runnable onClick) {
            super(x, y, w, h, Component.empty(), b -> onClick.run(), DEFAULT_NARRATION);
            this.xTexStart = xTexStart;
            this.yTexStart = yTexStart;
            this.yDiffTex = yDiffTex;
            this.texture = texture;
        }

        @Override
        public void renderWidget(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
            int v = this.isHoveredOrFocused() ? this.yTexStart + this.yDiffTex : this.yTexStart;
            RenderSystem.enableBlend();
            gfx.blit(this.texture, this.getX(), this.getY(), this.xTexStart, v, this.width, this.height, 256, 256);
            RenderSystem.disableBlend();
        }
    }

    private class ToggleButton extends Button {
        private final BooleanSupplier selected;

        ToggleButton(String label, int x, int y, BooleanSupplier selected, Runnable onClick) {
            super(x, y, 42, 16, Component.literal(label), b -> onClick.run(), DEFAULT_NARRATION);
            this.selected = selected;
        }

        @Override
        public boolean isFocused() {
            // Keep the vanilla highlighted sprite while the server-synced toggle remains enabled.
            return selected.getAsBoolean();
        }
    }

    private void renderAttributes(GuiGraphics gfx, AbstractHumanCompanionEntity companion) {
        int x = ATTR_LEFT + 3;
        int y = ATTR_TOP + 3;
        int width = (ATTR_RIGHT - ATTR_LEFT) - 6;
        //gfx.drawString(this.font, Component.literal("Attributes").withStyle(ChatFormatting.UNDERLINE), x, y, 0x000000, false);
        //y += 10;
        drawStatLine(gfx, x, y, width, "Strength", companion.getStrength(), isSpecialist(companion, 0));
        y += 10;
        drawStatLine(gfx, x, y, width, "Dexterity", companion.getDexterity(), isSpecialist(companion, 1));
        y += 10;
        drawStatLine(gfx, x, y, width, "Intelligence", companion.getIntelligence(), isSpecialist(companion, 2));
        y += 10;
        drawStatLine(gfx, x, y, width, "Endurance", companion.getEndurance(), isSpecialist(companion, 3));
    }

    private void drawStatLine(GuiGraphics gfx, int x, int y, int width, String name, int value, boolean highlight) {
        String line = name + ": " + value + (highlight ? " ★" : "");
        int color = highlight ? 0xFFD54F : 0x000000;
        for (FormattedCharSequence seq : this.font.split(Component.literal(line), width)) {
            gfx.drawString(this.font, seq, x, y, color, false);
            y += 10;
            if (y > ATTR_BOTTOM) break;
        }
    }

    private boolean isSpecialist(AbstractHumanCompanionEntity companion, int idx) {
        return companion.getSpecialistAttributeIndex() == idx;
    }

    private void renderWantedFood(GuiGraphics gfx, AbstractHumanCompanionEntity companion) {
        int foodX = FOOD_LEFT + 2;
        int foodY = FOOD_TOP + 2;
        int foodWidth = (FOOD_RIGHT - FOOD_LEFT) - 4;
        String food = companion.getFoodStatusForGui();
        if (food.isEmpty()) {
            food = "Not Hungry";
        }
        for (FormattedCharSequence line : this.font.split(Component.literal(food), foodWidth)) {
            gfx.drawString(this.font, line, foodX, foodY, 0x000000, false);
            foodY += 10;
            if (foodY > FOOD_BOTTOM) break; // stay inside strip
        }
    }

    private int drawWrappedLine(GuiGraphics gfx, Component text, int x, int y, int width) {
        int currentY = y;
        for (FormattedCharSequence seq : this.font.split(text, width)) {
            gfx.drawString(this.font, seq, x, currentY, 0x000000, false);
            currentY += 10;
        }
        return currentY;
    }

    private Component traitName(String id) { return Component.empty(); }
    private Component backstoryName(String id) { return Component.empty(); }
    private String formatFirstTamed(AbstractHumanCompanionEntity companion) { return ""; }
    private String formatDistance(long meters) { return ""; }
}
