package primitives;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link primitives.Vector} class
 *
 * @author Adir and Meir
 */
class VectorTest {

    /**
     * Delta value for accuracy when comparing the numbers of type 'double' in assertEquals.
     */
    private final double DELTA = 0.000001;

    /**
     * Test method for {@link primitives.Vector#add(primitives.Vector)}.
     */
    @Test
    void add() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Checks whether the result of the sum is the correct result.
        assertEquals(new Vector(4, 4, 4), new Vector(3, 2, 1).add(new Vector(1, 2, 3)), "add() wrong result");

        // =============== Boundary Values Tests ==================
        // TC11: Checks whether adding two vectors in different directions, throws an exception because zero vectors are not allowed.
        assertThrows(IllegalArgumentException.class, () -> new Vector(1, 2, 3).add(new Vector(-1, -2, -3)),
                "add() adding two vectors in different directions, does not throw an exception");
    }

    /**
     * Test method for {@link primitives.Vector#subtract(primitives.Point)}.
     */
    @Test
    void testSubtract() {
        Vector v1 = new Vector(1, 2, 3);
        // ============ Equivalence Partitions Tests ==============
        // TC01: Checks whether the result of the subtraction is the correct result.
        assertEquals(v1.subtract(new Vector(-2, -4, -6)), new Vector(3, 6, 9), "subtract() wrong result");

        // =============== Boundary Values Tests ==================
        // TC11 Checks whether subtraction of a vector from itself, throws an exception because zero vectors are not allowed.
        assertThrows(IllegalArgumentException.class, () -> v1.subtract(v1),
                "subtract() subtraction of a vector from itself, does not throw an exception");
    }

    /**
     * Test method for {@link primitives.Vector#scale(double)}.
     */
    @Test
    void scale() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Checks if the result of vector multiplication is the correct result.
        assertEquals(new Vector(-2, -4, 6), //
                new Vector(1, 2, -3).scale(-2), //
                "scale() wrong result");

        // =============== Boundary Values Tests ==================
        // TC11: Checks whether multiplies a vector by a zero scalar, throws an exception because zero vectors are not allowed.
        assertThrows(IllegalArgumentException.class, () -> new Vector(1, 2, -3).scale(0),//
                "scale() multiplies a vector by a zero scalar, does not throw an exception");
    }

    /**
     * Test method for {@link primitives.Vector#dotProduct(primitives.Vector)}.
     */
    @Test
    void dotProduct() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Checks a scalar product between two vectors when the result is positive.
        assertEquals(32d, new Vector(1, 2, -3).dotProduct(new Vector(4, 5, -6)), DELTA, "dotProduct() wrong result");
        // TC02: Checks a scalar product between two vectors when the result is negative.
        assertEquals(-4d, new Vector(1, 2, -3).dotProduct(new Vector(4, 5, 6)), DELTA, "dotProduct() wrong result");

        // =============== Boundary Values Tests ==================
        // TC11: Checks a scalar product between two orthogonal vectors.
        assertEquals(0d, new Vector(1, 0, 0).dotProduct(new Vector(0, 1, 0)), DELTA, "dotProduct() wrong result for orthogonal vectors");
    }

    /**
     * Test method for {@link primitives.Vector#crossProduct(primitives.Vector)}.
     */
    @Test
    void crossProduct() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Checks if a cross product between two vectors yields the correct result.
        assertEquals(new Vector(-3, 6, -3), new Vector(1, 2, 3).crossProduct(new Vector(4, 5, 6)), "crossProduct() wrong result");
        // TC02: Checks if a cross product between two vectors yields the correct result.
        assertEquals(new Vector(0, 0, 1), new Vector(1, 0, 0).crossProduct(new Vector(0, 1, 0)), "crossProduct() wrong result");

        // =============== Boundary Values Tests ==================
        // TC11: Checks if cross product between two parallel vectors, throws an exception because zero vectors are not allowed.
        assertThrows(IllegalArgumentException.class, () -> new Vector(1, 2, 3).crossProduct(new Vector(2, 4, 6)),//
                "crossProduct() cross product between two parallel vectors, does not throw an exception");
        // =============== Boundary Values Tests ==================
        // TC12: Checks if cross product between two vectors where one of them is the zero vector, throws an exception because zero vectors are not allowed.
        assertThrows(IllegalArgumentException.class, () -> new Vector(0, 0, 0).crossProduct(new Vector(1, 2, 3)),//
                "crossProduct() cross product between two vectors where one of them is the zero vector, does not throw an exception");
    }

    /**
     * Test method for {@link primitives.Vector#lengthSquared()}.
     */
    @Test
    void lengthSquared() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Checks if calculating the squared length of the vector yields the correct result.
        assertEquals(14d, new Vector(1, 2, 3).lengthSquared(), DELTA, "lengthSquared() wrong result");
    }

    /**
     * Test method for {@link primitives.Vector#length()}.
     */
    @Test
    void length() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Checks if calculating the length of the vector yields the correct result.
        assertEquals(5d, new Vector(0, 3, 4).length(), DELTA, "length() wrong result");
    }

    /**
     * Test method for {@link primitives.Vector#normalize()}.
     */
    @Test
    void normalize() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Checks if the result of vector normalization is the correct result.
        assertEquals(new Vector(0.6d, 0.8d, 0d), new Vector(3, 4, 0).normalize(), "normalize() wrong result");

        // =============== Boundary Values Tests ==================
        // TC11: Checks whether normalize the zero vector, throws an exception.
        assertThrows(IllegalArgumentException.class, () -> new Vector(0, 0, 0).normalize(), //
                "normalize() normalize the zero vector, does not throw an exception");
    }
}