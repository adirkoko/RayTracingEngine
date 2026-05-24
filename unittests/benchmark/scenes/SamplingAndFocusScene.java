package benchmark.scenes;

import benchmark.core.RenderCameraSpec;
import geometries.Geometries;
import geometries.Sphere;
import geometries.Triangle;
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
 * Image-quality scene for anti-aliasing, adaptive sampling, and depth of field.
 */
final class SamplingAndFocusScene implements BenchmarkScene {

    /**
     * Floor Y coordinate.
     */
    private static final double FLOOR_Y = -24;

    @Override
    public String name() {
        return "sampling-focus";
    }

    @Override
    public RenderCameraSpec cameraSpec() {
        return new RenderCameraSpec(
                new Point(0, 7, 30),
                new Vector(0, -0.12, -1),
                new Vector(0, 1, -0.12),
                120,
                96,
                96);
    }

    @Override
    public Scene createScene(AccelerationType accelerationType) {
        Geometries geometries = new Geometries().setAcceleration(accelerationType);
        Material floorMaterial = new Material().setKd(0.55).setKs(0.1).setShininess(35);
        Material wallMaterial = new Material().setKd(0.45).setKs(0.08).setShininess(20);
        Material matteMaterial = new Material().setKd(0.56).setKs(0.2).setShininess(70);
        Material edgeMaterial = new Material().setKd(0.5).setKs(0.05).setShininess(20);

        addSurfaces(geometries, floorMaterial, wallMaterial);
        addDepthLayers(geometries, matteMaterial);
        addDiagonalEdgeTargets(geometries, edgeMaterial);
        addFineDetailTargets(geometries, matteMaterial);

        return new Scene("Sampling and focus")
                .setBackground(new Color(6, 8, 11))
                .setAmbientLight(new AmbientLight(new Color(28, 28, 30), 0.18))
                .setGeometries(geometries)
                .setLights(List.of(
                        new PointLight(new Color(520, 410, 300), new Point(-42, 56, 12))
                                .setKl(0.00042)
                                .setKq(0.000022),
                        new PointLight(new Color(120, 150, 220), new Point(52, 20, -70))
                                .setKl(0.00075)
                                .setKq(0.000035)));
    }

    /**
     * Adds floor and back wall surfaces with large smooth regions for adaptive sampling.
     *
     * @param geometries    geometry collection
     * @param floorMaterial floor material
     * @param wallMaterial  wall material
     */
    private static void addSurfaces(Geometries geometries, Material floorMaterial, Material wallMaterial) {
        geometries.add(
                new Triangle(
                        new Point(-105, FLOOR_Y, -45),
                        new Point(105, FLOOR_Y, -45),
                        new Point(105, FLOOR_Y, -230))
                        .setEmission(new Color(35, 37, 39))
                        .setMaterial(floorMaterial),
                new Triangle(
                        new Point(-105, FLOOR_Y, -45),
                        new Point(105, FLOOR_Y, -230),
                        new Point(-105, FLOOR_Y, -230))
                        .setEmission(new Color(35, 37, 39))
                        .setMaterial(floorMaterial),
                new Triangle(
                        new Point(-92, FLOOR_Y, -218),
                        new Point(92, FLOOR_Y, -218),
                        new Point(92, 70, -218))
                        .setEmission(new Color(24, 29, 40))
                        .setMaterial(wallMaterial),
                new Triangle(
                        new Point(-92, FLOOR_Y, -218),
                        new Point(92, 70, -218),
                        new Point(-92, 70, -218))
                        .setEmission(new Color(24, 29, 40))
                        .setMaterial(wallMaterial));
    }

    /**
     * Adds foreground, focal-plane, and background objects.
     *
     * @param geometries geometry collection
     * @param material   shared material
     */
    private static void addDepthLayers(Geometries geometries, Material material) {
        geometries.add(
                groundedSphere(-34, -78, 8, new Color(88, 38, 34), material),
                groundedSphere(0, -125, 11, new Color(76, 66, 32), material),
                groundedSphere(32, -178, 9, new Color(34, 68, 88), material),
                new Sphere(new Point(-6, 18, -126), 5)
                        .setEmission(new Color(92, 92, 96))
                        .setMaterial(material));
    }

    /**
     * Adds high-contrast diagonal triangles for edge aliasing comparison.
     *
     * @param geometries geometry collection
     * @param material   marker material
     */
    private static void addDiagonalEdgeTargets(Geometries geometries, Material material) {
        for (int i = 0; i < 7; i++) {
            double x = -66 + i * 20;
            Color color = i % 2 == 0 ? new Color(110, 112, 116) : new Color(12, 14, 18);
            geometries.add(new Triangle(
                    new Point(x, -12, -214),
                    new Point(x + 7, -12, -214),
                    new Point(x + 23, 36, -214))
                    .setEmission(color)
                    .setMaterial(material));
        }
    }

    /**
     * Adds small detail markers near the focal plane.
     *
     * @param geometries geometry collection
     * @param material   marker material
     */
    private static void addFineDetailTargets(Geometries geometries, Material material) {
        for (int i = -5; i <= 5; i++) {
            geometries.add(new Sphere(new Point(i * 5.5, FLOOR_Y + 1.8, -142 - Math.abs(i) * 2), 1.8)
                    .setEmission(new Color(32 + (i + 5) * 7, 42, 70 + Math.abs(i) * 5))
                    .setMaterial(material));
        }
    }

    /**
     * Creates a sphere resting on the floor.
     *
     * @param x        center X coordinate
     * @param z        center Z coordinate
     * @param radius   sphere radius
     * @param emission emission color
     * @param material material
     * @return grounded sphere
     */
    private static Sphere groundedSphere(double x, double z, double radius, Color emission, Material material) {
        Sphere sphere = new Sphere(new Point(x, FLOOR_Y + radius, z), radius);
        sphere.setEmission(emission).setMaterial(material);
        return sphere;
    }
}
