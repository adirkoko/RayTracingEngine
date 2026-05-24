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
 * Bounded recursion scene with reflective and transparent materials.
 * <p>
 * This scene keeps global sampling controlled while multiplying intersection work through
 * reflection, transparency, glossy reflection, and diffused glass rays. It is useful for
 * checking whether acceleration still helps once primary rays spawn secondary global rays.
 */
final class ReflectionTransparencyScene implements BenchmarkScene {

    @Override
    public String name() {
        return "reflection-transparency";
    }

    @Override
    public RenderCameraSpec cameraSpec() {
        return new RenderCameraSpec(
                new Point(0, 8, 26),
                new Vector(0, -0.11, -1),
                new Vector(0, 1, -0.11),
                120,
                95,
                95);
    }

    @Override
    public Scene createScene(AccelerationType accelerationType) {
        Geometries geometries = new Geometries().setAcceleration(accelerationType);

        Material floorMaterial = new Material()
                .setKd(0.45)
                .setKs(0.16)
                .setShininess(50)
                .setKr(0.18);
        Material wallMaterial = new Material().setKd(0.5).setKs(0.08).setShininess(25);
        Material mirrorMaterial = new Material()
                .setKd(0.18)
                .setKs(0.55)
                .setShininess(180)
                .setKr(0.52)
                .setReflectionBlur(0.05)
                .setGlobalSamples(4);
        Material glassMaterial = new Material()
                .setKd(0.12)
                .setKs(0.42)
                .setShininess(160)
                .setKt(0.45)
                .setTransparencyBlur(0.045)
                .setGlobalSamples(4);
        Material matteMaterial = new Material().setKd(0.55).setKs(0.22).setShininess(70);

        geometries.add(
                new Triangle(
                        new Point(-95, -24, -55),
                        new Point(95, -24, -55),
                        new Point(95, -24, -230))
                        .setEmission(new Color(34, 36, 38))
                        .setMaterial(floorMaterial),
                new Triangle(
                        new Point(-95, -24, -55),
                        new Point(95, -24, -230),
                        new Point(-95, -24, -230))
                        .setEmission(new Color(34, 36, 38))
                        .setMaterial(floorMaterial),
                new Triangle(
                        new Point(-85, -24, -210),
                        new Point(85, -24, -210),
                        new Point(85, 68, -210))
                        .setEmission(new Color(22, 28, 44))
                        .setMaterial(wallMaterial),
                new Triangle(
                        new Point(-85, -24, -210),
                        new Point(85, 68, -210),
                        new Point(-85, 68, -210))
                        .setEmission(new Color(22, 28, 44))
                        .setMaterial(wallMaterial),
                new Sphere(new Point(-26, -6, -100), 16)
                        .setEmission(new Color(72, 72, 78))
                        .setMaterial(mirrorMaterial),
                new Sphere(new Point(24, -8, -118), 14)
                        .setEmission(new Color(32, 56, 82))
                        .setMaterial(glassMaterial),
                new Sphere(new Point(-2, 16, -145), 8)
                        .setEmission(new Color(82, 68, 34))
                        .setMaterial(matteMaterial));

        addWitnessObjects(geometries, matteMaterial);

        return new Scene("Reflection transparency")
                .setBackground(new Color(5, 7, 10))
                .setAmbientLight(new AmbientLight(new Color(30, 30, 34), 0.18))
                .setGeometries(geometries)
                .setLights(List.of(
                        new PointLight(new Color(560, 430, 300), new Point(-48, 62, 18))
                                .setKl(0.00045)
                                .setKq(0.000025),
                        new PointLight(new Color(130, 170, 260), new Point(50, 20, -35))
                                .setKl(0.0007)
                                .setKq(0.000035)));
    }

    /**
     * Adds small bounded objects that secondary rays can reflect or refract toward.
     *
     * @param geometries geometry collection
     * @param material   material
     */
    private static void addWitnessObjects(Geometries geometries, Material material) {
        for (int y = -1; y <= 1; y++) {
            for (int x = -2; x <= 2; x++) {
                geometries.add(new Sphere(new Point(x * 10, -18 + y * 7, -168 - y * 6), 2.5)
                        .setEmission(new Color(
                                38 + (x + 2) * 8,
                                44 + (y + 1) * 12,
                                58 + (2 - x) * 5))
                        .setMaterial(material));
            }
        }
    }
}
