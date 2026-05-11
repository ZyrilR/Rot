package ui;

import battle.BattleManager;
import battle.BattleReward;
import brainrots.BrainRot;
import brainrots.LevelUpResult;
import items.Item;
import items.Capsule;
import engine.GamePanel;

import input.KeyboardHandler;
import skills.Skill;
import utils.AssetManager;
import utils.Constants;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

import static utils.Constants.*;

public class BattleUI {

    private enum BattleState {
        INITIALIZING, MENU, SKILL_SELECT, TEAM_SELECT, TEAM_CONFIRM,
        BAG_OPEN, ANIMATION, ENEMY_AI, CLEANUP, FINISH, MESSAGE,
        LEVELUP_CHECK, LEVELUP_REPLACE_CONFIRM, LEVELUP_REPLACE_SELECT,
        TRAINER_POSE, ENEMY_REVEAL, SWAP_PROMPT
    }
    private enum MenuOption { FIGHT, BAG, TEAM, RUN }
    private enum AnimMode { IDLE, HURT, BUFF, ATTACK }

    private int pickAnimFrame(AnimMode mode) {
        switch (mode) {
            case HURT: {
                int span = HURT_END - HURT_START + 1;
                int idx  = (globalAnimTick / Math.max(1, HURT_TICKS_PER_FRAME)) % span;
                return HURT_START + idx;
            }
            case ATTACK: {
                int span = ATK_END - ATK_START + 1;
                int idx  = (globalAnimTick / Math.max(1, ATK_TICKS_PER_FRAME)) % span;
                return ATK_START + idx;
            }
            case BUFF: {
                int idx = (globalAnimTick / Math.max(1, BUFF_TICKS_PER_FRAME)) % 2;
                return (idx == 0) ? BUFF_FRAME : IDLE_FRAME;
            }
            default: return IDLE_FRAME;
        }
    }

    private static class BattleMessage {
        String line1, line2;
        int activeActor;
        int tickDuration;
        BattleMessage(String l1, String l2, int activeActor) {
            this(l1, l2, activeActor, -1);
        }
        BattleMessage(String l1, String l2, int activeActor, int tickDuration) {
            this.line1 = l1; this.line2 = l2; this.activeActor = activeActor;
            this.tickDuration = tickDuration;
        }
    }

    private final GamePanel gp;
    private final KeyboardHandler kh;
    private BattleManager battle;

    private BattleState currentState = BattleState.INITIALIZING;
    private BattleState stateAfterMessage = BattleState.MENU;

    private MenuOption menuCursor = MenuOption.FIGHT;
    private int skillCursor = 0;
    private int partyCursor = 0;
    private int confirmCursor = 0;
    private int inputCooldown = 0;

    private boolean playerMovesFirst = true;
    private boolean turnOneComplete = false;
    private boolean turnTwoComplete = false;
    private boolean isInitialSendOut = true;
    private int playerChosenIndex = 0;
    private int enemyChosenIndex = 0;

    private String dialogueLine1 = "";
    private String dialogueLine2 = "";
    private int currentMessageActor = 0;
    private int dialogueTicks = 0;

    private final Queue<BattleMessage> messageQueue = new LinkedList<>();

    private final Queue<Object[]> pendingReplacements = new LinkedList<>();
    private BrainRot replaceTargetRot;
    private Skill    replaceCandidateSkill;
    private int      replaceSlotCursor = 0;

    private final Map<String, BufferedImage> spriteCache = new HashMap<>();
    private BufferedImage hpFrame_player, hpFrame_enemy, dialogueBoxFrame, playerBackSprite;

    private static final int IDLE_FRAME = 1;
    private static final int HURT_START = 2;
    private static final int HURT_END   = 3;
    private static final int BUFF_FRAME = 4;
    private static final int ATK_START  = 5;
    private static final int ATK_END    = 6;

    private static final int IDLE_TICKS_PER_FRAME = 30;
    private static final int HURT_TICKS_PER_FRAME = 6;
    private static final int BUFF_TICKS_PER_FRAME = 8;
    private static final int ATK_TICKS_PER_FRAME  = 5;

    private int globalAnimTick = 0;

    private int poseTick = 0;
    private static final int POSE_SLIDE_IN_END = 18;
    private static final int POSE_HOLD_END     = 48;
    private static final int POSE_SLIDE_OUT_END = 66;
    private BufferedImage trainerPoseSprite;

    private int revealTick = 0;
    private static final int REVEAL_DELAY = 45;

    private boolean awaitingSwapPrompt = false;
    private int swapCursor = 0;

    // --- BATTLE BG VARIABLES ---
    private BufferedImage grassBg;
    private BufferedImage darkGrassBg;
    private BufferedImage caveBg;
    private BufferedImage waterBg;
    private BufferedImage trainerBg;
    public int currentBgTile = 2;

    public BattleUI(GamePanel gp, KeyboardHandler kh) {
        this.gp = gp;
        this.kh = kh;

        loadAssets();
    }

    private void loadAssets() {
        hpFrame_player   = AssetManager.loadImage("/res/UI/Battle/hp_frame_player.png");
        hpFrame_enemy    = AssetManager.loadImage("/res/UI/Battle/hp_frame_enemy.png");
        dialogueBoxFrame = AssetManager.loadImage("/res/UI/Battle/dialogue_box.png");
        playerBackSprite = AssetManager.loadImage("/res/InteractiveTiles/Player/4.png");
        grassBg = AssetManager.loadImage("/res/BattleBG/GrassBattleBG.png");
        darkGrassBg = AssetManager.loadImage("/res/BattleBG/DarkGrassBG.png");
        caveBg = AssetManager.loadImage("/res/BattleBG/CaveBattleBG.png");
        waterBg = AssetManager.loadImage("/res/BattleBG/WaterBattleBG.png");
        trainerBg = AssetManager.loadImage("/res/BattleBG/TrainerBattleBG.png");
    }

    // Shield to prevent fading from overwriting the background tile
    public void setBattle(BattleManager battle) {
        if (this.battle == battle) {
            setBattle(battle, this.currentBgTile);
        } else {
            setBattle(battle, 2);
        }
    }

    public void setBattle(BattleManager battle, int bgTileId) {
        this.battle = battle;
        this.currentBgTile = bgTileId;

        System.out.println("===========================================");
        System.out.println("BATTLE TRIGGERED!");
        System.out.println("Tile ID passed to BattleUI: " + bgTileId);
        System.out.println("===========================================");

        this.currentState = BattleState.INITIALIZING;
        this.turnOneComplete = false;
        this.turnTwoComplete = false;
        this.isInitialSendOut = true;
        this.awaitingSwapPrompt = false;
        this.poseTick = 0;
        this.revealTick = 0;
        this.trainerPoseSprite = null;
        this.messageQueue.clear();
        this.inputCooldown = INPUT_DELAY * 2;
    }

    public void update() {
        if (gp.GAMESTATE.equalsIgnoreCase("INVENTORY")) return;

        globalAnimTick++;

        if (inputCooldown > 0) { inputCooldown--; return; }

        switch (currentState) {
            case INITIALIZING             -> updateInitializing();
            case MENU                     -> updateMenu();
            case SKILL_SELECT             -> updateSkillSelect();
            case TEAM_SELECT              -> updateTeamSelect();
            case TEAM_CONFIRM             -> updateTeamConfirm();
            case BAG_OPEN                 -> updateBagOpen();
            case ANIMATION                -> updateAnimation();
            case ENEMY_AI                 -> updateEnemyAI();
            case CLEANUP                  -> updateCleanup();
            case FINISH                   -> updateFinish();
            case MESSAGE                  -> updateMessage();
            case LEVELUP_CHECK            -> updateLevelupCheck();
            case LEVELUP_REPLACE_CONFIRM  -> updateLevelupReplaceConfirm();
            case LEVELUP_REPLACE_SELECT   -> updateLevelupReplaceSelect();
            case TRAINER_POSE             -> updateTrainerPose();
            case ENEMY_REVEAL             -> updateEnemyReveal();
            case SWAP_PROMPT              -> updateSwapPrompt();
        }
    }

    private void loadTrainerPoseSprite() {
        npc.TrainerNPC tr = battle.getTrainer();
        if (tr == null) { trainerPoseSprite = null; return; }
        if (tr.sprites != null && tr.sprites.size() >= 5) {
            trainerPoseSprite = tr.sprites.get(4);
        } else {
            trainerPoseSprite = null;
        }
    }

