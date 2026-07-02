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
- [Setup Rápido via Terminal (Windows)](#setup-rápido-via-terminal-windows)
- [Setup Rápido via Terminal (macOS/Linux)](#setup-rápido-via-terminal-macoslinux)
- [Compilando o Projeto](#compilando-o-projeto)
- [Instalando no Celular via Wi-Fi](#instalando-no-celular-via-wi-fi)
- [Instalando no Celular via USB](#instalando-no-celular-via-usb)
- [Ativando o Teclado no Android](#ativando-o-teclado-no-android)
- [Debug e Logs](#debug-e-logs)
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

O **Hermetic Symbol Keyboard** é um teclado Android construído sobre a API `InputMethodService`, com um painel dedicado para inserção de símbolos herméticos, alquímicos, astrológicos e do alfabeto hebraico (Aleph Beit).

Inclui um **teclado hebraico transliterado** onde cada tecla mostra o nome da letra em caracteres latinos (ex: "Aleph", "Shin") e insere o caractere hebraico correspondente (א, ש).

Funciona em **qualquer dispositivo Android 11+** com qualquer resolução de tela.

---

## Funcionalidades

- ⌨️ Teclado QWERTY completo com shift, backspace, enter
- 🔮 Painel de símbolos herméticos com busca e categorias
- ♈ Signos do zodíaco (12) · ☿ Planetas (10) · 🜂 Elementos (4) · 🜍 Alquímicos (4)
- א **Aleph Beit completo** (22 letras + 5 formas finais) com gematria
- 🇮🇱 **Teclado hebraico transliterado** — teclas com nomes em latim, output em hebraico
- ☥ Símbolos egípcios · ⛤ Esotéricos diversos
- 🎨 Tema Dark Hermetic (cores: #4C2062, #271F0E, #090908)
- ⭐ Favoritos e recentes persistidos (Room DB)
- 🔍 Busca por nome, keyword ou significado

---

## Requisitos do Sistema

### Para usar o app
| Requisito | Mínimo |
|-----------|--------|
| Android | 11 (API 30) ou superior |
| Espaço | ~13 MB |

### Para compilar

| Ferramenta | Versão | Instalação automática no setup |
|------------|--------|-------------------------------|
| Java JDK | 17 | ✅ |
| Android SDK | API 34 + Build Tools 34 | ✅ |
| Gradle | 8.5 | ✅ |
| ADB | Último | ✅ |

---

## Setup Rápido via Terminal (Windows)

Abra o **PowerShell** (não precisa ser admin) e execute os comandos abaixo. O processo completo leva ~5 minutos com internet boa.

### 1. Instalar JDK 17

```powershell
winget install Microsoft.OpenJDK.17 --accept-package-agreements --accept-source-agreements
```

Após instalar, feche e reabra o PowerShell, depois configure:

```powershell
# Encontrar o JDK instalado
$jdk = (Get-ChildItem "C:\Program Files\Microsoft" -Filter "jdk-17*" -Directory).FullName
Write-Host "JDK encontrado em: $jdk"

# Configurar variáveis de ambiente (permanente)
[Environment]::SetEnvironmentVariable("JAVA_HOME", $jdk, "User")
$env:JAVA_HOME = $jdk
$env:Path = "$jdk\bin;$env:Path"

# Verificar
java -version
```

### 2. Instalar Android SDK + ADB

```powershell
# Criar pasta do SDK
New-Item -ItemType Directory -Force -Path "C:\Android\Sdk" | Out-Null

# Baixar Platform Tools (ADB)
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
(New-Object Net.WebClient).DownloadFile("https://dl.google.com/android/repository/platform-tools-latest-windows.zip", "C:\Android\platform-tools.zip")
Expand-Archive "C:\Android\platform-tools.zip" -DestinationPath "C:\" -Force

# Baixar Command Line Tools
(New-Object Net.WebClient).DownloadFile("https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip", "C:\Android\cmdline-tools.zip")
Expand-Archive "C:\Android\cmdline-tools.zip" -DestinationPath "C:\Android\Sdk\cmdline-tools\temp" -Force
Move-Item "C:\Android\Sdk\cmdline-tools\temp\cmdline-tools" "C:\Android\Sdk\cmdline-tools\latest" -Force
Remove-Item "C:\Android\Sdk\cmdline-tools\temp" -Recurse -Force

# Configurar variáveis
[Environment]::SetEnvironmentVariable("ANDROID_HOME", "C:\Android\Sdk", "User")
$currentPath = [Environment]::GetEnvironmentVariable("Path", "User")
[Environment]::SetEnvironmentVariable("Path", "$currentPath;C:\platform-tools;C:\Android\Sdk\cmdline-tools\latest\bin", "User")
$env:ANDROID_HOME = "C:\Android\Sdk"
$env:Path = "C:\platform-tools;C:\Android\Sdk\cmdline-tools\latest\bin;$env:Path"

# Aceitar licenças
$yesAnswers = ("y`n" * 20)
$yesAnswers | cmd /c "C:\Android\Sdk\cmdline-tools\latest\bin\sdkmanager.bat --licenses --sdk_root=C:\Android\Sdk"

# Instalar SDK Platform 34 e Build Tools
cmd /c "C:\Android\Sdk\cmdline-tools\latest\bin\sdkmanager.bat --sdk_root=C:\Android\Sdk --install `"platforms;android-34`" `"build-tools;34.0.0`" `"platform-tools`""
```

### 3. Instalar Gradle

```powershell
(New-Object Net.WebClient).DownloadFile("https://services.gradle.org/distributions/gradle-8.5-bin.zip", "C:\Android\gradle.zip")
Expand-Archive "C:\Android\gradle.zip" -DestinationPath "C:\" -Force
$env:Path = "C:\gradle-8.5\bin;$env:Path"

# Verificar
gradle --version
```

### 4. Clonar e Compilar

```powershell
git clone https://github.com/marcoshermeticum/Hermetic-Symbol-Keyboard.git
cd Hermetic-Symbol-Keyboard

# Criar local.properties
"sdk.dir=C\:\\Android\\Sdk" | Out-File -Encoding utf8 local.properties

# Compilar
gradle assembleDebug --no-daemon
```

O APK será gerado em: `app\build\outputs\apk\debug\app-debug.apk`

---

## Setup Rápido via Terminal (macOS/Linux)

### 1. Instalar dependências

```bash
# macOS
brew install openjdk@17 gradle

# Ubuntu/Debian
sudo apt install openjdk-17-jdk gradle

# Configurar JAVA_HOME
export JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null || echo /usr/lib/jvm/java-17-openjdk-amd64)
```

### 2. Instalar Android SDK

```bash
mkdir -p ~/Android/Sdk
cd ~/Android

# Baixar command-line tools
# macOS:
curl -o cmdline-tools.zip https://dl.google.com/android/repository/commandlinetools-mac-11076708_latest.zip
# Linux:
curl -o cmdline-tools.zip https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip

unzip cmdline-tools.zip -d Sdk/cmdline-tools/temp
mv Sdk/cmdline-tools/temp/cmdline-tools Sdk/cmdline-tools/latest
rm -rf Sdk/cmdline-tools/temp

# Configurar PATH
export ANDROID_HOME=~/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools

# Adicionar ao ~/.bashrc ou ~/.zshrc para persistir:
echo 'export ANDROID_HOME=~/Android/Sdk' >> ~/.bashrc
echo 'export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools' >> ~/.bashrc

# Aceitar licenças e instalar SDK
yes | sdkmanager --licenses --sdk_root=$ANDROID_HOME
sdkmanager --sdk_root=$ANDROID_HOME "platforms;android-34" "build-tools;34.0.0" "platform-tools"
```

### 3. Clonar e Compilar

```bash
git clone https://github.com/marcoshermeticum/Hermetic-Symbol-Keyboard.git
cd Hermetic-Symbol-Keyboard

# Criar local.properties
echo "sdk.dir=$ANDROID_HOME" > local.properties

# Compilar
gradle assembleDebug --no-daemon
```

---

## Compilando o Projeto

Após o setup, qualquer build subsequente é rápido:

```bash
# Windows (PowerShell) - certifique-se que JAVA_HOME e ANDROID_HOME estão setados
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.19.10-hotspot"
$env:ANDROID_HOME = "C:\Android\Sdk"
$env:Path = "$env:JAVA_HOME\bin;C:\gradle-8.5\bin;C:\platform-tools;$env:Path"

cd C:\caminho\para\Hermetic-Symbol-Keyboard
gradle assembleDebug --no-daemon

# macOS/Linux
cd /caminho/para/Hermetic-Symbol-Keyboard
gradle assembleDebug --no-daemon
```

Build incremental leva ~30-40 segundos. Build limpo:
```bash
gradle clean assembleDebug --no-daemon
```

---

## Instalando no Celular via Wi-Fi

Método recomendado — não precisa de cabo USB. Requer Android 11+.

### No celular:

1. **Ativar Opções de Desenvolvedor:**
   - Configurações → Sobre o telefone → (Samsung: Informações do software) → toque **7x** em "Número da versão"

2. **Ativar Depuração sem fio:**
   - Configurações → Opções do desenvolvedor → **Depuração sem fio** → ATIVAR

3. **Parear:**
   - Dentro de "Depuração sem fio", toque **"Parear dispositivo com código de pareamento"**
   - Anote o **IP:Porta** e o **código de 6 dígitos**

### No terminal do PC:

```powershell
# Parear (só na primeira vez)
adb pair 192.168.X.X:PORTA_PAREAMENTO CODIGO
# Exemplo: adb pair 192.168.0.160:38968 971993

# Conectar (anote o IP:Porta da tela PRINCIPAL de "Depuração sem fio", não a de pareamento)
adb connect 192.168.X.X:PORTA_CONEXAO
# Exemplo: adb connect 192.168.0.160:39867

# Verificar conexão
adb devices
# Deve mostrar: 192.168.X.X:PORTA    device

# Instalar o APK
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

> ⚠️ A porta de **pareamento** e a porta de **conexão** são diferentes! Verifique no celular.
>
> ⚠️ A porta muda toda vez que você reativa a depuração sem fio. Se `adb connect` falhar, verifique a porta atual no celular.

---

## Instalando no Celular via USB

### Pré-requisitos:
- Cabo USB **com dados** (não só carga)
- Windows: [Samsung USB Driver](https://developer.samsung.com/android-usb-driver) instalado
- No celular: Opções do desenvolvedor → **Depuração USB** ativada
- Samsung: Ativar também **"Instalar via USB"**

### Comandos:

```powershell
# Conectar cabo USB e aceitar o popup no celular

# Verificar
adb devices
# Deve mostrar: SERIAL_NUMBER    device

# Instalar
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

---

## Ativando o Teclado no Android

Após instalar o APK, o teclado precisa ser ativado manualmente (requisito de segurança do Android):

### Via ADB (mais rápido):
```bash
# Abrir configurações de teclado diretamente
adb shell am start -a android.settings.INPUT_METHOD_SETTINGS
```

### Manualmente (Samsung):
1. Configurações → **Gerenciamento geral** → **Lista de teclados e padrão**
2. Ative **"Hermetic Keyboard"**
3. Toque em **"Teclado padrão"** e selecione Hermetic Keyboard

### Manualmente (Outros Android):
1. Configurações → Sistema → Idioma e entrada → Teclado na tela
2. Ative Hermetic Keyboard
3. Defina como padrão

### Testar:
Abra qualquer app com campo de texto (WhatsApp, Notas, etc.) — o teclado Hermetic deve aparecer.

**Layout do teclado:**
```
┌───┬───┬───┬───┬───┬───┬───┬───┬───┬───┐
│ q │ w │ e │ r │ t │ y │ u │ i │ o │ p │
├───┼───┼───┼───┼───┼───┼───┼───┼───┼───┘
│ a │ s │ d │ f │ g │ h │ j │ k │ l │
├────┼───┼───┼───┼───┼───┼───┼───┼────┤
│ ⇧  │ z │ x │ c │ v │ b │ n │ m │ ⌫  │
├────┼───┼───┼───────────────┼───┼────┤
│?123│ 🔮│ א │    espaço     │ . │ ↵  │
└────┴───┴───┴───────────────┴───┴────┘

🔮 = Painel de símbolos herméticos
א  = Teclado hebraico transliterado
```

---

## Debug e Logs

### Ver logs do teclado em tempo real:
```bash
adb logcat -s HermeticKB:D

# Filtrar múltiplas tags
adb logcat -s HermeticKB:D SymbolPanel:D HebrewKB:D

# Salvar em arquivo
adb logcat -s HermeticKB > debug.log
```

### Comandos úteis:
```bash
# Forçar fechar o teclado (se travar)
adb shell am force-stop com.hermetic.keyboard.debug

# Desinstalar
adb uninstall com.hermetic.keyboard.debug

# Screenshot
adb exec-out screencap -p > screenshot.png

# Informações do device
adb shell getprop ro.product.model
adb shell getprop ro.build.version.release
adb shell wm size
```

### Reconectar Wi-Fi (se a conexão cair):
```bash
# A porta muda quando a depuração é reativada
# Verifique a nova porta no celular e reconecte:
adb connect 192.168.X.X:NOVA_PORTA
```

---

## Executando Testes

### Testes unitários (rodam no PC, sem device):
```bash
gradle testDebugUnitTest --no-daemon

# Classe específica
gradle testDebugUnitTest --no-daemon --tests "com.hermetic.keyboard.symbols.search.SearchEngineTest"
```

### Testes instrumentados (requerem device conectado):
```bash
gradle connectedDebugAndroidTest --no-daemon
```

---

## CI/CD

O projeto usa **GitHub Actions** com 3 pipelines:

| Pipeline | Trigger | O que faz |
|----------|---------|-----------|
| **CI** | Push / PR para main | Lint → Build → Unit Tests → Instrumented Tests |
| **Release** | Tag `v*` | Build release → Assina APK → Cria GitHub Release |
| **Nightly** | Cron 03:00 UTC | Build completo + todos os testes |

### Configurar Secrets para Release:

No GitHub: Settings → Secrets → Actions:

| Secret | Valor |
|--------|-------|
| `KEYSTORE_FILE` | Keystore em base64 |
| `KEYSTORE_PASSWORD` | Senha da keystore |
| `KEY_ALIAS` | Alias da chave |
| `KEY_PASSWORD` | Senha da chave |

### Gerar Keystore:
```bash
keytool -genkey -v -keystore hermetic-keyboard.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias hermetic-key -storepass SUA_SENHA \
  -dname "CN=Hermetic Keyboard, O=MarcosHermeticum, C=BR"

# Converter para base64 (para GitHub Secret)
# Windows PowerShell:
[Convert]::ToBase64String([IO.File]::ReadAllBytes("hermetic-keyboard.jks")) | Set-Content keystore_base64.txt
# Linux/macOS:
base64 -w 0 hermetic-keyboard.jks > keystore_base64.txt
```

### Criar release:
```bash
git tag v1.0.0
git push origin v1.0.0
# GitHub Actions gera e publica o APK automaticamente
```

---

## Estrutura do Projeto

```
Hermetic-Symbol-Keyboard/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/hermetic/keyboard/
│   │   │   │   ├── ime/HermeticIME.kt              # InputMethodService
│   │   │   │   ├── ui/
│   │   │   │   │   ├── QwertyKeyboardView.kt       # Teclado QWERTY
│   │   │   │   │   ├── KeyboardLayoutManager.kt    # Gerencia views
│   │   │   │   │   ├── panel/HermeticPanelView.kt  # Painel símbolos
│   │   │   │   │   ├── panel/SymbolGridAdapter.kt   # Grid adapter
│   │   │   │   │   └── hebrew/HebrewKeyboardView.kt # Teclado hebraico
│   │   │   │   ├── symbols/
│   │   │   │   │   ├── model/Symbol.kt             # Modelos
│   │   │   │   │   ├── data/SymbolDatabase.kt      # Room DB
│   │   │   │   │   ├── data/SymbolDataProvider.kt  # JSON loader
│   │   │   │   │   ├── repository/SymbolRepository.kt
│   │   │   │   │   └── search/SearchEngine.kt      # Busca
│   │   │   │   └── settings/SettingsActivity.kt
│   │   │   ├── res/
│   │   │   │   ├── raw/symbols.json                # 75 símbolos
│   │   │   │   ├── values/colors.xml               # Branding
│   │   │   │   ├── values/strings.xml              # EN
│   │   │   │   ├── values-pt-rBR/strings.xml       # PT-BR
│   │   │   │   └── values-es/strings.xml           # ES
│   │   │   └── AndroidManifest.xml
│   │   ├── test/                                   # Unit tests
│   │   └── androidTest/                            # Instrumented tests
│   └── build.gradle.kts
├── .github/workflows/                              # CI/CD
├── base.json                                       # Especificação
├── docs/SETUP_SAMSUNG_DEBUG.md                     # Guia Samsung
├── local.properties                                # (não commitado)
└── build.gradle.kts
```

---

## Categorias de Símbolos

| Categoria | Ícone | Qtd | Exemplos |
|-----------|-------|-----|----------|
| Planetary | ☉ | 10 | ☉ ☽ ☿ ♀ ♂ ♃ ♄ ♅ ♆ ♇ |
| Zodiac | ♈ | 12 | ♈ ♉ ♊ ♋ ♌ ♍ ♎ ♏ ♐ ♑ ♒ ♓ |
| Elements | 🜂 | 4 | 🜂 🜁 🜃 🜄 |
| Alchemy | 🜍 | 4 | 🜔 🜍 ☿ 🜪 |
| Aleph Beit | א | 27 | א ב ג ד ה ו ז ח ט י כ ל מ נ ס ע פ צ ק ר ש ת |
| Egyptian | ☥ | 1 | ☥ |
| Esoteric | ✡ | 17 | ✡ ☤ ☯ ⛤ ⛧ ∞ △ ▽ |

---

## Teclado Hebraico Transliterado

Acessível pela tecla **א** no teclado principal.

| Tecla mostra | Insere | Gematria |
|:---:|:---:|:---:|
| Aleph | א | 1 |
| Bet | ב | 2 |
| Gimel | ג | 3 |
| Dalet | ד | 4 |
| He | ה | 5 |
| Vav | ו | 6 |
| Zayin | ז | 7 |
| Chet | ח | 8 |
| Tet | ט | 9 |
| Yod | י | 10 |
| Kaf | כ | 20 |
| Lamed | ל | 30 |
| Mem | מ | 40 |
| Nun | נ | 50 |
| Samekh | ס | 60 |
| Ayin | ע | 70 |
| Pe | פ | 80 |
| Tsade | צ | 90 |
| Qof | ק | 100 |
| Resh | ר | 200 |
| Shin | ש | 300 |
| Tav | ת | 400 |
| Kaf· | ך | 500 |
| Mem· | ם | 600 |
| Nun· | ן | 700 |
| Pe· | ף | 800 |
| Tsade· | ץ | 900 |

Long-press em qualquer tecla mostra: nome completo + caractere + valor de gematria.

---

## Contribuindo

```bash
git clone https://github.com/marcoshermeticum/Hermetic-Symbol-Keyboard.git
cd Hermetic-Symbol-Keyboard
git checkout -b feature/minha-feature

# Desenvolver...

gradle testDebugUnitTest --no-daemon  # Garantir que testes passam
git add -A
git commit -m "feat: descrição da feature"
git push origin feature/minha-feature
# Abrir PR no GitHub
```

### Convenções:
- Kotlin para código novo
- Commits: `feat:`, `fix:`, `docs:`, `test:`, `refactor:`

---

## Troubleshooting

### "adb não reconhecido"
```powershell
# Adicionar ao PATH da sessão
$env:Path += ";C:\platform-tools"
# Ou reinstalar: baixe de https://developer.android.com/tools/releases/platform-tools
```

### "JAVA_HOME not set"
```powershell
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.19.10-hotspot"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
```

### Build falha: "SDK not found"
```powershell
# Criar local.properties na raiz do projeto
"sdk.dir=C\:\\Android\\Sdk" | Out-File -Encoding utf8 local.properties
```

### "adb connect" falha (conexão recusada)
- A porta muda toda vez que a depuração sem fio é reativada
- No celular: Opções do desenvolvedor → Depuração sem fio → verifique a porta atual
- Celular e PC precisam estar na **mesma rede Wi-Fi**

### "device unauthorized"
- Desconecte e reconecte (USB) ou reconecte (Wi-Fi)
- No celular, aceite o popup "Permitir depuração USB?"
- Se não aparecer: Opções do desenvolvedor → "Revogar autorizações" → reconecte

### Teclado instalou mas não aparece
- Reinicie o dispositivo
- Vá em Configurações → Apps → Hermetic Keyboard → verifique se está habilitado
- `adb shell am start -a android.settings.INPUT_METHOD_SETTINGS`

### Símbolos alquímicos aparecem como □
- Caracteres U+1F700+ requerem fontes compatíveis
- Solução futura: embutir Noto Sans Symbols 2 em `app/src/main/res/font/`

### Build lento na primeira vez
Normal. A primeira compilação baixa ~300 MB de dependências. Builds seguintes levam ~30s.

---

## Licença

**GNU General Public License v3.0** — veja [LICENSE](LICENSE).

---

## Links Úteis

- [Android IME Docs](https://developer.android.com/develop/ui/views/touch-and-input/creating-input-method)
- [Unicode Alchemical Symbols](https://www.unicode.org/charts/PDF/U1F700.pdf)
- [Unicode Hebrew Block](https://www.unicode.org/charts/PDF/U0590.pdf)
- [Noto Sans Symbols 2](https://fonts.google.com/noto/specimen/Noto+Sans+Symbols+2)
- [ADB Releases](https://developer.android.com/tools/releases/platform-tools)
- [Samsung USB Driver](https://developer.samsung.com/android-usb-driver)
