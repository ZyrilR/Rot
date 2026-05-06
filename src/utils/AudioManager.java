package utils;

import javazoom.jl.player.Player;
import java.io.InputStream;

public class AudioManager {

    private static AudioTrack currentTrack;
    private static AudioTrack savedOverworldTrack;

    // --- MASTER VOLUME CONTROL (0 to 10) ---
    public static int volume = 7;

    /**
     * Hacks into the Java Audio hardware to dynamically change the volume
     * of all ACTIVE audio lines across the JVM.
     */
    public static void applyVolume() {
        try {
            // Clamp volume bounds
            volume = Math.max(0, Math.min(10, volume));

            // Convert 0-10 linear slider scale to decibels (logarithmic)
            float db = (volume == 0) ? -80.0f : (float) (20.0 * Math.log10(volume / 10.0));

            for (javax.sound.sampled.Mixer.Info info : javax.sound.sampled.AudioSystem.getMixerInfo()) {
                javax.sound.sampled.Mixer mixer = javax.sound.sampled.AudioSystem.getMixer(info);

                // CRITICAL FIX: Grab active source instances, not info classes
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
     * Smart play method. Automatically saves the Overworld track if interrupted.
     */
    public static void playMusic(String path, boolean loop) {
        if (currentTrack != null && currentTrack.path.equals(path)) {
            if (currentTrack.isPaused) currentTrack.resumeTrack();
            return;
        }

        if (currentTrack != null) {
            if (currentTrack.path.equals(Constants.BGM_OVERWORLD)) {
                currentTrack.pauseTrack();
                savedOverworldTrack = currentTrack;
            } else {
                currentTrack.stopTrack();
            }
        }

        currentTrack = new AudioTrack(path, loop);
        currentTrack.start();
        System.out.println("[AudioManager] Playing: " + path);
    }

    /**
     * Plays a short sound effect once OVER the background music.
     */
    public static void playSFX(String path) {
        new Thread(() -> {
            try {
                InputStream is = AudioManager.class.getResourceAsStream(path);
                if (is == null) return;

                Player sfxPlayer = new Player(is);

                // THE TRICK: Play exactly 1 frame to force Java to open the audio line
                sfxPlayer.play(1);
                applyVolume(); // Immediately clamp the volume to the user's setting

                sfxPlayer.play(); // Play the rest of the sound
                sfxPlayer.close(); // Clean up
            } catch (Exception e) {
                System.err.println("[AudioManager] SFX Error: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Restores the exact paused state of the Overworld music.
     */
    public static void resumeOverworldMusic() {
        if (currentTrack != null) currentTrack.stopTrack();

        if (savedOverworldTrack != null && savedOverworldTrack.isPlaying) {
            currentTrack = savedOverworldTrack;
            currentTrack.resumeTrack();
            savedOverworldTrack = null;
            applyVolume(); // Re-apply volume in case it changed during battle
        } else {
            playMusic(Constants.BGM_OVERWORLD, true);
        }
    }

    public static void stopMusic() {
        if (currentTrack != null) currentTrack.stopTrack();
        if (savedOverworldTrack != null) savedOverworldTrack.stopTrack();
        currentTrack = null;
        savedOverworldTrack = null;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // INNER CLASS: AudioTrack (Immortal Thread with Volume Hooks)
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

                                // THE TRICK: Decode 1 frame to open the line, then apply volume
                                player.play(1);
                                AudioManager.applyVolume();
                            }

                            // Play smoothly in 2-frame chunks
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
            AudioManager.applyVolume(); // Ensure volume is right when unpausing
        }

        void stopTrack() { isPlaying = false; isPaused = false; }
    }
}