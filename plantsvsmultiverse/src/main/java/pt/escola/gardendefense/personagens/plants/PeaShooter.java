package pt.escola.gardendefense.personagens.plants;

import javafx.scene.Node;

/**
 * Planta básica que atira ervilhas numa linha.
 */
public abstract class PeaShooter extends Planta {

    @Override
    public Node criarVista() {
        // Custom templates inheriting PeaShooter can still supply their own texture.
        return "PeaShooterParts.png".equals(getTextura()) ? new PeaShooterView() : super.criarVista();
    }

    @Override
    public int getOrdemNaBarra() { return 0; }

    @Override
    public String getNome() {
        return "PeaShooter";
    }

    @Override
    public int getVidaMax() {
        return 100;
    }

    @Override
    public String getTextura() {
        return "PeaShooterParts.png";
    }

    @Override
    public double getLargura() {
        return 88;
    }

    @Override
    public double getAltura() {
        return 88;
    }

    @Override
    public int getCustoSol() {
        return 50;
    }

    @Override
    public int getDano() {
        return 1;
    }

    @Override
    public double getIntervaloTiro() {
        return 1.3;
    }

    @Override
    public double getVelocidadeProjetil() {
        return 300;
    }
}
