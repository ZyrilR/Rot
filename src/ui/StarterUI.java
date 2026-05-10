package ui;

import brainrots.BrainRot;
import brainrots.BrainRotFactory;
import engine.GamePanel;
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

    private enum State { TRANSITION_TO_BLACK, FADE_IN_LAB, INTRO_TEXT, CHOOSE, CONFIRM, FINISH_TEXT, FADE_TO_BLACK }

    private final GamePanel gp;
    private final KeyboardHandler kh;

    private State currentState = State.TRANSITION_TO_BLACK;
    private int textIndex = 0;
    private int capsuleCursor = 1;
    private int confirmCursor = 0;
    private int inputCooldown = 0;

    // --- TYPEWRITER STATE VARIABLES ---
    private String targetText = "";
    private String displayedText = "";
    private int charIndex = 0;

    // --- THE FIX: FLOAT ACCUMULATOR FOR 1.5x SPEED ---
    private float charAccumulator = 0f;

    private final String[] introSpeakers = {
            "Sir Khai", "Sir Khai", "Sir Khai", "Player", "Sir Khai", "Player", "Sir Khai"
    };

    private final String[] introLines = {
            "Yo. You're the new one? Cool. Take a Brainrot. Any of these three.",
            "They're all unhinged in different ways. Good luck, no pressure.",
            "Everything is on fire a little bit. You'll be fine. Probably.",
            "Which one do I choose?",
            "That depends. Are you the type of person who hits things, or dances at problems?",
            "...",
            "Bro just pick one. We're losing daylight."
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
    }

    public void open() {
        currentState = State.TRANSITION_TO_BLACK;
        gp.BLACKFADEEFFECT.start(BlackFadeEffect.FadeMode.FADE_IN_TO_BLACK, 8);

        textIndex = 0;
        capsuleCursor = 1;
        confirmCursor = 0;
        charIndex = 0;
        displayedText = "";
        targetText = "";
        charAccumulator = 0f;
        inputCooldown = 0;

        rollRandomStarters();
    }

    private void loadAssets() {
        capsuleImg = AssetManager.loadImage("/res/Items/Capsule/BLUE.png");
        if (capsuleImg == null) {
            capsuleImg = AssetManager.loadImage("/res/Templates/Items/9.png");
        }
    }

    private void rollRandomStarters() {
        List<String> poolList = new ArrayList<>(Arrays.asList(fullPool));
        Collections.shuffle(poolList);
        for (int i = 0; i < 3; i++) {
            starterRots[i] = BrainRotFactory.create(poolList.get(i), 5);
        }
    }

    private void setTargetText(String text) {
        if (!this.targetText.equals(text)) {
            this.targetText = text;
            this.displayedText = "";
            this.charIndex = 0;
            this.charAccumulator = 0f;
        }
    }

    public void update() {
        // --- 1. DETERMINE TEXT ---
        if (currentState == State.INTRO_TEXT || currentState == State.CHOOSE ||
                currentState == State.CONFIRM || currentState == State.FINISH_TEXT) {

            String expectedText = switch (currentState) {
                case INTRO_TEXT  -> introLines[textIndex];
                case CHOOSE      -> "Choose your first BrainRot partner.";
                case CONFIRM     -> "Do you want to choose " + starterRots[capsuleCursor].getName() + "?";
                case FINISH_TEXT -> "You received " + starterRots[capsuleCursor].getName() + "!";
                default          -> "";
            };
            setTargetText(expectedText);
        }

        // --- 2. 1.5x TYPEWRITER SPEED LOGIC ---
        if (charIndex < targetText.length() && currentState != State.FADE_TO_BLACK && currentState != State.FADE_IN_LAB && currentState != State.TRANSITION_TO_BLACK) {
            float baseCharsPerFrame = 1.0f / (TEXT_SPEED + 1.0f);
            float speedMultiplier = 1.5f; // 1.5x Speed Boost

            charAccumulator += (baseCharsPerFrame * speedMultiplier);

            while (charAccumulator >= 1.0f && charIndex < targetText.length()) {
                displayedText += targetText.charAt(charIndex);
                charIndex++;
                charAccumulator -= 1.0f;
            }
        }

        // --- 3. INPUT COOLDOWN ---
        if (inputCooldown > 0) {
            inputCooldown--;
            kh.enterPressed = false;
            return;
        }

        // --- 4. SKIP TYPEWRITER ---
        if (kh.enterPressed && charIndex < targetText.length() && currentState != State.FADE_TO_BLACK && currentState != State.FADE_IN_LAB && currentState != State.TRANSITION_TO_BLACK) {
            kh.enterPressed = false;
            displayedText = targetText;
            charIndex = targetText.length();
            inputCooldown = INPUT_DELAY;
            return;
        }

        // --- 5. NORMAL STATE LOGIC ---
        switch (currentState) {
            case TRANSITION_TO_BLACK -> {
                gp.BLACKFADEEFFECT.update();
                if (gp.BLACKFADEEFFECT.isFullyBlack()) {
                    currentState = State.FADE_IN_LAB;
                    gp.BLACKFADEEFFECT.start(BlackFadeEffect.FadeMode.FADE_OUT_TO_PLAY, 8);
                }
            }
            case FADE_IN_LAB -> {
                gp.BLACKFADEEFFECT.update();
                if (gp.BLACKFADEEFFECT.isFadeOutComplete()) {
                    currentState = State.INTRO_TEXT;
                }
            }
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
                    utils.AudioManager.playMusic(utils.Constants.BGM_OVERWORLD, true);
                    progression.NarrativeManager.pendingWorldIntro = true;
                }
            }
        }
    }

    public void draw(Graphics2D g2) {
        if (currentState == State.TRANSITION_TO_BLACK) {
            if (gp.world != null) gp.world.draw(g2);
            if (gp.player != null) gp.player.draw(g2);
            gp.BLACKFADEEFFECT.draw(g2);
            return;
        }

        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

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

        if (currentState != State.FADE_TO_BLACK && currentState != State.FADE_IN_LAB && currentState != State.TRANSITION_TO_BLACK) {
            String currentSpeaker = "";

            switch (currentState) {
                case INTRO_TEXT  -> {
                    String rawSpeaker = introSpeakers[textIndex];
                    currentSpeaker = rawSpeaker.equals("Player") ? gp.player.name : rawSpeaker;
                }
                case CHOOSE      -> currentSpeaker = "ROT";
                case CONFIRM     -> currentSpeaker = "ROT";
                case FINISH_TEXT -> currentSpeaker = "ROT";
            }

            int boxY = SCREEN_HEIGHT - 136;
            drawDialogueBox(g2, baseFont, currentSpeaker, displayedText, targetText, boxY);

            if (currentState == State.CONFIRM) {
                drawYesNoMenu(g2, baseFont, boxY);
            }
        }

        if (currentState == State.FADE_TO_BLACK || currentState == State.FADE_IN_LAB) {
            gp.BLACKFADEEFFECT.draw(g2);
        }
    }

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
        drawWrappedTextPlain(g2, base.deriveFont(10f), desc, cardX + 170, cardY + 102, cardW - 190, 16);
    }

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
        drawWrappedTextPlain(g2, base.deriveFont(10f), desc, cardX + 210, cardY + 155, cardW - 500, 16);

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
        int badgeH = fm.getHeight() + 5;
        int badgeW = fm.stringWidth(typeName.toUpperCase()) + (padX * 2);

        int badgeTopY = y - fm.getAscent() - 5;

        g2.setColor(typeColor(typeName));
        g2.fillRoundRect(x, badgeTopY, badgeW, badgeH + 2, 4, 4);

        g2.setColor(Color.WHITE);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.drawString(typeName.toUpperCase(), x + padX, y);

        g2.setFont(oldFont);
        return badgeW;
    }

    private void drawDialogueBox(Graphics2D g2, Font base, String speaker, String drawnText, String fullText, int boxY) {
        int boxH = 126;
        int boxX = 10;
        int boxW = SCREEN_WIDTH - 20;

        if (dialogueBoxFrame != null) {
            g2.drawImage(dialogueBoxFrame, 6, boxY, SCREEN_WIDTH - 12, boxH, null);
        } else {
            drawPCBorder(g2, boxX, boxY, boxW, boxH, 15);
        }

        int textX = boxX + 35;

        BufferedImage portrait = getPortrait(speaker);
        if (portrait != null) {
            g2.setColor(new Color(230, 226, 218));
            g2.fillRoundRect(boxX + 16, boxY + 16, 90, 90, 8, 8);
            g2.setColor(new Color(190, 185, 172));
            g2.drawRoundRect(boxX + 16, boxY + 16, 90, 90, 8, 8);

            g2.drawImage(portrait, boxX + 21, boxY + 21, 80, 80, null);
            textX += 85;
        }

        int maxTextWidth = boxW - (textX - boxX) - 25;

        g2.setFont(base.deriveFont(Font.BOLD, 14f));
        FontMetrics fm = g2.getFontMetrics();

        int lineHeight = 22;
        int speakerSpacing = (speaker != null && !speaker.isEmpty()) ? 24 : 0;

        int totalLines = countWrappedLines(fullText, fm, maxTextWidth);
        int totalContentHeight = speakerSpacing + (totalLines * lineHeight);
        int startY = boxY + ((boxH - totalContentHeight) / 2) + fm.getAscent();

        if (speaker != null && !speaker.isEmpty()) {
            g2.setColor(new Color(40, 40, 40));
            g2.setFont(base.deriveFont(Font.BOLD, 18f));

            if (speaker.equals("Sir Khai")) {
                g2.drawString(speaker + " [VIDEO CALL]:", textX, startY);
            } else {
                g2.drawString(speaker + ":", textX, startY - 8);
            }
            startY += speakerSpacing;
        }

        g2.setFont(base.deriveFont(Font.BOLD, 14f));
        g2.setColor(new Color(64, 64, 64));
        drawWrappedTextDynamically(g2, drawnText, textX, startY, maxTextWidth, lineHeight);
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

        int ts = 8;
        int cx = menuX + 35;
        int cy = menuY + (confirmCursor == 0 ? 38 : 81);
        g2.setColor(new Color(44, 44, 42));
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.fillPolygon(new int[]{ cx, cx, cx + ts }, new int[]{ cy - ts, cy + ts, cy }, 3);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
    }

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

    private int countWrappedLines(String text, FontMetrics fm, int maxWidth) {
        if (text == null || text.isEmpty()) return 0;
        int count = 0;
        for (String line : text.split("\n")) {
            StringBuilder currentLine = new StringBuilder();
            for (String word : line.split(" ")) {
                String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
                if (fm.stringWidth(testLine) > maxWidth && currentLine.length() > 0) {
                    count++;
                    currentLine = new StringBuilder(word);
                } else {
                    currentLine = new StringBuilder(testLine);
                }
            }
            if (currentLine.length() > 0) count++;
        }
        return count;
    }

    private void drawWrappedTextDynamically(Graphics2D g2, String text, int x, int y, int maxWidth, int lineHeight) {
        if (text.toLowerCase().contains("do you want to choose")) { maxWidth -= 200; lineHeight = 28;}
        FontMetrics fm = g2.getFontMetrics();

        for (String line : text.split("\n")) {
            StringBuilder currentLine = new StringBuilder();
            for (String word : line.split(" ")) {
                String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
                if (fm.stringWidth(testLine) > maxWidth && currentLine.length() > 0) {
                    g2.drawString(currentLine.toString(), x, y);
                    y += lineHeight;
                    currentLine = new StringBuilder(word);
                } else {
                    currentLine = new StringBuilder(testLine);
                }
            }
            if (currentLine.length() > 0) {
                g2.drawString(currentLine.toString(), x, y);
                y += lineHeight;
            }
        }
    }

    private void drawWrappedTextPlain(Graphics2D g2, Font font, String text, int x, int y, int maxWidth, int lineHeight) {
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();
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

    private BufferedImage getPortrait(String speakerName) {
        if (speakerName == null || speakerName.isEmpty() || speakerName.equals("System") || speakerName.equals("ROT")) return null;
        String safeName = speakerName.toUpperCase();
        if (safeName.contains("TUNG TUNG")) return AssetManager.getBrainRotSprite("TUNG TUNG TUNG SAHUR", "NORMAL", false, 1);
        if (safeName.contains("SIR KHAI")) return AssetManager.loadImage("/res/InteractiveTiles/5/1.png");
        if (safeName.contains("CHUYAOI") || safeName.contains("PLAYER") || speakerName.equals(gp.player.name)) return AssetManager.loadImage("/res/InteractiveTiles/Player/4.png");

        return null;
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