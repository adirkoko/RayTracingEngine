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

    /**
     * Gets a bounded scene with controlled reflection/transparency recursion.
     *
     * @return benchmark scene
     */
    public static BenchmarkScene reflectionTransparency() {
        return new ReflectionTransparencyScene();
    }

    /**
     * Gets a shadow-heavy scene with sampled lights and transparent blockers.
     *
     * @return benchmark scene
     */
    public static BenchmarkScene shadowHeavy() {
        return new ShadowHeavyScene();
    }

    /**
     * Gets an image-quality scene for grounded objects and sampled soft shadows.
     *
     * @return benchmark scene
     */
    public static BenchmarkScene groundedSoftShadow() {
        return new GroundedSoftShadowScene();
    }

    /**
     * Gets an image-quality scene for anti-aliasing, adaptive sampling, and depth of field.
     *
     * @return benchmark scene
     */
    public static BenchmarkScene samplingAndFocus() {
        return new SamplingAndFocusScene();
    }

    /**
     * Gets an image-quality scene for reflection, glossy reflection, transparency, and diffused glass.
     *
     * @return benchmark scene
     */
    public static BenchmarkScene globalMaterials() {
        return new GlobalMaterialsScene();
    }
}
