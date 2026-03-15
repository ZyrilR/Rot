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
        if (!isWalking && kh.running) currentSpeed += 8;

        // --- Step 1: Start a tile move ---
        if (!isWalking && kh.isMoving()) {
            if (kh.upPressed) player.setDirection("up");
            else if (kh.downPressed) player.setDirection("down");
            else if (kh.leftPressed) player.setDirection("left");
            else if (kh.rightPressed) player.setDirection("right");

            // Collision check for intended tile
            player.collisionOn = false;
            gp.collisionChecker.checkTile(player);

            if (!player.collisionOn) {
                isWalking = true;
                moveProgress = 0;
                player.setIsMoving(true); // trigger walking animation
            }
        }

        // --- Step 2: Move across the tile ---
        if (isWalking) {
            switch (player.getDirection()) {
                case "up" -> player.worldY -= currentSpeed;
                case "down" -> player.worldY += currentSpeed;
                case "left" -> player.worldX -= currentSpeed;
                case "right" -> player.worldX += currentSpeed;
            }

            moveProgress += currentSpeed;

            // --- Step 3: Snap to grid at tile boundary ---
            if (moveProgress >= TILE_SIZE) {
                int overshoot = moveProgress - TILE_SIZE;
                switch (player.getDirection()) {
                    case "up" -> player.worldY += overshoot;
                    case "down" -> player.worldY -= overshoot;
                    case "left" -> player.worldX += overshoot;
                    case "right" -> player.worldX -= overshoot;
                }

                moveProgress = 0;
                isWalking = false;
                player.setIsMoving(false);
            }
        }

        // --- Step 4: Ignore input mid-tile ---
        if (!kh.isMoving() && !isWalking) {
            player.setIsMoving(false);
        }
    }
}
