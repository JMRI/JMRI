package jmri.jmrit.catalog;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.ImageObserver;
import java.net.URL;

import javax.annotation.CheckForNull;

public class NamedIconSelector extends NamedIcon {

    private final NamedIcon namedIcon;

    /**
     * Create a NamedIcon that is a complete copy of an existing NamedIcon
     *
     * @param pOld Object to copy i.e. copy of the original icon, but NOT a
     *             complete copy of pOld (no transformations done)
     */
    public NamedIconSelector(NamedIcon pOld) {
        if (!(pOld instanceof NamedIconSelector)) {
            throw new IllegalArgumentException("pOld is of unknown class: " + (pOld != null ? pOld.getClass().getName() : "null"));
        }

        NamedIconSelector old = (NamedIconSelector) pOld;
        if (old.namedIcon != null) {
            if (old.namedIcon instanceof NamedIconImage) {
                namedIcon = new NamedIconImage((NamedIconImage) pOld);
            } else if (old.namedIcon instanceof NamedIconTesting) {
                namedIcon = new NamedIconTesting(old.namedIcon.getURL(), old.namedIcon.getName());
            } else {
                throw new IllegalArgumentException(
                        "pOld.namedIcon is of unknown class: "
                        + (old.namedIcon != null ? old.namedIcon.getClass().getName() : "null"));
            }
        } else {
            throw new IllegalArgumentException("pOld is a NamedIconSelector where namedIcon is null");
        }
    }

    /**
     * Create a NamedIcon that is really a complete copy of an existing
     * NamedIcon
     *
     * @param pOld Object to copy
     * @param comp the container the new icon is embedded in
     */
    public NamedIconSelector(NamedIcon pOld, Component comp) {
        if (pOld instanceof NamedIconImage) {
            namedIcon = new NamedIconImage((NamedIconImage) pOld, comp);
        } else {
            throw new IllegalArgumentException("pOld is of unknown class: " + (pOld != null ? pOld.getClass().getName() : "null"));
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
    public NamedIconSelector(String pUrl, String pName) {
        // REMOVE THIS!!!
        // REMOVE THIS!!!
        // REMOVE THIS!!!
        // REMOVE THIS!!!
        if (pUrl.equals("program:resources/clock2.gif")) {
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
    public NamedIconSelector(URL pUrl, String pName) {
        namedIcon = new NamedIconImage(pUrl, pName);
    }


    /**
     * Create a named icon from an Image. N.B. NamedIcon's create
     * using this constructor can NOT be animated GIFs
     * @param im Image to use
     */
    public NamedIconSelector(Image im) {
        namedIcon = new NamedIconImage(im);
    }

    /** {@inheritDoc} */
    @CheckForNull
    @Override
    public String getName() {
        return namedIcon.getName();
    }

    /** {@inheritDoc} */
    @Override
    public void setName(@CheckForNull String name) {
        namedIcon.setName(name);
    }

    /** {@inheritDoc} */
    @CheckForNull
    @Override
    public String getURL() {
        return namedIcon.getURL();
    }

    /** {@inheritDoc} */
    @Override
    public void setURL(@CheckForNull String url) {
        namedIcon.setURL(url);
    }

    /** {@inheritDoc} */
    @Override
    public int getRotation() {
        return namedIcon.getRotation();
    }

    /** {@inheritDoc} */
    @Override
    public void setRotation(int pRotation, Component comp) {
        namedIcon.setRotation(pRotation, comp);
    }

    /** {@inheritDoc} */
    @Override
    public Image createRotatedImage(Image pImage, Component pComponent, int pRotation) {
        return namedIcon.createRotatedImage(pImage, pComponent, pRotation);
    }

    /** {@inheritDoc} */
    @Override
    public int getDegrees() {
        return namedIcon.getDegrees();
    }

    /** {@inheritDoc} */
    @Override
    public double getScale() {
        return namedIcon.getScale();
    }

    /** {@inheritDoc} */
    @Override
    public void setLoad(int d, double s, Component comp) {
        namedIcon.setLoad(d, s, comp);
    }

    /** {@inheritDoc} */
    @Override
    public void transformImage(int w, int h, AffineTransform t, Component comp) {
        namedIcon.transformImage(w, h, t, comp);
    }

    /** {@inheritDoc} */
    @Override
    public void scale(double scale, Component comp) {
        namedIcon.scale(scale, comp);
    }

    /** {@inheritDoc} */
    @Override
    public void rotate(int degree, Component comp) {
        namedIcon.rotate(degree, comp);
    }

    /** {@inheritDoc} */
    @Override
    public double reduceTo(int width, int height, double limit) {
        return namedIcon.reduceTo(width, height, limit);
    }

    /** {@inheritDoc} */
    @Override
    public void flip(int flip, Component comp) {
        namedIcon.flip(flip, comp);
    }

    /** {@inheritDoc} */
    @Override
    public void setImageObserver(ImageObserver observer) {
        namedIcon.setImageObserver(observer);
    }

    /** {@inheritDoc} */
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        namedIcon.paintIcon(c, g, x, y);
    }

    /** {@inheritDoc} */
    @Override
    public Image getImage() {
        return namedIcon.getImage();
    }

    /** {@inheritDoc} */
    @Override
    public void setImage(Image image) {
        namedIcon.setImage(image);
    }

    /** {@inheritDoc} */
    @Override
    public int getIconWidth() {
        return namedIcon.getIconWidth();
    }

    /** {@inheritDoc} */
    @Override
    public int getIconHeight() {
        return namedIcon.getIconHeight();
    }

}
