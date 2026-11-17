# Artes do quintal

Geradas com a ferramenta integrada `image_gen`, em duas chamadas independentes, com `transparent_background: true`. Os PNGs finais foram copiados para os recursos do projeto; a aplicação não depende de ficheiros externos.

## Ficheiros utilizados

- [Casa virada para a direita](../../src/main/resources/assets/textures/ui/cottage-right-v2.png) — `src/main/resources/assets/textures/ui/cottage-right-v2.png`. A versão `cottage-v1.png` foi preservada como original.
- [Placa dos botões](../../src/main/resources/assets/textures/ui/wood-button-v1.png) — `src/main/resources/assets/textures/ui/wood-button-v1.png`.

Os dois ficheiros têm transparência real. A casa é reutilizada no menu e na margem esquerda do mapa. A placa é reutilizada em todos os botões, com uma tonalidade verde nas ações principais. As legendas são texto JavaFX, para manter o português legível em qualquer tamanho.

## Prompt da casa

Use case: stylized-concept. Asset type: transparent PNG house sprite for a 2D cartoon garden-defense Java game inspired by classic Plants vs. Zombies. One original charming crooked suburban cottage, isolated, entire building fully visible, three-quarter view with the front door facing viewer and a narrow right side wall visible, viewed slightly from above so the little stone doorstep has perspective. Compact warm cream horizontal siding, terracotta tile gable roof with hand-painted irregular shingles, muted blue-green glass windows with thick cream frames, a friendly crooked wooden front door and small brass knob, one flower box. Strong clear slightly irregular dark olive-brown outlines, simple chunky shapes, hand-painted limited shading with 2-3 values, warm afternoon light from upper left. Palette matches green lawn, parchment, terracotta, ochre and olive. Readable silhouette at 250px tall. 2D casual game art from the late 2000s, drawn by an experienced game illustrator, lively subtle asymmetry, restrained texture. No characters, no lawn, no surrounding scenery, no sky, no text, no logos, no watermark, no 3D rendering or photorealism, no gradients glowing effects, no excessive detail. Real transparent background, no checkerboard pattern. Portrait canvas with generous narrow transparent padding so roof and doorstep aren't clipped.

## Prompt da placa

Use case: stylized-concept. Asset type: a single transparent PNG horizontal wooden button plate for a 2D cartoon garden-defense game inspired by classic Plants vs. Zombies. One wide, chunky hand-painted wooden garden sign, broad rectangle approximately 5:1 aspect ratio, lightly crooked corners and a few small cut chips, warm honey brown wood with olive-brown dark outlines, subtle 2-tone bevel, restrained curved wood grain concentrated near top and bottom. A small aged brass nail near each end. Large clean empty center for readable runtime text, no text anywhere. Tiny restrained dark olive leaf sprig at the far left corner only, never covering the center. Warm light from upper left. Experienced human game illustrator style, late 2000s 2D casual game art, crisp clear readable silhouette, no procedural noisy texture, no realistic photo or 3D render, no drop shadow outside the object, no glow, no characters, no background objects, no watermark. Single sign centered and occupying most canvas width, transparent background, no checkerboard painted into image. Landscape canvas.

## Correção da orientação da casa

Editada com a ferramenta integrada `image_gen`, usando a casa anterior como referência e `transparent_background: true`. A fachada da porta e o degrau apontam para a direita, em direção ao relvado. O enquadramento no menu e no mapa foi ajustado à nova proporção da imagem.

### Prompt de orientação

Edit the referenced cottage game sprite. Correct ONLY the building orientation: the front facade containing the wooden door MUST face SCREEN RIGHT towards the garden. Show a broad LEFT SIDE WALL on the left and a narrower FRONT FACADE on the RIGHT, with the front door clearly placed on that RIGHT-facing facade near the rightmost edge of the building. The door's stone doorstep must extend towards SCREEN RIGHT, not toward the bottom of the image. Camera is three-quarter slightly elevated, viewing the house from its left-front corner, for a Plants vs Zombies style game with the house on the LEFT edge and lawn extending to the RIGHT. This is a rotation of the architecture, not merely moving the doorknob or mirroring the original front-facing view. Keep the same cream cottage, warm terracotta tiled gable roof, chimney, wooden door and brass knob, blue-green windows, flower box, thick dark olive-brown outlines, hand-painted 2D cartoon style and restrained shading. Entire cottage isolated with real transparent background, including roof and rightward doorstep, no lawn, no surrounding scenery, no text, no characters. Preserve stylistic identity. The left side wall should occupy approximately two thirds of the visible width; the door facade on the right occupies one third and faces right, making the direction unmistakable.

### Prompt final de limpeza do contorno

Clean up ONLY the transparency/background of the last generated cottage sprite. Keep the cottage architecture, RIGHT-facing door facade, left side wall, rightward stone doorstep, colors, linework and all details exactly the same. Remove ALL of the brown/gold glow, haze, atmospheric shadows, background color and any other pixels outside the physical cottage, roof, chimney, flower box, foundation and steps. Everything beyond the exact building silhouette must be completely transparent with alpha zero. No outer shadow, no glow, no background at all, no checkerboard. Preserve a clean, tight antialiased silhouette for compositing into a 2D garden game. Keep the full building in frame. Do not add anything.

## Sons

`src/main/resources/assets/sounds/ui-tap.wav`, `seed-plant.wav` e `sun-pick.wav` são pequenos sons PCM mono a 22050 Hz sintetizados no projeto, sem amostras externas. Usam ruído filtrado e osciladores com envelopes curtos: toque de madeira, plantio e campainha de recolha. Todos passam pelo volume de efeitos do FXGL.

