package benchmark.scenes;

import benchmark.core.RenderCameraSpec;
import geometries.Geometries;
import geometries.Plane;
import geometries.Sphere;
import geometries.Tube;
import geometries.acceleration.AccelerationType;
import lighting.AmbientLight;
import lighting.PointLight;
import primitives.Color;
import primitives.Material;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;
import scene.Scene;

import java.util.List;

/**
 * Mixed bounded/unbounded scene for validating acceleration fallback paths.
 * <p>
 * The planes and infinite tube in this scene do not have finite bounding boxes, while
 * the spheres do. This forces BVH and GRID indexes to combine accelerated bounded
 * traversal with linear checks for different kinds of unbounded geometry.
 */
final class UnboundedFallbackScene implements BenchmarkScene {

    @Override
    public String name() {
        return "unbounded-fallback";
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
        Material wallMaterial = new Material().setKd(0.48).setKs(0.08).setShininess(25);
        Material tubeMaterial = new Material().setKd(0.46).setKs(0.28).setShininess(110);
        Material sphereMaterial = new Material().setKd(0.48).setKs(0.26).setShininess(100);

        geometries.add(
                new Plane(new Point(0, -18, -90), new Vector(0, 1, 0))
                        .setEmission(new Color(34, 36, 38))
                        .setMaterial(wallMaterial),
                new Plane(new Point(0, 0, -185), new Vector(0, 0, 1))
                        .setEmission(new Color(22, 28, 42))
                        .setMaterial(wallMaterial),
                new Tube(new Ray(new Point(-42, -10, -118), new Vector(1, 0.16, -0.22)), 3.0)
                        .setEmission(new Color(58, 64, 82))
                        .setMaterial(tubeMaterial),
                new Sphere(new Point(-28, -6, -92), 12)
                        .setEmission(new Color(82, 42, 36))
                        .setMaterial(sphereMaterial),
                new Sphere(new Point(24, -8, -112), 10)
                        .setEmission(new Color(38, 62, 86))
                        .setMaterial(sphereMaterial),
                new Sphere(new Point(0, 10, -138), 8)
                        .setEmission(new Color(80, 70, 36))
                        .setMaterial(sphereMaterial));

        return new Scene("Unbounded fallback")
                .setBackground(new Color(5, 7, 10))
                .setAmbientLight(new AmbientLight(new Color(28, 28, 30), 0.2))
                .setGeometries(geometries)
                .setLights(List.of(
                        new PointLight(new Color(520, 390, 270), new Point(-45, 55, 15))
                                .setKl(0.0005)
                                .setKq(0.000025),
                        new PointLight(new Color(120, 155, 230), new Point(45, 20, -35))
                                .setKl(0.0007)
                                .setKq(0.000035)));
    }
}
