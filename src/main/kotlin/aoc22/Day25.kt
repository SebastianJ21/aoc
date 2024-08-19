package aoc22

import readInput
import java.math.BigInteger

class Day25 {
    fun solve() {
        val rawInput = readInput("day25.txt")
        val exponents = prepareExponents(rawInput)
        val answer = rawInput.sumOf { convertSNAFUToDecadic(it, exponents).sum() }
        println(answer)

        // 20-1-0=-2=-2220=0011 TODO decoding
    }

    private fun convertSNAFUToDecadic(
        snafuNumber: String,
        exponents: List<Long>,
    ): List<Long> {
        val parseSnafu = { snafuDecimal: Char ->
            when (snafuDecimal) {
                '-' -> -1
                '=' -> -2
                else -> snafuDecimal.digitToInt()
            }
        }

        return snafuNumber
            .reversed()
            .mapIndexed { index, char -> parseSnafu(char) * exponents[index] }
    }

    private fun prepareExponents(input: List<String>): List<Long> {
        val longestLine = input.maxOf { it.length }
        return (0..longestLine + 4).map {
            BigInteger.valueOf(5L).pow(it).toLong()
        }
    }
}
