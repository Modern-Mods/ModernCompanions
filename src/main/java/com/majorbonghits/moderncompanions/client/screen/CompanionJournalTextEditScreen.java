package com.majorbonghits.moderncompanions.client.screen;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import com.majorbonghits.moderncompanions.network.EditCompanionJournalPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import org.lwjgl.glfw.GLFW;

/** One native text field for a journal edit; Enter submits the owner-checked update. */
public class CompanionJournalTextEditScreen extends Screen {
    private final Screen parent;
    private final int companionId;
    private final String field;
    private EditBox input;

    public CompanionJournalTextEditScreen(Screen parent, int companionId, String field) {
        super(Component.translatable("gui.modern_companions.journal.edit." + field));
        this.parent = parent;
        this.companionId = companionId;
        this.field = field;
    }

    @Override
    protected void init() {
        input = addRenderableWidget(new EditBox(font, width / 2 - 100, height / 2 - 10, 200, 20, getTitle()));
        input.setMaxLength(field.equals("age") ? 3 : field.equals("bio") ? 240 : field.equals("name") ? 64 : 240);
        currentCompanion().ifPresent(companion -> input.setValue(switch (field) {
            case "name" -> companion.getName().getString();
            case "age" -> Integer.toString(companion.getAgeYears());
            case "bio" -> companion.getCustomBio();
            case "skin" -> companion.getCustomSkinUrl().orElse("");
            default -> "";
        }));
        addRenderableWidget(new CompanionJournalEditScreen.JournalTexturedButton(
                Component.translatable("gui.done"), width / 2 - 24, height / 2 + 18, this::submit));
        setInitialFocus(input);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            submit();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void submit() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() != null) {
            mc.getConnection().send(new ServerboundCustomPayloadPacket(
                    new EditCompanionJournalPayload(companionId, field, input.getValue())));
        }
        mc.setScreen(parent);
    }

    private java.util.Optional<AbstractHumanCompanionEntity> currentCompanion() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || !(mc.level.getEntity(companionId) instanceof AbstractHumanCompanionEntity companion)) {
            return java.util.Optional.empty();
        }
        return java.util.Optional.of(companion);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        renderBackground(gfx, mouseX, mouseY, partialTick);
        gfx.drawCenteredString(font, getTitle(), width / 2, height / 2 - 32, 0xFFFFFF);
        super.render(gfx, mouseX, mouseY, partialTick);
    }
}
