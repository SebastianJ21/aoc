@file:Suppress("MemberVisibilityCanBePrivate")

package aoc20

import AOCYear
import kotlinx.collections.immutable.persistentHashMapOf
import readInput

private typealias Position3D = Triple<Int, Int, Int>

class Day17 {

    data class Position4D(val x: Int, val y: Int, val z: Int, val w: Int)

    fun solve() {
        val rawInput = readInput("day17.txt", AOCYear.Twenty)

        val initialPositions = rawInput.flatMapIndexed { x, row ->
            row.mapIndexedNotNull { y, value ->
                if (value == '#') Triple(x, y, 0) else null
            }
        }.toSet()

        val get3DNeighbors = { position: Position3D ->
            directions3D.map { (x, y, z) ->
                Position3D(position.first + x, position.second + y, position.third + z)
            }
        }

        val partOne = (1..6).fold(initialPositions) { positions, _ -> nextRound(positions, get3DNeighbors) }.size

        val initialPositions4D = initialPositions.map { (x, y, z) -> Position4D(x, y, z, 0) }.toSet()

        val get4DNeighbors = { position: Position4D ->
            directions4D.map { (x, y, z, w) ->
                Position4D(position.x + x, position.y + y, position.z + z, position.w + w)
            }
        }

        val partTwo = (1..6).fold(initialPositions4D) { position, _ -> nextRound(position, get4DNeighbors) }.size

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }

    private val baseDirections = listOf(
        0 to 1,
        0 to -1,
        1 to 0,
        -1 to 0,
        1 to 1,
        1 to -1,
        -1 to 1,
        -1 to -1,
        0 to 0,
    )

    val directions3D = (-1..1)
        .flatMap { z -> baseDirections.map { (x, y) -> Triple(x, y, z) } }
        .minus(Triple(0, 0, 0))

    val directions4D = directions3D.plus(Triple(0, 0, 0)).let { directions3D ->
        (-1..1).flatMap { w ->
            directions3D.map { (x, y, z) -> Position4D(x, y, z, w) }
        }
    }.minus(Position4D(0, 0, 0, 0))

    fun <T> nextRound(active: Set<T>, getNeighbors: (T) -> List<T>): Set<T> {
        val initialOccurrences = persistentHashMapOf<T, Int>()

        val (remainingActive, neighborsTrack) =
            active.fold(setOf<T>() to initialOccurrences) { (acc, neighborOccurrences), position ->

                val neighbours = getNeighbors(position).toSet()

                val newNeighborOccurrences = neighbours.fold(neighborOccurrences) { map, neighbour ->
                    map.put(neighbour, (map[neighbour] ?: 0) + 1)
                }

                val activeNeighbors = active.count { it in neighbours }

                val newAcc = if (activeNeighbors == 2 || activeNeighbors == 3) acc + (position) else acc

                newAcc to newNeighborOccurrences
            }

        val newActive = neighborsTrack.filter { (position, count) -> count == 3 && position !in active }.keys

        return remainingActive + newActive
    }
}
