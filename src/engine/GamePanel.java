package engine;

import brainrots.BrainRotFactory;
import brainrots.Tier;
import input.KeyboardHandler;
import map.WorldLoader;
import npc.NPC;
import npc.MarketNPC;
import overworld.EncounterSystem;
import overworld.Player;
import storage.PCSystem;
import tile.CollisionChecker;
import tile.Tile;
import tile.TileManager;
import tile.TileTeleporter;
import ui.*;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import items.ItemRegistry;

import static utils.Constants.*;

public class GamePanel extends JPanel {

    public KeyboardHandler KEYBOARDHANDLER  = new KeyboardHandler();
    public EncounterSystem encounterSystem = new EncounterSystem();
    public CollisionChecker COLLISIONCHECKER = new CollisionChecker(this);
    public Player player = new Player(this, KEYBOARDHANDLER);

    public String GAMESTATE = "play";

    // UIs
    public DialogueBox DIALOGUEBOX = new DialogueBox(this);
    public BlackFadeEffect BLACKFADEEFFECT = new BlackFadeEffect();
    public BattleUI BATTLEUI = new BattleUI(this, KEYBOARDHANDLER);
    public StarterUI STARTERUI = new StarterUI(this, KEYBOARDHANDLER);
    public final ShopUI SHOPUI = new ShopUI(this);
    public final PCUI PCUI = new PCUI(this, player.getPCSYSTEM());
    public final MenuUI MENUUI = new MenuUI(this);
    public final InventoryUI INVENTORYUI = new InventoryUI(this);

    public final WorldLoader world = new WorldLoader(this);

    public GamePanel() {
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.setFocusable(true);this.requestFocusInWindow();
        this.setFocusTraversalKeysEnabled(false);
        this.addKeyListener(KEYBOARDHANDLER);

        world.loadMap(WORLD, true);

        if (player.getPCSYSTEM().getPartySize() == 0) {
            GAMESTATE = "starter";
        }
    }

    public ArrayList<TileManager> getWorldBackgroundLayer() { return world.getBackgroundLayer(); }
    public ArrayList<TileManager> getWorldBuildingLayer() { return world.getBuildingLayer(); }
    public TileManager getWorldInteractiveLayer() { return world.getInteractiveLayer(); }

    public void update() {
        switch (GAMESTATE.toUpperCase()) {
            case "STARTER":
                STARTERUI.update();
                break;

            case "PLAY":
                // --- ADD THESE 3 LINES ---
                if (!BLACKFADEEFFECT.isFadeOutComplete()) {
                    BLACKFADEEFFECT.update();
                }

                player.update();

                encounterSystem.checkTrainerLook(player, world.getInteractiveLayer().getNPCs(), this);
                encounterSystem.checkTrainerLook(player, world.getInteractiveLayer().getNPCs(), this);
                TileTeleporter tr = CollisionChecker.getTeleporterTileInCurrentPosition(this, player);
                if (tr != null) world.loadMap(tr.getLink(), true);

                if (KEYBOARDHANDLER.ePressed) {
                    KEYBOARDHANDLER.ePressed = false;
                    player.checkInteraction();
                }
                if (KEYBOARDHANDLER.escPressed) {
                    KEYBOARDHANDLER.escPressed = false;
                    MENUUI.open();
                    GAMESTATE = "menu";
                }

                if (encounterSystem.getActiveBattle() != null && !GAMESTATE.equalsIgnoreCase("battle_fade") && !GAMESTATE.equalsIgnoreCase("battle")) {
                    GAMESTATE = "battle_fade";
                    BLACKFADEEFFECT.start(BlackFadeEffect.FadeMode.FADE_IN_TO_BLACK, 10);
                }
                break;

            case "BATTLE_FADE":
                BLACKFADEEFFECT.update();
                if (BLACKFADEEFFECT.isFullyBlack()) {
                    BATTLEUI.setBattle(encounterSystem.getActiveBattle());
                    GAMESTATE = "battle";
                    BLACKFADEEFFECT.start(BlackFadeEffect.FadeMode.FADE_OUT_TO_PLAY, 10);
                }
                break;

            case "BATTLE":
                BATTLEUI.update();
                BLACKFADEEFFECT.update();
                break;

            case "DIALOGUE": DIALOGUEBOX.update(); break;
            case "SHOP":     SHOPUI.update(); break;
            case "PC":       PCUI.update(); break;
            case "MENU":     MENUUI.update(); break;

            case "INVENTORY":
                INVENTORYUI.update();
                // If we are in a battle and an item was selected, bounce back to the battle state
                if (encounterSystem.getActiveBattle() != null && INVENTORYUI.getSelectedItemForBattle() != null) {
                    GAMESTATE = "battle";
                }
                break;
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        if (GAMESTATE.equalsIgnoreCase("STARTER")) {
            STARTERUI.draw(g2);
        } else if (GAMESTATE.equalsIgnoreCase("BATTLE") || (GAMESTATE.equalsIgnoreCase("INVENTORY") && encounterSystem.getActiveBattle() != null)) {
            // Draw Battle Base
            BATTLEUI.draw(g2);
            // Draw Bag ON TOP if Bag is open during battle
            if (GAMESTATE.equalsIgnoreCase("INVENTORY")) {
                INVENTORYUI.draw(g2);
            }
        } else {
            world.draw(g2);
            for (npc.NPC npc : world.getInteractiveLayer().getNPCs()) {
                if (npc != null) npc.draw(g2, this);
            }
            player.draw(g2);
        }

        switch (GAMESTATE.toLowerCase()) {
            case "dialogue": DIALOGUEBOX.draw(g2); break;
            case "shop":     SHOPUI.draw(g2); break;
            case "menu":     MENUUI.draw(g2); break;
            case "pc":       PCUI.draw(g2); break;
            case "inventory":
                // Only draw here if it's an OVERWORLD inventory (battle handled above)
                if (encounterSystem.getActiveBattle() == null) {
                    INVENTORYUI.draw(g2);
                }
                break;
        }

        if (GAMESTATE.equalsIgnoreCase("BATTLE_FADE") || (GAMESTATE.equalsIgnoreCase("BATTLE") && !BLACKFADEEFFECT.isFadeOutComplete())) {
            BLACKFADEEFFECT.draw(g2);
        }

        g2.dispose();
    }
}