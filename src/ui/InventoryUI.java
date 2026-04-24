package ui;

import brainrots.BrainRot;
import engine.GamePanel;
import items.*;
import skills.Skill;
import skills.SkillPool;
import utils.AssetManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

import static utils.Constants.*;

public class InventoryUI {

    private enum Tab    { ITEMS, CAPSULES }
    private enum Layout { ITEM_LIST, PARTY_SELECT, MOVE_SWAP }

    private final GamePanel gp;

    private Tab    activeTab  = Tab.ITEMS;
    private Layout layout     = Layout.ITEM_LIST;

    private int itemCursor   = 0;
    private int scrollOffset = 0;
    private int partyCursor  = 0;
    private int moveCursor   = 0;

    private int inputCooldown = 0;

    private String statusMessage = "";
    private int    statusTimer   = 0;

    private Item     pendingItem         = null;
    private BrainRot pendingScrollTarget = null;

    // --- BATTLE HOOKS ---
    private boolean openedInBattle = false;
    private items.Item selectedItemForBattle = null;

    private final Map<String, BufferedImage> imgCache = new HashMap<>();

    public InventoryUI(GamePanel gp) {
        this.gp = gp;
    }

    public Item getSelectedItemForBattle() {
        return selectedItemForBattle;
    }

    public void clearSelectedItemForBattle() {
        selectedItemForBattle = null;
        openedInBattle = false;
    }

    public void open() {
        this.openedInBattle = false;
        resetUIState();
        System.out.println("[InventoryUI] Opened.");
    }

    public void openInBattle() {
        this.openedInBattle = true;
        this.selectedItemForBattle = null;
        resetUIState();
        System.out.println("[InventoryUI] Opened in Battle.");
    }

    private void resetUIState() {
        activeTab           = Tab.ITEMS;
        layout              = Layout.ITEM_LIST;
        itemCursor          = 0;
        scrollOffset        = 0;
        partyCursor         = 0;
        moveCursor          = 0;
        pendingItem         = null;
        pendingScrollTarget = null;
        statusMessage       = "";
        statusTimer         = 0;
        inputCooldown       = INPUT_DELAY * 2;
    }

    public void update() {
        if (inputCooldown > 0) { inputCooldown--; return; }
        if (statusTimer   > 0) { statusTimer--;   if (statusTimer == 0) statusMessage = ""; }

        switch (layout) {
            case ITEM_LIST    -> updateItemList();
            case PARTY_SELECT -> updatePartySelect();
            case MOVE_SWAP    -> updateMoveSwap();
        }
    }

    private void updateItemList() {
        if (gp.KEYBOARDHANDLER.tabPressed) {
            gp.KEYBOARDHANDLER.tabPressed = false;
            activeTab    = (activeTab == Tab.ITEMS) ? Tab.CAPSULES : Tab.ITEMS;
            itemCursor   = 0;
            scrollOffset = 0;
            clearStatus();
            inputCooldown = INPUT_DELAY;
            return;
        }

        if (gp.KEYBOARDHANDLER.escPressed) {
            gp.KEYBOARDHANDLER.escPressed = false;
            close();
            return;
        }

        List<Item> list = currentList();
        int size = list.size();

        if (gp.KEYBOARDHANDLER.upPressed && itemCursor > 0) {
            itemCursor--;
            clampScroll(computeVisibleCount());
            inputCooldown = INPUT_DELAY;
            return;
        }
        if (gp.KEYBOARDHANDLER.downPressed && itemCursor < size - 1) {
            itemCursor++;
            clampScroll(computeVisibleCount());
            inputCooldown = INPUT_DELAY;
            return;
        }

        if (gp.KEYBOARDHANDLER.enterPressed) {
            gp.KEYBOARDHANDLER.enterPressed = false;
            if (size == 0) { setStatus("Your bag is empty!"); inputCooldown = INPUT_DELAY; return; }
            handleItemUse(list.get(itemCursor));
            inputCooldown = INPUT_DELAY;
        }
    }

    private void clampScroll(int visibleCount) {
        if (itemCursor < scrollOffset) scrollOffset = itemCursor;
        if (itemCursor >= scrollOffset + visibleCount) scrollOffset = itemCursor - visibleCount + 1;
        if (scrollOffset < 0) scrollOffset = 0;
    }

    private void handleItemUse(Item item) {
        if (openedInBattle) {
            if (item instanceof Capsule) {
                this.selectedItemForBattle = item;
                return;
            } else {
                pendingItem = item;
                partyCursor = 0;
                layout = Layout.PARTY_SELECT;
                return;
            }
        }

        if (item instanceof Capsule) {
            setStatus("Can't use capsules outside of battle!");
            return;
        }
        if (gp.player.getPCSYSTEM().getPartySize() == 0) {
            setStatus("No BrainRots in your party!");
            return;
        }
        pendingItem = item;
        partyCursor = 0;
        layout      = Layout.PARTY_SELECT;
        clearStatus();
    }

