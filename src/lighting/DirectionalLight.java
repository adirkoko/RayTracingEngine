package lighting;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

/**
 * Class representing a directional light source.
 *
 * @author Adir and Meir.
 */
public class DirectionalLight extends Light implements LightSource {

    /**
     * The direction of the light source.
     */
    private final Vector direction;

    /**
     * Constructor for DirectionalLight.
     *
     * @param intensity the intensity of the light source
     * @param direction the direction of the light source
     */
    public DirectionalLight(Color intensity, Vector direction) {
        super(intensity);
        this.direction = direction.normalize();
    }

    @Override
    public Color getIntensity(Point p) {
        return intensity;
    }

    @Override
    public Vector getL(Point p) {
        return direction;
    }
}
