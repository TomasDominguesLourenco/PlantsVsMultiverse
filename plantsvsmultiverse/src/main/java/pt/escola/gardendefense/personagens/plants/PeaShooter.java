package pt.escola.gardendefense.personagens.plants;

/**
 * Planta básica que atira ervilhas numa linha.
 */
public class PeaShooter extends Planta {

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
        return "PeaShooter.png";
    }

    @Override
    public double getLargura() {
        return 52;
    }

    @Override
    public double getAltura() {
        return 58;
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
