package geometries;
import primitives.*;

/**
 * Interface for geometric bodies.
 *
 * @author Adir and Meir.
 */
public interface Geometry extends Intersectable{

    /**
     * Calculates the normal vector to the geometry at the specified point.
     *
     * @param point The point on the geometry where the normal is to be calculated.
     * @return The normal vector at the given point.
     */
    Vector getNormal(Point point);
}