package ui;

import engine.GamePanel;
import npc.NPC;
import tile.TileManager;
import utils.AssetManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import static utils.Constants.*;

/**
 * Full-screen world map overlay.
 *
 * Layout:
 *   - Thin gold border all around
 *   - Map fills almost the entire overlay
 *   - Map name overlaid top-right inside the map (no title bar)
 *   - Thin bottom strip: player coordinates + close hint
 *
 * Controls:  M / ESC — close
 */
public class MapUI {

    private final GamePanel gp;

    private int           inputCooldown = 0;
    private BufferedImage mapCache      = null;
    private boolean       cacheDirty    = true;

    // Computed each draw
    private int    mapX, mapY, mapW, mapH;
    private double tileScale    = 1.0;
    private int    tileDrawSize = 1;

    private static final int BORDER = 2;  // gold border thickness (px)
    private static final int BAR_H  = 22; // bottom strip height

    public MapUI(GamePanel gp) {
        this.gp = gp;
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    public void open() {
        inputCooldown = INPUT_DELAY * 2;
        cacheDirty    = true;
        System.out.println("[MapUI] Opened.");
    }

    /** Call after every world load / teleport so the cache is rebuilt. */
    public void invalidateCache() {
        cacheDirty = true;
    }

    // ── Update ────────────────────────────────────────────────────────────────

    public void update() {
        if (inputCooldown > 0) { inputCooldown--; return; }

        if (gp.KEYBOARDHANDLER.mPressed || gp.KEYBOARDHANDLER.escPressed) {
            gp.KEYBOARDHANDLER.mPressed   = false;
            gp.KEYBOARDHANDLER.escPressed = false;
            gp.GAMESTATE = "play";
            System.out.println("[MapUI] Closed.");
        }
    }

    // ── Draw ──────────────────────────────────────────────────────────────────

    public void draw(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        Font base = (AssetManager.pokemonGb != null)
                ? AssetManager.pokemonGb : new Font("Monospaced", Font.PLAIN, 10);

        // ── 1. Compute scale first so we know the exact map pixel size ────────
        //    Fit the map to the full screen minus the border strips and bottom bar.
        int availW = SCREEN_WIDTH  - BORDER * 2;
        int availH = SCREEN_HEIGHT - BORDER * 2 - BAR_H;

        if (MAX_WORLD_COL > 0 && MAX_WORLD_ROW > 0) {
            double sx = (double) availW / (MAX_WORLD_COL * TILE_SIZE);
            double sy = (double) availH / (MAX_WORLD_ROW * TILE_SIZE);
            tileScale    = Math.min(sx, sy);
            tileDrawSize = Math.max(1, (int)(TILE_SIZE * tileScale));
        }

        // Exact pixel size of the rendered map
        int renderedW = MAX_WORLD_COL * tileDrawSize;
        int renderedH = MAX_WORLD_ROW * tileDrawSize;

        // Total overlay = map + border strips + bottom bar
        int totalW = renderedW + BORDER * 2;
        int totalH = renderedH + BORDER * 2 + BAR_H;

        // Centre the overlay on screen
        int bx = (SCREEN_WIDTH  - totalW) / 2;
        int by = (SCREEN_HEIGHT - totalH) / 2;

        // Map pixel origin (inside the border)
        mapX = bx + BORDER;
        mapY = by + BORDER;
        mapW = renderedW;
        mapH = renderedH;

        // ── 2. Dark background behind everything ──────────────────────────────
        g2.setColor(new Color(0, 0, 0, 160)); // ~63% opacity
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        // ── 3. Build / draw cached map image ─────────────────────────────────
        if (cacheDirty) buildMapCache();

        if (mapCache != null) {
            Shape prev = g2.getClip();
            g2.setClip(mapX, mapY, mapW, mapH);

            g2.drawImage(mapCache, mapX, mapY, renderedW, renderedH, null);
            drawNPCDots(g2, mapX, mapY);
            drawPlayerDot(g2, mapX, mapY);

            g2.setClip(prev);
        }

        // ── 4. Gold border — four fillRect strips, no gaps at corners ─────────
        //    top, bottom, left, right — each exactly BORDER pixels thick,
        //    flush against the map and the bottom bar.
        Color gold = new Color(216, 184, 88);
        g2.setColor(gold);
        // top
        g2.fillRect(bx, by, totalW, BORDER);
        // bottom (below the bar)
        g2.fillRect(bx, by + totalH - BORDER, totalW, BORDER);
        // left
        g2.fillRect(bx, by, BORDER, totalH);
        // right
        g2.fillRect(bx + totalW - BORDER, by, BORDER, totalH);

        // ── 5. Bottom strip ───────────────────────────────────────────────────
        int barY = mapY + mapH;   // immediately below the map

        // PCUI status bar colour (215, 210, 200)
        g2.setColor(new Color(215, 210, 200));
        g2.fillRect(mapX, barY, mapW, BAR_H);

        g2.setFont(base.deriveFont(6f));
        FontMetrics bfm = g2.getFontMetrics();
        int textY = barY + (BAR_H + bfm.getAscent()) / 2;

        // Coordinates — left
        int playerGridX = gp.player.worldX / TILE_SIZE;
        int playerGridY = gp.player.worldY / TILE_SIZE;
        g2.setColor(new Color(44, 44, 42));
        g2.drawString("X " + playerGridX + "  Y " + playerGridY,
                mapX + 8, textY);

        // Hint — right
        String hint = "M/ESC Close";
        g2.setColor(new Color(120, 116, 108));
        g2.drawString(hint, mapX + mapW - bfm.stringWidth(hint) - 8, textY);

        // ── 6. Map name overlaid top-right inside the map ─────────────────────
        if (mapCache != null) {
            String mapName = buildPathLabel(gp.CURRENT_PATH);
            g2.setFont(base.deriveFont(Font.BOLD, 10f));
            FontMetrics fm = g2.getFontMetrics();
            int nameW = fm.stringWidth(mapName);
            int nameX = mapX + mapW - nameW - 10;
            int nameY = mapY + 16;

            g2.setColor(new Color(0, 0, 0, 155));
            g2.fillRect(nameX - 5, nameY - fm.getAscent() - 2,
                    nameW + 10, fm.getHeight() + 4);
            g2.setColor(gold);
            g2.drawString(mapName, nameX, nameY);
        }
    }

    // ── Cache build ───────────────────────────────────────────────────────────

    private void buildMapCache() {
        if (MAX_WORLD_COL <= 0 || MAX_WORLD_ROW <= 0) return;
        int imgW = MAX_WORLD_COL * tileDrawSize;
        int imgH = MAX_WORLD_ROW * tileDrawSize;
        if (imgW <= 0 || imgH <= 0) return;

        mapCache = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D mg = mapCache.createGraphics();
        mg.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        mg.setColor(new Color(20, 20, 20));
        mg.fillRect(0, 0, imgW, imgH);

        drawLayerGroup(mg, gp.world.getBackgroundLayer());
        drawLayerGroup(mg, gp.world.getDecorationLayer());
        drawLayerGroup(mg, gp.world.getBuildingLayer());

        TileManager interactive = gp.getWorldInteractiveLayer();
        if (interactive != null) renderTileManager(mg, interactive);

        mg.dispose();
        cacheDirty = false;
        System.out.println("[MapUI] Cache built (" + imgW + "x" + imgH + ").");
    }

    private void drawLayerGroup(Graphics2D mg, ArrayList<TileManager> layers) {
        if (layers == null) return;
        for (TileManager tm : layers)
            if (tm != null) renderTileManager(mg, tm);
    }

    private void renderTileManager(Graphics2D mg, TileManager tm) {
        int[][] map = tm.getMap();
        if (map == null) return;
        for (int row = 0; row < map.length; row++) {
            for (int col = 0; col < map[0].length; col++) {
                int tileNum = map[row][col];
                if (tileNum == 0) continue;
                int idx = tileNum - 1;
                if (idx < 0 || idx >= tm.getTiles().size()) continue;
                BufferedImage img = tm.getTiles().get(idx).image;
                if (img == null) continue;
                mg.drawImage(img,
                        col * tileDrawSize, row * tileDrawSize,
                        tileDrawSize, tileDrawSize, null);
            }
        }
    }

    // ── Live dots ─────────────────────────────────────────────────────────────

    private void drawNPCDots(Graphics2D g2, int offsetX, int offsetY) {
        TileManager interactive = gp.getWorldInteractiveLayer();
        if (interactive == null) return;
        java.util.List<NPC> npcs = interactive.getNPCs();
        if (npcs == null) return;

        int r = Math.max(2, tileDrawSize / 3);
        for (NPC npc : npcs) {
            if (npc == null) continue;
            int cx = offsetX + (npc.worldX / TILE_SIZE) * tileDrawSize + tileDrawSize / 2;
            int cy = offsetY + (npc.worldY / TILE_SIZE) * tileDrawSize + tileDrawSize / 2;
            // dark outline
            g2.setColor(new Color(20, 15, 5, 180));
            g2.fillOval(cx - r - 1, cy - r - 1, (r + 1) * 2, (r + 1) * 2);
            // yellow fill
            g2.setColor(new Color(255, 220, 50));
            g2.fillOval(cx - r, cy - r, r * 2, r * 2);
        }
    }

    private void drawPlayerDot(Graphics2D g2, int offsetX, int offsetY) {
        int gridX = gp.player.worldX / TILE_SIZE;
        int gridY = gp.player.worldY / TILE_SIZE;
        int cx    = offsetX + gridX * tileDrawSize + tileDrawSize / 2;
        int cy    = offsetY + gridY * tileDrawSize + tileDrawSize / 2;
        int r     = Math.max(4, tileDrawSize / 2);

        // dark outline
        g2.setColor(new Color(60, 0, 0, 200));
        g2.fillOval(cx - r - 1, cy - r - 1, (r + 1) * 2, (r + 1) * 2);
        // red fill
        g2.setColor(new Color(220, 50, 50));
        g2.fillOval(cx - r, cy - r, r * 2, r * 2);
        // subtle highlight
        int hl = Math.max(1, r / 2);
        g2.setColor(new Color(255, 200, 200, 160));
        g2.fillOval(cx - hl, cy - r + 1, hl, hl);

        // white directional triangle
        int tri = Math.max(3, r + 2);
        g2.setColor(Color.WHITE);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        switch (gp.player.getDirection()) {
            case "up"    -> g2.fillPolygon(
                    new int[]{ cx,           cx - tri/2,  cx + tri/2 },
                    new int[]{ cy - r - tri, cy - r,      cy - r     }, 3);
            case "down"  -> g2.fillPolygon(
                    new int[]{ cx,           cx - tri/2,  cx + tri/2 },
                    new int[]{ cy + r + tri, cy + r,      cy + r     }, 3);
            case "left"  -> g2.fillPolygon(
                    new int[]{ cx - r - tri, cx - r,      cx - r     },
                    new int[]{ cy,           cy - tri/2,  cy + tri/2 }, 3);
            case "right" -> g2.fillPolygon(
                    new int[]{ cx + r + tri, cx + r,      cx + r     },
                    new int[]{ cy,           cy - tri/2,  cy + tri/2 }, 3);
        }
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private String buildPathLabel(String path) {
        if (path == null || path.isEmpty()) return "Unknown";
        String p = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        int last = p.lastIndexOf('/');
        return last >= 0 ? p.substring(last + 1) : p;
    }
}