    private void updatePartySelect() {
        int partySize = gp.player.getPCSYSTEM().getPartySize();

        if (gp.KEYBOARDHANDLER.escPressed) {
            gp.KEYBOARDHANDLER.escPressed = false;
            cancelToItemList("Cancelled.");
            return;
        }
        if (gp.KEYBOARDHANDLER.upPressed && partyCursor > 0) {
            partyCursor--; inputCooldown = INPUT_DELAY; return;
        }
        if (gp.KEYBOARDHANDLER.downPressed && partyCursor < partySize - 1) {
            partyCursor++; inputCooldown = INPUT_DELAY; return;
        }
        if (gp.KEYBOARDHANDLER.enterPressed) {
            gp.KEYBOARDHANDLER.enterPressed = false;
            BrainRot target = gp.player.getPCSYSTEM().getPartyMember(partyCursor);

            if (openedInBattle) {
                this.selectedItemForBattle = pendingItem;
                return;
            }

            if (target != null) applyItemToTarget(target);
            inputCooldown = INPUT_DELAY;
        }
    }

    private void applyItemToTarget(BrainRot target) {
        if      (pendingItem instanceof Stew     s) applyStew(s, target);
        else if (pendingItem instanceof Antidote a) applyAntidote(a, target);
        else if (pendingItem instanceof Scroll   s) applyScroll(s, target);
        else if (pendingItem instanceof UPBottle b) {
            // move to skill-select overlay
            pendingScrollTarget = target;
            moveCursor = 0;
            layout = Layout.MOVE_SWAP;
            clearStatus();
        }
    }

    private void applyStew(Stew stew, BrainRot target) {
        if (target.getCurrentHp() >= target.getMaxHp()) {
            setStatus(target.getName() + " HP is full!"); return;
        }
        int before = target.getCurrentHp();
        stew.use(target);
        int healed = target.getCurrentHp() - before;
        consumePendingItem();
        progression.QuestSystem.getInstance().onItemUsed("STEW");
        setStatus(target.getName() + " +" + healed + " HP!");
        returnToItemList();
    }

    private void applyAntidote(Antidote antidote, BrainRot target) {
        String cure = antidote.getStatusToCure().toUpperCase();

        if (cure.equals("DEBUFF")) {
            if (!hasDebuffs(target)) {
                setStatus("No stat debuffs to clear!");
                return;
            }
            antidote.use(target);
            consumePendingItem();
            progression.QuestSystem.getInstance().onItemUsed("ANTIDOTE");
            setStatus(target.getName() + " debuffs cleared!");
            returnToItemList();
            return;
        }

        if (!target.hasStatus(cure)) {
            String shortName = target.getName();
            setStatus(shortName + " isn't " + capitalize(cure.toLowerCase()) + "!");
            return;
        }
        antidote.use(target);
        consumePendingItem();
        String shortName = target.getName();
        setStatus(shortName + " cured!");
        returnToItemList();
    }

    private boolean hasDebuffs(BrainRot target) {
        return target.hasActiveDebuffs();
    }

    private void applyScroll(Scroll scroll, BrainRot target) {
        Scroll.ScrollResult result = scroll.validate(target);
        switch (result) {
            case SUCCESS -> {
                scroll.apply(target, null);
                consumePendingItem();
                progression.QuestSystem.getInstance().onItemUsed("SCROLL");
                progression.QuestSystem.getInstance().checkSkillCollector(gp.player.getPCSYSTEM().getParty());
                String shortName = target.getName();
                setStatus(shortName + " learned " + scroll.getSkillName() + "!");
                returnToItemList();
            }
            case MOVESET_FULL -> {
                pendingScrollTarget = target;
                moveCursor = 0;
                layout     = Layout.MOVE_SWAP;
                clearStatus();
            }
            case TYPE_MISMATCH -> {
                String shortName = target.getName();
                setStatus(shortName + " can't learn this!");
            }
            case SIGNATURE_MISMATCH -> {
                String owner = findSignatureOwner(scroll.getSkillName());
                setStatus("Only " + (owner != null ? owner : "its owner") + " can learn this!");
            }
            case ALREADY_KNOWN -> {
                String shortName = target.getName();
                setStatus(shortName + " already knows this!");
            }
            case SKILL_NOT_FOUND -> setStatus("Error: skill not found");
            default              -> setStatus("Cannot use this scroll");
        }
        inputCooldown = INPUT_DELAY;
    }

