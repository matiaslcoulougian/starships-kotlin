package utils

import kotlin.random.Random

fun getRandomDouble(min: Int, max: Int): Double {
    require(min < max) { "Invalid range [$min, $max]" }
    return min + Random.nextDouble() * (max - min)
}

fun getRandomInt(min: Int, max: Int): Int {
    require(min < max) { "Invalid range [$min, $max]" }
    return (min..max).random()
}