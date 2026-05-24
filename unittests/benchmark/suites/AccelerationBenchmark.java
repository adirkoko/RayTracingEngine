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

import java.util.List;

/**
 * Render-batch acceleration benchmark that compares AUTO, LINEAR, BVH, and GRID profiles.
 * This class is intentionally included only in the {@code benchmarks} Maven profile.
 */
class AccelerationBenchmark {

    /**
     * Benchmark image resolution kept modest because this benchmark produces real PNG files.
     */
    private static final int IMAGE_RESOLUTION = 160;

    /**
     * Runs the acceleration profile batch.
     */
    @Test
    void renderAccelerationBatch() {
        for (BenchmarkScene scene : List.of(
                SceneCatalog.smallOverhead(),
                SceneCatalog.uniformBounded(),
                SceneCatalog.clusteredBounded(),
                SceneCatalog.mixedScaleBounded(),
                SceneCatalog.profileComparison())) {
            renderScene(scene);
        }
    }

    /**
     * Runs one scene through the acceleration profile set.
     *
     * @param scene benchmark scene
     */
    private void renderScene(BenchmarkScene scene) {
        RenderBatchResult result = new RenderBatchRunner().run(new RenderBatch(
                "acceleration",
                scene.name(),
                IMAGE_RESOLUTION,
                IMAGE_RESOLUTION,
                1.0,
                BenchmarkOutputLayout.defaultLayout(),
                scene.cameraSpec(),
                scene::createScene,
                ProfileCatalog.accelerationBaselineProfiles()));

        System.out.println();
        System.out.println("Acceleration batch: " + result.batchId());
        System.out.println("Manifest: " + result.manifestPath());
        System.out.println("Metrics: " + result.metricsPath());
        for (RenderRunResult run : result.runs())
            System.out.printf("%-24s %8d ms  %s%n", run.profileName(), run.elapsedMillis(), run.imageName());
    }
}
