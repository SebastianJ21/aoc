package aoc21

import convertInputToMatrix
import readInput
import transposed
import kotlin.math.pow

class Day3 {

    fun solve() {
        val rawInput = readInput("day3.txt", AOCYear.TwentyOne)

        val matrix = convertInputToMatrix(rawInput) { value -> value.digitToInt() }
        val transposedMatrix = matrix.transposed()

        val mostCommonBits = transposedMatrix.map { it.mostCommonBit() }
        val leastCommonBits = transposedMatrix.map { it.leastCommonBit() }
        val partOne = binaryToDecimal(leastCommonBits) * binaryToDecimal(mostCommonBits)

        val oxygenRating = rating(matrix, transposedMatrix) { mostCommonBit() }
        val co2Rating = rating(matrix, transposedMatrix) { leastCommonBit() }
        val partTwo = binaryToDecimal(oxygenRating) * binaryToDecimal(co2Rating)

        println("Part One: $partOne")
        println("Part One: $partTwo")
    }

    private fun rating(
        matrix: List<List<Int>>,
        transposedMatrix: List<List<Int>>,
        bitSelector: List<Int>.() -> Int,
    ): List<Int> {
        val sequence =
            generateSequence(Triple(matrix, transposedMatrix, 0)) { (currentMatrix, currentTransposed, index) ->
                if (currentMatrix.size <= 1) return@generateSequence null

                val bitToKeep = currentTransposed[index].bitSelector()
                val newMatrix = currentMatrix.filter { bits -> bits[index] == bitToKeep }

                Triple(newMatrix, newMatrix.transposed(), index + 1)
            }

        val lastRating = sequence.last()

        return lastRating.first.single()
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
