@file:Suppress("MemberVisibilityCanBePrivate")

package aoc22

import Position
import mapToInt
import readInput
import kotlin.math.abs

class Day15 {

    fun solve() {
        val rawInput = readInput("day15.txt")

        val beaconSensorPositions = rawInput.map { line ->
            val (sensorPart, beaconPart) = line.split(": ")

            val (sensorX, sensorY) = sensorPart.split("at ").let { (_, rawCoordinates) ->
                rawCoordinates.replace(Regex("(x=|y=)"), "").split(", ").mapToInt()
            }

            val (beaconX, beaconY) = beaconPart.split("at ").let { (_, rawCoordinates) ->
                rawCoordinates.replace(Regex("(x=|y=)"), "").split(", ").mapToInt()
            }

            Position(sensorY, sensorX) to Position(beaconY, beaconX)
        }

        val sensorRanges = beaconSensorPositions.map { (sensor, beacon) ->
            sensor to manhattanDistance(sensor, beacon)
        }

        val partOneRow = 2000000

        val partOneRanges = sensorRanges.mapNotNull { (sensor, range) ->
            getRangeInRow(sensor, range, partOneRow)
        }

        val takenPositions = beaconSensorPositions.flatMap { it.toList() }.filter { it.first == partOneRow }.toSet()

        val partOne = mergeRanges(partOneRanges).sumOf { it.last - it.first + 1 } - takenPositions.size

        val (resultRow, colRanges) = (4000000 downTo 0).firstNotNullOf { fixedRow ->
            val ranges = sensorRanges.mapNotNull { (sensor, range) ->
                getRangeInRow(sensor, range, fixedRow)
            }

            mergeRanges(ranges).takeIf { it.size > 1 }?.let { fixedRow to it }
        }

        val resultCol = colRanges.let { (first, second) ->
            check(second.first - first.last == 2)
            second.first - 1
        }

        val partTwo = resultRow + resultCol * 4000000L

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }

    fun mergeRanges(ranges: List<IntRange>): List<IntRange> {
        val sortedRanges = ranges.sortedBy { it.first }

        return sortedRanges.fold(listOf(sortedRanges.first())) { acc, range ->
            val lastRange = acc.last()

            when {
                range.first in lastRange && range.last in lastRange -> acc
                range.first in lastRange -> acc.dropLast(1).plusElement(lastRange.first..range.last)
                else -> acc.plusElement(range)
            }
        }
    }

    fun getRangeInRow(sensor: Position, radius: Int, row: Int): IntRange? {
        val sensorRowRange = sensor.first - radius..sensor.first + radius

        if (row !in sensorRowRange) return null

        val distanceToRow = abs(row - sensor.first)

        val colRadius = radius - distanceToRow

        return sensor.second - colRadius..sensor.second + colRadius
    }

    private fun manhattanDistance(a: Position, b: Position) = a.let { (x1, y1) ->
        abs(b.first - x1) + abs(b.second - y1)
    }
}
