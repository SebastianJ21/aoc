package aoc23

import AOCYear
import applyDirection
import convertInputToCharMatrix
import get
import getOrNull
import readInput
import kotlin.math.absoluteValue

class Day10 {
    val up = -1 to 0
    val down = 1 to 0
    val left = 0 to -1
    val right = 0 to 1

    val baseDirections = listOf(up, down, left, right)

    val pipeToDirections = mapOf(
        '|' to listOf(up, down),
        '-' to listOf(left, right),
        'L' to listOf(up, right),
        'J' to listOf(up, left),
        '7' to listOf(left, down),
        'F' to listOf(right, down),
    )

    val rawInput = readInput("day10.txt", AOCYear.TwentyThree)
    val matrix = convertInputToCharMatrix(rawInput)

    fun solve() {
        val start: Position = matrix.indices.firstNotNullOf { rowI ->
            matrix[rowI].indices
                .find { colI -> matrix[rowI][colI] == 'S' }
                ?.let { rowI to it }
        }

        val firstAfterStart = baseDirections
            .map { start.applyDirection(it) }
            .first { newPosition ->
                val pipe = matrix.getOrNull(newPosition)
                if (pipe == null || pipe == '.') return@first false

                val directions = pipeToDirections[pipe]!!

                directions.any { newPosition.applyDirection(it) == start }
            }

        val points = generateSequence(start to firstAfterStart) { (previous, current) ->
            when {
                current == start -> null
                else -> current to current.next(previous)
            }
        }.map { it.first }.toList()

        val partOne = points.size / 2

        val area = polygonArea(points)
        // Pick's theorem
        val partTwo = area - (points.size / 2) + 1

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }

    // Shoelace formula
    fun polygonArea(points: List<Position>): Int =
        points
            .zipWithNext()
            .plus(points.last() to points.first())
            .sumOf { (first, second) ->
                val (x0, y0) = first
                val (x1, y1) = second

                (x0 * y1) - (x1 * y0)
            }.div(2).absoluteValue

    fun Position.next(previous: Position): Position {
        val pipe = matrix[this]

        return pipeToDirections
            .getValue(pipe)
            .firstNotNullOf { possibleDirection ->
                this.applyDirection(possibleDirection)
                    .takeIf { it != previous }
            }
    }
}
