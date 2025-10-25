package aoc24

import AOCAnswer
import AOCSolution
import Position
import applyDirection
import get
import getOrNull
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentHashSetOf
import kotlinx.collections.immutable.persistentSetOf
import positionsOf
import readInput
import toMatrix

class Day10 : AOCSolution {

    val up = -1 to 0
    val down = 1 to 0
    val left = 0 to -1
    val right = 0 to 1

    val directions = listOf(up, right, down, left)

    override fun solve(): AOCAnswer {
        val rawInput = readInput("day10.txt", AOCYear.TwentyFour)
        val map = rawInput.toMatrix { it.digitToInt() }

        val startPositions = map.positionsOf { it == 0 }

        val partOne = startPositions.sumOf { start -> getPathPositions(map, start).count { map[it] == 9 } }
        val partTwo = startPositions.sumOf { start -> getPathScore(map, start) }

        return AOCAnswer(partOne, partTwo)
    }

    private fun getPathPositions(matrix: List<List<Int>>, start: Position): Set<Position> {
        fun explore(position: Position, seen: PersistentSet<Position>): PersistentSet<Position> {
            val nextValue = matrix[position] + 1

            val nextPositions = directions
                .map { position.applyDirection(it) }
                .filter { it !in seen && matrix.getOrNull(it) == nextValue }

            return nextPositions.fold(seen.add(position)) { seenAcc, pos ->
                explore(pos, seenAcc)
            }
        }
        val seenPositions = explore(start, persistentSetOf())

        return seenPositions
    }

    // Counts number of paths that end with value 9
    private fun getPathScore(matrix: List<List<Int>>, start: Position): Int {
        fun explore(position: Position, seen: PersistentSet<Position>): Int {
            val currentValue = matrix[position]
            if (currentValue == 9) return 1

            val nextPositions = directions
                .map { position.applyDirection(it) }
                .filter { it !in seen && matrix.getOrNull(it) == currentValue + 1 }
            val newSeen = seen.add(position)

            return nextPositions.sumOf { explore(it, newSeen) }
        }

        return explore(start, persistentHashSetOf())
    }
}
