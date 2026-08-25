package com.majorbonghits.moderncompanions.entity.magic;

/** No-world regression check for safe spell repertoires and physical mage starter weapons. */
public final class MagicCompanionKitTest {
    private MagicCompanionKitTest() {}

    public static void main(String[] args) {
        assert MagicCompanionKit.SORCERER.hostileTargetsOnly;
        assert "chain_lightning".equals(MagicCompanionKit.SORCERER.ironHeavy);
        assert MagicCompanionKit.LIGHTNING_MAGE.hostileTargetsOnly;
        assert "lightning_bolt".equals(MagicCompanionKit.LIGHTNING_MAGE.ironHeavy);
        for (String part : MagicCompanionKit.LIGHTNING_MAGE.arsHeavy) {
            assert !"AugmentSplit".equals(part) : "Lightning Mage heavy Ars cast must stay single-target";
        }
        assert "firebolt".equals(MagicCompanionKit.BATTLEMAGE.ironBasic);
        assert "fang_strike".equals(MagicCompanionKit.BATTLEMAGE.ironHeavy);
        assert !"spectral_hammer".equals(MagicCompanionKit.BATTLEMAGE.ironBasic);
        assert !"fang_ward".equals(MagicCompanionKit.BATTLEMAGE.ironHeavy);
        assert "greater_heal".equals(MagicCompanionKit.CLERIC.ironHeavy);
        assert !"healing_circle".equals(MagicCompanionKit.CLERIC.ironHeavy);
        assert !"regeneration".equals(MagicCompanionKit.CLERIC.ironUtility);

        assert MagicCompanionKit.FIRE_MAGE.spawnWeaponId().equals("wooden_quarterstaff");
        assert MagicCompanionKit.LIGHTNING_MAGE.spawnWeaponId().equals("iron_dagger");
        assert MagicCompanionKit.NECROMANCER.spawnWeaponId().equals("stone_dagger");
        assert MagicCompanionKit.CLERIC.spawnWeaponId().equals("golden_sword");
        assert MagicCompanionKit.WIZARD.spawnWeaponId().equals("wooden_quarterstaff");
        assert MagicCompanionKit.SORCERER.spawnWeaponId().equals("iron_dagger");
        assert MagicCompanionKit.WARLOCK.spawnWeaponId().equals("iron_dagger");
        assert MagicCompanionKit.WITCH.spawnWeaponId().equals("wooden_quarterstaff");
        assert MagicCompanionKit.HAG.spawnWeaponId().equals("iron_dagger");
        assert MagicCompanionKit.CRYOMANCER.spawnWeaponId().equals("wooden_quarterstaff");
        assert MagicCompanionKit.DRUID.spawnWeaponId().equals("wooden_quarterstaff");
        assert MagicCompanionKit.ILLUSIONIST.spawnWeaponId().equals("iron_dagger");
        assert MagicCompanionKit.BATTLEMAGE.spawnWeaponId().equals("iron_dagger");
    }
}
