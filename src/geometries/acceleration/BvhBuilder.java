package geometries.acceleration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Builds a simple median-split bounding volume hierarchy.
 */
class BvhBuilder {

    /**
     * Maximum number of geometries in a leaf node.
     */
    private static final int LEAF_SIZE = 4;

    /**
     * Builds a BVH node from bounded geometry entries.
     *
     * @param entries bounded geometry entries
     * @return root node
     */
    BvhNode build(List<GeometryEntry> entries) {
        return buildNode(new ArrayList<>(entries));
    }

    /**
     * Recursively builds a BVH node for a bounded geometry subset.
     * The method first computes the union box for all entries. Small subsets
     * become leaf nodes; larger subsets are sorted by the center of their boxes
     * along the node's longest axis and split at the median.
     *
     * @param entries bounded geometry entries for the current subtree
     * @return leaf or internal BVH node enclosing all given entries
     */
    private BvhNode buildNode(List<GeometryEntry> entries) {
        BoundingBox box = entries.getFirst().box();
        for (int i = 1; i < entries.size(); i++) box = box.union(entries.get(i).box());

        if (entries.size() <= LEAF_SIZE) return new BvhNode(box, entries);

        int axis = box.longestAxis();
        entries.sort(Comparator.comparingDouble(entry -> entry.box().center(axis)));

        int middle = entries.size() / 2;
        return new BvhNode(
                box,
                buildNode(new ArrayList<>(entries.subList(0, middle))),
                buildNode(new ArrayList<>(entries.subList(middle, entries.size()))));
    }
}
