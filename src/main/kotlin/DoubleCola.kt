/*
Write a program that will print the name of a man who will drink the n-th can.
Note that in the very beginning the queue looks like that: Sheldon, Leonard, Penny, Rajesh, Howard. The first person is Sheldon.
 */
fun doubleCola(n: Int): String {
    val initialQueue = listOf("Sheldon" to 1, "Leonard" to 1, "Penny" to 1, "Rajesh" to 1, "Howard" to 1)

    val queueSequence = generateSequence(initialQueue to 0) { (queue, total) ->
        val (name, amount) = queue.first()
        val newTotal = total + amount

        val newQueue = queue.drop(1).plusElement(name to amount * 2)
        newQueue to newTotal
    }

    return queueSequence.first { (_, total) -> total >= n }.first.last().first
}
