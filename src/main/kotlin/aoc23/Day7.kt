package aoc23

import AOCYear
import readInput

class Day7 {

    private val cardOrderPart1 = "AKQJT98765432"
    private val cardOrderPart2 = "AKQT98765432J"

    fun solve() {
        val rawInput = readInput("day7.txt", AOCYear.TwentyThree)

        val (cardStrengths, cardStrengthPart2) = listOf(cardOrderPart1, cardOrderPart2)
            .map { it.reversed().mapIndexed { index, char -> char to index }.toMap() }

        val handToBet = rawInput.associate { line ->
            line
                .split(" ")
                .let { (hand, bet) -> hand to bet.toInt() }
        }

        fun Map<String, Int>.groupHandTypes(cardStrengths: Map<Char, Int>, groupSelector: (String) -> List<Int>) = this
            .keys
            .groupBy(groupSelector)
            .mapValues { (_, hands) ->
                hands.sortedWith { a, b ->
                    a.zip(b).firstNotNullOf { (aChar, bChar) ->
                        cardStrengths[aChar]!!.compareTo(cardStrengths[bChar]!!).takeIf { it != 0 }
                    }
                }
            }

        val handTypeToHands = handToBet.groupHandTypes(cardStrengths) { hand ->
            hand.groupingBy { it }.eachCount().values.sortedDescending()
        }

        val handTypeToHandsPart2 = handToBet.groupHandTypes(cardStrengthPart2) { hand ->
            val handWithoutJoker = hand.replace("J", "")
            val jokerCount = hand.length - handWithoutJoker.length

            val cardCounts = handWithoutJoker.groupingBy { it }.eachCount().values.sortedDescending()

            val (highestCountCard, restCards) = cardCounts.run { take(1) to drop(1) }

            val highestWithJoker = highestCountCard
                .map { it + jokerCount }
                .ifEmpty { listOf(jokerCount) }

            highestWithJoker + restCards
        }

        fun Map<List<Int>, List<String>>.totalScore() = keys
            .sortedWith { a, b -> a.zip(b).firstNotNullOf { (aInt, bInt) -> aInt.compareTo(bInt).takeIf { it != 0 } } }
            .flatMap { getValue(it) }
            .mapIndexed { index, hand ->
                val rank = index + 1
                handToBet[hand]!! * rank
            }
            .sum()

        val partOne = handTypeToHands.totalScore()
        val partTwo = handTypeToHandsPart2.totalScore()

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }
}
