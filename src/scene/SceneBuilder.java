package scene;

import geometries.*;
import lighting.AmbientLight;
import org.w3c.dom.*;

import javax.xml.parsers.*;
import java.io.*;

import primitives.*;

/**
 * SceneBuilder is a utility class to build Scene objects from XML files.
 * This class uses the DOM parser to read and parse the XML file and extract scene information.
 * Provides methods to parse colors, points, vectors, and rays from strings.
 *
 * @author Adir and Meir
 */
public class SceneBuilder {

    /**
     * Builds a Scene object from an XML file.
     *
     * @param fileName the XML file name
     * @return the constructed Scene object
     */
    public static Scene buildSceneFromXml(String fileName) {
        Scene scene = new Scene("XML Scene");

        try {
            // Prepare to read and parse the XML file
            Document doc = DocumentBuilderFactory
                    .newInstance()
                    .newDocumentBuilder()
                    .parse(new File(fileName)); // Parse the XML file
            doc.getDocumentElement().normalize(); // Normalize the XML structure

            // Parse background color
            // Extract the background-color attribute from the root element and parse it into a Color object
            String backgroundColor = doc.getDocumentElement().getAttribute("background-color");
            scene.setBackground(parseColor(backgroundColor));

            // Parse ambient light
            // Find the ambient-light element and extract its color attribute, then create an AmbientLight object
            NodeList ambientLightList = doc.getElementsByTagName("ambient-light");
            if (ambientLightList.getLength() > 0) {
                Element ambientLightElement = (Element) ambientLightList.item(0);
                String ambientColor = ambientLightElement.getAttribute("color");
                scene.setAmbientLight(new AmbientLight(parseColor(ambientColor), new Double3(1)));
            }

            // Parse geometries
            // Find the geometries element and process its child elements (sphere, triangle, cylinder, plane, tube, polygon)
            NodeList geometriesList = doc.getElementsByTagName("geometries");
            if (geometriesList.getLength() > 0) {
                Element geometriesElement = (Element) geometriesList.item(0);
                NodeList geometries = geometriesElement.getChildNodes();

                for (int i = 0; i < geometries.getLength(); i++) {
                    Node node = geometries.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Element geometryElement = (Element) node;
                        String tagName = geometryElement.getTagName();
                        switch (tagName) {
                            case "sphere":
                                // Parse sphere attributes and create a Sphere object
                                scene.geometries.add(new Sphere(
                                        parsePoint(geometryElement.getAttribute("center")),
                                        Double.parseDouble(geometryElement.getAttribute("radius"))));
                                break;
                            case "triangle":
                                // Parse triangle attributes and create a Triangle object
                                scene.geometries.add(new Triangle(
                                        parsePoint(geometryElement.getAttribute("p0")),
                                        parsePoint(geometryElement.getAttribute("p1")),
                                        parsePoint(geometryElement.getAttribute("p2"))));
                                break;
                            case "cylinder":
                                // Parse cylinder attributes and create a Cylinder object
                                scene.geometries.add(new Cylinder(
                                        parseRay(geometryElement.getAttribute("axisRay")),
                                        Double.parseDouble(geometryElement.getAttribute("radius")),
                                        Double.parseDouble(geometryElement.getAttribute("height"))));
                                break;
                            case "plane":
                                // Parse plane attributes and create a Plane object
                                scene.geometries.add(new Plane(
                                        parsePoint(geometryElement.getAttribute("point")),
                                        parseVector(geometryElement.getAttribute("normal"))));
                                break;
                            case "tube":
                                // Parse tube attributes and create a Tube object
                                scene.geometries.add(new Tube(
                                        parseRay(geometryElement.getAttribute("axisRay")),
                                        Double.parseDouble(geometryElement.getAttribute("radius"))));
                                break;
                            case "polygon":
                                // Parse polygon attributes and create a Polygon object
                                NodeList points = geometryElement.getElementsByTagName("point");
                                Point[] vertices = new Point[points.getLength()];
                                for (int j = 0; j < points.getLength(); j++) {
                                    vertices[j] = parsePoint(points.item(j).getTextContent());
                                }
                                scene.geometries.add(new Polygon(vertices));
                                break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Print the stack trace if there is an exception
        }

        return scene; // Return the constructed Scene object
    }

    /**
     * Parses a string representing a color in the format "R G B" into a Color object.
     *
     * @param colorString the string representing the color
     * @return the parsed Color object
     */
    private static Color parseColor(String colorString) {
        String[] rgb = colorString.split(" ");
        return new Color(Double.parseDouble(rgb[0]), Double.parseDouble(rgb[1]), Double.parseDouble(rgb[2]));
    }

    /**
     * Parses a string representing a point in the format "x y z" into a Point object.
     *
     * @param pointString the string representing the point
     * @return the parsed Point object
     */
    private static Point parsePoint(String pointString) {
        String[] coords = pointString.split(" ");
        return new Point(Double.parseDouble(coords[0]), Double.parseDouble(coords[1]), Double.parseDouble(coords[2]));
    }

    /**
     * Parses a string representing a vector in the format "x y z" into a Vector object.
     *
     * @param vectorString the string representing the vector
     * @return the parsed Vector object
     */
    private static Vector parseVector(String vectorString) {
        String[] coords = vectorString.split(" ");
        return new Vector(Double.parseDouble(coords[0]), Double.parseDouble(coords[1]), Double.parseDouble(coords[2]));
    }

    /**
     * Parses a string representing a ray in the format "px py pz dx dy dz" into a Ray object.
     *
     * @param rayString the string representing the ray
     * @return the parsed Ray object
     */
    private static Ray parseRay(String rayString) {
        String[] coords = rayString.split(" ");

        return new Ray(
                new Point(Double.parseDouble(coords[0]), Double.parseDouble(coords[1]), Double.parseDouble(coords[2])),
                new Vector(Double.parseDouble(coords[3]), Double.parseDouble(coords[4]), Double.parseDouble(coords[5])));
    }
}
