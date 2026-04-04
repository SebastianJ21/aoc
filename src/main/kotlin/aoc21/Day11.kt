package aoc21

import AOCAnswer
import AOCSolution
import Position
import applyDirection
import at
import plus
import positionsOf
import readInput
import toMatrix

private const val FLASH_THRESHOLD = 9

class Day11 : AOCSolution {

    private val up = -1 at 0
    private val down = 1 at 0
    private val left = 0 at -1
    private val right = 0 at 1

    private val neighborhood = listOf(up + left, up, up + right, right, down + right, down, down + left, left)

    override fun solve(): AOCAnswer {
        val rawInput = readInput("day11.txt", AOCYear.TwentyOne)
        val matrix = rawInput.toMatrix { value -> value.digitToInt() }

        val matrixSequence = generateSequence(matrix) { matrix -> performStep(matrix) }

        val partOne = matrixSequence
            .drop(1)
            .take(100)
            .sumOf { matrix -> matrix.sumOf { row -> row.count { it == 0 } } }

        val totalPositions = matrix.size * matrix.first().size

        val partTwo = matrixSequence.indexOfFirst { matrix ->
            matrix.sumOf { row -> row.count { it == 0 } } == totalPositions
        }

        return AOCAnswer(partOne, partTwo)
    }

    private fun performStep(matrix: List<List<Int>>): List<List<Int>> {
        val flashed = hashSetOf<Position>()

        val matrixSequence = generateSequence(matrix.incAll()) { current ->
            val flashPositions = current.positionsOf { it > FLASH_THRESHOLD }.filter { flashed.add(it) }

            val positionToIncrement = flashPositions
                .flatMap { flashPosition -> neighborhood.map { flashPosition.applyDirection(it) } }
                .groupingBy { it }
                .eachCount()

            if (positionToIncrement.isEmpty()) return@generateSequence null

            val next = current.mapIndexed { rowI, row ->
                row.mapIndexed { colI, value -> value + (positionToIncrement[rowI at colI] ?: 0) }
            }

            next
        }

        val finalMatrix = matrixSequence.last()

        return finalMatrix.map { row -> row.map { if (it > FLASH_THRESHOLD) 0 else it } }
    }

    private fun List<List<Int>>.incAll() = this.map { row -> row.map { it + 1 } }
}
