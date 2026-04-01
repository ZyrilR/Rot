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
                "A warm, lightly seasoned stew. Restores 25% of a BrainRot's max HP.",
                "/assets/Items/Stew/stew_mild.png", 25, 500));

        registerItem(new Stew(
                "MODERATE STEW",
                "A hearty bowl of stew with rich broth. Restores 50% of a BrainRot's max HP.",
                "/assets/Items/Stew/stew_moderate.png", 50, 800));

        registerItem(new Stew(
                "SUPER STEW",
                "An absurdly large pot of premium stew. Fully restores a BrainRot's HP.",
                "/assets/Items/Stew/stew_super.png", 100, 1200));

        // ── Antidotes ─────────────────────────────────────────────────────────
        registerItem(new Antidote(
                "CONFUSION CURE",
                "Snaps a confused BrainRot back to its senses. Clears the CONFUSE status.",
                "/assets/Templates/Items/4.png", "CONFUSE", 600));

        registerItem(new Antidote(
                "PARALYZE CURE",
                "A fast-acting tonic that restores full mobility. Clears the PARALYZE status.",
                "/assets/Templates/Items/4.png", "PARALYZE", 600));

        registerItem(new Antidote(
                "BURN CURE",
                "A cooling salve that soothes searing burns. Clears the BURN status.",
                "/assets/Templates/Items/4.png", "BURN", 600));

        registerItem(new Antidote(
                "DEBUFF TONIC",
                "A restorative brew that resets all lowered stats back to normal.",
                "/assets/Templates/Items/4.png", "DEBUFF", 700));

        // ── Capsules ──────────────────────────────────────────────────────────
        registerItem(new Capsule(
                "RED CAPSULE",
                "A standard capture capsule. Capture rate scales with the target's missing HP.",
                "/assets/Templates/Items/9.png", 500));

        registerItem(new Capsule(
                "BLUE CAPSULE",
                "An improved capsule with a +10% capture bonus over the standard red.",
                "/assets/Templates/Items/9.png", 700));

        registerItem(new Capsule(
                "SPEED CAPSULE",
                "Engineered for fast targets. Grants +20% capture bonus against BrainRots with Speed above 30.",
                "/assets/Templates/Items/9.png", 900));

        registerItem(new Capsule(
                "HEAVY CAPSULE",
                "Built for tough targets. Grants +20% capture bonus against BrainRots with Defense above 40.",
                "/assets/Templates/Items/9.png", 1200));

        registerItem(new Capsule(
                "MASTER CAPSULE",
                "A legendary capsule forged from pure Rot energy. Guarantees capture — no questions asked.",
                "/assets/Templates/Items/9.png", 1500));

        // ── Scrolls — NORMAL pool ─────────────────────────────────────────────
        registerItem(new Scroll(
                "Guard Up Scroll",
                "Teach a BrainRot Guard Up. Raises the user's Defense. Any type can learn this.",
                "/assets/Items/Scroll/scroll_normal.png", "Guard Up", 0));

        registerItem(new Scroll(
                "Focus Stance Scroll",
                "Teach a BrainRot Focus Stance. Sharply raises the user's Attack. Any type.",
                "/assets/Items/Scroll/scroll_normal.png", "Focus Stance", 0));

        registerItem(new Scroll(
                "Quick Step Scroll",
                "Teach a BrainRot Quick Step. Boosts Speed in an instant. Any type.",
                "/assets/Items/Scroll/scroll_normal.png", "Quick Step", 0));

        registerItem(new Scroll(
                "Rest Scroll",
                "Teach a BrainRot Rest. Restores 30% HP by taking a short breather. Any type.",
                "/assets/Items/Scroll/scroll_normal.png", "Rest", 0));

        registerItem(new Scroll(
                "Speed Boost Scroll",
                "Teach a BrainRot Speed Boost. Significantly increases Speed each turn. Any type.",
                "/assets/Items/Scroll/scroll_normal.png", "Speed Boost", 0));

        registerItem(new Scroll(
                "Fortify Scroll",
                "Teach a BrainRot Fortify. Raises Defense with a stalwart stance. Any type.",
                "/assets/Items/Scroll/scroll_normal.png", "Fortify", 0));

        registerItem(new Scroll(
                "Evasion Up Scroll",
                "Teach a BrainRot Evasion Up. A mysterious dodge technique with no direct effect. Any type.",
                "/assets/Items/Scroll/scroll_normal.png", "Evasion Up", 0));

        // ── Scrolls — FIGHTING pool ───────────────────────────────────────────
        registerItem(new Scroll(
                "Wooden Thump Scroll",
                "Teach a BrainRot Wooden Thump. A raw, crushing Fighting-type strike.",
                "/assets/Items/Scroll/scroll_fighting.png", "Wooden Thump", 0));

        registerItem(new Scroll(
                "Power Combo Scroll",
                "Teach a BrainRot Power Combo. A rapid barrage that may cause the target to Flinch.",
                "/assets/Items/Scroll/scroll_fighting.png", "Power Combo", 0));

        registerItem(new Scroll(
                "Counter Guard Scroll",
                "Teach a BrainRot Counter Guard. A defensive Fighting stance — no direct damage.",
                "/assets/Items/Scroll/scroll_fighting.png", "Counter Guard", 0));

        registerItem(new Scroll(
                "Battle Cry Scroll",
                "Teach a BrainRot Battle Cry. An intimidating roar that lowers the target's Attack.",
                "/assets/Items/Scroll/scroll_fighting.png", "Battle Cry", 0));

        // ── Scrolls — WATER pool ──────────────────────────────────────────────
        registerItem(new Scroll(
                "Aqua Dash Scroll",
                "Teach a BrainRot Aqua Dash. A swift Water-type tackle through a wave.",
                "/assets/Items/Scroll/scroll_water.png", "Aqua Dash", 0));

        registerItem(new Scroll(
                "Tidal Crash Scroll",
                "Teach a BrainRot Tidal Crash. A powerful surge that lowers the target's Defense.",
                "/assets/Items/Scroll/scroll_water.png", "Tidal Crash", 0));

        registerItem(new Scroll(
                "Aqua Engine Scroll",
                "Teach a BrainRot Aqua Engine. A jet-propelled Water attack that raises the user's Speed.",
                "/assets/Items/Scroll/scroll_water.png", "Aqua Engine", 0));

        registerItem(new Scroll(
                "Mist Veil Scroll",
                "Teach a BrainRot Mist Veil. Wraps the user in obscuring mist — status only.",
                "/assets/Items/Scroll/scroll_water.png", "Mist Veil", 0));

        // ── Scrolls — PSYCHIC pool ────────────────────────────────────────────
        registerItem(new Scroll(
                "Mind Pulse Scroll",
                "Teach a BrainRot Mind Pulse. A focused psychic burst dealing Psychic-type damage.",
                "/assets/Items/Scroll/scroll_psychic.png", "Mind Pulse", 0));

        registerItem(new Scroll(
                "Rhythm Distort Scroll",
                "Teach a BrainRot Rhythm Distort. Warps the target's sense of rhythm, causing Confusion.",
                "/assets/Items/Scroll/scroll_psychic.png", "Rhythm Distort", 0));

        registerItem(new Scroll(
                "Mental Break Scroll",
                "Teach a BrainRot Mental Break. A shattering psi strike that lowers the target's Defense.",
                "/assets/Items/Scroll/scroll_psychic.png", "Mental Break", 0));

        registerItem(new Scroll(
                "Confusion Wave Scroll",
                "Teach a BrainRot Confusion Wave. A disorienting wave with a high chance to Confuse.",
                "/assets/Items/Scroll/scroll_psychic.png", "Confusion Wave", 0));

        // ── Scrolls — FLYING pool ─────────────────────────────────────────────
        registerItem(new Scroll(
                "Sky Dive Scroll",
                "Teach a BrainRot Sky Dive. Plummets from the sky for heavy Flying-type damage.",
                "/assets/Items/Scroll/scroll_fly.png", "Sky Dive", 0));

        registerItem(new Scroll(
                "Jetstream Slash Scroll",
                "Teach a BrainRot Jetstream Slash. A razor-wind attack that slows the target's Speed.",
                "/assets/Items/Scroll/scroll_fly.png", "Jetstream Slash", 0));

        registerItem(new Scroll(
                "Air Cutter Scroll",
                "Teach a BrainRot Air Cutter. Slices the air into a sharp, accurate Flying attack.",
                "/assets/Items/Scroll/scroll_fly.png", "Air Cutter", 0));

        registerItem(new Scroll(
                "Wind Guard Scroll",
                "Teach a BrainRot Wind Guard. Channels wind into a protective barrier — status move.",
                "/assets/Items/Scroll/scroll_fly.png", "Wind Guard", 0));

        // ── Scrolls — SAND pool ───────────────────────────────────────────────
        registerItem(new Scroll(
                "Prickly Trunk Scroll",
                "Teach a BrainRot Prickly Trunk. Slams with a spiny, sand-crusted limb.",
                "/assets/Items/Scroll/scroll_sand.png", "Prickly Trunk", 0));

        registerItem(new Scroll(
                "Sand Surge Scroll",
                "Teach a BrainRot Sand Surge. Blasts the target with a gritty wave of sand.",
                "/assets/Items/Scroll/scroll_sand.png", "Sand Surge", 0));

        registerItem(new Scroll(
                "Dune Crash Scroll",
                "Teach a BrainRot Dune Crash. A heavy sand-fall attack that may Flinch the target.",
                "/assets/Items/Scroll/scroll_sand.png", "Dune Crash", 0));

        registerItem(new Scroll(
                "Desert Wall Scroll",
                "Teach a BrainRot Desert Wall. Raises Defense by hardening a shell of compressed sand.",
                "/assets/Items/Scroll/scroll_sand.png", "Desert Wall", 0));

        // ── Scrolls — GRASS pool ──────────────────────────────────────────────
        registerItem(new Scroll(
                "Vine Lash Scroll",
                "Teach a BrainRot Vine Lash. Whips the target with razor-sharp vines.",
                "/assets/Items/Scroll/scroll_grass.png", "Vine Lash", 0));

        registerItem(new Scroll(
                "Leaf Strike Scroll",
                "Teach a BrainRot Leaf Strike. A powerful overhead blow with a hardened leaf blade.",
                "/assets/Items/Scroll/scroll_grass.png", "Leaf Strike", 0));

        registerItem(new Scroll(
                "Jungle Fever Scroll",
                "Teach a BrainRot Jungle Fever. A wild battle frenzy that sharply raises Attack.",
                "/assets/Items/Scroll/scroll_grass.png", "Jungle Fever", 0));

        registerItem(new Scroll(
                "Root Bind Scroll",
                "Teach a BrainRot Root Bind. Entangles the target in fast-growing roots.",
                "/assets/Items/Scroll/scroll_grass.png", "Root Bind", 0));

        // ── Scrolls — ROCK pool ───────────────────────────────────────────────
        registerItem(new Scroll(
                "Rock Slam Scroll",
                "Teach a BrainRot Rock Slam. Hurls a massive boulder that may cause Flinch.",
                "/assets/Items/Scroll/scroll_stone.png", "Rock Slam", 0));

        registerItem(new Scroll(
                "Stone Guard Scroll",
                "Teach a BrainRot Stone Guard. Wraps the body in stone plating, raising Defense.",
                "/assets/Items/Scroll/scroll_stone.png", "Stone Guard", 0));

        registerItem(new Scroll(
                "Boulder Crash Scroll",
                "Teach a BrainRot Boulder Crash. Charges forward with a granite-coated body.",
                "/assets/Items/Scroll/scroll_stone.png", "Boulder Crash", 0));

        registerItem(new Scroll(
                "Defense Curl Scroll",
                "Teach a BrainRot Defense Curl. Curls into an impenetrable rocky ball, raising Defense.",
                "/assets/Items/Scroll/scroll_stone.png", "Defense Curl", 0));

        // ── Scrolls — FIRE pool ───────────────────────────────────────────────
        registerItem(new Scroll(
                "Flame Crash Scroll",
                "Teach a BrainRot Flame Crash. A blazing slam with a high chance to Burn the target.",
                "/assets/Items/Scroll/scroll_fire.png", "Flame Crash", 0));

        registerItem(new Scroll(
                "Heat Burst Scroll",
                "Teach a BrainRot Heat Burst. Releases a sudden wave of scorching heat.",
                "/assets/Items/Scroll/scroll_fire.png", "Heat Burst", 0));

        registerItem(new Scroll(
                "Inferno Dash Scroll",
                "Teach a BrainRot Inferno Dash. A flaming charge that leaves a trail of Burn.",
                "/assets/Items/Scroll/scroll_fire.png", "Inferno Dash", 0));

        registerItem(new Scroll(
                "Burn Rush Scroll",
                "Teach a BrainRot Burn Rush. Charges through the enemy, spreading flames and Burning on contact.",
                "/assets/Items/Scroll/scroll_fire.png", "Burn Rush", 0));

        // ── Scrolls — DARK pool ───────────────────────────────────────────────
        registerItem(new Scroll(
                "Shadow Jab Scroll",
                "Teach a BrainRot Shadow Jab. A quick, deceptive strike from the shadows.",
                "/assets/Items/Scroll/scroll_dark.png", "Shadow Jab", 0));

        registerItem(new Scroll(
                "Night Slash Scroll",
                "Teach a BrainRot Night Slash. A precise dark-type slash that lowers the target's Attack.",
                "/assets/Items/Scroll/scroll_dark.png", "Night Slash", 0));

        registerItem(new Scroll(
                "Fear Stare Scroll",
                "Teach a BrainRot Fear Stare. A terrifying gaze that slows the target's Speed.",
                "/assets/Items/Scroll/scroll_dark.png", "Fear Stare", 0));

        registerItem(new Scroll(
                "Ambush Scroll",
                "Teach a BrainRot Ambush. Strikes from a concealed position for pure Dark-type damage.",
                "/assets/Items/Scroll/scroll_dark.png", "Ambush", 0));

        // ── Scrolls — POISON pool ─────────────────────────────────────────────
        registerItem(new Scroll(
                "Toxic Steam Scroll",
                "Teach a BrainRot Toxic Steam. Releases a cloud of poisonous vapour.",
                "/assets/Items/Scroll/scroll_poison.png", "Toxic Steam", 0));

        registerItem(new Scroll(
                "Venom Drip Scroll",
                "Teach a BrainRot Venom Drip. Drips concentrated venom onto the target.",
                "/assets/Items/Scroll/scroll_poison.png", "Venom Drip", 0));

        registerItem(new Scroll(
                "Acid Spray Scroll",
                "Teach a BrainRot Acid Spray. Corrodes the target's armour, lowering Defense.",
                "/assets/Items/Scroll/scroll_poison.png", "Acid Spray", 0));

        registerItem(new Scroll(
                "Poison Fang Scroll",
                "Teach a BrainRot Poison Fang. Bites with venom-soaked fangs.",
                "/assets/Items/Scroll/scroll_poison.png", "Poison Fang", 0));

        // ── Signature scrolls — TUNG TUNG TUNG SAHUR ──────────────────────────
        registerItem(new Scroll(
                "Sahur Chant Scroll",
                "Signature scroll for Tung Tung Tung Sahur. The ancient Sahur chant saps the target's Speed.",
                "/assets/Items/Scroll/scroll_fighting.png", "Sahur Chant", 0));

        registerItem(new Scroll(
                "Wake-Up Call Scroll",
                "Signature scroll for Tung Tung Tung Sahur. A thunderous wake-up slam — no mercy.",
                "/assets/Items/Scroll/scroll_fighting.png", "Wake-Up Call", 0));

        registerItem(new Scroll(
                "Infinite Sahur Scroll",
                "Signature scroll for Tung Tung Tung Sahur. An unrelenting endless chant that Paralyzes the target.",
                "/assets/Items/Scroll/scroll_fighting.png", "Infinite Sahur", 0));

        // ── Signature scrolls — TRALALERO TRALALA ─────────────────────────────
        registerItem(new Scroll(
                "Sneaker Dash Scroll",
                "Signature scroll for Tralalero Tralala. A hypersonic Water-type sprint.",
                "/assets/Items/Scroll/scroll_water.png", "Sneaker Dash", 0));

        registerItem(new Scroll(
                "Glitchy Rhythm Scroll",
                "Signature scroll for Tralalero Tralala. A corrupted beat that Confuses the target.",
                "/assets/Items/Scroll/scroll_psychic.png", "Glitchy Rhythm", 0));

        registerItem(new Scroll(
                "Neon Rave Scroll",
                "Signature scroll for Tralalero Tralala. An overwhelming psychedelic burst of Psychic energy.",
                "/assets/Items/Scroll/scroll_psychic.png", "Neon Rave", 0));

        // ── Signature scrolls — BOMBARDINO CROCODILO ──────────────────────────
        registerItem(new Scroll(
                "Caffeine Snap Scroll",
                "Signature scroll for Bombardino Crocodilo. A hyper-caffeinated Water lunge that raises Speed.",
                "/assets/Items/Scroll/scroll_water.png", "Caffeine Snap", 0));

        registerItem(new Scroll(
                "Tail Missile Scroll",
                "Signature scroll for Bombardino Crocodilo. Launches a tail like a missile, lowering Defense.",
                "/assets/Items/Scroll/scroll_fly.png", "Tail Missile", 0));

        registerItem(new Scroll(
                "Ristretto Nuke Scroll",
                "Signature scroll for Bombardino Crocodilo. A condensed espresso-powered Fire explosion. Burns.",
                "/assets/Items/Scroll/scroll_fire.png", "Ristretto Nuke", 0));

        // ── Signature scrolls — LIRILI LARILA ─────────────────────────────────
        registerItem(new Scroll(
                "Sandal Stomp Scroll",
                "Signature scroll for Lirili Larila. Stomps with a sandal for Sand-type damage. May Flinch.",
                "/assets/Items/Scroll/scroll_sand.png", "Sandal Stomp", 0));

        registerItem(new Scroll(
                "Clock Rewind Scroll",
                "Signature scroll for Lirili Larila. Rewinds time to recover HP.",
                "/assets/Items/Scroll/scroll_sand.png", "Clock Rewind", 0));

        registerItem(new Scroll(
                "Timeline Rot Scroll",
                "Signature scroll for Lirili Larila. Corrupts the target's timeline — high power, chaotic effect.",
                "/assets/Items/Scroll/scroll_sand.png", "Timeline Rot", 0));

        // ── Signature scrolls — BRR BRR PATAPIM ───────────────────────────────
        registerItem(new Scroll(
                "Patapim Stomp Scroll",
                "Signature scroll for Brr Brr Patapim. A thunderous Grass-type ground stomp.",
                "/assets/Items/Scroll/scroll_grass.png", "Patapim Stomp", 0));

        registerItem(new Scroll(
                "Nose Snort Scroll",
                "Signature scroll for Brr Brr Patapim. A powerful Rock-type snort that may Paralyze.",
                "/assets/Items/Scroll/scroll_stone.png", "Nose Snort", 0));

        registerItem(new Scroll(
                "Forest Grunt Scroll",
                "Signature scroll for Brr Brr Patapim. A primal Grass-type battle cry of sheer power.",
                "/assets/Items/Scroll/scroll_grass.png", "Forest Grunt", 0));

        // ── Signature scrolls — BONECA AMBALABU ───────────────────────────────
        registerItem(new Scroll(
                "Tire Burnout Scroll",
                "Signature scroll for Boneca Ambalabu. Screeches into the target leaving burning tyre marks.",
                "/assets/Items/Scroll/scroll_fire.png", "Tire Burnout", 0));

        registerItem(new Scroll(
                "Ribbit Roll Scroll",
                "Signature scroll for Boneca Ambalabu. A rolling Rock-type tackle that raises user Defense.",
                "/assets/Items/Scroll/scroll_stone.png", "Ribbit Roll", 0));

        registerItem(new Scroll(
                "Licking Loop Scroll",
                "Signature scroll for Boneca Ambalabu. Relentlessly licks the target in a Fire-type frenzy.",
                "/assets/Items/Scroll/scroll_fire.png", "Licking Loop", 0));

        // ── Signature scrolls — UDIN DIN DIN DIN DUN ──────────────────────────
        registerItem(new Scroll(
                "Frequency Pulse Scroll",
                "Signature scroll for Udin Din Din Din Dun. A devastating Psychic frequency blast.",
                "/assets/Items/Scroll/scroll_psychic.png", "Frequency Pulse", 0));

        registerItem(new Scroll(
                "The Din-Stutter Scroll",
                "Signature scroll for Udin Din Din Din Dun. A stuttering psi wave that lowers Defense.",
                "/assets/Items/Scroll/scroll_psychic.png", "The Din-Stutter", 0));

        registerItem(new Scroll(
                "Brain Scramble Scroll",
                "Signature scroll for Udin Din Din Din Dun. Scrambles the target's brainwaves with Psychic energy.",
                "/assets/Items/Scroll/scroll_psychic.png", "Brain Scramble", 0));

        // ── Signature scrolls — CAPUCCINO ASSASSINO ───────────────────────────
        registerItem(new Scroll(
                "Steam Vent Scroll",
                "Signature scroll for Capuccino Assassino. Blasts scalding steam for heavy Poison damage.",
                "/assets/Items/Scroll/scroll_poison.png", "Steam Vent", 0));

        registerItem(new Scroll(
                "Metal Frother Scroll",
                "Signature scroll for Capuccino Assassino. Bludgeons with a steel milk frother — Dark type.",
                "/assets/Items/Scroll/scroll_dark.png", "Metal Frother", 0));

        registerItem(new Scroll(
                "Double Shot Scroll",
                "Signature scroll for Capuccino Assassino. Two rapid Dark-type espresso shots fired in quick succession.",
                "/assets/Items/Scroll/scroll_dark.png", "Double Shot", 0));
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