    private void updateMoveSwap() {
        if (pendingScrollTarget == null) { cancelToItemList("Cancelled."); return; }
        int moveCount = pendingScrollTarget.getMoves().size();

        if (gp.KEYBOARDHANDLER.escPressed) {
            gp.KEYBOARDHANDLER.escPressed = false;
            pendingScrollTarget = null;
            cancelToItemList("Scroll cancelled.");
            return;
        }
        if (gp.KEYBOARDHANDLER.upPressed   && moveCursor > 0)            { moveCursor--; inputCooldown = INPUT_DELAY; return; }
        if (gp.KEYBOARDHANDLER.downPressed  && moveCursor < moveCount - 1){ moveCursor++; inputCooldown = INPUT_DELAY; return; }

        if (gp.KEYBOARDHANDLER.enterPressed) {
            gp.KEYBOARDHANDLER.enterPressed = false;
            if (pendingItem instanceof Scroll scroll) {
                Scroll.ScrollResult result = scroll.apply(pendingScrollTarget, moveCursor);
                if (result == Scroll.ScrollResult.SWAPPED) {
                    consumePendingItem();
                    progression.QuestSystem.getInstance().onItemUsed("SCROLL");
                    progression.QuestSystem.getInstance().checkSkillCollector(gp.player.getPCSYSTEM().getParty());
                    setStatus(pendingScrollTarget.getName() + " learned " + scroll.getSkillName() + "!");
                } else {
                    setStatus("Could not swap move");
                }
            } else if (pendingItem instanceof UPBottle bottle) {
                skills.Skill chosenSkill = pendingScrollTarget.getMoves().get(moveCursor);
                if (bottle.isSkillFull(chosenSkill)) {
                    setStatus(chosenSkill.getName() + " already at max UP!");
                    inputCooldown = INPUT_DELAY;
                    return; // stay in MOVE_SWAP so player can pick another
                }
                bottle.use(pendingScrollTarget, moveCursor);
                consumePendingItem();
                progression.QuestSystem.getInstance().onItemUsed("UP_BOTTLE");
                setStatus(chosenSkill.getName() + " UP restored!");
            }
            pendingScrollTarget = null;
            returnToItemList();
            inputCooldown = INPUT_DELAY;
        }
    }

    private void returnToItemList() {
        layout      = Layout.ITEM_LIST;
        pendingItem = null;
        int size = currentList().size();
        if (size == 0) { itemCursor = 0; scrollOffset = 0; }
        else if (itemCursor >= size) { itemCursor = size - 1; clampScroll(computeVisibleCount()); }
    }

    private void cancelToItemList(String msg) {
        pendingItem         = null;
        pendingScrollTarget = null;
        layout              = Layout.ITEM_LIST;
        if (msg != null && !msg.isEmpty()) setStatus(msg);
        inputCooldown = INPUT_DELAY;
    }

    private void close() {
        if (openedInBattle) {
            gp.GAMESTATE = "battle";
            openedInBattle = false;
        } else {
            gp.GAMESTATE = "menu";
            gp.MENUUI.open();
        }
        System.out.println("[InventoryUI] Closed.");
    }

    private void consumePendingItem() {
        if (pendingItem == null) return;
        gp.player.getInventory().removeItem(pendingItem);
        pendingItem = null;
    }

    private List<Item> currentList() {
        List<Item> all = gp.player.getInventory().getRawItems();
        List<Item> out = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        for (Item item : all) {
            boolean cap = (item instanceof Capsule);
            if (activeTab == Tab.CAPSULES &&  cap && seen.add(item.getName())) out.add(item);
            if (activeTab == Tab.ITEMS    && !cap && seen.add(item.getName())) out.add(item);
        }
        return out;
    }

    public void draw(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Font base = (AssetManager.pokemonGb != null)
                ? AssetManager.pokemonGb : new Font("Monospaced", Font.PLAIN, 10);

        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        int winX = TILE_SIZE, winY = TILE_SIZE;
        int winW = SCREEN_WIDTH  - TILE_SIZE * 2;
        int winH = SCREEN_HEIGHT - TILE_SIZE * 2;

        drawWindow(g2, winX, winY, winW, winH);
        drawTitleBar(g2, base, winX, winY, winW);

        int bodyY = winY + 52;

        int statusBarH = STATUS_BAR_H;
        int statusBarY = winY + winH - statusBarH - 8;

        int leftW = (int)(winW * LEFT_SPLIT / 100.0);
        int divX  = winX + leftW;

        g2.setColor(new Color(200, 195, 180));
        g2.drawLine(divX, bodyY, divX, statusBarY - 4);

        drawLeftPanel (g2, base, winX, bodyY, leftW, statusBarY);
        drawRightPanel(g2, base, winX, winW,  bodyY, divX, statusBarY);

        if (layout == Layout.PARTY_SELECT) drawPartySelectOverlay(g2, base, winX, winY, winW, winH);
        if (layout == Layout.MOVE_SWAP)    drawMoveSwapOverlay   (g2, base, winX, winY, winW, winH);

        drawStatusBar(g2, base, winX, winY, winW, statusBarY, statusBarH);
    }

