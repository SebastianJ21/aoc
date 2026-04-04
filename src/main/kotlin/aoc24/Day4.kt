package aoc24

import AOCAnswer
import AOCSolution
import Direction
import Position
import at
import get
import getInDirectionOrNull
import plus
import positions
import readInput
import toCharMatrix

class Day4 : AOCSolution {

    private val up = -1 at 0
    private val down = 1 at 0
    private val left = 0 at -1
    private val right = 0 at 1

    private val directions = listOf(
        up,
        down,
        left,
        right,
        up + left,
        up + right,
        down + left,
        down + right,
    )

    private val targetPartOne = "XMAS".toList()
    private val targetPartTwo = "MS".toSet()

    private val directionPathsPartOne = directions.map { direction -> targetPartOne.indices.map { direction * it } }
    private val directionPathsPartTwo = listOf(listOf(up + left, down + right), listOf(up + right, down + left))

    override fun solve(): AOCAnswer {
        val rawInput = readInput("day4.txt", AOCYear.TwentyFour)
        val charMatrix = rawInput.toCharMatrix()

        val positions = charMatrix.positions()

        val partOne = positions.sumOf { charMatrix.positionCount(it) }
        val partTwo = positions.count { charMatrix.hasXShapeTarget(it) }

        return AOCAnswer(partOne, partTwo)
    }

    private fun List<List<Char>>.hasXShapeTarget(origin: Position): Boolean {
        if (this[origin] != 'A') return false

        return directionPathsPartTwo.all { directionPath ->
            val pathChars = directionPath.mapNotNull { this.getInDirectionOrNull(origin, it) }

            targetPartTwo == pathChars.toSet()
        }
    }

    private fun List<List<Char>>.positionCount(origin: Position): Int {
        if (this[origin] != targetPartOne.first()) return 0

        return directionPathsPartOne.count { directionPath ->
            val pathChars = directionPath.mapNotNull { this.getInDirectionOrNull(origin, it) }

            pathChars == targetPartOne
        }
    }

    private operator fun Direction.times(scalar: Int): Direction = first * scalar at second * scalar
}
