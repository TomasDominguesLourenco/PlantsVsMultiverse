package pt.escola.gardendefense.personagens;

/**
 * Contrato base de todos os personagens do jogo (plantas e zombies).
 */
public interface Personagem {

    /** Nome de exibição do personagem. */
    String getNome();

    /** Vida máxima do personagem. */
    int getVidaMax();

    /** Ficheiro de textura em assets/textures. */
    String getTextura();

    /** Largura da textura no ecrã. */
    double getLargura();

    /** Altura da textura no ecrã. */
    double getAltura();
}
