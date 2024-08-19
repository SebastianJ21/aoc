package aoc21

import AOCYear
import readInput
import kotlin.math.min

class Day21 {
    data class GameStatus(
        val player1Score: Int,
        val player1Position: Int,

        val player2Score: Int,
        val player2Position: Int,
        val wasPlayer1Turn: Boolean,

        val lastDiceNumber: Int,
        val throwNumber: Int,
    )

    fun solve() {
        val rawInput = readInput("day21.txt", AOCYear.TwentyOne)
        val player1Start = rawInput.first().last().digitToInt()
        val player2Start = rawInput.last().last().digitToInt()

        val initialGameStatus = GameStatus(0, player1Start, 0, player2Start, false, 0, 1)

        val gameHistory = initialGameStatus.deterministicGameHistory().toList()

        val totalDiceRolls = (gameHistory.size - 1) * 3
        val losingPlayerScore = gameHistory.last().run { min(player1Score, player2Score) }

        val partOne = totalDiceRolls * losingPlayerScore
        val partTwo = initialGameStatus.diracFinalScore().toList().max()

        println("Part One: $partOne")
        println("Part Two: $partTwo")
    }

    private fun nextPosition(position: Int, diceThrow: Int) = ((position + diceThrow - 1) % 10) + 1

    private fun getNextDiceNumbers(lastDiceNumber: Int) = (1..3).map {
        val number = lastDiceNumber + it
        if (number <= 100) number else number - 100
    }

    private fun GameStatus.deterministicGameHistory(): Sequence<GameStatus> = generateSequence(this) { gameStatus ->
        val player1Score = gameStatus.player1Score
        val player2Score = gameStatus.player2Score
        if (player1Score >= 1000 || player2Score >= 1000) {
            return@generateSequence null
        }

        val throws = getNextDiceNumbers(gameStatus.lastDiceNumber)
        val isPlayer2Turn = gameStatus.wasPlayer1Turn

        val (newPlayer1Score, newPlayer1Position) = if (isPlayer2Turn) {
            player1Score to gameStatus.player1Position
        } else {
            val newPosition = nextPosition(gameStatus.player1Position, throws.sum())
            player1Score + newPosition to newPosition
        }

        val (newPlayer2Score, newPlayer2Position) = if (!isPlayer2Turn) {
            player2Score to gameStatus.player2Position
        } else {
            val newPosition = nextPosition(gameStatus.player2Position, throws.sum())
            player2Score + newPosition to newPosition
        }

        gameStatus.copy(
            player1Score = newPlayer1Score,
            player1Position = newPlayer1Position,
            player2Score = newPlayer2Score,
            player2Position = newPlayer2Position,
            wasPlayer1Turn = !gameStatus.wasPlayer1Turn,
            lastDiceNumber = throws.last(),
        )
    }

    private fun GameStatus.diracFinalScore(): Pair<Long, Long> {
        val cache = mutableMapOf<GameStatus, Pair<Long, Long>>()

        fun GameStatus.playQuantumGame(): Pair<Long, Long> {
            cache[this]?.let { return it }

            if (throwNumber == 1 && isGameOver()) {
                val points = points()
                cache[this] = points
                return points
            }

            val isLastThrow = throwNumber == 3
            val nextThrow = if (isLastThrow) 1 else throwNumber + 1

            fun makeNextGame(throwNumber: Int): GameStatus {
                val position = if (wasPlayer1Turn) player2Position else player1Position

                return nextPosition(position, throwNumber).let { nextPosition ->
                    if (wasPlayer1Turn) {
                        copy(
                            player2Score = if (isLastThrow) player2Score + nextPosition else player2Score,
                            lastDiceNumber = throwNumber,
                            wasPlayer1Turn = !isLastThrow,
                            player2Position = nextPosition,
                            throwNumber = nextThrow,
                        )
                    } else {
                        copy(
                            player1Score = if (isLastThrow) player1Score + nextPosition else player1Score,
                            lastDiceNumber = throwNumber,
                            wasPlayer1Turn = isLastThrow,
                            player1Position = nextPosition,
                            throwNumber = nextThrow,
                        )
                    }
                }
            }

            val result = (1..3).map { makeNextGame(it).playQuantumGame() }.reduce { acc, it -> acc + it }

            cache[this] = result

            return result
        }

        return playQuantumGame()
    }

    operator fun Pair<Long, Long>.plus(other: Pair<Long, Long>) = (first + other.first) to (second + other.second)

    private fun GameStatus.isGameOver() = player1Score >= 21 || player2Score >= 21

    private fun GameStatus.points() = when {
        player1Score >= 21 -> 1L to 0L
        player2Score >= 21 -> 0L to 1L
        else -> error("Neither player has won")
    }
}
