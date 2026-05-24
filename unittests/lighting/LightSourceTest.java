package lighting;

import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Point;
import primitives.Vector;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link lighting.LightSource} sample behavior.
 */
class LightSourceTest {

    /**
     * Test method for {@link lighting.LightSource#getSamples(Point)}.
     */
    @Test
    void testDefaultSingleSample() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Point light exposes its classic light data as one sample
        Point point = new Point(1, 2, 3);
        PointLight pointLight = new PointLight(new Color(100, 80, 60), Point.ZERO);
        List<LightSample> samples = pointLight.getSamples(point);

        assertEquals(1, samples.size(), "Point light should return one default sample");
        assertTrue(samples.getFirst().intensity().isSimilar(pointLight.getIntensity(point), 0),
                "Sample intensity should match getIntensity");
        assertEquals(pointLight.getL(point), samples.getFirst().direction(),
                "Sample direction should match getL");
        assertEquals(pointLight.getDistance(point), samples.getFirst().distance(),
                "Sample distance should match getDistance");

        // TC02: Directional light keeps infinite-distance single-sample behavior
        DirectionalLight directionalLight = new DirectionalLight(new Color(100, 80, 60), new Vector(1, 0, 0));
        samples = directionalLight.getSamples(point);

        assertEquals(1, samples.size(), "Directional light should return one default sample");
        assertTrue(samples.getFirst().intensity().isSimilar(directionalLight.getIntensity(point), 0),
                "Sample intensity should match getIntensity");
        assertEquals(directionalLight.getL(point), samples.getFirst().direction(),
                "Sample direction should match getL");
        assertEquals(Double.POSITIVE_INFINITY, samples.getFirst().distance(),
                "Directional light sample should keep infinite distance");
    }

    /**
     * Test method for {@link lighting.LightSample#LightSample(Color, Vector, double)}.
     */
    @Test
    void testLightSampleValidation() {
        assertThrows(IllegalArgumentException.class,
                () -> new LightSample(null, new Vector(1, 0, 0), 1),
                "Light sample should reject null intensity");

        assertThrows(IllegalArgumentException.class,
                () -> new LightSample(new Color(1, 1, 1), null, 1),
                "Light sample should reject null direction");

        assertThrows(IllegalArgumentException.class,
                () -> new LightSample(new Color(1, 1, 1), new Vector(1, 0, 0), -1),
                "Light sample should reject negative distance");
    }
}
