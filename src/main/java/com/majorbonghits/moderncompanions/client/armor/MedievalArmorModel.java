package com.majorbonghits.moderncompanions.client.armor;

import com.majorbonghits.moderncompanions.Constants;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Bakes the 128x128 Medieval Armory meshes into the standard humanoid part names
 * used by vanilla, companions, and Epic Fight's wearable-layer bridge.
 */
public final class MedievalArmorModel {
    private enum ArmorPart {
        HEAD, CHEST, LEGS, BOOTS
    }

    public enum Geometry {
        COPPER_SCALE_CHESTPLATE("copper_scale", ArmorPart.CHEST, "copper_scale_chestplate", MedievalArmorModel::createCopperScaleArmorChestplate),
        COPPER_SCALE_HELMET("copper_scale", ArmorPart.HEAD, "copper_scale_helmet", MedievalArmorModel::createCopperScaleArmorHelmet),
        COPPER_SCALE_LEGGINGS("copper_scale", ArmorPart.LEGS, "copper_scale_leggings", MedievalArmorModel::createCopperScaleArmorLeggings),
        DIAMOND_WEAVE_BOOTS("diamond_weave", ArmorPart.BOOTS, "diamond_weave_boots", MedievalArmorModel::createDiamondWeaveBoots),
        DIAMOND_WEAVE_CHESTPLATE("diamond_weave", ArmorPart.CHEST, "diamond_weave_chestplate", MedievalArmorModel::createDiamondWeaveChestplate),
        DIAMOND_WEAVE_HELMET("diamond_weave", ArmorPart.HEAD, "diamond_weave_helmet", MedievalArmorModel::createDiamondWeaveHelmet),
        DIAMOND_WEAVE_LEGGINGS("diamond_weave", ArmorPart.LEGS, "diamond_weave_leggings", MedievalArmorModel::createDiamondWeaveLeggings),
        GAMBISON_BOOTS("gambison", ArmorPart.BOOTS, "gambison_boots", MedievalArmorModel::createGambisonArmorBoots),
        GAMBISON_CHESTPLATE("gambison", ArmorPart.CHEST, "gambison_chestplate", MedievalArmorModel::createGambisonArmorChestplate),
        GAMBISON_HELMET("gambison", ArmorPart.HEAD, "gambison_helmet", MedievalArmorModel::createGambisonArmorHelmet),
        GAMBISON_LEGGINGS("gambison", ArmorPart.LEGS, "gambison_leggings", MedievalArmorModel::createGambisonArmorLeggings),
        HEAVY_DIAMOND_BOOTS("heavy_diamond", ArmorPart.BOOTS, "heavy_diamond_boots", MedievalArmorModel::createHeavyDiamondArmorBoots),
        HEAVY_DIAMOND_CHESTPLATE("heavy_diamond", ArmorPart.CHEST, "heavy_diamond_chestplate", MedievalArmorModel::createHeavyDiamondArmorChestplate),
        HEAVY_DIAMOND_HELMET("heavy_diamond", ArmorPart.HEAD, "heavy_diamond_helmet", MedievalArmorModel::createHeavyDiamondArmorHelmet),
        HEAVY_DIAMOND_LEGGINGS("heavy_diamond", ArmorPart.LEGS, "heavy_diamond_leggings", MedievalArmorModel::createHeavyDiamondArmorLeggings),
        HEAVY_IRON_BOOTS("heavy_iron_armor", ArmorPart.BOOTS, "heavy_iron_armor_boots", MedievalArmorModel::createHeavyIronArmorBoots),
        HEAVY_IRON_CHESTPLATE("heavy_iron_armor", ArmorPart.CHEST, "heavy_iron_armor_chestplate", MedievalArmorModel::createHeavyIronArmorChestplate),
        HEAVY_IRON_HELMET("heavy_iron_armor", ArmorPart.HEAD, "heavy_iron_armor_helmet", MedievalArmorModel::createHeavyIronArmorHelmet),
        HEAVY_IRON_LEGGINGS("heavy_iron_armor", ArmorPart.LEGS, "heavy_iron_armor_leggings", MedievalArmorModel::createHeavyIronArmorLeggings),
        HEAVY_NETHERITE_HELMET("heavy_netherite", ArmorPart.HEAD, "heavy_netherite_helmet", MedievalArmorModel::createHeavyNetheriteArmorHelmet),
        LIGHT_DIAMOND_BOOTS("light_diamond", ArmorPart.BOOTS, "light_diamond_boots", MedievalArmorModel::createLightDiamondArmorBoots),
        LIGHT_DIAMOND_CHESTPLATE("light_diamond", ArmorPart.CHEST, "light_diamond_chestplate", MedievalArmorModel::createLightDiamondArmorChestplate),
        LIGHT_DIAMOND_HELMET("light_diamond", ArmorPart.HEAD, "light_diamond_helmet", MedievalArmorModel::createLightDiamondArmorHelmet),
        LIGHT_DIAMOND_LEGGINGS("light_diamond", ArmorPart.LEGS, "light_diamond_leggings", MedievalArmorModel::createLightDiamondArmorLeggings),
        LIGHT_IRON_BOOTS("light_iron_armor", ArmorPart.BOOTS, "light_iron_armor_boots", MedievalArmorModel::createLightIronArmorBoots),
        LIGHT_IRON_CHESTPLATE("light_iron_armor", ArmorPart.CHEST, "light_iron_armor_chestplate", MedievalArmorModel::createLightIronArmorChestplate),
        LIGHT_IRON_HELMET("light_iron_armor", ArmorPart.HEAD, "light_iron_armor_helmet", MedievalArmorModel::createLightIronArmorHelmet),
        LIGHT_IRON_LEGGINGS("light_iron_armor", ArmorPart.LEGS, "light_iron_armor_leggings", MedievalArmorModel::createLightIronArmorLeggings),
        MAIL_IRON_BOOTS("iron_mail", ArmorPart.BOOTS, "iron_mail_boots", MedievalArmorModel::createMailIronArmorBoots),
        MAIL_IRON_CHESTPLATE("iron_mail", ArmorPart.CHEST, "iron_mail_chestplate", MedievalArmorModel::createMailIronArmorChestplate),
        MAIL_IRON_HELMET("iron_mail", ArmorPart.HEAD, "iron_mail_helmet", MedievalArmorModel::createMailIronArmorHelmet),
        MAIL_IRON_LEGGINGS("iron_mail", ArmorPart.LEGS, "iron_mail_leggings", MedievalArmorModel::createMailIronArmorLeggings),
        MEDIUM_DIAMOND_BOOTS("medium_diamond", ArmorPart.BOOTS, "medium_diamond_boots", MedievalArmorModel::createMediumDiamondArmorBoots),
        MEDIUM_DIAMOND_CHESTPLATE("medium_diamond", ArmorPart.CHEST, "medium_diamond_chestplate", MedievalArmorModel::createMediumDiamondArmorChestplate),
        MEDIUM_DIAMOND_HELMET("medium_diamond", ArmorPart.HEAD, "medium_diamond_helmet", MedievalArmorModel::createMediumDiamondArmorHelmet),
        MEDIUM_DIAMOND_LEGGINGS("medium_diamond", ArmorPart.LEGS, "medium_diamond_leggings", MedievalArmorModel::createMediumDiamondArmorLeggings),
        MEDIUM_IRON_BOOTS("medium_iron_armor", ArmorPart.BOOTS, "medium_iron_armor_boots", MedievalArmorModel::createMediumIronArmorBoots),
        MEDIUM_IRON_CHESTPLATE("medium_iron_armor", ArmorPart.CHEST, "medium_iron_armor_chestplate", MedievalArmorModel::createMediumIronArmorChestplate),
        MEDIUM_IRON_HELMET("medium_iron_armor", ArmorPart.HEAD, "medium_iron_armor_helmet", MedievalArmorModel::createMediumIronArmorHelmet),
        MEDIUM_IRON_LEGGINGS("medium_iron_armor", ArmorPart.LEGS, "medium_iron_armor_leggings", MedievalArmorModel::createMediumIronArmorLeggings),
        ;

