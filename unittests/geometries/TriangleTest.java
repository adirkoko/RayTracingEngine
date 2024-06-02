package geometries;

import primitives.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TriangleTest {

    /**
     * Delta value for accuracy when comparing the numbers of type 'double' in assertEquals.
     */
    private final double DELTA = 0.000001;

    /**
     * Test method for {@link geometries.Triangle#Triangle(primitives.Point, primitives.Point, primitives.Point)}.
     */
    @Test
    public void testConstructor() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Checks if an exception is thrown when everything is fine.
        Point p1 = new Point(1, 0, 0);
        Point p2 = new Point(0, 1, 0);
        Point p3 = new Point(0, 0, 1);
        assertDoesNotThrow(() -> new Triangle(p1, p2, p3), //
                "Triangle(Point, Point, Point) Failed constructing a correct triangle");

        // =============== Boundary Values Tests ==================
        // TC11: Checks if an exception is thrown when two identical points make up the triangle.
        assertThrows(IllegalArgumentException.class, //
                () -> new Triangle(p1, p1, p3), //
                "Triangle(Point, Point, Point) When the first and second points are the same, does not throw an exception");

        // TC12: Checks if all points are on the same line.
        assertThrows(IllegalArgumentException.class, //
                () -> new Triangle(new Point(0, 0, 1), new Point(0, 0.5, 1), new Point(0, 1, 1)), //
                "Triangle(Point, Point, Point) Constructed a triangle with all points on the same line, does not throw an exception");
    }

    /**
     * Test method for {@link geometries.Polygon#getNormal(primitives.Point)}.
     */
    @Test
    void testGetNormal() {
        Point p1 = new Point(1, 0, 0);
        Point p2 = new Point(0, 1, 0);
        Point p3 = new Point(0, 0, 1);
        // ============ Equivalence Partitions Tests ==============
        // TC01: Checks that the normal is the right one.
        Triangle triangle = new Triangle(p1, p2, p3);

        Vector result = new Triangle(p1, p2, p3).getNormal(p3);

        // Check the length is 1
        assertEquals(1, result.length(), DELTA, "getNormal(Point) the length of the normal is not 1");

        // Check the normal is orthogonal to one edge of the triangle.
        assertEquals(0d, result.dotProduct(p2.subtract(p1)), DELTA, "getNormal(Point) the normal is not orthogonal to the edge p1-p2");

        // Check exception for point not on the triangle
        assertThrows(IllegalArgumentException.class,
                () -> triangle.getNormal(new Point(1, 1, 1)), //
                "getNormal(Point) the point is not on the triangle, does not throw an exception");
    }
}