    private void drawTitleBar(Graphics2D g2, Font base, int winX, int winY, int winW) {
        g2.setColor(new Color(44, 44, 42));
        g2.fillRoundRect(winX + 8, winY + 8, winW - 16, 36, 8, 8);

        g2.setFont(base.deriveFont(Font.BOLD, 15f));
        g2.setColor(new Color(241, 239, 232));
        g2.drawString("BACKPACK", winX + 28, winY + 32);

        String[] labels = { "ITEMS", "CAPSULES" };
        Tab[]    tabs   = { Tab.ITEMS, Tab.CAPSULES };

        g2.setFont(base.deriveFont(Font.BOLD, 10f));
        FontMetrics fm = g2.getFontMetrics();
        int btnH = 22, gap = 4, right = winX + winW - 16;

        for (int i = labels.length - 1; i >= 0; i--) {
            int btnW = fm.stringWidth(labels[i]) + 16;
            int btnX = right - btnW, btnY = winY + 14;
            boolean active = (activeTab == tabs[i]);
            g2.setColor(active ? new Color(216, 184, 88) : new Color(80, 78, 72));
            g2.fillRoundRect(btnX, btnY, btnW, btnH, 6, 6);
            g2.setColor(active ? new Color(44, 44, 42) : new Color(200, 196, 185));
            g2.drawString(labels[i],
                    btnX + (btnW - fm.stringWidth(labels[i])) / 2,
                    btnY + (btnH - fm.getHeight()) / 2 + fm.getAscent());
            right = btnX - gap;
        }

        g2.setColor(new Color(216, 184, 88));
        g2.drawLine(winX + 8, winY + 46, winX + winW - 8, winY + 46);
    }

    private void drawLeftPanel(Graphics2D g2, Font base,
                               int winX, int bodyY, int leftW, int statusBarY) {

        List<Item> list = currentList();
        Item sel = (!list.isEmpty() && itemCursor < list.size()) ? list.get(itemCursor) : null;

        int panelX      = winX + OUTER_PAD;
        int panelW      = leftW - OUTER_PAD - 10;
        int panelBottom = statusBarY - 8;

        int imgCardY = bodyY + 8;
        int imgCardH = 170;
        if (imgCardY + imgCardH > panelBottom) imgCardH = panelBottom - imgCardY - 4;
        drawCard(g2, panelX, imgCardY, panelW, imgCardH, 8);

        if (sel != null) {
            BufferedImage img = loadItemImage(sel);
            int m = 10;
            if (img != null) g2.drawImage(img, panelX + m, imgCardY + m, panelW - m * 2, imgCardH - m * 2, null);
            else drawCentredText(g2, base, panelX, imgCardY, panelW, imgCardH, "[img]");
        } else {
            drawCentredText(g2, base, panelX, imgCardY, panelW, imgCardH, "—");
        }

        int descCardY = imgCardY + imgCardH + 6;
        int descCardH = panelBottom - descCardY;
        if (descCardH < 24) return;

        drawCard(g2, panelX, descCardY, panelW, descCardH, 8);

        if (sel == null) {
            g2.setFont(base.deriveFont(11f));
            g2.setColor(new Color(140, 136, 128));
            g2.drawString("Select an item.", panelX + 12, descCardY + 20);
            return;
        }

        Shape prevClip = g2.getClip();
        g2.setClip(panelX + 4, descCardY + 4, panelW - 8, descCardH - 8);

        int tx    = panelX + 14;
        int textW = panelW - 20;
        int ty    = descCardY + 24;

        g2.setFont(base.deriveFont(Font.BOLD, 13f));
        FontMetrics nameFm = g2.getFontMetrics();
        ty = drawWordWrapped(g2, nameFm, sel.getName(), tx, ty, textW,
                NAME_LINE_H, new Color(44, 44, 42));
        ty += 6;

        g2.setFont(base.deriveFont(10f));
        ty = drawWordWrapped(g2, g2.getFontMetrics(), sel.getDescription(),
                tx, ty, textW, DESC_LINE_H, new Color(88, 84, 76));
        ty += 8;

        if (sel instanceof Scroll scroll) {
            Skill scrollSkill = skills.SkillRegistry.get(scroll.getSkillName());
            if (scrollSkill != null) {
                String typeName = scrollSkill.getType().name();
                g2.setFont(base.deriveFont(8f));
                FontMetrics badgeFm = g2.getFontMetrics();
                int badgePadX = 6, badgeH = 20;
                int badgeW = badgeFm.stringWidth(typeName) + badgePadX * 2 + 6;

                g2.setColor(typeColor(typeName));
                g2.fillRoundRect(tx, ty - 8, badgeW, badgeH, 4, 4);
                g2.setColor(Color.WHITE);
                g2.drawString(typeName, tx + badgePadX + 3, ty + 5);
                ty += badgeH + 6;
            }
        }

        g2.setClip(prevClip);
    }

