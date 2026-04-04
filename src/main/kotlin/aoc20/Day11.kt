package aoc20

import AOCAnswer
import AOCSolution
import AOCYear
import Direction
import Position
import applyDirection
import at
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import plus
import readInput
import kotlin.math.abs

class Day11 : AOCSolution {

    private val up = -1 at 0
    private val down = 1 at 0
    private val left = 0 at -1
    private val right = 0 at 1

    private val neighborhood = listOf(up + left, up, up + right, right, down + right, down, down + left, left)

    override fun solve(): AOCAnswer {
        val rawInput = readInput("day11.txt", AOCYear.Twenty)

        val (initialOccupied: Set<Position>, initialUnoccupied: List<Position>) = rawInput.foldIndexed(
            persistentSetOf<Position>() to persistentListOf<Position>(),
        ) { rowI, acc, row ->
            row.foldIndexed(acc) { colI, (occupied, unoccupied), value ->
                when (value) {
                    'L' -> occupied to unoccupied.add(rowI at colI)
                    '#' -> occupied.add(rowI at colI) to unoccupied
                    else -> occupied to unoccupied
                }
            }
        }

        val allSeats = initialOccupied + initialUnoccupied

        val closestSeats = findClosestSeats(allSeats, rawInput.size, rawInput[0].length)

        val immediateClosest = closestSeats.mapValues { (seat, closestSeats) ->
            closestSeats.filter {
                val (rowDist, colDist) = manhattanDistance(seat, it)
                rowDist < 2 && colDist < 2
            }
        }

        val partOneStateSequence = generateSequence(initialOccupied to initialUnoccupied) { state ->
            getNextState(state.first, state.second, immediateClosest, 4).takeIf { it != state }
        }

        val partTwoStateSequence = generateSequence(initialOccupied to initialUnoccupied) { state ->
            getNextState(state.first, state.second, closestSeats, 5).takeIf { it != state }
        }

        val partOne = partOneStateSequence.last().first.size
        val partTwo = partTwoStateSequence.last().first.size

        return AOCAnswer(partOne, partTwo)
    }

    private fun manhattanDistance(a: Position, b: Position) = abs(b.first - a.first) to abs(b.second - a.second)

    private fun findClosestSeats(seats: Set<Position>, rows: Int, cols: Int): Map<Position, List<Position>> {
        fun Position.outOfBounds() = first < 0 || first >= rows || second < 0 || second >= cols

        fun positionSequence(position: Position, direction: Direction) =
            generateSequence(position.applyDirection(direction)) { current -> current.applyDirection(direction) }
                .takeWhile { !it.outOfBounds() && it !in seats }

        return seats.associateWith { position ->
            neighborhood.mapNotNull { direction ->
                val last = positionSequence(position, direction).last()

                if (last in seats) last else null
            }
        }
    }

    private fun getNextState(
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
