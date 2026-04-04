package aoc22

import AOCAnswer
import AOCSolution
import Position
import applyDirection
import at
import get
import getOrNull
import positionsOf
import readInput
import toMatrix
import java.util.PriorityQueue
import kotlin.math.min

class Day12 : AOCSolution {

    private val up = -1 at 0
    private val down = 1 at 0
    private val left = 0 at -1
    private val right = 0 at 1

    private val directions = listOf(up, down, left, right)

    override fun solve(): AOCAnswer {
        val rawInput = readInput("day12.txt")

        val startPosition = rawInput.positionsOf { it == 'S' }.single()
        val endPosition = rawInput.positionsOf { it == 'E' }.single()

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

        val lowestPositions = rawInput.positionsOf { it == 'a' }

        val partTwo = lowestPositions.minOf { shortestPaths[it] ?: Int.MAX_VALUE }

        return AOCAnswer(partOne, partTwo)
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
