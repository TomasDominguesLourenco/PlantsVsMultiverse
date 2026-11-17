package pt.escola.gardendefense;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.UserAction;

import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

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

    private GardenUI ui;
    private Polygon hoverHighlight;

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
        settings.setTitle("Plants vs. Multiverse");
        settings.setVersion("2.0");
        settings.setStageStyle(javafx.stage.StageStyle.UNDECORATED);
        settings.setFullScreenAllowed(true);
        settings.setFullScreenFromStart(true);
        settings.setPreserveResizeRatio(true);
    }

    @Override
    protected void initGame() {
        loopBGM("MainTheme.wav");
        getGameScene().setBackgroundColor(Color.web("#83905c"));

        hoverHighlight = new Polygon();
        hoverHighlight.setMouseTransparent(true);
        hoverHighlight.setStrokeWidth(2);
        hoverHighlight.setVisible(false);
        getGameScene().addUINode(hoverHighlight);
        ui = new GardenUI(this::startGame, this::showMenu, () -> javafx.application.Platform.exit());
        getGameScene().addUINode(ui.root);

        showMenu();
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
                if (state != State.MENU) {
                    showMenu();
                }
            }
        }, KeyCode.M);

        getInput().addAction(new UserAction("Voltar ao quintal") {
            @Override
            protected void onActionBegin() {
                if (state != State.MENU) showMenu();
            }
        }, KeyCode.ESCAPE);

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
        var ground = BoardProjection.world(mx, my);
        mx = ground.getX();
        my = ground.getY();
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

        hoverHighlight.setVisible(false);
        drawLawn();
        ui.showMenu();
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

        ui.showGame();

        drawLawn();
        state = State.PLAYING;
        updateInterface();
    }

    private void drawLawn() {
        entityBuilder()
                .view(new LawnView(false))
                .zIndex(-100)
                .buildAndAttach();
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
                .view(groundShadow(PLANT_W, PLANT_H))
                .view(texture("PeaShooter.png", PLANT_W, PLANT_H))
                .buildAndAttach();

        plantGrid[row][col] = new Plant(plant, row, col);
        hasPlant[row][col] = true;
        BoardProjection.place(plant, PLANT_W, PLANT_H, row, 0);
    }

    private javafx.scene.shape.Ellipse groundShadow(double width, double height) {
        var shadow = new javafx.scene.shape.Ellipse(width / 2 + 5, height - 3, width * 0.48, 7);
        shadow.setFill(Color.rgb(34, 49, 24, 0.24));
        shadow.setMouseTransparent(true);
        return shadow;
    }

    @Override
    protected void onUpdate(double tpf) {
        if (ui == null) return;
        if (state != State.PLAYING) {
            return;
        }

        elapsed += tpf;
        updateInterface();

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
        if (state != State.PLAYING) return;
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
        var ground = BoardProjection.world(getInput().getMouseXUI(), getInput().getMouseYUI());
        int col = (int) Math.floor((ground.getX() - GRID_X) / CELL_W);
        int row = (int) Math.floor((ground.getY() - GRID_Y) / CELL_H);
        if (row < 0 || row >= ROWS || col < 0 || col >= COLS) {
            hoverHighlight.setVisible(false);
            return;
        }
        BoardProjection.cell(hoverHighlight, row, col);
        boolean available = !hasPlant[row][col] && sun >= PLANT_COST;
        hoverHighlight.setStroke(Color.web(available ? "#f2e59b" : "#b86d4e"));
        hoverHighlight.setFill(Color.web(available ? "#f2e59b30" : "#b86d4e25"));
        hoverHighlight.setVisible(true);
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
        BoardProjection.place(bullet, 16, 16, plant.row, 32);
    }

    private void updateBullets(double tpf) {
        for (Bullet bullet : new ArrayList<>(bullets)) {
            bullet.entity.setX(bullet.entity.getX() + 300 * tpf);
            BoardProjection.place(bullet.entity, 16, 16, bullet.row, 32);
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
        BoardProjection.place(zombieEntity, ZOMBIE_W, ZOMBIE_H, row, 0);
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

            BoardProjection.place(e, ZOMBIE_W, ZOMBIE_H, zombie.row, 0);
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
        double groundY = GRID_Y + random.nextDouble() * (ROWS * CELL_H - 50);
        var landing = BoardProjection.screen(x + 27, groundY + 27);
        x = landing.getX() - 27;
        double targetY = landing.getY() - 27;

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
        ui.update(sun, score, lives, elapsed);
    }

    private void endGame() {
        state = State.GAME_OVER;
        hoverHighlight.setVisible(false);

        ui.showResult(score);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
