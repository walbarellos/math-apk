#include <jni.h>
#include <string>
#include "BaseConverter.h"

using namespace basecalc;

// Helper: new Java String from std::string
static jstring toJString(JNIEnv* env, const std::string& s) {
    return env->NewStringUTF(s.c_str());
}

// Helper: std::string from jstring
static std::string fromJString(JNIEnv* env, jstring js) {
    const char* chars = env->GetStringUTFChars(js, nullptr);
    std::string s(chars);
    env->ReleaseStringUTFChars(js, chars);
    return s;
}

extern "C" {

// ─── evaluate(expr) → JSON string ────────────────────────────────────────────
//
// Returns a JSON object the Kotlin ViewModel will parse.
// Format:
// {
//   "ok": true/false,
//   "error": "...",
//   "value": 42.0,
//   "decimal": "42",
//   "bases": [
//     { "base": 2, "label": "Base 2 — Binário", "int": "101010",
//       "frac": "", "repeats": false, "repeat": "",
//       "steps": "42 ÷ 2 = 21  resto 0\n..." },
//     ...
//   ]
// }

JNIEXPORT jstring JNICALL
Java_com_basecalc_BaseConverterJNI_evaluate(JNIEnv* env, jobject /*thiz*/, jstring jexpr) {
    std::string expr = fromJString(env, jexpr);
    CalcResult r = evaluate(expr);

    // Build JSON manually (no external JSON lib needed)
    auto escape = [](const std::string& s) -> std::string {
        std::string out;
        out.reserve(s.size() + 4);
        for (char c : s) {
            if (c == '"')  out += "\\\"";
            else if (c == '\\') out += "\\\\";
            else if (c == '\n') out += "\\n";
            else if (c == '\r') out += "\\r";
            else                out += c;
        }
        return out;
    };

    std::ostringstream json;
    json << "{";
    json << "\"ok\":" << (r.ok ? "true" : "false") << ",";
    json << "\"error\":\"" << escape(r.error) << "\",";

    if (r.ok) {
        json << "\"value\":" << r.value << ",";
        json << "\"decimal\":\"" << escape(r.decimalFull) << "\",";
        json << "\"bases\":[";
        for (size_t i = 0; i < r.bases.size(); ++i) {
            const auto& b = r.bases[i];
            if (i > 0) json << ",";
            json << "{";
            json << "\"base\":" << b.base << ",";
            json << "\"label\":\"" << escape(b.label) << "\",";
            json << "\"valid\":" << (b.valid ? "true" : "false") << ",";
            json << "\"int\":\"" << escape(b.intPart) << "\",";
            json << "\"frac\":\"" << escape(b.fracPart) << "\",";
            json << "\"repeats\":" << (b.fracRepeats ? "true" : "false") << ",";
            json << "\"repeat\":\"" << escape(b.repeatPart) << "\",";
            json << "\"steps\":\"" << escape(stepsToString(b.steps)) << "\"";
            json << "}";
        }
        json << "]";
    }
    json << "}";

    return toJString(env, json.str());
}

// ─── validateDigits(digits, base) → error string or "" ──────────────────────

JNIEXPORT jstring JNICALL
Java_com_basecalc_BaseConverterJNI_validateAndConvert(JNIEnv* env, jobject /*thiz*/,
                                                       jstring jdigits, jint base) {
    std::string digits = fromJString(env, jdigits);
    BackConvert bc = fromBase(digits, (int)base);
    if (!bc.ok) return toJString(env, std::string("{\"ok\":false,\"error\":\"") + bc.error + "\"}");
    std::ostringstream json;
    json << "{\"ok\":true,\"value\":" << bc.value << "}";
    return toJString(env, json.str());
}

} // extern "C"
