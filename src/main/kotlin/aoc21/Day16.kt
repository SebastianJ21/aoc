@file:Suppress("MemberVisibilityCanBePrivate")

package aoc21

import AOCYear
import product
import readInput

sealed class Packet {
    abstract val version: Int
    abstract val typeId: Int
    abstract val bitLength: Int
    abstract val value: Long

    data class Literal(
        override val version: Int,
        override val typeId: Int,
        override val bitLength: Int,
        override val value: Long,
    ) : Packet()

    data class Operator(
        override val version: Int,
        override val typeId: Int,
        override val bitLength: Int,
        val subPackets: List<Packet>,
        override val value: Long,
    ) : Packet()
}

private typealias PacketsAndBitLength = Pair<List<Packet>, Int>

class Day16 {

    fun solve() {
        val rawInput = readInput("day16.txt", AOCYear.TwentyOne)

        val binaryInput = rawInput.single().map { it.digitToInt(16).toString(2).padStart(4, '0') }.joinToString("")

        val packet = binaryInput.parsePacket()

        val partOne = packet.sumVersionNumbers()
        val partTwo = packet.value

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }

    fun Packet.sumVersionNumbers(): Int =
        version + if (this is Packet.Operator) subPackets.sumOf { it.sumVersionNumbers() } else 0

    fun String.parsePacket(): Packet {
        val version = take(3).toInt(2)
        val typeId = drop(3).take(3).toInt(2)

        val packetDataString = drop(6)

        return if (typeId == 4) {
            packetDataString.parseLiteral(version)
        } else {
            val (subPackets, bitLength) = packetDataString.parseOperatorSubPackets()

            val subPacketValues = subPackets.map { it.value }

            val value = when (typeId) {
                0 -> subPacketValues.sum()
                1 -> subPacketValues.product()
                2 -> subPacketValues.min()
                3 -> subPacketValues.max()
                else -> {
                    check(subPacketValues.size == 2)

                    val isOne = when (typeId) {
                        5 -> subPacketValues[0] > subPacketValues[1]
                        6 -> subPacketValues[0] < subPacketValues[1]
                        7 -> subPacketValues[0] == subPacketValues[1]
                        else -> error("Unknown typeId $typeId")
                    }

                    if (isOne) 1 else 0
                }
            }

            Packet.Operator(version, typeId, bitLength, subPackets, value)
        }
    }

    fun String.parseOperatorSubPackets(): PacketsAndBitLength {
        val lengthTypeId = first().digitToInt(2)

        return if (lengthTypeId == 0) {
            val totalLengthInBits = drop(1).take(15).toInt(2)

            val subPacketsString = drop(16).take(totalLengthInBits)

            fun parseSubPackets(string: String): List<Packet> {
                return if (string.isEmpty()) {
                    emptyList()
                } else {
                    val packet = string.parsePacket()
                    val nextStr = string.drop(packet.bitLength)
                    listOf(packet) + parseSubPackets(nextStr)
                }
            }

            val subPackets = parseSubPackets(subPacketsString)

            // SubPacket length + version + type + lengthType + totalLength
            val bitLength = totalLengthInBits + 22

            subPackets to bitLength
        } else {
            val numberOfSubPackets = drop(1).take(11).toInt(2)

            val packetsStart = drop(12)

            fun parseSubPackets(string: String, n: Int): List<Packet> {
                return if (n == 0) {
                    emptyList()
                } else {
                    val packet = string.parsePacket()
                    val nextStr = string.drop(packet.bitLength)
                    listOf(packet) + parseSubPackets(nextStr, n - 1)
                }
            }

            val subPackets = parseSubPackets(packetsStart, numberOfSubPackets)

            // SubPacket length + version + type + lengthType + numberOfSubPackets
            val bitLength = subPackets.sumOf { it.bitLength } + 18

            subPackets to bitLength
        }
    }

    fun String.parseLiteral(version: Int): Packet.Literal {
        val chunks = chunked(5)

        val oneBitChunks = chunks.takeWhile { it.first() == '1' }

        val encodedNumber = oneBitChunks
            .plusElement(chunks[oneBitChunks.size])
            .joinToString("") { it.drop(1) }
            .toLong(2)

        // Chunks + Version (3 bits) + TypeId (3 bits)
        val bitLength = (oneBitChunks.size.inc() * 5) + 6

        return Packet.Literal(version, 4, bitLength, encodedNumber)
    }
}
