package aoc20

import AOCYear
import readInput
import splitBy

class Day14 {

    fun solve() {
        val rawInput = readInput("day14.txt", AOCYear.Twenty)

        val paddedInput = rawInput.reversed().flatMap { line ->
            if (line.contains("mask")) {
                listOf(line, "")
            } else {
                listOf(line)
            }
        }.reversed().dropWhile { it.isEmpty() }

        val groups = paddedInput
            .splitBy { isEmpty() }
            .associate { groupLines ->
                val (_, mask) = groupLines.first().split(" = ")

                val values = groupLines.drop(1).map { memoryLine ->
                    val (rawMemory, rawValue) = memoryLine.split(" = ")

                    val memory = rawMemory.dropWhile { !it.isDigit() }.takeWhile { it.isDigit() }.toInt()
                    val value = rawValue.toInt().toString(2).padStart(mask.length, '0')

                    memory to value
                }

                mask to values
            }

        val resultMap = buildMap {
            groups.forEach { (mask, values) ->
                values.forEach { (address, value) ->

                    val resultValue = mask.zip(value) { maskChar, valueChar ->
                        when (maskChar) {
                            'X' -> valueChar
                            else -> maskChar
                        }
                    }.joinToString("")

                    put(address, resultValue)
                }
            }
        }

        val partOne = resultMap.values.sumOf { it.toLong(2) }

        val resultMapPartTwo = buildMap {
            groups.forEach { (mask, values) ->
                values.forEach { (address, value) ->
                    val binaryAddress = address.toString(2).padStart(mask.length, '0')

                    val maskedAddress = mask.zip(binaryAddress) { maskChar, addressChar ->
                        when (maskChar) {
                            '0' -> addressChar
                            else -> maskChar
                        }
                    }.joinToString("")

                    val floatingIndices = maskedAddress.foldIndexed(listOf<Int>()) { index, acc, c ->
                        if (c == 'X') acc + index else acc
                    }
                    val floatingBitsCount = mask.count { it == 'X' }
                    val maxFloatingNumber = "1".repeat(floatingBitsCount).toInt(2)

                    fun replaceFloating(value: Int): String {
                        val floatingValues = value.toString(2).padStart(floatingBitsCount, '0')

                        return buildString {
                            append(maskedAddress)

                            floatingValues.forEachIndexed { index, replacement ->
                                val addressIndex = floatingIndices[index]

                                set(addressIndex, replacement)
                            }
                        }
                    }

                    val resultValue = value.toLong(2)

                    (0..maxFloatingNumber).forEach {
                        put(replaceFloating(it).toLong(2), resultValue)
                    }
                }
            }
        }

        val partTwo = resultMapPartTwo.values.sum()

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }
}
