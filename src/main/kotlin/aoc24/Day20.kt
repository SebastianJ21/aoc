package aoc24

import AOCAnswer
import AOCSolution
import Direction
import Position
import applyDirection
import at
import inputLines
import positionsOf
import kotlin.math.absoluteValue

class Day20 : AOCSolution {

    private val up = -1 at 0
    private val down = 1 at 0
    private val left = 0 at -1
    private val right = 0 at 1

    private val directions = listOf(up, left, down, right)

    override fun solve(): AOCAnswer {
        val inputLines = inputLines()

        val positions = inputLines.positionsOf { it != '#' }.toSet()
        val end = inputLines.positionsOf { it == 'E' }.first()
        val start = inputLines.positionsOf { it == 'S' }.first()

        val path = tracePath(positions, start, end)
        val positionToDistance = path.asReversed().withIndex().associate { (index, position) -> position to index }

        val partOne = score(positionToDistance = positionToDistance, size = 2, minSave = 100)
        val partTwo = score(positionToDistance = positionToDistance, size = 20, minSave = 100)

        return AOCAnswer(partOne, partTwo)
    }

    private fun score(positionToDistance: Map<Position, Int>, size: Int, minSave: Int): Int {
        val radiusDirections = radiusDirections(size = size)

        return positionToDistance
            .entries
            .sumOf { (position, distance) ->
                val shortcuts = radiusDirections.mapNotNull { direction ->
                    positionToDistance[position.applyDirection(direction)]?.plus(direction.size())
                }

                shortcuts.count { distance - it >= minSave }
            }
    }

    private fun tracePath(positions: Set<Position>, start: Position, end: Position): List<Position> {
        val queue = ArrayDeque(listOf(start))
        val visited = LinkedHashSet<Position>(positions.size)

        while (queue.isNotEmpty()) {
            val position = queue.removeFirst()

            if (!visited.add(position)) continue
            if (position == end) continue

            val direction = directions.single {
                val nextPosition = position.applyDirection(it)

                nextPosition in positions && nextPosition !in visited
            }

            queue.add(position.applyDirection(direction))
        }

        return visited.toList()
    }

    private fun radiusDirections(size: Int): List<Direction> {
        val queue = ArrayDeque(directions.map { it to 1 })
        val seen = mutableSetOf<Direction>()

        while (queue.isNotEmpty()) {
            val (direction, distance) = queue.removeFirst()

            if (distance > size) continue
            if (!seen.add(direction)) continue

            directions.forEach {
                val newDirection = direction.applyDirection(it)

                if (newDirection !in seen) {
                    queue.add(newDirection to distance + 1)
                }
            }
        }

        seen.remove(0 at 0)

        return seen.toList()
    }

    private fun Direction.size() = first.absoluteValue + second.absoluteValue
}
