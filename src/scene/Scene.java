package scene;

import lighting.AmbientLight;
import geometries.Geometries;
import primitives.Color;

/**
 * Represents a 3D scene with background color, ambient light, and geometries.
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
        for (Geometries geo : geometries) {
            this.geometries.add(geo);
        }
        return this;
    }
}