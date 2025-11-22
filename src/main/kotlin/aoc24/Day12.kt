package aoc24

import AOCAnswer
import AOCSolution
import Direction
import Position
import applyDirection
import get
import getInDirectionOrNull
import getOrNull
import kotlinx.collections.immutable.persistentHashSetOf
import positions
import readInput
import toCharMatrix
import kotlin.Pair
import kotlin.collections.sumOf

class Day12 : AOCSolution {

    val up = -1 to 0
    val down = 1 to 0
    val left = 0 to -1
    val right = 0 to 1

    val directions: List<Direction> = listOf(up, right, down, left)

    override fun solve(): AOCAnswer {
        val rawInput = readInput("day12.txt", AOCYear.TwentyFour)
        val map = rawInput.toCharMatrix()

        val regions = map
            .positions()
            .fold(listOf<Pair<Char, Set<Position>>>()) { exploredRegions, position ->
                val regionName = map[position]

                // Region already explored
                if (exploredRegions.any { (name, positions) -> name == regionName && position in positions }) {
                    return@fold exploredRegions
                }

                val regionPositions = exploreRegion(position, map)
                listOf(regionName to regionPositions) + exploredRegions
            }

        val partOne = regions.sumOf { (name, positions) ->
            val area = positions.size

            val perimeter = positions.sumOf { position ->
                directions.count {
                    map.getInDirectionOrNull(position, it) != name
                }
            }

            area * perimeter
        }

        val partTwo = regions.sumOf { (name, positions) ->
            val area = positions.size
            val sides = countRegionSides(name, positions, map)

            sides * area
        }

        return AOCAnswer(partOne, partTwo)
    }

    private fun countRegionSides(name: Char, positions: Set<Position>, map: List<List<Char>>): Int {
        val seen = hashSetOf<Pair<Position, Direction>>()

        fun markSeen(position: Position, moveDirection: Direction, edgeDirection: Direction) {
            if (map.getOrNull(position) != name) return
            // Checks whether this position still has an edge in the edgeDirection
            if (map.getInDirectionOrNull(position, edgeDirection) == name) return

            seen.add(position to edgeDirection)

            markSeen(position.applyDirection(moveDirection), moveDirection, edgeDirection)
        }

        val sides = positions.sumOf { position ->
            val (neighborDirections, edgeDirections) = directions.partition {
                map.getInDirectionOrNull(position, it) == name
            }

            val edgeCount = edgeDirections.count { direction -> (position to direction) !in seen }

            neighborDirections.forEach { moveDirection ->
                edgeDirections.forEach { direction ->
                    markSeen(position, moveDirection, direction)
                }
            }

            edgeCount
        }

        return sides
    }

    private fun exploreRegion(start: Position, map: List<List<Char>>): Set<Position> {
        val regionName = map[start]

        val initialQueue = persistentHashSetOf(start)
        val initialSeen = persistentHashSetOf<Position>()

        val exploreSequence = generateSequence(initialQueue to initialSeen) { (queue, seen) ->
            val position = queue.firstOrNull() ?: return@generateSequence null

            val newPositions = directions.mapNotNull { direction ->
                val potentialPosition = position.applyDirection(direction)

                potentialPosition.takeIf { it !in seen && map.getOrNull(it) == regionName }
            }

            queue.remove(position).addAll(newPositions) to seen.add(position)
        }

        val (_, regionPositions) = exploreSequence.last()

        return regionPositions
    }
}
