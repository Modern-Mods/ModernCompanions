package com.majorbonghits.moderncompanions.compat.magic;

import net.minecraft.world.InteractionHand;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

/** Calls loaded magic-mod APIs without linking Modern Companions to either optional jar. */
public final class MagicCastingCompat {
    public static final String IRONS = "irons_spellbooks";
    public static final String ARS = "ars_nouveau";
    private static final Set<String> IRON_SUMMONED_SWORD_TYPES = Set.of("summoned_sword", "summoned_claymore", "summoned_rapier");

    private MagicCastingCompat() {}

    public static boolean available() {
        return ironsLoaded() || arsLoaded();
    }

    public static boolean ironsLoaded() {
        return ModList.get().isLoaded(IRONS);
    }

    public static boolean arsLoaded() {
        return ModList.get().isLoaded(ARS);
    }

    /** Iron's treats mob recasts as no-ops, so track its live three-weapon batch directly. */
    public static boolean hasIronSummonedSwords(LivingEntity caster) {
        if (!ironsLoaded() || !(caster.level() instanceof ServerLevel level)) return false;
        for (Entity entity : level.getEntities().getAll()) {
            if (isSummonedSword(entity) && summonerOf(entity) == caster) return true;
        }
        return false;
    }

    /** Try both real APIs when both mods are present; never substitute a vanilla imitation. */
    public static boolean cast(LivingEntity caster, LivingEntity target, String ironSpell, String... arsParts) {
        if (ironsLoaded() && (!arsLoaded() || caster.getRandom().nextBoolean()) && castIron(caster, ironSpell)) return true;
        if (arsLoaded() && castArs(caster, target, arsParts)) return true;
        return ironsLoaded() && castIron(caster, ironSpell);
    }

    private static boolean castIron(LivingEntity caster, String spellId) {
        try {
            ClassLoader loader = MagicCastingCompat.class.getClassLoader();
            Class<?> registry = Class.forName("io.redspace.ironsspellbooks.api.registry.SpellRegistry", true, loader);
            Object spell = registry.getMethod("getSpell", String.class).invoke(null, "irons_spellbooks:" + spellId);
            Class<?> magicDataType = Class.forName("io.redspace.ironsspellbooks.api.magic.MagicData", true, loader);
            Object data = magicDataType.getMethod("getPlayerMagicData", LivingEntity.class).invoke(null, caster);
            Class<?> castSource = Class.forName("io.redspace.ironsspellbooks.api.spells.CastSource", true, loader);
            @SuppressWarnings({"unchecked", "rawtypes"}) Object mob = Enum.valueOf((Class) castSource, "MOB");
            Level level = caster.level();
            if (!(boolean) call(spell, "checkPreCastConditions", level, 1, caster, data)) return false;
            call(spell, "onCast", level, 1, caster, mob, data);
            call(spell, "onServerCastComplete", level, 1, caster, data, false);
            return true;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return false;
        }
    }

    private static boolean castArs(LivingEntity caster, LivingEntity target, String... parts) {
        try {
            ClassLoader loader = MagicCastingCompat.class.getClassLoader();
            Object spell = Class.forName("com.hollingsworth.arsnouveau.api.spell.Spell", true, loader).getConstructor().newInstance();
            for (String part : parts) spell = call(spell, "add", arsPart(loader, part));
            Object wrappedCaster = Class.forName("com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster", true, loader)
                    .getConstructor(LivingEntity.class).newInstance(caster);
            Object context = newInstance(Class.forName("com.hollingsworth.arsnouveau.api.spell.SpellContext", true, loader), caster.level(), spell, caster, wrappedCaster);
            Object resolver = newInstance(Class.forName("com.hollingsworth.arsnouveau.api.spell.EntitySpellResolver", true, loader), context);
            return (boolean) call(resolver, "onCastOnEntity", ItemStack.EMPTY, target, InteractionHand.MAIN_HAND);
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return false;
        }
    }

    private static boolean isSummonedSword(Entity entity) {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return id != null && IRONS.equals(id.getNamespace()) && IRON_SUMMONED_SWORD_TYPES.contains(id.getPath());
    }

    private static Entity summonerOf(Entity entity) {
        try {
            Object summoner = call(entity, "getSummoner");
            return summoner instanceof Entity result ? result : null;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static Object arsPart(ClassLoader loader, String simpleName) throws ReflectiveOperationException {
        for (String group : new String[]{"method", "effect", "augment"}) {
            try {
                Class<?> type = Class.forName("com.hollingsworth.arsnouveau.common.spell." + group + "." + simpleName, true, loader);
                Field field = type.getField("INSTANCE");
                return field.get(null);
            } catch (ClassNotFoundException ignored) {
                // Next Ars spell-part group.
            }
        }
        throw new ClassNotFoundException(simpleName);
    }

    private static Object newInstance(Class<?> type, Object... args) throws ReflectiveOperationException {
        for (Constructor<?> constructor : type.getConstructors()) {
            if (matches(constructor.getParameterTypes(), args)) return constructor.newInstance(args);
        }
        throw new NoSuchMethodException(type.getName());
    }

    private static Object call(Object target, String name, Object... args) throws ReflectiveOperationException {
        for (Method method : target.getClass().getMethods()) {
            if (method.getName().equals(name) && matches(method.getParameterTypes(), args)) return method.invoke(target, args);
        }
        throw new NoSuchMethodException(target.getClass().getName() + "." + name);
    }

    private static boolean matches(Class<?>[] types, Object[] args) {
        if (types.length != args.length) return false;
        for (int i = 0; i < types.length; i++) {
            Class<?> type = types[i].isPrimitive() ? boxed(types[i]) : types[i];
            if (args[i] == null ? types[i].isPrimitive() : !type.isInstance(args[i])) return false;
        }
        return true;
    }

    private static Class<?> boxed(Class<?> type) {
        return type == int.class ? Integer.class : type == boolean.class ? Boolean.class : type;
    }
}
