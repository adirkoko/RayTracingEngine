package benchmark.core;

import primitives.Point;
import primitives.Vector;
import renderer.Camera;

/**
 * Shared camera geometry for all profiles in one batch.
 *
 * @param location     camera location
 * @param toward       forward direction
 * @param up           up direction
 * @param viewDistance view plane distance
 * @param viewWidth    view plane width
 * @param viewHeight   view plane height
 */
public record RenderCameraSpec(
        Point location,
        Vector toward,
        Vector up,
        double viewDistance,
        double viewWidth,
        double viewHeight) {

    /**
     * Validates camera spec data.
     */
    public RenderCameraSpec {
        if (location == null || toward == null || up == null)
            throw new IllegalArgumentException("Camera vectors cannot be null");
        if (viewDistance <= 0 || viewWidth <= 0 || viewHeight <= 0)
            throw new IllegalArgumentException("Camera view parameters must be positive");
    }

    /**
     * Creates a camera builder initialized with the shared camera geometry.
     *
     * @return camera builder
     */
    Camera.Builder newBuilder() {
        return Camera.getBuilder()
                .setLocation(location)
                .setDirection(toward, up)
                .setVpDistance(viewDistance)
                .setVpSize(viewWidth, viewHeight);
    }
}
