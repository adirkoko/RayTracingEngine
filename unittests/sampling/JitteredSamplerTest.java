package sampling;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link sampling.JitteredSampler} class
 *
 * @author Adir and Meir
 */
class JitteredSamplerTest {

    /**
     * Test method for {@link sampling.JitteredSampler#JitteredSampler(int, double, double)}.
     */
    @Test
    void testConstructor() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Regular case - sample size 4, pixel size 1x1
        JitteredSampler sampler = new JitteredSampler(4, 1.0, 1.0);
        assertEquals(4, sampler.getSampleSize(), "Sample size should be 4");
        assertEquals(16, sampler.getSamples().size(), "There should be 16 jittered samples");

        // Check samples are within bounds
        for (Sample2D sample : sampler.getSamples()) {
            assertTrue(sample.x() >= -0.5 && sample.x() <= 0.5, "X coordinate out of bounds");
            assertTrue(sample.y() >= -0.5 && sample.y() <= 0.5, "Y coordinate out of bounds");
        }

        // TC02: Minimum sample size (1)
        sampler = new JitteredSampler(1, 1.0, 1.0);
        assertEquals(1, sampler.getSampleSize(), "Sample size should be 1");
        assertEquals(1, sampler.getSamples().size(), "There should be 1 jittered sample");

        // Check sample is within bounds
        Sample2D sample = sampler.getSamples().getFirst();
        assertTrue(sample.x() >= -0.5 && sample.x() <= 0.5, "X coordinate out of bounds");
        assertTrue(sample.y() >= -0.5 && sample.y() <= 0.5, "Y coordinate out of bounds");

        // =============== Boundary Values Tests ==================
        // TC11: Sample size of zero
        assertThrows(IllegalArgumentException.class, () ->
                        new JitteredSampler(0, 1.0, 1.0),
                "Expected IllegalArgumentException for sample size of 0");

        // TC12: Negative sample size
        assertThrows(IllegalArgumentException.class, () ->
                        new JitteredSampler(-1, 1.0, 1.0),
                "Expected IllegalArgumentException for negative sample size");

    }

    /**
     * Test method for {@link sampling.JitteredSampler#getSampleSize()}.
     */
    @Test
    void testGetSampleSize() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Sample size retrieval
        assertEquals(4,
                new JitteredSampler(4, 1.0, 1.0).getSampleSize(),
                "Sample size should be 4");
    }

    /**
     * Test method for {@link sampling.JitteredSampler#getSamples()}.
     */
    @Test
    void testGetSamples() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Jittered samples retrieval
        assertEquals(16,
                new JitteredSampler(4, 1.0, 1.0).getSamples().size(),
                "There should be 16 jittered samples");
    }
}
