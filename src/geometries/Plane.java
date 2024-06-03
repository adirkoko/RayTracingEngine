package geometries;
import primitives.*;
import java.util.List;

/**
 * Represents a Plane in 3D space
 *
 * @author Adir and Meir.
 */
public class Plane implements Geometry {

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
     *
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
    public List<Point> findIntersections(Ray ray) {
        Point p0 = ray.getHead();
        Vector v = ray.getDirection();

        // Check if the ray starts at the reference point of the plane
        if (q.equals(p0)) {
            return null;
        }

        // Calculate the dot product of the plane's normal and the ray's direction vector
        double nv = normal.dotProduct(v);

        // If the dot product is zero, the ray is parallel to the plane
        if (Util.isZero(nv)) {
            return null;
        }

        // Calculate the parameter t for the intersection point
        double t = (q.subtract(p0)).dotProduct(normal) / nv;

        // If t is negative or zero, the intersection point is behind the ray's origin or at the origin
        if (Util.alignZero(t) <= 0) {
            return null;
        }

        // Calculate and return the intersection point
        return List.of(ray.getPoint(t));
    }
}