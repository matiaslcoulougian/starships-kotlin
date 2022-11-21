package models

data class ScoreBoard(
    private val scores: Map<String, Int>
) {
    fun addPoints(starshipId: String, points: Int): ScoreBoard {
        val previousPoints = scores[starshipId]
        return if (previousPoints != null) copy(scores = scores + mapOf(Pair(starshipId, previousPoints + points)))
        else copy(scores = scores + mapOf(Pair(starshipId, 0 + points)))
    }

    fun getScore(): Map<String, Int> = scores
}