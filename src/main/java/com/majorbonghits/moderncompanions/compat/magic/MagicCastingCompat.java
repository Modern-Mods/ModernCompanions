package com.majorbonghits.moderncompanions.compat.magic;

import net.minecraft.world.InteractionHand;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

/** Calls loaded magic-mod APIs without linking Modern Companions to either optional jar. */
public final class MagicCastingCompat {
    public static final String IRONS = "irons_spellbooks";
    public static final String ARS = "ars_nouveau";
    private static final Set<String> IRON_SUMMONED_SWORD_TYPES = Set.of("summoned_sword", "summoned_claymore", "summoned_rapier");
    private static final Class<?> IRON_SPELL_CONTAINER = optionalClass("io.redspace.ironsspellbooks.api.spells.ISpellContainer");
    private static final Class<?> IRON_SCROLL = optionalClass("io.redspace.ironsspellbooks.api.item.IScroll");
    private static final Class<?> IRON_PRESET_CONTAINER = optionalClass("io.redspace.ironsspellbooks.api.spells.IPresetSpellContainer");
    private static final Class<?> ARS_CASTER_PROVIDER = optionalClass("com.hollingsworth.arsnouveau.api.spell.ItemCasterProvider");

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

    /** Add native magic attributes to the shared companion set when their mod is present. */
    public static void addMagicAttributes(AttributeSupplier.Builder builder) {
        if (ironsLoaded()) {
            addAttribute(builder, IRONS, "max_mana", 100.0D);
            addIronAttribute(builder, "mana_regen", "cooldown_reduction", "spell_power", "spell_resist",
                    "cast_time_reduction", "summon_damage", "casting_movespeed",
                    "fire_magic_resist", "ice_magic_resist", "lightning_magic_resist",
                    "holy_magic_resist", "ender_magic_resist", "blood_magic_resist",
                    "evocation_magic_resist", "nature_magic_resist", "eldritch_magic_resist",
                    "fire_spell_power", "ice_spell_power", "lightning_spell_power",
                    "holy_spell_power", "ender_spell_power", "blood_spell_power",
                    "evocation_spell_power", "nature_spell_power", "eldritch_spell_power");
        }
        if (arsLoaded()) {
            // Ars uses zero as the player attribute base. Companions already own a
            // 100-point mana pool, so a 100 base preserves ADD_VALUE and percentage gear.
            addAttribute(builder, ARS, "perk.max_mana", 100.0D);
            addAttribute(builder, ARS, "perk.mana_regen", 0.0D);
            addAttribute(builder, ARS, "perk.spell_damage", 0.0D);
            addAttribute(builder, ARS, "perk.warding", 0.0D);
        }
    }

    private static void addIronAttribute(AttributeSupplier.Builder builder, String... paths) {
        for (String path : paths) addAttribute(builder, IRONS, path, 1.0D);
    }

