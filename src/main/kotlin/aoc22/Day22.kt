@file:Suppress("MemberVisibilityCanBePrivate")

package aoc22

import AOCAnswer
import AOCSolution
import Direction
import Position
import applyDirection
import getOrNull
import mapToInt
import readInput
import splitBy
import transposed

class Day22 : AOCSolution {

    data class CellConnection(val toCell: Cell.Active, val newDirection: Direction? = null)

    sealed class Cell {
        abstract val row: Int
        abstract val column: Int

        data class Void(override val row: Int, override val column: Int) : Cell()

        data class Active(
            override val row: Int,
            override val column: Int,

            val isWall: Boolean,
            val cubeSection: Int,

            val normalizedRow: Int,
            val normalizedCol: Int,
        ) : Cell() {
            val cellConnections = mutableMapOf<Direction, CellConnection>()
        }
    }

    val up: Direction = -1 to 0
    val down: Direction = 1 to 0
    val left: Direction = 0 to -1
    val right: Direction = 0 to 1

    val directions = listOf(up, down, left, right)

    fun labelCellWithSection(row: Int, column: Int): Pair<Position, Int> {
        val cubeSection = when {
            row in 1..50 && column in 51..100 -> 1
            row in 1..50 && column in 101..150 -> 2
            row in 51..100 && column in 51..100 -> 3
            row in 101..150 && column in 1..50 -> 4
            row in 101..150 && column in 51..100 -> 5
            row in 151..200 && column in 1..50 -> 6
            else -> error("Illegal indices ($row, $column)")
        }
        // Store normalized indexes [1..50]
        val normalizedRow = ((row - 1) % 50) + 1
        val normalizedCol = ((column - 1) % 50) + 1

        return (normalizedRow to normalizedCol) to cubeSection
    }

    override fun solve(): AOCAnswer {
        val rawInput = readInput("day22.txt", AOCYear.TwentyTwo)

        val (matrixInput, instructions) = rawInput.splitBy { isEmpty() }

        val longestRowLength = matrixInput.maxOf { it.length }
        val normalizedInput = matrixInput.map { it.padEnd(longestRowLength, ' ') }

        val baseCells = normalizedInput.mapIndexed { rowI, row ->
            row.mapIndexed { colI, value ->
                if (value.isWhitespace()) {
                    Cell.Void(rowI + 1, colI + 1)
                } else {
                    val (normalizedPos, cubeSection) = labelCellWithSection(rowI + 1, colI + 1)

                    Cell.Active(
                        row = rowI + 1,
                        column = colI + 1,
                        isWall = value == '#',
                        cubeSection = cubeSection,
                        normalizedRow = normalizedPos.first,
                        normalizedCol = normalizedPos.second,
                    )
                }
            }
        }

        // Since we do a ton of mutability, each part gets its own cells
        val cubeCells = baseCells.map { row -> row.map { it.clone() } }
        val simpleCells = baseCells.map { row -> row.map { it.clone() } }

        // Create base connections in the map (No wrapping)
        cubeCells.apply {
            addDirectConnections()
            addCubeWarpConnections()
        }

        simpleCells.apply {
            addDirectConnections()
            addSimpleWarpConnections()
        }

        val commands = parseInstructions(instructions.single(), right)

        val initialSimpleCell = simpleCells.first().first { it is Cell.Active } as Cell.Active
        val (resultCellSimple, resultDirectionSimple) = executeCommands(commands, initialSimpleCell)

        val partOne = (resultCellSimple.row * 1000) + (4 * resultCellSimple.column) +
            directionToResultValue(resultDirectionSimple)

        val initialCubeCell = cubeCells.first().first { it is Cell.Active } as Cell.Active
        val (resultCell, resultDirection) = executeCommands(commands, initialCubeCell)

        val partTwo = (resultCell.row * 1000) + (4 * resultCell.column) + directionToResultValue(resultDirection)

        return AOCAnswer(partOne, partTwo)
    }

    fun executeCommands(commands: List<Pair<Direction, Int>>, initialCell: Cell.Active): Pair<Cell.Active, Direction> {
        val (initialDirection, firstSteps) = commands.first()
        val initial = executeCommand(initialCell, initialDirection, firstSteps)

        val result = commands.drop(1).fold(initial) { (cell, direction), (directionCommand, steps) ->
            val newDirection = makeTurnByDirectionCommand(direction, directionCommand)

            executeCommand(cell, newDirection, steps)
        }

        return result
    }

    fun Cell.clone() = when (this) {
        is Cell.Active -> copy()
        is Cell.Void -> copy()
    }

