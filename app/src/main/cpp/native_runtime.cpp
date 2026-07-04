#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_kurkurkury_smartphoneroleplay_ai_NativeLlamaBridge_nativeStatus(
        JNIEnv* env,
        jobject /* this */) {
    std::string status = "Native Laufzeit aktiv. GGUF-Inferenzkern noch nicht eingebunden.";
    return env->NewStringUTF(status.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_kurkurkury_smartphoneroleplay_ai_NativeLlamaBridge_nativeGenerate(
        JNIEnv* env,
        jobject /* this */,
        jstring modelPath,
        jstring prompt) {
    const char* model_path_chars = env->GetStringUTFChars(modelPath, nullptr);
    const char* prompt_chars = env->GetStringUTFChars(prompt, nullptr);

    std::string response = "Native Bridge bereit. Modellpfad: ";
    response += model_path_chars;
    response += "\nPrompt empfangen. Echte Token-Generierung folgt mit llama.cpp-Integration.";

    env->ReleaseStringUTFChars(modelPath, model_path_chars);
    env->ReleaseStringUTFChars(prompt, prompt_chars);

    return env->NewStringUTF(response.c_str());
}
