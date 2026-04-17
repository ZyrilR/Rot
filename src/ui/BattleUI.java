package ui;

import battle.BattleManager;
import brainrots.BrainRot;
import brainrots.ExperienceSystem;
import brainrots.LevelUpResult;
import items.Item;
import items.Capsule;
import engine.GamePanel;

import input.KeyboardHandler;
import skills.Skill;
import utils.AssetManager;
import utils.RandomUtil;
import utils.Constants;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import static utils.Constants.*;

public class BattleUI {

    private enum BattleState {
        INITIALIZING, MENU, SKILL_SELECT, TEAM_SELECT, TEAM_CONFIRM, // Added TEAM_CONFIRM
        BAG_OPEN, ANIMATION, ENEMY_AI, CLEANUP, FINISH, MESSAGE
    }
    private enum MenuOption { FIGHT, BAG, TEAM, RUN }

    private final GamePanel gp;
    private final KeyboardHandler kh;
    private BattleManager battle;

    private BattleState currentState = BattleState.INITIALIZING;
    private BattleState stateAfterMessage = BattleState.MENU;

    private MenuOption menuCursor = MenuOption.FIGHT;
    private int skillCursor = 0;
    private int partyCursor = 0;
    private int confirmCursor = 0; // 0 = YES, 1 = NO
    private int inputCooldown = 0;

    private boolean playerMovesFirst = true;
    private boolean turnOneComplete = false;
    private boolean turnTwoComplete = false;
    private boolean isInitialSendOut = true;
    private int playerChosenIndex = 0;
    private int enemyChosenIndex = 0;

    private String dialogueLine1 = "";
    private String dialogueLine2 = "";
    private int dialogueTicks = 0;

    private final Queue<String[]> messageQueue = new LinkedList<>();

    private final Map<String, BufferedImage> spriteCache = new HashMap<>();
    private BufferedImage hpFrame_player, hpFrame_enemy, dialogueBoxFrame, playerBackSprite;

    public BattleUI(GamePanel gp, KeyboardHandler kh) {
        this.gp = gp;
        this.kh = kh;
        loadAssets();
    }

    private void loadAssets() {
        hpFrame_player   = AssetManager.loadImage("/res/UI/Battle/hp_frame_player.png");
        hpFrame_enemy    = AssetManager.loadImage("/res/UI/Battle/hp_frame_enemy.png");
        dialogueBoxFrame = AssetManager.loadImage("/res/UI/Battle/dialogue_box.png");
        playerBackSprite = AssetManager.loadImage("/res/InteractiveTiles/player/4.png");
    }

    public void setBattle(BattleManager battle) {
        this.battle = battle;
        this.currentState = BattleState.INITIALIZING;
        this.turnOneComplete = false;
        this.turnTwoComplete = false;
        this.isInitialSendOut = true;
        this.messageQueue.clear();
        this.inputCooldown = INPUT_DELAY * 2;
    }

    public void update() {
        if (gp.GAMESTATE.equalsIgnoreCase("INVENTORY")) return;
        if (inputCooldown > 0) { inputCooldown--; return; }

        switch (currentState) {
            case INITIALIZING -> updateInitializing();
            case MENU -> updateMenu();
            case SKILL_SELECT -> updateSkillSelect();
            case TEAM_SELECT -> updateTeamSelect();
            case TEAM_CONFIRM -> updateTeamConfirm();
            case BAG_OPEN -> updateBagOpen();
            case ANIMATION -> updateAnimation();
            case ENEMY_AI -> updateEnemyAI();
            case CLEANUP -> updateCleanup();
            case FINISH -> updateFinish();
            case MESSAGE -> updateMessage();
        }
    }

    private void queueMessage(String line1, String line2) {
        messageQueue.add(new String[]{line1, line2});
    }

    private void playNextMessage(BattleState nextState) {
        if (!messageQueue.isEmpty()) {
            String[] msg = messageQueue.poll();
            dialogueLine1 = msg[0];
            dialogueLine2 = msg[1];
            dialogueTicks = 90;
            stateAfterMessage = nextState;
            currentState = BattleState.MESSAGE;
        } else {
            currentState = nextState;
        }
    }

