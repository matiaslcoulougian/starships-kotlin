package adapters

import edu.austral.ingsis.starships.ui.ElementColliderType
import edu.austral.ingsis.starships.ui.ElementModel
import edu.austral.ingsis.starships.ui.ImageRef
import models.*
import kotlin.math.log

class GameModelToUIAdapter {
    companion object {
        val STARSHIP_IMAGE_REF_1 = ImageRef("starship", 80.0, 80.0)
        val STARSHIP_IMAGE_REF_2 = ImageRef("starship2", 70.0, 70.0)
        val STARSHIP_IMAGE_REF_3 = ImageRef("starship3", 80.0, 80.0)
        val BULlET_IMAGE_REF_BLUE = ImageRef("bullet-blue", 30.0, 30.0)
        val BULlET_IMAGE_REF_RED = ImageRef("bullet-red", 30.0, 30.0)
        val ASTEROID_IMAGE_REF_1 = ImageRef("asteroid1", 100.0, 100.0)
        val ASTEROID_IMAGE_REF_2 = ImageRef("asteroid2", 100.0, 100.0)
        val HEALTH_BOOSTER = ImageRef("heart", 50.0, 50.0)
    }

    fun adaptStarship(starship: Starship): ElementModel {
        return ElementModel(
            starship.getId(),
            starship.getPosition().getX(),
            starship.getPosition().getY(),
            70.0,
            70.0,
            starship.getRotation(),
            ElementColliderType.Elliptical,
            getStarshipImage(starship.getType())
        )
    }

    fun adaptAsteroid(asteroid: Asteroid): ElementModel {
        val image = if (asteroid.getType() == 1) ASTEROID_IMAGE_REF_1 else ASTEROID_IMAGE_REF_2
        return ElementModel(
            asteroid.getId(),
            asteroid.getPosition().getX(),
            asteroid.getPosition().getY(),
            asteroid.getLife().toDouble() * 1.6,
            asteroid.getLife().toDouble() * 1.6,
            asteroid.getRotation(),
            ElementColliderType.Elliptical,
            image
        )
    }

    fun adaptBullet(bullet: Bullet): ElementModel {
        return ElementModel(
            bullet.getId(),
            bullet.getPosition().getX(),
            bullet.getPosition().getY(),
            15.0,
            10.0,
            bullet.getRotation(),
            ElementColliderType.Rectangular,
            getBulletImage(bullet.getColor())
        )
    }

    fun adaptBooster(booster: Booster): ElementModel {
        return ElementModel(
            booster.getId(),
            booster.getPosition().getX(),
            booster.getPosition().getY(),
            50.0,
            50.0,
            0.0,
            ElementColliderType.Elliptical,
            getBoosterImage(booster.getType())
        )
    }

    fun addElements(elements: MutableMap<String, ElementModel>, movables: List<Movable>, boosters: List<Booster>) {
        movables.forEach {
            if (elements[it.getId()] == null) {
                val adaptedMovable = movableToUI(it)
                if (adaptedMovable != null) elements[it.getId()] = adaptedMovable
            }
        }
        boosters.forEach {
            if (elements[it.getId()] == null) {
                elements[it.getId()] = boosterToUI(it)
            }
        }
    }

    fun updateElementsPosition(elements: Map<String, ElementModel>, movables: List<Movable>) {
        movables.forEach {
            val element = elements[it.getId()]
            if (element != null) {
                element.x.set(it.getPosition().getX())
                element.y.set(it.getPosition().getY())
                element.rotationInDegrees.set(it.getRotation())
                //if (it is Asteroid && it.getLife() < 0) element.image.set(SimpleObjectProperty(EXPLODED_ASTEROID))
            }
        }
    }

    fun removeElements(elements: MutableMap<String, ElementModel>, movables: List<Movable>, boosters: List<Booster>) {
        val movablesToRemove = elements.keys.filter { !movables.map { movable -> movable.getId() }.contains(it) && !it.contains("booster")}
        val boostersToRemove = elements.keys.filter { !boosters.map { booster -> booster.getId() }.contains(it) && it.contains("booster") }
        movablesToRemove.forEach {
            elements.remove(it)
        }
        boostersToRemove.forEach {
            elements.remove(it)
        }
    }

    fun removeAllElements(elements: MutableMap<String, ElementModel>, movables: List<Movable>, boosters: List<Booster>) {
        movables.forEach {
            elements.remove(it.getId())
        }
        boosters.forEach {
            elements.remove(it.getId())
        }
    }

    private fun movableToUI(movable: Movable): ElementModel? {
        return when (movable) {
            is Starship -> adaptStarship(movable)
            is Asteroid -> adaptAsteroid(movable)
            is Bullet -> adaptBullet(movable)
            else -> null
        }
    }

    private fun boosterToUI(booster: Booster): ElementModel = adaptBooster(booster)

    private fun getStarshipImage(type: Int): ImageRef {
        return when (type) {
            1 -> STARSHIP_IMAGE_REF_1
            2 -> STARSHIP_IMAGE_REF_2
            3 -> STARSHIP_IMAGE_REF_3
            else -> STARSHIP_IMAGE_REF_1
        }
    }

    private fun getBulletImage(color: BulletColor): ImageRef {
        return when (color) {
            BulletColor.BLUE -> BULlET_IMAGE_REF_BLUE
            BulletColor.RED -> BULlET_IMAGE_REF_RED
        }
    }

    private fun getBoosterImage(type: BoosterType): ImageRef {
        return when (type) {
            BoosterType.HEALTH -> HEALTH_BOOSTER
        }
    }
}