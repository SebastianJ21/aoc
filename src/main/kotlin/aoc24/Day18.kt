package aoc24

import AOCAnswer
import AOCSolution
import Position
import applyDirection
import at
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentHashSetOf
import mapToInt
import inputLines
import java.util.PriorityQueue

class Day18 : AOCSolution {

    private val up = -1 at 0
    private val down = 1 at 0
    private val left = 0 at -1
    private val right = 0 at 1

    private val directions = listOf(up, left, down, right)

    private val start = 0 at 0
    private val end = 70 at 70

    override fun solve(): AOCAnswer {
        val inputLines = inputLines()

        // Positions are provided with flipped coordinates (x = col, y = row)
        val allBlockedPositions = inputLines.map { it.split(",").mapToInt().let { (y, x) -> Position(x, y) } }

        // 71 x 71 grid
        val allPositions = (0..70).flatMap { x -> (0..70).map { y -> Position(x, y) } }

        val positions = (allPositions - allBlockedPositions.take(1024).toSet()).toSet()

        val shortestPath = shortestPath(positions, start, end) ?: error("No path between $start and $end found")
        val partOne = shortestPath.size

        // The first position which when removed, caused no path to exist
        val blockingPosition = findBlockingPosition(shortestPath, positions, allBlockedPositions.drop(1024))
        val partTwo = blockingPosition.let { (x, y) -> "$y,$x" }

        return AOCAnswer(partOne, partTwo)
    }

    fun findBlockingPosition(
        path: Set<Position>,
        positions: Set<Position>,
        blockedPositions: List<Position>,
    ): Position {
        val relevantPositionIndex = blockedPositions.indexOfFirst { it in path }
        check(relevantPositionIndex != -1) { "Blocking position does not exist!" }

        val skippedPositions = blockedPositions.take(relevantPositionIndex + 1)

        val newPositions = positions.minus(skippedPositions)

        val nextPath = shortestPath(newPositions, start, end)

        return if (nextPath == null) {
            blockedPositions[relevantPositionIndex]
        } else {
            findBlockingPosition(
                path = nextPath,
                positions = newPositions,
                blockedPositions = blockedPositions.drop(skippedPositions.size),
            )
        }
    }

    private data class Node(
        val position: Position,
        val distance: Int,
        val previousPositions: PersistentSet<Position>,
    )

    private fun shortestPath(positions: Set<Position>, start: Position, end: Position): Set<Position>? {
        val positionToShortestPath = HashMap<Position, Int>(positions.size)
        positionToShortestPath[start] = 0

        val visited = HashSet<Position>(positions.size)

        val queue = PriorityQueue<Node>(1000, Comparator { a, b -> a.distance.compareTo(b.distance) })
        queue.offer(Node(position = start, distance = 0, previousPositions = persistentHashSetOf()))

        while (queue.isNotEmpty()) {
            val (position, distance, previousPositions) = queue.poll()

            if (position == end) return previousPositions
            if (!visited.add(position)) continue

            directions.forEach { direction ->
                val nextPosition = position.applyDirection(direction)
                if (nextPosition !in positions || nextPosition in visited) return@forEach

                val currentShortestPath = positionToShortestPath[nextPosition] ?: Int.MAX_VALUE

                if (currentShortestPath > distance + 1) {
                    positionToShortestPath[nextPosition] = distance + 1

                    queue.offer(Node(nextPosition, distance + 1, previousPositions.add(position)))
                }
            }
        }

        // No path
        return null
    }
}
