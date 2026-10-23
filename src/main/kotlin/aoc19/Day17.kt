package aoc19

import AOCAnswer
import AOCSolution
import Direction
import Position
import applyDirection
import at
import get
import inputLines
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentHashSetOf
import mapToLong
import positionsOf
import splitBy
import kotlin.math.cos
import kotlin.math.sin

class Day17 : AOCSolution {

    private val up = -1 at 0
    private val down = 1 at 0
    private val left = 0 at -1
    private val right = 0 at 1

    private val directions = listOf(up, down, left, right)

    private companion object {
        const val SCAFFOLD_TILE = '#'
        const val ROBOT_TILE = '^'
        const val MAX_FUNCTION_SIZE = 20
    }

    private enum class Turn { LEFT, RIGHT }

    override fun solve(): AOCAnswer {
        val inputLines = inputLines()

        val inputInstructions = inputLines.single().split(',').mapToLong()
        val initialState = ExecutionState.fromList(inputInstructions)

        val cameraOutputs = IntCodeRunner.executeInstructions(initialState).outputs

        val matrix = cameraOutputs.map { it.toInt().toChar() }.splitBy { it == '\n' }.filter { it.isNotEmpty() }
        val scaffoldPositions = matrix.positionsOf { it == SCAFFOLD_TILE || it == ROBOT_TILE }.toSet()

        val positionToNeighbors = scaffoldPositions.associateWith { position ->
            directions.mapNotNull { direction -> position.applyDirection(direction).takeIf { it in scaffoldPositions } }
        }

        val intersectionPositions = positionToNeighbors.filterValues { it.size == 4 }.keys
        val partOne = intersectionPositions.sumOf { position -> position.first * position.second }

        val walkNodes = positionToNeighbors.keys - intersectionPositions

        val walkNodeToPaths = walkNodes.associateWith {
            getDirectionToPath(start = it, positionToNeighbors = positionToNeighbors)
        }

        val startPosition = scaffoldPositions.single { matrix[it] == ROBOT_TILE }
        val startDirection = up

        val resultCommands = collectWalkCommands(
            startPosition = startPosition,
            startDirection = startDirection,
            connectionNodeToPaths = walkNodeToPaths,
        )

        val commandGroups = compressCommandsIntoGroups(resultCommands)

        check(commandGroups.size == 3)

        val functionOrder = commandGroups.flatMapIndexed { index, group ->
            val groupId = 'A'.plus(index)

            indexesOf(resultCommands, group).map { groupId to it.first }
        }.sortedBy { (_, index) -> index }.map { (groupId) -> groupId }

        val newlineCode = '\n'.code

        val functionOrderInput = functionOrder.joinToString(",") { it.toString() }.map { it.code }.plus(newlineCode)
        val functionsInput = commandGroups.flatMap { group ->
            val inputCommands = robotCommandsToIntCodeInput(group)

            inputCommands + newlineCode
        }
        val robotInput = (functionOrderInput + functionsInput + 'n'.code + newlineCode).map { it.toLong() }

        val initialStatePartTwo = initialState.copy(memory = initialState.memory.putting(0, 2), inputs = robotInput)
        val partTwo = IntCodeRunner.executeInstructions(initialStatePartTwo).outputs.last()

        return AOCAnswer(partOne, partTwo)
    }

    private fun collectWalkCommands(
        startPosition: Position,
        startDirection: Direction,
        connectionNodeToPaths: Map<Position, Map<Direction, List<Position>>>,
    ): List<String> {
        fun collect(position: Position, direction: Direction, seen: PersistentSet<Position>): List<String> {
            if (connectionNodeToPaths.all { (connectionNode) -> connectionNode in seen }) {
                return emptyList()
            }

            val directionToPath = connectionNodeToPaths.getValue(position)

            val (newDirection, pathToTake) = directionToPath.entries.single { (_, path) -> path.last() !in seen }
            val command = getTurn(direction, newDirection).toCommand(pathToTake.size)

            return listOf(command) + collect(pathToTake.last(), newDirection, seen.addingAll(pathToTake))
        }

        return collect(startPosition, startDirection, persistentHashSetOf(startPosition))
    }

