package aoc25

import AOCAnswer
import AOCSolution
import AOCYear
import kotlinx.collections.immutable.toImmutableList
import mapToLong
import readInput

class Day2 : AOCSolution {

    private val powersOf10 = generateSequence(1L) { it * 10 }.takeWhile { it > 0 }.toImmutableList()

    override fun solve(): AOCAnswer {
        val rawInput = readInput("day2.txt", AOCYear.TwentyFive)

        val ranges = rawInput.single()
            .split(",")
            .map { rawRange -> rawRange.split("-").mapToLong().let { (a, b) -> a..b } }

        val partOne = ranges.sumOf { range -> range.sumOf { value -> value.takeIf { it.isInvalid() } ?: 0L } }
        val partTwo = ranges.sumOf { range -> range.sumOf { value -> value.takeIf { it.isInvalid2() } ?: 0L } }

        return AOCAnswer(partOne, partTwo)
    }

    private fun Long.isInvalid(): Boolean {
        val digits = this.length()

        if (digits % 2 != 0) return false

        val mid = digits / 2
        val decimals = powersOf10[mid]

        val left = (this / decimals)
        val right = (this - (left * decimals))

        return left == right
    }

    private fun Long.isInvalid2(): Boolean {
        val length = this.length()
        val mid = length / 2

        val last = this.last(length = length)

        return (1..mid).any { sliceSize ->
            if (length % sliceSize != 0) return@any false

            val slice = this.take(n = sliceSize, length = length)

            // Fast path out if it does not match
            if (slice.last(length = sliceSize) != last) return@any false

            slice.repeat(n = length / sliceSize, length = sliceSize) == this
        }
    }

    // maxLog10ForLeadingZeros[i] == floor(log10(2^(Long.SIZE - i)))
    private val maxLog10ForLeadingZeros = listOf(
        19, 18, 18, 18, 18, 17, 17, 17, 16, 16, 16, 15, 15, 15, 15, 14, 14, 14, 13, 13, 13, 12, 12, 12,
        12, 11, 11, 11, 10, 10, 10, 9, 9, 9, 9, 8, 8, 8, 7, 7, 7, 6, 6, 6, 6, 5, 5, 5, 4, 4, 4, 3, 3, 3,
        3, 2, 2, 2, 1, 1, 1, 0, 0, 0,
    )

    /**
     * Based on Hacker's Delight Fig. 11-10, the two-table-lookup
     *
     * The key idea is that based on the number of leading zeros (equivalently, `floor(log2(x))`), we
     * can narrow the possible `floor(log10(x))` values to two. For example, if `floor(log2(x))` is 6,
     * then 64 <= x < 128, so `floor(log10(x))` is either 1 or 2.
     */
    private fun Long.log10(): Int {
        val y = maxLog10ForLeadingZeros[this.countLeadingZeroBits()]

        return if (this < powersOf10[y]) y - 1 else y
    }

    private fun Long.length(): Int = log10() + 1

    private fun Long.take(n: Int, length: Int): Long {
        if (n > length) return this

        return this / powersOf10[length - n]
    }
    private fun Long.last(length: Int): Long {
        if (length < 2) return this

        return this - take(length - 1, length) * 10L
    }

    private fun Long.repeat(n: Int, length: Int): Long {
        val multiplier = powersOf10[length]
        var result = this

        repeat(n - 1) {
            // Expand the result with decimal placed needed for another repetition
            result = (result * multiplier + this)
        }

        return result
    }
}
