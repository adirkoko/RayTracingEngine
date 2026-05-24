package geometries.acceleration;

/**
 * Geometry acceleration mode used for performance benchmarking and debugging.
 */
public enum AccelerationType {
    /**
     * Chooses an internal acceleration strategy automatically.
     */
    AUTO,

    /**
     * Uses bounding volume hierarchy traversal for bounded geometry collections.
     */
    BVH,

    /**
     * Checks geometries linearly without spatial acceleration.
     */
    LINEAR
}
