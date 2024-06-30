package renderer;

import geometries.Intersectable.GeoPoint;
import primitives.Color;
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
        var intersections = scene.geometries.findGeoIntersections(ray);
        if (intersections == null) return scene.background;

        GeoPoint closestPoint = ray.findClosestGeoPoint(intersections);
        return calcColor(closestPoint);
    }

    /**
     * Calculates the color of a GeoPoint.
     *
     * @param geoPoint the GeoPoint to calculate the color for
     * @return the color of the GeoPoint
     */
    private Color calcColor(GeoPoint geoPoint) {
        return geoPoint.geometry.getEmission().add(scene.ambientLight.getIntensity());
    }
}
