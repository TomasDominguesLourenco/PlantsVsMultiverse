package pt.escola.gardendefense.personagens.plants;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;

import java.util.Objects;

public final class GokuPlant extends Planta {
    private static final String IDLE = "GokuPlantIdle.png";
    private static final int FRAME_WIDTH = 224;
    private static final int FRAME_HEIGHT = 304;
    private static final double HEIGHT = 88;
    private static final RadialGradient KI = new RadialGradient(0, 0, 0.5, 0.5, 0.5, true,
            CycleMethod.NO_CYCLE, new Stop(0, Color.WHITE), new Stop(0.35, Color.web("#fff7ac")),
            new Stop(0.65, Color.web("#ffd747")), new Stop(0.85, Color.web("#ff9b24")),
            new Stop(1, Color.TRANSPARENT));

    private Image[] idleFrames;

    @Override
    public Node criarVista() {
        if (idleFrames == null) {
            String path = "/assets/textures/" + IDLE;
            var sheet = new Image(Objects.requireNonNull(getClass().getResource(path), path).toExternalForm());
            idleFrames = SpriteSheetView.split(sheet, FRAME_WIDTH, FRAME_HEIGHT, 12, 24);
        }
        return new SpriteSheetView(idleFrames, 24, getLargura(), getAltura());
    }

    @Override
    public Node criarVistaProjetil(double tamanho) {
        double raio = tamanho / 2;
        return new Circle(raio, raio, raio, KI);
    }

    @Override
    public double getAlturaProjetil() {
        return 46;
    }

    @Override
    public String getNome() {
        return "Goku Planta";
    }

    @Override
    public int getVidaMax() {
        return 100;
    }

    @Override
    public String getTextura() {
        return IDLE;
    }

    @Override
    public double getLargura() {
        int border = 2 * SpriteSheetView.FRAME_PADDING;
        return HEIGHT * (FRAME_WIDTH + border) / (FRAME_HEIGHT + border);
    }

    @Override
    public double getAltura() {
        return HEIGHT;
    }

    @Override
    public int getCustoSol() {
        return 100;
    }

    @Override
    public int getDano() {
        return 2;
    }

    @Override
    public double getIntervaloTiro() {
        return 1.3;
    }

    @Override
    public double getVelocidadeProjetil() {
        return 350;
    }
}
