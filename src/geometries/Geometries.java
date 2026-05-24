package geometries;

import java.util.LinkedList;
import java.util.List;

import geometries.acceleration.AccelerationType;
import geometries.acceleration.BoundingBox;
import geometries.acceleration.GeometryIndex;
import geometries.acceleration.GeometryIndexes;
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
     * Minimum bounded geometry count for using BVH instead of linear traversal.
     */
    private static final int BVH_THRESHOLD = 4;

    /**
     * A private unmodifiable field of a list of geometric objects, initialized with an empty list
     */
    private final List<Intersectable> geometries = new LinkedList<>();

    /**
     * Lazily rebuilt geometry index.
     */
    private GeometryIndex index;

    /**
     * Geometry acceleration mode.
     */
    private AccelerationType accelerationType = AccelerationType.AUTO;

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
        index = null;
    }

    /**
     * Sets the internal acceleration mode.
     * Intended for performance benchmarking and debugging.
     *
     * @param accelerationType acceleration mode
     * @return the current geometry collection
     */
    public Geometries setAcceleration(AccelerationType accelerationType) {
        if (accelerationType == null)
            throw new IllegalArgumentException("Acceleration type cannot be null");
        this.accelerationType = accelerationType;
        index = null;
        return this;
    }

    @Override
    protected List<GeoPoint> findGeoIntersectionsHelper(Ray ray, double maxDistance) {
        return getIndex().findGeoIntersections(ray, maxDistance);
    }

    /**
     * Finds the closest geometry intersection with a ray.
     *
     * @param ray the ray
     * @return closest intersection, or null if none exists
     */
    public GeoPoint findClosestGeoIntersection(Ray ray) {
        return findClosestGeoIntersection(ray, Double.POSITIVE_INFINITY);
    }

    /**
     * Finds the closest geometry intersection with a ray up to a maximum distance.
     *
     * @param ray         the ray
     * @param maxDistance maximum intersection distance
     * @return closest intersection, or null if none exists
     */
    public GeoPoint findClosestGeoIntersection(Ray ray, double maxDistance) {
        return getIndex().findClosestGeoIntersection(ray, maxDistance);
    }

    @Override
    BoundingBox getBoundingBox() {
        if (geometries.isEmpty()) return null;

        BoundingBox box = null;
        for (Intersectable geometry : geometries) {
            BoundingBox geometryBox = geometry.getBoundingBox();
            if (geometryBox == null) return null;
            box = box == null ? geometryBox : box.union(geometryBox);
        }

        return box;
    }

    /**
     * Gets the current geometry index, rebuilding it lazily if needed.
     * The index is invalidated whenever geometries are added or the acceleration
     * mode changes, so intersection queries always reflect the current collection.
     *
     * @return active geometry index
     */
    private GeometryIndex getIndex() {
        if (index == null) index = buildIndex();
        return index;
    }

    /**
     * Builds the best internal index for the current geometry set.
     * In AUTO mode, small or mostly unbounded collections stay linear to avoid
     * BVH build overhead; larger bounded collections use BVH. Explicit modes are
     * used for benchmarking and debugging comparisons.
     *
     * @return newly built geometry index
     */
    private GeometryIndex buildIndex() {
        return GeometryIndexes.build(geometries, accelerationType, BVH_THRESHOLD, geometry -> geometry.getBoundingBox());
    }
}
