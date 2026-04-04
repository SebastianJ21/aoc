package aoc19

import AOCAnswer
import AOCSolution
import AOCYear
import Position
import aoc19.IntCodeRunner.Companion.executeInstructions
import applyDirection
import at
import mapToLong
import readInput
import java.util.PriorityQueue
import kotlin.math.min

class Day15 : AOCSolution {

    private val up = -1 at 0
    private val down = 1 at 0
    private val left = 0 at -1
    private val right = 0 at 1

    private val directions = listOf(up to 1L, down to 2L, left to 3L, right to 4L)

    enum class PositionType { WALL, TILE, OXYGEN }

    override fun solve(): AOCAnswer {
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

        return AOCAnswer(partOne, partTwo)
    }

    private fun spreadOxygen(state: Map<Position, PositionType>) = buildMap {
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
                }
            }
        }
    }

    private fun dijkstra(initialState: ExecutionState): Map<Position, Pair<Int, PositionType>> {
        val visited = hashSetOf<Position>()
        val queue = PriorityQueue<Triple<Int, Position, ExecutionState>>(compareBy { (distance) -> distance })
        val startPosition = 0 at 0

        queue.add(Triple(0, startPosition, initialState))

        val resultMap: Map<Position, Pair<Int, PositionType>> = buildMap {
            put(startPosition, 0 to PositionType.TILE)

            while (queue.isNotEmpty()) {
                val (currentDistance, position, currentState) = queue.remove()

                if (!visited.add(position)) continue

                val tileDataToCheck = directions.map { (direction, input) ->
                    val positionToCheck = position.applyDirection(direction)

                    val resultState = executeInstructions(currentState.withInputs(input).withClearedOutputs(), 1)

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
