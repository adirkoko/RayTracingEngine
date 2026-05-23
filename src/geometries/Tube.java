package geometries;

import primitives.*;

import java.util.List;

import static primitives.Util.alignZero;
import static primitives.Util.isZero;

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
    protected List<GeoPoint> findGeoIntersectionsHelper(Ray ray, double maxDistance) {
        Point rayHead = ray.getHead();
        Vector rayDirection = ray.getDirection();
        Point axisHead = axisRay.getHead();
        Vector axisDirection = axisRay.getDirection();

        double dx = rayHead.getX() - axisHead.getX();
        double dy = rayHead.getY() - axisHead.getY();
        double dz = rayHead.getZ() - axisHead.getZ();

        double vVa = rayDirection.dotProduct(axisDirection);
        double dVa = dx * axisDirection.getX() + dy * axisDirection.getY() + dz * axisDirection.getZ();

        double vX = rayDirection.getX() - axisDirection.getX() * vVa;
        double vY = rayDirection.getY() - axisDirection.getY() * vVa;
        double vZ = rayDirection.getZ() - axisDirection.getZ() * vVa;

        double dX = dx - axisDirection.getX() * dVa;
        double dY = dy - axisDirection.getY() * dVa;
        double dZ = dz - axisDirection.getZ() * dVa;

        double a = alignZero(vX * vX + vY * vY + vZ * vZ);
        if (isZero(a)) return null;

        double b = alignZero(2 * (vX * dX + vY * dY + vZ * dZ));
        double c = alignZero(dX * dX + dY * dY + dZ * dZ - radiusSquared);
        double discriminant = alignZero(b * b - 4 * a * c);
        if (discriminant <= 0) return null;

        double sqrtDiscriminant = Math.sqrt(discriminant);
        double t1 = alignZero((-b - sqrtDiscriminant) / (2 * a));
        double t2 = alignZero((-b + sqrtDiscriminant) / (2 * a));

        if (t1 <= 0 || alignZero(maxDistance - t1) <= 0)
            return t2 > 0 && alignZero(maxDistance - t2) > 0
                    ? List.of(new GeoPoint(this, ray.getPoint(t2)))
                    : null;

        return t2 > 0 && alignZero(maxDistance - t2) > 0
                ? List.of(new GeoPoint(this, ray.getPoint(t1)), new GeoPoint(this, ray.getPoint(t2)))
                : List.of(new GeoPoint(this, ray.getPoint(t1)));
    }
}
