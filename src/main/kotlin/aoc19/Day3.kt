package aoc19

import AOCAnswer
import AOCSolution
import AOCYear
import Direction
import Position
import applyDirection
import at
import readInput
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class Day3 : AOCSolution {

    private val up = -1 at 0
    private val down = 1 at 0
    private val left = 0 at -1
    private val right = 0 at 1

    private val origin = 0 at 0

    private data class LineRange(val x: IntProgression, val y: IntProgression, val startSteps: Int)

    override fun solve(): AOCAnswer {
        val rawInput = readInput("day3.txt", AOCYear.Nineteen)

        val linePositions = rawInput.map { line ->
            val rawCommands = line.split(",")

            rawCommands.runningFold(origin to 0) { (position, steps), rawCommand ->
                val direction = rawCommand.first()
                val size = rawCommand.drop(1).toInt()

                val movement = getDirectionVector(direction).scaleBy(size)
                val newPosition = position.applyDirection(movement)

                newPosition to steps + size
            }
        }

        val lineRanges = linePositions.map { positions ->
            positions.zipWithNext { (fromPosition, startSteps), (toPosition) ->
                val (x0, y0) = fromPosition
                val (x1, y1) = toPosition

                LineRange(pickRange(x0, x1), pickRange(y0, y1), startSteps)
            }
        }

        val (line1Ranges, line2Ranges) = lineRanges

        val intersectingRanges = line1Ranges.flatMap { line1 ->
            val intersecting = line2Ranges.filter { line2 -> intersects(line1, line2) }

            intersecting.map { line2 -> line1 to line2 }
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

        val validIntersections = intersectionPoints.filter { (point) -> point != origin }

        // Ignore origin intersection
        val partOne = validIntersections.minOf { (point) -> manhattanDistance(origin, point) }
        val partTwo = validIntersections.minOf { (_, steps) -> steps }

        return AOCAnswer(partOne, partTwo)
    }

    private fun IntProgression.intersects(other: IntProgression): Boolean {
        val (low, high) = min(first, last) to max(first, last)
        val (otherLow, otherHigh) = min(other.first, other.last) to max(other.first, other.last)

        return !(high < otherLow || low > otherHigh)
    }

    private fun intersects(a: LineRange, b: LineRange) = a.x.intersects(b.x) && a.y.intersects(b.y)

    private fun pickRange(from: Int, to: Int) = if (from <= to) from..to else from downTo to

    private fun manhattanDistance(a: Position, b: Position) = abs(a.first - b.first) + abs(a.second - b.second)

    private fun getDirectionVector(direction: Char): Direction = when (direction) {
        'R' -> right
        'L' -> left
        'D' -> down
        'U' -> up
        else -> error("Illegal direction $direction")
    }

    private fun Position.scaleBy(value: Int) = first * value at second * value
}
