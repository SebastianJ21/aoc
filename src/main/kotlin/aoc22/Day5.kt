package aoc22

import AOCAnswer
import AOCSolution
import readInput
import splitBy
import transposed

class Day5 : AOCSolution {

    private data class Operation(
        val amount: Int,
        val from: Int,
        val to: Int,
    )

    override fun solve(): AOCAnswer {
        val rawInput = readInput("day5.txt")

        val (mapLines, operationLines) = rawInput.splitBy { it.isEmpty() }

        val lineLength = mapLines.maxOf { it.length }
        val mapRows = mapLines
            .map { it.padEnd(lineLength).toList() } // Pad to common line length to enable `rotated` (uses transposed)
            .rotated()
            .filter { (first) -> first.isDigit() } // Keep only rows with map data

        val initialMap = mapRows.associate { line ->
            val identifier = line.first().digitToInt()
            val elements = line.drop(1).takeWhile { it.isLetter() }

            identifier to elements
        }

        val operations = operationLines.map { line ->
            val (amount, from, to) = line.split(" ").mapNotNull { it.toIntOrNull() }

            Operation(amount, from, to)
        }

        val resultStatePartOne = operations.fold(initialMap) { map, op -> map.applyOperation(op, false) }
        val resultStatePartTwo = operations.fold(initialMap) { map, op -> map.applyOperation(op, true) }

        val partOne = resultStatePartOne.values.map { it.last() }.joinToString("")
        val partTwo = resultStatePartTwo.values.map { it.last() }.joinToString("")

        return AOCAnswer(partOne, partTwo)
    }

    private fun Map<Int, List<Char>>.applyOperation(operation: Operation, moveAtOnce: Boolean): Map<Int, List<Char>> {
        val from = this.getValue(operation.from)
        val to = this.getValue(operation.to)

        val toMove = from.takeLast(operation.amount).let { if (moveAtOnce) it else it.asReversed() }

        val newTo = to + toMove
        val newFrom = from.dropLast(operation.amount)

        return this + (operation.to to newTo) + (operation.from to newFrom)
    }

    private fun List<List<Char>>.rotated() = this.transposed().map { it.asReversed() }
}
