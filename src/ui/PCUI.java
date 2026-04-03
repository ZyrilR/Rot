package ui;

import brainrots.BrainRot;
import brainrots.Tier;
import engine.GamePanel;
import skills.Skill;
import storage.PCSystem;
import storage.PCSystem.MoveResult;
import storage.Slot;
import utils.AssetManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static utils.Constants.*;

/**
 * Renders and handles input for the BrainRot PC Storage system.
 *
 * Layout states:
 *   BOX    - 5×5 grid + data card
 *   PARTY  - 6-row list + data card
 *   DETAIL - overlay with INFO or MOVES sub-tab
 *
 * Controls:
 *   WASD/Arrows - move cursor (BOX/PARTY)
 *   P           - toggle BOX ↔ PARTY
 *   TAB         - cycle box (BOX) | toggle INFO↔MOVES (DETAIL)
 *   E           - open DETAIL for hovered BrainRot
 *   ENTER       - select / confirm swap (BOX/PARTY)
 *   ESC         - deselect held → back to PC
 *   B           -  close
 */
public class PCUI {

    // ── Enums ─────────────────────────────────────────────────────────────────
    private enum Layout    { BOX, PARTY, DETAIL }
    private enum DetailTab { INFO, MOVES }

    // ── State ─────────────────────────────────────────────────────────────────
    private final GamePanel gp;
    private final PCSystem  pc;

    private Layout    layout         = Layout.BOX;
    private Layout    previousLayout = Layout.BOX;
    private DetailTab detailTab      = DetailTab.INFO;
    private BrainRot  detailRot      = null;

    private int boxCursorRow  = 0;
    private int boxCursorCol  = 0;
    private int currentBox    = 0;
    private int partyCursorRow = 0;
    private int movesCursor   = 0;

    private Slot     selectedSlot = null;
    private BrainRot heldRot      = null;

    private int    inputCooldown  = 0;
    private String statusMessage  = "";
    private int    statusTimer    = 0; // ticks remaining; -1 = persist

    private final Map<String, BufferedImage> spriteCache = new HashMap<>();

