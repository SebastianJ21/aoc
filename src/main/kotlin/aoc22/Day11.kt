@file:Suppress("MemberVisibilityCanBePrivate")

package aoc22

import alsoPrintLn
import lcm
import mapToLong
import product
import readInput
import splitBy
import java.math.BigInteger

class Day11 {

    data class Monkey(
        val id: Int,
        val startingItems: List<Long>,
        val getNextMonkeyId: (Long) -> Int,
        val getNextItemScore: (Long) -> Long,
        val testNumber: Long,
    )

    fun solve() {
        val rawInput = readInput("day11.txt", AOCYear.TwentyTwo)

        val monkeyInputs = rawInput.splitBy { it.isEmpty() }
        val monkeys = monkeyInputs.map { parseMonkey(it) }

        val lcm = monkeys.map { it.testNumber.toBigInteger() }.reduce(BigInteger::lcm).toLong()

        monkeys.performInspections(20) { it / 3 }.alsoPrintLn { }
        monkeys.performInspections(10000) { it % lcm }.alsoPrintLn { }
    }

    fun List<Monkey>.performInspections(rounds: Int, reduceItemScore: (Long) -> Long): Long {
        forEachIndexed { index, monkey -> check(index == monkey.id) }
        val monkeys = this

        val inspectionCounts: List<Long> = buildList {
            monkeys.forEach { _ -> add(0L) }

            val monkeyItems = monkeys.map { (_, startingItems) -> startingItems.toMutableList() }

            repeat(rounds) {
                monkeys.forEach { monkey ->
                    val items = monkeyItems[monkey.id]

                    items.forEach { item ->
                        val newItem = reduceItemScore(monkey.getNextItemScore(item))

                        val newMonkeyId = monkey.getNextMonkeyId(newItem)

                        monkeyItems[newMonkeyId].add(newItem)

                        this[monkey.id]++
                    }

                    items.clear()
                }
            }
        }

        return inspectionCounts.sortedDescending().take(2).product()
    }

    fun parseMonkey(lines: List<String>): Monkey {
        val id = lines.first().split(" ").let { (_, id) -> id.removeSuffix(":").toInt() }
        val startingItems = lines[1].split(": ").let { (_, items) -> items.split(", ").mapToLong() }
        val operation = lines[2].split("old ").let { (_, rawOp) ->
            val (rawFunc, rawValue) = rawOp.split(" ")

            val value = rawValue.toLongOrNull()
            val operationFunction = parseOperationFunction(rawFunc.single());

            { num: Long -> operationFunction(num, value ?: num) }
        }
        val testNumber = lines[3].split("by ").let { (_, num) -> num.toLong() }

        val trueResult = lines[4].split("monkey ").let { (_, num) -> num.toInt() }
        val falseResult = lines[5].split("monkey ").let { (_, num) -> num.toInt() }

        val testFunc = { num: Long -> if (num % testNumber == 0L) trueResult else falseResult }

        return Monkey(id, startingItems, testFunc, operation, testNumber)
    }

    fun parseOperationFunction(char: Char): (Long, Long) -> Long = when (char) {
        '*' -> Long::times
        '+' -> Long::plus
        else -> error("Unknown operation $char")
    }
}