        private final String family;
        private final ArmorPart part;
        private final ModelLayerLocation layerLocation;
        private final Supplier<LayerDefinition> definition;

        Geometry(String family, ArmorPart part, String layer, Supplier<LayerDefinition> definition) {
            this.family = family;
            this.part = part;
            this.layerLocation = new ModelLayerLocation(Constants.id("medieval_armor/" + layer), "main");
            this.definition = definition;
        }

        public boolean matches(EquipmentSlot slot) {
            return switch (part) {
                case HEAD -> slot == EquipmentSlot.HEAD;
                case CHEST -> slot == EquipmentSlot.CHEST;
                case LEGS -> slot == EquipmentSlot.LEGS;
                case BOOTS -> slot == EquipmentSlot.FEET;
            };
        }

        private static Geometry forFamily(String family, ArmorPart part) {
            for (Geometry geometry : values()) {
                if (geometry.family.equals(family) && geometry.part == part) {
                    return geometry;
                }
            }
            return null;
        }
    }

    private MedievalArmorModel() {
    }

    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        for (Geometry geometry : Geometry.values()) {
            event.registerLayerDefinition(geometry.layerLocation, geometry.definition);
        }
    }

    public static Geometry geometryForItemId(String itemId) {
        ArmorPart part;
        String family;
        if (itemId.endsWith("_helmet")) {
            part = ArmorPart.HEAD;
            family = itemId.substring(0, itemId.length() - "_helmet".length());
        } else if (itemId.endsWith("_chestplate")) {
            part = ArmorPart.CHEST;
            family = itemId.substring(0, itemId.length() - "_chestplate".length());
        } else if (itemId.endsWith("_leggings")) {
            part = ArmorPart.LEGS;
            family = itemId.substring(0, itemId.length() - "_leggings".length());
        } else if (itemId.endsWith("_boots")) {
            part = ArmorPart.BOOTS;
            family = itemId.substring(0, itemId.length() - "_boots".length());
        } else {
            return null;
        }

        family = switch (family) {
            case "iron_mail_white_cloth" -> "iron_mail";
            case "light_netherite" -> "light_diamond";
            case "medium_netherite" -> "medium_diamond";
            case "heavy_netherite" -> part == ArmorPart.HEAD ? "heavy_netherite" : "heavy_diamond";
            default -> family;
        };
        return Geometry.forFamily(family, part);
    }

    public static HumanoidModel<LivingEntity> create(EntityModelSet modelSet, Geometry geometry) {
        ModelPart root = modelSet.bakeLayer(geometry.layerLocation);
        Map<String, ModelPart> parts = new HashMap<>();
        parts.put("head", emptyPart());
        parts.put("hat", emptyPart());
        parts.put("body", emptyPart());
        parts.put("right_arm", emptyPart());
        parts.put("left_arm", emptyPart());
        parts.put("right_leg", emptyPart());
        parts.put("left_leg", emptyPart());
        switch (geometry.part) {
            case HEAD -> parts.put("head", root.getChild("bb_main"));
            case CHEST -> {
                parts.put("body", root.getChild("Torso"));
                parts.put("left_arm", root.getChild("ArmsL"));
                parts.put("right_arm", root.getChild("ArmsR"));
            }
            case LEGS -> {
                parts.put("body", root.getChild("body"));
                parts.put("left_leg", root.getChild("LeggingsL"));
                parts.put("right_leg", root.getChild("LeggingsR"));
            }
            case BOOTS -> {
                parts.put("left_leg", root.getChild("BootsL"));
                parts.put("right_leg", root.getChild("BootsR"));
            }
        }
        return new HumanoidModel<>(new ModelPart(List.of(), parts));
    }

    private static ModelPart emptyPart() {
        return new ModelPart(List.of(), Map.of());
    }

