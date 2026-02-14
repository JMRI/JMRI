package jmri.util.davidflanagan;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.PixelGrabber;

public class ImageUtils {
    /**
     * Scale an image down to a specified maximum width and height. This does multiple downscaling steps to avoid
     * rendering issues with large images.
     * 
     * @param img The image to scale down.
     * @param maxWidth The maximum width of the scaled image.
     * @param maxHeight The maximum height of the scaled image.
     * @return The scaled image.
     */
    public static Image getScaledInstance(Image img, int maxWidth, int maxHeight) {
        int w = img.getWidth(null);
        int h = img.getHeight(null);

        // 1. Determine the final target dimensions immediately
        double scale = Math.min((double) maxWidth / w, (double) maxHeight / h);
        
        // Optional: Prevent upscaling if the image is already smaller than the target
        scale = Math.min(scale, 1.0); 

        int targetW = (int) Math.round(w * scale);
        int targetH = (int) Math.round(h * scale);

        Image currentImg = img;
        int currentW = w;
        int currentH = h;

        int type = isImageTransparent(img) ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;

        // 2. Multistep downscaling loop
        while (currentW > targetW || currentH > targetH) {
            // Drop by 50% or jump straight to target if we're close
            currentW = Math.max(targetW, currentW / 2);
            currentH = Math.max(targetH, currentH / 2);

            BufferedImage tmp = new BufferedImage(currentW, currentH, type);
            Graphics2D g2 = tmp.createGraphics();
            
            // High-quality settings
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.drawImage(currentImg, 0, 0, currentW, currentH, null);
            g2.dispose();

            currentImg = tmp;
        }

        return currentImg;
    }

    /**
     * Check if an image is transparent.
     * 
     * @param img The image to check.
     * @return true if the image is transparent, false otherwise.
     */
    private static boolean isImageTransparent(Image img) {
        if (img instanceof BufferedImage) {
            return ((BufferedImage) img).getTransparency() != Transparency.OPAQUE;
        }

        // Use a PixelGrabber to peek at the ColorModel
        PixelGrabber pg = new PixelGrabber(img, 0, 0, 1, 1, false);
        try {
            pg.grabPixels(); // We only need to grab the metadata/first pixel
            ColorModel cm = pg.getColorModel();
            return cm != null && cm.hasAlpha();
        } catch (InterruptedException e) {
            return true; // Assume transparent if interrupted to be safe
        }
    }
}