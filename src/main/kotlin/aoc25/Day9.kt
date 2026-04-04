package aoc25

import AOCAnswer
import AOCSolution
import Position
import applyDirection
import at
import kotlinx.collections.immutable.persistentHashSetOf
import kotlinx.collections.immutable.persistentListOf
import mapToInt
import readInput
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class Day9 : AOCSolution {

    private val up = -1 at 0
    private val down = 1 at 0
    private val left = 0 at -1
    private val right = 0 at 1

    private val directions = listOf(up, right, down, left)

    private data class Intervals(val x: IntRange, val y: IntRange)

    override fun solve(): AOCAnswer {
        val rawInput = readInput("day9.txt", AOCYear.TwentyFive)

        val positions = rawInput
            .map { line -> line.split(",").mapToInt() }
            .map { (x, y) -> Position(x, y) }

        val intervals = positions.plus(positions.first()).zipWithNext { a, b -> makeInterval(a, b) }
        val xIntervals = intervals.filter { it.y.size() == 1 }
        val yIntervals = intervals.filter { it.x.size() == 1 }

        val intervalPositions = intervals.flatMap { it.toPositions() }
        val allPositions = (intervalPositions + positions).toSet()

        val positionPairs = positions
            .flatMapIndexed { index, positionA ->
                positions.drop(index + 1).map { positionB -> positionA to positionB }
            }
            .sortedByDescending { (positionA, positionB) -> area(positionA, positionB) }

        val partOne = positionPairs.first().let { (a, b) -> area(a, b) }

        val xCorners = xIntervals.flatMap { listOf(it.x.first, it.x.last) }.toSet()
        val yCorners = yIntervals.flatMap { listOf(it.y.first, it.y.last) }.toSet()

        val partTwo = positionPairs.first { (positionA, positionB) ->
            val positionC = Position(positionA.first, positionB.second)
            val positionD = Position(positionB.first, positionA.second)

            val paths = listOf(
                makeInterval(positionA, positionC),
                makeInterval(positionA, positionD),
                makeInterval(positionB, positionC),
                makeInterval(positionB, positionD),
            )

            if (paths.any { isBlocked(it, xIntervals, yIntervals) }) return@first false

            !isOutside(positionC, xIntervals, yIntervals, allPositions, xCorners, yCorners) &&
                !isOutside(positionD, xIntervals, yIntervals, allPositions, xCorners, yCorners)
        }.let { area(it.first, it.second) }

        return AOCAnswer(partOne, partTwo)
    }

    private fun isBlocked(path: Intervals, xIntervals: List<Intervals>, yIntervals: List<Intervals>): Boolean {
        val sharesNoEdges: (Intervals) -> Boolean = {
            (it.y.first != path.y.first && it.y.first != path.y.last) &&
                (it.y.last != path.y.first && it.y.last != path.y.last) &&
                (it.x.first != path.x.first && it.x.first != path.x.last) &&
                (it.x.last != path.x.first && it.x.last != path.x.last)
        }

        return when {
            path.x.size() == 1 ->
                xIntervals.any { it.x.intersects(path.x) && it.y.first in path.y && sharesNoEdges(it) }

            path.y.size() == 1 ->
                yIntervals.any { it.y.intersects(path.y) && it.x.first in path.x && sharesNoEdges(it) }

            else -> error("Intervals must be strictly vertical or horizontal")
        }
    }

    private fun isOutside(
        position: Position,
        xIntervals: List<Intervals>,
        yIntervals: List<Intervals>,
        allPositions: Set<Position>,
        xCorners: Set<Int>,
        yCorners: Set<Int>,
    ): Boolean {
        if (position in allPositions) return false
        val safePosition = findUnobstructedPosition(position, allPositions, xCorners, yCorners)

        val relevantY = yIntervals.filter { safePosition.second in it.y }

        val upBeam = 0..safePosition.first - 1
        val upCount = relevantY.count { upBeam.intersects(it.x) }
        if (upCount % 2 == 0) return true

        val downBeam = safePosition.first + 1..Int.MAX_VALUE
        val downCount = relevantY.count { downBeam.intersects(it.x) }
        if (downCount % 2 == 0) return true

        val relevantX = xIntervals.filter { safePosition.first in it.x }

        val leftBeam = 0..safePosition.second - 1
        val leftCount = relevantX.count { leftBeam.intersects(it.y) }
        if (leftCount % 2 == 0) return true

        val rightBeam = safePosition.second + 1..Int.MAX_VALUE
        val rightCount = relevantX.count { rightBeam.intersects(it.y) }
        return rightCount % 2 == 0
    }

    // Searches for a position that is not restricted by restrictedX or restrictedY in its position
    private fun findUnobstructedPosition(
        initial: Position,
        allPositions: Set<Position>,
        restrictedX: Set<Int>,
        restrictedY: Set<Int>,
    ): Position {
        val initialState = persistentListOf(initial) to persistentHashSetOf<Position>()
        val searchSequence = generateSequence(initialState) { (queue, seen) ->
            val position = queue.last()
            if (position in seen) return@generateSequence queue.removeAt(queue.lastIndex) to seen

            val newPositions = directions
                .mapNotNull { direction -> position.applyDirection(direction).takeIf { it !in allPositions } }
                .shuffled()

            queue.removeAt(queue.lastIndex).addAll(newPositions) to seen.add(position)
        }

        return searchSequence
            .map { (queue) -> queue.last() }
            .first { position -> position.first !in restrictedX && position.second !in restrictedY }
    }

    private fun Intervals.toPositions(): List<Position> {
        val positions = when {
            x.size() == 1 -> y.map { colI -> Position(x.first, colI) }
            y.size() == 1 -> x.map { rowI -> Position(rowI, y.first) }
            else -> error("Invalid intervals $this")
        }

        return positions.subList(1, positions.lastIndex)
    }

    private fun makeAscendingInterval(a: Int, b: Int) = min(a, b)..max(a, b)

    private fun makeInterval(a: Position, b: Position) = Intervals(
        x = makeAscendingInterval(a.first, b.first),
        y = makeAscendingInterval(a.second, b.second),
    )

    private fun area(a: Position, b: Position) = abs(a.first - b.first).inc().toLong() * abs(a.second - b.second).inc()

    private fun IntRange.size() = last - first + 1

    private fun IntRange.intersects(other: IntRange) = !(last < other.first || first > other.last)
}
