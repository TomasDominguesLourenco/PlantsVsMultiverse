package pt.escola.gardendefense.personagens.zombies;

import javafx.scene.Node;
import pt.escola.gardendefense.personagens.Personagem;

import static com.almasb.fxgl.dsl.FXGL.texture;

/**
 * Tipo base de todos os zombies: define velocidade e dano por segundo
 * aplicado às plantas que bloqueiam o caminho.
 */
public abstract class Zombie implements Personagem {

    /** Ordem de descoberta dos moldes no catálogo. */
    public int getOrdemNoCatalogo() {
        return 100;
    }

    public Node criarVista() {
        return texture(getTextura(), getLargura(), getAltura());
    }

    /** Velocidade de deslocação em pixels por segundo. */
    public abstract double getVelocidade();

    /** Dano por segundo infligido às plantas. */
    public abstract double getDanoPorSegundo();
}
