package aoc24

import AOCAnswer
import AOCSolution
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import readInput

class Day9 : AOCSolution {

    private sealed interface MemoryBlock {
        data object Free : MemoryBlock

        @JvmInline
        value class Data(val value: Int) : MemoryBlock
    }

    override fun solve(): AOCAnswer {
        val rawInput = readInput("day9.txt", AOCYear.TwentyFour)

        // Digits alternate between indicating the length of a file and the length of free space
        // The last digit is of file length, so we add an extra 0 to indicate the free space and enable pairing
        val diskMap = rawInput.single().map { it.digitToInt() } + 0

        val initialMemory = diskMap.chunked(2).withIndex().flatMap { (blockIndex, memoryBlock) ->
            val (usedSpace, freeSpace) = memoryBlock

            List(usedSpace) { MemoryBlock.Data(blockIndex) } + List(freeSpace) { MemoryBlock.Free }
        }.toPersistentList()

        val partOne = compactMemoryFragmented(initialMemory).sum()
        val partTwo = compactMemoryContiguous(initialMemory).sum()

        return AOCAnswer(partOne, partTwo)
    }

    private fun compactMemoryFragmented(initialMemory: PersistentList<MemoryBlock>): PersistentList<MemoryBlock> {
        val initial = Triple(initialMemory, 0, initialMemory.lastIndex)
        val memoryMoveSequence = generateSequence(initial) { (memory, frontIndex, backIndex) ->
            when {
                frontIndex >= backIndex -> null
                memory[frontIndex] != MemoryBlock.Free -> Triple(memory, frontIndex + 1, backIndex)
                memory[backIndex] !is MemoryBlock.Data -> Triple(memory, frontIndex, backIndex - 1)
                else -> {
                    val newMemory = memory.set(frontIndex, memory[backIndex]).set(backIndex, MemoryBlock.Free)

                    Triple(newMemory, frontIndex + 1, backIndex - 1)
                }
            }
        }

        val (resultMemory) = memoryMoveSequence.last()

        return resultMemory
    }

    private fun compactMemoryContiguous(initialMemory: PersistentList<MemoryBlock>): PersistentList<MemoryBlock> {
        val initialFreeSpace = initialMemory.scanFreeSpace().toPersistentList()

        val initial = Triple(initialMemory, initialMemory.lastIndex, initialFreeSpace)
        val memoryMoveSequence = generateSequence(initial) { (memory, backIndex, freeSpace) ->
            val memoryBlock = memory.getOrNull(backIndex)

            when (memoryBlock) {
                MemoryBlock.Free -> Triple(memory, backIndex - 1, freeSpace)

                is MemoryBlock.Data -> {
                    val memoryId = memoryBlock.value

                    val memorySize = (backIndex downTo 0).takeWhile { index ->
                        val slot = memory[index]

                        slot is MemoryBlock.Data && slot.value == memoryId
                    }.size

                    val freeMemoryIndex = freeSpace.indexOfFirst { it.size() >= memorySize }

                    val freeSpaceIndices = freeSpace.getOrNull(freeMemoryIndex)

                    if (freeSpaceIndices == null || freeSpaceIndices.first > backIndex) {
                        Triple(memory, backIndex - memorySize, freeSpace)
                    } else {
                        val freeSpaceFrom = freeSpaceIndices.first
                        val freeSpaceTo = freeSpaceIndices.last

                        val memoryToFree = backIndex - memorySize + 1..backIndex
                        val memoryToWrite = freeSpaceFrom until freeSpaceFrom + memorySize

                        val unusedFreeMemory = freeSpaceFrom + memorySize..freeSpaceTo

                        val newFreeSpace = if (unusedFreeMemory.size() > 0) {
                            freeSpace.set(freeMemoryIndex, unusedFreeMemory)
                        } else {
                            freeSpace.removeAt(freeMemoryIndex)
                        }

                        val newMemory = memory
                            .replaceRange(memoryToWrite, memory[backIndex])
                            .replaceRange(memoryToFree, MemoryBlock.Free)

                        Triple(newMemory, backIndex - memorySize, newFreeSpace)
                    }
                }
                // Reached the end (start) of the list
                null -> return@generateSequence null
            }
        }

        val (resultMemory) = memoryMoveSequence.last()

        return resultMemory
    }

    private fun List<MemoryBlock>.sum() = this
        .withIndex()
        .sumOf { (index, memoryBlock) ->
            when (memoryBlock) {
                is MemoryBlock.Data -> memoryBlock.value.toLong() * index
                MemoryBlock.Free -> 0L
            }
        }

    private fun IntRange.size() = last - first + 1

    private fun PersistentList<MemoryBlock>.replaceRange(range: IntRange, value: MemoryBlock) =
        range.fold(this) { memory, index -> memory.set(index, value) }

    private fun PersistentList<MemoryBlock>.scanFreeSpace(): List<IntRange> {
        val sequence = generateSequence(0 to emptyList<IntRange>()) { (index, collectedRanges) ->
            when {
                index > lastIndex -> null
                this[index] != MemoryBlock.Free -> index + 1 to collectedRanges
                else -> {
                    val freeBlockSize = (index..lastIndex).takeWhile { index -> this[index] == MemoryBlock.Free }.size
                    val newRange = (index until index + freeBlockSize)

                    (index + freeBlockSize) to collectedRanges.plusElement(newRange)
                }
            }
        }

        val (_, collectedRanges) = sequence.last()

        return collectedRanges
    }
}
