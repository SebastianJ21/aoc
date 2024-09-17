@file:Suppress("MemberVisibilityCanBePrivate")

package aoc19

import AOCYear
import Direction
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

    data class PaintState(
        val executionState: ExecutionState,
        val position: Position,
        val direction: Direction,
        val currentWhiteTiles: PersistentSet<Position>,
        val paintedTiles: PersistentSet<Position>,
    )

    fun solve() {
        val rawInput = readInput("day11.txt", AOCYear.Nineteen)

        val inputInstructions = rawInput.single().split(",").mapToLong()
        val initialExecutionState = ExecutionState.fromList(inputInstructions, listOf())

        fun getFinalPaintState(initialWhiteTiles: PersistentSet<Position>): PaintState {
            val initialState = PaintState(
                executionState = initialExecutionState,
                position = Position(0, 0),
                direction = up,
                currentWhiteTiles = initialWhiteTiles,
                paintedTiles = initialWhiteTiles,
            )

            val paintSequence = generateSequence(initialState) { paintState ->
                val (state, position, direction, currentWhiteTiles, paintedTiles) = paintState

                val input = if (position in currentWhiteTiles) 1L else 0L

                executeTwiceOrNull(state.copy(inputs = listOf(input)))?.let { (newState, outputs) ->
                    val (paintInstruction, turnInstruction) = outputs

                    val (newWhiteTiles, newPaintedTiles) = when (paintInstruction) {
                        0L -> currentWhiteTiles.remove(position) to paintedTiles
                        1L -> currentWhiteTiles.add(position) to paintedTiles.add(position)
                        else -> error("Illegal instruction $paintInstruction")
                    }

                    val newDirection = when (turnInstruction) {
                        0L -> turnLeft(direction)
                        1L -> turnRight(direction)
                        else -> error("Illegal instruction $paintInstruction")
                    }
                    val newPosition = position.applyDirection(newDirection)

                    PaintState(newState, newPosition, newDirection, newWhiteTiles, newPaintedTiles)
                }
            }

            return paintSequence.last()
        }

        val partOnePaintState = getFinalPaintState(persistentHashSetOf())
        val partTwoPaintState = getFinalPaintState(persistentHashSetOf(Position(0, 0)))

        val partTwoWhiteTiles = partTwoPaintState.currentWhiteTiles

        val partOne = partOnePaintState.paintedTiles.size

        val maxCol = partTwoWhiteTiles.maxOf { (_, col) -> col }.inc()
        val maxRow = partTwoWhiteTiles.maxOf { (row) -> row }.inc()

        val finalMatrix = List(maxRow) { rowI ->
            List(maxCol) { colI -> if (partTwoWhiteTiles.contains(rowI to colI)) "#" else " " }
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

    fun turnLeft(currentDirection: Direction) = when (currentDirection) {
        up -> left
        left -> down
        down -> right
        right -> up
        else -> error("Illegal direction $currentDirection")
    }

    fun turnRight(currentDirection: Direction) = when (currentDirection) {
        up -> right
        left -> up
        down -> left
        right -> down
        else -> error("Illegal direction $currentDirection")
    }
}
