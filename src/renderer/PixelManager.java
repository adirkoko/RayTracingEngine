package renderer;

/**
 * PixelManager is a helper class. It is used for multi-threading in the
 * renderer and for tracking pixel-rendering progress.<br/>
 * A Camera uses one pixel manager object and several Pixel objects - one in
 * each thread.
 *
 * @author Dan Zilberstein
 */
class PixelManager {

    /**
     * Immutable class for object containing allocated pixel (with its row and
     * column numbers)
     *
     * @param col The column index of the pixel.
     * @param row The row index of the pixel.
     */
    record Pixel(int col, int row) {
    }

    /**
     * Maximum rows of pixels
     */
    private final int maxRows;
    /**
     * Maximum columns of pixels
     */
    private final int maxCols;
    /**
     * Total amount of pixels in the generated image
     */
    private final long totalPixels;

    /**
     * Currently processed row of pixels
     */
    private volatile int cRow = 0;
    /**
     * Currently processed column of pixels
     */
    private volatile int cCol = -1;
    /**
     * Amount of pixels that have been processed
     */
    private volatile long pixels = 0L;
    /**
     * Last reported progress update percentage
     */
    private volatile int lastReported = 0;

    /**
     * Progress report interval in tenths of a percent.
     */
    private final int progressInterval;

    /**
     * Render progress listener.
     */
    private final RenderProgressListener progressListener;

    /**
     * Render run identifier.
     */
    private final String renderId;

    /**
     * Render run start timestamp.
     */
    private final long renderStartedMillis;

    /**
     * Pixel rendering stage start timestamp.
     */
    private final long stageStartedMillis;
    /**
     * Mutual exclusion object for synchronizing next pixel allocation between
     * threads
     */
    private final Object mutexNext = new Object();
    /**
     * Mutual exclusion object for progress reporting by different threads
     */
    private final Object mutexPixels = new Object();

    /**
     * Initialize pixel manager data for multi-threading with default console progress reporting.
     *
     * @param maxRows  the amount of pixel rows
     * @param maxCols  the amount of pixel columns
     * @param interval progress report interval in percent
     */
    PixelManager(int maxRows, int maxCols, double interval) {
        this(maxRows, maxCols, interval, "render", System.currentTimeMillis(), System.currentTimeMillis(),
                RenderProgressListener.CONSOLE);
    }

    /**
     * Initialize pixel manager data for multi-threading.
     *
     * @param maxRows             the amount of pixel rows
     * @param maxCols             the amount of pixel columns
     * @param interval            progress report interval in percent
     * @param renderId            render run identifier
     * @param renderStartedMillis render run start timestamp
     * @param stageStartedMillis  pixel rendering stage start timestamp
     * @param progressListener    progress listener
     */
    PixelManager(
            int maxRows,
            int maxCols,
            double interval,
            String renderId,
            long renderStartedMillis,
            long stageStartedMillis,
            RenderProgressListener progressListener) {
        if (maxRows <= 0 || maxCols <= 0)
            throw new IllegalArgumentException("Pixel dimensions must be positive");
        if (interval < 0)
            throw new IllegalArgumentException("Progress interval cannot be negative");
        if (progressListener == null)
            throw new IllegalArgumentException("Progress listener cannot be null");

        this.maxRows = maxRows;
        this.maxCols = maxCols;
        totalPixels = (long) maxRows * maxCols;
        progressInterval = interval == 0 ? 0 : Math.max(1, (int) Math.round(interval * 10));
        this.renderId = renderId;
        this.renderStartedMillis = renderStartedMillis;
        this.stageStartedMillis = stageStartedMillis;
        this.progressListener = progressListener;
        report(0, 0);
    }

    /**
     * Function for thread-safe manipulating of main follow up Pixel object - this
     * function is critical section for all the threads, and the pixel manager data
     * is the shared data of this critical section.<br/>
     * The function provides next available pixel number each call.
     *
     * @return true if next pixel is allocated, false if there are no more pixels
     */
    Pixel nextPixel() {
        synchronized (mutexNext) {
            if (cRow == maxRows) return null;

            ++cCol;
            if (cCol < maxCols)
                return new Pixel(cRow, cCol);

            cCol = 0;
            ++cRow;
            if (cRow < maxRows)
                return new Pixel(cRow, cCol);
        }
        return null;
    }

    /**
     * Marks one pixel as completed and reports progress when the configured interval is reached.
     */
    void pixelDone() {
        synchronized (mutexPixels) {
            ++pixels;
            int percentage = (int) (1000L * pixels / totalPixels);
            if (pixels == totalPixels || progressInterval > 0 && percentage - lastReported >= progressInterval) {
                lastReported = percentage;
                report(pixels, percentage / 10.0);
            }
        }
    }

    /**
     * Reports final pixel rendering progress when it was not emitted by the last pixel update.
     */
    void finish() {
        synchronized (mutexPixels) {
            if (lastReported < 1000) {
                lastReported = 1000;
                report(totalPixels, 100);
            }
        }
    }

    /**
     * Reports pixel rendering progress.
     *
     * @param completedPixels completed pixels
     * @param percent         progress percentage
     */
    private void report(long completedPixels, double percent) {
        long now = System.currentTimeMillis();
        progressListener.onProgress(new RenderProgress(
                renderId,
                RenderStage.RENDER_PIXELS,
                completedPixels,
                totalPixels,
                percent,
                now - renderStartedMillis,
                now - stageStartedMillis,
                now));
    }
}
