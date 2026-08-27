package battle;

import brainrots.BrainRot;
import brainrots.Tier;
import brainrots.Type;
import skills.Skill;
import skills.SkillType;

/**
 * Dependency-free regression tests for tactical battle decisions and previews.
 * Run with assertions enabled through scripts/test.sh.
 */
public final class BattleAITest {

    private static int assertions = 0;

    public static void main(String[] args) {
        damagePreviewIsStableAndBounded();
        typeAdvantageImprovesMoveScore();
        knockoutMoveBeatsWeakerMove();
        healingIsValuableAtCriticalHealth();
        unusableMovesAreRejected();
        emptyMoveSetFallsBackToStruggle();

        System.out.println("BattleAITest passed (" + assertions + " assertions)");
    }

    private static void damagePreviewIsStableAndBounded() {
        BrainRot attacker = rot("Attacker", Type.FIRE, 100, 60, 45, 50);
        BrainRot defender = rot("Defender", Type.GRASS, 100, 45, 50, 40);
        Skill move = move("Flare", SkillType.FIRE, 20, "NONE");

        DamageCalculator.DamageRange first = DamageCalculator.preview(move, attacker, defender);
        DamageCalculator.DamageRange second = DamageCalculator.preview(move, attacker, defender);

        check(first.min > 0, "damaging moves must preview positive damage");
        check(first.min <= first.max, "damage preview must have an ordered range");
        check(first.min == second.min && first.max == second.max,
                "preview must not consume randomness");
        check(first.effectiveness > 1.0, "fire should be effective against grass");
    }

    private static void typeAdvantageImprovesMoveScore() {
        BrainRot attacker = rot("Attacker", Type.NORMAL, 100, 55, 45, 45);
        BrainRot defender = rot("Defender", Type.GRASS, 100, 45, 50, 40);
        Skill neutral = move("Neutral Hit", SkillType.NORMAL, 20, "NONE");
        Skill effective = move("Fire Hit", SkillType.FIRE, 20, "NONE");

        check(BattleAI.scoreMove(effective, attacker, defender)
                        > BattleAI.scoreMove(neutral, attacker, defender),
                "AI should value a type advantage");
    }

    private static void knockoutMoveBeatsWeakerMove() {
        BrainRot attacker = rot("Attacker", Type.NORMAL, 100, 65, 40, 40);
        BrainRot defender = rot("Defender", Type.NORMAL, 100, 45, 35, 35);
        defender.takeDamage(90);
        Skill weak = move("Tap", SkillType.NORMAL, 1, "NONE");
        Skill finisher = move("Finisher", SkillType.NORMAL, 20, "NONE");

        check(BattleAI.scoreMove(finisher, attacker, defender)
                        > BattleAI.scoreMove(weak, attacker, defender),
                "AI should prioritize a reliable knockout");
    }

    private static void healingIsValuableAtCriticalHealth() {
        BrainRot attacker = rot("Attacker", Type.NORMAL, 100, 45, 45, 45);
        BrainRot defender = rot("Defender", Type.NORMAL, 100, 45, 45, 45);
        attacker.takeDamage(80);
        Skill heal = move("Recover", SkillType.NORMAL, 0, "HEAL");
        Skill weak = move("Tap", SkillType.NORMAL, 1, "NONE");

        check(BattleAI.scoreMove(heal, attacker, defender)
                        > BattleAI.scoreMove(weak, attacker, defender),
                "AI should strongly consider healing at critical health");
    }

    private static void unusableMovesAreRejected() {
        BrainRot attacker = rot("Attacker", Type.NORMAL, 100, 45, 45, 45);
        BrainRot defender = rot("Defender", Type.NORMAL, 100, 45, 45, 45);
        Skill exhausted = move("Empty", SkillType.NORMAL, 20, "NONE");
        exhausted.setCurrentUP(0);

        check(BattleAI.scoreMove(exhausted, attacker, defender)
                        == Double.NEGATIVE_INFINITY,
                "moves without UP must never be selected");
    }

    private static void emptyMoveSetFallsBackToStruggle() {
        BrainRot attacker = rot("Attacker", Type.NORMAL, 100, 45, 45, 45);
        BrainRot defender = rot("Defender", Type.NORMAL, 100, 45, 45, 45);

        check(BattleAI.chooseMove(attacker, defender, true) == BattleManager.STRUGGLE_INDEX,
                "an opponent with no usable moves must use Struggle");
    }

    private static BrainRot rot(String name, Type type, int hp, int attack, int defense, int speed) {
        return new BrainRot(name, type, null, Tier.NORMAL, 10, hp, attack, defense, speed);
    }

    private static Skill move(String name, SkillType type, int power, String effect) {
        return new Skill(name, type, power, 10, effect, "Test move");
    }

    private static void check(boolean condition, String message) {
        assertions++;
        if (!condition) throw new AssertionError(message);
    }
}
