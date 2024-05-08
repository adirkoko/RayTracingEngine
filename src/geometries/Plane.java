package geometries;

import primitives.Point;
import primitives.Vector;

/**
 * Represents a Plane in 3D space.
 */
public class Plane implements Geometry {
    private final Point q;
    private final Vector normal;

    /**
     * Constructor using three points.
     *
     * @param p1 First point on the plane
     * @param p2 Second point on the plane
     * @param p3 Third point on the plane
     */
    public Plane(Point p1, Point p2, Point p3) {
        this.q = p1;
        this.normal = null;  // Normal calculation will be implemented in the next stage
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
}