package aoc22

import AOCAnswer
import AOCSolution
import Position
import getOrNull
import plus
import readInput
import toMatrix
import kotlin.math.abs
import kotlin.math.min

typealias TileMatrix = List<List<List<Day24.TileType>>>

class Day24 : AOCSolution {
    enum class TileType {
        UP,
        DOWN,
        LEFT,
        RIGHT,
        WALL,
    }

    val up = -1 to 0
    val down = 1 to 0
    val left = 0 to -1
    val right = 0 to 1
    val wait = 0 to 0

    private val moves = listOf(up, down, left, right, wait)

    override fun solve(): AOCAnswer {
        val rawInput = readInput("day24.txt")

        val matrix = rawInput.toMatrix { value ->
            val tileType = when (value) {
                '#' -> TileType.WALL
                '>' -> TileType.RIGHT
                '<' -> TileType.LEFT
                '^' -> TileType.UP
                'v' -> TileType.DOWN
                '.' -> null
                else -> error("Illegal character in input: $this")
            }

            listOfNotNull(tileType)
        }
        val matrixSequence = constructMatrixSequence(matrix)

        val start = 0 to 1
        val end = 21 to 150

        val partOne = findShortestPath(0, matrixSequence, start, end)

        val partTwo = partOne.let { (pathLength, matrixIndex) ->
            val (pathBackLength, backMatrixIndex) = findShortestPath(matrixIndex, matrixSequence, end, start)
            val (finalPathLength, _) = findShortestPath(backMatrixIndex, matrixSequence, start, end)

            pathLength + pathBackLength + finalPathLength
        }

        return AOCAnswer(partOne.first, partTwo)
    }

    private fun manhattanDistance(a: Position, b: Position) = abs(a.first - b.first) + abs(a.second - b.second)

    private fun findShortestPath(
        onMatrixIndex: Int,
        matrixSequence: List<TileMatrix>,
        start: Position,
        destination: Position,
    ): Pair<Int, Int> {
        val cache = matrixSequence.map { hashMapOf<Position, Int>() }

        var best = Int.MAX_VALUE

        fun search(matrixIndex: Int, position: Position, round: Int): Pair<Int, Int>? {
            when {
                position == destination -> {
                    best = min(best, round)
                    return round to matrixIndex
                }
                (cache[matrixIndex][position] ?: Int.MAX_VALUE) <= round -> return null
                round + manhattanDistance(position, destination) >= best -> return null
            }

            cache[matrixIndex][position] = round

            val nextMatrixIndex = (matrixIndex + 1) % matrixSequence.size
            val nextMatrix = matrixSequence[nextMatrixIndex]

            return moves
                .map { position + it }
                .filter { nextMatrix.getOrNull(it)?.isEmpty() == true }
                .mapNotNull { search(nextMatrixIndex, it, round + 1) }
                .minByOrNull { it.first }
        }

        return search(onMatrixIndex, start, 0) ?: error("A path does not exist!")
    }

    private fun constructMatrixSequence(fromMatrix: TileMatrix): List<TileMatrix> {
        val firstHash = fromMatrix.hashCode()

        val sequence = generateSequence(fromMatrix) { matrix ->
            matrix.constructNext().takeIf { it.hashCode() != firstHash }
        }

        return sequence.toList()
    }

    private fun TileMatrix.constructNext(): TileMatrix {
        val lastRowIndex = first().lastIndex
        val lastColIndex = lastIndex

        val matrix = this

        val positionToTiles = buildList {
            matrix.forEachIndexed { rowIndex, row ->
                row.forEachIndexed { colIndex, tiles ->
                    tiles.forEach { tileType ->
                        when (tileType) {
                            TileType.LEFT -> {
                                if (colIndex == 1) {
                                    add(Position(rowIndex, lastRowIndex - 1) to TileType.LEFT)
                                } else {
                                    add(Position(rowIndex, colIndex - 1) to TileType.LEFT)
                                }
                            }
                            TileType.RIGHT -> {
                                if (colIndex == lastRowIndex - 1) {
                                    add(Position(rowIndex, 1) to TileType.RIGHT)
                                } else {
                                    add(Position(rowIndex, colIndex + 1) to TileType.RIGHT)
                                }
                            }
                            TileType.UP -> {
                                if (rowIndex == 1) {
                                    add(Position(lastColIndex - 1, colIndex) to TileType.UP)
                                } else {
                                    add(Position(rowIndex - 1, colIndex) to TileType.UP)
                                }
                            }

                            TileType.DOWN -> {
                                if (rowIndex == lastColIndex - 1) {
                                    add(Position(1, colIndex) to TileType.DOWN)
                                } else {
                                    add(Position(rowIndex + 1, colIndex) to TileType.DOWN)
                                }
                            }

                            TileType.WALL -> {
                                add(Position(rowIndex, colIndex) to TileType.WALL)
                            }
                        }
                    }
                }
            }
        }.groupBy({ it.first }, { it.second })

        return mapIndexed { rowI, row ->
            List(row.size) { colI ->
                positionToTiles[rowI to colI] ?: emptyList()
            }
        }
    }
}
