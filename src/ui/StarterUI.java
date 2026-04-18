package ui;

import brainrots.BrainRot;
import brainrots.BrainRotFactory;
import brainrots.Tier;
import engine.GamePanel;
import ui.BlackFadeEffect;
import input.KeyboardHandler;
import skills.Skill;
import utils.AssetManager;
import utils.Constants;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static utils.Constants.*;

public class StarterUI {

    // --- ADDED FADE_TO_BLACK STATE ---
    private enum State { INTRO_TEXT, CHOOSE, CONFIRM, FINISH_TEXT, FADE_TO_BLACK }

    private final GamePanel gp;
    private final KeyboardHandler kh;

    private State currentState = State.INTRO_TEXT;
    private int textIndex = 0;
    private int capsuleCursor = 1;
    private int confirmCursor = 0;
    private int inputCooldown = 0;

    private final String[] introLines = {
            "Welcome to the laboratory!",
            "It's dangerous to go alone into the tall grass.",
            "Choose one of these 3 partners to begin!"
    };

    private final String[] fullPool = {
            "TUNG TUNG TUNG SAHUR", "TRALALERO TRALALA", "BOMBARDINO CROCODILO",
            "LIRILI LARILA", "BRR BRR PATAPIM", "BONECA AMBALABU",
            "UDIN DIN DIN DIN DUN", "CAPUCCINO ASSASSINO"
    };

    private final BrainRot[] starterRots = new BrainRot[3];

    private BufferedImage capsuleImg, dialogueBoxFrame;
    private final Map<String, BufferedImage> spriteCache = new HashMap<>();

    public StarterUI(GamePanel gp, KeyboardHandler kh) {
        this.gp = gp;
        this.kh = kh;
        loadAssets();
        rollRandomStarters();
    }

    private void loadAssets() {
        capsuleImg = AssetManager.loadImage("/res/Templates/Items/9.png");
        dialogueBoxFrame = AssetManager.loadImage("/res/UI/Battle/dialogue_box.png");
    }

    private void rollRandomStarters() {
        List<String> poolList = new ArrayList<>(Arrays.asList(fullPool));
        Collections.shuffle(poolList);

        for (int i = 0; i < 3; i++) {
            starterRots[i] = BrainRotFactory.create(poolList.get(i), Tier.NORMAL);
        }
    }

    public void update() {
        if (inputCooldown > 0) { inputCooldown--; return; }

        switch (currentState) {
            case INTRO_TEXT -> {
                if (kh.enterPressed) {
                    kh.enterPressed = false;
                    textIndex++;
                    if (textIndex >= introLines.length) currentState = State.CHOOSE;
                    inputCooldown = INPUT_DELAY;
                }
            }
            case CHOOSE -> {
                if (kh.leftPressed && capsuleCursor > 0) {
                    capsuleCursor--; inputCooldown = INPUT_DELAY;
                } else if (kh.rightPressed && capsuleCursor < 2) {
                    capsuleCursor++; inputCooldown = INPUT_DELAY;
                } else if (kh.enterPressed) {
                    kh.enterPressed = false;
                    confirmCursor = 0;
                    currentState = State.CONFIRM;
                    inputCooldown = INPUT_DELAY;
                }
            }
            case CONFIRM -> {
                if (kh.upPressed || kh.downPressed) {
                    confirmCursor = (confirmCursor == 0) ? 1 : 0;
                    inputCooldown = INPUT_DELAY;
                } else if (kh.enterPressed) {
                    kh.enterPressed = false;
                    if (confirmCursor == 0) {
                        gp.player.getPCSYSTEM().addBrainRot(starterRots[capsuleCursor]);
                        currentState = State.FINISH_TEXT;
                    } else {
                        currentState = State.CHOOSE;
                    }
                    inputCooldown = INPUT_DELAY;
                } else if (kh.escPressed) {
                    kh.escPressed = false;
                    currentState = State.CHOOSE;
                    inputCooldown = INPUT_DELAY;
                }
            }
            case FINISH_TEXT -> {
                if (kh.enterPressed) {
                    kh.enterPressed = false;
                    currentState = State.FADE_TO_BLACK;
                    gp.BLACKFADEEFFECT.start(BlackFadeEffect.FadeMode.FADE_IN_TO_BLACK, 8);
                }
            }
            case FADE_TO_BLACK -> {
                gp.BLACKFADEEFFECT.update();
                if (gp.BLACKFADEEFFECT.isFullyBlack()) {
                    gp.GAMESTATE = "play";
                    gp.BLACKFADEEFFECT.start(BlackFadeEffect.FadeMode.FADE_OUT_TO_PLAY, 8);
                }
            }
        }
    }

