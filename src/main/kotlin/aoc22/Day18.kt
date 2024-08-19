package aoc22

import mapToInt
import readInput

private typealias Point = Triple<Int, Int, Int>

class Day18 {

    val directions = listOf(
        Triple(1, 0, 0),
        Triple(-1, 0, 0),
        Triple(0, 1, 0),
        Triple(0, -1, 0),
        Triple(0, 0, 1),
        Triple(0, 0, -1),
    )

    fun solve() {
        val rawInput = readInput("day18.txt")
        val points = rawInput.map { line ->
            line.split(",")
                .mapToInt()
                .let { Point(it[0], it[1], it[2]) }
        }

        val pointsSet = points.toSet()

        val pointToNeighbors = points.associateWith { getNeighbors(it) }

        val neighbours = pointToNeighbors.values.map { neighbors ->
            neighbors.filter { it in pointsSet }
        }

        val partOne = neighbours.sumOf { 6 - it.size }

        val xBounds = points.minOf { it.first }..points.maxOf { it.first }
        val yBounds = points.minOf { it.second }..points.maxOf { it.second }
        val zBounds = points.minOf { it.third }..points.maxOf { it.third }

        val partTwo = points.sumOf { point ->
            pointToNeighbors.getValue(point).count {
                gridSearch3D(it, pointToNeighbors.keys, xBounds, yBounds, zBounds)
            }
        }

        println("Part One: $partOne")
        println("Part Two: $partTwo")
    }

    private fun gridSearch3D(
        start: Point,
        allPoints: Set<Point>,
        xBounds: IntRange,
        yBounds: IntRange,
        zBounds: IntRange,
    ): Boolean {
        if (start in allPoints) return false

        val queue = ArrayDeque<Point>()
        val visited = hashSetOf<Point>()

        queue.addLast(start)
        visited.add(start)

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()

            if (current.isOutsideBounds(xBounds, yBounds, zBounds)) {
                return true
            }

            val toCheck = getNeighbors(current).filter {
                visited.add(it) && it !in allPoints
            }

            queue.addAll(toCheck)
        }

        return false
    }

    private operator fun Point.plus(other: Point) =
        Point(first + other.first, second + other.second, third + other.third)

    private fun getNeighbors(point: Point): List<Point> {
        return directions.map { point + it }
    }

    private fun Point.isOutsideBounds(xBounds: IntRange, yBounds: IntRange, zBounds: IntRange): Boolean {
        return first !in xBounds || second !in yBounds || third !in zBounds
    }
}
