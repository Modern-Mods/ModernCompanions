package com.majorbonghits.moderncompanions.client.screen;

import com.majorbonghits.moderncompanions.ModernCompanions;
import com.majorbonghits.moderncompanions.client.renderer.CompanionRenderer;
import com.majorbonghits.moderncompanions.client.screen.job.CompanionJobScreen;
import com.majorbonghits.moderncompanions.compat.sophisticatedbackpacks.SophisticatedBackpackCompat;
import com.majorbonghits.moderncompanions.core.ModConfig;
import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import com.majorbonghits.moderncompanions.menu.CompanionMenu;
import com.majorbonghits.moderncompanions.network.CompanionActionPayload;
import com.majorbonghits.moderncompanions.network.CompanionToggleEquipmentRenderPayload;
import com.majorbonghits.moderncompanions.network.OpenCompanionCuriosPayload;
import com.majorbonghits.moderncompanions.network.OpenCompanionBackpackPayload;
import com.majorbonghits.moderncompanions.network.SetPatrolRadiusPayload;
import com.majorbonghits.moderncompanions.network.ToggleFlagPayload;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

import java.util.Optional;
import java.util.function.BooleanSupplier;

/** Companion inventory screen using the seven-row companion layout. */
public class CompanionScreen extends AbstractContainerScreen<CompanionMenu> {
    private static final ResourceLocation JOB_BG = ResourceLocation.fromNamespaceAndPath(
            ModernCompanions.MOD_ID, "textures/gui/newinventory.png");
    private static final ResourceLocation NO_JOB_BG = ResourceLocation.fromNamespaceAndPath(
            ModernCompanions.MOD_ID, "textures/gui/newinventory_nojob.png");
    private static final ResourceLocation BUTTONS = ResourceLocation.fromNamespaceAndPath(
            ModernCompanions.MOD_ID, "textures/gui/newbuttons.png");
    private static final ResourceLocation SMALL_BUTTONS = ResourceLocation.fromNamespaceAndPath(
            ModernCompanions.MOD_ID, "textures/gui/newbuttons_small.png");
    private static final ResourceLocation RENDER_TOGGLE = ResourceLocation.fromNamespaceAndPath(
            "curios", "textures/gui/curios/inventory.png");
    private static final int BG_WIDTH = 458;
    private static final int BG_HEIGHT = 249;
    private static final int CONTENT_X_OFFSET = 103;
    private static final int BUTTON_X = CONTENT_X_OFFSET + 181;
    private static final int BUTTON_Y = 17;
    private static final ResourceLocation COSMETIC_ARMOR = ResourceLocation.fromNamespaceAndPath(
            ModernCompanions.MOD_ID, "textures/gui/cosmeticarmor.png");
    private static final ResourceLocation EQUIPMENT_PANEL = ResourceLocation.fromNamespaceAndPath(
            ModernCompanions.MOD_ID, "textures/gui/equipmentpanel.png");
    private static final ResourceLocation MAGICAL_EQUIPMENT_PANEL = ResourceLocation.fromNamespaceAndPath(
            ModernCompanions.MOD_ID, "textures/gui/magical_equipmentpanel.png");
    private static final int EQUIPMENT_PANEL_X = 1;
    private static final int EQUIPMENT_PANEL_Y = 23;
    private static final int COSMETIC_PANEL_WIDTH = 101;
    private static final int COSMETIC_PANEL_HEIGHT = 90;
    private static final int COSMETIC_SLOT_X = 8;
    private static final int COSMETIC_SLOT_Y = 14;
    private static final int COSMETIC_CLOSE_X = 79;
    private static final int COSMETIC_CLOSE_Y = 69;
    private static final int COSMETIC_BUTTON_X = 78;
    // The magical panel uses this gap for its spellbook slot at panel y=33.
    private static final int COSMETIC_BUTTON_Y = 73;
    private static final int TEXT_COLOR = 0xFF000000;

    private boolean cosmeticArmorOpen;
    private boolean consumeCosmeticCloseRelease;

