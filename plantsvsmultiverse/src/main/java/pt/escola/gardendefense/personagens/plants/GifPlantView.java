package pt.escola.gardendefense.personagens.plants;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import org.w3c.dom.Node;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

/** GIFs fornecidos pelo artista, com transparência, duração e composição originais. */
public final class GifPlantView extends ImageView implements VistaAnimada {
    private final Frames idle;
    private final Frames attack;
    private boolean attacking;
    private double elapsed;

    GifPlantView(Frames idle, Frames attack, double width, double height) {
        super(idle.images[0]);
        this.idle = idle; this.attack = attack;
        setFitWidth(width); setFitHeight(height); setPreserveRatio(true);
    }

    @Override
    public void atacar() {
        attacking = true; elapsed = 0; setImage(attack.images[0]);
    }

    @Override
    public void advance(double seconds) {
        elapsed += seconds;
        if (attacking && elapsed >= attack.duration) {
            elapsed -= attack.duration; attacking = false;
        }
        Frames frames = attacking ? attack : idle;
        if (!attacking) elapsed %= frames.duration;
        double time = elapsed;
        int frame = 0;
        while (frame < frames.images.length - 1 && time >= frames.delays[frame]) {
            time -= frames.delays[frame++];
        }
        setImage(frames.images[frame]);
    }

    static final class Frames {
        final Image[] images;
        final double[] delays;
        final double duration;

        Frames(Image[] images, double[] delays) {
            this.images = images; this.delays = delays;
            double total = 0;
            for (double delay : delays) total += delay;
            duration = total;
        }
    }

    static Frames carregar(String texture) {
        String path = "/assets/textures/" + texture;
        ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
        try (var source = Objects.requireNonNull(GifPlantView.class.getResourceAsStream(path), path);
             var input = ImageIO.createImageInputStream(source)) {
            reader.setInput(input);
            Node screen = child(reader.getStreamMetadata().getAsTree("javax_imageio_gif_stream_1.0"), "LogicalScreenDescriptor");
            int width = number(screen, "logicalScreenWidth");
            int height = number(screen, "logicalScreenHeight");
            BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            int count = reader.getNumImages(true);
            Image[] images = new Image[count];
            double[] delays = new double[count];
            Graphics2D graphics = canvas.createGraphics();
            try {
                for (int i = 0; i < count; i++) {
                    Node metadata = reader.getImageMetadata(i).getAsTree("javax_imageio_gif_image_1.0");
                    Node descriptor = child(metadata, "ImageDescriptor");
                    Node control = child(metadata, "GraphicControlExtension");
                    String disposal = attribute(control, "disposalMethod");
                    int x = number(descriptor, "imageLeftPosition"), y = number(descriptor, "imageTopPosition");
                    int[] previous = disposal.equals("restoreToPrevious")
                            ? canvas.getRGB(0, 0, width, height, null, 0, width) : null;
                    graphics.setComposite(AlphaComposite.SrcOver);
                    graphics.drawImage(reader.read(i), x, y, null);
                    WritableImage frame = new WritableImage(width, height);
                    frame.getPixelWriter().setPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(),
                            canvas.getRGB(0, 0, width, height, null, 0, width), 0, width);
                    images[i] = frame;
                    delays[i] = Math.max(.02, number(control, "delayTime") / 100.0);
                    if (disposal.equals("restoreToBackgroundColor")) {
                        graphics.setComposite(AlphaComposite.Clear);
                        graphics.fillRect(x, y, number(descriptor, "imageWidth"), number(descriptor, "imageHeight"));
                    } else if (previous != null) {
                        canvas.setRGB(0, 0, width, height, previous, 0, width);
                    }
                }
            } finally { graphics.dispose(); }
            return new Frames(images, delays);
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível ler a animação " + texture, e);
        } finally { reader.dispose(); }
    }

    private static Node child(Node root, String name) {
        for (Node node = root.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node.getNodeName().equals(name)) return node;
        }
        throw new IllegalStateException("Metadados GIF em falta: " + name);
    }

    private static String attribute(Node node, String name) {
        return node.getAttributes().getNamedItem(name).getNodeValue();
    }

    private static int number(Node node, String name) { return Integer.parseInt(attribute(node, name)); }
}
