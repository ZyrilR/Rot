package utils;

import javazoom.jl.player.Player;
import java.io.InputStream;

public class AudioManager {

    private static AudioTrack currentTrack;

    // Holds ANY background map music (Overworld, Cave, Water, etc.) safely
    private static AudioTrack savedBackgroundTrack;

    // --- MASTER VOLUME CONTROL (0 to 10) ---
    public static int volume = 7;

    public static void applyVolume() {
        try {
            volume = Math.max(0, Math.min(10, volume));
            float db = (volume == 0) ? -80.0f : (float) (20.0 * Math.log10(volume / 10.0));

            for (javax.sound.sampled.Mixer.Info info : javax.sound.sampled.AudioSystem.getMixerInfo()) {
                javax.sound.sampled.Mixer mixer = javax.sound.sampled.AudioSystem.getMixer(info);

                for (javax.sound.sampled.Line line : mixer.getSourceLines()) {
                    try {
                        if (line.isOpen() && line.isControlSupported(javax.sound.sampled.FloatControl.Type.MASTER_GAIN)) {
                            javax.sound.sampled.FloatControl gainControl = (javax.sound.sampled.FloatControl) line.getControl(javax.sound.sampled.FloatControl.Type.MASTER_GAIN);
                            gainControl.setValue(db);
                        }
                    } catch (Exception e) {}
                }
            }
        } catch (Exception e) {
            System.err.println("[AudioManager] Volume control error.");
        }
    }

    /**
     * Smart play method. Protects the saved map track through the entire battle and victory sequence!
     */
    public static void playMusic(String path, boolean loop) {
        if (path == null) return;

        // 1. If requesting the exact track that's already playing, just unpause it
        if (currentTrack != null && currentTrack.path.equalsIgnoreCase(path)) {
            if (currentTrack.isPaused) currentTrack.resumeTrack();
            return;
        }

        // Define what tracks belong to the "Battle Sequence"
        boolean isNewInterruption = path.equalsIgnoreCase(Constants.BGM_WILD_BATTLE) ||
                path.equalsIgnoreCase(Constants.BGM_TRAINER_BATTLE) ||
                path.equalsIgnoreCase(Constants.BGM_VICTORY) ||
                path.equalsIgnoreCase(Constants.BGM_CAUGHT_ROT);

        if (currentTrack != null) {
            boolean isCurrentInterruption = currentTrack.path.equalsIgnoreCase(Constants.BGM_WILD_BATTLE) ||
                    currentTrack.path.equalsIgnoreCase(Constants.BGM_TRAINER_BATTLE) ||
                    currentTrack.path.equalsIgnoreCase(Constants.BGM_VICTORY) ||
                    currentTrack.path.equalsIgnoreCase(Constants.BGM_CAUGHT_ROT);

            // 2. Map -> Battle (Save map track)
            if (isNewInterruption && !isCurrentInterruption) {
                currentTrack.pauseTrack();
                savedBackgroundTrack = currentTrack;
                System.out.println("[AudioManager] Paused map track: " + savedBackgroundTrack.path);
            }
            // 3. Battle -> Victory (Transition between interruptions, keep map track saved!)
            else if (isNewInterruption && isCurrentInterruption) {
                currentTrack.stopTrack();
                // Notice we do NOT touch savedBackgroundTrack here!
            }
            // 4. Battle/Victory -> Map (Restore saved map track)
            else if (savedBackgroundTrack != null && savedBackgroundTrack.path.equalsIgnoreCase(path)) {
                System.out.println("[AudioManager] Smoothly Resuming map track: " + savedBackgroundTrack.path);
                currentTrack.stopTrack(); // Kill the battle/victory music
                currentTrack = savedBackgroundTrack; // Restore the map music
                currentTrack.resumeTrack();
                savedBackgroundTrack = null;
                applyVolume();
                return; // Exit early because we seamlessly resumed!
            }
            // 5. Map -> Different Map (e.g., Grass to Cave, kill everything, start fresh)
            else {
                currentTrack.stopTrack();
                if (savedBackgroundTrack != null) {
                    savedBackgroundTrack.stopTrack();
                    savedBackgroundTrack = null;
                }
            }
        }

        // Start the brand new track
        currentTrack = new AudioTrack(path, loop);
        currentTrack.start();
        System.out.println("[AudioManager] Playing NEW track: " + path);
    }

    public static void playSFX(String path) {
        new Thread(() -> {
            try {
                InputStream is = AudioManager.class.getResourceAsStream(path);
                if (is == null) return;

                Player sfxPlayer = new Player(is);
                sfxPlayer.play(1);
                applyVolume();

                sfxPlayer.play();
                sfxPlayer.close();
            } catch (Exception e) {
                System.err.println("[AudioManager] SFX Error: " + e.getMessage());
            }
        }).start();
    }

    public static void stopMusic() {
        if (currentTrack != null) currentTrack.stopTrack();
        if (savedBackgroundTrack != null) savedBackgroundTrack.stopTrack();
        currentTrack = null;
        savedBackgroundTrack = null;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // INNER CLASS: AudioTrack
    // ══════════════════════════════════════════════════════════════════════════
    private static class AudioTrack {
        String path;
        boolean loop;
        Player player;
        Thread thread;
        volatile boolean isPlaying = true;
        volatile boolean isPaused = false;

        AudioTrack(String path, boolean loop) {
            this.path = path;
            this.loop = loop;
        }

        void start() {
            thread = new Thread(() -> {
                while (isPlaying) {
                    if (isPaused) {
                        try { Thread.sleep(20); } catch (Exception e) {}
                    } else {
                        try {
                            if (player == null) {
                                InputStream is = AudioManager.class.getResourceAsStream(path);
                                if (is == null) {
                                    System.err.println("[AudioManager] Missing file: " + path);
                                    isPlaying = false;
                                    break;
                                }
                                player = new Player(is);

                                player.play(1);
                                AudioManager.applyVolume();
                            }

                            if (!player.play(2)) {
                                if (loop) {
                                    player.close();
                                    player = null;
                                } else {
                                    isPlaying = false;
                                }
                            }
                        } catch (Exception e) {
                            if (player != null) { player.close(); player = null; }
                            try { Thread.sleep(50); } catch (Exception ex) {}
                        }
                    }
                }
                if (player != null) player.close();
            });
            thread.start();
        }

        void pauseTrack() { isPaused = true; }

        void resumeTrack() {
            isPaused = false;
            AudioManager.applyVolume();
        }

        void stopTrack() { isPlaying = false; isPaused = false; }
    }
}