package renderer;

/**
 * Immutable progress event emitted during a render lifecycle.
 *
 * @param renderId           stable identifier for this render run
 * @param stage              current render stage
 * @param completedWork      completed work units in the current stage
 * @param totalWork          total work units in the current stage
 * @param percent            current stage completion percentage
 * @param elapsedMillis      milliseconds since the render run started
 * @param stageElapsedMillis milliseconds since the current stage started
 * @param timestampMillis    event timestamp in epoch milliseconds
 */
public record RenderProgress(
        String renderId,
        RenderStage stage,
        long completedWork,
        long totalWork,
        double percent,
        long elapsedMillis,
        long stageElapsedMillis,
        long timestampMillis) {

    /**
     * Validates progress data.
     */
    public RenderProgress {
        if (renderId == null || renderId.isBlank())
            throw new IllegalArgumentException("Render id cannot be null or blank");
        if (stage == null)
            throw new IllegalArgumentException("Render stage cannot be null");
        if (completedWork < 0)
            throw new IllegalArgumentException("Completed work cannot be negative");
        if (totalWork < 0)
            throw new IllegalArgumentException("Total work cannot be negative");
        if (completedWork > totalWork)
            throw new IllegalArgumentException("Completed work cannot exceed total work");
        if (percent < 0 || percent > 100)
            throw new IllegalArgumentException("Progress percent must be between 0 and 100");
        if (elapsedMillis < 0)
            throw new IllegalArgumentException("Elapsed time cannot be negative");
        if (stageElapsedMillis < 0)
            throw new IllegalArgumentException("Stage elapsed time cannot be negative");
        if (timestampMillis < 0)
            throw new IllegalArgumentException("Timestamp cannot be negative");
    }
}
