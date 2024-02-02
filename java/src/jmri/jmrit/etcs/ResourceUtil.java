package jmri.jmrit.etcs;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.*;

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

    // handled separately as file starts with MO
    private static final String LIMITED_SUPERVISION_DIR = "Limited Supervision" + FileUtil.SEPARATOR;

    private static final String ATO = "ATO" + FileUtil.SEPARATOR; // Automatic Train Operation
    private static final String DRIVER_REQUEST = "Driver Request" + FileUtil.SEPARATOR;
    private static final String LEVEL = "Level" + FileUtil.SEPARATOR;
    private static final String LEVEL_CROSSING = "Level Crossing" + FileUtil.SEPARATOR;
    private static final String MODE = "Mode" + FileUtil.SEPARATOR;
    private static final String NAVIGATION = "Navigation" + FileUtil.SEPARATOR;
    private static final String PLANNING = "Planning" + FileUtil.SEPARATOR;
    private static final String STATUS = "Status" + FileUtil.SEPARATOR;
    private static final String SETTING = "Setting" + FileUtil.SEPARATOR;
    private static final String SUPERVISED_MANOEUVRE = "Supervised Manoeuvre" + FileUtil.SEPARATOR;
    private static final String TRACK_CONDITIONS = "Track Conditions" + FileUtil.SEPARATOR;

    private static final String RESOURCES_DIRECTORY = FileUtil.PROFILE + "resources" +
        FileUtil.SEPARATOR + "etcs" +FileUtil.SEPARATOR;
    private static final String SYMBOLS_DIR = "symbols" + FileUtil.SEPARATOR;
    private static final String SOUNDS = "sounds" + FileUtil.SEPARATOR;

    private static final Color BACKGROUND_COLOUR = new Color(3,17,34); // dark blue background

    public static final java.io.File LIMITED_SUPERVISION36 = new java.io.File(
        FileUtil.getExternalFilename(RESOURCES_DIRECTORY + SYMBOLS_DIR + LIMITED_SUPERVISION_DIR + "MO_21.bmp"));

    private static boolean warnedNoResourceFound = false;
    private static boolean inTest = false;

    /**
     * Check if a resource is missing and user has already been warned.
     * @return true if missing, else false.
     */
    public static boolean resourceMissing(){
        return warnedNoResourceFound;
    }

    /**
     * Set the Resource missing and warned flag.
     * @param newVal true to set missing and warned, else false.
     */
    public static void setWarnedResourceMissing(boolean newVal){
        warnedNoResourceFound = newVal;
    }

    /**
     * Set running Unit Tests where resource may not be present.
     * @param newVal true if in testing, else false.
     */
    public static void setInTest(boolean newVal) {
        inTest = newVal;
    }

    /**
     * Get if in Unit Testing.
     * @return true if test flag set, else false.
     */
    public static boolean getInTest(){
        return inTest;
    }

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
        String dir = "";
        switch (fileName.substring(0, 3)) {
            case "ATO": // from ertms v4
                dir = ATO;
                break;
            case ("DR_"):
                dir = DRIVER_REQUEST;
                break;
            case ("LE_"):
                dir = LEVEL;
                break;
            case ("LS_"):
                dir = LIMITED_SUPERVISION_DIR;
                break;
            case ("LX_"):
                dir = LEVEL_CROSSING;
                break;
            case ("MO_"):
                dir = MODE;
                break;
            case ("NA_"):
                dir = NAVIGATION;
                break;
            case ("PL_"):
                dir = PLANNING;
                break;
            case ("SE_"):
                dir = SETTING;
                break;
            case "SM0": // from ertms4, no trailing _
                dir = SUPERVISED_MANOEUVRE;
                break;
            case ("ST_"):
                dir = STATUS;
                break;
            case ("TC_"):
                dir = TRACK_CONDITIONS;
                break;
            default:
                break;
        }
        return FileUtil.getExternalFilename(RESOURCES_DIRECTORY + SYMBOLS_DIR + dir + fileName);
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
                if ( !warnedNoResourceFound && !inTest ) {
                    log.error("Image file {} not found!", externalFileName);
                    warnedNoResourceFound = true;
                }
                // create directory as guide for user
                FileUtil.createDirectory(FileUtil.getExternalFilename(RESOURCES_DIRECTORY));
                return null;
            }
            Image image = ImageIO.read(imageFile);
            return new ImageIcon(image);
        } catch (IOException ex){
            if ( !warnedNoResourceFound && !getInTest()) {
                log.error("IO exception: ", ex);
                warnedNoResourceFound = true;
            }
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
            if (!getInTest()){
                log.error("Exception while reading image {}", f,ex);
            }
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
        String f = (RESOURCES_DIRECTORY + SOUNDS + s );
        String path = FileUtil.getExternalFilename(f);
        Path p = Paths.get(path);
        if ( getInTest() ){
            return;
        }
        if ( ! Files.exists(p) ) {
            log.error("Could not play sound file {}", path);
        } else {
            jmri.jmrit.Sound snd = new jmri.jmrit.Sound(path);
            snd.play();
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ResourceUtil.class);

}
