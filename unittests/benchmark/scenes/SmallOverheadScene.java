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
 * Very small bounded scene for measuring fixed acceleration overhead.
 * <p>
 * This scene intentionally contains only a few finite geometries. It is useful for checking
 * whether BVH, GRID, or AUTO add more setup/traversal cost than they save when the scene is
 * too small to benefit from spatial indexing.
 */
final class SmallOverheadScene implements BenchmarkScene {

    @Override
    public String name() {
        return "small-overhead";
    }

    @Override
    public RenderCameraSpec cameraSpec() {
        return new RenderCameraSpec(
                new Point(0, 2, 18),
                new Vector(0, -0.08, -1),
                new Vector(0, 1, -0.08),
                90,
                70,
                70);
    }

    @Override
    public Scene createScene(AccelerationType accelerationType) {
        Geometries geometries = new Geometries().setAcceleration(accelerationType);
        Material matte = new Material().setKd(0.55).setKs(0.18).setShininess(60);
        Material floor = new Material().setKd(0.6).setKs(0.08).setShininess(30);

        geometries.add(
                new Triangle(
                        new Point(-42, -16, -45),
                        new Point(42, -16, -45),
                        new Point(42, -16, -130))
                        .setEmission(new Color(32, 35, 38))
                        .setMaterial(floor),
                new Triangle(
                        new Point(-42, -16, -45),
                        new Point(42, -16, -130),
                        new Point(-42, -16, -130))
                        .setEmission(new Color(32, 35, 38))
                        .setMaterial(floor),
                new Sphere(new Point(-14, -5, -72), 10)
                        .setEmission(new Color(78, 38, 34))
                        .setMaterial(matte),
                new Sphere(new Point(12, -7, -86), 8)
                        .setEmission(new Color(34, 58, 82))
                        .setMaterial(matte));

        return new Scene("Small overhead")
                .setBackground(new Color(5, 7, 10))
                .setAmbientLight(new AmbientLight(new Color(28, 28, 30), 0.2))
                .setGeometries(geometries)
                .setLights(List.of(new PointLight(new Color(430, 340, 250), new Point(-30, 45, 5))
                        .setKl(0.0007)
                        .setKq(0.00004)));
    }
}
