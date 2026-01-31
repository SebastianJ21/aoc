package aoc25

import AOCAnswer
import AOCSolution
import invertListMap
import mapToInt
import readInput
import transposed
import java.math.BigInteger
import kotlin.IndexOutOfBoundsException
import kotlin.math.absoluteValue
import kotlin.math.min

class Day10 : AOCSolution {

    override fun solve(): AOCAnswer {
        val rawInput = readInput("day10.txt", AOCYear.TwentyFive)
        val inputs = rawInput.map { line ->
            val parts = line.split(" ")

            // [.####..#..]
            val array = parts.first().drop(1).dropLast(1).map { if (it == '#') 1 else 0 }

            // (2,3) (0,3) (1,3) (0,1,3)
            val buttons = parts.drop(1).dropLast(1).map {
                it.removePrefix("(").removeSuffix(")").split(",").mapToInt()
            }

            // {3,5,4,7}
            val joltage = parts.last().drop(1).dropLast(1).split(",").mapToInt()

            Triple(array, buttons, joltage)
        }

        val part1 = inputs.sumOf { (target, buttons) ->
            val initial = buttons
                .map { button -> List(target.size) { if (it in button) 1 else 0 } }
                .associateWith { 1 }

            generateSequence(initial) { current -> next(current, target) }
                .firstNotNullOf { createdArrayToCost -> createdArrayToCost[target] }
        }

        val partTwo = inputs.sumOf { (_, buttons, goal) ->
            val (normalizedMatrix, answers) = gauss(createMatrix(buttons, goal), goal)

            findMinAnswer(
                initialMatrix = normalizedMatrix,
                answers = answers.toIntArray(),
                globalMaxAnswer = goal.sum(),
            )
        }

        return AOCAnswer(part1, partTwo)
    }

    private fun next(current: List<Pair<IntArray, Int>>, seen: Set<Int>, target: IntArray): List<Pair<IntArray, Int>> {
        val newEntries = current.flatMapIndexed { index, (buttonA, buttonACost) ->
            current // .asSequence()
                .drop(index + 1)
                .mapNotNull { (buttonB, buttonBCost) ->
                    val new = IntArray(target.size) { index -> (buttonA[index] + buttonB[index]) % 2 }

                    if (new.contentHashCode() in seen) return@mapNotNull null

                    if (new.contentEquals(target)) return listOf(target to buttonBCost + buttonACost)

                    // new to (buttonACost + buttonBCost)
                    new to (buttonACost + buttonBCost)
                }
        }

        return current + newEntries.distinctBy { (createdArray) -> createdArray.contentHashCode() }
    }

    private fun next(current: Map<List<Int>, Int>, target: List<Int>): Map<List<Int>, Int> {
        val newEntries = current.entries.flatMapIndexed { index, (buttonA, buttonACost) ->
            current.entries
                .drop(index + 1)
                .mapNotNull { (buttonB, buttonBCost) ->
                    val new = List(target.size) { index -> (buttonA[index] + buttonB[index]) % 2 }

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
        val rowIndexes: BooleanArray,
        val nextFixedRowIndexes: List<Int>,
        val nextMatrix: List<List<Int>>,
    )

    private fun findMinAnswer(initialMatrix: List<List<Int>>, answers: IntArray, globalMaxAnswer: Int): Int {
        // Since we only use (and are interested in) whole numbers, our pivots can be non 1 (i.e 3)
        val rowIDivisor = initialMatrix.mapIndexed { rowI, row ->
            val pivotValue = row[rowI]
            require(pivotValue >= 0)

            pivotValue.coerceAtLeast(1)
        }.toIntArray()

        val initialFreeParams = initialMatrix.extractFreeParams()

        fun search(
            matrix: List<List<Int>>,
            answers: IntArray,
            fixedRowI: List<Int>,
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
                if (isInvalid && freeParams.none { state -> state.rowIndexes[rowI] }) return null
            }

            val freeParam = freeParams.first()
            val nextFreeParams = freeParams.drop(1)

            val maxParam = globalMaxAnswer - fixedAnswers

            return (0..maxParam)
                .asSequence()
                .map { selectedParam ->
                    // Generate new answers
                    val nextAnswers = IntArray(answers.size) { index ->
                        val answer = answers[index]

                        if (!freeParam.rowIndexes[index]) return@IntArray answer

                        val paramValue = matrix[index][freeParam.colIndex]

                        answer - selectedParam.times(paramValue)
                    }
//                    val nextAnswers = answers.mapIndexed { index, answer ->
//                        if (!freeParam.rowIndexes[index]) return@mapIndexed answer
//
//                        val paramValue = matrix[index][freeParam.colIndex]
//
//                        answer - selectedParam.times(paramValue)
//                    }

                    selectedParam to nextAnswers
                }
                .map { (selectedParam, newAnswers) ->
                    search(
                        matrix = freeParam.nextMatrix,
                        answers = newAnswers,
                        fixedRowI = freeParam.nextFixedRowIndexes,
                        freeParams = nextFreeParams,
                    )?.plus(selectedParam)
                }
                .fold(null as Int?) { min, result ->
                    when {
                        result == null -> min
                        min == null -> result
                        result > min -> return min
                        else -> min(result, min)
                    }
                }
        }

        val result = search(
            matrix = initialMatrix,
            answers = answers,
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
            // Select only from 'non reduced' rows
            val selectedRow = current
                // Prefer pivots with value of 1
                .find { row -> row[colI].absoluteValue == 1 && row.take(colI).all { it == 0 } }
                ?: current.find { row -> row[colI] != 0 && row.take(colI).all { it == 0 } }
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
        val sign = if (first { it != 0 } < 0) -1 else 1

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
            // Behaves like a hash map of { index: Int -> hasFreeParam: Boolean } (but blazingly fast)
            val freeParamRowIndexes = BooleanArray(this.size) { it in rowIndexes }

            FreeParam(
                colIndex = colIndex,
                rowIndexes = freeParamRowIndexes,
                nextMatrix = matrix,
                nextFixedRowIndexes = matrix.fixedRowIndexes(),
            )
        }

        return freeParams
    }

    private fun List<List<Int>>.fixedRowIndexes() =
        this.mapIndexedNotNull { rowI, row -> rowI.takeIf { row.singleOrNull { it != 0 } == 1 } }

    private fun List<Int>.gcd() = map { it.toBigInteger() }.reduce(BigInteger::gcd).intValueExact()

    private fun <T> List<T>.set(index: Int, element: T): List<T> {
        if (index < 0 || index >= size) {
            throw IndexOutOfBoundsException("index: $index, size: $size")
        }

        return toMutableList().apply { set(index, element) }
    }
}
