package jmri.jmrit.catalog;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.ImageObserver;

import javax.annotation.CheckForNull;

import jmri.util.TimerUtil;

public class NamedIconTesting extends NamedIcon {

    String url;
    String name;

    /**
     * Create a NamedIconTest
     *
     * @param url  URL of image file to load
     * @param name Human-readable name for the icon
     */
    public NamedIconTesting(String url, String name) {
        this.url = url;
        this.name = name;
    }

    /** {@inheritDoc} */
    @CheckForNull
    @Override
    public String getName() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public void setName(@CheckForNull String name) {
        this.name = name;
    }

    /** {@inheritDoc} */
    @CheckForNull
    @Override
    public String getURL() {
        return url;
    }

    /** {@inheritDoc} */
    @Override
    public void setURL(@CheckForNull String url) {
        this.url = url;
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
        int w = getIconWidth();
        int h = getIconHeight();
        angle++;
        int xx = (int) Math.round(Math.cos(Math.toRadians(angle)) * w / 2);
        int yy = (int) Math.round(Math.sin(Math.toRadians(angle)) * h / 2);
        g.drawLine(x-xx+w/2, y-yy+h/2, x+xx+w/2, y+yy+h/2);

        synchronized(this) {
            if (animator == null) {
                animator = new Animator(c);
                TimerUtil.schedule(animator, 100, 100);
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
        return 100;
    }

    /** {@inheritDoc} */
    @Override
    public int getIconHeight() {
        return 100;
    }

}
