package com.majorbonghits.moderncompanions.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import static com.majorbonghits.moderncompanions.ModernCompanions.MOD_ID;

/** Small journal edit menu; each choice opens Minecraft's native text field screen. */
public class CompanionJournalEditScreen extends Screen {
    private final Screen parent;
    private final int companionId;

    public CompanionJournalEditScreen(Screen parent, int companionId) {
        super(Component.translatable("gui.modern_companions.journal.edit"));
        this.parent = parent;
        this.companionId = companionId;
    }

    @Override
    protected void init() {
        int x = (width - 48) / 2;
        int y = height / 2 - 48;
        addRenderableWidget(new JournalTexturedButton(Component.translatable("gui.modern_companions.journal.edit.name"), x, y,
                () -> open("name")));
        addRenderableWidget(new JournalTexturedButton(Component.translatable("gui.modern_companions.journal.edit.age"), x, y + 24,
                () -> open("age")));
        addRenderableWidget(new JournalTexturedButton(Component.translatable("gui.modern_companions.journal.edit.bio"), x, y + 48,
                () -> open("bio")));
        addRenderableWidget(new JournalTexturedButton(Component.translatable("gui.modern_companions.journal.edit.skin"), x, y + 72,
                () -> open("skin")));
        // Keep an explicit visible return path beneath every edit choice.
        addRenderableWidget(new JournalTexturedButton(Component.translatable("gui.back"), x, y + 96, this::onClose));
    }

    private void open(String field) {
        Minecraft.getInstance().setScreen(new CompanionJournalTextEditScreen(this, companionId, field));
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        renderBackground(gfx, mouseX, mouseY, partialTick);
        super.render(gfx, mouseX, mouseY, partialTick);
    }

    /** Reuses the inventory's 48x16 normal/hover button states for journal actions. */
    static final class JournalTexturedButton extends Button {
        private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
                MOD_ID, "textures/gui/newbuttons.png");

        JournalTexturedButton(Component message, int x, int y, Runnable onClick) {
            super(x, y, 48, 16, message, button -> onClick.run(), DEFAULT_NARRATION);
        }

        @Override
        public void renderWidget(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
            int textureY = isHoveredOrFocused() ? 16 : 0;
            RenderSystem.enableBlend();
            gfx.blit(TEXTURE, getX(), getY(), 0, textureY, width, height, 48, 80);
            RenderSystem.disableBlend();
            var font = Minecraft.getInstance().font;
            gfx.drawString(font, getMessage(), getX() + width / 2 - font.width(getMessage()) / 2,
                    getY() + 4, 0xFF000000, false);
        }
    }
}
