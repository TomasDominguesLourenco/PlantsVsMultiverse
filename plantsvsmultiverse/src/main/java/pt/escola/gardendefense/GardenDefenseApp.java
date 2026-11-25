package pt.escola.gardendefense;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.UserAction;

import javafx.scene.input.KeyCode;
import javafx.scene.Cursor;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

import pt.escola.gardendefense.personagens.plants.CatalogoPlantas;
import pt.escola.gardendefense.personagens.plants.Planta;
import pt.escola.gardendefense.personagens.plants.VistaAnimada;
import pt.escola.gardendefense.personagens.zombies.ZombieNormal;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.almasb.fxgl.dsl.FXGL.*;

public class GardenDefenseApp extends GameApplication {

    private enum State { MENU, ENTERING_GARDEN, PLAYING, PAUSED, GAME_OVER }

    private static final int WIDTH = 1000;
    private static final int HEIGHT = 600;
    private static final int ROWS = 5;
    private static final int COLS = 9;

    private static final double GRID_X = 100;
    private static final double GRID_Y = 100;
    private static final double CELL_W = 90;
    private static final double CELL_H = 90;

    private static final int SUN_AMOUNT = 25;
    private static final int PROJECTILE_SIZE = 16;

    private final List<Planta> plantas = CatalogoPlantas.carregar();
    private final Planta plantaPadrao = plantas.getFirst();
    private Planta plantaSelecionada = plantaPadrao;
    private final ZombieNormal zombiePadrao = new ZombieNormal();

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
    private GardenTransition gardenTransition;
    private Polygon hoverHighlight;
    private boolean portalSelected;
    private Cursor portalCursor;

    private static class Plant {
        final Planta definicao;
        final VistaAnimada animation;
        Entity entity;
        final int row;
        final int col;
        double hp;
        Zombie attackTarget;
        double impactTimer;
        double shotWindup = -1;
        boolean preparingShot;
        int burstShotsRemaining;
        double burstShotTimer;
        double extraShotCooldown;
        double sunProductionTimer;
        PlantPortal portal;
        Entity portalEntity;

        Plant(Planta definicao, Entity entity, int row, int col, VistaAnimada animation) {
            this.definicao = definicao;
            this.animation = animation;
            this.hp = definicao.getVidaMax();
            this.entity = entity;
            this.row = row;
            this.col = col;
        }
    }

    private static class Zombie {
        final ZombieNormal definicao;
        Entity entity;
        final int row;
        int hp;
        final int maxHp;
        final double speed;

        Zombie(ZombieNormal definicao, Entity entity, int row, int hp, double speed) {
            this.definicao = definicao;
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

        final double speed;
        final int dano;
        final double lift;
        final VistaAnimada animation;
        double firstStep;
        ArcFlight arc;

        Bullet(Entity entity, int row, double speed, int dano, double lift, VistaAnimada animation, double firstStep) {
            this.speed = speed;
            this.dano = dano;
            this.lift = lift;
            this.animation = animation;
            this.firstStep = firstStep;
            this.entity = entity;
            this.row = row;
        }
    }

    private static class ArcFlight {
        final Zombie target;
        final double startX;
        final double duration;
        double elapsed;

        ArcFlight(Zombie target, double startX, double speed) {
            this.target = target;
            this.startX = startX;
            double distance = Math.max(0, targetX() - startX);
            double speedAtDistance = speed + 0.5 * distance;
            this.duration = Math.max(0.18, distance / speedAtDistance);
        }

        double extraCooldown() { return Math.max(0, targetX() - startX) / CELL_W * 0.30; }

        double targetX() {
            return target.entity.getX() + target.definicao.getLargura() / 2 - PROJECTILE_SIZE / 2.0;
        }
    }

    private static class Sun {
        static final double LIFETIME = 10.0;
        Entity entity;
        final double targetY;
        final int amount;
        double life = LIFETIME;
        SunToss toss;

        Sun(Entity entity, double targetY, int amount) {
            this.entity = entity;
            this.targetY = targetY;
            this.amount = amount;
        }
    }

    private record SunToss(double x, double y, double targetX) {}