    private void updateMessage() {
        if (dialogueTicks > 0) dialogueTicks--;
        if (kh.enterPressed || dialogueTicks <= 0) {
            kh.enterPressed = false;

            if (!messageQueue.isEmpty()) {
                playNextMessage(stateAfterMessage);
            } else {
                if (stateAfterMessage == BattleState.MENU || stateAfterMessage == BattleState.SKILL_SELECT) {
                    setPrompt();
                } else if (stateAfterMessage == BattleState.TEAM_SELECT) {
                    dialogueLine1 = "Who will you";
                    dialogueLine2 = "send out?";
                    dialogueTicks = 0;
                }
                currentState = stateAfterMessage;
                inputCooldown = INPUT_DELAY;
            }
        }
    }

    private void updateInitializing() {
        if (battle.isWildBattle()) {
            queueWildIntroDialogue(battle.getEnemyRot());
        } else {
            queueMessage("Trainer wants to battle!", "Get ready!");
        }
        queueMessage("Who will you", "send out?");
        playNextMessage(BattleState.TEAM_SELECT);
    }

    private void updateMenu() {
        if (kh.upPressed && (menuCursor == MenuOption.TEAM || menuCursor == MenuOption.RUN)) {
            menuCursor = (menuCursor == MenuOption.TEAM) ? MenuOption.FIGHT : MenuOption.BAG;
            inputCooldown = INPUT_DELAY;
        } else if (kh.downPressed && (menuCursor == MenuOption.FIGHT || menuCursor == MenuOption.BAG)) {
            menuCursor = (menuCursor == MenuOption.FIGHT) ? MenuOption.TEAM : MenuOption.RUN;
            inputCooldown = INPUT_DELAY;
        } else if (kh.leftPressed && (menuCursor == MenuOption.BAG || menuCursor == MenuOption.RUN)) {
            menuCursor = (menuCursor == MenuOption.BAG) ? MenuOption.FIGHT : MenuOption.TEAM;
            inputCooldown = INPUT_DELAY;
        } else if (kh.rightPressed && (menuCursor == MenuOption.FIGHT || menuCursor == MenuOption.TEAM)) {
            menuCursor = (menuCursor == MenuOption.FIGHT) ? MenuOption.BAG : MenuOption.RUN;
            inputCooldown = INPUT_DELAY;
        }

        if (kh.enterPressed) {
            kh.enterPressed = false;
            switch (menuCursor) {
                case FIGHT -> {
                    setPrompt();
                    currentState = BattleState.SKILL_SELECT;
                }
                case BAG ->  {
                    currentState = BattleState.BAG_OPEN;
                    gp.INVENTORYUI.openInBattle();
                    gp.GAMESTATE = "inventory";
                }
                case TEAM -> {
                    dialogueLine1 = "Who will you";
                    dialogueLine2 = "send out?";
                    dialogueTicks = 0;
                    currentState = BattleState.TEAM_SELECT;
                }
                case RUN -> attemptRun();
            }
            inputCooldown = INPUT_DELAY;
        }
    }

    private void updateBagOpen() {
        if (gp.GAMESTATE.equalsIgnoreCase("battle")) {
            Item chosen = gp.INVENTORYUI.getSelectedItemForBattle();

            if (chosen != null) {
                gp.INVENTORYUI.clearSelectedItemForBattle();
                executeItemTurn(chosen);
            } else {
                setPrompt();
                currentState = BattleState.MENU;
            }
        }
    }

    private void executeItemTurn(Item item) {
        playerMovesFirst = true;

        if (item instanceof Capsule) {
            if (!battle.isWildBattle()) {
                queueMessage("You can't catch", "a trainer's BrainRot!");
                playNextMessage(BattleState.MENU);
                return;
            }

            gp.player.getInventory().removeItem(item);

            boolean success = battle.executeCapture(item);
            if (success) {
                queueMessage("Gotcha!", battle.getEnemyRot().getName() + " was caught!");
                gp.player.getPCSYSTEM().addBrainRot(battle.getEnemyRot());
                playNextMessage(BattleState.FINISH);
            } else {
                queueMessage("Oh no!", "The BrainRot broke free!");
                playerChosenIndex = -2;
                playNextMessage(BattleState.ENEMY_AI);
            }
        } else {
            int oldHp = battle.getPlayerRot().getCurrentHp();
            item.use(battle.getPlayerRot());
            int healed = battle.getPlayerRot().getCurrentHp() - oldHp;

            queueMessage("Used " + item.getName() + "!", "");
            if (healed > 0) {
                queueMessage(battle.getPlayerRot().getName(), "recovered " + healed + " HP!");
            }

            gp.player.getInventory().removeItem(item);
            playerChosenIndex = -2;
            playNextMessage(BattleState.ENEMY_AI);
        }
    }

