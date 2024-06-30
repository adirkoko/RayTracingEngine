package geometries;

import java.util.LinkedList;
import java.util.List;

import primitives.Point;
import primitives.Ray;

/**
 * Represents a collection of geometric objects.
 * Implements the Intersectable interface using the Composite design pattern.
 *
 * @author Adir and Meir.
 */
public class Geometries implements Intersectable {

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
    public List<Point> findIntersections(Ray ray) {
        List<Point> intersections = null;

        for (Intersectable geo : geometries) {
            List<Point> tempIntersections = geo.findIntersections(ray);
            if (tempIntersections != null) {
                if (intersections == null)
                    intersections = new LinkedList<>(tempIntersections);
                else
                    intersections.addAll(tempIntersections);
            }
        }

        return intersections;
    }
}
