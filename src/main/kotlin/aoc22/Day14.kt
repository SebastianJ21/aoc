@file:Suppress("MemberVisibilityCanBePrivate")

package aoc22

import Position
import applyDirection
import invertListMap
import mapToInt
import readInput
import kotlin.math.max
import kotlin.math.min

class Day14 {

    data class Line(
        val x: IntRange,
        val y: IntRange,
    )

    val down = 1 to 0
    val left = 1 to -1
    val right = 1 to 1

    val sandDirections = listOf(down, left, right)

    fun solve() {
        val input = readInput("day14.txt")

        val intervals = input.flatMap { line ->
            val rawIntervals = line.split(" -> ")
            val points = rawIntervals.map {
                it.split(",").mapToInt().let { (y, x) -> y to x }
            }

            val intervals = points.zipWithNext { (y1, x1), (y2, x2) ->
                (min(x1, x2) to min(y1, y2)) to (max(x1, x2) to max(y1, y2))
            }

            intervals
        }.distinct()

        val lines = intervals.map { (from, to) -> Line(from.first..to.first, from.second..to.second) }

        val lowestPoint = lines.maxOf { it.x.last }
        val sandStart = 0 to 500

        // Dropped sand
        val sandPositions = getFinalSandPositions(sandStart, lines) { (x), _ -> x >= lowestPoint }

        val platformRow = lowestPoint + 2
        val platformLine = Line(platformRow..platformRow, Int.MIN_VALUE..Int.MAX_VALUE)

        val linesWithPlatform = lines + platformLine

        val sandPositionsWithPlatform = getFinalSandPositions(sandStart, linesWithPlatform) { current, dropped ->
            sandStart == current && current in dropped
        }

        println("Part One: ${sandPositions.size}")
        println("Part Two: ${sandPositionsWithPlatform.size}")
    }

    fun getFinalSandPositions(
        sandStart: Position,
        lines: List<Line>,
        stopDropPredicate: (Position, Set<Position>) -> Boolean,
    ): Set<Position> {
        val xPointToYInterval = invertListMap(
            lines.associateWith { (x) -> x.toList() },
        ).mapValues { (_, lines) -> lines.map { it.y } }

        return buildSet {
            val dropSandSequence = generateSequence(sandStart) { currentSand ->
                if (stopDropPredicate(currentSand, this)) {
                    return@generateSequence null
                }

                sandDirections.firstNotNullOfOrNull { direction ->
                    val newPosition = currentSand.applyDirection(direction)

                    newPosition.takeIf { (x, y) ->
                        newPosition !in this &&
                            xPointToYInterval[x]?.let { intervals -> intervals.any { y in it } } != true
                    }
                }
            }

            val settledSandSequence = generateSequence {
                val lastPosition = dropSandSequence.last()

                when {
                    stopDropPredicate(lastPosition, this) -> null
                    lastPosition == sandStart -> {
                        add(lastPosition)
                        null
                    }
                    else -> {
                        add(lastPosition)
                        lastPosition
                    }
                }
            }

            settledSandSequence.last()
        }
    }
}