    fun List<List<Cell>>.addCubeWarpConnections() {
        val cubeSectionToCubes = flatMap { cells -> cells.filterIsInstance<Cell.Active>() }.groupBy { it.cubeSection }

        cubeSectionToCubes.getValue(1).filter { it.row == 1 }.forEach { cell ->
            val new = cubeSectionToCubes[6]!!.first { it.column == 1 && it.row == cell.column + 100 }

            cell.cellConnections[up] = CellConnection(new, right)
            new.cellConnections[left] = CellConnection(cell, down)
        }

        cubeSectionToCubes.getValue(2).filter { it.row == 1 }.forEach { cell ->
            val new = cubeSectionToCubes[6]!!.first { it.row == 200 && it.column == cell.column - 100 }

            cell.cellConnections[up] = CellConnection(new, up)
            new.cellConnections[down] = CellConnection(cell, down)
        }

        cubeSectionToCubes.getValue(2).filter { it.column == 150 }.forEach { cell ->
            val new = cubeSectionToCubes[5]!!.first { it.column == 100 && it.row == 151 - cell.row }

            cell.cellConnections[right] = CellConnection(new, left)
            new.cellConnections[right] = CellConnection(cell, left)
        }

        cubeSectionToCubes.getValue(2).filter { it.row == 50 }.forEach { cell ->
            val new = cubeSectionToCubes[3]!!.first { it.column == 100 && it.row == cell.column - 50 }

            cell.cellConnections[down] = CellConnection(new, left)
            new.cellConnections[right] = CellConnection(cell, up)
        }

        cubeSectionToCubes.getValue(5).filter { it.row == 150 }.forEach { cell ->
            val new = cubeSectionToCubes[6]!!.first { it.normalizedCol == 50 && it.row == cell.column + 100 }

            cell.cellConnections[down] = CellConnection(new, left)
            new.cellConnections[right] = CellConnection(cell, up)
        }

        cubeSectionToCubes.getValue(3).filter { it.column == 51 }.forEach { cell ->
            val new = cubeSectionToCubes[4]!!.first { it.normalizedRow == 1 && it.column == cell.row - 50 }

            cell.cellConnections[left] = CellConnection(new, down)
            new.cellConnections[up] = CellConnection(cell, right)
        }

        cubeSectionToCubes.getValue(1).filter { it.column == 51 }.forEach { cell ->
            val new = cubeSectionToCubes[4]!!.first { it.column == 1 && it.row == 151 - cell.row }

            cell.cellConnections[left] = CellConnection(new, right)
            new.cellConnections[left] = CellConnection(cell, right)
        }
    }

    fun List<List<Cell>>.addSimpleWarpConnections() {
        forEach { row ->
            val firstActiveRow = row.first { it is Cell.Active } as Cell.Active
            val lastActiveRow = row.last { it is Cell.Active } as Cell.Active

            firstActiveRow.cellConnections[left] = CellConnection(lastActiveRow)
            lastActiveRow.cellConnections[right] = CellConnection(firstActiveRow)
        }

        transposed().forEach { col ->
            val firstActiveCol = col.first { it is Cell.Active } as Cell.Active
            val lastActiveCol = col.last { it is Cell.Active } as Cell.Active

            firstActiveCol.cellConnections[up] = CellConnection(lastActiveCol)
            lastActiveCol.cellConnections[down] = CellConnection(firstActiveCol)
        }
    }

    fun List<List<Cell>>.addDirectConnections() {
        // Create base connections in the map (No wrapping)
        forEachIndexed forCells@{ rowIndex, row ->
            row.forEachIndexed { colIndex, cell ->
                if (cell !is Cell.Active) return@forEachIndexed
                val position = rowIndex to colIndex

                directions.forEach { direction ->
                    val newPosition = position.applyDirection(direction)
                    val connectedCell = getOrNull(newPosition) ?: return@forEach

                    if (connectedCell is Cell.Active) {
                        cell.cellConnections[direction] = CellConnection(connectedCell)
                    }
                }
            }
        }
    }

    tailrec fun executeCommand(cell: Cell.Active, direction: Direction, steps: Int): Pair<Cell.Active, Direction> =
        if (steps == 0) {
            cell to direction
        } else {
            val (targetCell, changedDirection) = cell.cellConnections[direction]!!

            if (targetCell.isWall) {
                cell to direction
            } else {
                executeCommand(targetCell, changedDirection ?: direction, steps - 1)
            }
        }

    fun parseInstructions(line: String, startDirection: Direction): List<Pair<Direction, Int>> {
        val values = line.split(Regex("\\D")).mapToInt()
        val directions = line.filter { !it.isDigit() }.map { turnToDirection(it) }

        check(values.size > directions.size) { "Failed to assert missing start direction in instructions" }

        return listOf(startDirection).plus(directions).zip(values)
    }

    fun turnToDirection(turn: Char) = when (turn) {
        'L' -> left
        'R' -> right
        else -> error("Invalid turn $turn")
    }

    fun makeTurnByDirectionCommand(currentDirection: Direction, directionCommand: Direction) = when (currentDirection) {
        left -> when (directionCommand) {
            left -> (down)
            right -> (up)
            else -> error("")
        }
        right -> when (directionCommand) {
            left -> (up)
            right -> (down)
            else -> error("")
        }
        down -> when (directionCommand) {
            left -> (right)
            right -> (left)
            else -> error("")
        }
        up -> when (directionCommand) {
            left -> (left)
            right -> (right)
            else -> error("")
        }
        else -> error("Unknown direction $currentDirection")
    }

    fun directionToResultValue(direction: Direction) = when (direction) {
        right -> 0
        down -> 1
        left -> 2
        up -> 3
        else -> error("Invalid direction $direction")
    }
}
