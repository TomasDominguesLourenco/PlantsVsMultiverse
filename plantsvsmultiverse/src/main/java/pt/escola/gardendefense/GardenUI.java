package pt.escola.gardendefense;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.AccessibleRole;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import static com.almasb.fxgl.dsl.FXGL.texture;
import static com.almasb.fxgl.dsl.FXGL.getSettings;
import static com.almasb.fxgl.dsl.FXGL.getPrimaryStage;
import static com.almasb.fxgl.dsl.FXGL.play;

/** Illustrated menu props and a compact seed tray. No actor animation lives here. */
final class GardenUI {
    final Group root = new Group();
    private final Group menu = new Group();
    private final Group hud = new Group();
    private final Group result = new Group();
    private final Group help = new Group();
    private final Group pause = new Group();
    private final Slider musicVolume = new Slider(0, 100, getSettings().getGlobalMusicVolume() * 100);
    private final Slider soundVolume = new Slider(0, 100, getSettings().getGlobalSoundVolume() * 100);
    private final CheckBox muted = new CheckBox("Silenciar som");
    private final CheckBox fullScreen = new CheckBox("Ecrã inteiro");
    private final Text energy = label("150", 93, 54, 30, "#fff2bd", true);
    private final Text points = label("0000", 728, 43, 23, "#f5ebc6", true);
    private final Text hearts = label("● ● ●", 831, 44, 18, "#e8c46d", true);
    private final Text clock = label("00:00", 728, 64, 11, "#d3c69d", false);
    private final Text resultScore = label("", 387, 282, 23, "#3e4939", true);
    private final Text seedCost = label("50", 271, 68, 20, "#384b27", true);
    private final Rectangle seed;
    private ParallelTransition entrance;
    private int lastSun = -1, lastScore = -1, lastLives = -1, lastSecond = -1;

