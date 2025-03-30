@file:Suppress("MemberVisibilityCanBePrivate")

package aoc23

import AOCAnswer
import AOCSolution
import AOCYear
import readInput
import toCharMatrix
import transposed

class Day14 : AOCSolution {
    enum class Direction { N, W, S, E }

    override fun solve(): AOCAnswer {
        val rawInput = readInput("day14.txt", AOCYear.TwentyThree)
        val matrix = rawInput.toCharMatrix()

        val partOne = calculateMatrixScore(getMatrixTilted(matrix, Direction.N))
        val partTwo = calculateMatrixScore(performNMatrixSpins(matrix, 1000000000))

        return AOCAnswer(partOne, partTwo)
    }

    fun spinMatrix(matrix: List<List<Char>>) =
        Direction.entries.fold(matrix) { acc, direction -> getMatrixTilted(acc, direction) }

    fun performNMatrixSpins(matrix: List<List<Char>>, n: Int): List<List<Char>> {
        val cache = mutableMapOf<List<List<Char>>, Pair<List<List<Char>>, Int>>()

        return (1..n).fold(matrix) { currentMatrix, roundIndex ->
            val cycleStartIndex = cache[currentMatrix]?.second

            if (cycleStartIndex != null) {
                val cycleSize = roundIndex - cycleStartIndex
                val answerIndex = (n - cycleStartIndex) % cycleSize + cycleStartIndex

                return cache.values.firstNotNullOf { (cachedMatrix, index) ->
                    cachedMatrix.takeIf { index == answerIndex }
                }
            }

            val nextMatrix = spinMatrix(currentMatrix)

            cache[currentMatrix] = nextMatrix to roundIndex

            nextMatrix
        }
    }

    fun calculateMatrixScore(matrix: List<List<Char>>) = matrix.indices.sumOf { rowI ->
        matrix[rowI].count { it == 'O' } * (matrix.size - rowI)
    }

    fun getMatrixTilted(matrix: List<List<Char>>, direction: Direction): List<List<Char>> {
        val transpositionSelector = { list: List<List<Char>> ->
            if (direction in listOf(Direction.N, Direction.S)) list.transposed() else list
        }
        val rowOrderSelector = { space: String, rocks: String ->
            if (direction in listOf(Direction.N, Direction.W)) rocks + space else space + rocks
        }

        val transposed = transpositionSelector(matrix)

        val transformed = transposed.map { row ->
            row.joinToString("")
                .split("#")
                .joinToString("#") { rocksAndSpace ->
                    val (space, rocks) = rocksAndSpace.partition { it == '.' }

                    rowOrderSelector(space, rocks)
                }.toList()
        }

        val result = transpositionSelector(transformed)
        return result
    }
}
