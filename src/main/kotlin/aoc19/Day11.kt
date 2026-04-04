package aoc19

import AOCAnswer
import AOCSolution
import AOCYear
import Direction
import Position
import aoc19.IntCodeRunner.Companion.executeInstructions
import applyDirection
import at
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentHashSetOf
import mapToLong
import readInput

class Day11 : AOCSolution {

    private val up = -1 at 0
    private val down = 1 at 0
    private val left = 0 at -1
    private val right = 0 at 1

    private data class PaintState(
        val executionState: ExecutionState,
        val position: Position,
        val direction: Direction,
        val whiteTiles: PersistentSet<Position>,
        val paintedTiles: PersistentSet<Position>,
    )

    override fun solve(): AOCAnswer {
        val rawInput = readInput("day11.txt", AOCYear.Nineteen)

        val inputInstructions = rawInput.single().split(",").mapToLong()
        val initialExecutionState = ExecutionState.fromList(inputInstructions, listOf())

        val partOne = makePaintSequence(initialExecutionState, persistentHashSetOf()).last().paintedTiles.size

        val partTwoWhiteTiles = makePaintSequence(initialExecutionState, persistentHashSetOf(Position(0, 0)))
            .last()
            .whiteTiles

        val rowSize = partTwoWhiteTiles.maxOf { (row) -> row } + 1
        val colSize = partTwoWhiteTiles.maxOf { (_, col) -> col } + 1

        val answerMatrix = List(rowSize) { rowI ->
            List(colSize) { colI -> if (partTwoWhiteTiles.contains(rowI at colI)) "#" else " " }
        }

        val partTwo = "\n" + answerMatrix.joinToString("\n") { row -> row.joinToString("") }

        return AOCAnswer(partOne, partTwo)
    }

    private fun makePaintSequence(
        initialExecutionState: ExecutionState,
        initialWhiteTiles: PersistentSet<Position>,
    ): Sequence<PaintState> {
        val initialState = PaintState(
            executionState = initialExecutionState,
            position = Position(0, 0),
            direction = up,
            whiteTiles = initialWhiteTiles,
            paintedTiles = initialWhiteTiles,
        )

        val paintSequence = generateSequence(initialState) { paintState ->
            val (state, position, direction, whiteTiles, paintedTiles) = paintState

            // 0 if the robot is over a black panel or 1 if the robot is over a white panel
            val input = if (position in whiteTiles) 1L else 0L

            val newState = executeInstructions(state.withInputs(input).withClearedOutputs(), stopOnOutputSize = 2)

            if (newState.outputs.isEmpty()) return@generateSequence null
            check(newState.outputs.size == 2) { "Invalid output ${newState.outputs}. Expected 2 values." }

            val (paintInstruction, turnInstruction) = newState.outputs

            // 0 means to paint the position black, and 1 means to paint the position white
            val (newWhiteTiles, newPaintedTiles) = when (paintInstruction) {
                0L -> whiteTiles.remove(position) to paintedTiles
                1L -> whiteTiles.add(position) to paintedTiles.add(position)
                else -> error("Illegal instruction $paintInstruction")
            }

            // 0 means it should turn left 90 degrees, and 1 means it should turn right 90 degrees
            val newDirection = when (turnInstruction) {
                0L -> turnLeft(direction)
                1L -> turnRight(direction)
                else -> error("Illegal instruction $paintInstruction")
            }
            val newPosition = position.applyDirection(newDirection)

            PaintState(newState, newPosition, newDirection, newWhiteTiles, newPaintedTiles)
        }

        return paintSequence
    }

    private fun turnLeft(currentDirection: Direction) = when (currentDirection) {
        up -> left
        left -> down
        down -> right
        right -> up
        else -> error("Illegal direction $currentDirection")
    }

    private fun turnRight(currentDirection: Direction) = when (currentDirection) {
        up -> right
        left -> up
        down -> left
        right -> down
        else -> error("Illegal direction $currentDirection")
    }
}
