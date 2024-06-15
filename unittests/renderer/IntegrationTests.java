package unittests.renderer;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import geometries.*;
import primitives.*;
import renderer.*;
import scene.Scene;

/**
 * Integration tests between ray creation from the camera and intersections with geometries.
 */
class IntegrationTests {

    /**
     * Helper method to count the number of intersections.
     *
     * @param camera the camera
     * @param nX     the number of columns
     * @param nY     the number of rows
     * @param geo    the geometry to intersect with
     * @return the number of intersection points
     */
    private int countIntersections(Camera camera, int nX, int nY, Intersectable geo) {
        int count = 0;
        for (int i = 0; i < nY; i++) {
            for (int j = 0; j < nX; j++) {
                List<Point> intersections = geo.findIntersections(camera.constructRay(nX, nY, j, i));
                if (intersections != null) {
                    count += intersections.size();
                }
            }
        }
        return count;
    }


    @Test
    void testSphereIntersections() {
        Camera camera = Camera.getBuilder()
                .setLocation(new Point(0, 0, -3))
                .setDirection(new Vector(0, 0, 1), new Vector(0, 1, 0))
                .setVpDistance(1)
                .setVpSize(3, 3)
                .build();

        Sphere sphere = new Sphere(new Point(0, 0, 2.5), 1);

        assertEquals(2, countIntersections(camera, 3, 3, sphere), "Wrong number of intersections with sphere");
    }


    @Test
    void testPlaneIntersections() {
        Camera camera = Camera.getBuilder()
                .setLocation(new Point(0, 0, -3))
                .setDirection(new Vector(0, 0, 1), new Vector(0, 1, 0))
                .setVpDistance(1)
                .setVpSize(3, 3)
                .build();

        Plane plane = new Plane(new Point(0, 0, 2), new Vector(0, 0, 1));

        assertEquals(9, countIntersections(camera, 3, 3, plane), "Wrong number of intersections with plane");
    }

    @Test
    void testTriangleIntersections() {
        Camera camera = Camera.getBuilder()
                .setLocation(new Point(0, 0, -3))
                .setDirection(new Vector(0, 0, 1), new Vector(0, 1, 0))
                .setVpDistance(1)
                .setVpSize(3, 3)
                .build();

        Triangle triangle = new Triangle(new Point(0, 1, 2), new Point(1, -1, 2), new Point(-1, -1, 2));

        assertEquals(1, countIntersections(camera, 3, 3, triangle), "Wrong number of intersections with triangle");
    }
}
