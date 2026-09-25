package jmri.jmrit.catalog;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.ImageObserver;
import java.net.URL;
// import java.net.URL;

import javax.annotation.CheckForNull;
import javax.swing.ImageIcon;

import jmri.util.FileUtil;

/**
 * Extend an ImageIcon to remember the name from which it was created and
 * provide rotation and scaling services.
 * <p>
 * We store both a "URL" for finding the file this was made from (so we can load
 * this later), plus a shorter (localized) "name" for display in GUI.
 * <p>
 * These can be persisted by storing their name and rotation.
 * <p>
 * <hr>
 * <p>
 * If the NamedIcon is instanciated, it will internally create an instance of
 * one of the sub classes of the NamedIcon to handle the actual image.
 *
 * @see jmri.jmrit.display.configurexml.PositionableLabelXml
 * @author Bob Jacobsen Copyright 2002, 2008
 * @author Pete Cressman Copyright (c) 2009, 2010
 * @author Daniel Bergqvist Copyright (c) 2026
 *
 * Modified by Joe Comuzzi and Larry Allen to rotate animated GIFs
 */
public class NamedIcon extends ImageIcon {

    public static final int NOFLIP = 0X00;
    public static final int HORIZONTALFLIP = 0X01;
    public static final int VERTICALFLIP = 0X02;

    /**
     * Tell the constructor that it is called by a sub class.
     */
    protected enum Inherited { Yes };

    private final NamedIcon namedIcon;

    /**
     * Find the NamedIconImage corresponding to a file path. Understands the
     * <a href="http://jmri.org/help/en/html/doc/Technical/FileNames.shtml">standard
     * portable filename prefixes</a>.
     *
     * @param path The path to the file, either absolute or portable
     * @return the desired icon with this same name as its path
     */
    public static NamedIconImage getIconByName(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        if (FileUtil.findURL(path) == null) {
            return null;
        }
        return new NamedIconImage(path, path);
    }

    /**
     * This constructor is used by sub classes that wants to use
     * the empty constructor.
     * @param inherited tell the constructor that it's called by a sub class
     */
    protected NamedIcon(Inherited inherited) {
        this.namedIcon = null;
    }

    /**
     * This constructor is used by sub classes that wants to use
     * the constructor with an URL.
     * @param inherited  tell the constructor that it's called by a sub class
     * @param url        the url
     */
    protected NamedIcon(Inherited inherited, URL url) {
        super(url);
        this.namedIcon = null;
    }

    /**
     * This constructor is used by sub classes that wants to use
     * the constructor with an Image.
     * @param inherited  tell the constructor that it's called by a sub class
     * @param im         the image
     */
    protected NamedIcon(Inherited inherited, Image im) {
        super(im);
        this.namedIcon = null;
    }

    /**
     * Create a NamedIcon that is a complete copy of an existing NamedIcon
     *
     * @param pOld Object to copy i.e. copy of the original icon, but NOT a
     *             complete copy of pOld (no transformations done)
     */
    public NamedIcon(NamedIcon pOld) {
        if (!"jmri.jmrit.catalog.NamedIcon".equals(this.getClass().getName())) {
            throw new UnsupportedOperationException("This constructor must not be called by a sub class. Pass the parameter Inherited.Yes to the constructor.");
        }

        if (!(pOld instanceof NamedIcon)) {
            throw new IllegalArgumentException("pOld is of unknown class: " + (pOld != null ? pOld.getClass().getName() : "null"));
        }

        NamedIcon old = (NamedIcon) pOld;
        if (old.namedIcon != null) {
            if (old.namedIcon instanceof NamedIconImage) {
                namedIcon = new NamedIconImage((NamedIconImage) old.namedIcon);
            } else if (old.namedIcon instanceof NamedIconSVG) {
                namedIcon = new NamedIconSVG((NamedIconSVG) old.namedIcon);
            } else if (old.namedIcon instanceof NamedIconTesting) {
                namedIcon = new NamedIconTesting(old.namedIcon.getURL(), old.namedIcon.getName());
            } else {
                throw new IllegalArgumentException(
                        "pOld.namedIcon is of unknown class: "
                        + (old.namedIcon != null ? old.namedIcon.getClass().getName() : "null"));
            }
        } else {
            throw new IllegalArgumentException("pOld is a NamedIcon where namedIcon is null");
        }
    }

