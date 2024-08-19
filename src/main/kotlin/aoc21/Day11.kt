package aoc21

import Position
import convertInputToMatrix
import get
import getOrNull
import readInput

private typealias Matrix = List<List<Int>>
private const val FLASH_THRESHOLD = 10

class Day11 {

    fun solve() {
        val rawInput = readInput("day11.txt", AOCYear.TwentyOne)
        val matrix = convertInputToMatrix(rawInput) { value -> value.digitToInt() }

        val partOne = (1..100).fold(matrix to 0) { (matrix, score), _ ->
            val (newMatrix, stepScore) = performStep(matrix)

            newMatrix to score + stepScore
        }.second

        val totalPositions = matrix.size * matrix.first().size

        val stepSequence = generateSequence(matrix to 1) { (matrix, stepCount) ->
            val (newMatrix, stepScore) = performStep(matrix)

            when {
                stepScore == totalPositions -> null
                else -> newMatrix to stepCount + 1
            }
        }

        val partTwo = stepSequence.last().second

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }

    private fun performStep(matrix: Matrix): Pair<Matrix, Int> {
        val flashThreshold = FLASH_THRESHOLD - 1
        val initialPositionsToFlash = matrix.findFlashPositions(flashThreshold)

        val changedPositions = buildMap {
            fun nextFlashPositions(position: Position): List<Position> {
                val flashRadius = position.let { (row, col) ->
                    listOf(
                        row + 1 to col,
                        row - 1 to col,
                        row + 1 to col - 1,
                        row + 1 to col + 1,
                        row - 1 to col + 1,
                        row - 1 to col - 1,
                        row to col - 1,
                        row to col + 1,
                    )
                }

                val flashPositions = flashRadius.filter { positionToFlash ->
                    val value = get(positionToFlash) ?: matrix.getOrNull(positionToFlash) ?: return@filter false

                    if (value >= flashThreshold) return@filter false

                    val newValue = value + 1

                    put(positionToFlash, newValue)
                    newValue == flashThreshold
                }

                return flashPositions
            }

            putAll(initialPositionsToFlash.map { it to matrix[it] })

            val flashSequence = generateSequence(initialPositionsToFlash) { positionsToFlash ->
                if (positionsToFlash.isEmpty()) {
                    null
                } else {
                    positionsToFlash.flatMap { nextFlashPositions(it) }
                }
            }

            flashSequence.last()
        }

        val flashCount = changedPositions.values.count { it >= flashThreshold }

        val newMatrix = matrix.mapIndexed { rowI, row ->
            row.mapIndexed { colI, currentValue ->
                val value = changedPositions[rowI to colI] ?: currentValue

                if (value >= flashThreshold) 0 else value + 1
            }
        }

        return newMatrix to flashCount
    }

    private fun Matrix.findFlashPositions(flashThreshold: Int): List<Position> = flatMapIndexed { rowI, row ->
        row.mapIndexedNotNull { colI, value ->
            if (value == flashThreshold) rowI to colI else null
        }
    }
}
