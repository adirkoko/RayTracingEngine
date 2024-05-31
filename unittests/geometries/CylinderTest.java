package geometries;
import primitives.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CylinderTest {

    /**
     * Test method for {@link geometries.Cylinder#getNormal(primitives.Point)}.
     */
    @Test
    void testGetNormal() {
        Cylinder c1 = new Cylinder(new Ray(new Point(0, 0, 0), new Vector(0, 0, 1)), 1, 5);

        // ============ Equivalence Partitions Tests ==============
        // TC01: Checks that the normal is correct on the side surface
        assertEquals(new Vector(0, 1, 0), c1.getNormal(new Point(0, 1, 2)), "getNormal(Point) Wrong normal to side surface");

        // TC02: Checks that the normal is correct on the bottom base
        assertEquals(new Vector(0, 0, -1), c1.getNormal(new Point(0.5, 0.5, 0)), "getNormal(Point) Wrong normal to bottom base");

        // TC03: Checks that the normal is correct on the top base
        assertEquals(new Vector(0, 0, 1), c1.getNormal(new Point(0.5, 0.5, 5)), "getNormal(Point) Wrong normal to top base");

        // =============== Boundary Values Tests ==================
        // TC11: Checks that the normal is correct at the center of the bottom base
        assertEquals(new Vector(0, 0, -1), c1.getNormal(new Point(0, 0, 0)), "getNormal(Point) Wrong normal to center of bottom base");

        // TC12: Checks that the normal is correct at the center of the top base
        assertEquals(new Vector(0, 0, 1), c1.getNormal(new Point(0, 0, 5)), "getNormal(Point) Wrong normal to center of top base");
    }
}