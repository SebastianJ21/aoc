@file:Suppress("MemberVisibilityCanBePrivate")

package aoc19

import AOCYear
import convertInputToCharMatrix
import printAOCAnswers
import readInput
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2

typealias PositionDouble = Pair<Double, Double>

class Day10 {

    fun solve() {
        val rawInput = readInput("day10.txt", AOCYear.Nineteen)

        val matrix = convertInputToCharMatrix(rawInput)

        val positions = matrix.flatMapIndexed { rowI, row ->
            row.mapIndexedNotNull { colI, value ->
                if (value == '#') {
                    rowI.toDouble() to colI.toDouble()
                } else {
                    null
                }
            }
        }

        val (stationPosition, seenPositions) = positions.associateWith { fromPosition ->
            getFirstPositionsInSight(fromPosition, positions.minus(fromPosition)).size
        }.maxBy { (_, seenPositions) -> seenPositions }

        val initialState = positions.minus(stationPosition) to listOf<PositionDouble>()

        val removalSequence = generateSequence(initialState) { (positionsLeft, positionsRemoved) ->
            if (positionsLeft.isEmpty()) return@generateSequence null

            val newRemoved = getFirstPositionsInSight(stationPosition, positionsLeft)

            positionsLeft.minus(newRemoved.toSet()) to positionsRemoved.plus(newRemoved)
        }.flatMap { (_, removedPositions) -> removedPositions }

        val partTwo = removalSequence.toList()[199].let { (row, col) -> row + (col * 100) }.toInt()

        printAOCAnswers(seenPositions, partTwo)
    }

    fun getFirstPositionsInSight(origin: PositionDouble, positions: List<PositionDouble>): List<PositionDouble> {
        val slopes = positions.map { position ->
            val normalizedPosition = origin.first - position.first to position.second - origin.second

            // use atan2(x, y) (col, row) to get a north-west oriented slope
            val slope = atan2(normalizedPosition.second, normalizedPosition.first)

            val normalizedSlope = if (slope < 0.0) slope + (2 * PI) else slope

            Triple(position, normalizedPosition, normalizedSlope)
        }

        val sortedSlopes = slopes.sortedWith(
            compareBy<Triple<PositionDouble, PositionDouble, Double>> { (_, _, slope) -> slope }
                .thenBy { (_, normPos) -> abs(normPos.first) + abs(normPos.second) },
        )

        return sortedSlopes.distinctBy { (_, _, slope) -> slope }.map { (position) -> position }
    }
}
