package aoc20

import AOCYear
import readInput

class Day5 {

    fun solve() {
        val rawInput = readInput("day5.txt", AOCYear.Twenty)

        val allSeatRows = (0..127).toList()
        val allSeatColumns = (0..7).toList()

        val seatIds = rawInput.map { line ->
            val seatRow = line.take(7).fold(allSeatRows) { seats, char ->
                when (char) {
                    'F' -> seats.take(seats.size / 2)
                    'B' -> seats.takeLast(seats.size / 2)
                    else -> error("")
                }
            }.single()

            val seatCol = line.takeLast(3).fold(allSeatColumns) { seats, char ->
                when (char) {
                    'L' -> seats.take(seats.size / 2)
                    'R' -> seats.takeLast(seats.size / 2)
                    else -> error("")
                }
            }.single()

            seatRow * 8 + seatCol
        }

        val partOne = seatIds.max()

        val partTwo = seatIds.sorted()
            .windowed(2, 1)
            .first { (first, second) -> second - first == 2 }
            .first()
            .inc()

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }
}
