package ui;

import engine.GamePanel;
import utils.AssetManager;
import java.awt.*;
import static utils.Constants.*;

public class DialogueBox {

    GamePanel gp;
    private String[] currentDialogues;
    private int dialogueIndex = 0;

    // Typewriter math
    private String displayedText = "";
    private int charIndex = 0;
    private int textSpeedCounter = 0;
    private final int TEXT_SPEED = 1;

    private String npcName = "";

    public DialogueBox(GamePanel gp) {
        this.gp = gp;
    }

    public void startDialogue(String name, String[] dialogues) {
        this.npcName = name;
        this.currentDialogues = dialogues;
        this.dialogueIndex = 0;
        resetTypewriter();
        gp.GAMESTATE = "dialogue";
    }

    private void resetTypewriter() {
        displayedText = "";
        charIndex = 0;
        textSpeedCounter = 0;
    }

    public void update() {
        if (currentDialogues == null || dialogueIndex >= currentDialogues.length || currentDialogues[dialogueIndex] == null) {
            gp.GAMESTATE = "play";
            return;
        }

        if (gp.keyboardHandler.enterPressed) {
            gp.keyboardHandler.enterPressed = false;

            if (charIndex < currentDialogues[dialogueIndex].length()) {
                charIndex = currentDialogues[dialogueIndex].length();
                displayedText = currentDialogues[dialogueIndex];
            } else {
                dialogueIndex++;
                if (dialogueIndex >= currentDialogues.length || currentDialogues[dialogueIndex] == null) {
                    gp.GAMESTATE = "play";
                } else {
                    resetTypewriter();
                }
            }
        }

        if (dialogueIndex < currentDialogues.length && currentDialogues[dialogueIndex] != null) {
            String fullText = currentDialogues[dialogueIndex];
            if (charIndex < fullText.length()) {
                textSpeedCounter++;
                if (textSpeedCounter > TEXT_SPEED) {
                    displayedText += fullText.charAt(charIndex);
                    charIndex++;
                    textSpeedCounter = 0;
                }
            }
        }
    }

    public void draw(Graphics2D g2) {
        // 1. Calculate Box Position & Size
        int x = TILE_SIZE;
        int y = TILE_SIZE * 8;
        int width = SCREEN_WIDTH - (TILE_SIZE * 2);
        int height = TILE_SIZE * 3;
        int arc = 12;

        // 2. Draw Solid White Background
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(x, y, width, height, arc, arc);

        // 3. Draw the 3-Layered Border
        // Outer Dark Gray
        g2.setColor(new Color(80, 80, 80));
        g2.setStroke(new BasicStroke(6));
        g2.drawRoundRect(x, y, width, height, arc, arc);

        // Middle Gold
        g2.setColor(new Color(216, 184, 88));
        g2.setStroke(new BasicStroke(4));
        g2.drawRoundRect(x + 1, y + 1, width - 2, height - 2, arc, arc);

        // Inner Dark Gray
        g2.setColor(new Color(80, 80, 80));
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(x + 4, y + 4, width - 8, height - 8, arc - 2, arc - 2);

        // 4. Setup Font and Colors
        // Fallback to Arial if pokemonGb isn't loaded yet
        Font dialogueFont = (AssetManager.pokemonGb != null) ? AssetManager.pokemonGb : new Font("Arial", Font.PLAIN, 18);

        int textX = x + 35;
        int textY = y + 50;

        // 5. Draw NPC Name
        if (npcName != null && !npcName.isEmpty()) {
            g2.setColor(new Color(40, 40, 40));
            // Use derived font for the name (slightly larger/bold feel)
            g2.setFont(dialogueFont.deriveFont(Font.BOLD, 22f));
            g2.drawString(npcName + ":", textX, textY);
            textY += 35;
        }

        // 6. Draw Dialogue (Typewriter)
        g2.setColor(new Color(64, 64, 64));
        g2.setFont(dialogueFont.deriveFont(18f));

        for (String line : displayedText.split("\n")) {
            g2.drawString(line, textX, textY);
            textY += 40;
        }
    }
}