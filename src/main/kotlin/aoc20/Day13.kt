package aoc20

import AOCYear
import product
import readInput

class Day13 {

    fun solve() {
        val rawInput = readInput("day13.txt", AOCYear.Twenty)

        val (departTime, busIds) = rawInput.let { (departTime, busIds) ->
            val busIdsIndexed = busIds.split(',').mapIndexedNotNull { index, id ->
                id.toIntOrNull()?.let { it to index }
            }

            departTime.toInt() to busIdsIndexed
        }

        val partOne = busIds.map { (id) -> id to (id - departTime % id) }
            .minBy { it.second }
            .let { (id, waitTime) -> id * waitTime }

        val remainderModuloPairs = busIds.map { (id, index) ->
            val result = (id - index) % id

            result.toLong() to id.toLong()
        }

        val partTwo = chineseRemainderTheorem(remainderModuloPairs)

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }

    fun chineseRemainderTheorem(nums: List<Pair<Long, Long>>): Long {
        fun gcdExtended(a: Long, b: Long): Triple<Long, Long, Long> {
            if (a == 0L) {
                return Triple(b, 0, 1)
            }
            val (gcd, x1, y1) = gcdExtended(b % a, a)
            val x = y1 - (b / a) * x1
            val y = x1

            return Triple(gcd, x, y)
        }

        fun modInverse(a: Long, m: Long): Long {
            val (_, x, _) = gcdExtended(a, m)
            return (x % m + m) % m
        }

        val moduli = nums.map { it.second }

        val moduliProduct = moduli.product()

        val moduliDivisions = moduli.map { moduliProduct / it }

        val modularInverses = moduliDivisions.mapIndexed { index, it -> modInverse(it, moduli[index]) }

        val y = nums
            .map { it.first }.zip(modularInverses).zip(moduliDivisions) { (a, b), c -> Triple(a, b, c) }
            .sumOf { (value, modularInverse, modulusDivisor) ->
                value * modularInverse * modulusDivisor
            }

        return y % moduliProduct
    }
}
