package pt.escola.gardendefense.personagens.plants;

import javafx.scene.Node;
import pt.escola.gardendefense.personagens.Personagem;

import static com.almasb.fxgl.dsl.FXGL.texture;

/**
 * Tipo base de todas as plantas: define custo, dano e cadência de tiro.
 */
public abstract class Planta implements Personagem {

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
