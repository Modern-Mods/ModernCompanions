package com.majorbonghits.moderncompanions.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

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
        int x = (width - 100) / 2;
        int y = height / 2 - 48;
        addRenderableWidget(Button.builder(Component.translatable("gui.modern_companions.journal.edit.name"), button -> open("name"))
                .bounds(x, y, 100, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.modern_companions.journal.edit.age"), button -> open("age"))
                .bounds(x, y + 24, 100, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.modern_companions.journal.edit.bio"), button -> open("bio"))
                .bounds(x, y + 48, 100, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.modern_companions.journal.edit.skin"), button -> open("skin"))
                .bounds(x, y + 72, 100, 20).build());
        // Keep an explicit visible return path beneath every edit choice.
        addRenderableWidget(Button.builder(Component.translatable("gui.back"), button -> onClose())
                .bounds(x, y + 96, 100, 20).build());
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
}
