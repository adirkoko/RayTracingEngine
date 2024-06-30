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
        int width = 801;  // width of the image
        int height = 501; // height of the image
        int step = 50;
        // Colors for the grid lines and fill
        Color gridColor = new Color(224, 34, 0);
        Color fillColor = new Color(255, 255, 1);

        // Create ImageWriter object
        ImageWriter imageWriter = new ImageWriter("gridImage", width, height);

        // Loop through each pixel in the image
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                // Determine if the pixel is part of the grid lines
                imageWriter.writePixel(x, y, y % step == 0 || x % step == 0 ? gridColor : fillColor);

        // Write the image to file
        imageWriter.writeToImage();
    }
}