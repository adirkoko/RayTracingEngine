package benchmark;

import geometries.Geometries;
import geometries.Sphere;
import geometries.acceleration.AccelerationType;
import lighting.AmbientLight;
import lighting.PointLight;
import org.junit.jupiter.api.Test;
import primitives.*;
import renderer.Camera;
import renderer.ImageWriter;
import renderer.SimpleRayTracer;
import scene.Scene;

import java.util.ArrayList;
import java.util.List;

/**
 * Basic acceleration benchmarks for comparing LINEAR, BVH, and GRID.
 * These tests print timing data and are intentionally excluded from the default test suite.
 */
class AccelerationBenchmark {

    /**
     * Acceleration modes that are measured explicitly.
     */
    private static final AccelerationType[] MODES = {
            AccelerationType.LINEAR,
            AccelerationType.BVH,
            AccelerationType.GRID
    };

    /**
     * Number of warmup rounds before measuring.
     */
    private static final int WARMUP_ROUNDS = 2;

    /**
     * Number of measured rounds.
     */
    private static final int MEASURE_ROUNDS = 3;

    /**
     * Render benchmark image resolution.
     */
    private static final int RENDER_RESOLUTION = 32;

    /**
     * Compares intersection query time across acceleration modes.
     */
    @Test
    void compareIntersectionQueries() {
        List<Ray> rays = createQueryRays();

        System.out.println();
        System.out.println("Acceleration intersection benchmark");
        System.out.println("Mode      avg ms     ns/query   checksum");

        for (AccelerationType mode : MODES) {
            BenchmarkResult result = measure(() -> runIntersectionQueries(mode, rays));
            System.out.printf("%-8s %8.3f %12.1f %10d%n",
                    mode,
                    result.averageMillis(),
                    result.averageNanos() / rays.size(),
                    result.checksum());
        }
    }

    /**
     * Compares a small deterministic render workload across acceleration modes.
     */
    @Test
    void compareRenderWorkload() {
        System.out.println();
        System.out.println("Acceleration render benchmark");
        System.out.println("Mode      avg ms     ns/pixel   checksum");

        for (AccelerationType mode : MODES) {
            BenchmarkResult result = measure(() -> runRenderWorkload(mode));
            System.out.printf("%-8s %8.3f %12.1f %10d%n",
                    mode,
                    result.averageMillis(),
                    result.averageNanos() / (RENDER_RESOLUTION * RENDER_RESOLUTION),
                    result.checksum());
        }
    }

    /**
     * Measures a benchmark action over warmup and measured rounds.
     *
     * @param action benchmark action
     * @return benchmark result
     */
    private BenchmarkResult measure(BenchmarkAction action) {
        for (int i = 0; i < WARMUP_ROUNDS; i++) action.run();

        long checksum = 0;
        long totalNanos = 0;
        for (int i = 0; i < MEASURE_ROUNDS; i++) {
            long start = System.nanoTime();
            checksum += action.run();
            totalNanos += System.nanoTime() - start;
        }

        return new BenchmarkResult(totalNanos, MEASURE_ROUNDS, checksum);
    }

    /**
     * Runs all/closest intersection queries against a deterministic ray set.
     *
     * @param mode acceleration mode
     * @param rays query rays
     * @return deterministic checksum that prevents dead-code elimination
     */
    private long runIntersectionQueries(AccelerationType mode, List<Ray> rays) {
        Geometries geometries = createBenchmarkGeometries(mode);
        long checksum = 0;

        for (Ray ray : rays) {
            var intersections = geometries.findGeoIntersections(ray);
            if (intersections != null) checksum += intersections.size();

            var closest = geometries.findClosestGeoIntersection(ray);
            if (closest != null) checksum += Math.round(closest.point.distance(ray.getHead()));
        }

        return checksum;
    }

    /**
     * Runs a small camera/ray-tracer workload without writing image files.
     *
     * @param mode acceleration mode
     * @return deterministic checksum that prevents dead-code elimination
     */
    private long runRenderWorkload(AccelerationType mode) {
        Scene scene = createBenchmarkScene(mode);
        SimpleRayTracer rayTracer = new SimpleRayTracer(scene);
        Camera camera = Camera.getBuilder()
                .setLocation(Point.ZERO)
                .setDirection(new Vector(0, 0, -1), new Vector(0, 1, 0))
                .setVpDistance(120)
                .setVpSize(120, 120)
                .setImageWriter(new ImageWriter("benchmark-" + mode, RENDER_RESOLUTION, RENDER_RESOLUTION))
                .setRayTracer(rayTracer)
                .build();

        long checksum = 0;
        for (int i = 0; i < RENDER_RESOLUTION; i++) {
            for (int j = 0; j < RENDER_RESOLUTION; j++) {
                checksum += rayTracer.traceRay(camera.constructRay(RENDER_RESOLUTION, RENDER_RESOLUTION, j, i))
                        .getColor()
                        .getRGB();
            }
        }
        return checksum;
    }

    /**
     * Creates a benchmark scene with bounded geometries and one light.
     *
     * @param mode acceleration mode
     * @return benchmark scene
     */
    private Scene createBenchmarkScene(AccelerationType mode) {
        return new Scene("Acceleration benchmark " + mode)
                .setBackground(new Color(4, 5, 8))
                .setAmbientLight(new AmbientLight(new Color(20, 20, 20), 0.15))
                .setGeometries(createBenchmarkGeometries(mode))
                .setLights(List.of(new PointLight(new Color(450, 350, 250), new Point(-50, 80, 20))
                        .setKl(0.0005)
                        .setKq(0.00002)));
    }

    /**
     * Creates a deterministic bounded geometry collection.
     *
     * @param mode acceleration mode
     * @return benchmark geometries
     */
    private Geometries createBenchmarkGeometries(AccelerationType mode) {
        Geometries geometries = new Geometries().setAcceleration(mode);
        Material material = new Material().setKd(0.45).setKs(0.25).setShininess(80);

        for (int z = 0; z < 5; z++) {
            for (int y = -4; y <= 4; y++) {
                for (int x = -4; x <= 4; x++) {
                    geometries.add(new Sphere(new Point(x * 18, y * 18, -90 - z * 34), 5.5)
                            .setEmission(new Color(12 + z * 8, 18 + x * x, 20 + y * y))
                            .setMaterial(material));
                }
            }
        }

        return geometries;
    }

    /**
     * Creates deterministic camera-like query rays.
     *
     * @return query rays
     */
    private List<Ray> createQueryRays() {
        List<Ray> rays = new ArrayList<>();
        for (int y = -24; y <= 24; y += 2) {
            for (int x = -24; x <= 24; x += 2) {
                rays.add(new Ray(Point.ZERO, new Vector(x * 2.5, y * 2.5, -160)));
            }
        }
        return rays;
    }

    /**
     * Benchmark action.
     */
    @FunctionalInterface
    private interface BenchmarkAction {
        /**
         * Runs one benchmark round.
         *
         * @return checksum
         */
        long run();
    }

    /**
     * Benchmark timing result.
     *
     * @param totalNanos measured total time
     * @param rounds     measured round count
     * @param checksum   accumulated checksum
     */
    private record BenchmarkResult(long totalNanos, int rounds, long checksum) {
        /**
         * Gets average round time in nanoseconds.
         *
         * @return average nanoseconds
         */
        double averageNanos() {
            return (double) totalNanos / rounds;
        }

        /**
         * Gets average round time in milliseconds.
         *
         * @return average milliseconds
         */
        double averageMillis() {
            return averageNanos() / 1_000_000;
        }
    }
}
