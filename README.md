# Smartphoneroleplay

Android/Kotlin-App fuer eine lokale Smartphone-Roleplay-Chat-App.

## Produktstand

Diese Version ist als stabiler Android-Prototyp fertiggestellt:

- native Android-App-Struktur ohne externe Serverpflicht
- Chat-Oberflaeche im Dark-UI-Stil
- Demo-Charaktere: Reya, Kael, Mira
- Charakterwechsel
- lokale Demo-Antwortlogik ohne Server
- lokale Chat-Speicherung pro Charakter
- Chat leeren Funktion
- Erstellung eigener Charaktere
- optionaler GGUF-Modellimport
- fail-safe KI-Schicht: Wenn die native KI nicht vorhanden ist, bleibt die App startbar und nutzt den Demo-Modus
- GitHub Actions Workflow fuer Debug-APK-Build

## Eigene Charaktere erstellen

Im Eingabefeld folgendes Format verwenden und danach den Button `Neu` druecken:

```text
Name; Beschreibung; Begruessung
```

Beispiel:

```text
Luna; Vampirjaegerin in einer dunklen Stadt; Ich bin Luna. Bleib nah bei mir.
```

## Lokale KI / GGUF-Modell

Die App kann eine GGUF-Datei importieren. Der stabile Standard-Build ist bewusst ohne Pflicht-Native-Build konfiguriert, damit App und CI nicht an NDK, CMake oder llama.cpp scheitern.

Standardmodus:

```bash
gradle :app:assembleDebug
```

Optionaler Native-Modus:

```bash
gradle :app:assembleDebug -PenableNativeLlama=true
```

Im Native-Modus wird `app/src/main/cpp/CMakeLists.txt` verwendet und llama.cpp via CMake FetchContent eingebunden. Der Native-Modus ist experimentell und primaer fuer echte Android-Geraete mit `arm64-v8a` gedacht.

## Build-Hinweis

Repository in Android Studio oeffnen, Gradle synchronisieren und `app` auf einem Android-Geraet oder Emulator starten. Ohne Native-Flag startet die App im stabilen lokalen Demo-Modus.

## Aktuelle naechste Ausbaustufen

1. Native llama.cpp auf konkretem Android-Geraet testen
2. Modell-Empfehlung und Import-Anleitung fuer kleine GGUF-Modelle ergaenzen
3. Memory/Langzeitgedaechtnis verbessern
4. Szenario-Auswahl und Charakterprofile visuell ausbauen
5. Optional: Bildgenerierung passend zur Handlung ueber externen Dienst oder lokale Bridge
