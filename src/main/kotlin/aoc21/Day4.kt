package aoc21

import mapToInt
import readInput
import splitBy
import transposed

class Day4 {
    fun solve() {
        val rawInput = readInput("day4.txt", AOCYear.TwentyOne)

        val drawnNumbers = rawInput.first().split(",").mapToInt()

        val matrices = rawInput.drop(2).splitBy({ it.isEmpty() }) { line ->
            line.trimStart()
                .split("  ", " ")
                .mapToInt()
        }

        val zippedMatrices = matrices.zip(matrices.map { it.transposed() }) { base, transposed ->
            listOf(base, transposed)
        }.mapIndexed { index, matrixPair -> matrixPair to index }

        val (_, scores) =
            drawnNumbers.foldIndexed(zippedMatrices to listOf<Int>()) { index, (matrices, scores), currentNumber ->
                val currentNumbers = drawnNumbers.subList(0, index + 1).toSet()

                val matched = matrices.mapNotNull { (matrixPair, index) ->
                    matrixPair.firstOrNull { it.matchWithNumbers(currentNumbers) }?.let { it to index }
                }

                if (matched.isEmpty()) {
                    matrices to scores
                } else {
                    val newScores = matched.map { (matrix, _) ->
                        matrix.sumOfNotInNumbers(currentNumbers) * currentNumber
                    }

                    val matrixIdsToRemove = matched.map { (_, index) -> index }

                    matrices.filter { (_, index) -> index !in matrixIdsToRemove } to scores + newScores
                }
            }

        println("Part one: " + scores.first())
        println("Part two: " + scores.last())
    }

    private fun List<List<Int>>.matchWithNumbers(numbers: Set<Int>) = any { row -> row.all { it in numbers } }

    private fun List<List<Int>>.sumOfNotInNumbers(numbers: Set<Int>) = sumOf { row ->
        row.sumOf { if (it in numbers) 0 else it }
    }
}
