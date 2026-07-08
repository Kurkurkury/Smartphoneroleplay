# Smartphoneroleplay

Android/Kotlin-App fuer eine lokale Smartphone-Roleplay-Chat-App.

## Produktstand: 0.7.0 Safe Mode

Diese Version ist eine stabile Smartphone-App-Stufe nach Realgeraete-Test:

- native Android-App-Struktur ohne externe Serverpflicht
- Chat-Oberflaeche im Dark-UI-Stil
- Demo-Charaktere: Reya, Kael, Mira
- Charakterwechsel mit Speicherung des zuletzt gewaehlten Charakters
- lokale Demo-Antwortlogik ohne Server
- lokale Chat-Speicherung pro Charakter
- Chat leeren Funktion
- Erstellung eigener Charaktere
- GGUF-Modellimport und GGUF-Modell loeschen
- MediaPipe `.task` / `.litertlm` Import und Engine-Modell loeschen
- sicherer KI-Test ohne gefaehrlichen GGUF Load/Decode im Hauptprozess
- Hintergrund-Antwortgenerierung, damit die UI nicht blockiert
- fail-safe KI-Schicht: Die App bleibt startbar und bedienbar, auch wenn ein Modell nicht nutzbar ist
- GitHub Actions Workflow fuer Debug-APK-Build

## Wichtige technische Entscheidung

Ein Realgeraete-Test mit einem ca. 2.43 GB GGUF-Modell hat gezeigt, dass der direkte llama.cpp Load/Decode im App-Hauptprozess nativ crashen kann. Solche Native-Crashes koennen nicht sauber mit Kotlin `try/catch` abgefangen werden.

Deshalb ist der direkte GGUF-Chat in Version 0.7.0 bewusst gesperrt. Importierte GGUF-Modelle bleiben sichtbar und verwaltbar, aber die App nutzt sie nicht blind fuer Chatantworten. Dadurch bleibt die App stabil.

Aktiver stabiler Modus:

1. MediaPipe Engine-Modell, falls kompatibles `.task` oder `.litertlm` importiert ist
2. Demo/GGUF Safe Mode, falls nur GGUF importiert ist
3. Demo-Modus ohne Modell

## Eigene Charaktere erstellen

Im Eingabefeld folgendes Format verwenden und danach den Button `Neu` druecken:

```text
Name; Beschreibung; Begruessung
```

Beispiel:

```text
Luna; Vampirjaegerin in einer dunklen Stadt; Ich bin Luna. Bleib nah bei mir.
```

## Modellverwaltung in der App

- `GGUF-Modell`: `.gguf` importieren oder ersetzen
- `Engine-Modell`: `.task` oder `.litertlm` fuer MediaPipe importieren
- `GGUF weg`: importiertes GGUF-Modell loeschen
- `Engine weg`: importiertes MediaPipe-Engine-Modell loeschen
- `KI-Test`: sichere Diagnose anzeigen, ohne den gefaehrlichen GGUF Load/Decode im Hauptprozess auszufuehren

## Lokale KI / GGUF

GGUF bleibt das langfristig flexible Ziel, weil sehr viele Modelle als GGUF verfuegbar sind. In dieser stabilen Version wird GGUF aber nur als Safe-Mode-Modell verwaltet.

Fuer echte direkte GGUF-Inferenz ist der naechste technische Schritt ein isolierter Testpfad, z. B. separater Prozess oder kontrollierter Mini-Modell-Test. Bis dahin sollte die Haupt-App nicht erneut durch einen nativen Crash beendet werden.

Empfehlung fuer spaetere Tests:

- zuerst sehr kleine GGUF-Modelle testen
- bevorzugt unter ca. 1 GB
- keine 2B+ Modelle als ersten Smartphone-Test verwenden
- bei Crash: Modell ueber `GGUF weg` entfernen und kleineres Modell verwenden

## Build

Standard-Build:

```bash
gradle :app:assembleDebug
```

Fallback-Build ohne native llama.cpp-Unterstuetzung:

```bash
gradle :app:assembleDebug -PenableNativeLlama=false
```

Der Native-Build verwendet `app/src/main/cpp/CMakeLists.txt` und bindet llama.cpp via CMake FetchContent ein. Zielplattform ist aktuell `arm64-v8a`.

## Bedienung

1. App installieren und starten.
2. Im Demo-Modus Chat, Charakterwechsel, neue Charaktere und Speicherung testen.
3. Optional GGUF importieren; bei grossen Dateien bleibt die App im GGUF Safe Mode.
4. Optional kompatibles `.task` / `.litertlm` MediaPipe-Modell importieren.
5. `KI-Test` fuer sichere Diagnose nutzen.

## Naechste Ausbaustufen

1. Isolierten GGUF-Testprozess bauen, damit Native-Crashes die Haupt-App nicht killen
2. Kleines empfohlenes GGUF-Modell fuer Zielgeraete festlegen
3. Persistente echte GGUF-Session erst aktivieren, wenn der isolierte Testpfad stabil ist
4. Memory/Langzeitgedaechtnis verbessern
5. Szenario-Auswahl und Charakterprofile visuell ausbauen
