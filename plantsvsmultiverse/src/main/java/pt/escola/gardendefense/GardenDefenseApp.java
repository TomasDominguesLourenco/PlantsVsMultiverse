package pt.escola.gardendefense;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.UserAction;

import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.almasb.fxgl.dsl.FXGL.*;

public class GardenDefenseApp extends GameApplication {

    private enum State { MENU, PLAYING, GAME_OVER }

    private static final int WIDTH = 1000;
    private static final int HEIGHT = 600;
    private static final int ROWS = 5;
    private static final int COLS = 9;

    private static final double GRID_X = 100;
    private static final double GRID_Y = 100;
    private static final double CELL_W = 90;
    private static final double CELL_H = 90;

    private static final int PLANT_COST = 50;
    private static final int PLANT_HP = 100;
    private static final double PLANT_W = 52;
    private static final double PLANT_H = 58;
    private static final double ZOMBIE_W = 38;
    private static final double ZOMBIE_H = 80;
    private static final double ZOMBIE_DPS = 35;
    private static final int SUN_AMOUNT = 25;

    private State state = State.MENU;

    private int sun = 150;
    private int score = 0;
    private int lives = 3;
    private double elapsed = 0;

    private final boolean[][] hasPlant = new boolean[ROWS][COLS];
    private final Plant[][] plantGrid = new Plant[ROWS][COLS];
    private final double[][] shootTimers = new double[ROWS][COLS];

    private final List<Zombie> zombies = new ArrayList<>();
    private final List<Bullet> bullets = new ArrayList<>();
    private final List<Sun> suns = new ArrayList<>();

    private double spawnTimer = 0;
    private double sunTimer = 0;

    private Text statusText;
    private Text controlsText;
    private Text menuTitle;
    private Text menuSubtitle;
    private Text menuHint;
    private Text menuControls;
    private Text gameOverText;
    private Rectangle hoverHighlight;

    private static class Plant {
        Entity entity;
        final int row;
        final int col;
        double hp = PLANT_HP;

        Plant(Entity entity, int row, int col) {
            this.entity = entity;
            this.row = row;
            this.col = col;
        }
    }

    private static class Zombie {
        Entity entity;
        final int row;
        int hp;
        final int maxHp;
        final double speed;

        Zombie(Entity entity, int row, int hp, double speed) {
            this.entity = entity;
            this.row = row;
            this.hp = hp;
            this.maxHp = hp;
            this.speed = speed;
        }
    }

    private static class Bullet {
        Entity entity;
        final int row;

        Bullet(Entity entity, int row) {
            this.entity = entity;
            this.row = row;
        }
    }

    private static class Sun {
        Entity entity;
        final double targetY;
        double life = 10.0;

        Sun(Entity entity, double targetY) {
            this.entity = entity;
            this.targetY = targetY;
        }
    }

