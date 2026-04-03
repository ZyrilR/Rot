package items;

import java.util.ArrayList;

/**
 * Central registry for all purchasable / obtainable items.
 *
 * Scroll asset paths map to /assets/Items/Scroll/scroll_<type>.png
 * Stew  asset paths map to /assets/Items/Stew/stew*.png
 * Antidote asset paths use /assets/Templates/Items/4.png (temp)
 * Capsule  asset paths use /assets/Templates/Items/9.png (temp)
 */
public class ItemRegistry {

    private static final java.util.HashMap<String, Item> registry = new java.util.HashMap<>();

    static {

        // ── Stews (Potions) ───────────────────────────────────────────────────
        registerItem(new Stew(
                "MILD STEW",
                "A warm, lightly seasoned stew. Restores 25 percent of a BrainRot's max HP.",
                "/res/Items/Stew/stew_mild.png", 25, 500));

        registerItem(new Stew(
                "MODERATE STEW",
                "A hearty bowl of stew with rich broth. Restores 50 percent of a BrainRot's max HP.",
                "/res/Items/Stew/stew_moderate.png", 50, 800));

        registerItem(new Stew(
                "SUPER STEW",
                "An absurdly large pot of premium stew. Fully restores a BrainRot's HP.",
                "/res/Items/Stew/stew_super.png", 100, 1200));

        // ── Antidotes ─────────────────────────────────────────────────────────
        registerItem(new Antidote(
                "CONFUSION CURE",
                "Snaps a confused BrainRot back to its senses. Clears the CONFUSE status.",
                "/res/Templates/Items/4.png", "CONFUSE", 600));

        registerItem(new Antidote(
                "PARALYZE CURE",
                "A fast-acting tonic that restores full mobility. Clears the PARALYZE status.",
                "/res/Templates/Items/4.png", "PARALYZE", 600));

        registerItem(new Antidote(
                "BURN CURE",
                "A cooling salve that soothes searing burns. Clears the BURN status.",
                "/res/Templates/Items/4.png", "BURN", 600));

        registerItem(new Antidote(
                "DEBUFF TONIC",
                "A restorative brew that resets all lowered stats back to normal.",
                "/res/Templates/Items/4.png", "DEBUFF", 700));

        // ── Capsules ──────────────────────────────────────────────────────────
        registerItem(new Capsule(
                "RED CAPSULE",
                "A standard capture capsule. Capture rate scales with the target's missing HP.",
                "/res/Templates/Items/9.png", 500));

        registerItem(new Capsule(
                "BLUE CAPSULE",
                "An improved capsule with a +10 percent capture bonus over the standard red.",
                "/res/Templates/Items/9.png", 700));

        registerItem(new Capsule(
                "SPEED CAPSULE",
                "Engineered for fast targets. Grants +20 percent capture bonus against BrainRots with Speed above 30.",
                "/res/Templates/Items/9.png", 900));

        registerItem(new Capsule(
                "HEAVY CAPSULE",
                "Built for tough targets. Grants +20 percent capture bonus against BrainRots with Defense above 40.",
                "/res/Templates/Items/9.png", 1200));

        registerItem(new Capsule(
                "MASTER CAPSULE",
                "A legendary capsule forged from pure Rot energy. Guarantees capture, no questions asked.",
                "/res/Templates/Items/9.png", 1500));

        registerItem(new Scroll(
                "GUARD UP SCROLL",
                "Teach a BrainRot Guard Up. Raises the user's Defense.",
                "/res/Items/Scroll/scroll_normal.png", "Guard Up", 0));

        registerItem(new Scroll(
                "FOCUS STANCE SCROLL",
                "Teach a BrainRot Focus Stance. Sharply raises the user's Attack. ",
                "/res/Items/Scroll/scroll_normal.png", "Focus Stance", 0));

        registerItem(new Scroll(
                "QUICK STEP SCROLL",
                "Teach a BrainRot Quick Step. Boosts Speed in an instant. ",
                "/res/Items/Scroll/scroll_normal.png", "Quick Step", 0));

        registerItem(new Scroll(
                "REST SCROLL",
                "Teach a BrainRot Rest. Restores 30 percent HP by taking a short breather.",
                "/res/Items/Scroll/scroll_normal.png", "Rest", 0));

        registerItem(new Scroll(
                "SPEED BOOST SCROLL",
                "Teach a BrainRot Speed Boost. Significantly increases Speed each turn.",
                "/res/Items/Scroll/scroll_normal.png", "Speed Boost", 0));

        registerItem(new Scroll(
                "FORTIFY SCROLL",
                "Teach a BrainRot Fortify. Raises Defense with a stalwart stance.",
                "/res/Items/Scroll/scroll_normal.png", "Fortify", 0));

        registerItem(new Scroll(
                "EVASION UP SCROLL",
                "Teach a BrainRot Evasion Up. A mysterious dodge technique with no direct effect.",
                "/res/Items/Scroll/scroll_normal.png", "Evasion Up", 0));

// ── Scrolls - FIGHTING pool ───────────────────────────────────────────
        registerItem(new Scroll(
                "WOODEN THUMP SCROLL",
                "Teach a BrainRot Wooden Thump. A raw, crushing Fighting-type strike.",
                "/res/Items/Scroll/scroll_fighting.png", "Wooden Thump", 0));

        registerItem(new Scroll(
                "POWER COMBO SCROLL",
                "Teach a BrainRot Power Combo. A rapid barrage that may cause the target to Flinch.",
                "/res/Items/Scroll/scroll_fighting.png", "Power Combo", 0));

        registerItem(new Scroll(
                "COUNTER GUARD SCROLL",
                "Teach a BrainRot Counter Guard. A defensive Fighting stance - no direct damage.",
                "/res/Items/Scroll/scroll_fighting.png", "Counter Guard", 0));

        registerItem(new Scroll(
                "BATTLE CRY SCROLL",
                "Teach a BrainRot Battle Cry. An intimidating roar that lowers the target's Attack.",
                "/res/Items/Scroll/scroll_fighting.png", "Battle Cry", 0));

// ── Scrolls - WATER pool ──────────────────────────────────────────────
        registerItem(new Scroll(
                "AQUA DASH SCROLL",
                "Teach a BrainRot Aqua Dash. A swift Water-type tackle through a wave.",
                "/res/Items/Scroll/scroll_water.png", "Aqua Dash", 0));

        registerItem(new Scroll(
                "TIDAL CRASH SCROLL",
                "Teach a BrainRot Tidal Crash. A powerful surge that lowers the target's Defense.",
                "/res/Items/Scroll/scroll_water.png", "Tidal Crash", 0));

        registerItem(new Scroll(
                "AQUA ENGINE SCROLL",
                "Teach a BrainRot Aqua Engine. A jet-propelled Water attack that raises the user's Speed.",
                "/res/Items/Scroll/scroll_water.png", "Aqua Engine", 0));

        registerItem(new Scroll(
                "MIST VEIL SCROLL",
                "Teach a BrainRot Mist Veil. Wraps the user in obscuring mist - status only.",
                "/res/Items/Scroll/scroll_water.png", "Mist Veil", 0));

// ── Scrolls - PSYCHIC pool ────────────────────────────────────────────
        registerItem(new Scroll(
                "MIND PULSE SCROLL",
                "Teach a BrainRot Mind Pulse. A focused psychic burst dealing Psychic-type damage.",
                "/res/Items/Scroll/scroll_psychic.png", "Mind Pulse", 0));

        registerItem(new Scroll(
                "RHYTHM DISTORT SCROLL",
                "Teach a BrainRot Rhythm Distort. Warps the target's sense of rhythm, causing Confusion.",
                "/res/Items/Scroll/scroll_psychic.png", "Rhythm Distort", 0));

        registerItem(new Scroll(
                "MENTAL BREAK SCROLL",
                "Teach a BrainRot Mental Break. A shattering psi strike that lowers the target's Defense.",
                "/res/Items/Scroll/scroll_psychic.png", "Mental Break", 0));

        registerItem(new Scroll(
                "CONFUSION WAVE SCROLL",
                "Teach a BrainRot Confusion Wave. A disorienting wave with a high chance to Confuse.",
                "/res/Items/Scroll/scroll_psychic.png", "Confusion Wave", 0));

// ── Scrolls - FLYING pool ─────────────────────────────────────────────
        registerItem(new Scroll(
                "SKY DIVE SCROLL",
                "Teach a BrainRot Sky Dive. Plummets from the sky for heavy Flying-type damage.",
                "/res/Items/Scroll/scroll_fly.png", "Sky Dive", 0));

        registerItem(new Scroll(
                "JETSTREAM SLASH SCROLL",
                "Teach a BrainRot Jetstream Slash. A razor-wind attack that slows the target's Speed.",
                "/res/Items/Scroll/scroll_fly.png", "Jetstream Slash", 0));

        registerItem(new Scroll(
                "AIR CUTTER SCROLL",
                "Teach a BrainRot Air Cutter. Slices the air into a sharp, accurate Flying attack.",
                "/res/Items/Scroll/scroll_fly.png", "Air Cutter", 0));

        registerItem(new Scroll(
                "WIND GUARD SCROLL",
                "Teach a BrainRot Wind Guard. Channels wind into a protective barrier - status move.",
                "/res/Items/Scroll/scroll_fly.png", "Wind Guard", 0));

// ── Scrolls - SAND pool ───────────────────────────────────────────────
        registerItem(new Scroll(
                "PRICKLY TRUNK SCROLL",
                "Teach a BrainRot Prickly Trunk. Slams with a spiny, sand-crusted limb.",
                "/res/Items/Scroll/scroll_sand.png", "Prickly Trunk", 0));

        registerItem(new Scroll(
                "SAND SURGE SCROLL",
                "Teach a BrainRot Sand Surge. Blasts the target with a gritty wave of sand.",
                "/res/Items/Scroll/scroll_sand.png", "Sand Surge", 0));

        registerItem(new Scroll(
                "DUNE CRASH SCROLL",
                "Teach a BrainRot Dune Crash. A heavy sand-fall attack that may Flinch the target.",
                "/res/Items/Scroll/scroll_sand.png", "Dune Crash", 0));

        registerItem(new Scroll(
                "DESERT WALL SCROLL",
                "Teach a BrainRot Desert Wall. Raises Defense by hardening a shell of compressed sand.",
                "/res/Items/Scroll/scroll_sand.png", "Desert Wall", 0));

// ── Scrolls - GRASS pool ──────────────────────────────────────────────
        registerItem(new Scroll(
                "VINE LASH SCROLL",
                "Teach a BrainRot Vine Lash. Whips the target with razor-sharp vines.",
                "/res/Items/Scroll/scroll_grass.png", "Vine Lash", 0));

        registerItem(new Scroll(
                "LEAF STRIKE SCROLL",
                "Teach a BrainRot Leaf Strike. A powerful overhead blow with a hardened leaf blade.",
                "/res/Items/Scroll/scroll_grass.png", "Leaf Strike", 0));

        registerItem(new Scroll(
                "JUNGLE FEVER SCROLL",
                "Teach a BrainRot Jungle Fever. A wild battle frenzy that sharply raises Attack.",
                "/res/Items/Scroll/scroll_grass.png", "Jungle Fever", 0));

        registerItem(new Scroll(
                "ROOT BIND SCROLL",
                "Teach a BrainRot Root Bind. Entangles the target in fast-growing roots.",
                "/res/Items/Scroll/scroll_grass.png", "Root Bind", 0));

// ── Scrolls - ROCK pool ───────────────────────────────────────────────
        registerItem(new Scroll(
                "ROCK SLAM SCROLL",
                "Teach a BrainRot Rock Slam. Hurls a massive boulder that may cause Flinch.",
                "/res/Items/Scroll/scroll_stone.png", "Rock Slam", 0));

        registerItem(new Scroll(
                "STONE GUARD SCROLL",
                "Teach a BrainRot Stone Guard. Wraps the body in stone plating, raising Defense.",
                "/res/Items/Scroll/scroll_stone.png", "Stone Guard", 0));

        registerItem(new Scroll(
                "BOULDER CRASH SCROLL",
                "Teach a BrainRot Boulder Crash. Charges forward with a granite-coated body.",
                "/res/Items/Scroll/scroll_stone.png", "Boulder Crash", 0));

        registerItem(new Scroll(
                "DEFENSE CURL SCROLL",
                "Teach a BrainRot Defense Curl. Curls into an impenetrable rocky ball, raising Defense.",
                "/res/Items/Scroll/scroll_stone.png", "Defense Curl", 0));

// ── Scrolls - FIRE pool ───────────────────────────────────────────────
        registerItem(new Scroll(
                "FLAME CRASH SCROLL",
                "Teach a BrainRot Flame Crash. A blazing slam with a high chance to Burn the target.",
                "/res/Items/Scroll/scroll_fire.png", "Flame Crash", 0));

        registerItem(new Scroll(
                "HEAT BURST SCROLL",
                "Teach a BrainRot Heat Burst. Releases a sudden wave of scorching heat.",
                "/res/Items/Scroll/scroll_fire.png", "Heat Burst", 0));

        registerItem(new Scroll(
                "INFERNO DASH SCROLL",
                "Teach a BrainRot Inferno Dash. A flaming charge that leaves a trail of Burn.",
                "/res/Items/Scroll/scroll_fire.png", "Inferno Dash", 0));

        registerItem(new Scroll(
                "BURN RUSH SCROLL",
                "Teach a BrainRot Burn Rush. Charges through the enemy, spreading flames and Burning on contact.",
                "/res/Items/Scroll/scroll_fire.png", "Burn Rush", 0));

// ── Scrolls - DARK pool ───────────────────────────────────────────────
        registerItem(new Scroll(
                "SHADOW JAB SCROLL",
                "Teach a BrainRot Shadow Jab. A quick, deceptive strike from the shadows.",
                "/res/Items/Scroll/scroll_dark.png", "Shadow Jab", 0));

        registerItem(new Scroll(
                "NIGHT SLASH SCROLL",
                "Teach a BrainRot Night Slash. A precise dark-type slash that lowers the target's Attack.",
                "/res/Items/Scroll/scroll_dark.png", "Night Slash", 0));

        registerItem(new Scroll(
                "FEAR STARE SCROLL",
                "Teach a BrainRot Fear Stare. A terrifying gaze that slows the target's Speed.",
                "/res/Items/Scroll/scroll_dark.png", "Fear Stare", 0));

        registerItem(new Scroll(
                "AMBUSH SCROLL",
                "Teach a BrainRot Ambush. Strikes from a concealed position for pure Dark-type damage.",
                "/res/Items/Scroll/scroll_dark.png", "Ambush", 0));

// ── Scrolls - POISON pool ─────────────────────────────────────────────
        registerItem(new Scroll(
                "TOXIC STEAM SCROLL",
                "Teach a BrainRot Toxic Steam. Releases a cloud of poisonous vapour.",
                "/res/Items/Scroll/scroll_poison.png", "Toxic Steam", 0));

        registerItem(new Scroll(
                "VENOM DRIP SCROLL",
                "Teach a BrainRot Venom Drip. Drips concentrated venom onto the target.",
                "/res/Items/Scroll/scroll_poison.png", "Venom Drip", 0));

        registerItem(new Scroll(
                "ACID SPRAY SCROLL",
                "Teach a BrainRot Acid Spray. Corrodes the target's armour, lowering Defense.",
                "/res/Items/Scroll/scroll_poison.png", "Acid Spray", 0));

        registerItem(new Scroll(
                "POISON FANG SCROLL",
                "Teach a BrainRot Poison Fang. Bites with venom-soaked fangs.",
                "/res/Items/Scroll/scroll_poison.png", "Poison Fang", 0));

// ── Signature scrolls - TUNG TUNG TUNG SAHUR ──────────────────────────
        registerItem(new Scroll(
                "SAHUR CHANT SCROLL",
                "Signature scroll for Tung Tung Tung Sahur. The ancient Sahur chant saps the target's Speed.",
                "/res/Items/Scroll/scroll_fighting.png", "Sahur Chant", 0));

        registerItem(new Scroll(
                "WAKE-UP CALL SCROLL",
                "Signature scroll for Tung Tung Tung Sahur. A thunderous wake-up slam - no mercy.",
                "/res/Items/Scroll/scroll_fighting.png", "Wake-Up Call", 0));

        registerItem(new Scroll(
                "INFINITE SAHUR SCROLL",
                "Signature scroll for Tung Tung Tung Sahur. An unrelenting endless chant that Paralyzes the target.",
                "/res/Items/Scroll/scroll_fighting.png", "Infinite Sahur", 0));

// ── Signature scrolls - TRALALERO TRALALA ─────────────────────────────
        registerItem(new Scroll(
                "SNEAKER DASH SCROLL",
                "Signature scroll for Tralalero Tralala. A hypersonic Water-type sprint.",
                "/res/Items/Scroll/scroll_water.png", "Sneaker Dash", 0));

        registerItem(new Scroll(
                "GLITCHY RHYTHM SCROLL",
                "Signature scroll for Tralalero Tralala. A corrupted beat that Confuses the target.",
                "/res/Items/Scroll/scroll_psychic.png", "Glitchy Rhythm", 0));

        registerItem(new Scroll(
                "NEON RAVE SCROLL",
                "Signature scroll for Tralalero Tralala. An overwhelming psychedelic burst of Psychic energy.",
                "/res/Items/Scroll/scroll_psychic.png", "Neon Rave", 0));

// ── Signature scrolls - BOMBARDINO CROCODILO ──────────────────────────
        registerItem(new Scroll(
                "CAFFEINE SNAP SCROLL",
                "Signature scroll for Bombardino Crocodilo. A hyper-caffeinated Water lunge that raises Speed.",
                "/res/Items/Scroll/scroll_water.png", "Caffeine Snap", 0));

        registerItem(new Scroll(
                "TAIL MISSILE SCROLL",
                "Signature scroll for Bombardino Crocodilo. Launches a tail like a missile, lowering Defense.",
                "/res/Items/Scroll/scroll_fly.png", "Tail Missile", 0));

        registerItem(new Scroll(
                "RISTRETTO NUKE SCROLL",
                "Signature scroll for Bombardino Crocodilo. A condensed espresso-powered Fire explosion. Burns.",
                "/res/Items/Scroll/scroll_fire.png", "Ristretto Nuke", 0));

// ── Signature scrolls - LIRILI LARILA ─────────────────────────────────
        registerItem(new Scroll(
                "SANDAL STOMP SCROLL",
                "Signature scroll for Lirili Larila. Stomps with a sandal for Sand-type damage. May Flinch.",
                "/res/Items/Scroll/scroll_sand.png", "Sandal Stomp", 0));

        registerItem(new Scroll(
                "CLOCK REWIND SCROLL",
                "Signature scroll for Lirili Larila. Rewinds time to recover HP.",
                "/res/Items/Scroll/scroll_sand.png", "Clock Rewind", 0));

        registerItem(new Scroll(
                "TIMELINE ROT SCROLL",
                "Signature scroll for Lirili Larila. Corrupts the target's timeline - high power, chaotic effect.",
                "/res/Items/Scroll/scroll_sand.png", "Timeline Rot", 0));

// ── Signature scrolls - BRR BRR PATAPIM ───────────────────────────────
        registerItem(new Scroll(
                "PATAPIM STOMP SCROLL",
                "Signature scroll for Brr Brr Patapim. A thunderous Grass-type ground stomp.",
                "/res/Items/Scroll/scroll_grass.png", "Patapim Stomp", 0));

        registerItem(new Scroll(
                "NOSE SNORT SCROLL",
                "Signature scroll for Brr Brr Patapim. A powerful Rock-type snort that may Paralyze.",
                "/res/Items/Scroll/scroll_stone.png", "Nose Snort", 0));

        registerItem(new Scroll(
                "FOREST GRUNT SCROLL",
                "Signature scroll for Brr Brr Patapim. A primal Grass-type battle cry of sheer power.",
                "/res/Items/Scroll/scroll_grass.png", "Forest Grunt", 0));

// ── Signature scrolls - BONECA AMBALABU ───────────────────────────────
        registerItem(new Scroll(
                "TIRE BURNOUT SCROLL",
                "Signature scroll for Boneca Ambalabu. Screeches into the target leaving burning tyre marks.",
                "/res/Items/Scroll/scroll_fire.png", "Tire Burnout", 0));

        registerItem(new Scroll(
                "RIBBIT ROLL SCROLL",
                "Signature scroll for Boneca Ambalabu. A rolling Rock-type tackle that raises user Defense.",
                "/res/Items/Scroll/scroll_stone.png", "Ribbit Roll", 0));

        registerItem(new Scroll(
                "LICKING LOOP SCROLL",
                "Signature scroll for Boneca Ambalabu. Relentlessly licks the target in a Fire-type frenzy.",
                "/res/Items/Scroll/scroll_fire.png", "Licking Loop", 0));

// ── Signature scrolls - UDIN DIN DIN DIN DUN ──────────────────────────
        registerItem(new Scroll(
                "FREQUENCY PULSE SCROLL",
                "Signature scroll for Udin Din Din Din Dun. A devastating Psychic frequency blast.",
                "/res/Items/Scroll/scroll_psychic.png", "Frequency Pulse", 0));

        registerItem(new Scroll(
                "THE DIN-STUTTER SCROLL",
                "Signature scroll for Udin Din Din Din Dun. A stuttering psi wave that lowers Defense.",
                "/res/Items/Scroll/scroll_psychic.png", "The Din-Stutter", 0));

        registerItem(new Scroll(
                "BRAIN SCRAMBLE SCROLL",
                "Signature scroll for Udin Din Din Din Dun. Scrambles the target's brainwaves with Psychic energy.",
                "/res/Items/Scroll/scroll_psychic.png", "Brain Scramble", 0));

// ── Signature scrolls - CAPUCCINO ASSASSINO ───────────────────────────
        registerItem(new Scroll(
                "STEAM VENT SCROLL",
                "Signature scroll for Capuccino Assassino. Blasts scalding steam for heavy Poison damage.",
                "/res/Items/Scroll/scroll_poison.png", "Steam Vent", 0));

        registerItem(new Scroll(
                "METAL FROTHER SCROLL",
                "Signature scroll for Capuccino Assassino. Bludgeons with a steel milk frother - Dark type.",
                "/res/Items/Scroll/scroll_dark.png", "Metal Frother", 0));

        registerItem(new Scroll(
                "DOUBLE SHOT SCROLL",
                "Signature scroll for Capuccino Assassino. Two rapid Dark-type espresso shots fired in quick succession.",
                "/res/Items/Scroll/scroll_dark.png", "Double Shot", 0));
    }

    // ── Registry API ──────────────────────────────────────────────────────────

    public static void registerItem(Item item) {
        registry.put(item.getName().toUpperCase(), item);
    }

    public static Item getItem(String name) {
        Item item = registry.get(name.toUpperCase());
        if (item == null) System.out.println("[ItemRegistry] Item not found: " + name);
        return item;
    }

    public static ArrayList<Item> getAllItems() {
        return new ArrayList<>(registry.values());
    }

}