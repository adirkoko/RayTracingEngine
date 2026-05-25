# Ray Tracing Engine

A Java-based ray tracing renderer for building and rendering simple 3D scenes. The project is structured as a layered educational engine, moving from mathematical primitives and geometry intersections to lighting, scene composition, and final image generation.

Given a scene composed of geometries, materials, light sources, and a camera, the engine traces rays through the view plane and produces PNG output through the built-in `ImageWriter`.

---

## Overview

This project can:

- Trace rays from a camera into a 3D scene.
- Compute intersections against supported geometries.
- Shade surfaces using emission, ambient light, diffuse and specular lighting, shadows, reflection, and transparency.
- Apply supersampling and adaptive anti-aliasing.
- Write rendered images to the local `images/` directory.

For a detailed user-facing API guide, including rendering workflow, defaults, advanced quality settings, progress metrics, manifests, and best practices, see [API_REFERENCE.md](docs/API_REFERENCE.md).

---

## Build And Tooling

The project uses Maven for build automation and dependency management.

To preserve the original educational layout, the Maven configuration keeps the existing source structure:

```text
src/        # Main Java sources
unittests/  # JUnit 5 tests and visual rendering tests
```

JUnit 5 is managed directly through [pom.xml](pom.xml), so no IDE-specific library setup is required.

---

## Requirements

- JDK 21 or newer
- Maven 3.9 or newer

The codebase uses Java `record` syntax as well as Java 21 collection APIs such as `List#getFirst()`, so JDK 21+ is required.

---

## Command-Line Usage

### Compile

```powershell
mvn compile
```

### Run The Default Test Suite

```powershell
mvn test
```

By default, Maven runs the lighter unit and integration tests. The more CPU-intensive visual rendering tests are excluded from the standard test run.

### Build The Project

```powershell
mvn package
```

Build artifacts are generated under `target/`.

### Build Without Tests

```powershell
mvn -DskipTests package
```

### Run Visual Rendering Tests

Visual rendering tests produce PNG files under `images/`.

```powershell
mvn -Pvisual-tests test
```

Run a single visual test class:

```powershell
mvn -Pvisual-tests -Dtest=RenderTests test
```

Run a single visual test method:

```powershell
mvn -Pvisual-tests -Dtest=RenderTests#renderTwoColorTest test
```

### Run Benchmark Suites

Benchmark suites are kept under the `benchmarks` profile and are excluded from the default test suite because timing results depend on the machine and JVM state.

Run all benchmark suites:

```powershell
mvn -Pbenchmarks test
```

Run only the acceleration suite:

```powershell
mvn -Pbenchmarks -Dtest=AccelerationBenchmark test
```

Acceleration benchmarks compare `AUTO`, `LINEAR`, `BVH`, and `GRID` as render profiles over benchmark scenes such as `small-overhead`, `uniform-bounded`, `clustered-bounded`, `mixed-scale-bounded`, `unbounded-fallback`, `reflection-transparency`, `shadow-heavy`, `teapot-mesh`, `teapot-complex`, and `profile-comparison`.

Run only the image-quality render batch suite:

```powershell
mvn -Pbenchmarks -Dtest=RenderBatchBenchmark test
```

Render profile batches render representative image-quality scenes with one named quality-profile set, write one PNG per profile, and persist progress metrics plus a JSON manifest for later comparison.

Batch images are written under:

```text
images/benchmark/<suite>/<scene>/<batch-id>/<profile>.png
```

Batch history is written under:

```text
render-history/benchmark/<suite>/<scene>/<batch-id>/
|-- manifest.json
`-- progress.sqlite
```

Each manifest records the requested profile configuration and the completed run results. For `AUTO` acceleration runs, each run entry also includes `resolvedAccelerationType`, showing whether the geometry index actually used `LINEAR`, `BVH`, or `GRID`.

The image-quality suite keeps thread count fixed at `4` for all quality profiles. It compares visual settings such as baseline rendering, uniform supersampling, adaptive sampling, and depth of field over `grounded-soft-shadow`, `sampling-focus`, and `global-materials`; thread scaling belongs in a separate performance suite.

> The first Maven run may download dependencies and plugins into your local Maven cache.

---

## Key Features

### Geometry And Intersections

- Supports `Sphere`, `Plane`, `Triangle`, `Polygon`, `Tube`, and `Cylinder`
- Groups geometries through `Geometries`
- Computes normals and resolves the nearest intersection point
- Uses bounding boxes and a lazy internal geometry index, with BVH or Regular Grid traversal for bounded geometry collections
- Provides `setAcceleration(AUTO | BVH | GRID | LINEAR)` for benchmark and debugging comparisons

### Materials And Shading

- Phong-style diffuse and specular shading
- Per-geometry emission color
- Scene-level ambient lighting
- Recursive reflection via `kR` and transparency/refraction via `kT`
- Glossy reflection sampling through `reflectionBlur` and `globalSamples`
- Diffused glass sampling through `transparencyBlur` and `globalSamples`
- Transparency-aware soft shadow handling across light samples

### Lighting

- `AmbientLight`, `DirectionalLight`, `PointLight`, and `SpotLight`
- Distance attenuation through `setKl`, `setKq`, and `setKc`
- Narrow beam control through `SpotLight#setNarrowBeam`
- Light sampling infrastructure through `LightSample`, with one default sample per existing light source