    private final Random random = new Random();

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(WIDTH);
        settings.setHeight(HEIGHT);
        settings.setTitle("Plants VS Multiversus");
        settings.setVersion("2.0");
        settings.setStageStyle(javafx.stage.StageStyle.UNDECORATED);
        settings.setFullScreenAllowed(true);
    }

    @Override
    protected void initGame() {
        goFullscreen();
        loopBGM("MainTheme.wav");
        getGameScene().setBackgroundColor(Color.rgb(35, 45, 35));

        statusText = uiText("", 25, 35, 19);
        controlsText = uiText("Clique: plantar / apanhar sol | ENTER: começar | M: menu", 25, 575, 15);

        menuTitle = uiText("PLANTS VS MULTIVERSUS", 220, 200, 42);
        menuSubtitle = uiText("Defende o jardim dos zombies!", 330, 250, 22);
        menuControls = uiText(
                "Clique numa célula para plantar (50 energia)\n"
                + "Clique no sol para o apanhar (+25)\n"
                + "C: +25 energia (tecla de teste)",
                300, 320, 17
        );
        menuHint = uiText("— premi ENTER ou clique para começar —", 310, 430, 20);

        gameOverText = uiText("", 300, 260, 36);

        hoverHighlight = new Rectangle(CELL_W - 2, CELL_H - 2);
        hoverHighlight.setFill(Color.rgb(255, 255, 255, 0.25));
        hoverHighlight.setStroke(Color.YELLOW);
        hoverHighlight.setStrokeWidth(2);
        hoverHighlight.setVisible(false);
        getGameScene().addUINode(hoverHighlight);

        showMenu();
    }

    private Text uiText(String content, double x, double y, int size) {
        Text t = new Text(content);
        t.setFont(Font.font("Arial", size));
        t.setFill(Color.WHITE);
        t.setX(x);
        t.setY(y);
        getGameScene().addUINode(t);
        return t;
    }

    private void goFullscreen() {
        var bounds = javafx.stage.Screen.getPrimary().getBounds();
        var stage = getPrimaryStage();

        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());
    }

    @Override
    protected void initInput() {
        getInput().addAction(new UserAction("Confirmar") {
            @Override
            protected void onActionBegin() {
                if (state == State.MENU || state == State.GAME_OVER) {
                    startGame();
                }
            }
        }, KeyCode.ENTER);

        getInput().addAction(new UserAction("Menu") {
            @Override
            protected void onActionBegin() {
                if (state == State.GAME_OVER) {
                    showMenu();
                }
            }
        }, KeyCode.M);

        getInput().addAction(new UserAction("Energia de teste") {
            @Override
            protected void onActionBegin() {
                if (state == State.PLAYING) {
                    sun += 25;
                    updateInterface();
                }
            }
        }, KeyCode.C);

        getInput().addAction(new UserAction("Clique") {
            @Override
            protected void onActionBegin() {
                handleClick();
            }
        }, MouseButton.PRIMARY);
    }

    private void handleClick() {
        double mx = getInput().getMouseXUI();
        double my = getInput().getMouseYUI();

        if (state == State.MENU || state == State.GAME_OVER) {
            startGame();
            return;
        }

        if (state != State.PLAYING) {
            return;
        }

        if (tryCollectSun(mx, my)) {
            return;
        }

        tryPlant(mx, my);
    }

    private boolean tryCollectSun(double mx, double my) {
        for (Sun s : new ArrayList<>(suns)) {
            Entity e = s.entity;
            double cx = e.getX() + 27;
            double cy = e.getY() + 27;
            double dx = mx - cx;
            double dy = my - cy;

            if (dx * dx + dy * dy <= 42 * 42) {
                sun += SUN_AMOUNT;
                removeSun(s);
                updateInterface();
                return true;
            }
        }
        return false;
    }

    private void tryPlant(double mx, double my) {
        int col = (int) Math.floor((mx - GRID_X) / CELL_W);
        int row = (int) Math.floor((my - GRID_Y) / CELL_H);

        if (row < 0 || row >= ROWS || col < 0 || col >= COLS) {
            return;
        }

        if (hasPlant[row][col] || sun < PLANT_COST) {
            return;
        }

        sun -= PLANT_COST;
        plantAt(row, col);
        updateInterface();
    }

    private void showMenu() {
        state = State.MENU;
        clearWorld();

        statusText.setVisible(false);
        controlsText.setVisible(false);
        gameOverText.setVisible(false);
        hoverHighlight.setVisible(false);

        menuTitle.setVisible(true);
        menuSubtitle.setVisible(true);
        menuControls.setVisible(true);
        menuHint.setVisible(true);
    }

    private void startGame() {
        clearWorld();

        sun = 150;
        score = 0;
        lives = 3;
        elapsed = 0;
        spawnTimer = 0;
        sunTimer = 0;

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                hasPlant[row][col] = false;
                plantGrid[row][col] = null;
                shootTimers[row][col] = 0;
            }
        }

        menuTitle.setVisible(false);
        menuSubtitle.setVisible(false);
        menuControls.setVisible(false);
        menuHint.setVisible(false);
        gameOverText.setVisible(false);

        statusText.setVisible(true);
        controlsText.setVisible(true);

        drawLawn();
        state = State.PLAYING;
        updateInterface();
    }

    private void drawLawn() {
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
    }

    private void clearWorld() {
        for (Entity e : new ArrayList<>(getGameWorld().getEntities())) {
            getGameWorld().removeEntity(e);
        }
        zombies.clear();
        bullets.clear();
        suns.clear();

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                hasPlant[row][col] = false;
                plantGrid[row][col] = null;
            }
        }
    }

    private void plantAt(int row, int col) {
        double x = GRID_X + col * CELL_W + CELL_W / 2 - PLANT_W / 2;
        double y = GRID_Y + row * CELL_H + CELL_H / 2 - PLANT_H / 2;

        Entity plant = entityBuilder()
                .at(x, y)
                .view(texture("PeaShooter.png", PLANT_W, PLANT_H))
                .buildAndAttach();

        plantGrid[row][col] = new Plant(plant, row, col);
        hasPlant[row][col] = true;
    }

    @Override
    protected void onUpdate(double tpf) {
        if (state != State.PLAYING) {
            return;
        }

        elapsed += tpf;

        updateHover();

        spawnTimer += tpf;
        if (spawnTimer >= spawnInterval()) {
            spawnTimer = 0;
            spawnZombie();
        }

        sunTimer += tpf;
        if (sunTimer >= 5.0) {
            sunTimer = 0;
            spawnSun();
        }

        updatePlants(tpf);
        updateBullets(tpf);
        updateZombies(tpf);
        updateSuns(tpf);
        checkBulletHits();
    }

    private double spawnInterval() {
        return Math.max(1.5, 4.5 - elapsed * 0.04);
    }

    private double zombieSpeed() {
        return 25 + Math.min(25, elapsed * 0.3);
    }

    private int zombieHp() {
        return 3 + (int) (elapsed / 45);
    }

    private void updateHover() {
        double mx = getInput().getMouseXUI();
        double my = getInput().getMouseYUI();

        int col = (int) Math.floor((mx - GRID_X) / CELL_W);
        int row = (int) Math.floor((my - GRID_Y) / CELL_H);

        if (row < 0 || row >= ROWS || col < 0 || col >= COLS) {
            hoverHighlight.setVisible(false);
            return;
        }

        double x = GRID_X + col * CELL_W + 1;
        double y = GRID_Y + row * CELL_H + 1;
        hoverHighlight.setX(x);
        hoverHighlight.setY(y);
        hoverHighlight.setVisible(true);

        if (!hasPlant[row][col] && sun >= PLANT_COST) {
            hoverHighlight.setStroke(Color.YELLOW);
            hoverHighlight.setFill(Color.rgb(255, 255, 120, 0.3));
        } else {
            hoverHighlight.setStroke(Color.RED);
            hoverHighlight.setFill(Color.rgb(255, 80, 80, 0.3));
        }
    }

    private void updatePlants(double tpf) {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                Plant plant = plantGrid[row][col];
                if (plant == null) {
                    continue;
                }

                shootTimers[row][col] += tpf;
                if (shootTimers[row][col] >= 1.3 && hasZombieInRow(row)) {
                    shootTimers[row][col] = 0;
                    shoot(plant);
                }
            }
        }
    }

    private boolean hasZombieInRow(int row) {
        for (Zombie zombie : zombies) {
            if (zombie.row == row) {
                return true;
            }
        }
        return false;
    }

    private void shoot(Plant plant) {
        Entity e = plant.entity;
        double x = e.getX() + PLANT_W - 8;
        double y = e.getY() + 16;

        Entity bullet = entityBuilder()
                .at(x, y)
                .viewWithBBox(texture("ProjectilePea.png", 16, 16))
                .buildAndAttach();

        bullets.add(new Bullet(bullet, plant.row));
    }

    private void updateBullets(double tpf) {
        for (Bullet bullet : new ArrayList<>(bullets)) {
            bullet.entity.setX(bullet.entity.getX() + 300 * tpf);
            if (bullet.entity.getX() > WIDTH) {
                removeBullet(bullet);
            }
        }
    }

    private void spawnZombie() {
        int row = random.nextInt(ROWS);
        double x = WIDTH - ZOMBIE_W;
        double y = GRID_Y + row * CELL_H + CELL_H / 2 - ZOMBIE_H / 2;

        Entity zombieEntity = entityBuilder()
                .at(x, y)
                .viewWithBBox(texture("Zombie.png", ZOMBIE_W, ZOMBIE_H))
                .buildAndAttach();

        zombies.add(new Zombie(zombieEntity, row, zombieHp(), zombieSpeed()));
    }

    private void updateZombies(double tpf) {
        for (Zombie zombie : new ArrayList<>(zombies)) {
            Entity e = zombie.entity;
            Plant blocking = findBlockingPlant(zombie);

            if (blocking != null) {
                blocking.hp -= ZOMBIE_DPS * tpf;
                if (blocking.hp <= 0) {
                    removePlant(blocking);
                }
            } else {
                e.setX(e.getX() - zombie.speed * tpf);
            }

            if (e.getX() < GRID_X - 20) {
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

    private Plant findBlockingPlant(Zombie zombie) {
        Entity e = zombie.entity;
        double zLeft = e.getX();
        double zRight = e.getX() + ZOMBIE_W;

        for (int col = 0; col < COLS; col++) {
            Plant plant = plantGrid[zombie.row][col];
            if (plant == null) {
                continue;
            }

            double pLeft = plant.entity.getX();
            double pRight = pLeft + PLANT_W;

            if (zLeft < pRight && zRight > pLeft) {
                return plant;
            }
        }
        return null;
    }

    private void removePlant(Plant plant) {
        getGameWorld().removeEntity(plant.entity);
        plantGrid[plant.row][plant.col] = null;
        hasPlant[plant.row][plant.col] = false;
    }

    private void spawnSun() {
        double x = GRID_X + random.nextDouble() * (COLS * CELL_W - 40);
        double y = -10;
        double targetY = GRID_Y + random.nextDouble() * (ROWS * CELL_H - 50);

        Entity sunEntity = entityBuilder()
                .at(x, y)
                .viewWithBBox(texture("Sun.png", 54, 54))
                .buildAndAttach();

        suns.add(new Sun(sunEntity, targetY));
    }

    private void updateSuns(double tpf) {
        for (Sun s : new ArrayList<>(suns)) {
            Entity e = s.entity;

            if (e.getY() < s.targetY) {
                e.setY(Math.min(s.targetY, e.getY() + 45 * tpf));
            }

            s.life -= tpf;
            if (s.life <= 0) {
                removeSun(s);
            }
        }
    }

    private void checkBulletHits() {
        for (Bullet bullet : new ArrayList<>(bullets)) {
            Entity b = bullet.entity;

            for (Zombie zombie : new ArrayList<>(zombies)) {
                if (zombie.row != bullet.row) {
                    continue;
                }

                Entity z = zombie.entity;
                boolean sameRow = Math.abs(b.getY() - (z.getY() + ZOMBIE_H / 2)) < 30;
                boolean touching = Math.abs(b.getX() - (z.getX() + ZOMBIE_W / 2)) < 32;

                if (sameRow && touching) {
                    removeBullet(bullet);
                    zombie.hp--;

                    if (zombie.hp <= 0) {
                        removeZombie(zombie);
                        score += 10;
                        updateInterface();
                    } else {
                        double ratio = (double) zombie.hp / zombie.maxHp;
                        z.setOpacity(0.45 + 0.55 * ratio);
                    }
                    break;
                }
            }
        }
    }

    private void removeBullet(Bullet bullet) {
        getGameWorld().removeEntity(bullet.entity);
        bullets.remove(bullet);
    }

    private void removeZombie(Zombie zombie) {
        getGameWorld().removeEntity(zombie.entity);
        zombies.remove(zombie);
    }

    private void removeSun(Sun s) {
        getGameWorld().removeEntity(s.entity);
        suns.remove(s);
    }

    private void updateInterface() {
        statusText.setText(
                "Energia: " + sun
                + "    Pontuação: " + score
                + "    Vidas: " + lives
                + "    Tempo: " + (int) elapsed + "s"
        );
    }

    private void endGame() {
        state = State.GAME_OVER;
        hoverHighlight.setVisible(false);

        gameOverText.setText(
                "FIM DO JOGO!\n"
                + "Pontuação: " + score + "\n"
                + "ENTER: jogar outra vez   |   M: menu"
        );
        gameOverText.setX(310);
        gameOverText.setY(250);
        gameOverText.setVisible(true);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
