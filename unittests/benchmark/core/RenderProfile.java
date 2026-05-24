package benchmark.core;

import geometries.acceleration.AccelerationType;
import renderer.Camera;

/**
 * Describes one external render configuration for benchmark batches.
 *
 * @param name             stable profile name used in render ids and output names
 * @param threadsCount     render thread count; zero means single-threaded
 * @param sampleSize       anti-aliasing sample grid side length
 * @param adaptiveSampling true to enable adaptive pixel sampling
 * @param apertureRadius   depth-of-field aperture radius; zero disables depth of field
 * @param focalDistance    depth-of-field focal distance, required when aperture is positive
 * @param accelerationType geometry acceleration mode for this profile
 */
public record RenderProfile(
        String name,
        int threadsCount,
        int sampleSize,
        boolean adaptiveSampling,
        double apertureRadius,
        double focalDistance,
        AccelerationType accelerationType) {

    /**
     * Validates profile data.
     */
    public RenderProfile {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Profile name cannot be null or blank");
        if (threadsCount < 0)
            throw new IllegalArgumentException("Thread count cannot be negative");
        if (sampleSize <= 0)
            throw new IllegalArgumentException("Sample size must be positive");
        if (apertureRadius < 0)
            throw new IllegalArgumentException("Aperture radius cannot be negative");
        if (apertureRadius > 0 && focalDistance <= 0)
            throw new IllegalArgumentException("Focal distance must be positive when aperture is enabled");
        if (accelerationType == null)
            throw new IllegalArgumentException("Acceleration type cannot be null");
    }

    /**
     * Applies this profile to a camera builder.
     *
     * @param builder camera builder
     */
    void applyTo(Camera.Builder builder) {
        if (threadsCount > 0) builder.setThreadsCount(threadsCount);
        if (sampleSize > 1 || adaptiveSampling) builder.setSampleSize(sampleSize);
        if (adaptiveSampling) builder.setAdaptiveSampling(true);
        if (apertureRadius > 0) {
            builder.setApertureRadius(apertureRadius);
            builder.setFocalDistance(focalDistance);
        }
    }
}
