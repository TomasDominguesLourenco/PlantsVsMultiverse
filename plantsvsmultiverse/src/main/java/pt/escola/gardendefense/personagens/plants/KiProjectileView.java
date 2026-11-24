package pt.escola.gardendefense.personagens.plants;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.transform.Scale;

/** Core, light and a short trailing flame; all movement uses the game clock. */
public final class KiProjectileView extends Group implements VistaAnimada {
    private final Ellipse glow = new Ellipse(8, 8, 18, 13);
    private final Scale tailStretch = new Scale(1, 1, 8, 8);
    private final Circle[] sparks = new Circle[3];
    private double elapsed;

    KiProjectileView(double size) {
        setMouseTransparent(true);
        glow.setFill(new RadialGradient(0, 0, .5, .5, .5, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#fff5b58c")), new Stop(.5, Color.web("#ffd35740")),
                new Stop(1, Color.TRANSPARENT)));
        Path tail = new Path(new MoveTo(9, 1), new CubicCurveTo(-2, 0, -15, 8, -34, 8),
                new CubicCurveTo(-16, 10, -3, 17, 9, 15), new ClosePath());
        tail.setFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.TRANSPARENT), new Stop(.35, Color.web("#ffa94155")),
                new Stop(.75, Color.web("#ffe57cdd")), new Stop(1, Color.web("#fff7cf"))));
        tail.setStroke(null); tail.getTransforms().add(tailStretch);
        Path ribbon = new Path(new MoveTo(6, 5), new CubicCurveTo(-1, 5, -10, 8, -24, 8),
                new CubicCurveTo(-9, 9, -1, 12, 6, 11), new ClosePath());
        ribbon.setFill(Color.web("#fff5c6b3")); ribbon.setStroke(null);
        Ellipse rim = new Ellipse(8, 8, 10, 8.7);
        rim.setFill(Color.TRANSPARENT); rim.setStroke(Color.web("#ffd25ea6")); rim.setStrokeWidth(1);
        Circle core = new Circle(8, 8, 7.5);
        core.setFill(new RadialGradient(-35, .2, .45, .4, .7, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#fffef5")), new Stop(.42, Color.web("#fff9c4")),
                new Stop(.76, Color.web("#ffe174")), new Stop(1, Color.web("#f5ac37"))));
        Circle shine = new Circle(10, 5, 2, Color.web("#fffef3"));
        getChildren().addAll(glow, tail, ribbon, rim, core, shine);
        for (int i = 0; i < sparks.length; i++) {
            sparks[i] = new Circle(-8 - i * 9, i % 2 == 0 ? 3 : 13, 1.4 - i * .25, Color.web("#ffe9a2"));
            getChildren().add(sparks[i]);
        }
        getTransforms().add(new Scale(size / 16, size / 16));
    }

    @Override
    public void advance(double seconds) {
        elapsed += seconds;
        double pulse = Math.sin(elapsed * 24);
        glow.setScaleX(1 + pulse * .07); glow.setScaleY(1 - pulse * .05);
        tailStretch.setX(.94 + pulse * .06);
        for (int i = 0; i < sparks.length; i++) {
            sparks[i].setOpacity(.25 + .65 * (.5 + .5 * Math.sin(elapsed * 18 - i * 1.8)));
        }
    }
}
