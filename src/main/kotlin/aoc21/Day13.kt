package aoc21

import AOCYear
import alsoPrintLn
import readInput
import splitBy
import transposed

class Day13 {

    enum class FoldType {
        ROW,
        COLUMN,
    }

    fun solve() {
        val rawInput = readInput("day13.txt", AOCYear.TwentyOne)

        val (hashes, folds) = rawInput.splitBy { it.isEmpty() }

        val hashPositions = hashes.map { line ->
            val (col, row) = line.split(",")

            row.toInt() to col.toInt()
        }.toSet()

        val maxRow = hashPositions.maxOf { it.first }
        val maxCol = hashPositions.maxOf { it.second }

        val matrix = (0..maxRow).map { rowI ->
            (0..maxCol).map { colI ->
                if (rowI to colI in hashPositions) '#' else '.'
            }
        }

        val foldTypes = folds.map { if (it.contains("y")) FoldType.ROW else FoldType.COLUMN }

        val resultMatrix = foldTypes.fold(matrix) { currentMatrix, foldType ->
            foldMatrix(currentMatrix, foldType)
        }

        val partOne = foldMatrix(matrix, foldTypes.first()).sumOf { row -> row.count { it == '#' } }

        println("Part one: $partOne")
        println("Part two:")
        resultMatrix.forEach { it.alsoPrintLn { joinToString(" ") } }
    }

    fun foldMatrix(matrix: List<List<Char>>, foldType: FoldType): List<List<Char>> {
        val (upper, lower) = if (foldType == FoldType.ROW) {
            val upper = matrix.subList(0, matrix.size / 2)
            val lower = matrix.subList(matrix.size / 2 + 1, matrix.size)

            check(upper.size == lower.size) { "Cannot fold uneven matrices" }

            val lowerNormalized = lower.reversed()

            upper to lowerNormalized
        } else {
            val transposed = matrix.transposed()

            val upper = transposed.subList(0, transposed.size / 2).transposed()
            val lower = transposed.subList(transposed.size / 2 + 1, transposed.size).transposed()

            check(upper.size == lower.size) { "Cannot fold uneven matrices" }

            val lowerNormalized = lower.map { it.reversed() }

            upper to lowerNormalized
        }

        val merged = upper.mapIndexed { rowI, row ->
            row.mapIndexed { colI, value ->
                if (value == '#' || lower[rowI][colI] == '#') '#' else value
            }
        }

        return merged
    }
}
