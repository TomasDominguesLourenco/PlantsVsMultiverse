package pt.escola.gardendefense.personagens.plants;

import javafx.scene.Node;
import javafx.scene.image.ImageView;

/** Tanque de curta distância: prepara a picareta e acerta um golpe forte. */
public final class StevePlant extends Planta {
    private GifPlantView.Frames idle;
    private GifPlantView.Frames attack;

    private void carregarAnimacoes() {
        if (idle == null) {
            var idleFrames = GifPlantView.carregar("SteveIdle.gif");
            var attackFrames = GifPlantView.carregar("SteveAttack.gif");
            idle = idleFrames;
            attack = attackFrames;
        }
    }

    @Override public Node criarVista() {
        carregarAnimacoes();
        return new GifPlantView(idle, attack, getLargura(), getAltura());
    }

    @Override public Node criarIcone() {
        carregarAnimacoes();
        return new ImageView(idle.images[0]);
    }

    @Override public String getNome() { return "Steve"; }
    @Override public String getTextura() { return "SteveIdle.gif"; }
    @Override public int getOrdemNaBarra() { return 20; }
    @Override public double getLargura() { return 84; }
    @Override public double getAltura() { return 84; }
    @Override public int getVidaMax() { return 600; }
    @Override public int getCustoSol() { return 150; }
    @Override public int getDano() { return 10; }
    @Override public double getIntervaloTiro() { return 2.5; }
    @Override public boolean podeDisparar() { return false; }
    @Override public double getAlcanceCorpoACorpo() { return 40; }
    @Override public double getAtrasoImpacto() { return .4; }
    @Override public double getVelocidadeProjetil() { return 0; }
}
