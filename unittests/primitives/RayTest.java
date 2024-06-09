package primitives;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link primitives.Ray} class
 *
 * @author Adir and Meir
 */
class RayTest {

    /**
     * Test method for {@link primitives.Ray#getPoint(double)}.
     */
    @Test
    void testGetPoint() {
        Ray ray = new Ray(new Point(1, 1, 1), new Vector(1, 0, 0));

        // ============ Equivalence Partitions Tests ==============

        // TC01: Positive distance
        assertEquals(new Point(3, 1, 1), ray.getPoint(2), "Wrong point for positive distance");

        // TC02: Negative distance
        assertEquals(new Point(-1, 1, 1), ray.getPoint(-2), "Wrong point for negative distance");

        // =============== Boundary Values Tests ==================

        // TC03: Zero distance
        assertEquals(new Point(1, 1, 1), ray.getPoint(0), "Wrong point for zero distance");
    }
}
