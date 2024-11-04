package aoc19

import AOCAnswer
import AOCSolution
import AOCYear
import Position
import applyDirection
import readInput
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class Day3 : AOCSolution {

    val up = -1 to 0
    val down = 1 to 0
    val left = 0 to -1
    val right = 0 to 1

    data class LineRange(val x: IntProgression, val y: IntProgression, val startSteps: Int)

    fun Position.scaleBy(value: Int) = first * value to second * value

    fun getDirectionVector(direction: Char) = when (direction) {
        'R' -> right
        'L' -> left
        'D' -> down
        'U' -> up
        else -> error("Illegal direction $direction")
    }

    fun IntProgression.intersects(other: IntProgression): Boolean {
        val (low, high) = min(first, last) to max(first, last)
        val (otherLow, otherHigh) = min(other.first, other.last) to max(other.first, other.last)

        return !(high < otherLow || low > otherHigh)
    }

    fun intersects(a: LineRange, b: LineRange) = a.x.intersects(b.x) && a.y.intersects(b.y)

    override fun solve(): AOCAnswer {
        val rawInput = readInput("day3.txt", AOCYear.Nineteen)

        val linePositions = rawInput.map { line ->
            val rawCommands = line.split(",")

            rawCommands.runningFold(Position(0, 0) to 0) { (position, steps), rawCommand ->
                val direction = rawCommand.first()
                val size = rawCommand.drop(1).toInt()

                val movement = getDirectionVector(direction).scaleBy(size)

                val newPosition = position.applyDirection(movement)

                newPosition to steps + size
            }
        }

        fun pickRange(from: Int, to: Int) = if (from <= to) from..to else from downTo to

        val lineRanges = linePositions.map { positions ->
            positions.zipWithNext { from, to ->
                val (x0, y0) = from.first
                val (x1, y1) = to.first

                val startSteps = from.second

                LineRange(pickRange(x0, x1), pickRange(y0, y1), startSteps)
            }
        }

        val (line1, line2) = lineRanges

        val intersectingRanges = line1.flatMap { range ->
            line2.filter { range2 -> intersects(range, range2) }.map { range to it }
        }

        val intersectionPoints = intersectingRanges.map { (a, b) ->
            val xPoint = listOf(a.x, a.y).single { it.first == it.last }.first
            val yPoint = listOf(b.x, b.y).single { it.first == it.last }.first

            val xSteps = abs(a.x.first - xPoint) + abs(b.x.first - xPoint)
            val ySteps = abs(a.y.first - yPoint) + abs(b.y.first - yPoint)

            val initialSteps = a.startSteps + b.startSteps
            val totalSteps = xSteps + ySteps + initialSteps

            Position(xPoint, yPoint) to totalSteps
        }

        val origin = Position(0, 0)
        val validIntersections = intersectionPoints.filter { (point) -> point != origin }

        // Ignore origin intersection
        val partOne = validIntersections.minOf { (point) -> manhattanDistance(origin, point) }
        val partTwo = validIntersections.minOf { (_, steps) -> steps }

        return AOCAnswer(partOne, partTwo)
    }

    fun manhattanDistance(a: Position, b: Position) = abs(a.first - b.first) + abs(a.second - b.second)
}
