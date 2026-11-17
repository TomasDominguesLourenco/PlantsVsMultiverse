# Garden Defense — projeto escolar com FXGL

Protótipo Java inspirado em jogos de defesa de jardim, feito com FXGL.

## Requisitos

- JDK 25 (LTS)
- Maven
- VS Code com as extensões **Extension Pack for Java** e **Maven for Java**

## Instalar no Ubuntu/Debian

```bash
sudo apt update
sudo apt install openjdk-25-jdk maven
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

- **Rato**: clique numa célula da grelha para plantar; clique no sol para o apanhar (+25)
- `ENTER`: começar, jogar outra vez ou continuar uma partida pausada
- `ESC`, `P` ou `M`: pausar / continuar durante a partida
- `M`: voltar ao menu depois do fim do jogo
- `C`: adicionar 25 de energia (tecla de teste)

O jogo arranca em ecrã inteiro. O botão **PAUSA** abre as opções de continuar,
volume de música e efeitos, silêncio, janela/ecrã inteiro, menu principal e saída.
Voltar ao menu termina a partida atual. Mudar para outra janela pausa o jogo.
Os volumes mantêm-se entre partidas enquanto a aplicação estiver aberta.

Ao clicar em **JOGAR** ou premir **ENTER**, o menu sai com fade e zoom out;
o jardim entra com zoom in e um pequeno ressalto. A partida começa depois da transição.

Cada planta custa 50 de energia. O sol cai do céu periodicamente e tem de ser apanhado com o clique antes de desaparecer.
Os zombies avançam para a esquerda, param à frente das plantas e comem-nas. A dificuldade aumenta com o tempo (spawn mais frequente, zombies mais rápidos e com mais vida).

## Ficheiros

- `pom.xml`: dependências e configuração Maven.
- `src/main/java/pt/escola/gardendefense/GardenDefenseApp.java`: código do jogo.
- `DESIGN.md`: direção visual, controlos, verificações e capturas atuais.
- `docs/design/ARTWORK.md`: novas imagens e respetivos prompts.

## Nota

Este projeto é um protótipo didático. Podes expandi-lo com sprites, vários tipos de plantas,
animações, menus, níveis e colocação de plantas com o rato.
