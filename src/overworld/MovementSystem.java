package overworld;

import engine.GamePanel;
import input.KeyboardHandler;

import static utils.Constants.TILE_SIZE;

public class MovementSystem {
    private final GamePanel gp;
    private final KeyboardHandler kh;
    private final Player player;

    private int moveProgress = 0;      // How far we've moved in the current tile
    private boolean isWalking = false; // True while moving across a tile

    public MovementSystem(GamePanel gp, KeyboardHandler kh, Player player) {
        this.gp = gp;
        this.kh = kh;
        this.player = player;
    }

    public void update() {
        int currentSpeed = player.speed;
        // Speed adjustment (only at the start of a step)
        if (!isWalking && kh.running) currentSpeed += 8;

        // --- Step 1: Start a tile move ---
        // Inside Step 1: Start a tile move
        if (!isWalking && kh.isMoving()) {
            // 1. Set Direction first
            if (kh.upPressed) player.setDirection("up");
            else if (kh.downPressed) player.setDirection("down");
            else if (kh.leftPressed) player.setDirection("left");
            else if (kh.rightPressed) player.setDirection("right");

            // 2. CHECK COLLISION BEFORE STARTING
            player.collisionOn = false;
            gp.collisionChecker.checkTile(player);
            gp.collisionChecker.checkEntity(player, gp.npcs);

            // 3. ONLY start walking if the path is 100% clear
            if (!player.collisionOn) {
                isWalking = true;
                moveProgress = 0;
                player.setIsMoving(true);
            } else {
                // If blocked, just stay at the current tile boundary
                player.setIsMoving(false);
            }
        }

        // --- Step 2: Move across the tile ---
        if (isWalking) {
            switch (player.getDirection()) {
                case "up"    -> player.worldY -= currentSpeed;
                case "down"  -> player.worldY += currentSpeed;
                case "left"  -> player.worldX -= currentSpeed;
                case "right" -> player.worldX += currentSpeed;
            }

            moveProgress += currentSpeed;

            // --- Step 3: Snap to grid at tile boundary ---
            if (moveProgress >= TILE_SIZE) {
                // Snap to exact grid position
                int overshoot = moveProgress - TILE_SIZE;
                switch (player.getDirection()) {
                    case "up"    -> player.worldY += overshoot;
                    case "down"  -> player.worldY -= overshoot;
                    case "left"  -> player.worldX += overshoot;
                    case "right" -> player.worldX -= overshoot;
                }

                moveProgress = 0;
                isWalking = false;

                // Continue walking if key is still held and path is clear
                if (!kh.isMoving()) {
                    player.setIsMoving(false);
                }
            }
        }
    }
}
