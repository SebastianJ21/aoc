package aoc24

import AOCAnswer
import AOCSolution
import Direction
import Position
import applyDirection
import at
import get
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import positionsOf
import readInput
import splitBy

class Day15 : AOCSolution {

    val up = -1 at 0
    val down = 1 at 0
    val left = 0 at -1
    val right = 0 at 1

    private enum class Tile {
        OBSTACLE,
        BOX,
        ROBOT,
        EMPTY,
    }

    override fun solve(): AOCAnswer {
        val rawInput = readInput("day15.txt", AOCYear.TwentyFour)

        val (gridLines, instructionLines) = rawInput.splitBy { it.isEmpty() }

        val instructions = instructionLines.flatMap { line ->
            line.map { char ->
                when (char) {
                    '^' -> up
                    '>' -> right
                    '<' -> left
                    'v' -> down
                    else -> error("Unknown instruction: $char")
                }
            }
        }

        val grid = gridLines.map { line ->
            line.map {
                when (it) {
                    '#' -> Tile.OBSTACLE
                    'O' -> Tile.BOX
                    '@' -> Tile.ROBOT
                    '.' -> Tile.EMPTY
                    else -> error("Unknown tile: $it")
                }
            }.toPersistentList()
        }.toPersistentList()

        val initialRobotPosition = grid.positionsOf { it == Tile.ROBOT }.first()

        val (finalGrid) = instructions.fold(grid to initialRobotPosition) { (grid, robotPosition), instruction ->
            makeMove(grid, robotPosition, instruction)
        }

        val expandedGrid = grid.map { row ->
            row.flatMap { tile ->
                if (tile == Tile.ROBOT) {
                    listOf(tile, Tile.EMPTY)
                } else {
                    listOf(tile, tile)
                }
            }.toPersistentList()
        }.toPersistentList()

        val expandedGridRobotPosition = expandedGrid.positionsOf { it == Tile.ROBOT }.first()

        val (finalGrid2) = instructions
            .fold(expandedGrid to expandedGridRobotPosition) { (grid, robotPosition), instruction ->
                makeMove(grid, robotPosition, instruction, doubleBox = true)
            }

        val partOne = finalGrid.positionsOf { it == Tile.BOX }.sumOf { (rowI, colI) -> 100L * rowI + colI }

        val partTwo = finalGrid2.positionsOf { it == Tile.BOX }
            .windowed(2, 2) { boxes -> boxes.first() } // Keep only the 'left' box
            .sumOf { (rowI, colI) -> 100L * rowI + colI }

        return AOCAnswer(partOne, partTwo)
    }

    private fun makeMove(
        grid: PersistentList<PersistentList<Tile>>,
        position: Position,
        direction: Direction,
        doubleBox: Boolean = false,
    ): Pair<PersistentList<PersistentList<Tile>>, Position> {
        val contactPosition = position.applyDirection(direction)

        return when (grid[contactPosition]) {
            // Cannot move
            Tile.OBSTACLE -> grid to position
            // Make a move
            Tile.EMPTY -> grid.set(contactPosition, grid[position]).set(position, Tile.EMPTY) to contactPosition
            // Try to move the box
            Tile.BOX -> {
                val afterBoxMovedGrid = if (doubleBox) {
                    val (box1, box2) = determineBoxPositions(contactPosition, grid)

                    moveDoubleBox(grid, box1, box2, direction)
                } else {
                    val (afterBoxMovedGrid) = makeMove(grid, contactPosition, direction)
                    afterBoxMovedGrid
                }

                // Box moved
                if (afterBoxMovedGrid[contactPosition] == Tile.EMPTY) {
                    afterBoxMovedGrid.set(contactPosition, grid[position]).set(position, Tile.EMPTY) to contactPosition
                } else {
                    grid to position
                }
            }
            Tile.ROBOT -> robotEncounteredError()
        }
    }

