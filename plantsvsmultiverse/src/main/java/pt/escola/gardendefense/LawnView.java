package pt.escola.gardendefense;

import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import java.util.Random;

/** Painted scenery with an actual projected ground plane and upright architecture. */
final class LawnView extends Canvas {
    private final GraphicsContext g = getGraphicsContext2D();
    private final Random detail = new Random(73421);

    LawnView(boolean menu) {
        super(1000, 600);
        setMouseTransparent(true);
        sky();
        if (menu) drawMenuGarden(); else drawPlayingGarden();
    }

    private void sky() {
        g.setFill(new LinearGradient(0, 0, 0, 220, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#87cbd3")), new Stop(1, Color.web("#dce8b1"))));
        g.fillRect(0, 0, 1000, 600);
        cloud(310, 33, 1.0); cloud(699, 18, 0.8); cloud(894, 53, 0.6);
        g.setFill(Color.web("#9bb96d"));
        g.beginPath(); g.moveTo(0, 133);
        g.bezierCurveTo(82, 65, 140, 86, 212, 118);
        g.bezierCurveTo(335, 47, 413, 99, 507, 128);
        g.bezierCurveTo(641, 72, 713, 103, 775, 128);
        g.bezierCurveTo(893, 50, 947, 78, 1000, 102);
        g.lineTo(1000, 240); g.lineTo(0, 240); g.closePath(); g.fill();
        g.setFill(Color.web("#6c9453"));
        g.beginPath(); g.moveTo(0, 160);
        g.bezierCurveTo(117, 102, 162, 111, 264, 159);
        g.bezierCurveTo(396, 96, 462, 126, 556, 160);
        g.bezierCurveTo(724, 101, 811, 137, 864, 156);
        g.bezierCurveTo(938, 112, 973, 117, 1000, 127);
        g.lineTo(1000, 260); g.lineTo(0, 260); g.closePath(); g.fill();
    }

    private void drawPlayingGarden() {
        // Street and sidewalk recede towards the fence, alongside the playable lawn.
        rect(0, 150, 1000, 450, "#a6ad70");
        shape("#777c70", 859, 165, 1000, 165, 1000, 600, 939, 600);
        groundQuad(910, 87, 1045, 87, 1045, 590, 910, 590, "#b5b39a");
        for (int row = 0; row < 10; row++) {
            double yy = 88 + row * 50;
            groundQuad(915, yy, 1035, yy, 1035, yy + 46, 915, yy + 46,
                    row % 2 == 0 ? "#d4ceb0" : "#c9c4a7");
            groundLine(973, yy + (row % 2) * 17, 973, yy + 44, "#acae95", 1);
        }
        groundLine(1040, 90, 1040, 590, "#ebe2c0", 5);
        // The porch follows the same ground projection as the lawn.
        groundQuad(-55, 70, 98, 70, 98, 605, -55, 605, "#b2a58a");
        for (int row = 0; row < 9; row++) {
            double yy = 90 + row * 60;
            groundLine(-50, yy, 98, yy, "#908970", 2);
        }
        fence();
        house(false);
        // Shallow dirt face is visible along the front and right edges.
        Point2D a = BoardProjection.screen(100, 550), b = BoardProjection.screen(910, 550);
        shape("#70573b", a.getX(), a.getY(), b.getX(), b.getY(), b.getX() + 4, b.getY() + 13, a.getX() - 5, a.getY() + 13);
        groundQuad(94, 94, 916, 94, 916, 555, 94, 555, "#3d5d2c");
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 9; col++) {
                double x = 100 + col * 90, y = 100 + row * 90;
                groundQuad(x, y, x + 90, y, x + 90, y + 90, x, y + 90,
                        (row + col) % 2 == 0 ? "#83b64b" : "#76a843");
                // Tiny clusters, leaving the centre of each planting cell clear.
                for (int tuft = 0; tuft < 4; tuft++) {
                    double tx = x + 8 + detail.nextDouble() * 72;
                    double ty = y + 8 + detail.nextDouble() * 74;
                    Point2D p = BoardProjection.screen(tx, ty);
                    double s = BoardProjection.scaleAt(ty);
                    line(p.getX() - 2 * s, p.getY() - 3 * s, p.getX(), p.getY(), "#60913d", 1);
                    line(p.getX(), p.getY(), p.getX() + 2 * s, p.getY() - 4 * s, "#a3c76b", 1);
                }
            }
        }
        groundQuad(100, 100, 116, 100, 113, 550, 100, 550, "#355b322b");
        groundLine(100, 100, 910, 100, "#afd46e", 3);
        groundLine(100, 550, 910, 550, "#456d30", 3);
        for (int i = 0; i < 49; i++) {
            Point2D p = BoardProjection.screen(103 + i * 16.5, 551);
            line(p.getX(), p.getY(), p.getX() + 3, p.getY() + 5 + i % 3, "#77a346", 2);
        }
        // A garden hose and a handful of flowers are scenery, never game controls.
        g.setStroke(Color.web("#3e7351")); g.setLineWidth(4);
        g.strokeOval(52, 457, 35, 18); g.strokeOval(58, 461, 23, 10);
        line(54, 467, 41, 476, "#3e7351", 4);
        shape("#a8ac73", 0, 580, 96, 560, 946, 571, 1000, 600, 0, 600);
        for (int i = 0; i < 16; i++) {
            double fx = 107 + i * 54;
            oval(fx, 576 + i % 3 * 3, 21, 9, "#6b8b46");
            if (i % 3 == 0) flower(fx + 9, 576 + i % 3 * 3, "#ece1a4");
        }
    }

