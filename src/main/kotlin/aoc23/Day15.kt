package aoc23

import readInput

class Day15 {

    fun solve() {
        val rawInput = readInput("day15.txt", AOCYear.TwentyThree)

        fun String.runHASH() = fold(0) { acc, char ->
            acc
                .plus(char.code)
                .times(17)
                .mod(256)
        }

        val partOne = rawInput
            .single()
            .split(",")
            .sumOf { str -> str.runHASH() }

        val boxes = buildMap<Int, List<Pair<String, Int>>> {
            rawInput
                .single()
                .split(",")
                .forEach { str ->
                    val (label, number) = str.split("=", "-").run { first() to get(1).toIntOrNull() }
                    val boxHash = label.runHASH()
                    val currentBox = getOrDefault(boxHash, emptyList())

                    val updatedBox = when {
                        number == null -> currentBox.filter { it.first != label }

                        currentBox.any { it.first == label } ->
                            currentBox.map { if (it.first == label) label to number else it }

                        else -> currentBox.plus(label to number)
                    }

                    set(boxHash, updatedBox)
                }
        }

        val partTwo = boxes.entries.sumOf { (boxNumber, box) ->
            box.mapIndexed { lensIndex, (_, value) -> (boxNumber + 1) * (lensIndex + 1) * value }.sum()
        }

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }
}
