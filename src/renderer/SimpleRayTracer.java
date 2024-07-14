package renderer;

import geometries.Intersectable.GeoPoint;
import lighting.LightSource;
import primitives.*;
import scene.Scene;

import java.util.List;

import static primitives.Util.alignZero;

/**
 * SimpleRayTracer class that extends RayTracerBase.
 * Implements a simple ray tracer that calculates color at intersections using local effects and ambient light.
 *
 * @author Adir and Meir
 */
public class SimpleRayTracer extends RayTracerBase {

    /**
     * Offset size for primary rays to avoid shadow acne.
     */
    private static final double DELTA = 0.1;

    /**
     * Constructor for SimpleRayTracer.
     *
     * @param scene the scene to trace rays in
     */
    public SimpleRayTracer(Scene scene) {
        super(scene);
    }

    /**
     * Traces the given ray and calculates the color at the closest intersection point.
     *
     * @param ray the ray
     * @return the color at the closest intersection point or the background color if no intersection
     */
    public Color traceRay(Ray ray) {
        var intersections = scene.geometries.findGeoIntersections(ray);
        return intersections == null
                ? scene.background
                : calcColor(ray.findClosestGeoPoint(intersections), ray);
    }

    /**
     * Calculates the color at the intersection point including local effects and ambient light.
     *
     * @param intersection the intersection point
     * @param ray          the ray
     * @return the color at the intersection point
     */
    private Color calcColor(GeoPoint intersection, Ray ray) {
        return scene.ambientLight.getIntensity()
                .add(calcLocalEffects(intersection, ray));
    }

    /**
     * Calculates the local effects (diffuse and specular) of the lighting on a given point.
     *
     * @param gp  The geo point.
     * @param ray The ray.
     * @return The color with local lighting effects.
     */
    private Color calcLocalEffects(GeoPoint gp, Ray ray) {
        Vector n = gp.geometry.getNormal(gp.point); // Normal vector at the geo point
        Vector v = ray.getDirection(); // Direction vector of the ray
        double nv = alignZero(n.dotProduct(v)); // Dot product of normal and view direction

        if (nv == 0) return gp.geometry.getEmission(); // No lighting effect if vectors are orthogonal

        Material material = gp.geometry.getMaterial(); // Material properties of the geometry
        Color color = gp.geometry.getEmission(); // Base emission color of the geometry

        for (LightSource lightSource : scene.lights) {
            Vector l = lightSource.getL(gp.point); // Direction vector from point to light source

            // Check if the light source is on the same side of the surface as the view direction
            if (alignZero(n.dotProduct(l)) * nv > 0 && unshaded(gp, lightSource, l, n)) {
                Color iL = lightSource.getIntensity(gp.point); // Intensity of the light at the point

                // Add diffuse and specular lighting effects
                color = color.add(
                        iL.scale(
                                calcDiffuse(material.kD, l, n) // Diffuse component
                                        .add(calcSpecular(material.kS, l, n, v, material.nShininess, iL)) // Specular component
                        )
                );
            }
        }

        return color; // Return the final color with local lighting effects
    }

    /**
     * Calculates the diffuse lighting effect.
     *
     * @param kD the diffuse coefficient
     * @param l  the light direction vector
     * @param n  the normal vector at the point
     * @return the diffuse lighting effect color
     */
    private Double3 calcDiffuse(Double3 kD, Vector l, Vector n) {
        return kD.scale(Math.abs(l.dotProduct(n)));
    }

    /**
     * Calculates the specular lighting effect.
     *
     * @param kS             the specular coefficient
     * @param l              the light direction vector
     * @param n              the normal vector at the point
     * @param v              the view direction vector
     * @param shininess      the shininess coefficient
     * @param lightIntensity the intensity of the light
     * @return the specular lighting effect color
     */
    private Double3 calcSpecular(Double3 kS, Vector l, Vector n, Vector v, int shininess, Color lightIntensity) {
        Vector r = l.subtract(n.scale(2 * l.dotProduct(n))).normalize();
        double vr = alignZero(-v.dotProduct(r));
        return vr <= 0 ? Double3.ZERO : kS.scale(Math.pow(vr, shininess));
    }

    /**
     * Checks if a point is unshaded from a given light source.
     *
     * @param gp          The geo point to check.
     * @param lightSource The light source.
     * @param l           The light direction vector.
     * @param n           The normal vector at the point.
     * @return true if the point is unshaded, false otherwise.
     */
    private boolean unshaded(GeoPoint gp, LightSource lightSource, Vector l, Vector n) {
        Vector lightDirection = l.scale(-1); // from point to light source
        Vector delta = n.scale(n.dotProduct(lightDirection) > 0 ? DELTA : -DELTA); // Calculate the delta based on the dot product
        Point point = gp.point.add(delta); // Offset point to avoid self-shadowing
        double lightDistance = lightSource.getDistance(point); // Distance to the light source

        // Find intersections between the light ray and the geometries within the light distance
        List<GeoPoint> intersections = scene.geometries.findGeoIntersections(new Ray(point, l.scale(-1)), lightDistance);

        if (intersections == null) return true; // No intersections, point is unshaded

        for (GeoPoint intersection : intersections) {
            // If an intersection is found within the light distance and the transparency coefficient is less than 1
            if (alignZero(intersection.point.distance(point) - lightDistance) <= 0) {
                return false; // Point is shaded
            }
        }
        return true; // Point is unshaded
    }


}
