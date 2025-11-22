package aoc24

import AOCAnswer
import AOCSolution
import Direction
import mapToInt
import mapToLong
import readInput
import splitBy
import kotlin.math.min

class Day13 : AOCSolution {

    private data class Configuration(
        val buttonA: Direction,
        val buttonB: Direction,
        val target: Pair<Long, Long>,
    )

    private fun Configuration.verify(pressA: Long, pressB: Long): Boolean =
        buttonA.first * pressA + buttonB.first * pressB == target.first &&
            buttonA.second * pressA + buttonB.second * pressB == target.second

    override fun solve(): AOCAnswer {
        val rawInput = readInput("day13.txt", AOCYear.TwentyFour)

        val configurations = rawInput.splitBy { it.isEmpty() }.map { (buttonA, buttonB, prize) ->
            val (aX, aY) = buttonA.removePrefix("Button A: X+").replace("Y+", "").split(", ").mapToInt()
            val (bX, bY) = buttonB.removePrefix("Button B: X+").replace("Y+", "").split(", ").mapToInt()
            val (prizeX, prizeY) = prize.removePrefix("Prize: X=").replace("Y=", "").split(", ").mapToLong()

            Configuration(
                buttonA = Direction(aX, aY),
                buttonB = Direction(bX, bY),
                target = Pair(prizeX, prizeY),
            )
        }

        val partOne = configurations.sumOf { minCost(it) ?: 0 }

        val configurationsV2 = configurations.map { config ->
            val newTarget = config.target.first + 10000000000000 to config.target.second + 10000000000000

            config.copy(target = newTarget)
        }

        val partTwo = configurationsV2.sumOf { config ->
            val (x, y) = crammersRule(
                a1 = config.buttonA.first.toLong(),
                b1 = config.buttonB.first.toLong(),
                c1 = config.target.first,
                a2 = config.buttonA.second.toLong(),
                b2 = config.buttonB.second.toLong(),
                c2 = config.target.second,
            ) ?: return@sumOf 0L

            check(config.verify(x, y))

            x * 3 + y
        }

        return AOCAnswer(partOne, partTwo)
    }

    // it costs 3 tokens to push the A button and 1 token to push the B button.
    private fun minCost(configuration: Configuration): Long? = with(configuration) {
        val isValid = { pressA: Long ->
            (target.first - buttonA.first * pressA) % buttonB.first == 0L &&
                (target.second - buttonA.second * pressA) % buttonB.second == 0L
        }

        val calcBxPresses = { pressA: Long -> (target.first - buttonA.first * pressA) / buttonB.first }
        val calcByPresses = { pressA: Long -> (target.second - buttonA.second * pressA) / buttonB.second }

        val maxPressA = min(target.first.div(buttonA.first), target.second.div(buttonA.second)).inc()

        val pressA = (0L..maxPressA).firstOrNull { isValid(it) && (calcBxPresses(it) == calcByPresses(it)) }

        if (pressA == null) return null

        val pressB = calcBxPresses(pressA)

        val minCost = pressA * 3 + pressB
        return minCost
    }

    /**
     * Calculates determinant for 2x2 matrix.
     * ```
     * |a b|
     * |c d|
     * ```
     */
    private fun determinant(a: Long, b: Long, c: Long, d: Long) = a * d - b * c

    /**
     * Specific implementation for solving system of 2 linear equations.
     * ```
     * a1 * x1 + b1 * x2 = c1
     * a2 * x1 + b2 * x2 = c2
     * ```
     */
    private fun crammersRule(a1: Long, b1: Long, c1: Long, a2: Long, b2: Long, c2: Long): Pair<Long, Long>? {
        val det = determinant(a1, a2, b1, b2)

        val det1 = determinant(c1, c2, b1, b2)
        val det2 = determinant(a1, a2, c1, c2)

        if (det1 % det != 0L || det2 % det != 0L) return null

        val x1 = det1 / det
        val x2 = det2 / det

        return x1 to x2
    }
}
