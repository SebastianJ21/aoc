package aoc20

import AOCYear
import readInput

class Day21 {

    fun solve() {
        val rawInput = readInput("day21.txt", AOCYear.Twenty)

        val ingredientAllergens = rawInput.map { line ->
            val (rawIngredients, rawAllergens) = line.replace(Regex("[()]"), "").split(" contains ")

            val ingredients = rawIngredients.split(" ")
            val allergens = rawAllergens.split(", ")

            ingredients to allergens
        }

        val allergensToIngredients = ingredientAllergens
            .flatMap { (ingredients, allergens) -> allergens.map { it to ingredients } }
            .groupBy({ it.first }, { it.second })

        val allergenToPossibleIngredients = allergensToIngredients.mapValues { (_, ingredients) ->
            val first = ingredients.first()
            val rest = ingredients.drop(1)

            first.filter { ingredient ->
                rest.all { ingredient in it }
            }
        }

        val initialResolved = mapOf<String, String>()

        val resolveSeq = generateSequence(initialResolved to allergenToPossibleIngredients) { (resolved, unresolved) ->
            if (unresolved.isEmpty()) return@generateSequence null

            val newUnresolved = unresolved.mapValues { (_, ingredients) ->
                ingredients.filter { ingredient -> resolved.values.none { ingredient in it } }
            }

            val newResolved = newUnresolved.mapNotNull { (allergen, ingredients) ->
                ingredients.singleOrNull()?.let { allergen to it }
            }.toMap()

            resolved + newResolved to newUnresolved - newResolved.keys
        }

        val (allergenToIngredient) = resolveSeq.last()

        val ingredientsWithAllergen = allergenToIngredient.values

        val partOne = ingredientAllergens
            .flatMap { (ingredients) -> ingredients }
            .groupingBy { it }
            .eachCount()
            .filter { (ingredient) -> ingredient !in ingredientsWithAllergen }
            .values
            .sum()

        val partTwo = allergenToIngredient
            .toList()
            .sortedBy { (allergen) -> allergen }
            .joinToString(",") { (_, ingredient) -> ingredient }

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }
}
