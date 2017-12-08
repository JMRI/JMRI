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

    private BufferedImage back = null;
    private BufferedImage clip = null;
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
        back = (BufferedImage) img;
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
            imgWidth = back.getWidth(this);
            imgHeight = back.getHeight(this);
            double frameRatio = (double) getWidth() / (double) getHeight();
            double imgRatio = imgWidth / imgHeight;
            log.debug("ratios: fr {} - img {}", frameRatio, imgRatio);
//            // maintain squares on non square panels, reduce to fit frame
//            if (frameRatio < imgRatio) {// width limited
//                imgWidth = getWidth();
//                imgHeight = (int) (getWidth() / imgRatio);
//            } else { // height limited
//                imgWidth = (int) (getHeight() * imgRatio);
//                imgHeight = getHeight();
//            }

            // maintain squares on non square panels, enlarge to fill full frame
            if (frameRatio < imgRatio) { // image more oblong than frame
                imgWidth = (int) (imgHeight * frameRatio); // clip width
                // keep full imgHeight
            } else { // image taller than frame
                // keep full imgWidth
                imgHeight = (int) (imgWidth / frameRatio); // clip height
            }
            // clip part op back image
            clip = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);
            clip = back.getSubimage(0, 0, imgWidth, imgHeight);

            g.drawImage(clip, 0, 0, getWidth(), getHeight(), this);
        }
    }

    //private void update() {
    //    repaint();
    //}

    private static final Logger log = LoggerFactory.getLogger(ImagePanel.class);

}
