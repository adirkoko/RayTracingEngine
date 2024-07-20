package geometries;

import java.util.LinkedList;
import java.util.List;

import primitives.Ray;

/**
 * Represents a collection of geometric objects.
 * Implements the Intersectable class using the Composite design pattern.
 * Provides methods for adding geometries and finding intersections with rays.
 *
 * @author Adir and Meir.
 */
public class Geometries extends Intersectable {

    /**
     * A private unmodifiable field of a list of geometric objects, initialized with an empty list
     */
    private final List<Intersectable> geometries = new LinkedList<>();

    /**
     * Default constructor (empty).
     */
    public Geometries() {
    }

    /**
     * Constructor that takes multiple geometric objects and adds them to the collection.
     *
     * @param geometries the geometric objects to add
     */
    public Geometries(Intersectable... geometries) {
        add(geometries);
    }

    /**
     * Adds geometric objects to the collection.
     *
     * @param geometries the geometric objects to add
     */
    public void add(Intersectable... geometries) {
        this.geometries.addAll(List.of(geometries));
    }

    @Override
    protected List<GeoPoint> findGeoIntersectionsHelper(Ray ray, double maxDistance) {
        List<GeoPoint> intersections = null;

        for (Intersectable geo : geometries) {
            var tempGeoIntersections = geo.findGeoIntersections(ray, maxDistance);
            if (tempGeoIntersections != null) {
                if (intersections == null)
                    intersections = new LinkedList<>(tempGeoIntersections);
                else
                    intersections.addAll(tempGeoIntersections);
            }
        }

        return intersections;
    }
}
