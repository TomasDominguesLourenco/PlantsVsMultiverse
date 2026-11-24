package pt.escola.gardendefense;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.transform.Scale;
import javafx.util.Duration;

/** Camera movement is temporary: the board's projection and hit areas stay intact. */
final class GardenTransition {
    private final Node scene;
    private final Scale camera = new Scale(1, 1, 500, 300);
    private Timeline animation;

    GardenTransition(Node scene) { this.scene = scene; }

    void play(Runnable changeScene, Runnable finished) {
        cancel();
        scene.getTransforms().add(camera);
        animation = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(scene.opacityProperty(), 1),
                        new KeyValue(camera.xProperty(), 1), new KeyValue(camera.yProperty(), 1)),
                new KeyFrame(Duration.millis(260), _ -> changeScene.run(),
                        new KeyValue(scene.opacityProperty(), 0, Interpolator.EASE_IN),
                        new KeyValue(camera.xProperty(), .91, Interpolator.EASE_IN),
                        new KeyValue(camera.yProperty(), .91, Interpolator.EASE_IN)),
                new KeyFrame(Duration.millis(280),
                        new KeyValue(scene.opacityProperty(), 0),
                        new KeyValue(camera.xProperty(), .88), new KeyValue(camera.yProperty(), .88)),
                new KeyFrame(Duration.millis(610),
                        new KeyValue(scene.opacityProperty(), 1, Interpolator.EASE_OUT),
                        new KeyValue(camera.xProperty(), 1.018, Interpolator.EASE_OUT),
                        new KeyValue(camera.yProperty(), 1.018, Interpolator.EASE_OUT)),
                new KeyFrame(Duration.millis(750),
                        new KeyValue(scene.opacityProperty(), 1),
                        new KeyValue(camera.xProperty(), 1, Interpolator.EASE_BOTH),
                        new KeyValue(camera.yProperty(), 1, Interpolator.EASE_BOTH)));
        animation.setOnFinished(_ -> {
            reset();
            animation = null;
            finished.run();
        });
        animation.play();
    }

    void cancel() {
        if (animation != null) { animation.stop(); animation = null; }
        reset();
    }

    private void reset() {
        scene.setOpacity(1);
        camera.setX(1); camera.setY(1);
        scene.getTransforms().remove(camera);
    }
}
