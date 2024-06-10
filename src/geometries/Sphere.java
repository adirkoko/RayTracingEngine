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
    public List<Point> findIntersections(Ray ray) {
        // Check if the ray starts at the sphere's center
        if (center.equals(ray.getHead()))
            return List.of(ray.getPoint(radius));

        // Calculate the vector from the ray's origin to the sphere's center
        Vector toCenter = center.subtract(ray.getHead());
        // Project toCenter onto the ray's direction to find tm
        double projectionLength = ray.getDirection().dotProduct(toCenter);
        // Calculate the distance from the sphere's center to the ray
        double squaredDistanceToRay = toCenter.lengthSquared() - projectionLength * projectionLength;
        double squaredOffset = radiusSquared - squaredDistanceToRay;
        // If the distance is greater than or equal to the radius, there are no intersections
        if (alignZero(squaredOffset) <= 0)
            return null;

        // Calculate th, the distance from the projection to the intersection points
        double offset = sqrt(squaredOffset);
        // Calculate the distances to the intersection points: always t1 < t2
        double t2 = projectionLength + offset;
        if (alignZero(t2) <= 0) return null;

        double t1 = projectionLength - offset;
        return alignZero(t1) <= 0
                ? List.of(ray.getPoint(t2))
                : List.of(ray.getPoint(t1), ray.getPoint(t2));
    }

}
