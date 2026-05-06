package utils;

import javazoom.jl.player.Player;
import java.io.InputStream;

public class AudioManager {

    private static AudioTrack currentTrack;
    private static AudioTrack savedOverworldTrack;

    /**
     * Smart play method. Automatically saves the Overworld track if it is interrupted.
     */
    public static void playMusic(String path, boolean loop) {
        // If the requested track is already playing, just unpause it
        if (currentTrack != null && currentTrack.path.equals(path)) {
            if (currentTrack.isPaused) currentTrack.resumeTrack();
            return;
        }

        if (currentTrack != null) {
            // FOOLPROOF SAVE: If the track we are leaving is the Overworld, save it!
            if (currentTrack.path.equals(Constants.BGM_OVERWORLD)) {
                currentTrack.pauseTrack();
                savedOverworldTrack = currentTrack;
                System.out.println("[AudioManager] Auto-saved Overworld track.");
            } else {
                // Otherwise, kill the track (e.g., stopping battle music to play victory music)
                currentTrack.stopTrack();
            }
        }

        currentTrack = new AudioTrack(path, loop);
        currentTrack.start();
        System.out.println("[AudioManager] Playing: " + path);
    }

    /**
     * Restores the exact paused state of the Overworld music.
     */
    public static void resumeOverworldMusic() {
        if (currentTrack != null) currentTrack.stopTrack(); // Kill current (Victory/Battle)

        if (savedOverworldTrack != null && savedOverworldTrack.isPlaying) {
            currentTrack = savedOverworldTrack;
            currentTrack.resumeTrack();
            savedOverworldTrack = null;
            System.out.println("[AudioManager] Seamlessly resumed Overworld music.");
        } else {
            // Failsafe in case the memory was lost
            System.err.println("[AudioManager] Saved track lost. Restarting smoothly.");
            playMusic(Constants.BGM_OVERWORLD, true);
        }
    }

    // Add this new method inside utils.AudioManager.java

    /**
     * Plays a short sound effect once OVER the background music.
     * Uses a fire-and-forget temporary thread.
     */
    public static void playSFX(String path) {
        new Thread(() -> {
            try {
                InputStream is = AudioManager.class.getResourceAsStream(path);
                if (is == null) {
                    System.err.println("[AudioManager] SFX file not found: " + path);
                    return;
                }
                // Create a temporary player just for this sound
                Player sfxPlayer = new Player(is);
                sfxPlayer.play(); // Plays the sound once
                sfxPlayer.close(); // Cleans up memory instantly when done
            } catch (Exception e) {
                System.err.println("[AudioManager] SFX Error: " + e.getMessage());
            }
        }).start();
    }

    public static void stopMusic() {
        if (currentTrack != null) currentTrack.stopTrack();
        if (savedOverworldTrack != null) savedOverworldTrack.stopTrack();
        currentTrack = null;
        savedOverworldTrack = null;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // INNER CLASS: AudioTrack (Immortal Thread implementation)
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
                            }

                            // Play 2 frames (~50ms). Keeps buffer full but highly responsive to pauses.
                            if (!player.play(2)) {
                                if (loop) {
                                    player.close();
                                    player = null; // Rebuilds player to loop
                                } else {
                                    isPlaying = false; // Song ended
                                }
                            }
                        } catch (Exception e) {
                            // IMMORTAL THREAD FIX: Do not crash! Close and rebuild player.
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
        void resumeTrack() { isPaused = false; }
        void stopTrack() { isPlaying = false; isPaused = false; }
    }
}