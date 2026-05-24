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
 * Image-quality scene for grounded objects, contact shadows, and sampled soft shadows.
 */
final class GroundedSoftShadowScene implements BenchmarkScene {

    /**
     * Floor Y coordinate.
     */
    private static final double FLOOR_Y = -24;

    @Override
    public String name() {
        return "grounded-soft-shadow";
    }

    @Override
    public RenderCameraSpec cameraSpec() {
        return new RenderCameraSpec(
                new Point(0, 9, 32),
                new Vector(0, -0.16, -1),
                new Vector(0, 1, -0.16),
                120,
                105,
                105);
    }

    @Override
    public Scene createScene(AccelerationType accelerationType) {
        Geometries geometries = new Geometries().setAcceleration(accelerationType);
        Material floorMaterial = new Material().setKd(0.62).setKs(0.12).setShininess(45);
        Material wallMaterial = new Material().setKd(0.48).setKs(0.08).setShininess(25);
        Material matteMaterial = new Material().setKd(0.55).setKs(0.22).setShininess(80);
        Material transparentMaterial = new Material()
                .setKd(0.22)
                .setKs(0.28)
                .setShininess(100)
                .setKt(0.38);

        addRoomSurfaces(geometries, floorMaterial, wallMaterial);
        addGroundedReceivers(geometries, matteMaterial);
        addShadowBlockers(geometries, matteMaterial, transparentMaterial);

        return new Scene("Grounded soft shadow")
                .setBackground(new Color(5, 7, 10))
                .setAmbientLight(new AmbientLight(new Color(22, 22, 25), 0.16))
                .setGeometries(geometries)
                .setLights(List.of(
                        new SampledAreaLight(
                                new Color(720, 540, 360),
                                new Point(-42, 58, -42),
                                new Vector(1, 0, 0),
                                new Vector(0, 0.2, -1),
                                24,
                                20,
                                4,
                                0.00042,
                                0.000022),
                        new PointLight(new Color(70, 100, 150), new Point(54, 18, -72))
                                .setKl(0.0008)
                                .setKq(0.00004)));
    }

    /**
     * Adds a finite floor and a back wall.
     *
     * @param geometries    geometry collection
     * @param floorMaterial floor material
     * @param wallMaterial  wall material
     */
    private static void addRoomSurfaces(Geometries geometries, Material floorMaterial, Material wallMaterial) {
        geometries.add(
                new Triangle(
                        new Point(-115, FLOOR_Y, -45),
                        new Point(115, FLOOR_Y, -45),
                        new Point(115, FLOOR_Y, -235))
                        .setEmission(new Color(38, 40, 42))
                        .setMaterial(floorMaterial),
                new Triangle(
                        new Point(-115, FLOOR_Y, -45),
                        new Point(115, FLOOR_Y, -235),
                        new Point(-115, FLOOR_Y, -235))
                        .setEmission(new Color(38, 40, 42))
                        .setMaterial(floorMaterial),
                new Triangle(
                        new Point(-95, FLOOR_Y, -225),
                        new Point(95, FLOOR_Y, -225),
                        new Point(95, 72, -225))
                        .setEmission(new Color(24, 30, 42))
                        .setMaterial(wallMaterial),
                new Triangle(
                        new Point(-95, FLOOR_Y, -225),
                        new Point(95, 72, -225),
                        new Point(-95, 72, -225))
                        .setEmission(new Color(24, 30, 42))
                        .setMaterial(wallMaterial));
    }

    /**
     * Adds main objects that sit directly on the floor.
     *
     * @param geometries geometry collection
     * @param material   receiver material
     */
    private static void addGroundedReceivers(Geometries geometries, Material material) {
        geometries.add(
                groundedSphere(new Point(-34, 0, -122), 24, new Color(86, 38, 32), material),
                groundedSphere(new Point(24, -7, -136), 17, new Color(34, 62, 90), material),
                groundedSphere(new Point(0, -13, -82), 11, new Color(78, 64, 30), material),
                groundedSphere(new Point(48, -15, -92), 9, new Color(42, 80, 54), material));
    }

    /**
     * Adds opaque and transparent blockers between the area light and the floor.
     *
     * @param geometries           geometry collection
     * @param opaqueMaterial       opaque blocker material
     * @param transparentMaterial  transparent blocker material
     */
    private static void addShadowBlockers(
            Geometries geometries,
            Material opaqueMaterial,
            Material transparentMaterial) {
        geometries.add(
                new Sphere(new Point(-12, 10, -96), 7)
                        .setEmission(new Color(58, 46, 42))
                        .setMaterial(opaqueMaterial),
                new Sphere(new Point(10, 13, -108), 8)
                        .setEmission(new Color(38, 58, 78))
                        .setMaterial(transparentMaterial),
                new Sphere(new Point(32, 2, -118), 5)
                        .setEmission(new Color(64, 50, 44))
                        .setMaterial(opaqueMaterial),
                new Sphere(new Point(-44, 4, -92), 5)
                        .setEmission(new Color(42, 66, 76))
                        .setMaterial(transparentMaterial));
    }

    /**
     * Creates a sphere whose bottom touches the floor.
     *
     * @param center   requested center, with Y replaced by floor height plus radius
     * @param radius   sphere radius
     * @param emission emission color
     * @param material material
     * @return grounded sphere
     */
    private static Sphere groundedSphere(Point center, double radius, Color emission, Material material) {
        Sphere sphere = new Sphere(new Point(center.getX(), FLOOR_Y + radius, center.getZ()), radius);
        sphere.setEmission(emission).setMaterial(material);
        return sphere;
    }
}
