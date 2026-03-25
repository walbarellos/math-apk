package com.basecalc.core.conversion

import com.basecalc.core.model.BaseEntry
import com.basecalc.core.model.DivisionStep
import com.basecalc.core.model.FracStep
import com.basecalc.core.number.Rational
import com.basecalc.core.util.DigitSymbols
import java.math.BigInteger

/**
 * Converte um número racional ([Rational]) para uma base entre 2 e 16,
 * registrando todos os passos intermediários para fins didáticos.
 *
 * @param base  Base de destino.
 */
class BaseConverter(private val base: Int) {

    /**
     * Realiza a conversão.
     *
     * @param value             Número a converter.
     * @param maxFractionDigits Limite de dígitos fracionários calculados.
     */
    fun convert(value: Rational, maxFractionDigits: Int): BaseEntry {
        if (base < 2 || base > 16) {
            return BaseEntry(
                base = base,
                label = BaseLabelProvider.labelFor(base),
                intPart = "?",
                fracPart = "",
                repeatPart = "",
                repeats = false,
                steps = emptyList(),
                fracSteps = emptyList(),
                valid = false,
                error = "Base inválida",
            )
        }

        val negativo = value.numerador.signum() < 0
        val numeradorAbs = value.numerador.abs()
        val denominador = value.denominador

        val parteInteira = numeradorAbs / denominador
        val resto = numeradorAbs % denominador

        val (digitosInteiros, passosInteiros) = inteiroParaDigitos(parteInteira)

        val sinalizadoInteiro = if (negativo && (parteInteira != BigInteger.ZERO || resto != BigInteger.ZERO)) {
            "-$digitosInteiros"
        } else {
            digitosInteiros
        }

        val fracional = if (maxFractionDigits > 0 && resto != BigInteger.ZERO) {
            fracaoParaDigitos(resto, denominador, maxFractionDigits)
        } else {
            ResultadoFracional("", "", false, emptyList())
        }

        return BaseEntry(
            base = base,
            label = BaseLabelProvider.labelFor(base),
            intPart = sinalizadoInteiro,
            fracPart = fracional.antesDoBloco,
            repeatPart = fracional.blocoRepetido,
            repeats = fracional.repete,
            steps = passosInteiros,
            fracSteps = fracional.passos,
            valid = true,
            error = null,
        )
    }

    // ─── Parte inteira: divisões sucessivas ───────────────────────────────────

    /**
     * Converte a parte inteira [valor] por divisões sucessivas pela [base].
     * Retorna os dígitos (mais significativo primeiro) e os passos registrados.
     */
    private fun inteiroParaDigitos(valor: BigInteger): Pair<String, List<DivisionStep>> {
        if (valor == BigInteger.ZERO) return "0" to emptyList()

        val passos = mutableListOf<DivisionStep>()
        val digitos = StringBuilder()
        var atual = valor
        val baseBig = BigInteger.valueOf(base.toLong())

        while (atual > BigInteger.ZERO) {
            val quociente = atual / baseBig
            val resto = (atual % baseBig).toInt()
            val digitoChar = DigitSymbols.digitToChar(resto)

            passos.add(
                DivisionStep(
                    dividendo = atual,
                    base = base,
                    quociente = quociente,
                    resto = resto,
                    digito = digitoChar,
                )
            )
            digitos.insert(0, digitoChar)
            atual = quociente
        }

        return digitos.toString() to passos
    }

    // ─── Parte fracionária: multiplicações sucessivas ─────────────────────────

    /**
     * Converte a parte fracionária pelo método das multiplicações sucessivas.
     * Detecta dízima periódica comparando restos já vistos.
     *
     * @param restoInicial  Numerador do resto inicial (< [denominador]).
     * @param denominador   Denominador da fração original.
     * @param maxDigitos    Limite máximo de dígitos calculados.
     */
    private fun fracaoParaDigitos(
        restoInicial: BigInteger,
        denominador: BigInteger,
        maxDigitos: Int,
    ): ResultadoFracional {
        val vistos = mutableMapOf<BigInteger, Int>()  // resto → índice em que apareceu
        val digitos = StringBuilder()
        val passos = mutableListOf<FracStep>()
        val baseBig = BigInteger.valueOf(base.toLong())

        var resto = restoInicial
        var indiceRepeticao = -1
        var indice = 0

        while (resto != BigInteger.ZERO && indice < maxDigitos) {
            if (vistos.containsKey(resto)) {
                indiceRepeticao = vistos.getValue(resto)
                break
            }
            vistos[resto] = indice

            val produto = resto * baseBig
            val digito = (produto / denominador).toInt()
            val novoResto = produto % denominador

            passos.add(
                FracStep(
                    restoAnterior = resto,
                    denominador = denominador,
                    base = base,
                    produto = produto,
                    digito = digito,
                    restoApos = novoResto,
                )
            )

            digitos.append(DigitSymbols.digitToChar(digito))
            resto = novoResto
            indice++
        }

        // Verifica repetição não detectada dentro do loop (resto atingido ao sair por maxDigitos)
        if (resto != BigInteger.ZERO && indiceRepeticao == -1 && vistos.containsKey(resto)) {
            indiceRepeticao = vistos.getValue(resto)
        }

        return if (indiceRepeticao >= 0) {
            ResultadoFracional(
                antesDoBloco = digitos.substring(0, indiceRepeticao),
                blocoRepetido = digitos.substring(indiceRepeticao),
                repete = true,
                passos = passos,
            )
        } else {
            ResultadoFracional(
                antesDoBloco = digitos.toString(),
                blocoRepetido = "",
                repete = false,
                passos = passos,
            )
        }
    }

    // ─── Modelo interno ───────────────────────────────────────────────────────

    /** Resultado intermediário da conversão fracionária. */
    private data class ResultadoFracional(
        val antesDoBloco: String,
        val blocoRepetido: String,
        val repete: Boolean,
        val passos: List<FracStep>,
    )
}
