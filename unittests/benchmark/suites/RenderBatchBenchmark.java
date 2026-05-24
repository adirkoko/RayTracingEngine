package benchmark.suites;

import benchmark.core.BenchmarkOutputLayout;
import benchmark.core.RenderBatch;
import benchmark.core.RenderBatchResult;
import benchmark.core.RenderBatchRunner;
import benchmark.core.RenderProfile;
import benchmark.core.RenderRunResult;
import benchmark.profiles.ProfileCatalog;
import benchmark.scenes.BenchmarkScene;
import benchmark.scenes.SceneCatalog;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * End-to-end render batch benchmark for comparing output quality and render timing.
 * This class is intentionally included only in the {@code benchmarks} Maven profile.
 */
class RenderBatchBenchmark {

    /**
     * Benchmark image resolution kept high enough for visual comparison without turning
     * representative multi-scene runs into full final renders.
     */
    private static final int IMAGE_RESOLUTION = 720;

    /**
     * Runs deterministic scenes with image-quality profiles and writes images, metrics, and manifests.
     */
    @Test
    void renderProfileBatch() {
        List<RenderProfile> profiles = ProfileCatalog.imageQualityComparisonProfiles();

        for (BenchmarkScene scene : List.of(
                SceneCatalog.groundedSoftShadow(),
                SceneCatalog.samplingAndFocus(),
                SceneCatalog.globalMaterials()))
            renderScene(scene, profiles);
    }

    /**
     * Renders one scene/profile batch.
     *
     * @param scene    benchmark scene
     * @param profiles render profiles
     */
    private void renderScene(BenchmarkScene scene, List<RenderProfile> profiles) {
        RenderBatchResult result = new RenderBatchRunner().run(new RenderBatch(
                "image-quality",
                scene.name(),
                IMAGE_RESOLUTION,
                IMAGE_RESOLUTION,
                1.0,
                BenchmarkOutputLayout.defaultLayout(),
                scene.cameraSpec(),
                scene::createScene,
                profiles));

        System.out.println();
        System.out.println("Render batch: " + result.batchId());
        System.out.println("Manifest: " + result.manifestPath());
        System.out.println("Metrics: " + result.metricsPath());
        for (RenderRunResult run : result.runs())
            System.out.printf("%-24s %8d ms  %s%n", run.profileName(), run.elapsedMillis(), run.imageName());
    }
}
