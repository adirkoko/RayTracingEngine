package geometries;

import primitives.*;

import java.util.List;

import static java.lang.Math.sqrt;
import static primitives.Util.alignZero;

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
    public List<GeoPoint> findGeoIntersectionsHelper(Ray ray) {
        // Check if the ray starts at the center of the sphere
        if (center.equals(ray.getHead())) {
            // The intersection point is on the surface of the sphere
            return List.of(new GeoPoint(this, ray.getPoint(radius)));
        }

        // Calculate the vector from the ray's head to the center of the sphere
        Vector toCenter = center.subtract(ray.getHead());
        double projectionLength = ray.getDirection().dotProduct(toCenter);

        // Calculate the squared distance from the center of the sphere to the ray
        double squaredDistanceToRay = toCenter.lengthSquared() - projectionLength * projectionLength;
        double squaredOffset = radiusSquared - squaredDistanceToRay;
        if (alignZero(squaredOffset) <= 0) return null; // No intersection if the distance is greater than the radius

        // Calculate the distance from the projection point to the intersection points
        double offset = sqrt(squaredOffset);

        // Calculate the parameter t for the second intersection point
        double t2 = projectionLength + offset;
        if (alignZero(t2) <= 0) return null; // Both intersection points are behind the ray's origin

        // Calculate the parameter t for the first intersection point
        double t1 = projectionLength - offset;

        // Return the intersection as a GeoPoint
        return alignZero(t1) <= 0
                ? List.of(new GeoPoint(this, ray.getPoint(t2))) // Only the second intersection point is valid
                : List.of(new GeoPoint(this, ray.getPoint(t1)), new GeoPoint(this, ray.getPoint(t2))); // Both intersection points are valid
    }

}