public static LayerDefinition createCopperScaleArmorChestplate() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition ArmsL = partdefinition.addOrReplaceChild(
         "ArmsL",
         CubeListBuilder.create()
            .texOffs(83, 0)
            .addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F))
            .texOffs(83, 17)
            .addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.75F)),
         PartPose.offset(5.0F, 14.0F, 0.0F)
      );
      PartDefinition ArmsR = partdefinition.addOrReplaceChild(
         "ArmsR",
         CubeListBuilder.create()
            .texOffs(75, 34)
            .addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F))
            .texOffs(76, 51)
            .addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.75F)),
         PartPose.offset(-5.0F, 14.0F, 0.0F)
      );
      PartDefinition Torso = partdefinition.addOrReplaceChild(
         "Torso",
         CubeListBuilder.create()
            .texOffs(50, 34)
            .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.51F))
            .texOffs(0, 51)
            .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.75F)),
         PartPose.offset(0.0F, 12.0F, 0.0F)
      );
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

public static LayerDefinition createCopperScaleArmorHelmet() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition bb_main = partdefinition.addOrReplaceChild(
         "bb_main",
         CubeListBuilder.create()
            .texOffs(33, 0)
            .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.75F))
            .texOffs(33, 17)
            .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(1.0F))
            .texOffs(32, 93)
            .addBox(0.5F, -9.25F, -5.0F, -1.0F, 2.0F, 10.0F, new CubeDeformation(0.5F)),
         PartPose.offset(0.0F, 24.0F, 0.0F)
      );
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

public static LayerDefinition createCopperScaleArmorLeggings() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition LeggingsR = partdefinition.addOrReplaceChild(
         "LeggingsR",
         CubeListBuilder.create()
            .texOffs(51, 68)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F))
            .texOffs(85, 67)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.7F)),
         PartPose.offset(-2.0F, 12.0F, 0.0F)
      );
      PartDefinition LeggingsL = partdefinition.addOrReplaceChild(
         "LeggingsL",
         CubeListBuilder.create()
            .texOffs(68, 68)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F))
            .texOffs(85, 75)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.7F)),
         PartPose.offset(2.0F, 12.0F, 0.0F)
      );
      PartDefinition body = partdefinition.addOrReplaceChild(
         "body",
         CubeListBuilder.create().texOffs(78, 85).addBox(-4.0F, 9.1F, -2.0F, 8.0F, 2.0F, 4.0F, new CubeDeformation(0.48F)),
         PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

public static LayerDefinition createDiamondWeaveBoots() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition BootsR = partdefinition.addOrReplaceChild(
         "BootsR",
         CubeListBuilder.create().texOffs(61, 85).addBox(-2.0F, 7.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(1.0F)),
         PartPose.offset(-2.0F, 12.0F, 0.0F)
      );
      PartDefinition BootsL = partdefinition.addOrReplaceChild(
         "BootsL",
         CubeListBuilder.create().texOffs(44, 85).addBox(-2.0F, 7.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(1.0F)),
         PartPose.offset(2.0F, 12.0F, 0.0F)
      );
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

public static LayerDefinition createDiamondWeaveChestplate() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition ArmsL = partdefinition.addOrReplaceChild(
         "ArmsL",
         CubeListBuilder.create()
            .texOffs(83, 0)
            .addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F))
            .texOffs(83, 17)
            .addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.74F)),
         PartPose.offset(5.0F, 14.0F, 0.0F)
      );
      PartDefinition ArmsR = partdefinition.addOrReplaceChild(
         "ArmsR",
         CubeListBuilder.create()
            .texOffs(75, 34)
            .addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F))
            .texOffs(76, 50)
            .addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.74F)),
         PartPose.offset(-5.0F, 14.0F, 0.0F)
      );
      PartDefinition Torso = partdefinition.addOrReplaceChild(
         "Torso",
         CubeListBuilder.create()
            .texOffs(50, 34)
            .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.51F))
            .texOffs(0, 51)
            .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.75F))
            .texOffs(27, 85)
            .addBox(-0.5F, 8.55F, -1.55F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.5F)),
         PartPose.offset(0.0F, 12.0F, 0.0F)
      );
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

public static LayerDefinition createDiamondWeaveHelmet() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition bb_main = partdefinition.addOrReplaceChild(
         "bb_main",
         CubeListBuilder.create()
            .texOffs(33, 0)
            .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.75F))
            .texOffs(33, 17)
            .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(1.0F))
            .texOffs(32, 93)
            .addBox(0.5F, -9.25F, -5.0F, -1.0F, 2.0F, 10.0F, new CubeDeformation(0.5F)),
         PartPose.offset(0.0F, 24.0F, 0.0F)
      );
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

public static LayerDefinition createDiamondWeaveLeggings() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition LeggingsR = partdefinition.addOrReplaceChild(
         "LeggingsR",
         CubeListBuilder.create()
            .texOffs(51, 68)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F))
            .texOffs(85, 66)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.75F)),
         PartPose.offset(-2.0F, 12.0F, 0.0F)
      );
      PartDefinition LeggingsL = partdefinition.addOrReplaceChild(
         "LeggingsL",
         CubeListBuilder.create()
            .texOffs(68, 68)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F))
            .texOffs(84, 75)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.75F)),
         PartPose.offset(2.0F, 12.0F, 0.0F)
      );
      PartDefinition body = partdefinition.addOrReplaceChild(
         "body",
         CubeListBuilder.create().texOffs(78, 85).addBox(-4.0F, 9.1F, -2.0F, 8.0F, 2.0F, 4.0F, new CubeDeformation(0.48F)),
         PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

public static LayerDefinition createGambisonArmorBoots() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition BootsR = partdefinition.addOrReplaceChild(
         "BootsR",
         CubeListBuilder.create().texOffs(61, 85).addBox(-2.0F, 7.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(1.0F)),
         PartPose.offset(-2.0F, 12.0F, 0.0F)
      );
      PartDefinition BootsL = partdefinition.addOrReplaceChild(
         "BootsL",
         CubeListBuilder.create().texOffs(44, 85).addBox(-2.0F, 7.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(1.0F)),
         PartPose.offset(2.0F, 12.0F, 0.0F)
      );
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

public static LayerDefinition createGambisonArmorChestplate() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition ArmsL = partdefinition.addOrReplaceChild(
         "ArmsL",
         CubeListBuilder.create()
            .texOffs(83, 0)
            .addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F))
            .texOffs(83, 17)
            .addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.75F)),
         PartPose.offset(5.0F, 14.0F, 0.0F)
      );
      PartDefinition ArmsR = partdefinition.addOrReplaceChild(
         "ArmsR",
         CubeListBuilder.create()
            .texOffs(75, 34)
            .addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F))
            .texOffs(76, 51)
            .addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.75F)),
         PartPose.offset(-5.0F, 14.0F, 0.0F)
      );
      PartDefinition Torso = partdefinition.addOrReplaceChild(
         "Torso",
         CubeListBuilder.create()
            .texOffs(50, 34)
            .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.51F))
            .texOffs(0, 51)
            .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.75F)),
         PartPose.offset(0.0F, 12.0F, 0.0F)
      );
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

