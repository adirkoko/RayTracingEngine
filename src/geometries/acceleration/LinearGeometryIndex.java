package geometries.acceleration;

import geometries.Intersectable.GeoPoint;
import geometries.Intersectable;
import primitives.Ray;

import java.util.LinkedList;
import java.util.List;

/**
 * Linear geometry index that checks every geometry directly.
 */
class LinearGeometryIndex implements GeometryIndex {

    /**
     * Indexed geometries.
     */
    private final List<Intersectable> geometries;

    /**
     * Constructs a linear index over geometries.
     *
     * @param geometries geometries to index
     */
    LinearGeometryIndex(List<Intersectable> geometries) {
        this.geometries = List.copyOf(geometries);
    }

    @Override
    public List<GeoPoint> findGeoIntersections(Ray ray, double maxDistance) {
        List<GeoPoint> intersections = null;

        for (Intersectable geometry : geometries) {
            var geometryIntersections = geometry.findGeoIntersections(ray, maxDistance);
            if (geometryIntersections != null) {
                if (intersections == null)
                    intersections = new LinkedList<>(geometryIntersections);
                else
                    intersections.addAll(geometryIntersections);
            }
        }

        return intersections;
    }

    @Override
    public GeoPoint findClosestGeoIntersection(Ray ray, double maxDistance) {
        GeoPoint closest = null;
        double closestDistanceSquared = maxDistance * maxDistance;

        for (Intersectable geometry : geometries) {
            var geometryIntersections = geometry.findGeoIntersections(ray, maxDistance);
            if (geometryIntersections == null) continue;

            for (GeoPoint geoPoint : geometryIntersections) {
                double distanceSquared = geoPoint.point.distanceSquared(ray.getHead());
                if (distanceSquared < closestDistanceSquared) {
                    closestDistanceSquared = distanceSquared;
                    closest = geoPoint;
                }
            }
        }

        return closest;
    }
}
