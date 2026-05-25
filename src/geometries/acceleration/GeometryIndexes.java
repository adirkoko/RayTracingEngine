package geometries.acceleration;

import geometries.Intersectable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static primitives.Util.isZero;

/**
 * Factory for internal geometry acceleration indexes.
 */
public final class GeometryIndexes {

    /**
     * Minimum bounded geometry count for considering the regular grid in AUTO mode.
     */
    private static final int GRID_THRESHOLD = 24;

    /**
     * Small bounded collections where BVH build/traversal is still cheap and predictable.
     */
    private static final int SMALL_ACCELERATION_LIMIT = 8;

    /**
     * Maximum bounded count where large receiver surfaces usually favor linear traversal.
     */
    private static final int LARGE_SURFACE_LINEAR_LIMIT = 64;

    /**
     * Large object span threshold relative to the full scene bounds.
     */
    private static final double LARGE_SPAN_RATIO = 0.45;

    /**
     * Thin object span threshold relative to the full scene bounds.
     */
    private static final double THIN_SPAN_RATIO = 0.03;

    /**
     * Minimum large-span object ratio for treating a modest scene as receiver-heavy.
     */
    private static final double LARGE_SURFACE_LINEAR_RATIO = 0.08;

    /**
     * Minimum large-span object count for treating a modest scene as receiver-heavy.
     */
    private static final int LARGE_SURFACE_LINEAR_COUNT = 2;

    /**
     * Maximum large-span object ratio for choosing GRID automatically.
     */
    private static final double MAX_GRID_LARGE_SPAN_RATIO = 0.15;

    /**
     * Coarse bucket resolution used only for AUTO scene-distribution classification.
     */
    private static final int COARSE_BUCKETS = 4;

    /**
     * Minimum occupied coarse-bucket ratio for grid-friendly spatial distribution.
     */
    private static final double MIN_GRID_OCCUPANCY_RATIO = 0.10;

    /**
     * Maximum average center count per occupied coarse bucket for GRID.
     */
    private static final double MAX_GRID_BUCKET_LOAD = 5.0;

    /**
     * Don't let anyone instantiate this utility class.
     */
    private GeometryIndexes() {
    }

    /**
     * Builds a geometry index according to the requested acceleration mode.
     * Explicit modes force the selected traversal strategy. AUTO mode uses a
     * conservative scene-shape heuristic: small or receiver-heavy scenes stay
     * linear, uniformly distributed bounded scenes use GRID, and dense or
     * clustered bounded scenes use BVH.
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
        return buildResolved(geometries, type, bvhThreshold, boxResolver).index();
    }

    /**
     * Builds a geometry index and reports the acceleration type selected in practice.
     * For explicit modes the resolved type is the requested type. For AUTO it is
     * the type selected by the automatic scene-shape heuristic.
     *
     * @param geometries     geometries to index
     * @param type           requested acceleration mode
     * @param bvhThreshold   minimum bounded geometry count for AUTO BVH
     * @param boxResolver    function that returns a geometry's finite bounding box
     * @return index selection with the built index and resolved acceleration type
     */
    public static Selection buildResolved(
            List<Intersectable> geometries,
            AccelerationType type,
            int bvhThreshold,
            Function<Intersectable, BoundingBox> boxResolver) {
        if (type == AccelerationType.LINEAR)
            return new Selection(new LinearGeometryIndex(geometries), AccelerationType.LINEAR);

        List<GeometryEntry> bounded = new ArrayList<>();
        List<Intersectable> unbounded = new ArrayList<>();

        for (Intersectable geometry : geometries) {
            BoundingBox box = boxResolver.apply(geometry);
            if (box == null)
                unbounded.add(geometry);
            else
                bounded.add(new GeometryEntry(geometry, box));
        }

        if (type == AccelerationType.GRID)
            return new Selection(new RegularGrid(bounded, unbounded), AccelerationType.GRID);
        if (type == AccelerationType.BVH)
            return new Selection(new BvhGeometryIndex(bounded, unbounded), AccelerationType.BVH);

        AccelerationType resolvedType = chooseAutoType(bounded, unbounded, bvhThreshold);
        GeometryIndex index = switch (resolvedType) {
            case LINEAR -> new LinearGeometryIndex(geometries);
            case GRID -> new RegularGrid(bounded, unbounded);
            default -> new BvhGeometryIndex(bounded, unbounded);
        };
        return new Selection(index, resolvedType);
    }

