package geometries;

import primitives.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link geometries.Tube} class
 *
 * @author Adir and Meir
 */
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
        Tube tube = new Tube(new Ray(Point.ZERO, new Vector(1, 0, 0)), 1);
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
        Vector vector = new Tube(new Ray(Point.ZERO, new Vector(0, 0, 1)), 1).getNormal(new Point(0, 1, 0));
        assertEquals(new Vector(0, 1, 0), vector, "getNormal(Point) wrong result");
        assertEquals(1d, vector.length(), DELTA, "getNormal(Point) normal vector is not a unit vector");
    }

    /**
     * Test method for {@link geometries.Tube#findIntersections(primitives.Ray)}.
     */
    @Test
    void testFindIntersections() {
        Tube tube = new Tube(new Ray(Point.ZERO, new Vector(0, 0, 1)), 1);

        // ============ Equivalence Partitions Tests ==============

        // TC01: Ray crosses the tube in two points
        assertEquals(List.of(new Point(-1, 0, 1), new Point(1, 0, 1)),
                tube.findIntersections(new Ray(new Point(-2, 0, 1), new Vector(1, 0, 0))),
                "Ray should cross the tube in two points");

        // TC02: Ray starts inside the tube and crosses it once
        assertEquals(List.of(new Point(Math.sqrt(0.75), 0.5, 1)),
                tube.findIntersections(new Ray(new Point(0, 0.5, 1), new Vector(1, 0, 0))),
                "Ray from inside the tube should have one intersection");

        // TC03: Ray is outside the tube
        assertNull(tube.findIntersections(new Ray(new Point(-2, 2, 1), new Vector(1, 0, 0))),
                "Ray outside the tube should not intersect");

        // =============== Boundary Values Tests ==================

        // TC11: Ray is tangent to the tube
        assertNull(tube.findIntersections(new Ray(new Point(-1, 1, 1), new Vector(1, 0, 0))),
                "Tangent ray should not be counted as an intersection");

        // TC12: Ray is parallel to the tube axis
        assertNull(tube.findIntersections(new Ray(new Point(2, 0, 0), new Vector(0, 0, 1))),
                "Parallel ray outside the tube should not intersect");

        // TC13: Ray starts on the surface and goes inside the tube
        assertEquals(List.of(new Point(-1, 0, 1)),
                tube.findIntersections(new Ray(new Point(1, 0, 1), new Vector(-1, 0, 0))),
                "Ray from the surface into the tube should intersect once");

        // TC14: Ray starts on the surface and goes outside the tube
        assertNull(tube.findIntersections(new Ray(new Point(1, 0, 1), new Vector(1, 0, 0))),
                "Ray from the surface away from the tube should not intersect");
    }

    /**
     * Test method for {@link geometries.Tube#findGeoIntersectionsHelper(primitives.Ray, double)}.
     */
    @Test
    void testFindGeoIntersectionsHelper() {
        Tube tube = new Tube(new Ray(Point.ZERO, new Vector(0, 0, 1)), 1);

        // TC01: Both intersections are within max distance
        var result = tube.findGeoIntersectionsHelper(new Ray(new Point(-2, 0, 1), new Vector(1, 0, 0)), 4);
        assertEquals(2, result.size(), "Wrong number of intersections");

        // TC02: Only the first intersection is within max distance
        result = tube.findGeoIntersectionsHelper(new Ray(new Point(-2, 0, 1), new Vector(1, 0, 0)), 2);
        assertEquals(List.of(new Intersectable.GeoPoint(tube, new Point(-1, 0, 1))), result,
                "Only the closer intersection should be returned");

        // TC03: The intersection is exactly at max distance
        assertNull(tube.findGeoIntersectionsHelper(new Ray(new Point(-2, 0, 1), new Vector(1, 0, 0)), 1),
                "Intersection at max distance should not be included");
    }
}
