package renderer;

import org.junit.jupiter.api.Test;
import primitives.Point;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link renderer.JitteredGrid} class
 *
 * @author Adir and Meir
 */
class JitteredGridTest {

    /**
     * Test method for {@link renderer.JitteredGrid#JitteredGrid(int, double, double)}.
     */
    @Test
    void testConstructor() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Regular case - sample size 4, pixel size 1x1
        JitteredGrid grid = new JitteredGrid(4, 1.0, 1.0);
        assertEquals(4, grid.getSampleSize(), "Sample size should be 4");
        assertEquals(16, grid.getJitteredPoints().size(), "There should be 16 jittered points");

        // Check points are within bounds
        for (Point point : grid.getJitteredPoints()) {
            System.out.printf("X: %6.3f,       Y: %6.3f%n", point.getX(), point.getY());
            assertTrue(point.getX() >= -0.5 && point.getX() <= 0.5, "X coordinate out of bounds");
            assertTrue(point.getY() >= -0.5 && point.getY() <= 0.5, "Y coordinate out of bounds");
        }

        // TC02: Minimum sample size (1)
        grid = new JitteredGrid(1, 1.0, 1.0);
        assertEquals(1, grid.getSampleSize(), "Sample size should be 1");
        assertEquals(1, grid.getJitteredPoints().size(), "There should be 1 jittered point");

        // Check point is within bounds
        Point point = grid.getJitteredPoints().getFirst();
        assertTrue(point.getX() >= -0.5 && point.getX() <= 0.5, "X coordinate out of bounds");
        assertTrue(point.getY() >= -0.5 && point.getY() <= 0.5, "Y coordinate out of bounds");

        // =============== Boundary Values Tests ==================
        // TC11: Sample size of zero
        assertThrows(IllegalArgumentException.class, () ->
                        new JitteredGrid(0, 1.0, 1.0),
                "Expected IllegalArgumentException for sample size of 0");

        // TC12: Negative sample size
        assertThrows(IllegalArgumentException.class, () ->
                        new JitteredGrid(-1, 1.0, 1.0),
                "Expected IllegalArgumentException for negative sample size");

    }

    /**
     * Test method for {@link renderer.JitteredGrid#getSampleSize()}.
     */
    @Test
    void testGetSampleSize() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Sample size retrieval
        assertEquals(4,
                new JitteredGrid(4, 1.0, 1.0).getSampleSize(),
                "Sample size should be 4");
    }

    /**
     * Test method for {@link renderer.JitteredGrid#getJitteredPoints()}.
     */
    @Test
    void testGetJitteredPoints() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Jittered points retrieval
        assertEquals(16,
                new JitteredGrid(4, 1.0, 1.0).getJitteredPoints().size(),
                "There should be 16 jittered points");
    }
}
