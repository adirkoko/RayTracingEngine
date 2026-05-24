package renderer;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import primitives.*;
import scene.Scene;

import java.util.MissingResourceException;


/**
 * Unit tests for {@link renderer.Camera} class
 *
 * @author Adir and Meir
 */
class CameraTest {
    /**
     * Camera builder for the tests.
     */
    private final Camera.Builder cameraBuilder = Camera.getBuilder()
            .setLocation(Point.ZERO)
            .setDirection(new Vector(0, 0, -1), new Vector(0, -1, 0))
            .setVpDistance(10)
            .setImageWriter(new ImageWriter("test", 800, 800))
            .setRayTracer(new SimpleRayTracer(new Scene("Test")));

    /**
     * Creates a complete camera builder for configuration validation tests.
     *
     * @return a builder with all required rendering fields configured
     */
    private Camera.Builder createCompleteBuilder() {
        return Camera.getBuilder()
                .setLocation(Point.ZERO)
                .setDirection(new Vector(0, 0, -1), new Vector(0, -1, 0))
                .setVpDistance(10)
                .setVpSize(10, 10)
                .setImageWriter(new ImageWriter("test", 800, 800))
                .setRayTracer(new SimpleRayTracer(new Scene("Test")));
    }

    /**
     * Test method for {@link renderer.Camera#constructRay(int, int, int, int)}.
     */
    @Test
    void testConstructRay() {

        // ============ Equivalence Partitions Tests ==============
        // EP01: 4X4 Inside (1,1)
        Camera camera1 = cameraBuilder.setVpSize(8, 8).build();
        assertEquals(new Ray(Point.ZERO, new Vector(1, -1, -10)),
                camera1.constructRay(4, 4, 1, 1), "EP01: Ray constructed incorrectly for 4x4 grid, pixel (1,1)");

        // =============== Boundary Values Tests ==================
        // BV01: 4X4 Corner (0,0)
        assertEquals(new Ray(Point.ZERO, new Vector(3, -3, -10)),
                camera1.constructRay(4, 4, 0, 0), "BV01: Ray constructed incorrectly for 4x4 grid, corner pixel (0,0)");

        // BV02: 4X4 Side (0,1)
        assertEquals(new Ray(Point.ZERO, new Vector(1, -3, -10)),
                camera1.constructRay(4, 4, 1, 0), "BV02: Ray constructed incorrectly for 4x4 grid, side pixel (0,1)");

        // BV03: 3X3 Center (1,1)
        Camera camera2 = cameraBuilder.setVpSize(6, 6).build();
        assertEquals(new Ray(Point.ZERO, new Vector(0, 0, -10)),
                camera2.constructRay(3, 3, 1, 1), "BV03: Ray constructed incorrectly for 3x3 grid, center pixel (1,1)");

        // BV04: 3X3 Center of Upper Side (0,1)
        assertEquals(new Ray(Point.ZERO, new Vector(0, -2, -10)),
                camera2.constructRay(3, 3, 1, 0), "BV04: Ray constructed incorrectly for 3x3 grid, upper side pixel (0,1)");

        // BV05: 3X3 Center of Left Side (1,0)
        assertEquals(new Ray(Point.ZERO, new Vector(2, 0, -10)),
                camera2.constructRay(3, 3, 0, 1), "BV05: Ray constructed incorrectly for 3x3 grid, left side pixel (1,0)");

        // BV06: 3X3 Corner (0,0)
        assertEquals(new Ray(Point.ZERO, new Vector(2, -2, -10)),
                camera2.constructRay(3, 3, 0, 0), "BV06: Ray constructed incorrectly for 3x3 grid, corner pixel (0,0)");
    }

    /**
     * Test fail-fast validation for anti-aliasing configuration.
     */
    @Test
    void testAntiAliasingConfiguration() {
        assertThrows(IllegalStateException.class,
                () -> Camera.getBuilder().setSampleSize(4).setSampleNum(16),
                "Builder should reject multiple uniform sampling limits");

        assertThrows(IllegalStateException.class,
                () -> Camera.getBuilder().setMaxDepth(3),
                "Builder should reject maxDepth while adaptive sampling is disabled");

        assertThrows(IllegalStateException.class,
                () -> Camera.getBuilder().setAdaptiveSampling(true).setMaxDepth(3).setSampleSize(4),
                "Builder should reject mixing maxDepth with sampleSize");

        assertThrows(IllegalStateException.class,
                () -> Camera.getBuilder().setAdaptiveSampling(true).setMaxDepth(3).setAdaptiveSampling(false),
                "Builder should reject disabling adaptive sampling after maxDepth was configured");

        assertThrows(MissingResourceException.class,
                () -> createCompleteBuilder().setAdaptiveSampling(true).build(),
                "Adaptive sampling should require a recursion limit");

        assertDoesNotThrow(() -> createCompleteBuilder().setSampleSize(7).setAdaptiveSampling(true).build(),
                "Adaptive sampling should accept sampleSize as a recursion limit");

        assertDoesNotThrow(() -> createCompleteBuilder().setAdaptiveSampling(true).setMaxDepth(3).build(),
                "Adaptive sampling should accept maxDepth as a recursion limit");
    }

    /**
     * Test fail-fast validation for depth of field configuration.
     */
    @Test
    void testDepthOfFieldConfiguration() {
        assertDoesNotThrow(() -> createCompleteBuilder().build(),
                "Default camera should keep pinhole behavior without depth of field settings");

        assertDoesNotThrow(() -> createCompleteBuilder().setApertureRadius(0).build(),
                "Zero aperture radius should keep pinhole behavior");

        assertThrows(IllegalArgumentException.class,
                () -> Camera.getBuilder().setApertureRadius(-1),
                "Builder should reject a negative aperture radius");

        assertThrows(IllegalArgumentException.class,
                () -> Camera.getBuilder().setFocalDistance(0),
                "Builder should reject a non-positive focal distance");

        assertThrows(MissingResourceException.class,
                () -> createCompleteBuilder().setApertureRadius(1).build(),
                "Depth of field should require a focal distance");

        assertDoesNotThrow(() -> createCompleteBuilder().setApertureRadius(1).setFocalDistance(20).build(),
                "Depth of field should accept a positive aperture radius and focal distance");
    }

}
