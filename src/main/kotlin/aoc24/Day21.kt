package aoc24

import AOCAnswer
import AOCSolution
import Direction
import Position
import applyDirection
import at
import inputLines
import kotlinx.collections.immutable.persistentListOf
import positionsOf
import splitBy
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.isNotEmpty
import kotlin.collections.plus
import kotlin.collections.zipWithNext
import kotlin.math.abs

class Day21 : AOCSolution {

    private val up = -1 at 0
    private val down = 1 at 0
    private val left = 0 at -1
    private val right = 0 at 1

    private val directions = listOf(up, left, down, right)

    override fun solve(): AOCAnswer {
        val inputLines = inputLines()

        val codeNumToPositions = inputLines
            .associateWith { code -> code.map { elem -> numpad.positionsOf { it == elem }.single() } }
            .mapKeys { (code) -> code.filter { it.isDigit() }.toLong() }

        val startPosition = numpad.positionsOf { it == 'A' }.single()

        val partOne = codeNumToPositions
            .mapValues { (_, codePositions) -> resolve(listOf(startPosition) + codePositions, 2) }
            .entries
            .sumOf { (code, resultSequenceSize) -> code * resultSequenceSize }

        val partTwo = codeNumToPositions
            .mapValues { (_, codePositions) -> resolve(listOf(startPosition) + codePositions, 25) }
            .entries
            .sumOf { (code, resultSequenceSize) -> code * resultSequenceSize }

        return AOCAnswer(partOne, partTwo)
    }

    private val chunkSizeToCache = hashMapOf<Int, HashMap<List<Position>, Long>>()

    private fun resolve(codePositions: List<Position>, movepadCount: Int): Long {
        val initialMovepadPositions = initialMovepadPositions(codePositions)
        val movepadSections = initialMovepadPositions.splitBy { it == movepadActivate }
            .filter { it.isNotEmpty() }
            .map { it + movepadActivate }

        val firstHalfCount = Math.floorDiv(movepadCount, 2)
        val secondHalfCount = Math.ceilDiv(movepadCount, 2)

        val inHalf = movepadSections.flatMap { expandMovepads(it, firstHalfCount) }

        val cache = chunkSizeToCache.getOrPut(secondHalfCount) { hashMapOf() }

        val shortestSequenceLength = inHalf.sumOf { chunk ->
            cache.getOrPut(chunk) { expandMovepadsToScore(chunk, secondHalfCount).toLong() }
        }

        return shortestSequenceLength
    }

    private val startToEndToBestPath = hashMapOf<Position, HashMap<Position, List<Position>>>()

    private fun toMovepadPositions(start: Position, end: Position): List<Position> {
        startToEndToBestPath[start]?.get(end)?.let { return it }

        check(start in movepadAllPositions && end in movepadAllPositions)

        val possiblePaths = allPaths(start, end, movepadAllPositions)
            .map { path -> path.toDirections().toMovepadPositions() }

        val bestPath = possiblePaths.minBy { possiblePath -> pathCost(possiblePath, 5) }

        startToEndToBestPath.getOrPut(start) { hashMapOf() }[end] = bestPath

        return bestPath
    }

    private fun pathCost(path: List<Position>, n: Int): Int {
        val possiblePaths = listOf(movepadInitial).plus(path)
            .zipWithNext { from, to -> allPaths(from, to, movepadAllPositions) }
            .flatMap { paths -> paths.map { path -> path.toDirections().toMovepadPositions() } }

        if (n == 0) return possiblePaths.sumOf { it.size }

        return possiblePaths.sumOf { pathCost(it, n - 1) }
    }

    private fun allPaths(start: Position, end: Position, possible: List<Position>): List<List<Position>> {
        val queue = ArrayDeque(listOf(persistentListOf(start)))
        val paths = mutableListOf<List<Position>>()

        while (queue.isNotEmpty()) {
            val currentPath = queue.removeFirst()
            val lastPosition = currentPath.last()

            if (lastPosition == end) {
                paths.add(currentPath)
                continue
            }

            val currentDistance = distance(lastPosition, end)

            directions.forEach { direction ->
                val nextPosition = lastPosition.applyDirection(direction)

                if (distance(nextPosition, end) >= currentDistance || nextPosition !in possible) return@forEach

                queue.add(currentPath.add(nextPosition))
            }
        }

        return paths
    }

    // +---+---+---+
    // | 7 | 8 | 9 |
    // +---+---+---+
    // | 4 | 5 | 6 |
    // +---+---+---+
    // | 1 | 2 | 3 |
    // +---+---+---+
    //    | 0 | A |
    //    +---+---+
    private val numpad = listOf(
        listOf('7', '8', '9'),
        listOf('4', '5', '6'),
        listOf('1', '2', '3'),
        listOf(' ', '0', 'A'),
    )

    private val numpadAllPositions = numpad.positionsOf { !it.isWhitespace() }.toList()

    /*
            +---+---+
            | ^ | A |
        +---+---+---+
        | < | v | > |
        +---+---+---+
     */
    private val movePad = listOf(
        listOf(' ', '^', 'A'),
        listOf('<', 'v', '>'),
    )

    private val movepadActivate = movePad.positionsOf { it == 'A' }.single()
    private val movepadInitial = movepadActivate
    private val movepadAllPositions = movePad.positionsOf { !it.isWhitespace() }.toList()

    private fun expandMovepads(movepadPositions: List<Position>, n: Int): List<List<Position>> {
        return when (n) {
            0 -> listOf(movepadPositions)
            1 -> expandMovepad(movepadPositions)
            else -> expandMovepad(movepadPositions).flatMap { expandMovepads(it, n - 1) }
        }
    }

    private fun expandMovepadsToScore(movepadPositions: List<Position>, n: Int): Int {
        return when (n) {
            0 -> movepadPositions.size
            else -> expandMovepad(movepadPositions).sumOf { expandMovepadsToScore(it, n - 1) }
        }
    }

    private fun expandMovepad(movepadPositions: List<Position>): List<List<Position>> {
        return buildList(movepadAllPositions.size.dec() * movepadPositions.size) {
            add(toMovepadPositions(movepadInitial, movepadPositions.first()))
            addAll(movepadPositions.zipWithNext { start, end -> toMovepadPositions(start, end) })
        }
    }

    private fun List<Position>.toDirections(): List<Direction> =
        zipWithNext { a, b -> b - a }.onEach { check(it in directions) }

    private fun List<Direction>.toMovepadPositions(): List<Position> = map { it.toMovepadPosition() } + movepadActivate

    private fun Direction.toMovepadPosition() = when (this) {
        up -> 0 at 1
        down -> 1 at 1
        left -> 1 at 0
        right -> 1 at 2
        else -> error("Only Direction can converted to Movepad position. Expected: $directions, got: $this")
    }

    private fun initialMovepadPositions(codePositions: List<Position>): List<Position> {
        val path = codePositions.zipWithNext { start, end -> allPaths(start, end, numpadAllPositions) }
            .map { paths -> paths.map { path -> path.toDirections().toMovepadPositions() } }
            .flatMap { movepadPositionPaths ->
                movepadPositionPaths.minBy { movepadPositions -> expandMovepadsToScore(movepadPositions, 5) }
            }

        return path
    }

    private fun distance(a: Position, b: Position) = abs(a.first - b.first) + abs(a.second - b.second)

    private operator fun Position.minus(other: Position) = Position(first - other.first, second - other.second)
}