public static LayerDefinition createGambisonArmorHelmet() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition bb_main = partdefinition.addOrReplaceChild(
         "bb_main",
         CubeListBuilder.create()
            .texOffs(33, 0)
            .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.75F))
            .texOffs(33, 17)
            .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(1.0F))
            .texOffs(32, 93)
            .addBox(0.5F, -9.25F, -5.0F, -1.0F, 2.0F, 10.0F, new CubeDeformation(0.5F)),
         PartPose.offset(0.0F, 24.0F, 0.0F)
      );
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

public static LayerDefinition createGambisonArmorLeggings() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition LeggingsR = partdefinition.addOrReplaceChild(
         "LeggingsR",
         CubeListBuilder.create()
            .texOffs(51, 68)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F))
            .texOffs(85, 67)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.75F)),
         PartPose.offset(-2.0F, 12.0F, 0.0F)
      );
      PartDefinition LeggingsL = partdefinition.addOrReplaceChild(
         "LeggingsL",
         CubeListBuilder.create()
            .texOffs(68, 68)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F))
            .texOffs(85, 75)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.75F)),
         PartPose.offset(2.0F, 12.0F, 0.0F)
      );
      PartDefinition body = partdefinition.addOrReplaceChild(
         "body",
         CubeListBuilder.create().texOffs(78, 85).addBox(-4.0F, 9.1F, -2.0F, 8.0F, 2.0F, 4.0F, new CubeDeformation(0.48F)),
         PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

public static LayerDefinition createHeavyDiamondArmorBoots() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition BootsR = partdefinition.addOrReplaceChild(
         "BootsR",
         CubeListBuilder.create().texOffs(61, 85).addBox(-2.0F, 8.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(1.0F)),
         PartPose.offset(-2.0F, 12.0F, 0.0F)
      );
      PartDefinition BootsL = partdefinition.addOrReplaceChild(
         "BootsL",
         CubeListBuilder.create().texOffs(44, 85).addBox(-2.0F, 8.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(1.0F)),
         PartPose.offset(2.0F, 12.0F, 0.0F)
      );
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

public static LayerDefinition createHeavyDiamondArmorChestplate() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition ArmsL = partdefinition.addOrReplaceChild(
         "ArmsL",
         CubeListBuilder.create()
            .texOffs(83, 0)
            .addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F))
            .texOffs(83, 17)
            .addBox(-0.51F, -2.5F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(1.0F))
            .texOffs(7, 9)
            .addBox(-0.5F, 4.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(1.0F)),
         PartPose.offset(5.0F, 14.0F, 0.0F)
      );
      PartDefinition ArmsR = partdefinition.addOrReplaceChild(
         "ArmsR",
         CubeListBuilder.create()
            .texOffs(75, 34)
            .addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F))
            .texOffs(76, 51)
            .addBox(-3.49F, -2.5F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(1.0F))
            .texOffs(7, 19)
            .addBox(-3.5F, 4.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(1.0F)),
         PartPose.offset(-5.0F, 14.0F, 0.0F)
      );
      PartDefinition Torso = partdefinition.addOrReplaceChild(
         "Torso",
         CubeListBuilder.create()
            .texOffs(50, 34)
            .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.5F))
            .texOffs(0, 51)
            .addBox(-4.0F, 0.5F, -2.25F, 8.0F, 10.0F, 4.0F, new CubeDeformation(1.0F))
            .texOffs(0, 84)
            .addBox(-4.0F, 6.25F, -2.0F, 8.0F, 4.0F, 4.0F, new CubeDeformation(1.0F)),
         PartPose.offset(0.0F, 12.0F, 0.0F)
      );
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

public static LayerDefinition createHeavyDiamondArmorHelmet() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition bb_main = partdefinition.addOrReplaceChild(
         "bb_main",
         CubeListBuilder.create()
            .texOffs(33, 0)
            .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.75F))
            .texOffs(33, 17)
            .addBox(-4.0F, -8.25F, -3.75F, 8.0F, 8.0F, 8.0F, new CubeDeformation(1.0F))
            .texOffs(17, 34)
            .addBox(-4.0F, -8.25F, -4.25F, 8.0F, 8.0F, 8.0F, new CubeDeformation(1.0F)),
         PartPose.offset(0.0F, 24.0F, 0.0F)
      );
      PartDefinition cube_r1 = bb_main.addOrReplaceChild(
         "cube_r1",
         CubeListBuilder.create().texOffs(24, 52).addBox(-3.0F, -3.0F, -1.0F, 5.0F, 4.0F, 5.0F, new CubeDeformation(0.4F)),
         PartPose.offsetAndRotation(-0.7F, -0.5F, -6.0F, 0.0F, 0.7854F, 0.0F)
      );
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

