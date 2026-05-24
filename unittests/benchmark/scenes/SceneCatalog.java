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

    /*
     * TODO: Add small-overhead scene.
     * Purpose: very small bounded scene with only a few objects.
     * Expected use: measure the fixed build/traversal overhead of LINEAR, BVH,
     * GRID, and AUTO when acceleration may cost more than it saves.
     *
     * TODO: Add uniform bounded distribution scene.
     * Purpose: many similarly sized bounded objects spread evenly through space.
     * Expected use: compare BVH and GRID in the type of scene where both should
     * have enough spatial coherence to help.
     *
     * TODO: Add clustered bounded scene.
     * Purpose: many bounded objects arranged in dense clusters with empty space between them.
     * Expected use: stress GRID cell traversal and compare it against BVH hierarchy traversal.
     *
     * TODO: Add mixed-scale bounded scene.
     * Purpose: objects with very different bounding-box sizes in one scene.
     * Expected use: catch cases where median BVH splitting or automatic GRID resolution
     * handles large and small geometry poorly.
     *
     * TODO: Add unbounded fallback scene.
     * Purpose: include planes and other geometries without finite bounding boxes.
     * Expected use: verify acceleration fallback paths and avoid drawing conclusions
     * only from fully bounded scenes.
     *
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
