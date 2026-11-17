# Direção visual — quintal em perspectiva

A interface usa a composição de um jogo de jardim desenhado: casa de bairro, cerca de madeira, relva verde, título com contorno e sombra, lápide torta e placas pregadas. Os sprites existentes foram preservados. O cenário e as peças do menu são desenhados diretamente em Canvas, com formas e detalhes definidos no código.

## Menu

- **JOGAR** numa placa verde inclinada.
- **COMO JOGAR** abre e fecha um bilhete com as instruções.
- **SAIR** fecha a aplicação.
- **ENTER** começa a partida.

Os únicos movimentos decorativos são as transições curtas de entrada e a resposta das placas ao rato. Não há animação contínua dos personagens no menu.

## Mapa

O relvado tem perspectiva: as filas distantes são menores e a frente do terreno é mais larga. A calçada lateral e o acesso à casa seguem a mesma projeção. A casa, a cerca e os personagens mantêm-se verticais, em vez de serem achatados sobre a grelha.

`BoardProjection` converte as coordenadas lógicas do tabuleiro para o plano visível. A conversão inversa serve para o clique e para o contorno de seleção. As regras de combate continuam no tabuleiro lógico de 5 × 9 células. Os sprites são posicionados e dimensionados pela profundidade, sem novas rotações ou animações.

## Personagens e regras

Foram retirados o balanço dos zombies, o crescimento e recuo das plantas, a rotação dos sóis e os efeitos de círculos e textos que tinham sido acrescentados no primeiro redesenho. Mantêm-se os sprites, os valores de vida e velocidade, os disparos, a alimentação dos zombies e a progressão de dificuldade anteriores ao redesenho.

Cada ervilhadora custa **50 sol**; cada sol recolhido vale **25**. **ESC**, **M** ou **MENU** regressam ao menu e terminam a partida atual. **C** mantém a função de teste existente, adicionando 25 sol.

## Ficheiros

- `GardenUI.java`: menu ilustrado, placas interativas, bilhete de ajuda, barra superior e resultado.
- `LawnView.java`: casa, céu, cerca, vegetação, caminhos e relvado.
- `BoardProjection.java`: projeção do chão, seleção de células e posicionamento dos sprites.
- `GardenDefenseApp.java`: regras existentes e integração com o mapa projetado.

O trabalho partiu da branch local `Design`, no commit `bfdd464`. Não foi feito fetch, commit ou alteração de histórico. As alterações locais preexistentes no README, no Maven e na configuração do editor não foram reescritas. O projeto versiona `target/classes`, que o editor pode recompilar automaticamente.

## Verificação

Compilação com JDK 25 e dependências FXGL/JavaFX locais. Verificações em execução:

- Projeção e inversão das coordenadas das 45 células.
- Plantio em todas as células, custo correto e rejeição de cliques fora do relvado.
- Botões de jogar e abrir/fechar ajuda.
- Recolha de sol, fim de jogo, menu e reinício.
- Zombies param para comer e causam o dano original.
- Projéteis continuam a causar o dano original.
- Os zombies não recebem a rotação decorativa do primeiro redesenho.

As capturas da própria cena JavaFX foram revistas para conferir o encaixe dos personagens na grelha. O Maven não estava no PATH deste ambiente; a compilação foi feita diretamente com `javac`. Num ambiente com Maven, executar `mvn compile exec:java` nesta pasta.

## Capturas atuais

![Menu](docs/design/menu.png)

![Mapa em perspectiva](docs/design/game.png)

![Fim da partida](docs/design/result.png)