package geometries;

/**
 * Abstract class representing radial geometries, defined by a radius.
 *
 * @author Adir and Meir.
 */
public abstract class RadialGeometry implements Geometry {
    protected final double radius;

    /**
     * Constructor for radial geometries. Initializes the geometry with the given radius.
     *
     * @param radius The radius of the radial geometry.
     */
    public RadialGeometry(double radius) {
        this.radius = radius;
    }
}