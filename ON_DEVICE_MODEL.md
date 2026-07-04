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

Die App erkennt den Modellpfad und ist architektonisch auf On-Device-KI vorbereitet. Die native GGUF-Ausfuehrung ist noch nicht integriert.

## Naechster technischer Schritt

Ein nativer Android-Inferenzkern muss eingebunden werden, zum Beispiel ueber eine llama.cpp-basierte Android/JNI-Schicht. Danach kann die App die GGUF-Datei direkt laden und Antworten ohne PC-Server erzeugen.
