package renderer;

import geometries.Intersectable.GeoPoint;
import lighting.LightSource;
import primitives.*;
import scene.Scene;

import java.util.List;

import static primitives.Util.*;

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
    public static final double DELTA = 0.1;

    /**
     * Maximum recursion level for color calculation to prevent infinite recursion.
     */
    private static final int MAX_CALC_COLOR_LEVEL = 10;

    /**
     * Minimum attenuation coefficient for color calculation to prevent calculations that have insignificant impact.
     */
    private static final double MIN_CALC_COLOR_K = 0.001;
    /**
     * Start recursion from aggregated attenuation factor of 1 (i.e. no attenuation)
     */
    private static final Double3 INITIAL_K = Double3.ONE;

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
        GeoPoint closestPoint = findClosestIntersection(ray);
        return closestPoint == null
                ? scene.background
                : calcColor(closestPoint, ray);
    }

    /**
     * Calculates the color at the intersection point, including local effects and ambient light.
     *
     * @param intersection The intersection point.
     * @param ray          The ray that intersects the geometry.
     * @return The color at the intersection point.
     */
    private Color calcColor(GeoPoint intersection, Ray ray) {
        return calcColor(intersection, ray, MAX_CALC_COLOR_LEVEL, INITIAL_K)
                .add(scene.ambientLight.getIntensity());
    }

    /**
     * Finds the closest intersection point of a ray with the geometries in the scene.
     *
     * @param ray The ray for which to find the closest intersection.
     * @return The closest intersection point, or null if no intersection is found.
     */
    private GeoPoint findClosestIntersection(Ray ray) {
        List<GeoPoint> intersections = scene.geometries.findGeoIntersections(ray);
        return intersections == null ? null : ray.findClosestGeoPoint(intersections);
    }

    /**
     * Calculates the color at the intersection point, including local effects and global effects (reflection and transparency).
     *
     * @param gp    The intersection point.
     * @param ray   The ray that intersects the geometry.
     * @param level The recursion level.
     * @param k     The accumulated attenuation factor.
     * @return The color at the intersection point.
     */
    private Color calcColor(GeoPoint gp, Ray ray, int level, Double3 k) {
        Vector v = ray.getDirection();
        Vector n = gp.geometry.getNormal(gp.point);
        double vn = v.dotProduct(n);
        if (isZero(vn)) return Color.BLACK;
        Color color = calcLocalEffects(gp, v, n, vn, k); // Calculate local effects
        return 1 == level ? color : color.add(calcGlobalEffects(gp, v, n, vn, level, k)); // Add global effects
    }

    /**
     * Constructs a reflected ray.
     *
     * @param gp The geo point.
     * @param v  The incoming ray direction.
     * @param n  the normal vector at gp
     * @param vn v dot-product n
     * @return The reflected ray.
     */
    private Ray constructReflectedRay(GeoPoint gp, Vector v, Vector n, double vn) {
        return new Ray(gp.point, v.subtract(n.scale(2 * vn)), n);
    }

    /**
     * Constructs a refracted ray.
     *
     * @param gp The geo point.
     * @param v  The incoming ray direction.
     * @param n  the normal vector at gp
     * @return The refracted ray.
     */
    private Ray constructRefractedRay(GeoPoint gp, Vector v, Vector n) {
        return new Ray(gp.point, v, n);
    }

    /**
     * Calculates the global effect (reflection or transparency) of the ray.
     *
     * @param ray   The original ray.
     * @param level The recursion level.
     * @param k     The accumulated attenuation factor.
     * @param kx    The attenuation factor for the specific effect.
     * @return The color of the global effect.
     */
    private Color calcGlobalEffect(Ray ray, int level, Double3 k, Double3 kx) {
        Double3 kkx = kx.product(k);
        if (kkx.lowerThan(MIN_CALC_COLOR_K)) return Color.BLACK;
        GeoPoint gp = findClosestIntersection(ray);
        return (gp == null ? scene.background : calcColor(gp, ray, level - 1, kkx).scale(kx));
    }

    /**
     * Calculates the global effects (reflection and transparency) of the ray.
     *
     * @param gp    The intersection point.
     * @param v     The incoming ray direction.
     * @param n     the normal vector at gp
     * @param vn    v dot-product n
     * @param level The recursion level.
     * @param k     The accumulated attenuation factor.
     * @return The combined color of reflection and transparency.
     */
    private Color calcGlobalEffects(GeoPoint gp, Vector v, Vector n, double vn, int level, Double3 k) {
        Material material = gp.geometry.getMaterial();
        return calcGlobalEffect(constructRefractedRay(gp, v, n), level, k, material.kT)
                .add(calcGlobalEffect(constructReflectedRay(gp, v, n, vn), level, k, material.kR));
    }

    /**
     * Calculates the local effects (diffuse and specular) of the lighting on a given point.
     *
     * @param gp The geo point.
     * @param v  The incoming ray direction.
     * @param n  the normal vector at gp
     * @param vn v dot-product n
     * @param k  The accumulated attenuation factor.
     * @return The color with local lighting effects.
     */
    private Color calcLocalEffects(GeoPoint gp, Vector v, Vector n, double vn, Double3 k) {
        Color color = gp.geometry.getEmission(); // Base emission color of the geometry
        Material material = gp.geometry.getMaterial(); // Material properties of the geometry

        for (LightSource lightSource : scene.lights) {
            Vector l = lightSource.getL(gp.point); // Direction vector from point to light source
            double ln = alignZero(l.dotProduct(n));
            if (ln * vn > 0) { // Check if the light source is on the same side of the surface as the view direction
                Double3 ktr = transparency(lightSource, l, n, gp);
                if (!ktr.product(k).lowerThan(MIN_CALC_COLOR_K)) {
                    Color iL = lightSource.getIntensity(gp.point); //.scale(ktr); // Intensity of the light at the point
                    // Add diffuse and specular lighting effects
                    color = color.add(
                            iL.scale(
                                    calcDiffuse(material.kD, l, n) // Diffuse component
                                            .add(calcSpecular(material.kS, l, n, ln, v, material.nShininess)) // Specular component
                            )
                    );
                }
            }
        }

        return color; // Return the final color with local lighting effects
    }

    /**
     * Calculates the transparency coefficient for the given light source and ray.
     *
     * @param lightSource the light source
     * @param l           the direction from the point to the light source
     * @param n           the normal at the intersection point
     * @param geoPoint    the intersection point
     * @return the transparency coefficient
     */
    private Double3 transparency(LightSource lightSource, Vector l, Vector n, GeoPoint geoPoint) {
        Double3 ktr = Double3.ONE;
        Ray lightRay = new Ray(geoPoint.point, l.scale(-1), n);
        double lightDistance = lightSource.getDistance(geoPoint.point);
        List<GeoPoint> intersections = scene.geometries.findGeoIntersections(lightRay, lightDistance);
        if (intersections == null) return ktr;

        for (GeoPoint gp : intersections) {
            ktr = ktr.product(gp.geometry.getMaterial().kT);
            if (ktr.lowerThan(MIN_CALC_COLOR_K)) return Double3.ZERO;
        }
        return ktr;
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
     * @param kS        the specular coefficient
     * @param l         the light direction vector
     * @param n         the normal vector at the point
     * @param ln        l dot-product n
     * @param v         the view direction vector
     * @param shininess the shininess coefficient
     * @return the specular lighting effect color
     */
    private Double3 calcSpecular(Double3 kS, Vector l, Vector n, double ln, Vector v, int shininess) {
        Vector r = l.subtract(n.scale(2 * ln));
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
    @SuppressWarnings("unused")
    @Deprecated(forRemoval = true)
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
            if (alignZero(intersection.point.distance(point) - lightDistance) <= 0 && intersection.geometry.getMaterial().kT.equals(Double3.ZERO)) {
                return false; // Point is shaded
            }
        }
        return true; // Point is unshaded
    }

}
