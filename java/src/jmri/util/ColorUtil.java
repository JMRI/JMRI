package jmri.util;

import java.awt.Color;
import javax.annotation.CheckForNull;
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
            return "track";
        }
        String colorName = colorToName(color);
        if (colorName != null) {
            return colorName;
        }
        log.error("unknown color sent to colorToString");
        return "black";
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

    public static Color stringToColor(String string) {
        switch (string) {
            case "black":
                return Color.black;
            case "darkGray":
                return Color.darkGray;
            case "gray":
                return Color.gray;
            case "lightGray":
                return Color.lightGray;
            case "white":
                return Color.white;
            case "red":
                return Color.red;
            case "pink":
                return Color.pink;
            case "orange":
                return Color.orange;
            case "yellow":
                return Color.yellow;
            case "green":
                return Color.green;
            case "blue":
                return Color.blue;
            case "magenta":
                return Color.magenta;
            case "cyan":
                return Color.cyan;
            case "track":
                return null;
            default:
                break;
        }
        log.error("unknown color text '" + string + "' sent to stringToColor");
        return Color.black;
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
            return "black";
        } else if (color.equals(Color.darkGray)) {
            return "darkGray";
        } else if (color.equals(Color.gray)) {
            return "gray";
        } else if (color.equals(Color.lightGray)) {
            return "lightGray";
        } else if (color.equals(Color.white)) {
            return "white";
        } else if (color.equals(Color.red)) {
            return "red";
        } else if (color.equals(Color.pink)) {
            return "pink";
        } else if (color.equals(Color.orange)) {
            return "orange";
        } else if (color.equals(Color.yellow)) {
            return "yellow";
        } else if (color.equals(Color.green)) {
            return "green";
        } else if (color.equals(Color.blue)) {
            return "blue";
        } else if (color.equals(Color.magenta)) {
            return "magenta";
        } else if (color.equals(Color.cyan)) {
            return "cyan";
        }
        return null;
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(ColorUtil.class.getName());

}