    GardenUI(Runnable play, Runnable home, Runnable togglePause, Runnable quit) {
        root.getStylesheets().add(java.util.Objects.requireNonNull(
                GardenUI.class.getResource("/assets/ui/garden.css")).toExternalForm());
        root.getChildren().addAll(hud, menu, result, pause);
        buildHud(togglePause);
        seed = new Rectangle(195, 20, 136, 63);
        seed.setArcWidth(4); seed.setArcHeight(4);
        seed.setStroke(Color.web("#3d4a2b")); seed.setStrokeWidth(2);
        seed.setFill(Color.web("#e9dba6"));
        var pea = texture("PeaShooter.png", 39, 45);
        pea.setTranslateX(204); pea.setTranslateY(30);
        hud.getChildren().addAll(seed, pea,
                label("ERVILHADORA", 249, 38, 9, "#526236", true), seedCost,
                label("SOL", 299, 65, 9, "#526236", true));

        menu.getChildren().add(new LawnView(true));
        Canvas props = new Canvas(1000, 600);
        var g = props.getGraphicsContext2D();
        stone(g, 620, 137, 307, 376);
        // A few cracks and moss patches are placed deliberately, not random noise.
        stroke(g, "#6b7766", 2, 668, 165, 678, 189, 674, 203, 687, 218);
        stroke(g, "#6b7766", 2, 911, 372, 894, 383, 897, 395, 882, 405);
        fill(g, "#6e8647", 613, 492, 649, 482, 676, 494, 711, 489, 735, 513, 612, 516);
        fill(g, "#698143", 883, 488, 915, 470, 935, 497, 929, 517, 876, 514);
        // The little garden marker belongs to the scene, not a UI card.
        fill(g, "#705235", 320, 464, 331, 462, 340, 554, 330, 556);
        fill(g, "#6b5034", 272, 473, 393, 460, 398, 497, 276, 508);
        fill(g, "#b48c52", 271, 469, 391, 456, 395, 492, 275, 504);
        stroke(g, "#d1b47b", 1.5, 279, 475, 384, 464);
        menu.getChildren().add(props);
        var marker = label("QUINTAL Nº 09", 284, 491, 12, "#382f20", true);
        marker.setRotate(-6);
        menu.getChildren().add(marker);

        Group logo = new Group();
        logo.getChildren().addAll(logo("PLANTS", 255, 100, 72, "#9ac653", -4),
                logo("vs.", 372, 141, 32, "#bb6b46", -7),
                logo("MULTIVERSE", 216, 196, 54, "#e6d7a3", -3));
        menu.getChildren().add(logo);
        menu.getChildren().add(label("DEFENDE O TEU QUINTAL", 283, 226, 12, "#445d35", true));
        Text epitaph = label("ENTRA POR TUA CONTA…", 660, 226, 14, "#56624e", true);
        epitaph.setRotate(-2);
        menu.getChildren().addAll(epitaph,
                plank("JOGAR", 587, 251, 350, 75, -3, true, play),
                plank("COMO JOGAR", 622, 347, 291, 54, 2, false, () -> help.setVisible(!help.isVisible())),
                plank("SAIR", 669, 420, 206, 45, -2, false, quit),
                label("ENTER para começar", 712, 490, 12, "#e4e4be", false));
        // Existing sprites at modest size; zombies are not animated or redesigned.
        for (int i = 0; i < 3; i++) {
            double x = 330 + i * 84, y = 366 + i % 2 * 33;
            var shadow = new javafx.scene.shape.Ellipse(x + 31, y + 73, 30, 7);
            shadow.setFill(Color.web("#355c3040"));
            var plant = texture("PeaShooter.png", 62, 73);
            plant.setTranslateX(x); plant.setTranslateY(y);
            menu.getChildren().addAll(shadow, plant);
        }
        buildHelp();
        menu.getChildren().add(help);

        Rectangle veil = new Rectangle(1000, 600, Color.web("#1f3028b8"));
        Canvas resultStone = new Canvas(1000, 600);
        stone(resultStone.getGraphicsContext2D(), 280, 123, 440, 368);
        result.getChildren().addAll(veil, resultStone,
                label("OS ZOMBIES PASSARAM!", 338, 221, 29, "#3e4939", true), resultScore,
                plank("JOGAR OUTRA VEZ", 322, 325, 357, 57, -1, true, play),
                plank("VOLTAR AO MENU", 342, 402, 318, 46, 1, false, home));
        buildPause(togglePause, home, quit);
        hud.setVisible(false); result.setVisible(false); help.setVisible(false); pause.setVisible(false);
    }

    private void buildHud(Runnable togglePause) {
        Canvas tray = new Canvas(1000, 94);
        var g = tray.getGraphicsContext2D();
        fill(g, "#4c3d28", 22, 15, 354, 14, 360, 87, 28, 91);
        fill(g, "#8c6b40", 26, 18, 349, 18, 354, 81, 29, 85);
        stroke(g, "#b39056", 2, 31, 23, 343, 23);
        stroke(g, "#614c30", 2, 33, 76, 345, 72);
        fill(g, "#3e422b", 38, 26, 179, 25, 177, 76, 39, 79);
        fill(g, "#735a37", 713, 15, 971, 19, 975, 80, 710, 80);
        stroke(g, "#a78a55", 2, 719, 20, 963, 24);
        var sun = texture("Sun.png", 43, 43);
        sun.setTranslateX(44); sun.setTranslateY(29);
        hud.getChildren().addAll(tray, sun, energy, points, clock, hearts,
                label("PONTOS", 729, 25, 9, "#d3c69d", true),
                label("VIDAS", 832, 25, 9, "#d3c69d", true),
                plank("PAUSA", 894, 29, 81, 40, -1, false, togglePause));
        Text hint = label("Planta por 50 sol  ·  Apanha os sóis  ·  ESC / P: pausa", 338, 590, 11, "#eee7bb", true);
        Rectangle hintBacking = new Rectangle(hint.getX() - 13, 575, hint.getLayoutBounds().getWidth() + 26, 23);
        hintBacking.setArcWidth(8); hintBacking.setArcHeight(8); hintBacking.setFill(Color.web("#405b32db"));
        hud.getChildren().addAll(hintBacking, hint);
    }

