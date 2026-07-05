#include <jni.h>
#include <string>
#include <vector>
#include <algorithm>
#include <fstream>
#include <sstream>
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

static bool has_gguf_magic(const std::string& model_path) {
    std::ifstream file(model_path, std::ios::binary);
    if (!file.good()) return false;
    char magic[4] = {0, 0, 0, 0};
    file.read(magic, 4);
    return file.gcount() == 4 && magic[0] == 'G' && magic[1] == 'G' && magic[2] == 'U' && magic[3] == 'F';
}

static std::string run_llama_generation(
        const std::string& model_path,
        const std::string& prompt_text,
        int n_predict,
        int n_ctx_target) {
    if (model_path.empty()) return "Fehler: Modellpfad ist leer.";
    if (prompt_text.empty()) return "Fehler: Prompt ist leer.";

    ggml_backend_load_all();
    llama_model_params model_params = llama_model_default_params();
    model_params.n_gpu_layers = 0;

    llama_model* model = llama_model_load_from_file(model_path.c_str(), model_params);
    if (model == nullptr) return "Fehler: GGUF-Modell konnte nicht geladen werden.";

    const llama_vocab* vocab = llama_model_get_vocab(model);
    int n_prompt = -llama_tokenize(vocab, prompt_text.c_str(), static_cast<int32_t>(prompt_text.size()), nullptr, 0, true, true);
    if (n_prompt <= 0) {
        llama_model_free(model);
        return "Fehler: Prompt konnte nicht tokenisiert werden.";
    }

    std::vector<llama_token> prompt_tokens(static_cast<size_t>(n_prompt));
    int tokenized = llama_tokenize(vocab, prompt_text.c_str(), static_cast<int32_t>(prompt_text.size()), prompt_tokens.data(), static_cast<int32_t>(prompt_tokens.size()), true, true);
    if (tokenized < 0) {
        llama_model_free(model);
        return "Fehler: Tokenisierung fehlgeschlagen.";
    }

    llama_context_params ctx_params = llama_context_default_params();
    ctx_params.n_ctx = std::max(n_ctx_target, tokenized + n_predict + 16);
    ctx_params.n_batch = std::min(128, std::max(16, tokenized));
    ctx_params.n_threads = 2;
    ctx_params.n_threads_batch = 2;
    ctx_params.no_perf = true;

    llama_context* ctx = llama_init_from_model(model, ctx_params);
    if (ctx == nullptr) {
        llama_model_free(model);
        return "Fehler: Kontext konnte nicht erstellt werden.";
    }

    llama_sampler* sampler = llama_sampler_chain_init(llama_sampler_chain_default_params());
    llama_sampler_chain_add(sampler, llama_sampler_init_top_p(0.90f, 1));
    llama_sampler_chain_add(sampler, llama_sampler_init_temp(0.70f));
    llama_sampler_chain_add(sampler, llama_sampler_init_dist(1234));

    llama_batch batch = llama_batch_get_one(prompt_tokens.data(), tokenized);
    if (llama_decode(ctx, batch) != 0) {
        llama_sampler_free(sampler);
        llama_free(ctx);
        llama_model_free(model);
        return "Fehler: Prompt-Auswertung fehlgeschlagen.";
    }

    std::string output;
    for (int i = 0; i < n_predict; ++i) {
        llama_token next_token = llama_sampler_sample(sampler, ctx, -1);
        if (llama_vocab_is_eog(vocab, next_token)) break;
        output += token_to_piece(vocab, next_token);
        llama_batch next_batch = llama_batch_get_one(&next_token, 1);
        if (llama_decode(ctx, next_batch) != 0) break;
    }

    llama_sampler_free(sampler);
    llama_free(ctx);
    llama_model_free(model);

    if (output.empty()) output = "Modell geladen, aber keine Ausgabe erzeugt.";
    return output;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_kurkurkury_smartphoneroleplay_ai_NativeLlamaBridge_nativeStatus(JNIEnv* env, jobject) {
    std::string status = "Engine aktiv: llama.cpp native ist verlinkt.";
    return env->NewStringUTF(status.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_kurkurkury_smartphoneroleplay_ai_NativeLlamaBridge_nativeMiniInferenceDiagnostic(JNIEnv* env, jobject, jstring modelPath) {
    const std::string model_path = jstring_to_string(env, modelPath);
    std::ostringstream report;
    report << "Engine Diagnose\n";
    report << "1. Engine: llama.cpp native OK\n";
    report << "2. Dieser sichere Test laedt das Modell noch NICHT\n";

    if (model_path.empty()) {
        report << "3. Modellpfad: FEHLER - leer";
        return env->NewStringUTF(report.str().c_str());
    }

    std::ifstream probe(model_path, std::ios::binary | std::ios::ate);
    if (!probe.good()) {
        report << "3. Modellpfad: FEHLER - Datei nicht lesbar";
        return env->NewStringUTF(report.str().c_str());
    }

    const auto size_bytes = probe.tellg();
    report << "3. Modellpfad: OK\n";
    report << "4. Dateigroesse: " << static_cast<long long>(size_bytes / 1024 / 1024) << " MB\n";

    if (!has_gguf_magic(model_path)) {
        report << "5. GGUF Header: FEHLER";
        return env->NewStringUTF(report.str().c_str());
    }

    report << "5. GGUF Header: OK\n";
    report << "6. Naechster separater Schritt: Modell-Load-Test";
    return env->NewStringUTF(report.str().c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_kurkurkury_smartphoneroleplay_ai_NativeLlamaBridge_nativeGenerate(JNIEnv* env, jobject, jstring modelPath, jstring prompt) {
    const std::string output = run_llama_generation(jstring_to_string(env, modelPath), jstring_to_string(env, prompt), 96, 512);
    return env->NewStringUTF(output.c_str());
}
