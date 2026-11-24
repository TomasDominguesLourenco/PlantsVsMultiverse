package pt.escola.gardendefense;

import com.almasb.fxgl.entity.Entity;
import javafx.geometry.Point2D;
import javafx.scene.shape.Polygon;
import javafx.scene.transform.Affine;

/**
 * Projeção partilhada pelo cenário e pela interação com o jardim.
 * screen() desenha o chão em perspetiva; world() inverte a mesma projeção
 * para os cliques, mantendo o combate na grelha lógica original de 5 × 9.
 */
final class BoardProjection {
    private BoardProjection() {}

    static Point2D screen(double x, double y) {
        double depth = (y - 100) / 450;
        double t = 0.8 * depth / (1 - 0.2 * depth);
        double scale = 0.8 + 0.2 * t;
        return new Point2D(190 - 85 * t + (x - 100) * scale, 172 + 380 * t);
    }

    static Point2D world(double x, double y) {
        double t = (y - 172) / 380;
        double depth = t / (0.8 + 0.2 * t);
        return new Point2D(100 + (x - 190 + 85 * t) / (0.8 + 0.2 * t), 100 + depth * 450);
    }

    static double scaleAt(double y) {
        return 0.8 / (1 - 0.2 * (y - 100) / 450);
    }

    static void cell(Polygon polygon, int row, int col) {
        polygon.getPoints().clear();
        double x = 100 + col * 90, y = 100 + row * 90;
        for (Point2D p : new Point2D[]{screen(x, y), screen(x + 90, y), screen(x + 90, y + 90), screen(x, y + 90)}) {
            polygon.getPoints().addAll(p.getX(), p.getY());
        }
    }

    /** Upright sprites stand on the plane, rather than being sheared with the grass. */
    static void place(Entity entity, double width, double height, int row, double lift) {
        double groundY = 100 + row * 90 + 73;
        Point2D foot = screen(entity.getX() + width / 2, groundY);
        double scale = scaleAt(groundY);
        var transforms = entity.getViewComponent().getParent().getTransforms();
        if (transforms.isEmpty()) transforms.add(new Affine());
        Affine transform = (Affine) transforms.getFirst();
        // FXGL's entity translation is inside this parent, so it is scaled too.
        transform.setToTransform(scale, 0, foot.getX() - (entity.getX() + width / 2) * scale,
                0, scale, foot.getY() - (entity.getY() + height + lift) * scale);
        entity.setZIndex(row * 10 + (lift > 0 ? 2 : 0));
    }
}
