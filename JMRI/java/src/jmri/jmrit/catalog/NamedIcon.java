package jmri.jmrit.catalog;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.net.URL;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.swing.ImageIcon;
import jmri.jmrit.display.PositionableLabel;
import jmri.util.FileUtil;
import jmri.util.MathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extend an ImageIcon to remember the name from which it was created and
 * provide rotation {@literal &} scaling services.
 * <p>
 * We store both a "URL" for finding the file this was made from (so we can load
 * this later), plus a shorter "name" for display.
 * <p>
 * These can be persisted by storing their name and rotation
 *
 * @see jmri.jmrit.display.configurexml.PositionableLabelXml
 * @author Bob Jacobsen Copyright 2002, 2008
 * @author Pete Cressman Copyright (c) 2009, 2010
 */
public class NamedIcon extends ImageIcon {

    /**
     * Create a NamedIcon that is a complete copy of an existing NamedIcon
     *
     * @param pOld Object to copy i.e. copy of the original icon, but NOT a
     *             complete copy of pOld (no transformations done)
     */
    public NamedIcon(NamedIcon pOld) {
        this(pOld.mURL, pOld.mName);
    }

    /**
     * Create a NamedIcon that is really a complete copy of an existing
     * NamedIcon
     *
     * @param pOld Object to copy
     * @param comp the container the new icon is embedded in
     */
    public NamedIcon(NamedIcon pOld, Component comp) {
        this(pOld.mURL, pOld.mName);
        setLoad(pOld._degrees, pOld._scale, comp);
        setRotation(pOld.mRotation, comp);
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
        super(FileUtil.findURL(pUrl));
        URL u = FileUtil.findURL(pUrl);
        if (u == null) {
            log.warn("Could not load image from {} (file does not exist)", pUrl);
        }
        mDefaultImage = getImage();
        if (mDefaultImage == null) {
            log.warn("Could not load image from {} (image is null)", pUrl);
        }
        mName = pName;
        mURL = FileUtil.getPortableFilename(pUrl);
        mRotation = 0;
    }

    /**
     * Create a named icon that includes an image to be loaded from a URL.
     *
     * @param pUrl  String-form URL of image file to load
     * @param pName Human-readable name for the icon
     */
    public NamedIcon(URL pUrl, String pName) {
        this(pUrl.toString(), pName);
    }

    public NamedIcon(Image im) {
        super(im);
        mDefaultImage = getImage();
    }

    /**
     * Find the NamedIcon corresponding to a file path. Understands the
     * <a href="http://jmri.org/help/en/html/doc/Technical/FileNames.shtml">standard
     * portable filename prefixes</a>.
     *
     * @param path The path to the file, either absolute or portable
     * @return the desired icon with this same name as its path
     */
    static public NamedIcon getIconByName(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        if (FileUtil.findURL(path) == null) {
            return null;
        }
        return new NamedIcon(path, path);
    }

    /**
     * Return the human-readable name of this icon.
     *
     * @return the name or null if not set
     */
    @CheckForNull
    public String getName() {
        return mName;
    }

    /**
     * Set the human-readable name for this icon.
     *
     * @param name the new name, can be null
     */
    public void setName(@Nullable String name) {
        mName = name;
    }

    /**
     * Get the URL of this icon.
     *
     * @return the path to this icon in JMRI portable format or null if not set
     */
    @CheckForNull
    public String getURL() {
        return mURL;
    }

    /**
     * Set URL of original icon image. Setting this after initial construction
     * does not change the icon.
     *
     * @param url the URL associated with this icon
     */
    public void setURL(@Nullable String url) {
        mURL = url;
    }

    /**
     * Get the number of 90-degree rotations needed to properly display this
     * icon.
     *
     * @return 0 (no rotation), 1 (rotated 90 degrees), 2 (180 degrees), or 3
     *         (270 degrees)
     */
    public int getRotation() {
        return mRotation;
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
        // don't transform a blinking icon, it will no longer blink!
        if (pRotation == 0) {
            return;
        }
        if (pRotation > 3) {
            pRotation = 0;
        }
        if (pRotation < 0) {
            pRotation = 3;
        }
        mRotation = pRotation;
        setImage(createRotatedImage(mDefaultImage, comp, mRotation));
        _degrees = 0;
        if (Math.abs(_scale - 1.0) > .00001) {
            int w = (int) Math.ceil(_scale * getIconWidth());
            int h = (int) Math.ceil(_scale * getIconHeight());
            transformImage(w, h, _transformS, comp);
        }
    }

    private String mName = null;
    private String mURL = null;
    private Image mDefaultImage;
    /*
     public Image getOriginalImage() {
     return mDefaultImage;
     }*/

