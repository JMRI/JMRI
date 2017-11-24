package jmri.util.swing;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
//import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common utility to draw a BufferedImage as background for a JPanel.
 * Used in jmrit.catalog, jmrit.display.LE#AddItems, Palette.
 *
 * @author Egbert Broerse copyright (c) 2017
 */
public class ImagePanel extends JPanel {

    private Image back = null;

    /**
     * Set background images for ImagePanel.
     * @see jmri.jmrit.catalog.PreviewDialog#paintCheckers()
     */
    public void setImage(Image img) {
        back = img;
        repaint();
        log.debug("DrawPanel ready");
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (back != null) {
            int imgWidth, imgHeight;
            imgWidth = getWidth();
            imgHeight = getHeight();
            g.drawImage(back, 0, 0, imgWidth, imgHeight, this);
        }
    }

    private void update() {
        repaint();
    }

    private static final Logger log = LoggerFactory.getLogger(ImagePanel.class);

}