    /**
     * Geometry index build result.
     *
     * @param index            geometry index
     * @param accelerationType acceleration type selected in practice
     */
    public record Selection(GeometryIndex index, AccelerationType accelerationType) {
    }

    /**
     * Chooses an automatic acceleration type from pre-classified geometry data.
     * The heuristic is intentionally conservative because a bad GRID choice can
     * be much worse than a slightly imperfect BVH choice.
     *
     * @param bounded      bounded geometry entries
     * @param unbounded    unbounded geometries
     * @param bvhThreshold minimum bounded geometry count for acceleration
     * @return selected acceleration type
     */
    static AccelerationType chooseAutoType(
            List<GeometryEntry> bounded,
            List<Intersectable> unbounded,
            int bvhThreshold) {
        int boundedCount = bounded.size();
        if (boundedCount == 0) return AccelerationType.LINEAR;
        if (boundedCount < bvhThreshold) return AccelerationType.LINEAR;
        if (boundedCount <= Math.max(bvhThreshold, SMALL_ACCELERATION_LIMIT)) return AccelerationType.BVH;
        if (!unbounded.isEmpty() && boundedCount <= LARGE_SURFACE_LINEAR_LIMIT) return AccelerationType.LINEAR;

        AutoStats stats = AutoStats.from(bounded);
        if (isReceiverHeavy(stats) && boundedCount <= LARGE_SURFACE_LINEAR_LIMIT)
            return AccelerationType.LINEAR;

        return isGridFriendly(stats) ? AccelerationType.GRID : AccelerationType.BVH;
    }

    /**
     * Checks whether a bounded scene is a good candidate for regular-grid traversal.
     * GRID is selected only for sufficiently distributed bounded objects, without
     * large receiver surfaces that would be duplicated into many voxels.
     *
     * @param stats automatic-selection statistics
     * @return true when GRID is a good AUTO choice
     */
    private static boolean isGridFriendly(AutoStats stats) {
        return stats.boundedCount() >= GRID_THRESHOLD
                && stats.largeSpanRatio() <= MAX_GRID_LARGE_SPAN_RATIO
                && stats.occupancyRatio() >= MIN_GRID_OCCUPANCY_RATIO
                && stats.averageBucketLoad() <= MAX_GRID_BUCKET_LOAD;
    }

    /**
     * Checks whether a modest bounded scene is dominated by large receiver-like objects.
     * A single large object is not enough; the scene needs both a meaningful count
     * and ratio of large-span boxes before AUTO falls back to LINEAR.
     *
     * @param stats automatic-selection statistics
     * @return true when large receiver-like boxes dominate enough to prefer LINEAR
     */
    private static boolean isReceiverHeavy(AutoStats stats) {
        return stats.largeFlatSpanCount() >= LARGE_SURFACE_LINEAR_COUNT
                && stats.largeFlatSpanRatio() >= LARGE_SURFACE_LINEAR_RATIO;
    }

