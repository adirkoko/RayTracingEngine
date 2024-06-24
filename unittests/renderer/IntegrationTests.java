package renderer;

import geometries.*;
import primitives.*;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import scene.Scene;

/**
 * Integration tests between camera and geometries.
 * Tests different geometries and their intersections with rays from the camera.
 *
 * @author Adir and Meir
 */
class IntegrationTests {
    /**
     * Camera builder for the tests
     */
    private final Camera.Builder cameraBuilder = Camera.getBuilder()
            .setLocation(Point.ZERO)
            .setDirection(new Vector(0, 0, -1), new Vector(0, 1, 0)) // vTo, vUp
            .setVpDistance(1)
            .setImageWriter(new ImageWriter("test", 800, 800))
            .setRayTracer(new SimpleRayTracer(new Scene("Test")));

    /**
     * Helper method to check intersections between the camera and geometries.
     * Counts intersections in the view plane.
     *
     * @param camera                the camera.
     * @param geometry              the geometry to intersect with.
     * @param expectedIntersections the expected number of intersections.
     * @param assertMessage         the message to display if the assertion fails.
     * @throws CloneNotSupportedException if cloning the camera fails.
     */
    private void checkGeoIntersections(Camera camera, Geometry geometry, int expectedIntersections, String assertMessage) throws CloneNotSupportedException {
        int count = 0;
        for (int i = 0; i < 3; i++) { // Iterate through the 3x3 view plane
            for (int j = 0; j < 3; j++) {
                Ray ray = camera.constructRay(3, 3, j, i);
                if (geometry.findIntersections(ray) != null) {
                    count += geometry.findIntersections(ray).size(); // Add the intersection if exists
                }
            }
        }
        assertEquals(expectedIntersections, count, assertMessage);
    }

    /**
     * Test camera-sphere intersections in different cases.
     *
     * @throws CloneNotSupportedException if cloning the camera fails
     */
    @Test
    void testCameraSphere() throws CloneNotSupportedException {
        Camera camera = cameraBuilder.setVpSize(3, 3).setLocation(new Point(0, 0, 0.5)).build();

        // TC01: Unit sphere in the center of view plane
        Sphere sphere1 = new Sphere(new Point(0, 0, -3), 1d);
        checkGeoIntersections(camera, sphere1, 2, "ERROR Sphere: Expected 2 points");

        // TC02: Sphere larger than view plane - 18 intersection points
        Sphere sphere2 = new Sphere(new Point(0, 0, -2.5), 2.5d);
        checkGeoIntersections(camera, sphere2, 18, "ERROR Sphere: Expected 18 points");

        // TC03: Sphere closer to intersecting view plane - 10 points
        Sphere sphere3 = new Sphere(new Point(0, 0, -2), 2d);
        checkGeoIntersections(camera, sphere3, 10, "ERROR Sphere: Expected 10 points");

        // TC04: Sphere with view plane in middle of it - 9 points
        Sphere sphere4 = new Sphere(new Point(0, 0, -2), 4d);
        checkGeoIntersections(camera, sphere4, 9, "ERROR Sphere: Expected 9 points");

        // TC05: Sphere behind view plane - 0 points
        Sphere sphere5 = new Sphere(new Point(0, 0, 1), 0.5d);
        checkGeoIntersections(camera, sphere5, 0, "ERROR Sphere: Expected 0 points");
    }

    /**
     * Test different cases of camera-plane intersections.
     *
     * @throws CloneNotSupportedException if cloning the camera fails
     */
    @Test
    void testCameraPlane() throws CloneNotSupportedException {
        Camera camera = cameraBuilder.setVpSize(3, 3).build();

        // TC01: Plane parallel to view plane - 9 points
        Plane plane1 = new Plane(new Point(0, 0, -3), new Vector(0, 0, 1));
        checkGeoIntersections(camera, plane1, 9, "ERROR Plane: Expected 9 points");

        // TC02: Plane slightly slanted against view plane - 9 points
        Plane plane2 = new Plane(new Point(0, 0, -3), new Vector(0, 1, 2));
        checkGeoIntersections(camera, plane2, 9, "ERROR Plane: Expected 9 points");

        // TC03: Plane very slanted against view plane - 6 points
        Plane plane3 = new Plane(new Point(0, 0, -3), new Vector(0, 1, 1));
        checkGeoIntersections(camera, plane3, 6, "ERROR Plane: Expected 6 points");

        // TC04: Plane behind view plane - 0 points
        Plane plane4 = new Plane(new Point(1, 1, 2), new Point(-1, 1, 2), new Point(0, -10, 2));
        checkGeoIntersections(camera, plane4, 0, "ERROR Plane: Expected 0 points");
    }

    /**
     * Test different cases of camera-triangle intersections.
     *
     * @throws CloneNotSupportedException if cloning the camera fails
     */
    @Test
    void testCameraTriangle() throws CloneNotSupportedException {
        Camera camera = cameraBuilder.setVpSize(3, 3).build();

        // TC01: Small triangle in front of view plane - 1 point
        Triangle triangle1 = new Triangle(new Point(1, 1, -2), new Point(-1, 1, -2), new Point(0, -1, -2));
        checkGeoIntersections(camera, triangle1, 1, "ERROR Triangle: Expected 1 point");

        // TC02: Tall triangle - 2 points
        Triangle triangle2 = new Triangle(new Point(1, 1, -2), new Point(-1, 1, -2), new Point(0, -20, -2));
        checkGeoIntersections(camera, triangle2, 2, "ERROR Triangle: Expected 2 points");

        // TC03: Triangle behind view plane - 0 points
        Triangle triangle3 = new Triangle(new Point(1, 1, 2), new Point(-1, 1, 2), new Point(0, -20, 2));
        checkGeoIntersections(camera, triangle3, 0, "ERROR Triangle: Expected 0 points");
    }
}