package geometries.acceleration;

import geometries.Intersectable;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

/**
 * Factory for internal geometry acceleration indexes.
 */
public final class GeometryIndexes {

    /**
     * Don't let anyone instantiate this utility class.
     */
    private GeometryIndexes() {
    }

    /**
     * Builds a geometry index according to the requested acceleration mode.
     * AUTO mode uses the BVH only when the bounded geometry count reaches the
     * supplied threshold; explicit modes force the selected traversal strategy.
     *
     * @param geometries     geometries to index
     * @param type           requested acceleration mode
     * @param bvhThreshold   minimum bounded geometry count for AUTO BVH
     * @param boxResolver    function that returns a geometry's finite bounding box
     * @return newly built geometry index
     */
    public static GeometryIndex build(
            List<Intersectable> geometries,
            AccelerationType type,
            int bvhThreshold,
            Function<Intersectable, BoundingBox> boxResolver) {
        if (type == AccelerationType.LINEAR) return new LinearGeometryIndex(geometries);

        List<GeometryEntry> bounded = new LinkedList<>();
        List<Intersectable> unbounded = new LinkedList<>();

        for (Intersectable geometry : geometries) {
            BoundingBox box = boxResolver.apply(geometry);
            if (box == null)
                unbounded.add(geometry);
            else
                bounded.add(new GeometryEntry(geometry, box));
        }

        if (type == AccelerationType.GRID) return new RegularGrid(bounded, unbounded);

        return type == AccelerationType.BVH || bounded.size() >= bvhThreshold
                ? new BvhGeometryIndex(bounded, unbounded)
                : new LinearGeometryIndex(geometries);
    }
}