    private void updateSkillSelect() {
        int moveCount = battle.getPlayerRot().getMoves().size();

        if (kh.upPressed && skillCursor >= 2) skillCursor -= 2;
        else if (kh.downPressed && skillCursor < moveCount - 2) skillCursor += 2;
        else if (kh.leftPressed && skillCursor % 2 != 0) skillCursor--;
        else if (kh.rightPressed && skillCursor % 2 == 0 && skillCursor + 1 < moveCount) skillCursor++;

        if (kh.enterPressed) {
            kh.enterPressed = false;
            Skill chosenSkill = battle.getPlayerRot().getMoves().get(skillCursor);

            if (chosenSkill.getCurrentUP() < 1) {
                queueMessage(battle.getPlayerRot().getName(), "doesn't have enough UP for " + chosenSkill.getName() + "!");
                playNextMessage(BattleState.SKILL_SELECT);
            } else {
                playerChosenIndex = skillCursor;
                currentState = playerMovesFirst ? BattleState.ENEMY_AI : BattleState.ANIMATION;
            }
            inputCooldown = INPUT_DELAY;
        }

        if (kh.escPressed) {
            kh.escPressed = false;
            setPrompt();
            currentState = BattleState.MENU;
            inputCooldown = INPUT_DELAY;
        }
    }

    private void updateTeamSelect() {
        int size = gp.player.getPCSYSTEM().getPartySize();

        if (kh.upPressed && partyCursor > 0) { partyCursor--; inputCooldown = INPUT_DELAY; }
        else if (kh.downPressed && partyCursor < size - 1) { partyCursor++; inputCooldown = INPUT_DELAY; }

        if (kh.escPressed && battle.getPlayerRot() != null && !battle.getPlayerRot().getName().isEmpty() && !isInitialSendOut) {
            kh.escPressed = false;
            setPrompt();
            currentState = BattleState.MENU;
            inputCooldown = INPUT_DELAY;
        }

        if (kh.enterPressed) {
            kh.enterPressed = false;
            BrainRot selected = gp.player.getPCSYSTEM().getPartyMember(partyCursor);

            if (!isInitialSendOut && selected == battle.getPlayerRot()) {
                queueMessage(selected.getName(), "is already in battle!");
                playNextMessage(BattleState.TEAM_SELECT);
            } else if (selected.isFainted()) {
                queueMessage(selected.getName(), "has no energy left!");
                playNextMessage(BattleState.TEAM_SELECT);
            } else {
                dialogueLine1 = "Reviewing " + selected.getName() + ".";
                dialogueLine2 = "Send this BrainRot into battle?";
                dialogueTicks = 0;
                confirmCursor = 0;
                currentState = BattleState.TEAM_CONFIRM;
            }
            inputCooldown = INPUT_DELAY;
        }
    }

    private void updateTeamConfirm() {
        if (kh.upPressed || kh.downPressed) {
            confirmCursor = (confirmCursor == 0) ? 1 : 0;
            inputCooldown = INPUT_DELAY;
        }
        if (kh.escPressed) {
            kh.escPressed = false;
            dialogueLine1 = "Who will you";
            dialogueLine2 = "send out?";
            currentState = BattleState.TEAM_SELECT;
            inputCooldown = INPUT_DELAY;
        }
        if (kh.enterPressed) {
            kh.enterPressed = false;
            if (confirmCursor == 0) { // YES
                BrainRot selected = gp.player.getPCSYSTEM().getPartyMember(partyCursor);
                battle.setPlayerRot(selected);
                queueMessage("Go! " + selected.getName() + "!", "");

                if (isInitialSendOut) {
                    isInitialSendOut = false;
                    playNextMessage(BattleState.MENU);
                } else {
                    playerChosenIndex = -1;
                    playerMovesFirst = true;
                    playNextMessage(BattleState.ENEMY_AI);
                }
            } else { // NO
                dialogueLine1 = "Who will you";
                dialogueLine2 = "send out?";
                currentState = BattleState.TEAM_SELECT;
            }
            inputCooldown = INPUT_DELAY;
        }
    }

    private void updateEnemyAI() {
        enemyChosenIndex = Math.max(0, battle.getEnemyRot().getMoves().size() - 1);
        currentState = playerMovesFirst ? BattleState.ANIMATION : BattleState.MENU;
    }

