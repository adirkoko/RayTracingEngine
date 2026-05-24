package geometries.acceleration;

import geometries.Intersectable;
import geometries.Intersectable.GeoPoint;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static primitives.Util.alignZero;
import static primitives.Util.isZero;

/**
 * Regular voxel grid acceleration index using 3D-DDA traversal.
 */
class RegularGrid implements GeometryIndex {

    /**
     * Grid bounds enclosing all bounded entries.
     */
    private final BoundingBox box;

    /**
     * Linear fallback for unbounded geometries.
     */
    private final LinearGeometryIndex unboundedIndex;

    /**
     * Voxel to bounded geometry entries map.
     */
    private final Map<VoxelIndex, List<GeometryEntry>> voxels = new HashMap<>();

    /**
     * Number of cells along the x axis.
     */
    private final int sizeX;

    /**
     * Number of cells along the y axis.
     */
    private final int sizeY;

    /**
     * Number of cells along the z axis.
     */
    private final int sizeZ;

    /**
     * Cell size along the x axis.
     */
    private final double cellSizeX;

    /**
     * Cell size along the y axis.
     */
    private final double cellSizeY;

    /**
     * Cell size along the z axis.
     */
    private final double cellSizeZ;

    /**
     * Constructs a regular grid from pre-classified geometry groups.
     *
     * @param bounded   bounded geometry entries
     * @param unbounded unbounded geometries
     */
    RegularGrid(List<GeometryEntry> bounded, List<Intersectable> unbounded) {
        unboundedIndex = new LinearGeometryIndex(unbounded);

        box = unionBox(bounded);
        int[] dimensions = box == null ? new int[]{1, 1, 1} : dimensions(bounded.size(), box);
        sizeX = dimensions[0];
        sizeY = dimensions[1];
        sizeZ = dimensions[2];
        cellSizeX = cellSize(box, 0, sizeX);
        cellSizeY = cellSize(box, 1, sizeY);
        cellSizeZ = cellSize(box, 2, sizeZ);

        for (GeometryEntry entry : bounded) addEntry(entry);
    }

    @Override
    public List<GeoPoint> findGeoIntersections(Ray ray, double maxDistance) {
        List<GeoPoint> intersections = unboundedIndex.findGeoIntersections(ray, maxDistance);
        if (box == null) return intersections;

        double[] interval = box.intersectionInterval(ray, maxDistance);
        if (interval == null) return intersections;

        Set<GeometryEntry> checked = new HashSet<>();
        for (TraversedVoxel voxel : traverse(ray, interval)) {
            List<GeometryEntry> entries = voxels.get(voxel.index());
            if (entries == null) continue;

            for (GeometryEntry entry : entries) {
                if (!checked.add(entry) || !entry.box().intersects(ray, maxDistance)) continue;

                var entryIntersections = entry.geometry().findGeoIntersections(ray, maxDistance);
                if (entryIntersections != null) {
                    if (intersections == null)
                        intersections = new LinkedList<>(entryIntersections);
                    else
                        intersections.addAll(entryIntersections);
                }
            }
        }

        return intersections;
    }

    @Override
    public GeoPoint findClosestGeoIntersection(Ray ray, double maxDistance) {
        GeoPoint closest = unboundedIndex.findClosestGeoIntersection(ray, maxDistance);
        double closestDistance = closest == null ? maxDistance : closest.point.distance(ray.getHead());
        if (box == null) return closest;

        double[] interval = box.intersectionInterval(ray, closestDistance);
        if (interval == null) return closest;

        Set<GeometryEntry> checked = new HashSet<>();
        for (TraversedVoxel voxel : traverse(ray, interval)) {
            if (alignZero(voxel.enter() - closestDistance) > 0) break;

            List<GeometryEntry> entries = voxels.get(voxel.index());
            if (entries != null) {
                for (GeometryEntry entry : entries) {
                    if (!checked.add(entry) || !entry.box().intersects(ray, closestDistance)) continue;

                    var entryIntersections = entry.geometry().findGeoIntersections(ray, closestDistance);
                    if (entryIntersections == null) continue;

                    for (GeoPoint geoPoint : entryIntersections) {
                        double distance = geoPoint.point.distance(ray.getHead());
                        if (distance < closestDistance) {
                            closestDistance = distance;
                            closest = geoPoint;
                        }
                    }
                }
            }

            if (alignZero(closestDistance - voxel.exit()) <= 0) break;
        }

        return closest;
    }

