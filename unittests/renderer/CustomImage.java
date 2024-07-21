package renderer;

import geometries.*;
import lighting.AmbientLight;
import lighting.PointLight;
import lighting.SpotLight;
import primitives.*;
import scene.Scene;

public class CustomImage {
    public static void main(String[] args) {
        // Scene
        Scene scene = new Scene("Intersecting Spheres and Box Scene");
        scene.setBackground(new Color(135, 206, 235)) // Light sky blue
                .setAmbientLight(new AmbientLight(new Color(255, 255, 255), 0.1)); // Light white ambient light

        // Plane
        Intersectable plane = new Plane(new Point(0, -51, 0), new Vector(0, 1, 0))
                .setEmission(new Color(0, 0, 0)) // Black color
                .setMaterial(new Material().setKd(0.5).setKs(0.5).setKr(0.5).setShininess(100));

        // Intersecting Spheres
        Intersectable sphere1 = new Sphere(new Point(-60, 0, -170), 50d)
                .setEmission(new Color(255, 0, 0)) // Red color
                .setMaterial(new Material().setKd(0.5).setKs(0.5).setKt(0.3).setShininess(50));

        Intersectable sphere2 = new Sphere(new Point(0, 0, -150), 50d)
                .setEmission(new Color(0, 0, 255)) // Blue color
                .setMaterial(new Material().setKd(0.5).setKs(0.5).setKt(0.3).setShininess(50));

        // Box
        Intersectable box = new Geometries(
                new Polygon(
                        new Point(20, -50, -200),
                        new Point(20, -50, -100),
                        new Point(20, 50, -100),
                        new Point(20, 50, -200)) // Left face
                        .setEmission(new Color(34, 139, 34))
                        .setMaterial(new Material().setKd(0.5).setKs(0.5).setShininess(30)),
                new Polygon(
                        new Point(120, -50, -200),
                        new Point(120, -50, -100),
                        new Point(120, 50, -100),
                        new Point(120, 50, -200)) // Right face
                        .setEmission(new Color(34, 139, 34))
                        .setMaterial(new Material().setKd(0.5).setKs(0.5).setShininess(30)),
                new Polygon(
                        new Point(20, -50, -200),
                        new Point(120, -50, -200),
                        new Point(120, 50, -200),
                        new Point(20, 50, -200)) // Back face
                        .setEmission(new Color(34, 139, 34))
                        .setMaterial(new Material().setKd(0.5).setKs(0.5).setShininess(30)),
                new Polygon(
                        new Point(20, -50, -200),
                        new Point(120, -50, -200),
                        new Point(120, -50, -100),
                        new Point(20, -50, -100)) // Bottom face
                        .setEmission(new Color(34, 139, 34))
                        .setMaterial(new Material().setKd(0.5).setKs(0.5).setShininess(30)),
                new Polygon(
                        new Point(20, -50, -100),
                        new Point(120, -50, -100),
                        new Point(120, 50, -100),
                        new Point(20, 50, -100)) // Front face
                        .setEmission(new Color(34, 139, 34))
                        .setMaterial(new Material().setKd(0.5).setKs(0.5).setShininess(30)),
                new Polygon(
                        new Point(20, 50, -200),
                        new Point(120, 50, -200),
                        new Point(120, 50, -100),
                        new Point(20, 50, -100)) // Top face
                        .setEmission(new Color(34, 139, 34))
                        .setMaterial(new Material().setKd(0.5).setKs(0.5).setShininess(30))
        );

        // Add geometries to the scene
        scene.geometries.add(plane, sphere1, sphere2, box);

        // Add lights
        scene.lights.add(
                new SpotLight(new Color(255, 255, 255), new Point(200, 200, 100), new Vector(-1, -1, -2))
                        .setKl(0.0000005).setKq(0.0000005)
        );

        scene.lights.add(
                new PointLight(new Color(255, 255, 255), new Point(-200, 200, 100))
                        .setKl(0.0005).setKq(0.0005)
        );

        // Camera
        Camera camera = Camera.getBuilder()
                .setDirection(new Vector(0, 0, -1), new Vector(0, 1, 0))
                .setLocation(new Point(0, 100, 600))
                .setVpDistance(1000)
                .setVpSize(500, 500)
                .setRayTracer(new SimpleRayTracer(scene))
                .setImageWriter(new ImageWriter("intersectingSpheresAndBoxImage", 10000, 10000)) // File name and image size
                .build();

        // Render the image
        camera.renderImage();
        camera.writeToImage();
    }
}
