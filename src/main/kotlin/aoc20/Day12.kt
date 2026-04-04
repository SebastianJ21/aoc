package aoc20

import AOCAnswer
import AOCSolution
import AOCYear
import Direction
import Position
import applyDirection
import at
import plus
import readInput
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.sin

class Day12 : AOCSolution {

    private val up = -1 at 0
    private val down = 1 at 0
    private val left = 0 at -1
    private val right = 0 at 1

    private val directions = listOf(up, right, down, left)

    override fun solve(): AOCAnswer {
        val rawInput = readInput("day12.txt", AOCYear.Twenty)

        val directionSequence = sequence { while (true) yieldAll(directions) }
        val reverseDirectionSequence = sequence { while (true) yieldAll(directions.asReversed()) }

        val (finalPositionPartOne) = rawInput.fold((0 at 0) to right) { (position, direction), line ->
            val command = line.first()
            val value = line.drop(1).toInt()

            val (movement, newDirection) = when (command) {
                'N' -> up * value to direction
                'S' -> down * value to direction
                'W' -> left * value to direction
                'E' -> right * value to direction
                'L' -> {
                    val newDirection = getNewDirection(reverseDirectionSequence, value, direction)

                    (0 at 0) to newDirection
                }
                'R' -> {
                    val newDirection = getNewDirection(directionSequence, value, direction)

                    (0 at 0) to newDirection
                }
                'F' -> direction * value to direction

                else -> error("Unknown command $command")
            }

            position + movement to newDirection
        }

        val partOne = finalPositionPartOne.run { abs(first) + abs(second) }

        val (finalPositionPartTwo) = rawInput.fold(Position(0, 0) to Position(-1, 10)) { (ship, waypoint), line ->
            val command = line.first()
            val value = line.drop(1).toInt()

            when (command) {
                'N' -> ship to waypoint.applyDirection(up * value)
                'W' -> ship to waypoint.applyDirection(left * value)
                'E' -> ship to waypoint.applyDirection(right * value)
                'S' -> ship to waypoint.applyDirection(down * value)

                'L' -> ship to waypoint.rotate(value, false)
                'R' -> ship to waypoint.rotate(value, true)

                'F' -> ship.applyDirection(waypoint * value) to waypoint

                else -> error("Unknown command $command")
            }
        }

        val partTwo = finalPositionPartTwo.run { abs(first) + abs(second) }

        return AOCAnswer(partOne, partTwo)
    }

    private fun Position.rotate(degrees: Int, clockwise: Boolean): Position {
        val degreesDouble = degrees.toDouble().run { if (clockwise) unaryMinus() else unaryPlus() }
        val (x, y) = this
        val radians = Math.toRadians(degreesDouble)

        val newX = x * cos(radians) - y * sin(radians)
        val newY = x * sin(radians) + y * cos(radians)

        return round(newX).toInt() at round(newY).toInt()
    }

    private fun getNewDirection(sequence: Sequence<Position>, value: Int, direction: Direction): Position {
        val steps = value / 90

        return sequence.dropWhile { it != direction }.take(steps + 1).last()
    }

    private operator fun Position.times(n: Int) = first * n at second * n
}
