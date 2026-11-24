package pt.escola.gardendefense.personagens.plants;

import javafx.scene.Node;
import javafx.scene.image.ImageView;

public final class GokuPlant extends Planta {
    private GifPlantView.Frames idle;
    private GifPlantView.Frames attack;

    private void carregarAnimacoes() {
        if (idle == null) {
            var idleFrames = GifPlantView.carregar("GokuIdle.gif");
            var attackFrames = GifPlantView.carregar("GokuAttack.gif");
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

    @Override public Node criarVistaProjetil(double tamanho) { return new KiProjectileView(tamanho); }
    @Override public String getNome() { return "Goku Planta"; }
    @Override public String getTextura() { return "GokuIdle.gif"; }
    @Override public int getOrdemNaBarra() { return 10; }
    @Override public double getLargura() { return 88; }
    @Override public double getAltura() { return 88; }
    @Override public int getVidaMax() { return 450; }
    @Override public int getCustoSol() { return 100; }
    @Override public int getDano() { return 8; }
    @Override public double getIntervaloTiro() { return 3.2; }
    @Override public double getVelocidadeProjetil() { return 350; }
    @Override public double getAlturaProjetil() { return 32; }
    @Override public double getSaidaProjetilX() { return getLargura() * .78; }
    @Override public double getAtrasoDisparo() { return .4; }
}
