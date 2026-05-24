package renderer;

import static org.junit.jupiter.api.Assertions.*;

import metrics.RenderProgressWriters;
import org.junit.jupiter.api.Test;
import primitives.*;
import scene.Scene;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.util.LinkedList;
import java.util.List;
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
     * Test render progress listener configuration.
     */
    @Test
    void testProgressConfiguration() {
        assertThrows(IllegalArgumentException.class,
                () -> Camera.getBuilder().setProgressListener(null),
                "Builder should reject a null progress listener");

        assertThrows(IllegalArgumentException.class,
                () -> Camera.getBuilder().setProgressIntervalPercent(-0.1),
                "Builder should reject a negative progress interval");

        assertDoesNotThrow(() -> createCompleteBuilder().setProgressListener(RenderProgressListener.NONE).build(),
                "Builder should allow replacing default progress printing with a silent listener");
    }

    /**
     * Test fail-fast validation for render progress events.
     */
    @Test
    void testRenderProgressValidation() {
        assertDoesNotThrow(() -> new RenderProgress(
                        "render",
                        RenderStage.RENDER_PIXELS,
                        1,
                        2,
                        50,
                        10,
                        5,
                        100),
                "Valid render progress event should be accepted");

        assertThrows(IllegalArgumentException.class,
                () -> new RenderProgress("", RenderStage.RENDER_PIXELS, 0, 1, 0, 0, 0, 0),
                "Render progress should reject a blank render id");

        assertThrows(IllegalArgumentException.class,
                () -> new RenderProgress("render", RenderStage.RENDER_PIXELS, 2, 1, 100, 0, 0, 0),
                "Render progress should reject completed work beyond total work");

        assertThrows(IllegalArgumentException.class,
                () -> new RenderProgress("render", RenderStage.RENDER_PIXELS, 0, 1, 101, 0, 0, 0),
                "Render progress should reject invalid percentages");
    }

    /**
     * Test render progress events emitted by camera rendering and image writing.
     */
    @Test
    void testRenderProgressEvents() {
        List<RenderProgress> events = new LinkedList<>();

        createCompleteBuilder()
                .setImageWriter(new ImageWriter("progress-listener-test", 2, 2))
                .setProgressListener(events::add)
                .setProgressIntervalPercent(50)
                .build()
                .renderImage()
                .writeToImage();

        assertFalse(events.isEmpty(), "Render should emit progress events");
        assertEquals(RenderStage.RENDER_PIXELS, events.getFirst().stage(),
                "First event should start pixel rendering");
        assertEquals(0, events.getFirst().completedWork(),
                "First pixel rendering event should start at zero completed work");
        assertTrue(events.stream().anyMatch(progress ->
                        progress.stage() == RenderStage.RENDER_PIXELS && progress.completedWork() == progress.totalWork()),
                "Render should report completed pixel rendering");
        assertTrue(events.stream().anyMatch(progress -> progress.stage() == RenderStage.WRITE_IMAGE),
                "Render should report image writing");
        assertEquals(RenderStage.DONE, events.getLast().stage(),
                "Last event should report successful completion");

        String renderId = events.getFirst().renderId();
        assertTrue(events.stream().allMatch(progress -> progress.renderId().equals(renderId)),
                "All progress events in one render should share a render id");
    }

    /**
     * Test progress interval avoids per-pixel event emission.
     */
    @Test
    void testProgressIntervalAvoidsPerPixelEvents() {
        List<RenderProgress> events = new LinkedList<>();

        createCompleteBuilder()
                .setImageWriter(new ImageWriter("progress-interval-test", 10, 10))
                .setProgressListener(events::add)
                .setProgressIntervalPercent(50)
                .build()
                .renderImage();

        assertEquals(3, events.stream()
                        .filter(progress -> progress.stage() == RenderStage.RENDER_PIXELS)
                        .count(),
                "Pixel rendering should report start, interval, and finish rather than every pixel");
    }

    /**
     * Test CSV render progress persistence.
     *
     * @throws Exception if temporary file or render output handling fails
     */
    @Test
    void testCsvRenderProgressWriter() throws Exception {
        Path csvPath = Files.createTempDirectory("render-progress-csv").resolve("progress.csv");

        createCompleteBuilder()
                .setImageWriter(new ImageWriter("csv-progress-listener-test", 2, 2))
                .setProgressListener(RenderProgressWriters.csv(csvPath))
                .setProgressIntervalPercent(50)
                .build()
                .renderImage()
                .writeToImage();

        List<String> rows = Files.readAllLines(csvPath);
        assertTrue(rows.size() > 1, "CSV progress writer should write a header and progress rows");
        assertEquals("render_id,stage,completed_work,total_work,percent,elapsed_millis,stage_elapsed_millis,timestamp_millis",
                rows.getFirst(), "CSV progress writer should write the expected header");
        assertTrue(rows.stream().anyMatch(row -> row.contains(",RENDER_PIXELS,")),
                "CSV progress writer should write pixel-rendering rows");
        assertTrue(rows.stream().anyMatch(row -> row.contains(",DONE,")),
                "CSV progress writer should write a done row");
    }

    /**
     * Test SQLite render progress persistence.
     *
     * @throws Exception if temporary database or JDBC handling fails
     */
    @Test
    void testSqliteRenderProgressWriter() throws Exception {
        Path sqlitePath = Files.createTempDirectory("render-progress-sqlite").resolve("progress.sqlite");
        RenderProgressListener sqliteWriter = RenderProgressWriters.sqlite(sqlitePath);

        createCompleteBuilder()
                .setImageWriter(new ImageWriter("sqlite-progress-listener-test", 2, 2))
                .setProgressListener(sqliteWriter)
                .setProgressIntervalPercent(50)
                .build()
                .renderImage()
                .writeToImage();

        createCompleteBuilder()
                .setImageWriter(new ImageWriter("sqlite-progress-listener-test-second", 2, 2))
                .setProgressListener(sqliteWriter)
                .setProgressIntervalPercent(50)
                .build()
                .renderImage()
                .writeToImage();

        try (var connection = DriverManager.getConnection("jdbc:sqlite:" + sqlitePath.toAbsolutePath());
             var eventStatement = connection.createStatement();
             var eventResult = eventStatement.executeQuery("SELECT COUNT(*) FROM render_progress_events")) {
            assertTrue(eventResult.next(), "SQLite progress database should contain event rows");
            assertTrue(eventResult.getInt(1) > 0, "SQLite progress database should contain progress events");
        }

        try (var connection = DriverManager.getConnection("jdbc:sqlite:" + sqlitePath.toAbsolutePath());
             var runStatement = connection.createStatement();
             var runResult = runStatement.executeQuery("SELECT COUNT(*), MIN(status), MAX(status) FROM render_runs")) {
            assertTrue(runResult.next(), "SQLite progress database should contain a render-run row");
            assertEquals(2, runResult.getInt(1), "SQLite should keep one summary row per render id");
            assertEquals("DONE", runResult.getString(2), "SQLite render-run summaries should mark completed renders");
            assertEquals("DONE", runResult.getString(3), "SQLite render-run summaries should mark completed renders");
        }
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

    /**
     * Test default depth of field settings preserve pinhole ray construction.
     */
    @Test
    void testDepthOfFieldDefaultsPreservePinholeBehavior() {
        Camera defaultCamera = createCompleteBuilder().build();
        Camera zeroApertureCamera = createCompleteBuilder()
                .setApertureRadius(0)
                .build();

        assertEquals(defaultCamera.constructRay(5, 5, 2, 2),
                zeroApertureCamera.constructRay(5, 5, 2, 2),
                "Zero aperture should preserve default pinhole ray construction");
    }

}