    private final Random random = new Random();

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(WIDTH);
        settings.setHeight(HEIGHT);
        settings.setTitle("Plants vs. Multiverse");
        settings.setVersion("2.0");
        settings.setFullScreenAllowed(true);
        settings.setFullScreenFromStart(true);
        settings.setPreserveResizeRatio(true);
        // All menu and pause controls belong to GardenUI, including ESC.
        settings.setMainMenuEnabled(false);
        settings.setGameMenuEnabled(false);
    }

    @Override
    protected void initGame() {
        loopBGM("MainTheme.wav");
        getGameScene().setBackgroundColor(Color.web("#1d2c24"));

        hoverHighlight = new Polygon();
        hoverHighlight.setMouseTransparent(true);
        hoverHighlight.setStrokeWidth(2);
        hoverHighlight.setVisible(false);
        getGameScene().addUINode(hoverHighlight);
        ui = new GardenUI(plantas, plant -> {
            if (state == State.PLAYING) selectPlant(plant);
        }, () -> {
            if (state == State.PLAYING) selectPortal(!portalSelected);
        }, this::startGame, this::showMenu, this::togglePause,
                () -> javafx.application.Platform.exit());
        getGameScene().addUINode(ui.root);
        gardenTransition = new GardenTransition(getGameScene().getContentRoot());
        getPrimaryStage().setFullScreenExitKeyCombination(javafx.scene.input.KeyCombination.NO_MATCH);
        getPrimaryStage().setFullScreenExitHint("");
        getPrimaryStage().focusedProperty().addListener((_, _, focused) -> {
            if (!focused && state == State.PLAYING) pauseGame();
        });

        showMenu();
    }

    @Override
    protected void initInput() {
        getInput().addAction(new UserAction("Confirmar") {
            @Override
            protected void onActionBegin() {
                if (state == State.MENU || state == State.GAME_OVER) {
                    startGame();
                } else if (state == State.PAUSED) {
                    resumeGame();
                }
            }
        }, KeyCode.ENTER);

        getInput().addAction(new UserAction("Menu") {
            @Override
            protected void onActionBegin() {
                if (state == State.GAME_OVER) showMenu();
                else togglePause();
            }
        }, KeyCode.M);

        getInput().addAction(new UserAction("Pausar ou continuar") {
            @Override
            protected void onActionBegin() {
                if (portalSelected) selectPortal(false);
                else togglePause();
            }
        }, KeyCode.ESCAPE);

        getInput().addAction(new UserAction("Pausa rápida") {
            @Override
            protected void onActionBegin() { togglePause(); }
        }, KeyCode.P);

        getInput().addAction(new UserAction("Energia de teste") {
            @Override
            protected void onActionBegin() {
                if (state == State.PLAYING) {
                    sun += 25;
                    updateInterface();
                }
            }
        }, KeyCode.C);

        KeyCode[] atalhos = { KeyCode.DIGIT1, KeyCode.DIGIT2, KeyCode.DIGIT3,
                KeyCode.DIGIT4, KeyCode.DIGIT5, KeyCode.DIGIT6,
                KeyCode.DIGIT7, KeyCode.DIGIT8, KeyCode.DIGIT9 };
        for (int i = 0; i < Math.min(plantas.size(), atalhos.length); i++) {
            bindPlantSelection(plantas.get(i), atalhos[i]);
        }

        getInput().addAction(new UserAction("Clique") {
            @Override
            protected void onActionBegin() {
                handleClick();
            }
        }, MouseButton.PRIMARY);
    }

    private void bindPlantSelection(Planta plant, KeyCode key) {
        getInput().addAction(new UserAction("Selecionar " + plant.getClass().getName()) {
            @Override
            protected void onActionBegin() {
                if (state == State.PLAYING) {
                    selectPlant(plant);
                }
            }
        }, key);
    }

    private void selectPlant(Planta plant) {
        if (!plantas.contains(plant)) return;
        plantaSelecionada = plant;
        selectPortal(false);
    }

    private void selectPortal(boolean active) {
        portalSelected = active;
        if (active && portalCursor == null) portalCursor = PlantPortal.createCursor();
        getPrimaryStage().getScene().setCursor(active ? portalCursor : Cursor.DEFAULT);
        ui.showPortalSelected(active);
        if (!active) ui.showSelectedPlant(plantaSelecionada);
    }

