package com.majorbonghits.moderncompanions.client.screen;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.majorbonghits.moderncompanions.ModernCompanions;
import com.majorbonghits.moderncompanions.item.StoredCompanionItem;
import com.majorbonghits.moderncompanions.menu.CompanionTableMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Companion Table screen using the supplied enchanting-table texture and animated book. */
public final class CompanionTableScreen extends AbstractContainerScreen<CompanionTableMenu> {
    private static final ResourceLocation[] ENABLED_LEVEL_SPRITES = {
            sprite("level_1"), sprite("level_2"), sprite("level_3")
    };
    private static final ResourceLocation[] DISABLED_LEVEL_SPRITES = {
            sprite("level_1_disabled"), sprite("level_2_disabled"), sprite("level_3_disabled")
    };
    private static final ResourceLocation TRAIT_SLOT_SPRITE = sprite("trait_slot");
    private static final ResourceLocation TRAIT_SLOT_DISABLED_SPRITE = sprite("trait_slot_disabled");
    private static final ResourceLocation TRAIT_SLOT_HIGHLIGHTED_SPRITE = sprite("trait_slot_highlighted");
    private static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ModernCompanions.MOD_ID, "textures/gui/companion_table.png");
    private static final ResourceLocation ECHO_OUTLINE = ResourceLocation.fromNamespaceAndPath(
            ModernCompanions.MOD_ID, "textures/gui/echo_shard_outline.png");
    private static final ResourceLocation BOOK_TEXTURE = ResourceLocation.withDefaultNamespace(
            "textures/entity/enchanting_table_book.png");

    private final RandomSource random = RandomSource.create();
    private BookModel bookModel;
    private ItemStack lastSoulGem = ItemStack.EMPTY;
    private float flip;
    private float oldFlip;
    private float flipTarget;
    private float flipAcceleration;
    private float open;
    private float oldOpen;

    public CompanionTableScreen(CompanionTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 185;
    }

    @Override
    protected void init() {
        super.init();
        bookModel = new BookModel(minecraft.getEntityModels().bakeLayer(ModelLayers.BOOK));
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        tickBook();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(GUI_TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        if (menu.getSlot(CompanionTableMenu.ECHO_SHARD_SLOT).getItem().isEmpty()) {
            graphics.blit(ECHO_OUTLINE, leftPos + 15, topPos + 67, 0, 0, 16, 16, 16, 16);
        }
        renderBook(graphics, partialTick);

        for (int i = 0; i < 3; i++) {
            String traitId = menu.getTraitOption(i);
            renderTraitSlot(graphics, i, mouseX, mouseY, traitId != null);
            if (traitId != null) {
                Component label = traitLabel(traitId);
                int color = canSelectTrait() ? 0xFFFFFF : 0x777777;
                graphics.drawString(font, label, leftPos + 80, topPos + 16 + i * 19, color, false);
            }
        }

    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 4, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, 8, 91, 0x404040, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int relativeX = (int) mouseX - leftPos;
        int relativeY = (int) mouseY - topPos;
        if (relativeX >= 60 && relativeX < 168) {
            for (int row = 0; row < 3; row++) {
                if (relativeY >= 14 + row * 19 && relativeY < 33 + row * 19
                        && menu.getTraitOption(row) != null && minecraft != null && minecraft.gameMode != null) {
                    int traitSlot = button == 1 ? 1 : 0;
                    int buttonId = traitSlot * 3 + row;
                    // Match vanilla's preflight call, then send the server-authoritative choice.
                    if (menu.clickMenuButton(minecraft.player, buttonId)) {
                        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, buttonId);
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);

        int relativeX = mouseX - leftPos;
        int relativeY = mouseY - topPos;
        if (relativeX >= 60 && relativeX < 168) {
            for (int row = 0; row < 3; row++) {
                if (relativeY >= 14 + row * 19 && relativeY < 33 + row * 19
                        && menu.getTraitOption(row) != null) {
                    List<Component> tooltip = new ArrayList<>();
                    addTraitTooltipLines(tooltip, menu.getTraitOption(row));
                    tooltip.add(Component.translatable("gui.modern_companions.companion_table.costs",
                            StoredCompanionItem.PRIMARY_REFORGE_COST, StoredCompanionItem.SECONDARY_REFORGE_COST));
                    tooltip.add(Component.translatable("gui.modern_companions.companion_table.click_hint"));
                    // Bond II is required for primary reforging and therefore represents the
                    // highest requirement for the two actions offered by each trait row.
                    if (StoredCompanionItem.getBondLevel(menu.getSoulGem()) < 2) {
                        tooltip.add(Component.translatable(
                                "gui.modern_companions.companion_table.bond_not_strong")
                                .withStyle(ChatFormatting.RED));
                    }
                    graphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
                    break;
                }
            }
        }
    }

    private void renderBook(GuiGraphics graphics, float partialTick) {
        float openAmount = Mth.lerp(partialTick, oldOpen, open);
        float flipAmount = Mth.lerp(partialTick, oldFlip, flip);
        Lighting.setupForEntityInInventory();
        graphics.pose().pushPose();
        graphics.pose().translate((float) leftPos + 33.0F, (float) topPos + 31.0F, 100.0F);
        graphics.pose().scale(-40.0F, 40.0F, 40.0F);
        graphics.pose().mulPose(Axis.XP.rotationDegrees(25.0F));
        graphics.pose().translate((1.0F - openAmount) * 0.2F, (1.0F - openAmount) * 0.1F,
                (1.0F - openAmount) * 0.25F);
        graphics.pose().mulPose(Axis.YP.rotationDegrees(-(1.0F - openAmount) * 90.0F - 90.0F));
        graphics.pose().mulPose(Axis.XP.rotationDegrees(180.0F));
        float leftPage = Mth.clamp(Mth.frac(flipAmount + 0.25F) * 1.6F - 0.3F, 0.0F, 1.0F);
        float rightPage = Mth.clamp(Mth.frac(flipAmount + 0.75F) * 1.6F - 0.3F, 0.0F, 1.0F);
        bookModel.setupAnim(0.0F, leftPage, rightPage, openAmount);
        VertexConsumer consumer = graphics.bufferSource().getBuffer(bookModel.renderType(BOOK_TEXTURE));
        bookModel.renderToBuffer(graphics.pose(), consumer, 15728880, OverlayTexture.NO_OVERLAY);
        graphics.flush();
        graphics.pose().popPose();
        Lighting.setupFor3DItems();
    }

    private void tickBook() {
        ItemStack soulGem = menu.getSoulGem();
        if (!ItemStack.matches(soulGem, lastSoulGem)) {
            lastSoulGem = soulGem.copy();
            do {
                flipTarget += random.nextInt(4) - random.nextInt(4);
            } while (flip <= flipTarget + 1.0F && flip >= flipTarget - 1.0F);
        }

        oldFlip = flip;
        oldOpen = open;
        if (menu.getTraitOption(0) != null) {
            open += 0.2F;
        } else {
            open -= 0.2F;
        }
        open = Mth.clamp(open, 0.0F, 1.0F);
        float acceleration = Mth.clamp((flipTarget - flip) * 0.4F, -0.2F, 0.2F);
        flipAcceleration += (acceleration - flipAcceleration) * 0.9F;
        flip += flipAcceleration;
    }

    private void renderTraitSlot(GuiGraphics graphics, int row, int mouseX, int mouseY, boolean hasTrait) {
        int x = leftPos + 60;
        int y = topPos + 14 + row * 19;
        boolean enabled = hasTrait && canSelectTrait();
        boolean hovered = enabled && mouseX >= x && mouseY >= y && mouseX < x + 108 && mouseY < y + 19;

        // Match vanilla's slot -> level-icon draw order while keeping the sprites mod-owned.
        RenderSystem.enableBlend();
        graphics.blitSprite(enabled
                ? (hovered ? TRAIT_SLOT_HIGHLIGHTED_SPRITE : TRAIT_SLOT_SPRITE)
                : TRAIT_SLOT_DISABLED_SPRITE, x, y, 108, 19);
        if (hasTrait) {
            graphics.blitSprite(enabled ? ENABLED_LEVEL_SPRITES[row] : DISABLED_LEVEL_SPRITES[row],
                    x + 1, y + 1, 16, 16);
        }
        RenderSystem.disableBlend();
    }

    private boolean canSelectTrait() {
        // Bond I is enough for the secondary action represented by every row.
        return hasMaterialInputs() && StoredCompanionItem.getBondLevel(menu.getSoulGem()) >= 1;
    }

    private static ResourceLocation sprite(String name) {
        return ResourceLocation.fromNamespaceAndPath(ModernCompanions.MOD_ID,
                "container/companion_table/" + name);
    }

    private boolean hasMaterialInputs() {
        return !menu.getSlot(CompanionTableMenu.LAPIS_SLOT).getItem().isEmpty()
                && !menu.getSlot(CompanionTableMenu.ECHO_SHARD_SLOT).getItem().isEmpty();
    }

    private Component traitLabel(String traitId) {
        String full = Component.translatable("trait.modern_companions." + traitId).getString();
        return Component.literal(full.split("\\R", 2)[0]);
    }

    private void addTraitTooltipLines(List<Component> tooltip, String traitId) {
        // Split localized trait text before tooltip formatting so LF is not rendered as a glyph.
        for (String line : Component.translatable("trait.modern_companions." + traitId)
                .getString().split("\\R")) {
            if (!line.isBlank()) {
                tooltip.add(Component.literal(line.trim()));
            }
        }
    }
}
