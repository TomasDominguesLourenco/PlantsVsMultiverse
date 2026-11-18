package pt.escola.gardendefense.personagens.zombies;

import pt.escola.gardendefense.personagens.Personagem;

/**
 * Tipo base de todos os zombies: define velocidade e dano por segundo
 * aplicado às plantas que bloqueiam o caminho.
 */
public abstract class Zombie implements Personagem {

    /** Velocidade de deslocação em pixels por segundo. */
    public abstract double getVelocidade();

    /** Dano por segundo infligido às plantas. */
    public abstract double getDanoPorSegundo();
}
