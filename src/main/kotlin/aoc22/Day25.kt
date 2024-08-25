@file:Suppress("MemberVisibilityCanBePrivate")

package aoc22

import readInput
import java.math.BigInteger
import kotlin.math.abs

class Day25 {

    val snafuRange = -2..2

    fun solve() {
        val rawInput = readInput("day25.txt")

        val exponents = prepareExponents(rawInput)

        val decadicAnswer = rawInput.sumOf { convertSNAFUToDecadic(it, exponents).sum() }
        val result = convertDecadicToSNAFU(decadicAnswer)

        println("Part one: $result")
    }

    fun convertDecadicToSNAFU(value: Long): String {
        val exponents = generateSequence(1L) { it * 5L }
            .zipWithNext()
            .takeWhile { (it, _) -> it < value }
            .flatMap { it.toList() }
            .distinct()
            .toList()
            .reversed()

        val exponentValues = snafuRange.toList()

        val (_, resultSnafuExponents) = exponents.fold(0L to listOf<Int>()) { (currentValue, result), exponent ->
            val (newExponent, updatedValue) = exponentValues
                .associateWith { exponent * it + currentValue }
                .minBy { (_, exponentValue) -> abs(exponentValue - value) }

            updatedValue to (result + newExponent)
        }

        val resultSnafu = resultSnafuExponents.dropWhile { it == 0 }.joinToString("") { convertToSnafu(it).toString() }

        return resultSnafu
    }

    fun convertToSnafu(value: Int): Char = when (value) {
        -2 -> '='
        -1 -> '-'
        0 -> '0'
        1 -> '1'
        2 -> '2'
        else -> error("Illegal value $value")
    }

    fun parseSnafu(value: Char): Int {
        val parsedValue = when (value) {
            '-' -> -1
            '=' -> -2
            else -> value.digitToInt()
        }

        check(parsedValue in snafuRange)

        return parsedValue
    }

    fun convertSNAFUToDecadic(snafuNumber: String, exponents: List<Long>): List<Long> = snafuNumber
        .reversed()
        .mapIndexed { index, char -> parseSnafu(char) * exponents[index] }

    fun prepareExponents(input: List<String>): List<Long> {
        val longestLine = input.maxOf { it.length }

        return (0..longestLine).map {
            BigInteger.valueOf(5L).pow(it).toLong()
        }
    }
}
