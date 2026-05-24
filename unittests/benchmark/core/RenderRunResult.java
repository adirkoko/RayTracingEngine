package benchmark.core;

/**
 * Single profile output summary.
 *
 * @param profileName   profile name
 * @param renderId      render id
 * @param imageName     image name relative to the default image output directory
 * @param metricsPath   metrics database path
 * @param status        run status
 * @param elapsedMillis wall-clock elapsed time
 * @param error         failure message, or null on success
 */
public record RenderRunResult(
        String profileName,
        String renderId,
        String imageName,
        String metricsPath,
        String status,
        long elapsedMillis,
        String error) {
}
