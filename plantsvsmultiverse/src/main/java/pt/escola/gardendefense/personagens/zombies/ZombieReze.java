package pt.escola.gardendefense.personagens.zombies;

import pt.escola.gardendefense.combate.AlvoPlanta;

/**
 * Kamikaze: quando chega perto de uma planta, em vez de a começar a comer,
 * explode e destrói todas as plantas numa área 3 × 3 com o pivot no próprio
 * zombie — e morre na explosão.
 */
public class ZombieReze extends Zombie {

    /** Distância (px lógicos) à planta que dispara a explosão. */
    private static final double DISTANCIA_EXPLOSAO = 40;
    /** Raio da explosão em células: 1 ⇒ área 3 × 3. */
    private static final int RAIO_EXPLOSAO = 1;
    private static final int DANO_EXPLOSAO = 999_999;
    private static final double RAIO_ONDA = 140;
    private static final double DURACAO_ONDA = 0.3;

    private boolean explodiu;

    @Override
    public void atualizar(ContextoZombie contexto, double tpf) {
        if (explodiu || !chegouPertoDePlanta(contexto)) {
            return;
        }
        explodiu = true;

        for (AlvoPlanta alvo : contexto.plantasNaArea(RAIO_EXPLOSAO, RAIO_EXPLOSAO)) {
            contexto.danificar(alvo, DANO_EXPLOSAO);
        }
        contexto.ondaDeChoque(RAIO_ONDA, DURACAO_ONDA);
        contexto.vida(0);
    }

    /** Verdadeiro quando falta 40px ou menos para encostar numa planta da linha. */
    private boolean chegouPertoDePlanta(ContextoZombie contexto) {
        double zEsquerda = contexto.entidade().getX();
        double zDireita = zEsquerda + getLargura();

        for (AlvoPlanta alvo : contexto.plantasNaLinha()) {
            if (alvo.x() >= zDireita) {
                continue;
            }
            if (zEsquerda - (alvo.x() + alvo.largura()) <= DISTANCIA_EXPLOSAO) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getPesoSpawn() {
        return 5;
    }

    @Override
    public String getNome() {
        return "Zombie Reze";
    }

    @Override
    public int getVidaMax() {
        return 3;
    }

    @Override
    public String getTextura() {
        return "zombies_sprites/reze_sprite.png";
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
        return 50;
    }

    @Override
    public double getDanoPorSegundo() {
        return 35;
    }
}
