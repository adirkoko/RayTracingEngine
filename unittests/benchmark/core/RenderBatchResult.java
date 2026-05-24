package benchmark.core;

import java.nio.file.Path;
import java.util.List;

/**
 * Batch output summary.
 *
 * @param batchId      unique batch id
 * @param metricsPath  shared SQLite metrics path
 * @param manifestPath JSON manifest path
 * @param runs         completed runs
 */
public record RenderBatchResult(String batchId, Path metricsPath, Path manifestPath, List<RenderRunResult> runs) {
}
