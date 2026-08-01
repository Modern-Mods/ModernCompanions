package com.majorbonghits.moderncompanions.core;

import com.majorbonghits.moderncompanions.ModernCompanions;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.EnumMap;
import java.util.Map;

/** Registry entries for actor-specific companion voice pools. */
public final class ModSounds {
    public enum Cue {
        GREETING, CONFIRMATION, REFUSAL, COMPLETION, FAREWELL, PAIN, DEATH,
        ENEMY_SPOTTED, LOW_HEALTH, UNDER_ATTACK, IDLE, CELEBRATION
    }

    private static final String[] ACTORS = { "alex", "ian", "sean", "karen", "meghan" };
    private static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, ModernCompanions.MOD_ID);
    private static final Map<String, Map<Cue, DeferredHolder<SoundEvent, SoundEvent>>> EVENTS = new java.util.HashMap<>();

    static {
        for (String actor : ACTORS) {
            EnumMap<Cue, DeferredHolder<SoundEvent, SoundEvent>> actorEvents = new EnumMap<>(Cue.class);
            for (Cue cue : Cue.values()) {
                String id = "companion_" + actor + "_" + cue.name().toLowerCase(java.util.Locale.ROOT);
                ResourceLocation key = ResourceLocation.fromNamespaceAndPath(ModernCompanions.MOD_ID, id);
                actorEvents.put(cue, SOUNDS.register(id, () -> SoundEvent.createVariableRangeEvent(key)));
            }
            EVENTS.put(actor, actorEvents);
        }
    }

    private ModSounds() {}

    public static void register(IEventBus bus) {
        SOUNDS.register(bus);
    }

    public static DeferredHolder<SoundEvent, SoundEvent> event(String actor, Cue cue) {
        Map<Cue, DeferredHolder<SoundEvent, SoundEvent>> actorEvents = EVENTS.get(actor);
        return actorEvents == null ? null : actorEvents.get(cue);
    }
}
