package ui;

import brainrots.BrainRot;
import brainrots.Tier;
import engine.GamePanel;
import storage.PCSystem;
import storage.PCSystem.MoveResult;
import storage.Slot;
import utils.AssetManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import static utils.Constants.*;

/**
 * Renders and handles input for the BrainRot PC Storage system.
 *
 * Two layouts, toggled with P at any time:
 *
 *   Layout 1 — BOX VIEW  (default on open)
 *     • Left panel : 5×5 grid of box slots (cursor rows 0–4, cols 0–4)
 *     • Header     : "PC"  |  "BOX N / 10"  |  [PARTY BRAINROTS] (lit in party view)
 *     • Right panel: BrainRot data card (img, name, type, tier, hp)
 *     • Status bar : persistent selection feedback + nav hints
 *
 *   Layout 2 — PARTY VIEW
 *     • Left panel : 6 party member rows (avatar, name, tier bar, hp bar, type badge)
 *     • Header     : same, [PARTY BRAINROTS] button highlighted gold
 *     • Right panel: same data card
 *     • Status bar : persistent selection feedback + nav hints
 *
 * Controls (PC open):
 *   W/A/S/D  — move cursor within current layout
 *   P        — toggle between Box View and Party View (works from either)
 *   TAB      — cycle to next box (box view only)
 *   ENTER    — select / confirm move or swap
 *   ESC      — deselect held BrainRot (if any); else close PC
 *   B        — open PC (handled in GamePanel, outside this class)
 */
public class PCUI {

    // ── Layout enum ───────────────────────────────────────────────────────────

    private enum Layout { BOX, PARTY }

    // ── Grid constants ────────────────────────────────────────────────────────

    private static final int GRID_COLS = 5;
    private static final int GRID_ROWS = 5; // 5×5 = 25 slots per box
    private static final double PANEL_SPLIT = 0.60; // 60% left, 40% right

    // ── State ─────────────────────────────────────────────────────────────────

    private final GamePanel gp;
    private final PCSystem  pc;

    private Layout layout = Layout.BOX;

    // Box view cursor — rows 0–4, cols 0–4 only (no header button row)
    private int boxCursorRow = 0;
    private int boxCursorCol = 0;
    private int currentBox   = 0; // 0-indexed

    // Party view cursor — 0 to (partySize - 1)
    private int partyCursorRow = 0;

    // Selection state (null = nothing held)
    private Slot     selectedSlot = null;
    private BrainRot heldRot      = null;

    // ── Input cooldown ────────────────────────────────────────────────────────

    private int inputCooldown = 0;
    private static final int INPUT_DELAY = 10; // frames between key repeats

    // ── Status feedback ───────────────────────────────────────────────────────
    // No timer — message persists until explicitly replaced by a new action.
    // While heldRot != null the message is always the selection reminder.

    private String statusMessage = "";

    // ── Sprite cache ──────────────────────────────────────────────────────────

    /** Maps "Name_TIER" → BufferedImage. Loaded lazily, null-safe. */
    private final Map<String, BufferedImage> spriteCache = new HashMap<>();

    // ── Constructor ───────────────────────────────────────────────────────────