public static LayerDefinition createHeavyDiamondArmorLeggings() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition LeggingsR = partdefinition.addOrReplaceChild(
         "LeggingsR",
         CubeListBuilder.create()
            .texOffs(51, 68)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F))
            .texOffs(85, 66)
            .addBox(-2.0F, 0.0F, -2.0F, 3.0F, 4.0F, 4.0F, new CubeDeformation(1.0F)),
         PartPose.offset(-2.0F, 12.0F, 0.0F)
      );
      PartDefinition LeggingsL = partdefinition.addOrReplaceChild(
         "LeggingsL",
         CubeListBuilder.create()
            .texOffs(68, 68)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F))
            .texOffs(85, 74)
            .addBox(-1.0F, 0.0F, -2.0F, 3.0F, 4.0F, 4.0F, new CubeDeformation(1.0F)),
         PartPose.offset(2.0F, 12.0F, 0.0F)
      );
      PartDefinition body = partdefinition.addOrReplaceChild(
         "body",
         CubeListBuilder.create().texOffs(78, 85).addBox(-4.0F, 9.1F, -2.0F, 8.0F, 2.0F, 4.0F, new CubeDeformation(0.48F)),
         PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

public static LayerDefinition createHeavyIronArmorBoots() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition BootsR = partdefinition.addOrReplaceChild(
         "BootsR",
         CubeListBuilder.create().texOffs(61, 85).addBox(-2.0F, 8.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(1.0F)),
         PartPose.offset(-2.0F, 12.0F, 0.0F)
      );
      PartDefinition BootsL = partdefinition.addOrReplaceChild(
         "BootsL",
         CubeListBuilder.create().texOffs(44, 85).addBox(-2.0F, 8.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(1.0F)),
         PartPose.offset(2.0F, 12.0F, 0.0F)
      );
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

public static LayerDefinition createHeavyIronArmorChestplate() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition ArmsL = partdefinition.addOrReplaceChild(
         "ArmsL",
         CubeListBuilder.create()
            .texOffs(83, 0)
            .addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F))
            .texOffs(83, 17)
            .addBox(-0.51F, -2.5F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(1.0F))
            .texOffs(7, 9)
            .addBox(-0.5F, 4.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(1.0F)),
         PartPose.offset(5.0F, 14.0F, 0.0F)
      );
      PartDefinition ArmsR = partdefinition.addOrReplaceChild(
         "ArmsR",
         CubeListBuilder.create()
            .texOffs(75, 34)
            .addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F))
            .texOffs(76, 51)
            .addBox(-3.49F, -2.5F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(1.0F))
            .texOffs(7, 19)
            .addBox(-3.5F, 4.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(1.0F)),
         PartPose.offset(-5.0F, 14.0F, 0.0F)
      );
      PartDefinition Torso = partdefinition.addOrReplaceChild(
         "Torso",
         CubeListBuilder.create()
            .texOffs(50, 34)
            .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.5F))
            .texOffs(0, 51)
            .addBox(-4.0F, 0.5F, -2.25F, 8.0F, 10.0F, 4.0F, new CubeDeformation(1.0F))
            .texOffs(0, 84)
            .addBox(-4.0F, 6.25F, -2.0F, 8.0F, 4.0F, 4.0F, new CubeDeformation(1.0F)),
         PartPose.offset(0.0F, 12.0F, 0.0F)
      );
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

public static LayerDefinition createHeavyIronArmorHelmet() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition bb_main = partdefinition.addOrReplaceChild(
         "bb_main",
         CubeListBuilder.create()
            .texOffs(33, 0)
            .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.75F))
            .texOffs(33, 17)
            .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(1.0F))
            .texOffs(25, 85)
            .addBox(0.5F, -9.0F, -5.0F, -1.0F, 2.0F, 10.0F, new CubeDeformation(0.5F)),
         PartPose.offset(0.0F, 24.0F, 0.0F)
      );
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

public static LayerDefinition createHeavyIronArmorLeggings() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition LeggingsR = partdefinition.addOrReplaceChild(
         "LeggingsR",
         CubeListBuilder.create()
            .texOffs(51, 68)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F))
            .texOffs(85, 66)
            .addBox(-2.0F, 0.0F, -2.0F, 3.0F, 4.0F, 4.0F, new CubeDeformation(1.0F)),
         PartPose.offset(-2.0F, 12.0F, 0.0F)
      );
      PartDefinition LeggingsL = partdefinition.addOrReplaceChild(
         "LeggingsL",
         CubeListBuilder.create()
            .texOffs(68, 68)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F))
            .texOffs(85, 74)
            .addBox(-1.0F, 0.0F, -2.0F, 3.0F, 4.0F, 4.0F, new CubeDeformation(1.0F)),
         PartPose.offset(2.0F, 12.0F, 0.0F)
      );
      PartDefinition body = partdefinition.addOrReplaceChild(
         "body",
         CubeListBuilder.create().texOffs(78, 85).addBox(-4.0F, 9.1F, -2.0F, 8.0F, 2.0F, 4.0F, new CubeDeformation(0.48F)),
         PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

public static LayerDefinition createHeavyNetheriteArmorHelmet() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition bb_main = partdefinition.addOrReplaceChild(
         "bb_main",
         CubeListBuilder.create()
            .texOffs(33, 17)
            .addBox(-4.0F, -7.95F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(1.0F))
            .texOffs(33, 0)
            .addBox(-4.0F, -8.5F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.7F))
            .texOffs(45, 99)
            .addBox(-3.5F, -8.5F, -4.0F, 7.0F, 8.0F, 8.0F, new CubeDeformation(1.0F))
            .texOffs(82, 95)
            .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.75F)),
         PartPose.offset(0.0F, 24.0F, 0.0F)
      );
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

public static LayerDefinition createLightDiamondArmorBoots() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition BootsR = partdefinition.addOrReplaceChild(
         "BootsR",
         CubeListBuilder.create()
            .texOffs(61, 85)
            .addBox(-2.0F, 7.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(1.0F))
            .texOffs(61, 95)
            .addBox(-2.0F, 6.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.75F)),
         PartPose.offset(-2.0F, 12.0F, 0.0F)
      );
      PartDefinition BootsL = partdefinition.addOrReplaceChild(
         "BootsL",
         CubeListBuilder.create()
            .texOffs(44, 85)
            .addBox(-2.0F, 7.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(1.0F))
            .texOffs(44, 95)
            .addBox(-2.0F, 6.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.75F)),
         PartPose.offset(2.0F, 12.0F, 0.0F)
      );
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

