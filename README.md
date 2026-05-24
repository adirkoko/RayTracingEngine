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

> The first Maven run may download dependencies and plugins into your local Maven cache.

---

## Key Features

### Geometry And Intersections

- Supports `Sphere`, `Plane`, `Triangle`, `Polygon`, `Tube`, and `Cylinder`
- Groups geometries through `Geometries`
- Computes normals and resolves the nearest intersection point
- Uses bounding boxes and a lazy internal geometry index, with BVH traversal for bounded geometry collections
- Provides `setAcceleration(AUTO | BVH | LINEAR)` for benchmark and debugging comparisons

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
- Progress reporting through `PixelManager`

### Scene Setup

- Fluent programmatic scene construction through `Scene`, `Camera.Builder`, materials, and lights
- Basic XML scene loading with `SceneBuilder`
- Programmatic setup for lights, camera configuration, and advanced material behavior

### Test Coverage

- JUnit 5 tests for primitives, geometries, lighting, camera behavior, image writing, and visual rendering scenarios
- Visual rendering classes include `RenderTests`, `ShadowTests`, `ReflectionRefractionTests`, `LightsTests`, `CustomImageTest`, `BlackBall`, and `TeapotTest`

---

## Project Structure

```text
pom.xml              # Maven build configuration and dependency management
src/
|-- primitives/      # Point, Vector, Ray, Color, Double3, Material, utilities
|-- sampling/        # Reusable 2D sample generation for rendering effects
|-- geometries/      # Shapes and ray-intersection logic
|   `-- acceleration/ # Internal bounding boxes, geometry indexes, and BVH traversal
|-- lighting/        # Ambient, directional, point, and spot lights
|-- scene/           # Scene container and XML SceneBuilder
`-- renderer/        # Camera, ray tracers, ImageWriter, PixelManager, adaptive pixel sampling

unittests/
|-- primitives/      # Unit tests for math primitives
|-- sampling/        # Unit tests for sample generation
|-- geometries/      # Geometry and intersection tests
|-- lighting/        # Lighting render tests
`-- renderer/        # Camera tests and visual rendering tests

images/              # Generated PNG output folder, ignored by Git
target/              # Maven build output, ignored by Git
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
        .build();
```

If no anti-aliasing boundary is configured, the renderer uses one ray per pixel. Once anti-aliasing is configured, boundary setters are mutually exclusive within one builder. In uniform mode, use either `setSampleSize` or `setSampleNum`. In adaptive mode, use exactly one of `setMaxDepth`, `setSampleSize`, or `setSampleNum`; sample-based limits are rounded up to the nearest power-of-two grid and converted into a recursion depth.

Depth of field is disabled by default. Set a positive aperture radius and focal distance to sample rays across the lens aperture; an aperture radius of `0` preserves the existing pinhole-camera behavior.

When depth of field and anti-aliasing are both enabled, camera sampling pairs pixel samples with lens samples rather than taking their full Cartesian product.

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

### Geometry Acceleration

```java
import geometries.acceleration.AccelerationType;

scene.geometries.setAcceleration(AccelerationType.AUTO);   // Default
scene.geometries.setAcceleration(AccelerationType.BVH);    // Force BVH
scene.geometries.setAcceleration(AccelerationType.LINEAR); // Force direct traversal
```

This is intended for benchmarking and debugging. `AUTO` keeps acceleration transparent for normal rendering, while `BVH` and `LINEAR` make it easy to compare traversal strategies without changing the scene.

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

The `images/` directory is ignored by Git and created automatically by `ImageWriter` when an image is written.

---

## Known Limitations

- Regular Grid / voxel traversal is not implemented yet
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
