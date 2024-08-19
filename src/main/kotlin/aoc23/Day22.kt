package aoc23

import AOCYear
import mapToInt
import readInput
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class Day22 {

    data class Brick(
        val x: IntRange,
        val y: IntRange,
        val z: IntRange,
    )

    private fun Brick.dropZ() = copy(z = z.first.dec()..z.last.dec())

    private fun areTouching(brickA: Brick, brickB: Brick): Boolean {
        if (abs(max(brickA.z.first, brickB.z.first) - min(brickA.z.last, brickB.z.last)) != 1) {
            return false
        }

        return brickA.x.any { it in brickB.x } && brickA.y.any { it in brickB.y }
    }

    private fun settledBricks(bricks: List<Brick>): List<Brick> {
        val lowToHigh = bricks.sortedBy { it.z.first }

        return buildList(bricks.size) {
            lowToHigh.forEach { brick ->
                var current = brick
                while (true) {
                    if (1 in current.z || any { areTouching(current, it) }) {
                        add(current)
                        break
                    }
                    current = current.dropZ()
                }
            }
        }
    }

    private fun chainReactionScore(start: Brick, brickToSupported: Map<Brick, List<Brick>>): Int {
        val brickToCurrentlySupportedBy = brickToSupported.values.flatten().groupingBy { it }.eachCount().toMutableMap()

        val queue = ArrayDeque(listOf(start))
        var score = 0

        while (queue.isNotEmpty()) {
            val brick = queue.removeFirst()

            val supportedBricks = brickToSupported.getValue(brick)

            supportedBricks.forEach { supportedBrick ->
                val supportedBy = brickToCurrentlySupportedBy.getValue(supportedBrick)

                // If we remove this brick, the supported one will fall
                if (supportedBy == 1) {
                    queue.add(supportedBrick)
                    score++
                }

                brickToCurrentlySupportedBy[supportedBrick] = supportedBy.dec()
            }
        }
        return score
    }

    fun solve() {
        val rawInput = readInput("day22.txt", AOCYear.TwentyThree)

        val bricks = rawInput.map { line ->
            val (start, end) = line.split("~")

            start.split(",").mapToInt().zip(end.split(",").mapToInt()).let { (x, y, z) ->
                Brick(x.first..x.second, y.first..y.second, z.first..z.second)
            }
        }

        val settledBricks = settledBricks(bricks)

        val brickToSupportedBricks = settledBricks.associateWith { brick ->
            settledBricks.filter {
                areTouching(
                    brick,
                    it,
                ) && brick.z.last < it.z.first
            }
        }

        val brickToSupportedBy = brickToSupportedBricks.values.flatten().groupingBy { it }.eachCount()

        val partOne = brickToSupportedBricks.values.count { supportedBricks ->
            supportedBricks.none { brickToSupportedBy.getValue(it) == 1 }
        }

        val partTwo = settledBricks.sumOf { chainReactionScore(it, brickToSupportedBricks) }

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }
}
