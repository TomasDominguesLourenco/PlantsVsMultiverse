package pt.escola.gardendefense.personagens.zombies;

import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.jar.JarFile;

/** Discovers compiled zombie templates in this package, both in the IDE and in a JAR. */
public final class CatalogoZombies {
    private CatalogoZombies() {}

    public static List<Zombie> carregar() {
        String pacote = Zombie.class.getPackageName();
        String prefixo = pacote.replace('.', '/') + "/";
        try {
            Path origem = Path.of(Zombie.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            List<String> classes;
            if (Files.isDirectory(origem)) {
                Path pasta = origem.resolve(prefixo);
                try (var ficheiros = Files.walk(pasta)) {
                    classes = ficheiros.filter(Files::isRegularFile)
                            .map(pasta::relativize)
                            .map(Path::toString)
                            .map(nome -> prefixo + nome.replace('\\', '/'))
                            .filter(CatalogoZombies::classeDeTopo)
                            .sorted().toList();
                }
            } else {
                try (var jar = new JarFile(origem.toFile())) {
                    classes = jar.stream().map(java.util.jar.JarEntry::getName)
                            .filter(nome -> nome.startsWith(prefixo))
                            .filter(CatalogoZombies::classeDeTopo)
                            .sorted().toList();
                }
            }

            List<Zombie> zombies = new ArrayList<>();
            for (String ficheiro : classes) {
                String nome = ficheiro.substring(0, ficheiro.length() - 6).replace('/', '.');
                Class<?> tipo = Class.forName(nome, false, Zombie.class.getClassLoader());
                if (!Zombie.class.isAssignableFrom(tipo)) {
                    if (compilacaoPartiada(tipo)) {
                        System.err.println("AVISO: " + tipo.getSimpleName()
                                + " foi compilada em modo partiada pelo VS Code e não estende Zombie,"
                                + " por isso não aparece no jogo. Corre 'mvn clean compile'.");
                    }
                    continue;
                }
                if (tipo.isInterface() || Modifier.isAbstract(tipo.getModifiers())) continue;
                try {
                    zombies.add((Zombie) tipo.getConstructor().newInstance());
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException("O zombie " + nome
                            + " precisa de uma classe pública e um construtor público sem argumentos.", e);
                }
            }
            zombies.sort(Comparator.comparingInt(Zombie::getOrdemNoCatalogo)
                    .thenComparing(Zombie::getNome).thenComparing(zombie -> zombie.getClass().getName()));
            if (zombies.isEmpty()) throw new IllegalStateException("Não existem moldes de zombies em " + pacote);
            return List.copyOf(zombies);
        } catch (java.io.IOException | java.net.URISyntaxException | ClassNotFoundException e) {
            throw new IllegalStateException("Não foi possível carregar o catálogo de zombies.", e);
        }
    }

    /**
     * Cria uma instância nova a partir de um molde do catálogo.
     * Cada zombie ativo recebe a sua própria cópia, para as habilidades
     * guardarem estado (cooldowns, contadores) sem afetar os outros.
     */
    public static Zombie novaInstancia(Zombie molde) {
        try {
            return (Zombie) molde.getClass().getConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("O zombie " + molde.getClass().getName()
                    + " precisa de um construtor público sem argumentos.", e);
        }
    }

    /**
     * Deteta os stubs que o ECJ do VS Code grava quando compila sem classpath:
     * todos os métodos passam a lançar {@code Error("Unresolved compilation ...")}
     * e a classe deixa de estender a sua superclasse.
     */
    private static boolean compilacaoPartiada(Class<?> tipo) {
        try (var in = tipo.getResourceAsStream(tipo.getSimpleName() + ".class")) {
            if (in == null) return false;
            // Montado em runtime para a frase não ficar no bytecode desta classe
            // (senão esta classe detetava-se a si própria).
            String alvo = new String(new char[]{'U', 'n', 'r', 'e', 's', 'o', 'l', 'v', 'e', 'd', ' ',
                    'c', 'o', 'm', 'p', 'i', 'l', 'a', 't', 'i', 'o', 'n'});
            String conteudo = new String(in.readAllBytes(),
                    java.nio.charset.StandardCharsets.ISO_8859_1);
            return conteudo.contains(alvo);
        } catch (java.io.IOException e) {
            return false;
        }
    }

    private static boolean classeDeTopo(String nome) {
        return nome.endsWith(".class") && !nome.contains("$");
    }
}
