@file:Suppress("MemberVisibilityCanBePrivate")

package aoc23

import Position
import applyDirection
import getOrNull
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentHashSetOf
import readInput
import toCharMatrix

class Day23 {
    val up = -1 to 0
    val down = 1 to 0
    val left = 0 to -1
    val right = 0 to 1

    private val directions = listOf(up, down, left, right)

    private val slopeToDirection = mapOf(
        '^' to up,
        '>' to right,
        '<' to left,
        'v' to down,
    )

    fun solve() {
        val rawInput = readInput("day23.txt", AOCYear.TwentyThree)
        val matrix = rawInput.toCharMatrix()

        val (start, end) = Position(0, 1) to (matrix.lastIndex to matrix.last().lastIndex - 1)

        val matrixWithoutSlopes = matrix.map { row ->
            row.map { value ->
                if (value in slopeToDirection) '.' else value
            }
        }

        val toggles = matrixWithoutSlopes.flatMapIndexed { rowI, row ->
            row.mapIndexedNotNull { colI, value ->
                if (value != '.') return@mapIndexedNotNull null

                val position = rowI to colI
                val possibleDirections = directions.mapNotNull { direction ->
                    val check = position.applyDirection(direction)

                    check.takeIf { matrixWithoutSlopes.getOrNull(it) == '.' }
                }

                position to possibleDirections
            }.filter { (_, togglePaths) -> togglePaths.size > 2 }
        }.toMap() + (start to getNextPositions(start, matrix)) + (end to getNextPositions(end, matrix))

        val togglesPartOne = toggles.mapValues { (toggle, _) ->
            getNextPositions(toggle, matrix)
        }
        val togglePartOnePaths = getAllTogglePaths(matrix, togglesPartOne)
        val togglePaths = getAllTogglePaths(matrixWithoutSlopes, toggles)

        val partOne = findLongestToggleSequence(start, end, togglePartOnePaths)
        val partTwo = findLongestToggleSequence(start, end, togglePaths)

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }

    fun findLongestToggleSequence(
        start: Position,
        end: Position,
        togglePaths: Map<Position, List<Pair<Position, Int>>>,
    ): Int {
        val endPrecondition = togglePaths.getValue(end).map { it.first }.toSet().ifEmpty { setOf(end) }

        fun dfs(position: Position, sum: Int, seen: PersistentSet<Position>): Int {
            when {
                position in seen -> return 0
                position == end -> return sum
                seen.containsAll(endPrecondition) -> return 0
            }

            val localSeen = seen.add(position)
            val result = togglePaths[position]?.maxOf { (nextToggle, pathSize) ->
                dfs(nextToggle, sum + pathSize, localSeen)
            } ?: 0

            return result
        }

        return dfs(start, 0, persistentHashSetOf())
    }

    fun getAllTogglePaths(
        matrix: List<List<Char>>,
        toggles: Map<Position, List<Position>>,
    ): Map<Position, List<Pair<Position, Int>>> = toggles.mapValues { (toggle, togglePaths) ->
        val reachableToggles = togglePaths
            .map { path -> findNextTogglePath(path, toggles, matrix, toggle) }
            .filter { it.isNotEmpty() }

        reachableToggles.map { path -> path.last() to path.size }
    }

    fun getNextPositions(position: Position, matrix: List<List<Char>>, previous: Position? = null) =
        directions.mapNotNull { direction ->
            val newPosition = position.applyDirection(direction)
            val value = matrix.getOrNull(newPosition)

            if (value == null || newPosition == previous) return@mapNotNull null

            newPosition.takeIf { value == '.' || slopeToDirection[value] == direction }
        }

    fun findNextTogglePath(
        position: Position,
        toggles: Map<Position, List<Position>>,
        matrix: List<List<Char>>,
        startingFromToggle: Position? = null,
    ): List<Position> {
        val movementSequence = generateSequence(startingFromToggle to position) { (previous, current) ->
            val nextPositions = getNextPositions(current, matrix, previous)

            if (current in toggles || nextPositions.isEmpty()) return@generateSequence null

            current to nextPositions.single()
        }

        val movement = movementSequence.map { it.second }.toList()

        return if (movement.last() !in toggles) emptyList() else movement
    }
}
