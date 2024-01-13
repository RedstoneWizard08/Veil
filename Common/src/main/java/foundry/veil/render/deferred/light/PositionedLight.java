package foundry.veil.render.deferred.light;

import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * A light in the renderer that has a position
 *
 * @param <T> The type of light to return in setters
 * @author Ocelot
 */
public interface PositionedLight<T extends PositionedLight<T>> {

    /**
     * @return The position of this light
     */
    Vector3f getPosition();

    /**
     * Sets the origin position of this light.
     *
     * @param position The position of the light
     */
    default T setPosition(Vector3fc position) {
        return this.setPosition(position.x(), position.y(), position.z());
    }

    /**
     * Sets the origin position of this light.
     *
     * @param x The x position of the light
     * @param y The y position of the light
     * @param z The z position of the light
     */
    T setPosition(float x, float y, float z);
}