    private void handleClick() {
        double mx = getInput().getMouseXUI();
        double my = getInput().getMouseYUI();

        if (state != State.PLAYING) {
            return;
        }

        // Seed-tray clicks select a template; they must not collect a sun behind the HUD.
        if (my < 100) return;

        if (portalSelected) {
            Plant target = portalTarget(mx, my);
            if (target != null) openPortal(target);
            return;
        }

        if (tryCollectSun(mx, my)) {
            return;
        }

        tryPlant(mx, my);
    }

    private Plant portalTarget(double mx, double my) {
        for (int row = ROWS - 1; row >= 0; row--) {
            double groundY = GRID_Y + row * CELL_H + 73;
            double scale = BoardProjection.scaleAt(groundY);
            for (Plant plant : plantGrid[row]) {
                if (plant == null || plant.portal != null) continue;
                double width = plant.definicao.getLargura();
                var foot = BoardProjection.screen(plant.entity.getX() + width / 2, groundY);
                if (Math.abs(mx - foot.getX()) <= width * scale / 2
                        && my >= foot.getY() - plant.definicao.getAltura() * scale
                        && my <= foot.getY()) return plant;
            }
        }
        var ground = BoardProjection.world(mx, my);
        int row = (int) Math.floor((ground.getY() - GRID_Y) / CELL_H);
        int col = (int) Math.floor((ground.getX() - GRID_X) / CELL_W);
        if (row < 0 || row >= ROWS || col < 0 || col >= COLS) return null;
        Plant plant = plantGrid[row][col];
        return plant != null && plant.portal == null ? plant : null;
    }

    private void openPortal(Plant plant) {
        double width = plant.definicao.getLargura();
        double height = plant.definicao.getAltura();
        var ring = new PlantPortal.Ring(width, height);
        plant.portalEntity = entityBuilder().at(plant.entity.getX(), plant.entity.getY())
                .view(ring).buildAndAttach();
        BoardProjection.place(plant.portalEntity, width, height, plant.row, 0);
        plant.portalEntity.setZIndex(plant.row * 10 + 1);
        var art = plant.entity.getViewComponent().getChildren().getLast();
        plant.portal = new PlantPortal(ring, art, width, height);
        selectPortal(false);
    }

