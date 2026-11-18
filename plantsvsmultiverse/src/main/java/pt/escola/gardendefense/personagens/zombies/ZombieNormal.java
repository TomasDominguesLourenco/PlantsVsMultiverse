package pt.escola.gardendefense.personagens.zombies;

/**
 * Zombie básico: vida, velocidade e DPS usados pelo jogo desde a v1.
 */
public class ZombieNormal extends Zombie {

    @Override
    public String getNome() {
        return "Zombie Normal";
    }

    @Override
    public int getVidaMax() {
        return 3;
    }

    @Override
    public String getTextura() {
        return "Zombie.png";
    }

    @Override
    public double getLargura() {
        return 38;
    }

    @Override
    public double getAltura() {
        return 80;
    }

    @Override
    public double getVelocidade() {
        return 25;
    }

    @Override
    public double getDanoPorSegundo() {
        return 35;
    }
}
