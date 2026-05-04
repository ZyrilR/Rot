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

        // THE FIX: Base capture rate now has a 30% floor, plus up to 50% based on missing HP!
        double hpRatio  = (double) target.getCurrentHp() / target.getMaxHp();
        double baseRate = 30.0 + ((1.0 - hpRatio) * 50.0);

        // Status Effect Bonus (+15% if they are asleep, paralyzed, burned, etc.)
        if (!target.getStatus().equalsIgnoreCase("NONE")) {
            baseRate += 15.0;
        }

        // Clean Catch check — full HP before any damage modifier
        boolean atFullHp = (target.getCurrentHp() == target.getMaxHp());

        // Capsule bonus
        double bonus = 0;
        if (capsuleName.equals("SPEED CAPSULE") && playerRot.getSpeed() > target.getSpeed()) bonus = 20;
        if (capsuleName.equals("HEAVY CAPSULE") && playerRot.getDefense() > target.getDefense()) bonus = 20;
        if (hasTypeBonus(capsuleName, target)) bonus = 20;

        // Tier resistance (Makes Diamond/Gold harder to catch without being impossible)
        double tierMult = switch (target.getTier()) {
            case GOLD    -> 0.8;  // 20% harder to catch
            case DIAMOND -> 0.5;  // 50% harder to catch
            default      -> 1.0;  // Normal
        };

        double totalRate = (baseRate + bonus) * tierMult;

        // Cap the max chance at 95% so there is always a tiny risk of breaking out, unless using Master
        totalRate = Math.min(95.0, totalRate);

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