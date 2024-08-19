//  In an array A, you are asked to
// find number of tuples of indices i < j < k < l such that A[i] * A[j] * A[k] = A[l]

/*
    //  A[i] * A[j] * A[k] = A[l] -> A[i] * A[j]  = A[l] / A[k]

     1. Find all numbers that A[l] % A[k] == 0 && k < l
     2. Store their k indices in a map: { num -> [k indices...] }
 */
fun tuplesProduct(numbers: List<Int>): Int {
    val specialZero = mutableListOf<Int>()

    val possibleProductToKIndex = buildMap<Int, MutableList<Int>> {
        (numbers.lastIndex downTo 3).forEach { l ->
            val lValue = numbers[l]

            (l - 1 downTo 2).forEach forK@{ k ->
                val kValue = numbers[k]

                val divResult = when {
                    kValue == 0 -> if (lValue == 0) 0 else return@forK
                    lValue % kValue == 0 -> lValue / kValue
                    else -> return@forK
                }

                if (kValue == 0) {
                    specialZero.add(k)
                } else {
                    getOrPut(divResult) { mutableListOf() }.add(k)
                }
            }
        }
    }.mapValues { (_, values) -> values.sortedDescending() }

    val maxBoundary = possibleProductToKIndex.keys.max()

    val descendingGroups = specialZero.groupingBy { it }
        .eachCount()
        .map { (kIndex, count) -> kIndex to count }
        .sortedByDescending { it.first }

    val initialGroup = (descendingGroups.firstOrNull()?.first ?: Int.MIN_VALUE)..Int.MAX_VALUE to 0

    val groupRunSum = descendingGroups.runningFoldIndexed(initialGroup) { index, (_, lastCount), (kIndex, count) ->
        if (index != descendingGroups.lastIndex) {
            val next = descendingGroups[index + 1]

            next.first until kIndex to lastCount + count
        } else {
            Int.MIN_VALUE until kIndex to lastCount + count
        }
    }

    return (0..numbers.lastIndex - 3).sumOf { i ->
        val iNum = numbers[i]
        (i + 1..numbers.lastIndex - 2).sumOf { j ->
            val product = iNum * numbers[j]

            val specialZeroCount = groupRunSum.first { j in it.first }.second

            val numberOfProducts = if (product > maxBoundary) {
                0
            } else {
                val indices = possibleProductToKIndex[product]
                val firstLessThanIndex = indices?.indexOfFirst { it <= j }

                when (firstLessThanIndex) {
                    null -> 0
                    -1 -> indices.size
                    else -> firstLessThanIndex
                }
            }

            numberOfProducts + specialZeroCount
        }
    }
}
