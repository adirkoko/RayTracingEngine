package geometries;
import primitives.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link geometries.Plane} class
 *
 * @author Adir and Meir
 */
class PlaneTest {

    /**
     * Delta value for accuracy when comparing the numbers of type 'double' in assertEquals.
     */
    private final double DELTA = 0.000001;

    /**
     * Test method for {@link geometries.Plane#Plane(primitives.Point, primitives.Point, primitives.Point)}.
     */
    @Test
    public void testConstructor() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Checks if an exception is thrown when everything is fine.
        Point p1 = new Point(1, 0, 0);
        Point p2 = new Point(0, 1, 0);
        Point p3 = new Point(0, 0, 1);

        assertDoesNotThrow(() -> new Plane(p1, p2, p3), "Plane(Point, Point, Point) Failed constructing a correct Plane");

        // =============== Boundary Values Tests ==================
        // TC11: Checks if an exception is thrown when two identical points make up the plane.
        assertThrows(IllegalArgumentException.class,
                () -> new Plane(p1, p1, p3), //
                "Plane(Point, Point, Point) When the first and second points are the same, does not throw an exception");

        // TC12: Checks if an exception is thrown when all points are on the same vector.
        assertThrows(IllegalArgumentException.class,
                () -> new Plane(p1, new Point(2, 0, 0), new Point(3, 0, 0)), //
                "Plane(Point, Point, Point) When all points are on the same vector, does not throw an exception");

        // TC13: Checks if an exception is thrown when two points are coincident.
        assertThrows(IllegalArgumentException.class,
                () -> new Plane(p1, new Point(1, 0, 0), p3), //
                "Plane(Point, Point, Point) When two points are coincident, does not throw an exception");
    }

    /**
     * Test method for {@link geometries.Plane#getNormal(primitives.Point)}.
     */
    @Test
    void testGetNormalWithPoint() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Checks that the normal is the right one
        Plane plane = new Plane( //
                new Point(0, 0, 1), //
                new Point(0, 1, 0), //
                new Point(1, 0, 0));

        Vector result = plane.getNormal(new Point(0, 0, 1));

        // Check the length is 1
        assertEquals(1, result.length(), DELTA, //
                "getNormal(Point) the length of the normal is not 1");

        // Check the normal is orthogonal to the plane
        assertEquals(0d, result.dotProduct(new Vector(0, -1, 1)), //
                DELTA, "GetNormal(Point) the normal is not orthogonal to the plane");
        assertEquals(0d, result.dotProduct(new Vector(-1, 1, 0)), //
                DELTA, "GetNormal(Point) the normal is not orthogonal to the plane");
    }

    /**
     * Test method for {@link geometries.Plane#getNormal()}.
     */
    @Test
    public void testGetNormalWithoutPoint() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Checks that the normal is the right one
        Point p1 = new Point(1, 0, 0);
        Point p2 = new Point(0, 2, 1);
        Point p3 = new Point(2, 0, 1);

        Vector result = new Plane(p1, p2, p3).getNormal();

        // Check the length is 1
        assertEquals(1, result.length(), DELTA, "GetNormal() the length of the normal is not 1");

        // Check the normal is orthogonal to the vectors of the plane
        assertEquals(0d, result.dotProduct(p2.subtract(p1)), DELTA, "GetNormal() the normal is not orthogonal to the plane");
        assertEquals(0d, result.dotProduct(p3.subtract(p1)), DELTA, "GetNormal() the normal is not orthogonal to the plane");
    }
}