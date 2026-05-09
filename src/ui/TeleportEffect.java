package ui;

/**
 * Two-phase teleport effect.
 *  Phase IN  : cycle through directional idle frames slow → fast (player on origin map).
 *  Phase OUT : cycle fast → slow (player on destination map).
 * The map swap happens at the boundary between phases.
 *
 * The effect is rendered by Player.draw, which uses {@link #getDirectionFrame()}
 * to pick one of the four idle sprites (up, left, down, right) each frame.
 */
public class TeleportEffect {

    public enum Phase { IDLE, SPIN_IN, SWAPPING, SPIN_OUT }

    private static final int IN_FRAMES  = 60;
    private static final int OUT_FRAMES = 60;

    // Cycle order: up → left → down → right → loop
    private static final String[] DIRECTIONS = { "up", "left", "down", "right" };

    private Phase phase = Phase.IDLE;
    private int   tick  = 0;

    // Cumulative cycle position (in fractional frames-of-the-cycle) — never reset
    // mid-effect so the visible direction never snaps backwards.
    private double cyclePos = 0.0;

    public boolean isActive() { return phase != Phase.IDLE; }
    public Phase   getPhase() { return phase; }

    public void startSpinIn() {
        phase = Phase.SPIN_IN;
        tick  = 0;
        cyclePos = 0.0;
    }
    public void markReadyToSwap() { phase = Phase.SWAPPING; }
    public void startSpinOut() {
        phase = Phase.SPIN_OUT;
        tick  = 0;
    }
    public void stop() {
        phase = Phase.IDLE;
        tick  = 0;
        cyclePos = 0.0;
    }

    /** advance one frame; returns true when current phase has elapsed. */
    public boolean update() {
        if (phase == Phase.IDLE || phase == Phase.SWAPPING) return false;
        tick++;
        // Advance the cycle by an accelerating/decelerating amount per frame.
        // Speed ranges from ~0.08 cycle-steps/frame (slow) to ~1.2 (fast).
        double progress;
        double speed;
        if (phase == Phase.SPIN_IN) {
            progress = Math.min(1.0, tick / (double) IN_FRAMES);
            speed    = 0.08 + progress * 1.12;       // slow → fast
        } else { // SPIN_OUT
            progress = Math.min(1.0, tick / (double) OUT_FRAMES);
            speed    = 1.2  - progress * 1.12;       // fast → slow
        }
        cyclePos += speed;

        if (phase == Phase.SPIN_IN  && tick >= IN_FRAMES)  return true;
        if (phase == Phase.SPIN_OUT && tick >= OUT_FRAMES) return true;
        return false;
    }

    /** Returns the direction string ("up"/"left"/"down"/"right") to draw this frame. */
    public String getDirectionFrame() {
        if (phase == Phase.IDLE) return null;
        int idx = ((int) Math.floor(cyclePos)) % DIRECTIONS.length;
        if (idx < 0) idx += DIRECTIONS.length;
        return DIRECTIONS[idx];
    }
}
