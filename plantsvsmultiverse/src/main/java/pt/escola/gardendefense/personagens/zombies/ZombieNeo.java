package pt.escola.gardendefense.personagens.zombies;

import pt.escola.gardendefense.combate.Projetil;

/**
 * Esquiva-se das balas das plantas com uma inclinação para trás, como o Neo
 * no Matrix. Só efeito visual: não muda de linha nem fica invulnerável.
 */
public class ZombieNeo extends Zombie {

    /** Distância (px) a que a bala dispara a esquiva, para o pico da inclinação coincidir com a passagem. */
    private static final double DISTANCIA_BALA = 90;
    /** Segundos entre esquivas — é isto que torna a esquiva ocasional. */
    private static final double COOLDOWN_ESQUIVA = 4;
    private static final double DURACAO_ESQUIVA = 0.5;
    private static final double ANGULO_ESQUIVA = 24;

    private double cooldown;
    private double tempoEsquiva;

    @Override
    public void atualizar(ContextoZombie contexto, double tpf) {
        cooldown = Math.max(0, cooldown - tpf);

        if (tempoEsquiva <= 0 && cooldown <= 0 && balaAPontoDeAcertar(contexto)) {
            cooldown = COOLDOWN_ESQUIVA;
            tempoEsquiva = DURACAO_ESQUIVA;
        }

        if (tempoEsquiva > 0) {
            tempoEsquiva -= tpf;
            double progresso = Math.max(0, tempoEsquiva / DURACAO_ESQUIVA);
            contexto.inclinar(ANGULO_ESQUIVA * Math.sin(Math.PI * progresso));
        } else {
            contexto.inclinar(0);
        }
    }

    /** Verdadeiro quando uma bala da planta já está a caminho e a poucos pixels de chegar. */
    private boolean balaAPontoDeAcertar(ContextoZombie contexto) {
        double centroZombie = contexto.entidade().getX() + getLargura() / 2;
        for (Projetil projetil : contexto.projeteisInimigos()) {
            double gap = centroZombie - (projetil.entity.getX() + projetil.tamanho);
            if (gap >= 0 && gap <= DISTANCIA_BALA) {
                return true;
            }
        }
        return false;
    }

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