    /**
     * Create a NamedIcon that is really a complete copy of an existing
     * NamedIcon
     *
     * @param pOld Object to copy
     * @param comp the container the new icon is embedded in
     */
    public NamedIcon(NamedIcon pOld, Component comp) {
        if (!"jmri.jmrit.catalog.NamedIcon".equals(this.getClass().getName())) {
            throw new UnsupportedOperationException("This constructor must not be called by a sub class. Pass the parameter Inherited.Yes to the constructor.");
        }

        if (!(pOld instanceof NamedIcon)) {
            throw new IllegalArgumentException("pOld is of unknown class: " + (pOld != null ? pOld.getClass().getName() : "null"));
        }

        NamedIcon old = (NamedIcon) pOld;
        if (old.namedIcon != null) {
            if (old.namedIcon instanceof NamedIconImage) {
                namedIcon = new NamedIconImage((NamedIconImage) old.namedIcon, comp);
            } else if (old.namedIcon instanceof NamedIconTesting) {
                namedIcon = new NamedIconTesting(old.namedIcon.getURL(), old.namedIcon.getName());
            } else {
                throw new IllegalArgumentException(
                        "pOld.namedIcon is of unknown class: "
                        + (old.namedIcon != null ? old.namedIcon.getClass().getName() : "null"));
            }
        } else {
            throw new IllegalArgumentException("pOld is a NamedIcon where namedIcon is null");
        }
    }

    /**
     * Create a named icon that includes an image to be loaded from a URL.
     * <p>
     * The default access form is "file:", so a bare pathname to an icon file
     * will also work for the URL argument.
     *
     * @param pUrl  URL of image file to load
     * @param pName Human-readable name for the icon
     */
    public NamedIcon(String pUrl, String pName) {
        if (!"jmri.jmrit.catalog.NamedIcon".equals(this.getClass().getName())) {
            throw new UnsupportedOperationException("This constructor must not be called by a sub class. Pass the parameter Inherited.Yes to the constructor.");
        }

        if ( pUrl.toUpperCase().endsWith(".SVG") ) {
            namedIcon = new NamedIconSVG(pUrl, pName);
        } else if (pUrl.equals("program:resources/clock2.gif")) {
            // REMOVE THIS!!!
            // REMOVE THIS!!!
            // REMOVE THIS!!!
            // REMOVE THIS!!!
            namedIcon = new NamedIconTesting(pUrl, pName);
        } else {
            namedIcon = new NamedIconImage(pUrl, pName);
        }
    }

    /**
     * Create a named icon that includes an image to be loaded from a URL.
     *
     * @param pUrl  String-form URL of image file to load
     * @param pName Human-readable name for the icon
     */
    public NamedIcon(URL pUrl, String pName) {
        if (!"jmri.jmrit.catalog.NamedIcon".equals(this.getClass().getName())) {
            throw new UnsupportedOperationException("This constructor must not be called by a sub class. Pass the parameter Inherited.Yes to the constructor.");
        }

        namedIcon = new NamedIconImage(pUrl, pName);
    }


    /**
     * Create a named icon from an Image. N.B. NamedIcon's create
     * using this constructor can NOT be animated GIFs
     * @param im Image to use
     */
    public NamedIcon(Image im) {
        if (!"jmri.jmrit.catalog.NamedIcon".equals(this.getClass().getName())) {
            throw new UnsupportedOperationException("This constructor must not be called by a sub class. Pass the parameter Inherited.Yes to the constructor.");
        }

        namedIcon = new NamedIconImage(im);
    }

