package benchmark.scenes;

import benchmark.core.RenderCameraSpec;
import geometries.acceleration.AccelerationType;
import scene.Scene;

/**
 * Benchmark scene definition independent of runner, profile, and output concerns.
 */
public interface BenchmarkScene {

    /**
     * Gets the stable scene identifier.
     *
     * @return scene name
     */
    String name();

    /**
     * Gets the default camera framing for this scene.
     *
     * @return camera spec
     */
    RenderCameraSpec cameraSpec();

    /**
     * Creates a fresh scene configured with the requested acceleration mode.
     *
     * @param accelerationType acceleration mode
     * @return fresh scene
     */
    Scene createScene(AccelerationType accelerationType);
}
