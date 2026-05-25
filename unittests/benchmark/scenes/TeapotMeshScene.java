package benchmark.scenes;

import benchmark.core.RenderCameraSpec;
import geometries.Geometries;
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
 * Simple teapot mesh benchmark scene.
 * <p>
 * This scene keeps the workload focused on one dense triangle mesh. It is useful
 * for measuring the acceleration structures against a realistic mesh-like object
 * without adding global effects, soft shadows, or many unrelated primitives.
 */
final class TeapotMeshScene implements BenchmarkScene {

    @Override
    public String name() {
        return "teapot-mesh";
    }

    @Override
    public RenderCameraSpec cameraSpec() {
        return new RenderCameraSpec(
                new Point(0, 0, -1000),
                new Vector(0, 0, 1),
                new Vector(0, 1, 0),
                1000,
                200,
                200);
    }

    @Override
    public Scene createScene(AccelerationType accelerationType) {
        Geometries geometries = new Geometries().setAcceleration(accelerationType);
        TeapotMesh.addTo(geometries, new Color(180, 35, 28), new Material()
                .setKd(0.52)
                .setKs(0.42)
                .setShininess(120));

        return new Scene("Teapot mesh")
                .setBackground(new Color(4, 5, 7))
                .setAmbientLight(new AmbientLight(new Color(20, 18, 18), 0.15))
                .setGeometries(geometries)
                .setLights(List.of(
                        new PointLight(new Color(520, 460, 400), new Point(100, 45, -130))
                                .setKl(0.0001)
                                .setKq(0.000001),
                        new PointLight(new Color(120, 160, 230), new Point(-80, 30, -90))
                                .setKl(0.00018)
                                .setKq(0.0000015)));
    }
}
