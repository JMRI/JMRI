package jmri.jmrit.catalog;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.net.URL;

import javax.annotation.CheckForNull;
import javax.swing.ImageIcon;

import jmri.util.FileUtil;

public abstract class NamedIcon extends ImageIcon {

    /**
     * Create an empty named icon
     */
    public NamedIcon() {
    }

    /**
     * Create a named icon from an Image. N.B. NamedIcon's create
     * using this constructor can NOT be animated GIFs
     * @param im Image to use
     */
    public NamedIcon(Image im) {
        super(im);
    }

    /**
     * Creates an NamedIcon from the specified URL.
     * @param location the URL for the image
     */
    public NamedIcon(URL location) {
        super(location);
    }

    /**
     * Find the NamedIcon corresponding to a file path. Understands the
     * <a href="http://jmri.org/help/en/html/doc/Technical/FileNames.shtml">standard
     * portable filename prefixes</a>.
     *
     * @param path The path to the file, either absolute or portable
     * @return the desired icon with this same name as its path
     */
    public static NamedIcon getIconByName(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        if (FileUtil.findURL(path) == null) {
            return null;
        }
        return new NamedIconSelector(path, path);
    }

    /**
     * Return the human-readable name of this icon.
     *
     * @return the name or null if not set
     */
    @CheckForNull
    public abstract String getName();

    /**
     * Set the human-readable name for this icon.
     *
     * @param name the new name, can be null
     */
    public abstract void setName(@CheckForNull String name);

    /**
     * Get the URL of this icon.
     *
     * @return the path to this icon in JMRI portable format or null if not set
     */
    @CheckForNull
    public abstract String getURL();

    /**
     * Set URL of original icon image. Setting this after initial construction
     * does not change the icon.
     *
     * @param url the URL associated with this icon
     */
    public abstract void setURL(@CheckForNull String url);

    /**
     * Get the number of 90-degree rotations needed to properly display this
     * icon.
     *
     * @return 0 (no rotation), 1 (rotated 90 degrees), 2 (180 degrees), or 3
     *         (270 degrees)
     */
    public abstract int getRotation();

    /**
     * Set the number of 90-degree rotations needed to properly display this
     * icon.
     *
     * @param pRotation 0 (no rotation), 1 (rotated 90 degrees), 2 (180
     *                  degrees), or 3 (270 degrees)
     * @param comp      the component containing this icon
     */
    public abstract void setRotation(int pRotation, Component comp);

    /**
     * The following was based on a text-rotating applet from David Risner,
     * available at http://www.risner.org/java/rotate_text.html
     * Page unavailable as at April 2019
     *
     * @param pImage     Image to transform
     * @param pComponent Component containing the image, needed to obtain a
     *                   MediaTracker to process the image consistently with
     *                   display
     * @param pRotation  0-3 number of 90-degree rotations needed
     * @return new Image object containing the rotated input image
     */
    public abstract Image createRotatedImage(Image pImage, Component pComponent, int pRotation);

    public abstract int getDegrees();

    public abstract double getScale();

    public abstract void setLoad(int d, double s, Component comp);

    public abstract void transformImage(int w, int h, AffineTransform t, Component comp);

    /**
     * Scale as a percentage.
     *
     * @param scale the scale to set the image
     * @param comp  the containing component
     */
    /* public void scale(int s, Component comp) { //log.info("scale= "+s+",
     * "+getDescription()); if (s<1) { return; } scale(s/100.0, comp); }
     */
    public abstract void scale(double scale, Component comp);

    /**
     * Rotate from anchor point (upper left corner) and shift into place.
     *
     * @param degree the distance to rotate
     * @param comp   containing component
     */
    public abstract void rotate(int degree, Component comp);

    /**
     * Reduce this image size to within the given dimensions, with a limit on
     * the reduction in size.
     *
     * @param width new width
     * @param height new height
     * @param limit limit on the reduction in size
     * @return the scale by which this image was resized
     */
    public abstract double reduceTo(int width, int height, double limit);

    public static final int NOFLIP = 0X00;
    public static final int HORIZONTALFLIP = 0X01;
    public static final int VERTICALFLIP = 0X02;

    public abstract void flip(int flip, Component comp);

}
