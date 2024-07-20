package lighting;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

/**
 * Interface representing a light source in the scene.
 * Defines methods to get light intensity, direction, and distance at a given point.
 *
 * @author Adir and Meir.
 */
public interface LightSource {
    /**
     * Gets the intensity of the light at a given point.
     *
     * @param p the point where the light intensity is calculated
     * @return the intensity of the light at the given point
     */
    Color getIntensity(Point p);

    /**
     * Gets the direction of the light at a given point.
     *
     * @param p the point where the light direction is calculated
     * @return the direction of the light at the given point
     */
    Vector getL(Point p);

    /**
     * Gets the distance from the light source to a given point.
     *
     * @param point The point to calculate the distance to.
     * @return The distance from the light source to the point.
     */
    double getDistance(Point point);
}