    public void draw(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // pokemonGb everywhere; Arial fallback only if font failed to load
        Font baseFont = (AssetManager.pokemonGb != null)
                ? AssetManager.pokemonGb : new Font("Arial", Font.PLAIN, 18);

        GradientPaint bgGrad = new GradientPaint(0, 0, new Color(20, 25, 35), 0, SCREEN_HEIGHT, new Color(45, 90, 110));
        g2.setPaint(bgGrad);
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        if (currentState == State.FINISH_TEXT || currentState == State.FADE_TO_BLACK) {
            drawReceivedCard(g2, baseFont);
        } else {
            drawDeskAndCapsules(g2);
            if (currentState == State.CHOOSE || currentState == State.CONFIRM) {
                drawPreviewCard(g2, baseFont);
            }
        }

        if (currentState != State.FADE_TO_BLACK) {
            String currentText = switch (currentState) {
                case INTRO_TEXT  -> introLines[textIndex];
                case CHOOSE      -> "Choose your first BrainRot partner.";
                case CONFIRM     -> "Do you want to choose " + starterRots[capsuleCursor].getName() + "?";
                case FINISH_TEXT -> "You received " + starterRots[capsuleCursor].getName() + "!";
                default          -> "";
            };

            int boxY = SCREEN_HEIGHT - 136;
            drawDialogueBox(g2, baseFont, currentText, boxY);

            if (currentState == State.CONFIRM) {
                drawYesNoMenu(g2, baseFont, boxY);
            }
        }

        if (currentState == State.FADE_TO_BLACK) {
            gp.BLACKFADEEFFECT.draw(g2);
        }
    }

    // ── Desk + capsules — completely unchanged ────────────────────────────────

    private void drawDeskAndCapsules(Graphics2D g2) {
        int deskW = 500;
        int deskH = 140;
        int deskX = (SCREEN_WIDTH - deskW) / 2;
        int deskY = SCREEN_HEIGHT - 305;

        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillOval(deskX + 30, deskY + deskH - 30, deskW - 60, 50);

        GradientPaint deskGrad = new GradientPaint(deskX, deskY, new Color(140, 145, 150), deskX, deskY + deskH, new Color(80, 85, 90));
        g2.setPaint(deskGrad);
        g2.fillRoundRect(deskX, deskY, deskW, deskH, 25, 25);

        g2.setColor(new Color(60, 200, 255, 200));
        g2.fillRoundRect(deskX, deskY + 20, deskW, 6, 3, 3);

        g2.setColor(new Color(60, 65, 70));
        g2.drawRoundRect(deskX, deskY, deskW, deskH, 25, 25);

        int startX = deskX + 80;
        int gap = 140;
        int capY = deskY + 10;
        double wave = Math.sin(System.currentTimeMillis() / 250.0) * 8;

        for (int i = 0; i < 3; i++) {
            int cx = startX + (i * gap);
            boolean isSelected = (i == capsuleCursor);
            int floatOffset = (isSelected && (currentState == State.CHOOSE || currentState == State.CONFIRM)) ? (int)wave - 15 : 0;

            g2.setColor(new Color(40, 45, 50, 180));
            int shadowW = isSelected ? 50 : 60;
            g2.fillOval(cx + (60 - shadowW)/2, capY + 65, shadowW, 20);

            if (isSelected) {
                g2.setColor(new Color(60, 200, 255, 80));
                g2.fillOval(cx - 15, capY + 45, 90, 40);
            }

            if (capsuleImg != null) {
                g2.drawImage(capsuleImg, cx, capY + floatOffset, 60, 60, null);
            }

            if (isSelected && currentState == State.CHOOSE) {
                g2.setColor(new Color(255, 255, 255));
                int[] ax = {cx + 22, cx + 38, cx + 30};
                int[] ay = {capY - 30 + floatOffset, capY - 30 + floatOffset, capY - 15 + floatOffset};
                g2.fillPolygon(ax, ay, 3);
            }
        }
    }

