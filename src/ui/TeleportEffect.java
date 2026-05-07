package ui;

/**
 * Two-phase teleport spin effect.
 *  Phase IN  : spin slow → fast (player still on origin map).
 *  Phase OUT : spin fast → slow (player on destination map).
 * The map swap happens at the boundary between phases.
 */
public class TeleportEffect {

    public enum Phase { IDLE, SPIN_IN, SWAPPING, SPIN_OUT }

    private static final int IN_FRAMES  = 36;
    private static final int OUT_FRAMES = 36;

    private Phase phase = Phase.IDLE;
    private int   tick  = 0;

    public boolean isActive() { return phase != Phase.IDLE; }
    public Phase   getPhase() { return phase; }

    public void startSpinIn() {
        phase = Phase.SPIN_IN;
        tick  = 0;
    }
    public void markReadyToSwap() { phase = Phase.SWAPPING; }
    public void startSpinOut() {
        phase = Phase.SPIN_OUT;
        tick  = 0;
    }
    public void stop() {
        phase = Phase.IDLE;
        tick  = 0;
    }

    /** advance one frame; returns true when current phase has elapsed. */
    public boolean update() {
        if (phase == Phase.IDLE || phase == Phase.SWAPPING) return false;
        tick++;
        if (phase == Phase.SPIN_IN  && tick >= IN_FRAMES)  return true;
        if (phase == Phase.SPIN_OUT && tick >= OUT_FRAMES) return true;
        return false;
    }

    /** Current rotation angle in radians for the player sprite. */
    public double getRotation() {
        // accelerate during SPIN_IN, decelerate during SPIN_OUT
        double progress;
        double speedScale; // peak rotational speed in radians per frame
        if (phase == Phase.SPIN_IN) {
            progress   = Math.min(1.0, tick / (double) IN_FRAMES);
            speedScale = progress; // 0 → 1
        } else if (phase == Phase.SPIN_OUT) {
            progress   = Math.min(1.0, tick / (double) OUT_FRAMES);
            speedScale = 1.0 - progress; // 1 → 0
        } else {
            return 0;
        }
        // Integrate angle ~ cumulative spin so it doesn't reset; use tick * 0.6 baseline
        double base = tick * 0.6;
        return base * Math.max(0.2, speedScale);
    }
}
