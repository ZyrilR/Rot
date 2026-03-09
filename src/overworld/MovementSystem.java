package overworld;

import engine.GamePanel;
import input.KeyboardHandler;

public class MovementSystem {

    GamePanel gp;

    public MovementSystem(GamePanel gp) {
        this.gp = gp;
    }

    public void movePlayer(Player player, KeyboardHandler kh) {
        if (kh.upPressed) {
            player.y -= player.speed;
        }
        if (kh.downPressed) {
            player.y += player.speed;
        }
        if (kh.leftPressed) {
            player.x -= player.speed;
        }
        if (kh.rightPressed) {
            player.x += player.speed;
        }
    }
}