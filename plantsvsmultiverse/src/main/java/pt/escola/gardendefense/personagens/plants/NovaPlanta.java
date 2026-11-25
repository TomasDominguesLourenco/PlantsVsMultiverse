// Copia para src/main/java/pt/escola/gardendefense/personagens/plants/NovaPlanta.java.
// Se mudares o nome da classe, muda também o nome do ficheiro .java.
// O catálogo encontra a nova planta quando recompilas e reinicias o jogo.
package pt.escola.gardendefense.personagens.plants;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import static com.almasb.fxgl.dsl.FXGL.texture;

public class NovaPlanta extends PeaShooter {
    // 1. Identidade e posição do cartão (valor menor = mais à esquerda).
    private static final String NOME = "Red Plant"; // Nome do cartão e da planta no tabuleiro.
    private static final int ORDEM_NA_BARRA = 30;

    // 2. Arte. Guarda os ficheiros em src/main/resources/assets/textures/.
    // Usa os nomes exatos, incluindo maiúsculas e minúsculas.
    private static final String IDLE = "redidle.gif"; // PNG ou GIF no tabuleiro.
    private static final String ATAQUE = "RedCatapult.png";
    private static final String BALA = "RedProjectile.png";

    // 3. Tamanho no tabuleiro e atributos.
    private static final double LARGURA = 88;
    private static final double ALTURA = 88;
    private static final int CUSTO_SOL = 75;
    private static final int VIDA = 150;
    private static final int DANO_POR_BALA = 2;
    private static final double VELOCIDADE_BALA = 390;

    // 4. Cadência. Todos os tempos estão em segundos.
    private static final int FOTOGRAMA_LANCAMENTO = 10;
    private static final double DURACAO_PREPARACAO = 0.65;
    private static final double RECARGA = 5.0;
    private static final double REPOSICAO = 0.36;
    private static final Rectangle2D RED_BOUNDS = new Rectangle2D(320, 55, 865, 850);

    private GifPlantView.Frames idle;
    private GifPlantView.Frames ataque;

    @Override public String getNome() { return NOME; }
    @Override public String getTextura() { return IDLE; }
    @Override public int getOrdemNaBarra() { return ORDEM_NA_BARRA; }
    @Override public double getLargura() { return LARGURA; }
    @Override public double getAltura() { return ALTURA; }
    @Override public int getCustoSol() { return CUSTO_SOL; }
    @Override public int getVidaMax() { return VIDA; }
    @Override public int getDano() { return DANO_POR_BALA; }
    @Override public int getDanoProjetil(int vidaMaxAlvo) {
        return Math.max(DANO_POR_BALA, Math.ceilDiv(vidaMaxAlvo, 2));
    }
    @Override public double getVelocidadeProjetil() { return VELOCIDADE_BALA; }
    @Override public double getIntervaloTiro() { return DURACAO_PREPARACAO + RECARGA + REPOSICAO; }
    @Override public boolean usaTrajetoriaEmArco() { return true; }

    @Override
    public Node criarIcone() {
        carregarAnimacoes();
        return new ImageView(idle.images[0]);
    }

    @Override
    public Node criarVista() {
        carregarAnimacoes();
        return new GifPlantView(idle, ataque, LARGURA, ALTURA, FOTOGRAMA_LANCAMENTO,
                new Point2D(0.33, 0.32), false);
    }

    private void carregarAnimacoes() {
        if (ataque == null) {
            ataque = carregarCatapulta(texture(ATAQUE).getImage(), texture(BALA).getImage());
            // Repouso e ataque partilham as poses do mesmo atlas e o mesmo ponto de apoio.
            var frames = ataque.images;
            idle = new GifPlantView.Frames(new Image[]{frames[0], frames[1]},
                    new double[]{0.34, 0.34});
        }
    }