    private void drawRightPanel(Graphics2D g2, Font base,
                                int winX, int winW, int bodyY,
                                int divX, int statusBarY) {

        List<Item> list  = currentList();
        int totalCount   = list.size();

        int listX       = divX + OUTER_PAD;
        int listW       = winX + winW - listX - OUTER_PAD;
        int labelY      = bodyY + 16;
        int firstRowY   = labelY + 8;
        int listAreaBot = statusBarY - 8;
        int listAreaH   = listAreaBot - firstRowY;
        int visCount    = Math.max(1, listAreaH / ROW_H);

        String tabLabel = activeTab == Tab.ITEMS ? "ITEMS" : "CAPSULES";
        g2.setFont(base.deriveFont(10f));
        g2.setColor(new Color(120, 116, 108));
        g2.drawString(tabLabel, listX, labelY);

        if (totalCount > 0) {
            String counter = (itemCursor + 1) + " / " + totalCount;
            FontMetrics cfm = g2.getFontMetrics();
            g2.drawString(counter, listX + listW - cfm.stringWidth(counter), labelY);
        }

        if (totalCount == 0) {
            g2.setFont(base.deriveFont(10f));
            g2.setColor(new Color(150, 145, 138));
            g2.drawString("Nothing here.", listX, firstRowY + ROW_H / 2);
            return;
        }

        clampScroll(visCount);

        Shape prevClip = g2.getClip();
        g2.setClip(listX - 6, firstRowY, listW + 12, listAreaH);

        int endIdx = Math.min(scrollOffset + visCount, totalCount);

        for (int i = scrollOffset; i < endIdx; i++) {
            Item    item    = list.get(i);
            int     rowTop  = firstRowY + (i - scrollOffset) * ROW_H;
            int     textY   = rowTop + ROW_H - 9;
            boolean hovered = (i == itemCursor);

            if (hovered) {
                g2.setColor(new Color(178, 212, 244, 200));
                g2.fillRoundRect(listX - 4, rowTop, listW + 4, ROW_H - 2, 5, 5);
                g2.setColor(new Color(24, 95, 165));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(listX - 4, rowTop, listW + 4, ROW_H - 2, 5, 5);
                g2.setStroke(new BasicStroke(1));

                int ts = 7, cx = listX + 2, cy = rowTop + ROW_H / 2 - 1;
                g2.setColor(new Color(80, 76, 70));
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.fillPolygon(new int[]{ cx, cx, cx + ts }, new int[]{ cy - ts, cy + ts, cy }, 3);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
            }

            g2.setFont(base.deriveFont(hovered ? Font.BOLD : Font.PLAIN, 11f));
            g2.setColor(new Color(44, 44, 42));
            int nameMaxW = listW - 54;
            g2.drawString(truncate(item.getName(), g2.getFontMetrics(), nameMaxW), listX + 16, textY);

            String qty = "x " + countOf(item.getName(), gp.player.getInventory());
            g2.setFont(base.deriveFont(10f));
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(new Color(80, 78, 72));
            g2.drawString(qty, listX + listW - fm.stringWidth(qty) - 6, textY);

            if (i < endIdx - 1) {
                g2.setColor(new Color(205, 200, 190));
                g2.drawLine(listX, rowTop + ROW_H - 1, listX + listW, rowTop + ROW_H - 1);
            }
        }

        g2.setClip(prevClip);

        g2.setFont(base.deriveFont(9f));
        g2.setColor(new Color(140, 136, 128));
        int arrowX = listX + listW / 2;
        if (scrollOffset > 0)
            g2.drawString("^ more", arrowX - 16, firstRowY - 2);
        if (scrollOffset + visCount < totalCount) {
            int hintY = firstRowY + visCount * ROW_H + 10;
            if (hintY < listAreaBot + 14)
                g2.drawString("v more", arrowX - 16, hintY);
        }
    }

