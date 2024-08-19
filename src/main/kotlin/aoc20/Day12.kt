@file:Suppress("MemberVisibilityCanBePrivate")

package aoc20

import AOCYear
import Position
import applyDirection
import plus
import readInput
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.sin

class Day12 {

    val up = -1 to 0
    val down = 1 to 0
    val left = 0 to -1
    val right = 0 to 1

    val directions = listOf(up, right, down, left)

    fun solve() {
        val rawInput = readInput("day12.txt", AOCYear.Twenty)

        val directionSequence = sequence { while (true) yieldAll(directions) }
        val reverseDirectionSequence = sequence { while (true) yieldAll(directions.asReversed()) }

        fun getNewDirection(sequence: Sequence<Pair<Int, Int>>, value: Int, direction: Pair<Int, Int>): Pair<Int, Int> {
            val steps = value / 90

            return sequence.dropWhile { it != direction }.take(steps + 1).last()
        }

        val (finalPositionPartOne) = rawInput.fold((0 to 0) to right) { (position, direction), line ->
            val command = line.first()
            val value = line.drop(1).toInt()

            val (movement, newDirection) = when (command) {
                'N' -> up * value to direction
                'S' -> down * value to direction
                'W' -> left * value to direction
                'E' -> right * value to direction
                'L' -> {
                    val newDirection = getNewDirection(reverseDirectionSequence, value, direction)

                    (0 to 0) to newDirection
                }
                'R' -> {
                    val newDirection = getNewDirection(directionSequence, value, direction)

                    (0 to 0) to newDirection
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

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }

    fun Position.rotate(degrees: Int, clockwise: Boolean): Position {
        val degreesDouble = degrees.toDouble().run { if (clockwise) unaryMinus() else unaryPlus() }
        val (x, y) = this
        val radians = Math.toRadians(degreesDouble)

        val newX = x * cos(radians) - y * sin(radians)
        val newY = x * sin(radians) + y * cos(radians)

        return round(newX).toInt() to round(newY).toInt()
    }

    operator fun Pair<Int, Int>.times(n: Int) = first * n to second * n
}
