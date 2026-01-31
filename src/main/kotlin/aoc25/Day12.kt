package aoc25

import AOCAnswer
import AOCSolution
import mapToInt
import readInput
import splitBy

class Day12 : AOCSolution {

    override fun solve(): AOCAnswer {
        val rawInput = readInput("day12.txt", AOCYear.TwentyFive)

        val inputParts = rawInput.splitBy { it.isEmpty() }

        val isShapeHeader: (List<String>) -> Boolean = { it.first().matches(Regex("[0-9]:")) }

        val shapes = inputParts.takeWhile(isShapeHeader).map { shapeLines -> shapeLines.drop(1) }
        val instructions = inputParts.dropWhile(isShapeHeader).single()

        val inputs = instructions.map { instruction ->
            val (dimensions, shapeCounts) = instruction.split(": ")

            val heightToWidth = dimensions.split("x").mapToInt()
            val shapesCounts = shapeCounts.split(" ").mapToInt()

            heightToWidth to shapesCounts
        }

        val partOne = inputs.count { (heightToWidth, shapeCounts) ->
            val (height, width) = heightToWidth
            val area = height * width

            val shapeArea = shapeCounts
                .mapIndexed { shapeIndex, shapeCount -> shapes[shapeIndex].sumOf { it.length } * shapeCount }
                .sum()

            shapeArea < area
        }

        return AOCAnswer(partOne, "Not present on final day")
    }
}
