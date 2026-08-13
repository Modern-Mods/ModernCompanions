package com.majorbonghits.moderncompanions.client.armor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

/** Supplies the imported 128x128 Medieval Armory model for each armor slot. */
public final class MedievalArmorClientExtensions implements IClientItemExtensions {
    public static final MedievalArmorClientExtensions INSTANCE = new MedievalArmorClientExtensions();

    private MedievalArmorClientExtensions() {
    }

    @Override
    public HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack stack,
                                                   EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        MedievalArmorModel.Geometry geometry = MedievalArmorModel.geometryForItemId(itemId.getPath());
        if (geometry == null || !geometry.matches(equipmentSlot)) {
            return original;
        }
        return MedievalArmorModel.create(Minecraft.getInstance().getEntityModels(), geometry);
    }
}
