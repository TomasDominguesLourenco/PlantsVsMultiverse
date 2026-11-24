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
                if (!Zombie.class.isAssignableFrom(tipo) || tipo.isInterface()
                        || Modifier.isAbstract(tipo.getModifiers())) continue;
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

    private static boolean classeDeTopo(String nome) {
        return nome.endsWith(".class") && !nome.contains("$");
    }
}
