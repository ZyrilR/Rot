package battle;

import brainrots.BrainRot;
import skills.Skill;
import utils.RandomUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Scores an opponent's available moves using the live battle situation.
 *
 * Trainer opponents are deliberately more consistent than wild BrainRots, but
 * neither is perfectly predictable. This keeps ordinary encounters lively while
 * allowing important trainer fights to feel meaningfully tactical.
 */
public final class BattleAI {

    private BattleAI() {}

    private static final class Candidate {
        final int index;
        final double score;

        Candidate(int index, double score) {
            this.index = index;
            this.score = score;
        }
    }

    /**
     * Chooses a usable move, or {@link BattleManager#STRUGGLE_INDEX} when no move
     * has UP remaining.
     */
    public static int chooseMove(BrainRot attacker, BrainRot defender, boolean trainerBattle) {
        List<Candidate> ranked = rank(attacker, defender);
        if (ranked.isEmpty()) return BattleManager.STRUGGLE_INDEX;
        if (ranked.size() == 1) return ranked.get(0).index;

        // Trainers usually make the best play. Wild opponents are more impulsive.
        int bestMoveChance = trainerBattle ? 88 : 68;
        if (RandomUtil.chance(bestMoveChance)) return ranked.get(0).index;

        // Exploration is limited to the next two credible choices so enemies do
        // not waste turns on obviously bad moves merely for randomness' sake.
        int alternativeCount = Math.min(3, ranked.size());
        return ranked.get(RandomUtil.range(1, alternativeCount - 1)).index;
    }

    /** Exposed for diagnostics and lightweight tests. Higher is a better play. */
    public static double scoreMove(Skill skill, BrainRot attacker, BrainRot defender) {
        if (skill == null || attacker == null || defender == null || skill.getCurrentUP() <= 0) {
            return Double.NEGATIVE_INFINITY;
        }

        DamageCalculator.DamageRange damage = DamageCalculator.preview(skill, attacker, defender);
        double score = damage.average();

        if (damage.max >= defender.getCurrentHp()) score += 70;       // secure a knockout
        if (damage.effectiveness > 1.0) score += 22;                  // exploit a weakness
        else if (damage.effectiveness < 1.0) score -= 12;

        String effect = skill.getEffect() == null ? "NONE" : skill.getEffect().toUpperCase();
        double ownHpRatio = (double) attacker.getCurrentHp() / Math.max(1, attacker.getMaxHp());

        switch (effect) {
            case "HEAL" -> {
                double missingHp = 1.0 - ownHpRatio;
                score += missingHp * 55;
                if (ownHpRatio <= 0.35) score += 34;
                if (ownHpRatio >= 0.95) score -= 55;
            }
            case "BURN", "PARALYZE", "CONFUSE", "SLEEP" ->
                    score += defender.getStatus().equalsIgnoreCase("NONE") ? 18 : -18;
            case "FLINCH" -> score += attacker.getSpeed() >= defender.getSpeed() ? 12 : 3;
            case "LOWER_ATK", "LOWER_DEF", "LOWER_SPD" -> score += 10;
            case "RAISE_ATK" -> score += attacker.getAttack() <= attacker.getBaseAtk() ? 13 : 3;
            case "RAISE_DEF" -> score += attacker.getDefense() <= attacker.getBaseDef() ? 13 : 3;
            case "RAISE_SPD" -> score += attacker.getSpeed() <= attacker.getBaseSpeed() ? 13 : 3;
            default -> { }
        }

        // Prefer preserving scarce signature moves unless they offer a real edge.
        if (skill.getCurrentUP() == 1 && damage.max < defender.getCurrentHp()) score -= 4;
        return score;
    }

    private static List<Candidate> rank(BrainRot attacker, BrainRot defender) {
        List<Candidate> candidates = new ArrayList<>();
        if (attacker == null || defender == null || attacker.getMoves() == null) return candidates;

        for (int i = 0; i < attacker.getMoves().size(); i++) {
            Skill skill = attacker.getMoves().get(i);
            if (skill == null || skill.getCurrentUP() <= 0) continue;
            candidates.add(new Candidate(i, scoreMove(skill, attacker, defender)));
        }
        candidates.sort(Comparator.comparingDouble((Candidate c) -> c.score).reversed());
        return candidates;
    }
}
