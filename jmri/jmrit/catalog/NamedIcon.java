package jmri.jmrit.catalog;

import java.awt.image.*;
import java.awt.*;
import java.net.URL;
import javax.swing.*;

/**
 * Extend an ImageIcon to remember the name from which it was created,
 * and to provide rotation services
 * @author Bob Jacobsen  Copyright 2002
 * @version $Revision: 1.3 $
 */

public class NamedIcon extends ImageIcon {

    public NamedIcon(URL pUrl, String pName) {
        super(pUrl);
        mDefaultImage = getImage();
        mName = pName;
        mRotation = 0;
    }

    public NamedIcon(String pUrl, String pName) {
        super(pUrl);
        mDefaultImage = getImage();
        mName = pName;
        mRotation = 0;
    }

    public String getName() { return mName; }

    public int getRotation() { return mRotation; }
    public void setRotation(int pRotation, Component pComponent) {
        if (pRotation>3) pRotation = 0;
        if (pRotation<0) pRotation = 3;
        mRotation = pRotation;
        setImage(createRotatedImage(mDefaultImage, pComponent, mRotation));
    }

    private String mName=null;
    private Image mDefaultImage;

    /**
     * Valid values are <UL>
     * <LI>0 - no rotation
     * <LI>1 - 90 degrees counter-clockwise
     * <LI>2 - 180 degress counter-clockwise
     * <LI>3 - 270 degrees counter-clockwise
     * </UL>
     */
    int mRotation;

    /**
     * The following was based on a text-rotating applet from
     * David Risner, available at http://www.risner.org/java/rotate_text.html
     * @param pImage
     * @param pComponent
     * @param pRotation
     * @return new Image object containing the rotated input
     */
    public Image createRotatedImage(Image pImage, Component pComponent, int pRotation) {

        if (pRotation == 0) return pImage;

        MediaTracker mt = new MediaTracker(pComponent);
        mt.addImage(pImage, 0);
        try {
            mt.waitForAll();
        } catch (InterruptedException ie) {}

        int w = pImage.getWidth(null);
        int h = pImage.getHeight(null);

        int[] pixels = new int[w*h];
        PixelGrabber pg = new PixelGrabber(pImage, 0, 0, w, h, pixels, 0, w);
        try {
            pg.grabPixels();
        } catch (InterruptedException ie) {}
        int[] newPixels = new int[w*h];

        // transform the pixels
        MemoryImageSource imageSource = null;
        switch (pRotation) {
        case 1:  // 90 degrees
            for (int y=0; y < h; ++y) {
                for (int x=0; x < w; ++x) {
                    newPixels[x*h + y] = pixels[y*w + (w-1-x)];
                }
            }
            imageSource = new MemoryImageSource(h, w,
                ColorModel.getRGBdefault(), newPixels, 0, h);
            break;
        case 2: // 180 degrees
            for (int y=0; y < h; ++y) {
                for (int x=0; x < w; ++x) {
                    newPixels[x*h + y] = pixels[(w-1-x)*h + (h-1-y)];
                }
            }
            imageSource = new MemoryImageSource(w, h,
                ColorModel.getRGBdefault(), newPixels, 0, w);
            break;
        case 3: // 270 degrees
            for (int y=0; y < h; ++y) {
                for (int x=0; x < w; ++x) {
                    newPixels[x*h + y] = pixels[(h-1-y)*w + x];
                }
            }
            imageSource = new MemoryImageSource(h, w,
                ColorModel.getRGBdefault(), newPixels, 0, h);
            break;
        }

        Image myImage = pComponent.createImage(imageSource);
        mt.addImage(myImage, 1);
        try {
            mt.waitForAll();
        } catch (InterruptedException ie) {}
        return myImage;
    }

}