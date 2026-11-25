package pt.escola.gardendefense.combate;

/**
 * Descrição de um disparo: imagem, dano, velocidade e intervalo entre tiros.
 * Um personagem devolve este objeto para declarar a sua arma;
 * se devolver null, o personagem não dispara.
 */
public record ProjetilConfig(String textura, int dano, double velocidade, double intervalo,
                             double lift, double tamanho) {

    /** Altura acima do chão, igual ao tiro base das plantas. */
    public static final double LIFT_PADRAO = 32;
    /** Diâmetro em pixels do projétil base. */
    public static final double TAMANHO_PADRAO = 16;

    /** Arma com lift e tamanho por omissão. */
    public ProjetilConfig(String textura, int dano, double velocidade, double intervalo) {
        this(textura, dano, velocidade, intervalo, LIFT_PADRAO, TAMANHO_PADRAO);
    }

    public ProjetilConfig {
        if (textura == null || textura.isBlank()) {
            throw new IllegalArgumentException("O projétil precisa de uma textura em assets/textures.");
        }
        if (dano < 0) {
            throw new IllegalArgumentException("O dano do projétil não pode ser negativo.");
        }
        if (velocidade <= 0) {
            throw new IllegalArgumentException("A velocidade do projétil tem de ser maior que 0.");
        }
        if (intervalo <= 0) {
            throw new IllegalArgumentException("O intervalo entre tiros tem de ser maior que 0.");
        }
        if (tamanho <= 0) {
            throw new IllegalArgumentException("O tamanho do projétil tem de ser maior que 0.");
        }
    }
}
