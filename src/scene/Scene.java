package scene;

import lighting.AmbientLight;
import geometries.Geometries;
import lighting.LightSource;
import primitives.Color;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents a 3D scene with background color, ambient light, and geometries.
 * Contains properties for the scene's name, background color, ambient light, geometries, and light sources.
 * Provides methods for setting these properties and adding geometries.
 *
 * @author Adir and Meir
 */
public class Scene {
    /**
     * The name of the scene
     */
    public final String name;

    /**
     * The background color of the scene
     */
    public Color background = Color.BLACK;

    /**
     * The ambient light of the scene
     */
    public AmbientLight ambientLight = AmbientLight.NONE;

    /**
     * The geometries in the scene
     */
    public Geometries geometries = new Geometries();

    /**
     * The list of light sources in the scene.
     */
    public List<LightSource> lights = new LinkedList<>();

    /**
     * Constructor for creating a scene with a given name.
     *
     * @param name the name of the scene
     */
    public Scene(String name) {
        this.name = name;
    }

    /**
     * Sets the background color of the scene.
     *
     * @param background the background color to set
     * @return the current Scene object for chaining
     */
    public Scene setBackground(Color background) {
        this.background = background;
        return this;
    }

    /**
     * Sets the ambient light of the scene.
     *
     * @param ambientLight the ambient light to set
     * @return the current Scene object for chaining
     */
    public Scene setAmbientLight(AmbientLight ambientLight) {
        this.ambientLight = ambientLight;
        return this;
    }

    /**
     * Sets the geometries of the scene.
     *
     * @param geometries the geometries to set
     * @return the current Scene object for chaining
     */
    public Scene setGeometries(Geometries geometries) {
        this.geometries = geometries;
        return this;
    }

    /**
     * Adds multiple geometries to the scene.
     *
     * @param geometries the geometries to add
     * @return the current Scene object for chaining
     */
    public Scene addGeometries(Geometries... geometries) {
        this.geometries.add(geometries);
        return this;
    }

    /**
     * Sets the list of light sources in the scene.
     *
     * @param lights the light sources
     * @return the Scene object itself for chaining
     */
    public Scene setLights(List<LightSource> lights) {
        this.lights = lights;
        return this;
    }
}