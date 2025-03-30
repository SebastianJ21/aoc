package aoc21

import readInput
import toMatrix
import transposed
import kotlin.math.pow

class Day3 {

    fun solve() {
        val rawInput = readInput("day3.txt", AOCYear.TwentyOne)

        val matrix = rawInput.toMatrix { value -> value.digitToInt() }

        val (mostCommonBits, leastCommonBits) = matrix.transposed().run {
            map { it.mostCommonBit() } to map { it.leastCommonBit() }
        }

        val partOne = binaryToDecimal(leastCommonBits) * binaryToDecimal(mostCommonBits)

        val oxygenRating = rating(matrix) { mostCommonBit() }
        val co2Rating = rating(matrix) { leastCommonBit() }

        val partTwo = binaryToDecimal(oxygenRating) * binaryToDecimal(co2Rating)

        println("Part One: $partOne")
        println("Part One: $partTwo")
    }

    private fun rating(matrix: List<List<Int>>, bitSelector: List<Int>.() -> Int): List<Int> {
        val sequence = generateSequence(matrix to 0) { (currentMatrix, index) ->
            if (currentMatrix.size <= 1) return@generateSequence null

            val bitToKeep = currentMatrix.transposed()[index].bitSelector()
            val newMatrix = currentMatrix.filter { bits -> bits[index] == bitToKeep }

            newMatrix to index + 1
        }

        val (lastRatingMatrix) = sequence.last()

        return lastRatingMatrix.single()
    }

    private fun List<Int>.mostCommonBit(): Int {
        val oneBitCount = count { it == 1 }

        return if (oneBitCount >= size / 2.0) 1 else 0
    }

    private fun List<Int>.leastCommonBit(): Int {
        val zeroBitCount = count { it == 0 }

        return if (zeroBitCount <= size / 2.0) 0 else 1
    }

    private fun binaryToDecimal(bits: List<Int>): Int = bits
        .reversed()
        .mapIndexed { index, bit -> 2.0.pow(index).toInt() * bit }
        .sum()
}