    private fun moveDoubleBox(
        grid: PersistentList<PersistentList<Tile>>,
        boxLeft: Position,
        boxRight: Position,
        direction: Direction,
    ): PersistentList<PersistentList<Tile>> {
        require(boxLeft.applyDirection(right) == boxRight)
        require(grid[boxLeft] == Tile.BOX && grid[boxRight] == Tile.BOX)

        return when (direction) {
            left -> {
                val contactTilePosition = boxLeft.applyDirection(left)

                val afterMoveGrid = when (grid[contactTilePosition]) {
                    Tile.BOX -> moveDoubleBox(
                        grid = grid,
                        boxLeft = contactTilePosition.applyDirection(left),
                        boxRight = contactTilePosition,
                        direction = left,
                    )
                    Tile.OBSTACLE -> grid
                    Tile.EMPTY -> grid
                    Tile.ROBOT -> robotEncounteredError()
                }

                if (afterMoveGrid[contactTilePosition] == Tile.EMPTY) {
                    // ##...[][]...##
                    // ##..[].[]...##
                    // ##..[][]....##
                    afterMoveGrid.set(contactTilePosition, Tile.BOX).set(boxRight, Tile.EMPTY)
                } else {
                    grid
                }
            }
            right -> {
                val contactTilePosition = boxRight.applyDirection(right)

                val afterMoveGrid = when (grid[contactTilePosition]) {
                    Tile.BOX -> moveDoubleBox(
                        grid = grid,
                        boxLeft = contactTilePosition,
                        boxRight = contactTilePosition.applyDirection(right),
                        direction = right,
                    )
                    Tile.OBSTACLE -> grid

                    Tile.EMPTY -> grid
                    Tile.ROBOT -> robotEncounteredError()
                }

                if (afterMoveGrid[contactTilePosition] == Tile.EMPTY) {
                    // ##..[][]..##
                    // ##..[].[]..##
                    // ##...[][]...##
                    afterMoveGrid.set(contactTilePosition, Tile.BOX).set(boxLeft, Tile.EMPTY)
                } else {
                    grid
                }
            }
            up, down -> {
                val contactTile1Position = boxLeft.applyDirection(direction)

                val gridAfterMove1 = when (grid[contactTile1Position]) {
                    Tile.OBSTACLE -> grid
                    Tile.EMPTY -> grid
                    Tile.BOX -> {
                        val (nextBox1, nextBox2) = determineBoxPositions(contactTile1Position, grid)

                        moveDoubleBox(grid, nextBox1, nextBox2, direction)
                    }
                    Tile.ROBOT -> robotEncounteredError()
                }

                // If the contact tile 1 is not empty -> early return
                if (gridAfterMove1[contactTile1Position] != Tile.EMPTY) return grid

                val contactTile2Position = boxRight.applyDirection(direction)

                val gridAfterMove2 = when (gridAfterMove1[contactTile2Position]) {
                    Tile.OBSTACLE -> gridAfterMove1
                    Tile.EMPTY -> gridAfterMove1
                    Tile.BOX -> {
                        val (nextBox1, nextBox2) = determineBoxPositions(contactTile2Position, gridAfterMove1)

                        moveDoubleBox(gridAfterMove1, nextBox1, nextBox2, direction)
                    }
                    Tile.ROBOT -> robotEncounteredError()
                }

                // If the contact tile 2 is not empty -> early return
                if (gridAfterMove2[contactTile2Position] != Tile.EMPTY) return grid

                gridAfterMove2
                    .set(contactTile1Position, Tile.BOX).set(contactTile2Position, Tile.BOX)
                    .set(boxLeft, Tile.EMPTY).set(boxRight, Tile.EMPTY)
            }
            else -> error("Unknown direction $direction")
        }
    }

    // Count how many boxes are before boxPosition to determine whether it is open or closed
    // Similar principle as open-closed brackets algorithm
    private fun determineBoxPositions(boxPosition: Position, grid: List<List<Tile>>): Pair<Position, Position> {
        val (row, col) = boxPosition

        val boxCount = grid[row].subList(0, col).count { it == Tile.BOX }

        return if (boxCount % 2 == 0) {
            boxPosition to (row at col + 1)
        } else {
            (row at col - 1) to boxPosition
        }
    }

    private fun <T> PersistentList<PersistentList<T>>.set(position: Position, value: T) =
        this.set(position.first, this[position.first].set(position.second, value))

    private fun robotEncounteredError(): Nothing = error("Robot tile should not be encountered while making a move!")
}
