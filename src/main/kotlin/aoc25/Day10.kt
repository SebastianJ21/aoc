package aoc25

import AOCAnswer
import AOCSolution
import inputLines
import invertListMap
import mapToInt
import structures.IntList
import structures.toIntList
import transposed
import java.math.BigInteger
import kotlin.math.absoluteValue
import kotlin.math.min
import kotlin.math.sign

class Day10 : AOCSolution {

    override fun solve(): AOCAnswer {
        val inputLines = inputLines()

        val inputs = inputLines.map { line ->
            // [####] (2,3) (0,3) (1,3) (0,1,3) {34,24,10,51}
            val parts = line.split(" ")

            // [.####..#..]
            val array = parts.first().removeSurrounding("[", "]").map { if (it == '#') 1 else 0 }

            // (2,3) (0,3) (1,3) (0,1,3)
            val buttons = parts.drop(1).dropLast(1).map {
                it.removeSurrounding("(", ")").split(",").mapToInt()
            }

            // {3,5,4,7}
            val joltage = parts.last().removeSurrounding("{", "}").split(",").mapToInt()

            Triple(array, buttons, joltage)
        }

        val part1 = inputs.sumOf { (target, buttons) ->
            val initial = buttons
                .map { button -> IntList(target.size) { if (it in button) 1 else 0 } }
                .associateWith { 1 }

            generateSequence(initial) { current -> next(current, target) }
                .firstNotNullOf { createdArrayToCost -> createdArrayToCost[target] }
        }

        val partTwo = inputs.sumOf { (_, buttons, goal) ->
            val (normalizedMatrix, answers) = gauss(createMatrix(buttons, goal), goal)

            findMinAnswer(
                initialMatrix = normalizedMatrix,
                answers = answers,
                globalMaxAnswer = goal.sum(),
            )
        }

        return AOCAnswer(part1, partTwo)
    }

    private fun next(current: Map<IntList, Int>, target: List<Int>): Map<IntList, Int> {
        val newEntries = current.entries.flatMapIndexed { index, (buttonA, buttonACost) ->
            current.entries
                .drop(index + 1)
                .mapNotNull { (buttonB, buttonBCost) ->
                    val new = IntList(target.size) { index -> (buttonA[index] + buttonB[index]) % 2 }

                    when (new) {
                        target -> return mapOf(new to buttonBCost + buttonACost)
                        !in current -> new to (buttonACost + buttonBCost)
                        else -> null
                    }
                }
        }

        return current + newEntries.distinctBy { (createdArray) -> createdArray }
    }

    private class FreeParam(
        val colIndex: Int,
        val rowIndexes: IntList,
        val nextFixedRowIndexes: IntList,
        val nextMatrix: List<IntList>,
    )

    private fun findMinAnswer(
        initialMatrix: List<List<Int>>,
        answers: List<Int>,
        globalMaxAnswer: Int,
    ): Int {
        // Since we only use (and are interested in) whole numbers, our pivots can be non 1 (i.e., 3)
        val rowIDivisor = initialMatrix.mapIndexed { rowI, row ->
            val pivotValue = row[rowI]
            require(pivotValue >= 0)

            pivotValue.coerceAtLeast(1)
        }.toIntList()

        val initialFreeParams = initialMatrix.extractFreeParams()

        fun search(
            matrix: List<IntList>,
            answers: IntList,
            fixedRowI: IntList,
            freeParams: List<FreeParam>,
        ): Int? {
            if (freeParams.isEmpty()) {
                answers.forEachIndexed { index, answer ->
                    when {
                        answer < 0 -> return null
                        rowIDivisor[index] == 1 -> Unit
                        answer % rowIDivisor[index] != 0 -> return null
                    }
                }

                return answers.foldIndexed(0) { rowI, acc, answer -> acc + answer / rowIDivisor[rowI] }
            }

            val fixedAnswers = fixedRowI.sumOf {
                val answer = answers[it]
                if (answer < 0 || answer % rowIDivisor[it] != 0) return null

                answer / rowIDivisor[it]
            }

            answers.forEachIndexed { rowI, answer ->
                val isInvalid = answer < 0 || answer % rowIDivisor[rowI] != 0
                // Row has a negative (invalid) answer which cannot be changed in the future
                if (isInvalid && freeParams.none { state -> rowI in state.rowIndexes }) return null
            }

            val freeParam = freeParams.first()
            val nextFreeParams = freeParams.drop(1)

            val maxParam = globalMaxAnswer - fixedAnswers

            val minResult = (0..maxParam).fold(Int.MAX_VALUE) { min, selectedParam ->
                // There exist a number of total button presses that are lower than our `selectedParam`.
                // Since `selectedParam` is from a strictly increasing sequence, it implies no further possibilities
                if (min < selectedParam) return min

                // Generate new answers
                val newAnswers = answers.mutated { clonedAnswers ->
                    freeParam.rowIndexes.forEachInt { rowIndex ->
                        val answer = clonedAnswers[rowIndex]

                        clonedAnswers[rowIndex] = answer - selectedParam.times(matrix[rowIndex][freeParam.colIndex])
                    }
                }

                val result = search(
                    matrix = freeParam.nextMatrix,
                    answers = newAnswers,
                    fixedRowI = freeParam.nextFixedRowIndexes,
                    freeParams = nextFreeParams,
                )?.plus(selectedParam) ?: return@fold min

                if (result > min) return min

                min(result, min)
            }

            return minResult.takeIf { it != Int.MAX_VALUE }
        }

        val result = search(
            matrix = initialMatrix.map { it.toIntList() },
            answers = answers.toIntList(),
            fixedRowI = initialMatrix.fixedRowIndexes(),
            freeParams = initialFreeParams,
        )

        checkNotNull(result) { "Error in finding a solution for $initialMatrix with answers $answers" }

        return result
    }

