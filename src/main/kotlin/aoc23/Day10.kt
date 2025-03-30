package aoc23

import AOCAnswer
import AOCSolution
import AOCYear
import Position
import applyDirection
import get
import getOrNull
import positionsSequence
import readInput
import toCharMatrix
import kotlin.math.absoluteValue

class Day10 : AOCSolution {

    val up = -1 to 0
    val down = 1 to 0
    val left = 0 to -1
    val right = 0 to 1

    val directions = listOf(up, down, left, right)

    private val pipeToDirections = mapOf(
        '|' to listOf(up, down),
        '-' to listOf(left, right),
        'L' to listOf(up, right),
        'J' to listOf(up, left),
        '7' to listOf(left, down),
        'F' to listOf(right, down),
    )

    val rawInput = readInput("day10.txt", AOCYear.TwentyThree)
    val matrix = rawInput.toCharMatrix()

    override fun solve(): AOCAnswer {
        val start = matrix.positionsSequence().first { matrix[it] == 'S' }

        val nextAfterStart = directions
            .map { start.applyDirection(it) }
            .first { newPosition ->
                val pipe = matrix.getOrNull(newPosition)
                val pipeDirections = pipeToDirections[pipe] ?: return@first false

                pipeDirections.any { newPosition.applyDirection(it) == start }
            }

        val points = generateSequence(start to nextAfterStart) { (current, next) ->
            if (next == start) return@generateSequence null

            next to next.next(current)
        }.map { (current) -> current }.toList()

        val partOne = points.size / 2

        val area = polygonArea(points)
        // Pick's theorem
        val partTwo = area - (points.size / 2) + 1

        return AOCAnswer(partOne, partTwo)
    }

    // Shoelace formula
    private fun polygonArea(points: List<Position>): Int = points
        .zipWithNext()
        .plus(points.last() to points.first())
        .sumOf { (first, second) ->
            val (x0, y0) = first
            val (x1, y1) = second

            (x0 * y1) - (x1 * y0)
        }
        .div(2)
        .absoluteValue

    fun Position.next(previous: Position): Position {
        val pipe = matrix[this]
        val pipeDirections = pipeToDirections.getValue(pipe)

        return pipeDirections.firstNotNullOf { possibleDirection ->
            this.applyDirection(possibleDirection).takeIf { it != previous }
        }
    }
}