    private void fence() {
        rect(120, 152, 770, 9, "#7e6945");
        rect(120, 126, 770, 7, "#8f774d");
        for (int i = 0; i < 35; i++) {
            double x = 126 + i * 22;
            double top = 103 + (i % 5 == 1 ? -4 : i % 3);
            outlined("#ccba88", "#796745", 1.5, x, 168, x - 1, top + 8, x + 7, top, x + 15, top + 8, x + 14, 168);
            line(x + 3, top + 12, x + 4, 159, "#e2d3a3", 2);
            oval(x + 5, 132, 2, 2, "#7e6945");
        }
        // Bushes overlap the fence ends instead of creating a perfect repeated border.
        bush(900, 130, 56); bush(969, 117, 64);
    }

    private void house(boolean menu) {
        if (menu) {
            g.setFill(Color.web("#35472625"));
            g.fillOval(-27, 407, 289, 37);
            g.drawImage(GardenAssets.HOUSE, -95, 180, 385, 257);
        } else {
            // The facade is naturally cropped by the camera, as in the original game.
            // The lawn is drawn afterwards, retaining the exact projected boundary.
            g.drawImage(GardenAssets.HOUSE, -462, 30, 690, 460);
        }
    }
    private void drawMenuGarden() {
        shape("#a1ba69", 0, 270, 1000, 236, 1000, 600, 0, 600);
        shape("#739d46", 75, 431, 753, 280, 1000, 383, 1000, 600, 0, 600);
        for (int i = 0; i < 5; i++) {
            // Parallel mowing bands widen towards the camera, without crossing.
            shape("#87ac4a", 230 + i * 132, 397 - i * 22,
                    296 + i * 132, 386 - i * 22, 451 + i * 150, 600, 376 + i * 150, 600);
        }
        for (int i = 0; i < 19; i++) {
            double x = 273 + i * 39;
            outlined("#d0c293", "#8a8055", 1, x, 301 - i * 2, x - 2, 233 - i * 2,
                    x + 6, 225 - i * 2, x + 14, 234 - i * 2, x + 14, 299 - i * 2);
        }
        house(true);
        // The path curves around the house into the playable garden.
        shape("#c9bd98", 0, 472, 66, 435, 222, 415, 257, 439, 311, 600, 135, 600);
        line(26, 491, 198, 457, "#a49c7b", 2);
        line(70, 543, 245, 515, "#a49c7b", 2);
        line(127, 598, 284, 573, "#a49c7b", 2);
        bush(-20, 488, 70); bush(35, 545, 66);
        bush(952, 284, 74); bush(1010, 381, 80);
        for (int i = 0; i < 33; i++) {
            double x = 292 + detail.nextDouble() * 660, y = 335 + detail.nextDouble() * 240;
            line(x - 3, y - 4, x, y, "#5e8939", 1.5);
            line(x, y, x + 3, y - 6, "#a5c96d", 1.5);
        }
        // Foreground silhouettes frame the scene like an illustrated game cover.
        for (int i = 0; i < 9; i++) {
            double x = 348 + i * 79;
            oval(x, 578 + i % 3 * 4, 63, 25, "#557e37");
            if (i % 3 == 0) flower(x + 23, 579, "#f1dda0");
        }
    }

