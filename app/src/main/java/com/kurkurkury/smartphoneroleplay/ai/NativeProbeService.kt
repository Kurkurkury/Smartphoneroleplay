package com.kurkurkury.smartphoneroleplay.ai

import android.app.Service
import android.content.Intent
import android.os.IBinder

class NativeProbeService : Service() {
    companion object {
        const val ACTION_RESULT = "com.kurkurkury.smartphoneroleplay.NATIVE_PROBE_RESULT"
        const val EXTRA_MODEL_PATH = "model_path"
        const val EXTRA_RESULT = "result"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val modelPath = intent?.getStringExtra(EXTRA_MODEL_PATH).orEmpty()
        Thread {
            val result = try {
                if (modelPath.isBlank()) {
                    "Isolierter Native-Probe\nFehler: Modellpfad fehlt."
                } else {
                    val bridge = NativeLlamaBridge()
                    "Isolierter Native-Probe\n" + bridge.sessionDecodeDiagnostic(modelPath).text
                }
            } catch (error: Throwable) {
                "Isolierter Native-Probe\nFehler: ${error.message ?: error.javaClass.simpleName}"
            }
            sendBroadcast(Intent(ACTION_RESULT).apply {
                setPackage(packageName)
                putExtra(EXTRA_RESULT, result)
            })
            stopSelf(startId)
        }.start()
        return START_NOT_STICKY
    }
}
