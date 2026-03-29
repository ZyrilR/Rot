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

    // Typewriter math
    private String displayedText = "";
    private int charIndex = 0;
    private int textSpeedCounter = 0;

    private String npcName = "";

    public DialogueBox(GamePanel gp) {
        this.gp = gp;
    }

    public void startDialogue(String name, ArrayList<String> dialogues) {
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
        // 1. TYPEWRITER ANIMATION LOGIC
        if (currentDialogues != null && dialogueIndex < currentDialogues.size()) {
            String targetText = currentDialogues.get(dialogueIndex);

            // If the current line isn't fully spelled out yet, add the next letter
            if (charIndex < targetText.length()) {
                textSpeedCounter++;
                // Lower number = faster text. Higher number = slower text.
                if (textSpeedCounter > 1) {
                    displayedText += targetText.charAt(charIndex);
                    charIndex++;
                    textSpeedCounter = 0;
                }
            }
        }

        // 2. PLAYER INPUT LOGIC
        // When the player presses 'Enter' to go to the next line...
        if (gp.KEYBOARDHANDLER.enterPressed) {
            gp.KEYBOARDHANDLER.enterPressed = false; // Consume the button press

            // Move to the next line of dialogue
            dialogueIndex++;

            // Check if we have run out of dialogue lines!
            if (currentDialogues != null && dialogueIndex >= currentDialogues.size()) {

                // 1. Reset the index for the next time you talk to someone
                dialogueIndex = 0;

                // 2. Unfreeze the game! Put us back in play mode
                gp.GAMESTATE = "play";

            } else {
                // If there are still lines left, wipe the text clean and start typing the next line
                resetTypewriter();
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