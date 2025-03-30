@file:Suppress("MemberVisibilityCanBePrivate")

package aoc22

import Position
import applyDirection
import get
import getOrNull
import readInput
import toMatrix
import java.util.PriorityQueue
import kotlin.math.min

typealias CoordinatesL = Pair<Long, Long>

class Day12 {

    val up = -1 to 0
    val down = 1 to 0
    val left = 0 to -1
    val right = 0 to 1

    val directions = listOf(up, down, left, right)

    fun List<String>.matchingPositions(value: Char): List<Position> = flatMapIndexed { rowI, row ->
        row.mapIndexedNotNull { colI, char -> if (char == value) rowI to colI else null }
    }

    fun solve() {
        val rawInput = readInput("day12.txt")

        val startPosition = rawInput.matchingPositions('S').single()
        val endPosition = rawInput.matchingPositions('E').single()

        val inputMatrix = rawInput.toMatrix { value ->
            // Convert to height
            when {
                value.isLowerCase() -> value.code - 'a'.code
                value == 'S' -> 0
                value == 'E' -> 'z'.code - 'a'.code
                else -> error("Unknown value $this")
            }
        }

        val shortestPaths = dijkstra(inputMatrix, endPosition) { current, new -> current <= new + 1 }

        val partOne = shortestPaths.getValue(startPosition)

        val lowestPositions = rawInput.matchingPositions('a')

        val partTwo = lowestPositions.minOf { shortestPaths[it] ?: Int.MAX_VALUE }

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }

    private fun dijkstra(
        matrix: List<List<Int>>,
        startPosition: Position,
        elevationCheck: (current: Int, new: Int) -> Boolean,
    ): Map<Position, Int> {
        val visited = mutableSetOf<Position>()

        val queue = PriorityQueue<Pair<Int, Position>>(compareBy { it.first })
        queue.add(0 to startPosition)

        val resultMap: Map<Position, Int> = buildMap {
            put(startPosition, 0)

            while (queue.isNotEmpty()) {
                val (currentDistance, position) = queue.remove()

                if (!visited.add(position)) continue

                val currentHeight = matrix[position]

                val positionsToCheck = directions.mapNotNull { direction ->
                    val positionToCheck = position.applyDirection(direction)

                    matrix.getOrNull(positionToCheck)?.let { positionToCheck to it }
                }.filter { (positionToCheck, height) ->
                    positionToCheck !in visited && elevationCheck(currentHeight, height)
                }

                positionsToCheck.forEach { (positionToCheck, _) ->
                    val minDistance = min(this[positionToCheck] ?: Int.MAX_VALUE, currentDistance + 1)

                    this[positionToCheck] = minDistance

                    queue.add(minDistance to positionToCheck)
                }
            }
        }

        return resultMap
    }
}