    private fun gauss(matrix: List<List<Int>>, answers: List<Int>): Pair<List<List<Int>>, List<Int>> {
        // Obtain matrix | answers
        val augmented = matrix.zip(answers) { row, answer -> row + answer }

        val columns = matrix[0].indices
        val (reducedForm) = columns.foldIndexed(augmented to 0) { _, (current, rowI), colI ->
            // Select only from 'non-reduced' rows
            val availableRows = current.filter { row -> row.take(colI).all { it == 0 } }

            val selectedRow = availableRows
                // Prefer pivots with value of 1
                .find { row -> row[colI].absoluteValue == 1 }
                ?: availableRows.find { row -> row[colI] != 0 }
                ?: return@foldIndexed current to rowI

            val row = selectedRow.normalized()

            val preceding = current.take(rowI).normalizedBy(row)
            val following = current.drop(rowI).minusElement(selectedRow).normalizedBy(row)

            preceding.plusElement(row).plus(following) to rowI + 1
        }

        val (resultMatrix, resultAnswers) = reducedForm
            .filter { row -> row.any { it != 0 } }
            .transposed().run { dropLast(1).transposed() to last() }

        return resultMatrix to resultAnswers
    }

    private fun List<List<Int>>.normalizedBy(pivotRow: List<Int>): List<List<Int>> {
        val pivotIndex = pivotRow.indexOfFirst { it != 0 }
        if (pivotIndex == -1) return this

        val pivotValue = pivotRow[pivotIndex]
        check(pivotValue > 0) { "Only positive pivots pls" }

        return this.map { targetRow ->
            val targetValue = targetRow[pivotIndex]
            if (targetValue == 0) return@map targetRow

            val resultRow = targetRow.zip(pivotRow) { t, p -> t * pivotValue - p * targetValue }

            resultRow.normalized()
        }
    }

    // Ensures leading positive values and gcd
    private fun List<Int>.normalized(): List<Int> {
        if (all { it == 0 }) return this

        val gcd = gcd()
        val sign = first { it != 0 }.sign

        return map { value -> value * sign / gcd }
    }

    private fun createMatrix(buttons: List<List<Int>>, joltage: List<Int>): List<List<Int>> {
        return List(joltage.size) { rowI ->
            List(buttons.size) { colI ->
                if (rowI in buttons[colI]) 1 else 0
            }
        }
    }

    private fun List<List<Int>>.extractFreeParams(): List<FreeParam> {
        val rowIToFreeParamColIndexes = this.withIndex().associate { (rowI, row) ->
            check(row.take(rowI).all { it == 0 }) { "Matrix not properly normalized" }

            val isPivotRow = row[rowI] > 0
            val freeParamIndexes = row
                .mapIndexedNotNull { index, elem -> index.takeIf { elem != 0 } }
                .let { if (isPivotRow) it.drop(1) else it }

            rowI to freeParamIndexes
        }

        val freeParamColIndexToRowIndexes = invertListMap(rowIToFreeParamColIndexes)
        val freeParamColToRowPairs = freeParamColIndexToRowIndexes
            .toList()
            .sortedByDescending { (_, rowIndexes) -> rowIndexes.size }

        // Precompute the changes (selection of free params) of the matrix
        val matrixEvolution = freeParamColToRowPairs
            .runningFold(this) { lastMatrix, (colI) -> lastMatrix.map { row -> row.set(colI, 0) } }
            .drop(1) // Drop the initial matrix

        val freeParams = freeParamColToRowPairs.zip(matrixEvolution) { (colIndex, rowIndexes), matrix ->
            FreeParam(
                colIndex = colIndex,
                rowIndexes = rowIndexes.toIntList(),
                nextMatrix = matrix.map { it.toIntList() },
                nextFixedRowIndexes = matrix.fixedRowIndexes(),
            )
        }

        return freeParams
    }

    private fun List<List<Int>>.fixedRowIndexes() =
        this.mapIndexedNotNull { rowI, row -> rowI.takeIf { row.singleOrNull { it != 0 } == 1 } }.toIntList()

    private fun List<Int>.gcd(): Int = map { it.toBigInteger() }.reduce(BigInteger::gcd).intValueExact()

    private fun <T> List<T>.set(index: Int, element: T): List<T> {
        return toMutableList().apply { set(index, element) }
    }
}