public static LayerDefinition createLightDiamondArmorChestplate() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition ArmsL = partdefinition.addOrReplaceChild(
         "ArmsL",
         CubeListBuilder.create()
            .texOffs(83, 0)
            .addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F))
            .texOffs(83, 17)
            .addBox(-1.01F, -2.25F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.73F))
            .texOffs(106, 17)
            .addBox(1.93F, -7.3F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.9F))
            .texOffs(7, 10)
            .addBox(-1.01F, 4.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.75F)),
         PartPose.offset(5.0F, 14.0F, 0.0F)
      );
      PartDefinition ArmsR = partdefinition.addOrReplaceChild(
         "ArmsR",
         CubeListBuilder.create()
            .texOffs(75, 34)
            .addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F))
            .texOffs(76, 51)
            .addBox(-2.97F, -2.25F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.73F))
            .texOffs(7, 20)
            .addBox(-2.99F, 4.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.75F))
            .texOffs(106, 8)
            .addBox(-0.07F, -7.3F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.9F)),
         PartPose.offset(-5.0F, 14.0F, 0.0F)
      );
      PartDefinition Torso = partdefinition.addOrReplaceChild(
         "Torso",
         CubeListBuilder.create()
            .texOffs(50, 34)
            .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.51F))
            .texOffs(0, 51)
            .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 11.0F, 4.0F, new CubeDeformation(0.75F))
            .texOffs(0, 84)
            .addBox(-4.0F, 6.5F, -2.0F, 8.0F, 4.0F, 4.0F, new CubeDeformation(0.75F)),
         PartPose.offset(0.0F, 12.0F, 0.0F)
      );
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

public static LayerDefinition createLightDiamondArmorHelmet() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition bb_main = partdefinition.addOrReplaceChild(
         "bb_main",
         CubeListBuilder.create()
            .texOffs(33, 17)
            .addBox(-4.0F, -7.95F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(1.0F))
            .texOffs(33, 0)
            .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.75F))
            .texOffs(2, 0)
            .addBox(-3.5F, -8.0F, -4.0F, 7.0F, 8.0F, 8.0F, new CubeDeformation(1.0F)),
         PartPose.offset(0.0F, 24.0F, 0.0F)
      );
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

public static LayerDefinition createLightDiamondArmorLeggings() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition LeggingsR = partdefinition.addOrReplaceChild(
         "LeggingsR",
         CubeListBuilder.create()
            .texOffs(51, 68)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F))
            .texOffs(85, 66)
            .addBox(-2.0F, 2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.75F)),
         PartPose.offset(-2.0F, 12.0F, 0.0F)
      );
      PartDefinition LeggingsL = partdefinition.addOrReplaceChild(
         "LeggingsL",
         CubeListBuilder.create()
            .texOffs(68, 68)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F))
            .texOffs(84, 74)
            .addBox(-2.0F, 2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.75F))
            .texOffs(78, 86)
            .addBox(-2.0F, -1.25F, -1.9F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.75F)),
         PartPose.offset(2.0F, 12.0F, 0.0F)
      );
      PartDefinition body = partdefinition.addOrReplaceChild(
         "body",
         CubeListBuilder.create().texOffs(79, 95).addBox(-4.0F, 9.1F, -2.0F, 8.0F, 2.0F, 4.0F, new CubeDeformation(0.48F)),
         PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

public static LayerDefinition createLightIronArmorBoots() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition BootsR = partdefinition.addOrReplaceChild(
         "BootsR",
         CubeListBuilder.create()
            .texOffs(61, 85)
            .addBox(-2.0F, 7.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(1.0F))
            .texOffs(61, 95)
            .addBox(-2.0F, 6.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.75F)),
         PartPose.offset(-2.0F, 12.0F, 0.0F)
      );
      PartDefinition BootsL = partdefinition.addOrReplaceChild(
         "BootsL",
         CubeListBuilder.create()
            .texOffs(44, 85)
            .addBox(-2.0F, 7.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(1.0F))
            .texOffs(44, 95)
            .addBox(-2.0F, 6.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.75F)),
         PartPose.offset(2.0F, 12.0F, 0.0F)
      );
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

public static LayerDefinition createLightIronArmorChestplate() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition ArmsL = partdefinition.addOrReplaceChild(
         "ArmsL",
         CubeListBuilder.create()
            .texOffs(83, 0)
            .addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F))
            .texOffs(83, 17)
            .addBox(-1.01F, -2.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.9F))
            .texOffs(7, 10)
            .addBox(-1.01F, 4.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.75F)),
         PartPose.offset(5.0F, 14.0F, 0.0F)
      );
      PartDefinition ArmsR = partdefinition.addOrReplaceChild(
         "ArmsR",
         CubeListBuilder.create()
            .texOffs(75, 34)
            .addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F))
            .texOffs(76, 51)
            .addBox(-2.97F, -2.25F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.73F))
            .texOffs(7, 20)
            .addBox(-2.99F, 4.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.75F)),
         PartPose.offset(-5.0F, 14.0F, 0.0F)
      );
      PartDefinition Torso = partdefinition.addOrReplaceChild(
         "Torso",
         CubeListBuilder.create()
            .texOffs(50, 34)
            .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.51F))
            .texOffs(0, 51)
            .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 11.0F, 4.0F, new CubeDeformation(0.75F))
            .texOffs(0, 84)
            .addBox(-4.0F, 6.5F, -2.0F, 8.0F, 4.0F, 4.0F, new CubeDeformation(0.75F)),
         PartPose.offset(0.0F, 12.0F, 0.0F)
      );
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

