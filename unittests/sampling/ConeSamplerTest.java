package sampling;

import org.junit.jupiter.api.Test;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link sampling.ConeSampler}.
 */
class ConeSamplerTest {

    /**
     * Test method for {@link sampling.ConeSampler#ConeSampler(Vector, double, int)}.
     */
    @Test
    void testConstructor() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Regular cone sample generation
        ConeSampler sampler = new ConeSampler(new Vector(0, 0, 1), 0.25, 8);

        assertEquals(new Vector(0, 0, 1), sampler.getDirection(), "Cone direction should be normalized");
        assertEquals(0.25, sampler.getRadius(), "Cone radius should be stored");
        assertEquals(8, sampler.getSampleCount(), "Sample count should be stored");
        assertEquals(8, sampler.getSamples().size(), "Cone sampler should generate the requested number of samples");

        for (Vector sample : sampler.getSamples())
            assertEquals(1, sample.length(), 1e-10, "Cone sample should be normalized");

        // =============== Boundary Values Tests ==================
        // TC11: Null direction
        assertThrows(IllegalArgumentException.class, () -> new ConeSampler(null, 0.25, 8),
                "Expected IllegalArgumentException for null direction");

        // TC12: Radius of zero
        assertThrows(IllegalArgumentException.class, () -> new ConeSampler(new Vector(0, 0, 1), 0, 8),
                "Expected IllegalArgumentException for radius of 0");

        // TC13: Sample count of zero
        assertThrows(IllegalArgumentException.class, () -> new ConeSampler(new Vector(0, 0, 1), 0.25, 0),
                "Expected IllegalArgumentException for sample count of 0");
    }
}
