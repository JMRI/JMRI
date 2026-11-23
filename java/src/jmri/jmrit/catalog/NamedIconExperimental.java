package jmri.jmrit.catalog;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.ImageObserver;

import jmri.util.TimerUtil;

/**
 * NamedIcon that is used for some experimentation.
 *
 * @author Daniel Bergqvist Copyright (c) 2026
 */
public class NamedIconExperimental extends NamedIcon {

    /**
     * Create a NamedIconTest
     *
     * @param url  URL of image file to load
     * @param name Human-readable name for the icon
     */
    NamedIconExperimental(String url, String name) {
        super(Inherited.Yes);
        this.mURL = url;
        this.mName = name;
    }

    /** {@inheritDoc} */
    @Override
    public NamedIcon cloneMe() {
        return new NamedIconImage(mURL, mName);
    }

    /** {@inheritDoc} */
    @Override
    public NamedIcon cloneMe(Component comp) {
        NamedIconExperimental namedIcon = new NamedIconExperimental(mURL, mName);
//        namedIcon.setLoad(_degrees, _scale, comp);
//        namedIcon.setRotation(mRotation, comp);
        return namedIcon;
    }

    /** {@inheritDoc} */
    @Override
    public int getRotation() {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public void setRotation(int pRotation, Component comp) {
    }

    /** {@inheritDoc} */
    @Override
    public Image createRotatedImage(Image pImage, Component pComponent, int pRotation) {
        throw new UnsupportedOperationException("Not implemented yet");
//        return namedIcon.createRotatedImage(pImage, pComponent, pRotation);
    }

    /** {@inheritDoc} */
    @Override
    public int getDegrees() {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public double getScale() {
        return 1.0;
    }

    /** {@inheritDoc} */
    @Override
    public void setLoad(int d, double s, Component comp) {
    }

    /** {@inheritDoc} */
    @Override
    public void transformImage(int w, int h, AffineTransform t, Component comp) {
    }

    /** {@inheritDoc} */
    @Override
    public void scale(double scale, Component comp) {
    }

    /** {@inheritDoc} */
    @Override
    public void rotate(int degree, Component comp) {
    }

    /** {@inheritDoc} */
    @Override
    public double reduceTo(int width, int height, double limit) {
        return 1.0;
    }

    /** {@inheritDoc} */
    @Override
    public void flip(int flip, Component comp) {
    }

    /** {@inheritDoc} */
    @Override
    public void setImageObserver(ImageObserver observer) {
    }

    private int angle = 0;

    /** {@inheritDoc} */
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {

//        ((javax.swing.JLabel)c).setDoubleBuffered(true);
//        ((javax.swing.JLabel)c).setOpaque(false);
//        ((javax.swing.JLabel)c).setOpaque(true);

        int w = getIconWidth();
        int h = getIconHeight();
        int xx = (int) Math.round(Math.cos(Math.toRadians(angle/3.0)) * w / 2);
        int yy = (int) Math.round(Math.sin(Math.toRadians(angle/3.0)) * h / 2);
        g.drawLine(x-xx+w/2, y-yy+h/2, x+xx+w/2, y+yy+h/2);

        synchronized(this) {
            if (animator == null) {
                animator = new Animator(c);
                TimerUtil.schedule(animator, 100, 10);
            }
        }
    }

    private Animator animator;

    private class Animator extends java.util.TimerTask {
        private final Component c;

        private Animator(Component c) {
            this.c = c;
        }

        @Override
        public void run() {
            angle++;
            c.repaint();
        }

    }



    /** {@inheritDoc} */
    @Override
    public Image getImage() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void setImage(Image image) {
    }

    /** {@inheritDoc} */
    @Override
    public int getIconWidth() {
        return 1000;
    }

    /** {@inheritDoc} */
    @Override
    public int getIconHeight() {
        return 1000;
    }

}
