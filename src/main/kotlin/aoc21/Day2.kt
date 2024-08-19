package aoc21

import AOCYear
import readInput

class Day2 {

    fun solve() {
        val rawInput = readInput("day2.txt", AOCYear.TwentyOne)

        fun inputToCommands(commandMapper: (String, Int) -> (Triple<Int, Int, Int>) -> Triple<Int, Int, Int>) =
            rawInput.map { line ->
                val (command, valueStr) = line.split(" ")
                commandMapper(command, valueStr.toInt())
            }

        val partOneMapper = { command: String, value: Int ->
            { (horizontal, depth, aim): Triple<Int, Int, Int> ->
                when (command) {
                    "forward" -> Triple(horizontal + value, depth, aim)
                    "up" -> Triple(horizontal, depth - value, aim)
                    "down" -> Triple(horizontal, depth + value, aim)
                    else -> error("Unknown command $command")
                }
            }
        }

        val commandsPartOne = inputToCommands(partOneMapper)

        // Horizontal position, Depth, Aim
        val initialData = Triple(0, 0, 0)

        val finalDataPartOne = commandsPartOne.fold(initialData) { acc, function -> function(acc) }

        val partOne = finalDataPartOne.let { (horizontal, depth) -> horizontal * depth }

        val partTwoMapper = { command: String, value: Int ->
            { (horizontal, depth, aim): Triple<Int, Int, Int> ->
                when (command) {
                    "forward" -> Triple(horizontal + value, depth + (aim * value), aim)
                    "up" -> Triple(horizontal, depth, aim - value)
                    "down" -> Triple(horizontal, depth, aim + value)
                    else -> error("Unknown command $command")
                }
            }
        }

        val commandsPartTwo = inputToCommands(partTwoMapper)

        val finalPositionPartTwo = commandsPartTwo.fold(initialData) { acc, function -> function(acc) }

        val partTwo = finalPositionPartTwo.let { (horizontal, depth) -> horizontal * depth }

        println("Part One: $partOne")
        println("Part One: $partTwo")
    }
}