    /**
     * Valid values are <UL>
     * <LI>0 - no rotation
     * <LI>1 - 90 degrees counter-clockwise
     * <LI>2 - 180 degrees counter-clockwise
     * <LI>3 - 270 degrees counter-clockwise
     * </UL>
     */
    int mRotation;

    /**
     * The following was based on a text-rotating applet from David Risner,
     * available at http://www.risner.org/java/rotate_text.html
     *
     * @param pImage     Image to transform
     * @param pComponent Component containing the image, needed to obtain a
     *                   MediaTracker to process the image consistently with
     *                   display
     * @param pRotation  0-3 number of 90-degree rotations needed
     * @return new Image object containing the rotated input image
     */
    public Image createRotatedImage(Image pImage, Component pComponent, int pRotation) {
        log.debug("createRotatedImage: pRotation= {}, mRotation= {}", pRotation, mRotation);
        if (pRotation == 0) {
            return pImage;
        }

        MediaTracker mt = new MediaTracker(pComponent);
        mt.addImage(pImage, 0);
        try {
            mt.waitForAll();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt(); // retain if needed later
        }

        int w = pImage.getWidth(null);
        int h = pImage.getHeight(null);

        int[] pixels = new int[w * h];
        PixelGrabber pg = new PixelGrabber(pImage, 0, 0, w, h, pixels, 0, w);
        try {
            pg.grabPixels();
        } catch (InterruptedException ie) {
        }
        int[] newPixels = new int[w * h];

        // transform the pixels
        MemoryImageSource imageSource = null;
        switch (pRotation) {
            case 1:  // 90 degrees
                for (int y = 0; y < h; ++y) {
                    for (int x = 0; x < w; ++x) {
                        newPixels[x * h + y] = pixels[y * w + (w - 1 - x)];
                    }
                }
                imageSource = new MemoryImageSource(h, w,
                        ColorModel.getRGBdefault(), newPixels, 0, h);
                break;
            case 2: // 180 degrees
                for (int y = 0; y < h; ++y) {
                    for (int x = 0; x < w; ++x) {
                        newPixels[x * h + y] = pixels[(w - 1 - x) * h + (h - 1 - y)];
                    }
                }
                imageSource = new MemoryImageSource(w, h,
                        ColorModel.getRGBdefault(), newPixels, 0, w);
                break;
            case 3: // 270 degrees
                for (int y = 0; y < h; ++y) {
                    for (int x = 0; x < w; ++x) {
                        newPixels[x * h + y] = pixels[(h - 1 - y) * w + x];
                    }
                }
                imageSource = new MemoryImageSource(h, w,
                        ColorModel.getRGBdefault(), newPixels, 0, h);
                break;
            default:
                log.warn("Unhandled rotation code: {}", pRotation);
                break;
        }

        Image myImage = pComponent.createImage(imageSource);
        mt.addImage(myImage, 1);
        try {
            mt.waitForAll();
        } catch (InterruptedException ie) {
        }
        return myImage;
    }
    private int _degrees = 0;
    private double _scale = 1.0;
    private AffineTransform _transformS = new AffineTransform();    // scaling
    private AffineTransform _transformF = new AffineTransform();    // Fliped or Mirrored

    public int getDegrees() {
        return _degrees;
    }

    public double getScale() {
        return _scale;
    }

    public void setLoad(int d, double s, Component comp) {
        if (d != 0 || s != 1.0) {
            setImage(createRotatedImage(mDefaultImage, comp, 0));
            //mRotation = 3;
        }
        if (d != 0) {
            rotate(d, comp);
        }
        if (s != 1.0) {
            scale(s, comp);
        }
    }

    public void transformImage(int w, int h, AffineTransform t, Component comp) {
        if (w <= 0 || h <= 0) {
            if (log.isDebugEnabled()) {
                log.debug("transformImage bad coords {}",
                        ((jmri.jmrit.display.Positionable) comp).getNameString());
            }
            return;
        }
        BufferedImage bufIm = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bufIm.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
//         g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,   // Turned off due to poor performance, see Issue #3850 and PR #3855 for background
//                 RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.drawImage(getImage(), t, comp);
        setImage(bufIm);
        g2d.dispose();
    }

