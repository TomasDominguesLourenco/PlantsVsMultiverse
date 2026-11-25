package pt.escola.gardendefense.personagens.plants;

import javafx.scene.Node;

import static com.almasb.fxgl.dsl.FXGL.texture;

public class MikuPlant extends PeaShooter {
    private GifPlantView.Frames idle;
    private GifPlantView.Frames shooting;

    @Override public String getNome() { return "Miku Plant"; }
    @Override public String getTextura() { return "mikuIdle.gif"; }
    @Override public int getCustoSol() { return 75; }
    @Override public int getVidaMax() { return 150; }
    @Override public int getDano() { return 1; }
    @Override public double getIntervaloTiro() { return 2.4; }
    @Override public int getTirosPorRajada() { return 3; }
    @Override public double getIntervaloEntreTiros() { return 0.12; }
    @Override public double getVelocidadeProjetil() { return 300; }
    @Override public int getOrdemNaBarra() { return 20; }
    @Override public Node criarIcone() { return texture("mikuwow.png", getLargura(), getAltura()); }
    @Override
    public Node criarVistaProjetil(double tamanho) {
        var bala = texture("mikushot.gif", tamanho, tamanho);
        bala.setScaleX(1.5);
        bala.setScaleY(1.5);
        return bala;
    }
    @Override
    public Node criarVista() {
        if (idle == null) {
            idle = GifPlantView.carregar(getTextura());
        }
        if (shooting == null) {
            shooting = GifPlantView.carregar("mikushooting.gif");
        }
        return new GifPlantView(idle, shooting, getLargura(), getAltura());
    }
    @Override public double getAtrasoDisparo() { return 0.32; }
}
