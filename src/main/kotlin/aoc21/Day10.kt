package aoc21

import readInput
import java.util.Stack

class Day10 {

    private val illegalParenScore = mapOf(
        ')' to 3,
        ']' to 57,
        '}' to 1197,
        '>' to 25137,
    )

    private val autoCompleteParenScore = mapOf(
        ')' to 1,
        ']' to 2,
        '}' to 3,
        '>' to 4,
    )

    fun solve() {
        val rawInput = readInput("day10.txt", AOCYear.TwentyOne)

        val partOne = rawInput
            .map { it.keepIllegalClosingParens() }
            .filter { it.isNotEmpty() }
            .sumOf { illegalParenScore.getValue(it.first()) }

        val partTwo = rawInput
            .filter { it.keepIllegalClosingParens().isEmpty() }
            .map {
                it
                    .findUnclosedParens()
                    .foldRight(0L) { unclosedParen, acc ->
                        acc * 5L + autoCompleteParenScore.getValue(unclosedParen.closingParen())
                    }
            }
            .sorted()
            .run { get(size / 2) }

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }

    private fun String.keepIllegalClosingParens(): String {
        val stack = Stack<Char>()
        return filter { char ->
            if (char.isOpeningParen()) {
                stack.push(char)
                false
            } else {
                stack.pop().closingParen() != char
            }
        }
    }

    private fun String.findUnclosedParens(): List<Char> {
        val stack = Stack<Char>()
        forEach {
            if (it.isOpeningParen()) {
                stack.push(it)
            } else {
                val expectedClosingParen = stack.pop().closingParen()
                check(expectedClosingParen == it) { "Illegal paren! Expected $expectedClosingParen, got $it" }
            }
        }
        return stack.toList()
    }

    private fun Char.isOpeningParen() = this in listOf('{', '[', '(', '<')

    private fun Char.closingParen() = when (this) {
        '{' -> '}'
        '[' -> ']'
        '(' -> ')'
        '<' -> '>'
        else -> error("Illegal character, got $this")
    }
}