    private boolean tryCollectSun(double mx, double my) {
        for (Sun s : new ArrayList<>(suns)) {
            Entity e = s.entity;
            double cx = e.getX() + 27;
            double cy = e.getY() + 27;
            double dx = mx - cx;
            double dy = my - cy;

            if (dx * dx + dy * dy <= 42 * 42) {
                sun += s.amount;
                play("sun-pick.wav");
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

        Planta def = plantaSelecionada;
        if (hasPlant[row][col] || sun < def.getCustoSol()) {
            return;
        }

        plantAt(row, col, def);
        sun -= def.getCustoSol();
        play("seed-plant.wav");
        updateInterface();
    }

    private void showMenu() {
        if (gardenTransition != null) gardenTransition.cancel();
        ui.root.setDisable(false);
        state = State.MENU;
        clearWorld();

        hoverHighlight.setVisible(false);
        drawLawn();
        ui.showMenu();
    }

    private void togglePause() {
        if (state == State.PLAYING) pauseGame();
        else if (state == State.PAUSED) resumeGame();
    }

    private void pauseGame() {
        if (state != State.PLAYING) return;
        if (portalSelected) selectPortal(false);
        state = State.PAUSED;
        hoverHighlight.setVisible(false);
        ui.showPause();
    }

    private void resumeGame() {
        if (state != State.PAUSED) return;
        ui.hidePause();
        state = State.PLAYING;
    }

    private void startGame() {
        if (ui == null || (state != State.MENU && state != State.GAME_OVER)) return;
        state = State.ENTERING_GARDEN;
        hoverHighlight.setVisible(false);
        ui.root.setDisable(true);
        gardenTransition.play(this::prepareGame, () -> {
            ui.root.setDisable(false);
            state = State.PLAYING;
            if (!getPrimaryStage().isFocused()) pauseGame();
        });
    }

    private void prepareGame() {
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
        selectPlant(plantaPadrao);

        drawLawn();
        updateInterface();
    }

    private void drawLawn() {
        entityBuilder()
                .view(new LawnView(false))
                .zIndex(-100)
                .buildAndAttach();
    }

    private void clearWorld() {
        if (portalSelected) selectPortal(false);
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

    private void plantAt(int row, int col, Planta def) {
        double x = GRID_X + col * CELL_W + CELL_W / 2 - def.getLargura() / 2;
        double y = GRID_Y + row * CELL_H + CELL_H / 2 - def.getAltura() / 2;

        var view = def.criarVista();
        Entity plant = entityBuilder()
                .at(x, y)
                .view(groundShadow(def.getLargura(), def.getAltura()))
                .view(view)
                .buildAndAttach();

        plantGrid[row][col] = new Plant(def, plant, row, col,
                view instanceof VistaAnimada animation ? animation : null);
        shootTimers[row][col] = 0;
        hasPlant[row][col] = true;
        BoardProjection.place(plant, def.getLargura(), def.getAltura(), row, 0);
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
        return zombiePadrao.getVelocidade() + Math.min(25, elapsed * 0.3);
    }

    private int zombieHp() {
        return zombiePadrao.getVidaMax() + (int) (elapsed / 45);
    }

    private void updateHover() {
        if (portalSelected) {
            Plant target = portalTarget(getInput().getMouseXUI(), getInput().getMouseYUI());
            hoverHighlight.setVisible(target != null);
            if (target != null) {
                BoardProjection.cell(hoverHighlight, target.row, target.col);
                hoverHighlight.setStroke(Color.web("#53d9ff"));
                hoverHighlight.setFill(Color.web("#53d9ff30"));
            }
            return;
        }
        var ground = BoardProjection.world(getInput().getMouseXUI(), getInput().getMouseYUI());
        int col = (int) Math.floor((ground.getX() - GRID_X) / CELL_W);
        int row = (int) Math.floor((ground.getY() - GRID_Y) / CELL_H);
        if (row < 0 || row >= ROWS || col < 0 || col >= COLS) {
            hoverHighlight.setVisible(false);
            return;
        }
        BoardProjection.cell(hoverHighlight, row, col);
        boolean available = !hasPlant[row][col] && sun >= plantaSelecionada.getCustoSol();
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
                if (plant.portal != null) {
                    if (plant.portal.advance(tpf)) {
                        getGameWorld().removeEntity(plant.portalEntity);
                        removePlant(plant);
                    }
                    continue;
                }
                boolean burstInProgress = plant.burstShotsRemaining > 0;
                if (plant.animation != null) {
                    plant.animation.advance(tpf);
                }
                updateSunProduction(plant, tpf);
                if (burstInProgress) {
                    updateBurst(plant, tpf);
                    continue;
                }
                if (plant.burstShotsRemaining > 0) {
                    continue;
                }
                if (plant.definicao.getAlcanceCorpoACorpo() > 0) {
                    updateMelee(plant, tpf);
                    continue;
                }
                if (!plant.definicao.podeDisparar()) {
                    continue;
                }

                shootTimers[row][col] += tpf;
                if (plant.shotWindup >= 0) {
                    plant.shotWindup -= tpf;
                    if (plant.shotWindup <= 0) {
                        startBurst(plant, Math.max(0, -plant.shotWindup));
                        plant.shotWindup = -1;
                    }
                }
                if (!plant.preparingShot && plant.shotWindup < 0 && plant.burstShotsRemaining == 0
                        && shootTimers[row][col] >= plant.definicao.getIntervaloTiro() + plant.extraShotCooldown
                        && hasShotTarget(plant)) {
                    shootTimers[row][col] = 0;
                    plant.extraShotCooldown = 0;
                    if (plant.animation != null && plant.animation.getMomentoDisparo() >= 0) {
                        plant.preparingShot = true;
                        plant.animation.atacar(age -> {
                            plant.preparingShot = false;
                            startBurst(plant, age);
                        });
                    } else {
                        if (plant.animation != null) plant.animation.atacar();
                        if (plant.definicao.getAtrasoDisparo() > 0) plant.shotWindup = plant.definicao.getAtrasoDisparo();
                        else startBurst(plant, 0);
                    }
                }
            }
        }
    }

    private void startBurst(Plant plant, double birthAge) {
        shoot(plant, birthAge);
        plant.burstShotsRemaining = Math.max(0, plant.definicao.getTirosPorRajada() - 1);
        if (plant.burstShotsRemaining > 0) {
            plant.burstShotTimer = Math.max(0, plant.definicao.getIntervaloEntreTiros());
            updateBurst(plant, birthAge);
        }
    }

    private void updateBurst(Plant plant, double seconds) {
        plant.burstShotTimer -= seconds;
        while (plant.burstShotsRemaining > 0 && plant.burstShotTimer <= 0) {
            shoot(plant, -plant.burstShotTimer);
            plant.burstShotsRemaining--;
            plant.burstShotTimer += Math.max(0, plant.definicao.getIntervaloEntreTiros());
        }
        if (plant.burstShotsRemaining == 0) {
            shootTimers[plant.row][plant.col] = 0;
        }
    }

    private void updateMelee(Plant plant, double tpf) {
        double interval = plant.definicao.getIntervaloTiro();
        shootTimers[plant.row][plant.col] = Math.min(interval, shootTimers[plant.row][plant.col] + tpf);
        if (plant.attackTarget != null) {
            plant.impactTimer -= tpf;
            if (plant.impactTimer <= 0) finishMelee(plant);
        }
        if (plant.attackTarget == null && shootTimers[plant.row][plant.col] >= interval) {
            Zombie target = null;
            for (Zombie zombie : zombies) {
                if (inMeleeRange(plant, zombie)
                        && (target == null || zombie.entity.getX() < target.entity.getX())) target = zombie;
            }
            if (target != null) {
                shootTimers[plant.row][plant.col] = 0;
                plant.attackTarget = target;
                plant.impactTimer = plant.definicao.getAtrasoImpacto();
                if (plant.animation != null) plant.animation.atacar();
                if (plant.impactTimer <= 0) finishMelee(plant);
            }
        }
    }

    private boolean inMeleeRange(Plant plant, Zombie zombie) {
        if (zombie.row != plant.row) return false;
        double plantRight = plant.entity.getX() + plant.definicao.getLargura();
        double zombieCenter = zombie.entity.getX() + zombie.definicao.getLargura() / 2;
        return zombieCenter >= plant.entity.getX() + plant.definicao.getLargura() / 2
                && zombie.entity.getX() - plantRight <= plant.definicao.getAlcanceCorpoACorpo();
    }

    private void finishMelee(Plant plant) {
        Zombie target = plant.attackTarget;
        plant.attackTarget = null;
        // A swing can miss if the target leaves reach or another plant defeats it.
        if (zombies.contains(target) && inMeleeRange(plant, target)) damageZombie(target, plant.definicao.getDano());
    }

    private boolean hasZombieInRow(int row) {
        for (Zombie zombie : zombies) {
            if (zombie.row == row) {
                return true;
            }
        }
        return false;
    }

    private boolean hasShotTarget(Plant plant) {
        return plant.definicao.usaTrajetoriaEmArco() ? findArcTarget(plant) != null : hasZombieInRow(plant.row);
    }

    private Zombie findArcTarget(Plant plant) {
        Zombie nearest = null;
        double nearestX = Double.POSITIVE_INFINITY;
        double plantX = plant.entity.getX();
        for (Zombie zombie : zombies) {
            double center = zombie.entity.getX() + zombie.definicao.getLargura() / 2;
            if (zombie.row == plant.row && center >= plantX && center < nearestX) {
                nearest = zombie;
                nearestX = center;
            }
        }
        return nearest;
    }

    private void shoot(Plant plant, double birthAge) {
        Zombie target = plant.definicao.usaTrajetoriaEmArco() ? findArcTarget(plant) : null;
        if (plant.definicao.usaTrajetoriaEmArco() && target == null) return;
        Entity e = plant.entity;
        var muzzle = plant.animation == null ? null : plant.animation.getSaidaProjetil();
        double x = e.getX() + (muzzle == null ? plant.definicao.getSaidaProjetilX() : muzzle.getX()) - PROJECTILE_SIZE / 2.0;
        double y = GRID_Y + plant.row * CELL_H + CELL_H / 2 - PROJECTILE_SIZE / 2.0;
        var view = plant.definicao.criarVistaProjetil(PROJECTILE_SIZE);

        Entity bullet = entityBuilder()
                .at(x, y)
                .viewWithBBox(view)
                .buildAndAttach();

        double lift = muzzle == null ? plant.definicao.getAlturaProjetil()
                : plant.definicao.getAltura() - muzzle.getY() - PROJECTILE_SIZE / 2.0;
        int damage = target == null ? plant.definicao.getDano() : plant.definicao.getDanoProjetil(target.maxHp);
        Bullet shot = new Bullet(bullet, plant.row, plant.definicao.getVelocidadeProjetil(),
                damage, lift, view instanceof VistaAnimada animation ? animation : null, birthAge);
        if (target != null) {
            shot.arc = new ArcFlight(target, x, shot.speed);
            plant.extraShotCooldown = shot.arc.extraCooldown();
            if (plant.animation != null) plant.animation.prolongarRecarga(plant.extraShotCooldown);
        }
        bullets.add(shot);
        BoardProjection.place(bullet, PROJECTILE_SIZE, PROJECTILE_SIZE, plant.row, lift);
    }

    private void updateBullets(double tpf) {
        for (Bullet bullet : new ArrayList<>(bullets)) {
            double step = bullet.firstStep < 0 ? tpf : Math.min(tpf, bullet.firstStep);
            bullet.firstStep = -1;
            if (bullet.animation != null) bullet.animation.advance(step);
            if (bullet.arc != null) {
                updateArc(bullet, step);
                continue;
            }
            bullet.entity.setX(bullet.entity.getX() + bullet.speed * step);
            BoardProjection.place(bullet.entity, PROJECTILE_SIZE, PROJECTILE_SIZE, bullet.row, bullet.lift);
            if (bullet.entity.getX() > WIDTH) {
                removeBullet(bullet);
            }
        }
    }

    private void updateArc(Bullet bullet, double step) {
        ArcFlight arc = bullet.arc;
        arc.elapsed += step;
        double progress = Math.min(1, arc.elapsed / arc.duration);
        double landingLift = arc.target.definicao.getAltura() * 0.55;
        double lift = bullet.lift + (landingLift - bullet.lift) * progress
                + 92 * 4 * progress * (1 - progress);
        bullet.entity.setX(arc.startX + (arc.targetX() - arc.startX) * progress);
        BoardProjection.place(bullet.entity, PROJECTILE_SIZE, PROJECTILE_SIZE, bullet.row, lift);
    }

    private void spawnZombie() {
        ZombieNormal def = zombiePadrao;
        int row = random.nextInt(ROWS);
        double x = WIDTH - def.getLargura();
        double y = GRID_Y + row * CELL_H + CELL_H / 2 - def.getAltura() / 2;

        Entity zombieEntity = entityBuilder()
                .at(x, y)
                .viewWithBBox(texture(def.getTextura(), def.getLargura(), def.getAltura()))
                .buildAndAttach();

        zombies.add(new Zombie(def, zombieEntity, row, zombieHp(), zombieSpeed()));
        BoardProjection.place(zombieEntity, def.getLargura(), def.getAltura(), row, 0);
    }

    private void updateZombies(double tpf) {
        for (Zombie zombie : new ArrayList<>(zombies)) {
            Entity e = zombie.entity;
            Plant blocking = findBlockingPlant(zombie);

            if (blocking != null) {
                blocking.hp -= zombie.definicao.getDanoPorSegundo() * tpf;
                if (blocking.hp <= 0) {
                    removePlant(blocking);
                }
            } else {
                e.setX(e.getX() - zombie.speed * tpf);
            }

            BoardProjection.place(e, zombie.definicao.getLargura(), zombie.definicao.getAltura(), zombie.row, 0);
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
        double zRight = e.getX() + zombie.definicao.getLargura();

        for (int col = 0; col < COLS; col++) {
            Plant plant = plantGrid[zombie.row][col];
            if (plant == null || plant.portal != null) {
                continue;
            }

            double pLeft = plant.entity.getX();
            double pRight = pLeft + plant.definicao.getLargura();

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

    private void updateSunProduction(Plant plant, double seconds) {
        int amount = plant.definicao.getSolProduzido();
        if (amount <= 0) return;
        double interval = plant.definicao.getIntervaloProducaoSol();
        if (interval <= 0 || !Double.isFinite(interval)) return;

        plant.sunProductionTimer += seconds;
        if (plant.sunProductionTimer < interval) return;
        plant.sunProductionTimer %= interval;

        Runnable emit = () -> tossProducedSun(plant, amount);
        if (plant.animation == null) emit.run();
        else plant.animation.produzirSol(emit);
    }

    private void tossProducedSun(Plant plant, int amount) {
        double worldX = GRID_X + plant.col * CELL_W + CELL_W * 0.75;
        double worldY = GRID_Y + plant.row * CELL_H + CELL_H * 0.8;
        var landing = BoardProjection.screen(worldX, worldY);
        double x = landing.getX() - 27;
        double y = landing.getY() - 27;
        double startX = x - 12;
        double startY = y - plant.definicao.getAltura() * BoardProjection.scaleAt(worldY) * 0.8;
        Sun produced = createSun(startX, startY, y, amount);
        produced.toss = new SunToss(startX, startY, x);
        produced.entity.setZIndex(100);
    }

    private void spawnSun() {
        double x = GRID_X + random.nextDouble() * (COLS * CELL_W - 40);
        double y = -10;
        double groundY = GRID_Y + random.nextDouble() * (ROWS * CELL_H - 50);
        var landing = BoardProjection.screen(x + 27, groundY + 27);
        x = landing.getX() - 27;
        double targetY = landing.getY() - 27;

        createSun(x, y, targetY, SUN_AMOUNT);
    }

    private Sun createSun(double x, double y, double targetY, int amount) {
        Entity sunEntity = entityBuilder()
                .at(x, y)
                .viewWithBBox(texture("Sun.png", 54, 54))
                .buildAndAttach();

        Sun created = new Sun(sunEntity, targetY, amount);
        suns.add(created);
        return created;
    }

    private void updateSuns(double tpf) {
        for (Sun s : new ArrayList<>(suns)) {
            Entity e = s.entity;

            s.life -= tpf;
            if (s.toss != null) {
                var toss = s.toss;
                double progress = Math.min(1, (Sun.LIFETIME - s.life) / 0.46);
                e.setX(toss.x() + (toss.targetX() - toss.x()) * progress);
                e.setY(toss.y() + (s.targetY - toss.y()) * progress - 80 * progress * (1 - progress));
                if (progress >= 1) s.toss = null;
            } else if (e.getY() < s.targetY) {
                e.setY(Math.min(s.targetY, e.getY() + 45 * tpf));
            }

            if (s.life <= 0) {
                removeSun(s);
            }
        }
    }

    private void checkBulletHits() {
        for (Bullet bullet : new ArrayList<>(bullets)) {
            if (bullet.arc != null) {
                if (bullet.arc.elapsed >= bullet.arc.duration) {
                    removeBullet(bullet);
                    damageZombie(bullet.arc.target, bullet.dano);
                }
                continue;
            }
            Entity b = bullet.entity;

            for (Zombie zombie : new ArrayList<>(zombies)) {
                if (zombie.row != bullet.row) {
                    continue;
                }

                Entity z = zombie.entity;
                boolean sameRow = Math.abs(b.getY() - (z.getY() + zombie.definicao.getAltura() / 2)) < 30;
                boolean touching = Math.abs(b.getX() - (z.getX() + zombie.definicao.getLargura() / 2)) < 32;

                if (sameRow && touching) {
                    removeBullet(bullet);
                    damageZombie(zombie, bullet.dano);
                    break;
                }
            }
        }
    }

    private void damageZombie(Zombie zombie, int damage) {
        if (!zombies.contains(zombie)) return;
        zombie.hp -= damage;
        if (zombie.hp <= 0) {
            removeZombie(zombie);
            score += 10;
            updateInterface();
        } else {
            double ratio = (double) zombie.hp / zombie.maxHp;
            zombie.entity.setOpacity(0.45 + 0.55 * ratio);
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
        if (portalSelected) selectPortal(false);
        state = State.GAME_OVER;
        hoverHighlight.setVisible(false);

        ui.showResult(score);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
