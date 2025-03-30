package aoc24

import AOCAnswer
import AOCSolution
import Direction
import Position
import applyDirection
import get
import getOrNull
import plus
import positionsSequence
import readInput

class Day4 : AOCSolution {

    val up: Direction = -1 to 0
    val down: Direction = 1 to 0
    val left: Direction = 0 to -1
    val right: Direction = 0 to 1

    val directions = listOf(
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
        val charMatrix = rawInput.map { line -> line.toList() }

        val positionSequence = charMatrix.positionsSequence()

        val partOne = positionSequence.sumOf { charMatrix.positionCount(it) }
        val partTwo = positionSequence.count { charMatrix.hasXShapeTarget(it) }

        return AOCAnswer(partOne, partTwo)
    }

    private fun List<List<Char>>.hasXShapeTarget(origin: Position): Boolean {
        if (this[origin] != 'A') return false

        return directionPathsPartTwo.all { directionPath ->
            val pathChars = directionPath.mapNotNull { this.getOrNull(origin.applyDirection(it)) }

            targetPartTwo == pathChars.toSet()
        }
    }

    private fun List<List<Char>>.positionCount(origin: Position): Int {
        if (this[origin] != targetPartOne.first()) return 0

        return directionPathsPartOne.count { directionPath ->
            val pathChars = directionPath.mapNotNull {
                val position = origin.applyDirection(it)

                this.getOrNull(position)
            }

            pathChars == targetPartOne
        }
    }

    private operator fun Direction.times(scalar: Int): Direction = first * scalar to second * scalar
}
