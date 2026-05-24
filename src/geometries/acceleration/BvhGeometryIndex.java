package geometries.acceleration;

import geometries.Intersectable.GeoPoint;
import geometries.Intersectable;
import primitives.Ray;

import java.util.List;

/**
 * Bounding volume hierarchy index with linear fallback for unbounded geometries.
 */
class BvhGeometryIndex implements GeometryIndex {

    /**
     * Root of the bounded geometry hierarchy.
     */
    private final BvhNode root;

    /**
     * Linear fallback for unbounded geometries.
     */
    private final LinearGeometryIndex unboundedIndex;

    /**
     * Constructs a BVH index from pre-classified geometry groups.
     * Bounded geometries are placed inside the BVH, while unbounded geometries
     * are kept in a linear fallback index because they cannot be enclosed by a
     * finite bounding box.
     *
     * @param bounded   bounded geometry entries
     * @param unbounded unbounded geometries
     */
    BvhGeometryIndex(List<GeometryEntry> bounded, List<Intersectable> unbounded) {
        root = bounded.isEmpty() ? null : new BvhBuilder().build(bounded);
        unboundedIndex = new LinearGeometryIndex(unbounded);
    }

    @Override
    public List<GeoPoint> findGeoIntersections(Ray ray, double maxDistance) {
        List<GeoPoint> intersections = unboundedIndex.findGeoIntersections(ray, maxDistance);
        if (root == null) return intersections;

        var boundedIntersections = root.findGeoIntersections(ray, maxDistance);
        if (boundedIntersections == null) return intersections;
        if (intersections == null) return boundedIntersections;

        intersections.addAll(boundedIntersections);
        return intersections;
    }

    @Override
    public GeoPoint findClosestGeoIntersection(Ray ray, double maxDistance) {
        GeoPoint closest = unboundedIndex.findClosestGeoIntersection(ray, maxDistance);
        double closestDistance = closest == null ? maxDistance : closest.point.distance(ray.getHead());

        if (root == null) return closest;

        var boundedClosest = root.findClosestGeoIntersection(ray, closestDistance);
        return boundedClosest.point() == null ? closest : boundedClosest.point();
    }
}
