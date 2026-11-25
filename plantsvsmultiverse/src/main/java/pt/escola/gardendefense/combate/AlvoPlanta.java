package pt.escola.gardendefense.combate;

/**
 * Vista imutável de uma planta, entregue às habilidades dos zombies.
 * As coordenadas são as do tabuleiro lógico, tal como as entidades as usam.
 */
public record AlvoPlanta(int linha, int coluna, double x, double largura, double altura,
                         double vida, double vidaMax) {

    /** Centro lógico da planta na horizontal. */
    public double centroX() {
        return x + largura / 2;
    }
}
