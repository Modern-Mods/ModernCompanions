package com.majorbonghits.moderncompanions.core;

import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.TranslatableEnum;

import java.util.Locale;

/** Controls which companion voice cues are audible. */
public enum CompanionVoiceMode implements TranslatableEnum {
    FULL,
    LIMITED,
    OFF;

    public boolean allows(ModSounds.Cue cue) {
        return switch (this) {
            case FULL -> true;
            case LIMITED -> cue == ModSounds.Cue.PAIN
                    || cue == ModSounds.Cue.DEATH
                    || cue == ModSounds.Cue.IDLE;
            case OFF -> false;
        };
    }

    @Override
    public Component getTranslatedName() {
        return Component.translatable("modern_companions.configuration.companion.voice_mode."
                + name().toLowerCase(Locale.ROOT));
    }
}