    private void updateAnimation() {
        if (!turnOneComplete) {
            turnOneComplete = true;
            executeTurnOne();
        }
        else if (!turnTwoComplete) {
            turnTwoComplete = true;
            executeTurnTwo();
        }
        else {
            currentState = BattleState.CLEANUP;
        }
    }

    private void executeTurnOne() {
        BrainRot attacker = playerMovesFirst ? battle.getPlayerRot() : battle.getEnemyRot();
        BrainRot defender = playerMovesFirst ? battle.getEnemyRot() : battle.getPlayerRot();
        int skillIdx = playerMovesFirst ? playerChosenIndex : enemyChosenIndex;

        if (playerMovesFirst && (playerChosenIndex == -1 || playerChosenIndex == -2)) {
            playNextMessage(BattleState.ANIMATION);
            return;
        }

        if (!attacker.isFainted()) {
            Skill skill = attacker.getMoves().get(skillIdx);
            queueMessage(attacker.getName() + " used", skill.getName() + "!");

            int oldHp = defender.getCurrentHp();
            if (playerMovesFirst) battle.executePlayerTurn(skillIdx);
            else battle.executeEnemyTurn(skillIdx);
            int damage = oldHp - defender.getCurrentHp();

            if (damage > 0) {
                queueMessage(defender.getName(), "took " + damage + " damage!");
            } else if (damage < 0) {
                queueMessage(defender.getName(), "recovered " + (-damage) + " HP!");
            }

            if (defender.isFainted()) queueMessage(defender.getName() + " fainted!", "");
        }
        playNextMessage(BattleState.ANIMATION);
    }

    private void executeTurnTwo() {
        BrainRot attacker = playerMovesFirst ? battle.getEnemyRot() : battle.getPlayerRot();
        BrainRot defender = playerMovesFirst ? battle.getPlayerRot() : battle.getEnemyRot();
        int skillIdx = playerMovesFirst ? enemyChosenIndex : playerChosenIndex;

        if (!battle.getEnemyRot().isFainted() && !battle.getPlayerRot().isFainted() && !attacker.isFainted()) {
            if (!playerMovesFirst && (playerChosenIndex == -1 || playerChosenIndex == -2)) {
                // Nothing
            } else {
                Skill skill = attacker.getMoves().get(skillIdx);
                queueMessage(attacker.getName() + " used", skill.getName() + "!");

                int oldHp = defender.getCurrentHp();
                if (playerMovesFirst) battle.executeEnemyTurn(skillIdx);
                else battle.executePlayerTurn(skillIdx);
                int damage = oldHp - defender.getCurrentHp();

                if (damage > 0) {
                    queueMessage(defender.getName(), "took " + damage + " damage!");
                } else if (damage < 0) {
                    queueMessage(defender.getName(), "recovered " + (-damage) + " HP!");
                }

                if (defender.isFainted()) queueMessage(defender.getName() + " fainted!", "");
            }
        }
        playNextMessage(BattleState.ANIMATION);
    }

    private void updateCleanup() {
        battle.endTurn();
        turnOneComplete = false;
        turnTwoComplete = false;
        playerMovesFirst = battle.getPlayerRot().getSpeed() >= battle.getEnemyRot().getSpeed();

        if (battle.isOver()) {
            if (battle.getResult() == BattleManager.BattleResult.PLAYER_WIN) {
                int xp = ExperienceSystem.xpYield(battle.getEnemyRot());
                queueMessage(battle.getEnemyRot().getName() + " fainted!", "");
                queueMessage(battle.getPlayerRot().getName() + " gained", xp + " XP!");
                List<LevelUpResult> levelUps = battle.getPlayerRot().gainXp(xp);
                for (LevelUpResult lu : levelUps) {
                    queueMessage(battle.getPlayerRot().getName() + " grew to", "level " + lu.newLevel + "!");
                    if (lu.skillUnlocked != null) {
                        queueMessage(battle.getPlayerRot().getName() + " learned", lu.skillUnlocked.getName() + "!");
                    }
                }
            } else {
                queueMessage("Battle Finished!", "Result: " + battle.getResult().name());
            }
            playNextMessage(BattleState.FINISH);
        } else {
            if (playerMovesFirst) setPrompt();
            currentState = playerMovesFirst ? BattleState.MENU : BattleState.ENEMY_AI;
        }
    }

    private void updateFinish() {
        if (dialogueTicks > 0) dialogueTicks--;
        if (kh.enterPressed || dialogueTicks <= 0) {
            kh.enterPressed = false;
            gp.GAMESTATE = "play";
            gp.BLACKFADEEFFECT.start(BlackFadeEffect.FadeMode.FADE_OUT_TO_PLAY, 8);
            gp.encounterSystem.clearBattle();
            inputCooldown = INPUT_DELAY;
        }
    }