    /**
     * Return the human-readable name of this icon.
     *
     * @return the name or null if not set
     */
    @CheckForNull
    public String getName() {
        return namedIcon.getName();
    }

    /**
     * Set the human-readable name for this icon.
     *
     * @param name the new name, can be null
     */
    public void setName(@CheckForNull String name) {
        namedIcon.setName(name);
    }

    /**
     * Get the URL of this icon.
     *
     * @return the path to this icon in JMRI portable format or null if not set
     */
    @CheckForNull
    public String getURL() {
        return namedIcon.getURL();
    }

    /**
     * Set URL of original icon image. Setting this after initial construction
     * does not change the icon.
     *
     * @param url the URL associated with this icon
     */
    public void setURL(@CheckForNull String url) {
        namedIcon.setURL(url);
    }

    /**
     * Get the number of 90-degree rotations needed to properly display this
     * icon.
     *
     * @return 0 (no rotation), 1 (rotated 90 degrees), 2 (180 degrees), or 3
     *         (270 degrees)
     */
    public int getRotation() {
        return namedIcon.getRotation();
    }

    /**
     * Set the number of 90-degree rotations needed to properly display this
     * icon.
     *
     * @param pRotation 0 (no rotation), 1 (rotated 90 degrees), 2 (180
     *                  degrees), or 3 (270 degrees)
     * @param comp      the component containing this icon
     */
    public void setRotation(int pRotation, Component comp) {
        namedIcon.setRotation(pRotation, comp);
    }

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
    public Image createRotatedImage(Image pImage, Component pComponent, int pRotation) {
        return namedIcon.createRotatedImage(pImage, pComponent, pRotation);
    }

    public int getDegrees() {
        return namedIcon.getDegrees();
    }

    public double getScale() {
        return namedIcon.getScale();
    }

    public void setLoad(int d, double s, Component comp) {
        namedIcon.setLoad(d, s, comp);
    }

    public void transformImage(int w, int h, AffineTransform t, Component comp) {
        namedIcon.transformImage(w, h, t, comp);
    }

    /**
     * Scale as a percentage.
     *
     * @param scale the scale to set the image
     * @param comp  the containing component
     */
    public void scale(double scale, Component comp) {
        namedIcon.scale(scale, comp);
    }

    /**
     * Rotate from anchor point (upper left corner) and shift into place.
     *
     * @param degree the distance to rotate
     * @param comp   containing component
     */
    public void rotate(int degree, Component comp) {
        namedIcon.rotate(degree, comp);
    }

    /**
     * Reduce this image size to within the given dimensions, with a limit on
     * the reduction in size.
     *
     * @param width new width
     * @param height new height
     * @param limit limit on the reduction in size
     * @return the scale by which this image was resized
     */
    public double reduceTo(int width, int height, double limit) {
        return namedIcon.reduceTo(width, height, limit);
    }

    public void flip(int flip, Component comp) {
        namedIcon.flip(flip, comp);
    }

    /** {@inheritDoc} */
    @Override
    public void setImageObserver(ImageObserver observer) {
        if (namedIcon != null) {
            namedIcon.namedIcon.setImageObserver(observer);
        } else {
            super.setImageObserver(observer);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        if (namedIcon != null) {
            namedIcon.namedIcon.paintIcon(c, g, x, y);
        } else {
            super.paintIcon(c, g, x, y);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Image getImage() {
        if (namedIcon != null) {
            return namedIcon.getImage();
        } else {
            return super.getImage();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setImage(Image image) {
        if (namedIcon != null) {
            namedIcon.setImage(image);
        } else {
            super.setImage(image);
        }
    }

    /** {@inheritDoc} */
    @Override
    public int getIconWidth() {
        if (namedIcon != null) {
            return namedIcon.getIconWidth();
        } else {
            return super.getIconWidth();
        }
    }

    /** {@inheritDoc} */
    @Override
    public int getIconHeight() {
        if (namedIcon != null) {
            return namedIcon.getIconHeight();
        } else {
            return super.getIconHeight();
        }
    }

}