    private static void addAttribute(AttributeSupplier.Builder builder, String namespace, String path, double value) {
        BuiltInRegistries.ATTRIBUTE.getHolder(
                        net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ATTRIBUTE,
                                ResourceLocation.fromNamespaceAndPath(namespace, path)))
                .ifPresent(holder -> builder.add(holder, value));
    }

    /** Recognize native caster providers and stored spell containers, including addon subclasses. */
    public static boolean isMagicItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (ironsLoaded() && (hasIronSpellContainer(stack) || hasType(stack.getItem(), IRON_SCROLL)
                || hasType(stack.getItem(), IRON_PRESET_CONTAINER) || hasIronCasterClass(stack))) return true;
        return arsLoaded() && hasType(stack.getItem(), ARS_CASTER_PROVIDER);
    }

    private static boolean hasIronCasterClass(ItemStack stack) {
        for (Class<?> type = stack.getItem().getClass(); type != null; type = type.getSuperclass()) {
            String name = type.getName();
            if (name.equals("io.redspace.ironsspellbooks.item.CastingItem")
                    || name.equals("io.redspace.ironsspellbooks.item.SpellBook")
                    || name.equals("io.redspace.ironsspellbooks.item.Scroll")
                    || name.endsWith("HitherThitherWand")
                    || name.endsWith("Staff") || name.endsWith("StaffItem")
                    || name.endsWith("Wand") || name.endsWith("WandItem")) return true;
        }
        return false;
    }

    public static int maxMana(LivingEntity entity, int fallback) {
        int ironMax = (int) Math.round(attributeValue(entity, IRONS, "max_mana", fallback));
        int arsBonus = (int) Math.round(attributeValue(entity, ARS, "perk.max_mana", 100.0D) - 100.0D);
        return Math.max(1, Math.max(fallback, ironMax) + arsBonus);
    }

    public static int manaRegenInterval(LivingEntity entity, int fallback) {
        double iron = Math.max(0.01D, attributeValue(entity, IRONS, "mana_regen", 1.0D));
        double ars = Math.max(0.01D, 1.0D + attributeValue(entity, ARS, "perk.mana_regen", 0.0D));
        return Math.max(1, (int) Math.ceil(fallback / (iron * ars)));
    }

    public static int cooldownTicks(LivingEntity entity, int fallback) {
        // ponytail: mob casts finish immediately in the existing bridge; apply cast-time gear to cadence until a full cast lifecycle is needed.
        return adjustedInterval(entity, fallback, "cooldown_reduction", "cast_time_reduction");
    }

    public static double castingMovementSpeed(LivingEntity entity, double fallback) {
        return fallback * Math.max(1.0D, attributeValue(entity, IRONS, "casting_movespeed", 1.0D));
    }

    public static float spellPowerMultiplier(LivingEntity entity, String school) {
        double multiplier = Math.max(0.0D, attributeValue(entity, IRONS, "spell_power", 1.0D));
        if (school != null && !school.isBlank()) {
            multiplier *= Math.max(0.0D, attributeValue(entity, IRONS, school + "_spell_power", 1.0D));
        }
        return (float) multiplier;
    }

    /** Ars spell damage is additive, matching Ars' own damage resolver. */
    public static float arsSpellDamage(LivingEntity entity) {
        return (float) Math.max(0.0D, attributeValue(entity, ARS, "perk.spell_damage", 0.0D));
    }

    public static float reduceSpellDamage(LivingEntity entity, DamageSource source, float amount) {
        if (!isIronSpellDamage(source)) return amount;
        double resistance = Math.max(0.0D, attributeValue(entity, IRONS, "spell_resist", 1.0D) - 1.0D);
        String school = spellSchool(source);
        if (school != null) {
            resistance += Math.max(0.0D, attributeValue(entity, IRONS, school + "_magic_resist", 1.0D) - 1.0D);
        }
        return amount * (float) (1.0D - Math.min(0.95D, resistance));
    }

    private static int adjustedInterval(LivingEntity entity, int fallback, String... attributes) {
        double multiplier = 1.0D;
        for (String attribute : attributes) {
            multiplier *= Math.max(0.01D, attributeValue(entity, IRONS, attribute, 1.0D));
        }
        return Math.max(1, (int) Math.ceil(fallback / multiplier));
    }

    private static double attributeValue(LivingEntity entity, String namespace, String path, double fallback) {
        if ((IRONS.equals(namespace) && !ironsLoaded()) || (ARS.equals(namespace) && !arsLoaded())) return fallback;
        try {
            var key = net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ATTRIBUTE,
                    ResourceLocation.fromNamespaceAndPath(namespace, path));
            var holder = BuiltInRegistries.ATTRIBUTE.getHolder(key).orElse(null);
            AttributeInstance instance = holder == null ? null : entity.getAttribute(holder);
            return instance == null ? fallback : instance.getValue();
        } catch (RuntimeException ignored) {
            return fallback;
        }
    }

    public static boolean castHeldItem(LivingEntity caster, LivingEntity target) {
        ItemStack stack = caster.getMainHandItem();
        if (stack.isEmpty() || target == null || !target.isAlive()) return false;
        if (ironsLoaded() && castIronItem(caster, stack)) return true;
        return arsLoaded() && castArsItem(caster, target, stack);
    }

    private static boolean castIronItem(LivingEntity caster, ItemStack stack) {
        try {
            if (!hasIronSpellContainer(stack)) return false;
            Object container = IRON_SPELL_CONTAINER.getMethod("get", ItemStack.class).invoke(null, stack);
            if (container == null) return false;
            List<?> spells = (List<?>) call(container, "getActiveSpells");
            if (spells == null || spells.isEmpty()) return false;
            Object slot = spells.get(0);
            Object spell = call(slot, "getSpell");
            int level = (Integer) call(slot, "getLevel");
            Object data = Class.forName("io.redspace.ironsspellbooks.api.magic.MagicData")
                    .getMethod("getPlayerMagicData", LivingEntity.class).invoke(null, caster);
            if (!(Boolean) call(spell, "checkPreCastConditions", caster.level(), level, caster, data)) return false;
            Class<?> sourceType = Class.forName("io.redspace.ironsspellbooks.api.spells.CastSource");
            @SuppressWarnings({"unchecked", "rawtypes"}) Object mob = Enum.valueOf((Class) sourceType, "MOB");
            call(spell, "onCast", caster.level(), level, caster, mob, data);
            call(spell, "onServerCastComplete", caster.level(), level, caster, data, false);
            if (hasType(stack.getItem(), IRON_SCROLL)) stack.shrink(1);
            return true;
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
            return false;
        }
    }

    private static boolean castArsItem(LivingEntity caster, LivingEntity target, ItemStack stack) {
        try {
            if (!hasType(stack.getItem(), ARS_CASTER_PROVIDER)) return false;
            Object casterTool = call(stack.getItem(), "getSpellCaster", stack);
            if (casterTool == null) return false;
            Object spell = call(casterTool, "getSpell");
            if (Boolean.TRUE.equals(call(spell, "isEmpty"))) return false;
            spell = call(casterTool, "modifySpellBeforeCasting", caster.level(), caster, InteractionHand.MAIN_HAND, spell);
            Object wrappedCaster = Class.forName("com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster")
                    .getConstructor(LivingEntity.class).newInstance(caster);
            Object context = newInstance(Class.forName("com.hollingsworth.arsnouveau.api.spell.SpellContext"),
                    caster.level(), spell, caster, wrappedCaster, stack);
            Object resolver = newInstance(Class.forName("com.hollingsworth.arsnouveau.api.spell.EntitySpellResolver"), context);
            Object result = call(resolver, "onCastOnEntity", stack, target, InteractionHand.MAIN_HAND);
            if (result instanceof Boolean success && !success) return false;
            if (isArsScroll(stack)) stack.shrink(1);
            return true;
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
            return false;
        }
    }

    private static boolean hasIronSpellContainer(ItemStack stack) {
        if (IRON_SPELL_CONTAINER == null) return false;
        try {
            boolean present = (Boolean) IRON_SPELL_CONTAINER.getMethod("isSpellContainer", ItemStack.class).invoke(null, stack);
            if (!present && hasType(stack.getItem(), IRON_PRESET_CONTAINER)) {
                call(stack.getItem(), "initializeSpellContainer", stack);
                present = (Boolean) IRON_SPELL_CONTAINER.getMethod("isSpellContainer", ItemStack.class).invoke(null, stack);
            }
            return present;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return false;
        }
    }

    private static boolean isArsScroll(ItemStack stack) {
        String name = stack.getItem().getClass().getName();
        return name.endsWith("SpellParchment") || name.endsWith("SpellScroll");
    }

    private static boolean hasType(Object value, Class<?> type) {
        return type != null && type.isInstance(value);
    }

    private static Class<?> optionalClass(String name) {
        try {
            return Class.forName(name, false, MagicCastingCompat.class.getClassLoader());
        } catch (ClassNotFoundException | LinkageError ignored) {
            return null;
        }
    }

    private static boolean isIronSpellDamage(DamageSource source) {
        return source.typeHolder().unwrapKey()
                .map(key -> IRONS.equals(key.location().getNamespace()))
                .orElse(false);
    }

    private static String spellSchool(DamageSource source) {
        return source.typeHolder().unwrapKey()
                .map(key -> {
                    String path = key.location().getPath();
                    for (String school : new String[]{"fire", "ice", "lightning", "holy", "ender", "blood", "evocation", "nature", "eldritch"}) {
                        if (path.startsWith(school + "_")) return school;
                    }
                    return null;
                })
                .orElse(null);
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
