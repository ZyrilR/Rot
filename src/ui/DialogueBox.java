package ui;

import engine.GamePanel;
import utils.AssetManager;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import static utils.Constants.*;

public class DialogueBox {

    GamePanel gp;
    private ArrayList<String> speakers;
    private ArrayList<String> currentDialogues;
    private int dialogueIndex = 0;
    public boolean isPlaying = false;

    // --- CINEMATIC MODE FLAG ---
    public boolean cinematicMode = false;

    private String displayedText = "";
    private int charIndex = 0;

    // Float accumulator for 1.5x speed
    private float charAccumulator = 0f;

    private boolean pendingShopOpen = false;

    private Runnable onFinish = null;
    public void setOnFinish(Runnable cb) { this.onFinish = cb; }

    public DialogueBox(GamePanel gp) {
        this.gp = gp;
    }

    public void setPendingShopOpen(boolean open) {
        this.pendingShopOpen = open;
        gp.GAMESTATE = "shop";
        gp.SHOPUI.open();
    }

    public void startDialogue(String name, ArrayList<String> dialogues) {
        ArrayList<String> speakerList = new ArrayList<>();
        for (int i = 0; i < dialogues.size(); i++) {
            speakerList.add(name);
        }
        startCutscene(speakerList, dialogues);
    }

    public void startCutscene(ArrayList<String> speakerList, ArrayList<String> dialogues) {
        this.speakers = speakerList;
        this.currentDialogues = dialogues;
        this.dialogueIndex = 0;
        resetTypewriter();
        gp.GAMESTATE = "dialogue";
        isPlaying = true;
    }

    private void resetTypewriter() {
        displayedText = "";
        charIndex = 0;
        charAccumulator = 0f;
    }

    public void update() {
        if (currentDialogues == null || currentDialogues.isEmpty()) {
            gp.GAMESTATE = "play";
            return;
        }

        if (dialogueIndex >= currentDialogues.size()) {
            finishDialogue();
            return;
        }

        String targetText = currentDialogues.get(dialogueIndex);
        if (targetText == null) {
            finishDialogue();
            return;
        }

        // --- 1.5x TYPEWRITER SPEED LOGIC ---
        if (charIndex < targetText.length()) {
            float baseCharsPerFrame = 1.0f / (TEXT_SPEED + 1.0f);
            float speedMultiplier = 1.5f;

            charAccumulator += (baseCharsPerFrame * speedMultiplier);

            while (charAccumulator >= 1.0f && charIndex < targetText.length()) {
                displayedText += targetText.charAt(charIndex);
                charIndex++;
                charAccumulator -= 1.0f;
            }
        }

        if (gp.KEYBOARDHANDLER.enterPressed) {
            gp.KEYBOARDHANDLER.enterPressed = false;
            if (charIndex < targetText.length()) {
                displayedText = targetText;
                charIndex = targetText.length();
            } else {
                if (dialogueIndex + 1 >= currentDialogues.size()) {
                    finishDialogue();
                } else {
                    dialogueIndex++;
                    resetTypewriter();
                }
            }
        }
    }

    private void finishDialogue() {
        dialogueIndex = 0;
        resetTypewriter();
        gp.GAMESTATE = "play";
        isPlaying = false;
        Runnable cb = onFinish;
        onFinish = null;
        if (cb != null) cb.run();
    }

