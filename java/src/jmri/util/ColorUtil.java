package jmri.util;

import java.awt.Color;
import javax.annotation.CheckForNull;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A collection of utilities related to colors.
 *
 * @author Dave Duchamp Copyright: (c) 2004-2007
 */
public class ColorUtil {

    /**
     * Handles known colors plus special value for track
     *
     * @param color the color or null
     * @return the name of the color or "black" if a color was provided; "track"
     *         if color is null
     */
    @Nonnull
    public static String colorToString(@Nullable Color color) {
        if (color == null) {
            return Bundle.getMessage("ColorTrack");
        }
        String colorName = colorToName(color);
        if (colorName != null) {
            return colorName;
        }
        log.error("unknown color sent to colorToString");
        return Bundle.getMessage("ColorBlack");
    }

    /**
     * Returns known color name or hex value in form #RRGGBB
     *
     * @param color the color
     * @return the name or hex value of color; returns null if color is null
     */
    @CheckForNull
    public static String colorToColorName(@Nullable Color color) {
        if (color == null) {
            return null;
        }
        String colorName = colorToName(color);
        if (colorName != null) {
            return colorName;
        }
        return colorToHexString(color);
    }

    /**
     * @param string Either a hexidecimal representation of the rgb value of a 
     * color or a color name defined in jmri.NamedBeanBundle.properties. 
     */
    public static Color stringToColor(String string) {
        try {
            return Color.decode(string);
        } catch(NumberFormatException nfe) {
            if(string.equals(Bundle.getMessage("ColorBlack"))) {
                return Color.black;
            } else if(string.equals(Bundle.getMessage("ColorDarkGray"))) {
                return Color.darkGray;
            } else if(string.equals(Bundle.getMessage("ColorGray"))) {
                return Color.gray;
            } else if(string.equals(Bundle.getMessage("ColorLightGray"))) {
                return Color.lightGray;
            } else if(string.equals(Bundle.getMessage("ColorWhite"))) {
                return Color.white;
            } else if(string.equals(Bundle.getMessage("ColorRed"))) {
                return Color.red;
            } else if(string.equals(Bundle.getMessage("ColorPink"))) {
                return Color.pink;
            } else if(string.equals(Bundle.getMessage("ColorOrange"))) {
                return Color.orange;
            } else if(string.equals(Bundle.getMessage("ColorYellow"))) {
                return Color.yellow;
            } else if(string.equals(Bundle.getMessage("ColorGreen"))) {
                return Color.green;
            } else if(string.equals(Bundle.getMessage("ColorBlue"))) {
                return Color.blue;
            } else if(string.equals(Bundle.getMessage("ColorMagenta"))) {
                return Color.magenta;
            } else if(string.equals(Bundle.getMessage("ColorCyan"))) {
                return Color.cyan;
            } else if(string.equals(Bundle.getMessage("ColorTrack"))) {
                return null;
            }
            log.error("unknown color text '" + string + "' sent to stringToColor");
            return Color.black;
        }
    }

    /**
     * Convert a color into hex value of form #RRGGBB.
     *
     * @param color the color or null
     * @return the hex string or null if color is null
     */
    @CheckForNull
    public static String colorToHexString(@Nullable Color color) {
        if (color == null) {
            return null;
        }
        return String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
    }

    /**
     * Internal method to return string name of several known colors, returns
     * null if not in list.
     *
     * @param color the color
     * @return the color name or null if not known
     */
    @CheckForNull
    private static String colorToName(@Nullable Color color) {
        if (color == null) {
            return null;
        }
        if (color.equals(Color.black)) {
            return Bundle.getMessage("ColorBlack");
        } else if (color.equals(Color.darkGray)) {
            return Bundle.getMessage("ColorDarkGray");
        } else if (color.equals(Color.gray)) {
            return Bundle.getMessage("ColorGray");
        } else if (color.equals(Color.lightGray)) {
            return Bundle.getMessage("ColorLightGray");
        } else if (color.equals(Color.white)) {
            return Bundle.getMessage("ColorWhite");
        } else if (color.equals(Color.red)) {
            return Bundle.getMessage("ColorRed");
        } else if (color.equals(Color.pink)) {
            return Bundle.getMessage("ColorPink");
        } else if (color.equals(Color.orange)) {
            return Bundle.getMessage("ColorOrange");
        } else if (color.equals(Color.yellow)) {
            return Bundle.getMessage("ColorYellow");
        } else if (color.equals(Color.green)) {
            return Bundle.getMessage("ColorGreen");
        } else if (color.equals(Color.blue)) {
            return Bundle.getMessage("ColorBlue");
        } else if (color.equals(Color.magenta)) {
            return Bundle.getMessage("ColorMagenta");
        } else if (color.equals(Color.cyan)) {
            return Bundle.getMessage("ColorCyan");
        }
        return null;
    }

    /**
     * calculate the linear interpolation between two colors
     *
     * @param colorA the first color
     * @param colorB the second color
     * @param t  the fraction (between 0 and 1)
     * @return the linear interpolation between a and b for t
     */
    @CheckReturnValue
    public static Color lerp(@Nonnull Color colorA, @Nonnull Color colorB, double t) {
        return new Color(
                MathUtil.lerp(colorA.getRed(), colorB.getRed(), t),
                MathUtil.lerp(colorA.getGreen(), colorB.getGreen(), t),
                MathUtil.lerp(colorA.getBlue(), colorB.getBlue(), t),
                MathUtil.lerp(colorA.getAlpha(), colorB.getAlpha(), t)
        );
    }

    /**
     * set the alpha component of a color
     *
     * @param color the color
     * @param alpha the alpha component (integer 0 - 255)
     * @return the new color with the specified alpha
     */
    @CheckReturnValue
    public static Color setAlpha(@Nonnull Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    /**
     * set the alpha component of a color
     *
     * @param color the color
     * @param alpha the alpha component (double 0.0 - 1.0)
     * @return the new color with the specified alpha
     */
    @CheckReturnValue
    public static Color setAlpha(@Nonnull Color color, double alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), 
                (int) (255.0 * alpha));
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(ColorUtil.class);

}
