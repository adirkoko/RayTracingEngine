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
 * Image-quality scene for reflection, glossy reflection, transparency, and diffused glass.
 */
final class GlobalMaterialsScene implements BenchmarkScene {

    /**
     * Floor Y coordinate.
     */
    private static final double FLOOR_Y = -24;

    @Override
    public String name() {
        return "global-materials";
    }

    @Override
    public RenderCameraSpec cameraSpec() {
        return new RenderCameraSpec(
                new Point(0, 8, 30),
                new Vector(0, -0.13, -1),
                new Vector(0, 1, -0.13),
                120,
                100,
                100);
    }

    @Override
    public Scene createScene(AccelerationType accelerationType) {
        Geometries geometries = new Geometries().setAcceleration(accelerationType);
        Material floorMaterial = new Material()
                .setKd(0.48)
                .setKs(0.18)
                .setShininess(60)
                .setKr(0.08);
        Material wallMaterial = new Material().setKd(0.5).setKs(0.08).setShininess(25);
        Material markerMaterial = new Material().setKd(0.58).setKs(0.14).setShininess(45);
        Material sharpMirror = new Material()
                .setKd(0.16)
                .setKs(0.62)
                .setShininess(220)
                .setKr(0.58);
        Material glossyMirror = new Material()
                .setKd(0.18)
                .setKs(0.56)
                .setShininess(180)
                .setKr(0.54)
                .setReflectionBlur(0.09)
                .setGlobalSamples(8);
        Material clearGlass = new Material()
                .setKd(0.12)
                .setKs(0.48)
                .setShininess(160)
                .setKt(0.52);
        Material diffusedGlass = new Material()
                .setKd(0.14)
                .setKs(0.42)
                .setShininess(150)
                .setKt(0.48)
                .setTransparencyBlur(0.08)
                .setGlobalSamples(8);

        addSurfaces(geometries, floorMaterial, wallMaterial);
        addReferenceMarkers(geometries, markerMaterial);
        geometries.add(
                groundedSphere(-38, -112, 15, new Color(72, 74, 80), sharpMirror),
                groundedSphere(-10, -124, 15, new Color(64, 66, 74), glossyMirror),
                groundedSphere(21, -118, 14, new Color(32, 58, 88), clearGlass),
                groundedSphere(44, -94, 10, new Color(44, 72, 82), diffusedGlass));

        return new Scene("Global materials")
                .setBackground(new Color(5, 7, 10))
                .setAmbientLight(new AmbientLight(new Color(28, 28, 32), 0.17))
                .setGeometries(geometries)
                .setLights(List.of(
                        new PointLight(new Color(560, 430, 310), new Point(-48, 60, 14))
                                .setKl(0.00042)
                                .setKq(0.000022),
                        new PointLight(new Color(140, 180, 260), new Point(56, 26, -58))
                                .setKl(0.00072)
                                .setKq(0.000035)));
    }

    /**
     * Adds neutral floor and wall surfaces.
     *
     * @param geometries    geometry collection
     * @param floorMaterial floor material
     * @param wallMaterial  wall material
     */
    private static void addSurfaces(Geometries geometries, Material floorMaterial, Material wallMaterial) {
        geometries.add(
                new Triangle(
                        new Point(-110, FLOOR_Y, -45),
                        new Point(110, FLOOR_Y, -45),
                        new Point(110, FLOOR_Y, -235))
                        .setEmission(new Color(34, 36, 38))
                        .setMaterial(floorMaterial),
                new Triangle(
                        new Point(-110, FLOOR_Y, -45),
                        new Point(110, FLOOR_Y, -235),
                        new Point(-110, FLOOR_Y, -235))
                        .setEmission(new Color(34, 36, 38))
                        .setMaterial(floorMaterial),
                new Triangle(
                        new Point(-95, FLOOR_Y, -220),
                        new Point(95, FLOOR_Y, -220),
                        new Point(95, 72, -220))
                        .setEmission(new Color(22, 29, 44))
                        .setMaterial(wallMaterial),
                new Triangle(
                        new Point(-95, FLOOR_Y, -220),
                        new Point(95, 72, -220),
                        new Point(-95, 72, -220))
                        .setEmission(new Color(22, 29, 44))
                        .setMaterial(wallMaterial));
    }

    /**
     * Adds colored geometry that reflections and transparent rays can reveal.
     *
     * @param geometries geometry collection
     * @param material   marker material
     */
    private static void addReferenceMarkers(Geometries geometries, Material material) {
        for (int i = 0; i < 7; i++) {
            double x = -60 + i * 20;
            geometries.add(new Sphere(new Point(x, -8 + (i % 2) * 8, -170), 5)
                    .setEmission(new Color(36 + i * 12, 82 - i * 5, 42 + i * 8))
                    .setMaterial(material));
        }

        geometries.add(
                coloredPanel(-70, -154, new Color(92, 34, 34), material),
                coloredPanel(-22, -160, new Color(36, 84, 58), material),
                coloredPanel(28, -166, new Color(38, 58, 96), material),
                coloredPanel(70, -152, new Color(92, 74, 30), material));
    }

    /**
     * Creates a triangular floor marker.
     *
     * @param x        marker center X coordinate
     * @param z        marker center Z coordinate
     * @param emission emission color
     * @param material material
     * @return marker triangle
     */
    private static Triangle coloredPanel(double x, double z, Color emission, Material material) {
        Triangle triangle = new Triangle(
                new Point(x - 7, FLOOR_Y + 0.2, z - 8),
                new Point(x + 9, FLOOR_Y + 0.2, z - 3),
                new Point(x - 3, FLOOR_Y + 0.2, z + 10));
        triangle.setEmission(emission).setMaterial(material);
        return triangle;
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
