package aoc22

import AOCAnswer
import AOCSolution
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.toPersistentHashSet
import readInput
import kotlin.math.max

private const val CHAMBER_WIDTH = 7

class Day17 : AOCSolution {

    enum class RockType {
        MINUS,
        PLUS,
        REVERSE_L,
        I,
        SQUARE,
    }

    private data class FlowState(
        val windSequence: Sequence<Int>,
        val solidPoints: PersistentSet<PositionL>,
        val highestPoint: Long,
        val lastDroppedType: RockType,
    )

    private data class PositionL(val first: Long, val second: Long)
    private infix fun Long.at(other: Long) = PositionL(this, other)

    override fun solve(): AOCAnswer {
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

        return AOCAnswer(partOne, partTwo)
    }

    private fun getNextFlowState(state: FlowState): FlowState {
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

    private fun List<PositionL>.moveDown() = map { (x, y) -> x - 1 at y }

    private fun List<PositionL>.moveUp() = map { (x, y) -> x + 1 at y }

    private fun List<PositionL>.isValid(solidPoints: Set<PositionL>) =
        none { it in solidPoints || it.second < 0L || it.second > 6L }

    private fun List<PositionL>.applyYDirection(yDirection: Int) = map { (x, y) -> x at y + yDirection }

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

    private fun createNextRock(highestPoint: Long, type: RockType): List<PositionL> = when (type) {
        RockType.MINUS -> listOf(
            highestPoint + 4 at 2,
            highestPoint + 4 at 3,
            highestPoint + 4 at 4,
            highestPoint + 4 at 5,
        )
        RockType.PLUS -> listOf(
            highestPoint + 4 at 3,
            highestPoint + 5 at 2,
            highestPoint + 5 at 3,
            highestPoint + 5 at 4,
            highestPoint + 6 at 3,
        )
        RockType.REVERSE_L -> listOf(
            highestPoint + 4 at 2,
            highestPoint + 4 at 3,
            highestPoint + 4 at 4,
            highestPoint + 5 at 4,
            highestPoint + 6 at 4,
        )
        RockType.I -> listOf(
            highestPoint + 4 at 2,
            highestPoint + 5 at 2,
            highestPoint + 6 at 2,
            highestPoint + 7 at 2,
        )
        RockType.SQUARE -> listOf(
            highestPoint + 4 at 2,
            highestPoint + 4 at 3,
            highestPoint + 5 at 2,
            highestPoint + 5 at 3,
        )
    }
}
