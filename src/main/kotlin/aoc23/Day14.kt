package aoc23

import AOCYear
import convertInputToCharArrayMatrix
import readInput
import transposed

class Day14 {
    enum class Direction { N, W, S, E }

    fun solve() {
        val rawInput = readInput("day17.txt", AOCYear.TwentyThree)
        val matrix = convertInputToCharArrayMatrix(rawInput)

        fun spinMatrix(matrix: Array<Array<Char>>) =
            Direction.entries.fold(matrix) { acc, direction -> getMatrixTilted(acc, direction) }

        fun performNMatrixSpins(matrix: Array<Array<Char>>, n: Int): Array<Array<Char>> {
            val cache = mutableMapOf<Int, Pair<Array<Array<Char>>, Int>>()
            return (1..n).fold(matrix) { currentMatrix, roundIndex ->
                val hash = currentMatrix.contentDeepHashCode()

                if (hash in cache) {
                    val cycleStartIndex = cache[hash]!!.second
                    val cycleSize = roundIndex - cycleStartIndex
                    val answerIndex = (n - cycleStartIndex) % cycleSize + cycleStartIndex

                    return cache.values.firstNotNullOf { (cachedMatrix, index) ->
                        cachedMatrix.takeIf { index == answerIndex }
                    }
                }

                val nextMatrix = spinMatrix(currentMatrix)
                cache[hash] = nextMatrix to roundIndex

                nextMatrix
            }
        }

        fun calculateMatrixScore(matrix: Array<Array<Char>>) = matrix.indices.sumOf { rowI ->
            matrix[rowI].count { it == 'O' } * (matrix.size - rowI)
        }

        val partOne = calculateMatrixScore(getMatrixTilted(matrix, Direction.N))
        val partTwo = calculateMatrixScore(performNMatrixSpins(matrix, 1000000000))

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }

    fun getMatrixTilted(matrix: Array<Array<Char>>, direction: Direction): Array<Array<Char>> {
        val transpositionSelector = { arr: Array<Array<Char>> ->
            if (direction in listOf(Direction.N, Direction.S)) arr.transposed() else arr
        }
        val rowOrderSelector = { space: String, rocks: String ->
            if (direction in listOf(Direction.N, Direction.W)) rocks + space else space + rocks
        }

        return transpositionSelector(matrix)
            .map { row ->
                row.joinToString("")
                    .split("#")
                    .joinToString("#") { rocksAndSpace ->
                        rocksAndSpace.partition { it == '.' }.let { (space, rocks) -> rowOrderSelector(space, rocks) }
                    }
                    .toCharArray()
                    .toTypedArray()
            }.toTypedArray()
            .run(transpositionSelector)
    }
}
