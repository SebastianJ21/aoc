package aoc21

import AOCYear
import Position
import alsoPrintLn
import applyDirection
import readInput
import toCharMatrix

class Day25 {

    fun solve() {
        val rawInput = readInput("day25.txt", AOCYear.TwentyOne)
        val matrix = rawInput.toCharMatrix()

        val rows = matrix.size
        val cols = matrix.first().size

        fun getInitialPositions(): Pair<Set<Position>, Set<Position>> {
            val east = mutableSetOf<Position>()
            val south = mutableSetOf<Position>()

            matrix.forEachIndexed { rowI, row ->
                row.forEachIndexed { colI, char ->
                    when (char) {
                        '>' -> east.add(rowI to colI)
                        'v' -> south.add(rowI to colI)
                    }
                }
            }

            return east to south
        }
        val (initialEast, initialSouth) = getInitialPositions()

        generateSequence(initialEast to initialSouth) { (eastFacing, southFacing) ->
            val (newEast, changesEast) = performMovement(eastFacing, southFacing, 0 to 1, rows, cols)

            val (newSouth, changesSouth) = performMovement(southFacing, newEast, 1 to 0, rows, cols)

            if (changesEast == 0 && changesSouth == 0) null else newEast to newSouth
        }.count().alsoPrintLn { }
    }

    fun performMovement(
        movingPositions: Set<Position>,
        blockingPositions: Set<Position>,
        direction: Pair<Int, Int>,
        maxRow: Int,
        maxCol: Int,
    ): Pair<Set<Position>, Int> {
        val movedPositions = mutableSetOf<Position>()

        val movementCount = movingPositions.count { currentPosition ->
            val possiblePosition = currentPosition.applyDirection(direction)

            val newPosition = when {
                possiblePosition.first == maxRow -> 0 to possiblePosition.second
                possiblePosition.second == maxCol -> possiblePosition.first to 0
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
