package ui;

import brainrots.BrainRot;
import brainrots.BrainRotFactory;
import brainrots.Tier;
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
                    // --- TRIGGER THE FADE INSTEAD OF JUMPING TO PLAY ---
                    currentState = State.FADE_TO_BLACK;
                    gp.BLACKFADEEFFECT.start(BlackFadeEffect.FadeMode.FADE_IN_TO_BLACK, 8);
                }
            }
            case FADE_TO_BLACK -> {
                // --- UPDATE FADE AND JUMP TO PLAY WHEN FULLY BLACK ---
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

        GradientPaint bgGrad = new GradientPaint(0, 0, new Color(20, 25, 35), 0, SCREEN_HEIGHT, new Color(45, 90, 110));
        g2.setPaint(bgGrad);
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        if (currentState == State.FINISH_TEXT || currentState == State.FADE_TO_BLACK) {
            drawReceivedCard(g2);
        } else {
            drawDeskAndCapsules(g2);
            if (currentState == State.CHOOSE || currentState == State.CONFIRM) {
                drawPreviewCard(g2);
            }
        }

        // Only draw Dialogue if we are not fading
        if (currentState != State.FADE_TO_BLACK) {
            String currentText = switch (currentState) {
                case INTRO_TEXT -> introLines[textIndex];
                case CHOOSE -> "Choose your first BrainRot partner.";
                case CONFIRM -> "Do you want to choose " + starterRots[capsuleCursor].getName() + "?";
                case FINISH_TEXT -> "You received " + starterRots[capsuleCursor].getName() + "!";
                default -> "";
            };

            int boxY = SCREEN_HEIGHT - 136;
            drawDialogueBox(g2, currentText, boxY);

            if (currentState == State.CONFIRM) {
                drawYesNoMenu(g2, boxY);
            }
        }

        // --- DRAW THE FADE EFFECT ON TOP OF EVERYTHING ---
        if (currentState == State.FADE_TO_BLACK) {
            gp.BLACKFADEEFFECT.draw(g2);
        }
    }

    private void drawDeskAndCapsules(Graphics2D g2) {
        int deskW = 500;
        int deskH = 140;
        int deskX = (SCREEN_WIDTH - deskW) / 2;
        int deskY = SCREEN_HEIGHT - 320;

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

    private void drawPreviewCard(Graphics2D g2) {
        int cardW = 540, cardH = 210;
        int cardX = (SCREEN_WIDTH - cardW) / 2;
        int cardY = 30;

        g2.setColor(new Color(250, 250, 250, 245));
        g2.fillRoundRect(cardX, cardY, cardW, cardH, 15, 15);
        g2.setStroke(new BasicStroke(4));
        g2.setColor(new Color(90, 95, 105));
        g2.drawRoundRect(cardX, cardY, cardW, cardH, 15, 15);
        g2.setStroke(new BasicStroke(1));

        BrainRot preview = starterRots[capsuleCursor];
        BufferedImage sprite = getSprite(preview);

        if (sprite != null) {
            g2.drawImage(sprite, cardX + 20, cardY + 30, 130, 130, null);
        }

        g2.setFont(new Font("Arial", Font.BOLD, 22));
        g2.setColor(new Color(40, 44, 52));
        g2.drawString(preview.getName(), cardX + 170, cardY + 45);

        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.setColor(new Color(80, 80, 80));
        g2.drawString("TYPE: " + preview.getPrimaryType().name(), cardX + 170, cardY + 70);

        g2.setColor(new Color(60, 160, 80));
        g2.drawString("HP: " + preview.getMaxHp(), cardX + 320, cardY + 70);

        g2.setColor(new Color(200, 200, 200));
        g2.drawLine(cardX + 170, cardY + 85, cardX + cardW - 20, cardY + 85);

        g2.setFont(new Font("Arial", Font.PLAIN, 14));
        g2.setColor(new Color(60, 64, 70));
        String desc = Constants.getDescription(preview.getName());
        drawWrappedText(g2, desc, cardX + 170, cardY + 110, cardW - 190);
    }

    private void drawReceivedCard(Graphics2D g2) {
        int cardW = SCREEN_WIDTH - 60;
        int cardH = SCREEN_HEIGHT - 190;
        int cardX = 30;
        int cardY = 25;

        g2.setColor(new Color(250, 250, 250, 245));
        g2.fillRoundRect(cardX, cardY, cardW, cardH, 20, 20);
        g2.setStroke(new BasicStroke(5));
        g2.setColor(new Color(216, 184, 88));
        g2.drawRoundRect(cardX, cardY, cardW, cardH, 20, 20);
        g2.setStroke(new BasicStroke(1));

        BrainRot received = starterRots[capsuleCursor];
        BufferedImage sprite = getSprite(received);

        if (sprite != null) {
            g2.drawImage(sprite, cardX + 20, cardY + 60, 160, 160, null);
        }

        g2.setFont(new Font("Arial", Font.BOLD, 28));
        g2.setColor(new Color(40, 44, 52));
        g2.drawString(received.getName(), cardX + 210, cardY + 60);

        g2.setFont(new Font("Arial", Font.BOLD, 16));
        g2.setColor(new Color(80, 80, 80));
        g2.drawString("TYPE: " + received.getPrimaryType().name(), cardX + 210, cardY + 95);
        g2.drawString("LEVEL: " + received.getLevel(), cardX + 370, cardY + 95);

        g2.setColor(new Color(60, 160, 80));
        g2.drawString("HP: " + received.getMaxHp(), cardX + 210, cardY + 125);

        g2.setColor(new Color(200, 200, 200));
        g2.drawLine(cardX + 210, cardY + 145, cardX + cardW - 30, cardY + 145);

        g2.setFont(new Font("Arial", Font.PLAIN, 15));
        g2.setColor(new Color(60, 64, 70));
        String desc = Constants.getDescription(received.getName());
        drawWrappedText(g2, desc, cardX + 210, cardY + 175, cardW - 500);

        int skillsX = cardX + cardW - 270;
        int skillsY = cardY + 165;

        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.setColor(new Color(100, 100, 100));
        g2.drawString("STARTING SKILLS:", skillsX, skillsY);

        List<Skill> moves = received.getMoves();
        for (int i = 0; i < moves.size(); i++) {
            Skill m = moves.get(i);
            int sx = skillsX + (i % 2 == 1 ? 125 : 0);
            int sy = skillsY + 15 + (i >= 2 ? 80 : 0);

            g2.setColor(new Color(240, 235, 220));
            g2.fillRoundRect(sx, sy, 115, 70, 8, 8);

            g2.setColor(new Color(50, 50, 50));
            g2.setFont(new Font("Arial", Font.BOLD, 12));

            String[] mName = m.getName().split(" ");
            if (mName.length > 1) {
                g2.drawString(mName[0], sx + 10, sy + 20);
                g2.drawString(mName[1], sx + 10, sy + 35);
            } else {
                g2.drawString(m.getName(), sx + 10, sy + 25);
            }

            g2.setFont(new Font("Arial", Font.PLAIN, 11));
            g2.drawString("Type: " + m.getType().name(), sx + 10, sy + 50);
            g2.drawString("UP: " + m.getCurrentUP() + "/" + m.getMaxUP(), sx + 10, sy + 63);
        }
    }

    private void drawWrappedText(Graphics2D g2, String text, int x, int y, int maxWidth) {
        FontMetrics fm = g2.getFontMetrics();
        int lineHeight = fm.getHeight() + 4;
        int curY = y;
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();

        for (String word : words) {
            if (fm.stringWidth(line + word) < maxWidth) {
                line.append(word).append(" ");
            } else {
                g2.drawString(line.toString(), x, curY);
                line = new StringBuilder(word).append(" ");
                curY += lineHeight;
            }
        }
        g2.drawString(line.toString(), x, curY);
    }

    private void drawDialogueBox(Graphics2D g2, String text, int boxY) {
        int boxH = 126;
        int boxX = 10;
        int boxW = SCREEN_WIDTH - 20;

        if (dialogueBoxFrame != null) {
            g2.drawImage(dialogueBoxFrame, 6, boxY, SCREEN_WIDTH - 12, boxH, null);
        } else {
            g2.setColor(new Color(250, 250, 250));
            g2.fillRoundRect(boxX, boxY, boxW, boxH, 15, 15);
            g2.setStroke(new BasicStroke(5));
            g2.setColor(new Color(90, 95, 105));
            g2.drawRoundRect(boxX, boxY, boxW, boxH, 15, 15);
            g2.setStroke(new BasicStroke(2));
            g2.setColor(new Color(200, 80, 80));
            g2.drawRoundRect(boxX + 4, boxY + 4, boxW - 8, boxH - 8, 10, 10);
            g2.setStroke(new BasicStroke(1));
        }

        g2.setFont(new Font("Arial", Font.BOLD, 22));
        g2.setColor(new Color(50, 50, 50));

        drawWrappedText(g2, text, boxX + 25, boxY + 50, boxW - 180);
    }

    private void drawYesNoMenu(Graphics2D g2, int boxY) {
        int menuW = 160, menuH = 126;
        int menuX = SCREEN_WIDTH - menuW - 10;
        int menuY = boxY;

        g2.setColor(Color.WHITE);
        g2.fillRoundRect(menuX, menuY, menuW, menuH, 12, 12);
        g2.setStroke(new BasicStroke(4));
        g2.setColor(new Color(90, 95, 105));
        g2.drawRoundRect(menuX, menuY, menuW, menuH, 12, 12);

        g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.setColor(new Color(50, 50, 50));
        g2.drawString("YES", menuX + 60, menuY + 50);
        g2.drawString("NO", menuX + 60, menuY + 95);

        int cx = menuX + 35;
        int cy = menuY + (confirmCursor == 0 ? 36 : 81);
        g2.fillPolygon(new int[]{cx, cx+12, cx}, new int[]{cy, cy+8, cy+16}, 3);
    }

    private BufferedImage getSprite(BrainRot rot) {
        String key = rot.getName() + "_" + rot.getTier().name();
        if (spriteCache.containsKey(key)) return spriteCache.get(key);

        String path = "/res/InteractiveTiles/Brainrots/" + toFolderName(rot.getName()) + "/" + rot.getTier().name() + "_1.png";
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