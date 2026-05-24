package benchmark.scenes;

/**
 * Catalog of benchmark scene definitions.
 */
public final class SceneCatalog {

    /**
     * Private constructor to prevent utility class instantiation.
     */
    private SceneCatalog() {
    }

    /**
     * Gets the initial profile-comparison scene.
     *
     * @return benchmark scene
     */
    public static BenchmarkScene profileComparison() {
        return new ProfileComparisonScene();
    }

    /**
     * Gets a very small bounded scene for acceleration-overhead measurements.
     *
     * @return benchmark scene
     */
    public static BenchmarkScene smallOverhead() {
        return new SmallOverheadScene();
    }

    /**
     * Gets a uniformly distributed bounded scene for BVH/GRID comparison.
     *
     * @return benchmark scene
     */
    public static BenchmarkScene uniformBounded() {
        return new UniformBoundedScene();
    }

    /**
     * Gets a clustered bounded scene for uneven spatial-distribution measurements.
     *
     * @return benchmark scene
     */
    public static BenchmarkScene clusteredBounded() {
        return new ClusteredBoundedScene();
    }

    /**
     * Gets a mixed-scale bounded scene for comparing acceleration on uneven box sizes.
     *
     * @return benchmark scene
     */
    public static BenchmarkScene mixedScaleBounded() {
        return new MixedScaleBoundedScene();
    }

    /**
     * Gets a scene that combines bounded objects with unbounded planes.
     *
     * @return benchmark scene
     */
    public static BenchmarkScene unboundedFallback() {
        return new UnboundedFallbackScene();
    }

    /*
     * TODO: Add reflection/transparency recursion scene.
     * Purpose: reflective and transparent materials with controlled globalSamples values.
     * Expected use: measure recursion/global-effect cost and confirm acceleration still
     * helps when intersection work is multiplied by reflection and transparency rays.
     *
     * TODO: Add shadow-heavy light-sampling scene.
     * Purpose: multiple occluders and lights that can return several LightSample values.
     * Expected use: measure shadow ray behavior, soft-shadow sampling cost, and acceleration
     * impact on transparency-aware shadow checks.
     */
}