    public void draw(Graphics2D g2) {
        if (speakers == null || currentDialogues == null || dialogueIndex >= speakers.size()) {
            return;
        }

        int x, y, width, height;
        int arc = 12;

        if (cinematicMode) {
            // --- AESTHETIC CENTERED MODAL ---
            width = SCREEN_WIDTH - (TILE_SIZE * 4); // Nice margins on the side
            height = TILE_SIZE * 6;                 // Tall enough for the longest paragraph
            x = (SCREEN_WIDTH - width) / 2;         // Perfectly centered X
            y = (SCREEN_HEIGHT - height) / 2;       // Perfectly centered Y
        } else {
            // Standard RPG box at the bottom
            x = TILE_SIZE;
            y = TILE_SIZE * 8;
            width = SCREEN_WIDTH - (TILE_SIZE * 2);
            height = TILE_SIZE * 3;
        }

        g2.setColor(Color.WHITE);
        g2.fillRoundRect(x, y, width, height, arc, arc);
        g2.setColor(new Color(80, 80, 80));
        g2.setStroke(new BasicStroke(6));
        g2.drawRoundRect(x, y, width, height, arc, arc);
        g2.setColor(new Color(216, 184, 88));
        g2.setStroke(new BasicStroke(4));
        g2.drawRoundRect(x + 1, y + 1, width - 2, height - 2, arc, arc);
        g2.setColor(new Color(80, 80, 80));
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(x + 4, y + 4, width - 8, height - 8, arc - 2, arc - 2);
        g2.setStroke(new BasicStroke(1));

        Font dialogueFont = (AssetManager.pokemonGb != null)
                ? AssetManager.pokemonGb
                : new Font("Arial", Font.PLAIN, 18);

        String currentSpeaker = speakers.get(dialogueIndex);
        String targetText = currentDialogues.get(dialogueIndex);

        int textX = x + 35;

        BufferedImage portrait = getPortrait(currentSpeaker);
        if (portrait != null) {
            g2.setColor(new Color(230, 226, 218));
            g2.fillRoundRect(x + 16, y + 16, 112, 112, 8, 8);
            g2.setColor(new Color(190, 185, 172));
            g2.drawRoundRect(x + 16, y + 16, 112, 112, 8, 8);
            g2.drawImage(portrait, x + 24, y + 24, 96, 96, null);
            textX += 115;
        }

        int maxTextWidth = (x + width) - textX - 25;

        g2.setFont(dialogueFont.deriveFont(16f));
        FontMetrics fm = g2.getFontMetrics();

        int lineHeight = 26;
        int speakerSpacing = (currentSpeaker != null && !currentSpeaker.isEmpty()) ? 28 : 0;

        int startY;
        if (cinematicMode) {
            // Static Top-Left for the cinematic box (Leaves a clean 40px top margin)
            startY = y + 40 + fm.getAscent();
        } else {
            // Dynamic Center for normal bottom-boxes
            int totalLines = countWrappedLines(targetText, fm, maxTextWidth);
            int totalContentHeight = speakerSpacing + (totalLines * lineHeight);
            startY = y + ((height - totalContentHeight) / 2) + fm.getAscent() + 8;
        }

        if (currentSpeaker != null && !currentSpeaker.isEmpty()) {
            g2.setColor(new Color(40, 40, 40));
            g2.setFont(dialogueFont.deriveFont(Font.BOLD, 20f));
            g2.drawString(currentSpeaker + ":", textX, startY);
            startY += speakerSpacing;
        }

        g2.setColor(new Color(64, 64, 64));
        g2.setFont(dialogueFont.deriveFont(15f));
        drawWrappedText(g2, displayedText, textX, startY, maxTextWidth, lineHeight);
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

    private void drawWrappedText(Graphics2D g2, String text, int x, int y, int maxWidth, int lineHeight) {
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

    private BufferedImage getPortrait(String speakerName) {
        if (speakerName == null || speakerName.isEmpty() || speakerName.equals("System") || speakerName.equals("ROT")) return null;
        String safeName = speakerName.toUpperCase();
        if (safeName.contains("TUNG TUNG")) return AssetManager.getBrainRotSprite("TUNG TUNG TUNG SAHUR", "NORMAL", false, 1);
        if (safeName.contains("SIR KHAI")) return AssetManager.loadImage("/res/InteractiveTiles/Trainer_Portraits/SirKhai.png");
        if (safeName.contains("DIN") || safeName.contains("PLAYER") || speakerName.equals(gp.player.name)) return AssetManager.loadImage("/res/InteractiveTiles/Player/4.png");

        return null;
    }
}