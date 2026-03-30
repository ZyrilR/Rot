package items;

import brainrots.BrainRot;
import brainrots.Type;
import skills.Skill;
import skills.SkillPool;
import skills.SkillRegistry;
import skills.SkillType;

/**
 * A Scroll teaches a BrainRot a new skill.
 *
 * Conditions enforced:
 *  1. The skill must exist in the registry.
 *  2. The BrainRot's type must match the skill's SkillType
 *     (or the BrainRot must be the designated owner for signature moves).
 *  3. Signature moves can only be learned by their specific BrainRot.
 *  4. The BrainRot cannot already know the skill.
 *  5. Max 4 skills — if full, a swap index must be supplied via extraArgs.
 *
 * Usage:
 *   scroll.use(brainRot);               // learn normally (fails if full)
 *   scroll.use(brainRot, 2);            // swap slot index 2 with this skill
 *   scroll.use(brainRot, -1);           // dry-run: validate only, no changes
 */
public class Scroll extends Item {

    public enum ScrollResult {
        SUCCESS,
        SKILL_NOT_FOUND,
        TYPE_MISMATCH,
        SIGNATURE_MISMATCH,
        ALREADY_KNOWN,
        MOVESET_FULL,         // needs a swap index to proceed
        INVALID_SWAP_INDEX,
        SWAPPED               // successfully replaced an existing move
    }

    private final String skillName;

    public Scroll(String name, String description, String assetPath, String skillName, int price) {
        super(name, description, assetPath, price);
        this.skillName = skillName;
    }

    // ── Item interface ────────────────────────────────────────────────────────

    /**
     * extraArgs[0] (optional): Integer swap index (0–3).
     *   • Omit or pass null  → attempt normal learn (fails if moveset is full).
     *   • Pass a valid index → replace that move slot.
     *   • Pass -1            → dry-run validation only.
     */
    @Override
    public void use(BrainRot target, Object... extraArgs) {
        Integer swapIndex = null;
        if (extraArgs.length > 0 && extraArgs[0] instanceof Integer i) {
            swapIndex = i;
        }

        ScrollResult result = (swapIndex != null && swapIndex == -1)
                ? validate(target)
                : apply(target, swapIndex);

        logResult(result, target, swapIndex);
    }

    // ── Core logic ────────────────────────────────────────────────────────────

    /**
     * Validates AND applies the scroll to the target.
     * Returns the ScrollResult describing what happened.
     *
     * @param target    The BrainRot to teach.
     * @param swapIndex Slot index to replace (0–3), or null to attempt a fresh add.
     */
    public ScrollResult apply(BrainRot target, Integer swapIndex) {
        ScrollResult validation = validate(target);
        if (validation != ScrollResult.SUCCESS && validation != ScrollResult.MOVESET_FULL) {
            return validation; // hard failure — don't proceed
        }

        Skill skill = SkillRegistry.get(skillName);

        // Swap path
        if (swapIndex != null) {
            if (swapIndex < 0 || swapIndex >= target.getMoves().size()) {
                return ScrollResult.INVALID_SWAP_INDEX;
            }
            target.replaceMove(swapIndex, skill);
            return ScrollResult.SWAPPED;
        }

        // Normal learn path
        if (validation == ScrollResult.MOVESET_FULL) {
            return ScrollResult.MOVESET_FULL; // caller must ask player for swap index
        }

        target.addMove(skill);
        return ScrollResult.SUCCESS;
    }

    /**
     * Pure validation — no mutation.
     * Returns SUCCESS if all conditions pass, or the specific failure reason.
     */
    public ScrollResult validate(BrainRot target) {
        // 1. Skill must exist
        Skill skill = SkillRegistry.get(skillName);
        if (skill == null) return ScrollResult.SKILL_NOT_FOUND;

        // 2. Already knows this skill?
        boolean alreadyKnows = target.getMoves().stream()
                .anyMatch(m -> m.getName().equalsIgnoreCase(skillName));
        if (alreadyKnows) return ScrollResult.ALREADY_KNOWN;

        // 3. Signature move ownership check
        if (SkillPool.isSignatureMove(skillName)) {
            if (!SkillPool.isSignatureOf(skillName, target.getName())) {
                return ScrollResult.SIGNATURE_MISMATCH;
            }
            // Signature move owner bypasses type check — continue to capacity check
        } else {
            // 4. Type compatibility check for non-signature moves
            if (!isTypeCompatible(skill.getType(), target)) {
                return ScrollResult.TYPE_MISMATCH;
            }
        }

        // 5. Capacity check (must come last — it's a soft failure)
        if (target.getMoves().size() >= 4) return ScrollResult.MOVESET_FULL;

        return ScrollResult.SUCCESS;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Returns true if the skill's SkillType matches either of the BrainRot's Types.
     * SkillType and Type share identical names, so we compare by name string.
     */
    private boolean isTypeCompatible(SkillType skillType, BrainRot target) {
        return typeMatches(skillType, target.getPrimaryType())
                || (target.getSecondaryType() != null
                && typeMatches(skillType, target.getSecondaryType()));
    }

    private boolean typeMatches(SkillType skillType, Type brainRotType) {
        return skillType.name().equalsIgnoreCase(brainRotType.name());
    }

    private void logResult(ScrollResult result, BrainRot target, Integer swapIndex) {
        String skill = skillName;
        String rot   = target.getName();
        switch (result) {
            case SUCCESS           -> System.out.println(rot + " learned " + skill + "!");
            case SWAPPED           -> System.out.println(rot + " forgot the move in slot "
                    + swapIndex + " and learned " + skill + "!");
            case SKILL_NOT_FOUND   -> System.out.println("Error: Skill '" + skill + "' not found in registry.");
            case TYPE_MISMATCH     -> System.out.println(rot + " cannot learn " + skill
                    + " — type mismatch (needs "
                    + SkillRegistry.get(skill).getType() + ").");
            case SIGNATURE_MISMATCH-> System.out.println(skill + " is a signature move and cannot be learned by " + rot + ".");
            case ALREADY_KNOWN     -> System.out.println(rot + " already knows " + skill + ".");
            case MOVESET_FULL      -> System.out.println(rot + " already knows 4 moves. Choose a move to replace (0–3).");
            case INVALID_SWAP_INDEX-> System.out.println("Invalid swap index: " + swapIndex + ". Must be 0–3.");
        }
    }

    // ── Getter ────────────────────────────────────────────────────────────────

    public String getSkillName() { return skillName; }
}