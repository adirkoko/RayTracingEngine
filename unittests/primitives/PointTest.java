package primitives;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for primitives.Point class
 *
 * @author Adir and Meir
 */
class PointTest {
    /**
     * Delta value for accuracy when comparing the numbers of type 'double' in assertEquals.
     */
    private final double DELTA = 0.000001;

    /**
     * Test method for {@link primitives.Point#subtract(primitives.Point)}.
     */
    @Test
    void subtract() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Checks whether the result of the subtraction is the correct result.
        assertEquals(new Point(3, -5, -1), //
                new Point(4, -3, 2).subtract(new Vector(1, 2, 3)), //
                "subtract() wrong result");

        // =============== Boundary Values Tests ==================
        // TC11: Checks whether subtracting a point from itself throws an exception because zero vectors are not allowed.
        Point p = new Point(1, 2, 3);
        assertThrows(IllegalArgumentException.class, () -> p.subtract(p), //
                "subtract() for subtracting a vector from itself, does not throw an exception");
    }

    /**
     * Test method for {@link primitives.Point#add(primitives.Vector)}.
     */
    @Test
    void add() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Checks whether the result of the sum is the correct result.
        assertEquals(new Point(4, 4, 4), //
                new Point(3, 2, 1).add(new Vector(1, 2, 3)), //
                "add() wrong result");
    }


    /**
     * Test method for {@link primitives.Point#distanceSquared(Point)}.
     */
    @Test
    void distanceSquared() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Checks the squared distance between two points.
        assertEquals(27d, new Point(1, 2, 3).distanceSquared(new Point(4, 5, 6)), //
                DELTA, "distanceSquared() wrong result");

        // =============== Boundary Values Tests ==================
        // TC11: Checks the squared distance between a point and itself.
        assertEquals(0d, new Point(1, 2, 3).distanceSquared(new Point(1, 2, 3)), //
                DELTA, "distanceSquared() The squared distance between a point and itself should be zero.");
    }

    /**
     * Test method for {@link primitives.Point#distance(Point)}.
     */
    @Test
    void distance() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Checks distance between two points.
        assertEquals(5.196152d, new Point(1, 2, 3).distance(new Point(4, 5, 6)), //
                DELTA, "distance() wrong result");

        // =============== Boundary Values Tests ==================
        // TC11: Checks the distance between a point and itself.
        assertEquals(0d, new Point(1, 2, 3).distance(new Point(1, 2, 3)), //
                DELTA, "distance() The distance between a point and itself should be zero.");
    }
}