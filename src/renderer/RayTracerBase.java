package renderer;

import primitives.Color;
import primitives.Ray;
import scene.Scene;

/**
 * Provides a base for tracing rays through a scene to determine the color at each pixel.
 * This class is intended to be extended by specific ray tracing implementations.
 *
 * @author Adir and Meir
 */
public abstract class RayTracerBase {

    /**
     * The scene to trace rays in.
     */
    protected Scene scene;

    /**
     * Constructs a RayTracerBase with the given scene.
     *
     * @param scene the scene to trace rays in
     */
    public RayTracerBase(Scene scene) {
        this.scene = scene;
    }

    /**
     * Traces a ray through the scene and determines the color at the intersection point.
     *
     * @param ray the ray to trace
     * @return the color at the intersection point of the ray and the scene
     */
    public abstract Color traceRay(Ray ray);
}
