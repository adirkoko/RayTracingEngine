package geometries;

import primitives.*;

import java.util.List;

import static primitives.Util.alignZero;
import static primitives.Util.isZero;

/**
 * Represents a Plane in 3D space
 *
 * @author Adir and Meir.
 */
public class Plane extends Geometry {

    /**
     * The base point of the plane.
     */
    private final Point q;

    /**
     * The normal vector of the plane.
     */
    private final Vector normal;

    /**
     * Constructor using three points.
     *
     * @param p1 First point on the plane
     * @param p2 Second point on the plane
     * @param p3 Third point on the plane
     * @throws IllegalArgumentException when there are convergent pairs of points or the points are co-linear
     */
    public Plane(Point p1, Point p2, Point p3) {
        this.q = p1;
        this.normal = p2.subtract(p1).crossProduct(p3.subtract(p1)).normalize();
    }

    /**
     * Constructor using a point and a normal vector.
     *
     * @param point  Point on the plane
     * @param normal Normal vector of the plane
     */
    public Plane(Point point, Vector normal) {
        this.q = point;
        this.normal = normal.normalize();
    }

    /**
     * Gets the normal of the plane at a particular point.
     *
     * @param point the point on the plane
     * @return the normal vector
     */
    @Override
    public Vector getNormal(Point point) {
        return this.normal;
    }

    /**
     * Gets the normal of the plane.
     *
     * @return the normal vector
     */
    public Vector getNormal() {
        return this.normal;
    }

    @Override
    protected List<GeoPoint> findGeoIntersectionsHelper(Ray ray, double maxDistance) {
        Point head = ray.getHead();
        // If the ray's starting point is on the plane, return null
        if (q.equals(head)) return null;

        Vector direction = ray.getDirection();
        double nv = normal.dotProduct(direction);
        // If the ray is parallel to the plane, return null
        if (isZero(nv)) return null;

        double t = normal.dotProduct(q.subtract(head)) / nv;
        // Return null if the intersection is behind the ray's start, or if it exceeds the max distance
        if (alignZero(t) <= 0 || alignZero(maxDistance - t) <= 0) return null;

        return List.of(new GeoPoint(this, ray.getPoint(t)));
    }
}