package jmri.util.swing;

import java.awt.Graphics;
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

    /**
     * Set background images for ImagePanel.
     * For specifics, 
     * see the setupPanel() private method in {@link jmri.jmrit.catalog.PreviewDialog}
     * and the makeButtonPanel() private method in {@link jmri.jmrit.catalog.CatalogPanel}
     *
     * @param img Image to load as background
     */
    public void setImage(Image img) {
        back = (BufferedImage) img;
        repaint();
        log.debug("DrawPanel ready");
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (back != null) {
            int imgWidth;
            int imgHeight;
            imgWidth = back.getWidth(this);
            imgHeight = back.getHeight(this);
            double frameRatio = (double) getWidth() / (double) getHeight();
            double imgRatio = (double) imgWidth / (double) imgHeight;
            log.debug("ratios: fr {} - img {}", frameRatio, imgRatio);

            // maintain squares on non square panels, enlarge to fill full frame
            if (frameRatio < imgRatio) { // image more oblong than frame
                imgWidth = (int) (imgHeight * frameRatio); // clip width
                // keep full imgHeight
            } else { // image taller than frame
                // keep full imgWidth
                imgHeight = (int) (imgWidth / frameRatio); // clip height
            }
            // clip part of back image
            clip = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);
            clip = back.getSubimage(0, 0, Math.min(imgWidth, back.getWidth(this)),
                    Math.min(imgHeight, back.getHeight(this))); // catch clip size error on change to different pane

            g.drawImage(clip, 0, 0, getWidth(), getHeight(), this);
        }
    }

    private static final Logger log = LoggerFactory.getLogger(ImagePanel.class);

}
