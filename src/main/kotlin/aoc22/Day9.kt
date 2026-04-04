package aoc22

import AOCAnswer
import AOCSolution
import AOCYear
import Position
import applyDirection
import at
import plus
import readInput
import kotlin.math.abs

class Day9 : AOCSolution {

    private val up = -1 at 0
    private val down = 1 at 0
    private val left = 0 at -1
    private val right = 0 at 1

    private val diagonals = listOf(up + left, up + right, down + right, down + left)
    private val directions = listOf(up, down, left, right)

    override fun solve(): AOCAnswer {
        val rawInput = readInput("day9.txt", AOCYear.TwentyTwo)

        val allMovements = rawInput.map { line ->
            val (direction, scalar) = line.split(" ")

            val directionVector = when (direction) {
                "U" -> up
                "D" -> down
                "L" -> left
                "R" -> right
                else -> error("Unknown direction $direction")
            }

            directionVector to scalar.toInt()
        }

        val initialPosition = Position(0, 0)

        fun collectTailMovements(initialPositions: List<Position>) = buildSet {
            allMovements.fold(initialPositions) { positions, (direction, amount) ->
                (1..amount).fold(positions) { currentPositions, _ ->
                    val newHead = currentPositions.first().applyDirection(direction)

                    val newPositions = currentPositions
                        .drop(1)
                        .runningFold(newHead) { head, tail -> getNewTail(head, tail) }

                    add(newPositions.last())

                    newPositions
                }
            }
        }

        val partOne: Set<Position> = collectTailMovements(listOf(initialPosition, initialPosition))
        val partTwo: Set<Position> = collectTailMovements((1..10).map { initialPosition })

        return AOCAnswer(partOne.size, partTwo.size)
    }

    fun getNewTail(head: Position, tail: Position): Position {
        val (xDistance, yDistance) = coordinateDistance(head, tail)
        val manhattanDistance = xDistance + yDistance

        val isDiagonal = xDistance != 0 && yDistance != 0

        return when {
            manhattanDistance > 3 -> {
                val movement = diagonals.minBy { manhattanDistance(head, tail.applyDirection(it)) }

                val newTail = tail.applyDirection(movement)

                newTail
            }
            manhattanDistance == 3 -> {
                val movement = diagonals.first { manhattanDistance(head, tail.applyDirection(it)) == 1 }

                val newTail = tail.applyDirection(movement)

                newTail
            }
            manhattanDistance == 2 && !isDiagonal -> {
                val movement = directions.first { manhattanDistance(head, tail.applyDirection(it)) == 1 }
                val newTail = tail.applyDirection(movement)

                newTail
            }
            else -> tail
        }
    }

    private fun manhattanDistance(a: Position, b: Position) = abs(a.first - b.first) + abs(a.second - b.second)

    private fun coordinateDistance(a: Position, b: Position) = abs(a.first - b.first) to abs(a.second - b.second)
}