    /**
     * Automatic-selection statistics derived only from bounding boxes.
     *
     * @param boundedCount      bounded geometry count
     * @param largeSpanCount    number of objects spanning a large part of the scene box
     * @param largeFlatSpanCount number of flat objects spanning a large part of the scene box
     * @param largeSpanRatio    ratio of objects spanning a large part of the scene box
     * @param largeFlatSpanRatio ratio of flat objects spanning a large part of the scene box
     * @param occupancyRatio    occupied coarse bucket ratio
     * @param averageBucketLoad average center count per occupied coarse bucket
     */
    private record AutoStats(
            int boundedCount,
            int largeSpanCount,
            int largeFlatSpanCount,
            double largeSpanRatio,
            double largeFlatSpanRatio,
            double occupancyRatio,
            double averageBucketLoad) {

        /**
         * Builds AUTO statistics for bounded geometry entries.
         *
         * @param bounded bounded geometry entries
         * @return automatic-selection statistics
         */
        static AutoStats from(List<GeometryEntry> bounded) {
            if (bounded.isEmpty()) return new AutoStats(0, 0, 0, 0, 0, 0, 0);

            BoundingBox union = unionBox(bounded);
            Set<Integer> occupiedBuckets = new HashSet<>();
            int largeSpanCount = 0;
            int largeFlatSpanCount = 0;

            for (GeometryEntry entry : bounded) {
                BoundingBox box = entry.box();
                if (hasLargeSpan(box, union)) largeSpanCount++;
                if (hasLargeFlatSpan(box, union)) largeFlatSpanCount++;
                occupiedBuckets.add(bucketKey(box, union));
            }

            int occupiedCount = occupiedBuckets.size();
            return new AutoStats(
                    bounded.size(),
                    largeSpanCount,
                    largeFlatSpanCount,
                    largeSpanCount / (double) bounded.size(),
                    largeFlatSpanCount / (double) bounded.size(),
                    occupiedCount / (double) (COARSE_BUCKETS * COARSE_BUCKETS * COARSE_BUCKETS),
                    bounded.size() / (double) occupiedCount);
        }

        /**
         * Computes the union box for bounded entries.
         *
         * @param bounded bounded entries
         * @return union bounding box
         */
        private static BoundingBox unionBox(List<GeometryEntry> bounded) {
            BoundingBox union = null;
            for (GeometryEntry entry : bounded)
                union = union == null ? entry.box() : union.union(entry.box());
            return union;
        }

        /**
         * Checks whether a box spans a large part of the full scene bounds.
         *
         * @param box   geometry box
         * @param union full bounded scene box
         * @return true when the geometry is large relative to the scene bounds
         */
        private static boolean hasLargeSpan(BoundingBox box, BoundingBox union) {
            for (int axis = 0; axis < 3; axis++) {
                double unionExtent = extent(union, axis);
                if (!isZero(unionExtent) && extent(box, axis) / unionExtent >= LARGE_SPAN_RATIO)
                    return true;
            }

            return false;
        }

        /**
         * Checks whether a box is both large and flat relative to the full scene bounds.
         *
         * @param box   geometry box
         * @param union full bounded scene box
         * @return true when the box looks like a large receiver surface
         */
        private static boolean hasLargeFlatSpan(BoundingBox box, BoundingBox union) {
            return hasLargeSpan(box, union) && hasThinSpan(box, union);
        }

        /**
         * Checks whether a box has at least one thin axis relative to the scene bounds.
         *
         * @param box   geometry box
         * @param union full bounded scene box
         * @return true when the box is thin on at least one axis
         */
        private static boolean hasThinSpan(BoundingBox box, BoundingBox union) {
            for (int axis = 0; axis < 3; axis++) {
                double boxExtent = extent(box, axis);
                double unionExtent = extent(union, axis);
                if (isZero(boxExtent) || (!isZero(unionExtent) && boxExtent / unionExtent <= THIN_SPAN_RATIO))
                    return true;
            }

            return false;
        }

        /**
         * Gets the coarse bucket key for a box center.
         *
         * @param box   geometry box
         * @param union full bounded scene box
         * @return encoded bucket key
         */
        private static int bucketKey(BoundingBox box, BoundingBox union) {
            int x = bucketCoordinate(box.center(0), union, 0);
            int y = bucketCoordinate(box.center(1), union, 1);
            int z = bucketCoordinate(box.center(2), union, 2);
            return x * COARSE_BUCKETS * COARSE_BUCKETS + y * COARSE_BUCKETS + z;
        }

        /**
         * Gets one coarse bucket coordinate for a center coordinate.
         *
         * @param center center coordinate
         * @param union  full bounded scene box
         * @param axis   axis index
         * @return clamped bucket coordinate
         */
        private static int bucketCoordinate(double center, BoundingBox union, int axis) {
            double extent = extent(union, axis);
            if (isZero(extent)) return 0;

            int bucket = (int) ((center - coordinate(union.getMin(), axis)) / extent * COARSE_BUCKETS);
            return Math.max(0, Math.min(COARSE_BUCKETS - 1, bucket));
        }

        /**
         * Gets the extent of a box on one axis.
         *
         * @param box  bounding box
         * @param axis axis index
         * @return axis extent
         */
        private static double extent(BoundingBox box, int axis) {
            return coordinate(box.getMax(), axis) - coordinate(box.getMin(), axis);
        }

        /**
         * Gets one point coordinate by axis.
         *
         * @param point point
         * @param axis  axis index
         * @return axis coordinate
         */
        private static double coordinate(primitives.Point point, int axis) {
            return switch (axis) {
                case 0 -> point.getX();
                case 1 -> point.getY();
                default -> point.getZ();
            };
        }
    }
}
