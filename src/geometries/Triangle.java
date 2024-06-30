package geometries;

import primitives.*;

import java.util.ArrayList;
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
     * @param p1 First vertex of the triangle.
     * @param p2 Second vertex of the triangle.
     * @param p3 Third vertex of the triangle.
     */
    public Triangle(Point p1, Point p2, Point p3) {
        super(p1, p2, p3);
    }

    @Override
    public List<GeoPoint> findGeoIntersectionsHelper(Ray ray) {
        // Find intersections with the plane containing the polygon
        var planeIntersections = plane.findGeoIntersections(ray);
        if (planeIntersections == null) return null; // No intersection with the plane

        // Ray's origin and direction
        Point head = ray.getHead();
        Vector direction = ray.getDirection();

        // Calculate vectors from the ray's origin to the polygon's vertices
        Vector vectorToVertex1 = vertices.get(0).subtract(head);
        Vector vectorToVertex2 = vertices.get(1).subtract(head);

        // Calculate the normal of the plane formed by the first two vertices and the ray's origin
        Vector normal1 = vectorToVertex1.crossProduct(vectorToVertex2).normalize();
        double dotProduct1 = alignZero(direction.dotProduct(normal1));
        if (dotProduct1 == 0) return null; // Ray is parallel to the plane

        // Calculate the vector from the ray's origin to the third vertex
        Vector vectorToVertex3 = vertices.get(2).subtract(head);

        // Calculate the normal of the plane formed by the second and third vertices and the ray's origin
        Vector normal2 = vectorToVertex2.crossProduct(vectorToVertex3).normalize();
        double dotProduct2 = alignZero(direction.dotProduct(normal2));
        if (dotProduct1 * dotProduct2 <= 0) return null; // Intersection point is outside the polygon

        // Calculate the normal of the plane formed by the third and first vertices and the ray's origin
        Vector normal3 = vectorToVertex3.crossProduct(vectorToVertex1).normalize();
        double dotProduct3 = alignZero(direction.dotProduct(normal3));
        if (dotProduct1 * dotProduct3 <= 0) return null; // Intersection point is outside the polygon

        // Create a list of GeoPoints with the intersection points from the plane
        List<GeoPoint> geoPoints = new ArrayList<>();
        for (GeoPoint geoPoint : planeIntersections) {
            geoPoints.add(new GeoPoint(this, geoPoint.point));
        }
        return geoPoints; // The intersection point is inside the polygon
    }

}