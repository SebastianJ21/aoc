package aoc25

import AOCAnswer
import AOCSolution
import mapToLong
import readInput

class Day2 : AOCSolution {

    override fun solve(): AOCAnswer {
        val rawInput = readInput("day2.txt", AOCYear.TwentyFive)

        val ranges = rawInput.single()
            .split(",")
            .map { rawRange -> rawRange.split("-").mapToLong().let { (a, b) -> a..b } }

        val maxValue = ranges.maxOf { it.last } * 10L // Max value we will need to check decimal places for
        val powersOf10 = generateSequence(1L) { it * 10 }.takeWhile { it < maxValue }.toList()

        val partOne = ranges.sumOf { range ->
            range.sumOf { value -> value.takeIf { it.isInvalid(powersOf10) } ?: 0L }
        }
        val partTwo = ranges.sumOf { range -> range.sumOf { value -> value.takeIf { it.isInvalid2() } ?: 0L } }

        return AOCAnswer(partOne, partTwo)
    }

    private fun Long.isInvalid(powersOf10: List<Long>): Boolean {
        val digits = powersOf10.indexOfFirst { it > this }
        if (digits % 2 != 0) return false

        val mid = digits / 2

        val decimals = powersOf10[mid]

        val left = (this / decimals)
        val right = (this - (left * decimals))

        return left == right
    }

    private fun Long.isInvalid2(): Boolean {
        val strValue = toString()

        val mid = strValue.length / 2

        return (1..mid).any { subStrSize ->
            if (strValue.length % subStrSize != 0) return@any false
            // Fast path out if it does not match
            if (strValue[subStrSize - 1] != strValue.last()) return@any false

            val subStr = strValue.take(subStrSize)
            subStr.repeat(strValue.length / subStrSize) == strValue
        }
    }
}