    private void attemptRun() {
        if (battle.isWildBattle()) {
            queueMessage("Got away safely!", "");
            playNextMessage(BattleState.FINISH);
        } else {
            queueMessage("No running from", "a trainer battle!");
            playNextMessage(BattleState.MENU);
        }
    }

    private void setPrompt() {
        this.dialogueLine1 = "What will";
        this.dialogueLine2 = battle.getPlayerRot().getName() + " do?";
        this.dialogueTicks = 0;
    }

    private void queueWildIntroDialogue(BrainRot wildRot) {
        String l1 = "A wild " + wildRot.getName() + " appeared!";
        String l2 = switch (wildRot.getName().toUpperCase()) {
            case "TUNG TUNG TUNG SAHUR" -> "started drumming wildly!";
            case "TRALALERO TRALALA"    -> "strolled in with fresh kicks!";
            case "BOMBARDINO CROCODILO" -> "crash-landed into battle!";
            case "LIRILI LARILA"        -> "slowly stepped out of time...";
            case "BRR BRR PATAPIM"      -> "goes brr brr... then patapim!";
            case "BONECA AMBALABU"      -> "rolled up screeching!";
            case "UDIN DIN DIN DIN DUN" -> "is vibrating aggressively!";
            case "CAPUCCINO ASSASSINO"  -> "emerged from the espresso steam!";
            default                     -> "is ready to fight!";
        };
        queueMessage(l1, l2);
    }

    // ── DRAWING LOGIC ─────────────────────────────────────────────────────────

    public void draw(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(new Color(100, 180, 100));
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        if (currentState == BattleState.TEAM_SELECT || currentState == BattleState.TEAM_CONFIRM) {
            drawFullTeamScreen(g2);
            return;
        }

        g2.setColor(new Color(150, 210, 150));
        g2.fillOval(480, 200, 240, 60);
        g2.fillOval(40, 440, 300, 70);

        boolean showTrainer = (currentState == BattleState.INITIALIZING || isInitialSendOut);

        BufferedImage enemySprite  = getSprite(battle.getEnemyRot());
        if (enemySprite != null) g2.drawImage(enemySprite, 500, 40, 200, 200, null);

        if (showTrainer) {
            if (playerBackSprite != null) g2.drawImage(playerBackSprite, 80, 240, 220, 220, null);
        } else {
            BufferedImage playerSprite = getSprite(battle.getPlayerRot());
            if (playerSprite != null) g2.drawImage(playerSprite, 60, 220, 260, 260, null);
        }

        drawEnemyHpBlock(g2);

        if (!showTrainer) {
            drawPlayerHpBlock(g2);
        }

        int boxY = SCREEN_HEIGHT - 136;
        int boxH = 126;

        if (dialogueBoxFrame != null) g2.drawImage(dialogueBoxFrame, 6, boxY, SCREEN_WIDTH - 12, boxH, null);
        else {
            g2.setColor(new Color(250, 250, 250));
            g2.fillRoundRect(10, boxY, SCREEN_WIDTH - 20, boxH, 15, 15);
            g2.setStroke(new BasicStroke(5));
            g2.setColor(new Color(90, 95, 105));
            g2.drawRoundRect(10, boxY, SCREEN_WIDTH - 20, boxH, 15, 15);
            g2.setStroke(new BasicStroke(2));
            g2.setColor(new Color(200, 80, 80));
            g2.drawRoundRect(14, boxY + 4, SCREEN_WIDTH - 28, boxH - 8, 10, 10);
            g2.setStroke(new BasicStroke(1));
        }

        drawDialogueText(g2, boxY);

        if (currentState == BattleState.MENU) drawMenu(g2, boxY);
        else if (currentState == BattleState.SKILL_SELECT) drawSkillSelect(g2, boxY);
    }

