package com.majorbonghits.moderncompanions.entity;

import com.majorbonghits.moderncompanions.core.CompanionVoiceMode;
import com.majorbonghits.moderncompanions.core.ModSounds;

/** Pure regression check for the gender-to-actor contract. */
public final class CompanionVoiceTest {
    public static void main(String[] args) {
        assert CompanionVoice.isValidActor("alex", 0);
        assert CompanionVoice.isValidActor("ian", 0);
        assert CompanionVoice.isValidActor("sean", 0);
        assert CompanionVoice.isValidActor("karen", 1);
        assert CompanionVoice.isValidActor("meghan", 1);
        assert !CompanionVoice.isValidActor("karen", 0);
        assert !CompanionVoice.isValidActor("alex", 1);
        assert !CompanionVoice.isValidActor("unknown", 0);

        assert CompanionVoiceMode.FULL.allows(ModSounds.Cue.GREETING);
        assert CompanionVoiceMode.FULL.allows(ModSounds.Cue.CELEBRATION);
        assert CompanionVoiceMode.LIMITED.allows(ModSounds.Cue.PAIN);
        assert CompanionVoiceMode.LIMITED.allows(ModSounds.Cue.DEATH);
        assert CompanionVoiceMode.LIMITED.allows(ModSounds.Cue.IDLE);
        assert !CompanionVoiceMode.LIMITED.allows(ModSounds.Cue.GREETING);
        assert !CompanionVoiceMode.LIMITED.allows(ModSounds.Cue.CONFIRMATION);
        assert !CompanionVoiceMode.OFF.allows(ModSounds.Cue.PAIN);
    }
}