    private fun getDirectionToPath(
        start: Position,
        positionToNeighbors: Map<Position, List<Position>>,
    ): Map<Direction, List<Position>> {
        val neighbors = positionToNeighbors.getValue(start)

        val directionToPath = neighbors.associate { neighborPosition ->
            val direction = neighborPosition - start

            val path = generateSequence(neighborPosition) { it.applyDirection(direction) }
                .takeWhile { it in positionToNeighbors }
                .toList()

            direction to path
        }

        return directionToPath
    }

    private fun compressCommandsIntoGroups(commands: List<String>): List<List<String>> {
        val initialState = Triple(commands, commands, listOf<List<String>>())

        // Increase the window as long as that window hits multiple areas of the commands
        val sequence = generateSequence(initialState) { (currentCommands, commandsLeft, groups) ->
            if (currentCommands.isEmpty() || commandsLeft.isEmpty()) {
                return@generateSequence null
            }

            val bestGroup = currentCommands.asSequence()
                .runningFold(emptyList<String>()) { window, command -> window + command }
                .drop(1)
                .map { window -> window to indexesOf(currentCommands, window).size }
                .takeWhile { (window, occurrences) ->
                    occurrences != 1 && robotCommandsToIntCodeInput(window).size <= MAX_FUNCTION_SIZE
                }
                .maxBy { (window, occurrences) -> window.size * occurrences }
                .first

            val newGroups = groups.plusElement(bestGroup)
            val newCommandsLeft = commandsLeft.removeAllSublists(bestGroup)
            val newCommands = removeStartingGroups(currentCommands, newGroups)

            Triple(newCommands, newCommandsLeft, newGroups)
        }

        val (_, _, groups) = sequence.last()

        return groups
    }

    private fun robotCommandsToIntCodeInput(commands: List<String>) = commands
        .flatMap { command -> listOf(command.take(1), command.drop(1)) }
        .joinToString(",") { it }
        .map { it.code }

    private tailrec fun removeStartingGroups(commands: List<String>, groups: List<List<String>>): List<String> {
        val newCommands = groups.fold(commands) { remainingCommands, group ->
            val groupIsAtStart = indexesOf(remainingCommands, group).firstOrNull()?.first == 0

            if (groupIsAtStart) remainingCommands.drop(group.size) else remainingCommands
        }

        return if (newCommands == commands) commands else removeStartingGroups(newCommands, groups)
    }

    private fun List<String>.removeAllSublists(sublist: List<String>): List<String> {
        val indexes = indexesOf(this, sublist)

        val withoutSublists = indexes.foldRight(this) { range, acc ->
            acc.subList(0, range.first) + acc.subList(range.last, acc.size)
        }

        return withoutSublists
    }

    private fun indexesOf(list: List<String>, sublist: List<String>): List<IntRange> =
        list.windowed(sublist.size)
            .withIndex()
            .filter { (_, window) -> window == sublist }
            .map { (index) -> index until index + sublist.size }

    // Given 2 directions, say what rotation / turn happened
    private fun getTurn(current: Direction, next: Direction): Turn =
        Turn.entries.single { turn -> current.rotate(turn) == next }

    private fun Direction.rotate(turn: Turn): Direction {
        val degrees = when (turn) {
            Turn.LEFT -> 90.0
            Turn.RIGHT -> -90.0
        }

        val radians = Math.toRadians(degrees)

        val sinValue = sin(radians)
        val cosValue = cos(radians)

        val newX = first * cosValue - second * sinValue
        val newY = first * sinValue + second * cosValue

        return Direction(newX.toInt(), newY.toInt())
    }

    private fun Turn.toCommand(stepSize: Int): String {
        val turnString = when (this) {
            Turn.LEFT -> "L"
            Turn.RIGHT -> "R"
        }

        return "$turnString$stepSize"
    }

    private operator fun Position.minus(other: Position) = Position(first - other.first, second - other.second)
}
