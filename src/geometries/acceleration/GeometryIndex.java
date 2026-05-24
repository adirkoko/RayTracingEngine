package geometries.acceleration;

import geometries.Intersectable.GeoPoint;
import primitives.Ray;

import java.util.List;

/**
 * Internal acceleration abstraction for geometry intersection queries.
 */
public interface GeometryIndex {

    /**
     * Finds all intersections with a ray up to a maximum distance.
     *
     * @param ray         ray to intersect
     * @param maxDistance maximum intersection distance
     * @return list of intersections, or null if none exist
     */
    List<GeoPoint> findGeoIntersections(Ray ray, double maxDistance);

    /**
     * Finds the closest intersection with a ray up to a maximum distance.
     *
     * @param ray         ray to intersect
     * @param maxDistance maximum intersection distance
     * @return closest intersection, or null if none exists
     */
    GeoPoint findClosestGeoIntersection(Ray ray, double maxDistance);
}