    public PCUI(GamePanel gp, PCSystem pc) {
        this.gp = gp;
        this.pc = pc;
        System.out.println("[PCUI] Initialized.");
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    /** Called by GamePanel when B is pressed. Resets all UI state. */
    public void open() {
        layout         = Layout.PARTY;
        boxCursorRow   = 0;
        boxCursorCol   = 0;
        currentBox     = 0;
        partyCursorRow = 0;
        selectedSlot   = null;
        heldRot        = null;
        statusMessage  = "";
        inputCooldown  = INPUT_DELAY * 2; // guard: B key still registered this frame
        System.out.println("[PCUI] PC opened.");
    }

    // ── Update ────────────────────────────────────────────────────────────────

    /** Called every frame from GamePanel.update() when GAMESTATE == "pc". */
    public void update() {
        if (inputCooldown > 0) { inputCooldown--; return; }

        // ── P: toggle layout — works from either view ─────────────────────────
        if (gp.KEYBOARDHANDLER.pPressed) {
            gp.KEYBOARDHANDLER.pPressed = false;
            if (layout == Layout.BOX) {
                layout         = Layout.PARTY;
                partyCursorRow = 0;
                System.out.println("[PCUI] → Party view.");
            } else {
                layout = Layout.BOX;
                System.out.println("[PCUI] → Box view.");
            }
            inputCooldown = INPUT_DELAY;
            return; // skip remaining input this frame so nothing double-fires
        }

        if (layout == Layout.BOX) updateBoxLayout();
        else                       updatePartyLayout();
    }

    // ── Box layout input ──────────────────────────────────────────────────────

    private void updateBoxLayout() {
        boolean up    = gp.KEYBOARDHANDLER.upPressed;
        boolean down  = gp.KEYBOARDHANDLER.downPressed;
        boolean left  = gp.KEYBOARDHANDLER.leftPressed;
        boolean right = gp.KEYBOARDHANDLER.rightPressed;

        // Cursor movement — clamped strictly within the 5×5 grid
        if (up    && boxCursorRow > 0)             { boxCursorRow--; inputCooldown = INPUT_DELAY; }
        else if (down  && boxCursorRow < GRID_ROWS - 1) { boxCursorRow++; inputCooldown = INPUT_DELAY; }
        else if (left  && boxCursorCol > 0)             { boxCursorCol--; inputCooldown = INPUT_DELAY; }
        else if (right && boxCursorCol < GRID_COLS - 1) { boxCursorCol++; inputCooldown = INPUT_DELAY; }

        // TAB: cycle to next box
        if (gp.KEYBOARDHANDLER.tabPressed) {
            gp.KEYBOARDHANDLER.tabPressed = false;
            currentBox = (currentBox + 1) % PCSystem.BOX_COUNT;
            // Don't clobber the selection message if something is held
            if (heldRot == null) setStatus("Box " + (currentBox + 1));
            inputCooldown = INPUT_DELAY;
        }

        // ENTER: select / confirm
        if (gp.KEYBOARDHANDLER.enterPressed) {
            gp.KEYBOARDHANDLER.enterPressed = false;
            handleBoxSelect();
            inputCooldown = INPUT_DELAY;
        }

        // ESC: deselect if holding; else close PC
        if (gp.KEYBOARDHANDLER.escPressed) {
            gp.KEYBOARDHANDLER.escPressed = false;
            if (selectedSlot != null) {
                selectedSlot = null;
                heldRot      = null;
                setStatus("Deselected.");
            } else {
                gp.GAMESTATE = "play";
                System.out.println("[PCUI] PC closed.");
            }
            inputCooldown = INPUT_DELAY;
        }
    }

    // ── Party layout input ────────────────────────────────────────────────────

    private void updatePartyLayout() {
        boolean up   = gp.KEYBOARDHANDLER.upPressed;
        boolean down = gp.KEYBOARDHANDLER.downPressed;

        // Vertical cursor only — party is a list
        if (up   && partyCursorRow > 0)                    { partyCursorRow--; inputCooldown = INPUT_DELAY; }
        else if (down && partyCursorRow < pc.getPartySize() && partyCursorRow < PCSystem.PARTY_CAPACITY - 1) {
            partyCursorRow++;
            inputCooldown = INPUT_DELAY;
        }

        // ENTER: select / confirm
        if (gp.KEYBOARDHANDLER.enterPressed) {
            gp.KEYBOARDHANDLER.enterPressed = false;
            handlePartySelect();
            inputCooldown = INPUT_DELAY;
        }

        // ESC: deselect if holding; else close PC
        if (gp.KEYBOARDHANDLER.escPressed) {
            gp.KEYBOARDHANDLER.escPressed = false;
            if (selectedSlot != null) {
                selectedSlot = null;
                heldRot      = null;
                setStatus("Deselected.");
            } else {
                gp.GAMESTATE = "play";
                System.out.println("[PCUI] PC closed (party view).");
            }
            inputCooldown = INPUT_DELAY;
        }
    }

    // ── Selection logic: box ──────────────────────────────────────────────────

    private void handleBoxSelect() {
        int slotIndex   = boxCursorRow * GRID_COLS + boxCursorCol;
        BrainRot target = pc.getBoxMember(currentBox, slotIndex);
        Slot targetSlot = new Slot(currentBox, slotIndex);

        if (selectedSlot == null) {
            // First press — pick up
            if (target == null) { setStatus("[EMPTY SLOT] nothing to select."); return; }
            selectedSlot = targetSlot;
            heldRot      = target;
            // Persistent selection message; no timer
            setStatus("[SELECTED] " + target.getName());
            System.out.println("[PCUI] Picked up: " + target.getName() + " @ " + targetSlot);
        } else {
            // Second press — put down
            MoveResult result = pc.move(selectedSlot, targetSlot);
            if (result == MoveResult.SUCCESS || result == MoveResult.SWAPPED) {
                setStatus(result == MoveResult.SWAPPED ? "Swapped!" : heldRot.getName() + " moved!");
                selectedSlot = null;
                heldRot      = null;
            } else {
                // Keep selected, append why it failed
                setStatus("[SELECTED] " + heldRot.getName()
                         + PCSystem.resultMessage(result, null));
            }
        }
    }

    // ── Selection logic: party ────────────────────────────────────────────────

    private void handlePartySelect() {
        // Remove the clamping to partySize - 1 so we can select the empty slot
        int idx = partyCursorRow;

        BrainRot target = (idx < pc.getPartySize()) ? pc.getPartyMember(idx) : null;
        Slot targetSlot = new Slot(idx); // party slot

        if (selectedSlot == null) {
            // First press — pick up
            if (target == null) { setStatus("[EMPTY SLOT] nothing to select."); return; }
            selectedSlot = targetSlot;
            heldRot      = target;
            setStatus("[SELECTED] " + target.getName());
            System.out.println("[PCUI] Picked up party: " + target.getName());
        } else {
            // Second press — put down
            MoveResult result = pc.move(selectedSlot, targetSlot);
            if (result == MoveResult.SUCCESS || result == MoveResult.SWAPPED) {
                setStatus(result == MoveResult.SWAPPED ? "Swapped!" : heldRot.getName() + " moved!");
                selectedSlot = null;
                heldRot      = null;
            } else {
                setStatus("[SELECTED] " + heldRot.getName()
                         + PCSystem.resultMessage(result, null));
            }
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // RENDERING
    // ═════════════════════════════════════════════════════════════════════════

    /** Full PC overlay. Called from GamePanel.paintComponent() when GAMESTATE == "pc". */
    public void draw(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Font base = (AssetManager.pokemonGb != null)
                ? AssetManager.pokemonGb
                : new Font("Arial", Font.PLAIN, 13);

        // 1. Dim overlay
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        // 2. Window chrome
        int winX = TILE_SIZE, winY = TILE_SIZE;
        int winW = SCREEN_WIDTH  - TILE_SIZE * 2;
        int winH = SCREEN_HEIGHT - TILE_SIZE * 2;
        drawWindow(g2, winX, winY, winW, winH, 16);

        // 3. Header bar
        drawHeader(g2, base, winX, winY, winW);

        // 4. Body (layout-specific)
        int bodyY = winY + 52;
        if (layout == Layout.BOX) drawBoxBody  (g2, base, winX, winY, winW, winH, bodyY);
        else                       drawPartyBody(g2, base, winX, winY, winW, winH, bodyY);

        // 5. Status bar
        drawStatusBar(g2, base, winX, winY, winW, winH);
    }

    // ── Window chrome ─────────────────────────────────────────────────────────

    private void drawWindow(Graphics2D g2, int x, int y, int w, int h, int arc) {
        g2.setColor(new Color(245, 242, 235));
        g2.fillRoundRect(x, y, w, h, arc, arc);
        g2.setStroke(new BasicStroke(6));
        g2.setColor(new Color(80, 80, 80));
        g2.drawRoundRect(x, y, w, h, arc, arc);
        g2.setStroke(new BasicStroke(4));
        g2.setColor(new Color(216, 184, 88));
        g2.drawRoundRect(x + 1, y + 1, w - 2, h - 2, arc, arc);
        g2.setStroke(new BasicStroke(2));
        g2.setColor(new Color(80, 80, 80));
        g2.drawRoundRect(x + 4, y + 4, w - 8, h - 8, arc - 4, arc - 4);
        g2.setStroke(new BasicStroke(1));
    }

    // ── Header bar ────────────────────────────────────────────────────────────

    private void drawHeader(Graphics2D g2, Font base, int winX, int winY, int winW) {

        // Dark background bar
        g2.setColor(new Color(44, 44, 42));
        g2.fillRoundRect(winX + 8, winY + 8, winW - 16, 36, 8, 8);

        // "PC" label (left)
        g2.setFont(base.deriveFont(Font.BOLD, 15f));
        g2.setColor(new Color(241, 239, 232));
        g2.drawString("PC", winX + 28, winY + 32);

        // Center text (box or party info)
        String center = (layout == Layout.BOX)
                ? "BOX " + (currentBox + 1) + " / " + PCSystem.BOX_COUNT
                : "PARTY MEMBERS (" + pc.getPartySize() + " / " + PCSystem.PARTY_CAPACITY + ")";

        g2.setFont(base.deriveFont(11f));
        FontMetrics fm = g2.getFontMetrics();
        int cx = winX + 8 + (winW - 16) / 2 - fm.stringWidth(center) / 2;
        g2.drawString(center, cx, winY + 32);

        // Party button state
        boolean partyActive = (layout == Layout.PARTY);
        String btnLabel = "PARTY BRAINROTS";

        // Font + metrics for button
        g2.setFont(base.deriveFont(Font.BOLD, 10f));
        FontMetrics bfm = g2.getFontMetrics();

        // Dynamic button size
        int paddingX = 16;
        int btnW = bfm.stringWidth(btnLabel) + paddingX;
        int btnH = 22;

        // Position (right-aligned)
        int btnX = winX + winW - 16 - btnW;
        int btnY = winY + 14;

        // Button background
        g2.setColor(partyActive ? new Color(216, 184, 88) : new Color(80, 78, 72));
        g2.fillRoundRect(btnX, btnY, btnW, btnH, 6, 6);

        // Button text color
        g2.setColor(partyActive ? new Color(44, 44, 42) : new Color(200, 196, 185));

        // Center text inside button
        int textX = btnX + (btnW - bfm.stringWidth(btnLabel)) / 2;
        int textY = btnY + ((btnH - bfm.getHeight()) / 2) + bfm.getAscent();
        g2.drawString(btnLabel, textX, textY);

        // Gold divider
        g2.setColor(new Color(216, 184, 88));
        g2.drawLine(winX + 8, winY + 46, winX + winW - 8, winY + 46);
    }

    // ── Box body ──────────────────────────────────────────────────────────────

    private void drawBoxBody(Graphics2D g2, Font base,
                             int winX, int winY, int winW, int winH, int bodyY) {
        int divX  = winX + (int)(winW * PANEL_SPLIT);
        int leftW = divX - winX - 20;

        // Cell size: fill available area, capped at 62 px
        int cellSize = 70;

        int gridTotalW = cellSize * GRID_COLS;
        int gridStartX = winX + 8 + (divX - winX - 16 - gridTotalW) / 2;
        int gridStartY = bodyY + 10;

        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                int idx      = row * GRID_COLS + col;
                BrainRot rot = pc.getBoxMember(currentBox, idx);

                boolean isHovered  = (boxCursorRow == row && boxCursorCol == col);
                boolean isSelected = selectedSlot != null && selectedSlot.isBox()
                        && selectedSlot.getBoxIndex() == currentBox
                        && selectedSlot.getIndex()    == idx;

                drawSlotCell(g2, base,
                        gridStartX + col * cellSize,
                        gridStartY + row * cellSize,
                        cellSize, rot, isHovered, isSelected);
            }
        }

        // Data card — shows info for the currently hovered cell
        int hovIdx   = boxCursorRow * GRID_COLS + boxCursorCol;
        BrainRot hov = pc.getBoxMember(currentBox, hovIdx);
        drawDataCard(g2, base, divX, winY, winW, winH, bodyY, hov);
    }

    // ── Party body ────────────────────────────────────────────────────────────

    private void drawPartyBody(Graphics2D g2, Font base,
                               int winX, int winY, int winW, int winH, int bodyY) {
        int divX  = winX + (int)(winW * PANEL_SPLIT);
        int listX = winX + 18;
        int listW = divX - winX - 26;
        int rowH  = 56;
        int listY = bodyY + 6;


        for (int i = 0; i < PCSystem.PARTY_CAPACITY; i++) {
            BrainRot rot = (i < pc.getPartySize()) ? pc.getPartyMember(i) : null;

            boolean isSelected = selectedSlot != null && selectedSlot.isParty()
                    && selectedSlot.getIndex() == i;

            // Hover logic remains the same
            boolean isHovered = (partyCursorRow == i);

            drawPartyRow(g2, base, listX, listY + i * rowH, listW, rowH - 4,
                    rot, isHovered, isSelected);
        }

        // Simplified Card Data logic
        BrainRot hov = (partyCursorRow < pc.getPartySize())
                ? pc.getPartyMember(partyCursorRow)
                : null;

        // Hide card if dragging a box item over an empty slot
        if (heldRot != null && partyCursorRow >= pc.getPartySize()) {
            hov = null;
        }

        drawDataCard(g2, base, divX, winY, winW, winH, bodyY, hov);
    }

    // ── Slot cell (box grid) ──────────────────────────────────────────────────

    private void drawSlotCell(Graphics2D g2, Font base,
                              int x, int y, int size,
                              BrainRot rot,
                              boolean hovered, boolean selected) {
        int pad = 3;

        // Background
        Color bg = selected ? new Color(255, 230, 100)
                : hovered   ? new Color(178, 212, 244)
                :             new Color(220, 215, 205);
        g2.setColor(bg);
        g2.fillRoundRect(x + pad, y + pad, size - pad * 2, size - pad * 2, 6, 6);

        // Border
        g2.setColor(selected ? new Color(200, 150, 20)
                : hovered    ? new Color(24, 95, 165)
                :              new Color(170, 165, 155));
        g2.setStroke(selected || hovered ? new BasicStroke(2) : new BasicStroke(1));
        g2.drawRoundRect(x + pad, y + pad, size - pad * 2, size - pad * 2, 6, 6);
        g2.setStroke(new BasicStroke(1));

        if (rot != null) {
            BufferedImage img = getSprite(rot);
            int ip = pad + 4;
            if (img != null) {
                g2.drawImage(img, x + ip, y + ip, size - ip * 2, size - ip * 2, null);
            } else {
                // Coloured placeholder dot
                g2.setColor(new Color(100, 160, 220));
                g2.fillOval(x + size / 2 - 8, y + size / 2 - 8, 16, 16);
            }
        }
    }

    // ── Party row ─────────────────────────────────────────────────────────────

    private void drawPartyRow(Graphics2D g2, Font base,
                              int x, int y, int w, int h,
                              BrainRot rot,
                              boolean hovered, boolean selected) {
        Color bg = selected ? new Color(255, 230, 100)
                : hovered   ? new Color(178, 212, 244, 220)
                :             new Color(230, 226, 218);
        g2.setColor(bg);
        g2.fillRoundRect(x, y, w, h, 8, 8);

        g2.setColor(selected ? new Color(200, 150, 20)
                : hovered    ? new Color(24, 95, 165)
                :              new Color(185, 180, 170));
        g2.setStroke(selected || hovered ? new BasicStroke(2) : new BasicStroke(1));
        g2.drawRoundRect(x, y, w, h, 8, 8);
        g2.setStroke(new BasicStroke(1));

        if (rot == null) {
            g2.setFont(base.deriveFont(10f));
            g2.setColor(new Color(160, 155, 148));
            g2.drawString("— Empty —", x + 16, y + h / 2 + 4);
            return;
        }

        // Avatar circle
        int avSize = h - 10;
        int avX    = x + 6;
        int avY    = y + 5;

        g2.setColor(new Color(200, 195, 185));
        g2.fillOval(avX, avY, avSize, avSize);
        g2.setColor(new Color(155, 150, 140));
        g2.drawOval(avX, avY, avSize, avSize);

        BufferedImage img = getSprite(rot);
        if (img != null) {
            Shape clip = g2.getClip();
            g2.setClip(new java.awt.geom.Ellipse2D.Float(avX, avY, avSize, avSize));
            g2.drawImage(img, avX, avY, avSize, avSize, null);
            g2.setClip(clip);
        }

        // Text block
        int tx = avX + avSize + 8;
        g2.setFont(base.deriveFont(Font.BOLD, 12f));
        g2.setColor(new Color(44, 44, 42));
        String nm = rot.getName().length() > 18
                ? rot.getName().substring(0, 17) + "…" : rot.getName();
        g2.drawString(nm, tx, y + 16);

        drawLabeledBar(g2, base, tx, y + 22, 120, 7,
                "TIER", tierFraction(rot.getTier()),
                tierColor(rot.getTier()), new Color(200, 196, 186));

        drawLabeledBar(g2, base, tx, y + 36, 120, 7,
                "HP", (double) rot.getCurrentHp() / rot.getMaxHp(),
                hpColor(rot), new Color(200, 196, 186));

        // Type badge
        String typeLabel = rot.getPrimaryType().name();
        g2.setFont(base.deriveFont(9f));
        FontMetrics fm = g2.getFontMetrics();
        int bW = fm.stringWidth(typeLabel) + 12;
        int bH = 18;
        int bX = x + w - bW - 8;
        int bY = y + h / 2 - bH / 2;
        g2.setColor(typeColor(rot.getPrimaryType().name()));
        g2.fillRoundRect(bX, bY, bW, bH, 6, 6);
        g2.setColor(Color.WHITE);
        g2.drawString(typeLabel, bX + 6, bY + 13);
    }

    // ── Data card (right panel — shared by both layouts) ──────────────────────

    private void drawDataCard(Graphics2D g2, Font base,
                              int divX, int winY, int winW, int winH, int bodyY,
                              BrainRot rot) {
        int cX      = divX + 14;           // x-position of right panel
        int cY      = bodyY + 4;                // y-position remains the same
        int cW      = winW - cX + 20;      // narrower width
        int imgH    = 150;
        int infoH   = winH - bodyY - imgH - 70;

        // Divider line
        g2.setColor(new Color(200, 195, 180));
        g2.drawLine(divX, bodyY, divX, winY + winH - 36);

        // "BRAINROT DATA" label
        g2.setFont(base.deriveFont(Font.BOLD, 13f));
        g2.setColor(new Color(44, 44, 42));
        g2.drawString("BRAINROT DATA", cX + 28, cY + 16);

        // Image box
        int imgY = cY + 24;
        g2.setColor(new Color(225, 220, 210));
        g2.fillRoundRect(cX, imgY, cW, imgH, 8, 8);
        g2.setColor(new Color(185, 180, 168));
        g2.drawRoundRect(cX, imgY, cW, imgH, 8, 8);

        if (rot != null) {
            BufferedImage img = getSprite(rot);
            if (img != null) {
                int m = 10;
                g2.drawImage(img, cX + m, imgY + m, cW - m * 2, imgH - m * 2, null);
            } else {
                drawCentredText(g2, base, cX, imgY, cW, imgH, "img");
            }
        } else {
            drawCentredText(g2, base, cX, imgY, cW, imgH, "img");
        }

        // Info box
        int infoY = imgY + imgH + 6;
        g2.setColor(new Color(225, 220, 210));
        g2.fillRoundRect(cX, infoY, cW, infoH, 8, 8);
        g2.setColor(new Color(185, 180, 168));
        g2.drawRoundRect(cX, infoY, cW, infoH, 8, 8);

        int tx = cX + 16;
        int ty = infoY + 24;
        int lh = 22;

        if (rot != null) {
            g2.setFont(base.deriveFont(Font.BOLD, 12f));
            g2.setColor(new Color(44, 44, 42));
            String nm = rot.getName().length() > 16
                    ? rot.getName().substring(0, 15) + "…" : rot.getName();
            g2.drawString(nm, tx, ty); ty += lh;

            g2.setFont(base.deriveFont(11f));
            g2.setColor(new Color(80, 76, 70));
            g2.drawString("Type: " + rot.getPrimaryType().name()
                    + (rot.getSecondaryType() != null
                    ? " / " + rot.getSecondaryType().name() : ""), tx, ty); ty += lh;
            g2.drawString("Tier: " + rot.getTier().name(), tx, ty); ty += lh;
            g2.drawString("HP:   " + rot.getCurrentHp() + " / " + rot.getMaxHp(), tx, ty);
        } else {
            g2.setFont(base.deriveFont(11f));
            g2.setColor(new Color(140, 136, 128));
            for (String s : new String[]{"Name", "type", "lvl", "hp"}) {
                g2.drawString(s, tx, ty); ty += lh;
            }
        }
    }

    private void drawCentredText(Graphics2D g2, Font base,
                                 int bx, int by, int bw, int bh, String text) {
        g2.setFont(base.deriveFont(10f));
        g2.setColor(new Color(150, 145, 138));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(text, bx + (bw - fm.stringWidth(text)) / 2, by + bh / 2 + 4);
    }

    // ── Status bar ────────────────────────────────────────────────────────────

    private void drawStatusBar(Graphics2D g2, Font base, int winX, int winY, int winW, int winH) {

        int barH = 44; // increased height for 2 lines
        int barY = winY + winH - barH - 8;

        int barX = winX + 8;
        int barW = winW - 16;

        g2.setColor(new Color(215, 210, 200));
        g2.fillRoundRect(barX, barY, barW, barH, 5, 5);

        int padding = 12;

        // ── LEFT: status message (VERTICALLY CENTERED) ─────────
        int leftX = barX + padding;

        if (!statusMessage.isEmpty()) {
            g2.setFont(base.deriveFont(10f));
            g2.setColor(heldRot != null ? new Color(160, 110, 20) : new Color(44, 44, 42));

            FontMetrics fmStatus = g2.getFontMetrics();

            int leftY = barY + ((barH - fmStatus.getHeight()) / 2) + fmStatus.getAscent();
            int maxWidth = barW / 2 - padding;

            String display = statusMessage;
            while (fmStatus.stringWidth(display) > maxWidth && display.length() > 0) {
                display = display.substring(0, display.length() - 1);
            }
            if (!display.equals(statusMessage)) display += "…";

            g2.drawString(display, leftX, leftY);
        }

        // ── RIGHT: multi-line hints ──────────────────────────
        String line1, line2;

        if (layout == Layout.BOX) {
            line1 = "WASD Move   TAB Box   P Party";
            line2 = "ENTER Select   ESC Exit";
        } else {
            line1 = "WS Move   P Box";
            line2 = "ENTER Select   ESC Exit";
        }

        g2.setFont(base.deriveFont(8f));
        g2.setColor(new Color(120, 116, 108));

        FontMetrics fm = g2.getFontMetrics();

        int rightLimit = barX + barW - padding;

        int line1X = rightLimit - fm.stringWidth(line1);
        int line2X = rightLimit - fm.stringWidth(line2);

        int line1Y = barY + 18;
        int line2Y = barY + 30;

        g2.drawString(line1, line1X, line1Y);
        g2.drawString(line2, line2X, line2Y);
    }

    // ── Sprite loading ────────────────────────────────────────────────────────

    /**
     * Loads and caches the idle sprite for a BrainRot.
     * Path: /assets/Sprites/Brainrots/{FolderName}/{TIER}_1.png
     */
    private BufferedImage getSprite(BrainRot rot) {
        String key = rot.getName() + "_" + rot.getTier().name();
        if (spriteCache.containsKey(key)) return spriteCache.get(key);

        String path = "/assets/Sprites/Brainrots/"
                + toFolderName(rot.getName()) + "/" + rot.getTier().name() + "_1.png";
        BufferedImage img = AssetManager.loadImage(path);
        spriteCache.put(key, img); // cache even null to avoid repeated failed loads
        System.out.println("[PCUI] Sprite " + (img != null ? "loaded" : "MISSING") + ": " + path);
        return img;
    }

    /** Converts BrainRot display name → asset folder name. */
    private String toFolderName(String name) {
        return switch (name.toUpperCase()) {
            case "TUNG TUNG TUNG SAHUR"  -> "TungTungTungSahur";
            case "TRALALERO TRALALA"      -> "TralaleroTralala";
            case "BOMBARDINO CROCODILO"   -> "BombardinoCrocodilo";
            case "LIRILI LARILA"          -> "LiriliLarila";
            case "BRR BRR PATAPIM"        -> "BrrBrrPatapim";
            case "BONECA AMBALABU"        -> "BonecaAmbalabu";
            case "UDIN DIN DIN DIN DUN"   -> "OdindindinDun";
            case "CAPUCCINO ASSASSINO"    -> "CapuccinoAssasino";
            default                       -> name.replace(" ", "");
        };
    }

    // ── Drawing helpers ───────────────────────────────────────────────────────

    private void drawLabeledBar(Graphics2D g2, Font base,
                                int x, int y, int w, int h,
                                String label, double fraction,
                                Color fill, Color bg) {
        g2.setFont(base.deriveFont(8f));
        g2.setColor(new Color(100, 96, 90));
        g2.drawString(label, x, y + h);

        int off  = 26; // label width reserve
        int barW = w - off;

        g2.setColor(bg);
        g2.fillRoundRect(x + off, y, barW, h, 3, 3);

        int fillW = (int)(barW * Math.min(1.0, Math.max(0.0, fraction)));
        if (fillW > 0) {
            g2.setColor(fill);
            g2.fillRoundRect(x + off, y, fillW, h, 3, 3);
        }
        g2.setColor(new Color(160, 155, 145));
        g2.drawRoundRect(x + off, y, barW, h, 3, 3);
    }

    private Color hpColor(BrainRot rot) {
        double r = (double) rot.getCurrentHp() / rot.getMaxHp();
        return r > 0.5 ? new Color(60, 180, 80)
                : r > 0.25 ? new Color(220, 180, 40)
                : new Color(210, 60, 60);
    }

    private Color tierColor(Tier tier) {
        return switch (tier) {
            case DIAMOND -> new Color(100, 200, 240);
            case GOLD    -> new Color(216, 184, 88);
            default      -> new Color(160, 155, 145);
        };
    }

    private double tierFraction(Tier tier) {
        return switch (tier) { case DIAMOND -> 1.0; case GOLD -> 0.6; default -> 0.3; };
    }

    private Color typeColor(String typeName) {
        return switch (typeName.toUpperCase()) {
            case "FIGHTING" -> new Color(180,  80,  60);
            case "WATER"    -> new Color( 60, 130, 210);
            case "PSYCHIC"  -> new Color(200,  60, 140);
            case "FLYING"   -> new Color(120, 160, 220);
            case "SAND"     -> new Color(190, 155,  80);
            case "GRASS"    -> new Color( 80, 170,  80);
            case "ROCK"     -> new Color(140, 120,  80);
            case "FIRE"     -> new Color(220, 100,  40);
            case "DARK"     -> new Color( 80,  60, 100);
            case "POISON"   -> new Color(140,  70, 160);
            default         -> new Color(130, 126, 118);
        };
    }

    // ── Status helper ─────────────────────────────────────────────────────────

    /**
     * Sets the persistent status message.
     * No timer — stays displayed until replaced by the next action.
     */
    private void setStatus(String msg) {
        statusMessage = msg;
        System.out.println("[PCUI] Status: " + msg);
    }

    // ── Public accessor ───────────────────────────────────────────────────────

    public PCSystem getPCSystem() { return pc; }
}