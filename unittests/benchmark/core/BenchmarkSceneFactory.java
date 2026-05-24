package benchmark.core;

import geometries.acceleration.AccelerationType;
import scene.Scene;

/**
 * Creates a fresh benchmark scene for one acceleration mode.
 */
@FunctionalInterface
public interface BenchmarkSceneFactory {
    /**
     * Creates the scene.
     *
     * @param accelerationType acceleration mode for the scene geometry collection
     * @return fresh scene
     */
    Scene createScene(AccelerationType accelerationType);
}
