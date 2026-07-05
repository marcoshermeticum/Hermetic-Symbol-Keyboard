Crie um aplicativo Android (teclado/IME) chamado "Hermetic Symbol Keyboard" com as seguintes especificações. O foco absoluto desta versão é a ERGONOMIA, VELOCIDADE EXTREMA DE DIGITAÇÃO e FLUIDEZ.

## Visão Geral
Teclado Android nativo (InputMethodService) com foco em símbolos herméticos, astrológicos e alfabeto hebraico. Construído 100% programaticamente em Kotlin. Deve funcionar no Android 11+ com fluidez de 60fps.

## Stack Técnica
- Linguagem: Kotlin
- minSdk: 30, targetSdk: 34
- Build: Gradle 8.5 + AGP 8.2.2 + Kotlin 1.9.22
- Dependências: Room 2.6.1, Gson 2.10.1, Coroutines 1.7.3, Material 1.11.0
- Testes: JUnit 5, MockK, Espresso
- Package: com.hermetic.keyboard

## Branding / Cores
- Primary: #4C2062 | Primary Dark: #271F0E | Background: #090908
- Accent: #8B45B0 | Surface: #1A1519 | Key Background: #1E1424
- Key Text: #E8E0EC | Key Border: #3A2548 | Suggestion Bar: #140E18

## Arquitetura de Áreas de Toque e Performance (CRÍTICO)
Para garantir velocidade extrema e zero "zonas mortas":
- Hitboxes Contíguas: A área visual da tecla (Key Background) deve ser menor que sua área de toque (Hitbox). Não deve haver NENHUM espaço vazio (null space) entre os listeners das teclas. Onde termina o limite invisível de uma tecla, começa exatamente o da outra.
- Suporte a Multi-touch: Implementar detecção de `ACTION_POINTER_DOWN` e `ACTION_POINTER_UP`. Se o usuário digitar muito rápido com dois polegares, o teclado deve processar ambos os toques concorrentes sem anular nenhum.
- Zero Blocking: Nenhuma operação de dicionário, Room ou Gson pode rodar na Main Thread durante o `onTouchEvent`.
- Touch Slop e Center Gravity: Se o toque ocorrer exatamente na borda invisível entre duas teclas, o algoritmo deve calcular a distância entre o ponto de toque (x,y) e o centro geométrico das teclas adjacentes, acionando a mais próxima.

## Modos do Teclado (Transições Livres de Glitches)
As views devem ser pré-instanciadas e mantidas em memória (View cache) dentro de um `FrameLayout` de altura fixa.
Ao trocar de layout, NÃO redesenhe do zero. Alterne a visibilidade (View.VISIBLE / View.GONE) ou use um `ViewAnimator`. 
- Glitch Fix: O fundo (background) do container pai deve ser sempre preenchido (#090908) para evitar que a tela pisque transparente/branca durante a troca de views.
- Transição: Fade in/out de 80ms (rápido e imperceptível) em vez de slide para evitar repintura massiva (remeasure).

1. QWERTY — Layout anatômico
2. ?123 — Números + símbolos
3. 🔮 Símbolos Esotéricos — 133 símbolos
4. 😀 Emojis — Categorizados
5. א Hebraico Transliterado
6. 🎙️ Voice Input (Novo)

## Teclado QWERTY e Acentos
- Acentos via pop-up inline: Exibe no `ACTION_DOWN` contínuo após 300ms.
- O caractere base é inserido no `ACTION_DOWN` inicial. Se o acento for escolhido deslizando o dedo, um delete automático do base é feito antes de inserir o acentuado.
- Barra inferior: ?123, 😀, 🎙️ (Microfone), 🔮, espaço, ponto, enter.

## Sugestões Contextuais e Correção (User Friendly)
O sistema deve ser natural, onipresente e inteligente:
- Barra de Sugestões: 3 slots. O slot central é sempre a sugestão de correção primária (destacado com a cor Accent).
- Auto-Commit: Se o usuário digitar "cabeça" (errado) e pressionar ESPAÇO ou PONTUAÇÃO, a palavra central da barra de sugestões deve substituir automaticamente a palavra digitada.
- Processamento Assíncrono: Cada tecla pressionada dispara uma `Coroutine` no `Dispatchers.Default` para buscar sugestões (Prefix Match + Levenshtein distance < 2) sem travar a UI.
- Feedback Visual: Ao apagar uma palavra corrigida automaticamente com o Backspace, o teclado deve reverter para a palavra digitada originalmente.

## Entrada de Voz (Voice-to-Text)
- Uma tecla de Microfone (🎙️) ao lado do botão de espaço.
- Implementar via `SpeechRecognizer` API nativa do Android rodando em background, NÃO lançar a Activity externa do Google para que o teclado não perca o foco.
- Mostrar um indicador visual de "Ouvindo..." na Suggestion Bar enquanto o áudio é captado, inserindo o texto no `InputConnection` onPartialResults.

## Painel de Símbolos Esotéricos
- Grid com 133 símbolos (Planetas, Zodíaco, Elementos, Alquimia, Egípcio, Hebraico, Esotérico) consumidos do res/raw/symbols.json.
- Long-press em símbolo: tooltip (usando overlay view do IME, não PopupWindow).

## BackspaceHelper (reutilizável e fluido)
- ACTION_DOWN: Haptic feedback leve, delete de 1 char, aguarda 300ms.
- Repeat: A cada 50ms apaga 1 char. Após 1 segundo segurando, começa a apagar por PALAVRA (usando `ExtractedText`) para maior velocidade.

## Persistência e Android Resources
- SharedPreferences para pesos de palavras e learned words.
- Themes.xml, cores completas da paleta, e selector com estados visuais claros para as teclas.

## Device de teste alvo
Samsung Galaxy A30s, Android 11. Conexão via ADB. Foco em performance sob recursos limitados.