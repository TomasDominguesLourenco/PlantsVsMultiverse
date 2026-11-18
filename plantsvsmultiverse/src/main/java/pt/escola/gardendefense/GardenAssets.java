package pt.escola.gardendefense;

import javafx.scene.image.Image;
import java.util.Objects;

/** Shared decoded art; button instances do not repeatedly load full-size PNGs. */
final class GardenAssets {
    static final Image HOUSE = load("cottage-right-v2.png");
    static final Image PLANK = load("wood-button-v1.png");

    private GardenAssets() {}

    private static Image load(String name) {
        var path = "/assets/textures/ui/" + name;
        try (var stream = Objects.requireNonNull(GardenAssets.class.getResourceAsStream(path), path)) {
            return new Image(stream);
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Não foi possível carregar " + path, e);
        }
    }
}
