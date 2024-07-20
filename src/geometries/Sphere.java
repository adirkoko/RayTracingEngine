package geometries;

import primitives.*;

import java.util.List;

import static primitives.Util.alignZero;

/**
 * Represents a sphere in 3D space.
 * Extends the RadialGeometry class.
 * Provides methods to calculate the normal vector and find intersections with rays.
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
    protected List<GeoPoint> findGeoIntersectionsHelper(Ray ray, double maxDistance) {
        // Check if the ray starts at the center of the sphere
        if (center.equals(ray.getHead())) {
            return alignZero(maxDistance - radius) > 0
                    ? List.of(new GeoPoint(this, ray.getPoint(radius)))
                    : null;
        }

        // Calculate the vector from the ray's head to the center of the sphere
        Vector toCenter = center.subtract(ray.getHead());
        double projectionLength = ray.getDirection().dotProduct(toCenter);

        // Calculate the squared distance from the center of the sphere to the ray
        double squaredDistanceToRay = toCenter.lengthSquared() - projectionLength * projectionLength;
        double squaredOffset = radiusSquared - squaredDistanceToRay;
        if (alignZero(squaredOffset) <= 0) return null; // No intersection if the distance is greater than the radius

        // Calculate the distance from the projection point to the intersection points
        double offset = Math.sqrt(squaredOffset);

        // Calculate the parameter t for the second intersection point
        // Calculate the parameter t for the first intersection point
        double t1 = alignZero(projectionLength - offset);
        double t2 = alignZero(projectionLength + offset);
        if (t2 <= 0 || alignZero(maxDistance - t1) <= 0)
            return null; // Both intersection points are behind the ray's origin or beyond maxDistance

        // Return the intersection points as GeoPoints, filtering by maxDistance
        if (alignZero(maxDistance - t2) > 0)
            return t1 <= 0
                    ? List.of(new GeoPoint(this, ray.getPoint(t2)))
                    : List.of(
                    new GeoPoint(this, ray.getPoint(t1)),
                    new GeoPoint(this, ray.getPoint(t2))
            );
        else
            return t1 <= 0 ? null : List.of(new GeoPoint(this, ray.getPoint(t1)));
    }

}
