package pt.escola.gardendefense.personagens.zombies;

import javafx.scene.Node;
import pt.escola.gardendefense.combate.ProjetilConfig;
import pt.escola.gardendefense.personagens.Personagem;

import static com.almasb.fxgl.dsl.FXGL.texture;

/**
 * Tipo base de todos os zombies: define velocidade e dano por segundo
 * aplicado às plantas que bloqueiam o caminho.
 *
 * <p>Comportamento opcional, tudo com valores por omissão:</p>
 * <ul>
 *   <li>{@link #getArma()} — imagem, dano, velocidade e intervalo de um tiro próprio.</li>
 *   <li>{@link #getPesoSpawn()} — quanto pesa este tipo na sorta dos spawns.</li>
 *   <li>{@link #atualizar(ContextoZombie, double)} — habilidade especial por frame.</li>
 * </ul>
 *
 * <p>Cada zombie ativo é uma instância própria, guardada por si em campos da
 * classe (ex.: {@code private double cooldown;}), sem ser partilhada com os
 * outros zombies do mesmo tipo.</p>
 */
public abstract class Zombie implements Personagem {

    /** Ordem de descoberta dos moldes no catálogo. */
    public int getOrdemNoCatalogo() {
        return 100;
    }

    public Node criarVista() {
        return texture(getTextura(), getLargura(), getAltura());
    }

    /**
     * Peso deste tipo na escolha aleatória ao spawnar.
     * Se a soma dos pesos de todos os zombies der 100, os valores são
     * percentagens exatas (5 = 5% dos spawns). Peso 0 impede o tipo de aparecer.
     */
    public int getPesoSpawn() {
        return 100;
    }

    /**
     * Arma opcional deste zombie. Por omissão devolve null, ou seja,
     * o zombie não atira. Para um zombie com pistolas:
     * {@code return new ProjetilConfig("pistola.png", 2, 300, 1.2);}
     * onde 2 é o dano, 300 a velocidade em px/s e 1.2 o intervalo em segundos.
     */
    public ProjetilConfig getArma() {
        return null;
    }

    /**
     * Vista do projétil deste zombie. Por omissão usa a textura de {@link #getArma()}.
     * Cada chamada deve devolver um Node novo.
     */
    public Node criarVistaProjetil(double tamanho) {
        ProjetilConfig arma = getArma();
        if (arma == null) {
            throw new IllegalStateException(getNome() + " não tem arma; cria getArma() antes de criar o projétil.");
        }
        return texture(arma.textura(), tamanho, tamanho);
    }

    /**
     * Habilidade especial, chamada a cada frame enquanto a partida decorre.
     * Por omissão não faz nada. O contexto dá acesso ao próprio zombie e ao
     * campo de batalha (projéteis, plantas, linha, vida).
     */
    public void atualizar(ContextoZombie contexto, double tpf) {
        // Sem habilidade por omissão.
    }

    /** Velocidade de deslocação em pixels por segundo. */
    public abstract double getVelocidade();

    /** Dano por segundo infligido às plantas que bloqueiam o caminho. */
    public abstract double getDanoPorSegundo();
}
