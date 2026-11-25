package pt.escola.gardendefense.personagens.zombies;

/**
 * Zombie básico: vida, velocidade e DPS usados pelo jogo desde a v1.
 */
public class ZombieNeo extends Zombie {


    /** 5 dos 100 pesos totais, ou seja, 5% dos spawns. */
    @Override
    public int getPesoSpawn() {
        return 5;
    }

    @Override
    public String getNome() {
        return "Zombie Neo";
    }

    @Override
    public int getVidaMax() {
        return 3;
    }

    @Override
    public String getTextura() {
        return "zombies_sprites/neo_sprite.png";
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
