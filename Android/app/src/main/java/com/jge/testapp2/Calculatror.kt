package com.jge.testapp2

class Calculatror {

    private fun makeBit(tags: List<Int>): Int {
        return  tags.fold(0) { acc, tag -> acc or (1 shl tag) }
    }

    private fun <T> combinations(array: List<T>): List<List<T>> {
        val allCombinations = mutableListOf<List<T>>()
        val n = array.size
        // 2^n 가지의 부분집합을 생성합니다.
        val totalCombinations = 1 shl n

        for (i in 0 until totalCombinations) {
            val combination = mutableListOf<T>()
            for (j in 0 until n) {
                // i의 j번째 비트가 1인지 확인합니다.
                if ((i and (1 shl j)) != 0) {
                    combination.add(array[j])
                }
            }
            if (!combination.isEmpty()) {
                allCombinations.add(combination)
            }
        }

        return allCombinations
    }

    private fun checkTags(targetTag: Int, givenTags: List<Int>): Set<Int> {
        val result: MutableSet<Int> = mutableSetOf()
        for (comb in combinations(givenTags)) {
            val bit = makeBit(comb)

            if (canCreateTags(targetTag, bit)) {
                result.add(bit)
            }
        }

        return result
    }

    private fun canCreateTags(targetTag: Int, availableTags: Int) : Boolean {
        val result = ((targetTag and availableTags) == availableTags)

        return result
    }

    fun getResultData(givenTag: Int): Map<Int, Set<Item>> {
        var result: MutableMap<Int, Set<Item>> = mutableMapOf()
        val givenTags = Loader.convert(givenTag)

        val data = Loader.data

        for(element in data) {
            val key: Int = element.tag

            val res = checkTags(key, givenTags)

            if (res.isNotEmpty()) {
                res.forEach {
                    if (result.containsKey(it)) {
                        val existingValues = result[it] ?: emptySet()
                        result[it] = existingValues + element
                    } else {
                        result[it] = setOf(element)
                    }
                }
            }
        }

        return result
    }
}