package ui;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

import static utils.Constants.SCREEN_HEIGHT;
import static utils.Constants.SCREEN_WIDTH;

/**
 * Renders a radial darkness overlay for cave maps.
 *
 * Usage in GamePanel.paintComponent() — after world/player, before UI:
 *   DARKNESSOVERLAY.draw(g2, player.screenX + TILE_SIZE/2, player.screenY + TILE_SIZE/2);
 *
 * Only draws when active (set via setActive()).
 */
public class DarknessOverlay {

    // ── Constants ─────────────────────────────────────────────────────────────

    /** Fully lit inner radius around the player center (px). */
    private static final int INNER_RADIUS = 90;

    /** Outer radius where light fully fades to black (px). */
    private static final int OUTER_RADIUS = 180;

    /** Alpha of the surrounding darkness (0-255). */
    private static final int DARK_ALPHA = 235;

    // ── State ─────────────────────────────────────────────────────────────────

    private boolean active = false;

    // ── Public API ────────────────────────────────────────────────────────────

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    /**
     * Draw the darkness overlay centered on the given screen coordinates.
     *
     * @param g2      Graphics context
     * @param centerX Player screen center X
     * @param centerY Player screen center Y
     */
    public void draw(Graphics2D g2, int centerX, int centerY) {
        if (!active) return;

        // Create the gradient
        // CycleMethod.NO_CYCLE ensures everything outside OUTER_RADIUS
        // uses the last color (DARK_ALPHA black)
        RadialGradientPaint gradient = new RadialGradientPaint(
                centerX, centerY,
                OUTER_RADIUS,
                new float[] {
                        0.0f,
                        (float) INNER_RADIUS / OUTER_RADIUS,
                        1.0f
                },
                new Color[] {
                        new Color(0, 0, 0, 0),          // Center is clear
                        new Color(0, 0, 0, 0),          // Clear until inner radius
                        new Color(0, 0, 0, DARK_ALPHA)  // Fade to dark
                },
                MultipleGradientPaint.CycleMethod.NO_CYCLE
        );

        // Save previous paint
        Paint prevPaint = g2.getPaint();

        // Set the gradient and fill the entire screen
        g2.setPaint(gradient);
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        // Restore paint
        g2.setPaint(prevPaint);
    }
}