    private void drawFullTeamScreen(Graphics2D g2) {
        int pad = 15;
        int gap = 15;
        int panelW = (SCREEN_WIDTH - (pad * 2) - (gap * 2)) / 3;
        int panelH = SCREEN_HEIGHT - 165;

        drawUIPanel(g2, pad, pad, panelW, panelH, "TEAM SELECTION");
        drawUIPanel(g2, pad + panelW + gap, pad, panelW, panelH, "INFO");
        drawUIPanel(g2, pad + (panelW * 2) + (gap * 2), pad, panelW, panelH, "SKILLS");

        BrainRot selectedRot = gp.player.getPCSYSTEM().getPartyMember(partyCursor);

        int size = gp.player.getPCSYSTEM().getPartySize();
        for (int i = 0; i < size; i++) {
            BrainRot rot = gp.player.getPCSYSTEM().getPartyMember(i);

            int rowY = pad + 70 + (i * 55);
            int rowX = pad + 28;

            if (rot.isFainted() || rot == battle.getPlayerRot()) g2.setColor(new Color(150, 150, 150));
            else g2.setColor(new Color(50, 50, 50));

            g2.setFont(new Font("Arial", Font.BOLD, 14));
            String name = rot.getName();
            if (name.length() > 18) name = name.substring(0, 18) + "..";
            g2.drawString(name, rowX, rowY);

            g2.setFont(new Font("Arial", Font.BOLD, 12));
            String hpStr = rot.getCurrentHp() + "/" + rot.getMaxHp() + " HP";
            g2.drawString(hpStr, rowX + panelW - 105, rowY + 16);

            g2.setColor(new Color(80, 80, 80));
            g2.fillRect(rowX, rowY + 8, 115, 8);
            double hpFrac = Math.max(0, (double) rot.getCurrentHp() / rot.getMaxHp());
            g2.setColor(hpFrac > 0.5 ? new Color(80, 220, 100) : hpFrac > 0.2 ? new Color(220, 200, 50) : new Color(220, 80, 60));
            g2.fillRect(rowX, rowY + 8, (int)(115 * hpFrac), 8);

            if (i == partyCursor) {
                g2.setColor(new Color(50, 50, 50));
                g2.fillPolygon(new int[]{pad + 10, pad + 20, pad + 10}, new int[]{rowY - 10, rowY - 5, rowY}, 3);
            }
        }

        if (selectedRot != null) {
            int midX = pad + panelW + gap;
            BufferedImage spr = getSprite(selectedRot);
            if (spr != null) g2.drawImage(spr, midX + 15, pad + 50, 90, 90, null);

            g2.setFont(new Font("Arial", Font.BOLD, 16));
            g2.setColor(new Color(40, 40, 40));

            String[] nameParts = selectedRot.getName().split(" ");
            int ny = pad + 60;
            String currentLine = "";

            for(String part : nameParts) {
                String testLine = currentLine.isEmpty() ? part : currentLine + " " + part;
                if(g2.getFontMetrics().stringWidth(testLine) > panelW - 120) {
                    g2.drawString(currentLine, midX + 115, ny);
                    ny += 20;
                    currentLine = part;
                } else {
                    currentLine = testLine;
                }
            }
            g2.drawString(currentLine, midX + 115, ny);

            ny += 25;

            g2.setFont(new Font("Arial", Font.PLAIN, 14));
            g2.drawString("Type: " + selectedRot.getPrimaryType().name(), midX + 115, ny);
            ny += 20;
            g2.drawString("Lv " + selectedRot.getLevel(), midX + 115, ny);
            ny += 25;

            g2.setFont(new Font("Arial", Font.BOLD, 13));
            g2.drawString("HP: " + selectedRot.getCurrentHp() + "/" + selectedRot.getMaxHp(), midX + 115, ny);
            ny += 8;
            g2.setColor(new Color(80, 80, 80));
            g2.fillRect(midX + 115, ny, 90, 8);
            double mHpFrac = Math.max(0, (double) selectedRot.getCurrentHp() / selectedRot.getMaxHp());
            g2.setColor(new Color(220, 80, 60));
            g2.fillRect(midX + 115, ny, (int)(90 * mHpFrac), 8);

            g2.setFont(new Font("Arial", Font.PLAIN, 13));
            String desc = Constants.getDescription(selectedRot.getName());

            int dy = Math.max(ny + 30, pad + 160);

            StringBuilder line = new StringBuilder("Description: ");
            for (String word : desc.split(" ")) {
                if (g2.getFontMetrics().stringWidth(line + " " + word) > panelW - 40) {
                    g2.drawString(line.toString(), midX + 20, dy);
                    dy += 18;
                    line = new StringBuilder(word);
                } else {
                    line.append(" ").append(word);
                }
            }
            g2.drawString(line.toString(), midX + 20, dy);
        }

        if (selectedRot != null) {
            int rightX = pad + (panelW * 2) + (gap * 2);
            List<Skill> moves = selectedRot.getMoves();

            for (int i = 0; i < moves.size(); i++) {
                Skill m = moves.get(i);
                int sx = rightX + 15 + (i % 2 == 1 ? (panelW/2 - 5) : 0);
                int sy = pad + 50 + (i >= 2 ? 100 : 0);

                g2.setColor(new Color(240, 235, 220));
                g2.fillRoundRect(sx, sy, panelW/2 - 25, 80, 8, 8);

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
                g2.drawString("UP: " + m.getCurrentUP() + "/" + m.getMaxUP(), sx + 10, sy + 65);
            }
        }

        int boxY = SCREEN_HEIGHT - 136;
        if (dialogueBoxFrame != null) g2.drawImage(dialogueBoxFrame, 6, boxY, SCREEN_WIDTH - 12, 126, null);
        else drawUIPanel(g2, 10, boxY, SCREEN_WIDTH - 20, 126, "");

        drawDialogueText(g2, boxY);

        if (currentState == BattleState.TEAM_CONFIRM) {
            int menuW = 160, menuH = 126;
            int menuX = SCREEN_WIDTH - menuW - 10;
            int menuY = boxY;

            drawUIPanel(g2, menuX, menuY, menuW, menuH, "");

            g2.setFont(new Font("Arial", Font.BOLD, 20));
            g2.setColor(new Color(50, 50, 50));
            g2.drawString("YES", menuX + 60, menuY + 50);
            g2.drawString("NO", menuX + 60, menuY + 95);

            int cx = menuX + 35;
            int cy = menuY + (confirmCursor == 0 ? 36 : 81);
            drawCursor(g2, cx, cy);
        }
    }

