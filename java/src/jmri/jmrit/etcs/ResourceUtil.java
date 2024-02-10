package jmri.jmrit.etcs;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import jmri.util.FileUtil;

import org.apiguardian.api.API;

/**
 * Class to locate ERTMS Graphical and Audio resources.
 * @author Steve Young Copyright (C) 2024
 */
@API(status=API.Status.EXPERIMENTAL)
public class ResourceUtil {

    private ResourceUtil(){}

    private static final String RESOURCES_DIRECTORY = "resources" + FileUtil.SEPARATOR + "icons" +
        FileUtil.SEPARATOR + "etcs" + FileUtil.SEPARATOR;

    private static final String SOUNDS_DIR = "resources" + FileUtil.SEPARATOR + "sounds" + FileUtil.SEPARATOR;

    private static final Color BACKGROUND_COLOUR = new Color(3,17,34); // dark blue background

    /**
     * Get the File for an Image.
     * Note this File may not actually exist.
     * @param fileName the Filename to search for.
     * @return File with appropriate Directory.
     */
    @Nonnull
    public static java.io.File getImageFile(String fileName){
        return new java.io.File(getNameAndDirectoryForFile(fileName));
    }

    // Limited Supervsion MO_21 handled separately < ERTMS4
    private static String getNameAndDirectoryForFile(@Nonnull String fileName){
        return FileUtil.getExternalFilename(RESOURCES_DIRECTORY + fileName);
    }

    /**
     * Get the Image Icon for a given FileName.
     * @param fileName the FileName to search for.
     * @return ImageIcon, or null if image not located.
     */
    @CheckForNull
    public static ImageIcon getImageIcon(String fileName){
        String externalFileName =  getNameAndDirectoryForFile(fileName);
        try {
            java.io.File imageFile = new java.io.File(externalFileName);
            if(!imageFile.exists()) {
                log.error("Image file {} not found!", externalFileName);
                return null;
            }
            Image image = ImageIO.read(imageFile);
            return new ImageIcon(image);
        } catch (IOException ex){
            log.error("IO exception: ", ex);
            return null;
        }
    }

    @CheckForNull
    public static BufferedImage getTransparentImage(@Nonnull String str) {
        if (str.isBlank()){
            return null;
        }
        BufferedImage a = readFile(ResourceUtil.getImageFile(str));
        return ResourceUtil.convertColorToTransparent(a);
    }

    @CheckForNull
    public static BufferedImage readFile(java.io.File f){
        BufferedImage a = null;
        try {
            a = ImageIO.read(f);
        } catch (IOException ex) {
            log.error("Exception while reading image {}", f,ex);
        }
        return a;
    }

    /**
     * Convert an image containing the DID Background Colour to a transparent background.
     * @param image the Image to convert.
     * @return converted image, or null.
     */
    @CheckForNull
    public static BufferedImage convertColorToTransparent(@CheckForNull BufferedImage image) {
        if ( image == null ) {
            return null;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Color targetColor = BACKGROUND_COLOUR;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Get the color of the current pixel
                Color pixelColor = new Color(image.getRGB(x, y), true);

                // Check if the pixel color matches the target color
                if (pixelColor.equals(targetColor)) {
                    // Set the alpha (transparency) value to 0
                    pixelColor = new Color(0, 0, 0, 0);
                }

                // Set the modified color in the new image
                newImage.setRGB(x, y, pixelColor.getRGB());
            }
        }
        return newImage;
    }

    /**
     * Play one of the DMI UI Sounds.
     * 1 - S1_toofast.wav - 2 secs
     * 2 - S2_warning.wav - 3 secs
     * 3 - S_info.wav - 1 sec
     * 4 - click.wav - 1 sec
     * @param sound which Sound, plays once.
     */
    public static void playDmiSound(int sound) throws IllegalArgumentException {
        String s;
        switch (sound) {
            case 1:
                s = "S1_toofast.wav";
                break;
            case 2:
                s = "S2_warning.wav";
                break;
            case 3:
                s = "S_info.wav";
                break;
            case 4:
                s = "click.wav";
                break;
            default:
                throw new IllegalArgumentException("No Sound for slot "+ sound);
        }
        String path = FileUtil.getExternalFilename(SOUNDS_DIR + s);
        jmri.jmrit.Sound snd = new jmri.jmrit.Sound(path);
        snd.play();

    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ResourceUtil.class);

}
