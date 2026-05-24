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
 * Uniform bounded scene with many similarly sized objects spread through space.
 * <p>
 * The scene is designed to compare BVH and GRID on data with regular spatial coherence:
 * objects are finite, similarly sized, and distributed evenly enough that both acceleration
 * structures should have an opportunity to help.
 */
final class UniformBoundedScene implements BenchmarkScene {

    /**
     * Sphere grid radius.
     */
    private static final double SPHERE_RADIUS = 3.6;

    @Override
    public String name() {
        return "uniform-bounded";
    }

    @Override
    public RenderCameraSpec cameraSpec() {
        return new RenderCameraSpec(
                new Point(0, 0, 24),
                new Vector(0, 0, -1),
                new Vector(0, 1, 0),
                115,
                95,
                95);
    }

    @Override
    public Scene createScene(AccelerationType accelerationType) {
        Geometries geometries = new Geometries().setAcceleration(accelerationType);
        Material material = new Material().setKd(0.5).setKs(0.22).setShininess(80);

        for (int z = 0; z < 4; z++) {
            for (int y = -2; y <= 2; y++) {
                for (int x = -2; x <= 2; x++) {
                    geometries.add(new Sphere(new Point(x * 16, y * 16, -70 - z * 26), SPHERE_RADIUS)
                            .setEmission(new Color(
                                    24 + z * 15,
                                    32 + (x + 2) * 10,
                                    42 + (y + 2) * 9))
                            .setMaterial(material));
                }
            }
        }

        return new Scene("Uniform bounded")
                .setBackground(new Color(4, 6, 10))
                .setAmbientLight(new AmbientLight(new Color(24, 24, 28), 0.18))
                .setGeometries(geometries)
                .setLights(List.of(
                        new PointLight(new Color(520, 410, 300), new Point(-45, 55, 10))
                                .setKl(0.00045)
                                .setKq(0.000025),
                        new PointLight(new Color(120, 160, 240), new Point(55, -35, -10))
                                .setKl(0.00065)
                                .setKq(0.00003)));
    }
}
