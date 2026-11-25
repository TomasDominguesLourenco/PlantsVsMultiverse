package pt.escola.gardendefense.personagens.plants;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import java.util.Objects;
import java.util.function.DoubleConsumer;

/** Animação articulada: raízes fixas, respiração, preparação, emissão e recuo. */
public final class PeaShooterView extends Group implements VistaAnimada {
    private static final double SPRITE_SCALE = 0.72;
    private static final double RELEASE = .18;
    private static final double[] TIMES = {0, .10, .18, .29, .43, .60};
    private static final double[] XS = {1, .92, 1.10, .95, 1.02, 1};
    private static final double[] YS = {1, 1.06, .94, 1.04, .99, 1};
    private static final double[] LEAN = {0, -3, 2, -2, .5, 0};
    private static final double[] SHIFT = {0, -2, 3, -2, .5, 0};
    private static int serial;
    private final double phase = serial++ * .73;
    private final Group head;
    private final Scale headScale = new Scale(1, 1, 26, 50);
    private final Rotate headLean = new Rotate(0, 26, 50);
    private final Scale baseScale = new Scale(1, 1, 34, 55);
    private double elapsed;
    private double attackTime;
    private boolean attacking;
    private DoubleConsumer pendingShot;

    private static final class Art {
        static final Image IMAGE = new Image(Objects.requireNonNull(PeaShooterView.class.getResource(
                "/assets/textures/PeaShooterParts.png")).toExternalForm());
        static final Rectangle2D HEAD = bounds(0, (int) IMAGE.getWidth() / 2);
        static final Rectangle2D BASE = bounds((int) IMAGE.getWidth() / 2, (int) IMAGE.getWidth());

        private static Rectangle2D bounds(int start, int end) {
            int left = end, right = start, top = (int) IMAGE.getHeight(), bottom = 0;
            var pixels = IMAGE.getPixelReader();
            for (int y = 0; y < IMAGE.getHeight(); y++) for (int x = start; x < end; x++) {
                if ((pixels.getArgb(x, y) >>> 24) >= 24) {
                    left = Math.min(left, x); right = Math.max(right, x);
                    top = Math.min(top, y); bottom = Math.max(bottom, y);
                }
            }
            if (left > right || top > bottom) throw new IllegalStateException("Parte da ervilhadora vazia no atlas.");
            return new Rectangle2D(left, top, right - left + 1, bottom - top + 1);
        }
    }

    PeaShooterView() {
        ImageView base = part(Art.BASE, 68, 55);
        base.setTranslateX(4); base.setTranslateY(33);
        base.getTransforms().add(baseScale);
        head = new Group(part(Art.HEAD, 78, 53));
        head.getTransforms().addAll(headScale, headLean);
        getChildren().addAll(base, head);
        // Scale around the ground anchor so the roots stay in place.
        getTransforms().add(new Scale(SPRITE_SCALE, SPRITE_SCALE, 44, 88));
        pose();
    }

    private static ImageView part(Rectangle2D viewport, double width, double height) {
        ImageView image = new ImageView(Art.IMAGE);
        image.setViewport(viewport); image.setFitWidth(width); image.setFitHeight(height); image.setSmooth(true);
        return image;
    }

    @Override public double getMomentoDisparo() { return RELEASE; }
    @Override public Point2D getSaidaProjetil() { return localToParent(head.localToParent(73, 26)); }
    @Override public void atacar() { attacking = true; attackTime = 0; pendingShot = null; pose(); }
    @Override public void atacar(DoubleConsumer emitir) { atacar(); pendingShot = emitir; }

    @Override public void advance(double seconds) {
        elapsed += seconds;
        double age = attacking ? attackTime + seconds : 0;
        boolean release = pendingShot != null && age >= RELEASE;
        if (attacking) { attackTime = age; if (age >= TIMES[TIMES.length - 1]) attacking = false; }
        pose();
        if (release) {
            DoubleConsumer shot = pendingShot; pendingShot = null;
            shot.accept(Math.max(0, age - RELEASE));
        }
    }

    private void pose() {
        double breath = Math.sin(elapsed * 2.2 + phase);
        baseScale.setY(1 + breath * .009);
        if (attacking) {
            headScale.setX(sample(XS)); headScale.setY(sample(YS));
            headLean.setAngle(sample(LEAN));
            head.setTranslateX(12 + sample(SHIFT)); head.setTranslateY(2);
        } else {
            headScale.setX(1); headScale.setY(1 + breath * .006);
            headLean.setAngle(Math.sin(elapsed * 1.8 + phase) * 1.3);
            head.setTranslateX(12); head.setTranslateY(2 + breath * .7);
        }
    }

    private double sample(double[] values) {
        int i = 0;
        while (i < TIMES.length - 2 && attackTime > TIMES[i + 1]) i++;
        double t = Math.max(0, Math.min(1, (attackTime - TIMES[i]) / (TIMES[i + 1] - TIMES[i])));
        t = t * t * (3 - 2 * t);
        return values[i] + (values[i + 1] - values[i]) * t;
    }
}
