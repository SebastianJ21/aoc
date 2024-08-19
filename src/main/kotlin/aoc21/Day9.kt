package aoc21

import AOCYear
import Position
import applyDirection
import convertInputToArrayMatrix
import get
import getOrNull
import product
import readInput

class Day9 {
    val up = -1 to 0
    val down = 1 to 0
    val left = 0 to -1
    val right = 0 to 1

    val directions = listOf(up, down, left, right)

    fun solve() {
        val rawInput = readInput("day9.txt", AOCYear.TwentyOne)
        val matrix = convertInputToArrayMatrix(rawInput) { digitToInt() }

        // low points - the locations that are lower than any of its adjacent locations
        val lowPoints = matrix.flatMapIndexed { rowI, row ->
            row.filterIndexed { colI, current ->
                val currentPosition = rowI to colI

                val adjacentValues = directions.mapNotNull {
                    matrix.getOrNull(currentPosition.applyDirection(it))
                }

                adjacentValues.all { current < it }
            }
        }

        val partOne = lowPoints.sumOf { it + 1 }

        val lowPointsPositions = matrix.flatMapIndexed { rowI, row ->
            row.mapIndexedNotNull { colI, current ->
                val currentPosition = rowI to colI

                val adjacentValues = directions.mapNotNull {
                    matrix.getOrNull(currentPosition.applyDirection(it))
                }

                currentPosition.takeIf { adjacentValues.all { current < it } }
            }
        }

        val partTwo = lowPointsPositions
            .map { exploreBasin(it, matrix) }
            .sortedDescending()
            .take(3)
            .product()

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }

    private fun exploreBasin(position: Pair<Int, Int>, matrix: Array<Array<Int>>): Int {
        val seen = mutableSetOf<Position>()

        fun Position.dfsExploreBasin(): Int {
            val value = matrix[this]

            if (!seen.add(this) || value == 9) return 0

            val adjacentPositions = directions.map { applyDirection(it) }
                .filter { position ->
                    matrix.getOrNull(position)?.let { it >= value } ?: false
                }

            val discoveredSize = adjacentPositions.sumOf { it.dfsExploreBasin() }

            return discoveredSize + 1
        }

        return position.dfsExploreBasin()
    }
}