### Rendering

- Builder-based camera configuration
- Single-threaded rendering by default
- Optional multi-threaded rendering with `setThreadsCount`
- Uniform jittered-grid supersampling through `setSampleSize` or `setSampleNum`
- Recursive adaptive sampling via `setAdaptiveSampling(true)`
- Depth of field through `setApertureRadius` and `setFocalDistance`
- Recursion and sample-count guards for global effects and light sampling
- Fail-fast validation for conflicting anti-aliasing configuration
- Structured render progress events through `RenderProgressListener`

### Scene Setup

- Fluent programmatic scene construction through `Scene`, `Camera.Builder`, materials, and lights
- Basic XML scene loading with `SceneBuilder`
- Programmatic setup for lights, camera configuration, and advanced material behavior

### Test Coverage

- Default JUnit 5 tests cover primitives, sampling, geometries, lighting infrastructure, camera behavior, image writing, and lightweight renderer behavior
- Visual rendering classes include `RenderTests`, `ShadowTests`, `ReflectionRefractionTests`, `LightsTests`, `CustomImageTest`, `BlackBall`, and `TeapotTest`
- Visual rendering tests run only through `-Pvisual-tests`; benchmark suites run only through `-Pbenchmarks`

---

## Project Structure

```text
pom.xml              # Maven build configuration and dependency management
src/
|-- primitives/      # Point, Vector, Ray, Color, Double3, Material, utilities
|-- sampling/        # Reusable 2D sample generation for rendering effects
|-- geometries/      # Shapes and ray-intersection logic
|   `-- acceleration/ # Internal bounding boxes, geometry indexes, BVH, and Regular Grid traversal
|-- lighting/        # Ambient, directional, point, and spot lights
|-- metrics/         # Optional CSV and SQLite render progress persistence
|-- scene/           # Scene container and XML SceneBuilder
`-- renderer/        # Camera, ray tracers, ImageWriter, progress events, adaptive pixel sampling

unittests/
|-- primitives/      # Unit tests for math primitives
|-- sampling/        # Unit tests for sample generation
|-- geometries/      # Geometry and intersection tests
|-- lighting/        # Lighting render tests
|-- renderer/        # Camera tests and visual rendering tests
`-- benchmark/       # Opt-in benchmark runners, scenes, profiles, and suites

images/              # Generated PNG output folder, ignored by Git
render-history/      # Generated benchmark metrics and manifests, ignored by Git
target/              # Maven build output, ignored by Git
```

Benchmark code is intentionally kept outside the engine core:

```text
unittests/benchmark/
|-- core/     # Generic batch runner, output layout, render profiles, and result records
|-- scenes/   # Benchmark scene catalog and scene factories
|-- profiles/ # Named profile sets for acceleration, quality, threads, and sampling comparisons
`-- suites/   # Thin JUnit entry points that compose SceneCatalog + ProfileCatalog + RenderBatchRunner
```

---

## Quick Start Example

```java
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
        .setImageWriter(new ImageWriter("quick-start", 800, 800))
        .setRayTracer(new SimpleRayTracer(scene))
        .build();

camera.renderImage()
        .writeToImage();
```

This produces `images/quick-start.png`. The output directory is created automatically when needed.

---

## Common Configuration

This section covers the most common runtime knobs. For full defaults, validation rules, and extension APIs, see [API_REFERENCE.md](docs/API_REFERENCE.md).

### Camera

