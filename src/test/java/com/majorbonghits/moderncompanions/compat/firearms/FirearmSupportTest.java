package com.majorbonghits.moderncompanions.compat.firearms;

/** Pure regression check for the TacZ category names used by the optional bridge. */
public final class FirearmSupportTest {
    private FirearmSupportTest() {}

    public static void main(String[] args) {
        assert FirearmSupport.Specialty.fromTacZType("pistol").orElseThrow()
                == FirearmSupport.Specialty.PISTOL;
        assert FirearmSupport.Specialty.fromTacZType(" SMG ").orElseThrow()
                == FirearmSupport.Specialty.SMG;
        assert FirearmSupport.Specialty.fromTacZType("machine gun").orElseThrow()
                == FirearmSupport.Specialty.MACHINE_GUN;
        assert FirearmSupport.Specialty.fromTacZType("rpg").orElseThrow()
                == FirearmSupport.Specialty.HEAVY;
        assert FirearmSupport.Specialty.fromTacZType("launcher").isEmpty();
        assert FirearmSupport.Specialty.PISTOL.displayNameKey().equals("entity.modern_companions.firearm_specialist.pistol");
        assert FirearmSupport.Specialty.MACHINE_GUN.displayNameKey().equals("entity.modern_companions.firearm_specialist.machine_gun");
        assert FirearmSupport.Specialty.SNIPER.displayNameKey().equals("entity.modern_companions.firearm_specialist.sniper");
        assert FirearmSupport.Specialty.PISTOL.weight()
                + FirearmSupport.Specialty.SMG.weight()
                + FirearmSupport.Specialty.RIFLE.weight()
                + FirearmSupport.Specialty.SHOTGUN.weight()
                + FirearmSupport.Specialty.SNIPER.weight()
                + FirearmSupport.Specialty.MACHINE_GUN.weight()
                + FirearmSupport.Specialty.HEAVY.weight() == 100;
    }
}
