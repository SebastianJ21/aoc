@file:Suppress("MemberVisibilityCanBePrivate")

package aoc19

import AOCYear
import Position
import applyDirection
import mapToLong
import printAOCAnswers
import readInput
import java.util.PriorityQueue
import kotlin.math.min

class Day15 {

    val up = -1 to 0
    val down = 1 to 0
    val left = 0 to -1
    val right = 0 to 1

    val directions = listOf(up to 1L, down to 2L, left to 3L, right to 4L)

    enum class PositionType { WALL, TILE, OXYGEN }

    fun solve() {
        val rawInput = readInput("day15.txt", AOCYear.Nineteen)

        val inputInstructions = rawInput.single().split(",").mapToLong()
        val initialExecutionState = ExecutionState.fromList(inputInstructions, listOf())

        val shortestPaths = dijkstra(initialExecutionState)

        val (partOne) = shortestPaths.values.single { (_, type) -> type == PositionType.OXYGEN }

        val positionToTileType = shortestPaths.mapValues { (_, value) -> value.second }

        val partTwo = generateSequence(positionToTileType) { positions -> spreadOxygen(positions) }
            .zipWithNext()
            .takeWhile { (state, nextState) -> state != nextState }
            .count()

        printAOCAnswers(partOne, partTwo)
    }

    fun spreadOxygen(state: Map<Position, PositionType>) = buildMap {
        state.forEach { (position, type) ->
            when (type) {
                PositionType.OXYGEN -> {
                    put(position, PositionType.OXYGEN)

                    directions.forEach { (direction) ->
                        val adjacentPosition = position.applyDirection(direction)

                        if (state[adjacentPosition] == PositionType.TILE) {
                            put(adjacentPosition, PositionType.OXYGEN)
                        }
                    }
                }
                PositionType.TILE -> {
                    if (position !in this) {
                        put(position, PositionType.TILE)
                    }
                }
                PositionType.WALL -> {
                    put(position, PositionType.WALL)
                    // Type inference bug...
                    return@forEach
                }
            }
        }
    }

    private fun dijkstra(initialState: ExecutionState): Map<Position, Pair<Int, PositionType>> {
        val visited = hashSetOf<Position>()
        val queue = PriorityQueue<Triple<Int, Position, ExecutionState>>(compareBy { (distance) -> distance })
        val startPosition = 0 to 0

        queue.add(Triple(0, startPosition, initialState))

        val resultMap: Map<Position, Pair<Int, PositionType>> = buildMap {
            put(startPosition, 0 to PositionType.TILE)

            while (queue.isNotEmpty()) {
                val (currentDistance, position, currentState) = queue.remove()

                if (!visited.add(position)) continue

                val tileDataToCheck = directions.map { (direction, input) ->
                    val positionToCheck = position.applyDirection(direction)

                    val resultState = IntCodeRunner.executeInstructions(
                        currentState.copy(outputs = listOf(), inputs = listOf(input)),
                        1,
                    )

                    val tileType = when (resultState.outputs.single()) {
                        0L -> PositionType.WALL
                        1L -> PositionType.TILE
                        2L -> PositionType.OXYGEN
                        else -> error("Illegal IntCode output: ${resultState.outputs.single()}")
                    }

                    Triple(positionToCheck, tileType, resultState)
                }

                tileDataToCheck.forEach { (positionToCheck, tileType, state) ->
                    val currentBest = this[positionToCheck]?.first ?: Int.MAX_VALUE

                    val minDistance = min(currentBest, currentDistance + 1)

                    this[positionToCheck] = minDistance to tileType

                    if (tileType != PositionType.WALL) {
                        queue.add(Triple(minDistance, positionToCheck, state))
                    }
                }
            }
        }

        return resultMap
    }
}