    private int computeVisibleCount() {
        int winY       = TILE_SIZE;
        int winH       = SCREEN_HEIGHT - TILE_SIZE * 2;
        int bodyY      = winY + 52;
        int statusBarY = winY + winH - STATUS_BAR_H - 8;
        int labelY     = bodyY + 14;
        int firstRowY  = labelY + 12;
        int listAreaH  = (statusBarY - 8) - firstRowY;
        return Math.max(1, listAreaH / ROW_H);
    }

    private void drawPartySelectOverlay(Graphics2D g2, Font base,
                                        int winX, int winY, int winW, int winH) {
        g2.setColor(new Color(0, 0, 0, 130));
        g2.fillRoundRect(winX, winY, winW, winH, 16, 16);

        int panelW = 430, panelH = 318;
        int panelX = winX + (winW - panelW) / 2;
        int panelY = winY + (winH - panelH) / 2;
        drawCard(g2, panelX, panelY, panelW, panelH, 12);

        g2.setFont(base.deriveFont(Font.BOLD, 13f));
        g2.setColor(new Color(44, 44, 42));
        g2.drawString("Choose a BrainRot", panelX + 14, panelY + 22);
        g2.setColor(new Color(200, 195, 180));
        g2.drawLine(panelX + 10, panelY + 28, panelX + panelW - 10, panelY + 28);

        int rowH      = 44;
        int listY     = panelY + 36;
        int partySize = gp.player.getPCSYSTEM().getPartySize();

        Shape prev = g2.getClip();
        g2.setClip(panelX + 4, listY, panelW - 8, panelH - 54);

        for (int i = 0; i < partySize; i++) {
            BrainRot rot     = gp.player.getPCSYSTEM().getPartyMember(i);
            boolean  hovered = (i == partyCursor);
            boolean  greyed  = isGreyedOut(rot);
            int      rowY    = listY + i * rowH;

            Color bg = hovered
                    ? (greyed ? new Color(220, 210, 210) : new Color(178, 212, 244, 220))
                    : new Color(235, 231, 223);
            g2.setColor(bg);
            g2.fillRoundRect(panelX + 8, rowY, panelW - 16, rowH - 4, 6, 6);

            if (hovered && !greyed) {
                g2.setColor(new Color(24, 95, 165));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(panelX + 8, rowY, panelW - 16, rowH - 4, 6, 6);
                g2.setStroke(new BasicStroke(1));
                int ts = 6, cx = panelX + 12, cy = rowY + (rowH - 4) / 2;
                g2.setColor(new Color(80, 76, 70));
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.fillPolygon(new int[]{ cx, cx, cx + ts }, new int[]{ cy - ts, cy + ts, cy }, 3);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
            }

            int textX = panelX + 26;

            g2.setFont(base.deriveFont(Font.BOLD, 11f));
            g2.setColor(greyed ? new Color(160, 155, 148) : new Color(44, 44, 42));
            int nameMaxW = panelW - 140;
            g2.drawString(truncate(rot.getName(), g2.getFontMetrics(), nameMaxW), textX, rowY + 16);

            String info = "HP " + rot.getCurrentHp() + "/" + rot.getMaxHp()
                    + (rot.getStatus().equalsIgnoreCase("NONE") ? "" : "  [" + rot.getStatus() + "]");
            g2.setFont(base.deriveFont(9f));
            g2.setColor(greyed ? new Color(170, 165, 158) : new Color(88, 84, 76));
            g2.drawString(info, textX, rowY + 30);

            if (greyed) {
                String reason = greyReason(rot);
                g2.setFont(base.deriveFont(8f));
                FontMetrics fm = g2.getFontMetrics();
                g2.setColor(new Color(180, 80, 60));
                g2.drawString(reason, panelX + panelW - fm.stringWidth(reason) - 14, rowY + 24);
            }
        }

        g2.setClip(prev);

        g2.setFont(base.deriveFont(7f));
        g2.setColor(new Color(120, 116, 108));
        g2.drawString("WS Move   ENT Confirm   ESC Cancel", panelX + 14, panelY + panelH - 10);
    }

