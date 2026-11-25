// Copia este ficheiro para src/main/java/pt/escola/gardendefense/personagens/plants/PlantaFarm.java.
// Recompila e reinicia o jogo para a planta aparecer na barra.
// Se mudares o nome da classe, muda também o nome do ficheiro.
package pt.escola.gardendefense.personagens.plants;

import javafx.scene.Node;

import static com.almasb.fxgl.dsl.FXGL.texture;

public class PlantaFarm extends Planta {
    // Identidade e posição na barra. Um número menor aparece primeiro.
    private static final String NOME = "Doctor Farm"; // Nome do cartão e da planta no tabuleiro.
    private static final int ORDEM_NA_BARRA = -1;

    // Os ficheiros ficam em src/main/resources/assets/textures/.
    // IDLE pode ser PNG ou GIF; usa PNG em ICONE para um cartão fixo.
    private static final String IDLE = "doctorfarm.gif";
    private static final String ICONE = "doctorfarm.png";
    private static final String ANIMACAO_PRODUCAO = "farmadainsana.gif";
    private static final int FOTOGRAMA_EMISSAO_SOL = 6;
    private static final double LARGURA = 74;
    private static final double ALTURA = 74;

    private static final int CUSTO_SOL = 100;
    private static final int VIDA = 100;
    private static final int SOL_POR_RECOLHA = 25;
    private static final double SEGUNDOS_ENTRE_SOIS = 20;
    private GifPlantView.Frames idle;
    private GifPlantView.Frames producao;

    @Override public String getNome() { return NOME; }
    @Override public String getTextura() { return IDLE; }
    @Override public int getOrdemNaBarra() { return ORDEM_NA_BARRA; }
    @Override public double getLargura() { return LARGURA; }
    @Override public double getAltura() { return ALTURA; }
    @Override public int getCustoSol() { return CUSTO_SOL; }
    @Override public int getVidaMax() { return VIDA; }
    @Override public int getSolProduzido() { return SOL_POR_RECOLHA; }
    @Override public double getIntervaloProducaoSol() { return SEGUNDOS_ENTRE_SOIS; }

    // A planta produz sóis para recolher com um clique.
    @Override public boolean podeDisparar() { return false; }
    @Override public int getDano() { return 0; }
    @Override public double getIntervaloTiro() { return 0; }
    @Override public double getVelocidadeProjetil() { return 0; }

    @Override
    public Node criarIcone() {
        return texture(ICONE, LARGURA, ALTURA);
    }

    @Override
    public Node criarVista() {
        if (idle == null) {
            idle = GifPlantView.carregar(IDLE);
        }
        if (producao == null) {
            producao = GifPlantView.carregar(ANIMACAO_PRODUCAO);
        }
        return new GifPlantView(idle, producao, LARGURA, ALTURA, FOTOGRAMA_EMISSAO_SOL, null, false);
    }
}
