package ui;

import engine.GamePanel;
import utils.AssetManager;
import java.awt.*;
import java.util.ArrayList;

import static utils.Constants.*;

public class DialogueBox {

    GamePanel gp;
    private ArrayList<String> currentDialogues;
    private int dialogueIndex = 0;
    public boolean isPlaying = false;

    // Typewriter state
    private String displayedText = "";
    private int charIndex = 0;
    private int textSpeedCounter = 0;

    private String npcName = "";

    // Set to true when a MarketNPC interaction is pending shop open
    private boolean pendingShopOpen = false;

    public DialogueBox(GamePanel gp) {
        this.gp = gp;
    }

    public void setPendingShopOpen(boolean open) {
        this.pendingShopOpen = open;
        gp.GAMESTATE = "shop";
        gp.SHOPUI.open();
    }

    public void startDialogue(String name, ArrayList<String> dialogues) {
        this.npcName = name;
        this.currentDialogues = dialogues;
        this.dialogueIndex = 0;
        resetTypewriter();
        gp.GAMESTATE = "dialogue";
        isPlaying = true;
    }

    private void resetTypewriter() {
        displayedText = "";
        charIndex = 0;
        textSpeedCounter = 0;
    }

    public void update() {
        // Guard: no dialogues loaded
        if (currentDialogues == null || currentDialogues.isEmpty()) {
            gp.GAMESTATE = "play";
            return;
        }

        // Guard: dialogue index out of bounds — dialogue is finished
        if (dialogueIndex >= currentDialogues.size()) {
            finishDialogue();
            isPlaying = false;
            return;
        }

        String targetText = currentDialogues.get(dialogueIndex);
        if (targetText == null) {
            isPlaying = false;
            finishDialogue();
            return;
        }

        // 1. Typewriter: reveal one character at a time
        if (charIndex < targetText.length()) {
            textSpeedCounter++;
            if (textSpeedCounter > TEXT_SPEED) {
                displayedText += targetText.charAt(charIndex);
                charIndex++;
                textSpeedCounter = 0;
            }
        }

        // 2. Player presses ENTER to advance or skip
        if (gp.KEYBOARDHANDLER.enterPressed) {
            gp.KEYBOARDHANDLER.enterPressed = false;

            if (charIndex < targetText.length()) {
                // Skip typewriter — show full line immediately
                displayedText = targetText;
                charIndex = targetText.length();
            } else {
                // Advance to next line
                dialogueIndex++;

                if (dialogueIndex >= currentDialogues.size()) {
                    finishDialogue();
                } else {
                    resetTypewriter();
                }
            }
        }
    }

    /** Called when all dialogue lines have been shown */
    private void finishDialogue() {
        dialogueIndex = 0;
        resetTypewriter();

        if (pendingShopOpen) {
            pendingShopOpen = false;
            gp.SHOPUI.open();
            gp.GAMESTATE = "shop";
            System.out.println("[DialogueBox] Dialogue finished — opening shop.");
        } else {
            gp.GAMESTATE = "play";
            isPlaying = false;
        }
    }

    public void draw(Graphics2D g2) {
        // 1. Box position & size
        int x = TILE_SIZE;
        int y = TILE_SIZE * 8;
        int width = SCREEN_WIDTH - (TILE_SIZE * 2);
        int height = TILE_SIZE * 3;
        int arc = 12;

        // 2. White background
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(x, y, width, height, arc, arc);

        // 3. Three-layered border
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

        // 4. Font setup
        Font dialogueFont = (AssetManager.pokemonGb != null)
                ? AssetManager.pokemonGb
                : new Font("Arial", Font.PLAIN, 18);

        int textX = x + 35;
        int textY = y + 50;

        // 5. NPC name
        if (npcName != null && !npcName.isEmpty()) {
            g2.setColor(new Color(40, 40, 40));
            g2.setFont(dialogueFont.deriveFont(Font.BOLD, 22f));
            g2.drawString(npcName + ":", textX, textY);
            textY += 35;
        }

        // 6. Typewriter text
        g2.setColor(new Color(64, 64, 64));
        g2.setFont(dialogueFont.deriveFont(18f));

        for (String line : displayedText.split("\n")) {
            g2.drawString(line, textX, textY);
            textY += 40;
        }
    }
}