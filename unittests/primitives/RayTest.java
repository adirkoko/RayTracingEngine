package primitives;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link primitives.Ray} class
 *
 * @author Adir and Meir
 */
class RayTest {

    /**
     * Test method for {@link primitives.Ray#getPoint(double)}.
     */
    @Test
    void testGetPoint() {
        Ray ray = new Ray(new Point(1, 1, 1), new Vector(1, 0, 0));

        // ============ Equivalence Partitions Tests ==============

        // TC01: Positive distance
        assertEquals(new Point(3, 1, 1), ray.getPoint(2), "Error: Wrong point for positive distance");

        // TC02: Negative distance
        assertEquals(new Point(-1, 1, 1), ray.getPoint(-2), "Error: Wrong point for negative distance");

        // =============== Boundary Values Tests ==================

        // TC03: Zero distance
        assertEquals(new Point(1, 1, 1), ray.getPoint(0), "Error: Wrong point for zero distance");
    }

    /**
     * Test method for {@link primitives.Ray#findClosestPoint(List<Point>)}.
     */
    @Test
    void testFindClosestPoint() {
        Ray ray = new Ray(new Point(1, 1, 1), new Vector(0, 0, 1));
        Point p1 = new Point(1, 1, 2);
        Point p2 = new Point(1, 1, 3);
        Point p3 = new Point(1, 1, 4);

        // ============ Equivalence Partitions Tests ==============
        // TC01: The closest point is in the middle of the list
        List<Point> points = List.of(p2, p1, p3);
        assertEquals(p1, ray.findClosestPoint(points), "Error: The closest point is in the middle of the list");

        // =============== Boundary Values Tests ==================
        // TC02: The list is empty
        points = List.of();
        assertNull(ray.findClosestPoint(points), "Error: The list is empty");

        // TC03: The closest point is the first point in the list
        points = List.of(p1, p2, p3);
        assertEquals(p1, ray.findClosestPoint(points), "Error: The closest point is the first point in the list");

        // TC04: The closest point is the last point in the list
        points = List.of(p3, p2, p1);
        assertEquals(p1, ray.findClosestPoint(points), "Error: The closest point is the last point in the list");

    }
}
