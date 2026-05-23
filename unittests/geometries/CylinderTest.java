package geometries;

import primitives.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link geometries.Cylinder} class
 *
 * @author Adir and Meir
 */
class CylinderTest {

    /**
     * Test method for {@link geometries.Cylinder#getNormal(primitives.Point)}.
     */
    @Test
    void testGetNormal() {
        Cylinder c1 = new Cylinder(new Ray(Point.ZERO, new Vector(0, 0, 1)), 1, 5);

        // ============ Equivalence Partitions Tests ==============
        // TC01: Checks that the normal is correct on the side surface
        assertEquals(new Vector(0, 1, 0), c1.getNormal(new Point(0, 1, 2)), "getNormal(Point) Wrong normal to side surface");

        // TC02: Checks that the normal is correct on the bottom base
        assertEquals(new Vector(0, 0, -1), c1.getNormal(new Point(0.5, 0.5, 0)), "getNormal(Point) Wrong normal to bottom base");

        // TC03: Checks that the normal is correct on the top base
        assertEquals(new Vector(0, 0, 1), c1.getNormal(new Point(0.5, 0.5, 5)), "getNormal(Point) Wrong normal to top base");

        // =============== Boundary Values Tests ==================
        // TC11: Checks that the normal is correct at the center of the bottom base
        assertEquals(new Vector(0, 0, -1), c1.getNormal(Point.ZERO), "getNormal(Point) Wrong normal to center of bottom base");

        // TC12: Checks that the normal is correct at the center of the top base
        assertEquals(new Vector(0, 0, 1), c1.getNormal(new Point(0, 0, 5)), "getNormal(Point) Wrong normal to center of top base");
    }

    /**
     * Test method for {@link geometries.Cylinder#findIntersections(primitives.Ray)}.
     */
    @Test
    void testFindIntersections() {
        Cylinder cylinder = new Cylinder(new Ray(Point.ZERO, new Vector(0, 0, 1)), 1, 2);

        // TC01: Ray crosses the side surface inside the cylinder height
        assertEquals(List.of(new Point(-1, 0, 1), new Point(1, 0, 1)),
                cylinder.findIntersections(new Ray(new Point(-2, 0, 1), new Vector(1, 0, 0))),
                "Ray should cross the cylinder side in two points");

        // TC02: Ray crosses the infinite tube, but outside the cylinder height
        assertNull(cylinder.findIntersections(new Ray(new Point(-2, 0, 3), new Vector(1, 0, 0))),
                "Cylinder should reject side intersections outside its height");

        // TC03: Ray crosses both bases
        assertEquals(List.of(new Point(0.5, 0, 0), new Point(0.5, 0, 2)),
                cylinder.findIntersections(new Ray(new Point(0.5, 0, -1), new Vector(0, 0, 1))),
                "Ray should intersect both cylinder bases");
    }
}
