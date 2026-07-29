package com.majorbonghits.moderncompanions.client.screen;

import com.majorbonghits.moderncompanions.ModernCompanions;
import com.majorbonghits.moderncompanions.client.screen.job.CompanionJobScreen;
import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import com.majorbonghits.moderncompanions.menu.CompanionMenu;
import com.majorbonghits.moderncompanions.network.CompanionActionPayload;
import com.majorbonghits.moderncompanions.network.OpenCompanionCuriosPayload;
import com.majorbonghits.moderncompanions.network.OpenCompanionBackpackPayload;
import com.majorbonghits.moderncompanions.network.SetPatrolRadiusPayload;
import com.majorbonghits.moderncompanions.network.ToggleFlagPayload;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.fml.ModList;

import java.util.Optional;
import java.util.function.BooleanSupplier;

/** Companion inventory screen using the seven-row companion layout. */
public class CompanionScreen extends AbstractContainerScreen<CompanionMenu> {
    private static final ResourceLocation BG = ResourceLocation.fromNamespaceAndPath(
            ModernCompanions.MOD_ID, "textures/gui/newinventory.png");
    private static final ResourceLocation BUTTONS = ResourceLocation.fromNamespaceAndPath(
            ModernCompanions.MOD_ID, "textures/gui/newbuttons.png");
    private static final int BG_WIDTH = 355;
    private static final int BG_HEIGHT = 249;
    private static final int BUTTON_X = 181;
    private static final int BUTTON_Y = 17;
    private static final int TEXT_COLOR = 0xFF000000;

    public CompanionScreen(CompanionMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = BG_WIDTH;
        this.imageHeight = BG_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        int buttonX = leftPos + BUTTON_X;
        int buttonY = topPos + BUTTON_Y;
        addRenderableWidget(new TexturedButton("Alert", buttonX, buttonY,
                () -> safeCompanion().map(AbstractHumanCompanionEntity::isAlert).orElse(false),
                () -> sendToggle("alert")));
        addRenderableWidget(new TexturedButton("Hunting", buttonX, buttonY + 17,
                () -> safeCompanion().map(AbstractHumanCompanionEntity::isHunting).orElse(false),
                () -> sendToggle("hunt")));
        addRenderableWidget(new TexturedButton("Patrol", buttonX, buttonY + 34,
                () -> safeCompanion().map(AbstractHumanCompanionEntity::isPatrolling).orElse(false),
                () -> sendOrder("patrol")));
        addRenderableWidget(new TexturedButton("Guard", buttonX, buttonY + 51,
                () -> safeCompanion().map(AbstractHumanCompanionEntity::isGuarding).orElse(false),
                () -> sendOrder("guard")));
        addRenderableWidget(new TexturedButton("Follow", buttonX, buttonY + 68,
                () -> safeCompanion().map(AbstractHumanCompanionEntity::isFollowing).orElse(false),
                () -> sendOrder("follow")));
        addRenderableWidget(new TexturedButton("Sprint", buttonX, buttonY + 85,
                () -> safeCompanion().map(AbstractHumanCompanionEntity::isSprintEnabled).orElse(false),
                () -> sendToggle("sprint")));
        addRenderableWidget(new TexturedButton("Clear", buttonX, buttonY + 102, () -> false,
                () -> sendAction("clear_target")));
        addRenderableWidget(new TexturedButton("Pickup", buttonX, buttonY + 119,
                () -> safeCompanion().map(AbstractHumanCompanionEntity::isPickupEnabled).orElse(false),
                () -> sendToggle("pickup")));

        ResourceLocation radiusTex = ResourceLocation.fromNamespaceAndPath(
                ModernCompanions.MOD_ID, "textures/gui/radiusbutton.png");
        // The 16px sprites are centered on the former 12px control row.
        int radiusY = topPos + 154;
        addRenderableWidget(new RadiusButton(leftPos + 182, radiusY, 16, radiusTex, () -> adjustRadius(-2)));
        addRenderableWidget(new RadiusButton(leftPos + 205, radiusY, 0, radiusTex, () -> adjustRadius(2)));

        addRenderableWidget(new TexturedButton("Jobs", buttonX, topPos + 174, () -> false, this::openJobInfo));
        addRenderableWidget(new TexturedButton(Component.translatable("button.modern_companions.journal"),
                buttonX, topPos + 192, () -> false, this::openJournal));
        if (ModList.get().isLoaded("sophisticatedbackpacks") && ModList.get().isLoaded("curios")) {
            addRenderableWidget(new TexturedButton("Backpack", buttonX, topPos + 210, () -> false, this::openBackpack));
        }
        if (ModList.get().isLoaded("curios")) {
            addRenderableWidget(new TexturedButton("Curios", buttonX, topPos + 228, () -> false, this::openCurios));
        }
        addRenderableWidget(new ModeButton(leftPos + 267, topPos + 232, Component.literal("V"),
                () -> safeCompanion().map(AbstractHumanCompanionEntity::canHarmVillagers).orElse(false),
                () -> sendToggle("villagers")));
        addRenderableWidget(new ModeButton(leftPos + 284, topPos + 232, Component.literal("P"),
                () -> safeCompanion().map(AbstractHumanCompanionEntity::canHarmPlayers).orElse(false),
                () -> sendToggle("players")));
        addRenderableWidget(new TexturedButton("Release", leftPos + 301, topPos + 232, () -> false, () -> {
            sendAction("release");
            onClose();
        }, true));
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float partialTick, int mouseX, int mouseY) {
        gfx.blit(BG, leftPos, topPos, 0, 0, imageWidth, imageHeight, BG_WIDTH, BG_HEIGHT);
    }

