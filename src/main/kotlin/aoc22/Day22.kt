@file:Suppress("MemberVisibilityCanBePrivate")

package aoc22

import Position
import applyDirection
import convertInputToMatrix
import getOrNull
import mapToInt
import readInput
import splitBy
import transposed

class Day22 {

    data class CellConnection(
        val toCell: Cell.Active,
        val newDirection: String? = null,
    )

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
            lateinit var left: CellConnection
            lateinit var right: CellConnection
            lateinit var up: CellConnection
            lateinit var down: CellConnection
        }
    }

    val up = -1 to 0
    val down = 1 to 0
    val left = 0 to -1
    val right = 0 to 1

    private val directions = listOf(up, down, left, right)

    private fun labelCellWithSection(row: Int, column: Int): Pair<Position, Int> {
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

    fun solve() {
        val rawInput = readInput("day22.txt", AOCYear.TwentyTwo)

        val (matrixInput, instructions) = rawInput.splitBy { isEmpty() }

        val longestRowLength = matrixInput.maxOf { it.length }
        val normalizedMatrixInput = matrixInput.map { it.padEnd(longestRowLength, ' ') }

        val baseCells = convertInputToMatrix(normalizedMatrixInput) { value, (rowI, colI) ->
            when {
                value.isWhitespace() -> Cell.Void(rowI + 1, colI + 1)

                else -> {
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

        val commands = parseInstructions(instructions.single())

        val (initialDirection, initialMovement) = "R" to 31
        val initialCommand = directionToFunction(initialDirection)

        val initialCell = cubeCells.first().first { it is Cell.Active } as Cell.Active

        val firstExec = executeCommand(initialCell, initialCommand, initialMovement)

        // The first exec should not change direction mid-execution
        check(firstExec.second == null)

        val initialCellSimple = simpleCells.first().first { it is Cell.Active } as Cell.Active

        val (resultCellSimple, resultDirectionSimple) = executeCommands(commands, initialCellSimple, initialDirection)

        val partOne = (resultCellSimple.row * 1000) + (4 * resultCellSimple.column) +
            directionToResultValue(resultDirectionSimple)

        val (resultCell, resultDirection) = executeCommands(commands, firstExec.first, initialDirection)

        val partTwo = (resultCell.row * 1000) + (4 * resultCell.column) + directionToResultValue(resultDirection)

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }

    fun executeCommands(
        commands: List<Pair<String, Int>>,
        initialCell: Cell.Active,
        initialDirection: String,
    ): Pair<Cell.Active, String> {
        val result = commands.fold(initialCell to initialDirection) {
                (cell, direction), (movement, steps) ->

            val (movementFn, newDirection) = getMovementFuncByDirection(direction, movement)

            val (newNode, changedDirection) = executeCommand(cell, movementFn, steps)

            newNode to (changedDirection ?: newDirection)
        }

        return result
    }

    fun Cell.clone() = when (this) {
        is Cell.Active -> copy()
        is Cell.Void -> copy()
    }

    fun List<List<Cell>>.addCubeWarpConnections() {
        val cubeSectionToCubes = flatten().filterIsInstance<Cell.Active>().groupBy { it.cubeSection }

        cubeSectionToCubes.getValue(1).filter { it.row == 1 }.forEach { cell ->
            val new = cubeSectionToCubes[6]!!.first { it.column == 1 && it.row == cell.column + 100 }
            cell.up = CellConnection(new, "R")
            new.left = CellConnection(cell, "D")
        }

        cubeSectionToCubes.getValue(2).filter { it.row == 1 }.forEach { cell ->
            val new = cubeSectionToCubes[6]!!.first { it.row == 200 && it.column == cell.column - 100 }
            cell.up = CellConnection(new, "U")
            new.down = CellConnection(cell, "D")
        }

        cubeSectionToCubes.getValue(2).filter { it.column == 150 }.forEach { cell ->
            val new = cubeSectionToCubes[5]!!.first { it.column == 100 && it.row == 151 - cell.row }
            cell.right = CellConnection(new, "L")
            new.right = CellConnection(cell, "L")
        }

        cubeSectionToCubes.getValue(2).filter { it.row == 50 }.forEach { cell ->
            val new = cubeSectionToCubes[3]!!.first { it.column == 100 && it.row == cell.column - 50 }
            cell.down = CellConnection(new, "L")
            new.right = CellConnection(cell, "U")
        }

        cubeSectionToCubes.getValue(5).filter { it.row == 150 }.forEach { cell ->
            val new = cubeSectionToCubes[6]!!.first { it.normalizedCol == 50 && it.row == cell.column + 100 }
            cell.down = CellConnection(new, "L")
            new.right = CellConnection(cell, "U")
        }

        cubeSectionToCubes.getValue(3).filter { it.column == 51 }.forEach { cell ->
            val new = cubeSectionToCubes[4]!!.first { it.normalizedRow == 1 && it.column == cell.row - 50 }
            cell.left = CellConnection(new, "D")
            new.up = CellConnection(cell, "R")
        }

        cubeSectionToCubes.getValue(1).filter { it.column == 51 }.forEach { cell ->
            val new = cubeSectionToCubes[4]!!.first { it.column == 1 && it.row == 151 - cell.row }
            cell.left = CellConnection(new, "R")
            new.left = CellConnection(cell, "R")
        }
    }

    fun List<List<Cell>>.addSimpleWarpConnections() {
        forEach { row ->
            val firstActive = row.first { it is Cell.Active } as Cell.Active
            val lastActive = row.last { it is Cell.Active } as Cell.Active

            firstActive.left = CellConnection(lastActive)
            lastActive.right = CellConnection(firstActive)
        }

        transposed().forEach { row ->
            val firstActive = row.first { it is Cell.Active } as Cell.Active
            val lastActive = row.last { it is Cell.Active } as Cell.Active

            firstActive.up = CellConnection(lastActive)
            lastActive.down = CellConnection(firstActive)
        }
    }

    fun List<List<Cell>>.addDirectConnections() {
        // Create base connections in the map (No wrapping)
        forEachIndexed forCells@{ rowIndex, row ->
            row.forEachIndexed { colIndex, cell ->
                if (cell !is Cell.Active) return@forEachIndexed
                val position = rowIndex to colIndex

                val (up, down, left, right) = directions.map { direction ->
                    val newPosition = position.applyDirection(direction)

                    getOrNull(newPosition)
                        ?.takeIf { it is Cell.Active }
                        ?.let { CellConnection(it as Cell.Active) }
                }

                up?.let { cell.up = it }
                down?.let { cell.down = it }
                left?.let { cell.left = it }
                right?.let { cell.right = it }
            }
        }
    }

    private fun directionToResultValue(direction: String) = when (direction) {
        "R" -> 0
        "D" -> 1
        "L" -> 2
        "U" -> 3
        else -> error("Illegal direction $direction")
    }

    tailrec fun executeCommand(
        cell: Cell.Active,
        movement: (Cell.Active) -> CellConnection,
        steps: Int,
        newDirection: String? = null,
    ): Pair<Cell.Active, String?> = when (steps) {
        0 -> cell to newDirection
        else -> {
            val (targetCell, changedDirection) = movement(cell)

            when {
                targetCell.isWall -> cell to newDirection
                changedDirection != null -> {
                    val newMovementFn = directionToFunction(changedDirection)

                    executeCommand(targetCell, newMovementFn, steps - 1, changedDirection)
                }
                else -> executeCommand(targetCell, movement, steps - 1, newDirection)
            }
        }
    }

    private fun directionToFunction(direction: String) = when (direction) {
        "U" -> { cell: Cell.Active -> cell.up }
        "D" -> { cell: Cell.Active -> cell.down }
        "L" -> { cell: Cell.Active -> cell.left }
        "R" -> { cell: Cell.Active -> cell.right }
        else -> error("Illegal direction $direction")
    }

    private fun parseInstructions(line: String): List<Pair<String, Int>> {
        val values = line
            .map { if (it.isDigit()) it else ',' }
            .joinToString("")
            .split(",")
            .mapToInt()

        val directions = line
            .filter { !it.isDigit() }
            .map { it.toString() }

        return directions.zip(values.drop(1))
    }

    private fun getMovementFuncByDirection(currentDirection: String, directionCommand: String) =
        when (currentDirection) {
            "L" -> when (directionCommand) {
                "L" -> (directionToFunction("D") to "D")
                "R" -> (directionToFunction("U") to "U")
                else -> error("")
            }
            "R" -> when (directionCommand) {
                "L" -> (directionToFunction("U") to "U")
                "R" -> (directionToFunction("D") to "D")
                else -> error("")
            }
            "D" -> when (directionCommand) {
                "L" -> (directionToFunction("R") to "R")
                "R" -> (directionToFunction("L") to "L")
                else -> error("")
            }
            "U" -> when (directionCommand) {
                "L" -> (directionToFunction("L") to "L")
                "R" -> (directionToFunction("R") to "R")
                else -> error("")
            }
            else -> error("Illegal operation")
        }
}
