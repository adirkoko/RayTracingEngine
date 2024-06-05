package geometries;

import primitives.*;

import java.util.List;

/**
 * Represents a sphere in 3D space.
 *
 * @author Adir and Meir.
 */
public class Sphere extends RadialGeometry {

    /**
     * the center of the Sphere.
     */
    private final Point center;

    /**
     * Constructor for Sphere.
     *
     * @param center The center point of the sphere.
     * @param radius The radius of the sphere.
     */
    public Sphere(Point center, double radius) {
        super(radius);
        this.center = center;
    }

    @Override
    public Vector getNormal(Point point) {
        return point.subtract(center).normalize();
    }

    @Override
    public List<Point> findIntersections(Ray ray) {
        Point p0 = ray.getHead();
        Vector dir = ray.getDirection();

        // Handle case where ray starts at the center of the sphere
        if (center.equals(p0))
            return List.of(ray.getPoint(radius));

        // Calculate the vector from the ray's starting point to the sphere's center
        Vector u = center.subtract(p0);
        double tm = dir.dotProduct(u);

        //The point of the ray head is in such a position that the direction of the ray is farther from the center of the sphere.
        //That is, any potential intersection point will be behind the ray head, and therefore will not count as an intersection.
        if (tm < 0) {
            return null;
        }

        double dSquared = u.lengthSquared() - tm * tm;

        // If the distance from the sphere's center to the ray is greater than the radius, there's no intersection
        if (dSquared >= radius * radius)
            return null;

        double th = Math.sqrt(radius * radius - dSquared);
        double t1 = tm - th;
        double t2 = tm + th;

        // If both intersection points are behind the ray's origin, return null
        if (Util.alignZero(t1) <= 0 && Util.alignZero(t2) <= 0)
            return null;

        // If only t2 is positive, return the point corresponding to t2
        if (Util.alignZero(t1) <= 0)
            return List.of(ray.getPoint(t2));

        // If only t1 is positive, return the point corresponding to t1
        if (Util.alignZero(t2) <= 0)
            return List.of(ray.getPoint(t1));

        // If both t1 and t2 are positive, return both points
        return List.of(ray.getPoint(t1), ray.getPoint(t2));
    }

}
