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

static std::string run_llama_generation(const std::string& model_path, const std::string& prompt_text, int n_predict, int n_ctx_target) {
    if (model_path.empty()) return "Fehler: Modellpfad ist leer.";
    if (prompt_text.empty()) return "Fehler: Prompt ist leer.";

    ggml_backend_load_all();
    llama_model_params model_params = llama_model_default_params();
    model_params.n_gpu_layers = 0;

    llama_model* model = llama_model_load_from_file(model_path.c_str(), model_params);
    if (model == nullptr) return "Fehler: GGUF-Modell konnte nicht geladen werden.";

    const llama_vocab* vocab = llama_model_get_vocab(model);
    int n_prompt = -llama_tokenize(vocab, prompt_text.c_str(), static_cast<int32_t>(prompt_text.size()), nullptr, 0, true, true);
    if (n_prompt <= 0) { llama_model_free(model); return "Fehler: Prompt konnte nicht tokenisiert werden."; }

    std::vector<llama_token> prompt_tokens(static_cast<size_t>(n_prompt));
    int tokenized = llama_tokenize(vocab, prompt_text.c_str(), static_cast<int32_t>(prompt_text.size()), prompt_tokens.data(), static_cast<int32_t>(prompt_tokens.size()), true, true);
    if (tokenized < 0) { llama_model_free(model); return "Fehler: Tokenisierung fehlgeschlagen."; }

    llama_context_params ctx_params = llama_context_default_params();
    ctx_params.n_ctx = std::max(n_ctx_target, tokenized + n_predict + 16);
    ctx_params.n_batch = std::min(128, std::max(16, tokenized));
    ctx_params.n_threads = 2;
    ctx_params.n_threads_batch = 2;
    ctx_params.no_perf = true;

    llama_context* ctx = llama_init_from_model(model, ctx_params);
    if (ctx == nullptr) { llama_model_free(model); return "Fehler: Kontext konnte nicht erstellt werden."; }

    llama_sampler* sampler = llama_sampler_chain_init(llama_sampler_chain_default_params());
    llama_sampler_chain_add(sampler, llama_sampler_init_greedy());

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
Java_com_kurkurkury_smartphoneroleplay_ai_NativeLlamaBridge_nativeModelLoadDiagnostic(JNIEnv* env, jobject, jstring modelPath) {
    const std::string model_path = jstring_to_string(env, modelPath);
    std::ostringstream report;
    report << "Modell-Load-Test\n";
    report << "1. Engine: llama.cpp native OK\n";
    report << "2. Kontext/Inferenz: NICHT aktiv\n";
    if (model_path.empty()) { report << "3. Modellpfad: FEHLER - leer"; return env->NewStringUTF(report.str().c_str()); }
    if (!has_gguf_magic(model_path)) { report << "3. GGUF Header: FEHLER"; return env->NewStringUTF(report.str().c_str()); }
    report << "3. GGUF Header: OK\n";
    report << "4. Modell-Load: startet\n";
    ggml_backend_load_all();
    llama_model_params model_params = llama_model_default_params();
    model_params.n_gpu_layers = 0;
    llama_model* model = llama_model_load_from_file(model_path.c_str(), model_params);
    if (model == nullptr) { report << "5. Modell-Load: FEHLER - nullptr"; return env->NewStringUTF(report.str().c_str()); }
    report << "5. Modell-Load: OK\n";
    report << "6. Modell wird sofort freigegeben\n";
    llama_model_free(model);
    report << "7. Modell-Free: OK\n";
    report << "Naechster separater Schritt: Kontext-Test";
    return env->NewStringUTF(report.str().c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_kurkurkury_smartphoneroleplay_ai_NativeLlamaBridge_nativeContextDiagnostic(JNIEnv* env, jobject, jstring modelPath) {
    const std::string model_path = jstring_to_string(env, modelPath);
    std::ostringstream report;
    report << "Kontext-Test\n";
    report << "1. Engine: llama.cpp native OK\n";
    report << "2. Inferenz: NICHT aktiv\n";
    if (model_path.empty()) { report << "3. Modellpfad: FEHLER - leer"; return env->NewStringUTF(report.str().c_str()); }
    if (!has_gguf_magic(model_path)) { report << "3. GGUF Header: FEHLER"; return env->NewStringUTF(report.str().c_str()); }
    report << "3. GGUF Header: OK\n";
    report << "4. Modell-Load: startet\n";
    ggml_backend_load_all();
    llama_model_params model_params = llama_model_default_params();
    model_params.n_gpu_layers = 0;
    llama_model* model = llama_model_load_from_file(model_path.c_str(), model_params);
    if (model == nullptr) { report << "5. Modell-Load: FEHLER - nullptr"; return env->NewStringUTF(report.str().c_str()); }
    report << "5. Modell-Load: OK\n";
    report << "6. Kontext-Erstellung: startet\n";
    llama_context_params ctx_params = llama_context_default_params();
    ctx_params.n_ctx = 256;
    ctx_params.n_batch = 64;
    ctx_params.n_threads = 2;
    ctx_params.n_threads_batch = 2;
    ctx_params.no_perf = true;
    llama_context* ctx = llama_init_from_model(model, ctx_params);
    if (ctx == nullptr) { llama_model_free(model); report << "7. Kontext-Erstellung: FEHLER - nullptr"; return env->NewStringUTF(report.str().c_str()); }
    report << "7. Kontext-Erstellung: OK\n";
    report << "8. Kontext wird sofort freigegeben\n";
    llama_free(ctx);
    report << "9. Kontext-Free: OK\n";
    llama_model_free(model);
    report << "10. Modell-Free: OK\n";
    report << "Naechster separater Schritt: Decode-Test";
    return env->NewStringUTF(report.str().c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_kurkurkury_smartphoneroleplay_ai_NativeLlamaBridge_nativeMiniInferenceDiagnostic(JNIEnv* env, jobject, jstring modelPath) {
    const std::string model_path = jstring_to_string(env, modelPath);
    std::ostringstream report;
    report << "Decode-Erstes-Token-Test\n";
    report << "1. Engine: llama.cpp native OK\n";
    report << "2. Prompt: Hello\n";
    report << "3. Sampling: greedy, 1 Token\n";
    if (model_path.empty()) { report << "4. Modellpfad: FEHLER - leer"; return env->NewStringUTF(report.str().c_str()); }
    if (!has_gguf_magic(model_path)) { report << "4. GGUF Header: FEHLER"; return env->NewStringUTF(report.str().c_str()); }
    report << "4. GGUF Header: OK\n";
    report << "5. Modell-Load: startet\n";
    ggml_backend_load_all();
    llama_model_params model_params = llama_model_default_params();
    model_params.n_gpu_layers = 0;
    llama_model* model = llama_model_load_from_file(model_path.c_str(), model_params);
    if (model == nullptr) { report << "6. Modell-Load: FEHLER - nullptr"; return env->NewStringUTF(report.str().c_str()); }
    report << "6. Modell-Load: OK\n";

    const llama_vocab* vocab = llama_model_get_vocab(model);
    const std::string prompt = "Hello";
    int n_prompt = -llama_tokenize(vocab, prompt.c_str(), static_cast<int32_t>(prompt.size()), nullptr, 0, true, true);
    if (n_prompt <= 0) { llama_model_free(model); report << "7. Tokenisierung: FEHLER - Laenge"; return env->NewStringUTF(report.str().c_str()); }
    std::vector<llama_token> tokens(static_cast<size_t>(n_prompt));
    int tokenized = llama_tokenize(vocab, prompt.c_str(), static_cast<int32_t>(prompt.size()), tokens.data(), static_cast<int32_t>(tokens.size()), true, true);
    if (tokenized < 0) { llama_model_free(model); report << "7. Tokenisierung: FEHLER"; return env->NewStringUTF(report.str().c_str()); }
    report << "7. Tokenisierung: OK\n";
    report << "8. Token-Anzahl: " << tokenized << "\n";

    llama_context_params ctx_params = llama_context_default_params();
    ctx_params.n_ctx = 256;
    ctx_params.n_batch = 64;
    ctx_params.n_threads = 2;
    ctx_params.n_threads_batch = 2;
    ctx_params.no_perf = true;
    llama_context* ctx = llama_init_from_model(model, ctx_params);
    if (ctx == nullptr) { llama_model_free(model); report << "9. Kontext-Erstellung: FEHLER - nullptr"; return env->NewStringUTF(report.str().c_str()); }
    report << "9. Kontext-Erstellung: OK\n";

    report << "10. Decode Prompt: startet\n";
    llama_batch batch = llama_batch_get_one(tokens.data(), tokenized);
    int decode_result = llama_decode(ctx, batch);
    if (decode_result != 0) {
        llama_free(ctx);
        llama_model_free(model);
        report << "11. Decode Prompt: FEHLER - code " << decode_result;
        return env->NewStringUTF(report.str().c_str());
    }
    report << "11. Decode Prompt: OK\n";

    llama_sampler* sampler = llama_sampler_chain_init(llama_sampler_chain_default_params());
    llama_sampler_chain_add(sampler, llama_sampler_init_greedy());
    llama_token first_token = llama_sampler_sample(sampler, ctx, -1);
    std::string piece = token_to_piece(vocab, first_token);
    report << "12. Erstes Token: OK\n";
    report << "13. Token-ID: " << first_token << "\n";
    report << "14. Text: " << (piece.empty() ? "<leer>" : piece) << "\n";
    report << "15. Aufraeumen: startet\n";
    llama_sampler_free(sampler);
    llama_free(ctx);
    llama_model_free(model);
    report << "16. Aufraeumen: OK\n";
    report << "Naechster separater Schritt: Mehr-Token-Generierung";
    return env->NewStringUTF(report.str().c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_kurkurkury_smartphoneroleplay_ai_NativeLlamaBridge_nativeGenerate(JNIEnv* env, jobject, jstring modelPath, jstring prompt) {
    const std::string output = run_llama_generation(jstring_to_string(env, modelPath), jstring_to_string(env, prompt), 96, 512);
    return env->NewStringUTF(output.c_str());
}
