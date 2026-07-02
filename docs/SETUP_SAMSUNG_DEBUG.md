# 📱 Configurar Samsung Galaxy A30s para Depuração USB

Guia passo a passo para configurar seu **Samsung Galaxy A30s (SM-A307GT)** como dispositivo de depuração para desenvolvimento Android.

> Este guia também funciona para outros dispositivos Samsung com One UI.

---

## Pré-requisitos no PC

- Android Studio instalado (ou pelo menos o ADB via SDK Platform-Tools)
- Cabo USB **com suporte a dados** (nem todo cabo carrega dados!)
- [Samsung USB Driver](https://developer.samsung.com/android-usb-driver) instalado (Windows)

### Instalar Samsung USB Driver (Windows)

1. Baixe em: https://developer.samsung.com/android-usb-driver
2. Extraia o ZIP
3. Execute `SAMSUNG_USB_Driver_for_Mobile_Phones.exe`
4. Siga o instalador até o fim
5. **Reinicie o PC**

---

## Passo 1: Ativar Opções de Desenvolvedor

1. Abra **Configurações**
2. Role até o final e toque em **Sobre o telefone**
3. Toque em **Informações do software**
4. Toque **7 vezes seguidas** em **Número da versão**
   - Vai pedir sua senha/PIN/padrão de desbloqueio
   - Aparecerá a mensagem: *"Você agora é um desenvolvedor!"*
5. Volte para a tela principal de Configurações
6. Agora aparecerá a opção **Opções do desenvolvedor** (acima de "Sobre o telefone")

> 💡 Se já ativou antes, pule este passo.

---

## Passo 2: Ativar Depuração USB

1. Abra **Configurações → Opções do desenvolvedor**
2. Certifique-se de que o toggle principal (topo da tela) está **ATIVADO**
3. Role até a seção **Depuração**
4. Ative **Depuração USB**
   - Confirme no popup: "Permitir a depuração USB?"
   - Toque **OK**

---

## Passo 3: Ativar Instalação via USB (Samsung específico)

Ainda em **Opções do desenvolvedor**:

1. Role até encontrar **Instalar via USB**
2. Ative esta opção
   - Pode pedir para conectar à internet para verificação Samsung — aceite
   - Pode pedir login na Samsung Account — faça login se necessário

> ⚠️ Sem esta opção ativada, o ADB consegue conectar mas **não consegue instalar APKs**.

---

## Passo 4: Configurações adicionais recomendadas

Ainda em **Opções do desenvolvedor**, ative também:

| Opção | Para que serve |
|-------|----------------|
| **Permanecer ativo** | Tela não apaga enquanto carregando (útil durante debug) |
| **Permitir desbloqueio de OEM** | Necessário se futuramente quiser desbloquear bootloader |
| **Selecionar app de depuração** | Selecione "Hermetic Keyboard" quando estiver instalado |
| **Aguardar depurador** | (Opcional) O app espera você conectar o debugger antes de iniciar — útil para debugar o onCreate |

---

## Passo 5: Conectar ao PC

1. **Conecte o cabo USB** do celular ao PC
2. No celular aparecerá uma notificação **"Conectado como..."** (ou "Opções USB")
3. Toque na notificação e selecione: **Transferência de arquivos (MTP)**
   - Alguns modos USB não permitem ADB funcionar
4. Aparecerá um popup: **"Permitir depuração USB?"**
   - Marque ✅ **"Sempre permitir neste computador"**
   - Toque **Permitir**

> 🔑 Se o popup não aparecer:
> - Desconecte e reconecte o cabo
> - Troque a porta USB no PC
> - Verifique se o Samsung USB Driver está instalado
> - Tente outro cabo USB

---

## Passo 6: Verificar conexão no PC

Abra o terminal (PowerShell ou CMD):

```bash
adb devices
```

Saída esperada:
```
List of devices attached
XXXXXXXXXXXXXXX    device
```

Se aparecer `unauthorized`:
- Verifique no celular se há popup pendente de autorização
- Desconecte, reconecte, e aceite o popup

Se aparecer vazio:
```bash
# Reiniciar servidor ADB
adb kill-server
adb start-server
adb devices
```

---

## Passo 7: Testar a conexão

```bash
# Ver informações do dispositivo
adb shell getprop ro.product.model
# Deve retornar: SM-A307GT

# Ver versão do Android
adb shell getprop ro.build.version.release
# Deve retornar: 11 (ou superior)

# Listar teclados instalados
adb shell ime list -s

# Abrir configurações de teclado
adb shell am start -a android.settings.INPUT_METHOD_SETTINGS
```

---

## Passo 8: Instalar o APK de Debug

```bash
# A partir da raiz do projeto
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

Ou via Android Studio: selecione o device e clique **Run** (Shift+F10).

---

## Debug Wireless (sem cabo) — Android 11+

O Galaxy A30s com Android 11 suporta depuração sem fio:

### Pareamento inicial (uma vez)

1. No celular: **Opções do desenvolvedor → Depuração sem fio** → Ativar
2. Toque em **"Parear dispositivo com código de pareamento"**
3. Anote o **IP:Porta** e o **código** mostrados na tela

No PC:
```bash
adb pair 192.168.x.x:PORTA
# Digite o código de 6 dígitos mostrado no celular
# Deve retornar: Successfully paired
```

### Conectar (toda vez)

1. Em **Depuração sem fio**, anote o IP:Porta na seção principal (diferente da porta de pareamento!)

```bash
adb connect 192.168.x.x:PORTA
adb devices
# Deve mostrar o device como "device"
```

### Desconectar
```bash
adb disconnect 192.168.x.x:PORTA
```

> 📡 Celular e PC precisam estar na **mesma rede Wi-Fi**.

---

## Troubleshooting

### "device unauthorized"
- Desconecte o USB
- No celular: **Opções do desenvolvedor → Revogar autorizações de depuração USB** → Revogar
- Reconecte o cabo
- Aceite o popup no celular

### "no devices/emulators found"
- Verifique cabo USB (troque por um que você sabe que transfere dados)
- Reinstale o Samsung USB Driver
- Troque de porta USB (preferir portas traseiras, não hubs)
- Reinicie o ADB:
  ```bash
  adb kill-server
  adb start-server
  ```

### "INSTALL_FAILED_USER_RESTRICTED"
- Ative **"Instalar via USB"** nas Opções do desenvolvedor (Passo 3)
- Pode ser necessário login na Samsung Account

### "error: device still authorizing"
- Aguarde alguns segundos e tente novamente
- Se persistir, revogue autorizações e reconecte

### ADB detecta mas Android Studio não mostra o device
- Verifique que o Android Studio está usando o mesmo SDK que o ADB:
  ```bash
  adb version
  # Compare com: File → Settings → Android SDK → SDK Location
  ```
- Reinicie o Android Studio

### Celular desconecta durante debug
- Desative **otimização de bateria** para o app em debug
- Ative **"Permanecer ativo"** nas Opções do desenvolvedor
- Use um cabo USB de boa qualidade

---

## Resumo Rápido (Checklist)

- [ ] Samsung USB Driver instalado (Windows)
- [ ] Opções de Desenvolvedor ativadas (7 toques no "Número da versão")
- [ ] Depuração USB ativada
- [ ] Instalar via USB ativada
- [ ] Cabo USB com dados conectado
- [ ] Modo USB: Transferência de arquivos (MTP)
- [ ] Popup "Permitir depuração USB?" aceito (com "Sempre permitir")
- [ ] `adb devices` mostra o dispositivo como `device`
- [ ] Teste: `adb shell getprop ro.product.model` retorna `SM-A307GT`

---

## Referências

- [Documentação oficial: Depuração USB](https://developer.android.com/studio/debug/dev-options)
- [Samsung USB Drivers](https://developer.samsung.com/android-usb-driver)
- [ADB Command Reference](https://developer.android.com/tools/adb)
- [Depuração sem fio (Android 11+)](https://developer.android.com/tools/adb#connect-to-a-device-over-wi-fi)
