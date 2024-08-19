package aoc22

import AOCYear
import readInput
import kotlin.math.abs

class Day7 {

    sealed class Node {
        abstract val subDirectories: MutableList<Directory>
        abstract val fileSizes: MutableList<Long>

        data class RootDirectory(
            override val subDirectories: MutableList<Directory>,
            override val fileSizes: MutableList<Long>,
        ) : Node()

        data class Directory(
            val parent: Node,
            val name: String,
            override val subDirectories: MutableList<Directory> = mutableListOf(),
            override val fileSizes: MutableList<Long> = mutableListOf(),
        ) : Node()

        fun size(): Long = fileSizes.sum() + subDirectories.sumOf { it.size() }
    }

    fun solve() {
        val rawInput = readInput("day7.txt", AOCYear.TwentyTwo)

        val root = Node.RootDirectory(mutableListOf(), mutableListOf())

        generateSequence(root as Node to rawInput.drop(1)) { (current, inputs) ->
            if (inputs.isEmpty()) return@generateSequence null

            val command = inputs.first().split(" ").drop(1)

            val nextInputs = inputs.drop(1)

            when (command.first()) {
                "ls" -> {
                    nextInputs.takeWhile { !it.startsWith('$') }.forEach { rawNode ->
                        val (dirOrSize, name) = rawNode.split(" ")

                        if (dirOrSize == "dir") {
                            current.subDirectories.add(Node.Directory(current, name))
                        } else {
                            current.fileSizes.add(dirOrSize.toLong())
                        }
                    }

                    current to nextInputs.dropWhile { !it.startsWith('$') }
                }

                "cd" -> {
                    val changeToDirectory = command[1]

                    if (changeToDirectory == "..") {
                        if (current !is Node.Directory) {
                            error("Received command .. but current node is not a directory!")
                        }

                        current.parent to nextInputs
                    } else {
                        current.subDirectories.single { it.name == changeToDirectory } to nextInputs
                    }
                }

                else -> error("Unknown command ${command.first()}!")
            }
        }.count()

        val sizes: List<Long> = buildList {
            fun naiveDFS(node: Node) {
                node.subDirectories.forEach { naiveDFS(it) }

                add(node.size())
            }

            naiveDFS(root)
        }
        val sizesSorted = sizes.sorted()

        val partOne = sizesSorted.takeWhile { it <= 100000 }.sum()

        val totalSize = 70000000
        val requiredSize = 30000000

        val usedSize = sizesSorted.last()

        val toFree = abs(totalSize - requiredSize - usedSize)

        val partTwo = sizes.sorted().first { it >= toFree }

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }
}
