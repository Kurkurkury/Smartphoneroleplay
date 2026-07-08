# Smartphoneroleplay

Android/Kotlin-App fuer eine lokale Smartphone-Roleplay-Chat-App.

## Produktstand

Diese Version ist ein stabiler Android-Prototyp mit direktem lokalen GGUF-KI-Pfad:

- native Android-App-Struktur ohne externe Serverpflicht
- Chat-Oberflaeche im Dark-UI-Stil
- Demo-Charaktere: Reya, Kael, Mira
- Charakterwechsel
- lokale Demo-Antwortlogik ohne Server
- lokale Chat-Speicherung pro Charakter
- Chat leeren Funktion
- Erstellung eigener Charaktere
- GGUF-Modellimport fuer direkten lokalen Chat ueber llama.cpp
- MediaPipe `.task` / `.litertlm` bleibt als zweiter Engine-Pfad erhalten
- Hintergrund-Antwortgenerierung, damit die UI bei echter Inferenz nicht blockiert
- fail-safe KI-Schicht: Wenn keine echte Engine verfuegbar ist oder die Engine fehlschlaegt, bleibt die App startbar und zeigt klare Status-/Fehlermeldungen
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

Der bevorzugte echte lokale KI-Pfad ist jetzt GGUF ueber llama.cpp.

In der App:

1. Button `GGUF-Modell` druecken.
2. Eine `.gguf` Datei auswaehlen.
3. Warten, bis der Import fertig ist.
4. `KI-Test` druecken.
5. Wenn die Diagnose OK ist, im Chat normal schreiben.

Wichtig:

- `.gguf` = bevorzugter lokaler Chatpfad
- `.task` / `.litertlm` = MediaPipe/LiteRT-LM Fallbackpfad
- kleine quantisierte Modelle sind auf Smartphones deutlich realistischer als grosse Modelle
- empfohlen fuer erste Tests: sehr kleine GGUF-Modelle im Bereich ca. 0.5B bis 2B Parameter und niedrige Quantisierungsgroesse
- grosse Modelle koennen wegen RAM, Laufzeit oder Geraetelimits fehlschlagen

Wenn ein GGUF-Modell vorhanden ist, versucht der normale Chat zuerst den GGUF/llama.cpp-Pfad. Wenn kein GGUF vorhanden ist, versucht die App ein MediaPipe-Engine-Modell. Wenn kein echtes Modell vorhanden ist, nutzt die App den Demo-Modus.

## Build

Standard-Build mit nativer GGUF-Unterstuetzung:

```bash
gradle :app:assembleDebug
```

Fallback-Build ohne native llama.cpp-Unterstuetzung:

```bash
gradle :app:assembleDebug -PenableNativeLlama=false
```

Der Native-Build verwendet `app/src/main/cpp/CMakeLists.txt` und bindet llama.cpp via CMake FetchContent ein. Zielplattform ist aktuell `arm64-v8a`.

## Bedienung

1. App in Android Studio oeffnen oder Debug-APK bauen.
2. App auf Smartphone oder Emulator starten.
3. Ueber `GGUF-Modell` ein kleines `.gguf` Modell importieren.
4. Mit `KI-Test` pruefen, ob die native Engine geladen werden kann.
5. Im Chat normal schreiben.

## Aktuelle naechste Ausbaustufen

1. Kleines empfohlenes GGUF-Modell fuer Zielgeraete festlegen und dokumentieren
2. llama.cpp-Parameter fuer Geschwindigkeit, Speicher und Antwortqualitaet feinjustieren
3. Native Session persistent halten, damit GGUF nicht pro Antwort neu geladen werden muss
4. Memory/Langzeitgedaechtnis verbessern
5. Szenario-Auswahl und Charakterprofile visuell ausbauen