public static LayerDefinition createLightIronArmorHelmet() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition bb_main = partdefinition.addOrReplaceChild(
         "bb_main",
         CubeListBuilder.create()
            .texOffs(33, 17)
            .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(1.0F))
            .texOffs(33, 0)
            .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.75F))
            .texOffs(2, 0)
            .addBox(-3.5F, -8.0F, -4.0F, 7.0F, 8.0F, 8.0F, new CubeDeformation(1.0F)),
         PartPose.offset(0.0F, 24.0F, 0.0F)
      );
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

public static LayerDefinition createLightIronArmorLeggings() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition LeggingsR = partdefinition.addOrReplaceChild(
         "LeggingsR",
         CubeListBuilder.create()
            .texOffs(51, 68)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F))
            .texOffs(85, 66)
            .addBox(-2.0F, 2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.75F)),
         PartPose.offset(-2.0F, 12.0F, 0.0F)
      );
      PartDefinition LeggingsL = partdefinition.addOrReplaceChild(
         "LeggingsL",
         CubeListBuilder.create()
            .texOffs(68, 68)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F))
            .texOffs(84, 74)
            .addBox(-2.0F, 2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.75F))
            .texOffs(78, 86)
            .addBox(-2.0F, -1.25F, -1.9F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.75F)),
         PartPose.offset(2.0F, 12.0F, 0.0F)
      );
      PartDefinition body = partdefinition.addOrReplaceChild(
         "body",
         CubeListBuilder.create().texOffs(79, 95).addBox(-4.0F, 9.1F, -2.0F, 8.0F, 2.0F, 4.0F, new CubeDeformation(0.48F)),
         PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

public static LayerDefinition createMailIronArmorBoots() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition BootsR = partdefinition.addOrReplaceChild(
         "BootsR",
         CubeListBuilder.create().texOffs(61, 85).addBox(-2.0F, 7.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(1.0F)),
         PartPose.offset(-2.0F, 12.0F, 0.0F)
      );
      PartDefinition BootsL = partdefinition.addOrReplaceChild(
         "BootsL",
         CubeListBuilder.create().texOffs(44, 85).addBox(-2.0F, 7.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(1.0F)),
         PartPose.offset(2.0F, 12.0F, 0.0F)
      );
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

public static LayerDefinition createMailIronArmorChestplate() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition ArmsL = partdefinition.addOrReplaceChild(
         "ArmsL",
         CubeListBuilder.create().texOffs(83, 0).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F)),
         PartPose.offset(5.0F, 14.0F, 0.0F)
      );
      PartDefinition ArmsR = partdefinition.addOrReplaceChild(
         "ArmsR",
         CubeListBuilder.create().texOffs(75, 34).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F)),
         PartPose.offset(-5.0F, 14.0F, 0.0F)
      );
      PartDefinition Torso = partdefinition.addOrReplaceChild(
         "Torso",
         CubeListBuilder.create()
            .texOffs(50, 34)
            .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.51F))
            .texOffs(0, 51)
            .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.75F)),
         PartPose.offset(0.0F, 12.0F, 0.0F)
      );
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

public static LayerDefinition createMailIronArmorHelmet() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition bb_main = partdefinition.addOrReplaceChild(
         "bb_main",
         CubeListBuilder.create()
            .texOffs(33, 0)
            .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.75F))
            .texOffs(33, 17)
            .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(1.0F))
            .texOffs(32, 93)
            .addBox(0.5F, -9.25F, -5.0F, -1.0F, 2.0F, 10.0F, new CubeDeformation(0.5F)),
         PartPose.offset(0.0F, 24.0F, 0.0F)
      );
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

public static LayerDefinition createMailIronArmorLeggings() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition LeggingsR = partdefinition.addOrReplaceChild(
         "LeggingsR",
         CubeListBuilder.create()
            .texOffs(51, 68)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F))
            .texOffs(85, 66)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.75F)),
         PartPose.offset(-2.0F, 12.0F, 0.0F)
      );
      PartDefinition LeggingsL = partdefinition.addOrReplaceChild(
         "LeggingsL",
         CubeListBuilder.create()
            .texOffs(68, 68)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F))
            .texOffs(84, 74)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.75F)),
         PartPose.offset(2.0F, 12.0F, 0.0F)
      );
      PartDefinition body = partdefinition.addOrReplaceChild(
         "body",
         CubeListBuilder.create().texOffs(78, 85).addBox(-4.0F, 9.1F, -2.0F, 8.0F, 2.0F, 4.0F, new CubeDeformation(0.48F)),
         PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

public static LayerDefinition createMediumDiamondArmorBoots() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition BootsR = partdefinition.addOrReplaceChild(
         "BootsR",
         CubeListBuilder.create().texOffs(61, 85).addBox(-2.0F, 7.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(1.0F)),
         PartPose.offset(-2.0F, 12.0F, 0.0F)
      );
      PartDefinition BootsL = partdefinition.addOrReplaceChild(
         "BootsL",
         CubeListBuilder.create().texOffs(44, 85).addBox(-2.0F, 7.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(1.0F)),
         PartPose.offset(2.0F, 12.0F, 0.0F)
      );
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

public static LayerDefinition createMediumDiamondArmorChestplate() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition ArmsL = partdefinition.addOrReplaceChild(
         "ArmsL",
         CubeListBuilder.create()
            .texOffs(83, 0)
            .addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F))
            .texOffs(83, 17)
            .addBox(-1.01F, -2.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(1.0F))
            .texOffs(7, 10)
            .addBox(-1.01F, 4.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.75F)),
         PartPose.offset(5.0F, 14.0F, 0.0F)
      );
      PartDefinition ArmsR = partdefinition.addOrReplaceChild(
         "ArmsR",
         CubeListBuilder.create()
            .texOffs(75, 34)
            .addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F))
            .texOffs(76, 51)
            .addBox(-2.99F, -2.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(1.0F))
            .texOffs(7, 20)
            .addBox(-2.99F, 4.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.75F)),
         PartPose.offset(-5.0F, 14.0F, 0.0F)
      );
      PartDefinition Torso = partdefinition.addOrReplaceChild(
         "Torso",
         CubeListBuilder.create()
            .texOffs(50, 34)
            .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.51F))
            .texOffs(0, 51)
            .addBox(-4.0F, 0.5F, -2.0F, 8.0F, 10.0F, 4.0F, new CubeDeformation(0.75F))
            .texOffs(0, 84)
            .addBox(-4.0F, 6.5F, -2.0F, 8.0F, 4.0F, 4.0F, new CubeDeformation(0.75F)),
         PartPose.offset(0.0F, 12.0F, 0.0F)
      );
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