    // ── Preview card — same layout, pokemonGb font + PC triple-stroke border ──

    private void drawPreviewCard(Graphics2D g2, Font base) {
        int cardW = 540, cardH = 225;
        int cardX = (SCREEN_WIDTH - cardW) / 2;
        int cardY = 30;

        drawPCBorder(g2, cardX, cardY, cardW, cardH, 15);

        BrainRot preview = starterRots[capsuleCursor];
        BufferedImage sprite = getSprite(preview);

        if (sprite != null) {
            g2.drawImage(sprite, cardX + 20, cardY + 30, 130, 130, null);
        }

        g2.setFont(base.deriveFont(Font.BOLD, 14f));
        g2.setColor(new Color(44, 44, 42));
        g2.drawString(preview.getName(), cardX + 170, cardY + 45);

        // Header Badges
        int bX = cardX + 170;
        int bY = cardY + 68;
        int pW = drawTypeBadge(g2, preview.getPrimaryType().name(), bX, bY, 9f);
        int sW = 0;
        if (preview.getSecondaryType() != null && !preview.getSecondaryType().name().equals("NONE")) {
            sW = drawTypeBadge(g2, preview.getSecondaryType().name(), bX + pW + 5, bY, 9f) + 5;
        }

        g2.setFont(base.deriveFont(11f));
        g2.setColor(new Color(60, 160, 80));
        g2.drawString("HP: " + preview.getMaxHp(), bX + pW + sW + 15, bY);

        g2.setColor(new Color(200, 200, 200));
        g2.drawLine(cardX + 170, cardY + 82, cardX + cardW - 20, cardY + 80);

        g2.setFont(base.deriveFont(10f));
        g2.setColor(new Color(60, 64, 70));
        String desc = Constants.getDescription(preview.getName());
        drawWrappedText(g2, base.deriveFont(10f), desc, cardX + 170, cardY + 102, cardW - 190);
    }

    // ── Received card — same layout except skills section replaced with 4×1 rows