    private static GifPlantView.Frames carregarCatapulta(Image sheet, Image red) {
        int posesReposicao = 8;
        Image[] frames = new Image[19 + posesReposicao + 1];
        double[] tempos = new double[frames.length];
        int[] carregar = {318, 594, 867, 1140, 1411, 1681, 1948};
        int[] lancar = {311, 591, 878};
        int[] recarregar = {319, 598, 878, 1159, 1438, 1717, 1997};
        int index = 0;
        for (int centro : carregar) {
            // O painel 2 tem uma mancha escura no Red. Mantém a pose anterior pelo mesmo tempo.
            frames[index] = index == 2 ? frames[index - 1] : recortar(sheet, centro, 2, 202);
            index++;
        }
        for (int centro : lancar) frames[index++] = recortar(sheet, centro, 245, 446);
        // O painel 10 contém um Red em voo. O voo é desenhado pelo projétil do jogo.
        frames[index++] = recortar(sheet, 1537, 245, 446);
        frames[index++] = recortar(sheet, 1850, 245, 446);
        for (int centro : recarregar) frames[index++] = recortar(sheet, centro, 495, 686);
        int inicioReposicao = index;
        Image catapultaVazia = frames[index - 1];
        BufferedImage baseVazia = copiarPixels(catapultaVazia, 0, 0,
                (int) catapultaVazia.getWidth(), (int) catapultaVazia.getHeight());
        BufferedImage tomate = copiarPixels(red, (int) RED_BOUNDS.getMinX(), (int) RED_BOUNDS.getMinY(),
                (int) RED_BOUNDS.getWidth(), (int) RED_BOUNDS.getHeight());
        for (int i = 0; i < posesReposicao; i++) {
            frames[index++] = animarReposicao(baseVazia, tomate, i / (double) posesReposicao);
        }
        frames[index] = frames[0];
        java.util.Arrays.fill(tempos, 0, FOTOGRAMA_LANCAMENTO,
                DURACAO_PREPARACAO / FOTOGRAMA_LANCAMENTO);
        tempos[10] = 0.09;
        tempos[11] = 0.10;
        java.util.Arrays.fill(tempos, 12, inicioReposicao, (RECARGA - 0.19) / recarregar.length);
        java.util.Arrays.fill(tempos, inicioReposicao, tempos.length, REPOSICAO / (posesReposicao + 1));
        return new GifPlantView.Frames(frames, tempos);
    }

    private static BufferedImage copiarPixels(Image image, int x, int y, int largura, int altura) {
        int[] pixels = new int[largura * altura];
        image.getPixelReader().getPixels(x, y, largura, altura, PixelFormat.getIntArgbInstance(), pixels, 0, largura);
        var resultado = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_ARGB);
        resultado.setRGB(0, 0, largura, altura, pixels, 0, largura);
        return resultado;
    }

    private static Image animarReposicao(BufferedImage base, BufferedImage red, double progresso) {
        int largura = base.getWidth(), altura = base.getHeight();
        var pose = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_ARGB);
        var desenho = pose.createGraphics();
        try {
            desenho.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            desenho.drawImage(base, 0, 0, null);
            int x = (int) Math.round(27 + 13 * progresso);
            double queda = progresso * progresso * (3 - 2 * progresso);
            int y = (int) Math.round(-82 + 158 * queda);
            desenho.drawImage(red, x, y, 85, 82, null);
        } finally {
            desenho.dispose();
        }
        var frame = new WritableImage(largura, altura);
        frame.getPixelWriter().setPixels(0, 0, largura, altura, PixelFormat.getIntArgbInstance(),
                pose.getRGB(0, 0, largura, altura, null, 0, largura), 0, largura);
        return frame;
    }

    private static Image recortar(Image sheet, int centro, int topo, int base) {
        int tamanho = 264;
        int altura = base - topo;
        var frame = new WritableImage(tamanho, tamanho);
        var origem = sheet.getPixelReader();
        var destino = frame.getPixelWriter();
        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < tamanho; x++) {
                int pixel = origem.getArgb(centro - tamanho / 2 + x, topo + y);
                if ((pixel >>> 24) >= 24) {
                    destino.setArgb(x, 256 - altura + y, pixel);
                }
            }
        }
        return frame;
    }

    @Override
    public Node criarVistaProjetil(double tamanho) {
        var bala = new ImageView(texture(BALA).getImage());
        bala.setViewport(RED_BOUNDS);
        bala.setFitWidth(tamanho);
        bala.setFitHeight(tamanho);
        bala.setScaleX(1.9);
        bala.setScaleY(1.9);
        return bala;
    }
}