    public CompanionScreen(CompanionMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = BG_WIDTH;
        this.imageHeight = BG_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        for (var slot : this.menu.slots) {
            if (slot instanceof CompanionMenu.CompanionEquipmentSlot equipmentSlot) {
                int x = this.leftPos + slot.x + 12;
                int y = this.topPos + slot.y - 1;
                addRenderableWidget(new EquipmentRenderToggleButton(x, y, equipmentSlot.getEquipmentSlot()));
            }
        }
        addRenderableWidget(new CosmeticArmorButton(leftPos + COSMETIC_BUTTON_X, topPos + COSMETIC_BUTTON_Y,
                () -> cosmeticArmorOpen = true));
        int buttonX = leftPos + BUTTON_X;
        int buttonY = topPos + BUTTON_Y;
        addRenderableWidget(new TexturedButton(Component.translatable("button.modern_companions.alert"), buttonX, buttonY,
                () -> safeCompanion().map(AbstractHumanCompanionEntity::isAlert).orElse(false),
                () -> sendToggle("alert")));
        addRenderableWidget(new TexturedButton(Component.translatable("button.modern_companions.hunting"), buttonX, buttonY + 17,
                () -> safeCompanion().map(AbstractHumanCompanionEntity::isHunting).orElse(false),
                () -> sendToggle("hunt")));
        addRenderableWidget(new TexturedButton(Component.translatable("button.modern_companions.patrol"), buttonX, buttonY + 34,
                () -> safeCompanion().map(AbstractHumanCompanionEntity::isPatrolling).orElse(false),
                () -> sendOrder("patrol")));
        addRenderableWidget(new TexturedButton(Component.translatable("button.modern_companions.guard"), buttonX, buttonY + 51,
                () -> safeCompanion().map(companion -> companion.getJob().isWorker() ? companion.isWorkEnabled() : companion.isGuarding()).orElse(false),
                () -> safeCompanion().ifPresent(companion -> {
                    if (companion.getJob().isWorker()) sendToggle("work"); else sendOrder("guard");
                })) {
            @Override
            public void renderWidget(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
                setMessage(safeCompanion().map(companion -> companion.getJob().isWorker()
                        ? Component.translatable("button.modern_companions.work")
                        : Component.translatable("button.modern_companions.guard"))
                        .orElse(Component.translatable("button.modern_companions.guard")));
                super.renderWidget(gfx, mouseX, mouseY, partialTick);
            }
        });
        addRenderableWidget(new TexturedButton(Component.translatable("button.modern_companions.follow"), buttonX, buttonY + 68,
                () -> safeCompanion().map(AbstractHumanCompanionEntity::isFollowing).orElse(false),
                () -> sendOrder("follow")));
        addRenderableWidget(new TexturedButton(Component.translatable("button.modern_companions.sprint"), buttonX, buttonY + 85,
                () -> safeCompanion().map(AbstractHumanCompanionEntity::isSprintEnabled).orElse(false),
                () -> sendToggle("sprint")));
        addRenderableWidget(new TexturedButton(Component.translatable("button.modern_companions.clear"), buttonX, buttonY + 102, () -> false,
                () -> sendAction("clear_target")));
        addRenderableWidget(new TexturedButton(Component.translatable("button.modern_companions.pickup"), buttonX, buttonY + 119,
                () -> safeCompanion().map(AbstractHumanCompanionEntity::isPickupEnabled).orElse(false),
                () -> sendToggle("pickup")));

        ResourceLocation radiusTex = ResourceLocation.fromNamespaceAndPath(
                ModernCompanions.MOD_ID, "textures/gui/radiusbutton.png");
        // The 16px sprites are centered on the former 12px control row.
        int radiusY = topPos + 154;
        addRenderableWidget(new RadiusButton(leftPos + CONTENT_X_OFFSET + 182, radiusY, 16, radiusTex, () -> adjustRadius(-2)));
        addRenderableWidget(new RadiusButton(leftPos + CONTENT_X_OFFSET + 205, radiusY, 0, radiusTex, () -> adjustRadius(2)));

        int lowerButtonY = topPos + 174;
        if (ModConfig.safeGet(ModConfig.SHOW_JOBS_BUTTON)) {
            addRenderableWidget(new TexturedButton(Component.translatable("button.modern_companions.jobs"), buttonX, lowerButtonY, () -> false, this::openJobInfo));
            lowerButtonY += 18;
        }
        addRenderableWidget(new TexturedButton(Component.translatable("button.modern_companions.journal"),
                buttonX, lowerButtonY, () -> false, this::openJournal));
        lowerButtonY += 18;
        if (ModList.get().isLoaded("curios")) {
            addRenderableWidget(new TexturedButton(Component.translatable("button.modern_companions.curios"), buttonX, lowerButtonY, () -> false, this::openCurios));
            lowerButtonY += 18;
        }
        if (ModList.get().isLoaded("sophisticatedbackpacks") && ModList.get().isLoaded("curios")
                && safeCompanion().map(SophisticatedBackpackCompat::hasBackpack).orElse(false)) {
            addRenderableWidget(new TexturedButton(Component.translatable("button.modern_companions.pack"), buttonX, lowerButtonY, () -> false, this::openBackpack));
        }
        addRenderableWidget(new ModeButton(leftPos + 82, topPos + 147,
                () -> safeCompanion().map(AbstractHumanCompanionEntity::canHarmVillagers).orElse(false),
                () -> sendToggle("villagers")));
        addRenderableWidget(new ModeButton(leftPos + 82, topPos + 175,
                () -> safeCompanion().map(AbstractHumanCompanionEntity::canHarmPlayers).orElse(false),
                () -> sendToggle("players")));
        addRenderableWidget(new TexturedButton(Component.translatable("button.modern_companions.release"), leftPos + CONTENT_X_OFFSET + 301, topPos + 232, () -> false, () -> {
            sendAction("release");
            onClose();
        }, true));
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float partialTick, int mouseX, int mouseY) {
        // The no-job asset omits the inactive Currently/State panel entirely.
        ResourceLocation background = safeCompanion().filter(companion -> companion.getJob().isWorker()).isPresent()
                ? JOB_BG : NO_JOB_BG;
        gfx.blit(background, leftPos, topPos, 0, 0, imageWidth, imageHeight, BG_WIDTH, BG_HEIGHT);
        if (cosmeticArmorOpen) {
            return;
        }

        ResourceLocation equipmentPanel = isMagicalCompanion() ? MAGICAL_EQUIPMENT_PANEL : EQUIPMENT_PANEL;
        gfx.blit(equipmentPanel, leftPos + EQUIPMENT_PANEL_X, topPos + EQUIPMENT_PANEL_Y,
                0, 0, COSMETIC_PANEL_WIDTH, COSMETIC_PANEL_HEIGHT,
                COSMETIC_PANEL_WIDTH, COSMETIC_PANEL_HEIGHT);
        safeCompanion().ifPresent(companion -> {
            CompanionRenderer.setPreviewNameplateSuppressed(true);
            try {
                InventoryScreen.renderEntityInInventoryFollowsMouse(gfx,
                        leftPos + 27, topPos + 40, leftPos + 76, topPos + 107, 30, 0.0625F,
                        mouseX, mouseY, companion);
            } finally {
                CompanionRenderer.setPreviewNameplateSuppressed(false);
            }
        });
    }

