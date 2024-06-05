package geometries;

import primitives.*;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link geometries.Sphere} class
 *
 * @author Adir and Meir
 */
public class SphereTest {

    private final Point p001 = new Point(0, 0, 1);
    private final Point p100 = new Point(1, 0, 0);
    private final Vector v001 = new Vector(0, 0, 1);

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
        assertNull(sphere.findIntersections(new Ray(p01, v110)), "TC01: Ray's line out of sphere");

        // TC02: Ray starts before and crosses the sphere (2 points)
        final var result1 = sphere.findIntersections(new Ray(p01, v310)).stream()
                .sorted(Comparator.comparingDouble(p -> p.distance(p01)))
                .toList();
        assertEquals(2, result1.size(), "TC02: Wrong number of points");
        assertEquals(exp, result1, "TC02: Ray crosses sphere");


        // TC03: Ray starts inside the sphere (1 point)
        List<Point> result2 = sphere.findIntersections(new Ray(new Point(0.5, 0, 0), new Vector(1, 2, 0)));
        assertEquals(1, result2.size(), "Wrong number of points");
        assertEquals(List.of(new Point(1, 1, 0)), result2, "Ray from inside sphere");

        // TC04: Ray starts after the sphere (0 points)
        assertNull(sphere.findIntersections(new Ray(new Point(2, 1, 0), v310)),
                "Ray's line out of sphere");

        // =============== Boundary Values Tests ==================
        // **** Group: Ray's line crosses the sphere (but not the center)
        // TC11: Ray starts at sphere and goes inside (1 point)
        result2 = sphere.findIntersections(new Ray(p100, new Vector(0, -2, 0)));
        assertEquals(1, result2.size(), "Wrong number of points");
        assertEquals(List.of(new Point(1, -1, 0)), result2, "Ray from sphere inside");

        // TC12: Ray starts at sphere and goes outside (1 point)
        assertEquals(List.of(new Point(1,1,0)),sphere.findIntersections(new Ray(p100, new Vector(0, 1, 0))),
                "Ray's line out of sphere");

        // **** Group: Ray's line goes through the center
        // TC13: Ray starts before the sphere (2 points)
        final var result3 = sphere.findIntersections(new Ray(new Point(-1, 0, 0), new Vector(3, 0, 0))).stream()
                .sorted(Comparator.comparingDouble(p -> p.distance(new Point(-1, 0, 0))))
                .toList();
        assertEquals(2, result3.size(), "Wrong number of points");
        assertEquals(List.of(Point.ZERO, new Point(2, 0, 0)), result3, "Ray through center");

        // TC14: Ray starts at sphere and goes inside (1 point)
        result2 = sphere.findIntersections(new Ray(p100, new Vector(1, 0, 0)));
        assertEquals(1, result2.size(), "Wrong number of points");
        assertEquals(List.of(new Point(2, 0, 0)), result2, "Ray from and crosses sphere");

        // TC15: Ray starts inside (1 point)
        result2 = sphere.findIntersections(new Ray(new Point(1.5, 0, 0), new Vector(1, 0, 0)));
        assertEquals(1, result2.size(), "Wrong number of points");
        assertEquals(List.of(new Point(2, 0, 0)), result2, "Ray from inside sphere");

        // TC16: Ray starts at the center (1 point)
        result2 = sphere.findIntersections(new Ray(p100, new Vector(1, 0, 0)));
        assertEquals(1, result2.size(), "Wrong number of points");
        assertEquals(List.of(new Point(2, 0, 0)), result2, "Ray from center");

        // TC17: Ray starts at sphere and goes outside (0 points)
        assertNull(sphere.findIntersections(new Ray(p100, new Vector(1, 0, 0))),
                "Ray from sphere outside");

        // TC18: Ray starts after sphere (0 points)
        assertNull(sphere.findIntersections(new Ray(new Point(3, 0, 0), new Vector(1, 0, 0))),
                "Ray after sphere");

        // **** Group: Ray's line is tangent to the sphere (all tests 0 points)
        // TC19: Ray starts before the tangent point
        assertNull(sphere.findIntersections(new Ray(new Point(0, 1, 0), new Vector(1, 0, 0))),
                "Ray tangent, before sphere");

        // TC20: Ray starts at the tangent point
        assertNull(sphere.findIntersections(new Ray(new Point(1, 1, 0), new Vector(1, 0, 0))),
                "Ray tangent, at sphere");

        // TC21: Ray starts after the tangent point
        assertNull(sphere.findIntersections(new Ray(new Point(2, 1, 0), new Vector(1, 0, 0))),
                "Ray tangent, after sphere");

        // **** Group: Special cases
        // TC22: Ray's line is outside, ray is orthogonal to ray start to sphere's center line
        assertNull(sphere.findIntersections(new Ray(new Point(1, 0, 2), new Vector(0, 0, 1))),
                "Ray orthogonal to sphere's center line");
    }


}