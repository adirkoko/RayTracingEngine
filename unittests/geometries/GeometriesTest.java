package geometries;

import geometries.acceleration.AccelerationType;
import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link geometries.Geometries} class
 *
 * @author Adir and Meir
 */
class GeometriesTest {

    /**
     * Test method for {@link geometries.Geometries#findIntersections(primitives.Ray)}.
     */
    @Test
    void findIntersections() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Some of the geometries intersect the ray, and some don't. it intersects 2 geometries, plane and sphere.
        Geometries geometries = new Geometries(
                new Sphere(new Point(1, 0, 0), 1),
                new Plane(new Point(0, 0, 1), new Vector(0, 0, 1)),
                new Triangle(new Point(1, 1, 1), new Point(1, 2, 2), new Point(2, 3, 2)));

        List<Point> result = geometries.findIntersections(new Ray(new Point(0.5, 0.5, -2), new Vector(0, 0, 1)));
        assertEquals(3, result.size(), "Wrong number of points");

        // ================== Boundary Values Tests ==================
        // TC11: All the geometries intersect the ray. it intersects 3 geometries, plane, sphere and triangle.
        Geometries geometries2 = new Geometries(
                new Sphere(new Point(1, 0, 0), 1),
                new Plane(new Point(0, 0, 1), new Vector(0, 0, 1)),
                new Triangle(new Point(1, 3, 1), new Point(-3, -4, 2), new Point(4, 0, 2)));
        List<Point> result2 = geometries2.findIntersections(new Ray(new Point(0.5, 0.5, -4), new Vector(0, 0, 1)));
        assertEquals(4, result2.size(), "Wrong number of points");

        // TC12: None of the geometries intersect the ray
        assertNull(geometries2.findIntersections(new Ray(new Point(8.5, 7.5, 4), new Vector(0, 0, 1))), "Wrong number of points");

        // TC13: empty list of geometries
        Geometries emptyGeometries = new Geometries();
        assertNull(emptyGeometries.findIntersections(new Ray(new Point(-1, 0, 0), new Vector(3, 1, 0))), "Wrong number of points");

