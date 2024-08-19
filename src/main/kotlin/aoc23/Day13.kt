@file:Suppress("MemberVisibilityCanBePrivate")

package aoc23

import AOCYear
import convertInputToCharMatrix
import readInput
import splitBy
import transposed

class Day13 {
    fun solve() {
        val rawInput = readInput("day13.txt", AOCYear.TwentyThree)

        val rawMatrices = rawInput.splitBy { isEmpty() }

        val indexToResultPartOne = rawMatrices
            .mapIndexed { index, matrix -> index to matrix.calcNumber() }
            .toMap()

        val partOne = indexToResultPartOne.values.sumOf { it.eval() }

        val partTwo = rawMatrices.mapIndexed { index, matrix ->
            val smudges = matrix.findRowSmudge() + matrix.findColSmudge()

            val fixedMatrices = smudges.map { (smudgeRow, smudgeCol) ->
                matrix.mapIndexed { rowI, row ->
                    if (rowI == smudgeRow) {
                        val new = row[smudgeCol].getOpposite()
                        row.replaceRange(smudgeCol, smudgeCol + 1, new)
                    } else {
                        row
                    }
                }
            }

            val chosenIndicesPartOne = indexToResultPartOne[index]!!
            fixedMatrices.first().calcNumber(chosenIndicesPartOne).eval()
        }.sum()

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }

    private fun Pair<Int?, Int?>.eval() = first?.let { (it + 1) * 100 } ?: (second!! + 1)

    fun Char.getOpposite() = when (this) {
        '#' -> "."
        '.' -> "#"
        else -> error("Illegal char")
    }

    fun List<List<Char>>.potentialSymmetryStart(errorMargin: Int = 0) = this
        .toList()
        .zipWithNext()
        .mapIndexedNotNull { index, (rowA, rowB) ->
            index.takeIf {
                rowA.zip(rowB).count { (a, b) -> a != b } <= errorMargin
            }
        }

    fun List<String>.findRowSmudge(): List<Pair<Int, Int>> {
        val matrix = convertInputToCharMatrix(this)

        val rowIndex = matrix
            .potentialSymmetryStart(1)
            .mapNotNull { rowI ->
                matrix.indices.partition { it <= rowI }.let { (allLeft, allRight) ->
                    val symmetrical = allLeft.reversed().zip(allRight)
                    symmetrical.flatMap { (leftI, rightI) ->
                        matrix[leftI].zip(matrix[rightI])
                            .mapIndexedNotNull { index, (a, b) ->
                                (leftI to rightI to index)
                                    .takeIf { a != b }
                            }
                    }.takeIf { it.size == 1 }
                }
            }.flatten()

        return rowIndex.flatMap { (rows, col) -> listOf(rows.first to col, rows.second to col) }
    }

    fun List<String>.findColSmudge(): List<Pair<Int, Int>> {
        val transposed = convertInputToCharMatrix(this).transposed()

        val colIndex = transposed
            .potentialSymmetryStart(1)
            .mapNotNull { rowI ->
                transposed.indices.partition { it <= rowI }.let { (allLeft, allRight) ->
                    val symmetrical = allLeft.reversed().zip(allRight)
                    symmetrical.flatMap { (leftI, rightI) ->
                        transposed[leftI].zip(transposed[rightI])
                            .mapIndexedNotNull { index, (a, b) ->
                                (index to (leftI to rightI))
                                    .takeIf { a != b }
                            }
                    }.takeIf { it.size == 1 }
                }
            }.flatten()

        return colIndex.flatMap { (row, cols) -> listOf(row to cols.first, row to cols.second) }
    }

    fun List<String>.calcNumber(ignoreIndex: Pair<Int?, Int?>? = null): Pair<Int?, Int?> {
        val matrix = convertInputToCharMatrix(this)

        val rowIndices = matrix
            .potentialSymmetryStart()
            .filter { rowI ->
                matrix.indices.partition { it <= rowI }.let { (allLeft, allRight) ->
                    allLeft.reversed().zip(allRight)
                        .all { (leftI, rightI) ->
                            matrix[leftI] == matrix[rightI]
                        }
                }
            }

        val rowIndex = rowIndices.firstOrNull { ignoreIndex?.first != it }

        if (rowIndex != null) return rowIndex to null

        val transposed = matrix.transposed()

        val colIndices = transposed
            .potentialSymmetryStart()
            .filter { colI ->
                transposed.indices.partition { it <= colI }.let { (allLeft, allRight) ->
                    allLeft.reversed().zip(allRight)
                        .all { (leftI, rightI) ->
                            transposed[leftI] == transposed[rightI]
                        }
                }
            }

        val colIndex = colIndices.firstOrNull { ignoreIndex?.second != it }

        return null to colIndex!!
    }
}
