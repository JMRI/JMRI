package jmri.util.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common utility to draw a background of white sqaures.
 * Used in Catalog, LE AddItems, Palette.
 *
 * @author Egbert Broerse copyright (c) 2017
 */
public class DrawSquares extends JLabel {

    private final Color sqColor = Color.white;
    private int sqSize = 5; // square width in pixels
    int w = 1000;
    int h = 1000;

    /**
     * Draw grid of white squares on a (transparent) JLabel.
     * @see jmri.jmrit.catalog.PreviewDialog#paintCheckers()
     */
    public DrawSquares(JLayeredPane bg, int dim) {
        //w = bg.getWidth();
        //h = bg.getHeight();
        sqSize = dim;

        this.setSize(w, h);
        this.setOpaque(false);
        log.debug("DrawSquares ready");
    }

    @Override
    public void paint(Graphics g) {
        // paint the white squares
        g.setColor(sqColor);
        log.debug("w = {} h = {}", w, h);
        for (int j = 0; j <= (w / sqSize); j++) {
            for (int k = 0; k <= (h / sqSize); k++) {
                log.debug("j = {}, k = {}", k, j);
                if ((j + k) % 2 == 0) { // skip every other square
                    g.fillRect(k * sqSize, j * sqSize, sqSize, sqSize);
                    log.debug("j = {}, k = {}", k, j);
                }
            }
        }
    }

    private static final Logger log = LoggerFactory.getLogger(DrawSquares.class);

}
