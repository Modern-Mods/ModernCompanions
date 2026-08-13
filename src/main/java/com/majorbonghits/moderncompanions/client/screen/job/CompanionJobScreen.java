package com.majorbonghits.moderncompanions.client.screen.job;

import com.majorbonghits.moderncompanions.ModernCompanions;
import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import com.majorbonghits.moderncompanions.entity.job.CompanionJob;
import com.majorbonghits.moderncompanions.entity.job.JobToolPolicy;
import com.majorbonghits.moderncompanions.network.SetCompanionJobPayload;
import com.majorbonghits.moderncompanions.network.CompanionActionPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/**
 * Dedicated job screen sharing the journal texture. Shows current job info and
 * lets the player cycle the role without crowding the main inventory UI.
 */
public class CompanionJobScreen extends Screen {
    private static final ResourceLocation BG = ResourceLocation.fromNamespaceAndPath(ModernCompanions.MOD_ID, "textures/journal.png");
    private static final int BG_W = 256;
    private static final int BG_H = 240;

    private final Screen parent;
    private final int companionId;
    private int leftPos;
    private int topPos;
    private CycleButton<CompanionJob> jobSelector;

    public CompanionJobScreen(Screen parent, int companionId) {
        super(Component.translatable("gui.modern_companions.job"));
        this.parent = parent;
        this.companionId = companionId;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - BG_W) / 2;
        this.topPos = (this.height - BG_H) / 2;

        jobSelector = addRenderableWidget(CycleButton.builder(CompanionJob::displayName)
                .withValues(CompanionJob.values())
                .withInitialValue(safeCompanion().map(AbstractHumanCompanionEntity::getJob).orElse(CompanionJob.NONE))
                .create(leftPos + 14, topPos + 26, 160, 20, Component.translatable("gui.modern_companions.job"), (btn, job) -> setJob(job)));

        addRenderableWidget(Button.builder(Component.translatable("gui.modern_companions.deposit"), b -> requestDeposit())
                .pos(leftPos + 10, topPos + BG_H - 24)
                .size(60, 16)
                .build());

