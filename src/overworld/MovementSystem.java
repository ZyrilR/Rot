package overworld;

import engine.GamePanel;
import input.KeyboardHandler;

import java.awt.*;

public class MovementSystem {

    GamePanel gp;

    public MovementSystem(GamePanel gp) {
        this.gp = gp;
    }

    public void movePlayer(Player player, KeyboardHandler kh) {
        boolean moving = false;

        if (kh.upPressed) {
            player.setDirection("up");
            player.y -= player.speed;
            moving = true;
        } else if (kh.downPressed) {
            player.setDirection("down");
            player.y += player.speed;
            moving = true;
        } else if (kh.leftPressed) {
            player.setDirection("left");
            player.x -= player.speed;
            moving = true;
        } else if (kh.rightPressed) {
            player.setDirection("right");
            player.x += player.speed;
            moving = true;
        }
        player.setIsMoving(moving);
//        player.resetSpriteCounter();
    }
}