    @Override
    protected void renderLabels(GuiGraphics gfx, int mouseX, int mouseY) {
        safeCompanion().ifPresent(companion -> {
            drawCenteredText(gfx, companion.getDisplayName(), 296, 13);
            renderCompanionInfo(gfx, companion);
            renderAttributes(gfx, companion);
            renderWantedFood(gfx, companion);
        });
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        super.render(gfx, mouseX, mouseY, partialTick);
        renderTooltip(gfx, mouseX, mouseY);
    }

    private void renderCompanionInfo(GuiGraphics gfx, AbstractHumanCompanionEntity companion) {
        int x = 238;
        gfx.drawString(font, companion.getClassDisplayName(), x, 55, TEXT_COLOR, false);
        gfx.drawString(font, "Health: %.1f / %d".formatted(companion.getHealth(), (int) companion.getMaxHealth()),
                x, 65, TEXT_COLOR, false);

        float xpFraction = companion.getExperienceProgress();
        int xpNeeded = companion.getXpNeededForNextLevel();
        int xpHave = Math.round(xpFraction * xpNeeded);
        gfx.drawString(font, "Level " + companion.getExpLvl(), x, 75, TEXT_COLOR, false);
        gfx.fill(x, 86, x + 100, 92, 0xFF777777);
        gfx.fill(x + 1, 87, x + 1 + (int) (98 * xpFraction), 91, 0xFF55AA55);
        gfx.drawString(font, xpHave + "/" + xpNeeded, x, 93, TEXT_COLOR, false);
        // The taller Companion panel leaves room for the final status line below the XP readout.
        gfx.drawString(font, "Kills: " + companion.getKillCount(), x, 105, TEXT_COLOR, false);
        gfx.drawString(font, "Radius: " + companion.getPatrolRadius(), x + 45, 105, TEXT_COLOR, false);
    }

    private void renderAttributes(GuiGraphics gfx, AbstractHumanCompanionEntity companion) {
        int x = 246;
        drawStatLine(gfx, x, 138, "Strength", companion.getStrength(), isSpecialist(companion, 0));
        drawStatLine(gfx, x, 148, "Dexterity", companion.getDexterity(), isSpecialist(companion, 1));
        drawStatLine(gfx, x, 158, "Intelligence", companion.getIntelligence(), isSpecialist(companion, 2));
        drawStatLine(gfx, x, 168, "Endurance", companion.getEndurance(), isSpecialist(companion, 3));
    }

    private void drawStatLine(GuiGraphics gfx, int x, int y, String name, int value, boolean highlight) {
        gfx.drawString(font, name + ": " + value + (highlight ? " ★" : ""), x, y,
                highlight ? 0xFFFFD54F : TEXT_COLOR, false);
    }

    private boolean isSpecialist(AbstractHumanCompanionEntity companion, int index) {
        return companion.getSpecialistAttributeIndex() == index;
    }

    private void renderWantedFood(GuiGraphics gfx, AbstractHumanCompanionEntity companion) {
        String food = companion.getFoodStatusForGui();
        if (food.isEmpty()) {
            food = "Not Hungry";
        }
        int y = 204;
        for (FormattedCharSequence line : font.split(Component.literal(food), 106)) {
            gfx.drawString(font, line, 247, y, TEXT_COLOR, false);
            y += 10;
            if (y > 224) {
                break;
            }
        }
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

    private Optional<AbstractHumanCompanionEntity> safeCompanion() {
        AbstractHumanCompanionEntity companion = menu.getCompanion();
        if (companion == null && minecraft != null && minecraft.level != null
                && minecraft.level.getEntity(menu.getCompanionId()) instanceof AbstractHumanCompanionEntity found) {
            companion = found;
        }
        return Optional.ofNullable(companion);
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

    /** Safety switches use explicit colors instead of the generic on/off button sprite. */
    private class ModeButton extends Button {
        private final Component label;
        private final BooleanSupplier enabled;

        ModeButton(int x, int y, Component label, BooleanSupplier enabled, Runnable onClick) {
            super(x, y, 16, 16, Component.empty(), button -> onClick.run(), DEFAULT_NARRATION);
            this.label = label;
            this.enabled = enabled;
        }

        @Override
        public void renderWidget(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
            boolean on = enabled.getAsBoolean();
            int color = on ? 0xFFC63C3C : 0xFF3C9C57;
            if (isHoveredOrFocused()) color = on ? 0xFFE55353 : 0xFF54B86C;
            gfx.fill(getX(), getY(), getX() + width, getY() + height, color);
            gfx.renderOutline(getX(), getY(), width, height, 0xFF101010);
            drawCenteredText(gfx, label, getX() + width / 2, getY() + 4);
        }
    }
}
