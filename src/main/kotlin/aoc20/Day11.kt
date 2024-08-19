@file:Suppress("MemberVisibilityCanBePrivate")

package aoc20

import AOCYear
import Position
import applyDirection
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import plus
import readInput
import kotlin.math.abs

class Day11 {

    val up = -1 to 0
    val down = 1 to 0
    val left = 0 to -1
    val right = 0 to 1

    val neighborhood = listOf(up + left, up, up + right, right, down + right, down, down + left, left)

    fun solve() {
        val rawInput = readInput("day11.txt", AOCYear.Twenty)

        val initialState: Pair<Set<Position>, List<Position>> = rawInput.foldIndexed(
            persistentSetOf<Position>() to persistentListOf<Position>(),
        ) { rowI, acc, row ->
            row.foldIndexed(acc) { colI, (occupied, unoccupied), value ->
                when (value) {
                    'L' -> occupied to unoccupied.add(rowI to colI)
                    '#' -> occupied.add(rowI to colI) to unoccupied
                    else -> occupied to unoccupied
                }
            }
        }

        val allSeats = initialState.let { (occupied, unoccupied) -> occupied + unoccupied }

        val closestSeats = findClosestSeats(allSeats, rawInput.size, rawInput[0].length)

        val immediateClosest = closestSeats.mapValues { (seat, closestSeats) ->
            closestSeats.filter {
                val (rowDist, colDist) = manhattanDistance(seat, it)
                rowDist < 2 && colDist < 2
            }
        }

        val partOneStateSequence = generateSequence(initialState) { state ->
            getNextState(state.first, state.second, immediateClosest, 4).takeIf { it != state }
        }

        val partTwoStateSequence = generateSequence(initialState) { state ->
            getNextState(state.first, state.second, closestSeats, 5).takeIf { it != state }
        }

        val partOne = partOneStateSequence.last().first.size
        val partTwo = partTwoStateSequence.last().first.size

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }

    fun manhattanDistance(a: Position, b: Position) = abs(b.first - a.first) to abs(b.second - a.second)

    fun findClosestSeats(seats: Set<Position>, rows: Int, cols: Int): Map<Position, List<Position>> {
        fun Position.outOfBounds() = first < 0 || first >= rows || second < 0 || second >= cols

        fun positionSequence(position: Position, direction: Pair<Int, Int>) =
            generateSequence(position.applyDirection(direction)) { current ->
                if (current.outOfBounds() || current in seats) {
                    null
                } else {
                    current.applyDirection(direction)
                }
            }

        return seats.associateWith { position ->
            neighborhood.mapNotNull { direction ->
                val last = positionSequence(position, direction).last()

                if (last in seats) last else null
            }
        }
    }

    fun getNextState(
        occupied: Set<Position>,
        unoccupied: List<Position>,
        seatToClosest: Map<Position, List<Position>>,
        leavingThreshold: Int,
    ): Pair<Set<Position>, List<Position>> {
        val (leaving, staying) = occupied.partition { position ->
            seatToClosest[position]!!.count { it in occupied } >= leavingThreshold
        }

        val (arriving, stayingUnoccupied) = unoccupied.partition { position ->
            seatToClosest[position]!!.none { it in occupied }
        }

        return staying.plus(arriving).toSet() to leaving.plus(stayingUnoccupied)
    }
}
