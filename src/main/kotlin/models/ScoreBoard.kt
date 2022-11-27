package models

data class ScoreBoard(
    private val scores: Map<String, Int>
) {
    private fun addPoints(starshipId: String, points: Int): ScoreBoard {
        val previousPoints = scores[starshipId]
        return if (previousPoints != null) copy(scores = scores + mapOf(Pair(starshipId, previousPoints + points)))
        else copy(scores = scores + mapOf(Pair(starshipId, 0 + points)))
    }

     fun checkAndUpdateScores(collidable1: Collidable, collidable2: Collidable): ScoreBoard {
        return if (collidable1 is Bullet && collidable2 is Asteroid && collidable2.isDead()) {
            addPoints(collidable1.getStarshipId(), 10)
        } else if (collidable2 is Bullet && collidable1 is Asteroid && collidable1.isDead()) {
            addPoints(collidable2.getStarshipId(), 10)
        } else this
    }

    fun getScore(): Map<String, Int> = scores
}