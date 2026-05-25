package geometries;

import geometries.acceleration.AccelerationType;
import geometries.acceleration.GeometryIndexes;
import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Vector;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for AUTO acceleration selection heuristics.
 */
class GeometryIndexesTest {

    /**
     * AUTO should handle empty geometry collections without trying to build spatial stats.
     */
    @Test
    void autoUsesLinearForEmptyScene() {
        assertEquals("LinearGeometryIndex", autoIndexName(List.of()));
    }

    /**
     * AUTO should keep very small unbounded-heavy scenes linear.
     */
    @Test
    void autoUsesLinearForMostlyUnboundedScene() {
        assertEquals("LinearGeometryIndex", autoIndexName(List.of(
                new Sphere(new Point(0, 0, 5), 1),
                new Plane(new Point(0, 0, 7), new Vector(0, 0, 1)),
                new Plane(new Point(0, -2, 0), new Vector(0, 1, 0)))));
    }

    /**
     * AUTO should use GRID for evenly distributed bounded objects.
     */
    @Test
    void autoUsesGridForUniformBoundedDistribution() {
        List<Intersectable> geometries = new ArrayList<>();

        for (int z = 0; z < 4; z++) {
            for (int y = -2; y <= 2; y++) {
                for (int x = -2; x <= 2; x++)
                    geometries.add(new Sphere(new Point(x * 16, y * 16, -70 - z * 26), 3.6));
            }
        }

        assertEquals("RegularGrid", autoIndexName(geometries));
    }

    /**
     * AUTO should use BVH for clustered bounded objects.
     */
    @Test
    void autoUsesBvhForClusteredBoundedDistribution() {
        List<Intersectable> geometries = new ArrayList<>();

        addCluster(geometries, new Point(-30, -24, -82));
        addCluster(geometries, new Point(30, -18, -108));
        addCluster(geometries, new Point(-24, 24, -134));
        addCluster(geometries, new Point(32, 24, -160));

        assertEquals("BvhGeometryIndex", autoIndexName(geometries));
    }

    /**
     * AUTO should avoid GRID for modest scenes dominated by large receiver surfaces.
     */
    @Test
    void autoUsesLinearForLargeReceiverSurfaces() {
        List<Intersectable> geometries = new ArrayList<>(List.of(
                new Triangle(new Point(-120, -28, -35), new Point(120, -28, -35), new Point(120, -28, -260)),
                new Triangle(new Point(-120, -28, -35), new Point(120, -28, -260), new Point(-120, -28, -260)),
                new Triangle(new Point(-90, -28, -245), new Point(90, -28, -245), new Point(90, 70, -245)),
                new Triangle(new Point(-90, -28, -245), new Point(90, 70, -245), new Point(-90, 70, -245))));

        for (int row = 0; row < 4; row++) {
            for (int column = -4; column <= 4; column++)
                geometries.add(new Sphere(new Point(column * 14, -22 + row * 3, -185 - row * 18), 4.2));
        }

        assertEquals("LinearGeometryIndex", autoIndexName(geometries));
    }

    /**
     * AUTO should not treat one large bounded object as a receiver-heavy scene by itself.
     */
    @Test
    void autoDoesNotUseLinearForOneLargeObjectInDistributedScene() {
        List<Intersectable> geometries = new ArrayList<>();

        for (int z = 0; z < 4; z++) {
            for (int y = -3; y <= 3; y++) {
                for (int x = -3; x <= 3; x++)
                    geometries.add(new Sphere(new Point(x * 18, y * 18, -80 - z * 28), 2.8));
            }
        }

        geometries.add(new Sphere(new Point(0, 0, -122), 34));

        assertEquals("RegularGrid", autoIndexName(geometries));
    }

    /**
     * AUTO should allow GRID for mixed-scale bounded objects when large objects are volumetric.
     */
    @Test
    void autoUsesGridForMixedScaleBoundedObjects() {
        List<Intersectable> geometries = new ArrayList<>(List.of(
                new Sphere(new Point(-30, -8, -102), 20),
                new Sphere(new Point(28, 10, -118), 12),
                new Sphere(new Point(12, -24, -152), 8),
                new Sphere(new Point(-18, 24, -168), 6)));

        addSmallGrid(geometries, new Point(34, -24, -78), 2.1, 7.0);
        addSmallGrid(geometries, new Point(-42, 28, -134), 1.6, 5.8);
        addSmallGrid(geometries, new Point(40, 24, -184), 1.3, 5.2);

        assertEquals("RegularGrid", autoIndexName(geometries));
    }

    /**
     * AUTO should keep dense mesh-like bounded scenes on BVH.
     */
    @Test
    void autoUsesBvhForDenseMeshLikeGeometry() {
        List<Intersectable> geometries = new ArrayList<>();

        for (int y = 0; y < 12; y++) {
            for (int x = 0; x < 12; x++) {
                double minX = x * 2;
                double minY = y * 2;
                geometries.add(new Triangle(
                        new Point(minX, minY, -100),
                        new Point(minX + 1.4, minY + 0.2, -100.6),
                        new Point(minX + 0.1, minY + 1.4, -99.4)));
            }
        }

        assertEquals("BvhGeometryIndex", autoIndexName(geometries));
    }

    /**
     * Adds one compact sphere cluster.
     *
     * @param geometries geometry list
     * @param center     cluster center
     */
    private static void addCluster(List<Intersectable> geometries, Point center) {
        for (int z = -1; z <= 1; z++) {
            for (int y = -1; y <= 1; y++) {
                for (int x = -1; x <= 1; x++) {
                    geometries.add(new Sphere(new Point(
                            center.getX() + x * 5.4,
                            center.getY() + y * 5.4,
                            center.getZ() + z * 5.4),
                            2.2));
                }
            }
        }
    }

    /**
     * Adds a compact grid of small spheres.
     *
     * @param geometries geometry list
     * @param center     grid center
     * @param radius     sphere radius
     * @param spacing    center spacing
     */
    private static void addSmallGrid(List<Intersectable> geometries, Point center, double radius, double spacing) {
        for (int y = -1; y <= 1; y++) {
            for (int x = -1; x <= 1; x++) {
                geometries.add(new Sphere(new Point(
                        center.getX() + x * spacing,
                        center.getY() + y * spacing,
                        center.getZ()),
                        radius));
            }
        }
    }

    /**
     * Builds the AUTO index and returns its implementation class name.
     *
     * @param geometries geometries to index
     * @return simple implementation class name
     */
    private static String autoIndexName(List<Intersectable> geometries) {
        return GeometryIndexes.build(
                        geometries,
                        AccelerationType.AUTO,
                        4,
                        geometry -> geometry.getBoundingBox())
                .getClass()
                .getSimpleName();
    }
}
