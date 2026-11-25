package pt.escola.gardendefense.personagens.zombies;

import pt.escola.gardendefense.combate.Projetil;

/**
 * Zombie básico: vida, velocidade e DPS usados pelo jogo desde a v1.
 * Grita sempre que um projétil vem na sua direção e o grito bloqueia
 * (destrói) os projéteis inimigos à volta dele.
 */
public class ZombieAndreventura extends Zombie {

    /** Distância (px lógicos) a que a bala dispara o grito. */
    private static final double DISTANCIA_GRITO = 90;
    /** Raio do grito em px lógicos — projéteis dentro disto são bloqueados. */
    private static final double RAIO_GRITO = 130;
    private static final double DURACAO_GRITO = 0.35;

    @Override
    public void atualizar(ContextoZombie contexto, double tpf) {
        double zEsquerda = contexto.entidade().getX();
        for (Projetil projetil : contexto.projeteisInimigos()) {
            double gap = zEsquerda - (projetil.entity.getX() + projetil.tamanho);
            if (gap <= DISTANCIA_GRITO) {
                contexto.ondaDeChoque(RAIO_GRITO, DURACAO_GRITO);
                contexto.bloquearProjeteis(RAIO_GRITO);
                return;
            }
        }
    }

    @Override
    public int getPesoSpawn() {
        return 10;
    }

    @Override
    public String getNome() {
        return "Zombie Andre Ventura";
    }

    @Override
    public int getVidaMax() {
        return 3;
    }

    @Override
    public String getTextura() {
        return "zombies_sprites/andre_ventura_sprite.png";
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
