package pt.escola.gardendefense.personagens.plants;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.geometry.Point2D;
import javafx.scene.shape.Polygon;
import org.w3c.dom.Node;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;
import java.util.function.DoubleConsumer;

/** GIFs fornecidos pelo artista, com transparência, duração e composição originais. */
public final class GifPlantView extends ImageView implements VistaAnimada {
    private final Frames idle;
    private final Frames attack;
    private boolean attacking;
    private double elapsed;
    private final int shotFrame;
    private final Point2D muzzle;
    private final Polygon shotClip;
    private DoubleConsumer pendingShot;

    GifPlantView(Frames idle, Frames attack, double width, double height) {
        this(idle, attack, width, height, -1, null, false);
    }

    GifPlantView(Frames idle, Frames attack, double width, double height,
                 int shotFrame, Point2D muzzle, boolean hideEmbeddedShot) {
        super(idle.images[0]);
        this.idle = idle; this.attack = attack;
        this.shotFrame = shotFrame; this.muzzle = muzzle;
        setFitWidth(width); setFitHeight(height); setPreserveRatio(true);
        if (hideEmbeddedShot && muzzle != null) {
            double edge = (muzzle.getX() + .025) * width;
            double top = (muzzle.getY() - .12) * height, bottom = (muzzle.getY() + .12) * height;
            // Keep the muzzle flash, but hide the flying orb baked into two GIF frames.
            // The rest of the sprite (including the leaves below this band) stays intact.
            shotClip = new Polygon(0, 0, width, 0, width, top, edge, top,
                    edge, bottom, width, bottom, width, height, 0, height);
        } else shotClip = null;
    }

    @Override public double getMomentoDisparo() { return shotFrame < 0 ? -1 : attack.tempoFotograma(shotFrame); }
    @Override public Point2D getSaidaProjetil() {
        if (muzzle == null) return null;
        double scale = Math.min(getFitWidth() / getImage().getWidth(), getFitHeight() / getImage().getHeight());
        return new Point2D(muzzle.getX() * getImage().getWidth() * scale,
                muzzle.getY() * getImage().getHeight() * scale);
    }

    @Override
    public void atacar() {
        attacking = true; elapsed = 0; pendingShot = null; setImage(attack.images[0]); setClip(null);
    }

    @Override public void atacar(DoubleConsumer emitir) {
        atacar();
        if (shotFrame < 0) emitir.accept(0);
        else pendingShot = emitir;
    }

    @Override
    public void advance(double seconds) {
        elapsed += seconds;
        double attackAge = elapsed;
        boolean release = pendingShot != null && attackAge + 1e-9 >= getMomentoDisparo();
        if (attacking && elapsed >= attack.duration) {
            elapsed -= attack.duration; attacking = false;
        }
        Frames frames = attacking ? attack : idle;
        if (!attacking) elapsed %= frames.duration;
        double time = elapsed;
        int frame = 0;
        while (frame < frames.images.length - 1 && time + 1e-9 >= frames.delays[frame]) {
            time -= frames.delays[frame++];
        }
        setImage(frames.images[frame]);
        setClip(attacking && shotClip != null && frame >= shotFrame && frame < shotFrame + 2 ? shotClip : null);
        if (release) {
            DoubleConsumer shot = pendingShot; pendingShot = null;
            shot.accept(Math.max(0, attackAge - getMomentoDisparo()));
        }
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

        double tempoFotograma(int index) {
            if (index < 0 || index >= images.length) throw new IllegalArgumentException("Fotograma de disparo inválido: " + index);
            double time = 0;
            for (int i = 0; i < index; i++) time += delays[i];
            return time;
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
