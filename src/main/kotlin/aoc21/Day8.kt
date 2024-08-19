package aoc21

import readInput

class Day8 {

    private val segmentsToDigit = mapOf(
        listOf('a', 'b', 'c', 'e', 'f', 'g') to 0,
        listOf('c', 'f') to 1,
        listOf('a', 'c', 'd', 'e', 'g') to 2,
        listOf('a', 'c', 'd', 'f', 'g') to 3,
        listOf('b', 'c', 'd', 'f') to 4,
        listOf('a', 'b', 'd', 'f', 'g') to 5,
        listOf('a', 'b', 'd', 'e', 'f', 'g') to 6,
        listOf('a', 'c', 'f') to 7,
        listOf('a', 'b', 'c', 'd', 'e', 'f', 'g') to 8,
        listOf('a', 'b', 'c', 'd', 'f', 'g') to 9,
    )

    private val allSegments = listOf('a', 'b', 'c', 'd', 'e', 'f', 'g')

    fun solve() {
        val rawInput = readInput("day8.txt", AOCYear.TwentyOne)

        val input = rawInput.map { line ->
            line.split(" | ")
                .let { (signalPatters, message) ->
                    signalPatters.split(" ") to message.split(" ")
                }
        }

        val partOne = input.sumOf { (encryptedSegments, message) ->
            val encryptedToDecrypted = encryptedSegments.extractSegmentsMapping()

            message.count { encryptedSegment ->
                encryptedSegment.translateUsing(encryptedToDecrypted) in listOf(1, 4, 7, 8)
            }
        }

        val partTwo = input.sumOf { (encryptedSegments, message) ->
            val encryptedToDecrypted = encryptedSegments.extractSegmentsMapping()

            val translatedDigits = message.map { it.translateUsing(encryptedToDecrypted) }
            // Join the digits into a number
            translatedDigits.joinToString("").toInt()
        }
        println("Part one: $partOne")
        println("Part two: $partTwo")
    }

    private fun String.translateUsing(encryptedToDecrypted: Map<Char, Char>): Int {
        val decrypted = map { encryptedToDecrypted.getValue(it) }.sorted()

        return segmentsToDigit.getValue(decrypted)
    }

    private fun List<String>.extractSegmentsMapping(): Map<Char, Char> {
        val one = single { it.length == 2 }
        val seven = single { it.length == 3 }
        val four = single { it.length == 4 }
        val eight = single { it.length == 7 }

        // Only 0, 6, 9 use 6 digits
        val sixLengthGroup = filter { it.length == 6 }

        // Only 9 shares all chars with 4
        val nine = sixLengthGroup.single { four.all { char -> char in it } }

        // Without 9, only 0 shares all chars with 7
        val zero = sixLengthGroup.single { it != nine && seven.all { char -> char in it } }

        // Last remaining 6 length digit
        val six = sixLengthGroup.single { it != nine && it != zero }

        // Only 3 has length 5 and shares all chars with 7
        val three = single { it.length == 5 && seven.all { char -> char in it } }

        val a = seven.single { it !in one }
        val g = nine.single { it !in four && it != a }
        val d = three.single { it !in seven && it != g }
        val b = four.single { it !in one && it != d }
        val e = eight.single { it !in nine }
        val c = seven.single { it !in six }
        val f = one.single { it != c }

        return listOf(a, b, c, d, e, f, g).zip(allSegments).toMap()
    }
}
