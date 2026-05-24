package geometries;

import geometries.acceleration.BoundingBox;
import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link geometries.acceleration.BoundingBox}.
 */
class BoundingBoxTest {

    /**
     * Test method for {@link geometries.acceleration.BoundingBox#intersects(Ray, double)}.
     */
    @Test
    void testIntersects() {
        BoundingBox box = new BoundingBox(new Point(-1, -1, -1), new Point(1, 1, 1));

        // ============ Equivalence Partitions Tests ==============
        assertTrue(box.intersects(new Ray(new Point(0, 0, -3), new Vector(0, 0, 1)), 5),
                "Ray should intersect the box");

        assertFalse(box.intersects(new Ray(new Point(2, 0, -3), new Vector(0, 0, 1)), 5),
                "Ray outside the x slab should miss the box");

        // =============== Boundary Values Tests ==================
        assertTrue(box.intersects(new Ray(new Point(0, 0, 0), new Vector(1, 0, 0)), 5),
                "Ray starting inside the box should intersect it");

        assertFalse(box.intersects(new Ray(new Point(0, 0, -3), new Vector(0, 0, 1)), 1),
                "Ray should miss when the box is beyond max distance");
    }

    /**
     * Test method for {@link geometries.acceleration.BoundingBox#union(BoundingBox)}.
     */
    @Test
    void testUnion() {
        BoundingBox box = new BoundingBox(new Point(-1, -2, -3), new Point(1, 2, 3))
                .union(new BoundingBox(new Point(-2, -1, -4), new Point(2, 1, 4)));

        assertEquals(new Point(-2, -2, -4), box.getMin(), "Wrong union minimum corner");
        assertEquals(new Point(2, 2, 4), box.getMax(), "Wrong union maximum corner");
    }
}
