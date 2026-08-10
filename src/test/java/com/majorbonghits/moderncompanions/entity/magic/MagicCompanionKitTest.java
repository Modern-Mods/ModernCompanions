package com.majorbonghits.moderncompanions.entity.magic;

/** No-world regression check for the safe spell repertoire and hostile-only Sorcerer kit. */
public final class MagicCompanionKitTest {
    private MagicCompanionKitTest() {}

    public static void main(String[] args) {
        assert MagicCompanionKit.SORCERER.hostileTargetsOnly;
        assert "chain_lightning".equals(MagicCompanionKit.SORCERER.ironHeavy);
        assert "firebolt".equals(MagicCompanionKit.BATTLEMAGE.ironBasic);
        assert "fang_strike".equals(MagicCompanionKit.BATTLEMAGE.ironHeavy);
        assert !"spectral_hammer".equals(MagicCompanionKit.BATTLEMAGE.ironBasic);
        assert !"fang_ward".equals(MagicCompanionKit.BATTLEMAGE.ironHeavy);
    }
}
