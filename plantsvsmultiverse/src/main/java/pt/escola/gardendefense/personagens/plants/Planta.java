package pt.escola.gardendefense.personagens.plants;

import javafx.scene.Node;
import pt.escola.gardendefense.personagens.Personagem;

import static com.almasb.fxgl.dsl.FXGL.texture;

/**
 * Molde de planta: define imagem, custo, vida, dano e cadência de tiro.
 * Cria uma classe pública neste pacote (ou num subpacote), com construtor público
 * sem argumentos. Depois de compilar e reiniciar, CatalogoPlantas coloca-a na barra
 * automaticamente; não é necessário alterar a aplicação nem a interface.
 */
public abstract class Planta implements Personagem {

    /** Ordem do cartão no catálogo; valores menores aparecem primeiro. */
    public int getOrdemNaBarra() {
        return 100;
    }

    /** Vista independente para o cartão; spritesheets mostram o primeiro fotograma. */
    public Node criarIcone() {
        return criarVista();
    }

    public Node criarVista() {
        return texture(getTextura(), getLargura(), getAltura());
    }

    public boolean podeDisparar() {
        return true;
    }

    public Node criarVistaProjetil(double tamanho) {
        return texture("ProjectilePea.png", tamanho, tamanho);
    }

    public double getAlturaProjetil() {
        return 32;
    }

    /** Custo em energia para plantar. */
    public abstract int getCustoSol();

    /** Dano causado por cada projétil. */
    public abstract int getDano();

    /** Segundos entre cada tiro. */
    public abstract double getIntervaloTiro();

    /** Velocidade do projétil em pixels por segundo. */
    public abstract double getVelocidadeProjetil();
}
