package aoc22

import Position
import convertInputToArrayMatrix
import executeBlockOnEach
import getOrNull
import plus
import readInput
import kotlin.math.abs
import kotlin.math.min

typealias TileMatrix = Array<Array<List<Day24.TileType>>>

class Day24 {
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

    fun solve() {
        val rawInput = readInput("day24.txt")

        val matrix = convertInputToArrayMatrix(rawInput) {
            val tileType = when (this) {
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

        println("Part One: ${partOne.first}")
        println("Part Two: $partTwo")
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
        val firstHash = fromMatrix.contentDeepHashCode()

        val sequence = generateSequence(fromMatrix) { matrix ->
            matrix.constructNext().takeIf { it.contentDeepHashCode() != firstHash }
        }

        return sequence.toList()
    }

    private fun TileMatrix.constructNext(): TileMatrix {
        val rowSize = first().size
        val colSize = size

        val positionToTiles = buildList {
            executeBlockOnEach { tiles, (rowIndex, colIndex) ->
                tiles.forEach { tileType ->
                    when (tileType) {
                        TileType.LEFT -> {
                            if (colIndex == 1) {
                                add(Position(rowIndex, rowSize - 2) to TileType.LEFT)
                            } else {
                                add(Position(rowIndex, colIndex - 1) to TileType.LEFT)
                            }
                        }
                        TileType.RIGHT -> {
                            if (colIndex == rowSize - 2) {
                                add(Position(rowIndex, 1) to TileType.RIGHT)
                            } else {
                                add(Position(rowIndex, colIndex + 1) to TileType.RIGHT)
                            }
                        }
                        TileType.UP -> {
                            if (rowIndex == 1) {
                                add(Position(colSize - 2, colIndex) to TileType.UP)
                            } else {
                                add(Position(rowIndex - 1, colIndex) to TileType.UP)
                            }
                        }

                        TileType.DOWN -> {
                            if (rowIndex == colSize - 2) {
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
        }.groupBy({ it.first }, { it.second })

        return Array(colSize) { row ->
            Array(rowSize) { col ->
                positionToTiles[row to col] ?: emptyList()
            }
        }
    }
}
