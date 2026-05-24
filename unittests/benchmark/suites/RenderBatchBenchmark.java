package benchmark.suites;

import benchmark.core.BenchmarkOutputLayout;
import benchmark.core.RenderBatch;
import benchmark.core.RenderBatchResult;
import benchmark.core.RenderBatchRunner;
import benchmark.core.RenderRunResult;
import benchmark.profiles.ProfileCatalog;
import benchmark.scenes.BenchmarkScene;
import benchmark.scenes.SceneCatalog;
import org.junit.jupiter.api.Test;

/**
 * End-to-end render batch benchmark for comparing output quality and render timing.
 * This class is intentionally included only in the {@code benchmarks} Maven profile.
 */
class RenderBatchBenchmark {

    /**
     * Benchmark image resolution kept modest because this benchmark produces real PNG files.
     */
    private static final int IMAGE_RESOLUTION = 160;

    /**
     * Runs one deterministic scene with several render profiles and writes images, metrics, and a manifest.
     */
    @Test
    void renderProfileBatch() {
        BenchmarkScene scene = SceneCatalog.profileComparison();
        RenderBatchResult result = new RenderBatchRunner().run(new RenderBatch(
                "image-quality",
                scene.name(),
                IMAGE_RESOLUTION,
                IMAGE_RESOLUTION,
                1.0,
                BenchmarkOutputLayout.defaultLayout(),
                scene.cameraSpec(),
                scene::createScene,
                ProfileCatalog.imageQualitySmokeProfiles()));

        System.out.println();
        System.out.println("Render batch: " + result.batchId());
        System.out.println("Manifest: " + result.manifestPath());
        System.out.println("Metrics: " + result.metricsPath());
        for (RenderRunResult run : result.runs())
            System.out.printf("%-24s %8d ms  %s%n", run.profileName(), run.elapsedMillis(), run.imageName());
    }
}
