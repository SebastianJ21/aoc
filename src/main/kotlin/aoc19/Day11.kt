@file:Suppress("MemberVisibilityCanBePrivate")

package aoc19

import AOCYear
import Position
import applyDirection
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentHashSetOf
import mapToLong
import printAOCAnswers
import readInput

class Day11 {

    val up = -1 to 0
    val down = 1 to 0
    val left = 0 to -1
    val right = 0 to 1

    fun solve() {
        val rawInput = readInput("day11.txt", AOCYear.Nineteen)

        val inputInstructions = rawInput.single().split(",").mapToLong()
        val initialRobotState = Triple(ExecutionState.fromList(inputInstructions, listOf()), Position(0, 0), up)

        fun getFinalTiles(
            initialWhiteTiles: PersistentSet<Position>,
        ): Pair<PersistentSet<Position>, PersistentSet<Position>> {
            val initialTileState = initialWhiteTiles to initialWhiteTiles

            val paintSequence = generateSequence(initialRobotState to initialTileState) { (robotState, tilesState) ->
                val (state, position, direction) = robotState

                val (currentWhiteTiles, paintedTiles) = tilesState

                val input = if (position in currentWhiteTiles) 1L else 0L

                executeTwiceOrNull(state.copy(inputs = listOf(input)))?.let { (newState, outputs) ->
                    val (paintInstruction, turnInstruction) = outputs

                    val newTiles = when (paintInstruction) {
                        0L -> currentWhiteTiles.remove(position) to paintedTiles
                        1L -> {
                            currentWhiteTiles.add(position) to paintedTiles.add(position)
                        }
                        else -> error("Illegal instruction $paintInstruction")
                    }

                    val newDirection = when (turnInstruction) {
                        0L -> turnLeft(direction)
                        1L -> turnRight(direction)
                        else -> error("Illegal instruction $paintInstruction")
                    }
                    val newPosition = position.applyDirection(newDirection)

                    Triple(newState, newPosition, newDirection) to newTiles
                }
            }

            val (_, finalTileState) = paintSequence.last()

            return finalTileState
        }

        val (_, paintedTiles) = getFinalTiles(persistentHashSetOf())
        val (finalWhiteTiles) = getFinalTiles(persistentHashSetOf(Position(0, 0)))

        val partOne = paintedTiles.size

        val maxCol = finalWhiteTiles.maxOf { (_, col) -> col }.inc()
        val maxRow = finalWhiteTiles.maxOf { (row) -> row }.inc()

        val finalMatrix = List(maxRow) { rowI ->
            List(maxCol) { colI -> if (finalWhiteTiles.contains(rowI to colI)) "#" else " " }
        }

        val partTwo = "\n" + finalMatrix.joinToString("\n") { row ->
            row.joinToString("") { it }
        }

        printAOCAnswers(partOne, partTwo)
    }

    fun executeTwiceOrNull(state: ExecutionState): Pair<ExecutionState, Pair<Long, Long>>? {
        val first = IntCodeRunner.executeInstructions(state, true)
        val firstOutput = first.outputs.singleOrNull()

        val second = IntCodeRunner.executeInstructions(first.withoutOutputs(), true)
        val secondOutput = second.outputs.singleOrNull()

        // The program finished
        if (firstOutput == null || secondOutput == null) return null

        return second.withoutOutputs() to Pair(firstOutput, secondOutput)
    }

    private fun ExecutionState.withoutOutputs() = copy(outputs = emptyList())

    fun turnLeft(currentDirection: Pair<Int, Int>) = when (currentDirection) {
        up -> left
        left -> down
        down -> right
        right -> up
        else -> error("Illegal direction $currentDirection")
    }

    fun turnRight(currentDirection: Pair<Int, Int>) = when (currentDirection) {
        up -> right
        left -> up
        down -> left
        right -> down
        else -> error("Illegal direction $currentDirection")
    }
}