    /**
     * Adds one bounded geometry entry to every voxel overlapped by its box.
     *
     * @param entry bounded geometry entry
     */
    private void addEntry(GeometryEntry entry) {
        VoxelIndex min = voxelAt(entry.box().getMin());
        VoxelIndex max = voxelAt(entry.box().getMax());

        for (int x = min.x(); x <= max.x(); x++) {
            for (int y = min.y(); y <= max.y(); y++) {
                for (int z = min.z(); z <= max.z(); z++) {
                    voxels.computeIfAbsent(new VoxelIndex(x, y, z), ignored -> new LinkedList<>()).add(entry);
                }
            }
        }
    }

    /**
     * Traverses grid voxels with 3D-DDA.
     *
     * @param ray      ray to traverse
     * @param interval grid overlap interval
     * @return ordered traversed voxels
     */
    private List<TraversedVoxel> traverse(Ray ray, double[] interval) {
        List<TraversedVoxel> traversed = new LinkedList<>();
        Point point = ray.getPoint(interval[0]);
        Vector direction = ray.getDirection();

        int x = voxelCoordinate(point.getX(), 0);
        int y = voxelCoordinate(point.getY(), 1);
        int z = voxelCoordinate(point.getZ(), 2);

        int stepX = step(direction.getX());
        int stepY = step(direction.getY());
        int stepZ = step(direction.getZ());

        double tMaxX = nextBoundaryDistance(ray, x, stepX, 0);
        double tMaxY = nextBoundaryDistance(ray, y, stepY, 1);
        double tMaxZ = nextBoundaryDistance(ray, z, stepZ, 2);
        double tDeltaX = deltaDistance(direction.getX(), cellSizeX, sizeX);
        double tDeltaY = deltaDistance(direction.getY(), cellSizeY, sizeY);
        double tDeltaZ = deltaDistance(direction.getZ(), cellSizeZ, sizeZ);

        double enter = interval[0];
        while (isInside(x, y, z) && alignZero(enter - interval[1]) <= 0) {
            double exit = Math.min(interval[1], Math.min(tMaxX, Math.min(tMaxY, tMaxZ)));
            traversed.add(new TraversedVoxel(new VoxelIndex(x, y, z), enter, exit));
            if (alignZero(exit - interval[1]) >= 0) break;

            if (tMaxX <= tMaxY && tMaxX <= tMaxZ) {
                x += stepX;
                enter = tMaxX;
                tMaxX += tDeltaX;
            } else if (tMaxY <= tMaxZ) {
                y += stepY;
                enter = tMaxY;
                tMaxY += tDeltaY;
            } else {
                z += stepZ;
                enter = tMaxZ;
                tMaxZ += tDeltaZ;
            }
        }

        return traversed;
    }

    /**
     * Computes the union box for bounded entries.
     *
     * @param entries bounded geometry entries
     * @return union box, or null when there are no bounded entries
     */
    private BoundingBox unionBox(List<GeometryEntry> entries) {
        if (entries.isEmpty()) return null;

        BoundingBox union = entries.getFirst().box();
        for (int i = 1; i < entries.size(); i++) union = union.union(entries.get(i).box());
        return union;
    }

    /**
     * Chooses grid dimensions from scene bounds and geometry count.
     *
     * @param entryCount number of bounded entries
     * @param box        grid bounds
     * @return grid dimensions
     */
    private int[] dimensions(int entryCount, BoundingBox box) {
        double dx = extent(box, 0);
        double dy = extent(box, 1);
        double dz = extent(box, 2);
        double max = Math.max(dx, Math.max(dy, dz));
        int base = Math.max(1, (int) Math.ceil(Math.cbrt(entryCount)));

        return new int[]{
                dimensionFor(dx, max, base),
                dimensionFor(dy, max, base),
                dimensionFor(dz, max, base)};
    }

    /**
     * Chooses one axis dimension.
     *
     * @param extent axis extent
     * @param max    longest extent
     * @param base   base cell count
     * @return axis cell count
     */
    private int dimensionFor(double extent, double max, int base) {
        return isZero(extent) || isZero(max) ? 1 : Math.max(1, (int) Math.ceil(base * extent / max));
    }

    /**
     * Gets one axis extent for a box.
     *
     * @param box  bounding box
     * @param axis axis index
     * @return axis extent
     */
    private double extent(BoundingBox box, int axis) {
        return coordinate(box.getMax(), axis) - coordinate(box.getMin(), axis);
    }

