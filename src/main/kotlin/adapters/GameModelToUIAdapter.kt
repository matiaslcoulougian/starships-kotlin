package adapters

import edu.austral.ingsis.starships.ui.ElementColliderType
import edu.austral.ingsis.starships.ui.ElementModel
import edu.austral.ingsis.starships.ui.ImageRef
import models.Asteroid
import models.Bullet
import models.Movable
import models.Starship

class GameModelToUIAdapter {
    companion object {
        val STARSHIP_IMAGE_REF = ImageRef("starship", 70.0, 70.0)
        val ASTEROID_IMAGE_REF = ImageRef("asteroid", 100.0, 100.0)
        val BULlET_IMAGE_REF = ImageRef("bullet", 30.0, 30.0)
        val EXPLODED_ASTEROID = ImageRef("explosion", 100.0, 100.0)
    }

    fun adaptStarship(starship: Starship): ElementModel {
        return ElementModel(
            starship.getId(),
            starship.getPosition().getX(),
            starship.getPosition().getY(),
            50.0,
            50.0,
            starship.getRotation(),
            ElementColliderType.Elliptical,
            STARSHIP_IMAGE_REF
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
            BULlET_IMAGE_REF
        )
    }

    private fun elementToUI(movable: Movable): ElementModel {
        return when (movable) {
            is Starship -> adaptStarship(movable)
            is Asteroid -> adaptAsteroid(movable)
            is Bullet -> adaptBullet(movable)
        }
    }
}