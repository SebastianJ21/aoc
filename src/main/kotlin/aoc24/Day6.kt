package aoc24

import AOCAnswer
import AOCSolution
import Direction
import Position
import applyDirection
import getOrNull
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import positionsOf
import readInput
import toMatrix
import kotlin.math.max

class Day6 : AOCSolution {

    enum class Tile { EMPTY, OBSTRUCTION, GUARD }

    val up: Direction = -1 to 0
    val down: Direction = 1 to 0
    val left: Direction = 0 to -1
    val right: Direction = 0 to 1

    val directions = listOf(up, right, down, left)

    override fun solve(): AOCAnswer {
        val rawInput = readInput("day6.txt", AOCYear.TwentyFour)

        val matrix = rawInput.toMatrix {
            when (it) {
                '.' -> Tile.EMPTY
                '#' -> Tile.OBSTRUCTION
                '^' -> Tile.GUARD
                else -> error("Illegal char: $it")
            }
        }

        val initialPosition = matrix.positionsOf { it == Tile.GUARD }.first()
        val initialDirection = up

        val movementSequence = makeMovementSequence(matrix, initialPosition, initialDirection).toList()
        val distinctPositions = movementSequence.map { (position) -> position }.distinct()

        val partOne = distinctPositions.size

        val persistentMatrix = matrix.map { it.toPersistentList() }.toPersistentList()

        val partTwo = distinctPositions.withIndex().count { (positionIndex, obstructPosition) ->
            val (startPosition, startDirection) = movementSequence[max(positionIndex - 1, 0)]

            val newMatrix = persistentMatrix.replaceTile(obstructPosition, Tile.OBSTRUCTION)
            val movementWithObstruction = makeMovementSequence(newMatrix, startPosition, startDirection)

            val seenLocal = hashSetOf<Pair<Position, Direction>>()
            val isLoop = movementWithObstruction.any { value -> !seenLocal.add(value) }

            isLoop
        }

        return AOCAnswer(partOne, partTwo)
    }

    private fun makeMovementSequence(
        matrix: List<List<Tile>>,
        initialPosition: Position,
        initialDirection: Direction,
    ): Sequence<Pair<Position, Direction>> {
        val movementSequence = generateSequence(initialPosition to initialDirection) { (position, direction) ->
            val newPosition = position.applyDirection(direction)

            val tile = matrix.getOrNull(newPosition)

            when (tile) {
                null -> return@generateSequence null

                Tile.OBSTRUCTION -> {
                    val newDirectionIndex = directions.indexOf(direction).inc() % directions.size
                    val newDirection = directions[newDirectionIndex]

                    position to newDirection
                }

                Tile.EMPTY, Tile.GUARD -> newPosition to direction
            }
        }

        return movementSequence
    }

    private fun PersistentList<PersistentList<Tile>>.replaceTile(
        position: Position,
        tile: Tile,
    ): PersistentList<PersistentList<Tile>> = this.set(position.first, this[position.first].set(position.second, tile))
}
