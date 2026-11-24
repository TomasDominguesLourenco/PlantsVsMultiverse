package pt.escola.gardendefense.personagens.plants;

import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.jar.JarFile;

/** Discovers compiled plant templates in this package, both in the IDE and in a JAR. */
public final class CatalogoPlantas {
    private CatalogoPlantas() {}

    public static List<Planta> carregar() {
        String pacote = Planta.class.getPackageName();
        String prefixo = pacote.replace('.', '/') + "/";
        try {
            Path origem = Path.of(Planta.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            List<String> classes;
            if (Files.isDirectory(origem)) {
                Path pasta = origem.resolve(prefixo);
                try (var ficheiros = Files.walk(pasta)) {
                    classes = ficheiros.filter(Files::isRegularFile)
                            .map(pasta::relativize)
                            .map(Path::toString)
                            .map(nome -> prefixo + nome.replace('\\', '/'))
                            .filter(CatalogoPlantas::classeDeTopo)
                            .sorted().toList();
                }
            } else {
                try (var jar = new JarFile(origem.toFile())) {
                    classes = jar.stream().map(java.util.jar.JarEntry::getName)
                            .filter(nome -> nome.startsWith(prefixo))
                            .filter(CatalogoPlantas::classeDeTopo)
                            .sorted().toList();
                }
            }

            List<Planta> plantas = new ArrayList<>();
            for (String ficheiro : classes) {
                String nome = ficheiro.substring(0, ficheiro.length() - 6).replace('/', '.');
                Class<?> tipo = Class.forName(nome, false, Planta.class.getClassLoader());
                if (!Planta.class.isAssignableFrom(tipo) || tipo.isInterface()
                        || Modifier.isAbstract(tipo.getModifiers())) continue;
                try {
                    plantas.add((Planta) tipo.getConstructor().newInstance());
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException("A planta " + nome
                            + " precisa de uma classe pública e um construtor público sem argumentos.", e);
                }
            }
            plantas.sort(Comparator.comparingInt(Planta::getOrdemNaBarra)
                    .thenComparing(Planta::getNome).thenComparing(planta -> planta.getClass().getName()));
            if (plantas.isEmpty()) throw new IllegalStateException("Não existem moldes de plantas em " + pacote);
            return List.copyOf(plantas);
        } catch (java.io.IOException | java.net.URISyntaxException | ClassNotFoundException e) {
            throw new IllegalStateException("Não foi possível carregar o catálogo de plantas.", e);
        }
    }

    private static boolean classeDeTopo(String nome) {
        return nome.endsWith(".class") && !nome.contains("$");
    }
}