    private void drawReceivedCard(Graphics2D g2, Font base) {
        int cardW = SCREEN_WIDTH - 60;
        int cardH = SCREEN_HEIGHT - 190;
        int cardX = 30;
        int cardY = 25;

        drawPCBorder(g2, cardX, cardY, cardW, cardH, 20);

        BrainRot received = starterRots[capsuleCursor];
        BufferedImage sprite = getSprite(received);

        if (sprite != null) {
            g2.drawImage(sprite, cardX + 20, cardY + 60, 160, 160, null);
        }

        g2.setFont(base.deriveFont(Font.BOLD, 16f));
        g2.setColor(new Color(44, 44, 42));
        g2.drawString(received.getName(), cardX + 210, cardY + 60);

        // Header Badges
        int bX = cardX + 210;
        int bY = cardY + 88;
        int pW = drawTypeBadge(g2, received.getPrimaryType().name(), bX, bY, 9f);
        int sW = 0;
        if (received.getSecondaryType() != null && !received.getSecondaryType().name().equals("NONE")) {
            sW = drawTypeBadge(g2, received.getSecondaryType().name(), bX + pW + 5, bY, 9f) + 5;
        }

        g2.setFont(base.deriveFont(12f));
        g2.setColor(new Color(80, 80, 80));
        g2.drawString("LEVEL: " + received.getLevel(), bX + pW + sW + 15, bY);

        g2.setColor(new Color(60, 160, 80));
        g2.drawString("HP: " + received.getMaxHp(), cardX + 210, cardY + 118);

        g2.setColor(new Color(200, 200, 200));
        g2.drawLine(cardX + 210, cardY + 132, cardX + cardW - 30, cardY + 132);

        g2.setFont(base.deriveFont(10f));
        g2.setColor(new Color(60, 64, 70));
        String desc = Constants.getDescription(received.getName());
        drawWrappedText(g2, base.deriveFont(10f), desc, cardX + 210, cardY + 155, cardW - 500);

        // ── Starting skills: 4×1 move rows
        int skillsX = cardX + cardW - 280;
        int skillsY = cardY + 155;
        int rowW    = 250;
        int rowH    = 36;

        g2.setFont(base.deriveFont(Font.BOLD, 11f));
        g2.setColor(new Color(60, 64, 70));
        g2.drawString("STARTING SKILLS", skillsX, skillsY);
        skillsY += 8;

        g2.setColor(new Color(216, 184, 88));
        g2.drawLine(skillsX, skillsY, skillsX + rowW, skillsY);
        skillsY += 4;

        List<Skill> moves = received.getMoves();

        for (int i = 0; i < 4; i++) {
            int rowY = skillsY + i * rowH;
            g2.setColor(new Color(245, 242, 235));
            g2.fillRoundRect(skillsX, rowY, rowW, rowH - 2, 5, 5);

            if (i < 3) {
                g2.setColor(new Color(200, 200, 200));
                g2.drawLine(skillsX + 4, rowY + rowH - 2, skillsX + rowW - 4, rowY + rowH - 2);
            }

            int baseline = rowY + (rowH + 8) / 2;

            if (i < moves.size()) {
                Skill mv = moves.get(i);

                // --- Type badge using helper ---
                int skillBadgeW = drawTypeBadge(g2, mv.getType().name(), skillsX + 6, baseline, 6f);

                g2.setFont(base.deriveFont(Font.PLAIN, 9f));
                g2.setColor(new Color(44, 44, 42));
                g2.drawString(mv.getName(), skillsX + 6 + skillBadgeW + 8, baseline);

                String upStr = mv.getCurrentUP() + "/" + mv.getMaxUP();
                g2.setFont(base.deriveFont(7f));
                g2.setColor(new Color(88, 84, 76));
                g2.drawString(upStr, skillsX + rowW - g2.getFontMetrics().stringWidth(upStr) - 8, baseline);

            } else {
                g2.setFont(base.deriveFont(9f));
                g2.setColor(new Color(170, 165, 158));
                g2.drawString("-", skillsX + 12, baseline);
            }
        }
    }

    private int drawTypeBadge(Graphics2D g2, String typeName, int x, int y, float fontSize) {
        if (typeName == null || typeName.equalsIgnoreCase("NONE")) return 0;

        Font oldFont = g2.getFont();
        g2.setFont(oldFont.deriveFont(fontSize));
        FontMetrics fm = g2.getFontMetrics();

        int padX = 10;
        // fm.getHeight() is safer than Ascent+Descent for pixel fonts to prevent clipping
        int badgeH = fm.getHeight() + 5;
        int badgeW = fm.stringWidth(typeName.toUpperCase()) + (padX * 2);

        // Position the badge top relative to the baseline (y)
        // We subtract the ascent to get to the 'top' of the text,
        // then subtract 1 or 2 pixels for the top padding.
        int badgeTopY = y - fm.getAscent() - 5;

        g2.setColor(typeColor(typeName));
        // Increase badgeH slightly if it still looks tight on the bottom
        g2.fillRoundRect(x, badgeTopY, badgeW, badgeH + 2, 4, 4);

        g2.setColor(Color.WHITE);
        // Standardize text rendering to prevent sub-pixel shifting
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.drawString(typeName.toUpperCase(), x + padX, y);

        g2.setFont(oldFont);
        return badgeW;
    }

