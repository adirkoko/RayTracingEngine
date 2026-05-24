package geometries.acceleration;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static primitives.Util.alignZero;
import static primitives.Util.isZero;

/**
 * Axis-aligned bounding box for finite geometries.
 */
public class BoundingBox {

    /**
     * Minimum box corner.
     */
    private final Point min;

    /**
     * Maximum box corner.
     */
    private final Point max;

    /**
     * Constructs a bounding box from two corners.
     *
     * @param min minimum corner
     * @param max maximum corner
     */
    public BoundingBox(Point min, Point max) {
        this.min = min;
        this.max = max;
    }

    /**
     * Gets the minimum corner.
     *
     * @return minimum corner
     */
    public Point getMin() {
        return min;
    }

    /**
     * Gets the maximum corner.
     *
     * @return maximum corner
     */
    public Point getMax() {
        return max;
    }

    /**
     * Checks whether a ray intersects this box up to a maximum distance.
     *
     * @param ray         ray to test
     * @param maxDistance maximum intersection distance
     * @return true if the ray intersects this box
     */
    public boolean intersects(Ray ray, double maxDistance) {
        return intersectionDistance(ray, maxDistance) != Double.POSITIVE_INFINITY;
    }

    /**
     * Returns the first positive distance where a ray enters the box.
     *
     * @param ray         ray to test
     * @param maxDistance maximum intersection distance
     * @return first box hit distance or positive infinity if no hit exists
     */
    double intersectionDistance(Ray ray, double maxDistance) {
        double[] interval = intersectionInterval(ray, maxDistance);
        return interval == null ? Double.POSITIVE_INFINITY : interval[0];
    }

    /**
     * Returns the positive distance interval where a ray overlaps this box.
     *
     * @param ray         ray to test
     * @param maxDistance maximum intersection distance
     * @return two-value interval {entry, exit}, or null if no overlap exists
     */
    double[] intersectionInterval(Ray ray, double maxDistance) {
        Point head = ray.getHead();
        Vector direction = ray.getDirection();

        double tMin = 0;
        double tMax = maxDistance;

        double[] interval = axisInterval(head.getX(), direction.getX(), min.getX(), max.getX());
        if (interval == null) return null;
        tMin = Math.max(tMin, interval[0]);
        tMax = Math.min(tMax, interval[1]);
        if (alignZero(tMax - tMin) < 0) return null;

        interval = axisInterval(head.getY(), direction.getY(), min.getY(), max.getY());
        if (interval == null) return null;
        tMin = Math.max(tMin, interval[0]);
        tMax = Math.min(tMax, interval[1]);
        if (alignZero(tMax - tMin) < 0) return null;

        interval = axisInterval(head.getZ(), direction.getZ(), min.getZ(), max.getZ());
        if (interval == null) return null;
        tMin = Math.max(tMin, interval[0]);
        tMax = Math.min(tMax, interval[1]);
        if (alignZero(tMax - tMin) < 0 || alignZero(tMax) <= 0) return null;

        return new double[]{tMin > 0 ? tMin : 0, tMax};
    }

    /**
     * Unites this box with another box.
     *
     * @param other box to include
     * @return bounding box containing both boxes
     */
    public BoundingBox union(BoundingBox other) {
        return new BoundingBox(
                new Point(
                        Math.min(min.getX(), other.min.getX()),
                        Math.min(min.getY(), other.min.getY()),
                        Math.min(min.getZ(), other.min.getZ())),
                new Point(
                        Math.max(max.getX(), other.max.getX()),
                        Math.max(max.getY(), other.max.getY()),
                        Math.max(max.getZ(), other.max.getZ())));
    }

    /**
     * Gets the surface area of this box.
     *
     * @return box surface area
     */
    public double surfaceArea() {
        double dx = max.getX() - min.getX();
        double dy = max.getY() - min.getY();
        double dz = max.getZ() - min.getZ();
        return 2 * (dx * dy + dx * dz + dy * dz);
    }

    /**
     * Gets the longest box axis.
     *
     * @return 0 for x, 1 for y, 2 for z
     */
    public int longestAxis() {
        double dx = max.getX() - min.getX();
        double dy = max.getY() - min.getY();
        double dz = max.getZ() - min.getZ();
        return dx >= dy && dx >= dz ? 0 : dy >= dz ? 1 : 2;
    }

    /**
     * Gets the center coordinate for an axis.
     *
     * @param axis 0 for x, 1 for y, 2 for z
     * @return center coordinate
     */
    double center(int axis) {
        return switch (axis) {
            case 0 -> (min.getX() + max.getX()) / 2;
            case 1 -> (min.getY() + max.getY()) / 2;
            default -> (min.getZ() + max.getZ()) / 2;
        };
    }

    /**
     * Computes the ray overlap interval for one box axis.
     * If the ray is parallel to the axis slab, the method returns null when the
     * ray starts outside the slab, otherwise it returns an infinite interval for
     * that axis.
     *
     * @param head      ray head coordinate on the tested axis
     * @param direction ray direction coordinate on the tested axis
     * @param min       minimum box coordinate on the tested axis
     * @param max       maximum box coordinate on the tested axis
     * @return two-value interval {near, far}, or null when the ray misses the slab
     */
    private static double[] axisInterval(double head, double direction, double min, double max) {
        if (isZero(direction))
            return head < min || head > max ? null : new double[]{Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY};

        double t1 = (min - head) / direction;
        double t2 = (max - head) / direction;
        return t1 <= t2 ? new double[]{t1, t2} : new double[]{t2, t1};
    }
}
