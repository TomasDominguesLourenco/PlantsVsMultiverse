package pt.escola.gardendefense.personagens.plants;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;

/** Reproduz uma grelha de fotogramas por ordem de leitura, usando o tempo do jogo. */
public final class SpriteSheetView extends ImageView implements VistaAnimada {
    static final int FRAME_PADDING = 2;
    private final Image[] frames;
    private final int fps;
    private final double cycleSeconds;
    private double elapsed;
    private int currentFrame;

    SpriteSheetView(Image[] frames, int fps, double width, double height) {
        super(frames[0]);
        this.frames = frames;
        this.fps = fps;
        cycleSeconds = (double) frames.length / fps;
        setFitWidth(width);
        setFitHeight(height);
        setPreserveRatio(true);
    }

    static Image[] split(Image sheet, int frameWidth, int frameHeight, int columns, int frameCount) {
        Image[] frames = new Image[frameCount];
        var source = sheet.getPixelReader();
        for (int i = 0; i < frameCount; i++) {
            int sourceX = (i % columns) * frameWidth;
            int sourceY = (i / columns) * frameHeight;
            // Independent textures with a transparent border keep filtering inside each frame.
            var frame = new WritableImage(frameWidth + 2 * FRAME_PADDING, frameHeight + 2 * FRAME_PADDING);
            var destination = frame.getPixelWriter();
            for (int y = 0; y < frameHeight; y++) {
                for (int x = 0; x < frameWidth; x++) {
                    int argb = source.getArgb(sourceX + x, sourceY + y);
                    if ((argb >>> 24) >= 24) {
                        destination.setArgb(x + FRAME_PADDING, y + FRAME_PADDING, argb);
                    }
                }
            }
            frames[i] = frame;
        }
        return frames;
    }

    @Override
    public void advance(double seconds) {
        elapsed = (elapsed + seconds) % cycleSeconds;
        int frame = Math.min((int) (elapsed * fps), frames.length - 1);
        if (frame != currentFrame) {
            currentFrame = frame;
            setImage(frames[frame]);
        }
    }
}
