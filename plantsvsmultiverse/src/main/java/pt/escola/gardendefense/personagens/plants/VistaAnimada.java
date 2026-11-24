package pt.escola.gardendefense.personagens.plants;

import javafx.geometry.Point2D;
import java.util.function.DoubleConsumer;

/** Animações avançam apenas com o tempo da partida, respeitando a pausa. */
public interface VistaAnimada {
    void advance(double seconds);

    default void atacar() {}

    /** Instante de emissão dentro da animação; negativo usa o atraso do molde. */
    default double getMomentoDisparo() { return -1; }

    /** Avisa a emissão e fornece o tempo que já passou desde ela neste frame. */
    default void atacar(DoubleConsumer emitir) { atacar(); emitir.accept(0); }

    /** Boca em coordenadas locais, depois de aplicar a pose atual. */
    default Point2D getSaidaProjetil() { return null; }
}
