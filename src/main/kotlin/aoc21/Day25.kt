package aoc21

import AOCAnswer
import AOCSolution
import AOCYear
import Direction
import Position
import applyDirection
import at
import positionsOf
import readInput
import toCharMatrix

class Day25 : AOCSolution {

    private val east = 0 at 1
    private val south = 1 at 0

    override fun solve(): AOCAnswer {
        val rawInput = readInput("day25.txt", AOCYear.TwentyOne)
        val matrix = rawInput.toCharMatrix()

        val rows = matrix.size
        val cols = matrix.first().size

        val initialEast = matrix.positionsOf { it == '>' }.toSet()
        val initialSouth = matrix.positionsOf { it == 'v' }.toSet()

        val partOne = generateSequence(initialEast to initialSouth) { (eastFacing, southFacing) ->
            val (newEast, changesEast) = performMovement(
                movingPositions = eastFacing,
                blockingPositions = southFacing,
                direction = east,
                maxRow = rows,
                maxCol = cols,
            )

            val (newSouth, changesSouth) = performMovement(
                movingPositions = southFacing,
                blockingPositions = newEast,
                direction = south,
                maxRow = rows,
                maxCol = cols,
            )

            if (changesEast == 0 && changesSouth == 0) return@generateSequence null

            newEast to newSouth
        }.count()

        return AOCAnswer(partOne)
    }

    private fun performMovement(
        movingPositions: Set<Position>,
        blockingPositions: Set<Position>,
        direction: Direction,
        maxRow: Int,
        maxCol: Int,
    ): Pair<Set<Position>, Int> {
        val movedPositions = HashSet<Position>(movingPositions.size)

        val movementCount = movingPositions.count { currentPosition ->
            val possiblePosition = currentPosition.applyDirection(direction)

            val newPosition = when {
                possiblePosition.first == maxRow -> 0 at possiblePosition.second
                possiblePosition.second == maxCol -> possiblePosition.first at 0
                else -> possiblePosition
            }

            if (newPosition in movingPositions || newPosition in blockingPositions) {
                movedPositions.add(currentPosition)
                false
            } else {
                movedPositions.add(newPosition)
                true
            }
        }

        return movedPositions to movementCount
    }
}
