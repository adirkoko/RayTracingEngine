package benchmark.scenes;

import benchmark.core.RenderCameraSpec;
import geometries.Geometries;
import geometries.Sphere;
import geometries.Triangle;
import geometries.acceleration.AccelerationType;
import lighting.AmbientLight;
import primitives.Color;
import primitives.Material;
import primitives.Point;
import primitives.Vector;
import scene.Scene;

import java.util.List;

/**
 * Shadow-heavy bounded scene with sampled area lights and many occluders.
 * <p>
 * This scene multiplies local-lighting work through multiple sampled-light
 * values per light source. It mixes opaque and partially transparent blockers so
 * shadow rays exercise both hard occlusion and transparency-aware attenuation.
 */
final class ShadowHeavyScene implements BenchmarkScene {

    @Override
    public String name() {
        return "shadow-heavy";
    }

    @Override
    public RenderCameraSpec cameraSpec() {
        return new RenderCameraSpec(
                new Point(0, 8, 28),
                new Vector(0, -0.1, -1),
                new Vector(0, 1, -0.1),
                120,
                100,
                100);
    }

    @Override
    public Scene createScene(AccelerationType accelerationType) {
        Geometries geometries = new Geometries().setAcceleration(accelerationType);
        Material floorMaterial = new Material().setKd(0.58).setKs(0.12).setShininess(40);
        Material receiverMaterial = new Material().setKd(0.5).setKs(0.28).setShininess(90);
        Material opaqueOccluder = new Material().setKd(0.46).setKs(0.12).setShininess(40);
        Material transparentOccluder = new Material()
                .setKd(0.25)
                .setKs(0.22)
                .setShininess(90)
                .setKt(0.42);

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
                        new Point(-85, -24, -215),
                        new Point(85, -24, -215),
                        new Point(85, 68, -215))
                        .setEmission(new Color(22, 28, 42))
                        .setMaterial(floorMaterial),
                new Triangle(
                        new Point(-85, -24, -215),
                        new Point(85, 68, -215),
                        new Point(-85, 68, -215))
                        .setEmission(new Color(22, 28, 42))
                        .setMaterial(floorMaterial),
                new Sphere(new Point(-32, -8, -112), 13)
                        .setEmission(new Color(82, 44, 36))
                        .setMaterial(receiverMaterial),
                new Sphere(new Point(30, -8, -122), 13)
                        .setEmission(new Color(38, 64, 88))
                        .setMaterial(receiverMaterial),
                new Sphere(new Point(0, 14, -152), 9)
                        .setEmission(new Color(82, 70, 36))
                        .setMaterial(receiverMaterial));

        addOccluderCurtain(geometries, opaqueOccluder, transparentOccluder);

        return new Scene("Shadow heavy")
                .setBackground(new Color(5, 7, 10))
                .setAmbientLight(new AmbientLight(new Color(24, 24, 28), 0.16))
                .setGeometries(geometries)
                .setLights(List.of(
                        new SampledAreaLight(
                                new Color(620, 450, 300),
                                new Point(-42, 46, -36),
                                new Vector(1, 0, 0),
                                new Vector(0, 0.18, -1),
                                18,
                                18,
                                3,
                                0.00045,
                                0.000025),
                        new SampledAreaLight(
                                new Color(170, 220, 330),
                                new Point(46, 28, -72),
                                new Vector(1, 0, 0),
                                new Vector(0, 0.12, -1),
                                16,
                                14,
                                3,
                                0.0007,
                                0.000035)));
    }

    /**
     * Adds blockers between the sampled lights and receiver objects.
     *
     * @param geometries           geometry collection
     * @param opaqueMaterial       opaque blocker material
     * @param transparentMaterial  transparent blocker material
     */
    private static void addOccluderCurtain(
            Geometries geometries,
            Material opaqueMaterial,
            Material transparentMaterial) {
        for (int row = 0; row < 4; row++) {
            for (int column = -3; column <= 3; column++) {
                boolean transparent = (row + column) % 3 == 0;
                geometries.add(new Sphere(new Point(
                        column * 9,
                        -8 + row * 8,
                        -82 - row * 18 - Math.abs(column) * 2),
                        transparent ? 3.0 : 2.6)
                        .setEmission(transparent
                                ? new Color(38, 58, 76)
                                : new Color(56 + row * 8, 42 + Math.abs(column) * 6, 44))
                        .setMaterial(transparent ? transparentMaterial : opaqueMaterial));
            }
        }
    }

}