    @Override
    protected void renderSlot(GuiGraphics gfx, Slot slot) {
        if (slot instanceof CompanionMenu.CompanionCosmeticArmorSlot
                || cosmeticArmorOpen && (slot instanceof CompanionMenu.CompanionEquipmentSlot
                || slot instanceof CompanionMenu.CompanionSpellbookSlot)) {
            return;
        }
        super.renderSlot(gfx, slot);
    }

    @Override
    protected void renderLabels(GuiGraphics gfx, int mouseX, int mouseY) {
        safeCompanion().ifPresent(companion -> {
            drawCenteredText(gfx, companion.getDisplayName(), CONTENT_X_OFFSET + 296, 13);
            renderCompanionInfo(gfx, companion);
            renderAttributes(gfx, companion);
            renderWantedFood(gfx, companion);
            renderCurrentJob(gfx, companion);
        });
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        super.render(gfx, mouseX, mouseY, partialTick);
        if (cosmeticArmorOpen) {
            renderCosmeticArmorPopup(gfx, mouseX, mouseY);
        }
        if (!cosmeticArmorOpen) {
            renderTooltip(gfx, mouseX, mouseY);
        }
        if (cosmeticArmorOpen) {
            ItemStack hovered = cosmeticArmorAt(mouseX, mouseY);
            if (hovered != null && !hovered.isEmpty()) {
                gfx.renderTooltip(font, hovered, mouseX, mouseY);
            }
        }
    }

