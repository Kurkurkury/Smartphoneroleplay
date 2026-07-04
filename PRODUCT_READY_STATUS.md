# Product Ready Status

Status: `PRODUCT_READY_ANDROID_PROTOTYPE`

Branch: `finish/product-ready-v1`

## Fertiggestellt

- Android/Kotlin-Prototyp ist als stabile lokale App nutzbar.
- Standard-Build ist nicht mehr von NDK/CMake/llama.cpp abhaengig.
- Native llama.cpp ist optional ueber `-PenableNativeLlama=true` aktivierbar.
- Fehlende Native-Bibliothek fuehrt nicht mehr zum App-Crash.
- Lokaler Demo-Modus bleibt immer verfuegbar.
- GitHub Actions Debug-APK-Build ist vorhanden.
- Android Manifest, Theme und Gitignore wurden gehärtet.

## Build-Kommandos

Stabiler Standard-Build:

```bash
gradle :app:assembleDebug --no-daemon
```

Experimenteller Native-Build:

```bash
gradle :app:assembleDebug --no-daemon -PenableNativeLlama=true
```

## Einschraenkung

Der Native-llama.cpp-Pfad ist vorbereitet, aber ohne echten Android-NDK-Buildlauf auf einem passenden Runner/Geraet nicht final runtime-verifiziert. Der stabile Produktstand ist deshalb der Android-Prototyp mit fail-safe Demo-Modus plus optionaler Native-Erweiterung.
