#include <jni.h>
#include <string>
#include <vector>
#include <algorithm>
#include "llama.h"

static std::string jstring_to_string(JNIEnv* env, jstring value) {
    if (value == nullptr) return "";
    const char* chars = env->GetStringUTFChars(value, nullptr);
    std::string result(chars == nullptr ? "" : chars);
    if (chars != nullptr) env->ReleaseStringUTFChars(value, chars);
    return result;
}

static std::string token_to_piece(const llama_vocab* vocab, llama_token token) {
    char buffer[256];
    int length = llama_token_to_piece(vocab, token, buffer, sizeof(buffer), 0, true);
    if (length < 0) return "";
    return std::string(buffer, length);
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_kurkurkury_smartphoneroleplay_ai_NativeLlamaBridge_nativeStatus(
        JNIEnv* env,
        jobject /* this */) {
    std::string status = "Native Laufzeit aktiv. llama.cpp ist verlinkt.";
    return env->NewStringUTF(status.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_kurkurkury_smartphoneroleplay_ai_NativeLlamaBridge_nativeGenerate(
        JNIEnv* env,
        jobject /* this */,
        jstring modelPath,
        jstring prompt) {
    const std::string model_path = jstring_to_string(env, modelPath);
    const std::string prompt_text = jstring_to_string(env, prompt);

    if (model_path.empty()) {
        return env->NewStringUTF("Fehler: Modellpfad ist leer.");
    }
    if (prompt_text.empty()) {
        return env->NewStringUTF("Fehler: Prompt ist leer.");
    }

    ggml_backend_load_all();

    llama_model_params model_params = llama_model_default_params();
    model_params.n_gpu_layers = 0;

    llama_model* model = llama_model_load_from_file(model_path.c_str(), model_params);
    if (model == nullptr) {
        std::string error = "Fehler: GGUF-Modell konnte nicht geladen werden: ";
        error += model_path;
        return env->NewStringUTF(error.c_str());
    }

    const llama_vocab* vocab = llama_model_get_vocab(model);
    const int n_predict = 96;

    int n_prompt = -llama_tokenize(
            vocab,
            prompt_text.c_str(),
            static_cast<int32_t>(prompt_text.size()),
            nullptr,
            0,
            true,
            true
    );

    if (n_prompt <= 0) {
        llama_model_free(model);
        return env->NewStringUTF("Fehler: Prompt konnte nicht tokenisiert werden.");
    }

    std::vector<llama_token> prompt_tokens(static_cast<size_t>(n_prompt));
    int tokenized = llama_tokenize(
            vocab,
            prompt_text.c_str(),
            static_cast<int32_t>(prompt_text.size()),
            prompt_tokens.data(),
            static_cast<int32_t>(prompt_tokens.size()),
            true,
            true
    );

    if (tokenized < 0) {
        llama_model_free(model);
        return env->NewStringUTF("Fehler: Tokenisierung fehlgeschlagen.");
    }

    llama_context_params ctx_params = llama_context_default_params();
    ctx_params.n_ctx = std::max(512, n_prompt + n_predict + 16);
    ctx_params.n_batch = std::min(512, std::max(32, n_prompt));
    ctx_params.n_threads = 4;
    ctx_params.n_threads_batch = 4;
    ctx_params.no_perf = true;

    llama_context* ctx = llama_init_from_model(model, ctx_params);
    if (ctx == nullptr) {
        llama_model_free(model);
        return env->NewStringUTF("Fehler: Kontext konnte nicht erstellt werden.");
    }

    llama_sampler* sampler = llama_sampler_chain_init(llama_sampler_chain_default_params());
    llama_sampler_chain_add(sampler, llama_sampler_init_top_p(0.95f, 1));
    llama_sampler_chain_add(sampler, llama_sampler_init_temp(0.80f));
    llama_sampler_chain_add(sampler, llama_sampler_init_dist(1234));

    llama_batch batch = llama_batch_get_one(prompt_tokens.data(), tokenized);
    if (llama_decode(ctx, batch) != 0) {
        llama_sampler_free(sampler);
        llama_free(ctx);
        llama_model_free(model);
        return env->NewStringUTF("Fehler: Prompt-Auswertung fehlgeschlagen.");
    }

    std::string output;
    llama_token next_token;

    for (int i = 0; i < n_predict; ++i) {
        next_token = llama_sampler_sample(sampler, ctx, -1);
        if (llama_vocab_is_eog(vocab, next_token)) {
            break;
        }

        output += token_to_piece(vocab, next_token);

        llama_batch next_batch = llama_batch_get_one(&next_token, 1);
        if (llama_decode(ctx, next_batch) != 0) {
            output += "\n[Generierung abgebrochen: Decode-Fehler]";
            break;
        }
    }

    llama_sampler_free(sampler);
    llama_free(ctx);
    llama_model_free(model);

    if (output.empty()) {
        output = "Modell geladen, aber keine Ausgabe erzeugt.";
    }

    return env->NewStringUTF(output.c_str());
}