    private void buildPause(Runnable resume, Runnable home, Runnable quit) {
        Rectangle veil = new Rectangle(1000, 600, Color.web("#182d24b3"));
        Canvas backing = new Canvas(1000, 600);
        var g = backing.getGraphicsContext2D();
        fill(g, "#24322055", 281, 107, 719, 99, 731, 533, 283, 544);
        fill(g, "#584a2f", 278, 95, 714, 88, 724, 525, 280, 535);
        fill(g, "#ac8953", 284, 100, 708, 95, 716, 519, 286, 528);
        fill(g, "#ede1b6", 300, 120, 695, 115, 700, 506, 301, 515);
        stroke(g, "#c7b78a", 1.5, 315, 365, 684, 365);
        pause.getChildren().addAll(veil, backing,
                plank("PAUSA", 350, 69, 300, 72, -2, false, () -> {}),
                label("O jardim espera por ti.", 411, 156, 14, "#68704b", false),
                plank("CONTINUAR", 332, 175, 337, 56, -1, true, resume));
        // The title is decorative, not a keyboard focus stop or an empty action.
        var title = pause.getChildren().get(2);
        title.setMouseTransparent(true); title.setFocusTraversable(false);
        addVolumeRow("Música", musicVolume, 268);
        addVolumeRow("Efeitos", soundVolume, 315);
        muted.setLayoutX(321); muted.setLayoutY(334);
        fullScreen.setLayoutX(522); fullScreen.setLayoutY(334);
        fullScreen.setSelected(getPrimaryStage().isFullScreen());
        fullScreen.selectedProperty().addListener((observable, oldValue, value) -> getPrimaryStage().setFullScreen(value));
        getPrimaryStage().fullScreenProperty().addListener((observable, oldValue, value) -> fullScreen.setSelected(value));
        muted.selectedProperty().addListener((observable, oldValue, value) -> applyAudio());
        musicVolume.disableProperty().bind(muted.selectedProperty());
        soundVolume.disableProperty().bind(muted.selectedProperty());
        pause.getChildren().addAll(muted, fullScreen,
                plank("MENU PRINCIPAL", 313, 387, 231, 47, -1, false, home),
                plank("SAIR", 558, 387, 128, 47, 1, false, quit),
                label("Voltar ao menu termina a partida atual.", 361, 462, 12, "#7c7050", false),
                label("ESC, P ou ENTER para continuar", 376, 490, 12, "#52613b", true));
    }

    private void addVolumeRow(String caption, Slider slider, double baseline) {
        Text percent = label("", 651, baseline, 13, "#52613b", true);
        percent.textProperty().bind(slider.valueProperty().asString("%.0f%%"));
        slider.setLayoutX(412); slider.setLayoutY(baseline - 19); slider.setPrefWidth(223);
        slider.setBlockIncrement(5); slider.setAccessibleText("Volume de " + caption.toLowerCase());
        slider.setTooltip(new Tooltip("Volume de " + caption.toLowerCase()));
        slider.valueProperty().addListener((observable, oldValue, value) -> applyAudio());
        if (slider == soundVolume) slider.setOnMouseReleased(event -> play("ui-tap.wav"));
        pause.getChildren().addAll(label(caption, 323, baseline, 16, "#465638", true), slider, percent);
    }

    private void applyAudio() {
        getSettings().setGlobalMusicVolume(muted.isSelected() ? 0 : musicVolume.getValue() / 100);
        getSettings().setGlobalSoundVolume(muted.isSelected() ? 0 : soundVolume.getValue() / 100);
    }

