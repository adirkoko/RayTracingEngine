# Ray Tracing Engine API Reference

This document describes the public API used to build scenes, configure rendering, produce images, and collect render progress data. It is written for users who want to use the engine seriously, not only run the quick-start example.

## Who Should Read This

If you only want to render the demo scene, start with the project `README.md`. If you want to build custom scenes, tune image quality, collect metrics, compare render runs, or extend the renderer, this document is for you.

The main workflow is:

```text
primitives -> geometries/materials/lights -> scene -> camera -> ray tracer -> image/progress/manifest
```

## Requirements

- JDK 21 or newer
- Maven 3.9 or newer

## Public API Boundary

Most applications should use classes from `primitives`, `geometries`, `lighting`, `scene`, `renderer`, and optionally `metrics`. Classes under acceleration internals and renderer internals are documented only for extension, debugging, and architecture understanding.

## Contents

- [Requirements](#requirements)
- [Public API Boundary](#public-api-boundary)
- [Minimal Render Workflow](#minimal-render-workflow)
- [Package Map](#package-map)
- [Core Primitives](#core-primitives)
- [Materials And Global Effects](#materials-and-global-effects)
- [Geometry API](#geometry-api)
- [Geometry Collections And Acceleration](#geometry-collections-and-acceleration)
- [Scene API](#scene-api)
- [Lighting API](#lighting-api)
- [Camera And Rendering API](#camera-and-rendering-api)
- [Progress, Metrics, And Manifests](#progress-metrics-and-manifests)
- [XML Scene Loading](#xml-scene-loading)
- [Sampling Utilities](#sampling-utilities)
- [Error Handling](#error-handling)
- [Common Recipes](#common-recipes)
- [Best Practices](#best-practices)
- [Advanced And Internal APIs](#advanced-and-internal-apis)
- [Build And Test Commands](#build-and-test-commands)
- [Output Summary](#output-summary)

---

## Minimal Render Workflow

```java
import geometries.Sphere;
import lighting.AmbientLight;
import lighting.PointLight;
import primitives.Color;
import primitives.Material;
import primitives.Point;
import primitives.Vector;
import renderer.Camera;
import renderer.ImageWriter;
import renderer.SimpleRayTracer;
import scene.Scene;

Scene scene = new Scene("Demo")
        .setBackground(new Color(0, 0, 0))
        .setAmbientLight(new AmbientLight(new Color(20, 20, 20), 1.0));

scene.geometries.add(
        new Sphere(new Point(0, 0, -100), 50)
                .setEmission(new Color(50, 20, 20))
                .setMaterial(new Material()
                        .setKd(0.6)
                        .setKs(0.4)
                        .setShininess(100))
);

scene.lights.add(
        new PointLight(new Color(400, 400, 400), new Point(50, 50, 0))
                .setKl(0.0005)
                .setKq(0.0005)
);

Camera camera = Camera.getBuilder()
        .setLocation(new Point(0, 0, 0))
        .setDirection(new Vector(0, 0, -1), new Vector(0, 1, 0))
        .setVpDistance(100)
        .setVpSize(200, 200)
        .setImageWriter(new ImageWriter("demo", 800, 800))
        .setRayTracer(new SimpleRayTracer(scene))
        .build();

camera.renderImage()
        .writeToImage();
```

The image is written to:

```text
images/demo.png
```

---

## Package Map

| Package | Purpose |
|---|---|
| `primitives` | Math and render data: points, vectors, rays, colors, material coefficients |
| `geometries` | Shapes, geometry collections, intersection queries |
| `geometries.acceleration` | Acceleration selection and low-level indexes. Use mainly `AccelerationType` through `Geometries` |
| `lighting` | Ambient, directional, point, spot, and sampled light APIs |
| `scene` | Scene composition and basic XML loading |
| `renderer` | Camera, ray tracers, image writer, progress events, manifests |
| `metrics` | Optional CSV and SQLite render progress persistence |
| `sampling` | Reusable sample generators for advanced rendering extensions |

---

## Core Primitives

### `Point`

Represents a point in 3D space.

```java
Point p = new Point(1, 2, 3);
Point origin = Point.ZERO;
```

Important methods:

| Method | Meaning |
|---|---|
| `getX()`, `getY()`, `getZ()` | Coordinate access |
| `subtract(Point other)` | Returns a `Vector` from `other` to this point |
| `add(Vector other)` | Moves the point by a vector |
| `distanceSquared(Point other)` | Squared distance |
| `distance(Point other)` | Euclidean distance |

### `Vector`

Represents a non-zero vector. `Vector` extends `Point` but should be treated semantically as a direction or displacement.

```java
Vector forward = new Vector(0, 0, -1);
Vector unit = forward.normalize();
```

Important methods:

| Method | Meaning |
|---|---|
| `add(Vector other)` | Vector addition |
| `scale(double scalar)` | Scalar multiplication |
| `dotProduct(Vector other)` | Dot product |
| `crossProduct(Vector other)` | Cross product |
| `lengthSquared()`, `length()` | Vector length |
| `normalize()` | Unit vector |

Validation:

- A zero vector is rejected.

### `Ray`

Represents a ray with a head point and normalized direction.

```java
Ray ray = new Ray(new Point(0, 0, 0), new Vector(0, 0, -1));
Ray offsetRay = new Ray(point, direction, surfaceNormal);
```

Constructors:

| Constructor | Use |
|---|---|
| `Ray(Point head, Vector direction)` | General ray. Direction is normalized |
| `Ray(Point head, Vector direction, Vector normal)` | Offsets the ray head along the normal to avoid self-intersections |

Important methods:

| Method | Meaning |
|---|---|
| `getHead()` | Ray origin |
| `getDirection()` | Normalized direction |
| `getPoint(double t)` | Point at distance `t` along the ray |
| `findClosestPoint(List<Point>)` | Closest point to the ray head |
| `findClosestGeoPoint(List<GeoPoint>)` | Closest geometry intersection |

### `Color`

Stores RGB values as doubles. Components may exceed `255` during lighting calculations; `getColor()` clamps to Java `java.awt.Color`.

```java
Color red = new Color(255, 0, 0);
Color bright = new Color(500, 300, 100);
```

Important methods:

| Method | Meaning |
|---|---|
| `Color.BLACK` | Constant black |
| `new Color(double r, double g, double b)` | RGB constructor |
| `new Color(java.awt.Color other)` | Convert from AWT color |
| `getColor()` | Convert to clamped `java.awt.Color` |
| `add(Color... colors)` | Component-wise addition |
| `scale(double k)` | Scalar scale |
| `scale(Double3 k)` | Per-channel scale |
| `reduce(int k)` | Divide by an integer factor |
| `isSimilar(Color other, double tolerance)` | Component-wise tolerance check |

Validation:

- Color components cannot be negative.
- Scale factors cannot be negative.
- `reduce(k)` requires `k >= 1`.

### `Double3`

Represents three double values, usually RGB coefficients or per-channel material factors.

```java
Double3 uniform = new Double3(0.5);
Double3 rgb = new Double3(0.6, 0.4, 0.2);
```

Important constants and methods:

| API | Meaning |
|---|---|
| `Double3.ZERO` | `(0, 0, 0)` |
| `Double3.ONE` | `(1, 1, 1)` |
| `add`, `subtract`, `scale`, `reduce`, `product` | Component-wise math |
| `lowerThan(double)` | True when all components are below a scalar |
| `lowerThan(Double3)` | True when each component is below the matching component |

---

## Materials And Global Effects

`Material` stores surface coefficients. Materials are configured fluently and attached to geometries with `Geometry#setMaterial(...)`.

```java
Material material = new Material()
        .setKd(0.6)
        .setKs(0.4)
        .setShininess(100)
        .setKr(0.2)
        .setKt(0.1);
```

### Material Defaults

| Field | Default | Meaning |
|---|---:|---|
| `kD` | `Double3.ZERO` | Diffuse coefficient |
| `kS` | `Double3.ZERO` | Specular coefficient |
| `kR` | `Double3.ZERO` | Reflection coefficient |
| `kT` | `Double3.ZERO` | Transparency coefficient |
| `nShininess` | `0` | Specular exponent |
| `reflectionBlur` | `0` | `0` keeps perfect mirror reflection |
| `transparencyBlur` | `0` | `0` keeps straight transparency rays |
| `globalSamples` | `1` | Single global ray by default |

### Material Setters

| Method | Meaning |
|---|---|
| `setKd(double)` / `setKd(Double3)` | Diffuse coefficient |
| `setKs(double)` / `setKs(Double3)` | Specular coefficient |
| `setShininess(int)` | Specular exponent |
| `setKr(double)` / `setKr(Double3)` | Reflection attenuation |
| `setKt(double)` / `setKt(Double3)` | Transparency attenuation |
| `setReflectionBlur(double)` | Glossy reflection blur radius |
| `setTransparencyBlur(double)` | Diffused glass blur radius |
| `setGlobalSamples(int)` | Number of global-effect samples |

Validation:

- Material coefficients cannot be negative.
- `Double3` coefficients cannot be null and cannot contain negative components.
- `reflectionBlur` and `transparencyBlur` cannot be negative.
- `globalSamples` must be positive and cannot exceed `Material.MAX_GLOBAL_SAMPLES`.
- `Material.MAX_GLOBAL_SAMPLES` exposes the current implementation cap for blurred global-effect samples. Treat the exact value as an implementation limit, not a rendering-quality target.

### Reflection, Transparency, Glossy Reflection, Diffused Glass

Reflection is controlled by `kR`; transparency is controlled by `kT`.

```java
new Material()
        .setKr(0.6)
        .setKt(0.3);
```

Glossy reflection and diffused glass are enabled by combining non-zero blur with more than one global sample:

```java
new Material()
        .setKr(0.6)
        .setReflectionBlur(0.15)
        .setKt(0.3)
        .setTransparencyBlur(0.1)
        .setGlobalSamples(8);
```

Best practices:

- Keep `globalSamples` modest. It multiplies recursive ray work.
- Start with `4` or `8` samples before trying `16`.
- Leave blur at `0` when you want the classic single-ray behavior.
- Use render manifests or benchmark runners when comparing image quality and performance.

Runtime guards in `SimpleRayTracer` prevent uncontrolled recursion:

- Recursive color calculation is capped by the current implementation.
- Contributions below the internal minimum attenuation threshold are skipped.
- Cone-sampled global effects expand only for a limited recursive depth.

These guards are implementation limits rather than stable public API contracts.

---

## Geometry API

All shapes extend `Geometry`, which extends `Intersectable`.

### Common Geometry Methods

| Method | Meaning |
|---|---|
| `setEmission(Color emission)` | Sets intrinsic geometry color |
| `getEmission()` | Returns emission color |
| `setMaterial(Material material)` | Sets material |
| `getMaterial()` | Returns material |
| `getNormal(Point point)` | Surface normal at a point |
| `findIntersections(Ray ray)` | Intersection points only |
| `findGeoIntersections(Ray ray)` | Intersections with geometry references |
| `findGeoIntersections(Ray ray, double maxDistance)` | Distance-limited intersections |

`findIntersections(...)` and `findGeoIntersections(...)` return `null` when there are no intersections.

### `Intersectable.GeoPoint`

Represents an intersection with both the geometry and point:

```java
GeoPoint gp = new GeoPoint(geometry, point);
```

Fields:

| Field | Meaning |
|---|---|
| `geometry` | Geometry hit by the ray |
| `point` | Intersection point |

### Supported Shapes

#### `Sphere`

```java
new Sphere(new Point(0, 0, -100), 50);
```

Constructor:

| Constructor | Meaning |
|---|---|
| `Sphere(Point center, double radius)` | Finite sphere |

Acceleration:

- Has a finite bounding box.

#### `Plane`

```java
new Plane(new Point(0, 0, -100), new Vector(0, 0, 1));
new Plane(p1, p2, p3);
```

Constructors:

| Constructor | Meaning |
|---|---|
| `Plane(Point point, Vector normal)` | Plane from a point and normal |
| `Plane(Point p1, Point p2, Point p3)` | Plane from three points |

Acceleration:

- Unbounded. Acceleration structures keep it in a linear fallback path.

#### `Triangle`

```java
new Triangle(p1, p2, p3);
```

Constructor:

| Constructor | Meaning |
|---|---|
| `Triangle(Point p1, Point p2, Point p3)` | Triangle with three vertices |

Acceleration:

- Has a finite bounding box.

#### `Polygon`

```java
new Polygon(p1, p2, p3, p4);
```

Constructor:

| Constructor | Meaning |
|---|---|
| `Polygon(Point... vertices)` | Convex polygon with ordered vertices |

Validation:

- Requires at least three vertices.
- Vertices must be ordered around the polygon.
- Vertices must be coplanar.
- Polygon must be convex.
- Consecutive vertices cannot collapse into invalid edges.

Acceleration:

- Has a finite bounding box.

#### `Tube`

```java
new Tube(new Ray(axisPoint, axisDirection), radius);
```

Constructor:

| Constructor | Meaning |
|---|---|
| `Tube(Ray axisRay, double radius)` | Infinite tube around an axis ray |

Acceleration:

- Unbounded. Acceleration structures keep it in a linear fallback path.

#### `Cylinder`

```java
new Cylinder(new Ray(axisPoint, axisDirection), radius, height);
```

Constructor:

| Constructor | Meaning |
|---|---|
| `Cylinder(Ray axisRay, double radius, double height)` | Finite cylinder with circular caps |

Acceleration:

- Has a finite bounding box.

---

## Geometry Collections And Acceleration

`Geometries` is the standard collection used by `Scene`.

```java
Geometries geometries = new Geometries();
geometries.add(new Sphere(new Point(0, 0, -100), 50));
```

Important methods:

| Method | Meaning |
|---|---|
| `new Geometries()` | Empty collection |
| `new Geometries(Intersectable... geometries)` | Collection with initial geometries |
| `add(Intersectable... geometries)` | Adds geometries and invalidates the internal index |
| `size()` | Number of direct geometries |
| `setAcceleration(AccelerationType)` | Selects traversal strategy |
| `getAccelerationType()` | Requested acceleration mode |
| `getResolvedAccelerationType()` | Concrete mode selected by the active index |
| `findClosestGeoIntersection(Ray)` | Closest hit |
| `findClosestGeoIntersection(Ray, double maxDistance)` | Distance-limited closest hit |

### Acceleration Modes

Import:

```java
import geometries.acceleration.AccelerationType;
```

Use:

```java
scene.geometries.setAcceleration(AccelerationType.AUTO);
scene.geometries.setAcceleration(AccelerationType.LINEAR);
scene.geometries.setAcceleration(AccelerationType.BVH);
scene.geometries.setAcceleration(AccelerationType.GRID);
```

| Mode | Meaning |
|---|---|
| `AUTO` | Default. Chooses a strategy using scene-shape heuristics |
| `LINEAR` | Checks geometries directly |
| `BVH` | Uses bounding volume hierarchy for bounded geometries |
| `GRID` | Uses regular voxel grid traversal for bounded geometries |

`AUTO` is conservative:

- Very small scenes may stay linear.
- Uniform fully bounded distributions may use `GRID`.
- Fallback-heavy, receiver-heavy, dense, or clustered scenes usually use `BVH`.
- Unbounded geometries are handled through fallback traversal and do not force full linear traversal once enough bounded geometry exists.

To record what `AUTO` actually chose:

```java
AccelerationType resolved = scene.geometries.getResolvedAccelerationType();
```

If the internal index has not been built yet, this call builds it lazily and returns a defined concrete value.

Best practices:

- Use `AUTO` for normal rendering.
- Use explicit `LINEAR`, `BVH`, and `GRID` for benchmarking and debugging.
- Store `getResolvedAccelerationType()` in manifests or benchmark results when comparing performance.

---

## Scene API

`Scene` is a container for renderable content.

```java
Scene scene = new Scene("Scene name")
        .setBackground(new Color(0, 0, 0))
        .setAmbientLight(new AmbientLight(new Color(20, 20, 20), 0.2));
```

Public fields:

| Field | Default | Meaning |
|---|---:|---|
| `name` | constructor value | Scene name |
| `background` | `Color.BLACK` | Color returned when rays miss |
| `ambientLight` | `AmbientLight.NONE` | Scene ambient light |
| `geometries` | empty `Geometries` | Geometry collection |
| `lights` | empty list | Light sources |

Fluent setters:

| Method | Meaning |
|---|---|
| `setBackground(Color background)` | Scene background |
| `setAmbientLight(AmbientLight ambientLight)` | Ambient light |
| `setGeometries(Geometries geometries)` | Replace geometry collection |
| `setLights(List<LightSource> lights)` | Replace light list |
| `addGeometries(Geometries... geometries)` | Add geometry collections as composites |

Most code adds shapes directly through:

```java
scene.geometries.add(shape1, shape2, shape3);
scene.lights.add(light);
```

---

## Lighting API

Lighting uses the `LightSource` interface. Existing light classes return one light sample by default, preserving classic hard-shadow behavior.

### `AmbientLight`

```java
scene.setAmbientLight(new AmbientLight(new Color(50, 50, 50), 0.2));
scene.setAmbientLight(AmbientLight.NONE);
```

Constructors:

| Constructor | Meaning |
|---|---|
| `AmbientLight(Color iA, double kA)` | Uniform attenuation |
| `AmbientLight(Color iA, Double3 kA)` | Per-channel attenuation |

Constant:

| Constant | Meaning |
|---|---|
| `AmbientLight.NONE` | No ambient light |

### `DirectionalLight`

```java
new DirectionalLight(new Color(300, 300, 300), new Vector(1, -1, -1));
```

Constructor:

| Constructor | Meaning |
|---|---|
| `DirectionalLight(Color intensity, Vector direction)` | Infinite-distance light |

Distance is `Double.POSITIVE_INFINITY`.

### `PointLight`

```java
new PointLight(new Color(400, 400, 400), new Point(50, 50, 0))
        .setKl(0.0005)
        .setKq(0.0005);
```

Constructor and setters:

| API | Meaning |
|---|---|
| `PointLight(Color intensity, Point position)` | Point light |
| `setKc(double)` | Constant attenuation, default `1` |
| `setKl(double)` | Linear attenuation, default `0` |
| `setKq(double)` | Quadratic attenuation, default `0` |

Intensity attenuation formula:

```text
intensity / (kC + kL * distance + kQ * distance^2)
```

### `SpotLight`

```java
new SpotLight(new Color(400, 250, 120), new Point(50, 50, 0), new Vector(-1, -1, -2))
        .setKl(0.0005)
        .setKq(0.0005)
        .setNarrowBeam(10);
```

Constructor and setters:

| API | Meaning |
|---|---|
| `SpotLight(Color intensity, Point position, Vector direction)` | Focused point light |
| `setKc(double)` | Constant attenuation |
| `setKl(double)` | Linear attenuation |
| `setKq(double)` | Quadratic attenuation |
| `setNarrowBeam(double)` | Higher values make the beam narrower |

### `LightSource`

```java
public interface LightSource {
    Color getIntensity(Point p);
    Vector getL(Point p);
    double getDistance(Point point);
    default List<LightSample> getSamples(Point point);
}
```

Direction convention:

- `getL(Point)` returns the direction from the light toward the shaded point.
- Shadow rays are traced back toward the light internally.

### Soft Shadows And `LightSample`

`LightSample` represents one sampled light contribution:

```java
public record LightSample(Color intensity, Vector direction, double distance)
```

Default lights return one sample:

```java
List.of(new LightSample(getIntensity(point), getL(point), getDistance(point)))
```

To enable soft shadows, implement a custom `LightSource` whose `getSamples(Point)` returns multiple samples over an area light.

Validation:

- `LightSample` intensity cannot be null.
- Direction cannot be null.
- Distance cannot be negative.

Best practices:

- Keep light sample counts reasonable. The current ray tracer has an internal per-light sample guard to prevent accidental render explosions.
- Use multiple samples only for lights that need soft shadows.
- Use acceleration when many shadow rays are expected.

---

## Camera And Rendering API

`Camera` is configured through `Camera.Builder`.

```java
Camera camera = Camera.getBuilder()
        .setLocation(new Point(0, 0, 0))
        .setDirection(new Vector(0, 0, -1), new Vector(0, 1, 0))
        .setVpDistance(100)
        .setVpSize(200, 200)
        .setImageWriter(new ImageWriter("image-name", 800, 800))
        .setRayTracer(new SimpleRayTracer(scene))
        .build();
```

### Required Builder Methods

| Method | Meaning |
|---|---|
| `setLocation(Point position)` | Camera position |
| `setDirection(Vector toward, Vector up)` | Orthogonal direction vectors |
| `setVpDistance(double distance)` | Distance from camera to view plane |
| `setVpSize(double width, double height)` | View-plane size in world units |
| `setImageWriter(ImageWriter imageWriter)` | Output buffer and image resolution |
| `setRayTracer(RayTracerBase rayTracer)` | Ray tracing implementation |

`build()` throws `MissingResourceException` if required render data is missing.

### Optional Builder Methods

| Method | Default | Meaning |
|---|---:|---|
| `setThreadsCount(int)` | `0` | `0` means single-threaded; positive value starts that many worker threads |
| `setSampleSize(int)` | `1` | Uniform jittered grid side length, `N * N` rays |
| `setSampleNum(int)` | unset | Approximate sample count, rounded up to a square grid |
| `setAdaptiveSampling(boolean)` | `false` | Enables adaptive pixel sampling |
| `setMaxDepth(int)` | `0` | Adaptive recursion depth. Valid only after enabling adaptive sampling |
| `setApertureRadius(double)` | `0` | Depth-of-field aperture radius |
| `setFocalDistance(double)` | `0` | Required when aperture radius is positive |
| `setApertureSampleSize(int)` | `0` | Lens sample grid side length. `0` inherits pixel sample size |
| `setProgressListener(RenderProgressListener)` | `RenderProgressListener.CONSOLE` | Progress callback |
| `setProgressIntervalPercent(double)` | `0.1` | Progress event interval |
| `setRenderIdSupplier(Supplier<String>)` | UUID supplier | Custom render id |
| `setRenderManifestForImage()` | disabled | Writes `images/<image-name>-manifest.json` |
| `setRenderManifestPath(Path)` | disabled | Writes manifest to an explicit path |

Validation:

- Position cannot be null.
- Direction vectors cannot be null and must be orthogonal.
- View-plane size and distance must be positive.
- Image writer and ray tracer cannot be null.
- Thread count cannot be negative.
- Progress interval cannot be negative.
- Render id supplier cannot be null and must return a non-blank id.
- Manifest path cannot be null.
- Aperture radius cannot be negative.
- Positive aperture radius requires positive focal distance.
- Aperture sample size must be positive if configured.

### Rendering Methods

| Method | Meaning |
|---|---|
| `constructRay(int nX, int nY, int j, int i)` | Constructs the pinhole ray through one pixel |
| `renderImage()` | Traces all pixels into the `ImageWriter` buffer |
| `writeToImage()` | Writes the buffered image to disk |
| `printGrid(int interval, Color color)` | Draws a grid over the current image buffer |

Typical lifecycle:

```java
camera.renderImage()
        .writeToImage();
```

`renderImage()` computes the pixels. `writeToImage()` writes the PNG and also writes a successful render manifest if manifest output was enabled. If a render fails, the camera attempts to write a failed manifest without hiding the original failure.

### Anti-Aliasing

Uniform anti-aliasing:

```java
.setSampleSize(3)  // 3x3 jittered grid
```

or:

```java
.setSampleNum(10)  // rounded up to a valid square grid
```

Adaptive anti-aliasing:

```java
.setAdaptiveSampling(true)
.setMaxDepth(3)
```

or:

```java
.setAdaptiveSampling(true)
.setSampleSize(8)
```

Rules:

- If no sampling option is configured, the renderer traces one ray per pixel.
- `setSampleSize`, `setSampleNum`, and `setMaxDepth` are mutually exclusive boundary controls within one builder.
- `setMaxDepth` is valid only when adaptive sampling is enabled.
- Adaptive sampling requires exactly one boundary control: max depth, sample size, or sample count.

### Depth Of Field

Depth of field is disabled by default:

```java
.setApertureRadius(0)
```

Enable it with:

```java
.setApertureRadius(2.0)
.setFocalDistance(120)
.setApertureSampleSize(3)
```

Behavior:

- `apertureRadius == 0` preserves pinhole-camera behavior.
- Positive aperture radius requires a focal distance.
- If `setApertureSampleSize(...)` is not set, lens sampling inherits the pixel sample size for backward compatibility.
- Uniform AA and DOF pair pixel and lens samples instead of using a full Cartesian product, keeping ray growth controlled.

Best practices:

- Set aperture sample size explicitly when comparing DOF quality.
- Use low values first, such as `2` or `3`.
- Keep thread count positive for expensive DOF renders.

---

## Ray Tracing API

### `RayTracerBase`

Base class for custom ray tracers:

```java
public abstract class RayTracerBase {
    public RayTracerBase(Scene scene);
    public abstract Color traceRay(Ray ray);
}
```

Use this when you want to plug a different tracing strategy into `Camera`.

### `SimpleRayTracer`

Default ray tracer:

```java
new SimpleRayTracer(scene)
```

Features:

- Background color on ray miss.
- Geometry emission.
- Ambient light.
- Phong-style diffuse and specular shading.
- Transparency-aware shadow rays.
- Soft shadow support through multiple `LightSample` values.
- Recursive reflection through `kR`.
- Recursive transparency through `kT`.
- Glossy reflection through `reflectionBlur` and `globalSamples`.
- Diffused glass through `transparencyBlur` and `globalSamples`.
- Closest-hit lookup through `Geometries#findClosestGeoIntersection(...)`.

Best practices:

- Use `SimpleRayTracer` unless you are experimenting with a different renderer.
- Tune materials and sampling before increasing image resolution.
- Use acceleration for scenes with many objects, many shadow rays, or recursive global effects.

---

## Image Output API

### `ImageWriter`

```java
ImageWriter writer = new ImageWriter("image-name", 800, 800);
```

Constructor:

| Constructor | Meaning |
|---|---|
| `ImageWriter(String imageName, int nX, int nY)` | Creates an RGB image buffer |

Methods:

| Method | Meaning |
|---|---|
| `getNx()` | Image width in pixels |
| `getNy()` | Image height in pixels |
| `getImageName()` | Name without `.png` |
| `getImagePath()` | Full PNG path under `images/` |
| `getOutputDirectory()` | Static output directory path |
| `writePixel(int xIndex, int yIndex, Color color)` | Writes one pixel to the buffer |
| `writeToImage()` | Writes the PNG to disk |

Output directory:

```text
images/
```

The directory is created automatically when needed.

---

## Progress, Metrics, And Manifests

The renderer separates progress events, metrics persistence, and final run manifests:

- `RenderProgressListener` receives lifecycle events.
- `RenderProgressWriters` persists progress events to CSV or SQLite.
- Camera render manifests summarize the final render configuration and result.

### Render Progress Events

`RenderProgress` is an immutable event:

```java
public record RenderProgress(
        String renderId,
        RenderStage stage,
        long completedWork,
        long totalWork,
        double percent,
        long elapsedMillis,
        long stageElapsedMillis,
        long timestampMillis)
```

Stages:

| Stage | Meaning |
|---|---|
| `RENDER_PIXELS` | Pixels are being traced |
| `WRITE_IMAGE` | Image buffer is being written |
| `DONE` | Render lifecycle completed |
| `FAILED` | Render lifecycle failed |

Progress events are emitted by stage and configured percentage interval, not once per pixel.

### Progress Listeners

Built-in listener constants:

| API | Meaning |
|---|---|
| `RenderProgressListener.NONE` | Ignore progress |
| `RenderProgressListener.CONSOLE` | Print pixel-rendering progress |

Helpers:

| Method | Meaning |
|---|---|
| `RenderProgressListener.combine(...)` | Fan out progress to multiple listeners |
| `RenderProgressListener.resilient(listener)` | Suppress listener failures |
| `RenderProgressListener.resilient(listener, failureHandler)` | Suppress and report listener failures |

Example:

```java
Camera camera = Camera.getBuilder()
        // regular camera configuration...
        .setProgressListener(RenderProgressListener.combine(
                RenderProgressListener.CONSOLE,
                progress -> {
                    if (progress.stage() == RenderStage.RENDER_PIXELS) {
                        double percent = progress.percent();
                    }
                }))
        .build();
```

Best practice for long renders:

```java
.setProgressListener(RenderProgressListener.resilient(
        RenderProgressWriters.sqliteForImage("long-render"),
        failure -> System.err.println(failure.getMessage())))
```

Use fail-fast listeners while debugging metrics. Use resilient listeners when metrics should not fail a good render.

### CSV And SQLite Metrics

Import:

```java
import metrics.RenderProgressWriters;
```

CSV:

```java
.setProgressListener(RenderProgressWriters.csvForImage("demo"))
```

writes:

```text
images/demo-progress.csv
```

Explicit path:

```java
.setProgressListener(RenderProgressWriters.csv(Path.of("render-history", "demo.csv")))
```

SQLite:

```java
.setProgressListener(RenderProgressWriters.sqliteForImage("demo"))
```

writes:

```text
images/demo-progress.sqlite
```

SQLite tables:

| Table | Meaning |
|---|---|
| `render_runs` | One summary row per render id |
| `render_progress_events` | Event stream |

CSV columns:

```text
render_id,stage,completed_work,total_work,percent,elapsed_millis,stage_elapsed_millis,timestamp_millis
```

Writers:

- Are opt-in.
- Synchronize writes.
- Buffer output.
- Close automatically on `DONE` or `FAILED`.
- Have idempotent `close()`.

### Render Manifest

Render manifests are opt-in JSON summaries for a single image render.

Beside the image:

```java
.setRenderManifestForImage()
```

writes:

```text
images/<image-name>-manifest.json
```

Explicit path:

```java
.setRenderManifestPath(Path.of("render-history", "demo-manifest.json"))
```

Manifest fields:

| Section | Contents |
|---|---|
| `renderId` | Render identifier |
| `status` | `DONE` or `FAILED` |
| `error` | Failure message when available |
| `image` | Path, width, height, format |
| `timing` | Start, finish, elapsed, pixel render time, image write time |
| `camera` | Location, direction, up vector, view-plane settings |
| `renderSettings` | Threads, AA, adaptive sampling, DOF, progress interval |
| `acceleration` | Requested and resolved acceleration |
| `sceneSummary` | Scene name, geometry count, light count |

For `AUTO` acceleration, the manifest includes both:

```json
"acceleration": {
  "requested": "AUTO",
  "resolved": "BVH"
}
```

This makes render history and benchmark comparisons much more useful.

---

## XML Scene Loading

`SceneBuilder` can load basic scene geometry from XML:

```java
Scene scene = SceneBuilder.buildSceneFromXml("path/to/scene.xml");
```

Supported XML:

```xml
<scene background-color="R G B">
    <ambient-light color="R G B" />
    <geometries>
        <sphere center="x y z" radius="r" />
        <triangle p0="x y z" p1="x y z" p2="x y z" />
        <plane point="x y z" normal="x y z" />
        <tube axisRay="px py pz dx dy dz" radius="r" />
        <cylinder axisRay="px py pz dx dy dz" radius="r" height="h" />
        <polygon>
            <point>x y z</point>
            <point>x y z</point>
            <point>x y z</point>
        </polygon>
    </geometries>
</scene>
```

Limitations:

- Camera setup is not loaded from XML.
- Non-ambient lights are not loaded from XML.
- Materials, emission, acceleration settings, sampling, and render settings are not loaded from XML.
- Advanced scene setup is intended to be defined in Java code.

---

## Sampling Utilities

Most users configure sampling through `Camera`, `Material`, and custom `LightSource` implementations. The `sampling` package is useful when extending the engine.

### `Sample2D`

```java
public record Sample2D(double x, double y)
```

Represents a 2D offset.

### `JitteredSampler`

```java
new JitteredSampler(sampleSize, pixelWidth, pixelHeight)
```

Generates deterministic jittered offsets inside a pixel.

Methods:

| Method | Meaning |
|---|---|
| `getSamples()` | List of `Sample2D` offsets |
| `getSampleSize()` | Grid side length |

Validation:

- `sampleSize` must be positive.

### `DiskSampler`

```java
new DiskSampler(sampleSize, radius)
```

Generates deterministic random offsets inside a disk. Used by depth of field.

Methods:

| Method | Meaning |
|---|---|
| `getSamples()` | Disk offsets |
| `getSampleSize()` | Logical grid side length |
| `getRadius()` | Disk radius |

Validation:

- `sampleSize` must be positive.
- `radius` must be positive.

### `ConeSampler`

```java
new ConeSampler(direction, radius, sampleCount)
```

Generates deterministic direction samples inside a cone. Used by glossy reflection and diffused glass.

Methods:

| Method | Meaning |
|---|---|
| `getSamples()` | Direction samples |
| `getDirection()` | Center direction |
| `getRadius()` | Cone radius |
| `getSampleCount()` | Number of samples |

Validation:

- Direction cannot be null.
- Radius must be positive.
- Sample count must be positive.

---

## Error Handling

Most invalid configuration is rejected early by constructors, setters, or `Camera.Builder#build()`.

Common failure behavior:

| Area | Behavior |
|---|---|
| Primitive construction | Invalid values such as zero vectors or negative color components throw `IllegalArgumentException` |
| Material configuration | Negative coefficients, null `Double3` coefficients, and excessive `globalSamples` throw `IllegalArgumentException` |
| Camera builder | Missing required render data throws `MissingResourceException`; invalid settings throw `IllegalArgumentException` or `IllegalStateException` |
| Rendering | Render failures are propagated to the caller |
| Render manifests | If enabled, a failed render attempts to write a `FAILED` manifest before rethrowing the original exception |
| Progress listeners | Listener failures are fail-fast by default |
| Resilient progress listeners | `RenderProgressListener.resilient(...)` suppresses listener failures and optionally reports them to a handler |
| Metrics writers | CSV and SQLite failures are thrown unless wrapped with a resilient listener |

Best practice:

```java
.setProgressListener(RenderProgressListener.resilient(
        RenderProgressWriters.sqliteForImage("production-render"),
        failure -> System.err.println(failure.getMessage())))
```

Use fail-fast progress listeners while developing metrics code. Use resilient listeners when metrics should not fail a successful image render.

---

## Common Recipes

These snippets assume you already have a `Scene scene` and a standard `Camera.Builder` setup with location, direction, view-plane size, `ImageWriter`, and `SimpleRayTracer`.

### Render From A `main` Method

Use a normal Java entry point when using the engine as a library:

```java
package app;

public class MyRender {
    public static void main(String[] args) {
        Scene scene = new Scene("My Scene");
        // add geometries, materials, and lights...

        Camera.getBuilder()
                // regular camera configuration...
                .setImageWriter(new ImageWriter("my-scene", 800, 800))
                .setRayTracer(new SimpleRayTracer(scene))
                .build()
                .renderImage()
                .writeToImage();
    }
}
```

Compile and run:

```powershell
mvn compile
java -cp target/classes app.MyRender
```

### Render With Anti-Aliasing

```java
.setSampleSize(3)
```

This traces a `3 x 3` jittered sample grid per pixel.

### Render With Adaptive Sampling

```java
.setAdaptiveSampling(true)
.setMaxDepth(3)
```

Use adaptive sampling when large areas of the image are smooth and only edges/details need extra rays.

### Render With Depth Of Field

```java
.setApertureRadius(2.0)
.setFocalDistance(120)
.setApertureSampleSize(3)
```

Use a positive aperture radius to blur objects away from the focal plane.

### Render With Automatic Acceleration

```java
scene.geometries.setAcceleration(AccelerationType.AUTO);
```

Record the resolved mode when comparing performance:

```java
AccelerationType resolved = scene.geometries.getResolvedAccelerationType();
```

### Force BVH Or Grid For Benchmarks

```java
scene.geometries.setAcceleration(AccelerationType.BVH);
scene.geometries.setAcceleration(AccelerationType.GRID);
scene.geometries.setAcceleration(AccelerationType.LINEAR);
```

Use explicit modes to compare traversal strategies without changing scene content.

### Enable Glossy Reflection

```java
new Material()
        .setKr(0.6)
        .setReflectionBlur(0.15)
        .setGlobalSamples(8);
```

### Enable Diffused Glass

```java
new Material()
        .setKt(0.5)
        .setTransparencyBlur(0.1)
        .setGlobalSamples(8);
```

### Persist Progress To SQLite

```java
import metrics.RenderProgressWriters;

.setProgressListener(RenderProgressWriters.sqliteForImage("my-scene"))
```

For long or important renders, wrap metrics as best-effort:

```java
.setProgressListener(RenderProgressListener.resilient(
        RenderProgressWriters.sqliteForImage("my-scene"),
        failure -> System.err.println(failure.getMessage())))
```

### Write A Render Manifest

```java
.setRenderManifestForImage()
```

This writes:

```text
images/<image-name>-manifest.json
```

Use an explicit path when an external runner owns output layout:

```java
.setRenderManifestPath(Path.of("render-history", "my-scene.json"))
```

### Silence Console Progress

```java
.setProgressListener(RenderProgressListener.NONE)
```

This is useful for tests, benchmarks, and external progress handling.

---

## Best Practices

### Build Scenes Programmatically For Advanced Renders

XML loading is useful for simple geometry, but Java scene construction is the main path for serious renders because it supports:

- Materials.
- Emission.
- Lights.
- Camera configuration.
- Acceleration.
- Sampling.
- Metrics.
- Manifests.

### Keep Sampling Under Control

Expensive features can multiply ray counts:

- Anti-aliasing.
- Adaptive sampling.
- Depth of field.
- Soft shadows.
- Glossy reflection.
- Diffused glass.
- Reflection/transparency recursion.

Recommended approach:

1. Start with one feature at a time.
2. Use small sample counts.
3. Enable acceleration.
4. Use progress metrics and manifests.
5. Increase image resolution last.

### Use Explicit Render IDs For Comparisons

```java
.setRenderIdSupplier(() -> "scene-a__aa-3__bvh")
```

This makes CSV, SQLite, and manifest outputs easier to compare.

### Use `AUTO` Normally, Explicit Acceleration For Benchmarks

```java
scene.geometries.setAcceleration(AccelerationType.AUTO);
```

For benchmark comparisons:

```java
scene.geometries.setAcceleration(AccelerationType.LINEAR);
scene.geometries.setAcceleration(AccelerationType.BVH);
scene.geometries.setAcceleration(AccelerationType.GRID);
```

Always record `getResolvedAccelerationType()` when using `AUTO`.

### Silence Console Progress In Tests

```java
.setProgressListener(RenderProgressListener.NONE)
```

### Prefer SQLite For Render History

CSV is simple and human-readable. SQLite is better for many render runs because it keeps run summaries and event streams in one queryable file.

---

## Advanced And Internal APIs

These APIs are public in code but are not the primary user-facing workflow.

### `geometries.acceleration`

Primary user-facing API:

- `AccelerationType`
- `Geometries#setAcceleration(...)`
- `Geometries#getResolvedAccelerationType()`

Advanced/internal APIs:

| API | Meaning |
|---|---|
| `BoundingBox` | Axis-aligned finite box |
| `GeometryIndex` | Internal intersection index abstraction |
| `GeometryIndexes` | Index factory and AUTO selection implementation |
| `GeometryIndexes.Selection` | Built index plus resolved acceleration type |

Package-private classes such as `BvhGeometryIndex`, `RegularGrid`, `LinearGeometryIndex`, `BvhNode`, and `BvhBuilder` are implementation details.

### `renderer` Internals

Package-private implementation details:

| API | Meaning |
|---|---|
| `AdaptivePixelSampler` | Recursive adaptive pixel sampling |
| `PixelManager` | Pixel allocation and progress emission |
| `RenderManifestWriter` | JSON manifest serialization |

Use them only when extending the engine itself.

### Custom Ray Tracers

To replace `SimpleRayTracer`, extend:

```java
public abstract class RayTracerBase {
    public abstract Color traceRay(Ray ray);
}
```

Then pass it to:

```java
.setRayTracer(new MyRayTracer(scene))
```

---

## Build And Test Commands

Compile:

```powershell
mvn compile
```

Default tests:

```powershell
mvn test
```

Visual rendering tests:

```powershell
mvn -Pvisual-tests test
```

Benchmark suites:

```powershell
mvn -Pbenchmarks test
```

---

## Output Summary

| Output | How to enable | Path |
|---|---|---|
| PNG image | `writeToImage()` | `images/<image>.png` |
| CSV progress | `RenderProgressWriters.csvForImage(image)` | `images/<image>-progress.csv` |
| SQLite progress | `RenderProgressWriters.sqliteForImage(image)` | `images/<image>-progress.sqlite` |
| JSON manifest | `setRenderManifestForImage()` | `images/<image>-manifest.json` |
| Explicit manifest | `setRenderManifestPath(Path)` | User-provided path |
