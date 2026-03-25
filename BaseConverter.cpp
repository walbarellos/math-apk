#include "BaseConverter.h"
#include <cmath>
#include <sstream>
#include <algorithm>
#include <map>
#include <stdexcept>
#include <cctype>
#include <cstring>

namespace basecalc {

// ─── helpers ────────────────────────────────────────────────────────────────

static const char DIGITS[] = "0123456789ABCDEF";

static char digitChar(int d) {
    if (d >= 0 && d < 16) return DIGITS[d];
    return '?';
}

static std::string intToBase(long long n, int base, std::vector<ConversionStep>& steps) {
    if (base < 2 || base > 16) return "?";
    steps.clear();
    if (n == 0) return "0";
    bool neg = (n < 0);
    long long abs_n = std::abs(n);
    std::string result;
    long long tmp = abs_n;
    while (tmp > 0) {
        long long q = tmp / base;
        int r = (int)(tmp % base);
        ConversionStep s;
        s.dividend  = tmp;
        s.base      = base;
        s.quotient  = q;
        s.remainder = r;
        s.digit     = digitChar(r);
        steps.push_back(s);
        result = digitChar(r) + result;
        tmp = q;
    }
    return neg ? "-" + result : result;
}

// Fractional part conversion with periodic detection.
// Returns {str_before_repeat, repeat_block, exact}.
static void fracToBase(double frac, int base, int maxDigits,
                       std::string& before, std::string& repeat, bool& exact) {
    before.clear(); repeat.clear(); exact = true;
    if (frac <= 0.0) return;

    // map fractional state → position, to detect cycle
    std::map<std::string, int> seen;
    std::string allDigits;

    for (int i = 0; i < maxDigits + 8 && i < 32; ++i) {
        frac *= base;
        int d = (int)frac;
        frac -= d;
        allDigits += digitChar(d);

        // Canonicalize frac for cycle detection
        char buf[32];
        snprintf(buf, sizeof(buf), "%.10f", frac);
        std::string key(buf);

        if (frac < 1e-9) {
            // Exact termination
            before = allDigits;
            exact = true;
            return;
        }

        auto it = seen.find(key);
        if (it != seen.end()) {
            before = allDigits.substr(0, it->second);
            repeat = allDigits.substr(it->second);
            exact  = false;
            return;
        }
        seen[key] = (int)allDigits.size();
    }
    // Truncated — not exact but no clean cycle found in window
    before = allDigits.substr(0, maxDigits);
    repeat = "";
    exact  = false;
}

// ─── public API ──────────────────────────────────────────────────────────────

std::string baseLabel(int base) {
    switch (base) {
        case 2:  return "Base 2 — Binário";
        case 3:  return "Base 3";
        case 4:  return "Base 4";
        case 5:  return "Base 5";
        case 6:  return "Base 6";
        case 7:  return "Base 7";
        case 8:  return "Base 8 — Octal";
        case 9:  return "Base 9";
        case 10: return "Base 10 — Decimal";
        case 16: return "Base 16 — Hexadecimal";
        default: return "Base " + std::to_string(base);
    }
}

BaseResult toBase(double value, int base, int maxFracDigits) {
    BaseResult r;
    r.base        = base;
    r.label       = baseLabel(base);
    r.fracRepeats = false;
    r.valid       = true;

    if (base < 2 || base > 16) {
        r.valid = false;
        r.error = "Base inválida";
        return r;
    }
    if (!std::isfinite(value)) {
        r.valid = false;
        r.error = "Valor não finito";
        return r;
    }

    long long intPart  = (long long)std::trunc(value);
    double    fracPart = std::abs(value - (double)intPart);

    r.intPart = intToBase(intPart, base, r.steps);

    if (maxFracDigits > 0 && fracPart > 1e-10) {
        std::string before, repeat;
        bool exact;
        fracToBase(fracPart, base, maxFracDigits, before, repeat, exact);
        r.fracPart    = before;
        r.repeatPart  = repeat;
        r.fracRepeats = !exact && !repeat.empty();
    }
    return r;
}

BackConvert fromBase(const std::string& digits, int base) {
    BackConvert bc;
    bc.ok = true;
    bc.value = 0.0;
    if (base < 2 || base > 16) { bc.ok = false; bc.error = "Base inválida"; return bc; }

    // Split on '.'
    size_t dotPos = digits.find('.');
    std::string intStr  = digits.substr(0, dotPos);
    std::string fracStr = (dotPos != std::string::npos) ? digits.substr(dotPos+1) : "";

    bool neg = (!intStr.empty() && intStr[0] == '-');
    if (neg) intStr = intStr.substr(1);

    double result = 0.0;
    for (char c : intStr) {
        int d = -1;
        if (c >= '0' && c <= '9') d = c - '0';
        else if (c >= 'A' && c <= 'F') d = 10 + (c - 'A');
        else if (c >= 'a' && c <= 'f') d = 10 + (c - 'a');
        if (d < 0 || d >= base) {
            bc.ok = false;
            bc.error = std::string("Dígito '") + c + "' inválido para base " + std::to_string(base);
            return bc;
        }
        result = result * base + d;
    }
    double fracMult = 1.0 / base;
    for (char c : fracStr) {
        int d = -1;
        if (c >= '0' && c <= '9') d = c - '0';
        else if (c >= 'A' && c <= 'F') d = 10 + (c - 'A');
        else if (c >= 'a' && c <= 'f') d = 10 + (c - 'a');
        if (d < 0 || d >= base) {
            bc.ok = false;
            bc.error = std::string("Dígito '") + c + "' inválido para base " + std::to_string(base);
            return bc;
        }
        result += d * fracMult;
        fracMult /= base;
    }
    bc.value = neg ? -result : result;
    return bc;
}

std::string stepsToString(const std::vector<ConversionStep>& steps) {
    std::ostringstream oss;
    for (size_t i = 0; i < steps.size(); ++i) {
        const auto& s = steps[i];
        oss << s.dividend << " ÷ " << s.base
            << " = " << s.quotient
            << "  resto " << s.remainder;
        if (i + 1 == steps.size())
            oss << "  ← primeiro dígito";
        oss << "\n";
    }
    oss << "(ler restos de baixo pra cima)";
    return oss.str();
}

// ─── expression evaluator (recursive descent, safe) ─────────────────────────

struct Parser {
    const char* p;
    const char* end;

