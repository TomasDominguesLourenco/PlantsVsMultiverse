package pt.escola.gardendefense.personagens.plants;

import pt.escola.gardendefense.personagens.Personagem;

/**
 * Tipo base de todas as plantas: define custo, dano e cadência de tiro.
 */
public abstract class Planta implements Personagem {

    /** Custo em energia para plantar. */
    public abstract int getCustoSol();

    /** Dano causado por cada projétil. */
    public abstract int getDano();

    /** Segundos entre cada tiro. */
    public abstract double getIntervaloTiro();

    /** Velocidade do projétil em pixels por segundo. */
    public abstract double getVelocidadeProjetil();
}
