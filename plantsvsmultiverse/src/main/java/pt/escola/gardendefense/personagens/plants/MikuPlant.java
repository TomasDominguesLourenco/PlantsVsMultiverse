// MOLDE: copia este ficheiro para src/main/java/pt/escola/gardendefense/
// personagens/plants/NovaPlanta.java (retira a extensão .example).
// Compila e reinicia o jogo: o novo cartão aparece automaticamente.
// Não é preciso editar GardenDefenseApp nem GardenUI.


package pt.escola.gardendefense.personagens.plants;
    
// Herdar de PeaShooter permite começar com os valores de uma planta funcional.
// Para um comportamento totalmente próprio, herda de Planta e implementa os seus métodos.
public class MikuPlant extends PeaShooter {
    private GifPlantView.Frames idle;
    private GifPlantView.Frames shooting;   

    @Override public String getNome() { return "Miku Plant"; }
    @Override public String getTextura() { return "mikuIdle.gif"; } // Imagem em assets/textures.
    @Override public int getCustoSol() { return 75; }
    @Override public int getVidaMax() { return 150; }
    @Override public int getDano() { return 1; }
    @Override public double getIntervaloTiro() { return 2.4; }
    @Override public int getTirosPorRajada() { return 3; }
    @Override public double getIntervaloEntreTiros() { return 0.12; }
    @Override public double getVelocidadeProjetil() { return 300; }
    @Override public int getOrdemNaBarra() { return 20; } // Menor = mais à esquerda.
    @Override public javafx.scene.Node criarIcone() { return com.almasb.fxgl.dsl.FXGL.texture( "mikuwow.png", getLargura(), getAltura()); }
    @Override
    public javafx.scene.Node criarVistaProjetil(double tamanho) {
        var bala = com.almasb.fxgl.dsl.FXGL.texture("mikushot.gif", tamanho, tamanho);
        bala.setScaleX(1.5);
        bala.setScaleY(1.5);
        return bala;
    }
    @Override public javafx.scene.Node criarVista() {if (idle == null) {idle = GifPlantView.carregar(getTextura());} if (shooting == null) {shooting = GifPlantView.carregar("mikushooting.gif");} return new GifPlantView(idle, shooting, getLargura(), getAltura());}
    @Override public double getAtrasoDisparo() {return 0.32;}
    
}   
    // Opcional: sobrescreve criarVista() para animar a planta.
    // criarIcone() reutiliza a vista inicial; podes sobrescrevê-lo para um ícone próprio.
    // Cada chamada deve devolver um Node novo, nunca um Node partilhado.
    // Mantém a classe pública com construtor público sem argumentos (o implícito serve).
