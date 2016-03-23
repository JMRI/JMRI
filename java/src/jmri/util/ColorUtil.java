package jmri.util;

import java.awt.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * A collection of utilities relaed to colors.
 * 
 * @author Dave Duchamp Copyright: (c) 2004-2007
 */
public class ColorUtil{

    /**
     * Utility methods for converting between string and color Note: These names
     * are only used internally, so don't need a resource bundle
     */
    public static String colorToString(Color color) {
        if (color == null) {
            return "track";
        } else if (color.equals(Color.black)) {
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
        log.error("unknown color sent to colorToString");
        return "black";
    }

    public static Color stringToColor(String string) {
        if (string.equals("black")) {
            return Color.black;
        } else if (string.equals("darkGray")) {
            return Color.darkGray;
        } else if (string.equals("gray")) {
            return Color.gray;
        } else if (string.equals("lightGray")) {
            return Color.lightGray;
        } else if (string.equals("white")) {
            return Color.white;
        } else if (string.equals("red")) {
            return Color.red;
        } else if (string.equals("pink")) {
            return Color.pink;
        } else if (string.equals("orange")) {
            return Color.orange;
        } else if (string.equals("yellow")) {
            return Color.yellow;
        } else if (string.equals("green")) {
            return Color.green;
        } else if (string.equals("blue")) {
            return Color.blue;
        } else if (string.equals("magenta")) {
            return Color.magenta;
        } else if (string.equals("cyan")) {
            return Color.cyan;
        } else if (string.equals("track")) {
            return null;
        }
        log.error("unknown color text '" + string + "' sent to stringToColor");
        return Color.black;
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(ColorUtil.class.getName());

}
