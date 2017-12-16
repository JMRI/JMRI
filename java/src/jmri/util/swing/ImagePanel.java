package jmri.util.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common utility to draw a BufferedImage as background behind a JPanel.
 * Used in jmri.jmrit.catalog, jmri.jmrit.display.palette (via LayoutEditor 'Add Items' menu).
 *
 * @author Egbert Broerse copyright (c) 2017
 */
public class ImagePanel extends JPanel {

    private Image back = null;
    int imgWidth;
    int imgHeight;

    /**
     * Set background images for ImagePanel.
     * @see jmri.jmrit.catalog.PreviewDialog#setupPanel()
     * @see jmri.jmrit.catalog.CatalogPanel#makeButtonPanel()
     *
     * @param img Image to load as background
     */
    public void setImage(Image img) {
        back = img;
        repaint();
        log.debug("DrawPanel ready");
    }

    //public Dimension getPreferredSize() {
    //    return new Dimension(imgWidth, imgHeight);
    //}

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (back != null) {
            imgWidth = getWidth();
            imgHeight = getHeight();
            g.drawImage(back, 0, 0, imgWidth, imgHeight, this);
        }
    }

    //private void update() {
    //    repaint();
    //}

    private static final Logger log = LoggerFactory.getLogger(ImagePanel.class);

}
