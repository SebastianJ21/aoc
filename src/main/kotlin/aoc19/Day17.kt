@file:Suppress("MemberVisibilityCanBePrivate")

package aoc19

import AOCYear
import Direction
import Position
import aoc19.IntCodeRunner.Companion.executeInstructions
import applyDirection
import getOrNull
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentHashSetOf
import mapToLong
import printAOCAnswers
import readInput
import splitBy
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

class Day17 {

    val up: Direction = -1 to 0
    val down: Direction = 1 to 0
    val left: Direction = 0 to -1
    val right: Direction = 0 to 1

    val directions = listOf(up, left, down, right)

    enum class Turn { LEFT, RIGHT }

    fun Direction.rotate(turn: Turn): Direction {
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

    val scaffoldTile = '#'
    val robotTile = '^'

    // Given 2 directions, say what rotation / turn happened
    fun getTurn(currentDirection: Direction, nextDirection: Direction): Turn = Turn.entries.single { turn ->
        currentDirection.rotate(turn) == nextDirection
    }

    fun solve() {
        val rawInput = readInput("day17.txt", AOCYear.Nineteen)

        val inputInstructions = rawInput.single().split(',').mapToLong()
        val initialState = ExecutionState.fromList(inputInstructions)

        val cameraOutputs = executeInstructions(initialState).outputs

        val matrix = cameraOutputs.splitBy({ this == 10L }) { Char(it.toInt()) }.filter { it.isNotEmpty() }

        fun Char.isScaffold() = this == scaffoldTile || this == robotTile

        val positionToNeighborsPairs = matrix.flatMapIndexed { rowI, row ->
            row.mapIndexedNotNull { colI, tile ->
                if (!tile.isScaffold()) return@mapIndexedNotNull null

                val position = Position(rowI, colI)

                val neighbors = directions.mapNotNull { direction ->
                    position.applyDirection(direction).takeIf { matrix.getOrNull(it)?.isScaffold() == true }
                }

                position to neighbors
            }
        }

        val intersectionNodes = positionToNeighborsPairs.filter { (_, neighbors) -> neighbors.size == 4 }
        val partOne = intersectionNodes.sumOf { (position) -> position.first * position.second }

        val positionToNeighbors = positionToNeighborsPairs.toMap()

        val startPosition = positionToNeighbors.keys.single { position ->
            matrix.getOrNull(position) == robotTile
        }
        val startDirection = up

        val connectionPaths = findLongestWalksForConnectionNodes(positionToNeighbors, startPosition)

        val resultCommands =
            collectWalkCommands(
                startPosition,
                startDirection,
                emptyList(),
                persistentHashSetOf(startPosition),
                connectionPaths,
            )

        val commandGroups = compressCommandsIntoGroups(resultCommands)

        check(commandGroups.size == 3)

        val functionOrder = commandGroups.flatMapIndexed { index, group ->
            val groupId = 'A'.plus(index)

            indexesOf(resultCommands, group).map { groupId to it }
        }.sortedBy { (_, index) -> index }.map { (groupId) -> groupId }

        val newlineCode = '\n'.code

        val functionOrderInput = functionOrder.joinToString(",") { it.toString() }.map { it.code }.plus(newlineCode)
        val functionsInput = commandGroups.flatMap { group ->
            val inputCommands = robotCommandsToIntCodeInput(group)

            inputCommands + newlineCode
        }
        val robotInput = (functionOrderInput + functionsInput + 'n'.code + newlineCode).map { it.toLong() }

        val initialStatePartTwo = initialState.copy(memory = initialState.memory.put(0, 2), inputs = robotInput)
        val partTwo = executeInstructions(initialStatePartTwo).outputs.last()

        printAOCAnswers(partOne, partTwo)
    }

    fun robotCommandsToIntCodeInput(commands: List<String>) = commands
        .flatMap { command -> listOf(command.take(1), command.drop(1)) }
        .joinToString(",") { it }
        .map { it.code }

    fun findLongestWalksForConnectionNodes(
        positionToNeighbors: Map<Position, List<Position>>,
        start: Position,
    ): Map<Position, Map<Direction, List<Position>>> {
        fun getLongestWalks(from: Position): Map<Direction, List<Position>> {
            val neighbors = positionToNeighbors.getValue(from)

            val paths = neighbors.associate { initialNeighbor ->
                val direction: Direction = initialNeighbor - from

                val walkDirectionSequence = generateSequence(initialNeighbor) { position ->
                    val nextPosition = position.applyDirection(direction)

                    nextPosition.takeIf { it in positionToNeighbors }
                }

                val pathWalked = walkDirectionSequence.toList()

                direction to pathWalked
            }

            return paths
        }

        val turnNodes = positionToNeighbors.filter { (_, neighbors) -> neighbors.size == 2 }

        val walkNodes = turnNodes.keys + listOf(start)

        val nodeToLongestWalks = walkNodes.associateWith { nodePosition -> getLongestWalks(nodePosition) }

        return nodeToLongestWalks
    }

    fun compressCommandsIntoGroups(commands: List<String>): List<List<String>> {
        val initialState = Triple(commands, commands, listOf<List<String>>())

        val sequence = generateSequence(initialState) { (currentCommands, commandsLeft, groups) ->
            if (currentCommands.isEmpty() || commandsLeft.isEmpty()) {
                return@generateSequence null
            }

            val possibleGroups: List<Pair<List<String>, Int>> = buildList {
                currentCommands.forEachIndexed { index, command ->
                    if (index == 0) {
                        add(listOf(command) to 0)

                        return@forEachIndexed
                    }

                    val selectedWindow = last().first + command

                    val windowIndexes = indexesOf(currentCommands, selectedWindow)

                    if (windowIndexes.size == 1 || robotCommandsToIntCodeInput(selectedWindow).size > 20) {
                        return@buildList
                    }

                    add(selectedWindow to windowIndexes.size)
                }
            }

            val (bestGroup) = possibleGroups.maxBy { (window, occurrences) -> window.size * occurrences }

            val newGroups = groups.plusElement(bestGroup)
            val newCommandsLeft = commandsLeft.removeAllSublists(bestGroup)
            val newCommands = removeStartingGroups(currentCommands, newGroups)

            Triple(newCommands, newCommandsLeft, newGroups)
        }

        val (_, _, groups) = sequence.last()

        return groups
    }

    fun removeStartingGroups(initialCommands: List<String>, groups: List<List<String>>): List<String> {
        val newCommands = groups.fold(initialCommands) { remainingCommands, group ->
            val groupIsAtStart = indexesOf(remainingCommands, group).firstOrNull() == 0

            if (groupIsAtStart) remainingCommands.drop(group.size) else remainingCommands
        }

        return if (newCommands == initialCommands) initialCommands else removeStartingGroups(newCommands, groups)
    }

    fun List<String>.removeAllSublists(sublist: List<String>): List<String> {
        val sublistIndexes = indexesOf(this, sublist)

        val removedSublists = sublistIndexes.foldRight(this) { index, acc ->
            val left = acc.subList(0, max(index, 0))
            val right = acc.subList(index + sublist.size, acc.size)

            left + right
        }

        return removedSublists
    }

    fun indexesOf(list: List<String>, sublist: List<String>): List<Int> {
        val endIndex = list.size - sublist.size

        val (_, foundIndices) = generateSequence(0 to emptyList<Int>()) { (index, foundIndices) ->

            when {
                index > endIndex -> null
                list.subList(index, index + sublist.size) == sublist -> {
                    index + sublist.size to foundIndices.plus(index)
                }
                else -> index + 1 to foundIndices
            }
        }.last()

        return foundIndices
    }

    fun Turn.toCommand(stepSize: Int): String {
        val turnString = when (this) {
            Turn.LEFT -> "L"
            Turn.RIGHT -> "R"
        }

        return "$turnString$stepSize"
    }

    fun collectWalkCommands(
        position: Position,
        direction: Direction,
        pathTaken: List<String>,
        seen: PersistentSet<Position>,
        connectionNodeToPaths: Map<Position, Map<Direction, List<Position>>>,
    ): List<String> {
        if (connectionNodeToPaths.all { (connectionNode) -> connectionNode in seen }) {
            return pathTaken
        }

        val directionToPath = connectionNodeToPaths.getValue(position)

        val (newDirection, pathToTake) = directionToPath.entries.single { (_, path) -> path.last() !in seen }

        val turn = getTurn(direction, newDirection)

        val newPathTaken = pathTaken + turn.toCommand(pathToTake.size)
        val newSeen = seen.addAll(pathToTake)

        return collectWalkCommands(pathToTake.last(), newDirection, newPathTaken, newSeen, connectionNodeToPaths)
    }

    operator fun Position.minus(other: Position) = Position(first - other.first, second - other.second)
}
