# Native Diagnostic Safe Mode

Status: `BUILD_TRIGGERED`

This branch intentionally keeps the normal send path native-free.

## Purpose

The app crashed when sending a message in the native APK, even without a selected model. This indicates that the crash is in the send/reply path rather than only in GGUF loading.

## Current safe-mode behavior

- Normal message sending does not instantiate `NativeLlamaBridge`.
- Normal message sending does not call native status or generation.
- The app replies through the stable demo fallback.
- If a model file exists, the response only reports its size from Kotlin file APIs.

## Next test

Install the APK from this branch and send a short message. Expected result: no crash.
