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
 * Mixed-scale bounded scene with very different object sizes in one geometry collection.
 * <p>
 * This scene is designed to expose cases where median BVH splitting or automatic GRID
 * resolution can be skewed by large bounding boxes mixed with many much smaller ones.
 */
final class MixedScaleBoundedScene implements BenchmarkScene {

    @Override
    public String name() {
        return "mixed-scale-bounded";
    }

    @Override
    public RenderCameraSpec cameraSpec() {
        return new RenderCameraSpec(
                new Point(0, 2, 28),
                new Vector(0, -0.03, -1),
                new Vector(0, 1, -0.03),
                120,
                105,
                105);
    }

    @Override
    public Scene createScene(AccelerationType accelerationType) {
        Geometries geometries = new Geometries().setAcceleration(accelerationType);
        Material largeMaterial = new Material().setKd(0.45).setKs(0.32).setShininess(120);
        Material mediumMaterial = new Material().setKd(0.5).setKs(0.24).setShininess(80);
        Material smallMaterial = new Material().setKd(0.55).setKs(0.16).setShininess(55);

        geometries.add(
                new Sphere(new Point(-30, -8, -102), 20)
                        .setEmission(new Color(78, 42, 38))
                        .setMaterial(largeMaterial),
                new Sphere(new Point(28, 10, -118), 12)
                        .setEmission(new Color(38, 64, 86))
                        .setMaterial(mediumMaterial),
                new Sphere(new Point(12, -24, -152), 8)
                        .setEmission(new Color(74, 66, 34))
                        .setMaterial(mediumMaterial),
                new Sphere(new Point(-18, 24, -168), 6)
                        .setEmission(new Color(38, 78, 55))
                        .setMaterial(mediumMaterial));

        addSmallGrid(geometries, smallMaterial, new Point(34, -24, -78), 2.1, 7.0, new Color(58, 52, 84));
        addSmallGrid(geometries, smallMaterial, new Point(-42, 28, -134), 1.6, 5.8, new Color(70, 72, 42));
        addSmallGrid(geometries, smallMaterial, new Point(40, 24, -184), 1.3, 5.2, new Color(44, 78, 78));

        return new Scene("Mixed scale bounded")
                .setBackground(new Color(4, 6, 10))
                .setAmbientLight(new AmbientLight(new Color(24, 24, 28), 0.18))
                .setGeometries(geometries)
                .setLights(List.of(
                        new PointLight(new Color(560, 420, 290), new Point(-60, 65, 18))
                                .setKl(0.00045)
                                .setKq(0.000025),
                        new PointLight(new Color(120, 160, 250), new Point(55, -40, -20))
                                .setKl(0.00075)
                                .setKq(0.000035)));
    }

    /**
     * Adds a compact grid of small bounded objects.
     *
     * @param geometries geometry collection
     * @param material   sphere material
     * @param center     grid center
     * @param radius     sphere radius
     * @param spacing    distance between neighboring sphere centers
     * @param baseColor  base emission color
     */
    private static void addSmallGrid(
            Geometries geometries,
            Material material,
            Point center,
            double radius,
            double spacing,
            Color baseColor) {
        for (int y = -1; y <= 1; y++) {
            for (int x = -1; x <= 1; x++) {
                geometries.add(new Sphere(new Point(
                        center.getX() + x * spacing,
                        center.getY() + y * spacing,
                        center.getZ()),
                        radius)
                        .setEmission(baseColor.add(new Color(
                                (x + 1) * 8,
                                (y + 1) * 8,
                                (2 - x + y) * 4)))
                        .setMaterial(material));
            }
        }
    }
}