    private void renderCosmeticArmorPopup(GuiGraphics gfx, int mouseX, int mouseY) {
        int x = leftPos + EQUIPMENT_PANEL_X;
        int y = topPos + EQUIPMENT_PANEL_Y;
        gfx.blit(COSMETIC_ARMOR, x, y, 0, 0, COSMETIC_PANEL_WIDTH, COSMETIC_PANEL_HEIGHT,
                COSMETIC_PANEL_WIDTH, COSMETIC_PANEL_HEIGHT);

        safeCompanion().ifPresent(companion -> {
            CompanionRenderer.setPreviewNameplateSuppressed(true);
            try {
                InventoryScreen.renderEntityInInventoryFollowsMouse(gfx, x + 26, y + 17, x + 75, y + 84,
                        30, 0.0625F, mouseX, mouseY, companion);
            } finally {
                CompanionRenderer.setPreviewNameplateSuppressed(false);
            }

            EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
            for (int i = 0; i < slots.length; i++) {
                int slotX = x + COSMETIC_SLOT_X;
                int slotY = y + COSMETIC_SLOT_Y + i * 18;
                ItemStack stack = companion.getCosmeticArmorItem(slots[i]);
                if (stack.isEmpty()) {
                    renderCosmeticSlotBackground(gfx, i, slotX, slotY);
                } else {
                    gfx.renderItem(stack, slotX, slotY);
                    gfx.renderItemDecorations(font, stack, slotX, slotY);
                }
            }
        });

        boolean closeHovered = isInside(mouseX, mouseY, x + COSMETIC_CLOSE_X, y + COSMETIC_CLOSE_Y, 16, 16);
        gfx.blit(SMALL_BUTTONS, x + COSMETIC_CLOSE_X, y + COSMETIC_CLOSE_Y, 0, closeHovered ? 16 : 32,
                16, 16, 16, 80);
    }

    private void renderCosmeticSlotBackground(GuiGraphics gfx, int index, int x, int y) {
        Pair<ResourceLocation, ResourceLocation> icon = menu.slots.get(index).getNoItemIcon();
        if (icon != null) {
            TextureAtlasSprite sprite = minecraft.getTextureAtlas(icon.getFirst()).apply(icon.getSecond());
            gfx.blit(x, y, 0, 16, 16, sprite);
        }
    }

    private ItemStack cosmeticArmorAt(double mouseX, double mouseY) {
        int x = leftPos + EQUIPMENT_PANEL_X + COSMETIC_SLOT_X;
        int y = topPos + EQUIPMENT_PANEL_Y + COSMETIC_SLOT_Y;
        for (int i = 0; i < 4; i++) {
            if (isInside(mouseX, mouseY, x, y + i * 18, 16, 16)) {
                int slotIndex = i;
                return safeCompanion().map(companion -> companion.getCosmeticArmorItem(cosmeticSlot(slotIndex)))
                        .orElse(ItemStack.EMPTY);
            }
        }
        return ItemStack.EMPTY;
    }

    private static EquipmentSlot cosmeticSlot(int index) {
        return switch (index) {
            case 0 -> EquipmentSlot.HEAD;
            case 1 -> EquipmentSlot.CHEST;
            case 2 -> EquipmentSlot.LEGS;
            case 3 -> EquipmentSlot.FEET;
            default -> null;
        };
    }

