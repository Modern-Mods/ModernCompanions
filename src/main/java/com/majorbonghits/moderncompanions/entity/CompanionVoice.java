package com.majorbonghits.moderncompanions.entity;

import com.majorbonghits.moderncompanions.core.ModSounds;
import com.majorbonghits.moderncompanions.core.CompanionVoiceMode;
import com.majorbonghits.moderncompanions.core.ModConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;

import java.util.Locale;

/** Chooses one gender-compatible actor and keeps all playback on that actor's pools. */
public final class CompanionVoice {
    private static final String[] MALE = { "alex", "ian", "sean" };
    private static final String[] FEMALE = { "karen", "meghan" };
    private static final int ENEMY_SPOTTED_COOLDOWN_TICKS = 200; // 10 seconds at 20 TPS

    private CompanionVoice() {}

    public static void ensureActor(AbstractHumanCompanionEntity companion) {
        if (!isValidActor(companion.getVoiceActor(), companion.getSex())) {
            String[] pool = companion.getSex() == 1 ? FEMALE : MALE;
            companion.setVoiceActor(pool[companion.getRandom().nextInt(pool.length)]);
        }
    }

    public static boolean isValidActor(String actor, int sex) {
        if (actor == null) return false;
        String normalized = actor.toLowerCase(Locale.ROOT);
        String[] pool = sex == 1 ? FEMALE : MALE;
        for (String candidate : pool) {
            if (candidate.equals(normalized)) return true;
        }
        return false;
    }

    public static void play(AbstractHumanCompanionEntity companion, ModSounds.Cue cue) {
        if (companion.level().isClientSide()) return;
        CompanionVoiceMode mode = ModConfig.COMPANION_VOICE_MODE == null
                ? CompanionVoiceMode.FULL : ModConfig.safeGet(ModConfig.COMPANION_VOICE_MODE);
        if (!mode.allows(cue)) return;
        ensureActor(companion);
        int now = companion.tickCount;
        if (now < companion.voiceLockUntilTick()) return;
        int cooldown = cooldown(cue);
        Integer last = companion.voiceCooldowns().get(cue);
        if (last != null && now - last < cooldown) return;

        var event = ModSounds.event(companion.getVoiceActor(), cue);
        if (event == null) return;
        companion.voiceCooldowns().put(cue, now);
        companion.setVoiceLockUntilTick(now + Math.max(1, cooldown));
        int volumePercent = ModConfig.COMPANION_VOICE_VOLUME == null
                ? 80 : Math.max(0, Math.min(100, ModConfig.safeGet(ModConfig.COMPANION_VOICE_VOLUME)));
        companion.level().playSound(null, companion.getX(), companion.getY(), companion.getZ(), event.value(),
                SoundSource.VOICE, volumePercent / 100.0F,
                0.95F + companion.getRandom().nextFloat() * 0.1F);
    }

    /** Lets the closest owned, loaded companion announce a shared enemy target. */
    public static void playEnemySpotted(AbstractHumanCompanionEntity companion, LivingEntity target) {
        var ownerId = companion.getOwnerUUID();
        if (ownerId != null && companion.level() instanceof ServerLevel serverLevel) {
            // ponytail: full loaded-level scan keeps "present" exact; use an owner registry if this becomes frequent.
            AbstractHumanCompanionEntity closest = null;
            double closestDistance = Double.MAX_VALUE;
            for (var entity : serverLevel.getEntities().getAll()) {
                if (entity instanceof AbstractHumanCompanionEntity other
                        && other.isAlive()
                        && other.isTame()
                        && ownerId.equals(other.getOwnerUUID())) {
                    if (other != companion && other.getTarget() == target) return;
                    if (companion.getOwner() != null) {
                        double distance = companion.getOwner().distanceToSqr(other);
                        if (closest == null || distance < closestDistance
                                || (distance == closestDistance && other.getId() < closest.getId())) {
                            closest = other;
                            closestDistance = distance;
                        }
                    }
                }
            }
            if (closest != null) {
                play(closest, ModSounds.Cue.ENEMY_SPOTTED);
                return;
            }
        }
        play(companion, ModSounds.Cue.ENEMY_SPOTTED);
    }

    private static int cooldown(ModSounds.Cue cue) {
        return switch (cue) {
            case PAIN -> 12;
            case DEATH -> 0;
            case IDLE -> 240;
            case GREETING, CONFIRMATION, REFUSAL -> 35;
            case ENEMY_SPOTTED -> ENEMY_SPOTTED_COOLDOWN_TICKS;
            default -> 20;
        };
    }
}
