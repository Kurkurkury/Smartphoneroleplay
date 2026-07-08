# Smartphoneroleplay

Android/Kotlin-App fuer eine lokale Smartphone-Roleplay-Chat-App.

## Produktstand

Diese Version ist ein stabiler Android-Prototyp mit vorbereitetem echten lokalen KI-Pfad:

- native Android-App-Struktur ohne externe Serverpflicht
- Chat-Oberflaeche im Dark-UI-Stil
- Demo-Charaktere: Reya, Kael, Mira
- Charakterwechsel
- lokale Demo-Antwortlogik ohne Server
- lokale Chat-Speicherung pro Charakter
- Chat leeren Funktion
- Erstellung eigener Charaktere
- separater GGUF-Import fuer alten llama.cpp Diagnosepfad
- separater Engine-Modellimport fuer MediaPipe `.task` / `.litertlm`
- MediaPipe LLM Runtime als aktiver lokaler Engine-Pfad, wenn ein kompatibles Engine-Modell importiert ist
- persistente MediaPipe-Session, damit das Modell nicht bei jeder Antwort neu geladen wird
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

## Lokale KI / Engine-Modell

Der aktuelle echte lokale KI-Pfad ist MediaPipe. Dafuer muss in der App ueber `Engine-Modell` ein kompatibles `.task` oder `.litertlm` Modellpaket importiert werden.

Wichtig:

- `.task` / `.litertlm` = aktueller Engine-Pfad fuer MediaPipe/LiteRT-LM
- `.gguf` = alter llama.cpp Import-/Diagnosepfad, nicht der aktive Chat-Engine-Pfad
- ZIP-Dateien werden nicht direkt akzeptiert; waehle die eigentliche `.task` oder `.litertlm` Datei aus
- sehr grosse Modelle koennen auf Smartphones wegen RAM/Geraetegrenzen fehlschlagen

Wenn ein Engine-Modell vorhanden ist, versucht der normale Chat automatisch den MediaPipe-Pfad. Wenn keine Engine vorhanden ist, nutzt die App den stabilen Demo-Modus und markiert die Antwort entsprechend.

## Build

Stabiler Standard-Build ohne Native-llama.cpp-Pflicht:

```bash
gradle :app:assembleDebug
```

Optionaler alter Native-llama.cpp Diagnose-Build:

```bash
gradle :app:assembleDebug -PenableNativeLlama=true
```

Im Native-Modus wird `app/src/main/cpp/CMakeLists.txt` verwendet und llama.cpp via CMake FetchContent eingebunden. Der direkte llama.cpp Decode-Pfad ist aber bewusst nicht der aktive Chat-Pfad, weil er auf dem Testgeraet native Crashes verursacht hat.

## Bedienung

1. App in Android Studio oeffnen oder Debug-APK bauen.
2. App auf Smartphone oder Emulator starten.
3. Optional ueber `Engine-Modell` ein `.task` oder `.litertlm` Modell importieren.
4. Mit `KI-Test` pruefen, ob die Engine geladen werden kann.
5. Im Chat normal schreiben.

## Aktuelle naechste Ausbaustufen

1. Ein konkretes kleines `.task` / `.litertlm` Modell fuer Zielgeraete festlegen und dokumentieren
2. MediaPipe-Parameter fuer Geschwindigkeit, Speicher und Antwortqualitaet feinjustieren
3. Memory/Langzeitgedaechtnis verbessern
4. Szenario-Auswahl und Charakterprofile visuell ausbauen
5. Optional: Bildgenerierung passend zur Handlung ueber externen Dienst oder lokale Bridge
