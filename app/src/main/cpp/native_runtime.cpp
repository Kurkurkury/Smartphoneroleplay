#include <jni.h>
#include <string>
#include <vector>
#include <fstream>
#include <sstream>
#include "llama.h"

static std::string js(JNIEnv* env, jstring v) {
    if (!v) return "";
    const char* c = env->GetStringUTFChars(v, nullptr);
    std::string r(c ? c : "");
    if (c) env->ReleaseStringUTFChars(v, c);
    return r;
}

static bool gguf(const std::string& p) {
    std::ifstream f(p, std::ios::binary);
    char m[4] = {0,0,0,0};
    f.read(m, 4);
    return f.gcount() == 4 && m[0] == 'G' && m[1] == 'G' && m[2] == 'U' && m[3] == 'F';
}

static std::string piece(const llama_vocab* vocab, llama_token tok) {
    char b[256];
    int n = llama_token_to_piece(vocab, tok, b, sizeof(b), 0, true);
    return n > 0 ? std::string(b, n) : std::string("<leer>");
}

static llama_batch one_token_batch(llama_token tok, int32_t pos, bool logits) {
    llama_batch b = llama_batch_init(1, 0, 1);
    b.n_tokens = 1;
    b.token[0] = tok;
    b.pos[0] = pos;
    b.n_seq_id[0] = 1;
    b.seq_id[0][0] = 0;
    b.logits[0] = logits;
    return b;
}

