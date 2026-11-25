package pt.escola.gardendefense.combate;

import com.almasb.fxgl.entity.Entity;

/**
 * Projétil em movimento. As plantas disparam para a direita (direcao = 1)
 * e os zombies para a esquerda (direcao = -1).
 */
public final class Projetil {

    public final Entity entity;
    public final int linha;
    public final int dano;
    public final double velocidade;
    public final double lift;
    public final double tamanho;
    /** +1 para a direita, -1 para a esquerda. */
    public final int direcao;

    public Projetil(Entity entity, int linha, int dano, double velocidade,
                    double lift, double tamanho, int direcao) {
        this.entity = entity;
        this.linha = linha;
        this.dano = dano;
        this.velocidade = velocidade;
        this.lift = lift;
        this.tamanho = tamanho;
        this.direcao = direcao < 0 ? -1 : 1;
    }

    /** Verdadeiro quando o projétil viaja para a direita (disparado por uma planta). */
    public boolean paraDireita() {
        return direcao > 0;
    }
}
