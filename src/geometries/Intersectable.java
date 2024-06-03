package geometries;

import primitives.*;

import java.util.List;

/**
 * Interface for intersectable geometric bodies.
 */
public interface Intersectable {

    /**
     * Finds intersection points of a ray with the geometry.
     *
     * @param ray The ray to intersect with the geometry.
     * @return List of intersection points, or null if no intersections.
     */
    List<Point> findIntersections(Ray ray);
}
