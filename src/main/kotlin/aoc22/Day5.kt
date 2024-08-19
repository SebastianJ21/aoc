package aoc22

import readInput

class Day5 {
    fun solve() {
        val rawInput = readInput("day5.txt")
        val rawInputMap = rawInput.takeWhile { it.isNotEmpty() }

        val inputMap = rawInputMap.reversed().run {
            val identifiers = first()
            val data = drop(1)

            val identifierToValues = identifiers.mapIndexedNotNull { index, identifier ->
                if (identifier.isDigit()) {
                    val rowData = data.mapNotNull { dataRow ->
                        dataRow[index].takeIf { it.isLetter() }?.toString()
                    }
                    identifier.digitToInt() to rowData
                } else {
                    null
                }
            }
            identifierToValues.toMap()
        }

        val input = rawInput.drop(rawInputMap.size + 1).map { line ->
            line.split(" ")
                .mapNotNull { it.toIntOrNull() }
                .let { Triple(it[0], it[1], it[2]) }
        }

        val resultStatePartOne = input.fold(inputMap) { acc, op -> acc.executeOperation(op, false) }
        val resultStatePartTwo = input.fold(inputMap) { acc, op -> acc.executeOperation(op, true) }

        val lastFromEachPartOne = resultStatePartOne.values.joinToString("") { it.last() }
        val lastFromEachPartTwo = resultStatePartTwo.values.joinToString("") { it.last() }

        println("Part One: $lastFromEachPartOne")
        println("Part Two: $lastFromEachPartTwo")
    }

    private fun Map<Int, List<String>>.executeOperation(
        operation: Triple<Int, Int, Int>,
        moveAtOnce: Boolean,
    ): Map<Int, List<String>> {
        val (amount, from, to) = operation

        val toMove = getValue(from).takeLast(amount)
        val newAtTo = getValue(to) + toMove.run { if (moveAtOnce) this else reversed() }
        val newAtFrom = getValue(from).dropLast(amount)

        return this + (to to newAtTo) + (from to newAtFrom)
    }
}
