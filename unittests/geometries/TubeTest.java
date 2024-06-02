package geometries;
import primitives.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TubeTest {

    /**
     * Delta value for accuracy when comparing the numbers of type 'double' in assertEquals.
     */
    private final double DELTA = 0.000001;

    /**
     * Test method for {@link geometries.Tube#getNormal(primitives.Point)}.
     */
    @Test
    void testGetNormal() {
        // Tube with an axis in the X direction and radius 1.
        Tube tube = new Tube(new Ray(new Point(0, 0, 0), new Vector(1, 0, 0)), 1);
        Vector normal;

        // ============ Equivalence Partitions Tests ==============

        // TC01: Checks that the normal is the right one.
        normal = tube.getNormal(new Point(1, 1, 0));
        assertEquals(new Vector(0, 1, 0), normal, "getNormal(Point) wrong result");
        assertEquals(1d, normal.length(), DELTA, "getNormal(Point) normal vector is not a unit vector");

        // TC02: Checks that the normal is the right one.
        normal = tube.getNormal(new Point(2, 0, 1));
        assertEquals(new Vector(0, 0, 1), normal, "getNormal(Point) wrong result");
        assertEquals(1d, normal.length(), DELTA, "getNormal(Point) normal vector is not a unit vector");

        // =============== Boundary Values Tests ==================

        // TC11: Test normal at a point directly in front of the ray's head (1, 1, 0)
        Vector vector = new Tube(new Ray(new Point(0, 0, 0), new Vector(0, 0, 1)), 1).getNormal(new Point(0, 1, 0));
        assertEquals(new Vector(0, 1, 0), vector, "getNormal(Point) wrong result");
        assertEquals(1d, vector.length(), DELTA, "getNormal(Point) normal vector is not a unit vector");
    }
}