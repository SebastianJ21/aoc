package aoc25

import AOCAnswer
import AOCSolution
import mapToLong
import readInput
import splitBy
import transposed

class Day6 : AOCSolution {

    override fun solve(): AOCAnswer {
        val rawInput = readInput("day6.txt", AOCYear.TwentyFive)

        val functions = rawInput.last().split(" ").filter { it.isNotBlank() }.map {
            val operator: (Long, Long) -> Long = when (it) {
                "*" -> Long::times
                "+" -> Long::plus
                else -> error("Invalid operator $it")
            }
            operator
        }

        val rowNumbers = rawInput.dropLast(1).map { line ->
            line.split(" ").filter { it.isNotBlank() }.mapToLong()
        }

        val partOne = rowNumbers.transposed().zip(functions) { numbers, function -> numbers.reduce(function) }.sum()

        val colNumbers = rawInput
            .dropLast(1)
            .map { line -> line.toList() } // For `transposed` (needs List<List<T>> receiver)
            .transposed()
            .splitBy(
                predicate = { col -> col.none { it.isDigit() } },
                transform = { col -> col.filter { it.isDigit() }.joinToString("").toLong() },
            )

        val partTwo = colNumbers.zip(functions) { numbers, function -> numbers.reduce(function) }.sum()

        return AOCAnswer(partOne, partTwo)
    }
}