    private void cloud(double x, double y, double s) {
        g.setFill(Color.web("#edf0cc"));
        g.fillOval(x, y + 15 * s, 110 * s, 22 * s);
        g.fillOval(x + 17 * s, y, 46 * s, 39 * s);
        g.fillOval(x + 55 * s, y + 9 * s, 39 * s, 28 * s);
    }

    private void bush(double x, double y, double r) {
        oval(x - r, y - r * .6, r * 1.3, r, "#476a39");
        oval(x - r * .3, y - r, r * 1.3, r * 1.1, "#597f40");
        oval(x - r * .65, y - r * .7, r, r * .7, "#6b9149");
    }

    private void flower(double x, double y, String petals) {
        line(x, y + 10, x, y, "#587b3a", 2);
        for (int i = 0; i < 5; i++) {
            double a = i * Math.PI * 2 / 5;
            oval(x + Math.cos(a) * 3 - 2.5, y + Math.sin(a) * 3 - 2.5, 5, 5, petals);
        }
        oval(x - 2, y - 2, 4, 4, "#bb9651");
    }

    private void groundQuad(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4, String color) {
        Point2D a = BoardProjection.screen(x1, y1), b = BoardProjection.screen(x2, y2),
                c = BoardProjection.screen(x3, y3), d = BoardProjection.screen(x4, y4);
        shape(color, a.getX(), a.getY(), b.getX(), b.getY(), c.getX(), c.getY(), d.getX(), d.getY());
    }

    private void groundLine(double x1, double y1, double x2, double y2, String color, double width) {
        Point2D a = BoardProjection.screen(x1, y1), b = BoardProjection.screen(x2, y2);
        line(a.getX(), a.getY(), b.getX(), b.getY(), color, width);
    }

    private void rect(double x, double y, double w, double h, String color) { g.setFill(Color.web(color)); g.fillRect(x, y, w, h); }
    private void oval(double x, double y, double w, double h, String color) { g.setFill(Color.web(color)); g.fillOval(x, y, w, h); }
    private void line(double x1, double y1, double x2, double y2, String color, double width) {
        g.setStroke(Color.web(color)); g.setLineWidth(width); g.strokeLine(x1, y1, x2, y2);
    }
    private void outlined(String fill, String stroke, double width, double... p) {
        shape(fill, p); g.setStroke(Color.web(stroke)); g.setLineWidth(width);
        g.beginPath(); g.moveTo(p[0], p[1]);
        for (int i = 2; i < p.length; i += 2) g.lineTo(p[i], p[i + 1]);
        g.closePath(); g.stroke();
    }
    private void shape(String color, double... p) {
        double[] xs = new double[p.length / 2], ys = new double[xs.length];
        for (int i = 0; i < xs.length; i++) { xs[i] = p[i * 2]; ys[i] = p[i * 2 + 1]; }
        g.setFill(Color.web(color)); g.fillPolygon(xs, ys, xs.length);
    }
}
