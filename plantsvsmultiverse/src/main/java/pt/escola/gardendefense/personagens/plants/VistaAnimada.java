package pt.escola.gardendefense.personagens.plants;

/** Animações avançam apenas com o tempo da partida, respeitando a pausa. */
public interface VistaAnimada {
    void advance(double seconds);

    default void atacar() {}
}
