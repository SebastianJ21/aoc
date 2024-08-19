package aoc21

import AOCYear
import readInput
import kotlin.math.abs
import kotlin.math.max

typealias Area = Pair<IntRange, IntRange>

class Day17 {

    data class ThrowPosition(val x: Int, val y: Int, val xVelocity: Int, val yVelocity: Int) {

        fun isInTarget(target: Area): Boolean {
            val (xTarget, yTarget) = target

            return (x in xTarget) && (y in yTarget)
        }

        fun isAfterTarget(target: Area): Boolean {
            val (xTarget, yTarget) = target

            return x > xTarget.last || y < yTarget.first
        }
    }

    fun solve() {
        val target = readInput("day17.txt", AOCYear.TwentyOne)
            .single()
            .split(": ").let { (_, rawCoordinates) ->
                val (x, y) = rawCoordinates.replace("x=", "").replace("y=", "").split(", ")

                fun parseRange(rawRange: String) = rawRange.split("..").let { (from, to) ->
                    from.toInt()..to.toInt()
                }

                parseRange(x) to parseRange(y)
            }

        val (xTarget, yTarget) = target

        val xRange = 1..xTarget.last
        val yRange = yTarget.first..abs(yTarget.first)

        val partOne = xRange.maxOf { x ->
            yRange.maxOf { y ->
                ThrowPosition(0, 0, x, y).maxY(target)
            }
        }

        val partTwo = xRange.sumOf { x ->
            yRange.count { y ->
                ThrowPosition(0, 0, x, y).hitsTarget(target)
            }
        }

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }

    enum class ThrowState { HIT, MISS, PENDING }

    private fun ThrowPosition.maxY(area: Area): Int {
        val throwSequence = generateSequence(Triple(this, y, ThrowState.PENDING)) { (throwData, maxY, _) ->
            val nextThrowData = throwData.next()

            val (nextState, nextMaxY) = when {
                throwData.isInTarget(area) -> ThrowState.HIT to maxY
                throwData.isAfterTarget(area) -> ThrowState.MISS to 0
                else -> ThrowState.PENDING to max(maxY, nextThrowData.y)
            }

            Triple(nextThrowData, nextMaxY, nextState)
        }

        return throwSequence.first { (_, _, state) -> state != ThrowState.PENDING }.second
    }

    private fun ThrowPosition.hitsTarget(area: Area): Boolean {
        val throwSequence = generateSequence(this to ThrowState.PENDING) { (throwData, _) ->
            val nextState = when {
                throwData.isInTarget(area) -> ThrowState.HIT
                throwData.isAfterTarget(area) -> ThrowState.MISS
                else -> ThrowState.PENDING
            }

            throwData.next() to nextState
        }

        return throwSequence.first { (_, state) -> state != ThrowState.PENDING }.second == ThrowState.HIT
    }

    fun ThrowPosition.next(): ThrowPosition {
        val newXVelocity = when {
            xVelocity > 0 -> xVelocity - 1
            xVelocity < 0 -> xVelocity + 1
            else -> 0
        }

        return ThrowPosition(x + xVelocity, y + yVelocity, newXVelocity, yVelocity - 1)
    }
}
