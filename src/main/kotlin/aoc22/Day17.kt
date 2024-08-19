@file:Suppress("MemberVisibilityCanBePrivate")

package aoc22

import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.toPersistentHashSet
import readInput
import kotlin.math.max

private const val CHAMBER_WIDTH = 7

private typealias PositionL = Pair<Long, Long>

class Day17 {

    enum class RockType {
        MINUS,
        PLUS,
        REVERSE_L,
        I,
        SQUARE,
    }

    data class FlowState(
        val windSequence: Sequence<Int>,
        val solidPoints: PersistentSet<CoordinatesL>,
        val highestPoint: Long,
        val lastDroppedType: RockType,
    )

    fun solve() {
        val windPattern = readInput("day17.txt").first()
        val windMovements = windPattern.map { windCharToYMovement(it) }
        val windSequence = sequence { while (true) yieldAll(windMovements) }

        val initialFloor = List(CHAMBER_WIDTH) { PositionL(0, it.toLong()) }.toPersistentHashSet()
        val initialFlowState = FlowState(windSequence, initialFloor, 0, RockType.SQUARE)

        val flowStates = (1..2022).runningFold(initialFlowState) { flowState, _ ->
            getNextFlowState(flowState)
        }

        val partOne = flowStates.last().highestPoint

        val heightGrowths = flowStates.zipWithNext { a, b -> b.highestPoint - a.highestPoint }
        val heightGrowthsStr = heightGrowths.joinToString("")

        val chunkSize = heightGrowthsStr.length / 10
        // Find the start index of a repeating cycle
        val (cycleStartIndex, cycleLength) = heightGrowthsStr.run {
            (0..lastIndex).firstNotNullOf { fromIndex ->
                val chunk = substring(fromIndex, fromIndex + chunkSize)

                indexOf(chunk).takeIf { it != -1 }?.let { firstIndex ->
                    indexOf(chunk, firstIndex + chunkSize).takeIf { it != -1 }?.let { secondIndex ->
                        fromIndex to secondIndex - firstIndex
                    }
                }
            }
        }

        val cycleValue = heightGrowths.subList(cycleStartIndex, cycleStartIndex + cycleLength).sum()
        val startValue = heightGrowths.take(cycleStartIndex).sum()

        val target = 1000000000000L

        val numberOfCycles = (target - cycleStartIndex).floorDiv(cycleLength)
        val incompleteCycleSize = (target - cycleStartIndex) % cycleLength

        val incompleteCycleValue =
            heightGrowths.subList(cycleStartIndex, cycleStartIndex + incompleteCycleSize.toInt()).sum()

        val partTwo = (numberOfCycles * cycleValue) + startValue + incompleteCycleValue

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }

    fun getNextFlowState(state: FlowState): FlowState {
        val rockType = state.lastDroppedType.getNext()
        val initialRock = createNextRock(state.highestPoint, rockType)

        val fallSequence = generateSequence(initialRock to state.windSequence) { (rock, windSequence) ->
            if (!rock.isValid(state.solidPoints)) return@generateSequence null

            val windMovement = windSequence.first()

            val afterWind = rock.applyYDirection(windMovement).let {
                if (it.isValid(state.solidPoints)) it else rock
            }

            afterWind.moveDown() to windSequence.drop(1)
        }

        val (overlappingRock, newWindSeq) = fallSequence.last()

        val newRock = overlappingRock.moveUp()
        val newSolidPoints = state.solidPoints.addAll(newRock)
        val newHighestPoint = max(state.highestPoint, newRock.maxOf { (x) -> x })

        return FlowState(newWindSeq, newSolidPoints, newHighestPoint, rockType)
    }

    fun List<CoordinatesL>.moveDown() = map { (x, y) -> x - 1 to y }

    fun List<CoordinatesL>.moveUp() = map { (x, y) -> x + 1 to y }

    fun List<CoordinatesL>.isValid(solidPoints: Set<CoordinatesL>) =
        none { it in solidPoints || it.second < 0L || it.second > 6L }

    private fun List<CoordinatesL>.applyYDirection(yDirection: Int) = map { (x, y) -> x to y + yDirection }

    private fun windCharToYMovement(wind: Char) = when (wind) {
        '>' -> 1
        '<' -> -1
        else -> 0
    }

    private fun RockType.getNext() = when (this) {
        RockType.MINUS -> RockType.PLUS
        RockType.PLUS -> RockType.REVERSE_L
        RockType.REVERSE_L -> RockType.I
        RockType.I -> RockType.SQUARE
        RockType.SQUARE -> RockType.MINUS
    }

    private fun createNextRock(highestPoint: Long, type: RockType): List<CoordinatesL> = when (type) {
        RockType.MINUS -> listOf(
            highestPoint + 4 to 2,
            highestPoint + 4 to 3,
            highestPoint + 4 to 4,
            highestPoint + 4 to 5,
        )
        RockType.PLUS -> listOf(
            highestPoint + 4 to 3,
            highestPoint + 5 to 2,
            highestPoint + 5 to 3,
            highestPoint + 5 to 4,
            highestPoint + 6 to 3,
        )
        RockType.REVERSE_L -> listOf(
            highestPoint + 4 to 2,
            highestPoint + 4 to 3,
            highestPoint + 4 to 4,
            highestPoint + 5 to 4,
            highestPoint + 6 to 4,
        )
        RockType.I -> listOf(
            highestPoint + 4 to 2,
            highestPoint + 5 to 2,
            highestPoint + 6 to 2,
            highestPoint + 7 to 2,
        )
        RockType.SQUARE -> listOf(
            highestPoint + 4 to 2,
            highestPoint + 4 to 3,
            highestPoint + 5 to 2,
            highestPoint + 5 to 3,
        )
    }
}
