package jmri;

/**
 * Provide values and methods related to layout scale.
 * <P>
 * *
 * @author	Dave Duchamp Copyright (C) 2009
 * @version	$Revision$
 * @since 2.5.4
 */
public class Scale {

    public Scale() {
    }

    // Scale definitions
    public static final int Z = 0x01;	//  1:220
    public static final int N = 0x02;	//  1:160	
    public static final int TT = 0x03;	//  1:120
    public static final int HO = 0x04;  //  1:87
    public static final int S = 0x05;   //  1:64
    public static final int O = 0x06;	//  1:48
    public static final int G = 0x07;   //  1:24
    public static final int UK_N = 0x08;  //  1:148
    public static final int UK_OO = 0x09; //  1:76.2
    public static final int UK_O = 0x0A;  //  1:43.5

    public static final int NUM_SCALES = 10;

    /**
     * scaleFactor is a number less than 1 representing the reduction from full
     * size.
     */
    public static double getScaleFactor(int scale) {
        switch (scale) {
            case Z:
                return (1.0 / 220.0);
            case N:
                return (1.0 / 160.0);
            case TT:
                return (1.0 / 120.0);
            case HO:
                return (1.0 / 87.0);
            case S:
                return (1.0 / 64.0);
            case O:
                return (1.0 / 48.0);
            case G:
                return (1.0 / 24.0);
            case UK_N:
                return (1.0 / 148.0);
            case UK_OO:
                return (1.0 / 76.2);
            case UK_O:
                return (1.0 / 43.5);
            default:
                return 1.0;
        }
    }

    /**
     * Provide a standard human-readable indication of scale that includes both
     * name (letter) and reduction ratio.
     */
    public static String getScaleID(int scale) {
        switch (scale) {
            case Z:
                return ("Z - 1:220");
            case N:
                return ("N - 1:160");
            case TT:
                return ("TT - 1:120");
            case HO:
                return ("HO - 1:87");
            case S:
                return ("S - 1:64");
            case O:
                return ("O - 1:48");
            case G:
                return ("G = 1:24");
            case UK_N:
                return ("UK N - 1:148");
            case UK_OO:
                return ("UK OO - 1:76.2");
            case UK_O:
                return ("UK O - 1:43.5");
            default:
                return ("???");
        }
    }

    /**
     * Provide the short name for the scale.
     */
    public static String getShortScaleID(int scale) {
        switch (scale) {
            case Z:
                return ("Z");
            case N:
                return ("N");
            case TT:
                return ("TT");
            case HO:
                return ("HO");
            case S:
                return ("S");
            case O:
                return ("O");
            case G:
                return ("G");
            case UK_N:
                return ("UK-N");
            case UK_OO:
                return ("UK-OO");
            case UK_O:
                return ("UK-O");
            default:
                return ("???");
        }
    }

}
