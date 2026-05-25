package sampling;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link sampling.DiskSampler} class.
 */
class DiskSamplerTest {

    /**
     * Test method for {@link sampling.DiskSampler#DiskSampler(int, double)}.
     */
    @Test
    void testConstructor() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Regular case - sample size 4, radius 2
        DiskSampler sampler = new DiskSampler(4, 2.0);
        assertEquals(4, sampler.getSampleSize(), "Sample size should be 4");
        assertEquals(2.0, sampler.getRadius(), "Radius should be 2");
        assertEquals(16, sampler.getSamples().size(), "There should be 16 disk samples");

        for (Sample2D sample : sampler.getSamples())
            assertTrue(sample.x() * sample.x() + sample.y() * sample.y() <= 4.0,
                    "Sample should be inside the disk");

        // =============== Boundary Values Tests ==================
        // TC11: Sample size of zero
        assertThrows(IllegalArgumentException.class, () -> new DiskSampler(0, 1.0),
                "Expected IllegalArgumentException for sample size of 0");

        // TC12: Negative sample size
        assertThrows(IllegalArgumentException.class, () -> new DiskSampler(-1, 1.0),
                "Expected IllegalArgumentException for negative sample size");

        // TC13: Radius of zero
        assertThrows(IllegalArgumentException.class, () -> new DiskSampler(1, 0),
                "Expected IllegalArgumentException for radius of 0");

        // TC14: Negative radius
        assertThrows(IllegalArgumentException.class, () -> new DiskSampler(1, -1),
                "Expected IllegalArgumentException for negative radius");
    }

    /**
     * Test sample generation is deterministic for benchmark reproducibility.
     */
    @Test
    void testDeterministicSamples() {
        assertEquals(
                new DiskSampler(4, 2.0).getSamples(),
                new DiskSampler(4, 2.0).getSamples(),
                "Disk sampling should be reproducible for identical configuration");
    }
}