static std::string generate_simple(const std::string& model_path, const std::string& prompt, int max_tokens) {
    if (model_path.empty()) return "Fehler: Modellpfad ist leer.";
    if (prompt.empty()) return "Fehler: Prompt ist leer.";
    ggml_backend_load_all();
    llama_model_params mp = llama_model_default_params();
    mp.n_gpu_layers = 0;
    llama_model* model = llama_model_load_from_file(model_path.c_str(), mp);
    if (!model) return "Fehler: Modell konnte nicht geladen werden.";
    const llama_vocab* vocab = llama_model_get_vocab(model);
    int need = -llama_tokenize(vocab, prompt.c_str(), (int32_t)prompt.size(), nullptr, 0, true, true);
    if (need <= 0) { llama_model_free(model); return "Fehler: Tokenisierung."; }
    std::vector<llama_token> toks((size_t)need);
    int ntok = llama_tokenize(vocab, prompt.c_str(), (int32_t)prompt.size(), toks.data(), (int32_t)toks.size(), true, true);
    if (ntok <= 0) { llama_model_free(model); return "Fehler: Tokenisierung."; }
    llama_context_params cp = llama_context_default_params();
    cp.n_ctx = 512;
    cp.n_batch = 64;
    cp.n_threads = 2;
    cp.n_threads_batch = 2;
    cp.no_perf = true;
    llama_context* ctx = llama_init_from_model(model, cp);
    if (!ctx) { llama_model_free(model); return "Fehler: Kontext."; }
    for (int i = 0; i < ntok; ++i) {
        llama_batch b = one_token_batch(toks[(size_t)i], i, i == ntok - 1);
        int rc = llama_decode(ctx, b);
        llama_batch_free(b);
        if (rc != 0) { llama_free(ctx); llama_model_free(model); return "Fehler: Prompt-Decode."; }
    }
    llama_sampler* smp = llama_sampler_chain_init(llama_sampler_chain_default_params());
    llama_sampler_chain_add(smp, llama_sampler_init_greedy());
    std::string out;
    int pos = ntok;
    for (int i = 0; i < max_tokens; ++i) {
        llama_token t = llama_sampler_sample(smp, ctx, -1);
        if (llama_vocab_is_eog(vocab, t)) break;
        out += piece(vocab, t);
        llama_batch b = one_token_batch(t, pos++, true);
        int rc = llama_decode(ctx, b);
        llama_batch_free(b);
        if (rc != 0) break;
    }
    llama_sampler_free(smp);
    llama_free(ctx);
    llama_model_free(model);
    return out.empty() ? "<keine Ausgabe>" : out;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_kurkurkury_smartphoneroleplay_ai_NativeLlamaBridge_nativeStatus(JNIEnv* env, jobject) {
    return env->NewStringUTF("Engine aktiv: llama.cpp native ist verlinkt.");
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_kurkurkury_smartphoneroleplay_ai_NativeLlamaBridge_nativeModelLoadDiagnostic(JNIEnv* env, jobject, jstring modelPath) {
    std::string p = js(env, modelPath);
    std::ostringstream r;
    r << "Modell-Load-Test\n1. Engine: llama.cpp native OK\n2. Kontext/Inferenz: NICHT aktiv\n";
    if (!gguf(p)) { r << "3. GGUF Header: FEHLER"; return env->NewStringUTF(r.str().c_str()); }
    r << "3. GGUF Header: OK\n4. Modell-Load: startet\n";
    ggml_backend_load_all();
    llama_model_params mp = llama_model_default_params();
    mp.n_gpu_layers = 0;
    llama_model* model = llama_model_load_from_file(p.c_str(), mp);
    if (!model) { r << "5. Modell-Load: FEHLER"; return env->NewStringUTF(r.str().c_str()); }
    r << "5. Modell-Load: OK\n6. Modell wird sofort freigegeben\n";
    llama_model_free(model);
    r << "7. Modell-Free: OK\nNaechster separater Schritt: Kontext-Test";
    return env->NewStringUTF(r.str().c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_kurkurkury_smartphoneroleplay_ai_NativeLlamaBridge_nativeContextDiagnostic(JNIEnv* env, jobject, jstring modelPath) {
    std::string p = js(env, modelPath);
    std::ostringstream r;
    r << "Kontext-Test\n1. Engine: llama.cpp native OK\n2. Inferenz: NICHT aktiv\n";
    if (!gguf(p)) { r << "3. GGUF Header: FEHLER"; return env->NewStringUTF(r.str().c_str()); }
    r << "3. GGUF Header: OK\n4. Modell-Load: startet\n";
    ggml_backend_load_all();
    llama_model_params mp = llama_model_default_params();
    mp.n_gpu_layers = 0;
    llama_model* model = llama_model_load_from_file(p.c_str(), mp);
    if (!model) { r << "5. Modell-Load: FEHLER"; return env->NewStringUTF(r.str().c_str()); }
    r << "5. Modell-Load: OK\n6. Kontext-Erstellung: startet\n";
    llama_context_params cp = llama_context_default_params();
    cp.n_ctx = 256; cp.n_batch = 64; cp.n_threads = 2; cp.n_threads_batch = 2; cp.no_perf = true;
    llama_context* ctx = llama_init_from_model(model, cp);
    if (!ctx) { llama_model_free(model); r << "7. Kontext-Erstellung: FEHLER"; return env->NewStringUTF(r.str().c_str()); }
    r << "7. Kontext-Erstellung: OK\n8. Kontext wird sofort freigegeben\n";
    llama_free(ctx); llama_model_free(model);
    r << "9. Kontext-Free: OK\n10. Modell-Free: OK\nNaechster separater Schritt: Session-Decode-Test";
    return env->NewStringUTF(r.str().c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_kurkurkury_smartphoneroleplay_ai_NativeLlamaBridge_nativeMiniInferenceDiagnostic(JNIEnv* env, jobject, jstring modelPath) {
    std::string p = js(env, modelPath);
    std::ostringstream r;
    r << "Session-Decode-Test\n1. Batch: manuelle Positionen aktiv\n2. Prompt: Hallo Reya.\n3. Max Tokens: 16\n";
    std::string out = generate_simple(p, "Hallo Reya.", 16);
    if (out.rfind("Fehler:", 0) == 0) r << "4. Session-Decode: FEHLER\n" << out;
    else r << "4. Session-Decode: OK\n5. Antwort:\n" << out;
    return env->NewStringUTF(r.str().c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_kurkurkury_smartphoneroleplay_ai_NativeLlamaBridge_nativeGenerate(JNIEnv* env, jobject, jstring modelPath, jstring prompt) {
    std::string out = generate_simple(js(env, modelPath), js(env, prompt), 64);
    return env->NewStringUTF(out.c_str());
}