        addRenderableWidget(Button.builder(Component.translatable("gui.back"), b -> closeToParent())
                .pos(leftPos + BG_W - 60, topPos + BG_H - 24)
                .size(50, 16)
                .build());
    }

    private void setJob(CompanionJob job) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getConnection() == null) return;
        safeCompanion().ifPresent(companion -> {
            mc.getConnection().send(new ServerboundCustomPayloadPacket(new SetCompanionJobPayload(companionId, job.id())));
            companion.setJob(job);
            companion.onJobChanged();
        });
    }

    private void closeToParent() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;
        mc.setScreen(parent);
    }

    private void requestDeposit() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getConnection() == null) return;
        mc.getConnection().send(new ServerboundCustomPayloadPacket(new CompanionActionPayload(companionId, "deliver_now")));
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        gfx.fillGradient(0, 0, this.width, this.height, 0xC0101010, 0xD0000000);
        gfx.blit(BG, leftPos, topPos, 0, 0, BG_W, BG_H, BG_W, BG_H);

        safeCompanion().ifPresent(companion -> {
            int x = leftPos + 14;
            int y = topPos + 54;
            int width = BG_W - 28;

            CompanionJob job = companion.getJob();
            y = drawLine(gfx, Component.literal(companion.getName().getString()).withStyle(style -> style.withUnderlined(true)), x, y, width);
            y += 4;
            y = drawLine(gfx, Component.translatable("gui.modern_companions.job.current", job.displayName()), x, y, width);
            y += 4;
            for (FormattedCharSequence seq : this.font.split(job.shortDescription(), width)) {
                gfx.drawString(this.font, seq, x, y, 0x2B2B2B, false);
                y += 10;
            }
            Component warn = toolWarning(companion, job).orElse(null);
            if (warn != null) {
                y += 6;
                for (FormattedCharSequence seq : this.font.split(warn, width)) {
                    gfx.drawString(this.font, seq, x, y, 0xB00000, false);
                    y += 10;
                }
            }

            if (job == CompanionJob.MINER) {
                y += 6;
                y = drawLine(gfx, Component.translatable("job.modern_companions.miner.stats.counted", companion.getMinerOresCounted()), x, y, width);
                y = drawLine(gfx, Component.translatable("job.modern_companions.miner.stats.mined", companion.getMinerOresMined()), x, y, width);
                int remaining = Math.max(0, companion.getMinerOresCounted() - companion.getMinerOresMined());
                y = drawLine(gfx, Component.translatable("job.modern_companions.miner.stats.remaining", remaining), x, y, width);
                y = drawLine(gfx, Component.translatable("job.modern_companions.miner.stats.lifetime", companion.getMinerOresLifetime()), x, y, width);
            } else if (job == CompanionJob.LUMBERJACK) {
                y += 6;
                y = drawLine(gfx, Component.translatable("job.modern_companions.lumberjack.stats.session", companion.getLumberLogsSession()), x, y, width);
                y = drawLine(gfx, Component.translatable("job.modern_companions.lumberjack.stats.lifetime", companion.getLumberLogsLifetime()), x, y, width);
            } else if (job == CompanionJob.FISHER) {
                y += 6;
                y = drawLine(gfx, Component.translatable("job.modern_companions.fisher.stats.session", companion.getFishCaughtSession()), x, y, width);
                y = drawLine(gfx, Component.translatable("job.modern_companions.fisher.stats.lifetime", companion.getFishCaughtLifetime()), x, y, width);
            } else if (job == CompanionJob.FARMER) {
                y += 6;
                y = drawLine(gfx, Component.translatable("job.modern_companions.farmer.stats.harvested", companion.getFarmerHarvestedSession()), x, y, width);
                y = drawLine(gfx, Component.translatable("job.modern_companions.farmer.stats.planted", companion.getFarmerPlantedSession()), x, y, width);
                y = drawLine(gfx, Component.translatable("job.modern_companions.farmer.stats.lifetime", companion.getFarmerHarvestedLifetime()), x, y, width);
            } else if (job == CompanionJob.HUNTER) {
                y += 6;
                y = drawLine(gfx, Component.translatable("job.modern_companions.hunter.stats.session", companion.getHunterKillsSession()), x, y, width);
                y = drawLine(gfx, Component.translatable("job.modern_companions.hunter.stats.lifetime", companion.getHunterKillsLifetime()), x, y, width);
            } else if (job == CompanionJob.CHEF) {
                y += 6;
                y = drawLine(gfx, Component.translatable("job.modern_companions.chef.stats.session", companion.getChefCookedSession()), x, y, width);
                y = drawLine(gfx, Component.translatable("job.modern_companions.chef.stats.lifetime", companion.getChefCookedLifetime()), x, y, width);
            }
        });

        super.render(gfx, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        // Suppress default blur; handled in render().
    }

    private Optional<AbstractHumanCompanionEntity> safeCompanion() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null) return Optional.empty();
        var e = mc.level.getEntity(companionId);
        if (e instanceof AbstractHumanCompanionEntity comp) {
            return Optional.of(comp);
        }
        return Optional.empty();
    }

    private int drawLine(GuiGraphics gfx, Component text, int x, int y, int width) {
        for (FormattedCharSequence seq : this.font.split(text, width)) {
            gfx.drawString(this.font, seq, x, y, 0x2B2B2B, false);
            y += 10;
        }
        return y;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private Optional<Component> toolWarning(AbstractHumanCompanionEntity companion, CompanionJob job) {
        boolean missing = !JobToolPolicy.has(companion, job);
        if (!missing) return Optional.empty();
        return Optional.of(Component.translatable("job.modern_companions.requires_tool"));
    }
}
