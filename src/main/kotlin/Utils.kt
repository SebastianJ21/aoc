import java.io.File
import java.math.BigInteger

enum class AOCYear {
    TwentyThree,
    TwentyTwo,
    TwentyOne,
    Twenty,
    Nineteen,
}

const val RESOURCES_PATH = "src/main/resources"

/**
 * Reads lines from the given input txt file located in resources.
 */
fun readInput(name: String, year: AOCYear = AOCYear.TwentyTwo) =
    File("$RESOURCES_PATH/${year.getSuffix()}/$name").readLines()

fun AOCYear.getSuffix() = when (this) {
    AOCYear.TwentyThree -> "aoc23"
    AOCYear.TwentyTwo -> "aoc22"
    AOCYear.TwentyOne -> "aoc21"
    AOCYear.Twenty -> "aoc20"
    AOCYear.Nineteen -> "aoc19"
}

typealias Position = Pair<Int, Int>

/**
 * For more descriptive naming when used alongside [Position]
 */
typealias Direction = Pair<Int, Int>

fun Position.applyDirection(direction: Direction): Position = this + direction

operator fun Pair<Int, Int>.plus(other: Pair<Int, Int>) = first.plus(other.first) to second.plus(other.second)

inline fun <T> convertInputToMatrix(input: List<String>, transform: (Char) -> T): List<List<T>> =
    input.map { row -> row.map(transform) }

operator fun <T> List<List<T>>.get(position: Position) = this[position.first][position.second]

fun <T> List<List<T>>.getOrNull(position: Position) = getOrNull(position.first)?.getOrNull(position.second)

fun convertInputToCharMatrix(input: List<String>): List<List<Char>> = input.map { it.toList() }

fun Iterable<Int>.maxOrZero() = maxOrNull() ?: 0

fun <T> List<List<T>>.transposed(): List<List<T>> {
    val rowSize = first().size
    check(all { it.size == rowSize }) { "Array to be transposed is not a matrix" }

    return List(rowSize) { rowI ->
        List(size) { colI ->
            this[colI][rowI]
        }
    }
}

fun Collection<Int>.median(): Double {
    val sorted = sorted()
    val mid = size / 2

    return if (size % 2 == 0) {
        sorted[mid].toDouble()
    } else {
        (sorted[mid] + sorted[mid + 1]) / 2.0
    }
}

fun Iterable<String>.mapToInt() = map(String::toInt)

fun Iterable<String>.mapToLong() = map(String::toLong)

/**
 * Splits List of [T] into sublists by specified [predicate]. An equivalent of [split] but for Lists.
 */
inline fun <T> List<T>.splitBy(predicate: T.() -> Boolean): List<List<T>> {
    val (_, result) = foldIndexed(listOf<T>() to listOf<List<T>>()) { index, (localCollected, globalCollected), line ->
        when {
            index == lastIndex -> {
                // Split on the last element should yield and empty list
                if (predicate(line)) {
                    listOf<T>() to globalCollected.plusElement(localCollected).plusElement(emptyList())
                } else {
                    listOf<T>() to globalCollected.plusElement(localCollected + line)
                }
            }
            predicate(line) -> listOf<T>() to globalCollected.plusElement(localCollected)
            else -> localCollected + line to globalCollected
        }
    }

    return result
}

/**
 * Splits List of [T] into sublists by specified [predicate] and applies [transform] on each element. An equivalent of [split] but for Lists.
 */
inline fun <T, K> List<T>.splitBy(predicate: T.() -> Boolean, transform: (T) -> K): List<List<K>> {
    val (_, result) = foldIndexed(listOf<K>() to listOf<List<K>>()) { index, (localCollected, globalCollected), line ->
        when {
            index == lastIndex -> {
                // Split on the last element should yield and empty list
                if (predicate(line)) {
                    listOf<K>() to globalCollected.plusElement(localCollected).plusElement(emptyList())
                } else {
                    listOf<K>() to globalCollected.plusElement(localCollected + transform(line))
                }
            }
            predicate(line) -> listOf<K>() to globalCollected.plusElement(localCollected)
            else -> localCollected + transform(line) to globalCollected
        }
    }

    return result
}

inline fun <T> T.alsoPrintLn(before: String = "", transform: T.() -> Any? = { }) = also {
    val delim = if (before.isEmpty()) "" else " "

    val transformed = transform(this)

    val value = if (transformed == Unit) this else transformed
    println("$before$delim$value")
}

fun BigInteger.lcm(other: BigInteger) = this.times(other).div(this.gcd(other))

operator fun <T> Pair<T, T>.contains(element: T) = first == element || second == element

fun Iterable<Int>.product(): Int = reduce { acc, i -> acc * i }

fun Iterable<Long>.product(): Long = reduce { acc, i -> acc * i }

/**
 * Takes a [map] with values of [Iterable] type and inverts its keys and values so that keys having the same element
 * in their values appear in the value list of that element in the result map.
 */
fun <T, K, V : Iterable<T>> invertListMap(map: Map<K, V>): Map<T, List<K>> =
    map.flatMap { (key, value) -> value.map { v -> v to key } }.groupBy({ it.first }, { it.second })

fun <T> List<T>.firstAndRest() = first() to drop(1)

fun String.firstAndRest() = first() to drop(1)

@Deprecated(message = "Return a AOCAnswer instead", ReplaceWith("AOCAnswer", "src/main/kotlin/Main.kt/AOCAnswer"))
fun printAOCAnswers(partOne: Any?, partTwo: Any? = null) {
    println("Part one: ${partOne ?: "not solved"}")
    println("Part two: ${partTwo ?: "not solved"}")
}
