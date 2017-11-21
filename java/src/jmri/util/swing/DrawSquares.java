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
 * Common utility to draw a background of white sqaures.
 * Used in jmrit.catalog, jmrit.display.LE#AddItems, Palette.
 *
 * @author Egbert Broerse copyright (c) 2017
 */
public class DrawSquares extends JPanel {

    private final Color sqColor = new Color(235, 235, 235); // light gray
    private int sqSize = 5; // square width in pixels
    // private BufferedImage grid;
    int w = 1000;
    int h = 1000;

    /**
     * Draw grid of white squares on a (transparent) JLabel.
     * @see jmri.jmrit.catalog.PreviewDialog#paintCheckers()
     */
    public DrawSquares(JPanel bg, int dim) {
        // w = bg.getWidth();
        // h = bg.getHeight();
        sqSize = dim;

        this.setSize(w, h);
        Dimension d1 = new Dimension(w, h);
        this.setPreferredSize(d1);
        this.setMinimumSize(d1);
        this.setMaximumSize(d1);
        this.setOpaque(false);
        log.debug("DrawSquares ready");
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        // paint the gray squares
        g.setColor(sqColor);
        log.debug("Checkers w = {} h = {}", w, h);
        for (int j = 0; j <= (w / sqSize); j++) {
            for (int k = 0; k <= (h / sqSize); k++) {
                if ((j + k) % 2 == 0) { // skip every other square
                    g.fillRect(k * sqSize, j * sqSize, sqSize, sqSize); // gray square
                }
            }
        }
    }

    /* alternative graphic to replace the above paint() method */
    //    @Override
    //    public void paint(Graphics g) {
    //        // paint the gray squares
    //        if (grid == null) {
    //            BufferedImage grid = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    //            Graphics2D g2d = grid.createGraphics();
    //            g2d.setColor(sqColor);
    //            g2d.setClip(g.getClip());
    //            super.paint(g2d);
    //        log.debug("Checkers w = {} h = {}", w, h);
    //            for (int j = 0; j <= (w / sqSize); j++) {
    //                for (int k = 0; k <= (h / sqSize); k++) {
    //                    if ((j + k) % 2 == 0) { // skip every other square
    //                        g2d.fillRect(k * sqSize, j * sqSize, sqSize, sqSize);
    //                    }
    //                }
    //            }
    //            g2d.dispose();
    //        }
    //    }

    private void update() {
        repaint();
    }

    private static final Logger log = LoggerFactory.getLogger(DrawSquares.class);

}