    private void drawMoveSwapOverlay(Graphics2D g2, Font base,
                                     int winX, int winY, int winW, int winH) {
        g2.setColor(new Color(0, 0, 0, 130));
        g2.fillRoundRect(winX, winY, winW, winH, 16, 16);

        int panelW = 440, panelH = 260;
        int panelX = winX + (winW - panelW) / 2;
        int panelY = winY + (winH - panelH) / 2;
        drawCard(g2, panelX, panelY, panelW, panelH, 12);

        g2.setFont(base.deriveFont(Font.BOLD, 13f));
        g2.setColor(new Color(44, 44, 42));
        String title;
        String subtitle;
        if (pendingItem instanceof Scroll s) {
            title    = "Learning: " + truncate(s.getSkillName(), g2.getFontMetrics(), panelW - 60);
            subtitle = "Choose a move to replace:";
        } else if (pendingItem instanceof UPBottle b) {
            String restoreLabel = (b.getMode() == UPBottle.RestoreMode.FULL)
                    ? "Restore all UP"
                    : "Restore " + b.getRestoreAmount() + " UP";
            title    = restoreLabel;
            subtitle = "Choose a skill to restore:";
        } else {
            title    = "Choose a move:";
            subtitle = "";
        }
        g2.drawString(title, panelX + 14, panelY + 22);

        g2.setFont(base.deriveFont(9f));
        g2.setColor(new Color(100, 96, 90));
        g2.drawString(subtitle, panelX + 14, panelY + 39);

        g2.setColor(new Color(200, 195, 180));
        g2.drawLine(panelX + 10, panelY + 46, panelX + panelW - 10, panelY + 46);

        if (pendingScrollTarget == null) return;

        List<Skill> moves  = pendingScrollTarget.getMoves();
        int rowH = 44, listY = panelY + 52;
        int badgeH = 16, padX = 5;

        for (int i = 0; i < moves.size(); i++) {
            Skill   mv  = moves.get(i);
            boolean hov = (i == moveCursor);
            int     rowY = listY + i * rowH;

            g2.setColor(hov ? new Color(178, 212, 244, 220) : new Color(235, 231, 223));
            g2.fillRoundRect(panelX + 8, rowY, panelW - 16, rowH - 4, 6, 6);

            if (hov) {
                g2.setColor(new Color(24, 95, 165));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(panelX + 8, rowY, panelW - 16, rowH - 4, 6, 6);
                g2.setStroke(new BasicStroke(1));
                int ts = 6, cx = panelX + 12, cy = rowY + (rowH - 4) / 2;
                g2.setColor(new Color(80, 76, 70));
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.fillPolygon(new int[]{ cx, cx, cx + ts }, new int[]{ cy - ts, cy + ts, cy }, 3);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
            }

            g2.setFont(base.deriveFont(8f));
            FontMetrics fm = g2.getFontMetrics();
            int badgeW    = fm.stringWidth(mv.getType().name()) + padX * 2;
            int badgeX    = panelX + 24;
            int badgeTopY = rowY + (rowH - 4 - badgeH) / 2;
            g2.setColor(typeColor(mv.getType().name()));
            g2.fillRoundRect(badgeX, badgeTopY, badgeW, badgeH, 4, 4);
            g2.setColor(Color.WHITE);
            g2.drawString(mv.getType().name(), badgeX + padX, badgeTopY + 11);

            int nameX = panelX + 24 + badgeW + 8;
            boolean upFull = (pendingItem instanceof UPBottle) && mv.getCurrentUP() >= mv.getMaxUP();
            g2.setFont(base.deriveFont(hov ? Font.BOLD : Font.PLAIN, 11f));
            g2.setColor(upFull ? new Color(160, 155, 148) : new Color(44, 44, 42));
            g2.drawString(truncate(mv.getName(), g2.getFontMetrics(), panelW - 140), nameX, rowY + rowH / 2 + 5);

            String sp = "UP " + mv.getCurrentUP() + "/" + mv.getMaxUP();
            g2.setFont(base.deriveFont(9f));
            fm = g2.getFontMetrics();
            g2.setColor(new Color(100, 96, 90));
            g2.drawString(sp, panelX + panelW - fm.stringWidth(sp) - 16, rowY + rowH / 2 + 5);
        }

        g2.setFont(base.deriveFont(7f));
        g2.setColor(new Color(120, 116, 108));
        g2.drawString("WS Move   ENT Replace   ESC Cancel", panelX + 14, panelY + panelH - 10);
    }

    private void drawStatusBar(Graphics2D g2, Font base,
                               int winX, int winY, int winW,
                               int statusBarY, int statusBarH) {
        int barX = winX + 8, barW = winW - 16;

        g2.setColor(new Color(215, 210, 200));
        g2.fillRoundRect(barX, statusBarY, barW, statusBarH, 5, 5);

        String hint1 = "WS Move  TAB Tab";
        String hint2 = "ENT Use  ESC Close";

        g2.setFont(base.deriveFont(8f));
        g2.setColor(new Color(120, 116, 108));
        FontMetrics hfm = g2.getFontMetrics();
        int rx = barX + barW - 12;
        g2.drawString(hint1, rx - hfm.stringWidth(hint1), statusBarY + 18);
        g2.drawString(hint2, rx - hfm.stringWidth(hint2), statusBarY + 32);

        if (!statusMessage.isEmpty()) {
            g2.setFont(base.deriveFont(10f));
            FontMetrics mfm = g2.getFontMetrics();

            String msg   = truncate(statusMessage, mfm, barW);

            int msgX = barX + 14;
            int msgY = statusBarY + (statusBarH - mfm.getHeight()) / 2 + mfm.getAscent();

            g2.setColor(new Color(44, 44, 42));
            g2.drawString(msg, msgX, msgY);
        }
    }

