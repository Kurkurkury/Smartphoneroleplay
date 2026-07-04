# On-Device-Modell

Die App ist auf lokale Smartphone-KI vorbereitet.

## Ziel

Keine PC-Server-KI. Die KI soll direkt auf dem Android-Geraet laufen.

## Erwarteter Modellname

Die App erwartet spaeter diese Datei:

```text
local-roleplay-model.gguf
```

Das gewuenschte GGUF-Modell muss auf dem Smartphone entsprechend umbenannt werden.

## Erwarteter Speicherort

Nach der Installation liegt der App-spezifische Modellordner ungefaehr hier:

```text
Android/data/com.kurkurkury.smartphoneroleplay/files/models/
```

Dort soll die Datei liegen:

```text
Android/data/com.kurkurkury.smartphoneroleplay/files/models/local-roleplay-model.gguf
```

## Aktueller Stand

Die App erkennt den Modellpfad und ist architektonisch auf On-Device-KI vorbereitet.

Zusaetzlich ist jetzt eine native Android-Laufzeit vorbereitet:

- Android NDK/CMake ist im Build aktiviert
- arm64-v8a ist als Zielarchitektur gesetzt
- `NativeLlamaBridge.kt` verbindet Kotlin mit JNI
- `native_runtime.cpp` baut eine native Shared Library
- `OnDeviceReplyClient` ruft die native Bridge auf

## Noch nicht erledigt

Die echte GGUF-Token-Generierung ist noch nicht eingebunden. Der aktuelle native Code ist bewusst ein stabiler Platzhalter, damit die App weiter erfolgreich baut.

## Naechster technischer Schritt

Die native Platzhalter-Schicht muss durch einen echten GGUF-Inferenzkern ersetzt werden. Dafuer wird eine Android-kompatible llama.cpp/JNI-Integration benoetigt.