    /*
     void debugDraw(String op, Component c) {
     jmri.jmrit.display.Positionable pos = (jmri.jmrit.display.Positionable)c;
     java.awt.Rectangle r = c.getBounds();
     log.debug(pos.getNameString()+" "+op);
     System.out.println("\tBounds at ("+r.x+", "+r.y+") width= "+r.width+", height= "+r.height);
     System.out.println("\tLocation at ("+c.getX()+", "+c.getY()+") width= "+
     c.getWidth()+", height= "+c.getHeight());
     }
     */
    /**
     * Scale as a percentage.
     *
     * @param scale the scale to set the image
     * @param comp  the containing component
     */
    /* public void scale(int s, Component comp) { //log.info("scale= "+s+",
     * "+getDescription()); if (s<1) { return; } scale(s/100.0, comp); }
     */
    public void scale(double scale, Component comp) {
        setImage(mDefaultImage);
        _scale = scale;
        if (Math.abs(scale - 1.0) > .00001) {
            _transformS = AffineTransform.getScaleInstance(scale, scale);
        }
        rotate(_degrees, comp);
    }

    /**
     * Rotate from anchor point (upper left corner) and shift into place.
     *
     * @param degree the distance to rotate
     * @param comp   containing component
     */
    public void rotate(int degree, Component comp) {
        setImage(mDefaultImage);
        if (Math.abs(_scale - 1.0) > .00001) {
            int w = (int) Math.ceil(_scale * getIconWidth());
            int h = (int) Math.ceil(_scale * getIconHeight());
            transformImage(w, h, _transformS, comp);
        }
        mRotation = 0;
        // this _always_ returns a value between 0 and 360...
        // (and yes, it does work properly for negative numbers)
        _degrees = MathUtil.wrap(degree, 0, 360);
        if (_degrees == 0) {
            return;
        }
        double rad = Math.toRadians(_degrees);
        double w = getIconWidth();
        double h = getIconHeight();
        int width = (int) Math.ceil(Math.abs(h * Math.sin(rad)) + Math.abs(w * Math.cos(rad)));
        int heigth = (int) Math.ceil(Math.abs(h * Math.cos(rad)) + Math.abs(w * Math.sin(rad)));
        AffineTransform t;
        if (false) {
            // TODO: Test to see if the "else" case is necessary
            t = AffineTransform.getTranslateInstance(
                h * Math.sin(rad) - w * Math.cos(rad),
                -w * Math.sin(rad) - h * Math.cos(rad));
        } else {
            if (_degrees < 90) {
                t = AffineTransform.getTranslateInstance(h * Math.sin(rad), 0.0);
            } else if (_degrees < 180) {
                t = AffineTransform.getTranslateInstance(h * Math.sin(rad) - w * Math.cos(rad), -h * Math.cos(rad));
            } else if (_degrees < 270) {
                t = AffineTransform.getTranslateInstance(-w * Math.cos(rad), -w * Math.sin(rad) - h * Math.cos(rad));
            } else /* if (_degrees < 360) */ {
                t = AffineTransform.getTranslateInstance(0.0, -w * Math.sin(rad));
            }
        }
        AffineTransform r = AffineTransform.getRotateInstance(rad);
        t.concatenate(r);
        transformImage(width, heigth, t, comp);
        if (comp instanceof PositionableLabel) {
            ((PositionableLabel) comp).setDegrees(_degrees);
        }
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
        int w = getIconWidth();
        int h = getIconHeight();
        double scale = 1.0;
        if (w > width) {
            scale = ((double) width) / w;
        }
        if (h > height) {
            scale = Math.min(scale, ((double) height) / h);
        }
        if (scale < 1) { // make a thumbnail
            if (limit > 0.0) {
                scale = Math.max(scale, limit);  // but not too small
            }
//            java.awt.Image im = getImage();
//            im.getScaledInstance((int)Math.ceil(scale * w), (int)Math.ceil(scale * h), java.awt.Image.SCALE_DEFAULT);
//            setImage(im);
            AffineTransform t = AffineTransform.getScaleInstance(scale, scale);
            transformImage((int) Math.ceil(scale * w), (int) Math.ceil(scale * h), t, null);
        }
        return scale;
    }

    public final static int NOFLIP = 0X00;
    public final static int HORIZONTALFLIP = 0X01;
    public final static int VERTICALFLIP = 0X02;

    public void flip(int flip, Component comp) {
        if (flip == NOFLIP) {
            setImage(mDefaultImage);
            _transformF = new AffineTransform();
            _degrees = 0;
            int w = (int) Math.ceil(_scale * getIconWidth());
            int h = (int) Math.ceil(_scale * getIconHeight());
            transformImage(w, h, _transformF, comp);
            return;
        }
        int w = getIconWidth();
        int h = getIconHeight();
        if (flip == HORIZONTALFLIP) {
            _transformF = AffineTransform.getScaleInstance(-1, 1);
            _transformF.translate(-w, 0);
        } else {
            _transformF = AffineTransform.getScaleInstance(1, -1);
            _transformF.translate(0, -h);
        }

        transformImage(w, h, _transformF, null);
    }

    private final static Logger log = LoggerFactory.getLogger(NamedIcon.class);

}
