@file:Suppress("MemberVisibilityCanBePrivate")

package aoc19

import AOCAnswer
import AOCSolution
import AOCYear
import Direction
import Position
import aoc19.IntCodeRunner.Companion.executeInstructions
import applyDirection
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentHashSetOf
import mapToLong
import readInput

class Day11 : AOCSolution {

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

    override fun solve(): AOCAnswer {
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

                val newState = executeInstructions(state.withInputs(input).withClearedOutputs(), 2)

                if (newState.outputs.isEmpty()) return@generateSequence null

                val (paintInstruction, turnInstruction) = newState.outputs

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

        return AOCAnswer(partOne, partTwo)
    }

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