    private void drawUIPanel(Graphics2D g2, int x, int y, int w, int h, String title) {
        g2.setColor(new Color(250, 250, 250));
        g2.fillRoundRect(x, y, w, h, 12, 12);
        g2.setStroke(new BasicStroke(4));
        g2.setColor(new Color(90, 95, 105));
        g2.drawRoundRect(x, y, w, h, 12, 12);
        g2.setStroke(new BasicStroke(1));

        if (!title.isEmpty()) {
            g2.setFont(new Font("Arial", Font.BOLD, 18));
            g2.setColor(new Color(50, 50, 50));
            FontMetrics fm = g2.getFontMetrics();
            int tx = x + (w - fm.stringWidth(title)) / 2;
            g2.drawString(title, tx, y + 25);
            g2.drawLine(x + 10, y + 35, x + w - 10, y + 35);
        }
    }

    private void drawEnemyHpBlock(Graphics2D g2) {
        BrainRot rot = battle.getEnemyRot();
        drawHpFrame(g2, 40, 40, 320, 76, rot, false, hpFrame_enemy);
    }

    private void drawPlayerHpBlock(Graphics2D g2) {
        BrainRot rot = battle.getPlayerRot();
        drawHpFrame(g2, SCREEN_WIDTH - 360, SCREEN_HEIGHT - 250, 340, 96, rot, true, hpFrame_player);
    }

    private void drawHpFrame(Graphics2D g2, int x, int y, int w, int h, BrainRot rot, boolean isPlayer, BufferedImage frameImg) {
        if (frameImg != null) {
            g2.drawImage(frameImg, x, y, w, h, null);
        } else {
            drawUIPanel(g2, x, y, w, h, "");
        }

        Font baseFont = new Font("Arial", Font.BOLD, 15);
        g2.setFont(baseFont);
        g2.setColor(new Color(50, 50, 50));
        g2.drawString(rot.getName(), x + 20, y + 28);

        g2.setFont(baseFont.deriveFont(Font.BOLD, 14f));
        String lvStr = "Lv " + rot.getLevel();
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(lvStr, x + w - fm.stringWidth(lvStr) - 20, y + 28);

        int barX = x + 45, barY = y + 42, barW = w - 70, barH = 10;
        g2.setColor(new Color(80, 80, 80));
        g2.fillRect(barX, barY, barW, barH);

        double hpFrac = Math.max(0, (double) rot.getCurrentHp() / rot.getMaxHp());
        Color hpC = hpFrac > 0.5 ? new Color(80, 220, 100) : hpFrac > 0.2 ? new Color(220, 200, 50) : new Color(220, 80, 60);
        g2.setColor(hpC);
        g2.fillRect(barX, barY, (int)(barW * hpFrac), barH);

        if (isPlayer) {
            g2.setFont(baseFont);
            g2.setColor(new Color(50, 50, 50));
            String hpNum = rot.getCurrentHp() + " / " + rot.getMaxHp();
            fm = g2.getFontMetrics();
            g2.drawString(hpNum, x + w - fm.stringWidth(hpNum) - 25, y + 78);

        }
    }

