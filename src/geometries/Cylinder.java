package geometries;
import primitives.*;

/**
 * Represents a cylinder in 3D space.
 *
 * @author Adir and Meir
 */
public class Cylinder extends Tube {

    /**
     * The height of the tube.
     */
    private final double height;

    /**
     * Constructor for Cylinder
     *
     * @param axisRay The central axis of the cylinder.
     * @param radius  The radius of the cylinder.
     * @param height  The height of the cylinder.
     */
    public Cylinder(Ray axisRay, double radius, double height) {
        super(axisRay, radius);
        this.height = height;
    }

    @Override
    public Vector getNormal(Point point) {
        Point head = axisRay.getHead();
        Vector direction = axisRay.getDirection();

        // If the point is in the center of the bottom base.
        if (point.equals(head)) return direction.scale(-1); // Normal is opposite to the direction of the axis

        // Calculate the projection of the point onto the axis ray
        double projectionLength = direction.dotProduct(point.subtract(head));

        // If the point is on the bottom base
        if (Util.isZero(projectionLength)) return direction.scale(-1); // Normal is opposite to the direction of the axis

        // If the point is on the top base
        if (Util.isZero(projectionLength - height)) return direction; // Normal is the same as the direction of the axis

        // Calculate the vector from the closest point on the axis to the given point, then normalize it.
        return point.subtract(head.add(direction.scale(projectionLength))).normalize();
    }
}
