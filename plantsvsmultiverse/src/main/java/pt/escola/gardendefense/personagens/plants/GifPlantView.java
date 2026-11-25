package pt.escola.gardendefense.personagens.plants;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.geometry.Point2D;
import javafx.scene.Group;
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
public final class GifPlantView extends Group implements VistaAnimada {
    private final ImageView current = new ImageView();
    private final double width;
    private final double height;
    private final Frames idle;
    private final Frames attack;
    private final double idleResumeTime;
    private boolean attacking;
    private double elapsed;
    private double extraRecovery;
    private final int shotFrame;
    private final Point2D muzzle;
    private final Polygon shotClip;
    private DoubleConsumer pendingShot;

    GifPlantView(Frames idle, Frames attack, double width, double height) {
        this(idle, attack, width, height, -1, null, false);
    }

    GifPlantView(Frames idle, Frames attack, double width, double height,
                 int shotFrame, Point2D muzzle, boolean hideEmbeddedShot) {
        this.idle = idle; this.attack = attack;
        this.shotFrame = shotFrame; this.muzzle = muzzle;
        this.width = width;
        this.height = height;
        idleResumeTime = idle.tempoFotograma(closestIdleFrame());
        current.setFitWidth(width);
        current.setFitHeight(height);
        current.setPreserveRatio(true);
        getChildren().add(current);
        show(idle.images[0]);
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
        var image = current.getImage();
        double scale = scale(image);
        return new Point2D(current.getTranslateX() + muzzle.getX() * image.getWidth() * scale,
                current.getTranslateY() + muzzle.getY() * image.getHeight() * scale);
    }

    @Override public void prolongarRecarga(double seconds) { extraRecovery = seconds; }

    @Override
    public void atacar() {
        attacking = true;
        elapsed = 0;
        extraRecovery = 0;
        pendingShot = null;
        show(attack.images[0]);
        current.setClip(null);
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
        double launch = getMomentoDisparo();
        boolean release = pendingShot != null && attackAge + 1e-9 >= launch;
        double totalAttack = attack.duration + extraRecovery;
        if (attacking && elapsed >= totalAttack) {
            elapsed = idleResumeTime + elapsed - totalAttack;
            attacking = false;
        }
        Frames frames = attacking ? attack : idle;
        if (!attacking) elapsed %= frames.duration;
        double time = elapsed;
        if (attacking && extraRecovery > 0 && time > launch) {
            time = launch + (time - launch) * (attack.duration - launch) / (totalAttack - launch);
        }
        int frame = 0;
        while (frame < frames.images.length - 1 && time + 1e-9 >= frames.delays[frame]) {
            time -= frames.delays[frame++];
        }
        show(frames.images[frame]);
        current.setClip(attacking && shotClip != null && frame >= shotFrame && frame < shotFrame + 2 ? shotClip : null);
        if (release) {
            DoubleConsumer shot = pendingShot; pendingShot = null;
            shot.accept(Math.max(0, attackAge - launch));
        }
    }

    private double scale(Image image) {
        return Math.min(width / image.getWidth(), height / image.getHeight());
    }

    private int closestIdleFrame() {
        Image finalPose = attack.images[attack.images.length - 1];
        double bestDistance = Double.POSITIVE_INFINITY;
        int bestFrame = 0;
        for (int frame = 0; frame < idle.images.length; frame++) {
            Image candidate = idle.images[frame];
            if (candidate == finalPose) return frame;
            double distance = 0;
            for (int row = 0; row < 16; row++) {
                for (int col = 0; col < 16; col++) {
                    double x = (col + .5) * width / 16;
                    double y = (row + .5) * height / 16;
                    var first = samplePose(finalPose, x, y);
                    var second = samplePose(candidate, x, y);
                    double a = first.getOpacity(), b = second.getOpacity();
                    distance += Math.abs(a - b) + Math.abs(a * first.getRed() - b * second.getRed())
                            + Math.abs(a * first.getGreen() - b * second.getGreen())
                            + Math.abs(a * first.getBlue() - b * second.getBlue());
                }
            }
            if (distance < bestDistance) {
                bestDistance = distance;
                bestFrame = frame;
            }
        }
        return bestFrame;
    }

    private javafx.scene.paint.Color samplePose(Image image, double x, double y) {
        double scale = scale(image);
        double sampleX = (x - (width - image.getWidth() * scale) / 2) / scale;
        double sampleY = (y - (height - image.getHeight() * scale)) / scale;
        if (sampleX < 0 || sampleX >= image.getWidth() || sampleY < 0 || sampleY >= image.getHeight()) {
            return javafx.scene.paint.Color.TRANSPARENT;
        }
        return image.getPixelReader().getColor((int) sampleX, (int) sampleY);
    }

    private void show(Image image) {
        if (current.getImage() == image) return;
        current.setImage(image);
        double scale = scale(image);
        current.setTranslateX((width - image.getWidth() * scale) / 2);
        current.setTranslateY(height - image.getHeight() * scale);
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
