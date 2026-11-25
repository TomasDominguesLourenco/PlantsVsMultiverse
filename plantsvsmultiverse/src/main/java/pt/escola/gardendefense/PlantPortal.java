package pt.escola.gardendefense;

import javafx.scene.Group;
import javafx.scene.ImageCursor;
import javafx.scene.Node;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Circle;
import javafx.scene.transform.Scale;

final class PlantPortal {
    private static final double OPEN_TIME = 0.13;
    private static final double FALL_TIME = 0.37;
    private static final double CLOSE_TIME = 0.14;
    private final Ring ring;
    private final Node plant;
    private final Rectangle mask;
    private final double height;
    private final double originalY;
    private final Scale squeeze;
    private double elapsed;

    PlantPortal(Ring ring, Node plant, double width, double height) {
        this.ring = ring;
        this.plant = plant;
        this.height = height;
        originalY = plant.getTranslateY();
        mask = new Rectangle(-width, -height, width * 3, height * 2);
        squeeze = new Scale(1, 1, width / 2, height);
        plant.getTransforms().add(squeeze);
        plant.setClip(mask);
        ring.setOpacity(0);
    }

    boolean advance(double seconds) {
        elapsed += seconds;
        double fall = Math.clamp((elapsed - OPEN_TIME) / FALL_TIME, 0, 1);
        double drop = height * fall * fall;
        squeeze.setX(1 - 0.54 * fall);
        squeeze.setY(1 - 0.22 * fall);
        plant.setTranslateY(originalY + drop);
        mask.setHeight(height * 2 - drop / squeeze.getY());
        plant.setVisible(fall < 1);
        double aperture = Math.clamp(Math.min(elapsed / OPEN_TIME,
                (OPEN_TIME + FALL_TIME + CLOSE_TIME - elapsed) / CLOSE_TIME), 0, 1);
        ring.animate(elapsed, aperture, fall);
        return elapsed >= OPEN_TIME + FALL_TIME + CLOSE_TIME;
    }

    static final class Ring extends Group {
        private final double x, y, radius;
        private final Scale spread;
        private final Ellipse aura;
        private final Circle[] sparks = new Circle[8];

        Ring(double width, double height) {
            x = width / 2;
            y = height;
            radius = width * 0.45;
            spread = new Scale(1, 1, x, y);
            getTransforms().add(spread);
            aura = new Ellipse(x, y, radius * 1.35, 15);
            aura.setFill(new RadialGradient(0, 0, .5, .5, .5, true, CycleMethod.NO_CYCLE,
                    new Stop(.47, Color.web("#27cbffb0")), new Stop(1, Color.TRANSPARENT)));
            var opening = new Ellipse(x, y, radius, 8.5);
            opening.setFill(new RadialGradient(0, 0, .5, .5, .5, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#050718")), new Stop(.72, Color.web("#101b39")),
                    new Stop(1, Color.web("#3455a4"))));
            opening.setStroke(Color.web("#72e7ff"));
            opening.setStrokeWidth(2.5);
            var edge = new Ellipse(x, y, radius * 0.87, 5.6);
            edge.setFill(Color.TRANSPARENT);
            edge.setStroke(Color.web("#ffb258"));
            getChildren().addAll(aura, opening, edge);
            for (int i = 0; i < sparks.length; i++) {
                sparks[i] = new Circle(i % 2 == 0 ? 1.6 : 1.1,
                        Color.web(i % 3 == 0 ? "#ffd094" : "#b2f3ff"));
                getChildren().add(sparks[i]);
            }
            setMouseTransparent(true);
        }

        void animate(double elapsed, double aperture, double fall) {
            double eased = aperture * aperture * (3 - 2 * aperture);
            spread.setX(0.12 + 0.88 * eased);
            spread.setY(0.24 + 0.76 * eased);
            setOpacity(eased);
            aura.setOpacity(0.72 + 0.28 * Math.sin(elapsed * 23));
            for (int i = 0; i < sparks.length; i++) {
                double angle = elapsed * 13 + i * (2 * Math.PI / sparks.length);
                double drift = 1.18 - 0.42 * fall;
                sparks[i].setCenterX(x + radius * drift * Math.cos(angle));
                sparks[i].setCenterY(y + 10 * drift * Math.sin(angle));
            }
        }
    }

    static ImageCursor createCursor() {
        int size = 32;
        var image = new WritableImage(size, size);
        var pixels = image.getPixelWriter();
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                double dx = x - 15.5, dy = y - 15.5;
                double left = Math.hypot((dx + 5) / 6, dy / 10);
                double right = Math.hypot((dx - 5) / 6, dy / 10);
                if (left > .65 && left < 1.2) pixels.setColor(x, y, Color.web("#3dd8ff"));
                if (right > .65 && right < 1.2) pixels.setColor(x, y, Color.web("#ff9d41"));
                if (Math.hypot(dx, dy) < 2) pixels.setColor(x, y, Color.WHITE);
            }
        }
        return new ImageCursor(image, 16, 16);
    }
}
