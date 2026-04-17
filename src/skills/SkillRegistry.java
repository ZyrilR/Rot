package skills;

import java.util.HashMap;

/**
 * Central registry for all skills in the game.
 * Stores normal pool, type pools, and signature moves.
 */
public class SkillRegistry {

    private static final HashMap<String, Skill> registry = new HashMap<>();

    static {
        // ── Normal Pool ──────────────────────────────────────────────────────
        register(new Skill("Guard Up", SkillType.NORMAL, 0, 10, "RAISE_DEF",
                "Hardens the body into a steel-tight stance.  Raises Defense for 2 turns."));
        register(new Skill("Focus Stance", SkillType.NORMAL, 0, 15, "RAISE_ATK",
                "Channels inner rage into raw striking power.  Raises Attack for 2 turns."));
        register(new Skill("Quick Step", SkillType.NORMAL, 0, 10, "RAISE_SPD",
                "A burst of explosive footwork that leaves afterimages.  Raises Speed for 2 turns."));
        register(new Skill("Rest", SkillType.NORMAL, 0, 15, "HEAL",
                "Takes a deep breath and lets the body recover.  Restores 30 percent of max HP."));
        register(new Skill("Speed Boost", SkillType.NORMAL, 0, 15, "RAISE_SPD",
                "Ignites an adrenaline surge through every muscle.  Significantly increases Speed."));
        register(new Skill("Fortify", SkillType.NORMAL, 0, 15, "RAISE_DEF",
                "Braces every joint and tightens every guard.  Raises Defense with a stalwart stance."));
        register(new Skill("Evasion Up", SkillType.NORMAL, 0, 10, "NONE",
                "Slips into an unpredictable rhythm that makes every strike feel like a guess.  Raises Evasion for 3 turns."));
        register(new Skill("Lullaby", SkillType.NORMAL, 0, 10, "SLEEP",
                "A soothing melody that may cause the target to fall Asleep."));

        // ── Fighting ─────────────────────────────────────────────────────────
        register(new Skill("Wooden Thump", SkillType.FIGHTING, 20, 20, "NONE",
                "A thunderous overhead slam with bone-rattling force.  Power: 20."));
        register(new Skill("Power Combo", SkillType.FIGHTING, 15, 15, "FLINCH",
                "A rapid two-hit barrage that leaves no time to breathe.  Power: 15 - May cause the target to Flinch."));
        register(new Skill("Counter Guard", SkillType.FIGHTING, 0, 15, "NONE",
                "Absorbs the next physical blow and coils for a devastating counter.  Raises user's counter damage."));
        register(new Skill("Battle Cry", SkillType.FIGHTING, 0, 15, "LOWER_ATK",
                "Lets out a savage roar that shakes the enemy's confidence.  Lowers the target's Attack."));

        // ── Water ─────────────────────────────────────────────────────────────
        register(new Skill("Aqua Dash", SkillType.WATER, 15, 15, "NONE",
                "Rockets forward on a jet of pressurized water.  Power: 15."));
        register(new Skill("Tidal Crash", SkillType.WATER, 20, 20, "LOWER_DEF",
                "Summons a wall of water and hurls it at the target.  Power: 20 - May lower the target's Defense."));
        register(new Skill("Aqua Engine", SkillType.WATER, 15, 15, "RAISE_SPD",
                "A jet-propelled hydro strike that leaves the user moving faster than before.  Power: 15 - Boosts user Speed after landing."));
        register(new Skill("Mist Veil", SkillType.WATER, 0, 15, "NONE",
                "Wraps the battlefield in a dense, disorienting mist.  Reduces enemy Accuracy for 2 turns."));

        // ── Psychic ──────────────────────────────────────────────────────────
        register(new Skill("Mind Pulse", SkillType.PSYCHIC, 15, 15, "NONE",
                "Fires a focused burst of psychic energy that never goes astray.  Power: 15."));
        register(new Skill("Rhythm Distort", SkillType.PSYCHIC, 0, 15, "CONFUSE",
                "Warps the target's internal beat until nothing makes sense.  Confuses the target for 2 turns."));
        register(new Skill("Mental Break", SkillType.PSYCHIC, 20, 20, "LOWER_DEF",
                "A shattering psi blast that punches straight through mental barriers.  Power: 20 - Lowers the target's Defense."));
        register(new Skill("Confusion Wave", SkillType.PSYCHIC, 15, 15, "CONFUSE",
                "Sends a disorienting shockwave rippling through the mind.  Power: 15 - May leave the target Confused."));
        register(new Skill("Dream Haze", SkillType.PSYCHIC, 0, 15, "SLEEP",
                "Surrounds the enemy in a thick mist; 40% chance to inflict Sleep."));

        // ── Flying ───────────────────────────────────────────────────────────
        register(new Skill("Sky Dive", SkillType.FLYING, 20, 20, "NONE",
                "Launches skyward then plummets like a comet onto the target.  Power: 20."));
        register(new Skill("Jetstream Slash", SkillType.FLYING, 15, 15, "LOWER_SPD",
                "Slices through the air at blistering speed, trailing razor wind.  Power: 15 - May lower the target's Speed."));
        register(new Skill("Air Cutter", SkillType.FLYING, 15, 15, "NONE",
                "Condenses the wind into a paper-thin blade and hurls it forward.  Power: 15."));
        register(new Skill("Wind Guard", SkillType.FLYING, 0, 10, "NONE",
                "Summons a spiraling gust that deflects incoming strikes.  Raises Evasion for 2 turns."));

        // ── Sand ─────────────────────────────────────────────────────────────
        register(new Skill("Prickly Trunk", SkillType.SAND, 20, 20, "NONE",
                "Slams the target with a sand-crusted, spine-covered limb.  Power: 20."));
        register(new Skill("Sand Surge", SkillType.SAND, 15, 15, "NONE",
                "Kicks up a gritty sandstorm and drives it into the target at full force.  Power: 15."));
        register(new Skill("Dune Crash", SkillType.SAND, 15, 15, "FLINCH",
                "A collapsing dune of compressed sand smashes down from above.  Power: 15 - May cause the target to Flinch."));
        register(new Skill("Desert Wall", SkillType.SAND, 0, 10, "RAISE_DEF",
                "Hardens a shell of compressed desert sand around the body.  Raises Defense for 3 turns."));

        // ── Grass ────────────────────────────────────────────────────────────
        register(new Skill("Vine Lash", SkillType.GRASS, 15, 15, "NONE",
                "Whips razor-sharp vines at blinding speed.  Power: 15."));
        register(new Skill("Leaf Strike", SkillType.GRASS, 20, 20, "NONE",
                "Brings a hardened leaf blade crashing down with incredible force.  Power: 20."));
        register(new Skill("Jungle Fever", SkillType.GRASS, 0, 15, "RAISE_ATK",
                "Unleashes a primal battle frenzy fueled by the jungle's energy.  Sharply raises Attack."));
        register(new Skill("Root Bind", SkillType.GRASS, 15, 15, "NONE",
                "Fast-growing roots erupt from the ground and coil around the target.  Power: 15."));

        // ── Rock ─────────────────────────────────────────────────────────────
        register(new Skill("Rock Slam", SkillType.ROCK, 20, 20, "FLINCH",
                "Hurls a massive boulder with both hands and drives it into the target.  Power: 20 - May cause Flinch."));
        register(new Skill("Stone Guard", SkillType.ROCK, 0, 15, "RAISE_DEF",
                "Wraps the body in plates of living stone that absorb punishment.  Raises Defense."));
        register(new Skill("Boulder Crash", SkillType.ROCK, 15, 15, "NONE",
                "Coats the entire body in granite and charges at full speed.  Power: 15."));
        register(new Skill("Defense Curl", SkillType.ROCK, 0, 10, "RAISE_DEF",
                "Curls into an impenetrable rocky ball that laughs at weak strikes.  Slightly raises Defense."));

        // ── Fire ─────────────────────────────────────────────────────────────
        register(new Skill("Flame Crash", SkillType.FIRE, 20, 20, "BURN",
                "A blazing full-body slam that sets the target alight.  Power: 20 - May inflict Burn."));
        register(new Skill("Heat Burst", SkillType.FIRE, 15, 15, "NONE",
                "Releases a sudden shockwave of scorching heat with no warning.  Power: 15."));
        register(new Skill("Inferno Dash", SkillType.FIRE, 15, 15, "BURN",
                "A flaming high-speed charge that leaves a trail of fire in its wake.  Power: 15 - May Burn the target on contact."));
        register(new Skill("Burn Rush", SkillType.FIRE, 15, 15, "BURN",
                "Charges through the enemy spreading flames across two turns.  Power: 15 - Chance to leave the target Burned."));

        // ── Dark ─────────────────────────────────────────────────────────────
        register(new Skill("Shadow Jab", SkillType.DARK, 15, 15, "NONE",
                "A quick deceptive jab launched from the blind spot.  Power: 15."));
        register(new Skill("Night Slash", SkillType.DARK, 20, 20, "LOWER_ATK",
                "A precise dark blade drawn in a single fluid motion.  Power: 20 - May lower the target's Attack."));
        register(new Skill("Fear Stare", SkillType.DARK, 0, 15, "LOWER_SPD",
                "Locks eyes with the target and lets a primal terror sink in.  Lowers the target's Speed."));
        register(new Skill("Ambush", SkillType.DARK, 15, 15, "NONE",
                "Strikes from a concealed position when the target least expects it.  Power: 15."));

        // ── Poison ───────────────────────────────────────────────────────────
        register(new Skill("Toxic Steam", SkillType.POISON, 20, 20, "NONE",
                "Releases a thick cloud of poisonous vapour that coats the lungs.  Power: 20."));
        register(new Skill("Venom Drip", SkillType.POISON, 15, 15, "NONE",
                "Drips concentrated venom that keeps burning long after contact.  Power: 15."));
        register(new Skill("Acid Spray", SkillType.POISON, 15, 15, "LOWER_DEF",
                "Corrodes armour and skin alike with a pressurized acid jet.  Power: 15 - Lowers the target's Defense."));
        register(new Skill("Poison Fang", SkillType.POISON, 15, 15, "NONE",
                "Buries venom-soaked fangs deep into the target.  Power: 15."));

        // ── Signature Moves ──────────────────────────────────────────────────
        // Tung Tung Tung Sahur
        register(new Skill("Sahur Chant", SkillType.FIGHTING, 15, 15, "LOWER_SPD",
                "An ancient wake-up chant that rattles the soul and drains the will to move.  Power: 15 - Lowers the target's Speed."));
        register(new Skill("Wake-Up Call", SkillType.FIGHTING, 10, 10, "NONE",
                "A thunderous strike that hits twice as hard when the target is Asleep or Confused.  Power: 10."));
        register(new Skill("Infinite Sahur", SkillType.FIGHTING, 5, 5, "PARALYZE",
                "An unending drum solo that rattles every nerve in the body.  Power: 5 - 50 percent chance to leave the target Paralyzed."));

        // Tralalero Tralala
        register(new Skill("Sneaker Dash", SkillType.WATER, 20, 20, "NONE",
                "A hypersonic sprint that always lands first regardless of Speed.  Power: 20."));
        register(new Skill("Glitchy Rhythm", SkillType.PSYCHIC, 15, 15, "CONFUSE",
                "A corrupted beat that lodges itself in the target's brain and refuses to leave.  Power: 15 - Confuses the target for 2 turns."));
        register(new Skill("Neon Rave", SkillType.PSYCHIC, 5, 5, "NONE",
                "An overwhelming psychedelic light show that leaves enemies too dazzled to act.  Power: 5."));

        // Bombardino Crocodilo
        register(new Skill("Caffeine Snap", SkillType.WATER, 20, 20, "RAISE_SPD",
                "A hyper-caffeinated lunge that bites hard and sends the user into overdrive.  Power: 20 - Boosts user Speed after landing."));
        register(new Skill("Tail Missile", SkillType.FLYING, 10, 10, "LOWER_DEF",
                "Launches a tail like a heat-seeking missile at the target.  Power: 10 - Cracks the target's Defense on impact."));
        register(new Skill("Ristretto Nuke", SkillType.FIRE, 5, 5, "BURN",
                "Condenses an entire espresso into one catastrophic detonation.  Power: 5 - Burns everything caught in the blast."));

        // Lirili Larila
        register(new Skill("Sandal Stomp", SkillType.SAND, 15, 15, "FLINCH",
                "Brings a massive sandal crashing down with the weight of history behind it.  Power: 15 - May cause the target to Flinch."));
        register(new Skill("Clock Rewind", SkillType.SAND, 0, 10, "HEAL",
                "Rewinds the flow of time just enough to undo recent wounds.  Restores 30 percent of max HP."));
        register(new Skill("Timeline Rot", SkillType.SAND, 5, 5, "NONE",
                "Corrupts the target's personal timeline and reverses the turn order for 5 turns.  Power: 5."));

        // Brr Brr Patapim
        register(new Skill("Patapim Stomp", SkillType.GRASS, 20, 20, "NONE",
                "Brings an oversized foot down with earth-splitting force.  Power: 20."));
        register(new Skill("Nose Snort", SkillType.ROCK, 15, 15, "PARALYZE",
                "A powerful snort that fires compressed air like a cannonball.  Power: 15 - 30 percent chance to leave the target Paralyzed."));
        register(new Skill("Forest Grunt", SkillType.GRASS, 5, 5, "NONE",
                "A primal battle cry that summons vine traps from the earth.  Power: 5."));

        // Boneca Ambalabu
        register(new Skill("Tire Burnout", SkillType.FIRE, 20, 20, "BURN",
                "Screeches into the target at full throttle, leaving scorched rubber behind.  Power: 20 - 20 percent chance to inflict Burn."));
        register(new Skill("Ribbit Roll", SkillType.ROCK, 15, 15, "RAISE_DEF",
                "Curls into a rolling ball of pure muscle and stone then smashes the target.  Power: 15 - Raises user Defense on hit."));
        register(new Skill("Licking Loop", SkillType.FIRE, 15, 15, "NONE",
                "Relentlessly licks the target in a blazing fire-type frenzy.  Power: 15."));

        // Udin Din Din Din Dun
        register(new Skill("Frequency Pulse", SkillType.PSYCHIC, 20, 20, "NONE",
                "A devastating psychic frequency blast delivered with perfect precision.  Power: 20."));
        register(new Skill("The Din-Stutter", SkillType.PSYCHIC, 15, 15, "LOWER_DEF",
                "A stuttering psi wave that disrupts the target's mental fortitude.  Power: 15 - Lowers the target's Defense."));
        register(new Skill("Brain Scramble", SkillType.PSYCHIC, 15, 15, "NONE",
                "Fires a scrambled signal that rewires the enemy's brain mid-battle.  Power: 15."));

        // Capuccino Assassino
        register(new Skill("Steam Vent", SkillType.POISON, 20, 20, "NONE",
                "Blasts scalding steam from hidden vents with terrifying pressure.  Power: 20."));
        register(new Skill("Metal Frother", SkillType.DARK, 15, 15, "NONE",
                "Bludgeons the target with a steel milk frother in rapid mechanical bursts.  Power: 15."));
        register(new Skill("Double Shot", SkillType.DARK, 10, 10, "NONE",
                "Two rapid espresso-powered Dark strikes fired in quick succession.  Power: 10."));
    }

    public static void register(Skill skill) {
        registry.put(skill.getName().toUpperCase(), skill);
    }

    public static Skill get(String name) {
        Skill skill = registry.get(name.toUpperCase());
        if (skill == null) System.err.println("Skill not found: " + name);
        return skill == null ? null : skill.copy();
    }

    public static boolean has(String name) {
        return registry.containsKey(name.toUpperCase());
    }
}