```java
Camera camera = Camera.getBuilder()
        .setLocation(Point)                 // Camera position
        .setDirection(forward, up)          // Orthogonal direction vectors
        .setVpDistance(double)              // Distance to the view plane
        .setVpSize(width, height)           // View-plane size in world units
        .setImageWriter(ImageWriter)        // Output image name and resolution
        .setRayTracer(RayTracerBase)        // Usually SimpleRayTracer
        .setThreadsCount(int)               // Optional; 0 means single-threaded
        .setSampleSize(int)                 // Optional N x N jittered grid
        .setSampleNum(int)                  // Optional approximate sample count
        .setAdaptiveSampling(boolean)       // Optional adaptive sampling
        .setMaxDepth(int)                   // Adaptive only; call after enabling adaptive sampling
        .setApertureRadius(double)          // Optional; 0 keeps pinhole-camera behavior
        .setFocalDistance(double)           // Required when aperture radius is positive
        .setApertureSampleSize(int)         // Optional lens sample grid side length
        .setProgressListener(listener)      // Optional structured render progress callback
        .setProgressIntervalPercent(double) // Optional progress callback interval
        .setRenderIdSupplier(supplier)      // Optional custom render id generation
        .setRenderManifestForImage()        // Optional JSON manifest beside image output
        .setRenderManifestPath(Path)        // Optional explicit JSON manifest path
        .build();
```

If no anti-aliasing boundary is configured, the renderer uses one ray per pixel. Once anti-aliasing is configured, boundary setters are mutually exclusive within one builder. In uniform mode, use either `setSampleSize` or `setSampleNum`. In adaptive mode, use exactly one of `setMaxDepth`, `setSampleSize`, or `setSampleNum`; sample-based limits are rounded up to the nearest power-of-two grid and converted into a recursion depth.

Depth of field is disabled by default. Set a positive aperture radius and focal distance to sample rays across the lens aperture; an aperture radius of `0` preserves the existing pinhole-camera behavior.

Depth-of-field sampling can be controlled independently with `setApertureSampleSize(...)`. If no aperture sample size is configured, the lens sampler inherits the pixel sample size for backward compatibility. When depth of field and anti-aliasing are both enabled, camera sampling pairs pixel samples with lens samples rather than taking their full Cartesian product.

Enable depth of field:

```java
Camera camera = Camera.getBuilder()
        // regular camera configuration...
        .setApertureRadius(2.0)
        .setFocalDistance(120)
        .setApertureSampleSize(3)
        .build();
```

### Render Progress And Metrics

The renderer emits structured progress events through `RenderProgressListener` without depending on UI, database, or logging code. Use `RenderProgressListener.NONE` for silent renders, or attach custom listeners for UI, metrics, render history, and benchmark tooling.

```java
Camera camera = Camera.getBuilder()
        // regular camera configuration...
        .setProgressListener(progress -> {
            if (progress.stage() == RenderStage.RENDER_PIXELS) {
                double percent = progress.percent();
            }
        })
        .setProgressIntervalPercent(1.0)
        .build();
```

Progress events are emitted by stage and configured interval, not once per pixel. Optional persistence is provided through `RenderProgressWriters`:

```java
import metrics.RenderProgressWriters;

Camera camera = Camera.getBuilder()
        // regular camera configuration...
        .setProgressListener(RenderProgressListener.resilient(
                RenderProgressListener.combine(
                        RenderProgressListener.CONSOLE,
                        RenderProgressWriters.sqliteForImage("quick-start"))))
        .build();
```

The image-local helpers write beside generated images:

```text
images/quick-start-progress.csv
images/quick-start-progress.sqlite
```

SQLite stores `render_runs` and `render_progress_events`. For benchmark or history systems, prefer explicit paths and stable render IDs:

```java
import java.nio.file.Path;

.setRenderIdSupplier(() -> "benchmark-run-001")
.setProgressListener(RenderProgressWriters.sqlite(Path.of("render-history", "renders.sqlite")))
```

Writers buffer their output, synchronize writes, and close automatically on terminal render events.

### Render Manifest

Render manifests are opt-in JSON summaries for a single image render. They are separate from progress metrics: progress listeners record lifecycle events, while the manifest records the final run configuration and result summary.

Write a manifest beside the PNG:

```java
Camera camera = Camera.getBuilder()
        // regular camera configuration...
        .setRenderManifestForImage()
        .build();
```

For an image named `quick-start`, this writes:

```text
images/quick-start-manifest.json
```

Use an explicit path when a render-history or benchmark tool owns the output layout:

```java
import java.nio.file.Path;

.setRenderManifestPath(Path.of("render-history", "quick-start.json"))
```

The manifest includes render id, status, image path and resolution, timing, camera geometry, sampling and depth-of-field settings, requested and resolved acceleration mode, scene name, geometry count, and light count. If `AUTO` acceleration is used, the manifest records both the requested `AUTO` value and the resolved `LINEAR`, `BVH`, or `GRID` value.

### Materials

```java
Material material = new Material()
        .setKd(0.6)             // Diffuse coefficient
        .setKs(0.4)             // Specular coefficient
        .setShininess(100)      // Specular exponent
        .setKr(0.3)             // Reflection coefficient
        .setKt(0.2)             // Transparency coefficient
        .setReflectionBlur(0.0) // Glossy reflection blur; 0 keeps current behavior
        .setTransparencyBlur(0.0) // Diffused glass blur; 0 keeps current behavior
        .setGlobalSamples(1);   // Global-effect sample count; 1 keeps current behavior
```

