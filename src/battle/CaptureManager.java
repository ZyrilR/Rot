package battle;

import brainrots.BrainRot;
import items.Capsule;
import progression.QuestSystem;
import utils.RandomUtil;

import java.util.List;

public class CaptureManager {

    public static boolean attempt(Capsule capsule, BrainRot target,
                                  BrainRot playerRot, List<BrainRot> playerTeam) {

        String capsuleName = capsule.getName().toUpperCase();

        // Master capsule = guaranteed
        if (capsuleName.equals("MASTER CAPSULE")) {
            return capture(target, target.getCurrentHp() == target.getMaxHp());
        }

        // Base capture rate scales with missing HP (0–60%)
        double hpRatio  = (double) target.getCurrentHp() / target.getMaxHp();
        double baseRate = (1.0 - hpRatio) * 60.0;

        // Clean Catch check — full HP before any damage modifier
        boolean atFullHp = (target.getCurrentHp() == target.getMaxHp());

        // Capsule bonus
        double bonus = 0;
        if (capsuleName.equals("SPEED CAPSULE") && playerRot.getSpeed() > target.getSpeed()) bonus = 20;
        if (capsuleName.equals("HEAVY CAPSULE") && playerRot.getDefense() > target.getDefense()) bonus = 20;
        if (hasTypeBonus(capsuleName, target)) bonus = 20;

        double totalRate = Math.min(95, baseRate + bonus);
        System.out.printf("Capture rate: %.1f%%%n", totalRate);

        if (RandomUtil.chance(totalRate)) {
            return capture(target, atFullHp);
        } else {
            System.out.println(target.getName() + " broke free!");
            return false;
        }
    }

    private static boolean hasTypeBonus(String capsuleName, BrainRot target) {
        if (!capsuleName.endsWith(" CAPSULE")) return false;
        String typePrefix = capsuleName.replace(" CAPSULE", "").trim();
        switch (typePrefix) {
            case "NORMAL": case "RED":   case "BLUE":
            case "SPEED":  case "HEAVY": case "MASTER":
                return false;
        }
        if (target.getPrimaryType().name().equalsIgnoreCase(typePrefix)) return true;
        return target.getSecondaryType() != null &&
                target.getSecondaryType().name().equalsIgnoreCase(typePrefix);
    }

    private static boolean capture(BrainRot target, boolean atFullHp) {
        // DO NOT add to playerTeam directly here! BattleUI will route it safely through PCSystem.
        System.out.println(target.getName() + " was caught!");

        // Fire quest hooks
        QuestSystem.getInstance().onCapture(target, atFullHp);

        if (target.getTier() == brainrots.Tier.GOLD)
            QuestSystem.getInstance().onGoldTierAcquired();

        if (target.getTier() == brainrots.Tier.DIAMOND)
            QuestSystem.getInstance().onDiamondTierAcquired(target.getName());

        return true;
    }
}