# ☿ Hermetic Symbol Keyboard

> Teclado Android com símbolos herméticos, alquímicos, astrológicos, Aleph Beit hebraico e esotéricos em Unicode.

[![Android CI](https://github.com/marcoshermeticum/Hermetic-Symbol-Keyboard/actions/workflows/ci.yml/badge.svg)](https://github.com/marcoshermeticum/Hermetic-Symbol-Keyboard/actions)
[![Min SDK](https://img.shields.io/badge/minSdk-30-green)](https://developer.android.com/about/versions/11)
[![License](https://img.shields.io/badge/license-GPL--3.0-blue)](LICENSE)

---

## 📋 Índice

- [Sobre o Projeto](#sobre-o-projeto)
- [Funcionalidades](#funcionalidades)
- [Requisitos do Sistema](#requisitos-do-sistema)
- [Configuração do Ambiente de Desenvolvimento](#configuração-do-ambiente-de-desenvolvimento)
- [Compilando o Projeto](#compilando-o-projeto)
- [Instalando no Celular](#instalando-no-celular)
- [Debug via Computador (ADB)](#debug-via-computador-adb)
- [Executando Testes](#executando-testes)
- [CI/CD](#cicd)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Categorias de Símbolos](#categorias-de-símbolos)
- [Contribuindo](#contribuindo)
- [Troubleshooting](#troubleshooting)
- [Licença](#licença)

---

## Sobre o Projeto

O **Hermetic Symbol Keyboard** é um fork do [OpenBoard](https://github.com/openboard-team/openboard) que adiciona um painel dedicado para inserção de símbolos herméticos, alquímicos, astrológicos e do alfabeto hebraico (Aleph Beit) — todos baseados em codepoints Unicode oficiais.

O teclado funciona em **qualquer dispositivo Android 11+** com qualquer resolução de tela.

**Dispositivo de teste principal:** Samsung Galaxy A30s SM-A307GT (720x1560, HD+)

---

## Funcionalidades

- ⌨️ Teclado QWERTY completo multi-idioma (EN, PT-BR, ES, FR, DE, IT)
- 🔮 Painel de símbolos herméticos com busca e favoritos
- ♈ Signos do zodíaco (12 símbolos)
- ☿ Símbolos planetários (10 símbolos)
- 🜂 Elementos clássicos (4 símbolos)
- 🜍 Símbolos alquímicos
- א Aleph Beit completo (22 letras + 5 formas finais)
- ☥ Símbolos egípcios (Ankh)
- ⛤ Símbolos esotéricos diversos (pentagramas, hexagramas, etc.)
- 😀 Painel de emojis padrão
- 🎨 Temas: Dark Hermetic, Light Hermetic, AMOLED Black, Classic
- ⭐ Sistema de favoritos e recentes
- 🔍 Busca por nome, keyword ou significado

---

## Requisitos do Sistema

### Para usar o app
| Requisito | Mínimo |
|-----------|--------|
| Android | 11 (API 30) ou superior |
| Espaço | ~50 MB |
| Resolução | Qualquer (responsivo) |

### Para desenvolvimento

| Ferramenta | Versão Mínima | Como verificar |
|------------|---------------|----------------|
| **Java JDK** | 17 | `java -version` |
| **Android Studio** | Hedgehog (2023.1.1) ou superior | Help → About |
| **Android SDK** | API 34 | SDK Manager |
| **Android Build Tools** | 34.0.0 | SDK Manager |
| **Gradle** | 8.2+ (via wrapper) | `./gradlew --version` |
| **Git** | 2.30+ | `git --version` |
| **ADB** | Incluído no SDK | `adb --version` |

### Hardware recomendado para build
- RAM: 8 GB mínimo (16 GB recomendado)
- Disco: 10 GB livres para SDK + projeto
- CPU: Qualquer x64 moderno

---

## Configuração do Ambiente de Desenvolvimento

### 1. Instalar Java JDK 17

**Windows:**
```powershell
# Via winget
winget install Microsoft.OpenJDK.17

# Ou baixe manualmente de:
# https://adoptium.net/temurin/releases/?version=17
```

**macOS:**
```bash
brew install openjdk@17
```

**Linux (Ubuntu/Debian):**
```bash
sudo apt install openjdk-17-jdk
```

Verifique:
```bash
java -version
# Deve mostrar: openjdk version "17.x.x"
```

### 2. Instalar Android Studio

1. Baixe em: https://developer.android.com/studio
2. Instale normalmente
3. Na primeira execução, aceite os termos e instale os componentes padrão
4. Abra o **SDK Manager** (Tools → SDK Manager):
   - **SDK Platforms:** Marque "Android 14.0 (API 34)" e "Android 11.0 (API 30)"
   - **SDK Tools:** Marque:
     - Android SDK Build-Tools 34
     - Android SDK Command-line Tools
     - Android Emulator
     - Android SDK Platform-Tools
     - Google Play services (opcional)
5. Clique "Apply" e aguarde o download

### 3. Configurar variáveis de ambiente

**Windows (PowerShell como admin):**
```powershell
# Adicione ao perfil do PowerShell ou variáveis de sistema
[Environment]::SetEnvironmentVariable("ANDROID_HOME", "$env:LOCALAPPDATA\Android\Sdk", "User")
[Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Microsoft\jdk-17.x.x", "User")

# Adicione ao PATH
$path = [Environment]::GetEnvironmentVariable("Path", "User")
[Environment]::SetEnvironmentVariable("Path", "$path;$env:LOCALAPPDATA\Android\Sdk\platform-tools;$env:LOCALAPPDATA\Android\Sdk\tools", "User")
```

**macOS/Linux (adicione ao ~/.bashrc ou ~/.zshrc):**
```bash
export ANDROID_HOME=$HOME/Android/Sdk
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
export PATH=$PATH:$ANDROID_HOME/platform-tools:$ANDROID_HOME/tools
```

### 4. Clonar o repositório

```bash
git clone https://github.com/marcoshermeticum/Hermetic-Symbol-Keyboard.git
cd Hermetic-Symbol-Keyboard
```

### 5. Abrir no Android Studio

1. File → Open → Selecione a pasta do projeto
2. Aguarde o Gradle sync completar (pode levar alguns minutos na primeira vez)
3. Se pedir para atualizar o Gradle plugin, aceite

---

## Compilando o Projeto

### Via Android Studio
1. Selecione a build variant: **debug** (para desenvolvimento) ou **release** (para distribuição)
   - Build → Select Build Variant
2. Clique em **Build → Make Project** (ou Ctrl+F9)
3. O APK será gerado em: `app/build/outputs/apk/debug/app-debug.apk`

### Via linha de comando

```bash
# Build debug
./gradlew assembleDebug

# Build release (requer keystore configurada)
./gradlew assembleRelease

# Limpar e rebuildar
./gradlew clean assembleDebug
```

**Windows (CMD):**
```cmd
gradlew.bat assembleDebug
```

O APK ficará em:
- Debug: `app/build/outputs/apk/debug/hermetic-keyboard-v1.0.0-debug.apk`
- Release: `app/build/outputs/apk/release/hermetic-keyboard-v1.0.0-release.apk`

---

## Instalando no Celular

### Método 1: Instalação direta via USB (recomendado)

#### Pré-requisitos no celular:

1. **Ativar Opções de Desenvolvedor:**
   - Vá em: Configurações → Sobre o telefone
   - Toque 7 vezes em "Número da versão" (ou "Informações do software" → "Número da versão" em Samsung)
   - Aparecerá: "Você agora é um desenvolvedor!"

2. **Ativar Depuração USB:**
   - Vá em: Configurações → Opções do desenvolvedor
   - Ative "Depuração USB"
   - (Samsung) Ative também "Instalar via USB"

3. **Permitir fontes desconhecidas:**
   - Configurações → Biometria e segurança → Instalar apps desconhecidos
   - Permita para "Meus Arquivos" ou "Gerenciador de arquivos"

#### Instalar via ADB:

```bash
# Conecte o celular via USB
# No celular, aceite o popup "Permitir depuração USB?"

# Verifique se o dispositivo aparece
adb devices
# Deve mostrar algo como: XXXXXXXX    device

# Instale o APK
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

#### Instalar via Android Studio:
1. Conecte o celular via USB
2. Selecione o dispositivo no dropdown ao lado do botão Run
3. Clique em **Run** (Shift+F10)
4. O app será compilado, instalado e aberto automaticamente

### Método 2: Transferir APK manualmente

1. Compile o APK (veja seção anterior)
2. Copie o arquivo `.apk` para o celular (via USB, Google Drive, email, etc.)
3. No celular, abra o gerenciador de arquivos
4. Navegue até o APK e toque nele
5. Aceite a instalação

### Ativar o teclado após instalar:

1. Vá em: **Configurações → Gerenciamento geral → Lista de teclados e padrão**
   - (Ou: Configurações → Idioma e entrada → Teclado na tela)
2. Ative o **Hermetic Symbol Keyboard**
3. Toque em "Teclado padrão" e selecione o Hermetic Keyboard
4. Abra qualquer app com campo de texto para testar

---

## Debug via Computador (ADB)

### Configuração inicial

```bash
# Verificar conexão
adb devices

# Se o dispositivo não aparecer:
# 1. Troque o cabo USB (use um com dados, não só carga)
# 2. Troque a porta USB
# 3. No celular: revogue autorizações USB e reconecte
# 4. Windows: instale o driver USB Samsung: 
#    https://developer.samsung.com/android-usb-driver
```

### Debug com logcat

```bash
# Ver todos os logs do app
adb logcat -s HermeticKB

# Filtrar por tag específica
adb logcat -s HermeticKB:D SymbolPanel:D

# Salvar logs em arquivo
adb logcat -s HermeticKB > debug_log.txt

# Limpar logs anteriores
adb logcat -c
```

### Debug via Android Studio (Breakpoints)

1. Coloque breakpoints clicando na margem esquerda do editor
2. Conecte o celular via USB
3. Clique em **Debug** (Shift+F9) em vez de Run
4. Use o painel "Debug" para inspecionar variáveis, step through, etc.

### Debug wireless (Android 11+)

```bash
# No celular: Opções do desenvolvedor → Depuração sem fio → Ativar
# Toque em "Parear dispositivo com código de pareamento"

# No computador:
adb pair <IP>:<PORTA>
# Digite o código de pareamento mostrado no celular

# Conectar:
adb connect <IP>:<PORTA>

# Verificar:
adb devices
```

### Comandos ADB úteis

```bash
# Desinstalar o app
adb uninstall com.hermetic.keyboard

# Reinstalar mantendo dados
adb install -r -d app-debug.apk

# Abrir configurações de teclado
adb shell am start -a android.settings.INPUT_METHOD_SETTINGS

# Capturar screenshot
adb exec-out screencap -p > screenshot.png

# Gravar tela (máx 3 min)
adb shell screenrecord /sdcard/demo.mp4

# Ver uso de memória do app
adb shell dumpsys meminfo com.hermetic.keyboard

# Forçar fechar o app
adb shell am force-stop com.hermetic.keyboard
```

---

## Executando Testes

### Testes unitários (rodam no computador, sem device)

```bash
# Rodar todos os testes unitários
./gradlew testDebugUnitTest

# Rodar teste específico
./gradlew testDebugUnitTest --tests "com.hermetic.keyboard.symbols.SymbolRepositoryTest"

# Com relatório de cobertura (Jacoco)
./gradlew testDebugUnitTest jacocoTestReport

# Relatório em: app/build/reports/jacoco/index.html
```

### Testes instrumentados (requerem device ou emulador)

```bash
# Rodar todos os testes instrumentados
./gradlew connectedDebugAndroidTest

# Rodar classe específica
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.hermetic.keyboard.ui.HermeticPanelTest
```

### Testes via Android Studio

1. Abra o arquivo de teste
2. Clique no ícone ▶️ verde ao lado do nome da classe ou método
3. Selecione "Run" ou "Debug"

### Criar emulador para testes

1. Tools → Device Manager → Create Device
2. Selecione:
   - **Para testar Galaxy A30s:** Escolha um device com resolução 720x1560 (ou crie custom)
   - **Para testar HD:** Pixel 3a (1080x2220)
   - **Para testar telas menores:** Nexus 5 (1080x1920)
3. System Image: API 30 (Android 11) ou API 34 (Android 14)
4. Finalize e inicie o emulador

### Estrutura de testes

```
app/
├── src/
│   ├── test/                          # Testes unitários (JVM)
│   │   └── java/com/hermetic/keyboard/
│   │       ├── symbols/
│   │       │   ├── SymbolRepositoryTest.kt
│   │       │   ├── SearchEngineTest.kt
│   │       │   └── FavoritesManagerTest.kt
│   │       └── data/
│   │           └── SymbolDataProviderTest.kt
│   │
│   └── androidTest/                   # Testes instrumentados (device)
│       └── java/com/hermetic/keyboard/
│           ├── ui/
│           │   ├── HermeticPanelTest.kt
│           │   ├── CategoryNavigationTest.kt
│           │   └── SymbolInsertionTest.kt
│           └── db/
│               └── SymbolDatabaseTest.kt
```

---

## CI/CD

O projeto utiliza **GitHub Actions** para integração e entrega contínua.

### Pipelines

| Pipeline | Trigger | O que faz |
|----------|---------|-----------|
| **PR Check** | Push / Pull Request | Lint, build, unit tests, upload APK debug |
| **Release** | Tag `v*` | Build release, assina APK, cria GitHub Release |
| **Nightly** | Cron 03:00 UTC | Build completo, todos os testes, screenshot tests |

### Configurar Secrets no GitHub

Para o pipeline de release funcionar, configure estes secrets no repositório:

1. Vá em: Settings → Secrets and variables → Actions
2. Adicione:
   - `KEYSTORE_FILE` — Conteúdo da keystore em base64:
     ```bash
     base64 -w 0 hermetic-keyboard.jks > keystore_base64.txt
     ```
   - `KEYSTORE_PASSWORD` — Senha da keystore
   - `KEY_ALIAS` — Alias da chave
   - `KEY_PASSWORD` — Senha da chave

### Gerar Keystore (primeira vez)

```bash
keytool -genkey -v -keystore hermetic-keyboard.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias hermetic-key \
  -storepass SUA_SENHA_AQUI \
  -dname "CN=Hermetic Keyboard, OU=Dev, O=MarcosHermeticum, L=City, ST=State, C=BR"
```

> ⚠️ **NUNCA commite a keystore no repositório!** Adicione `*.jks` ao `.gitignore`.

### Quality Gates

- Cobertura mínima: **80%**
- Lint errors: **0** (tolerância zero)
- Todos os testes devem passar

---

## Estrutura do Projeto

```
Hermetic-Symbol-Keyboard/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/hermetic/keyboard/
│   │   │   │   ├── HermeticIME.kt              # InputMethodService principal
│   │   │   │   ├── symbols/
│   │   │   │   │   ├── SymbolRepository.kt     # Acesso aos símbolos
│   │   │   │   │   ├── SymbolCategory.kt       # Modelo de categoria
│   │   │   │   │   ├── Symbol.kt               # Modelo de símbolo
│   │   │   │   │   ├── SearchEngine.kt         # Motor de busca
│   │   │   │   │   ├── FavoritesManager.kt     # Gerenciador de favoritos
│   │   │   │   │   └── RecentsManager.kt       # Gerenciador de recentes
│   │   │   │   ├── data/
│   │   │   │   │   ├── SymbolDatabase.kt       # Room Database
│   │   │   │   │   ├── SymbolDao.kt            # Data Access Object
│   │   │   │   │   └── SymbolDataProvider.kt   # Carrega JSON
│   │   │   │   ├── ui/
│   │   │   │   │   ├── HermeticPanelView.kt    # View do painel
│   │   │   │   │   ├── CategoryAdapter.kt      # Adapter categorias
│   │   │   │   │   └── SymbolGridAdapter.kt    # Adapter grid
│   │   │   │   └── settings/
│   │   │   │       └── SettingsActivity.kt     # Configurações
│   │   │   ├── res/
│   │   │   │   ├── layout/                     # Layouts XML
│   │   │   │   ├── values/                     # Strings, themes
│   │   │   │   ├── raw/
│   │   │   │   │   └── symbols.json            # Dados dos símbolos
│   │   │   │   └── font/
│   │   │   │       └── noto_sans_symbols2.ttf  # Fonte fallback
│   │   │   └── AndroidManifest.xml
│   │   ├── test/                               # Unit tests
│   │   └── androidTest/                        # Instrumented tests
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── .github/
│   └── workflows/
│       ├── ci.yml                              # PR checks
│       ├── release.yml                         # Release pipeline
│       └── nightly.yml                         # Nightly builds
├── base.json                                   # Especificação do projeto
├── README.md                                   # Este arquivo
├── LICENSE                                     # GPL-3.0
├── .gitignore
└── build.gradle.kts                            # Root build file
```

---

## Categorias de Símbolos

| Categoria | Ícone | Quantidade | Exemplos |
|-----------|-------|------------|----------|
| Planetary Symbols | ☉ | 10 | ☉ ☽ ☿ ♀ ♂ ♃ ♄ ♅ ♆ ♇ |
| Zodiac Signs | ♈ | 12 | ♈ ♉ ♊ ♋ ♌ ♍ ♎ ♏ ♐ ♑ ♒ ♓ |
| Classical Elements | 🜂 | 4 | 🜂 🜄 🜁 🜃 |
| Alchemical Symbols | 🜍 | 4+ | 🜔 🜍 ☿ 🜪 |
| Aleph Beit | א | 27 | א ב ג ד ה ו ז ח ט י כ ל מ נ ס ע פ צ ק ר ש ת |
| Egyptian | ☥ | 1 | ☥ |
| Misc. Esoteric | ✡ | 25+ | ✡ ☤ ⚕ ☯ ⛤ ⛧ ∞ |

---

## Contribuindo

1. Fork o repositório
2. Crie uma branch: `git checkout -b feature/minha-feature`
3. Faça commits com mensagens claras
4. Garanta que os testes passam: `./gradlew testDebugUnitTest`
5. Abra um Pull Request

### Padrões de código
- Kotlin como linguagem preferida para código novo
- Seguir [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- ktlint para formatação automática
- Nomes de commit em inglês, formato: `feat:`, `fix:`, `docs:`, `test:`, `refactor:`

---

## Troubleshooting

### "Dispositivo não encontrado" no ADB
```bash
# Reiniciar servidor ADB
adb kill-server
adb start-server
adb devices
```
- Verifique se o cabo USB suporta dados (não só carga)
- Windows: Instale o [Samsung USB Driver](https://developer.samsung.com/android-usb-driver)
- Verifique se "Depuração USB" está ativada

### Build falha com "SDK not found"
- Verifique se `ANDROID_HOME` está configurado
- Crie um arquivo `local.properties` na raiz do projeto:
  ```properties
  sdk.dir=C\:\\Users\\SEU_USER\\AppData\\Local\\Android\\Sdk
  ```

### Símbolos alquímicos não aparecem (quadrados □)
- Os caracteres U+1F700+ requerem fontes que os suportem
- O app embarca a Noto Sans Symbols 2 como fallback
- Se mesmo assim não renderizar, verifique se a fonte está sendo carregada corretamente

### Teclado não aparece nas opções
- Reinicie o dispositivo após instalar
- Vá em: Configurações → Apps → Hermetic Keyboard → Permissões
- Verifique se o app não está sendo bloqueado pelo otimizador de bateria

### Emulador muito lento
- Use imagens x86_64 (não ARM) no emulador
- Ative a aceleração de hardware:
  - Windows: Intel HAXM ou Windows Hypervisor Platform
  - Linux: KVM
- Aloque pelo menos 2 GB de RAM ao emulador

### Erro "Gradle sync failed"
```bash
# Limpar cache do Gradle
./gradlew --stop
rm -rf ~/.gradle/caches/
./gradlew clean
```

---

## Licença

Este projeto é um fork do OpenBoard e está licenciado sob a **GNU General Public License v3.0**.

Veja o arquivo [LICENSE](LICENSE) para detalhes.

---

## Links Úteis

- [OpenBoard (original)](https://github.com/openboard-team/openboard)
- [Android IME Documentation](https://developer.android.com/develop/ui/views/touch-and-input/creating-input-method)
- [Unicode Alchemical Symbols Block](https://www.unicode.org/charts/PDF/U1F700.pdf)
- [Unicode Hebrew Block](https://www.unicode.org/charts/PDF/U0590.pdf)
- [Noto Sans Symbols 2 Font](https://fonts.google.com/noto/specimen/Noto+Sans+Symbols+2)
- [Android Studio Download](https://developer.android.com/studio)
- [Samsung USB Drivers](https://developer.samsung.com/android-usb-driver)
