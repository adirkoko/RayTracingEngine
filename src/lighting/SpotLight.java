package lighting;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

import static primitives.Util.alignZero;

/**
 * Class representing a spotlight source.
 * SpotLight has a specific position, direction, and intensity.
 * Provides methods to get light intensity, direction, and distance at a given point.
 * Allows setting attenuation factors and narrow beam factor for the light.
 *
 * @author Adir and Meir.
 */
public class SpotLight extends PointLight {

    /**
     * Determines the focus of the light beam
     * Higher values mean a narrower beam.
     */
    private double narrowBeam = 1;

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
        double projection = alignZero(direction.dotProduct(getL(p)));
        return projection <= 0 ? Color.BLACK
                : super.getIntensity(p)
                .scale((narrowBeam == 1) ? projection : Math.pow(projection, narrowBeam));
    }

    @Override
    public SpotLight setKc(double kC) {
        return (SpotLight) super.setKc(kC);
    }

    @Override
    public SpotLight setKl(double kL) {
        return (SpotLight) super.setKl(kL);
    }

    @Override
    public SpotLight setKq(double kQ) {
        return (SpotLight) super.setKq(kQ);
    }

    /**
     * Sets the narrow beam factor.
     *
     * @param narrowBeam the narrow beam factor
     * @return the SpotLight object itself for chaining
     */
    public SpotLight setNarrowBeam(double narrowBeam) {
        this.narrowBeam = narrowBeam;
        return this;
    }

}
