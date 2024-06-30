package primitives;

import java.util.List;

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
        return Util.isZero(t) ? head : head.add(direction.scale(t));
    }

    /**
     * Finds the closest point to the ray's starting point from a list of points.
     *
     * @param points The list of points to find the closest point from.
     * @return The closest point to the ray's starting point, or null if the list is empty.
     */
    public Point findClosestPoint(List<Point> points) {
        // Check if the list is null or empty
        if (points == null || points.isEmpty()) {
            return null;
        }

        // Initialize the closest point
        Point closestPoint = null;
        // Initialize the distance to be greater than any real distance
        double closestDistance = Double.POSITIVE_INFINITY;

        // Iterate through the points in the list
        for (Point point : points) {
            // Compute the distance from the ray's start to the current point
            double distance = point.distance(head);
            // If the current point is closer, update the closest point and distance
            if (distance < closestDistance) {
                closestDistance = distance;
                closestPoint = point;
            }
        }

        // Return the closest point found
        return closestPoint;
    }
}
