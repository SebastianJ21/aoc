@file:Suppress("MemberVisibilityCanBePrivate")

package aoc19

import AOCAnswer
import AOCSolution
import AOCYear
import kotlinx.collections.immutable.persistentHashSetOf
import lcm
import mapToInt
import readInput
import transposed
import java.math.BigInteger
import kotlin.math.abs

typealias Position_Velocity = Pair<Int, Int>

class Day12 : AOCSolution {

    override fun solve(): AOCAnswer {
        val rawInput = readInput("day12.txt", AOCYear.Nineteen)

        val moonPositions = rawInput.map { line ->
            line.replace(Regex("<|>|[xyz]="), "").split(", ").mapToInt()
        }

        val moonPositionAxes = moonPositions.transposed()
        val moonAxesWithVelocity = moonPositionAxes.map { axis -> axis.map { Position_Velocity(it, 0) } }

        val partOneFinal = (1..1000).fold(moonAxesWithVelocity) { currentMoons, _ ->
            currentMoons.map { getNextAxis(it) }
        }

        val partOne = partOneFinal.run {
            val positions = map { axis -> axis.map { it.first } }.transposed()
            val velocities = map { axis -> axis.map { it.second } }.transposed()

            positions.zip(velocities) { position, velocity ->
                position.sumOf { abs(it) } * velocity.sumOf { abs(it) }
            }.sum()
        }

        // First repeating state = Period of repetition for each axis (as they are independent of each other) -> LCM
        val partTwo = moonAxesWithVelocity.map { findPeriod(it).toBigInteger() }.reduce(BigInteger::lcm)

        return AOCAnswer(partOne, partTwo)
    }

    fun getNextAxis(axisWithVelocity: List<Position_Velocity>) =
        axisWithVelocity.mapIndexed { index, (position, initialVelocity) ->
            val newVelocity = axisWithVelocity.foldIndexed(initialVelocity) { otherIndex, velocity, (otherPosition) ->
                if (otherIndex == index) return@foldIndexed velocity

                val change = when {
                    position > otherPosition -> -1
                    position < otherPosition -> 1
                    else -> 0
                }

                velocity + change
            }

            position + newVelocity to newVelocity
        }

    fun findPeriod(axis: List<Position_Velocity>): Int {
        val initialSeen = persistentHashSetOf<List<Position_Velocity>>()

        val sequence = generateSequence(axis to initialSeen) { (current, seen) ->
            if (current in seen) return@generateSequence null

            getNextAxis(current) to seen.add(current)
        }

        val periodSize = sequence.count() - 1

        return periodSize
    }
}
