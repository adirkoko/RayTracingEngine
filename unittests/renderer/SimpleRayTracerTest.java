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
}
