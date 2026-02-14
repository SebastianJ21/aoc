package aoc24

import AOCAnswer
import AOCSolution
import mapToInt
import product
import readInput

class Day14 : AOCSolution {

    private data class Position(val first: Int, val second: Int)
    private typealias Direction = Position

    private fun Position.applyDirection(other: Position) = Position(first + other.first, second + other.second)
    private fun Position.scaled(n: Int) = Position(first * n, second * n)

    companion object {
        private const val WIDTH: Int = 101
        private const val HEIGHT: Int = 103

        private const val PRINT_PART_TWO_SOLUTION = false
    }

    override fun solve(): AOCAnswer {
        val rawInput = readInput("day14.txt", AOCYear.TwentyFour)

        // space which is 101 tiles wide and 103 tiles tall
        val robots = rawInput.map { line ->
            // p=54,45 v=-37,75
            line.split(" ").let { (position, velocity) ->
                val (col, row) = position.removePrefix("p=").split(",").mapToInt()
                val (colVelocity, rowVelocity) = velocity.removePrefix("v=").split(",").mapToInt()

                Position(row, col) to Direction(rowVelocity, colVelocity)
            }
        }

        val midHeight = HEIGHT / 2
        val midWidth = WIDTH / 2

        val (q1Rows, q1Cols) = (0 until midHeight) to (0 until midWidth)
        val (q2Rows, q2Cols) = (midHeight + 1 until HEIGHT) to (0 until midWidth)
        val (q3Rows, q3Cols) = (0 until midHeight) to (midWidth + 1 until WIDTH)
        val (q4Rows, q4Cols) = (midHeight + 1 until HEIGHT) to (midWidth + 1 until WIDTH)

        fun scoreAt(i: Int): Int {
            val quadrantScores = buildList<Int>(capacity = 4) {
                addAll(listOf(0, 0, 0, 0))

                robots.forEach { (position, direction) ->
                    val (nextRow, nextCol) = position.applyDirection(direction.scaled(i))

                    val newRow = Math.floorMod(nextRow, HEIGHT)
                    val newCol = Math.floorMod(nextCol, WIDTH)

                    val index = when (newRow) {
                        in q1Rows if newCol in q1Cols -> 0
                        in q2Rows if newCol in q2Cols -> 1
                        in q3Rows if newCol in q3Cols -> 2
                        in q4Rows if newCol in q4Cols -> 3
                        else -> return@forEach
                    }

                    this[index]++
                }
            }

            return quadrantScores.product()
        }

        val partOne = scoreAt(100)

        val maxIterations = WIDTH * HEIGHT
        val partTwo = (1..maxIterations).minBy { scoreAt(it) }

        // Logic to visually check whether the solution is correct (no other way of confirming...)
        if (PRINT_PART_TWO_SOLUTION) {
            val partTwoRobotPositions = robots.map { (position, direction) ->
                val (scaledRow, scaledCol) = position.applyDirection(direction.scaled(partTwo))

                Position(Math.floorMod(scaledRow, HEIGHT), Math.floorMod(scaledCol, WIDTH))
            }

            printBoard(partTwoRobotPositions)
        }

        return AOCAnswer(partOne, partTwo)
    }

    private fun printBoard(robots: List<Position>) {
        val robotsSet = robots.toSet()

        repeat(HEIGHT) { rowI ->
            val row = List(WIDTH) { colI -> if (Position(rowI, colI) in robotsSet) "#" else " " }

            println(row.joinToString(""))
        }
    }
}
