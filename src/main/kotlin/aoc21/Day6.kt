package aoc21

import AOCYear
import mapToInt
import readInput

private const val LANTERNFISH_STATES_COUNT = 9

class Day6 {

    fun solve() {
        val rawInput = readInput("day6.txt", AOCYear.TwentyOne)
        val initialState = getInitialState(rawInput)

        val partOneAnswer = initialState.getStateAfter(80).sum()
        val partTwoAnswer = initialState.getStateAfter(256).sum()

        println("Part one: $partOneAnswer")
        println("Part two: $partTwoAnswer")
    }

    private fun List<Long>.getStateAfter(rounds: Int): List<Long> =
        (1..rounds).fold(this) { state, _ -> state.nextState() }

    private fun List<Long>.nextState(): List<Long> {
        val first = first()
        val newEnd = listOf(this[7] + first, this[8], first)

        return drop(1).dropLast(2) + newEnd
    }

    private fun getInitialState(input: List<String>): List<Long> = input
        .single()
        .split(",")
        .mapToInt()
        .groupingBy { it }
        .eachCount()
        .let { stateToCount ->
            List(LANTERNFISH_STATES_COUNT) { state ->
                val initialState = stateToCount[state] ?: 0

                initialState.toLong()
            }
        }
}
