package aoc24

import AOCAnswer
import AOCSolution
import mapToInt
import readInput
import splitBy

class Day5 : AOCSolution {

    override fun solve(): AOCAnswer {
        val rawInput = readInput("day5.txt", AOCYear.TwentyFour)

        val (rules, rawCommands) = rawInput.splitBy { isEmpty() }

        val lowerToHigherNumbers = rules
            .map { it.split('|').mapToInt() }
            .onEach { require(it.size == 2) { "Expected rule to be of size 2! Was: $it" } }
            .groupBy({ (lower) -> lower }, { (_, higher) -> higher })
            .mapValues { (_, values) -> values.toSet() }

        val commands = rawCommands.map { it.split(',').mapToInt() }

        val (correctCommands, incorrectCommands) = commands.partition { command ->
            isCommandValid(command, lowerToHigherNumbers)
        }

        val fixedCommands = incorrectCommands.map { fixCommand(it, lowerToHigherNumbers) }

        val partOne = correctCommands.sumOf { it[it.size / 2] }
        val partTwo = fixedCommands.sumOf { it[it.size / 2] }

        return AOCAnswer(partOne, partTwo)
    }

    private fun isCommandValid(command: List<Int>, lowerToHigherNumbers: Map<Int, Set<Int>>) =
        findSwapIndex(command, lowerToHigherNumbers) == null

    private fun fixCommand(command: List<Int>, lowerToHigherNumbers: Map<Int, Set<Int>>): List<Int> {
        val (swapIndex0, swapIndex1) = findSwapIndex(command, lowerToHigherNumbers) ?: return command

        return fixCommand(command.swap(swapIndex0, swapIndex1), lowerToHigherNumbers)
    }

    private fun findSwapIndex(command: List<Int>, lowerToHigherNumbers: Map<Int, Set<Int>>): Pair<Int, Int>? {
        return command.withIndex().firstNotNullOfOrNull { (index, current) ->
            val preceding = command.take(index)
            val higher = lowerToHigherNumbers[current] ?: return@firstNotNullOfOrNull null

            // If a rule exists for current command, no preceding number can be included in it
            preceding.indexOfFirst { it in higher }.takeIf { it != -1 }?.let { index to it }
        }
    }

    private fun List<Int>.swap(index0: Int, index1: Int): List<Int> {
        return mapIndexed { index, elem ->
            when (index) {
                index0 -> this[index1]
                index1 -> this[index0]
                else -> elem
            }
        }
    }
}
