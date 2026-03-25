package com.basecalc

/**
 * Ponte JNI para o motor C++.
 * Todas as chamadas pesadas de conversão ficam no lado nativo.
 */
object BaseConverterJNI {

    init {
        System.loadLibrary("basecalc")
    }

    /**
     * Avalia uma expressão aritmética e retorna JSON com os resultados
     * em todas as bases. Veja jni_bridge.cpp para o formato do JSON.
     */
    external fun evaluate(expr: String): String

    /**
     * Valida dígitos e converte um número de uma base específica para decimal.
     * Retorna JSON: {"ok":true,"value":42.0} ou {"ok":false,"error":"..."}
     */
    external fun validateAndConvert(digits: String, base: Int): String
}
