package geometries;

import geometries.acceleration.BoundingBox;
import primitives.*;

import java.util.LinkedList;
import java.util.List;

import static primitives.Util.alignZero;
import static primitives.Util.isZero;

/**
 * Represents a cylinder in 3D space.
 * Extends the Tube class by adding a height property.
 * Provides methods for calculating the normal vector at a given point on the cylinder.
 *
 * @author Adir and Meir
 */
public class Cylinder extends Tube {

    /**
     * The height of the tube.
     */
    private final double height;

    /**
     * Constructor for Cylinder.
     *
     * @param axisRay The central axis of the cylinder.
     * @param radius  The radius of the cylinder.
     * @param height  The height of the cylinder.
     */
    public Cylinder(Ray axisRay, double radius, double height) {
        super(axisRay, radius);
        this.height = height;
    }

    @Override
    public Vector getNormal(Point point) {
        Point head = axisRay.getHead();
        Vector direction = axisRay.getDirection();

        // If the point is in the center of the bottom base.
        if (point.equals(head)) return direction.scale(-1); // Normal is opposite to the direction of the axis.

        // Calculate the projection of the point onto the axis ray
        double projectionLength = direction.dotProduct(point.subtract(head));

        // If the point is on the bottom base
        if (Util.isZero(projectionLength))
            return direction.scale(-1); // Normal is opposite to the direction of the axis.

        // If the point is on the top base
        if (Util.isZero(projectionLength - height))
            return direction; // Normal is the same as the direction of the axis.

        // Calculate the vector from the closest point on the axis to the given point, then normalize it.
        return point.subtract(head.add(direction.scale(projectionLength))).normalize();
    }

    @Override
    BoundingBox getBoundingBox() {
        Point head = axisRay.getHead();
        Point top = axisRay.getPoint(height);
        return new BoundingBox(
                new Point(
                        Math.min(head.getX(), top.getX()) - radius,
                        Math.min(head.getY(), top.getY()) - radius,
                        Math.min(head.getZ(), top.getZ()) - radius),
                new Point(
                        Math.max(head.getX(), top.getX()) + radius,
                        Math.max(head.getY(), top.getY()) + radius,
                        Math.max(head.getZ(), top.getZ()) + radius));
    }

    @Override
    protected List<GeoPoint> findGeoIntersectionsHelper(Ray ray, double maxDistance) {
        Vector direction = axisRay.getDirection();
        List<GeoPoint> intersections = null;

        var sideIntersections = super.findGeoIntersectionsHelper(ray, maxDistance);
        if (sideIntersections != null) {
            for (GeoPoint geoPoint : sideIntersections) {
                double projectionLength = alignZero(direction.dotProduct(geoPoint.point.subtract(axisRay.getHead())));
                if (projectionLength > 0 && alignZero(height - projectionLength) > 0) {
                    if (intersections == null) intersections = new LinkedList<>();
                    intersections.add(geoPoint);
                }
            }
        }

        double nv = direction.dotProduct(ray.getDirection());
        if (isZero(nv)) return intersections;

        intersections = addBaseIntersection(ray, maxDistance, axisRay.getHead(), nv, intersections);
        return addBaseIntersection(ray, maxDistance, axisRay.getPoint(height), nv, intersections);
    }

    /**
     * Adds a base intersection if the ray hits the finite circular cap.
     */
    private List<GeoPoint> addBaseIntersection(Ray ray, double maxDistance, Point center, double nv, List<GeoPoint> intersections) {
        Point head = ray.getHead();
        Vector direction = axisRay.getDirection();

        double dx = center.getX() - head.getX();
        double dy = center.getY() - head.getY();
        double dz = center.getZ() - head.getZ();
        double t = alignZero((dx * direction.getX() + dy * direction.getY() + dz * direction.getZ()) / nv);

        if (t <= 0 || alignZero(maxDistance - t) <= 0) return intersections;

        Point point = ray.getPoint(t);
        if (alignZero(radiusSquared - point.distanceSquared(center)) < 0) return intersections;

        if (intersections == null) intersections = new LinkedList<>();
        intersections.add(new GeoPoint(this, point));
        return intersections;
    }
}
