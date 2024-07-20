package geometries;

import primitives.*;

import java.util.List;

/**
 * Represents a tube in 3D space, defined by a radius and a central axis ray.
 * Extends the RadialGeometry class.
 * Provides methods to calculate the normal vector at a given point on the tube.
 *
 * @author Adir and Meir.
 */
public class Tube extends RadialGeometry {

    /**
     * The Ray in the center of the tube.
     */
    protected final Ray axisRay;

    /**
     * Constructor for Tube.
     *
     * @param axisRay The central axis of the tube.
     * @param radius  The radius of the tube.
     */
    public Tube(Ray axisRay, double radius) {
        super(radius);
        this.axisRay = axisRay;
    }

    /**
     * Returns the normal vector to the Tube object at the specified point.
     *
     * @param point The point on the Tube object.
     * @return The normal vector to the Tube object at the specified point.
     */
    @Override
    public Vector getNormal(Point point) {
        Point head = axisRay.getHead();
        Vector direction = axisRay.getDirection();

        // Calculate the projection of the point onto the axis ray.
        // That is, how far along the direction vector from the starting point head one must go to reach the closest point to the given point.
        // alignZero(double) in order to avoid negligible computational errors with small numbers.
        double projectionLength = Util.alignZero(direction.dotProduct(point.subtract(head)));

        // Calculates the vector from the closest point on the axis to the given point, normalizes it, and returns it as a vector normal to the given point on the surface of the Tube.
        return point.subtract(projectionLength == 0 ? head : head.add(direction.scale(projectionLength))).normalize();
    }

    @Override
    public List<GeoPoint> findGeoIntersectionsHelper(Ray ray, double maxDistance) {
        return null; //TODO
    }
}
