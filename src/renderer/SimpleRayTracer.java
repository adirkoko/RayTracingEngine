package renderer;

import geometries.Intersectable.GeoPoint;
import lighting.LightSample;
import lighting.LightSource;
import primitives.*;
import sampling.ConeSampler;
import scene.Scene;

import java.util.LinkedList;
import java.util.List;

import static primitives.Util.*;

/**
 * SimpleRayTracer class that extends RayTracerBase.
 * Implements a simple ray tracer that calculates color at intersections using local effects and ambient light.
 * Provides methods for tracing rays, calculating color, and handling local and global effects.
 *
 * @author Adir and Meir
 */
public class SimpleRayTracer extends RayTracerBase {
    /**
     * Maximum recursion level for color calculation to prevent infinite recursion.
     */
    private static final int MAX_CALC_COLOR_LEVEL = 10;

    /**
     * Minimum attenuation coefficient for color calculation to prevent calculations that have insignificant impact.
     */
    private static final double MIN_CALC_COLOR_K = 0.001;

    /**
     * Maximum number of light samples consumed from one light source.
     */
    private static final int MAX_LIGHT_SAMPLES = 64;

    /**
     * Maximum recursive layers that may expand one global effect into multiple cone samples.
     */
    private static final int MAX_GLOBAL_SAMPLE_DEPTH = 1;

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
        return calcColor(intersection, ray, MAX_CALC_COLOR_LEVEL, INITIAL_K, MAX_GLOBAL_SAMPLE_DEPTH)
                .add(scene.ambientLight.getIntensity());
    }

    /**
     * Finds the closest intersection point of a ray with the geometries in the scene.
     *
     * @param ray The ray for which to find the closest intersection.
     * @return The closest intersection point, or null if no intersection is found.
     */
    private GeoPoint findClosestIntersection(Ray ray) {
        return scene.geometries.findClosestGeoIntersection(ray);
    }

    /**
     * Calculates the color at the intersection point, including local effects and global effects (reflection and transparency).
     *
     * @param gp    The intersection point.
     * @param ray   The ray that intersects the geometry.
     * @param level The recursion level.
     * @param k     The accumulated attenuation factor.
     * @param globalSampleDepth remaining recursive depth that may expand cone-sampled global effects
     * @return The color at the intersection point.
     */
    private Color calcColor(GeoPoint gp, Ray ray, int level, Double3 k, int globalSampleDepth) {
        if (level <= 0 || k.lowerThan(MIN_CALC_COLOR_K)) return Color.BLACK;
        Vector v = ray.getDirection();
        Vector n = gp.geometry.getNormal(gp.point);
        double vn = v.dotProduct(n);
        if (isZero(vn)) return Color.BLACK;
        Color color = calcLocalEffects(gp, v, n, vn, k); // Calculate local effects
        return 1 == level ? color : color.add(calcGlobalEffects(gp, v, n, vn, level, k, globalSampleDepth)); // Add global effects
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
        return new Ray(gp.point, constructReflectedDirection(v, n, vn), n);
    }

    /**
     * Calculates the reflected direction.
     *
     * @param v  The incoming ray direction.
     * @param n  the normal vector at gp
     * @param vn v dot-product n
     * @return The reflected direction.
     */
    private Vector constructReflectedDirection(Vector v, Vector n, double vn) {
        return v.subtract(n.scale(2 * vn));
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
     * @param globalSampleDepth remaining recursive depth that may expand cone-sampled global effects
     * @return The color of the global effect.
     */
    private Color calcGlobalEffect(Ray ray, int level, Double3 k, Double3 kx, int globalSampleDepth) {
        Double3 kkx = kx.product(k);
        if (kkx.lowerThan(MIN_CALC_COLOR_K)) return Color.BLACK;
        GeoPoint gp = findClosestIntersection(ray);
        return (gp == null ? scene.background : calcColor(gp, ray, level - 1, kkx, Math.max(0, globalSampleDepth - 1)).scale(kx));
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
     * @param globalSampleDepth remaining recursive depth that may expand cone-sampled global effects
     * @return The combined color of reflection and transparency.
     */
    private Color calcGlobalEffects(GeoPoint gp, Vector v, Vector n, double vn, int level, Double3 k, int globalSampleDepth) {
        Material material = gp.geometry.getMaterial();
        return calcTransparencyEffect(gp, v, n, level, k, material, globalSampleDepth)
                .add(calcReflectionEffect(gp, v, n, vn, level, k, material, globalSampleDepth));
    }

    /**
     * Calculates transparency, either as a single straight-through ray or as averaged diffused glass samples.
     *
     * @param gp       The geo point.
     * @param v        The incoming ray direction.
     * @param n        the normal vector at gp
     * @param level    The recursion level.
     * @param k        The accumulated attenuation factor.
     * @param material The material of the geometry.
     * @param globalSampleDepth remaining recursive depth that may expand cone-sampled global effects
     * @return The transparency color contribution.
     */
    private Color calcTransparencyEffect(GeoPoint gp, Vector v, Vector n, int level, Double3 k, Material material, int globalSampleDepth) {
        if (material.kT.product(k).lowerThan(MIN_CALC_COLOR_K)) return Color.BLACK;
        if (isZero(material.transparencyBlur) || globalSampleDepth <= 0)
            return calcGlobalEffect(constructRefractedRay(gp, v, n), level, k, material.kT, globalSampleDepth);

        List<Ray> refractedRays = constructDiffusedRefractedRays(gp, v, n, material, globalSampleCount(material));
        Color color = Color.BLACK;
        for (Ray refractedRay : refractedRays)
            color = color.add(calcGlobalEffect(refractedRay, level, k, material.kT, globalSampleDepth));
        return color.reduce(refractedRays.size());
    }

    /**
     * Calculates reflection, either as a perfect mirror ray or as averaged glossy samples.
     *
     * @param gp       The geo point.
     * @param v        The incoming ray direction.
     * @param n        the normal vector at gp
     * @param vn       v dot-product n
     * @param level    The recursion level.
     * @param k        The accumulated attenuation factor.
     * @param material The material of the geometry.
     * @param globalSampleDepth remaining recursive depth that may expand cone-sampled global effects
     * @return The reflected color contribution.
     */
    private Color calcReflectionEffect(GeoPoint gp, Vector v, Vector n, double vn, int level, Double3 k, Material material, int globalSampleDepth) {
        if (material.kR.product(k).lowerThan(MIN_CALC_COLOR_K)) return Color.BLACK;
        if (isZero(material.reflectionBlur) || globalSampleDepth <= 0)
            return calcGlobalEffect(constructReflectedRay(gp, v, n, vn), level, k, material.kR, globalSampleDepth);

        List<Ray> reflectedRays = constructGlossyReflectedRays(gp, constructReflectedDirection(v, n, vn), n, material, globalSampleCount(material));
        Color color = Color.BLACK;
        for (Ray reflectedRay : reflectedRays)
            color = color.add(calcGlobalEffect(reflectedRay, level, k, material.kR, globalSampleDepth));
        return color.reduce(reflectedRays.size());
    }

    /**
     * Gets a bounded global sample count from a material.
     *
     * @param material The material of the geometry.
     * @return bounded sample count
     */
    private int globalSampleCount(Material material) {
        return Math.max(1, Math.min(material.globalSamples, Material.MAX_GLOBAL_SAMPLES));
    }

    /**
     * Constructs glossy reflection rays around a perfect reflection direction.
     *
     * @param gp                  The geo point.
     * @param reflectedDirection  The perfect reflection direction.
     * @param n                   The normal vector at gp.
     * @param material            The material of the geometry.
     * @param sampleCount         Number of glossy samples to construct.
     * @return Glossy reflection rays.
     */
    private List<Ray> constructGlossyReflectedRays(GeoPoint gp, Vector reflectedDirection, Vector n, Material material, int sampleCount) {
        List<Ray> reflectedRays = new LinkedList<>();
        for (Vector direction : new ConeSampler(
                reflectedDirection, material.reflectionBlur, sampleCount).getSamples())
            reflectedRays.add(new Ray(gp.point, direction, n));
        return reflectedRays;
    }

    /**
     * Constructs diffused transparency rays around the straight-through direction.
     *
     * @param gp       The geo point.
     * @param v        The incoming ray direction.
     * @param n        The normal vector at gp.
     * @param material The material of the geometry.
     * @param sampleCount Number of diffused transparency samples to construct.
     * @return Diffused transparency rays.
     */
    private List<Ray> constructDiffusedRefractedRays(GeoPoint gp, Vector v, Vector n, Material material, int sampleCount) {
        List<Ray> refractedRays = new LinkedList<>();
        for (Vector direction : new ConeSampler(
                v, material.transparencyBlur, sampleCount).getSamples())
            refractedRays.add(new Ray(gp.point, direction, n));
        return refractedRays;
    }

    /**
     * Calculates the local effects (diffuse and specular) of the lighting on a given point.
     *
     * @param gp The geo point.
     * @param v  The incoming ray direction.
     * @param n  The normal vector at gp.
     * @param vn v dot-product n.
     * @param k  The accumulated attenuation factor.
     * @return The color with local lighting effects.
     */
    private Color calcLocalEffects(GeoPoint gp, Vector v, Vector n, double vn, Double3 k) {
        Color color = gp.geometry.getEmission(); // Base emission color of the geometry
        Material material = gp.geometry.getMaterial(); // Material properties of the geometry

        for (LightSource lightSource : scene.lights)
            color = color.add(calcLightContribution(gp, v, n, vn, k, material, lightSource));

        return color; // Return the final color with local lighting effects
    }

    /**
     * Calculates the averaged contribution of all samples returned by a light source.
     *
     * @param gp          The geo point.
     * @param v           The incoming ray direction.
     * @param n           The normal vector at gp.
     * @param vn          v dot-product n.
     * @param k           The accumulated attenuation factor.
     * @param material    The material of the geometry.
     * @param lightSource The sampled light source.
     * @return The light contribution after per-sample shadow checks.
     */
    private Color calcLightContribution(
            GeoPoint gp, Vector v, Vector n, double vn, Double3 k, Material material, LightSource lightSource) {
        List<LightSample> lightSamples = lightSource.getSamples(gp.point);
        if (lightSamples == null || lightSamples.isEmpty()) return Color.BLACK;

        Color color = Color.BLACK;
        int sampleCount = Math.min(lightSamples.size(), MAX_LIGHT_SAMPLES);
        for (int i = 0; i < sampleCount; i++)
            color = color.add(calcLightSampleContribution(gp, v, n, vn, k, material, lightSamples.get(i)));

        return color.reduce(sampleCount);
    }

    /**
     * Calculates one light sample contribution, including transparency along its shadow ray.
     *
     * @param gp          The geo point.
     * @param v           The incoming ray direction.
     * @param n           The normal vector at gp.
     * @param vn          v dot-product n.
     * @param k           The accumulated attenuation factor.
     * @param material    The material of the geometry.
     * @param lightSample The sampled light contribution.
     * @return The sample contribution, or black when blocked or irrelevant.
     */
    private Color calcLightSampleContribution(
            GeoPoint gp, Vector v, Vector n, double vn, Double3 k, Material material, LightSample lightSample) {
        Vector l = lightSample.direction();
        double ln = alignZero(l.dotProduct(n));
        if (ln * vn <= 0) return Color.BLACK;

        Double3 ktr = transparency(gp, lightSample, l, n);
        if (ktr.product(k).lowerThan(MIN_CALC_COLOR_K)) return Color.BLACK;

        Color iL = lightSample.intensity().scale(ktr);
        return iL.scale(calcDiffuse(material, ln))
                .add(iL.scale(calcSpecular(material, n, l, ln, v)));
    }

    /**
     * Calculates the transparency coefficient for the given light source and ray.
     *
     * @param gp          The geo point.
     * @param lightSample The light sample.
     * @param l           The direction from the light sample to the point.
     * @param n           The normal at the intersection point.
     * @return The transparency coefficient.
     */
    private Double3 transparency(GeoPoint gp, LightSample lightSample, Vector l, Vector n) {
        Double3 ktr = Double3.ONE;
        List<GeoPoint> intersections = scene.geometries.findGeoIntersections(
                new Ray(gp.point, l.scale(-1), n)
                , lightSample.distance());
        if (intersections == null) return ktr;

        for (GeoPoint p : intersections) {
            ktr = ktr.product(p.geometry.getMaterial().kT); // Calculate the transparency coefficient
            if (ktr.lowerThan(MIN_CALC_COLOR_K))
                return Double3.ZERO; // If transparency is below the threshold, return zero
        }
        return ktr; // Return the final transparency coefficient
    }

    /**
     * Calculates the diffuse lighting effect.
     *
     * @param mat The material of the geometry.
     * @param nl  The dot product of the light direction and the normal vector.
     * @return The diffuse lighting effect color.
     */
    private Double3 calcDiffuse(Material mat, double nl) {
        return mat.kD.scale(alignZero(nl) < 0 ? -nl : nl); // Calculate the diffuse component based on the dot product
    }

    /**
     * Calculates the specular lighting effect.
     *
     * @param mat The material of the geometry.
     * @param n   The normal vector at the point.
     * @param l   The light direction vector.
     * @param nl  The dot product of the light direction and the normal vector.
     * @param v   The view direction vector.
     * @return The specular lighting effect color.
     */
    private Double3 calcSpecular(Material mat, Vector n, Vector l, double nl, Vector v) {
        double vr = v.dotProduct(l.subtract(n.scale(nl * 2))); // Calculate the reflection vector
        return (alignZero(vr) > 0) ? Double3.ZERO : mat.kS.scale(Math.pow(-vr, mat.nShininess)); // Calculate the specular component
    }
}
