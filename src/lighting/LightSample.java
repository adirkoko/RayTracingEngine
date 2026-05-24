package lighting;

import primitives.Color;
import primitives.Vector;

/**
 * Represents one sampled contribution from a light source toward a point in the scene.
 *
 * @param intensity light intensity for this sample
 * @param direction direction from the sampled light position toward the scene point
 * @param distance  distance from the sampled light position to the scene point
 */
public record LightSample(Color intensity, Vector direction, double distance) {

    /**
     * Constructs a light sample and validates its data.
     */
    public LightSample {
        if (intensity == null)
            throw new IllegalArgumentException("Light sample intensity cannot be null");
        if (direction == null)
            throw new IllegalArgumentException("Light sample direction cannot be null");
        if (distance < 0)
            throw new IllegalArgumentException("Light sample distance cannot be negative");
    }
}
