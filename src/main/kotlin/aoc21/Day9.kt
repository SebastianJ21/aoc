package aoc21

import AOCAnswer
import AOCSolution
import AOCYear
import Position
import applyDirection
import at
import get
import getInDirectionOrNull
import getOrNull
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentHashSetOf
import product
import readInput
import toMatrix

class Day9 : AOCSolution {

    private val up = -1 at 0
    private val down = 1 at 0
    private val left = 0 at -1
    private val right = 0 at 1

    private val directions = listOf(up, down, left, right)

    override fun solve(): AOCAnswer {
        val rawInput = readInput("day9.txt", AOCYear.TwentyOne)
        val matrix = rawInput.toMatrix { value -> value.digitToInt() }

        // low points - the locations that are lower than any of its adjacent locations
        val lowPointsPositions = matrix.flatMapIndexed { rowI, row ->
            row.mapIndexedNotNull { colI, current ->
                val currentPosition = rowI at colI

                val isLowPoint = directions.all { direction ->
                    val adjacent = matrix.getInDirectionOrNull(currentPosition, direction)

                    adjacent == null || current < adjacent
                }

                currentPosition.takeIf { isLowPoint }
            }
        }

        val lowPoints = lowPointsPositions.map { matrix[it] }

        val partOne = lowPoints.sumOf { it + 1 }

        val partTwo = lowPointsPositions
            .map { exploreBasin(it, matrix) }
            .sortedDescending()
            .take(3)
            .product()

        return AOCAnswer(partOne, partTwo)
    }

    private fun exploreBasin(position: Position, matrix: List<List<Int>>): Int {
        fun Position.dfsExploreBasin(seen: PersistentSet<Position>): Int {
            val value = matrix[this]

            if (this in seen || value == 9) return 0

            val adjacentPositions = directions.mapNotNull { direction ->
                val adjacent = applyDirection(direction)
                val adjacentValue = matrix.getOrNull(adjacent)

                adjacent.takeIf { adjacentValue != null && adjacentValue >= value }
            }

            val discoveredSize = adjacentPositions.sumOf { it.dfsExploreBasin(seen.add(this)) }

            return discoveredSize + 1
        }

        return position.dfsExploreBasin(persistentHashSetOf())
    }
}
