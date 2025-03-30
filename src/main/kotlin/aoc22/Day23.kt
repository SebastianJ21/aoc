@file:Suppress("MemberVisibilityCanBePrivate")

package aoc22

import Position
import plus
import readInput
import toCharMatrix
import transposed

private const val EMPTY_POSITION = '.'
private const val OCCUPIED_POSITION = '#'
private typealias Direction = Pair<Int, Int>

class Day23 {
    private fun directionToVector(direction: String) = when (direction) {
        "N" -> (-1 to 0)
        "NE" -> (-1 to 1)
        "E" -> (0 to 1)
        "SE" -> (1 to 1)
        "S" -> (1 to 0)
        "SW" -> (1 to -1)
        "W" -> (0 to -1)
        "NW" -> (-1 to -1)
        else -> error("Invalid direction $direction")
    }

    private val directions =
        listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW").map { directionToVector(it) }

    val propositionToDirections = listOf(
        "N" to listOf("N", "NE", "NW"),
        "S" to listOf("S", "SE", "SW"),
        "W" to listOf("W", "SW", "NW"),
        "E" to listOf("E", "NE", "SE"),
    ).associate { (key, value) -> directionToVector(key) to value.map { directionToVector(it) } }

    fun solve() {
        val rawInput = readInput("day23.txt")
        val matrix = rawInput.toCharMatrix()

        tailrec fun perform(
            positions: Set<Position>,
            order: List<Direction>,
            round: Int,
            roundLimit: Int,
        ): Pair<Int, Set<Position>> = if (round >= roundLimit) {
            roundLimit to positions
        } else {
            val (newPositions, noMovements) = executeRound(positions, order)

            if (noMovements) {
                round to newPositions
            } else {
                perform(newPositions, order.shiftLeft(), round + 1, roundLimit)
            }
        }

        val initialOrder = listOf("N", "S", "W", "E").map { directionToVector(it) }
        val partOneRounds = 11

        val partOneMatrix = matrix.buffered(EMPTY_POSITION, partOneRounds)

        val initialOccupiedPositions = getOccupiedPositions(partOneMatrix)

        val (_, finalOccupiedPositions) = perform(initialOccupiedPositions, initialOrder, 1, partOneRounds)

        val partOne = countEmptyPositions(partOneMatrix, finalOccupiedPositions)

        // Part two does not care for the 'buffering' as we only care about the number of rounds to no movements
        val (partTwo) = perform(initialOccupiedPositions, initialOrder, 1, 1000)

        println("Part One: $partOne")
        println("Part Two: $partTwo")
    }

    private fun countEmptyPositions(initialMatrix: List<List<Char>>, occupiedPositions: Set<Position>): Int {
        val matrix = initialMatrix.mapIndexed { rowI, row ->
            List(row.size) { colI ->
                if (rowI to colI in occupiedPositions) OCCUPIED_POSITION else EMPTY_POSITION
            }
        }

        val normalizedMatrix = matrix
            .dropWhile { row -> row.all { it == EMPTY_POSITION } }
            .dropLastWhile { row -> row.all { it == EMPTY_POSITION } }
            .transposed()
            .dropWhile { row -> row.all { it == EMPTY_POSITION } }
            .dropLastWhile { row -> row.all { it == EMPTY_POSITION } }

        return normalizedMatrix.sumOf { row ->
            row.count { it == EMPTY_POSITION }
        }
    }

    private fun executeRound(
        positions: Set<Position>,
        propositionOrder: List<Direction>,
    ): Pair<Set<Position>, Boolean> {
        val currentToNew: Map<Position, Position> = buildMap {
            positions.forEach { position ->

                getDirectionOrNull(propositionOrder, position, positions)?.let { direction ->
                    this[position] = position + direction
                }
            }
        }

        val newPosToCount = currentToNew.values.groupingBy { it }.eachCount()

        val validMovements = currentToNew.filterValues { newPosToCount[it] == 1 }

        return positions.minus(validMovements.keys).plus(validMovements.values) to validMovements.isEmpty()
    }

    private fun List<Direction>.shiftLeft() = drop(1) + first()

    private fun getDirectionOrNull(
        propositionOrder: List<Direction>,
        position: Position,
        occupiedPositions: Set<Position>,
    ): Direction? {
        val emptyPositions = directions.filter { (position + it) !in occupiedPositions }

        if (emptyPositions.size == directions.size) return null

        return propositionOrder.firstOrNull { possibleDirection ->
            val requiredDirections = propositionToDirections.getValue(possibleDirection)

            requiredDirections.all { it in emptyPositions }
        }
    }

    fun getOccupiedPositions(matrix: List<List<Char>>) = matrix.flatMapIndexed { rowI, row ->
        row.mapIndexedNotNull { colI, value ->
            if (value == OCCUPIED_POSITION) rowI to colI else null
        }.toSet()
    }.toSet()

    private fun List<List<Char>>.buffered(bufferWith: Char, bufferSize: Int): List<List<Char>> {
        val bufferRow = List(bufferSize) { List(first().size) { bufferWith } }

        val withBufferedRows = bufferRow + this + bufferRow

        val bufferCol = List(bufferSize) { List(withBufferedRows.size) { bufferWith } }

        val buffered = (bufferCol + withBufferedRows.transposed() + bufferCol).transposed()

        return buffered
    }
}
