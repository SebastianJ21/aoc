@file:Suppress("MemberVisibilityCanBePrivate")

package aoc19

import AOCAnswer
import AOCSolution
import AOCYear
import Position
import aoc19.IntCodeRunner.Companion.executeInstructions
import applyDirection
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentList
import mapToLong
import plus
import readInput

class Day13 : AOCSolution {

    val up = -1 to 0
    val down = 1 to 0
    val left = 0 to -1
    val right = 0 to 1

    enum class Tile {
        WALL,
        BALL,
        PADDLE,
        BLOCK,
        EMPTY,
    }

    fun Int.toTile(): Tile = when (this) {
        0 -> Tile.EMPTY
        1 -> Tile.WALL
        2 -> Tile.BLOCK
        3 -> Tile.PADDLE
        4 -> Tile.BALL
        else -> error("Invalid tileId $this")
    }

    data class GameState(
        val executionState: ExecutionState,
        val boardState: PersistentMap<Position, Tile>,
        val score: Int,
        val ballPosition: Position,
        val paddlePosition: Position,
    )

    override fun solve(): AOCAnswer {
        val rawInput = readInput("day13.txt", AOCYear.Nineteen)

        val instructions = rawInput.single().split(",").mapToLong()

        val boardCreationSequence = generateSequence(ExecutionState.fromList(instructions)) { state ->
            executeInstructions(state.withClearedOutputs(), 3)
        }.drop(1).takeWhile { it.outputs.isNotEmpty() }

        val initialGameBoard = boardCreationSequence.fold(persistentMapOf<Position, Tile>()) { board, state ->
            val (updatedBoard) = updateBoard(board, 0, state)

            updatedBoard
        }

        val partOne = initialGameBoard.count { (_, tile) -> tile == Tile.BLOCK }

        val gameInstructions = instructions.toPersistentList().set(0, 2)

        val startGameExecutionState = generateSequence(ExecutionState.fromList(gameInstructions)) { state ->
            executeInstructions(state.withClearedOutputs(), 3)
        }.drop(1).drop(boardCreationSequence.count()).first()

        val initialGameState = GameState(
            executionState = startGameExecutionState,
            boardState = initialGameBoard,
            score = 0,
            ballPosition = initialGameBoard.entries.first { (_, tile) -> tile == Tile.BALL }.key,
            paddlePosition = initialGameBoard.entries.first { (_, tile) -> tile == Tile.PADDLE }.key,
        )

        val gameSequence = generateSequence(initialGameState) { (state, board, score, ball, paddle) ->
            val maintainDistanceInput = state.inputs.firstOrNull() ?: getPaddleDirection(paddle.second, ball.second)

            val newState = executeInstructions(state.withInputs(maintainDistanceInput).withClearedOutputs(), 6)
                .let { if (it.hasScoreUpdate()) executeInstructions(it, 9) else it }

            if (newState.outputs.isEmpty()) return@generateSequence null

            val (newBoard, newScore) = updateBoard(board, score, newState)

            val newBallPosition = getNewBallPosition(ball, newBoard)
            val newPaddle = getNewPaddlePosition(paddle, newBoard)

            val resultState = if (newBoard[ball] != Tile.BALL) {
                val ballYMovement = newBallPosition.second - ball.second
                val nextBallY = newBallPosition.second + ballYMovement

                val willHitPaddle = paddle.first - newBallPosition.first == 1

                // Ball will hit the paddle -> Don't move the paddle in prediction of the ball movement
                val input = if (willHitPaddle && newBallPosition.second == paddle.second) {
                    0L
                } else {
                    getPaddleDirection(paddle.second, nextBallY)
                }

                newState.withInputs(input)
            } else {
                newState
            }

            GameState(resultState, newBoard, newScore, newBallPosition, newPaddle)
        }

        val partTwo = gameSequence.last().score

        return AOCAnswer(partOne, partTwo)
    }

    val possibleBallDirections = listOf(up + left, up + right, down + left, down + right, 0 to 0)

    fun getNewBallPosition(oldPosition: Position, boardState: PersistentMap<Position, Tile>): Position =
        possibleBallDirections.firstNotNullOf { direction ->
            oldPosition.applyDirection(direction).takeIf { boardState[it] == Tile.BALL }
        }

    val possiblePaddleDirections = listOf(left, right, 0 to 0)

    fun getNewPaddlePosition(oldPosition: Position, boardState: PersistentMap<Position, Tile>) =
        possiblePaddleDirections.firstNotNullOf { direction ->
            oldPosition.applyDirection(direction).takeIf { boardState[it] == Tile.PADDLE }
        }

    fun ExecutionState.hasScoreUpdate() =
        if (outputs.isEmpty()) false else outputs.let { (x, y) -> x == -1L && y == 0L }

    fun updateBoard(
        boardState: PersistentMap<Position, Tile>,
        score: Int,
        gameState: ExecutionState,
    ): Pair<PersistentMap<Position, Tile>, Int> = gameState.outputs
        .map { it.toInt() }
        .chunked(3)
        .fold(boardState to score) { (state, score), (x, y, id) ->
            if (x == -1 && y == 0) {
                state to id
            } else {
                state.put(Position(y, x), id.toTile()) to score
            }
        }

    fun getPaddleDirection(paddle: Int, ball: Int): Long = when {
        paddle > ball -> -1L
        paddle < ball -> 1L
        else -> 0L
    }
}
