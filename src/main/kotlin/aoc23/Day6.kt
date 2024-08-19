package aoc23

import AOCYear
import product
import readInput

class Day6 {

    fun solve() {
        val rawInput = readInput("day6.txt", AOCYear.TwentyThree)

        val races = rawInput.map { line ->
            line.split(" ").mapNotNull { it.toLongOrNull() }
        }.let { (times, distances) -> times.zip(distances) }

        fun Pair<Long, Long>.waysToBreakRecord() = let { (time, distance) ->
            val timeLowerBound = distance / time

            (timeLowerBound..time / 2)
                .firstOrNull { chargeFor -> chargeFor * (time - chargeFor) > distance }
                ?.let { time - (it * 2) + 1 } ?: 0L
        }

        val partOne = races.map { race -> race.waysToBreakRecord() }.product()

        val partTwo = races.reduce { (time, distance), (addTime, addDistance) ->
            ("$time$addTime").toLong() to ("$distance$addDistance").toLong()
        }.waysToBreakRecord()

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }
}
