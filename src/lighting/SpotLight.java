package lighting;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

import static primitives.Util.alignZero;

/**
 * Class representing a spotlight source.
 */
public class SpotLight extends PointLight {

    /**
     * The direction of the spotlight.
     */
    private final Vector direction;

    /**
     * Constructor for SpotLight.
     *
     * @param intensity the intensity of the light source
     * @param position  the position of the light source
     * @param direction the direction of the light source
     */
    public SpotLight(Color intensity, Point position, Vector direction) {
        super(intensity, position);
        this.direction = direction.normalize();
    }

    @Override
    public Color getIntensity(Point p) {
        double projection = getL(p).dotProduct(direction);
        return projection <= 0 ? Color.BLACK : super.getIntensity(p).scale(projection);
    }
}
