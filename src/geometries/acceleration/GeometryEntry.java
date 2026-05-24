package geometries.acceleration;

import geometries.Intersectable;

/**
 * Bounded geometry entry used by acceleration structures.
 *
 * @param geometry geometry object
 * @param box      geometry bounding box
 */
record GeometryEntry(Intersectable geometry, BoundingBox box) {
}