    // ── Dialogue box — PC triple-stroke border + pokemonGb ───────────────────

    private void drawDialogueBox(Graphics2D g2, Font base, String text, int boxY) {
        int boxH = 126;
        int boxX = 10;
        int boxW = SCREEN_WIDTH - 20;

        if (dialogueBoxFrame != null) {
            g2.drawImage(dialogueBoxFrame, 6, boxY, SCREEN_WIDTH - 12, boxH, null);
        } else {
            drawPCBorder(g2, boxX, boxY, boxW, boxH, 15);
        }

        g2.setFont(base.deriveFont(Font.BOLD, 16f));
        g2.setColor(new Color(44, 44, 42));
        drawWrappedText(g2, base.deriveFont(Font.BOLD, 16f), text, boxX + 28, boxY + 55, boxW - 20);
    }

    private void drawYesNoMenu(Graphics2D g2, Font base, int boxY) {
        int menuW = 160, menuH = 126;
        int menuX = SCREEN_WIDTH - menuW - 10;
        int menuY = boxY;

        drawPCBorder(g2, menuX, menuY, menuW, menuH, 12);

        g2.setFont(base.deriveFont(Font.BOLD, 13f));
        g2.setColor(new Color(44, 44, 42));
        g2.drawString("YES", menuX + 60, menuY + 45);
        g2.drawString("NO",  menuX + 60, menuY + 88);

        // Cursor triangle — fillPolygon, no ▶ glyph (pokemonGb limitation)
        int ts = 8;
        int cx = menuX + 35;
        int cy = menuY + (confirmCursor == 0 ? 38 : 81);
        g2.setColor(new Color(44, 44, 42));
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.fillPolygon(new int[]{ cx, cx, cx + ts }, new int[]{ cy - ts, cy + ts, cy }, 3);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
    }

    // ── Shared helpers ────────────────────────────────────────────────────────

    /**
     * Triple-stroke window border — exactly matches PC / Inventory / Quest UIs.
     * dark 6px → gold 4px → dark 2px
     */
    private void drawPCBorder(Graphics2D g2, int x, int y, int w, int h, int arc) {
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
        g2.drawRoundRect(x + 4, y + 4, w - 8, h - 8, Math.max(arc - 4, 4), Math.max(arc - 4, 4));
        g2.setStroke(new BasicStroke(1));
    }

    private void drawWrappedText(Graphics2D g2, Font font, String text, int x, int y, int maxWidth) {
        if (text.toLowerCase().contains("do you want to choose")) maxWidth -= 200;
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();
        int lineHeight = fm.getHeight() + 6;
        int curY = y;
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            String test = line.isEmpty() ? word : line + " " + word;
            if (fm.stringWidth(test) < maxWidth) {
                line = new StringBuilder(test);
            } else {
                g2.drawString(line.toString(), x, curY);
                line = new StringBuilder(word);
                curY += lineHeight;
            }
        }
        g2.drawString(line.toString(), x, curY);
    }

    /** Type badge colors — identical to PCUI / InventoryUI. */
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

    private BufferedImage getSprite(BrainRot rot) {
        String key = rot.getName() + "_" + rot.getTier().name();
        if (spriteCache.containsKey(key)) return spriteCache.get(key);
        String path = "/res/InteractiveTiles/Brainrots/" + toFolderName(rot.getName())
                + "/" + rot.getTier().name() + "_1.png";
        BufferedImage img = AssetManager.loadImage(path);
        if (img != null) spriteCache.put(key, img);
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
}