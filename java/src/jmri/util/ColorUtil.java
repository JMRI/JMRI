package jmri.util;

import java.awt.Color;
import javax.annotation.CheckForNull;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A collection of utilities related to colors.
 *
 * @author Dave Duchamp Copyright: (c) 2004-2007
 */
public class ColorUtil {

    /*
     * Color lists for screen colors.
     */
    public final static String ColorTrack = "track";
    public final static String ColorBlack = "black";
    public final static String ColorDarkGray = "darkGray";
    public final static String ColorGray = "gray";
    public final static String ColorLightGray = "lightGray";
    public final static String ColorWhite = "white";
    public final static String ColorRed = "red";
    public final static String ColorPink = "pink";
    public final static String ColorOrange = "orange";
    public final static String ColorYellow = "yellow";
    public final static String ColorGreen = "green";
    public final static String ColorBlue = "blue";
    public final static String ColorMagenta = "magenta";
    public final static String ColorCyan = "cyan";
    public final static String ColorClear = "clear";
    public final static String ColorBrown = "brown";

    public final static Color clear = setAlpha(Color.BLACK, 0);
    public final static Color CLEAR = clear;
    public final static Color BROWN = new Color(102, 51, 0);

    /**
     * Handles known colors plus special value for track.
     *
     * @param color the color or null
     * @return the name of the color or "black" if a color was provided; "track"
     *         if color is null
     */
    @Nonnull
    public static String colorToString(@CheckForNull Color color) {
        if (color == null) {
            return ColorTrack;
        }
        String colorName = colorToName(color);
        if (colorName != null) {
            return colorName;
        }
        log.error("unknown color sent to colorToString");
        return ColorBlack;
    }

    /**
     * Returns known color name or hex value in form #RRGGBB.
     *
     * @param color the color
     * @return the name or hex value of color; returns null if color is null
     */
    @CheckForNull
    public static String colorToColorName(@CheckForNull Color color) {
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
     * Returns localized color name or hex value in form #RRGGBB.
     *
     * @since 4.13.1
     * @param color the color object
     * @return the localized name or hex value of color; returns null if color is null
     */
    @CheckForNull
    public static String colorToLocalizedName(@CheckForNull Color color) {
        if (color == null) {
            return null;
        }
        String colorName = colorToName(color);
        if (colorName != null) {
            colorName = Character.toUpperCase(colorName.charAt(0)) + colorName.substring(1);
            return Bundle.getMessage(colorName);
        }
        return colorToHexString(color);
    }

    /**
     * @param string Either a hexadecimal representation of the rgb value of a
     *                   color or a color name defined as a constant.
     * @return the color from the string or null if the string equals
     *         {@value #ColorTrack} or equals the localized value for "None"
     * @throws IllegalArgumentException if string cannot be converted into a Color
     */
    public static Color stringToColor(String string) {
        try {
            return Color.decode(string);
        } catch (NumberFormatException nfe) {
            switch (string) {
               case ColorBlack:
                   return Color.black;
               case ColorDarkGray:
                   return Color.darkGray;
               case ColorGray:
                   return Color.gray;
               case ColorLightGray:
                   return Color.lightGray;
               case ColorWhite:
                   return Color.white;
               case ColorRed:
                   return Color.red;
               case ColorPink:
                   return Color.pink;
               case ColorOrange:
                   return Color.orange;
               case ColorYellow:
                   return Color.yellow;
               case ColorGreen:
                   return Color.green;
               case ColorBlue:
                   return Color.blue;
               case ColorMagenta:
                   return Color.magenta;
               case ColorCyan:
                   return Color.cyan;
               case ColorBrown:
                   return BROWN;
               case ColorTrack:
                   return null;
               default:
                   // check translated strings, just in case there is one in a data file.
                    if (string.equals(Bundle.getMessage("Black"))) {
                      return Color.black;
                    }
                    if (string.equals(Bundle.getMessage("DarkGray"))) {
                      return Color.darkGray;
                    }
                    if (string.equals(Bundle.getMessage("Gray"))) {
                      return Color.gray;
                    }
                    if (string.equals(Bundle.getMessage("LightGray"))) {
                      return Color.lightGray;
                    }
                    if (string.equals(Bundle.getMessage("White"))) {
                      return Color.white;
                    }
                    if (string.equals(Bundle.getMessage("Red"))) {
                      return Color.red;
                    }
                    if (string.equals(Bundle.getMessage("Pink"))) {
                      return Color.pink;
                    }
                    if (string.equals(Bundle.getMessage("Yellow"))) {
                      return Color.yellow;
                    }
                    if (string.equals(Bundle.getMessage("Green"))) {
                      return Color.green;
                    }
                    if (string.equals(Bundle.getMessage("Orange"))) {
                      return Color.orange;
                    }
                    if (string.equals(Bundle.getMessage("Blue"))) {
                      return Color.blue;
                    }
                    if (string.equals(Bundle.getMessage("Magenta"))) {
                      return Color.magenta;
                    }
                    if (string.equals(Bundle.getMessage("Cyan"))) {
                      return Color.cyan;
                    }
                    if (string.equals(Bundle.getMessage("ColorClear"))) {
                        return clear;
                    }
                    if (string.equals(Bundle.getMessage("None"))) {
                       return null;
                    } else {
                      log.error("unknown color text '{}' sent to stringToColor", string);
                      throw new IllegalArgumentException("unknown color text '" + string + "'");
                   }
            }
        }
    }

    /**
     * Convert a color into hex value of form #RRGGBB.
     *
     * @param color the color or null
     * @return the hex string or null if color is null
     */
    @CheckForNull
    public static String colorToHexString(@CheckForNull Color color) {
        if (color == null) {
            return null;
        }
        return String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
    }

    /**
     * Internal method to return string name of several known colors.
     *
     * @param color the color
     * @return the color name or null if not known/not in list
     */
    @CheckForNull
    private static String colorToName(@CheckForNull Color color) {
        if (color == null) {
            return null;
        }
        if (color.equals(Color.black)) {
            return ColorBlack;
        } else if (color.equals(Color.darkGray)) {
            return ColorDarkGray;
        } else if (color.equals(Color.gray)) {
            return ColorGray;
        } else if (color.equals(Color.lightGray)) {
            return ColorLightGray;
        } else if (color.equals(Color.white)) {
            return ColorWhite;
        } else if (color.equals(Color.red)) {
            return ColorRed;
        } else if (color.equals(Color.pink)) {
            return ColorPink;
        } else if (color.equals(Color.orange)) {
            return ColorOrange;
        } else if (color.equals(Color.yellow)) {
            return ColorYellow;
        } else if (color.equals(Color.green)) {
            return ColorGreen;
        } else if (color.equals(Color.blue)) {
            return ColorBlue;
        } else if (color.equals(Color.magenta)) {
            return ColorMagenta;
        } else if (color.equals(Color.cyan)) {
            return ColorCyan;
        } else if (color.equals(BROWN)) {
            return ColorBrown;
        }
        return null;
    }

    /**
     * Return the color (Black/White) that most contrasts with the specified
     * color.
     *
     * @param color the source color
     * @return the contrasting color
     */
    public static Color contrast(@Nonnull Color color) {
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();
        int average = (red + green + blue) / 3;

        return (average >= 128) ? Color.BLACK : Color.WHITE;
    }

    /**
     * Calculate the linear interpolation between two colors.
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
     * Set the alpha component of a color.
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
     * Set the alpha component of a color.
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
