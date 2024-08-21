package aoc20

import AOCYear
import readInput
import splitBy

class Day4 {

    fun solve() {
        val rawInput = readInput("day4.txt", AOCYear.Twenty)

        val rawPassports = rawInput.splitBy { isEmpty() }

        val blankCid = "cid" to "420"

        val validEntries = rawPassports.mapNotNull { rawPassport ->
            val passportEntries = rawPassport.joinToString(" ").split(" ")

            val entryFields = passportEntries.associate { entry ->
                val (name, value) = entry.split(':')

                name to value
            } + blankCid

            entryFields.takeIf { it.keys.size == 8 }
        }

        val partOne = validEntries.size

        val eclValidation = listOf("amb", "blu", "brn", "gry", "grn", "hzl", "oth")

        val validPassports = validEntries.filter { entries ->
            entries.all { (entry, value) ->
                when (entry) {
                    "byr" -> value.length == 4 && value.toInt() in 1920..2002
                    "iyr" -> value.length == 4 && value.toInt() in 2010..2020
                    "eyr" -> value.length == 4 && value.toInt() in 2020..2030
                    "hgt" -> {
                        val number = value.takeWhile { it.isDigit() }.toInt()
                        val unit = value.takeLastWhile { !it.isDigit() }

                        when (unit) {
                            "in" -> number in 59..76
                            "cm" -> number in 150..193
                            else -> false
                        }
                    }
                    "hcl" -> value.startsWith('#') && value.drop(1) matches Regex("[0-9a-f]{6}")
                    "ecl" -> value in eclValidation
                    "pid" -> value.length == 9 && value.all { it.isDigit() }
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
