package benchmark.scenes;

import benchmark.core.RenderCameraSpec;
import geometries.Geometries;
import geometries.Sphere;
import geometries.acceleration.AccelerationType;
import lighting.AmbientLight;
import lighting.PointLight;
import primitives.Color;
import primitives.Material;
import primitives.Point;
import primitives.Vector;
import scene.Scene;

import java.util.List;

/**
 * Clustered bounded scene with dense object groups separated by empty space.
 * <p>
 * This scene stresses regular-grid traversal by combining locally dense geometry with
 * large empty regions. It is useful for comparing uniform voxel stepping against BVH
 * hierarchy traversal when the spatial distribution is intentionally uneven.
 */
final class ClusteredBoundedScene implements BenchmarkScene {

    /**
     * Sphere radius used inside each cluster.
     */
    private static final double SPHERE_RADIUS = 2.2;

    /**
     * Distance between neighboring sphere centers inside a cluster.
     */
    private static final double CLUSTER_SPACING = 5.4;

    @Override
    public String name() {
        return "clustered-bounded";
    }

    @Override
    public RenderCameraSpec cameraSpec() {
        return new RenderCameraSpec(
                new Point(0, 0, 28),
                new Vector(0, 0, -1),
                new Vector(0, 1, 0),
                120,
                105,
                105);
    }

    @Override
    public Scene createScene(AccelerationType accelerationType) {
        Geometries geometries = new Geometries().setAcceleration(accelerationType);
        Material material = new Material().setKd(0.52).setKs(0.24).setShininess(90);

        addCluster(geometries, material, new Point(-30, -24, -82), new Color(86, 46, 58));
        addCluster(geometries, material, new Point(30, -18, -108), new Color(46, 78, 92));
        addCluster(geometries, material, new Point(-24, 24, -134), new Color(80, 70, 36));
        addCluster(geometries, material, new Point(32, 24, -160), new Color(42, 82, 58));

        return new Scene("Clustered bounded")
                .setBackground(new Color(4, 6, 10))
                .setAmbientLight(new AmbientLight(new Color(24, 24, 28), 0.16))
                .setGeometries(geometries)
                .setLights(List.of(
                        new PointLight(new Color(560, 420, 280), new Point(-55, 60, 20))
                                .setKl(0.00045)
                                .setKq(0.000025),
                        new PointLight(new Color(120, 170, 250), new Point(60, -45, -20))
                                .setKl(0.0007)
                                .setKq(0.00003)));
    }

    /**
     * Adds one dense cluster of similarly sized bounded objects.
     *
     * @param geometries geometry collection
     * @param material   shared sphere material
     * @param center     cluster center
     * @param baseColor  cluster base emission color
     */
    private static void addCluster(Geometries geometries, Material material, Point center, Color baseColor) {
        for (int z = -1; z <= 1; z++) {
            for (int y = -1; y <= 1; y++) {
                for (int x = -1; x <= 1; x++) {
                    geometries.add(new Sphere(new Point(
                            center.getX() + x * CLUSTER_SPACING,
                            center.getY() + y * CLUSTER_SPACING,
                            center.getZ() + z * CLUSTER_SPACING),
                            SPHERE_RADIUS)
                            .setEmission(baseColor.add(new Color(
                                    (x + 1) * 7,
                                    (y + 1) * 7,
                                    (z + 1) * 7)))
                            .setMaterial(material));
                }
            }
        }
    }
}