    /**
     * Gets one axis cell size.
     *
     * @param box       grid bounds
     * @param axis      axis index
     * @param dimension axis cell count
     * @return cell size
     */
    private double cellSize(BoundingBox box, int axis, int dimension) {
        return box == null || dimension == 1 ? extentOrOne(box, axis) : extent(box, axis) / dimension;
    }

    /**
     * Gets an axis extent or a harmless positive size for degenerate axes.
     *
     * @param box  grid bounds
     * @param axis axis index
     * @return non-zero size
     */
    private double extentOrOne(BoundingBox box, int axis) {
        return box == null || isZero(extent(box, axis)) ? 1 : extent(box, axis);
    }

    /**
     * Gets the voxel containing a point.
     *
     * @param point point inside or on the grid bounds
     * @return voxel index
     */
    private VoxelIndex voxelAt(Point point) {
        return new VoxelIndex(
                voxelCoordinate(point.getX(), 0),
                voxelCoordinate(point.getY(), 1),
                voxelCoordinate(point.getZ(), 2));
    }

    /**
     * Converts an axis coordinate to a clamped voxel coordinate.
     *
     * @param coordinate axis coordinate
     * @param axis       axis index
     * @return voxel coordinate
     */
    private int voxelCoordinate(double coordinate, int axis) {
        int dimension = dimension(axis);
        if (dimension == 1) return 0;

        int voxel = (int) Math.floor((coordinate - coordinate(box.getMin(), axis)) / cellSize(axis));
        return Math.max(0, Math.min(dimension - 1, voxel));
    }

    /**
     * Calculates the distance to the next voxel boundary on one axis.
     *
     * @param ray   ray to traverse
     * @param voxel current voxel coordinate
     * @param step  traversal step
     * @param axis  axis index
     * @return distance to next boundary
     */
    private double nextBoundaryDistance(Ray ray, int voxel, int step, int axis) {
        if (step == 0 || dimension(axis) == 1) return Double.POSITIVE_INFINITY;

        double boundary = coordinate(box.getMin(), axis) + (voxel + (step > 0 ? 1 : 0)) * cellSize(axis);
        return (boundary - coordinate(ray.getHead(), axis)) / coordinate(ray.getDirection(), axis);
    }

    /**
     * Calculates the distance between voxel boundary crossings on one axis.
     *
     * @param direction axis direction
     * @param cellSize  axis cell size
     * @param dimension axis cell count
     * @return boundary crossing distance
     */
    private double deltaDistance(double direction, double cellSize, int dimension) {
        return isZero(direction) || dimension == 1 ? Double.POSITIVE_INFINITY : Math.abs(cellSize / direction);
    }

    /**
     * Gets direction step for one axis.
     *
     * @param direction axis direction
     * @return -1, 0, or 1
     */
    private int step(double direction) {
        return isZero(direction) ? 0 : direction > 0 ? 1 : -1;
    }

    /**
     * Checks whether voxel coordinates are inside the grid.
     *
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     * @return true when inside the grid
     */
    private boolean isInside(int x, int y, int z) {
        return x >= 0 && x < sizeX && y >= 0 && y < sizeY && z >= 0 && z < sizeZ;
    }

    /**
     * Gets one axis coordinate from a point.
     *
     * @param point point
     * @param axis  axis index
     * @return axis coordinate
     */
    private double coordinate(Point point, int axis) {
        return switch (axis) {
            case 0 -> point.getX();
            case 1 -> point.getY();
            default -> point.getZ();
        };
    }

    /**
     * Gets one axis dimension.
     *
     * @param axis axis index
     * @return axis dimension
     */
    private int dimension(int axis) {
        return switch (axis) {
            case 0 -> sizeX;
            case 1 -> sizeY;
            default -> sizeZ;
        };
    }

    /**
     * Gets one axis cell size.
     *
     * @param axis axis index
     * @return axis cell size
     */
    private double cellSize(int axis) {
        return switch (axis) {
            case 0 -> cellSizeX;
            case 1 -> cellSizeY;
            default -> cellSizeZ;
        };
    }

    /**
     * Traversed voxel segment.
     *
     * @param index voxel index
     * @param enter entry distance into the voxel
     * @param exit  exit distance from the voxel
     */
    private record TraversedVoxel(VoxelIndex index, double enter, double exit) {
    }
}
