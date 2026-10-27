# Garden Defense — projeto escolar com FXGL

Protótipo Java inspirado em jogos de defesa de jardim, feito com FXGL.

## Requisitos

- JDK 21
- Maven
- VS Code com as extensões **Extension Pack for Java** e **Maven for Java**

## Instalar no Ubuntu/Debian

```bash
sudo apt update
sudo apt install openjdk-21-jdk maven
```

Confirma as instalações:

```bash
java -version
javac -version
mvn -version
```

## Abrir no VS Code

1. Extrai o ZIP.
2. No VS Code, escolhe **File → Open Folder** e abre a pasta `GardenDefense`.
3. Abre um terminal integrado na pasta onde está o `pom.xml`.
4. Compila e descarrega as dependências:

```bash
mvn clean compile
```

5. Executa o jogo:

```bash
mvn exec:java
```

## Controlos

- `Q`: plantar na linha 1
- `W`: plantar na linha 2
- `E`: plantar na linha 3
- `R`: plantar na linha 4
- `T`: plantar na linha 5
- `C`: adicionar 25 de energia (tecla de teste)

Cada planta custa 50 de energia. A energia aumenta automaticamente ao longo do jogo.
Os zombies aparecem periodicamente, avançam para a esquerda e precisam de três impactos para serem derrotados.

## Ficheiros

- `pom.xml`: dependências e configuração Maven.
- `src/main/java/pt/escola/gardendefense/GardenDefenseApp.java`: código do jogo.

## Nota

Este projeto é um protótipo didático. Podes expandi-lo com sprites, vários tipos de plantas,
animações, menus, níveis e colocação de plantas com o rato.