    // ── Constructor ───────────────────────────────────────────────────────────
    public PCUI(GamePanel gp, PCSystem pc) {
        this.gp = gp;
        this.pc = pc;
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────
    public void open() {
        layout         = Layout.PARTY;
        previousLayout = Layout.PARTY;
        boxCursorRow   = 0;
        boxCursorCol   = 0;
        currentBox     = 0;
        partyCursorRow = 0;
        movesCursor    = 0;
        selectedSlot   = null;
        heldRot        = null;
        detailRot      = null;
        statusMessage  = "";
        statusTimer    = 0;
        inputCooldown  = INPUT_DELAY * 2;
        System.out.println("[PCUI] PC opened.");
    }

    // ── Update ────────────────────────────────────────────────────────────────
    public void update() {
        if (inputCooldown > 0) { inputCooldown--; return; }

        // Tick status timer (only when not holding a BrainRot)
        if (statusTimer > 0 && heldRot == null) {
            statusTimer--;
            if (statusTimer == 0) statusMessage = "";
        }

        switch (layout) {
            case BOX    -> updateBoxLayout();
            case PARTY  -> updatePartyLayout();
            case DETAIL -> updateDetailLayout();
        }
    }

    // ── BOX layout input ──────────────────────────────────────────────────────
    private void updateBoxLayout() {
        if (gp.KEYBOARDHANDLER.shiftPressed) {
            gp.KEYBOARDHANDLER.shiftPressed = false;
            layout = Layout.PARTY; previousLayout = Layout.PARTY;
            partyCursorRow = 0; inputCooldown = INPUT_DELAY;
            return;
        }

        if      (gp.KEYBOARDHANDLER.upPressed    && boxCursorRow > 0)             { boxCursorRow--;  inputCooldown = INPUT_DELAY; }
        else if (gp.KEYBOARDHANDLER.downPressed   && boxCursorRow < GRID_ROWS - 1) { boxCursorRow++;  inputCooldown = INPUT_DELAY; }
        else if (gp.KEYBOARDHANDLER.leftPressed   && boxCursorCol > 0)             { boxCursorCol--;  inputCooldown = INPUT_DELAY; }
        else if (gp.KEYBOARDHANDLER.rightPressed  && boxCursorCol < GRID_COLS - 1) { boxCursorCol++;  inputCooldown = INPUT_DELAY; }

        if (gp.KEYBOARDHANDLER.tabPressed) {
            gp.KEYBOARDHANDLER.tabPressed = false;
            currentBox = (currentBox + 1) % PCSystem.BOX_COUNT;
            inputCooldown = INPUT_DELAY;
        }

        if (gp.KEYBOARDHANDLER.ePressed) {
            gp.KEYBOARDHANDLER.ePressed = false;
            int idx = boxCursorRow * GRID_COLS + boxCursorCol;
            BrainRot hov = pc.getBoxMember(currentBox, idx);
            if (hov != null) openDetail(hov, Layout.BOX);
            else setStatus("No BrainRot here.", false);
            inputCooldown = INPUT_DELAY;
        }

        if (gp.KEYBOARDHANDLER.enterPressed) {
            gp.KEYBOARDHANDLER.enterPressed = false;
            handleBoxSelect(); inputCooldown = INPUT_DELAY;
        }

        if (gp.KEYBOARDHANDLER.escPressed) {
            gp.KEYBOARDHANDLER.escPressed = false;
            if (selectedSlot != null) { selectedSlot = null; heldRot = null; setStatus("Deselected.", false); }
            else{ gp.GAMESTATE = "play";System.out.println("[PCUI] PC closed (party view)."); }
            inputCooldown = INPUT_DELAY;
        }
    }

    // ── PARTY layout input ────────────────────────────────────────────────────
    private void updatePartyLayout() {
        if (gp.KEYBOARDHANDLER.shiftPressed) {
            gp.KEYBOARDHANDLER.shiftPressed = false;
            layout = Layout.BOX; previousLayout = Layout.BOX;
            inputCooldown = INPUT_DELAY;
            return;
        }

        if      (gp.KEYBOARDHANDLER.upPressed   && partyCursorRow > 0)                           { partyCursorRow--; inputCooldown = INPUT_DELAY; }
        else if (gp.KEYBOARDHANDLER.downPressed  && partyCursorRow < PCSystem.PARTY_CAPACITY - 1) { partyCursorRow++; inputCooldown = INPUT_DELAY; }

        if (gp.KEYBOARDHANDLER.ePressed) {
            gp.KEYBOARDHANDLER.ePressed = false;
            BrainRot hov = (partyCursorRow < pc.getPartySize()) ? pc.getPartyMember(partyCursorRow) : null;
            if (hov != null) openDetail(hov, Layout.PARTY);
            else setStatus("No BrainRot here.", false);
            inputCooldown = INPUT_DELAY;
        }

        if (gp.KEYBOARDHANDLER.enterPressed) {
            gp.KEYBOARDHANDLER.enterPressed = false;
            handlePartySelect(); inputCooldown = INPUT_DELAY;
        }

        if (gp.KEYBOARDHANDLER.escPressed) {
            gp.KEYBOARDHANDLER.escPressed = false;
            if (selectedSlot != null) { selectedSlot = null; heldRot = null; setStatus("Deselected.", false); }
            else{ gp.GAMESTATE = "play";System.out.println("[PCUI] PC closed (party view)."); }
            inputCooldown = INPUT_DELAY;
        }
    }

    // ── DETAIL layout input ───────────────────────────────────────────────────
    private void updateDetailLayout() {
        if (gp.KEYBOARDHANDLER.tabPressed) {
            gp.KEYBOARDHANDLER.tabPressed = false;
            detailTab   = (detailTab == DetailTab.INFO) ? DetailTab.MOVES : DetailTab.INFO;
            movesCursor = 0; inputCooldown = INPUT_DELAY;
            return;
        }

        if (detailTab == DetailTab.MOVES && detailRot != null) {
            int count = detailRot.getMoves().size();
            if      (gp.KEYBOARDHANDLER.upPressed   && movesCursor > 0)         { movesCursor--; inputCooldown = INPUT_DELAY; }
            else if (gp.KEYBOARDHANDLER.downPressed  && movesCursor < count - 1) { movesCursor++; inputCooldown = INPUT_DELAY; }
        }

        if (gp.KEYBOARDHANDLER.escPressed) {
            gp.KEYBOARDHANDLER.escPressed = false;
            layout = previousLayout; detailRot = null; inputCooldown = INPUT_DELAY;
            System.out.println("[PCUI] ← Back to " + layout + " view.");
        }
    }

    // ── Detail open ───────────────────────────────────────────────────────────
    private void openDetail(BrainRot rot, Layout from) {
        previousLayout = from;
        detailRot      = rot;
        detailTab      = DetailTab.INFO;
        movesCursor    = 0;
        layout         = Layout.DETAIL;
        System.out.println("[PCUI] Detail → " + rot.getName());
    }

    // ── Selection: box ────────────────────────────────────────────────────────
    private void handleBoxSelect() {
        int slotIndex   = boxCursorRow * GRID_COLS + boxCursorCol;
        BrainRot target = pc.getBoxMember(currentBox, slotIndex);
        Slot targetSlot = new Slot(currentBox, slotIndex);

        if (selectedSlot == null) {
            if (target == null) { setStatus("Empty slot.", false); return; }
            selectedSlot = targetSlot; heldRot = target;
            setStatus("Holding: " + target.getName(), true);
        } else {
            MoveResult result = pc.move(selectedSlot, targetSlot);
            boolean ok = result == MoveResult.SUCCESS || result == MoveResult.SWAPPED;
            setStatus(ok ? (result == MoveResult.SWAPPED ? "Swapped!" : heldRot.getName() + " moved!")
                    : PCSystem.resultMessage(result, null), false);
            if (ok) { selectedSlot = null; heldRot = null; }
        }
    }

    // ── Selection: party ──────────────────────────────────────────────────────
    private void handlePartySelect() {
        BrainRot target = (partyCursorRow < pc.getPartySize()) ? pc.getPartyMember(partyCursorRow) : null;
        Slot targetSlot = new Slot(partyCursorRow);

        if (selectedSlot == null) {
            if (target == null) { setStatus("Empty slot.", false); return; }
            selectedSlot = targetSlot; heldRot = target;
            setStatus("Holding: " + target.getName(), true);
        } else {
            MoveResult result = pc.move(selectedSlot, targetSlot);
            boolean ok = result == MoveResult.SUCCESS || result == MoveResult.SWAPPED;
            setStatus(ok ? (result == MoveResult.SWAPPED ? "Swapped!" : heldRot.getName() + " moved!")
                    : PCSystem.resultMessage(result, null), false);
            if (ok) { selectedSlot = null; heldRot = null; }
        }
    }

    // ── RENDERING ──────────────────────────────────────────────────────
    public void draw(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Font base = (AssetManager.pokemonGb != null)
                ? AssetManager.pokemonGb : new Font("Arial", Font.PLAIN, 13);

        // Dim overlay
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        int winX = TILE_SIZE, winY = TILE_SIZE;
        int winW = SCREEN_WIDTH  - TILE_SIZE * 2;
        int winH = SCREEN_HEIGHT - TILE_SIZE * 2;
        drawWindow(g2, winX, winY, winW, winH);

        if (layout == Layout.DETAIL) {
            drawDetailView(g2, base, winX, winY, winW, winH);
        } else {
            drawHeader(g2, base, winX, winY, winW);
            int bodyY = winY + 52;
            if (layout == Layout.BOX) drawBoxBody  (g2, base, winX, winY, winW, winH, bodyY);
            else                       drawPartyBody(g2, base, winX, winY, winW, winH, bodyY);
            drawStatusBar(g2, base, winX, winY, winW, winH);
        }
    }

    // ── Window chrome ─────────────────────────────────────────────────────────
    private void drawWindow(Graphics2D g2, int x, int y, int w, int h) {
        int arc = 16;
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
        // Dark title bar
        g2.setColor(new Color(44, 44, 42));
        g2.fillRoundRect(winX + 8, winY + 8, winW - 16, 36, 8, 8);

        g2.setFont(base.deriveFont(Font.BOLD, 15f));
        g2.setColor(new Color(241, 239, 232));
        g2.drawString("PC", winX + 28, winY + 32);

        // Center label
        String center = (layout == Layout.BOX)
                ? "BOX " + (currentBox + 1) + " / " + PCSystem.BOX_COUNT
                : "PARTY  (" + pc.getPartySize() + " / " + PCSystem.PARTY_CAPACITY + ")";
        g2.setFont(base.deriveFont(11f));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(center, winX + 8 + (winW - 16) / 2 - fm.stringWidth(center) / 2, winY + 32);

        // Party toggle button
        boolean partyActive = (layout == Layout.PARTY);
        String  btnLabel    = "PARTY BRAINROTS";
        g2.setFont(base.deriveFont(Font.BOLD, 10f));
        FontMetrics bfm = g2.getFontMetrics();
        int btnW = bfm.stringWidth(btnLabel) + 16, btnH = 22;
        int btnX = winX + winW - 16 - btnW,        btnY = winY + 14;

        g2.setColor(partyActive ? new Color(216, 184, 88) : new Color(80, 78, 72));
        g2.fillRoundRect(btnX, btnY, btnW, btnH, 6, 6);
        g2.setColor(partyActive ? new Color(44, 44, 42) : new Color(200, 196, 185));
        g2.drawString(btnLabel,
                btnX + (btnW - bfm.stringWidth(btnLabel)) / 2,
                btnY + (btnH - bfm.getHeight()) / 2 + bfm.getAscent());

        g2.setColor(new Color(216, 184, 88));
        g2.drawLine(winX + 8, winY + 46, winX + winW - 8, winY + 46);
    }

    // ── BOX body ──────────────────────────────────────────────────────────────
    private void drawBoxBody(Graphics2D g2, Font base,
                             int winX, int winY, int winW, int winH, int bodyY) {
        int divX     = winX + (int)(winW * PANEL_SPLIT);
        int cellSize = 70;
        int gridW    = cellSize * GRID_COLS;
        int gridX    = winX + 8 + (divX - winX - 16 - gridW) / 2;
        int gridY    = bodyY + 10;

        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                int idx  = row * GRID_COLS + col;
                boolean hovered  = (boxCursorRow == row && boxCursorCol == col);
                boolean selected = selectedSlot != null && selectedSlot.isBox()
                        && selectedSlot.getBoxIndex() == currentBox
                        && selectedSlot.getIndex() == idx;
                drawSlotCell(g2, base,
                        gridX + col * cellSize, gridY + row * cellSize,
                        cellSize, pc.getBoxMember(currentBox, idx), hovered, selected);
            }
        }

