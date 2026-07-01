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
- [Teclado Hebraico Transliterado](#teclado-hebraico-transliterado)
- [Contribuindo](#contribuindo)
- [Troubleshooting](#troubleshooting)
- [Licença](#licença)

---

## Sobre o Projeto

O **Hermetic Symbol Keyboard** é um teclado Android construído sobre a API `InputMethodService`, com um painel dedicado para inserção de símbolos herméticos, alquímicos, astrológicos e do alfabeto hebraico (Aleph Beit) — todos baseados em codepoints Unicode oficiais.

Além do painel de símbolos, inclui um **teclado hebraico transliterado** onde cada tecla mostra o nome da letra em caracteres latinos (ex: "Aleph", "Shin") e insere o caractere hebraico correspondente (א, ש).

O teclado funciona em **qualquer dispositivo Android 11+** com qualquer resolução de tela.

**Dispositivo de teste principal:** Samsung Galaxy A30s SM-A307GT (720x1560, HD+)

---

## Funcionalidades

- ⌨️ Teclado QWERTY completo multi-idioma (EN, PT-BR, ES)
- 🔮 Painel de símbolos herméticos com busca e favoritos
- ♈ Signos do zodíaco (12 símbolos)
- ☿ Símbolos planetários (10 símbolos)
- 🜂 Elementos clássicos (4 símbolos)
- 🜍 Símbolos alquímicos
- א **Aleph Beit completo** (22 letras + 5 formas finais)
- 🇮🇱 **Teclado hebraico transliterado** — teclas com nomes em latim, output em hebraico
- ☥ Símbolos egípcios (Ankh)
- ⛤ Símbolos esotéricos diversos (pentagramas, hexagramas, etc.)
- 🎨 Temas: Dark Hermetic, Light Hermetic, AMOLED Black, Classic
- ⭐ Sistema de favoritos e recentes
- 🔍 Busca por nome, keyword ou significado
- 📊 Gematria: valor numérico exibido nas letras hebraicas

---

## Requisitos do Sistema

### Para usar o app
| Requisito | Mínimo |
|-----------|--------|
| Android | 11 (API 30) ou superior |
| Espaço | ~30 MB |
| Resolução | Qualquer (responsivo) |

### Para desenvolvimento

| Ferramenta | Versão Mínima | Como verificar |
|------------|---------------|----------------|
| **Java JDK** | 17 | `java -version` |
| **Android Studio** | Hedgehog (2023.1.1)+ | Help → About |
| **Android SDK** | API 34 | SDK Manager |
| **Android Build Tools** | 34.0.0 | SDK Manager |
| **Gradle** | 8.5 (via wrapper) | `gradlew --version` |
| **Git** | 2.30+ | `git --version` |
| **ADB** | Incluído no SDK | `adb --version` |
| **Kotlin** | 1.9.22 (via Gradle) | Automático |

### Hardware recomendado para build
- RAM: 8 GB mínimo (16 GB recomendado)
- Disco: 10 GB livres para SDK + projeto
- CPU: Qualquer x64 moderno

---

## Configuração do Ambiente de Desenvolvimento

### 1. Instalar Java JDK 17

**Windows (PowerShell):**
```powershell
winget install Microsoft.OpenJDK.17
```

**Windows (manual):** Baixe em https://adoptium.net/temurin/releases/?version=17

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
# openjdk version "17.x.x"
```

### 2. Instalar Android Studio

1. Baixe em: https://developer.android.com/studio
2. Instale e execute a configuração inicial
3. **SDK Manager** (Tools → SDK Manager):
   - **SDK Platforms:** Marque "Android 14.0 (API 34)" e "Android 11.0 (API 30)"
   - **SDK Tools:** Marque:
     - Android SDK Build-Tools 34
     - Android SDK Command-line Tools
     - Android Emulator
     - Android SDK Platform-Tools
4. Clique "Apply" e aguarde

### 3. Configurar variáveis de ambiente

**Windows (PowerShell como admin):**
```powershell
[Environment]::SetEnvironmentVariable("ANDROID_HOME", "$env:LOCALAPPDATA\Android\Sdk", "User")
[Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Microsoft\jdk-17", "User")

# Adicionar ao PATH
$path = [Environment]::GetEnvironmentVariable("Path", "User")
[Environment]::SetEnvironmentVariable("Path", "$path;$env:LOCALAPPDATA\Android\Sdk\platform-tools", "User")
```

**macOS/Linux (~/.bashrc ou ~/.zshrc):**
```bash
export ANDROID_HOME=$HOME/Android/Sdk
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
export PATH=$PATH:$ANDROID_HOME/platform-tools
```

### 4. Clonar o repositório

```bash
git clone https://github.com/marcoshermeticum/Hermetic-Symbol-Keyboard.git
cd Hermetic-Symbol-Keyboard
```

### 5. Setup inicial (opcional)

No Windows, execute o script de setup que guia a configuração:
```cmd
setup.bat
```

Ou simplesmente abra o projeto no Android Studio — ele configura tudo automaticamente.

### 6. Abrir no Android Studio

1. File → Open → Selecione a pasta do projeto
2. Aguarde o Gradle sync (pode levar alguns minutos na primeira vez)
3. Se pedir para atualizar o Gradle plugin, aceite

### 7. Verificar ambiente

```bash
# Windows
gradlew.bat assembleDebug

# macOS/Linux
chmod +x gradlew
./gradlew assembleDebug
```

Se compilar sem erros, o ambiente está pronto.

---

## Compilando o Projeto

### Via Android Studio
1. Build → Select Build Variant → escolha **debug** ou **release**
2. Build → Make Project (Ctrl+F9)
3. APK gerado em: `app/build/outputs/apk/debug/`

### Via linha de comando

```bash
# Build debug
gradlew.bat assembleDebug

# Build release (requer keystore)
gradlew.bat assembleRelease

# Limpar e rebuildar
gradlew.bat clean assembleDebug
```

---

## Instalando no Celular

### Pré-requisitos no celular

#### 1. Ativar Opções de Desenvolvedor
- **Samsung Galaxy A30s:** Configurações → Sobre o telefone → Informações do software → toque 7x em "Número da versão"
- **Outros Android:** Configurações → Sobre o telefone → toque 7x em "Número da versão"

#### 2. Ativar Depuração USB
- Configurações → Opções do desenvolvedor → Depuração USB: **ATIVADO**
- (Samsung) Também ative: "Instalar via USB"

#### 3. Permitir fontes desconhecidas (para instalação manual)
- Configurações → Biometria e segurança → Instalar apps desconhecidos → Permitir para seu gerenciador de arquivos

---

### Método 1: Run direto pelo Android Studio (recomendado para dev)

1. Conecte o celular via USB (cabo com dados!)
2. No popup do celular, aceite "Permitir depuração USB?" (marque "Sempre permitir")
3. Selecione o dispositivo no dropdown ao lado do botão ▶️ Run
4. Clique **Run** (Shift+F10)
5. O app será compilado, instalado e aberto automaticamente

### Método 2: Instalar via ADB (linha de comando)

```bash
# Verificar se o dispositivo está conectado
adb devices
# Deve mostrar: XXXXXXXX    device

# Instalar o APK
adb install -r app\build\outputs\apk\debug\app-debug.apk

# Se já existir uma versão instalada, force reinstall
adb install -r -d app\build\outputs\apk\debug\app-debug.apk
```

### Método 3: Copiar APK manualmente

1. Copie o `.apk` para o celular (USB, Drive, email)
2. No celular, abra o gerenciador de arquivos
3. Toque no APK e aceite a instalação

---

### ⌨️ Ativar o teclado após instalar

**Samsung Galaxy A30s:**
1. Configurações → **Gerenciamento geral** → **Lista de teclados e padrão**
2. Ative **Hermetic Keyboard** (ou "Teclado Hermético")
3. Toque em **Teclado padrão** e selecione o Hermetic Keyboard
4. Abra qualquer app com campo de texto para testar

**Outros Android:**
1. Configurações → Sistema → Idioma e entrada → Teclado na tela
2. Ative o Hermetic Keyboard
3. Defina como padrão

**Via ADB (atalho):**
```bash
# Abrir configurações de teclado diretamente
adb shell am start -a android.settings.INPUT_METHOD_SETTINGS
```

---

## Debug via Computador (ADB)

### Verificar conexão
```bash
adb devices

# Se não aparecer:
adb kill-server
adb start-server
adb devices
```

### Debug com Logcat
```bash
# Ver logs do app (tag principal)
adb logcat -s HermeticKB

# Filtrar múltiplas tags
adb logcat -s HermeticKB:D SymbolPanel:D HebrewKB:D

# Salvar em arquivo
adb logcat -s HermeticKB > debug_log.txt

# Limpar buffer
adb logcat -c
```

### Debug com Breakpoints (Android Studio)
1. Coloque breakpoints clicando na margem esquerda do editor
2. Clique em **Debug** (Shift+F9) em vez de Run
3. Use o painel "Debug" para step through, inspecionar variáveis

### Debug Wireless (Android 11+)
```bash
# No celular: Opções do desenvolvedor → Depuração sem fio → Ativar
# Toque "Parear dispositivo com código de pareamento"

adb pair <IP>:<PORTA_PAREAMENTO>
# Digite o código mostrado no celular

adb connect <IP>:<PORTA_CONEXAO>
adb devices
```

### Comandos ADB úteis
```bash
# Desinstalar
adb uninstall com.hermetic.keyboard

# Reinstalar mantendo dados
adb install -r -d app-debug.apk

# Abrir config de teclado
adb shell am start -a android.settings.INPUT_METHOD_SETTINGS

# Screenshot
adb exec-out screencap -p > screenshot.png

# Gravar tela
adb shell screenrecord /sdcard/demo.mp4

# Uso de memória
adb shell dumpsys meminfo com.hermetic.keyboard

# Force stop
adb shell am force-stop com.hermetic.keyboard
```

---

## Executando Testes

### Testes unitários (rodam na JVM, sem device)

```bash
# Todos
gradlew.bat testDebugUnitTest

# Classe específica
gradlew.bat testDebugUnitTest --tests "com.hermetic.keyboard.symbols.search.SearchEngineTest"

# Com relatório de cobertura
gradlew.bat testDebugUnitTest jacocoTestReport
# Relatório: app/build/reports/jacoco/index.html
```

### Testes instrumentados (requerem device/emulador)

```bash
# Todos
gradlew.bat connectedDebugAndroidTest

# Classe específica
gradlew.bat connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.hermetic.keyboard.ui.HermeticPanelTest
```

### Criar emulador para testes

1. Tools → Device Manager → Create Device
2. Selecione hardware:
   - **Simular Galaxy A30s:** Custom device 720×1560, 6.4", 268dpi
   - **HD genérico:** Pixel 3a
   - **Tela menor:** Nexus 5
3. System Image: **API 30** (Android 11) x86_64
4. Finalize e inicie o emulador

### Estrutura de testes
```
app/src/
├── test/java/com/hermetic/keyboard/     # Unit tests (JVM)
│   └── symbols/
│       ├── SymbolRepositoryTest.kt      # Repositório + favoritos + recentes
│       └── search/
│           └── SearchEngineTest.kt      # Motor de busca
│
└── androidTest/java/com/hermetic/keyboard/  # Instrumented (device)
    ├── ui/
    │   ├── HermeticPanelTest.kt         # Painel de símbolos
    │   └── HebrewKeyboardTest.kt        # Teclado hebraico
    └── db/
        └── SymbolDatabaseTest.kt        # Room DB
```

---

## CI/CD

O projeto usa **GitHub Actions** com 3 pipelines:

| Pipeline | Trigger | O que faz |
|----------|---------|-----------|
| **CI** | Push / PR para main | Lint → Build → Unit Tests → Instrumented Tests |
| **Release** | Tag `v*` | Build release → Assina APK → Cria GitHub Release |
| **Nightly** | Cron 03:00 UTC | Build + todos os testes + reports |

### Configurar Secrets (para release)

No GitHub: Settings → Secrets and variables → Actions → New repository secret:

| Secret | Valor |
|--------|-------|
| `KEYSTORE_FILE` | Keystore em base64 (ver abaixo) |
| `KEYSTORE_PASSWORD` | Senha da keystore |
| `KEY_ALIAS` | Alias da chave |
| `KEY_PASSWORD` | Senha da chave |

### Gerar Keystore (primeira vez)

```bash
keytool -genkey -v -keystore hermetic-keyboard.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias hermetic-key \
  -storepass SUA_SENHA_AQUI \
  -dname "CN=Hermetic Keyboard, OU=Dev, O=MarcosHermeticum, L=City, ST=State, C=BR"
```

Converter para base64 (para o GitHub Secret):
```bash
# Linux/macOS
base64 -w 0 hermetic-keyboard.jks > keystore_base64.txt

# Windows PowerShell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("hermetic-keyboard.jks")) | Set-Content keystore_base64.txt
```

> ⚠️ **NUNCA commite a keystore!** O `.gitignore` já exclui `*.jks`.

### Criar uma release

```bash
git tag v1.0.0
git push origin v1.0.0
# O GitHub Actions fará o build, assinará o APK e criará a release automaticamente
```

---

## Estrutura do Projeto

```
Hermetic-Symbol-Keyboard/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/hermetic/keyboard/
│   │   │   │   ├── ime/
│   │   │   │   │   └── HermeticIME.kt              # InputMethodService principal
│   │   │   │   ├── symbols/
│   │   │   │   │   ├── model/
│   │   │   │   │   │   ├── Symbol.kt               # Modelo de símbolo
│   │   │   │   │   │   ├── SymbolCategory.kt       # Modelo de categoria
│   │   │   │   │   │   └── HebrewKey.kt            # Modelo de tecla hebraica
│   │   │   │   │   ├── data/
│   │   │   │   │   │   ├── SymbolDatabase.kt       # Room Database
│   │   │   │   │   │   ├── Daos.kt                 # DAOs (Favorites, Recents)
│   │   │   │   │   │   ├── Entities.kt             # Entidades Room
│   │   │   │   │   │   └── SymbolDataProvider.kt   # Carrega JSON
│   │   │   │   │   ├── repository/
│   │   │   │   │   │   └── SymbolRepository.kt     # Repositório central
│   │   │   │   │   └── search/
│   │   │   │   │       └── SearchEngine.kt         # Motor de busca
│   │   │   │   ├── ui/
│   │   │   │   │   ├── KeyboardLayoutManager.kt    # Gerencia layouts/views
│   │   │   │   │   ├── panel/
│   │   │   │   │   │   ├── HermeticPanelView.kt    # Painel de símbolos
│   │   │   │   │   │   └── SymbolGridAdapter.kt    # Adapter do grid
│   │   │   │   │   └── hebrew/
│   │   │   │   │       └── HebrewKeyboardView.kt   # Teclado hebraico transliterado
│   │   │   │   └── settings/
│   │   │   │       └── SettingsActivity.kt         # Configurações
│   │   │   ├── res/
│   │   │   │   ├── layout/                         # Layouts XML
│   │   │   │   ├── values/                         # Strings, colors, themes (EN)
│   │   │   │   ├── values-pt-rBR/                  # Strings (PT-BR)
│   │   │   │   ├── values-es/                      # Strings (ES)
│   │   │   │   ├── drawable/                       # Key backgrounds, icons
│   │   │   │   ├── raw/
│   │   │   │   │   └── symbols.json                # Dados de todos os símbolos
│   │   │   │   ├── xml/
│   │   │   │   │   ├── method.xml                  # Definição do IME + subtypes
│   │   │   │   │   └── preferences.xml             # Tela de preferências
│   │   │   │   └── mipmap-anydpi-v26/              # Adaptive icon
│   │   │   └── AndroidManifest.xml
│   │   ├── test/                                   # Unit tests (JVM)
│   │   └── androidTest/                            # Instrumented tests (device)
│   ├── build.gradle.kts                            # App-level Gradle
│   └── proguard-rules.pro
├── .github/workflows/
│   ├── ci.yml                                      # CI pipeline
│   ├── release.yml                                 # Release pipeline
│   └── nightly.yml                                 # Nightly builds
├── gradle/wrapper/
│   └── gradle-wrapper.properties
├── base.json                                       # Especificação completa do projeto
├── README.md                                       # Este arquivo
├── LICENSE                                         # GPL-3.0
├── .gitignore
├── build.gradle.kts                                # Root Gradle
├── settings.gradle.kts                             # Gradle settings
├── gradle.properties                               # Gradle properties
└── gradlew.bat                                     # Gradle wrapper (Windows)
```

---

## Categorias de Símbolos

| Categoria | Ícone | Qtd | Exemplos |
|-----------|-------|-----|----------|
| Planetary Symbols | ☉ | 10 | ☉ ☽ ☿ ♀ ♂ ♃ ♄ ♅ ♆ ♇ |
| Zodiac Signs | ♈ | 12 | ♈ ♉ ♊ ♋ ♌ ♍ ♎ ♏ ♐ ♑ ♒ ♓ |
| Classical Elements | 🜂 | 4 | 🜂 🜁 🜃 🜄 |
| Alchemical Symbols | 🜍 | 4 | 🜔 🜍 ☿ 🜪 |
| Aleph Beit | א | 27 | א ב ג ד ה ו ז ח ט י כ ל מ נ ס ע פ צ ק ר ש ת ך ם ן ף ץ |
| Egyptian | ☥ | 1 | ☥ |
| Misc. Esoteric | ✡ | 17 | ✡ ☤ ⚕ ☯ ⛤ ⛧ ∞ △ ▽ |

---

## Teclado Hebraico Transliterado

Um dos diferenciais deste teclado é o layout **Hebrew Transliterated** — ideal para estudantes de Kabbalah, gematria e tradições herméticas que não estão familiarizados com o layout físico hebraico.

### Como funciona

| Tecla mostra | Output inserido | Gematria |
|--------------|-----------------|----------|
| Aleph | א | 1 |
| Bet | ב | 2 |
| Gimel | ג | 3 |
| Shin | ש | 300 |
| Tav | ת | 400 |
| Mem· | ם (sofit) | 600 |
| ... | ... | ... |

### Acessar o layout
- Via **tecla globe** (alternância de idioma) no teclado principal
- Via **Configurações → Idioma e entrada → Subtypes**
- Via tecla **ABC** no teclado hebraico para voltar ao QWERTY

### Long-press
Segure qualquer tecla para ver:
- Nome completo da letra
- Caractere hebraico ampliado
- Valor de gematria

---

## Contribuindo

1. Fork o repositório
2. Crie branch: `git checkout -b feature/minha-feature`
3. Commits claros: `feat: add new symbol category`
4. Garanta que testes passam: `gradlew.bat testDebugUnitTest`
5. Abra Pull Request

### Convenções
- Kotlin para código novo
- [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Commits: `feat:`, `fix:`, `docs:`, `test:`, `refactor:`

---

## Troubleshooting

### Dispositivo não aparece no ADB
```bash
adb kill-server
adb start-server
adb devices
```
- Use cabo USB **com dados** (não apenas carga)
- Windows: instale [Samsung USB Driver](https://developer.samsung.com/android-usb-driver)
- Verifique "Depuração USB" ativada

### Build falha: "SDK not found"
Crie `local.properties` na raiz:
```properties
sdk.dir=C\:\\Users\\SEU_USER\\AppData\\Local\\Android\\Sdk
```

### Símbolos alquímicos aparecem como □
- Caracteres U+1F700+ requerem fontes compatíveis
- O app embarca Noto Sans Symbols 2 como fallback
- Baixe a fonte de: https://fonts.google.com/noto/specimen/Noto+Sans+Symbols+2
- Coloque em: `app/src/main/res/font/`

### Teclado não aparece nas opções
- Reinicie o dispositivo após instalar
- Verifique se o app não está sendo bloqueado pelo otimizador de bateria
- Use: `adb shell am start -a android.settings.INPUT_METHOD_SETTINGS`

### Emulador lento
- Use imagens **x86_64** (não ARM)
- Windows: Ative Windows Hypervisor Platform (Settings → Apps → Optional Features)
- Aloque 2+ GB de RAM ao emulador

### Gradle sync falha
```bash
gradlew.bat --stop
# Delete a pasta .gradle do projeto e do user:
rmdir /s /q .gradle
rmdir /s /q %USERPROFILE%\.gradle\caches
gradlew.bat clean
```

### Erro de JDK version
Verifique que está usando JDK 17:
```bash
java -version
# Deve ser 17.x

# Se necessário, defina explicitamente:
set JAVA_HOME=C:\Program Files\Microsoft\jdk-17
```

---

## Licença

Este projeto está licenciado sob a **GNU General Public License v3.0**.

Veja [LICENSE](LICENSE) para detalhes completos.

---

## Links Úteis

- [Android IME Documentation](https://developer.android.com/develop/ui/views/touch-and-input/creating-input-method)
- [Unicode Alchemical Symbols Block](https://www.unicode.org/charts/PDF/U1F700.pdf)
- [Unicode Hebrew Block](https://www.unicode.org/charts/PDF/U0590.pdf)
- [Noto Sans Symbols 2](https://fonts.google.com/noto/specimen/Noto+Sans+Symbols+2)
- [Noto Sans Hebrew](https://fonts.google.com/noto/specimen/Noto+Sans+Hebrew)
- [Android Studio](https://developer.android.com/studio)
- [Samsung USB Drivers](https://developer.samsung.com/android-usb-driver)
- [GitHub Actions for Android](https://github.com/marketplace/actions/android-emulator-runner)
