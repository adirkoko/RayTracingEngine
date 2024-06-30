package geometries;

/**
 * Abstract class representing radial geometries, defined by a radius.
 *
 * @author Adir and Meir.
 */
public abstract class RadialGeometry extends Geometry {
    /**
     * The radius of the geometry.
     */
    protected final double radius;

    /**
     * The square of the radius. Storing this value avoids repeated computations of the radius squared.
     */
    protected final double radiusSquared;

    /**
     * Constructor for radial geometries. Initializes the geometry with the given
     * radius.
     *
     * @param radius The radius of the radial geometry.
     */
    public RadialGeometry(double radius) {
        this.radius = radius;
        this.radiusSquared = radius * radius;
    }
}