        // TC14: only one geometry in the list, with a couple of geometries intersects the ray. The ray intersects the sphere.
        List<Point> result3 = geometries.findIntersections(new Ray(new Point(0.5, 0.5, -0.5), new Vector(0, 1, 0)));
        assertEquals(1, result3.size(), "Wrong number of points");
    }

    /**
     * Test method for {@link geometries.Geometries#findGeoIntersectionsHelper(primitives.Ray, double)}.
     */
    @Test
    public void findGeoIntersectionsHelper() {
        Geometries geometries = new Geometries(
                new Sphere(new Point(0, 0, 2), 1),
                new Plane(new Point(0, 0, 5), new Vector(0, 0, 1)),
                new Triangle(new Point(0, 1, 4), new Point(1, 0, 4), new Point(-1, -1, 4))
        );
        List<Intersectable.GeoPoint> result;

        // ============ Equivalence Partitions Tests ==============

        // TC01: Ray intersects some geometries within max distance
        result = geometries.findGeoIntersectionsHelper(new Ray(
                new Point(0, 0, 0),
                new Vector(0, 0, 1)), 5.1);
        assertNotNull(result, "TC01 Ray intersects some geometries within max distance");
        assertEquals(4, result.size(), "TC01 Wrong number of intersection points");

        // TC02: Ray does not intersect any geometry due to max distance
        result = geometries.findGeoIntersectionsHelper(new Ray(
                new Point(0, 0, 0),
                new Vector(0, 0, 1)), 1.0);
        assertNull(result, "TC02 Ray does not intersect any geometry due to max distance");

        // =============== Boundary Values Tests ==================

        // TC11: Ray intersects geometries exactly at max distance
        result = geometries.findGeoIntersectionsHelper(new Ray(
                new Point(0, 0, 0),
                new Vector(0, 0, 1)), 4.0);
        assertNotNull(result, "TC11 Ray intersects geometries exactly at max distance");
        assertEquals(2, result.size(), "TC11 Wrong number of intersection points");
    }

    /**
     * Test method for BVH-backed {@link geometries.Geometries#findGeoIntersectionsHelper(Ray, double)}.
     */
    @Test
    void findGeoIntersectionsWithBvh() {
        Geometries geometries = new Geometries(
                new Sphere(new Point(0, 0, 3), 1),
                new Sphere(new Point(0, 0, 7), 1),
                new Sphere(new Point(0, 0, 11), 1),
                new Sphere(new Point(0, 0, 15), 1),
                new Sphere(new Point(4, 0, 3), 1));

        var result = geometries.findGeoIntersections(new Ray(Point.ZERO, new Vector(0, 0, 1)));

        assertNotNull(result, "Ray should intersect the bounded geometries through the BVH");
        assertEquals(8, result.size(), "Wrong number of BVH-backed intersections");
    }

    /**
     * Test method for Grid-backed {@link geometries.Geometries#findGeoIntersectionsHelper(Ray, double)}.
     */
    @Test
    void findGeoIntersectionsWithGrid() {
        Geometries geometries = createBoundedCollection()
                .setAcceleration(AccelerationType.GRID);

        var result = geometries.findGeoIntersections(new Ray(Point.ZERO, new Vector(0, 0, 1)));

        assertNotNull(result, "Ray should intersect the bounded geometries through the regular grid");
        assertEquals(8, result.size(), "Wrong number of Grid-backed intersections");
    }

    /**
     * Test Grid traversal with unbounded fallback geometries.
     */
    @Test
    void findGeoIntersectionsWithGridAndUnboundedFallback() {
        Geometries geometries = new Geometries(
                new Sphere(new Point(0, 0, 5), 1),
                new Plane(new Point(0, 0, 7), new Vector(0, 0, 1)))
                .setAcceleration(AccelerationType.GRID);

        var result = geometries.findGeoIntersections(new Ray(Point.ZERO, new Vector(0, 0, 1)));

        assertNotNull(result, "Grid should combine bounded traversal with unbounded fallback");
        assertEquals(3, result.size(), "Wrong number of Grid intersections with unbounded fallback");
        assertEquals(new Point(0, 0, 4),
                geometries.findClosestGeoIntersection(new Ray(Point.ZERO, new Vector(0, 0, 1))).point,
                "Grid should preserve closest-hit behavior");
    }

    /**
     * Test method for {@link geometries.Geometries#findClosestGeoIntersection(Ray)}.
     */
    @Test
    void findClosestGeoIntersection() {
        Geometries geometries = new Geometries(
                new Sphere(new Point(0, 0, 9), 1),
                new Sphere(new Point(0, 0, 5), 1),
                new Sphere(new Point(0, 0, 13), 1),
                new Sphere(new Point(0, 0, 17), 1));

        Ray ray = new Ray(Point.ZERO, new Vector(0, 0, 1));

        assertEquals(new Point(0, 0, 4), geometries.findClosestGeoIntersection(ray).point,
                "Wrong closest intersection before rebuilding the index");

        geometries.add(new Sphere(new Point(0, 0, 3), 0.5));

        assertEquals(new Point(0, 0, 2.5), geometries.findClosestGeoIntersection(ray).point,
                "Wrong closest intersection after lazy index rebuild");
    }

    /**
     * Test acceleration mode selection for benchmarking and debugging.
     */
    @Test
    void setAcceleration() {
        Ray ray = new Ray(Point.ZERO, new Vector(0, 0, 1));
        Geometries geometries = createBoundedCollection();

        geometries.setAcceleration(AccelerationType.LINEAR);
        List<Point> linearIntersections = geometries.findIntersections(ray);
        Intersectable.GeoPoint linearClosest = geometries.findClosestGeoIntersection(ray);

        geometries.setAcceleration(AccelerationType.BVH);
        List<Point> bvhIntersections = geometries.findIntersections(ray);
        Intersectable.GeoPoint bvhClosest = geometries.findClosestGeoIntersection(ray);

        geometries.setAcceleration(AccelerationType.GRID);
        List<Point> gridIntersections = geometries.findIntersections(ray);
        Intersectable.GeoPoint gridClosest = geometries.findClosestGeoIntersection(ray);

        assertEquals(linearIntersections, bvhIntersections,
                "BVH and linear traversal should return the same intersections");

        assertEquals(linearClosest, bvhClosest,
                "BVH and linear traversal should return the same closest intersection");

        assertEquals(linearIntersections.size(), gridIntersections.size(),
                "Grid and linear traversal should return the same number of intersections");

        assertTrue(gridIntersections.containsAll(linearIntersections),
                "Grid and linear traversal should return the same intersections");

        assertEquals(linearClosest, gridClosest,
                "Grid and linear traversal should return the same closest intersection");

        assertThrows(IllegalArgumentException.class, () -> geometries.setAcceleration(null),
                "Null acceleration type should be rejected");
    }

    /**
     * Test resolved acceleration mode reporting.
     */
    @Test
    void getResolvedAccelerationType() {
        Geometries empty = new Geometries();
        assertEquals(AccelerationType.AUTO, empty.getAccelerationType(),
                "Default requested acceleration mode should be AUTO");
        assertEquals(AccelerationType.LINEAR, empty.getResolvedAccelerationType(),
                "Empty AUTO collection should resolve to LINEAR");

        Geometries geometries = createUniformBoundedCollection();
        assertEquals(AccelerationType.GRID, geometries.getResolvedAccelerationType(),
                "Uniform bounded AUTO collection should resolve to GRID");

        geometries.setAcceleration(AccelerationType.BVH);
        assertEquals(AccelerationType.BVH, geometries.getResolvedAccelerationType(),
                "Explicit BVH should resolve to BVH");

        geometries.setAcceleration(AccelerationType.LINEAR);
        assertEquals(AccelerationType.LINEAR, geometries.getResolvedAccelerationType(),
                "Explicit LINEAR should resolve to LINEAR");

        geometries.setAcceleration(AccelerationType.AUTO);
        assertEquals(AccelerationType.GRID, geometries.getResolvedAccelerationType(),
                "AUTO should resolve before any render or intersection query");

        Geometries changing = new Geometries();
        assertEquals(AccelerationType.LINEAR, changing.getResolvedAccelerationType(),
                "Empty AUTO collection should resolve before any render or intersection query");

        changing.add(
                new Sphere(new Point(0, 0, 3), 1),
                new Sphere(new Point(0, 0, 7), 1),
                new Sphere(new Point(0, 0, 11), 1),
                new Sphere(new Point(0, 0, 15), 1));
        assertEquals(AccelerationType.BVH, changing.getResolvedAccelerationType(),
                "Resolved acceleration should be recomputed after geometry changes");
    }

    /**
     * Test all acceleration modes against LINEAR on a small mixed scene.
     */
    @Test
    void accelerationModesMatchLinearOnSmallScene() {
        List<Ray> rays = List.of(
                new Ray(new Point(0, 0, -5), new Vector(0, 0, 1)),
                new Ray(new Point(2, 0, -5), new Vector(0, 0, 1)),
                new Ray(new Point(-2, 0, -5), new Vector(1, 0, 4)),
                new Ray(new Point(5, 5, -5), new Vector(0, 0, 1))
        );
        Geometries geometries = createMixedSmallCollection();

        for (Ray ray : rays) {
            geometries.setAcceleration(AccelerationType.LINEAR);
            List<Point> expectedIntersections = geometries.findIntersections(ray);
            Intersectable.GeoPoint expectedClosest = geometries.findClosestGeoIntersection(ray);

            for (AccelerationType type : AccelerationType.values()) {
                geometries.setAcceleration(type);
                assertSamePoints(expectedIntersections, geometries.findIntersections(ray),
                        type + " should match LINEAR intersections");

                Intersectable.GeoPoint actualClosest = geometries.findClosestGeoIntersection(ray);
                assertEquals(expectedClosest == null ? null : expectedClosest.point,
                        actualClosest == null ? null : actualClosest.point,
                        type + " should match LINEAR closest intersection");
            }
        }
    }

    /**
     * Creates a bounded geometry collection above the BVH threshold.
     */
    private Geometries createBoundedCollection() {
        return new Geometries(
                new Sphere(new Point(0, 0, 3), 1),
                new Sphere(new Point(0, 0, 7), 1),
                new Sphere(new Point(0, 0, 11), 1),
                new Sphere(new Point(0, 0, 15), 1),
                new Sphere(new Point(4, 0, 3), 1));
    }

    /**
     * Creates a uniform bounded geometry collection that should be grid-friendly.
     */
    private Geometries createUniformBoundedCollection() {
        Geometries geometries = new Geometries();

        for (int z = 0; z < 4; z++) {
            for (int y = -2; y <= 2; y++) {
                for (int x = -2; x <= 2; x++)
                    geometries.add(new Sphere(new Point(x * 16, y * 16, -70 - z * 26), 3.6));
            }
        }

        return geometries;
    }

    /**
     * Creates a small mixed bounded/unbounded geometry collection.
     */
    private Geometries createMixedSmallCollection() {
        return new Geometries(
                new Sphere(new Point(0, 0, 0), 1),
                new Sphere(new Point(2, 0, 2), 0.75),
                new Triangle(new Point(-1, -1, 3), new Point(1, -1, 3), new Point(0, 1, 3)),
                new Plane(new Point(0, 0, 4), new Vector(0, 0, 1)));
    }

    /**
     * Asserts that two point lists contain the same points, ignoring traversal order.
     *
     * @param expected expected point list, or null
     * @param actual   actual point list, or null
     * @param message  assertion message
     */
    private void assertSamePoints(List<Point> expected, List<Point> actual, String message) {
        if (expected == null || actual == null) {
            assertEquals(expected, actual, message);
            return;
        }

        assertEquals(expected.size(), actual.size(), message);
        assertTrue(actual.containsAll(expected), message);
    }
}
