package aoc24

import AOCAnswer
import AOCSolution
import Position
import applyDirection
import at
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentHashSetOf
import positionsOf
import inputLines
import java.util.PriorityQueue
import kotlin.math.abs

class Day16 : AOCSolution {

    private val up = -1 at 0
    private val down = 1 at 0
    private val left = 0 at -1
    private val right = 0 at 1

    private val directions = listOf(up, left, down, right)

    companion object {
        private const val MOVE_COST = 1
        private const val TURN_COST = 1_000
    }

    override fun solve(): AOCAnswer {
        val inputLines = inputLines()

        val start = inputLines.positionsOf { it == 'S' }.single()
        val end = inputLines.positionsOf { it == 'E' }.single()

        val positions = inputLines.positionsOf { it != '#' }.toHashSet()

        val startState = State(
            position = start,
            directionIndex = directions.indexOf(right),
            score = 0,
            previousPositions = persistentHashSetOf(),
        )

        val positionToAllPaths = allPaths(positions, startState, end)

        val endPaths = positionToAllPaths.getValue(end)
        val bestScore = endPaths.minOf { it.score }

        val bestScorePathAllPositions = endPaths
            .asSequence()
            .filter { it.score == bestScore }
            .flatMap { it.previousPositions }
            .plus(end)
            .distinct()

        val partOne = bestScore
        val partTwo = bestScorePathAllPositions.count()

        return AOCAnswer(partOne, partTwo)
    }

    private data class State(
        val position: Position,
        val directionIndex: Int,
        val score: Int,
        val previousPositions: PersistentSet<Position>,
    )

    private fun allPaths(positions: Set<Position>, start: State, end: Position): Map<Position, List<State>> {
        val positionToPaths = HashMap<Position, List<State>>(positions.size)
        positionToPaths[start.position] = listOf(start)

        val queue = PriorityQueue<State>(1000, Comparator { a, b -> a.score.compareTo(b.score) })
        queue.offer(start)

        while (queue.isNotEmpty()) {
            val (position, currentDirectionIndex, score, previousPositions) = queue.poll()

            if (position == end) continue

            directions.forEachIndexed { directionIndex, direction ->
                val nextPosition = position.applyDirection(direction)
                if (nextPosition !in positions || position in previousPositions) return@forEachIndexed

                val numOfTurns = when (abs(currentDirectionIndex - directionIndex)) {
                    0 -> 0
                    1, 3 -> 1
                    2 -> 2
                    else -> error("")
                }
                val cost = (numOfTurns * TURN_COST) + MOVE_COST
                val newScore = score + cost

                val currentPaths = positionToPaths[nextPosition].orEmpty()

                val isBest = currentPaths.none { it.directionIndex == directionIndex && it.score < newScore }
                if (!isBest) return@forEachIndexed

                val state = State(
                    position = nextPosition,
                    directionIndex = directionIndex,
                    score = newScore,
                    previousPositions = previousPositions.add(position),
                )

                positionToPaths[nextPosition] = currentPaths + state
                queue.offer(state)
            }
        }

        return positionToPaths
    }
}
