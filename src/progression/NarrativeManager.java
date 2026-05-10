package progression;

import engine.GamePanel;
import java.util.ArrayList;
import java.util.Arrays;

public class NarrativeManager {
    public static boolean pendingWorldIntro = false;

    public static void checkTriggers(GamePanel gp) {
        // Wait until we are in the "play" state AND the screen is fully faded in
        if (pendingWorldIntro && gp.GAMESTATE.equalsIgnoreCase("play")) {
            if (gp.BLACKFADEEFFECT.isFadeOutComplete()) {
                pendingWorldIntro = false;
                playWorldIntroSequence(gp);
            }
        }
    }

    /**
     * The 4:00 AM Awakening Sequence.
     * This establishes the RotVerse lore and introduces Tung Tung Tung Sahur.
     */
    public static void playIntroSequence(GamePanel gp) {
        ArrayList<String> speakers = new ArrayList<>();
        ArrayList<String> lines = new ArrayList<>();

        // FIX: Exactly 7 speakers for 7 lines to prevent out-of-bounds error!
        speakers.addAll(Arrays.asList(
                "System",
                "Tung Tung Tung Sahur",
                gp.player.name,
                "Tung Tung Tung Sahur",
                gp.player.name,
                "Tung Tung Tung Sahur",
                "System" // For the narrator action at the end
        ));

        lines.addAll(Arrays.asList(
                "4:00 AM. Route 130.\nA rhythmic thumping echoes outside your window.",
                "Tung. Tung. TUNG.",
                "...it is literally four in the morning, bro.",
                "TUNG TUNG TUNG.",
                "Okay. Okay. I'm up. What is happening to this world?",
                "...tung.",
                "[The log nods toward the horizon, where the sunrise looks slightly wrong]"
        ));

        // Start the PvZ Style Cutscene
        gp.DIALOGUEBOX.startCutscene(speakers, lines);

        // Trigger the Cinematic Fade-In explicitly
        gp.DIALOGUEBOX.setOnFinish(() -> {
            gp.GAMESTATE = "starter";
            gp.STARTERUI.open();
        });
    }

    public static void playWorldIntroSequence(GamePanel gp) {
        ArrayList<String> speakers = new ArrayList<>();
        ArrayList<String> lines = new ArrayList<>();

        // Exactly 3 speakers for the 3 paragraphs
        for (int i = 0; i < 4; i++) speakers.add("ROT");

        lines.addAll(Arrays.asList(
                "Welcome to RotVerse. A sprawling, sun-baked coastal world where the internet bled into reality and never left. Somewhere between the green plains of Route 131, and the depths of Cave 131...",
                "the laws of nature gave up and went home. Animals fused with fighter jets. Espresso machines grew legs. A log decided it was people. Nobody questioned it. This landscape is built on vibes, memes, and the absurdity of a civilization that spent too much",
                "time online. The world is divided into Routes, Rooms, and Caves. Five Gym Leaders preside over Water, Flying, Fighting, Rock, and Sand. Each Leader represents a flavor of the Brainrot. Above them all sits the Gym Master",
                "the supreme authority of Rot who awaits challengers in a fortress so unhinged it has multiple floors. Good luck."
        ));

        // Turn on the centered cinematic box!
        gp.DIALOGUEBOX.cinematicMode = true;
        gp.DIALOGUEBOX.startCutscene(speakers, lines);

        // Give control back to the player when done, and turn OFF cinematic mode
        gp.DIALOGUEBOX.setOnFinish(() -> {
            gp.DIALOGUEBOX.cinematicMode = false;
            gp.GAMESTATE = "play";
        });
    }
}