    private void updateTrainerPose() {
        poseTick++;
        if (poseTick >= POSE_SLIDE_OUT_END) {
            poseTick = 0;
            BrainRot er = battle.getEnemyRot();
            if (battle.getTrainer() != null && er != null) {
                queueMessage(battle.getTrainer().name + " sent out", er.getName() + "!", 2);
            }
            playNextMessage(BattleState.ENEMY_REVEAL);
        }
    }

    private void updateEnemyReveal() {
        revealTick++;
        if (revealTick >= REVEAL_DELAY) {
            revealTick = 0;
            if (isInitialSendOut) {
                BrainRot lead = battle.getPlayerRot();
                isInitialSendOut = false;
                if (lead != null) queueMessage("Go! " + lead.getName() + "!", "", 1);
                playNextMessage(BattleState.MENU);
            } else if (awaitingSwapPrompt) {
                awaitingSwapPrompt = false;
                swapCursor = 0;
                dialogueLine1 = "Swap your BrainRot?";
                dialogueLine2 = "";
                dialogueTicks = 0;
                currentState = BattleState.SWAP_PROMPT;
                inputCooldown = INPUT_DELAY;
            } else {
                setPrompt();
                currentState = BattleState.MENU;
                inputCooldown = INPUT_DELAY;
            }
        }
    }

    private void updateSwapPrompt() {
        if (kh.upPressed || kh.downPressed) {
            swapCursor = (swapCursor == 0) ? 1 : 0;
            utils.AudioManager.playSFX(utils.Constants.SFX_SELECT);
            inputCooldown = INPUT_DELAY;
        }
        if (kh.enterPressed) {
            kh.enterPressed = false;
            utils.AudioManager.playSFX(utils.Constants.SFX_ENTER);
            if (swapCursor == 0) {
                dialogueLine1 = "Who will you";
                dialogueLine2 = "send out?";
                dialogueTicks = 0;
                currentState = BattleState.TEAM_SELECT;
            } else {
                setPrompt();
                currentState = BattleState.MENU;
            }
            inputCooldown = INPUT_DELAY;
        }
    }

    private void queueMessage(String line1, String line2) {
        messageQueue.add(new BattleMessage(line1, line2, 0));
    }

    private void queueMessage(String line1, String line2, int activeActor) {
        messageQueue.add(new BattleMessage(line1, line2, activeActor));
    }

    private void queueMessage(String line1, String line2, int activeActor, int tickDuration) {
        messageQueue.add(new BattleMessage(line1, line2, activeActor, tickDuration));
    }

