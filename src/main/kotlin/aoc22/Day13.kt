@file:Suppress("MemberVisibilityCanBePrivate")

package aoc22

import product
import readInput
import splitBy

class Day13 {
    sealed interface Packet {
        data class Collection(val values: List<Packet>) : Packet
        data class Value(val value: Int) : Packet
    }

    fun Packet.compareWith(other: Packet): Int = when {
        this is Packet.Value && other is Packet.Value -> value.compareTo(other.value)

        this is Packet.Value && other is Packet.Collection -> Packet.Collection(listOf(this)).compareWith(other)

        this is Packet.Collection && other is Packet.Value -> compareWith(Packet.Collection(listOf(other)))

        this is Packet.Collection && other is Packet.Collection -> {
            values.zip(other.values).firstNotNullOfOrNull { (a, b) ->
                a.compareWith(b).takeIf { it != 0 }
            } ?: values.size.compareTo(other.values.size)
        }

        else -> error("Cannot happen but compiler isn't that smart")
    }

    fun parseIntoPackets(inputString: String): Packet {
        fun parseCollection(str: String): Pair<String, Packet.Collection> {
            val parsingSequence = generateSequence(str to Packet.Collection(emptyList())) { (string, packet) ->
                val parsed = string.takeWhile { it.isDigit() }.ifEmpty { string.firstOrNull()?.toString() }

                val nextString = string.drop(1)

                when (parsed) {
                    "," -> nextString to packet
                    "[" -> {
                        val (modifiedString, parsedPacket) = parseCollection(nextString)

                        modifiedString to Packet.Collection(packet.values + parsedPacket)
                    }
                    "]", null -> null
                    else -> {
                        val newPacket = Packet.Value(parsed.toInt())

                        nextString to Packet.Collection(packet.values + newPacket)
                    }
                }
            }

            val (modifiedString, newPacket) = parsingSequence.last()

            return modifiedString.removePrefix("]") to newPacket
        }

        return parseCollection(inputString).second.values.single()
    }

    fun solve() {
        val rawInput = readInput("day13.txt", AOCYear.TwentyTwo)

        val packetPairs = rawInput.splitBy { isEmpty() }.map { (a, b) ->
            parseIntoPackets(a) to parseIntoPackets(b)
        }

        val partOne = packetPairs.mapIndexedNotNull { index, (a, b) ->
            index.inc().takeIf { a.compareWith(b) == -1 }
        }.sum()

        val dividerPackets = listOf(parseIntoPackets("[[2]]"), parseIntoPackets("[[6]]"))

        val allPackets = packetPairs.flatMap { it.toList() } + dividerPackets
        val sorted = allPackets.sortedWith { a, b -> a.compareWith(b) }

        val partTwo = dividerPackets.map { sorted.indexOf(it) + 1 }.product()

        println("Part One: $partOne")
        println("Part Two: $partTwo")
    }
}
