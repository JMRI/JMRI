package jmri.util.swing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common utility to draw colored rectangular Image.
 *
 * @author Egbert Broerse copyright (c) 2017
 */
public class DrawSquares {

    /**
     * Produce a grid of contrasting squares. Ideally the width and height match
     * the parent frame size.
     *
     * @param width  image width in pixels
     * @param height image height in pixels
     * @param dim    length of sides of squares in pixels
     * @param color1 background color
     * @param color2 contrasting fill color
     * @return the image with a grid of squares
     */
    public static BufferedImage getImage(int width, int height, int dim, Color color1, Color color2) {
        Color sqColor = new Color(235, 235, 235); // light gray
        Color bgColor = Color.white;
        BufferedImage result;
        int w = 500;
        int h = 500;
        if (width > 0) {
            w = width;
        }
        if (height > 0) {
            h = height;
        }
        if (color1 != null) {
            bgColor = color1;
        }
        if (color2 != null) {
            sqColor = color2;
        }
        // paint alternate squares
        result = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = result.createGraphics();
        g2d.setColor(bgColor);
        g2d.fillRect(0, 0, w, h); // plain rect background
        if (sqColor != bgColor) {
            g2d.setColor(sqColor);
            for (int j = 0; j <= (w / dim); j++) {
                for (int k = 0; k <= (h / dim); k++) {
                    if ((j + k) % 2 == 0) { // skip every other square
                        g2d.fillRect(j * dim, k * dim, dim, dim); // gray squares
                    }
                }
            }
        }
        g2d.dispose();

        log.debug("DrawSquares ready");
        return result;
    }

    private static final Logger log = LoggerFactory.getLogger(DrawSquares.class);

}
