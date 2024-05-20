package geometries;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

/**
 * Represents a cylinder in 3D space.
 *
 * @author Adir and Meir
 */
public class Cylinder extends Tube {
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
        return null; // Will be implemented later
    }
}