        BrainRot hov = pc.getBoxMember(currentBox, boxCursorRow * GRID_COLS + boxCursorCol);
        drawDataCard(g2, base, divX, winY, winW, winH, bodyY, hov);
    }

    // ── PARTY body ────────────────────────────────────────────────────────────
    private void drawPartyBody(Graphics2D g2, Font base,
                               int winX, int winY, int winW, int winH, int bodyY) {
        int divX  = winX + (int)(winW * PANEL_SPLIT);
        int listX = winX + OUTER_PAD;
        int listW = divX - winX - OUTER_PAD - 8;
        int rowH  = 60;
        int listY = bodyY + 10;

        for (int i = 0; i < PCSystem.PARTY_CAPACITY; i++) {
            BrainRot rot = (i < pc.getPartySize()) ? pc.getPartyMember(i) : null;
            boolean selected = selectedSlot != null && selectedSlot.isParty() && selectedSlot.getIndex() == i;
            drawPartyRow(g2, base, listX, listY + i * rowH - 2, listW, rowH - 6, rot, partyCursorRow == i, selected);
        }

        BrainRot hov = (partyCursorRow < pc.getPartySize()) ? pc.getPartyMember(partyCursorRow) : null;
        drawDataCard(g2, base, divX, winY, winW, winH, bodyY, hov);
    }

    // ── Slot cell ─────────────────────────────────────────────────────────────
    private void drawSlotCell(Graphics2D g2, Font base, int x, int y, int size,
                              BrainRot rot, boolean hovered, boolean selected) {
        int pad = 3;
        Color bg = selected ? new Color(255, 230, 100) : hovered ? new Color(178, 212, 244) : new Color(220, 215, 205);
        g2.setColor(bg);
        g2.fillRoundRect(x + pad, y + pad, size - pad * 2, size - pad * 2, 6, 6);
        g2.setColor(selected ? new Color(200, 150, 20) : hovered ? new Color(24, 95, 165) : new Color(170, 165, 155));
        g2.setStroke(selected || hovered ? new BasicStroke(2) : new BasicStroke(1));
        g2.drawRoundRect(x + pad, y + pad, size - pad * 2, size - pad * 2, 6, 6);
        g2.setStroke(new BasicStroke(1));

        if (rot != null) {
            BufferedImage img = getSprite(rot);
            int ip = pad + 4;
            if (img != null) g2.drawImage(img, x + ip, y + ip, size - ip * 2, size - ip * 2, null);
            else { g2.setColor(new Color(100, 160, 220)); g2.fillOval(x + size / 2 - 8, y + size / 2 - 8, 16, 16); }
        }
    }

    // ── Party row ─────────────────────────────────────────────────────────────
    private void drawPartyRow(Graphics2D g2, Font base, int x, int y, int w, int h,
                              BrainRot rot, boolean hovered, boolean selected) {
        Color bg = selected ? new Color(255, 230, 100) : hovered ? new Color(178, 212, 244, 220) : new Color(230, 226, 218);
        g2.setColor(bg);
        g2.fillRoundRect(x, y, w, h, 8, 8);
        g2.setColor(selected ? new Color(200, 150, 20) : hovered ? new Color(24, 95, 165) : new Color(185, 180, 170));
        g2.setStroke(selected || hovered ? new BasicStroke(2) : new BasicStroke(1));
        g2.drawRoundRect(x, y, w, h, 8, 8);
        g2.setStroke(new BasicStroke(1));

        if (rot == null) {
            g2.setFont(base.deriveFont(10f));
            g2.setColor(new Color(160, 155, 148));
            g2.drawString("- Empty -", x + 16, y + h / 2 + 4);
            return;
        }

        // Avatar
        int avSize = h - 10;
        g2.setColor(new Color(200, 195, 185));
        g2.fillOval(x + 6, y + 5, avSize, avSize);
        g2.setColor(new Color(155, 150, 140));
        g2.drawOval(x + 6, y + 5, avSize, avSize);
        BufferedImage img = getSprite(rot);
        if (img != null) {
            Shape clip = g2.getClip();
            g2.setClip(new java.awt.geom.Ellipse2D.Float(x + 6, y + 5, avSize, avSize));
            g2.drawImage(img, x + 6, y + 5, avSize, avSize, null);
            g2.setClip(clip);
        }

        int tx = x + 6 + avSize + 8;

        // Name
        g2.setFont(base.deriveFont(Font.BOLD, 11f));
        g2.setColor(new Color(44, 44, 42));
        g2.drawString(rot.getName(), tx, y + 18);

        // Lvl placeholder
        g2.setFont(base.deriveFont(8f));
        g2.setColor(new Color(120, 116, 108));
        g2.drawString("LVL?? [TODO]", tx, y + 30);

        // HP bar + number
        int barY  = y + 36, barH2 = 7, hpBarW = 160, hpOff = 18;
        g2.setFont(base.deriveFont(8f));
        g2.setColor(new Color(100, 96, 90));
        g2.drawString("HP", tx, barY + barH2);
        double hpFrac = (double) rot.getCurrentHp() / rot.getMaxHp();
        g2.setColor(new Color(200, 196, 186));
        g2.fillRoundRect(tx + hpOff, barY, hpBarW - hpOff, barH2, 3, 3);
        int fillW = (int)((hpBarW - hpOff) * Math.min(1.0, Math.max(0.0, hpFrac)));
        if (fillW > 0) { g2.setColor(hpColor(rot)); g2.fillRoundRect(tx + hpOff, barY, fillW, barH2, 3, 3); }
        g2.setColor(new Color(160, 155, 145));
        g2.drawRoundRect(tx + hpOff, barY, hpBarW - hpOff, barH2, 3, 3);
        g2.setFont(base.deriveFont(8f));
        g2.setColor(new Color(64, 60, 55));
        g2.drawString(rot.getCurrentHp() + "/" + rot.getMaxHp(), tx + hpBarW + 6, barY + barH2);

        // Type badges (stacked, right-aligned)
        g2.setFont(base.deriveFont(8f));
        FontMetrics tbFm = g2.getFontMetrics();
        String primLabel = rot.getPrimaryType().name();
        String secLabel  = rot.getSecondaryType() != null ? rot.getSecondaryType().name() : null;
        int pBW = tbFm.stringWidth(primLabel) + 10;
        int sBW = secLabel != null ? tbFm.stringWidth(secLabel) + 10 : 0;
        int bW  = Math.max(pBW, sBW);
        int bH  = 14, gap = 3;
        int totalBH = secLabel != null ? bH * 2 + gap : bH;
        int bY  = y + (h - totalBH) / 2;
        int bX  = x + w - bW - 8;

        g2.setColor(typeColor(primLabel));
        g2.fillRoundRect(bX, bY, bW, bH, 4, 4);
        g2.setColor(Color.WHITE);
        g2.drawString(primLabel, bX + (bW - tbFm.stringWidth(primLabel)) / 2, bY + 11);

        if (secLabel != null) {
            int secY = bY + bH + gap;
            g2.setColor(typeColor(secLabel));
            g2.fillRoundRect(bX, secY, bW, bH, 4, 4);
            g2.setColor(Color.WHITE);
            g2.drawString(secLabel, bX + (bW - tbFm.stringWidth(secLabel)) / 2, secY + 11);
        }
    }

    // ── Data card (right panel for BOX / PARTY) ───────────────────────────────
    private void drawDataCard(Graphics2D g2, Font base,
                              int divX, int winY, int winW, int winH, int bodyY,
                              BrainRot rot) {
        int cX   = divX + 10;
        int cW   = winW - cX + 30;
        int cY   = bodyY + 4;
        int imgH = 200;
        int infoH = 130;

        // Vertical divider
        g2.setColor(new Color(200, 195, 180));
        g2.drawLine(divX, bodyY, divX, winY + winH - STATUS_BAR_H - 10);

        // Section label
        g2.setFont(base.deriveFont(Font.BOLD, 13f));
        g2.setColor(new Color(44, 44, 42));
        g2.drawString("BRAINROT DATA", cX + 28, cY + 14);

        // Image card
        int imgY = cY + 24;
        drawCard(g2, cX, imgY, cW, imgH, 8);
        if (rot != null) {
            BufferedImage img = getSprite(rot);
            if (img != null) g2.drawImage(img, cX + 10, imgY + 10, cW - 20, imgH - 20, null);
            else drawCentredText(g2, base, cX, imgY, cW, imgH, "img");
        } else {
            drawCentredText(g2, base, cX, imgY, cW, imgH, "img");
        }

        // Info card
        int infoY = imgY + imgH + 6;
        drawCard(g2, cX, infoY, cW, infoH, 8);
        int tx = cX + 14;
        int ty = infoY + 24;
        int lh = 22;

        if (rot != null) {
            g2.setFont(base.deriveFont(Font.BOLD, 14f));
            g2.setColor(new Color(44, 44, 42));
            ty = drawWrappedName(g2, base, rot.getName(), tx, ty, cW - 24, lh);

            g2.setFont(base.deriveFont(10f));
            g2.setColor(new Color(80, 76, 70));
            g2.drawString("Type: " + rot.getPrimaryType().name()
                    + (rot.getSecondaryType() != null ? " / " + rot.getSecondaryType().name() : ""), tx, ty); ty += lh;
            g2.drawString("Tier: " + rot.getTier().name(), tx, ty); ty += lh;
            g2.drawString("HP:   " + rot.getCurrentHp() + " / " + rot.getMaxHp(), tx, ty);
        } else {
            g2.setFont(base.deriveFont(10f));
            g2.setColor(new Color(140, 136, 128));
            for (String s : new String[]{ "-", "-", "-", "-" }) { g2.drawString(s, tx, ty); ty += lh; }
        }
    }

    // ── Status bar (BOX / PARTY) ──────────────────────────────────────────────
    private void drawStatusBar(Graphics2D g2, Font base, int winX, int winY, int winW, int winH) {
        int barH = STATUS_BAR_H, barY = winY + winH - barH - 8;
        int barX = winX + 8,    barW = winW - 16;

        g2.setColor(new Color(215, 210, 200));
        g2.fillRoundRect(barX, barY, barW, barH, 5, 5);

        int padding = 12;

        if (!statusMessage.isEmpty()) {
            g2.setFont(base.deriveFont(10f));
            g2.setColor(heldRot != null ? new Color(160, 110, 20) : new Color(44, 44, 42));
            FontMetrics fm = g2.getFontMetrics();
            String display = truncate(statusMessage, fm, barW / 2 - padding);
            g2.drawString(display, barX + padding, barY + (barH - fm.getHeight()) / 2 + fm.getAscent());
        }

        String line1 = (layout == Layout.BOX)
                ? "WASD Move  TAB Box  Shift Party"
                : "WS Move  Shift Box  E Info";
        String line2 = (layout == Layout.BOX)
                ? "E Info  ENT Select  ESC Cancel/Close"
                : "ENT Select  ESC Cancel/Close";

        g2.setFont(base.deriveFont(8f));
        g2.setColor(new Color(120, 116, 108));
        FontMetrics fm = g2.getFontMetrics();
        int rx = barX + barW - padding;
        g2.drawString(line1, rx - fm.stringWidth(line1), barY + 18);
        g2.drawString(line2, rx - fm.stringWidth(line2), barY + 30);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // DETAIL VIEW
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Full-window detail overlay for a single BrainRot.
     * Both sub-tabs share the same title bar, right panel, and status bar;
     * only the left panel content differs.
     */
    private void drawDetailView(Graphics2D g2, Font base, int winX, int winY, int winW, int winH) {
        // Title bar
        g2.setColor(new Color(44, 44, 42));
        g2.fillRoundRect(winX + 8, winY + 8, winW - 16, 36, 8, 8);

        String title = (detailTab == DetailTab.INFO) ? "BRAINROT INFO" : "BATTLE MOVES";
        g2.setFont(base.deriveFont(Font.BOLD, 15f));
        g2.setColor(new Color(241, 239, 232));
        g2.drawString(title, winX + 28, winY + 32);

        drawDetailTabButtons(g2, base, winX, winY, winW);

        g2.setColor(new Color(216, 184, 88));
        g2.drawLine(winX + 8, winY + 46, winX + winW - 8, winY + 46);

        int bodyY = winY + 52;
        int divX  = winX + (int)(winW * PANEL_SPLIT);

        // Vertical panel divider
        g2.setColor(new Color(200, 195, 180));
        g2.drawLine(divX, bodyY, divX, winY + winH - STATUS_BAR_H - 10);

        // Left panel - varies per sub-tab
        if (detailTab == DetailTab.INFO) drawInfoLeft (g2, base, winX, winY, winW, winH, bodyY, divX);
        else                              drawMovesLeft(g2, base, winX, winY, winW, winH, bodyY, divX);

        // Right panel - shared between both sub-tabs
        drawDetailRightPanel(g2, base, winX, winY, winW, winH, bodyY, divX);

        drawDetailStatusBar(g2, base, winX, winY, winW, winH);
    }

    // ── Sub-tab pill buttons ──────────────────────────────────────────────────
    private void drawDetailTabButtons(Graphics2D g2, Font base, int winX, int winY, int winW) {
        String[]    labels = { "INFO", "MOVES" };
        DetailTab[] tabs   = { DetailTab.INFO, DetailTab.MOVES };

        g2.setFont(base.deriveFont(Font.BOLD, 10f));
        FontMetrics fm = g2.getFontMetrics();
        int btnH = 20, padX = 12, gap = 4;
        int right = winX + winW - 16;

        for (int i = labels.length - 1; i >= 0; i--) {
            int btnW = fm.stringWidth(labels[i]) + padX;
            int btnX = right - btnW, btnY = winY + 16;
            boolean active = (detailTab == tabs[i]);
            g2.setColor(active ? new Color(216, 184, 88) : new Color(80, 78, 72));
            g2.fillRoundRect(btnX, btnY, btnW, btnH, 6, 6);
            g2.setColor(active ? new Color(44, 44, 42) : new Color(200, 196, 185));
            g2.drawString(labels[i], btnX + (btnW - fm.stringWidth(labels[i])) / 2,
                    btnY + (btnH - fm.getHeight()) / 2 + fm.getAscent());
            right = btnX - gap;
        }
    }

    // ── Shared right panel (image + name card) ────────────────────────────────

    /**
     * Used by both INFO and MOVES sub-tabs.
     * Draws the BrainRot sprite and a name / level card to the right of the divider.
     */
    private void drawDetailRightPanel(Graphics2D g2, Font base,
                                      int winX, int winY, int winW, int winH,
                                      int bodyY, int divX) {
        if (detailRot == null) return;

        // ── Panel bounds ──────────────────────────────────────────────────────
        // Adjusted to match cX = divX + 10 and cW logic
        int panelX = divX + 10;
        int panelW = winW - (panelX - winX) - 20;
        int panelY = bodyY + 6;

        // ── Image card (Matches imgH = 200 style) ──────────────────────────
        int imgCardH = 200;
        int imgMargin = 10;
        drawCard(g2, panelX, panelY, panelW, imgCardH, 8);

        BufferedImage img = getSprite(detailRot);
        if (img != null) {
            // Drawing with 10px internal padding as seen in drawDataCard
            g2.drawImage(img, panelX + imgMargin, panelY + imgMargin,
                    panelW - (imgMargin * 2), imgCardH - (imgMargin * 2), null);
        } else {
            drawCentredText(g2, base, panelX, panelY, panelW, imgCardH, "img");
        }

        // ── Name & Level card (Matches infoH = 130 style) ─────────────────────
        int infoCardY = panelY + imgCardH + 6; // 6px gap between cards
        int infoCardH = 150;
        drawCard(g2, panelX, infoCardY, panelW, infoCardH, 8);

        // Padding and line height matching drawDataCard
        int tx = panelX + 14;
        int ty = infoCardY + 24;
        int lh = 20;

        // Name - Bold 14pt
        g2.setFont(base.deriveFont(Font.BOLD, 14f));
        g2.setColor(new Color(44, 44, 42));
        ty = drawWrappedName(g2, base, detailRot.getName(), tx, ty, panelW - 24, lh);

        // Level & Type Info - 10pt
        g2.setFont(base.deriveFont(10f));
        g2.setColor(new Color(80, 76, 70));
        g2.drawString("Lv. ??", tx, ty);

        // XP Bar - Positioned below the level/type line
        ty += 12; // Gap before bar
        int barW = panelW - 28;
        drawLabeledBar(g2, base, tx, ty, barW, 8, "XP",
                0.0, new Color(120, 200, 100), new Color(200, 196, 186));

        // Footer text - 8pt
        g2.setFont(base.deriveFont(8f));
        g2.setColor(new Color(140, 136, 128));
        g2.drawString("XP system TODO", tx, ty + 18);
    }

    // ── INFO left panel ───────────────────────────────────────────────────────
    private void drawInfoLeft(Graphics2D g2, Font base,
                              int winX, int winY, int winW, int winH,
                              int bodyY, int divX) {
        if (detailRot == null) return;

        // ── Panel bounds ──────────────────────────────────────────────────────
        int panelX = winX + OUTER_PAD;                           // left edge of left panel
        int panelW = divX - winX - OUTER_PAD - 8;               // panel width (8px gap before divider)
        int panelY = bodyY + 8;                                  // top of panel content
        int panelH = winH - (bodyY - winY) - STATUS_BAR_H - 16; // total available height

        // ── Shared card layout constants ──────────────────────────────────────
        int cardPadX    = 12;  // horizontal inner padding inside every card
        int cardPadTop  = 24;  // y offset for card section title from card top
        int cardGap     = 6;  // vertical gap between stacked cards
        int rowLineH    = 26;  // baseline-to-baseline height for each label+badge row

        // ── Classification card ───────────────────────────────────────────────
        int classCardH = 100;
        drawCard(g2, panelX, panelY, panelW, classCardH, 8);

        // Section title
        g2.setFont(base.deriveFont(Font.BOLD, 13f));
        g2.setColor(new Color(44, 44, 42));
        g2.drawString("Classification", panelX + cardPadX, panelY + cardPadTop);

        // Type row - label baseline aligns with badge centre
        int badgeH      = 20;
        int labelIndent = cardPadX + 2;      // x for "Type /" and "Tier /" labels
        int badgeOffX   = labelIndent + 54;  // x where badges start (after label text)

        int typeRowY    = panelY + cardPadTop + rowLineH;        // baseline for "Type /" label
        int typeBadgeY  = typeRowY - badgeH + 8;                 // badge top aligned to label baseline

        g2.setFont(base.deriveFont(10f));
        g2.setColor(new Color(80, 76, 70));
        g2.drawString("Type", panelX + labelIndent, typeRowY);

        drawTypeBadge(g2, base, detailRot.getPrimaryType().name(), panelX + badgeOffX, typeBadgeY, badgeH);
        if (detailRot.getSecondaryType() != null) {
            g2.setFont(base.deriveFont(8f));
            int primBadgeW = g2.getFontMetrics().stringWidth(detailRot.getPrimaryType().name()) + 14;
            drawTypeBadge(g2, base, detailRot.getSecondaryType().name(),
                    panelX + badgeOffX + primBadgeW + 4, typeBadgeY, badgeH);
        }

        // Tier row - same x alignment as type row, one rowLineH below
        int tierRowY   = typeRowY + rowLineH;
        int tierBadgeY = tierRowY - badgeH + 8;

        g2.setFont(base.deriveFont(10f));
        g2.setColor(new Color(80, 76, 70));
        g2.drawString("Tier", panelX + labelIndent, tierRowY);

        String tierLabel = detailRot.getTier().name();
        g2.setFont(base.deriveFont(8f));
        FontMetrics tierFm = g2.getFontMetrics();
        int tierBadgeW = tierFm.stringWidth(tierLabel) + 14;
        g2.setColor(tierColor(detailRot.getTier()));
        g2.fillRoundRect(panelX + badgeOffX, tierBadgeY, tierBadgeW, badgeH, 4, 4);
        g2.setColor(Color.WHITE);
        g2.drawString(tierLabel, panelX + badgeOffX + 7, tierBadgeY + 12);

        // ── Stats card ────────────────────────────────────────────────────────
        int statsCardY = panelY + classCardH + cardGap;
        int statsCardH = 140;
        drawCard(g2, panelX, statsCardY, panelW, statsCardH, 8);

        // Section title
        g2.setFont(base.deriveFont(Font.BOLD, 13f));
        g2.setColor(new Color(44, 44, 42));
        g2.drawString("Stats", panelX + cardPadX, statsCardY + cardPadTop);

        // Stat rows - consistent indent and row spacing
        int statContentX = panelX + cardPadX + 4; // x for stat labels / bar start
        int statBarW     = panelW - cardPadX * 2 - 8;
        int statBarH     = 8;
        int statRowH     = 28; // baseline-to-baseline for each stat row
        int firstStatY   = statsCardY + cardPadTop + 14; // top of first bar

        double hpFrac = (double) detailRot.getCurrentHp() / detailRot.getMaxHp();
        drawStatRow(g2, base, statContentX, firstStatY, statBarW, statBarH,
                "HP", detailRot.getCurrentHp() + "/" + detailRot.getMaxHp(),
                hpFrac, hpColor(detailRot));

        drawStatPlain(g2, base, statContentX, firstStatY + statRowH,
                "Def", String.valueOf(detailRot.getDefense()));

        drawStatPlain(g2, base, statContentX, firstStatY + statRowH * 2,
                "Spd", String.valueOf(detailRot.getSpeed()));
    }

    // ── MOVES left panel ──────────────────────────────────────────────────────
    private void drawMovesLeft(Graphics2D g2, Font base,
                               int winX, int winY, int winW, int winH,
                               int bodyY, int divX) {
        if (detailRot == null) return;

        // ── Panel bounds ──────────────────────────────────────────────────────
        int panelX = winX + OUTER_PAD;
        int panelW = divX - winX - OUTER_PAD - 8;
        int panelY = bodyY + 8;
        int panelH = winH - (bodyY - winY) - STATUS_BAR_H - 16;

        // ── Shared card layout constants ──────────────────────────────────────
        int cardPadX    = 12; // horizontal inner padding inside every card
        int cardPadTop  = 14; // y offset for card section title from card top
        int cardGap     = 6; // vertical gap between stacked cards

        List<Skill> moves = detailRot.getMoves();
        if (!moves.isEmpty() && movesCursor >= moves.size()) movesCursor = moves.size() - 1;

        // ── Moves card ──────────────────────────
        int movesCardH    = 190;
        int movesCardBotY = panelY + movesCardH;
        drawCard(g2, panelX, panelY, panelW, movesCardH, 8);

        // Section title
        g2.setFont(base.deriveFont(Font.BOLD, 13f));
        g2.setColor(new Color(44, 44, 42));
        g2.drawString("Moves", panelX + cardPadX, panelY + cardPadTop + 10);

        // Move rows - evenly divide space below the title
        int rowListY = panelY + cardPadTop + 16;      // top of first row
        int rowH     = 38; // height per row

        int badgeH         = 16; // type badge height inside each row
        int badgePadX      = 5;  // text padding inside badge
        int rowTextOffsetY = rowH / 2 + 5; // baseline offset within a row (vertically centred)
        int spRightMarginX = panelX + panelW - cardPadX; // right edge for SP cost text

        for (int i = 0; i < 4; i++) {
            boolean hasMv   = (i < moves.size());
            boolean hovered = (i == movesCursor);
            int rowY = rowListY + i * rowH;

            // Hover highlight
            if (hovered) {
                g2.setColor(new Color(178, 212, 244, 180));
                g2.fillRoundRect(panelX + 6, rowY, panelW - 12, rowH - 2, 5, 5);
                g2.setColor(new Color(24, 95, 165));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(panelX + 6, rowY, panelW - 12, rowH - 2, 5, 5);
                g2.setStroke(new BasicStroke(1));
            }

            if (hasMv) {
                Skill mv = moves.get(i);

                // Type badge
                g2.setFont(base.deriveFont(8f));
                FontMetrics badgeFm = g2.getFontMetrics();
                int badgeW  = badgeFm.stringWidth(mv.getType().name()) + badgePadX * 2;
                int badgeX  = panelX + cardPadX;
                int badgeTopY = rowY + (rowH - badgeH) / 2; // badge vertically centred in row
                g2.setColor(typeColor(mv.getType().name()));
                g2.fillRoundRect(badgeX, badgeTopY, badgeW, badgeH, 4, 4);
                g2.setColor(Color.WHITE);
                g2.drawString(mv.getType().name(), badgeX + badgePadX, badgeTopY + 11);

                // Skill name - starts right after the badge with a small gap
                int nameX = 180;
                g2.setFont(base.deriveFont(hovered ? Font.BOLD : Font.PLAIN, 11f));
                g2.setColor(new Color(44, 44, 42));
                g2.drawString(mv.getName(), nameX, rowY + rowTextOffsetY);

                // SP cost - right-aligned
                String spStr = "UP " + mv.getSpCost() + "/" + mv.getSpCost();
                g2.setFont(base.deriveFont(10f));
                g2.setColor(new Color(80, 76, 70));
                FontMetrics spFm = g2.getFontMetrics();
                g2.drawString(spStr, spRightMarginX - spFm.stringWidth(spStr), rowY + rowTextOffsetY);

            } else {
                g2.setFont(base.deriveFont(10f));
                g2.setColor(new Color(170, 165, 158));
                g2.drawString("-", panelX + cardPadX + 4, rowY + rowTextOffsetY);
            }

            // Row divider (skip after last row)
            if (i < 3) {
                int dividerY = rowY + rowH - 1;
                g2.setColor(new Color(195, 190, 180));
                g2.drawLine(panelX + cardPadX, dividerY, panelX + panelW - cardPadX, dividerY);
            }
        }

        // ── Description card (lower, remaining height) ────────────────────────
        int descCardY = movesCardBotY + cardGap;
        int descCardH = 110;
        drawCard(g2, panelX, descCardY, panelW, descCardH, 8);

        // Section title
        g2.setFont(base.deriveFont(Font.BOLD, 13f));
        g2.setColor(new Color(44, 44, 42));
        g2.drawString("Description", panelX + cardPadX, descCardY + cardPadTop + 10);

        // Description text - starts one line below the title
        int descTextY = descCardY + cardPadTop + 30;
        int descTextW = panelW - cardPadX * 2;

        if (!moves.isEmpty() && movesCursor < moves.size()) {
            drawWrappedText(g2, base.deriveFont(10f),
                    buildTempDescription(moves.get(movesCursor)),
                    panelX + cardPadX, descTextY, descTextW, 15, new Color(64, 60, 55));
        } else {
            g2.setFont(base.deriveFont(10f));
            g2.setColor(new Color(150, 146, 138));
            g2.drawString("No moves learned yet.", panelX + cardPadX, descTextY);
        }
    }

    // ── Detail status bar ─────────────────────────────────────────────────────
    private void drawDetailStatusBar(Graphics2D g2, Font base, int winX, int winY, int winW, int winH) {
        int barH = STATUS_BAR_H, barY = winY + winH - barH - 8;
        g2.setColor(new Color(215, 210, 200));
        g2.fillRoundRect(winX + 8, barY, winW - 16, barH, 5, 5);

        String line1 = (detailTab == DetailTab.INFO) ? "TAB Moves" : "WS Move  TAB Info";
        String line2 = "ESC Back";
        g2.setFont(base.deriveFont(8f));
        g2.setColor(new Color(120, 116, 108));
        FontMetrics fm = g2.getFontMetrics();
        int rx = winX + winW - 8 - 12;
        g2.drawString(line1, rx - fm.stringWidth(line1), barY + 18);
        g2.drawString(line2, rx - fm.stringWidth(line2), barY + 30);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // SHARED DRAW HELPERS
    // ═════════════════════════════════════════════════════════════════════════

    /** Rounded card background + border. */
    private void drawCard(Graphics2D g2, int x, int y, int w, int h, int arc) {
        g2.setColor(new Color(230, 226, 218));
        g2.fillRoundRect(x, y, w, h, arc, arc);
        g2.setColor(new Color(190, 185, 172));
        g2.drawRoundRect(x, y, w, h, arc, arc);
    }

    /** "LABEL [bar] value" stat row - used for HP. */
    private void drawStatRow(Graphics2D g2, Font base,
                             int x, int y, int totalW, int barH,
                             String label, String valueText,
                             double fraction, Color fill) {
        g2.setFont(base.deriveFont(10f));
        FontMetrics fm = g2.getFontMetrics();

        // 1. Setup Measurements
        int labelWidth = fm.stringWidth(label);
        int valueWidth = fm.stringWidth(valueText);
        int padding = 12;

        // 2. Calculate Bar Positioning
        // barX starts right after the label text
        int barX = x + labelWidth + padding;

        // bw (Bar Width) takes up all remaining space between label and value
        int bw = totalW - labelWidth - valueWidth - (padding * 2);

        // 3. Vertical Centering Math
        int textY = y + (barH / 2) + (fm.getAscent() / 2) - 1;

        // 4. Draw Label
        g2.setColor(new Color(80, 76, 70));
        g2.drawString(label, x, textY);

        // 5. Draw Bar Background (The full "empty" slot)
        g2.setColor(new Color(200, 196, 186));
        g2.fillRoundRect(barX, y, bw, barH, 4, 4);

        // 6. Draw Bar Fill (Adjusted by HP fraction)
        double safeFraction = Math.max(0.0, Math.min(1.0, fraction));
        int fillW = (int) (bw * safeFraction);

        if (fillW > 0) {
            g2.setColor(fill);
            // We use fillW here to represent the actual HP level
            g2.fillRoundRect(barX, y, fillW, barH, 4, 4);
        }

        // 7. Draw Bar Outline
        g2.setColor(new Color(160, 155, 145));
        g2.drawRoundRect(barX, y, bw, barH, 4, 4);

        // 8. Draw Value Text (Right-aligned after the bar)
        g2.setColor(new Color(64, 60, 55));
        g2.drawString(valueText, barX + bw + padding, textY);
    }

    /** "LABEL  value" plain stat row - used for Def and Spd. */
    private void drawStatPlain(Graphics2D g2, Font base, int x, int y, String label, String value) {
        g2.setFont(base.deriveFont(10f));
        g2.setColor(new Color(80, 76, 70));
        g2.drawString(label, x, y + 10);
        g2.setFont(base.deriveFont(Font.BOLD, 11f));
        g2.setColor(new Color(44, 44, 42));
        g2.drawString(value, x + 34, y + 10);
    }

    /** Colored type badge pill. */
    private void drawTypeBadge(Graphics2D g2, Font base, String typeName, int x, int y, int h) {
        g2.setFont(base.deriveFont(8f));
        FontMetrics fm = g2.getFontMetrics();
        int w = fm.stringWidth(typeName) + 14;
        g2.setColor(typeColor(typeName));
        g2.fillRoundRect(x, y, w, h, 4, 4);
        g2.setColor(Color.WHITE);
        g2.drawString(typeName, x + 7, y + 12);
    }

    /** Labeled bar (e.g. XP bar). */
    private void drawLabeledBar(Graphics2D g2, Font base,
                                int x, int y, int w, int h, String label,
                                double fraction, Color fill, Color bg) {
        int off = 26;
        g2.setFont(base.deriveFont(8f));
        g2.setColor(new Color(100, 96, 90));
        g2.drawString(label, x, y + h);
        g2.setColor(bg);
        g2.fillRoundRect(x + off, y, w - off, h, 3, 3);
        int fillW = (int)((w - off) * Math.min(1.0, Math.max(0.0, fraction)));
        if (fillW > 0) { g2.setColor(fill); g2.fillRoundRect(x + off, y, fillW, h, 3, 3); }
        g2.setColor(new Color(160, 155, 145));
        g2.drawRoundRect(x + off, y, w - off, h, 3, 3);
    }

    /** Word-wrap text renderer. */
    private void drawWrappedText(Graphics2D g2, Font font, String text,
                                 int x, int y, int maxWidth, int lineHeight, Color color) {
        g2.setFont(font);
        g2.setColor(color);
        FontMetrics fm = g2.getFontMetrics();
        StringBuilder line = new StringBuilder();
        int cy = y;
        for (String word : text.split(" ")) {
            String test = line.isEmpty() ? word : line + " " + word;
            if (fm.stringWidth(test) > maxWidth && !line.isEmpty()) {
                g2.drawString(line.toString(), x, cy);
                cy += lineHeight;
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(test);
            }
        }
        if (!line.isEmpty()) g2.drawString(line.toString(), x, cy);
    }

    /**
     * Draws a BrainRot name, wrapping onto a second line if it overflows maxW.
     * Returns the y position after the name block (so callers can continue below).
     */
    private int drawWrappedName(Graphics2D g2, Font base, String name, int x, int y, int maxW, int lineH) {
        FontMetrics fm = g2.getFontMetrics();
        if (fm.stringWidth(name) <= maxW) {
            g2.drawString(name, x, y);
            return y + lineH;
        }
        String[] words = name.split(" ");
        StringBuilder first = new StringBuilder();
        int split = 0;
        for (int i = 0; i < words.length; i++) {
            String t = first.isEmpty() ? words[i] : first + " " + words[i];
            if (fm.stringWidth(t) <= maxW) { first = new StringBuilder(t); split = i; }
            else break;
        }
        g2.setFont(base.deriveFont(Font.BOLD, 11f));
        g2.drawString(first.toString(), x, y);
        StringBuilder second = new StringBuilder();
        for (int i = split + 1; i < words.length; i++) second.append(i > split + 1 ? " " : "").append(words[i]);
        if (second.length() > 0) { g2.drawString(second.toString(), x, y + lineH); return y + lineH * 2; }
        return y + lineH;
    }

    /** Centred placeholder text inside a card. */
    private void drawCentredText(Graphics2D g2, Font base, int bx, int by, int bw, int bh, String text) {
        g2.setFont(base.deriveFont(10f));
        g2.setColor(new Color(150, 145, 138));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(text, bx + (bw - fm.stringWidth(text)) / 2, by + bh / 2 + 4);
    }

    /** Truncates a string with "…" if it exceeds maxPx pixels wide. */
    private String truncate(String text, FontMetrics fm, int maxPx) {
        if (fm.stringWidth(text) <= maxPx) return text;
        while (text.length() > 0 && fm.stringWidth(text + "…") > maxPx) text = text.substring(0, text.length() - 1);
        return text + "…";
    }

    /** Temp move description from effect tag until Skill gains a description field. */
    private String buildTempDescription(Skill skill) {
        String suffix = switch (skill.getEffect().toUpperCase()) {
            case "BURN"      -> " May inflict BURN.";
            case "PARALYZE"  -> " May inflict PARALYZE.";
            case "CONFUSE"   -> " May CONFUSE the target.";
            case "FLINCH"    -> " May cause FLINCH.";
            case "LOWER_DEF" -> " Lowers target Defense.";
            case "LOWER_ATK" -> " Lowers target Attack.";
            case "LOWER_SPD" -> " Lowers target Speed.";
            case "RAISE_ATK" -> " Raises user Attack.";
            case "RAISE_DEF" -> " Raises user Defense.";
            case "RAISE_SPD" -> " Raises user Speed.";
            case "HEAL"      -> " Restores user HP.";
            default          -> " No additional effect.";
        };
        String pwr = (skill.getPower() > 0) ? "Power: " + skill.getPower() + "." : " Status move.";
        return skill.getName() + " - " + skill.getType().name().toLowerCase() + "-type." + pwr + suffix;
    }

    // ── Sprite loading ────────────────────────────────────────────────────────

    private BufferedImage getSprite(BrainRot rot) {
        String key = rot.getName() + "_" + rot.getTier().name();
        if (spriteCache.containsKey(key)) return spriteCache.get(key);
        String path = "/assets/Sprites/Brainrots/" + toFolderName(rot.getName())
                + "/" + rot.getTier().name() + "_1.png";
        BufferedImage img = AssetManager.loadImage(path);
        spriteCache.put(key, img);
        System.out.println("[PCUI] Sprite " + (img != null ? "loaded" : "MISSING") + ": " + path);
        return img;
    }

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

    // ── Color helpers ─────────────────────────────────────────────────────────

    private Color hpColor(BrainRot rot) {
        double r = (double) rot.getCurrentHp() / rot.getMaxHp();
        return r > 0.5 ? new Color(60, 180, 80) : r > 0.25 ? new Color(220, 180, 40) : new Color(210, 60, 60);
    }

    private Color tierColor(Tier tier) {
        return switch (tier) {
            case DIAMOND -> new Color(100, 200, 240);
            case GOLD    -> new Color(216, 184, 88);
            default      -> new Color(160, 155, 145);
        };
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
     * Sets the status message.
     * @param persist  true = message stays until explicitly cleared (e.g. while holding a BrainRot)
     *                 false = message auto-clears after STATUS_TICKS frames
     */
    private void setStatus(String msg, boolean persist) {
        statusMessage = msg;
        statusTimer   = persist ? -1 : STATUS_TICKS;
        System.out.println("[PCUI] Status: " + msg);
    }

    // ── Public accessor ───────────────────────────────────────────────────────

    public PCSystem getPCSystem() { return pc; }
}