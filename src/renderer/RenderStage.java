package renderer;

/**
 * High-level stage in a render lifecycle.
 */
public enum RenderStage {
    /**
     * Pixels are being traced and written into the image buffer.
     */
    RENDER_PIXELS,

    /**
     * The image buffer is being written to disk.
     */
    WRITE_IMAGE,

    /**
     * The render lifecycle completed successfully.
     */
    DONE,

    /**
     * The render lifecycle failed.
     */
    FAILED
}
