package aoc22

import mapToLong
import readInput
import kotlin.math.absoluteValue

private const val DECRYPTION_KEY = 811589153L

class Day20 {
    data class NumberWrapper(
        val id: Int,
        val value: Long,
        val moduloValue: Int,
    )

    fun solve() {
        val input = readInput("day20.txt").mapToLong()

        val wrappedNumbers = input.mapIndexed { index, value ->
            NumberWrapper(
                id = index,
                value = value,
                moduloValue = (value % input.lastIndex).absoluteValue.toInt(),
            )
        }

        val wrappedNumbersPartTwo = wrappedNumbers.map {
            val newValue = it.value * DECRYPTION_KEY
            NumberWrapper(
                id = it.id,
                value = newValue,
                moduloValue = (newValue % input.lastIndex).absoluteValue.toInt(),
            )
        }

        val numberIndices = input.indices.toList()
        val valuesPartOne = wrappedNumbers.map { it.moduloValue }

        val partOneMix = input.foldIndexed(numberIndices) { index, indices, originalNum ->
            if (originalNum >= 0) {
                performMixForward(indices, index, valuesPartOne)
            } else {
                performMixBackward(indices, index, valuesPartOne)
            }
        }

        val valuesPartTwo = wrappedNumbersPartTwo.map { it.moduloValue }

        val partTwoMix = (1..10).fold(numberIndices) { acc, _ ->
            input.foldIndexed(acc) { index, indices, originalNum ->
                if (originalNum >= 0) {
                    performMixForward(indices, index, valuesPartTwo)
                } else {
                    performMixBackward(indices, index, valuesPartTwo)
                }
            }
        }

        fun calculateResult(mixedNums: List<Int>, wrappedNums: List<NumberWrapper>): Long {
            val resultMap = mixedNums.mapIndexed { originalIndex, newIndex ->
                wrappedNums[originalIndex] to newIndex
            }.toMap()

            val invertedResultMap = resultMap.entries.associate { (key, value) -> value to key }

            val (_, zeroIndex) = resultMap.entries.first { (number) -> number.value == 0L }

            return (1..3).map { (zeroIndex + (1000 * it)) % resultMap.size }
                .sumOf { invertedResultMap.getValue(it).value }
        }

        val partOne = calculateResult(partOneMix, wrappedNumbers)
        val partTwo = calculateResult(partTwoMix, wrappedNumbersPartTwo)

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }

    private fun performMixForward(
        numberIndices: List<Int>,
        originalNumIndex: Int,
        numberValues: List<Int>,
    ): List<Int> {
        val value = numberValues[originalNumIndex]

        val currentNumberIndex = numberIndices[originalNumIndex]

        val newNumberIdx = (currentNumberIndex + value) % numberIndices.size

        return if (newNumberIdx >= currentNumberIndex) {
            val updateRange = currentNumberIndex..newNumberIdx

            numberIndices.map { currentIndex ->
                when (currentIndex) {
                    currentNumberIndex -> newNumberIdx
                    in updateRange -> currentIndex - 1
                    else -> currentIndex
                }
            }
        } else {
            val updateRange1 = currentNumberIndex until numberIndices.size
            val updateRange2 = 1..newNumberIdx

            numberIndices.map { currentIndex ->
                when (currentIndex) {
                    currentNumberIndex -> newNumberIdx
                    in updateRange1 -> currentIndex - 1
                    in updateRange2 -> currentIndex - 1
                    0 -> updateRange1.last
                    else -> currentIndex
                }
            }
        }
    }

    private fun performMixBackward(
        numberIndices: List<Int>,
        originalNumIndex: Int,
        numberValues: List<Int>,
    ): List<Int> {
        val value = numberValues[originalNumIndex]
        val currentNumberIndex = numberIndices[originalNumIndex]

        val newNumberIdx = (currentNumberIndex - value).let {
            if (it < 0) numberIndices.size + it else it
        }

        return if (newNumberIdx <= currentNumberIndex) {
            val updateRange = newNumberIdx..currentNumberIndex

            numberIndices.map { currentIndex ->
                when (currentIndex) {
                    currentNumberIndex -> newNumberIdx
                    in updateRange -> currentIndex + 1
                    else -> currentIndex
                }
            }
        } else {
            val updateRange1 = 0..currentNumberIndex
            val updateRange2 = newNumberIdx until numberIndices.lastIndex

            numberIndices.map { currentIndex ->
                when (currentIndex) {
                    currentNumberIndex -> newNumberIdx
                    in updateRange1 -> currentIndex + 1
                    in updateRange2 -> currentIndex + 1
                    numberIndices.lastIndex -> 0
                    else -> currentIndex
                }
            }
        }
    }
}