public static LayerDefinition createMediumDiamondArmorHelmet() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition bb_main = partdefinition.addOrReplaceChild(
         "bb_main",
         CubeListBuilder.create()
            .texOffs(33, 0)
            .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F))
            .texOffs(33, 17)
            .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.75F))
            .texOffs(2, 99)
            .addBox(-4.2F, -7.8F, -4.2F, 4.0F, 8.0F, 8.0F, new CubeDeformation(0.55F))
            .texOffs(30, 99)
            .addBox(0.2F, -7.8F, -4.2F, 4.0F, 8.0F, 8.0F, new CubeDeformation(0.55F))
            .texOffs(45, 99)
            .addBox(-3.5F, -8.0F, -4.0F, 7.0F, 8.0F, 8.0F, new CubeDeformation(1.0F)),
         PartPose.offset(0.0F, 24.0F, 0.0F)
      );
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

public static LayerDefinition createMediumDiamondArmorLeggings() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition LeggingsR = partdefinition.addOrReplaceChild(
         "LeggingsR",
         CubeListBuilder.create()
            .texOffs(51, 68)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F))
            .texOffs(85, 66)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.75F)),
         PartPose.offset(-2.0F, 12.0F, 0.0F)
      );
      PartDefinition LeggingsL = partdefinition.addOrReplaceChild(
         "LeggingsL",
         CubeListBuilder.create()
            .texOffs(68, 68)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F))
            .texOffs(84, 74)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.75F)),
         PartPose.offset(2.0F, 12.0F, 0.0F)
      );
      PartDefinition body = partdefinition.addOrReplaceChild(
         "body",
         CubeListBuilder.create().texOffs(78, 85).addBox(-4.0F, 9.1F, -2.0F, 8.0F, 2.0F, 4.0F, new CubeDeformation(0.48F)),
         PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

public static LayerDefinition createMediumIronArmorBoots() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition BootsR = partdefinition.addOrReplaceChild(
         "BootsR",
         CubeListBuilder.create().texOffs(61, 85).addBox(-2.0F, 7.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(1.0F)),
         PartPose.offset(-2.0F, 12.0F, 0.0F)
      );
      PartDefinition BootsL = partdefinition.addOrReplaceChild(
         "BootsL",
         CubeListBuilder.create().texOffs(44, 85).addBox(-2.0F, 7.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(1.0F)),
         PartPose.offset(2.0F, 12.0F, 0.0F)
      );
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

public static LayerDefinition createMediumIronArmorChestplate() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition ArmsL = partdefinition.addOrReplaceChild(
         "ArmsL",
         CubeListBuilder.create()
            .texOffs(83, 0)
            .addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F))
            .texOffs(83, 17)
            .addBox(-1.01F, -2.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(1.0F))
            .texOffs(7, 10)
            .addBox(-1.01F, 4.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.75F)),
         PartPose.offset(5.0F, 14.0F, 0.0F)
      );
      PartDefinition ArmsR = partdefinition.addOrReplaceChild(
         "ArmsR",
         CubeListBuilder.create()
            .texOffs(75, 34)
            .addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F))
            .texOffs(76, 51)
            .addBox(-2.99F, -2.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(1.0F))
            .texOffs(7, 20)
            .addBox(-2.99F, 4.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.75F)),
         PartPose.offset(-5.0F, 14.0F, 0.0F)
      );
      PartDefinition Torso = partdefinition.addOrReplaceChild(
         "Torso",
         CubeListBuilder.create()
            .texOffs(50, 34)
            .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.51F))
            .texOffs(0, 51)
            .addBox(-4.0F, 0.5F, -2.0F, 8.0F, 10.0F, 4.0F, new CubeDeformation(0.75F))
            .texOffs(0, 84)
            .addBox(-4.0F, 6.5F, -2.0F, 8.0F, 4.0F, 4.0F, new CubeDeformation(0.75F)),
         PartPose.offset(0.0F, 12.0F, 0.0F)
      );
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

public static LayerDefinition createMediumIronArmorHelmet() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition bb_main = partdefinition.addOrReplaceChild(
         "bb_main",
         CubeListBuilder.create()
            .texOffs(33, 0)
            .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.75F))
            .texOffs(33, 17)
            .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(1.0F))
            .texOffs(32, 93)
            .addBox(0.5F, -9.25F, -5.0F, -1.0F, 2.0F, 10.0F, new CubeDeformation(0.5F)),
         PartPose.offset(0.0F, 24.0F, 0.0F)
      );
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

public static LayerDefinition createMediumIronArmorLeggings() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition LeggingsR = partdefinition.addOrReplaceChild(
         "LeggingsR",
         CubeListBuilder.create()
            .texOffs(51, 68)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F))
            .texOffs(85, 66)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.75F)),
         PartPose.offset(-2.0F, 12.0F, 0.0F)
      );
      PartDefinition LeggingsL = partdefinition.addOrReplaceChild(
         "LeggingsL",
         CubeListBuilder.create()
            .texOffs(68, 68)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F))
            .texOffs(84, 74)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.75F)),
         PartPose.offset(2.0F, 12.0F, 0.0F)
      );
      PartDefinition body = partdefinition.addOrReplaceChild(
         "body",
         CubeListBuilder.create().texOffs(78, 85).addBox(-4.0F, 9.1F, -2.0F, 8.0F, 2.0F, 4.0F, new CubeDeformation(0.48F)),
         PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      return LayerDefinition.create(meshdefinition, 128, 128);
   }
}
