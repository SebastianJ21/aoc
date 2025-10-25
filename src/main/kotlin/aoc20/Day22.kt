@file:Suppress("MemberVisibilityCanBePrivate")

package aoc20

import AOCYear
import mapToInt
import readInput
import splitBy

class Day22 {

    fun solve() {
        val rawInput = readInput("day22.txt", AOCYear.Twenty)

        val numbers = (1..10L).reduce { acc, num -> acc * num * num }

        val (startDeck1, startDeck2) = rawInput.splitBy { it.isEmpty() }.map { it.drop(1).mapToInt() }

        val partOne = playGame(startDeck1, startDeck2)
            .toList()
            .first { it.isNotEmpty() }
            .reversed()
            .mapIndexed { index, num -> num * (index + 1) }
            .sum()

        val partTwo = playRecursiveGame(startDeck1, startDeck2)
            .toList()
            .first { it.isNotEmpty() }
            .reversed()
            .mapIndexed { index, num -> num * (index + 1) }
            .sum()

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }

    fun playGame(deck1: List<Int>, deck2: List<Int>): Pair<List<Int>, List<Int>> {
        val gameSequence = generateSequence(deck1 to deck2) { (deck1, deck2) ->
            if (deck1.isEmpty() || deck2.isEmpty()) return@generateSequence null

            val card1 = deck1.first()
            val card2 = deck2.first()

            val restDeck1 = deck1.drop(1)
            val restDeck2 = deck2.drop(1)

            if (card1 > card2) {
                restDeck1 + card1 + card2 to restDeck2
            } else {
                restDeck1 to restDeck2 + card2 + card1
            }
        }

        return gameSequence.last()
    }

    fun playRecursiveGame(deck1: List<Int>, deck2: List<Int>): Pair<List<Int>, List<Int>> {
        val seen1 = hashSetOf<Int>()
        val seen2 = hashSetOf<Int>()

        val gameSequence = generateSequence(deck1 to deck2) { (deck1, deck2) ->
            if (deck1.isEmpty() || deck2.isEmpty()) return@generateSequence null

            if (!seen1.add(deck1.hashCode()) || !seen2.add(deck2.hashCode())) {
                return@generateSequence deck1 to emptyList()
            }

            val card1 = deck1.first()
            val card2 = deck2.first()

            val restDeck1 = deck1.drop(1)
            val restDeck2 = deck2.drop(1)

            val is1Winner = if (card1 <= restDeck1.size && card2 <= restDeck2.size) {
                val (player1Result) = playRecursiveGame(restDeck1.take(card1), restDeck2.take(card2))

                player1Result.isNotEmpty()
            } else {
                card1 > card2
            }

            if (is1Winner) {
                restDeck1 + card1 + card2 to restDeck2
            } else {
                restDeck1 to restDeck2 + card2 + card1
            }
        }

        return gameSequence.last()
    }
}
