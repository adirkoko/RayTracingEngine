package scene;

import lighting.AmbientLight;
import org.w3c.dom.*;

import javax.xml.parsers.*;
import java.io.*;

import primitives.Color;
import primitives.Point;
import geometries.Sphere;
import geometries.Triangle;

/**
 * SceneBuilder is a utility class to build Scene objects from XML files.
 * This class uses the DOM parser to read and parse the XML file and extract scene information.
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
            File inputFile = new File(fileName);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile); // Parse the XML file
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
                scene.setAmbientLight(new AmbientLight(parseColor(ambientColor), 1));
            }

            // Parse geometries
            // Find the geometries element and process its child elements (sphere, triangle)
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
                                Point center = parsePoint(geometryElement.getAttribute("center"));
                                double radius = Double.parseDouble(geometryElement.getAttribute("radius"));
                                scene.geometries.add(new Sphere(center, radius));
                                break;
                            case "triangle":
                                // Parse triangle attributes and create a Triangle object
                                Point p0 = parsePoint(geometryElement.getAttribute("p0"));
                                Point p1 = parsePoint(geometryElement.getAttribute("p1"));
                                Point p2 = parsePoint(geometryElement.getAttribute("p2"));
                                scene.geometries.add(new Triangle(p0, p1, p2));
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
}
