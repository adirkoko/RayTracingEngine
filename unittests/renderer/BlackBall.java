package renderer;

import geometries.*;
import lighting.AmbientLight;
import org.junit.jupiter.api.Test;
import primitives.*;
import scene.Scene;

/**
 * BlackBall Test.
 *
 * @author Adir and Meir
 */
public class BlackBall {

    /**
     * Scene setup method
     */
    private Scene createBlackBallScene() {
        Scene scene = new Scene("Test");
        scene.setBackground(new Color(255, 255, 255)) // White background
                .setAmbientLight(new AmbientLight(new Color(255, 255, 255), 0.1)); // White ambient light

        // Add geometries to the scene
        scene.geometries.add(new Sphere(new Point(0, 100, 0), 100d)
                .setEmission(new Color(0, 0, 0))); // Black sphere

        return scene;
    }

    /**
     * Black ball with no anti-aliasing test.
     */
    @Test
    public void blackBall_NoAntiAliasing() {
        Camera.getBuilder()
                .setDirection(new Vector(0, 0, -1), new Vector(0, 1, 0))
                .setLocation(new Point(0, 100, 600))
                .setVpDistance(1000)
                .setVpSize(500, 500)
                .setRayTracer(new SimpleRayTracer(createBlackBallScene()))
                .setImageWriter(new ImageWriter("blackBallNoAnti-aliasing", 500, 500)) // File name and image size
                .build()
                .renderImage()
                .writeToImage();
    }

    /**
     * Black ball with anti-aliasing test.
     */
    @Test
    public void blackBall_WithAntiAliasing() {
        Camera.getBuilder()
                .setDirection(new Vector(0, 0, -1), new Vector(0, 1, 0))
                .setLocation(new Point(0, 100, 600))
                .setVpDistance(1000)
                .setVpSize(500, 500)
                .setRayTracer(new SimpleRayTracer(createBlackBallScene()))
                .setImageWriter(new ImageWriter("blackBallWithAnti-aliasing", 500, 500)) // File name and image size
                .setSampleSize(55)
                .build()
                .renderImage()
                .writeToImage();
    }

    /**
     * Black ball with adaptive anti-aliasing test.
     */
    @Test
    public void blackBall_WithAdaptiveAntiAliasing() {
        Camera.getBuilder()
                .setDirection(new Vector(0, 0, -1), new Vector(0, 1, 0))
                .setLocation(new Point(0, 100, 600))
                .setVpDistance(1000)
                .setVpSize(500, 500)
                .setRayTracer(new SimpleRayTracer(createBlackBallScene()))
                .setImageWriter(new ImageWriter("blackBallWithAdaptiveAnti-aliasing", 500, 500)) // File name and image size
                .setSampleSize(55)
                .setAdaptiveSampling(true)
                .build()
                .renderImage()
                .writeToImage();
    }
}
