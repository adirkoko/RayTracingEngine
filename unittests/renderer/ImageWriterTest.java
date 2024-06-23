package renderer;

import org.junit.jupiter.api.Test;
import primitives.Color;

/**
 * Unit tests for {@link renderer.ImageWriter} class
 *
 * @author Adir and Meir
 */
class ImageWriterTest {

    /**
     * Test method for {@link renderer.ImageWriter#writeToImage()}.
     */
    @Test
    void testWriteToImage() {
        int width = 800;  // width of the image
        int height = 500; // height of the image
        int rows = 10;    // number of rows in the grid
        int columns = 16; // number of columns in the grid

        // Colors for the grid lines and fill
        Color gridColor = new Color(224, 34, 0);
        Color fillColor = new Color(255, 255, 1);

        // Create ImageWriter object
        ImageWriter imageWriter = new ImageWriter("gridImage", width, height);

        // Loop through each pixel in the image
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Determine if the pixel is part of the grid lines
                if (y % (height / rows) == 0 || x % (width / columns) == 0) {
                    imageWriter.writePixel(x, y, gridColor); // Set grid line color
                } else {
                    imageWriter.writePixel(x, y, fillColor); // Set fill color
                }
            }
        }

        // Write the image to file
        imageWriter.writeToImage();
    }
}