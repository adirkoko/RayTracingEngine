package geometries;

import java.util.LinkedList;
import java.util.List;

import primitives.Ray;

/**
 * Represents a collection of geometric objects.
 * Implements the Intersectable class using the Composite design pattern.
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
    public List<GeoPoint> findGeoIntersectionsHelper(Ray ray) {
        List<GeoPoint> intersections = null;

        for (Intersectable geo : geometries) {
            List<GeoPoint> tempGeoIntersections = geo.findGeoIntersections(ray);
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
