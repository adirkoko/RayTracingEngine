package renderer;

import geometries.*;
import lighting.AmbientLight;
import lighting.PointLight;
import lighting.SpotLight;
import org.junit.jupiter.api.Test;
import primitives.*;
import scene.Scene;

/**
 * CustomImage Test.
 *
 * @author Adir and Meir
 */
public class CustomImageTest {

    /**
     * Creates a scene with intersecting spheres and a box.
     *
     * @return The scene configured with geometries and lights.
     */
    private Scene createCustomScene() {
        Scene scene = new Scene("Intersecting Spheres and Box Scene");
        scene.setBackground(new Color(135, 206, 235)) // Light sky blue
                .setAmbientLight(new AmbientLight(new Color(255, 255, 255), 0.1)); // white ambient light

        // Add geometries to the scene
        scene.geometries.add(
                // Plane
                new Plane(new Point(0, -51, 0), new Vector(0, 1, 0))
                        .setEmission(new Color(0, 0, 0)) // Black color
                        .setMaterial(new Material().setKd(0.5).setKs(0.5).setKr(0.5).setShininess(100)),

                // Intersecting Spheres
                new Sphere(new Point(-60, 0, -170), 50d)
                        .setEmission(new Color(255, 0, 0)) // Red color
                        .setMaterial(new Material().setKd(0.5).setKs(0.5).setKt(0.3).setShininess(50)),
                new Sphere(new Point(0, 0, -150), 50d)
                        .setEmission(new Color(0, 0, 255)) // Blue color
                        .setMaterial(new Material().setKd(0.5).setKs(0.5).setKt(0.3).setShininess(50)),
                // Box
                new Geometries(
                        new Polygon( // Left face
                                new Point(20, -50, -200),
                                new Point(20, -50, -100),
                                new Point(20, 50, -100),
                                new Point(20, 50, -200))
                                .setEmission(new Color(34, 139, 34))
                                .setMaterial(new Material().setKd(0.5).setKs(0.5).setShininess(30)),
                        new Polygon( // Right face
                                new Point(120, -50, -200),
                                new Point(120, -50, -100),
                                new Point(120, 50, -100),
                                new Point(120, 50, -200))
                                .setEmission(new Color(34, 139, 34))
                                .setMaterial(new Material().setKd(0.5).setKs(0.5).setShininess(30)),
                        new Polygon( // Back face
                                new Point(20, -50, -200),
                                new Point(120, -50, -200),
                                new Point(120, 50, -200),
                                new Point(20, 50, -200))
                                .setEmission(new Color(34, 139, 34))
                                .setMaterial(new Material().setKd(0.5).setKs(0.5).setShininess(30)),
                        new Polygon( // Bottom face
                                new Point(20, -50, -200),
                                new Point(120, -50, -200),
                                new Point(120, -50, -100),
                                new Point(20, -50, -100))
                                .setEmission(new Color(34, 139, 34))
                                .setMaterial(new Material().setKd(0.5).setKs(0.5).setShininess(30)),
                        new Polygon( // Front face
                                new Point(20, -50, -100),
                                new Point(120, -50, -100),
                                new Point(120, 50, -100),
                                new Point(20, 50, -100))
                                .setEmission(new Color(34, 139, 34))
                                .setMaterial(new Material().setKd(0.5).setKs(0.5).setShininess(30)),
                        new Polygon( // Top face
                                new Point(20, 50, -200),
                                new Point(120, 50, -200),
                                new Point(120, 50, -100),
                                new Point(20, 50, -100))
                                .setEmission(new Color(34, 139, 34))
                                .setMaterial(new Material().setKd(0.5).setKs(0.5).setShininess(30)))
        );

        // Add lights
        scene.lights.add(new SpotLight(
                new Color(255, 255, 255),
                new Point(200, 200, 100),
                new Vector(-1, -1, -2))
                .setKl(0.0000005).setKq(0.0000005));

        scene.lights.add(new PointLight(
                new Color(255, 255, 255),
                new Point(-200, 200, 100))
                .setKl(0.0005).setKq(0.0005));

        return scene;
    }

    /**
     * Renders an image with no anti-aliasing.
     */
    @Test
    public void renderCustomImage_NoAntiAliasing() {
        Scene scene = createCustomScene();

        Camera.getBuilder()
                .setDirection(new Vector(0, 0, -1), new Vector(0, 1, 0))
                .setLocation(new Point(0, 100, 600))
                .setVpDistance(1000)
                .setVpSize(500, 500)
                .setRayTracer(new SimpleRayTracer(scene))
                .setImageWriter(new ImageWriter("CustomImageNoAnti-aliasing", 1000, 1000)) // File name and image size
                .build()
                .renderImage()
                .writeToImage();
    }

    /**
     * Renders an image with anti-aliasing.
     */
    @Test
    public void renderCustomImage_WithAntiAliasing() {
        Scene scene = createCustomScene();

        Camera.getBuilder()
                .setDirection(new Vector(0, 0, -1), new Vector(0, 1, 0))
                .setLocation(new Point(0, 100, 600))
                .setVpDistance(1000)
                .setVpSize(500, 500)
                .setRayTracer(new SimpleRayTracer(scene))
                .setImageWriter(new ImageWriter("CustomImageWithAnti-aliasing", 100, 100)) // File name and image size
                .setSampleSize(17)
                .build()
                .renderImage()
                .writeToImage();
    }

    /**
     * Renders an image with adaptive anti-aliasing.
     */
    @Test
    public void renderCustomImage_WithAdaptiveAntiAliasing() {
        Scene scene = createCustomScene();

        Camera.getBuilder()
                .setDirection(new Vector(0, 0, -1), new Vector(0, 1, 0))
                .setLocation(new Point(0, 100, 600))
                .setVpDistance(1000)
                .setVpSize(500, 500)
                .setRayTracer(new SimpleRayTracer(scene))
                .setImageWriter(new ImageWriter("CustomImageWithAdaptiveAnti-aliasing", 100, 100)) // File name and image size
                .setSampleSize(17)
                .setAdaptiveSampling(true)
                .build()
                .renderImage()
                .writeToImage();
    }
}
