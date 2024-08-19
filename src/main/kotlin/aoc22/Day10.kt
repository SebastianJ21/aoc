package aoc22

import AOCYear
import readInput
import kotlin.math.abs

class Day10 {

    fun solve() {
        val rawInput = readInput("day10.txt", AOCYear.TwentyTwo)

        val instructionSequence = sequence { while (true) yieldAll(rawInput.asSequence()) }

        val cyclesToCollect = (0..240 step 40).toList()
        val lastCycleIndex = cyclesToCollect.max()

        val execution =
            instructionSequence.take(lastCycleIndex).runningFold(1 to 1) { (value, cycle), instruction ->
                if (instruction == "noop") {
                    value to cycle + 1
                } else {
                    val toAdd = instruction.split(" ").let { (_, toAdd) -> toAdd.toInt() }

                    value + toAdd to cycle + 2
                }
            }

        val allCycles = execution.zipWithNext().flatMap { (from, to) ->
            val fromCycle = from.second
            val toCycle = to.second

            (fromCycle until toCycle).map { from.first to it }
        }.toList().dropLastWhile { it.second > lastCycleIndex }

        val partOneCycles = cyclesToCollect.map { it + 20 }.dropLast(1)

        val partOne = partOneCycles.sumOf { cycleToFind ->
            val valueAtCycle = allCycles[cycleToFind - 1].first
            cycleToFind * valueAtCycle
        }

        val partTwo = allCycles.map { (value, cycle) ->
            val drawIndex = cycle.dec() % 40

            if (abs(drawIndex - value) <= 1) "#" else "."
        }.windowed(40, 40)

        println("Part one: $partOne")
        println("Part two: ")
        partTwo.forEach { println(it) }
    }
}