    private static boolean isInside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (cosmeticArmorOpen) {
            int panelX = leftPos + EQUIPMENT_PANEL_X;
            int panelY = topPos + EQUIPMENT_PANEL_Y;
            if (isInside(mouseX, mouseY, panelX + COSMETIC_CLOSE_X, panelY + COSMETIC_CLOSE_Y, 16, 16)) {
                cosmeticArmorOpen = false;
                // The close control overlaps the functional offhand slot; own its release too.
                consumeCosmeticCloseRelease = true;
                return true;
            }
            if (isInside(mouseX, mouseY, panelX, panelY, COSMETIC_PANEL_WIDTH, COSMETIC_PANEL_HEIGHT)) {
                for (int i = 0; i < 4; i++) {
                    if (isInside(mouseX, mouseY, panelX + COSMETIC_SLOT_X,
                            panelY + COSMETIC_SLOT_Y + i * 18, 16, 16)) {
                        if ((button == 0 || button == 1) && minecraft.gameMode != null && minecraft.player != null) {
                            minecraft.gameMode.handleInventoryMouseClick(menu.containerId,
                                    menu.getCosmeticArmorSlotIndex(i), button, ClickType.PICKUP, minecraft.player);
                        }
                        return true;
                    }
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (consumeCosmeticCloseRelease) {
            consumeCosmeticCloseRelease = false;
            return true;
        }
        if (cosmeticArmorOpen && isInside(mouseX, mouseY,
                leftPos + EQUIPMENT_PANEL_X, topPos + EQUIPMENT_PANEL_Y,
                COSMETIC_PANEL_WIDTH, COSMETIC_PANEL_HEIGHT)) {
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void renderCompanionInfo(GuiGraphics gfx, AbstractHumanCompanionEntity companion) {
        int x = CONTENT_X_OFFSET + 238;
        gfx.drawString(font, companion.getClassDisplayName(), x, 55, TEXT_COLOR, false);
        gfx.drawString(font, Component.translatable("gui.modern_companions.health", companion.getHealth(), (int) companion.getMaxHealth()),
                x, 65, TEXT_COLOR, false);

        float xpFraction = companion.getExperienceProgress();
        int xpNeeded = companion.getXpNeededForNextLevel();
        int xpHave = Math.round(xpFraction * xpNeeded);
        gfx.drawString(font, Component.translatable("gui.modern_companions.level", companion.getExpLvl()), x, 75, TEXT_COLOR, false);
        gfx.fill(x, 86, x + 100, 92, 0xFF777777);
        gfx.fill(x + 1, 87, x + 1 + (int) (98 * xpFraction), 91, 0xFF55AA55);
        gfx.drawString(font, Component.translatable("gui.modern_companions.xp_progress", xpHave, xpNeeded), x, 93, TEXT_COLOR, false);
        // The taller Companion panel leaves room for the final status line below the XP readout.
        gfx.drawString(font, Component.translatable("gui.modern_companions.kills", companion.getKillCount()), x, 105, TEXT_COLOR, false);
        gfx.drawString(font, Component.translatable("gui.modern_companions.radius", companion.getPatrolRadius()), x + 45, 105, TEXT_COLOR, false);
    }

    private void renderAttributes(GuiGraphics gfx, AbstractHumanCompanionEntity companion) {
        int x = CONTENT_X_OFFSET + 246;
        drawStatLine(gfx, x, 138, Component.translatable("gui.modern_companions.attribute.strength"), companion.getStrength(), isSpecialist(companion, 0));
        drawStatLine(gfx, x, 148, Component.translatable("gui.modern_companions.attribute.dexterity"), companion.getDexterity(), isSpecialist(companion, 1));
        drawStatLine(gfx, x, 158, Component.translatable("gui.modern_companions.attribute.intelligence"), companion.getIntelligence(), isSpecialist(companion, 2));
        drawStatLine(gfx, x, 168, Component.translatable("gui.modern_companions.attribute.endurance"), companion.getEndurance(), isSpecialist(companion, 3));
    }

    private void drawStatLine(GuiGraphics gfx, int x, int y, Component name, int value, boolean highlight) {
        gfx.drawString(font, Component.translatable("gui.modern_companions.attribute.value", name, value, highlight ? " ★" : ""), x, y,
                highlight ? 0xFFFFD54F : TEXT_COLOR, false);
    }

    private boolean isSpecialist(AbstractHumanCompanionEntity companion, int index) {
        return companion.getSpecialistAttributeIndex() == index;
    }

    private boolean isMagicalCompanion() {
        return safeCompanion().map(AbstractHumanCompanionEntity::hasMana).orElse(false);
    }

    private void renderWantedFood(GuiGraphics gfx, AbstractHumanCompanionEntity companion) {
        Component food = companion.getFoodStatusForGui();
        if (food.getString().isEmpty()) {
            food = Component.translatable("gui.modern_companions.food.not_hungry");
        }
        int y = 204;
        for (FormattedCharSequence line : font.split(food, 106)) {
            gfx.drawString(font, line, CONTENT_X_OFFSET + 247, y, TEXT_COLOR, false);
            y += 10;
            if (y > 224) {
                break;
            }
        }
    }

    /** Texture owns `Currently`; draw only synchronized dynamic job text inside its panel. */
    private void renderCurrentJob(GuiGraphics gfx, AbstractHumanCompanionEntity companion) {
        if (!companion.getJob().isWorker()) return;
        gfx.enableScissor(leftPos + 4, topPos + 205, leftPos + 100, topPos + 239);
        // renderLabels already translates to the GUI origin; using leftPos here drew off-panel.
        gfx.drawString(font, font.plainSubstrByWidth(companion.getJob().displayName().getString(), 90), 7, 216, TEXT_COLOR, false);
        gfx.drawString(font, font.plainSubstrByWidth(companion.getJobStatusComponent().getString(), 90), 7, 227, TEXT_COLOR, false);
        gfx.disableScissor();
    }

    private void drawCenteredText(GuiGraphics gfx, Component text, int centerX, int y) {
        gfx.drawString(font, text, centerX - font.width(text) / 2, y, TEXT_COLOR, false);
    }

    private void sendToggle(String flag) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getConnection() == null) return;
        safeCompanion().ifPresent(companion -> {
            boolean newValue = !companion.getFlagValue(flag);
            mc.getConnection().send(new ServerboundCustomPayloadPacket(
                    new ToggleFlagPayload(menu.getCompanionId(), flag, newValue)));
            companion.applyFlag(flag, newValue);
        });
    }

    private void sendOrder(String order) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getConnection() == null) return;
        // Orders are radio-style controls: selecting one always leaves exactly one active.
        mc.getConnection().send(new ServerboundCustomPayloadPacket(
                new ToggleFlagPayload(menu.getCompanionId(), order, true)));
        safeCompanion().ifPresent(companion -> companion.applyFlag(order, true));
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
            mc.getConnection().send(new ServerboundCustomPayloadPacket(
                    new SetPatrolRadiusPayload(menu.getCompanionId(), target)));
            companion.setPatrolRadius(target);
        });
    }

    private void openCurios() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.getConnection() != null) {
            mc.getConnection().send(new ServerboundCustomPayloadPacket(new OpenCompanionCuriosPayload(menu.getCompanionId())));
        }
    }

    private void openBackpack() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.getConnection() != null) {
            mc.getConnection().send(new ServerboundCustomPayloadPacket(new OpenCompanionBackpackPayload(menu.getCompanionId())));
        }
    }

    private void openJobInfo() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) {
            mc.setScreen(new CompanionJobScreen(this, menu.getCompanionId()));
        }
    }

    private void openJournal() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) {
            mc.setScreen(new CompanionJournalScreen(this, menu.getCompanionId()));
        }
    }

    private void toggleEquipmentRender(EquipmentSlot slot) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.getConnection() != null) {
            mc.getConnection().send(new ServerboundCustomPayloadPacket(
                    new CompanionToggleEquipmentRenderPayload(menu.getCompanionId(), equipmentIndex(slot))));
        }
    }

    private static int equipmentIndex(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> 0;
            case CHEST -> 1;
            case LEGS -> 2;
            case FEET -> 3;
            case MAINHAND -> 4;
            case OFFHAND -> 5;
            default -> -1;
        };
    }

    private Optional<AbstractHumanCompanionEntity> safeCompanion() {
        AbstractHumanCompanionEntity companion = menu.getCompanion();
        if (companion == null && minecraft != null && minecraft.level != null
                && minecraft.level.getEntity(menu.getCompanionId()) instanceof AbstractHumanCompanionEntity found) {
            companion = found;
        }
        return Optional.ofNullable(companion);
    }

    private final class CosmeticArmorButton extends Button {
        CosmeticArmorButton(int x, int y, Runnable onPress) {
            super(x, y, 16, 16, Component.translatable("button.modern_companions.cosmetic_armor"),
                    button -> onPress.run(), DEFAULT_NARRATION);
        }

        @Override
        public void renderWidget(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
            if (cosmeticArmorOpen) return;
            gfx.blit(SMALL_BUTTONS, getX(), getY(), 0, isHoveredOrFocused() ? 16 : 32,
                    width, height, 16, 80);
        }
    }

    private class RadiusButton extends Button {
        private final int xTexStart;
        private final ResourceLocation texture;

        RadiusButton(int x, int y, int xTexStart, ResourceLocation texture, Runnable onClick) {
            super(x, y, 16, 16, Component.empty(), button -> onClick.run(), DEFAULT_NARRATION);
            this.xTexStart = xTexStart;
            this.texture = texture;
        }

        @Override
        public void renderWidget(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
            int textureY = isHoveredOrFocused() ? 16 : 0;
            RenderSystem.enableBlend();
            gfx.blit(texture, getX(), getY(), xTexStart, textureY, width, height, 32, 32);
            RenderSystem.disableBlend();
        }
    }

    private class EquipmentRenderToggleButton extends Button {
        private final EquipmentSlot slot;

        EquipmentRenderToggleButton(int x, int y, EquipmentSlot slot) {
            super(x, y, 8, 8, Component.empty(), button -> toggleEquipmentRender(slot), DEFAULT_NARRATION);
            this.slot = slot;
        }

        @Override
        public void renderWidget(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
            if (cosmeticArmorOpen) return;
            int texX = safeCompanion().map(companion -> companion.isEquipmentRenderVisible(slot)).orElse(true) ? 75 : 83;
            gfx.blit(RENDER_TOGGLE, getX(), getY(), texX, 0, 8, 8, 256, 256);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return !cosmeticArmorOpen && super.mouseClicked(mouseX, mouseY, button);
        }
    }

    private class TexturedButton extends Button {
        private final BooleanSupplier toggled;
        private final Runnable releaseAction;
        private final boolean releaseStyle;
        private boolean releasePressed;

        TexturedButton(String label, int x, int y, BooleanSupplier toggled, Runnable onClick) {
            this(Component.literal(label), x, y, toggled, onClick, false);
        }

        TexturedButton(Component label, int x, int y, BooleanSupplier toggled, Runnable onClick) {
            this(label, x, y, toggled, onClick, false);
        }

        TexturedButton(String label, int x, int y, BooleanSupplier toggled, Runnable onClick, boolean releaseStyle) {
            this(Component.literal(label), x, y, toggled, onClick, releaseStyle);
        }

        TexturedButton(Component label, int x, int y, BooleanSupplier toggled, Runnable onClick, boolean releaseStyle) {
            super(x, y, 48, 16, label, button -> onClick.run(), DEFAULT_NARRATION);
            this.toggled = toggled;
            this.releaseAction = onClick;
            this.releaseStyle = releaseStyle;
        }

        @Override
        public void renderWidget(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
            int textureY = releaseStyle ? (releasePressed ? 64 : 48)
                    : (toggled.getAsBoolean() ? 32 : (isHoveredOrFocused() ? 16 : 0));
            RenderSystem.enableBlend();
            gfx.blit(BUTTONS, getX(), getY(), 0, textureY, width, height, 48, 80);
            RenderSystem.disableBlend();
            drawCenteredText(gfx, getMessage(), getX() + width / 2, getY() + 4);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!releaseStyle) {
                return super.mouseClicked(mouseX, mouseY, button);
            }
            if (button == 0 && active && visible && isMouseOver(mouseX, mouseY)) {
                releasePressed = true;
                return true;
            }
            return false;
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            if (!releaseStyle || !releasePressed || button != 0) {
                return false;
            }
            releasePressed = false;
            if (isMouseOver(mouseX, mouseY)) {
                releaseAction.run();
            }
            return true;
        }
    }

    /** Safety switches use the asset's green/off and dark-red/on states. */
    private class ModeButton extends Button {
        private final BooleanSupplier enabled;

        ModeButton(int x, int y, BooleanSupplier enabled, Runnable onClick) {
            super(x, y, 16, 16, Component.empty(), button -> onClick.run(), DEFAULT_NARRATION);
            this.enabled = enabled;
        }

        @Override
        public void renderWidget(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
            int textureY = enabled.getAsBoolean() ? 64 : 32;
            RenderSystem.enableBlend();
            gfx.blit(SMALL_BUTTONS, getX(), getY(), 0, textureY, width, height, 16, 80);
            RenderSystem.disableBlend();
        }
    }
}
