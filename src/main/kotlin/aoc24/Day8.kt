package aoc24

import AOCAnswer
import AOCSolution
import Position
import get
import getOrNull
import positions
import readInput
import toCharMatrix
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

class Day8 : AOCSolution {

    override fun solve(): AOCAnswer {
        val rawInput = readInput("day8.txt", AOCYear.TwentyFour)
        val matrix = rawInput.toCharMatrix()

        val antennaPositions = matrix
            .positions()
            .groupBy { matrix[it] }
            .minus('.')
            .values

        val closestAntiNodePositions = getAntiNodePositions(
            antennaPositions = antennaPositions,
            rowIndices = matrix.indices,
            checkOnlyClosest = true,
        ).filter { matrix.getOrNull(it) != null }.distinct()

        val allAntiNodePositions = getAntiNodePositions(
            antennaPositions = antennaPositions,
            rowIndices = matrix.indices,
            checkOnlyClosest = false,
        ).filter { matrix.getOrNull(it) != null }.distinct()

        val partOne = closestAntiNodePositions.size
        val partTwo = allAntiNodePositions.size

        return AOCAnswer(partOne, partTwo)
    }

    private fun getAntiNodePositions(
        antennaPositions: Collection<List<Position>>,
        rowIndices: IntRange,
        checkOnlyClosest: Boolean,
    ): List<Position> {
        return antennaPositions.flatMap { positions ->
            positions.flatMapIndexed { index, positionA ->
                positions.drop(index + 1).flatMap { positionB ->
                    val slope = calculateSlope(positionA, positionB)
                    val slopeIntercept = makeSlopeInterceptFunction(positionA, slope)

                    val rowIndexA = positionA.first
                    val rowIndexB = positionB.first

                    val rowDistance = (rowIndexA - rowIndexB).absoluteValue

                    val rowIndexesToCheck = if (checkOnlyClosest) {
                        val possibleRowIndexes = listOf(
                            rowIndexA + rowDistance,
                            rowIndexA - rowDistance,
                            rowIndexB + rowDistance,
                            rowIndexB - rowDistance,
                        )

                        possibleRowIndexes.filter { it in rowIndices }.minus(listOf(rowIndexA, rowIndexB))
                    } else {
                        generateSequence(rowDistance) { it + rowDistance }
                            .map { listOf(rowIndexA + it, rowIndexA - it, rowIndexB + it, rowIndexB - it) }
                            .map { rowIndexes -> rowIndexes.filter { it in rowIndices } }
                            .takeWhile { it.isNotEmpty() }
                            .flatten()
                            .toList()
                    }

                    rowIndexesToCheck.map { rowI ->
                        val colIndex = slopeIntercept(rowI)
                        val colIndexInt = colIndex.roundToInt()

                        Position(rowI, colIndexInt)
                    }
                }
            }
        }
    }

    private fun calculateSlope(a: Position, b: Position): Double {
        // Position holds (rowI, colI), which translates to (y, x) in cartesian coors
        val dy = a.first - b.first
        val dx = a.second - b.second

        val slope = dy.toDouble() / dx.toDouble()

        return slope
    }

    private fun makeSlopeInterceptFunction(a: Position, slope: Double): (rowI: Int) -> Double {
        // Slope-intercept formula
        // (y − y1) / m + x1  = x
        return { rowI: Int -> (rowI - a.first) / slope + a.second }
    }
}
