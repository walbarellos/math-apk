#pragma once
#include <string>
#include <vector>
#include <cstdint>

namespace basecalc {

struct ConversionStep {
    long long dividend;
    int  base;
    long long quotient;
    int  remainder;
    char digit;
};

struct BaseResult {
    int         base;
    std::string label;        // "Base 2 — Binário"
    std::string intPart;      // digits in that base
    std::string fracPart;     // "" if integer
    bool        fracRepeats;  // true if periodic
    std::string repeatPart;   // the periodic block, if any
    std::vector<ConversionStep> steps;
    bool        valid;
    std::string error;
};

struct CalcResult {
    double      value;
    bool        isInteger;
    std::string expression;
    std::string decimalFull;   // full decimal string, no rounding
    std::vector<BaseResult> bases;
    bool        ok;
    std::string error;
};

// Evaluate a simple infix expression (+, -, *, /, %)
// Returns ok=false if expression is malformed or division by zero.
CalcResult evaluate(const std::string& expr);

// Convert a decimal value to a specific base.
// maxFracDigits: how many fractional digits to compute (0 = integers only).
BaseResult toBase(double value, int base, int maxFracDigits = 4);

// Convert a number in a given base back to decimal (for verification).
// Returns false in `ok` if digits are invalid for the base.
struct BackConvert { double value; bool ok; std::string error; };
BackConvert fromBase(const std::string& digits, int base);

// Human-readable label for a base.
std::string baseLabel(int base);

// Build step-by-step string for classroom display.
// Returns lines like "37 ÷ 4 = 9  resto 1"
std::string stepsToString(const std::vector<ConversionStep>& steps);

} // namespace basecalc
