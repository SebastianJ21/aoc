package aoc19

import AOCAnswer
import AOCSolution
import Position
import applyDirection
import at
import get
import getInDirectionOrNull
import inputLines
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentHashSetOf
import kotlinx.collections.immutable.toPersistentHashSet
import kotlinx.collections.immutable.toPersistentSet
import plus
import positionsOf
import java.util.Collections
import java.util.SortedSet
import kotlin.collections.forEach
import kotlin.collections.set

class Day18 : AOCSolution {

    private val up = -1 at 0
    private val down = 1 at 0
    private val left = 0 at -1
    private val right = 0 at 1

    private val directions = listOf(up, right, down, left)

    override fun solve(): AOCAnswer {
        val inputLines = inputLines()

        val matrix = inputLines.map { it.toList() }

        val start = matrix.positionsOf { it == '@' }.single()
        val keys = matrix.flatMap { row -> row.filter { it.isLowerCase() } }

        val partOne = search(startPositions = listOf(start), matrix = matrix, allKeys = keys)

        val newRobotPositions =
            listOf(up + left, up + right, down + left, down + right).map { start.applyDirection(it) }
        val newWallPositions = directions.map { start.applyDirection(it) } + start

        val newMatrix = matrix.mutate { mutableMatrix ->
            newRobotPositions.forEach { (rowI, colI) -> mutableMatrix[rowI][colI] = '@' }
            newWallPositions.forEach { (rowI, colI) -> mutableMatrix[rowI][colI] = '#' }
        }

        val partTwo = search(newRobotPositions, newMatrix, keys)

        return AOCAnswer(partOne, partTwo)
    }

    private data class Key(
        val position: Position,
        val char: Char,
    )

    private fun search(startPositions: List<Position>, matrix: List<List<Char>>, allKeys: List<Char>): Int {
        val positionToReachableKeys = HashMap<Position, Map<Key, SortedSet<Pair<Set<Char>, Int>>>>()
        val cache = HashMap<Pair<Set<Position>, Set<Char>>, Int>(1_000)

        fun execute(
            positions: PersistentSet<Position>,
            keys: PersistentSet<Char>,
            keysLeft: PersistentSet<Char>,
            score: Int,
        ): Int {
            if (keysLeft.isEmpty()) return score

            cache[positions to keys]?.let { cached ->
                if (score >= cached) return Int.MAX_VALUE
            }

            // Positions -> reachable keys for each -> select best for each key
            val positionToBestPath = positions.flatMap { position ->
                positionToReachableKeys.getOrPut(position) { reachableKeys(position, matrix) }
                    .filterKeys { it.char in keysLeft }
                    .mapNotNull { (key, paths) ->
                        paths.firstOrNull { (keysNeeded) -> keys.containsAll(keysNeeded) }
                            ?.let { (_, distance) -> Triple(position, key, distance) }
                    }
            }

            val result = positionToBestPath
                .sortedBy { (_, _, distance) -> distance }
                .minOf { (position, key, distance) ->
                    execute(
                        positions = positions.removing(position).adding(key.position),
                        keys.adding(key.char),
                        keysLeft.removing(key.char),
                        score + distance,
                    )
                }

            cache[positions to keys] = score

            return result
        }

        val res = execute(
            positions = startPositions.toPersistentSet(),
            keys = persistentHashSetOf(),
            keysLeft = allKeys.toPersistentHashSet(),
            score = 0,
        )
        return res
    }

    private fun reachableKeys(
        start: Position,
        matrix: List<List<Char>>,
    ): Map<Key, SortedSet<Pair<Set<Char>, Int>>> {
        val positionToPaths = hashMapOf<Position, Set<Pair<Set<Char>, Int>>>()

        val queue = ArrayDeque(listOf(Triple(start, 0, setOf<Char>())))

        while (queue.isNotEmpty()) {
            val (position, distance, keysUsed) = queue.removeFirst()

            positionToPaths[position]?.let { paths ->
                // Is there a path using the same keys with better distance?
                if (paths.any { (pathKeys, pathDist) -> pathDist <= distance && keysUsed.containsAll(pathKeys) }) {
                    continue
                }
            }

            positionToPaths[position] = positionToPaths[position].orEmpty() + (keysUsed to distance)

            directions.forEach { direction ->
                val value = matrix.getInDirectionOrNull(position, direction)
                if (value == null || value == '#') return@forEach

                val nextPosition = position.applyDirection(direction)

                val newKeysUsed = if (value.isUpperCase()) {
                    keysUsed + value.lowercaseChar()
                } else {
                    keysUsed
                }

                queue.addLast(Triple(nextPosition, distance + 1, newKeysUsed))
            }
        }

        val keyToPaths = positionToPaths
            .filterKeys { position -> matrix[position].isLowerCase() }
            .mapKeys { (position) -> Key(position, matrix[position]) }
            .mapValues { (_, paths) ->
                Collections.unmodifiableSortedSet(paths.toSortedSet(compareBy { (_, distance) -> distance }))
            }

        return keyToPaths
    }

    private fun List<List<Char>>.mutate(mutator: (MutableList<MutableList<Char>>) -> Unit) =
        mapTo(mutableListOf()) { it.toMutableList() }
            .apply(mutator)
            .map { it.toList() }
}
