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
     * Uses regular voxel grid traversal for bounded geometry collections.
     */
    GRID,

    /**
     * Checks geometries linearly without spatial acceleration.
     */
    LINEAR
}