    private void playNextMessage(BattleState nextState) {
        if (!messageQueue.isEmpty()) {
            BattleMessage msg = messageQueue.poll();
            dialogueLine1 = msg.line1;
            dialogueLine2 = msg.line2;
            currentMessageActor = msg.activeActor;
            dialogueTicks = (msg.tickDuration > 0) ? msg.tickDuration : 90;
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
        isInitialSendOut = true;
        awaitingSwapPrompt = false;

        if (battle.isWildBattle()) {
            queueWildIntroDialogue(battle.getEnemyRot());
            playNextMessage(BattleState.ENEMY_REVEAL);
        } else {
            queueMessage("Trainer wants to battle!", "Get ready!");
            loadTrainerPoseSprite();
            poseTick = 0;
            playNextMessage(BattleState.TRAINER_POSE);
        }
    }

    private void updateMenu() {
        if (kh.upPressed && (menuCursor == MenuOption.TEAM || menuCursor == MenuOption.RUN)) {
            menuCursor = (menuCursor == MenuOption.TEAM) ? MenuOption.FIGHT : MenuOption.BAG;
            utils.AudioManager.playSFX(utils.Constants.SFX_SELECT);
            inputCooldown = INPUT_DELAY;
        } else if (kh.downPressed && (menuCursor == MenuOption.FIGHT || menuCursor == MenuOption.BAG)) {
            menuCursor = (menuCursor == MenuOption.FIGHT) ? MenuOption.TEAM : MenuOption.RUN;
            utils.AudioManager.playSFX(utils.Constants.SFX_SELECT);
            inputCooldown = INPUT_DELAY;
        } else if (kh.leftPressed && (menuCursor == MenuOption.BAG || menuCursor == MenuOption.RUN)) {
            menuCursor = (menuCursor == MenuOption.BAG) ? MenuOption.FIGHT : MenuOption.TEAM;
            utils.AudioManager.playSFX(utils.Constants.SFX_SELECT);
            inputCooldown = INPUT_DELAY;
        } else if (kh.rightPressed && (menuCursor == MenuOption.FIGHT || menuCursor == MenuOption.TEAM)) {
            menuCursor = (menuCursor == MenuOption.FIGHT) ? MenuOption.BAG : MenuOption.RUN;
            utils.AudioManager.playSFX(utils.Constants.SFX_SELECT);
            inputCooldown = INPUT_DELAY;
        }

        if (kh.enterPressed) {
            kh.enterPressed = false;
            utils.AudioManager.playSFX(utils.Constants.SFX_ENTER);

            switch (menuCursor) {
                case FIGHT -> {
                    setPrompt();
                    BrainRot pr = battle.getPlayerRot();
                    if (pr != null && pr.isOutOfUP()) {
                        playerChosenIndex = battle.STRUGGLE_INDEX;
                        currentState = playerMovesFirst ? BattleState.ENEMY_AI : BattleState.ANIMATION;
                    } else {
                        int moveCount = pr.getMoves().size();
                        if (skillCursor >= moveCount) skillCursor = 0;
                        currentState = BattleState.SKILL_SELECT;
                    }
                }
                case BAG   -> {
                    currentState = BattleState.BAG_OPEN;
                    gp.INVENTORYUI.openInBattle();
                    gp.GAMESTATE = "inventory";
                }
                case TEAM  -> {
                    dialogueLine1 = "Who will you";
                    dialogueLine2 = "send out?";
                    dialogueTicks = 0;
                    currentState = BattleState.TEAM_SELECT;
                }
                case RUN   -> attemptRun();
            }
            inputCooldown = INPUT_DELAY;
        }
    }

    private void updateBagOpen() {
        if (gp.GAMESTATE.equalsIgnoreCase("battle")) {
            Item chosen = gp.INVENTORYUI.getSelectedItemForBattle();
            if (chosen != null) {
                BrainRot chosenTarget = gp.INVENTORYUI.getSelectedTargetForBattle();
                gp.INVENTORYUI.clearSelectedItemForBattle();
                executeItemTurn(chosen, chosenTarget);
            } else {
                setPrompt();
                currentState = BattleState.MENU;
            }
        }
    }

    private void executeItemTurn(Item item) { executeItemTurn(item, null); }

    private void executeItemTurn(Item item, BrainRot chosenTarget) {
        playerMovesFirst = true;
        if (item instanceof Capsule) {
            if (!battle.isWildBattle()) {
                queueMessage("You can't catch", "a trainer's BrainRot!");
                playNextMessage(BattleState.MENU);
                return;
            }
            gp.player.getInventory().removeItem(item);
            boolean success = battle.executeCapture(item);

            String rotName = battle.getEnemyRot().getName();
            queueMessage("You threw a " + item.getName() + "!", "", 1, 45);
            queueMessage("Catching...", "*shake*",   2, 35);
            queueMessage("Catching...", "*shake shake*", 2, 35);
            queueMessage("Catching...", "*shake shake shake*", 2, 35);

            if (success) {
                utils.AudioManager.playMusic(utils.Constants.BGM_CAUGHT_ROT, false);
                queueMessage("Click!", "Gotcha!", 1, 50);
                queueMessage(rotName, "was caught!", 1);
                gp.NOTIFICATION.push("Captured!", rotName,
                        new java.awt.Color(120, 200, 240));
                gp.player.getPCSYSTEM().addBrainRot(battle.getEnemyRot());
                playNextMessage(BattleState.FINISH);
            } else {
                queueMessage("Oh no!", "The BrainRot broke free!", 2);
                playerChosenIndex = -2;
                playNextMessage(BattleState.ENEMY_AI);
            }
        } else {
            BrainRot target = chosenTarget != null ? chosenTarget : battle.getPlayerRot();
            int oldHp = target.getCurrentHp();
            item.use(target);
            int healed = target.getCurrentHp() - oldHp;
            queueMessage("Used " + item.getName() + "!", "", 1);
            if (healed > 0)
                queueMessage(target.getName(), "recovered " + healed + " HP!", 1);
            gp.player.getInventory().removeItem(item);
            playerChosenIndex = -2;
            playNextMessage(BattleState.ENEMY_AI);
        }
    }

    private void updateSkillSelect() {
        int moveCount = battle.getPlayerRot().getMoves().size();
        if (moveCount <= 0) {
            queueMessage(battle.getPlayerRot().getName(), "has no moves!");
            playNextMessage(BattleState.MENU);
            return;
        }
        if (skillCursor >= moveCount) skillCursor = 0;
        if (kh.upPressed && skillCursor >= 2) {
            skillCursor -= 2; utils.AudioManager.playSFX(utils.Constants.SFX_SELECT); inputCooldown = INPUT_DELAY;
        }
        else if (kh.downPressed && skillCursor < moveCount - 2) {
            skillCursor += 2; utils.AudioManager.playSFX(utils.Constants.SFX_SELECT); inputCooldown = INPUT_DELAY;
        }
        else if (kh.leftPressed && skillCursor % 2 != 0) {
            skillCursor--; utils.AudioManager.playSFX(utils.Constants.SFX_SELECT); inputCooldown = INPUT_DELAY;
        }
        else if (kh.rightPressed && skillCursor % 2 == 0 && skillCursor + 1 < moveCount) {
            skillCursor++; utils.AudioManager.playSFX(utils.Constants.SFX_SELECT); inputCooldown = INPUT_DELAY;
        }

        if (kh.enterPressed) {
            kh.enterPressed = false;
            utils.AudioManager.playSFX(utils.Constants.SFX_ENTER);
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
        if (kh.upPressed && partyCursor > 0) {
            partyCursor--; utils.AudioManager.playSFX(utils.Constants.SFX_SELECT); inputCooldown = INPUT_DELAY;
        }
        else if (kh.downPressed && partyCursor < size - 1) {
            partyCursor++; utils.AudioManager.playSFX(utils.Constants.SFX_SELECT); inputCooldown = INPUT_DELAY;
        }

        if (kh.escPressed && battle.getPlayerRot() != null && !battle.getPlayerRot().getName().isEmpty() && !isInitialSendOut) {
            kh.escPressed = false;
            setPrompt();
            currentState = BattleState.MENU;
            inputCooldown = INPUT_DELAY;
        }

        if (kh.enterPressed) {
            kh.enterPressed = false;
            utils.AudioManager.playSFX(utils.Constants.SFX_ENTER);
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
            utils.AudioManager.playSFX(utils.Constants.SFX_SELECT);
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
            utils.AudioManager.playSFX(utils.Constants.SFX_ENTER);
            if (confirmCursor == 0) {
                BrainRot selected = gp.player.getPCSYSTEM().getPartyMember(partyCursor);
                battle.setPlayerRot(selected);
                skillCursor = 0;
                queueMessage("Go! " + selected.getName() + "!", "");
                if (isInitialSendOut) {
                    isInitialSendOut = false;
                    playNextMessage(BattleState.MENU);
                } else {
                    playerChosenIndex = -1;
                    playerMovesFirst = true;
                    playNextMessage(BattleState.ENEMY_AI);
                }
            } else {
                dialogueLine1 = "Who will you";
                dialogueLine2 = "send out?";
                currentState = BattleState.TEAM_SELECT;
            }
            inputCooldown = INPUT_DELAY;
        }
    }

    private void updateLevelupCheck() {
        if (pendingReplacements.isEmpty()) {
            if (!battle.isOver()) {
                if (battle.getTrainer() != null) {
                    awaitingSwapPrompt = true;
                    revealTick = 0;
                    currentState = BattleState.ENEMY_REVEAL;
                } else {
                    setPrompt();
                    currentState = BattleState.MENU;
                }
                inputCooldown = INPUT_DELAY;
                return;
            }
            playNextMessage(BattleState.FINISH);
            return;
        }
        Object[] next = pendingReplacements.poll();
        replaceTargetRot      = (BrainRot) next[0];
        replaceCandidateSkill = (Skill)    next[1];
        replaceSlotCursor     = 0;
        confirmCursor         = 0;
        dialogueLine1 = replaceTargetRot.getName() + " already has 4 moves,";
        dialogueLine2 = "do you wish to replace a move?";
        dialogueTicks = 0;
        currentState  = BattleState.LEVELUP_REPLACE_CONFIRM;
        inputCooldown = INPUT_DELAY;
    }

    private void updateLevelupReplaceConfirm() {
        if (kh.upPressed || kh.downPressed) {
            confirmCursor = (confirmCursor == 0) ? 1 : 0;
            utils.AudioManager.playSFX(utils.Constants.SFX_SELECT);
            inputCooldown = INPUT_DELAY;
        }
        if (kh.enterPressed) {
            kh.enterPressed = false;
            utils.AudioManager.playSFX(utils.Constants.SFX_ENTER);
            if (confirmCursor == 0) {
                dialogueLine1 = "Choose a move to forget";
                dialogueLine2 = "for " + replaceCandidateSkill.getName() + ".";
                currentState  = BattleState.LEVELUP_REPLACE_SELECT;
            } else {
                queueMessage(replaceTargetRot.getName() + " did not learn", replaceCandidateSkill.getName() + ".");
                playNextMessage(BattleState.LEVELUP_CHECK);
            }
            inputCooldown = INPUT_DELAY;
        }
    }

    private void updateLevelupReplaceSelect() {
        int moveCount = replaceTargetRot.getMoves().size();
        if (kh.upPressed && replaceSlotCursor >= 2) {
            replaceSlotCursor -= 2; utils.AudioManager.playSFX(utils.Constants.SFX_SELECT); inputCooldown = INPUT_DELAY;
        }
        else if (kh.downPressed && replaceSlotCursor < moveCount - 2) {
            replaceSlotCursor += 2; utils.AudioManager.playSFX(utils.Constants.SFX_SELECT); inputCooldown = INPUT_DELAY;
        }
        else if (kh.leftPressed && replaceSlotCursor % 2 != 0) {
            replaceSlotCursor--; utils.AudioManager.playSFX(utils.Constants.SFX_SELECT); inputCooldown = INPUT_DELAY;
        }
        else if (kh.rightPressed && replaceSlotCursor % 2 == 0 && replaceSlotCursor + 1 < moveCount) {
            replaceSlotCursor++; utils.AudioManager.playSFX(utils.Constants.SFX_SELECT); inputCooldown = INPUT_DELAY;
        }

        if (kh.escPressed) {
            kh.escPressed = false;
            dialogueLine1 = replaceTargetRot.getName() + " already has 4 moves,";
            dialogueLine2 = "do you wish to replace a move?";
            currentState  = BattleState.LEVELUP_REPLACE_CONFIRM;
            inputCooldown = INPUT_DELAY;
            return;
        }
        if (kh.enterPressed) {
            kh.enterPressed = false;
            utils.AudioManager.playSFX(utils.Constants.SFX_ENTER);
            Skill forgotten = replaceTargetRot.getMoves().get(replaceSlotCursor);
            replaceTargetRot.replaceMove(replaceSlotCursor, replaceCandidateSkill);
            queueMessage(replaceTargetRot.getName() + " forgot",   forgotten.getName() + "!");
            queueMessage(replaceTargetRot.getName() + " learned",  replaceCandidateSkill.getName() + "!");
            playNextMessage(BattleState.LEVELUP_CHECK);
            inputCooldown = INPUT_DELAY;
        }
    }

    private void updateEnemyAI() {
        BrainRot er = battle.getEnemyRot();
        if (er != null && er.isOutOfUP()) {
            enemyChosenIndex = battle.STRUGGLE_INDEX;
        } else if (er != null && !er.getMoves().isEmpty()) {
            enemyChosenIndex = utils.RandomUtil.range(0, er.getMoves().size() - 1);
        } else {
            enemyChosenIndex = battle.STRUGGLE_INDEX;
        }
        currentState = playerMovesFirst ? BattleState.ANIMATION : BattleState.MENU;
    }

    private void updateAnimation() {
        if (!turnOneComplete) { turnOneComplete = true; executeTurnOne(); }
        else if (!turnTwoComplete) { turnTwoComplete = true; executeTurnTwo(); }
        else { currentState = BattleState.CLEANUP; }
    }

    private void executeTurnOne() {
        BrainRot attacker = playerMovesFirst ? battle.getPlayerRot() : battle.getEnemyRot();
        BrainRot defender = playerMovesFirst ? battle.getEnemyRot() : battle.getPlayerRot();
        int skillIdx      = playerMovesFirst ? playerChosenIndex : enemyChosenIndex;
        int attackerActor = playerMovesFirst ? 1 : 2;
        int defenderActor = playerMovesFirst ? 2 : 1;

        if (playerMovesFirst && (playerChosenIndex == -1 || playerChosenIndex == -2)) {
            playNextMessage(BattleState.ANIMATION); return;
        }
        if (!attacker.isFainted()) {
            if (skillIdx == battle.STRUGGLE_INDEX) {
                queueMessage(attacker.getName() + " has no UP left!", "", attackerActor);
                queueMessage(attacker.getName() + " used", "Struggle!", attackerActor);
                int defOldHp = defender.getCurrentHp();
                int atkOldHp = attacker.getCurrentHp();
                battle.executeStruggleTurn(playerMovesFirst);
                int damage = defOldHp - defender.getCurrentHp();
                int recoil = atkOldHp - attacker.getCurrentHp();
                if (damage > 0) queueMessage(defender.getName(), "took " + damage + " damage!", defenderActor);
                if (recoil > 0) queueMessage(attacker.getName(), "is hit by recoil! (" + recoil + ")", attackerActor);
                if (defender.isFainted()) queueMessage(defender.getName() + " fainted!", "");
                if (attacker.isFainted()) queueMessage(attacker.getName() + " fainted!", "");
            } else {
                if (skillIdx < 0 || skillIdx >= attacker.getMoves().size()) {
                    skillIdx = 0;
                }
                Skill skill = attacker.getMoves().get(skillIdx);
                queueMessage(attacker.getName() + " used", skill.getName() + "!", attackerActor);
                int oldHp = defender.getCurrentHp();
                if (playerMovesFirst) battle.executePlayerTurn(skillIdx);
                else                  battle.executeEnemyTurn(skillIdx);
                int damage = oldHp - defender.getCurrentHp();
                if (damage > 0)      queueMessage(defender.getName(), "took " + damage + " damage!",      defenderActor);
                else if (damage < 0) queueMessage(defender.getName(), "recovered " + (-damage) + " HP!");
                queueEffectMessage(skill, attacker, defender, attackerActor, defenderActor);
                if (defender.isFainted()) queueMessage(defender.getName() + " fainted!", "");
            }
        }
        playNextMessage(BattleState.ANIMATION);
    }

    private void executeTurnTwo() {
        BrainRot attacker = playerMovesFirst ? battle.getEnemyRot()  : battle.getPlayerRot();
        BrainRot defender = playerMovesFirst ? battle.getPlayerRot() : battle.getEnemyRot();
        int skillIdx      = playerMovesFirst ? enemyChosenIndex : playerChosenIndex;
        int attackerActor = playerMovesFirst ? 2 : 1;
        int defenderActor = playerMovesFirst ? 1 : 2;

        if (!battle.getEnemyRot().isFainted() && !battle.getPlayerRot().isFainted() && !attacker.isFainted()) {
            if (!playerMovesFirst && (playerChosenIndex == -1 || playerChosenIndex == -2)) {
                // no-op
            } else if (skillIdx == battle.STRUGGLE_INDEX) {
                queueMessage(attacker.getName() + " has no UP left!", "", attackerActor);
                queueMessage(attacker.getName() + " used", "Struggle!", attackerActor);
                int defOldHp = defender.getCurrentHp();
                int atkOldHp = attacker.getCurrentHp();
                battle.executeStruggleTurn(!playerMovesFirst);
                int damage = defOldHp - defender.getCurrentHp();
                int recoil = atkOldHp - attacker.getCurrentHp();
                if (damage > 0) queueMessage(defender.getName(), "took " + damage + " damage!", defenderActor);
                if (recoil > 0) queueMessage(attacker.getName(), "is hit by recoil! (" + recoil + ")", attackerActor);
                if (defender.isFainted()) queueMessage(defender.getName() + " fainted!", "");
                if (attacker.isFainted()) queueMessage(attacker.getName() + " fainted!", "");
            } else {
                if (skillIdx < 0 || skillIdx >= attacker.getMoves().size()) {
                    skillIdx = 0;
                }
                Skill skill = attacker.getMoves().get(skillIdx);
                queueMessage(attacker.getName() + " used", skill.getName() + "!", attackerActor);
                int oldHp = defender.getCurrentHp();
                if (playerMovesFirst) battle.executeEnemyTurn(skillIdx);
                else                  battle.executePlayerTurn(skillIdx);
                int damage = oldHp - defender.getCurrentHp();
                if (damage > 0)      queueMessage(defender.getName(), "took " + damage + " damage!",      defenderActor);
                else if (damage < 0) queueMessage(defender.getName(), "recovered " + (-damage) + " HP!");
                queueEffectMessage(skill, attacker, defender, attackerActor, defenderActor);
                if (defender.isFainted()) queueMessage(defender.getName() + " fainted!", "");
            }
        }
        playNextMessage(BattleState.ANIMATION);
    }

    private void updateCleanup() {
        battle.endTurn();
        turnOneComplete  = false;
        turnTwoComplete  = false;
        playerMovesFirst = battle.getPlayerRot().getSpeed() >= battle.getEnemyRot().getSpeed();

        if (battle.isOver()) {
            if (battle.getResult() == BattleManager.BattleResult.PLAYER_WIN) {
                utils.AudioManager.playMusic(utils.Constants.BGM_VICTORY, false);

                BattleReward.Result reward = battle.getReward();
                queueMessage(battle.getEnemyRot().getName() + " fainted!", "");
                queueMessage(battle.getPlayerRot().getName() + " gained", reward.xp + " XP!", 1);
                if (!reward.suppressDrops) {
                    queueMessage("Coins earned:", "+" + reward.coins + " RotCoins!");
                    if (reward.hasScroll())
                        queueMessage("Loot drop!", reward.scrollSkillName + " Scroll" + (reward.scrollAdded ? " found!" : " [Bag full]"));
                }

                BrainRot playerRot = battle.getPlayerRot();
                for (LevelUpResult lu : reward.levelUps) {
                    queueMessage(playerRot.getName() + " grew to", "level " + lu.newLevel + "!", 1);
                    if (lu.skillUnlocked != null) {
                        if (playerRot.getMoves().size() < 4) {
                            playerRot.addMove(lu.skillUnlocked);
                            queueMessage(playerRot.getName() + " learned", lu.skillUnlocked.getName() + "!");
                        } else {
                            pendingReplacements.add(new Object[]{ playerRot, lu.skillUnlocked });
                            queueMessage(playerRot.getName() + " wants to learn", lu.skillUnlocked.getName() + "!", 1);
                        }
                    }
                }

                boolean trainerHasMore = battle.trainerHasNextEnemy();
                if (trainerHasMore) {
                    BrainRot next = battle.advanceToNextTrainerEnemy();
                    if (next != null) {
                        utils.AudioManager.playMusic(utils.Constants.BGM_WILD_BATTLE, true);
                        playerMovesFirst = battle.getPlayerRot().getSpeed() >= next.getSpeed();
                        awaitingSwapPrompt = true;
                        revealTick = 0;
                        if (battle.getTrainer() != null)
                            queueMessage(battle.getTrainer().name + " sent out", next.getName() + "!", 2);
                    }
                } else if (battle.getTrainer() != null) {
                    npc.TrainerNPC tr = battle.getTrainer();
                    tr.markDefeated(gp.getGameTime());
                    String key = gp.CURRENT_PATH + "@"
                            + (tr.worldX / TILE_SIZE) + ","
                            + (tr.worldY / TILE_SIZE);
                    gp.defeatedTrainers.put(key, gp.getGameTime());

                    // Gym leaders / masters award a badge and are one-time fights.
                    String badgeId = null;
                    if (tr instanceof npc.GymLeader gl) badgeId = gl.getBadgeId();
                    else if (tr instanceof npc.GymMaster gm) badgeId = gm.getBadgeId();
                    if (badgeId != null) {
                        progression.BadgeSystem.getInstance().onLeaderDefeated(badgeId);
                    }

                    int trainerCoins = tr.getRotCoins();
                    if (trainerCoins > 0) {
                        gp.player.earnRotCoins(trainerCoins);
                        queueMessage("Coins earned:", "+" + trainerCoins + " RotCoins!");
                        tr.clearRotCoins();
                    }
                    if (tr.getInventory() != null) {
                        java.util.List<items.Item> drops = new java.util.ArrayList<>(tr.getInventory().getRawItems());
                        for (items.Item it : drops) {
                            if (it == null) continue;
                            boolean added = gp.player.getInventory().addItem(it);
                            queueMessage("Loot drop!", it.getName() + (added ? " obtained!" : " [Bag full]"));
                            if (added) tr.getInventory().removeItem(it);
                        }
                    }
                }

                BattleState afterMessages;
                if (!pendingReplacements.isEmpty()) afterMessages = BattleState.LEVELUP_CHECK;
                else if (trainerHasMore)            afterMessages = BattleState.ENEMY_REVEAL;
                else                                afterMessages = BattleState.FINISH;
                playNextMessage(afterMessages);
            } else {
                queueMessage("Battle Finished!", "Result: " + battle.getResult().name());
                playNextMessage(pendingReplacements.isEmpty() ? BattleState.FINISH : BattleState.LEVELUP_CHECK);
            }
        } else {
            if (battle.getPlayerRot() != null && battle.getPlayerRot().isFainted()
                    && battle.hasHealthyReserves()) {
                queueMessage(battle.getPlayerRot().getName() + " fainted!", "");
                queueMessage("Choose your next", "BrainRot!", 1);
                isInitialSendOut = true;
                playerChosenIndex = -1;
                playNextMessage(BattleState.TEAM_SELECT);
                return;
            }
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

            gp.updateMusic();
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

    private void queueEffectMessage(Skill skill, BrainRot user, BrainRot target, int userActor, int targetActor) {
        if (skill == null || skill.getEffect() == null) return;
        String e = skill.getEffect().toUpperCase();
        String userName = user.getName();
        String tgtName  = target.getName();
        switch (e) {
            case "RAISE_ATK" -> queueMessage(userName + " raised", "its Attack stat!", userActor);
            case "RAISE_DEF" -> queueMessage(userName + " raised", "its Defense stat!", userActor);
            case "RAISE_SPD" -> queueMessage(userName + " raised", "its Speed stat!", userActor);
            case "LOWER_ATK" -> queueMessage(tgtName + "'s",       "Attack fell!",     targetActor);
            case "LOWER_DEF" -> queueMessage(tgtName + "'s",       "Defense fell!",    targetActor);
            case "LOWER_SPD" -> queueMessage(tgtName + "'s",       "Speed fell!",      targetActor);
            case "HEAL"      -> queueMessage(userName,             "restored some HP!", userActor);
            case "BURN"      -> queueMessage(tgtName,              "was burned!",       targetActor);
            case "PARALYZE"  -> queueMessage(tgtName,              "was paralyzed!",    targetActor);
            case "CONFUSE"   -> queueMessage(tgtName,              "became confused!",  targetActor);
            case "SLEEP"     -> queueMessage(tgtName,              "fell asleep!",      targetActor);
            default -> {}
        }
    }

    private void queueWildIntroDialogue(BrainRot wildRot) {
        String l1 = "A wild " + wildRot.getName() + " appeared!";
        String l2 = switch (wildRot.getName().toUpperCase()) {
            case "TUNG TUNG TUNG SAHUR"  -> "started drumming wildly!";
            case "TRALALERO TRALALA"     -> "strolled in with fresh kicks!";
            case "BOMBARDINO CROCODILO"  -> "crash-landed into battle!";
            case "LIRILI LARILA"         -> "slowly stepped out of time...";
            case "BRR BRR PATAPIM"       -> "goes brr brr... then patapim!";
            case "BONECA AMBALABU"       -> "rolled up screeching!";
            case "UDIN DIN DIN DIN DUN"  -> "is vibrating aggressively!";
            case "CAPUCCINO ASSASSINO"   -> "emerged from the espresso steam!";
            default                      -> "is ready to fight!";
        };
        queueMessage(l1, l2);
    }

    public void draw(Graphics2D g2) {
        if (this.battle == null) return;
        if (this.battle.getEnemyRot()  == null) return;
        if (this.battle.getPlayerRot() == null) return;

        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // --- NEW BATTLE BACKGROUND LOGIC WITH WATER ---
        if(currentBgTile == 999 && trainerBg != null){
            g2.drawImage(trainerBg, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT, null);
        }else if (currentBgTile == 556 && waterBg != null) {
            g2.drawImage(waterBg, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT, null);
        } else if (currentBgTile == 16 && darkGrassBg != null) {
            g2.drawImage(darkGrassBg, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT, null);
        } else if (currentBgTile == 84 && caveBg != null) {
            g2.drawImage(caveBg, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT, null);
        } else if (currentBgTile == 2 && grassBg != null) {
            g2.drawImage(grassBg, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT, null);
        } else {
            g2.setColor(new Color(100, 180, 100));
            g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
            g2.setColor(new Color(150, 210, 150));
            g2.fillOval(480, 200, 240, 60);
            g2.fillOval(40, 440, 300, 70);
        }

        if (currentState == BattleState.TEAM_SELECT || currentState == BattleState.TEAM_CONFIRM) {
            drawFullTeamScreen(g2);
            return;
        }

        AnimMode pMode = AnimMode.IDLE, eMode = AnimMode.IDLE;
        if (currentState == BattleState.MESSAGE) {
            String l1 = (dialogueLine1 != null) ? dialogueLine1.toLowerCase() : "";
            String l2 = (dialogueLine2 != null) ? dialogueLine2.toLowerCase() : "";
            boolean usedSkill = l1.contains(" used");
            boolean tookHit   = l2.contains("took") || l2.contains("damage");
            boolean buffMsg   = l1.contains("raised") || l2.contains("raised")
                    || l1.contains("fell")   || l2.contains("fell")
                    || l1.contains("recover")|| l2.contains("recover")
                    || l2.contains("burned")  || l2.contains("paralyzed")
                    || l2.contains("confused")|| l2.contains("asleep");

            if (usedSkill) {
                if (currentMessageActor == 1) pMode = AnimMode.ATTACK;
                else if (currentMessageActor == 2) eMode = AnimMode.ATTACK;
            }
            if (tookHit) {
                if (currentMessageActor == 1) pMode = AnimMode.HURT;
                else if (currentMessageActor == 2) eMode = AnimMode.HURT;
            }
            if (buffMsg) {
                if (currentMessageActor == 1) pMode = AnimMode.BUFF;
                else if (currentMessageActor == 2) eMode = AnimMode.BUFF;
            }
        }
        int pFrame = pickAnimFrame(pMode);
        int eFrame = pickAnimFrame(eMode);

        boolean inTrainerPose  = (currentState == BattleState.TRAINER_POSE);
        boolean inEnemyReveal  = (currentState == BattleState.ENEMY_REVEAL);
        boolean inSwapPrompt   = (currentState == BattleState.SWAP_PROMPT);
        boolean hidePlayerRot  = inTrainerPose || inEnemyReveal || inSwapPrompt
                || currentState == BattleState.INITIALIZING || isInitialSendOut;
        boolean hideEnemyRot   = inTrainerPose;

        if (!hideEnemyRot) {
            BufferedImage enemySprite = AssetManager.getBrainRotSprite(battle.getEnemyRot().getName(), battle.getEnemyRot().getTier().name(), false, eFrame);
            if (enemySprite != null) g2.drawImage(enemySprite, 500, 60, 200, 200, null);
        }

        if (hidePlayerRot) {
            if (!inTrainerPose && playerBackSprite != null)
                g2.drawImage(playerBackSprite, 80, SCREEN_HEIGHT - 340, 220, 220, null);
        } else {
            BufferedImage playerSprite = AssetManager.getBrainRotSprite(battle.getPlayerRot().getName(), battle.getPlayerRot().getTier().name(), true, pFrame);
            if (playerSprite != null) g2.drawImage(playerSprite, 60, SCREEN_HEIGHT - 360, 260, 260, null);
        }

        if (!hideEnemyRot) drawEnemyHpBlock(g2);
        if (!hidePlayerRot) drawPlayerHpBlock(g2);

        if (inTrainerPose && trainerPoseSprite != null) {
            int targetX = SCREEN_WIDTH / 2 - 160;
            int finalY  = 60;
            int x;
            float alpha = 1f;
            if (poseTick < POSE_SLIDE_IN_END) {
                float t = (float) poseTick / POSE_SLIDE_IN_END;
                x = (int)(SCREEN_WIDTH - (SCREEN_WIDTH - targetX) * t);
            } else if (poseTick < POSE_HOLD_END) {
                x = targetX;
            } else {
                float t = (float)(poseTick - POSE_HOLD_END) / (POSE_SLIDE_OUT_END - POSE_HOLD_END);
                x = (int)(targetX - (targetX + 320) * t);
            }
            Composite prev = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.drawImage(trainerPoseSprite, x, finalY, 320, 380, null);
            g2.setComposite(prev);
        }

        int boxY = SCREEN_HEIGHT - 136;
        int boxH = 126;
        if (dialogueBoxFrame != null) g2.drawImage(dialogueBoxFrame, 6, boxY, SCREEN_WIDTH - 12, boxH, null);
        else drawBattleBox(g2, 10, boxY, SCREEN_WIDTH - 20, boxH);

        drawDialogueText(g2, boxY - 4, boxH);

        if (currentState == BattleState.MENU)                      drawMenu(g2, boxY);
        else if (currentState == BattleState.SKILL_SELECT)         drawSkillSelect(g2, boxY);
        else if (currentState == BattleState.LEVELUP_REPLACE_CONFIRM) drawLevelupConfirm(g2, boxY);
        else if (currentState == BattleState.LEVELUP_REPLACE_SELECT)  drawLevelupReplaceSelect(g2, boxY);
        else if (currentState == BattleState.SWAP_PROMPT)          drawSwapPrompt(g2, boxY);
    }

    private void drawSwapPrompt(Graphics2D g2, int boxY) {
        int menuW = 160, menuX = SCREEN_WIDTH - menuW - 10;
        drawBattleBox(g2, menuX, boxY, menuW, 126);

        g2.setFont(getCustomFont(Font.BOLD, 16f));
        g2.setColor(new Color(44, 44, 42));
        g2.drawString("YES", menuX + 60, boxY + 50);
        g2.drawString("NO",  menuX + 60, boxY + 90);
        drawCursor(g2, menuX + 35, boxY + (swapCursor == 0 ? 36 : 76));
    }

    private void drawFullTeamScreen(Graphics2D g2) {
        Font base = getCustomFont(Font.PLAIN, 10);

        int pad    = 15;
        int gap    = 15;
        int panelW = (SCREEN_WIDTH - pad * 2 - gap * 2) / 3;
        int panelH = SCREEN_HEIGHT - 165;

        drawBattlePanel(g2, pad,                        pad, panelW, panelH, "TEAM");
        drawBattlePanel(g2, pad + panelW + gap,         pad, panelW, panelH, "INFO");
        drawBattlePanel(g2, pad + (panelW+gap)*2,       pad, panelW, panelH, "SKILLS");

        BrainRot sel = gp.player.getPCSYSTEM().getPartyMember(partyCursor);
        int size     = gp.player.getPCSYSTEM().getPartySize();

        int listX  = pad + 8;
        int listW  = panelW - 16;
        int rowH   = 52;
        int listY  = pad + 38;

        for (int i = 0; i < size; i++) {
            BrainRot rot     = gp.player.getPCSYSTEM().getPartyMember(i);
            boolean  hovered = (i == partyCursor);
            boolean  fainted = rot.isFainted();
            int      rowTop  = listY + i * rowH;

            Color bg = hovered ? new Color(178, 212, 244, 220) : new Color(230, 226, 218);
            g2.setColor(fainted ? new Color(210, 205, 198) : bg);
            g2.fillRoundRect(listX, rowTop, listW, rowH - 4, 8, 8);

            if (hovered) {
                g2.setColor(new Color(24, 95, 165));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(listX, rowTop, listW, rowH - 4, 8, 8);
                g2.setStroke(new BasicStroke(1));
                int ts = 6, cx = listX + 4, cy = rowTop + (rowH - 4) / 2;
                g2.setColor(new Color(80, 76, 70));
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.fillPolygon(new int[]{cx, cx, cx+ts}, new int[]{cy-ts, cy+ts, cy}, 3);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
            }

            int tx      = listX + 12;
            int nameMaxW = listW - 12 - 4;

            g2.setFont(base.deriveFont(Font.BOLD, 10f));
            g2.setColor(fainted ? new Color(160, 155, 148) : new Color(44, 44, 42));
            String nameStr = rot.getName();
            FontMetrics nmFm = g2.getFontMetrics();
            while (nmFm.stringWidth(nameStr) > nameMaxW && nameStr.length() > 1)
                nameStr = nameStr.substring(0, nameStr.length() - 1);
            g2.drawString(nameStr, tx, rowTop + 16);

            int hpBarY = rowTop + 24;
            int hpBarH = 5;
            int hpBarW = listW - 12 - 10;
            double hpFr = Math.max(0, Math.min(1, (double) rot.getCurrentHp() / rot.getMaxHp()));
            g2.setFont(base.deriveFont(7f));
            g2.setColor(new Color(100, 96, 90));
            g2.drawString("HP", tx, hpBarY + hpBarH);
            int hpLW = g2.getFontMetrics().stringWidth("HP") + 3;
            g2.setColor(new Color(200, 196, 186));
            g2.fillRoundRect(tx + hpLW, hpBarY, hpBarW - hpLW, hpBarH, 2, 2);
            int fillPx = (int)((hpBarW - hpLW) * hpFr);
            if (fillPx > 0) { g2.setColor(hpColorBattle(rot)); g2.fillRoundRect(tx + hpLW, hpBarY, fillPx, hpBarH, 2, 2); }
            g2.setColor(new Color(160, 155, 145));
            g2.drawRoundRect(tx + hpLW, hpBarY, hpBarW - hpLW, hpBarH, 2, 2);

            g2.setFont(base.deriveFont(7f));
            g2.setColor(new Color(64, 60, 55));
            g2.drawString(rot.getCurrentHp() + "/" + rot.getMaxHp(), tx, rowTop + rowH - 10);

        }

        if (sel != null) {
            int midX  = pad + panelW + gap + 12;
            int midW  = panelW - 24;
            int infoY = pad + 38;

            BufferedImage spr = AssetManager.getBrainRotSprite(sel.getName(), sel.getTier().name(), false, 1);
            if (spr != null) g2.drawImage(spr, midX + (midW/2 - 45), infoY, 110, 110, null);

            int ty = infoY + 128;

            g2.setFont(base.deriveFont(Font.BOLD, 14f));
            g2.setColor(new Color(44, 44, 42));
            StringBuilder nLine = new StringBuilder();
            for (String word : sel.getName().split(" ")) {
                String test = nLine.isEmpty() ? word : nLine + " " + word;
                if (g2.getFontMetrics().stringWidth(test) <= midW) { nLine = new StringBuilder(test); }
                else { g2.drawString(nLine.toString(), midX, ty); ty += 18; nLine = new StringBuilder(word); }
            }
            if (!nLine.isEmpty()) { g2.drawString(nLine.toString(), midX, ty); ty += 16; }

            g2.setFont(base.deriveFont(10f));
            g2.setColor(new Color(100, 96, 90));
            String lvStr = "Lv." + sel.getLevel() + " ";
            g2.drawString(lvStr, midX, ty);
            int lvW = g2.getFontMetrics().stringWidth(lvStr);

            int badgeH = 14;
            int pW = drawTypeBadgeBattle(g2, base, sel.getPrimaryType().name(), midX + lvW, ty - 10, badgeH);
            if (sel.getSecondaryType() != null)
                pW += drawTypeBadgeBattle(g2, base, sel.getSecondaryType().name(), midX + lvW + pW + 4, ty - 10, badgeH) + 4;

            ty += 10;

            g2.setFont(base.deriveFont(10f));
            g2.setColor(new Color(80, 76, 70));
            g2.drawString("HP", midX, ty + 7);
            int hpLW2 = g2.getFontMetrics().stringWidth("HP") + 4;
            double hpFr2 = Math.max(0, Math.min(1, (double) sel.getCurrentHp() / sel.getMaxHp()));
            int hpBW = midW - hpLW2 - 62;
            g2.setColor(new Color(200, 196, 186));
            g2.fillRoundRect(midX + hpLW2, ty, hpBW, 8, 3, 3);
            int hpFill = (int)(hpBW * hpFr2);
            if (hpFill > 0) { g2.setColor(hpColorBattle(sel)); g2.fillRoundRect(midX + hpLW2, ty, hpFill, 8, 3, 3); }
            g2.setColor(new Color(160, 155, 145));
            g2.drawRoundRect(midX + hpLW2, ty, hpBW, 8, 3, 3);
            g2.setFont(base.deriveFont(9f));
            g2.setColor(new Color(44, 44, 42));
            g2.drawString(sel.getCurrentHp() + "/" + sel.getMaxHp(), midX + hpLW2 + hpBW + 4, ty + 8);
            ty += 26;

            g2.setFont(base.deriveFont(10f));
            g2.setColor(new Color(80, 76, 70));
            g2.drawString("ATK" + sel.getBaseAtk(), midX,       ty);
            g2.drawString("DEF" + sel.getBaseDef(), midX + 62,  ty);
            g2.drawString("SPD" + sel.getBaseSpeed(), midX + 120, ty);
            ty += 10;

            g2.setColor(new Color(200, 195, 180));
            g2.drawLine(midX, ty, midX + midW, ty);
            ty += 16;

            g2.setFont(base.deriveFont(9f));
            g2.setColor(new Color(88, 84, 76));
            String desc = Constants.getDescription(sel.getName());
            if (desc != null) {
                StringBuilder dLine = new StringBuilder();
                int maxDescY = pad + panelH - 8;
                for (String word : desc.split(" ")) {
                    String test = dLine.isEmpty() ? word : dLine + " " + word;
                    if (g2.getFontMetrics().stringWidth(test) <= midW) { dLine = new StringBuilder(test); }
                    else {
                        if (ty < maxDescY) { g2.drawString(dLine.toString(), midX, ty); ty += 13; }
                        dLine = new StringBuilder(word);
                    }
                }
                if (dLine.length() > 0 && ty < maxDescY)
                    g2.drawString(dLine.toString(), midX, ty);
            }
        }

        if (sel != null) {
            int rightX = pad + (panelW + gap) * 2 + 10;
            int rightW = panelW - 20;
            int skillsY = pad + 38;
            int rowH2   = 44;
            List<Skill> moves = sel.getMoves();

            for (int i = 0; i < 4; i++) {
                int rowY   = skillsY + i * (rowH2 + 4);
                boolean mv = i < moves.size();

                g2.setColor(new Color(235, 232, 224));
                g2.fillRoundRect(rightX, rowY, rightW, rowH2, 5, 5);

                if (i < 3) {
                    g2.setColor(new Color(200, 196, 186));
                    g2.drawLine(rightX + 4, rowY + rowH2 + 3, rightX + rightW - 4, rowY + rowH2 + 3);
                }

                int baseline = rowY + rowH2 / 2 + 5;

                if (mv) {
                    Skill sk = moves.get(i);

                    int bH2   = 14;
                    int bTopY = rowY + (rowH2 - bH2) / 2;
                    int bX2   = rightX + 5;
                    int bW2   = drawTypeBadgeBattle(g2, base, sk.getType().name(), bX2, bTopY, bH2);

                    g2.setFont(base.deriveFont(Font.BOLD, 9f));
                    g2.setColor(new Color(44, 44, 42));
                    String upStr = sk.getCurrentUP() + "/" + sk.getMaxUP();
                    int upW = g2.getFontMetrics(base.deriveFont(8f)).stringWidth(upStr);
                    int nameX = bX2 + bW2 + 5;
                    int nameMaxW2 = rightW - (nameX - rightX) - upW - 10;
                    String mName = sk.getName();
                    FontMetrics mnFm = g2.getFontMetrics();
                    while (mnFm.stringWidth(mName) > nameMaxW2 && mName.length() > 1)
                        mName = mName.substring(0, mName.length() - 1);
                    g2.drawString(mName, nameX, baseline);

                    g2.setFont(base.deriveFont(8f));
                    g2.setColor(sk.getCurrentUP() < 1 ? new Color(200, 80, 60) : new Color(100, 96, 90));
                    FontMetrics upFm = g2.getFontMetrics();
                    g2.drawString(upStr, rightX + rightW - upFm.stringWidth(upStr) - 5, baseline);
                } else {
                    g2.setFont(base.deriveFont(10f));
                    g2.setColor(new Color(170, 165, 158));
                    g2.drawString("-", rightX + 10, baseline);
                }
            }
        }

        int boxY = SCREEN_HEIGHT - 136;
        int boxH = 126;
        drawBattleBox(g2, 10, boxY, SCREEN_WIDTH - 20, boxH);
        drawDialogueText(g2, boxY - 6, boxH);

        if (currentState == BattleState.TEAM_CONFIRM) {
            int menuW = 160, menuH = 126;
            int menuX = SCREEN_WIDTH - menuW - 10;
            drawBattleBox(g2, menuX, boxY, menuW, menuH);

            Font btnFont = getCustomFont(Font.BOLD, 14f);
            g2.setFont(btnFont);
            g2.setColor(new Color(44, 44, 42));
            g2.drawString("YES", menuX + 55, boxY + 48);
            g2.drawString("NO",  menuX + 55, boxY + 86);

            int ts = 8, cx = menuX + 32, cy = boxY + (confirmCursor == 0 ? 40 : 78);
            g2.setColor(new Color(44, 44, 42));
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.fillPolygon(new int[]{cx, cx, cx+ts}, new int[]{cy-ts, cy+ts, cy}, 3);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
        }
    }

    private void drawEnemyHpBlock(Graphics2D g2) {
        drawHpFrame(g2, 40, 40, 320, 96, battle.getEnemyRot(), hpFrame_enemy);
    }

    private void drawPlayerHpBlock(Graphics2D g2) {
        drawHpFrame(g2, SCREEN_WIDTH - 360, SCREEN_HEIGHT - 250, 340, 96, battle.getPlayerRot(), hpFrame_player);
    }

    private void drawHpFrame(Graphics2D g2, int x, int y, int w, int h,
                             BrainRot rot, BufferedImage frameImg) {
        if (frameImg != null) g2.drawImage(frameImg, x, y, w, h, null);
        else drawBattleBox(g2, x, y, w, h);

        y += 2;

        g2.setColor(new Color(44, 44, 42));
        drawFittingString(g2, rot.getName(), x + 20, y + 30, w - 90, 13f, Font.BOLD);

        g2.setFont(getCustomFont(Font.BOLD, 12f));
        String lvStr = "Lv" + rot.getLevel();
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(lvStr, x + w - fm.stringWidth(lvStr) - 20, y + 30);

        int barX = x + 20;
        int barY = y + 42;
        int barW = w - 40;
        int barH = 10;
        g2.setColor(new Color(80, 80, 80));
        g2.fillRect(barX, barY, barW, barH);
        double hpFrac = Math.max(0, (double) rot.getCurrentHp() / rot.getMaxHp());
        g2.setColor(hpColorBattle(rot));
        g2.fillRect(barX, barY, (int)(barW * hpFrac), barH);

        g2.setFont(getCustomFont(Font.BOLD, 12f));
        g2.setColor(new Color(44, 44, 42));

        String hpNum = rot.getCurrentHp() + "/" + rot.getMaxHp();
        FontMetrics fm2 = g2.getFontMetrics();

        g2.drawString(hpNum, x + w - fm2.stringWidth(hpNum) - 20, barY + 30);

        Font badgeFont = getCustomFont(Font.PLAIN, 10);
        int badgeY = barY + 16;
        int bx = x + 20;
        int bxAfter = bx;
        if (rot.getPrimaryType() != null) {
            int bw1 = drawTypeBadgeBattle(g2, badgeFont, rot.getPrimaryType().name(), bx, badgeY, 14);
            bxAfter = bx + bw1 + 4;
            if (rot.getSecondaryType() != null) {
                int bw2 = drawTypeBadgeBattle(g2, badgeFont, rot.getSecondaryType().name(), bxAfter, badgeY, 14);
                bxAfter += bw2 + 4;
            }
        }

        if (rot.getStatus() != null && !rot.getStatus().equalsIgnoreCase("NONE")) {
            drawStatusBadge(g2, badgeFont, rot.getStatus(), bxAfter, badgeY, 14);
        }
    }

    private int drawStatusBadge(Graphics2D g2, Font base, String status, int x, int y, int h) {
        g2.setFont(base.deriveFont(8f));
        FontMetrics fm = g2.getFontMetrics();
        String label = status.toUpperCase();
        int w = fm.stringWidth(label) + 14;
        g2.setColor(statusColor(label));
        g2.fillRoundRect(x, y, w, h, 4, 4);
        g2.setColor(Color.WHITE);
        g2.drawString(label, x + 7, y + h - 4);
        return w;
    }

    private Color statusColor(String s) {
        return switch (s) {
            case "BURN"     -> new Color(220,  90,  40);
            case "PARALYZE" -> new Color(220, 180,  40);
            case "CONFUSE"  -> new Color(180,  90, 200);
            case "SLEEP"    -> new Color( 80, 100, 180);
            case "FLINCH"   -> new Color(120, 120, 120);
            default         -> new Color(130, 126, 118);
        };
    }

    private void drawMenu(Graphics2D g2, int boxY) {
        int menuW = 320, menuX = SCREEN_WIDTH - menuW - 10;
        drawBattleBox(g2, menuX, boxY, menuW, 126);

        g2.setFont(getCustomFont(Font.BOLD, 18f));
        g2.setColor(new Color(44, 44, 42));
        g2.drawString("FIGHT", menuX + 50,  boxY + 50);
        g2.drawString("BAG",   menuX + 200, boxY + 50);
        g2.drawString("TEAM",  menuX + 50,  boxY + 90);
        g2.drawString("RUN",   menuX + 200, boxY + 90);

        int cx = menuX + 25, cy = boxY + 36;
        if (menuCursor == MenuOption.BAG  || menuCursor == MenuOption.RUN)  cx += 150;
        if (menuCursor == MenuOption.TEAM || menuCursor == MenuOption.RUN)  cy += 40;
        drawCursor(g2, cx, cy);
    }

    private void drawSkillSelect(Graphics2D g2, int boxY) {
        int menuW = 480, menuX = SCREEN_WIDTH - menuW - 10;
        drawBattleBox(g2, menuX, boxY, menuW, 126);

        Font badgeFont = getCustomFont(Font.PLAIN, 10);
        List<Skill> moves = battle.getPlayerRot().getMoves();
        for (int i = 0; i < moves.size(); i++) {
            int dx = menuX + 40 + (i % 2 == 1 ? 230 : 0);
            int dy = boxY  + 50 + (i >= 2 ? 40 : 0);
            Skill sk = moves.get(i);
            g2.setColor(sk.getCurrentUP() < 1 ? new Color(180, 180, 180) : new Color(44, 44, 42));
            drawFittingString(g2, sk.getName(), dx, dy, 200, 16f, Font.BOLD);

            if (sk.getType() != null)
                drawTypeBadgeBattle(g2, badgeFont, sk.getType().name(), dx, dy + 4, 14);

            String upStr = "UP " + sk.getCurrentUP() + "/" + sk.getMaxUP();
            g2.setFont(getCustomFont(Font.BOLD, 11f));
            g2.setColor(sk.getCurrentUP() < 1 ? new Color(200, 80, 60) : new Color(80, 76, 70));
            FontMetrics upFm = g2.getFontMetrics();
            g2.drawString(upStr, dx + 200 - upFm.stringWidth(upStr), dy + 16);

            if (i == skillCursor) drawCursor(g2, dx - 25, dy - 14);
        }
    }

    private void drawLevelupConfirm(Graphics2D g2, int boxY) {
        int menuW = 160, menuX = SCREEN_WIDTH - menuW - 10;
        drawBattleBox(g2, menuX, boxY, menuW, 126);

        g2.setFont(getCustomFont(Font.BOLD, 16f));
        g2.setColor(new Color(44, 44, 42));
        g2.drawString("YES", menuX + 60, boxY + 50);
        g2.drawString("NO",  menuX + 60, boxY + 90);
        drawCursor(g2, menuX + 35, boxY + (confirmCursor == 0 ? 36 : 76));
    }

    private void drawLevelupReplaceSelect(Graphics2D g2, int boxY) {
        int menuW = 480, menuX = SCREEN_WIDTH - menuW - 10;
        drawBattleBox(g2, menuX, boxY, menuW, 126);

        g2.setFont(getCustomFont(Font.BOLD, 16f));
        g2.setColor(new Color(44, 44, 42));
        List<Skill> moves = replaceTargetRot.getMoves();
        for (int i = 0; i < moves.size(); i++) {
            int dx = menuX + 40 + (i % 2 == 1 ? 230 : 0);
            int dy = boxY  + 50 + (i >= 2 ? 40 : 0);
            g2.drawString(moves.get(i).getName(), dx, dy);
            if (i == replaceSlotCursor) drawCursor(g2, dx - 25, dy - 14);
        }
    }

    private void drawDialogueText(Graphics2D g2, int boxY, int boxH) {
        if ((dialogueLine1 == null || dialogueLine1.isEmpty())
                && (dialogueLine2 == null || dialogueLine2.isEmpty())) return;

        boolean line2Present = dialogueLine2 != null && !dialogueLine2.isEmpty();
        int lineH  = 34;
        int totalH = line2Present ? lineH * 2 : lineH;
        int startY = boxY + (boxH - totalH) / 2 + lineH - 4;

        g2.setColor(new Color(44, 44, 42));
        int maxW = SCREEN_WIDTH - 70;

        if (dialogueLine1 != null && !dialogueLine1.isEmpty())
            drawFittingString(g2, dialogueLine1, 30, startY, maxW, 15f, Font.BOLD);
        if (line2Present)
            drawFittingString(g2, dialogueLine2, 30, startY + lineH, maxW, 15f, Font.BOLD);
    }

    private void drawBattleBox(Graphics2D g2, int x, int y, int w, int h) {
        int arc = 12;
        g2.setColor(new Color(245, 242, 235));
        g2.fillRoundRect(x, y, w, h, arc, arc);
        g2.setStroke(new BasicStroke(3));
        g2.setColor(new Color(80, 80, 80));
        g2.drawRoundRect(x, y, w, h, arc, arc);
        g2.setStroke(new BasicStroke(2));
        g2.setColor(new Color(216, 184, 88));
        g2.drawRoundRect(x + 1, y + 1, w - 2, h - 2, arc, arc);
        g2.setStroke(new BasicStroke(1));
        g2.setColor(new Color(80, 80, 80));
        g2.drawRoundRect(x + 3, y + 3, w - 6, h - 6, arc - 2, arc - 2);
    }

    private void drawBattlePanel(Graphics2D g2, int x, int y, int w, int h, String title) {
        drawBattleBox(g2, x, y, w, h);
        if (!title.isEmpty()) {
            g2.setColor(new Color(44, 44, 42));
            g2.fillRoundRect(x + 4, y + 4, w - 8, 26, 8, 8);
            g2.setFont(getCustomFont(Font.BOLD, 11f));
            g2.setColor(new Color(241, 239, 232));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(title, x + (w - fm.stringWidth(title)) / 2, y + 22);
        }
    }

    private Font getCustomFont(int style, float size) {
        return AssetManager.pokemonGb != null
                ? AssetManager.pokemonGb.deriveFont(style, size)
                : new Font("Arial", style, (int) size);
    }

    private void drawFittingString(Graphics2D g2, String text, int x, int y,
                                   int maxWidth, float startSize, int fontStyle) {
        float sz = startSize;
        g2.setFont(getCustomFont(fontStyle, sz));
        FontMetrics fm = g2.getFontMetrics();
        while (fm.stringWidth(text) > maxWidth && sz > 8f) {
            sz -= 1f;
            g2.setFont(getCustomFont(fontStyle, sz));
            fm = g2.getFontMetrics();
        }
        g2.drawString(text, x, y);
    }

    private void drawCursor(Graphics2D g2, int x, int y) {
        g2.setColor(new Color(44, 44, 42));
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.fillPolygon(new int[]{x, x, x+10}, new int[]{y, y+14, y+7}, 3);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
    }

    private int drawTypeBadgeBattle(Graphics2D g2, Font base, String typeName, int x, int y, int h) {
        g2.setFont(base.deriveFont(8f));
        FontMetrics fm = g2.getFontMetrics();
        int w = fm.stringWidth(typeName) + 14;
        g2.setColor(typeColor(typeName));
        g2.fillRoundRect(x, y, w, h, 4, 4);
        g2.setColor(Color.WHITE);
        g2.drawString(typeName, x + 7, y + h - 4);
        return w;
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

    private Color hpColorBattle(BrainRot rot) {
        double r = (double) rot.getCurrentHp() / rot.getMaxHp();
        return r > 0.5 ? new Color(60, 180, 80) : r > 0.25 ? new Color(220, 180, 40) : new Color(210, 60, 60);
    }
}