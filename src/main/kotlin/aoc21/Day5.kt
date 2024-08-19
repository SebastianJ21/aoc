package aoc21

import AOCYear
import mapToInt
import readInput

class Day5 {
    data class LineSegment(val x: IntProgression, val y: IntProgression)

    fun solve() {
        val rawInput = readInput("day5.txt", AOCYear.TwentyOne)

        val lineSegments = extractLineSegments(rawInput)

        val (straightLines, diagonalLines) =
            lineSegments.partition { (x, y) ->
                x.first == x.last || y.first == y.last
            }

        val straightLinesPoints =
            straightLines.flatMap { (xRange, yRange) ->
                xRange.flatMap { x ->
                    yRange.map { y -> x to y }
                }
            }

        val partOne = straightLinesPoints.groupingBy { it }.eachCount().count { it.value > 1 }

        val diagonalLinesPoints = diagonalLines.flatMap { (xRange, yRange) -> xRange.zip(yRange) }

        val partTwo = (straightLinesPoints + diagonalLinesPoints).groupingBy { it }.eachCount().count { it.value > 1 }

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }

    private fun extractLineSegments(input: List<String>): List<LineSegment> = input.map { line ->
        line.replace(" ", "")
            .split("->")
            .let { (from, to) ->
                val (x1, y1) = from.split(",").mapToInt()
                val (x2, y2) = to.split(",").mapToInt()

                val xStep = if (x1 <= x2) 1 else -1
                val yStep = if (y1 <= y2) 1 else -1

                val xProgression = IntProgression.fromClosedRange(x1, x2, xStep)
                val yProgression = IntProgression.fromClosedRange(y1, y2, yStep)

                LineSegment(xProgression, yProgression)
            }
    }
}
