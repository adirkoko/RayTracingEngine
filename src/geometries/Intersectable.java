package geometries;

import primitives.*;

import java.util.List;

/**
 * Abstract class for intersectable geometric bodies.
 *
 * @author Adir and Meir.
 */
public abstract class Intersectable {

    /**
     * Find intersections with the given ray.
     *
     * @param ray the ray
     * @return list of intersection points
     */
    public List<Point> findIntersections(Ray ray) {
        var geoList = findGeoIntersections(ray);
        return geoList == null ? null : geoList.stream().map(gp -> gp.point).toList();
    }

    /**
     * Find intersections with the given ray.
     *
     * @param ray the ray
     * @return list of intersection points as GeoPoint objects
     */
    public List<GeoPoint> findGeoIntersections(Ray ray) {
        return findGeoIntersectionsHelper(ray);
    }

    /**
     * Helper method to find intersections with the given ray.
     *
     * @param ray the ray
     * @return list of intersection points as GeoPoint objects
     */
    protected abstract List<GeoPoint> findGeoIntersectionsHelper(Ray ray);

    /**
     * GeoPoint is a helper class that represents a point on a geometry.
     */
    public static class GeoPoint {

        /**
         * The geometry on which the point lies.
         */
        public Geometry geometry;

        /**
         * The point on the geometry.
         */
        public Point point;

        /**
         * Constructor for GeoPoint.
         *
         * @param geometry the geometry
         * @param point    the point on the geometry
         */
        public GeoPoint(Geometry geometry, Point point) {
            this.geometry = geometry;
            this.point = point;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            return (obj instanceof GeoPoint other)
                    && this.geometry.equals(other.geometry)
                    && this.point.equals(other.point);
        }

        @Override
        public String toString() {
            return "geometry = " + geometry + " point = " + point;
        }
    }
}
