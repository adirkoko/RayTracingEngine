package geometries;
import primitives.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SphereTest {

    /**
     * Delta value for accuracy when comparing the numbers of type 'double' in assertEquals.
     */
    private final double DELTA = 0.000001;

    /**
     * Test method for {@link geometries.Sphere#getNormal(Point)}.
     */
    @Test
    public void testGetNormal() {
        // ============ Equivalence Partitions Tests ==============
        Sphere sphere = new Sphere(new Point(0, 0, 0), 4);
        Vector normal;

        // TC01: Test normal on positive X-axis.
        normal = sphere.getNormal(new Point(4, 0, 0));
        assertEquals(new Vector(1, 0, 0), normal, "getNormal() wrong normal vector");
        assertEquals(1, normal.length(), DELTA, "getNormal() normal vector is not a unit vector");

        // TC02: Test normal on positive Y-axis
        normal = sphere.getNormal(new Point(0, 4, 0));
        assertEquals(new Vector(0, 1, 0), normal, "getNormal() wrong normal vector");
        assertEquals(1, normal.length(), DELTA, "getNormal() normal vector is not a unit vector");

        // TC03: Test normal on positive Z-axis
        normal = sphere.getNormal(new Point(0, 0, 4));
        assertEquals(new Vector(0, 0, 1), normal, "getNormal() wrong normal vector");
        assertEquals(1, normal.length(), DELTA, "getNormal() normal vector is not a unit vector");
    }
}