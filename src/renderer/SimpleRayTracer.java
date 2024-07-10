package renderer;

import geometries.Intersectable.GeoPoint;
import lighting.LightSource;
import primitives.*;
import scene.Scene;

import static primitives.Util.alignZero;

/**
 * SimpleRayTracer class that extends RayTracerBase.
 * Implements a simple ray tracer that calculates color at intersections using local effects and ambient light.
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
     * @param gp  the geo point
     * @param ray the ray
     * @return the color with local lighting effects
     */
    private Color calcLocalEffects(GeoPoint gp, Ray ray) {
        Vector n = gp.geometry.getNormal(gp.point);
        Vector v = ray.getDirection();
        double nv = alignZero(n.dotProduct(v));
        if (nv == 0) return gp.geometry.getEmission();

        Material material = gp.geometry.getMaterial();
        Color color = gp.geometry.getEmission();
        for (LightSource lightSource : scene.lights) {
            Vector l = lightSource.getL(gp.point);
            double nl = alignZero(n.dotProduct(l));
            if (nl * nv > 0) {
                Color iL = lightSource.getIntensity(gp.point);
                color = color.add(
                        iL.scale(calcDiffuse(material.kD, l, n)
                                .add(calcSpecular(material.kS, l, n, v, material.nShininess, iL))
                        ));
            }
        }
        return color;
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

}