    private void buildHelp() {
        Polygon paper = new Polygon(235, 273, 553, 267, 566, 468, 244, 480);
        paper.setFill(Color.web("#e9dfaf")); paper.setStroke(Color.web("#75663e")); paper.setStrokeWidth(3);
        help.getChildren().addAll(paper,
                label("COMO JOGAR", 269, 314, 25, "#40552e", true),
                label("1. Clica na relva para plantar.\n    Cada ervilhadora custa 50 sol.\n\n2. Apanha os sóis: valem 25.\n\n3. Protege a casa dos zombies!", 269, 346, 14, "#4b5837", false),
                label("Clica novamente na placa para fechar.", 267, 462, 11, "#7a7952", false));
    }

    void showMenu() {
        pause.setVisible(false);
        hud.setVisible(false); result.setVisible(false); help.setVisible(false); menu.setVisible(true); enter(menu);
    }
    void showGame() {
        pause.setVisible(false);
        stopEntrance(); menu.setVisible(false); result.setVisible(false); hud.setVisible(true); enter(hud);
    }
    void showResult(int score) {
        pause.setVisible(false);
        resultScore.setText(String.format("%04d pontos", score)); result.setVisible(true); enter(result);
    }

    void showPause() { pause.setVisible(true); enter(pause); }
    void hidePause() { stopEntrance(); pause.setVisible(false); }
    void update(int sun, int score, int lives, double seconds) {
        if (sun != lastSun) {
            energy.setText(Integer.toString(sun));
            seed.setFill(Color.web(sun >= 50 ? "#e9dba6" : "#aaa58b")); lastSun = sun;
        }
        if (score != lastScore) { points.setText(String.format("%04d", score)); lastScore = score; }
        if (lives != lastLives) {
            hearts.setText("● ".repeat(Math.max(0, lives)) + "○ ".repeat(Math.max(0, 3 - lives))); lastLives = lives;
        }
        if ((int) seconds != lastSecond) {
            clock.setText(String.format("%02d:%02d", (int) seconds / 60, (int) seconds % 60)); lastSecond = (int) seconds;
        }
    }
    private void stopEntrance() {
        if (entrance != null) entrance.stop();
        for (Group group : new Group[]{menu, hud, result, pause}) { group.setOpacity(1); group.setTranslateY(0); }
    }
    private void enter(Group node) {
        stopEntrance();
        FadeTransition fade = new FadeTransition(Duration.millis(220), node);
        fade.setFromValue(0); fade.setToValue(1);
        TranslateTransition slide = new TranslateTransition(Duration.millis(260), node);
        slide.setFromY(5); slide.setToY(0); slide.setInterpolator(Interpolator.EASE_OUT);
        entrance = new ParallelTransition(fade, slide); entrance.play();
    }

