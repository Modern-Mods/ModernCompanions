package com.majorbonghits.moderncompanions.compat.jade;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.ui.Element;

/** Mekanism-style 100 by 13 resource bar without a Mekanism dependency. */
final class CompanionResourceBarElement extends Element {
    private static final int WIDTH = 100;
    private static final int HEIGHT = 13;

    private final String text;
    private final int filled;
    private final int color;

    CompanionResourceBarElement(String name, int current, int max, int color) {
        text = name + " " + current + "/" + max;
        filled = max <= 0 ? 0 : Mth.clamp(Math.round((WIDTH - 2) * current / (float) max), 0, WIDTH - 2);
        this.color = color;
    }

    @Override
    public Vec2 getSize() {
        return new Vec2(WIDTH, HEIGHT + 2);
    }

    @Override
    public void render(GuiGraphics graphics, float rawX, float rawY, float maxX, float maxY) {
        int x = Mth.floor(rawX);
        int y = Mth.floor(rawY) + 1;
        // Same border and interior bounds as Mekanism's LookingAtElement (MIT).
        graphics.fill(x, y, x + WIDTH - 1, y + 1, 0xFF000000);
        graphics.fill(x, y, x + 1, y + HEIGHT - 1, 0xFF000000);
        graphics.fill(x + WIDTH - 1, y, x + WIDTH, y + HEIGHT - 1, 0xFF000000);
        graphics.fill(x, y + HEIGHT - 1, x + WIDTH, y + HEIGHT, 0xFF000000);
        if (filled > 0) {
            graphics.fill(x + 1, y + 1, x + 1 + filled, y + HEIGHT - 1, color);
        }
        graphics.drawString(Minecraft.getInstance().font, text, x + 3, y + 3, 0xFFFFFF, false);
    }
}
