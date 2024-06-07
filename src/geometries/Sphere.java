package geometries;
import primitives.*;
import java.util.List;
import static java.lang.Math.sqrt;

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
        if (center.equals(ray.getHead())) {
            return List.of(ray.getPoint(radius));
        }

        // Calculate the vector from the ray's origin to the sphere's center
        Vector toCenter = center.subtract(ray.getHead());

        // Project toCenter onto the ray's direction to find tm
        double projectionLength = ray.getDirection().dotProduct(toCenter);

        // Calculate the distance from the sphere's center to the ray
        double distanceToRay = sqrt(toCenter.lengthSquared() - projectionLength * projectionLength);

        // If the distance is greater than or equal to the radius, there are no intersections
        if (distanceToRay >= radius) {
            return null;
        }

        // Calculate th, the distance from the projection to the intersection points
        double offset = sqrt(radius * radius - distanceToRay * distanceToRay);

        // Calculate the distances to the intersection points
        double t1 = projectionLength - offset;
        double t2 = projectionLength + offset;

        // Check the intersection points and return them
        if (Util.alignZero(t1) > 0 && Util.alignZero(t2) > 0) {
            Point intersection1 = ray.getPoint(t1);
            Point intersection2 = ray.getPoint(t2);
            return List.of(intersection1, intersection2);
        }
        if (Util.alignZero(t1) > 0) {
            Point intersection1 = ray.getPoint(t1);
            return List.of(intersection1);
        }
        if (Util.alignZero(t2) > 0) {
            Point intersection2 = ray.getPoint(t2);
            return List.of(intersection2);
        }
        return null;
    }


}