    private Group plank(String caption, double x, double y, double w, double h, double angle, boolean primary, Runnable action) {
        ImageView art = new ImageView(GardenAssets.PLANK);
        art.setFitWidth(w); art.setFitHeight(h * 1.23); art.setTranslateY(-h * .18); art.setSmooth(true);
        if (primary) art.setEffect(new ColorAdjust(.35, -.35, -.18, 0));
        int size = primary ? 30 : h > 45 ? 22 : w < 100 ? 12 : 18;
        Text text = label(caption, 0, h * .57 + size * .32, size, primary ? "#fff1c5" : "#40331f", true);
        text.setFont(Font.font("Arial Black", FontWeight.BOLD, size));
        while (text.getLayoutBounds().getWidth() > w * .77 && size > 10) {
            text.setFont(Font.font("Arial Black", FontWeight.BOLD, --size));
        }
        text.setX((w - text.getLayoutBounds().getWidth()) / 2);
        Group group = new Group(art, text);
        DropShadow shadow = new DropShadow(4, Color.web("#27352455")); shadow.setOffsetY(3);
        group.setEffect(shadow);
        group.setAccessibleRole(AccessibleRole.BUTTON); group.setAccessibleText(caption); group.setFocusTraversable(true);
        group.setTranslateX(x); group.setTranslateY(y); group.setRotate(angle); group.setCursor(Cursor.HAND);
        TranslateTransition hover = new TranslateTransition(Duration.millis(120), group);
        hover.setInterpolator(Interpolator.EASE_OUT);
        group.setOnMouseEntered(e -> { hover.stop(); hover.setToY(y - 3); hover.play(); text.setFill(Color.web("#fff4c4")); });
        group.setOnMouseExited(e -> { hover.stop(); hover.setToY(y); hover.play(); text.setFill(Color.web(primary ? "#fff1c5" : "#40331f")); group.setScaleX(1); group.setScaleY(1); });
        group.setOnMousePressed(e -> { group.setScaleX(.985); group.setScaleY(.985); });
        group.setOnMouseReleased(e -> { group.setScaleX(1); group.setScaleY(1); });
        group.setOnMouseClicked(e -> { play("ui-tap.wav"); action.run(); e.consume(); });
        group.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.SPACE) { play("ui-tap.wav"); action.run(); e.consume(); } });
        return group;
    }

    private static Group logo(String value, double x, double y, int size, String color, double angle) {
        Text back = label(value, 3, 5, size, "#3d492c", true);
        Text front = label(value, 0, 0, size, color, true);
        for (Text t : new Text[]{back, front}) {
            t.setFont(Font.font("Arial Black", FontWeight.BOLD, size));
            t.setStroke(Color.web("#39462b")); t.setStrokeWidth(1.5);
        }
        Group group = new Group(back, front); group.setTranslateX(x); group.setTranslateY(y); group.setRotate(angle);
        return group;
    }

    private static void stone(GraphicsContext g, double x, double y, double w, double h) {
        g.setFill(Color.web("#34432e55")); g.fillOval(x - 20, y + h - 12, w + 40, 30);
        g.setFill(Color.web("#66735d"));
        g.beginPath(); g.moveTo(x, y + h); g.lineTo(x + 5, y + 65);
        g.bezierCurveTo(x + 10, y - 18, x + w * .63, y - 34, x + w - 17, y + 25);
        g.lineTo(x + w + 4, y + h - 2); g.closePath(); g.fill();
        g.setStroke(Color.web("#47543e")); g.setLineWidth(4); g.stroke();
        g.setFill(Color.web("#a6af90"));
        g.beginPath(); g.moveTo(x + 10, y + h - 12); g.lineTo(x + 15, y + 67);
        g.bezierCurveTo(x + 18, y - 4, x + w * .62, y - 19, x + w - 28, y + 34);
        g.lineTo(x + w - 10, y + h - 14); g.closePath(); g.fill();
        stroke(g, "#c0c6a4", 3, x + 23, y + 95, x + 22, y + 62, x + 38, y + 35);
        stroke(g, "#8a967b", 2, x + w - 30, y + 78, x + w - 25, y + h - 28);
        fill(g, "#8c987a", x + 28, y + h - 53, x + 76, y + h - 58, x + 90, y + h - 23, x + 25, y + h - 26);
    }
    private static void stroke(GraphicsContext g, String color, double width, double... p) {
        g.setStroke(Color.web(color)); g.setLineWidth(width); g.beginPath(); g.moveTo(p[0], p[1]);
        for (int i = 2; i < p.length; i += 2) g.lineTo(p[i], p[i + 1]); g.stroke();
    }
    private static void fill(GraphicsContext g, String color, double... p) {
        double[] xs = new double[p.length / 2], ys = new double[xs.length];
        for (int i = 0; i < xs.length; i++) { xs[i] = p[i * 2]; ys[i] = p[i * 2 + 1]; }
        g.setFill(Color.web(color)); g.fillPolygon(xs, ys, xs.length);
    }
    static Text label(String value, double x, double y, int size, String color, boolean bold) {
        Text text = new Text(x, y, value);
        text.setFont(Font.font("Trebuchet MS", bold ? FontWeight.BOLD : FontWeight.NORMAL, size));
        text.setFill(Color.web(color)); text.setMouseTransparent(true); return text;
    }
}
