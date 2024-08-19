package aoc20

import AOCYear
import readInput
import splitBy

class Day4 {

    fun solve() {
        val rawInput = readInput("day4.txt", AOCYear.Twenty)

        val rawPassports = rawInput.splitBy { isEmpty() }

        val blankCid = "cid" to "420"

        val allEntriesPassports = rawPassports.mapNotNull { rawPassport ->
            val entryFields = rawPassport.joinToString(" ").split(" ").associate { fullEntry ->
                val (entry, value) = fullEntry.split(':')
                entry to value
            } + blankCid

            entryFields.takeIf { it.keys.size == 8 }
        }

        val partOne = allEntriesPassports.size

        val hclValidation = (0..9).map { it.toString().single() } + listOf('a', 'b', 'c', 'd', 'e', 'f')
        val eclValidation = listOf("amb", "blu", "brn", "gry", "grn", "hzl", "oth")

        val validPassports = allEntriesPassports.filter { passportEntries ->
            passportEntries.all { (entry, rawValue) ->
                when (entry) {
                    "byr" -> rawValue.length == 4 && rawValue.toInt() in 1920..2002
                    "iyr" -> rawValue.length == 4 && rawValue.toInt() in 2010..2020
                    "eyr" -> rawValue.length == 4 && rawValue.toInt() in 2020..2030
                    "hgt" -> {
                        val number = rawValue.takeWhile { it.isDigit() }.toInt()
                        val unit = rawValue.takeLastWhile { !it.isDigit() }

                        when (unit) {
                            "in" -> number in 59..76
                            "cm" -> number in 150..193
                            else -> false
                        }
                    }
                    "hcl" -> rawValue.run {
                        startsWith('#') && length == 7 && drop(1).all { it in hclValidation }
                    }
                    "ecl" -> rawValue in eclValidation
                    "pid" -> rawValue.length == 9 && rawValue.all { it.isDigit() }
                    "cid" -> true
                    else -> false
                }
            }
        }

        val partTwo = validPassports.size

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }
}
