package aoc22

import AOCAnswer
import AOCSolution
import Position
import applyDirection
import at
import get
import getInDirectionOrNull
import getOrNull
import product
import readInput
import toMatrix
import kotlin.math.max

class Day8 : AOCSolution {

    private val up = -1 at 0
    private val down = 1 at 0
    private val left = 0 at -1
    private val right = 0 at 1

    private val directions = listOf(up, down, left, right)

    override fun solve(): AOCAnswer {
        val input = readInput("day8.txt", AOCYear.TwentyTwo)
        val matrix = input.toMatrix { value -> value.digitToInt() }

        val (visibleCount, scenicMax) = matrix.foldIndexed(0 to 0) { rowI, acc, row ->
            row.foldIndexed(acc) { colI, (visibleCount, scenicMax), _ ->
                val position = rowI at colI

                val isVisible = isTreeVisible(position, matrix)
                val scenicScore = getScenicScoreOfATree(position, matrix)

                val newVisibleCount = if (isVisible) visibleCount + 1 else visibleCount
                val newScenicMax = max(scenicMax, scenicScore)

                newVisibleCount to newScenicMax
            }
        }

        return AOCAnswer(visibleCount, scenicMax)
    }

    private fun isTreeVisible(treePosition: Position, matrix: List<List<Int>>): Boolean {
        val height = matrix[treePosition]

        val isVisible = directions.any { direction ->
            val seq = generateSequence(treePosition.applyDirection(direction)) { position ->
                position.applyDirection(direction)
            }

            seq.none { position ->
                matrix.getOrNull(position)?.let { it >= height } ?: return@any true
            }
        }

        return isVisible
    }

    private fun getScenicScoreOfATree(treePosition: Position, matrix: List<List<Int>>): Int {
        val treeHeight = matrix[treePosition]

        return directions.map { direction ->
            val firstPosition = treePosition.applyDirection(direction)
            val firstHeight = matrix.getOrNull(firstPosition) ?: return@map 0

            val seq = generateSequence(firstPosition to firstHeight) { (position, _) ->
                val nextHeight = matrix.getInDirectionOrNull(position, direction) ?: return@generateSequence null
                val nextPosition = position.applyDirection(direction)

                nextPosition to nextHeight
            }.toList()

            val score = seq.takeWhile { (_, height) -> height < treeHeight }.size

            if (score < seq.size) score + 1 else score
        }.product()
    }
}
