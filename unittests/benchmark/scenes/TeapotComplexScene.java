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
 * Complex teapot benchmark scene with a dense mesh and surrounding bounded geometry.
 * <p>
 * The scene uses the teapot as the dominant triangle workload, then adds a finite
 * floor, a finite wall, scattered small objects, transparent objects, reflective
 * objects, and several lights. It is intended to show how LINEAR, BVH, GRID, and
 * AUTO behave when primary rays, shadow rays, and a small amount of recursion all
 * traverse the same mixed bounded scene.
 */
final class TeapotComplexScene implements BenchmarkScene {

    @Override
    public String name() {
        return "teapot-complex";
    }

    @Override
    public RenderCameraSpec cameraSpec() {
        return new RenderCameraSpec(
                new Point(0, 4, -260),
                new Vector(0, -0.04, 1),
                new Vector(0, 1, 0.04),
                280,
                180,
                155);
    }

    @Override
    public Scene createScene(AccelerationType accelerationType) {
        Geometries geometries = new Geometries().setAcceleration(accelerationType);

        Material teapotMaterial = new Material()
                .setKd(0.48)
                .setKs(0.48)
                .setShininess(150);
        Material floorMaterial = new Material()
                .setKd(0.55)
                .setKs(0.16)
                .setShininess(45)
                .setKr(0.12);
        Material wallMaterial = new Material().setKd(0.52).setKs(0.08).setShininess(25);
        Material matteMaterial = new Material().setKd(0.56).setKs(0.18).setShininess(55);
        Material glossyMaterial = new Material()
                .setKd(0.24)
                .setKs(0.52)
                .setShininess(170)
                .setKr(0.36);
        Material glassMaterial = new Material()
                .setKd(0.16)
                .setKs(0.36)
                .setShininess(130)
                .setKt(0.42);

        addRoomSurfaces(geometries, floorMaterial, wallMaterial);
        TeapotMesh.addTo(geometries, new Color(168, 48, 38), teapotMaterial);
        addFeatureObjects(geometries, matteMaterial, glossyMaterial, glassMaterial);
        addScatterObjects(geometries, matteMaterial);

        return new Scene("Teapot complex")
                .setBackground(new Color(5, 7, 10))
                .setAmbientLight(new AmbientLight(new Color(28, 27, 30), 0.16))
                .setGeometries(geometries)
                .setLights(List.of(
                        new PointLight(new Color(620, 500, 360), new Point(-92, 86, -115))
                                .setKl(0.00028)
                                .setKq(0.000012),
                        new PointLight(new Color(190, 230, 330), new Point(88, 38, -72))
                                .setKl(0.00042)
                                .setKq(0.000018),
                        new PointLight(new Color(170, 120, 90), new Point(12, -12, -150))
                                .setKl(0.0005)
                                .setKq(0.000025)));
    }

    /**
     * Adds finite receiver surfaces around the teapot.
     *
     * @param geometries    geometry collection
     * @param floorMaterial floor material
     * @param wallMaterial  wall material
     */
    private static void addRoomSurfaces(Geometries geometries, Material floorMaterial, Material wallMaterial) {
        geometries.add(
                new Triangle(
                        new Point(-155, -43, -120),
                        new Point(155, -43, -120),
                        new Point(155, -43, 145))
                        .setEmission(new Color(34, 36, 37))
                        .setMaterial(floorMaterial),
                new Triangle(
                        new Point(-155, -43, -120),
                        new Point(155, -43, 145),
                        new Point(-155, -43, 145))
                        .setEmission(new Color(34, 36, 37))
                        .setMaterial(floorMaterial),
                new Triangle(
                        new Point(-155, -43, 145),
                        new Point(155, -43, 145),
                        new Point(155, 92, 145))
                        .setEmission(new Color(24, 29, 39))
                        .setMaterial(wallMaterial),
                new Triangle(
                        new Point(-155, -43, 145),
                        new Point(155, 92, 145),
                        new Point(-155, 92, 145))
                        .setEmission(new Color(24, 29, 39))
                        .setMaterial(wallMaterial));
    }

    /**
     * Adds larger objects that create reflection, transparency, and shadow-ray work.
     *
     * @param geometries      geometry collection
     * @param matteMaterial   matte material
     * @param glossyMaterial  reflective material
     * @param glassMaterial   transparent material
     */
    private static void addFeatureObjects(
            Geometries geometries,
            Material matteMaterial,
            Material glossyMaterial,
            Material glassMaterial) {
        geometries.add(
                new Sphere(new Point(-88, -24, -28), 17)
                        .setEmission(new Color(42, 72, 92))
                        .setMaterial(glossyMaterial),
                new Sphere(new Point(83, -22, 20), 15)
                        .setEmission(new Color(42, 70, 88))
                        .setMaterial(glassMaterial),
                new Sphere(new Point(64, 12, 88), 10)
                        .setEmission(new Color(82, 68, 36))
                        .setMaterial(matteMaterial),
                new Sphere(new Point(-62, 18, 72), 8)
                        .setEmission(new Color(72, 44, 70))
                        .setMaterial(matteMaterial));
    }

    /**
     * Adds many small bounded objects while leaving the teapot readable in the center.
     *
     * @param geometries geometry collection
     * @param material   shared matte material
     */
    private static void addScatterObjects(Geometries geometries, Material material) {
        for (int row = 0; row < 5; row++) {
            double z = -86 + row * 38;
            for (int column = -5; column <= 5; column++) {
                if (Math.abs(column) <= 2 && row >= 1 && row <= 3) continue;

                double radius = 2.2 + (Math.abs(column) + row) % 4 * 0.55;
                geometries.add(new Sphere(new Point(
                        column * 24 + (row % 2) * 7,
                        -42 + radius,
                        z + (column % 2) * 6),
                        radius)
                        .setEmission(new Color(
                                44 + row * 8,
                                48 + (column + 5) * 4,
                                58 + (5 - Math.abs(column)) * 5))
                        .setMaterial(material));
            }
        }

        for (int i = 0; i < 16; i++) {
            int side = i % 2 == 0 ? -1 : 1;
            double height = -30 + (i % 4) * 12;
            geometries.add(new Sphere(new Point(
                    side * (96 + (i % 4) * 8),
                    height,
                    -64 + (i / 2) * 22),
                    2.4 + (i % 3) * 0.45)
                    .setEmission(new Color(58 + i * 3, 50 + (i % 5) * 6, 72))
                    .setMaterial(material));
        }
    }
}
