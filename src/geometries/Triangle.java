package geometries;

import primitives.*;

import java.util.List;

import static primitives.Util.alignZero;

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
        // Find intersections with the plane containing the triangle
        var planeIntersections = plane.findIntersections(ray);
        if (planeIntersections == null)
            // No intersection with the plane, therefore no intersection with the triangle
            return null;

        // Calculate vectors from the ray's origin to the triangle's vertices
        Point p0 = ray.getHead();
        // Get the direction vector of the ray
        Vector rayDirection = ray.getDirection();

        Vector vectorToVertex1 = vertices.get(0).subtract(p0);
        Vector vectorToVertex2 = vertices.get(1).subtract(p0);
        // Calculate normal vectors for the planes formed by the ray and the triangle's edges
        Vector normal1 = vectorToVertex1.crossProduct(vectorToVertex2).normalize();
        // Check if the intersection point is inside the triangle
        double dotProduct1 = alignZero(rayDirection.dotProduct(normal1));
        if (dotProduct1 == 0) return null;

        Vector vectorToVertex3 = vertices.get(2).subtract(p0);
        Vector normal2 = vectorToVertex2.crossProduct(vectorToVertex3).normalize();
        double dotProduct2 = alignZero(rayDirection.dotProduct(normal2));
        if (dotProduct1 * dotProduct2 <= 0) return null;

        Vector normal3 = vectorToVertex3.crossProduct(vectorToVertex1).normalize();
        double dotProduct3 = alignZero(rayDirection.dotProduct(normal3));
        if (dotProduct1 * dotProduct3 <= 0) return null;

        return planeIntersections; // The intersection point is inside the triangle
    }

}