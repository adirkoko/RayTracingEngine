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
 * Small deterministic scene used as the initial render-profile comparison smoke scene.
 */
final class ProfileComparisonScene implements BenchmarkScene {

    @Override
    public String name() {
        return "profile-comparison";
    }

    @Override
    public RenderCameraSpec cameraSpec() {
        return new RenderCameraSpec(
                new Point(0, 8, 24),
                new Vector(0, -0.12, -1),
                new Vector(0, 1, -0.12),
                120,
                95,
                95);
    }

    @Override
    public Scene createScene(AccelerationType accelerationType) {
        Geometries geometries = new Geometries().setAcceleration(accelerationType);
        Material floorMaterial = new Material().setKd(0.55).setKs(0.12).setShininess(40);
        Material backWallMaterial = new Material().setKd(0.42).setKs(0.08).setShininess(25);
        Material sphereMaterial = new Material().setKd(0.45).setKs(0.35).setShininess(120);
        Material glassMaterial = new Material().setKd(0.15).setKs(0.45).setShininess(160).setKt(0.25);

        geometries.add(
                new Triangle(
                        new Point(-120, -28, -35),
                        new Point(120, -28, -35),
                        new Point(120, -28, -260))
                        .setEmission(new Color(35, 38, 42))
                        .setMaterial(floorMaterial),
                new Triangle(
                        new Point(-120, -28, -35),
                        new Point(120, -28, -260),
                        new Point(-120, -28, -260))
                        .setEmission(new Color(35, 38, 42))
                        .setMaterial(floorMaterial),
                new Triangle(
                        new Point(-90, -28, -245),
                        new Point(90, -28, -245),
                        new Point(90, 70, -245))
                        .setEmission(new Color(22, 29, 45))
                        .setMaterial(backWallMaterial),
                new Triangle(
                        new Point(-90, -28, -245),
                        new Point(90, 70, -245),
                        new Point(-90, 70, -245))
                        .setEmission(new Color(22, 29, 45))
                        .setMaterial(backWallMaterial),
                new Sphere(new Point(-32, -8, -115), 18)
                        .setEmission(new Color(80, 28, 30))
                        .setMaterial(sphereMaterial.setKr(0.18).setReflectionBlur(0.04).setGlobalSamples(4)),
                new Sphere(new Point(28, -12, -135), 14)
                        .setEmission(new Color(28, 58, 86))
                        .setMaterial(glassMaterial.setTransparencyBlur(0.03).setGlobalSamples(4)),
                new Sphere(new Point(0, 14, -165), 11)
                        .setEmission(new Color(76, 63, 30))
                        .setMaterial(new Material().setKd(0.35).setKs(0.55).setShininess(180).setKr(0.1))
        );

        for (int row = 0; row < 4; row++) {
            for (int column = -4; column <= 4; column++) {
                geometries.add(new Sphere(new Point(column * 14, -22 + row * 3, -185 - row * 18), 4.2)
                        .setEmission(new Color(20 + row * 14, 28 + column * column * 3, 38 + row * 8))
                        .setMaterial(new Material().setKd(0.5).setKs(0.18).setShininess(70)));
            }
        }

        return new Scene("Render profile comparison")
                .setBackground(new Color(5, 7, 10))
                .setAmbientLight(new AmbientLight(new Color(30, 30, 34), 0.2))
                .setGeometries(geometries)
                .setLights(List.of(
                        new PointLight(new Color(500, 390, 260), new Point(-45, 65, 20))
                                .setKl(0.0004)
                                .setKq(0.00002),
                        new PointLight(new Color(130, 170, 260), new Point(60, 25, -60))
                                .setKl(0.0007)
                                .setKq(0.00003)));
    }
}
