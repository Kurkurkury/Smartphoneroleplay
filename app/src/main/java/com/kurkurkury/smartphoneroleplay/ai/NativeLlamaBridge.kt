package com.kurkurkury.smartphoneroleplay.ai

class NativeLlamaBridge {
    companion object {
        init {
            System.loadLibrary("smartphoneroleplay_native")
        }
    }

    external fun nativeStatus(): String

    external fun nativeGenerate(modelPath: String, prompt: String): String
}
