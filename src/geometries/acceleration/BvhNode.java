package geometries.acceleration;

import geometries.Intersectable.GeoPoint;
import primitives.Ray;

import java.util.LinkedList;
import java.util.List;

/**
 * Node in a bounding volume hierarchy.
 */
class BvhNode {

    /**
     * Bounding box enclosing the node.
     */
    private final BoundingBox box;

    /**
     * Left child.
     */
    private final BvhNode left;

    /**
     * Right child.
     */
    private final BvhNode right;

    /**
     * Leaf entries.
     */
    private final List<GeometryEntry> entries;

    /**
     * Constructs a leaf node.
     *
     * @param box     node bounding box
     * @param entries leaf entries
     */
    BvhNode(BoundingBox box, List<GeometryEntry> entries) {
        this.box = box;
        this.entries = List.copyOf(entries);
        this.left = null;
        this.right = null;
    }

    /**
     * Constructs an internal node.
     *
     * @param box   node bounding box
     * @param left  left child
     * @param right right child
     */
    BvhNode(BoundingBox box, BvhNode left, BvhNode right) {
        this.box = box;
        this.left = left;
        this.right = right;
        this.entries = null;
    }

    /**
     * Finds all intersections in this node.
     *
     * @param ray         ray to intersect
     * @param maxDistance maximum intersection distance
     * @return intersections, or null if none exist
     */
    List<GeoPoint> findGeoIntersections(Ray ray, double maxDistance) {
        if (!box.intersects(ray, maxDistance)) return null;

        List<GeoPoint> intersections = null;
        if (entries != null) {
            for (GeometryEntry entry : entries) {
                if (!entry.box().intersects(ray, maxDistance)) continue;

                var entryIntersections = entry.geometry().findGeoIntersections(ray, maxDistance);
                if (entryIntersections != null) {
                    if (intersections == null)
                        intersections = new LinkedList<>(entryIntersections);
                    else
                        intersections.addAll(entryIntersections);
                }
            }
            return intersections;
        }

        intersections = addAll(intersections, left.findGeoIntersections(ray, maxDistance));
        return addAll(intersections, right.findGeoIntersections(ray, maxDistance));
    }

    /**
     * Finds the closest intersection in this node.
     *
     * @param ray             ray to intersect
     * @param closestDistance current closest distance
     * @return closest hit result
     */
    ClosestHit findClosestGeoIntersection(Ray ray, double closestDistance) {
        double boxDistance = box.intersectionDistance(ray, closestDistance);
        if (boxDistance == Double.POSITIVE_INFINITY) return new ClosestHit(null, closestDistance);

        if (entries != null) return findClosestInLeaf(ray, closestDistance);

        double leftDistance = left.box.intersectionDistance(ray, closestDistance);
        double rightDistance = right.box.intersectionDistance(ray, closestDistance);
        BvhNode first = leftDistance <= rightDistance ? left : right;
        BvhNode second = first == left ? right : left;

        ClosestHit closest = first.findClosestGeoIntersection(ray, closestDistance);
        ClosestHit other = second.findClosestGeoIntersection(ray, closest.distance());
        return other.point() == null ? closest : other;
    }

    /**
     * Finds the closest intersection inside this leaf node.
     * Each geometry is first tested against its own bounding box using the
     * current closest distance, then exact geometry intersections are checked
     * only for boxes that can still improve the closest hit.
     *
     * @param ray             ray to intersect
     * @param closestDistance current closest distance
     * @return closest hit in the leaf, or an empty hit when none exists
     */
    private ClosestHit findClosestInLeaf(Ray ray, double closestDistance) {
        GeoPoint closest = null;

        for (GeometryEntry entry : entries) {
            if (!entry.box().intersects(ray, closestDistance)) continue;

            var intersections = entry.geometry().findGeoIntersections(ray, closestDistance);
            if (intersections == null) continue;

            for (GeoPoint geoPoint : intersections) {
                double distance = geoPoint.point.distance(ray.getHead());
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closest = geoPoint;
                }
            }
        }

        return new ClosestHit(closest, closestDistance);
    }

    /**
     * Adds a list of intersections to an accumulated list while preserving null
     * as the "no intersections" value used by the geometry API.
     *
     * @param intersections accumulated intersections, or null when empty
     * @param additions     intersections to add, or null when empty
     * @return combined intersection list, or null when both inputs are empty
     */
    private static List<GeoPoint> addAll(List<GeoPoint> intersections, List<GeoPoint> additions) {
        if (additions == null) return intersections;
        if (intersections == null) return new LinkedList<>(additions);
        intersections.addAll(additions);
        return intersections;
    }

    /**
     * Closest hit result.
     *
     * @param point    closest point
     * @param distance closest distance
     */
    record ClosestHit(GeoPoint point, double distance) {
    }
}
