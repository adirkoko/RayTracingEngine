package primitives;

import static geometries.Intersectable.GeoPoint;

import java.util.List;

import static primitives.Util.isZero;
import static renderer.SimpleRayTracer.DELTA;


/**
 * Represents a ray in three-dimensional space defined by a starting point (head) and a direction vector.
 * The direction vector is always normalized.
 *
 * @author Adir and Meir.
 */
public class Ray {

    /**
     * The starting point of the Ray.
     */
    private final Point head;

    /**
     * The direction of the Ray.
     */
    private final Vector direction;

    /**
     * Constructor for Ray that accepts a starting point and a direction vector.
     *
     * @param head      The start point of the ray
     * @param direction The direction vector of the ray
     */
    public Ray(Point head, Vector direction) {
        this.head = head;
        this.direction = direction.normalize();
    }

    /**
     * Constructor for Ray that accepts a starting point, a direction vector, and a normal vector.
     * The start point is shifted along the normal to avoid precision issues.
     *
     * @param head      The start point of the ray.
     * @param direction The direction vector of the ray. it must be normalized (length=1)
     * @param normal    The normal vector at the start point.
     */
    public Ray(Point head, Vector direction, Vector normal) {
        double delta = normal.dotProduct(direction) > 0 ? DELTA : -DELTA;
        this.head = head.add(normal.scale(delta));
        this.direction = direction;
    }

    /**
     * Returns the starting point of the ray.
     *
     * @return starting point of ray.
     */
    public Point getHead() {
        return head;
    }

    /**
     * Returns the directional vector of the ray.
     *
     * @return directional vector of ray.
     */
    public Vector getDirection() {
        return direction;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        return (obj instanceof Ray other)
                && this.head.equals(other.head)
                && this.direction.equals(other.direction);
    }

    @Override
    public String toString() {
        return head + "->" + direction;
    }

    /**
     * Calculates a point on the ray at a given distance from the ray's origin.
     *
     * @param t the distance from the ray's origin
     * @return the point on the ray at distance t
     */
    public Point getPoint(double t) {
        return isZero(t) ? head : head.add(direction.scale(t));
    }

    /**
     * Find the closest point to the ray's origin from a list of points.
     *
     * @param points the list of points
     * @return the closest point
     */
    public Point findClosestPoint(List<Point> points) {
        return points == null || points.isEmpty() ? null
                : findClosestGeoPoint(points.stream()
                .map(p -> new GeoPoint(null, p)).toList()).point;
    }

    /**
     * Finds the closest GeoPoint to the ray's origin from a list of GeoPoints.
     *
     * @param geoPoints The list of GeoPoints to search.
     * @return The closest GeoPoint to the ray's origin.
     */
    public GeoPoint findClosestGeoPoint(List<GeoPoint> geoPoints) {
        if (geoPoints == null || geoPoints.isEmpty()) return null;

        GeoPoint closestGeoPoint = null;
        double closestDistanceSquared = Double.POSITIVE_INFINITY;

        for (var geoPoint : geoPoints) {
            double distanceSquared = geoPoint.point.distanceSquared(head);
            if (distanceSquared < closestDistanceSquared) {
                closestDistanceSquared = distanceSquared;
                closestGeoPoint = geoPoint;
            }
        }

        return closestGeoPoint;
    }
}
