package pt.escola.gardendefense;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.UserAction;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.Node;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.util.Duration;

import pt.escola.gardendefense.combate.AlvoPlanta;
import pt.escola.gardendefense.combate.Projetil;
import pt.escola.gardendefense.combate.ProjetilConfig;
import pt.escola.gardendefense.personagens.plants.CatalogoPlantas;
import pt.escola.gardendefense.personagens.plants.Planta;
import pt.escola.gardendefense.personagens.plants.SpriteSheetView;
import pt.escola.gardendefense.personagens.zombies.CatalogoZombies;
import pt.escola.gardendefense.personagens.zombies.ContextoZombie;
import pt.escola.gardendefense.personagens.zombies.Zombie;

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
    private final List<Zombie> moldesZombies = CatalogoZombies.carregar();

    private State state = State.MENU;

    private int sun = 150;
    private int score = 0;
    private int lives = 3;
    private double elapsed = 0;

    private final boolean[][] hasPlant = new boolean[ROWS][COLS];
    private final Plant[][] plantGrid = new Plant[ROWS][COLS];
    private final double[][] shootTimers = new double[ROWS][COLS];

    private final List<ActiveZombie> zombies = new ArrayList<>();
    private final List<Projetil> projeteis = new ArrayList<>();
    private final List<Sun> suns = new ArrayList<>();

    private double spawnTimer = 0;
    private double sunTimer = 0;

    private GardenUI ui;
    private GardenTransition gardenTransition;
    private Polygon hoverHighlight;

    private static class Plant {
        final Planta definicao;
        final SpriteSheetView animation;
        Entity entity;
        final int row;
        final int col;
        double hp;

        Plant(Planta definicao, Entity entity, int row, int col, SpriteSheetView animation) {
            this.definicao = definicao;
            this.animation = animation;
            this.hp = definicao.getVidaMax();
            this.entity = entity;
            this.row = row;
            this.col = col;
        }
    }

    private static class ActiveZombie {
        final Zombie definicao;
        Entity entity;
        int row;
        int hp;
        final int maxHp;
        final pt.escola.gardendefense.personagens.zombies.SpriteSheetView animation;
        double speed;
        double shootTimer;
        double inclinacao;

        ActiveZombie(Zombie definicao, Entity entity, int row, int hp, double speed, Node view) {
            this.definicao = definicao;
            this.entity = entity;
            this.row = row;
            this.hp = hp;
            this.maxHp = hp;
            this.animation = view instanceof pt.escola.gardendefense.personagens.zombies.SpriteSheetView sprite
                    ? sprite : null;
            this.speed = speed;
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
                togglePause();
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
        ui.showSelectedPlant(plant);
    }

    private void handleClick() {
        double mx = getInput().getMouseXUI();
        double my = getInput().getMouseYUI();

        if (state != State.PLAYING) {
            return;
        }

        // Seed-tray clicks select a template; they must not collect a sun behind the HUD.
        if (my < 100) return;

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
        for (Entity e : new ArrayList<>(getGameWorld().getEntities())) {
            getGameWorld().removeEntity(e);
        }
        zombies.clear();
        projeteis.clear();
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
                view instanceof SpriteSheetView animation ? animation : null);
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
        updateProjeteis(tpf);
        updateZombies(tpf);
        if (state != State.PLAYING) return;
        updateSuns(tpf);
        checkProjeteis();
    }

    private double spawnInterval() {
        return Math.max(1.5, 4.5 - elapsed * 0.04);
    }

    private double zombieSpeed(Zombie def) {
        return def.getVelocidade() + Math.min(25, elapsed * 0.3);
    }

    private int zombieHp(Zombie def) {
        return def.getVidaMax() + (int) (elapsed / 45);
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
                if (plant.animation != null) {
                    plant.animation.advance(tpf);
                }
                if (!plant.definicao.podeDisparar()) {
                    continue;
                }

                shootTimers[row][col] += tpf;
                if (shootTimers[row][col] >= plant.definicao.getIntervaloTiro() && hasZombieInRow(row)) {
                    shootTimers[row][col] = 0;
                    shoot(plant);
                }
            }
        }
    }

    private boolean hasZombieInRow(int row) {
        for (ActiveZombie zombie : zombies) {
            if (zombie.row == row) {
                return true;
            }
        }
        return false;
    }

    private void shoot(Plant plant) {
        Entity e = plant.entity;
        double x = e.getX() + plant.definicao.getLargura() - 8;
        double y = e.getY() + 16;

        lancarProjetil(plant.definicao.criarVistaProjetil(PROJECTILE_SIZE), plant.row,
                plant.definicao.getVelocidadeProjetil(), plant.definicao.getDano(),
                plant.definicao.getAlturaProjetil(), PROJECTILE_SIZE, 1, x, y);
    }

    /**
     * Cria o projétil e coloca-o na linha indicada.
     * direcao 1 viaja para a direita (plantas); -1 viaja para a esquerda (zombies).
     */
    private void lancarProjetil(javafx.scene.Node view, int linha, double velocidade, int dano,
                                double lift, double tamanho, int direcao, double x, double y) {
        Entity entity = entityBuilder()
                .at(x, y)
                .viewWithBBox(view)
                .buildAndAttach();

        projeteis.add(new Projetil(entity, linha, dano, velocidade, lift, tamanho, direcao));
        BoardProjection.place(entity, tamanho, tamanho, linha, lift);
    }

    private void updateProjeteis(double tpf) {
        for (Projetil projetil : new ArrayList<>(projeteis)) {
            projetil.entity.setX(projetil.entity.getX() + projetil.direcao * projetil.velocidade * tpf);
            BoardProjection.place(projetil.entity, projetil.tamanho, projetil.tamanho,
                    projetil.linha, projetil.lift);
            if (projetil.entity.getX() > WIDTH || projetil.entity.getX() + projetil.tamanho < 0) {
                removerProjetil(projetil);
            }
        }
    }

    private void spawnZombie() {
        Zombie molde = sortearZombie();
        if (molde == null) {
            return;
        }
        Zombie def = CatalogoZombies.novaInstancia(molde);
        int row = random.nextInt(ROWS);
        double x = WIDTH - def.getLargura();
        double y = GRID_Y + row * CELL_H + CELL_H / 2 - def.getAltura() / 2;

        Node view = def.criarVista();
        Entity zombieEntity = entityBuilder()
                .at(x, y)
                .viewWithBBox(view)
                .buildAndAttach();

        zombies.add(new ActiveZombie(def, zombieEntity, row, zombieHp(def), zombieSpeed(def), view));
        BoardProjection.place(zombieEntity, def.getLargura(), def.getAltura(), row, 0);
    }

    /** Escolhe um molde de acordo com o peso declarado em Zombie.getPesoSpawn(). */
    private Zombie sortearZombie() {
        int total = 0;
        for (Zombie molde : moldesZombies) {
            total += Math.max(0, molde.getPesoSpawn());
        }
        if (total <= 0) {
            return null;
        }

        int sorteio = random.nextInt(total);
        for (Zombie molde : moldesZombies) {
            sorteio -= Math.max(0, molde.getPesoSpawn());
            if (sorteio < 0) {
                return molde;
            }
        }
        return moldesZombies.getLast();
    }

    private void updateZombies(double tpf) {
        for (ActiveZombie zombie : new ArrayList<>(zombies)) {
            if (!zombies.contains(zombie)) {
                continue;
            }
            if (zombie.animation != null) {
                zombie.animation.advance(tpf);
            }

            // Habilidade especial do tipo; pode mudar a linha, a vida, a velocidade, etc.
            zombie.definicao.atualizar(new ContextoDoZombie(zombie), tpf);
            if (!zombies.contains(zombie)) {
                continue;
            }

            dispararSeTiverArma(zombie, tpf);

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

            BoardProjection.place(e, zombie.definicao.getLargura(), zombie.definicao.getAltura(),
                    zombie.row, 0, zombie.inclinacao);
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

    /** Dispara a arma declarada em Zombie.getArma() quando há planta na frente. */
    private void dispararSeTiverArma(ActiveZombie zombie, double tpf) {
        ProjetilConfig arma = zombie.definicao.getArma();
        if (arma == null) {
            return;
        }
        if (!plantaAFrente(zombie)) {
            zombie.shootTimer = 0;
            return;
        }

        zombie.shootTimer += tpf;
        if (zombie.shootTimer < arma.intervalo()) {
            return;
        }
        zombie.shootTimer = 0;

        Entity e = zombie.entity;
        double y = e.getY() + (zombie.definicao.getAltura() - arma.tamanho()) / 2;
        lancarProjetil(zombie.definicao.criarVistaProjetil(arma.tamanho()), zombie.row,
                arma.velocidade(), arma.dano(), arma.lift(), arma.tamanho(), -1, e.getX(), y);
    }

    /** Verdadeiro se a linha do zombie tem uma planta ainda à sua esquerda. */
    private boolean plantaAFrente(ActiveZombie zombie) {
        for (int col = 0; col < COLS; col++) {
            Plant plant = plantGrid[zombie.row][col];
            if (plant != null && plant.entity.getX() < zombie.entity.getX()) {
                return true;
            }
        }
        return false;
    }

    /** Contexto entregue a Zombie.atualizar(): o mundo à volta do próprio zombie. */
    private final class ContextoDoZombie implements ContextoZombie {
        private final ActiveZombie zombie;

        private ContextoDoZombie(ActiveZombie zombie) {
            this.zombie = zombie;
        }

        @Override
        public Entity entidade() {
            return zombie.entity;
        }

        @Override
        public int linha() {
            return zombie.row;
        }

        @Override
        public boolean mudarLinha(int linha) {
            if (linha < 0 || linha >= ROWS) {
                return false;
            }
            if (linha == zombie.row) {
                return true;
            }
            zombie.row = linha;
            BoardProjection.place(zombie.entity, zombie.definicao.getLargura(),
                    zombie.definicao.getAltura(), linha, 0, zombie.inclinacao);
            return true;
        }

        @Override
        public double velocidade() {
            return zombie.speed;
        }

        @Override
        public void velocidade(double pxPorSegundo) {
            zombie.speed = pxPorSegundo;
        }

        @Override
        public double vida() {
            return zombie.hp;
        }

        @Override
        public void vida(double vida) {
            zombie.hp = (int) Math.ceil(vida);
            if (zombie.hp <= 0 && zombies.contains(zombie)) {
                removeZombie(zombie);
                score += 10;
                updateInterface();
            }
        }

        @Override
        public List<Projetil> projeteisInimigos() {
            List<Projetil> proximos = new ArrayList<>();
            double frenteDoZombie = zombie.entity.getX();
            for (Projetil projetil : projeteis) {
                if (projetil.paraDireita() && projetil.linha == zombie.row
                        && projetil.entity.getX() < frenteDoZombie) {
                    proximos.add(projetil);
                }
            }
            return List.copyOf(proximos);
        }

        @Override
        public List<AlvoPlanta> plantasNaLinha() {
            List<AlvoPlanta> alvos = new ArrayList<>();
            for (int col = 0; col < COLS; col++) {
                Plant plant = plantGrid[zombie.row][col];
                if (plant != null) {
                    alvos.add(alvoDe(plant));
                }
            }
            return List.copyOf(alvos);
        }

        @Override
        public List<AlvoPlanta> plantasNaArea(int raioLinhas, int raioColunas) {
            List<AlvoPlanta> alvos = new ArrayList<>();
            int col = colunaDoZombie();
            int linhaInicial = Math.max(0, zombie.row - raioLinhas);
            int linhaFinal = Math.min(ROWS - 1, zombie.row + raioLinhas);
            int colInicial = Math.max(0, col - raioColunas);
            int colFinal = Math.min(COLS - 1, col + raioColunas);

            for (int linha = linhaInicial; linha <= linhaFinal; linha++) {
                for (int c = colInicial; c <= colFinal; c++) {
                    Plant plant = plantGrid[linha][c];
                    if (plant != null) {
                        alvos.add(alvoDe(plant));
                    }
                }
            }
            return List.copyOf(alvos);
        }

        /** Coluna lógica do tabuleiro onde o zombie está de pé. */
        private int colunaDoZombie() {
            double centro = zombie.entity.getX() + zombie.definicao.getLargura() / 2;
            int col = (int) Math.floor((centro - GRID_X) / CELL_W);
            return Math.max(0, Math.min(COLS - 1, col));
        }

        @Override
        public void ondaDeChoque(double raioMax, double duracao) {
            double groundY = BoardProjection.chao(zombie.row);
            double centroX = zombie.entity.getX() + zombie.definicao.getLargura() / 2;
            Point2D ecrã = BoardProjection.screen(centroX, groundY);
            double escala = BoardProjection.scaleAt(groundY);

            Circle anel = new Circle(0);
            anel.setFill(null);
            anel.setStroke(Color.web("#ffb347"));
            anel.setStrokeWidth(8);

            Entity efeito = entityBuilder()
                    .at(ecrã.getX(), ecrã.getY())
                    .view(anel)
                    .zIndex(1000)
                    .buildAndAttach();

            Timeline onda = new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(anel.radiusProperty(), 0),
                            new KeyValue(anel.opacityProperty(), 1)),
                    new KeyFrame(Duration.seconds(Math.max(0.05, duracao)),
                            new KeyValue(anel.radiusProperty(), raioMax * escala),
                            new KeyValue(anel.opacityProperty(), 0)));
            onda.setOnFinished(ev -> getGameWorld().removeEntity(efeito));
            onda.play();
        }

        @Override
        public int bloquearProjeteis(double raioMax) {
            int bloqueados = 0;
            double centroX = zombie.entity.getX() + zombie.definicao.getLargura() / 2;
            double centroY = zombie.entity.getY() + zombie.definicao.getAltura() / 2;
            double raio2 = raioMax * raioMax;

            for (Projetil projetil : new ArrayList<>(projeteis)) {
                if (!projetil.paraDireita()) {
                    continue;
                }
                double dx = projetil.entity.getX() + projetil.tamanho / 2 - centroX;
                double dy = projetil.entity.getY() + projetil.tamanho / 2 - centroY;
                if (dx * dx + dy * dy <= raio2) {
                    removerProjetil(projetil);
                    bloqueados++;
                }
            }
            return bloqueados;
        }

        @Override
        public void danificar(AlvoPlanta alvo, int dano) {
            if (alvo == null || alvo.linha() < 0 || alvo.linha() >= ROWS
                    || alvo.coluna() < 0 || alvo.coluna() >= COLS) {
                return;
            }
            Plant plant = plantGrid[alvo.linha()][alvo.coluna()];
            if (plant == null) {
                return;
            }
            plant.hp -= dano;
            if (plant.hp <= 0) {
                removePlant(plant);
            }
        }

        @Override
        public void lancar(ProjetilConfig arma, int linha) {
            if (arma == null || linha < 0 || linha >= ROWS) {
                return;
            }
            Entity e = zombie.entity;
            double y = e.getY() + (zombie.definicao.getAltura() - arma.tamanho()) / 2;
            lancarProjetil(zombie.definicao.criarVistaProjetil(arma.tamanho()), linha,
                    arma.velocidade(), arma.dano(), arma.lift(), arma.tamanho(), -1, e.getX(), y);
        }

        @Override
        public double tempoDecorrido() {
            return elapsed;
        }

        @Override
        public void inclinar(double graus) {
            zombie.inclinacao = graus;
        }
    }

    private AlvoPlanta alvoDe(Plant plant) {
        return new AlvoPlanta(plant.row, plant.col, plant.entity.getX(),
                plant.definicao.getLargura(), plant.definicao.getAltura(),
                plant.hp, plant.definicao.getVidaMax());
    }

    private Plant findBlockingPlant(ActiveZombie zombie) {
        Entity e = zombie.entity;
        double zLeft = e.getX();
        double zRight = e.getX() + zombie.definicao.getLargura();

        for (int col = 0; col < COLS; col++) {
            Plant plant = plantGrid[zombie.row][col];
            if (plant == null) {
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

    private void checkProjeteis() {
        for (Projetil projetil : new ArrayList<>(projeteis)) {
            if (!projeteis.contains(projetil)) {
                continue;
            }
            if (projetil.paraDireita()) {
                atingirZombie(projetil);
            } else {
                atingirPlanta(projetil);
            }
        }
    }

    private void atingirZombie(Projetil projetil) {
        Entity b = projetil.entity;

        for (ActiveZombie zombie : new ArrayList<>(zombies)) {
            if (zombie.row != projetil.linha) {
                continue;
            }

            Entity z = zombie.entity;
            boolean sameRow = Math.abs(b.getY() - (z.getY() + zombie.definicao.getAltura() / 2)) < 30;
            boolean touching = Math.abs(b.getX() - (z.getX() + zombie.definicao.getLargura() / 2)) < 32;

            if (sameRow && touching) {
                removerProjetil(projetil);
                zombie.hp -= projetil.dano;

                if (zombie.hp <= 0) {
                    removeZombie(zombie);
                    score += 10;
                    updateInterface();
                } else {
                    double ratio = (double) zombie.hp / zombie.maxHp;
                    z.setOpacity(0.45 + 0.55 * ratio);
                }
                return;
            }
        }
    }

    private void atingirPlanta(Projetil projetil) {
        Entity b = projetil.entity;
        if (projetil.linha < 0 || projetil.linha >= ROWS) {
            return;
        }

        for (int col = 0; col < COLS; col++) {
            Plant plant = plantGrid[projetil.linha][col];
            if (plant == null) {
                continue;
            }

            Entity p = plant.entity;
            boolean touching = Math.abs(b.getX() - (p.getX() + plant.definicao.getLargura() / 2)) < 32;
            if (!touching) {
                continue;
            }

            removerProjetil(projetil);
            plant.hp -= projetil.dano;
            if (plant.hp <= 0) {
                removePlant(plant);
            }
            return;
        }
    }

    private void removerProjetil(Projetil projetil) {
        getGameWorld().removeEntity(projetil.entity);
        projeteis.remove(projetil);
    }

    private void removeZombie(ActiveZombie zombie) {
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