    void skipWs() { while (p < end && isspace((unsigned char)*p)) p++; }

    double parseExpr();    // handles + -
    double parseTerm();    // handles * / %
    double parseFactor();  // handles unary -, parens, number
    double parseNumber();
};

double Parser::parseNumber() {
    skipWs();
    bool neg = false;
    if (p < end && *p == '-') { neg = true; p++; }
    if (p >= end || (!isdigit((unsigned char)*p) && *p != '.'))
        throw std::runtime_error("número esperado");
    char* ep;
    double v = strtod(p, &ep);
    if (ep == p) throw std::runtime_error("número inválido");
    p = ep;
    return neg ? -v : v;
}

double Parser::parseFactor() {
    skipWs();
    if (p >= end) throw std::runtime_error("expressão incompleta");
    if (*p == '(') {
        p++;
        double v = parseExpr();
        skipWs();
        if (p >= end || *p != ')') throw std::runtime_error("falta ')'");
        p++;
        return v;
    }
    return parseNumber();
}

double Parser::parseTerm() {
    double left = parseFactor();
    while (true) {
        skipWs();
        if (p >= end) break;
        char op = *p;
        if (op != '*' && op != '/' && op != '%') break;
        p++;
        double right = parseFactor();
        if (op == '*') left *= right;
        else if (op == '/') {
            if (right == 0.0) throw std::runtime_error("divisão por zero");
            left /= right;
        } else { // %
            if (right == 0.0) throw std::runtime_error("módulo por zero");
            left = std::fmod(left, right);
        }
    }
    return left;
}

double Parser::parseExpr() {
    skipWs();
    double left = parseTerm();
    while (true) {
        skipWs();
        if (p >= end) break;
        char op = *p;
        if (op != '+' && op != '-') break;
        p++;
        double right = parseTerm();
        left = (op == '+') ? left + right : left - right;
    }
    return left;
}

CalcResult evaluate(const std::string& expr) {
    CalcResult res;
    res.ok = false;

    if (expr.empty()) {
        res.error = "Expressão vazia";
        return res;
    }

    Parser parser;
    parser.p   = expr.c_str();
    parser.end = expr.c_str() + expr.size();

    try {
        double val = parser.parseExpr();
        parser.skipWs();
        if (parser.p != parser.end) {
            res.error = "Caractere inesperado: " + std::string(1, *parser.p);
            return res;
        }
        if (!std::isfinite(val)) {
            res.error = "Resultado inválido (infinito ou NaN)";
            return res;
        }
        res.value      = val;
        res.isInteger  = (std::trunc(val) == val);
        res.expression = expr;
        res.ok         = true;

        // Build decimal string without rounding
        if (res.isInteger) {
            res.decimalFull = std::to_string((long long)val);
        } else {
            char buf[64];
            snprintf(buf, sizeof(buf), "%.6g", val);
            res.decimalFull = buf;
        }

        // Convert to all target bases
        int targetBases[] = {2, 3, 4, 5, 6, 7, 8, 9, 10, 16};
        for (int b : targetBases) {
            res.bases.push_back(toBase(val, b, 4));
        }
    } catch (const std::exception& e) {
        res.error = e.what();
    }
    return res;
}

} // namespace basecalc
