package jmri.util.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common utility to draw colored rectangular Image.
 * Used as background in jmrit.catalog, jmrit.display.CPE#AddItems, Palette.
 *
 * @author Egbert Broerse copyright (c) 2017
 */
public class DrawSquares {

    /**
     * Produce either a plain image or a grid of gray/white squares.
     *
     * @param width  image width in pixels to match parent frame size
     * @param height image height in pixels to match parent frame size
     * @param dim    length of sides of squares in pixels
     * @param color1 background color
     * @param color2 contrasting squares fill color
     * @see jmri.jmrit.catalog.PreviewDialog#setupPanel()
     */
    public static BufferedImage getImage(int width, int height, int dim, Color color1, Color color2) {
        Color sqColor = new Color(235, 235, 235); // light gray
        Color bgColor = Color.white;
        int sqSize = 10; // square width in pixels
        BufferedImage back;
        int w = 500;
        int h = 500;
        if (width > 0) { w = width; }
        if (height > 0) { h = height; }
        sqSize = dim;
        if (color1 != null) {
            bgColor = color1;
        }
        if (color2 != null) {
            sqColor = color2;
        }
        // paint alternate squares
        back = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = back.createGraphics();
        g2d.setColor(bgColor);
        g2d.fillRect(0, 0, w, h); // plain rect background
        if (sqColor != bgColor) {
            g2d.setColor(sqColor);
            for (int j = 0; j <= (w / sqSize); j++) {
                for (int k = 0; k <= (h / sqSize); k++) {
                    if ((j + k) % 2 == 0) { // skip every other square
                        g2d.fillRect(j * sqSize, k * sqSize, sqSize, sqSize); // gray squares
                    }
                }
            }
        }
        g2d.dispose();

        // this.setSize(w, h);
        // Dimension d1 = new Dimension(w, h);
        // this.setPreferredSize(d1);
        // this.setMinimumSize(d1);
        // this.setMaximumSize(d1);
        // this.setOpaque(true);
        log.debug("DrawSquares ready");
        return back;
    }

    private static final Logger log = LoggerFactory.getLogger(DrawSquares.class);

}
