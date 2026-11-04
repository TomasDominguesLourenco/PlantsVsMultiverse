package pt.escola.gardendefense;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

import java.util.Random;

/** Decorative scenery only: the playable surface keeps its original coordinates. */
final class LawnView extends Canvas {
    private final GraphicsContext g = getGraphicsContext2D();
    private final Random detail = new Random(73421);

    LawnView(double x, double y, double cellW, double cellH, int rows, int cols) {
        super(1000, 600);
        setMouseTransparent(true);
        double w = cols * cellW;
        double h = rows * cellH;

        rect(0, 0, 1000, 600, "#83905c");
        // The far boundary and the visible side of the house give an oblique view.
        rect(0, 0, 1000, 92, "#526a40");
        for (int i = 0; i < 48; i++) {
            double bx = i * 23 - 12;
            oval(bx, 5 + detail.nextInt(24), 55, 48, i % 2 == 0 ? "#617f45" : "#708b4c");
        }
        rect(76, 63, 924, 9, "#726347");
        rect(76, 85, 924, 7, "#726347");
        for (int i = 0; i < 42; i++) {
            double fx = 82 + i * 23;
            polygon("#ac9a70", fx, 92, fx, 47, fx + 7, 39, fx + 14, 47, fx + 14, 92);
            line(fx + 2, 49, fx + 2, 88, "#c4b58b", 2);
        }

        // A narrow house wall and doorstep, cropped naturally by the camera.
        rect(0, 0, 59, 562, "#d8c69b");
        for (int sy = 12; sy < 560; sy += 24) {
            line(0, sy, 59, sy, "#b8a781", 2);
        }
        polygon("#a69270", 59, 0, 79, 24, 79, 548, 59, 562);
        rect(5, 183, 43, 126, "#7a6c51");
        rect(9, 188, 35, 115, "#89a8a0");
        line(26, 188, 26, 303, "#eee0b9", 4);
        line(9, 244, 44, 244, "#eee0b9", 4);
        polygon("#b9af96", 0, 548, 59, 548, 94, 580, 0, 580);

        rect(79, 92, 921, 476, "#a89671");
        // Stone path on the invasion side, with staggered, slightly irregular joints.
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 3; col++) {
                double px = 921 + col * 32 + (row % 2) * 14;
                double py = 100 + row * 55;
                g.setFill(Color.web((row + col) % 2 == 0 ? "#c4bea5" : "#b5b198"));
                g.fillRoundRect(px, py, 29, 51, 7, 7);
                line(px + 5, py + 3, px + 24, py + 3, "#d7d0b9", 1);
            }
        }

        // Visible soil faces below and beside the turf create shallow depth.
        polygon("#655035", x, y + h, x + w, y + h, x + w + 9, y + h + 13, x - 9, y + h + 13);
        polygon("#806441", x + w, y, x + w + 9, y - 7, x + w + 9, y + h + 13, x + w, y + h);
        rect(x - 6, y - 5, w + 12, h + 5, "#486233");

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                double cx = x + col * cellW;
                double cy = y + row * cellH;
                Color base = Color.web((row + col) % 2 == 0 ? "#83ac49" : "#78a343");
                g.setFill(new LinearGradient(0, 0, 0.15, 1, true, CycleMethod.NO_CYCLE,
                        new Stop(0, base.brighter().interpolate(base, 0.75)),
                        new Stop(1, base.darker().interpolate(base, 0.8))));
                g.fillRect(cx, cy, cellW, cellH);
                // Small, repeatable hand-drawn tufts instead of noisy random tiles.
                for (int tuft = 0; tuft < 20; tuft++) {
                    double tx = cx + 6 + detail.nextDouble() * (cellW - 12);
                    double ty = cy + 8 + detail.nextDouble() * (cellH - 16);
                    String shade = tuft % 3 == 0 ? "#a0bd62" : "#6e953e";
                    line(tx - 2, ty - 3, tx, ty, shade, 1);
                    line(tx, ty, tx + 2, ty - 4, shade, 1);
                }
                line(cx + 1, cy + cellH - 1, cx + cellW - 1, cy + cellH - 1, "#719440", 1);
            }
        }
        // Turf lip, cast shadow and a planted border frame the playable rectangle.
        line(x, y, x + w, y, "#acc76d", 3);
        line(x, y, x, y + h, "#5c7e35", 3);
        line(x, y + h + 2, x + w, y + h + 2, "#455c2e", 4);
        for (int i = 0; i < 72; i++) {
            double tx = x + i * w / 72;
            line(tx, y + h, tx + 3, y + h + 5 + detail.nextInt(4), "#6c913c", 2);
        }
        rect(79, 575, 921, 25, "#667b43");
        for (int i = 0; i < 30; i++) {
            double fx = 115 + i * 29;
            oval(fx, 580 + detail.nextInt(12), 16, 8, "#536e37");
            if (i % 4 == 0) {
                oval(fx + 4, 578, 5, 5, "#e5ce85");
            }
        }
    }

    private void rect(double x, double y, double w, double h, String color) {
        g.setFill(Color.web(color));
        g.fillRect(x, y, w, h);
    }

    private void oval(double x, double y, double w, double h, String color) {
        g.setFill(Color.web(color));
        g.fillOval(x, y, w, h);
    }

    private void line(double x1, double y1, double x2, double y2, String color, double width) {
        g.setStroke(Color.web(color));
        g.setLineWidth(width);
        g.strokeLine(x1, y1, x2, y2);
    }

    private void polygon(String color, double... points) {
        double[] xs = new double[points.length / 2];
        double[] ys = new double[xs.length];
        for (int i = 0; i < xs.length; i++) {
            xs[i] = points[i * 2];
            ys[i] = points[i * 2 + 1];
        }
        g.setFill(Color.web(color));
        g.fillPolygon(xs, ys, xs.length);
    }
}
