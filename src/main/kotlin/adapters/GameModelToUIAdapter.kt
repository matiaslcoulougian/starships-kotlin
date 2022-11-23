package adapters

import edu.austral.ingsis.starships.ui.ElementColliderType
import edu.austral.ingsis.starships.ui.ElementModel
import edu.austral.ingsis.starships.ui.ImageRef
import models.*

class GameModelToUIAdapter {
    companion object {
        val STARSHIP_IMAGE_REF_1 = ImageRef("starship", 80.0, 80.0)
        val STARSHIP_IMAGE_REF_2 = ImageRef("starship2", 90.0, 90.0)
        val ASTEROID_IMAGE_REF = ImageRef("asteroid", 100.0, 100.0)
        val BULlET_IMAGE_REF_BLUE = ImageRef("bullet-blue", 30.0, 30.0)
        val BULlET_IMAGE_REF_RED = ImageRef("bullet-red", 30.0, 30.0)
        val EXPLODED_ASTEROID = ImageRef("explosion", 100.0, 100.0)
    }

    fun adaptStarship(starship: Starship): ElementModel {
        return ElementModel(
            starship.getId(),
            starship.getPosition().getX(),
            starship.getPosition().getY(),
            60.0,
            70.0,
            starship.getRotation(),
            ElementColliderType.Elliptical,
            getStarshipImage(starship.getType())
        )
    }

    fun adaptAsteroid(asteroid: Asteroid): ElementModel {
        return ElementModel(
            asteroid.getId(),
            asteroid.getPosition().getX(),
            asteroid.getPosition().getY(),
            asteroid.getLife().toDouble(),
            asteroid.getLife().toDouble(),
            asteroid.getRotation(),
            ElementColliderType.Elliptical,
            ASTEROID_IMAGE_REF
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

    fun addElements(elements: MutableMap<String, ElementModel>, movables: List<Movable>) {
        movables.forEach {
            if (elements[it.getId()] == null) {
                elements[it.getId()] = elementToUI(it)
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

    fun removeElements(elements: MutableMap<String, ElementModel>, movables: List<Movable>) {
        val toRemove = elements.keys.filter { !movables.map { movable -> movable.getId() }.contains(it) }
        toRemove.forEach {
            elements.remove(it)
        }
    }

    fun removeAllElements(elements: MutableMap<String, ElementModel>, movables: List<Movable>) {
        movables.forEach {
            elements.remove(it.getId())
        }
    }

    private fun elementToUI(movable: Movable): ElementModel {
        return when (movable) {
            is Starship -> adaptStarship(movable)
            is Asteroid -> adaptAsteroid(movable)
            is Bullet -> adaptBullet(movable)
        }
    }

    private fun getStarshipImage(type: Int): ImageRef {
        return when (type) {
            1 -> STARSHIP_IMAGE_REF_1
            2 -> STARSHIP_IMAGE_REF_2
            else -> STARSHIP_IMAGE_REF_1
        }
    }

    private fun getBulletImage(color: BulletColor): ImageRef {
        return when (color) {
            BulletColor.BLUE -> BULlET_IMAGE_REF_BLUE
            BulletColor.RED -> BULlET_IMAGE_REF_RED
        }
    }
}