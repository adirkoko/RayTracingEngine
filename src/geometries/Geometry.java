package geometries;

import primitives.Point;
import primitives.Vector;

/**
 * Interface for geometric bodies
 */
public interface Geometry {

    /**
     * Calculates the normal vector to the geometry at the specified point.
     *
     * @param point The point on the geometry where the normal is to be calculated.
     * @return The normal vector at the given point.
     */
    Vector getNormal(Point point);
}