    private boolean isGreyedOut(BrainRot rot) {
        if (rot == null) return true;

        if (pendingItem instanceof Stew)
            return rot.getCurrentHp() >= rot.getMaxHp();

        if (pendingItem instanceof Antidote antidote) {
            String cure = antidote.getStatusToCure().toUpperCase();
            if (cure.equals("DEBUFF")) return !hasDebuffs(rot);
            return !rot.hasStatus(cure);
        }

        if (pendingItem instanceof Scroll scroll) {
            Scroll.ScrollResult result = scroll.validate(rot);
            return result != Scroll.ScrollResult.SUCCESS
                    && result != Scroll.ScrollResult.MOVESET_FULL;
        }

        if (pendingItem instanceof UPBottle) {
            // Grey out if all moves are already at max UP
            return rot.getMoves().isEmpty()
                    || rot.getMoves().stream().allMatch(s -> s.getCurrentUP() >= s.getMaxUP());
        }

        return false;
    }

    private String greyReason(BrainRot rot) {
        if (pendingItem instanceof Stew)
            return "HP full";

        if (pendingItem instanceof Antidote a) {
            String cure = a.getStatusToCure().toUpperCase();
            if (cure.equals("DEBUFF")) return "No debuffs";
            return "Not " + capitalize(cure.toLowerCase());
        }

        if (pendingItem instanceof UPBottle)
            return "All UP full";

        if (pendingItem instanceof Scroll scroll) {
            Scroll.ScrollResult r = scroll.validate(rot);
            return switch (r) {
                case TYPE_MISMATCH      -> "Wrong type";
                case SIGNATURE_MISMATCH -> "Not its move";
                case ALREADY_KNOWN      -> "Already known";
                case SKILL_NOT_FOUND    -> "Not found";
                default                 -> "Can't learn";
            };
        }

        return "";
    }

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

    private void drawCard(Graphics2D g2, int x, int y, int w, int h, int arc) {
        g2.setColor(new Color(230, 226, 218));
        g2.fillRoundRect(x, y, w, h, arc, arc);
        g2.setColor(new Color(190, 185, 172));
        g2.drawRoundRect(x, y, w, h, arc, arc);
    }

    private int drawWordWrapped(Graphics2D g2, FontMetrics fm, String text,
                                int x, int y, int maxW, int lineH, Color color) {
        g2.setColor(color);
        int cy = y;
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            String test = line.isEmpty() ? word : line + " " + word;
            if (fm.stringWidth(test) > maxW && !line.isEmpty()) {
                g2.drawString(line.toString(), x, cy);
                cy += lineH + 4;
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(test);
            }
        }
        if (!line.isEmpty()) { g2.drawString(line.toString(), x, cy); cy += lineH; }
        return cy;
    }

    private void drawCentredText(Graphics2D g2, Font base,
                                 int bx, int by, int bw, int bh, String t) {
        g2.setFont(base.deriveFont(10f));
        g2.setColor(new Color(150, 145, 138));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(t, bx + (bw - fm.stringWidth(t)) / 2, by + bh / 2 + 4);
    }

    private String truncate(String text, FontMetrics fm, int maxPx) {
        if (fm.stringWidth(text) <= maxPx) return text;
        while (text.length() > 1 && fm.stringWidth(text + "…") > maxPx)
            text = text.substring(0, text.length() - 1);
        return text + "…";
    }

    private BufferedImage loadItemImage(Item item) {
        String path = item.getAssetPath();
        if (path == null || path.isEmpty()) return null;
        return imgCache.computeIfAbsent(path, AssetManager::loadImage);
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

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private String findSignatureOwner(String skillName) {
        for (String rot : new String[]{
                "TUNG TUNG TUNG SAHUR", "TRALALERO TRALALA", "BOMBARDINO CROCODILO",
                "LIRILI LARILA", "BRR BRR PATAPIM", "BONECA AMBALABU",
                "UDIN DIN DIN DIN DUN", "CAPUCCINO ASSASSINO"}) {
            if (SkillPool.isSignatureOf(skillName, rot)) return rot;
        }
        return null;
    }

    private void setStatus(String msg) {
        statusMessage = "[ " + msg + " ]";
        statusTimer   = STATUS_TICKS;
        System.out.println("[InventoryUI] " + msg);
    }

    private void clearStatus() {
        statusMessage = "";
        statusTimer   = 0;
    }
}