package brainrots;

import java.util.Arrays;
import java.util.List;

/**
 * Central registry listing all 8 BrainRot names.
 * Use BrainRotFactory.create(name, tier) to instantiate them.
 */
public class BrainRotRegistry {

    public static final String TUNG_TUNG      = "TUNG TUNG TUNG SAHUR";
    public static final String TRALALERO      = "TRALALERO TRALALA";
    public static final String BOMBARDINO     = "BOMBARDINO CROCODILO";
    public static final String LIRILI         = "LIRILI LARILA";
    public static final String PATAPIM        = "BRR BRR PATAPIM";
    public static final String BONECA         = "BONECA AMBALABU";
    public static final String UDIN           = "UDIN DIN DIN DIN DUN";
    public static final String CAPUCCINO      = "CAPUCCINO ASSASSINO";

    public static final List<String> ALL = Arrays.asList(
            TUNG_TUNG, TRALALERO, BOMBARDINO, LIRILI, PATAPIM, BONECA, UDIN, CAPUCCINO
    );

    /**
     * Convenience: spawn a BrainRot by registry constant and tier.
     */
    public static BrainRot spawn(String name, Tier tier) {
        return BrainRotFactory.create(name, tier);
    }

    /**
     * Returns true if the given name is a valid registered BrainRot.
     */
    public static boolean isValid(String name) {
        return ALL.contains(name.toUpperCase());
    }
}