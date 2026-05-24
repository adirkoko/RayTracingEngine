package benchmark.core;

import java.nio.file.Path;

/**
 * Defines where benchmark images and history files are written.
 */
public record BenchmarkOutputLayout(String imageRootName, Path historyRoot) {

    /**
     * Validates output layout data.
     */
    public BenchmarkOutputLayout {
        if (imageRootName == null || imageRootName.isBlank())
            throw new IllegalArgumentException("Image root name cannot be null or blank");
        if (historyRoot == null)
            throw new IllegalArgumentException("History root cannot be null");
    }

    /**
     * Creates the default benchmark output layout.
     *
     * @return default output layout
     */
    public static BenchmarkOutputLayout defaultLayout() {
        return new BenchmarkOutputLayout("benchmark", Path.of("render-history", "benchmark"));
    }

    /**
     * Resolves the image name used by {@code ImageWriter}, without the PNG extension.
     *
     * @param suiteName suite name
     * @param sceneName scene name
     * @param batchId   unique batch id
     * @param profile   render profile
     * @return image name relative to the default image output directory
     */
    String imageName(String suiteName, String sceneName, String batchId, RenderProfile profile) {
        return Path.of(imageRootName, suiteName, sceneName, batchId, profile.name()).toString();
    }

    /**
     * Resolves the history directory for a benchmark batch.
     *
     * @param suiteName suite name
     * @param sceneName scene name
     * @param batchId   unique batch id
     * @return history directory
     */
    Path historyDirectory(String suiteName, String sceneName, String batchId) {
        return historyRoot.resolve(suiteName).resolve(sceneName).resolve(batchId);
    }
}
