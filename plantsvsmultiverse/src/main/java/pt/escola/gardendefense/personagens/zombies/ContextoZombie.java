package pt.escola.gardendefense.personagens.zombies;

import com.almasb.fxgl.entity.Entity;
import pt.escola.gardendefense.combate.AlvoPlanta;
import pt.escola.gardendefense.combate.Projetil;
import pt.escola.gardendefense.combate.ProjetilConfig;

import java.util.List;

/**
 * Acesso ao jogo para as habilidades especiais dos zombies.
 * O objeto é criado antes de cada chamada a {@link Zombie#atualizar}
 * e só é válido durante essa chamada.
 *
 * <p>Exemplos do que dá para fazer: ler {@link #projeteisInimigos()} e
 * chamar {@link #mudarLinha(int)} para desviar de um tiro, ou usar
 * {@link #plantasNaLinha()} com {@link #danificar(AlvoPlanta, int)}
 * num ataque especial.</p>
 */
public interface ContextoZombie {

    /** Entidade gráfica do próprio zombie. */
    Entity entidade();

    /** Linha lógica do tabuleiro (0 = fundo, 4 = frente). */
    int linha();

    /** Muda o zombie de linha. Devolve false se a linha não existir. */
    boolean mudarLinha(int linha);

    /** Velocidade atual em pixels por segundo. */
    double velocidade();

    /** Define a velocidade em pixels por segundo, substituindo a calculada pelo jogo. */
    void velocidade(double pxPorSegundo);

    /** Vida atual do zombie. */
    double vida();

    /** Define a vida atual; um valor igual ou menor que 0 mata o zombie. */
    void vida(double vida);

    /** Projéteis das plantas que ainda estão à frente do zombie e a aproximar-se. */
    List<Projetil> projeteisInimigos();

    /** Plantas vivas na mesma linha do zombie. */
    List<AlvoPlanta> plantasNaLinha();

    /**
     * Plantas vivas numa área centrada na célula do próprio zombie:
     * {@code ±raioLinhas} linhas e {@code ±raioColunas} colunas
     * (1, 1 ⇒ área 3 × 3). Sai vazio se a área não tem plantas.
     */
    List<AlvoPlanta> plantasNaArea(int raioLinhas, int raioColunas);

    /** Aplica dano a uma planta devolvida por {@link #plantasNaLinha()}. */
    void danificar(AlvoPlanta alvo, int dano);

    /**
     * Efeito visual de onda de choque centrado no próprio zombie: um anel que
     * se expande até {@code raioMax} (px lógicos) e desaparece em
     * {@code duracao} segundos. Só desenha, não faz dano.
     */
    void ondaDeChoque(double raioMax, double duracao);

    /**
     * Destrói os projéteis inimigos (os das plantas) dentro do raio
     * {@code raioMax} centrado no próprio zombie. Devolve quantos foram
     * bloqueados. Não afeta os projéteis disparados por zombies.
     */
    int bloquearProjeteis(double raioMax);

    /** Dispara o projétil do próprio zombie para a esquerda, na linha indicada. */
    void lancar(ProjetilConfig arma, int linha);

    /** Segundos decorridos desde o início da partida. */
    double tempoDecorrido();

    /**
     * Inclina o sprite do próprio zombie, em graus: positivas fazem o topo
     * inclinar para a direita (para trás de um zombie que anda para a esquerda).
     * Chamar a cada frame — {@code 0} endireita. Só efeito visual: não muda a
     * linha nem a posição lógica, nem dá invulnerabilidade.
     */
    void inclinar(double graus);
}
