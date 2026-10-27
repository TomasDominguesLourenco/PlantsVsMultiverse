package pt.escola.gardendefense;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.UserAction;

import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.almasb.fxgl.dsl.FXGL.*;

public class GardenDefenseApp extends GameApplication {

    private static final int WIDTH = 1000;
    private static final int HEIGHT = 600;
    private static final int ROWS = 5;
    private static final int COLS = 9;

    private static final double GRID_X = 100;
    private static final double GRID_Y = 100;
    private static final double CELL_W = 90;
    private static final double CELL_H = 90;

    private int sun = 150;
    private int score = 0;
    private int lives = 3;
    private boolean gameOver = false;

    // Esta versão permite uma planta por linha.
    private final boolean[] planted = new boolean[ROWS];
    private final double[] shootTimers = new double[ROWS];

    private final List<Entity> zombies = new ArrayList<>();
    private final List<Entity> bullets = new ArrayList<>();
    private final Map<Entity, Integer> zombieHealth = new HashMap<>();
    private final Map<Entity, Integer> zombieRows = new HashMap<>();

    private final Random random = new Random();
    private double spawnTimer = 0;
    private double sunTimer = 0;

    private Text statusText;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(WIDTH);
        settings.setHeight(HEIGHT);
        settings.setTitle("Garden Defense");
        settings.setVersion("1.0");
    }

    @Override
    protected void initGame() {
        getGameScene().setBackgroundColor(Color.rgb(35, 45, 35));

        // Desenha a grelha do relvado.
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                double x = GRID_X + col * CELL_W;
                double y = GRID_Y + row * CELL_H;

                Color grass = (row + col) % 2 == 0
                        ? Color.rgb(105, 190, 75)
                        : Color.rgb(90, 175, 65);

                entityBuilder()
                        .at(x, y)
                        .view(new Rectangle(CELL_W - 2, CELL_H - 2, grass))
                        .buildAndAttach();
            }
        }

        statusText = new Text();
        statusText.setFont(Font.font("Arial", 19));
        statusText.setFill(Color.WHITE);
        statusText.setX(25);
        statusText.setY(35);
        getGameScene().addUINode(statusText);

        Text controlsText = new Text(
                "Q/W/E/R/T: plantar | C: +25 energia (teste)"
        );
        controlsText.setFont(Font.font("Arial", 15));
        controlsText.setFill(Color.WHITE);
        controlsText.setX(25);
        controlsText.setY(570);
        getGameScene().addUINode(controlsText);

        updateInterface();
    }

    @Override
    protected void initInput() {
        addPlantKey(KeyCode.Q, 0);
        addPlantKey(KeyCode.W, 1);
        addPlantKey(KeyCode.E, 2);
        addPlantKey(KeyCode.R, 3);
        addPlantKey(KeyCode.T, 4);

        getInput().addAction(new UserAction("Energia de teste") {
            @Override
            protected void onActionBegin() {
                if (!gameOver) {
                    sun += 25;
                    updateInterface();
                }
            }
        }, KeyCode.C);
    }

    private void addPlantKey(KeyCode key, int row) {
        getInput().addAction(new UserAction("Plantar linha " + (row + 1)) {
            @Override
            protected void onActionBegin() {
                plant(row);
            }
        }, key);
    }

    private void plant(int row) {
        if (gameOver || planted[row] || sun < 50) {
            return;
        }

        sun -= 50;
        planted[row] = true;

        double x = GRID_X + 20;
        double centerY = GRID_Y + row * CELL_H + CELL_H / 2;

        // Caule
        entityBuilder()
                .at(x + 10, centerY + 2)
                .view(new Rectangle(12, 25, Color.DARKGREEN))
                .buildAndAttach();

        // Corpo da planta
        entityBuilder()
                .at(x, centerY - 20)
                .view(new Circle(22, Color.LIMEGREEN))
                .buildAndAttach();

        // Centro da planta
        entityBuilder()
                .at(x + 12, centerY - 24)
                .view(new Circle(7, Color.GOLD))
                .buildAndAttach();

        updateInterface();
    }

    @Override
    protected void onUpdate(double tpf) {
        if (gameOver) {
            return;
        }

        spawnTimer += tpf;
        if (spawnTimer >= 4.0) {
            spawnTimer = 0;
            spawnZombie();
        }

        sunTimer += tpf;
        if (sunTimer >= 5.0) {
            sunTimer = 0;
            sun += 25;
            updateInterface();
        }

        for (int row = 0; row < ROWS; row++) {
            if (!planted[row]) {
                continue;
            }

            shootTimers[row] += tpf;
            if (shootTimers[row] >= 1.3 && hasZombieInRow(row)) {
                shootTimers[row] = 0;
                shoot(row);
            }
        }

        moveBullets(tpf);
        moveZombies(tpf);
        checkCollisions();
    }

    private void spawnZombie() {
        int row = random.nextInt(ROWS);
        double x = WIDTH - 35;
        double centerY = GRID_Y + row * CELL_H + CELL_H / 2;

        Entity zombie = entityBuilder()
                .at(x, centerY - 20)
                .viewWithBBox(new Circle(23, Color.FORESTGREEN))
                .buildAndAttach();

        zombies.add(zombie);
        zombieHealth.put(zombie, 3);
        zombieRows.put(zombie, row);
    }

    private boolean hasZombieInRow(int row) {
        for (Entity zombie : zombies) {
            if (zombieRows.getOrDefault(zombie, -1) == row) {
                return true;
            }
        }
        return false;
    }

    private void shoot(int row) {
        double x = GRID_X + 75;
        double centerY = GRID_Y + row * CELL_H + CELL_H / 2;

        Entity bullet = entityBuilder()
                .at(x, centerY - 8)
                .viewWithBBox(new Circle(9, Color.YELLOW))
                .buildAndAttach();

        bullets.add(bullet);
    }

    private void moveBullets(double tpf) {
        for (Entity bullet : new ArrayList<>(bullets)) {
            bullet.setX(bullet.getX() + 300 * tpf);
            if (bullet.getX() > WIDTH) {
                removeBullet(bullet);
            }
        }
    }

    private void moveZombies(double tpf) {
        for (Entity zombie : new ArrayList<>(zombies)) {
            zombie.setX(zombie.getX() - 25 * tpf);

            if (zombie.getX() < GRID_X - 20) {
                lives--;
                removeZombie(zombie);
                updateInterface();

                if (lives <= 0) {
                    endGame();
                    return;
                }
            }
        }
    }

    private void checkCollisions() {
        for (Entity bullet : new ArrayList<>(bullets)) {
            for (Entity zombie : new ArrayList<>(zombies)) {
                boolean sameRow = Math.abs(bullet.getY() - zombie.getY()) < 30;
                boolean touching = Math.abs(bullet.getX() - zombie.getX()) < 30;

                if (sameRow && touching) {
                    removeBullet(bullet);

                    int health = zombieHealth.getOrDefault(zombie, 3) - 1;
                    if (health <= 0) {
                        removeZombie(zombie);
                        score += 10;
                        updateInterface();
                    } else {
                        zombieHealth.put(zombie, health);
                    }
                    break;
                }
            }
        }
    }

    private void removeBullet(Entity bullet) {
        getGameWorld().removeEntity(bullet);
        bullets.remove(bullet);
    }

    private void removeZombie(Entity zombie) {
        getGameWorld().removeEntity(zombie);
        zombies.remove(zombie);
        zombieHealth.remove(zombie);
        zombieRows.remove(zombie);
    }

    private void updateInterface() {
        if (statusText != null) {
            statusText.setText(
                    "Energia: " + sun
                    + "    Pontuação: " + score
                    + "    Vidas: " + lives
            );
        }
    }

    private void endGame() {
        gameOver = true;

        Text message = new Text("FIM DO JOGO!\nPontuação: " + score);
        message.setFont(Font.font("Arial", 38));
        message.setFill(Color.WHITE);
        message.setX(330);
        message.setY(300);

        getGameScene().addUINode(message);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
