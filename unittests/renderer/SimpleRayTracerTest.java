package renderer;

import geometries.Plane;
import geometries.Sphere;
import lighting.LightSample;
import lighting.LightSource;
import org.junit.jupiter.api.Test;
import primitives.*;
import scene.Scene;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link renderer.SimpleRayTracer}.
 */
class SimpleRayTracerTest {

    /**
     * Test soft-shadow averaging across multiple light samples.
     */
    @Test
    void testSoftShadowSamples() {
        Color fullyLit = tracePlane(List.of(
                new Point(4, 0, -5),
                new Point(-4, 0, -5)
        ), false);
        Color partiallyBlocked = tracePlane(List.of(
                new Point(4, 0, -5),
                new Point(-4, 0, -5)
        ), true);

        assertTrue(partiallyBlocked.isSimilar(fullyLit.reduce(2), 1e-10),
                "Blocking one of two equal light samples should produce half the full light contribution");
    }

    /**
     * Test glossy reflection sampling keeps averaged global contribution.
     */
    @Test
    void testGlossyReflectionSamples() {
        Color background = new Color(20, 40, 60);
        Scene scene = new Scene("Glossy reflection test")
                .setBackground(background);
        scene.geometries.add(new Plane(new Point(0, 0, -1), new Vector(0, 0, 1))
                .setMaterial(new Material()
                        .setKr(1)
                        .setReflectionBlur(0.2)
                        .setGlobalSamples(8)));

        assertTrue(new SimpleRayTracer(scene)
                        .traceRay(new Ray(Point.ZERO, new Vector(0, 0, -1)))
                        .isSimilar(background, 1e-10),
                "Glossy reflection samples should average the same background contribution");
    }

    /**
     * Test diffused glass sampling keeps averaged global contribution.
     */
    @Test
    void testDiffusedGlassSamples() {
        Color background = new Color(30, 50, 70);
        Scene scene = new Scene("Diffused glass test")
                .setBackground(background);
        scene.geometries.add(new Plane(new Point(0, 0, -1), new Vector(0, 0, 1))
                .setMaterial(new Material()
                        .setKt(1)
                        .setTransparencyBlur(0.2)
                        .setGlobalSamples(8)));

        assertTrue(new SimpleRayTracer(scene)
                        .traceRay(new Ray(Point.ZERO, new Vector(0, 0, -1)))
                        .isSimilar(background, 1e-10),
                "Diffused glass samples should average the same background contribution");
    }

    /**
     * Test light sample processing is capped to prevent unbounded shadow rays.
     */
    @Test
    void testLightSampleCap() {
        Scene scene = new Scene("Light sample cap test");
        scene.geometries.add(new Plane(new Point(0, 0, -10), new Vector(0, 0, 1))
                .setMaterial(new Material().setKd(1)));
        scene.lights.add(new ManySampleLight());

        assertTrue(new SimpleRayTracer(scene)
                        .traceRay(new Ray(Point.ZERO, new Vector(0, 0, -1)))
                        .isSimilar(new Color(100, 100, 100), 1e-10),
                "Ray tracer should consume only the bounded light sample prefix");
    }

    /**
     * Traces a diffuse plane under a sampled light, optionally blocking one sample.
     *
     * @param lightPositions sampled light positions
     * @param addBlocker     true to block the first sample
     * @return traced color at the center of the plane
     */
    private Color tracePlane(List<Point> lightPositions, boolean addBlocker) {
        Scene scene = new Scene("Soft shadow test");
        scene.geometries.add(new Plane(new Point(0, 0, -10), new Vector(0, 0, 1))
                .setMaterial(new Material().setKd(1)));
        if (addBlocker)
            scene.geometries.add(new Sphere(new Point(2, 0, -7.5), 0.5));
        scene.lights.add(new SampledLight(new Color(100, 100, 100), lightPositions));

        return new SimpleRayTracer(scene).traceRay(new Ray(Point.ZERO, new Vector(0, 0, -1)));
    }

    /**
     * Test light source that samples fixed point-light positions.
     *
     * @param intensity      light intensity
     * @param lightPositions sampled light positions
     */
    private record SampledLight(Color intensity, List<Point> lightPositions) implements LightSource {

        @Override
        public Color getIntensity(Point p) {
            return intensity;
        }

        @Override
        public Vector getL(Point p) {
            return p.subtract(lightPositions.getFirst()).normalize();
        }

        @Override
        public double getDistance(Point point) {
            return lightPositions.getFirst().distance(point);
        }

        @Override
        public List<LightSample> getSamples(Point point) {
            return lightPositions.stream()
                    .map(lightPosition -> new LightSample(
                            intensity,
                            point.subtract(lightPosition).normalize(),
                            lightPosition.distance(point)))
                    .toList();
        }
    }

    /**
     * Test light source that returns more samples than the ray tracer should consume.
     */
    private static class ManySampleLight implements LightSource {

        @Override
        public Color getIntensity(Point p) {
            return new Color(100, 100, 100);
        }

        @Override
        public Vector getL(Point p) {
            return new Vector(0, 0, -1);
        }

        @Override
        public double getDistance(Point point) {
            return Double.POSITIVE_INFINITY;
        }

        @Override
        public List<LightSample> getSamples(Point point) {
            List<LightSample> samples = new java.util.LinkedList<>();
            for (int i = 0; i < 64; i++)
                samples.add(new LightSample(new Color(100, 100, 100), new Vector(0, 0, -1), Double.POSITIVE_INFINITY));
            for (int i = 0; i < 16; i++)
                samples.add(new LightSample(Color.BLACK, new Vector(0, 0, -1), Double.POSITIVE_INFINITY));
            return samples;
        }
    }
}