Each coefficient also provides a `Double3` overload for per-channel control.

`globalSamples` is capped by `Material.MAX_GLOBAL_SAMPLES`. `SimpleRayTracer` also bounds light samples per light source and allows cone-sampled global effects to expand only one recursive layer; deeper reflection/transparency recursion falls back to single rays while still respecting `MAX_CALC_COLOR_LEVEL` and `MIN_CALC_COLOR_K`.

Enable glossy reflection or diffused glass by setting a positive blur radius together with `globalSamples`:

```java
new Material()
        .setKr(0.6)
        .setReflectionBlur(0.15)
        .setKt(0.3)
        .setTransparencyBlur(0.1)
        .setGlobalSamples(8);
```

### Geometry Acceleration

```java
import geometries.acceleration.AccelerationType;

scene.geometries.setAcceleration(AccelerationType.AUTO);   // Default
scene.geometries.setAcceleration(AccelerationType.BVH);    // Force BVH
scene.geometries.setAcceleration(AccelerationType.GRID);   // Force Regular Grid / 3D-DDA
scene.geometries.setAcceleration(AccelerationType.LINEAR); // Force direct traversal
```

`AUTO` is the normal rendering default and keeps acceleration transparent. It uses a conservative scene-shape heuristic: small or receiver-heavy scenes can stay linear, uniformly distributed bounded scenes can use Regular Grid, and dense or clustered bounded scenes usually use BVH. Explicit `BVH`, `GRID`, and `LINEAR` modes are mainly useful for benchmarking and debugging comparisons without changing the scene.

When `AUTO` is used, `scene.geometries.getResolvedAccelerationType()` reports the concrete strategy chosen for the current geometry collection. If the geometry index has not been built yet, the call builds it lazily and returns the resolved `LINEAR`, `BVH`, or `GRID` value.

### Lights

```java
new DirectionalLight(new Color(400, 400, 400), new Vector(1, -1, -1));

new PointLight(new Color(400, 400, 400), new Point(50, 50, 0))
        .setKl(0.0005)
        .setKq(0.0005);

new SpotLight(new Color(400, 250, 120), new Point(50, 50, 0), new Vector(-1, -1, -2))
        .setKl(0.0005)
        .setKq(0.0005)
        .setNarrowBeam(10);
```

Existing light classes return one `LightSample` by default, preserving classic hard-shadow behavior. Soft shadows are enabled by using a `LightSource` implementation that returns multiple `LightSample` values from `getSamples(Point)`.

---

## XML Scene Loading

`SceneBuilder` can load a basic scene from XML:

```java
Scene scene = SceneBuilder.buildSceneFromXml("path/to/scene.xml");
```

Supported XML content:

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

The XML loader currently covers basic scene construction only. Camera setup, non-ambient lighting, and advanced material configuration are still intended to be defined in code.

---

## Output

Rendered images are written to:

```text
<project-root>/images/*.png
```

Optional render progress files are also written under `images/` when using `RenderProgressWriters.csvForImage(...)` or `RenderProgressWriters.sqliteForImage(...)`.

```text
<project-root>/images/*-progress.csv
<project-root>/images/*-progress.sqlite
```

Optional per-image manifests are written under `images/` when using `setRenderManifestForImage()`.

```text
<project-root>/images/*-manifest.json
```

The `images/` directory is ignored by Git and created automatically when image, progress, or manifest output is written.

---

## Known Limitations

- Regular Grid uses a basic automatic voxel resolution; AUTO selects it only for scenes that look grid-friendly according to bounding-box statistics
- BVH construction currently uses a basic median split rather than a surface-area heuristic
- XML loading is intentionally limited and does not cover full scene or render configuration
- Visual rendering tests can be CPU-intensive, especially with high sample counts and large output resolutions

---

## Architecture Overview

The renderer is organized in layers of increasing abstraction:

1. `primitives` for mathematical foundations such as `Point`, `Vector`, `Color`, and `Ray`
2. `sampling` for reusable sample generation that stays independent of rendering, lighting, and scene composition
3. `geometries` for shape definitions and intersection logic
4. `lighting` for light sources and attenuation behavior
5. `scene` for scene composition and XML loading
6. `renderer` for camera logic, ray tracing, image output, adaptive pixel sampling, and progress tracking

This layered structure keeps the mathematical core, scene representation, and rendering pipeline clearly separated, making the project easier to understand, test, and extend.

---

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.