    private void drawMenu(Graphics2D g2, int boxY) {
        int menuW = 320;
        int menuX = SCREEN_WIDTH - menuW - 10;
        int menuY = boxY;
        int menuH = 126;

        drawUIPanel(g2, menuX, menuY, menuW, menuH, "");

        g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.setColor(new Color(50, 50, 50));

        g2.drawString("FIGHT", menuX + 50, menuY + 50);
        g2.drawString("BAG", menuX + 200, menuY + 50);
        g2.drawString("TEAM", menuX + 50, menuY + 90);
        g2.drawString("RUN", menuX + 200, menuY + 90);

        int cx = menuX + 25;
        int cy = menuY + 36;
        if (menuCursor == MenuOption.BAG || menuCursor == MenuOption.RUN) cx += 150;
        if (menuCursor == MenuOption.TEAM || menuCursor == MenuOption.RUN) cy += 40;
        drawCursor(g2, cx, cy);
    }

    private void drawSkillSelect(Graphics2D g2, int boxY) {
        int menuW = 480;
        int menuX = SCREEN_WIDTH - menuW - 10;
        int menuY = boxY;
        int menuH = 126;

        drawUIPanel(g2, menuX, menuY, menuW, menuH, "");

        g2.setFont(new Font("Arial", Font.BOLD, 18));

        List<Skill> moves = battle.getPlayerRot().getMoves();
        for (int i = 0; i < moves.size(); i++) {
            int dx = menuX + 40 + (i % 2 == 1 ? 230 : 0);
            int dy = menuY + 50 + (i >= 2 ? 40 : 0);

            if (moves.get(i).getCurrentUP() < 1) {
                g2.setColor(new Color(180, 180, 180));
            } else {
                g2.setColor(new Color(50, 50, 50));
            }

            g2.drawString(moves.get(i).getName(), dx, dy);
            if (i == skillCursor) drawCursor(g2, dx - 25, dy - 14);
        }
    }

    private void drawDialogueText(Graphics2D g2, int boxY) {
        g2.setFont(new Font("Arial", Font.BOLD, 22));
        g2.setColor(new Color(50, 50, 50));

        boolean menuOpen = (currentState == BattleState.MENU ||
                currentState == BattleState.SKILL_SELECT);

        if (menuOpen || currentState == BattleState.TEAM_SELECT || currentState == BattleState.TEAM_CONFIRM) {
            g2.drawString(dialogueLine1, 35, boxY + 55);
            g2.drawString(dialogueLine2, 35, boxY + 95);
        } else {
            FontMetrics fm = g2.getFontMetrics();
            int boxX = 10;
            int boxW = SCREEN_WIDTH - 20;
            int boxH = 126;

            boolean hasLine2 = dialogueLine2 != null && !dialogueLine2.isEmpty();

            if (hasLine2) {
                int totalTextHeight = fm.getHeight() * 2;
                int startY = boxY + (boxH - totalTextHeight) / 2 + fm.getAscent();

                int x1 = boxX + (boxW - fm.stringWidth(dialogueLine1)) / 2;
                g2.drawString(dialogueLine1, x1, startY);

                int x2 = boxX + (boxW - fm.stringWidth(dialogueLine2)) / 2;
                g2.drawString(dialogueLine2, x2, startY + fm.getHeight() + 5);
            } else {
                int x1 = boxX + (boxW - fm.stringWidth(dialogueLine1)) / 2;
                int y1 = boxY + (boxH - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(dialogueLine1, x1, y1);
            }
        }
    }

    private void drawCursor(Graphics2D g2, int x, int y) {
        g2.setColor(new Color(50, 50, 50));
        g2.fillPolygon(new int[]{x, x+12, x}, new int[]{y, y+8, y+16}, 3);
    }

    private BufferedImage getSprite(BrainRot rot) {
        if (rot == null) return null;
        String key = rot.getName() + "_" + rot.getTier().name();
        if (spriteCache.containsKey(key)) return spriteCache.get(key);

        String path = "/res/InteractiveTiles/Brainrots/" + toFolderName(rot.getName())
                + "/" + rot.getTier().name() + "_1.png";
        BufferedImage img = AssetManager.loadImage(path);
        spriteCache.put(key, img);
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