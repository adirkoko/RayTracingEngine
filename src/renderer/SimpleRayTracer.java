package renderer;

import primitives.Color;
import primitives.Point;
import primitives.Ray;
import scene.Scene;

/**
 * SimpleRayTracer class that extends RayTracerBase.
 *
 * @author Adir and Meir
 */
public class SimpleRayTracer extends RayTracerBase {

    /**
     * Constructor for SimpleRayTracer.
     *
     * @param scene the scene to trace rays in
     */
    public SimpleRayTracer(Scene scene) {
        super(scene);
    }

    @Override
    public Color traceRay(Ray ray) {
        var intersection = ray.findClosestPoint(scene.geometries.findIntersections(ray));
        return intersection == null ? scene.background : calcColor(intersection);
    }

    /**
     * Calculates the color of a point.
     *
     * @param point the point to calculate the color for
     * @return the color of the point
     */
    private Color calcColor(Point point) {
        return scene.ambientLight.getIntensity();
    }
}
