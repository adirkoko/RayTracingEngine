# Ray Tracing Engine

A sophisticated **ray tracing renderer** written in **Java**, built as a layered set of packages progressing from mathematical primitives through geometries, lighting, and scene management to the final rendering engine.

Define a 3D scene with geometries, materials, and lights, position a camera, and the renderer shoots rays through pixels to generate a **PNG image**.

> This repository is structured as an IDE project (IntelliJ IDEA/Eclipse). It does not include Maven/Gradle build files by default.

---

## Key Features

### Geometric Intersection Engine
- Support for common shapes: **Sphere**, **Plane**, **Triangle**, **Polygon**, **Tube**, and **Cylinder**
- Robust intersection detection and normal vector computation

### Materials & Shading
- **Phong-style shading** with Diffuse and Specular components
- **Emission color** per geometry for self-luminous objects
- **Reflection** (`kR`) and **Refraction/Transparency** (`kT`) with recursive ray tracing
- Material properties: diffuse coefficient (kD), specular coefficient (kS), and shininess

### Lighting System
- **Light types**: Ambient, Directional, Point, and Spot lights
- **Shadow rendering** with epsilon offset handling for edge cases
- Physically plausible light attenuation and falloff

### Anti-Aliasing & Supersampling
- **Jittered grid sampling** for smooth edges
- Optional **adaptive sampling** for intelligent quality control
- Configurable sample count and patterns

### Performance & Scalability
- **Multi-threaded rendering** with configurable thread count
- **Progress tracking** via `PixelManager` for interactive feedback
- Efficient ray-geometry intersection caching

### Scene Management
- **XML scene loading** via `SceneBuilder` for quick scene setup
- Programmatic scene building via fluent API
- Background colors and ambient lighting configuration

### Testing & Demos
- Comprehensive **JUnit 5** test suite
- Render demos included as tests (`ShadowTests`, `ReflectionRefractionTests`, `TeapotTest`, etc.)
- Tests produce PNG images demonstrating various features

---

## Project Structure

```
src/
├── primitives/      # Math foundations: Point, Vector, Ray, Color, Double3, Material
├── geometries/      # Shapes and intersection logic: Sphere, Plane, Triangle, etc.
├── lighting/        # Light types: Ambient, Directional, Point, Spot
├── scene/           # Scene container and XML SceneBuilder
└── renderer/        # Camera, RayTracer, ImageWriter, PixelManager, anti-aliasing

unittests/
├── primitives/      # Unit tests for geometric primitives
├── geometries/      # Intersection and geometry tests
├── lighting/        # Lighting and shading tests
└── renderer/        # Render demos with visual output

images/             # Output folder for rendered PNG images (created automatically)
```

---

## System Requirements

- **Java Development Kit (JDK) 17 or higher**  
  *(The project uses Java `record` types in `PixelManager`, requiring Java 16+)*
- **IDE**: IntelliJ IDEA or Eclipse (or compatible Java IDE)
- **JUnit 5 (Jupiter)** for running tests
- **Maven/Gradle** (optional—not included by default)

---

## Getting Started

### IntelliJ IDEA Setup

1. **Clone/Open** the repository folder in IntelliJ IDEA.

2. **Configure the Project SDK**:
   - Go to *File → Project Structure → Project*
   - Set the **SDK** to **JDK 17+**
   - Set the **Language Level** to **17** or higher

3. **Mark Source Folders** (if not auto-detected):
   - Right-click `src/` → *Mark Directory as* → **Sources Root**
   - Right-click `unittests/` → *Mark Directory as* → **Test Sources Root**

4. **Add JUnit 5**:
   - IntelliJ usually detects the test framework from imports
   - If prompted, click **Add JUnit 5 library**
   - Alternatively, go to *Project Structure → Libraries* and add **JUnit 5 (Jupiter)**

### Eclipse Setup

1. **Import** the repository as an existing project.

2. **Configure JDK**:
   - Right-click project → *Properties → Java Build Path*
   - Set the **JRE** to **Java 17+**

3. **Mark Source Folders**:
   - `src/` as source folder
   - `unittests/` as source folder

4. **Add JUnit 5**:
   - Right-click project → *Build Path → Add Libraries*
   - Select **JUnit** and choose **JUnit 5**

---

## Running the Renderer

The easiest way to use the renderer is through the included **render demos**, which are implemented as JUnit tests.

### Run Render Demo Tests

Navigate to `unittests/renderer/` and run any of these test classes:

- **`ShadowTests`** — Demonstrates shadow rendering
- **`ReflectionRefractionTests`** — Shows reflective and refractive materials
- **`TeapotTest`** — Complex mesh rendering
- **`CustomImageTest`** — Custom scene setup
- **`RenderTests`** — Additional render demonstrations

**To run a test:**
1. Right-click the test class in your IDE
2. Select **Run** (or press `Ctrl+Shift+F10` in IntelliJ)
3. Check the `images/` folder for generated PNG files

> **Tip**: Some tests may be marked with `@Disabled`. Remove or comment out the annotation to enable them.

---

## Quick Start Example

Here's a minimal example to render a red sphere with a light source:

```java
// Create scene
Scene scene = new Scene("Demo")
    .setBackground(new Color(0, 0, 0))
    .setAmbientLight(new AmbientLight(new Color(20, 20, 20), new Double3(1)));

// Add geometry
scene.geometries.add(
    new Sphere(new Point(0, 0, -100), 50)
        .setEmission(new Color(50, 20, 20))
        .setMaterial(new Material()
            .setKd(new Double3(0.6))      // Diffuse coefficient
            .setKs(new Double3(0.4))      // Specular coefficient
            .setShininess(100))            // Specular exponent
);

// Add light
scene.lights.add(new PointLight(new Color(400, 400, 400), new Point(50, 50, 0)));

// Configure camera and render
Camera camera = Camera.getBuilder()
    .setLocation(new Point(0, 0, 0))
    .setDirection(new Vector(0, 0, -1), new Vector(0, -1, 0))
    .setVpDistance(100)
    .setVpSize(200, 200)
    .setImageWriter(new ImageWriter("demo", 800, 800))
    .setRayTracer(new SimpleRayTracer(scene))
    .build();

camera.renderImage();
camera.writeToImage();  // Writes to images/demo.png
```

---

## Configuration Guide

### Camera Settings

Configure rendering behavior via the `Camera.Builder`:

```java
Camera camera = Camera.getBuilder()
    .setLocation(Point)              // Camera position
    .setDirection(forward, up)       // View direction and up vector
    .setVpDistance(double)           // Distance to view plane
    .setVpSize(width, height)        // View plane size (world units)
    .setImageWriter(ImageWriter)     // Output image configuration
    .setRayTracer(RayTracer)         // Ray tracing engine
    .setThreadsCount(int)            // Number of threads (optional)
    .setSampleSize(int)              // Supersampling grid size
    .setSampleNum(int)               // Samples per pixel
    .setAdaptiveSampling(boolean)    // Enable adaptive sampling
    .build();
```

### Material Properties

Define material behavior with these coefficients:

```java
Material material = new Material()
    .setKd(Double3)          // Diffuse coefficient (0.0 - 1.0)
    .setKs(Double3)          // Specular coefficient (0.0 - 1.0)
    .setShininess(int)       // Specular exponent (higher = shinier)
    .setKr(Double3)          // Reflection coefficient
    .setKt(Double3);         // Refraction/transparency coefficient
```

### Light Types

The renderer supports multiple light sources:

```java
// Ambient lighting (global illumination)
new AmbientLight(Color, intensity)

// Directional light (like sunlight)
new DirectionalLight(Color, direction)

// Point light (omnidirectional from a point)
new PointLight(Color, position)

// Spotlight (cone-shaped illumination)
new SpotLight(Color, position, direction, angle)
```

---

## Loading Scenes from XML

Use `SceneBuilder` to load scenes from XML files:

```java
Scene scene = SceneBuilder.buildSceneFromXml("path/to/scene.xml");
```

### Supported XML Elements

```xml
<scene background-color="R G B">
    <ambient-light color="R G B" />
    <geometries>
        <sphere center="x y z" radius="r" />
        <triangle p0="x y z" p1="x y z" p2="x y z" />
        <plane point="x y z" normal="x y z" />
        <tube axisRay="x y z dx dy dz" radius="r" />
        <cylinder axisRay="x y z dx dy dz" radius="r" height="h" />
        <polygon>
            <point>x y z</point>
            <point>x y z</point>
            <!-- more points -->
        </polygon>
    </geometries>
</scene>
```

**Note**: The XML loader focuses on basic scene setup. Lights and camera configuration are best done programmatically.

---

## Extending the Renderer

### Adding New Geometries

1. Create a new class implementing `Intersectable`
2. Implement the `findIntersections(Ray)` method
3. Ensure proper normal vector computation
4. Add your geometry to the `geometries/` package

### Implementing Custom Ray Tracers

Extend `RayTracerBase` or modify `SimpleRayTracer` to implement custom lighting models, color computations, or optimization strategies.

### Custom Sampling Strategies

Modify the `JitteredGrid` class or create a new sampling strategy to customize anti-aliasing behavior. Integrate with the `Camera` class for rendering.

---

## Troubleshooting

### No Images Are Generated

- Verify that a render test was executed successfully
- Ensure `camera.writeToImage()` is called in your code
- Check that the `images/` folder exists (create manually if needed)
- Review test output for any exceptions

### JUnit 5 Not Found

- Confirm JUnit 5 (Jupiter) is added to your project classpath
- In IntelliJ: *File → Project Structure → Libraries → Add library → JUnit 5*
- In Eclipse: Right-click project → *Build Path → Add Libraries → JUnit*

### Compilation Errors About `record`

- Verify your **Project SDK** is set to **JDK 17+**
- Check *Project Structure → Project → Language Level* is at least **Java 17**
- Rebuild the project

### Rendering is Slow

- Reduce `setSampleSize()` or `setSampleNum()` for faster (lower-quality) renders
- Increase `setThreadsCount()` to use more CPU cores
- Disable `setAdaptiveSampling()` if it's enabled
- Simplify your scene (fewer geometries or lights)

---

## Output

Rendered images are automatically saved to:

```
<project-root>/images/*.png
```

The `images/` folder is listed in `.gitignore` and will be created automatically on first render.

---

## Example Test Runs

To see the renderer in action:

1. Open `unittests/renderer/ShadowTests.java`
2. Right-click the class and select **Run**
3. Wait for rendering to complete
4. Open `images/` folder to view the generated PNG

Each demo produces different visual effects, making it easy to understand the renderer's capabilities.

---

## Architecture Overview

The renderer is organized in layers of increasing abstraction:

1. **Primitives** — Mathematical foundations (Point, Vector, Color, Ray)
2. **Geometries** — Shape definitions and ray-geometry intersections
3. **Lighting** — Light sources and material shading
4. **Scene** — Scene graph and XML loading
5. **Renderer** — Camera, ray tracing, and image output

This layered design makes it easy to understand, test, and extend individual components.

---

## License

This project is provided as-is for educational and development purposes.

---

## Contact & Support

For questions or issues:
- Review the inline code documentation
- Check the JUnit tests for usage examples
- Examine the `unittests/renderer/` demos for visual reference
