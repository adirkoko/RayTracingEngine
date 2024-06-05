package geometries;

import primitives.*;

import java.util.List;

/**
 * Represents a triangle in 3D space, defined by three vertices.
 *
 * @author Adir and Meir.
 */
public class Triangle extends Polygon {

    /**
     * Constructor for Triangle using three points.
     *
     * @param p1 First vertex of the triangle
     * @param p2 Second vertex of the triangle
     * @param p3 Third vertex of the triangle
     */
    public Triangle(Point p1, Point p2, Point p3) {
        super(p1, p2, p3);
    }

    @Override
    public List<Point> findIntersections(Ray ray) {
        // Step 1: Find intersections with the plane
        // First, we find where the ray intersects with the plane of the triangle.
        List<Point> intersections = plane.findIntersections(ray);
        if (intersections == null) {
            return null; // If there's no intersection with the plane, there's no intersection with the triangle.
        }

        // Step 2: Get the intersection point with the plane
        // If there is an intersection, get the intersection point.
        Point intersection = intersections.get(0);

        // Step 3: Check if the intersection point is inside the triangle using barycentric coordinates
        // Get the vertices of the triangle.
        Point a = vertices.get(0);
        Point b = vertices.get(1);
        Point c = vertices.get(2);

        // Create vectors for the edges of the triangle and the vector from vertex a to the intersection point.
        Vector v0 = b.subtract(a);
        Vector v1 = c.subtract(a);
        Vector v2 = intersection.subtract(a);

        // Step 4: Calculate dot products for barycentric coordinates
        // Compute dot products of the edge vectors and the vector to the intersection point.
        double d00 = v0.dotProduct(v0);
        double d01 = v0.dotProduct(v1);
        double d11 = v1.dotProduct(v1);
        double d20 = v2.dotProduct(v0);
        double d21 = v2.dotProduct(v1);

        // Step 5: Calculate barycentric coordinates
        // Calculate the barycentric coordinates (u, v, w) for the intersection point.
        double denom = d00 * d11 - d01 * d01;
        double v = (d11 * d20 - d01 * d21) / denom;
        double w = (d00 * d21 - d01 * d20) / denom;
        double u = 1.0 - v - w;

        // Step 6: Check if the intersection point is inside the triangle
        // If all barycentric coordinates are between 0 and 1, the intersection point is inside the triangle.
        if (u >= 0 && v >= 0 && w >= 0) {
            return List.of(intersection); // Return the intersection point as a list.
        }

        return null; // If the intersection point is not inside the triangle, return null.
    }
}