package geometries;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link geometries.Sphere} class
 *
 * @author Adir and Meir
 */
public class SphereTest {

    /**
     * Predefined point (1, 0, 0) used in test cases.
     */
    private final Point p100 = new Point(1, 0, 0);

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
        Sphere sphere = new Sphere(Point.ZERO, 4);
        Vector normal;

        // TC01: Test normal on positive X-axis.
        normal = sphere.getNormal(new Point(4, 0, 0));
        assertEquals(new Vector(1, 0, 0), normal, "getNormal() wrong normal vector");
        assertEquals(1, normal.length(), DELTA, "getNormal() normal vector is not a unit vector");
    }

    /**
     * Test method for {@link geometries.Sphere#findIntersections(primitives.Ray)}.
     */
    @Test
    public void testFindIntersections() {
        Sphere sphere = new Sphere(p100, 1d); // Sphere centered at (1, 0, 0) with radius 1
        final Point gp1 = new Point(0.0651530771650466, 0.355051025721682, 0); // Expected intersection point 1
        final Point gp2 = new Point(1.53484692283495, 0.844948974278318, 0); // Expected intersection point 2
        final List<Point> exp = List.of(gp1, gp2); // List of expected intersection points
        final Vector v310 = new Vector(3, 1, 0); // Ray direction vector for tests
        final Vector v110 = new Vector(1, 1, 0); // Another ray direction vector for tests
        final Point p01 = new Point(-1, 0, 0); // Ray origin point for tests

        // ============ Equivalence Partitions Tests ==============
        // TC01: Ray's line is outside the sphere (0 points)
        assertNull(sphere.findIntersections(new Ray(p01, v110)), "EP TC01: Ray's line out of sphere");

        // TC02: Ray starts before and crosses the sphere (2 points)
        List<Point> result1 = sphere.findIntersections(new Ray(p01, v310)).stream()
                .sorted(Comparator.comparingDouble(p -> p.distance(p01)))
                .toList();
        assertEquals(2, result1.size(), "EP TC02: Wrong number of points");
        assertEquals(exp, result1, "EP TC02: Ray crosses sphere");


        // TC03: Ray starts inside the sphere (1 point)
        result1 = sphere.findIntersections(new Ray(new Point(0.5, 0, 0), new Vector(1, 2, 0)));
        assertEquals(1, result1.size(), "EP TC03: Wrong number of points");
        assertEquals(List.of(new Point(1, 1, 0)), result1, "EP TC03: Ray from inside sphere");

        // TC04: Ray starts after the sphere (0 points)
        assertNull(sphere.findIntersections(new Ray(new Point(2, 1, 0), v310)),
                "EP TC04: Ray's line out of sphere");

        // =============== Boundary Values Tests ==================
        // **** Group: Ray's line crosses the sphere (but not the center)
        // TC11: Ray starts at sphere and goes inside (1 point)
        List<Point> result2 = sphere.findIntersections(new Ray(p100, new Vector(0, -2, 0)));
        assertEquals(1, result2.size(), "BVA TC11: Wrong number of points");
        assertEquals(List.of(new Point(1, -1, 0)), result2, "BVA TC11: Ray from sphere inside");

        // TC12: Ray starts at sphere and goes outside (1 point)
        assertEquals(List.of(new Point(1, 1, 0)), sphere.findIntersections(new Ray(p100, new Vector(0, 1, 0))),
                "BVA TC12: Ray's line out of sphere");

        // **** Group: Ray's line goes through the center
        // TC13: Ray starts before the sphere (2 points)
        final var result3 = sphere.findIntersections(new Ray(new Point(-1, 0, 0), new Vector(3, 0, 0))).stream()
                .sorted(Comparator.comparingDouble(p -> p.distance(new Point(-1, 0, 0))))
                .toList();
        assertEquals(2, result3.size(), "BVA TC13: Wrong number of points");
        assertEquals(List.of(Point.ZERO, new Point(2, 0, 0)), result3, "1BVA TC: Ray through center");

        // TC14: Ray starts at sphere and goes inside (1 point)
        result2 = sphere.findIntersections(new Ray(p100, new Vector(1, 0, 0)));
        assertEquals(1, result2.size(), "BVA TC14: Wrong number of points");
        assertEquals(List.of(new Point(2, 0, 0)), result2, "BVA TC14: Ray from and crosses sphere");

        // TC15: Ray starts inside (1 point)
        result2 = sphere.findIntersections(new Ray(new Point(1.5, 0, 0), new Vector(1, 0, 0)));
        assertEquals(1, result2.size(), "BVA TC15: Wrong number of points");
        assertEquals(List.of(new Point(2, 0, 0)), result2, "BVA TC15: Ray from inside sphere");

        // TC16: Ray starts at the center (1 point)
        result2 = sphere.findIntersections(new Ray(p100, new Vector(1, 0, 0)));
        assertEquals(1, result2.size(), "BVA TC16: Wrong number of points");
        assertEquals(List.of(new Point(2, 0, 0)), result2, "BVA TC16: Ray from center");

        // TC17: Ray starts at sphere and goes outside (1 point)
        result2 = sphere.findIntersections(new Ray(new Point(1.5, 0, 0), new Vector(1, 0, 0)));
        assertEquals(1, result2.size(), "BVA TC17: Wrong number of points");
        assertEquals(List.of(new Point(2, 0, 0)), result2, "BVA TC17: Ray from sphere outside");

        // TC18: Ray starts after sphere (0 points)
        assertNull(sphere.findIntersections(new Ray(new Point(3, 0, 0), new Vector(1, 0, 0))),
                "BVA TC18: Ray after sphere");

        // **** Group: Ray's line is tangent to the sphere (all tests 0 points)
        // TC19: Ray starts before the tangent point
        assertNull(sphere.findIntersections(new Ray(new Point(0, 1, 0), new Vector(1, 0, 0))),
                "BVA TC19: Ray tangent, before sphere");

        // TC20: Ray starts at the tangent point
        assertNull(sphere.findIntersections(new Ray(new Point(1, 1, 0), new Vector(1, 0, 0))),
                "BVA TC20: Ray tangent, at sphere");

        // TC21: Ray starts after the tangent point
        assertNull(sphere.findIntersections(new Ray(new Point(2, 1, 0), new Vector(1, 0, 0))),
                "BVA TC21: Ray tangent, after sphere");

        // **** Group: Special cases
        // TC22: Ray's line is outside, ray is orthogonal to ray start to sphere's center line
        assertNull(sphere.findIntersections(new Ray(new Point(1, 0, 2), new Vector(0, 0, 1))),
                "BVA TC22: Ray orthogonal to sphere's center line");

    }

    /**
     * Test method for {@link geometries.Sphere#(primitives.Ray)}.
     */
    @Test
    void findGeoIntersectionsHelper() {
        // Sphere centered at (0, 0, 0) with radius 1
        Sphere sphere = new Sphere(new Point(0, 0, 0), 1);
        List<Intersectable.GeoPoint> result;

        // ============ Equivalence Partitions Tests ==============
        // TC01: Ray starts at the center of the sphere and goes outwards
        result = sphere.findGeoIntersectionsHelper(new Ray(
                new Point(0, 0, 0),
                new Vector(1, 0, 0)), 2.0);
        assertEquals(1, result.size(), "TC01: Ray starts at the center and intersects the sphere");
        assertEquals(new Point(1, 0, 0), result.getFirst().point, "TC01: Intersection point");

        // TC02: Ray starts outside the sphere and intersects the sphere twice within maxDistance
        result = sphere.findGeoIntersectionsHelper(new Ray(
                new Point(-2, 0, 0),
                new Vector(1, 0, 0)), 5.0);
        assertEquals(2, result.size(), "TC02: Ray intersects the sphere twice");
        assertEquals(new Point(-1, 0, 0), result.get(0).point, "TC02: First intersection point");
        assertEquals(new Point(1, 0, 0), result.get(1).point, "TC02: Second intersection point");

        // TC03: Ray starts inside the sphere and intersects the sphere once within maxDistance
        result = sphere.findGeoIntersectionsHelper(new Ray(
                new Point(0.5, 0, 0),
                new Vector(1, 0, 0)), 2.0);
        assertEquals(1, result.size(), "TC03: Ray starts inside and intersects the sphere once");
        assertEquals(new Point(1, 0, 0), result.getFirst().point, "TC03: Intersection point");

        // TC04: Ray starts inside the sphere and does not intersect within maxDistance
        result = sphere.findGeoIntersectionsHelper(new Ray(
                new Point(0.5, 0, 0),
                new Vector(1, 0, 0)), 0.2);
        assertNull(result, "TC04: Ray starts inside the sphere and does not intersect within maxDistance");

        // =============== Boundary Values Tests ==================
        // TC11: Ray starts outside the sphere and intersects the sphere once within maxDistance
        result = sphere.findGeoIntersectionsHelper(new Ray(
                new Point(-2, 0, 0),
                new Vector(1, 0, 0)), 1.5);
        assertEquals(1, result.size(), "TC11: Ray intersects the sphere once");
        assertEquals(new Point(-1, 0, 0), result.getFirst().point, "TC11: Intersection point");

        // TC12: Ray starts outside the sphere and does not intersect the sphere within maxDistance
        result = sphere.findGeoIntersectionsHelper(new Ray(
                new Point(-2, 0, 0),
                new Vector(1, 0, 0)), 0.5);
        assertNull(result, "TC12: Ray does not intersect the sphere within maxDistance");

        // TC13: Ray starts outside the sphere and is tangent to the sphere within maxDistance
        result = sphere.findGeoIntersectionsHelper(new Ray(
                new Point(-2, 1, 0),
                new Vector(1, 0, 0)), 5.0);
        assertNull(result, "TC13: Ray is tangent to the sphere within maxDistance");
    }
}