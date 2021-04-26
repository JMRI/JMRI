package jmri.jmrit.display.layoutEditor;

/**
 * Enum denoting the different behaviors of different
 * types of "HitPoints".
 * <p>
 * Extracted from static in constants in LayoutEditor
 *
 * @author Dave Duchamp Copyright: (c) 2004-2007
 * @author George Warner Copyright: (c) 2017-2019
 * @author Bob Jacobsen Copyright: (c) 2020
 */

public enum HitPointType {
                    //  Historical numerical values, used in old files, now ordinals with tests
    NONE,           //     0   
    POS_POINT,      //     1   
    TURNOUT_A,      //     2    throat for RH, LH, and WYE turnouts
    TURNOUT_B,      //     3    continuing route for RH and LH turnouts
    TURNOUT_C,      //     4    diverging route for RH and LH turnouts
    TURNOUT_D,      //     5    4th route for crossovers
    LEVEL_XING_A,   //     6   
    LEVEL_XING_B,   //     7   
    LEVEL_XING_C,   //     8   
    LEVEL_XING_D,   //     9   
    TRACK,          //     10   
    TURNOUT_CENTER,   //     11    non-connection points should be last
    LEVEL_XING_CENTER,  //     12   
    TURNTABLE_CENTER,   //     13   
    LAYOUT_POS_LABEL,   //     14   
    LAYOUT_POS_JCOMP,   //     15   
    MULTI_SENSOR,   //     16   
    MARKER,         //     17   
    TRACK_CIRCLE_CENTRE,   //     18   
    UNUSED_19,      //     19   
    SLIP_CENTER,    //     20   should be @Deprecated (use SLIP_LEFT & SLIP_RIGHT instead)
    SLIP_A,         //     21   
    SLIP_B,         //     22   
    SLIP_C,         //     23   
    SLIP_D,         //     24   
    SLIP_LEFT,      //     25   
    SLIP_RIGHT,     //     26  
    UNUSED_27,      //     27   
    UNUSED_28,                //     28   
    UNUSED_29,                //     29   
    BEZIER_CONTROL_POINT_0,   //     30    offset for TrackSegment Bezier control points , minimum)
    BEZIER_CONTROL_POINT_1,   //     31     \
    BEZIER_CONTROL_POINT_2,   //     32      \
    BEZIER_CONTROL_POINT_3,   //     33       \
    BEZIER_CONTROL_POINT_4,   //     34        } -- DON'T USE THESE; PLACEHOLDERS ONLY
    BEZIER_CONTROL_POINT_5,   //     35       /
    BEZIER_CONTROL_POINT_6,   //     36      /
    BEZIER_CONTROL_POINT_7,   //     37     /
    BEZIER_CONTROL_POINT_8,   //     38    offset for TrackSegment Bezier control points , maximum)
    SHAPE_CENTER,    //     39   
    SHAPE_POINT_0,   //     40    offset for Shape points, minimum)
    SHAPE_POINT_1,   //     41     \
    SHAPE_POINT_2,   //     42      \
    SHAPE_POINT_3,   //     43       \
    SHAPE_POINT_4,   //     44        \ __ DON'T USE THESE; PLACEHOLDERS ONLY
    SHAPE_POINT_5,   //     45        /
    SHAPE_POINT_6,   //     46       /
    SHAPE_POINT_7,   //     47      /
    SHAPE_POINT_8,   //     48     /
    SHAPE_POINT_9,   //     49    offset for Shape points ,   //     maximum)
    TURNTABLE_RAY_0,   //     50    offset for turntable connection points ,   //     minimum)
    TURNTABLE_RAY_1,   //     51    \
    TURNTABLE_RAY_2,   //     52     \
    TURNTABLE_RAY_3,   //     53      \
    TURNTABLE_RAY_4,   //     54       \
    TURNTABLE_RAY_5,   //     55        \
    TURNTABLE_RAY_6,   //     56         \
    TURNTABLE_RAY_7,   //     57          |
    TURNTABLE_RAY_8,   //     58          |
    TURNTABLE_RAY_9,   //     59          |
    TURNTABLE_RAY_10,   //     60         |
    TURNTABLE_RAY_11,   //     61         |
    TURNTABLE_RAY_12,   //     62         |
    TURNTABLE_RAY_13,   //     63         |
    TURNTABLE_RAY_14,   //     64         |
    TURNTABLE_RAY_15,   //     65         |
    TURNTABLE_RAY_16,   //     66         |
    TURNTABLE_RAY_17,   //     67         |
    TURNTABLE_RAY_18,   //     68         |
    TURNTABLE_RAY_19,   //     69         |
    TURNTABLE_RAY_20,   //     70         |
    TURNTABLE_RAY_21,   //     71         |
    TURNTABLE_RAY_22,   //     72         |
    TURNTABLE_RAY_23,   //     73         |
    TURNTABLE_RAY_24,   //     74         |
    TURNTABLE_RAY_25,   //     75         |
    TURNTABLE_RAY_26,   //     76         |
    TURNTABLE_RAY_27,   //     77         |
    TURNTABLE_RAY_28,   //     78         |
    TURNTABLE_RAY_29,   //     79         |
    TURNTABLE_RAY_30,   //     80         |
    TURNTABLE_RAY_31,   //     81         |
    TURNTABLE_RAY_32,   //     82         |
    TURNTABLE_RAY_33,   //     83         |
    TURNTABLE_RAY_34,   //     84         |
    TURNTABLE_RAY_35,   //     85         |
    TURNTABLE_RAY_36,   //     86         |
    TURNTABLE_RAY_37,   //     87         |
    TURNTABLE_RAY_38,   //     88         |
    TURNTABLE_RAY_39,   //     89         |
    TURNTABLE_RAY_40,   //     90         |
    TURNTABLE_RAY_41,   //     91         |
    TURNTABLE_RAY_42,   //     92         |
    TURNTABLE_RAY_43,   //     93         |
    TURNTABLE_RAY_44,   //     94         |
    TURNTABLE_RAY_45,   //     95         | -- DON'T USE THESE; PLACEHOLDERS ONLY
    TURNTABLE_RAY_46,   //     96         |
    TURNTABLE_RAY_47,   //     97         |
    TURNTABLE_RAY_48,   //     98         |
    TURNTABLE_RAY_49,   //     99         |
    TURNTABLE_RAY_50,   //     100         |
    TURNTABLE_RAY_51,   //     101         |
    TURNTABLE_RAY_52,   //     102         |
    TURNTABLE_RAY_53,   //     103         |
    TURNTABLE_RAY_54,   //     104         |
    TURNTABLE_RAY_55,   //     105         |
    TURNTABLE_RAY_56,   //     106         |
    TURNTABLE_RAY_57,   //     107         |
    TURNTABLE_RAY_58,   //     108        /
    TURNTABLE_RAY_59,   //     109       /
    TURNTABLE_RAY_60,   //     110      /
    TURNTABLE_RAY_61,   //     111     /
    TURNTABLE_RAY_62,   //     112    /
    TURNTABLE_RAY_63;   //     113  
        
    /**
     * @param hitType the hit point type
     * @return true if this is for a connection to a LayoutTrack
     */
    protected static boolean isConnectionHitType(HitPointType hitType) {
        switch (hitType) {
            case POS_POINT:
            case TURNOUT_A:
            case TURNOUT_B:
            case TURNOUT_C:
            case TURNOUT_D:
            case LEVEL_XING_A:
            case LEVEL_XING_B:
            case LEVEL_XING_C:
            case LEVEL_XING_D:
            case TRACK:
            case SLIP_A:
            case SLIP_B:
            case SLIP_C:
            case SLIP_D:
                return true; // these are all connection types
            case NONE:
            case TURNOUT_CENTER:
            case LEVEL_XING_CENTER:
            case TURNTABLE_CENTER:
            case LAYOUT_POS_LABEL:
            case LAYOUT_POS_JCOMP:
            case MULTI_SENSOR:
            case MARKER:
            case TRACK_CIRCLE_CENTRE:
            case SLIP_CENTER:
            case SLIP_LEFT:
            case SLIP_RIGHT:
                return false; // these are not
            default:
                break;
        }
        if (isBezierHitType(hitType)) {
            return false; // these are not
        } else if (isTurntableRayHitType(hitType)) {
            return true; // these are all connection types
        }
        return false; // This is unexpected
    }

    /**
     * @param hitType the hit point type
     * @return true if this hit type is for a layout control
     */
    protected static boolean isControlHitType(HitPointType hitType) {
        switch (hitType) {
            case TURNOUT_CENTER:
            case SLIP_CENTER:
            case SLIP_LEFT:
            case SLIP_RIGHT:
                return true; // these are all control types
            case POS_POINT:
            case TURNOUT_A:
            case TURNOUT_B:
            case TURNOUT_C:
            case TURNOUT_D:
            case LEVEL_XING_A:
            case LEVEL_XING_B:
            case LEVEL_XING_C:
            case LEVEL_XING_D:
            case TRACK:
            case SLIP_A:
            case SLIP_B:
            case SLIP_C:
            case SLIP_D:
            case NONE:
            case LEVEL_XING_CENTER:
            case TURNTABLE_CENTER:
            case LAYOUT_POS_LABEL:
            case LAYOUT_POS_JCOMP:
            case MULTI_SENSOR:
            case MARKER:
            case TRACK_CIRCLE_CENTRE:
                return false; // these are not
            default:
                break;
        }
        if (isBezierHitType(hitType)) {
            return false; // these are not control types
        } else if (isTurntableRayHitType(hitType)) {
            return true; // these are all control types
        }
        return false; // This is unexpected
    }

    protected static boolean isTurnoutHitType(HitPointType hitType) {
        return (hitType.compareTo(HitPointType.TURNOUT_A) >= 0) && (hitType.compareTo(HitPointType.TURNOUT_D) <= 0);
    }

    protected static boolean isSlipHitType(HitPointType hitType) {
        return (hitType.compareTo(HitPointType.SLIP_A) >= 0) && (hitType.compareTo(HitPointType.SLIP_RIGHT) <= 0);
    }

    protected static boolean isBezierHitType(HitPointType hitType) {
        return (hitType.compareTo(HitPointType.BEZIER_CONTROL_POINT_0) >= 0) && (hitType.compareTo(HitPointType.BEZIER_CONTROL_POINT_8) <= 0);
    }

    protected static boolean isLevelXingHitType(HitPointType hitType) {
        return (hitType.compareTo(HitPointType.LEVEL_XING_A) >= 0) && (hitType.compareTo(HitPointType.LEVEL_XING_D) <= 0);
    }

    protected static boolean isTurntableRayHitType(HitPointType hitType) {
        return (hitType.compareTo(HitPointType.TURNTABLE_RAY_0) >= 0) && (hitType.compareTo(HitPointType.TURNTABLE_RAY_63) <= 0);
    }

    /**
     * @param hitType the hit point type
     * @return true if this is for a popup menu
     */
    protected static boolean isPopupHitType(HitPointType hitType) {
        switch (hitType) {
            case LEVEL_XING_CENTER:
            case POS_POINT:
            case SLIP_CENTER:
            case SLIP_LEFT:
            case SLIP_RIGHT:
            case TRACK:
            case TRACK_CIRCLE_CENTRE:
            case TURNOUT_CENTER:
            case TURNTABLE_CENTER:
                return true;
            case LAYOUT_POS_JCOMP:
            case LAYOUT_POS_LABEL:
            case LEVEL_XING_A:
            case LEVEL_XING_B:
            case LEVEL_XING_C:
            case LEVEL_XING_D:
            case MARKER:
            case MULTI_SENSOR:
            case NONE:
            case SLIP_A:
            case SLIP_B:
            case SLIP_C:
            case SLIP_D:
            case TURNOUT_A:
            case TURNOUT_B:
            case TURNOUT_C:
            case TURNOUT_D:
                return false; // these are not
            default:
                break;
        }
        if (isBezierHitType(hitType)) {
            return true; // these are all popup hit types
        } else if (isTurntableRayHitType(hitType)) {
            return true; // these are all popup hit types
        }
        return false;
    }

    // *****************************************************************
    //    TURNTABLE_RAY support
    // *****************************************************************
    /**
     * Find the 0-63 index with respect to TURNTABLE_RAY_0
     * of a given enum entry.  Throws {@link IllegalArgumentException} if
     * the given enum value isn't one of the TURNTABLE_RAY_n entries.
     * <p>
     * Ideally, this would be replaced by turntable code that works
     * directly with the enum values as a step toward using objects
     * to implement hit points.
     * @return (Temporary) 0-63 index of the enum element
     */
    protected int turntableTrackIndex() {
        int result = this.ordinal() - HitPointType.TURNTABLE_RAY_0.ordinal();
        if (result < 0) {
            throw new IllegalArgumentException(this.toString() + "is not a valid TURNTABLE_RAY");
        }
        if (result > 63) {
            throw new IllegalArgumentException(this.toString() + "is not a valid TURNTABLE_RAY");
        }
        return result;
    }

    /**
     * Return a specific TURNTABLE_RAY from its 0-63 index.
     * Throws {@link IllegalArgumentException} if
     * the given index value isn't valid for the TURNTABLE_RAY entries.
     * <p>
     * Ideally, this would be replaced by turntable code that works
     * directly with the enum values as a step toward using objects
     * to implement hit points.
     * @param i (Temporary) 0-63 index of the enum element
     * @return Requested enum element
     */
    protected static HitPointType turntableTrackIndexedValue(int i) {
        if (i < 0 || i > 63) {
            throw new IllegalArgumentException(i + "is not a valid TURNTABLE_RAY index");
        }
        return HitPointType.values()[(TURNTABLE_RAY_0.ordinal() + i)];
    }

    /**
     * Return an array of the valid TURNTABLE_RAY enum values.
     * Meant for interations over the set of rays.  Order is
     * from 0 to 63.
     * @return (Temporary) Array containing TURNTABLE_RAY_0 through TURNTABLE_RAY_63
     */
    protected static HitPointType[] turntableValues() {
        return new HitPointType[]{TURNTABLE_RAY_0, TURNTABLE_RAY_1, TURNTABLE_RAY_2, TURNTABLE_RAY_3, TURNTABLE_RAY_4, TURNTABLE_RAY_5, TURNTABLE_RAY_6, TURNTABLE_RAY_7, TURNTABLE_RAY_8, TURNTABLE_RAY_9, TURNTABLE_RAY_10, TURNTABLE_RAY_11, TURNTABLE_RAY_12, TURNTABLE_RAY_13, TURNTABLE_RAY_14, TURNTABLE_RAY_15, TURNTABLE_RAY_16, TURNTABLE_RAY_17, TURNTABLE_RAY_18, TURNTABLE_RAY_19, TURNTABLE_RAY_20, TURNTABLE_RAY_21, TURNTABLE_RAY_22, TURNTABLE_RAY_23, TURNTABLE_RAY_24, TURNTABLE_RAY_25, TURNTABLE_RAY_26, TURNTABLE_RAY_27, TURNTABLE_RAY_28, TURNTABLE_RAY_29, TURNTABLE_RAY_30, TURNTABLE_RAY_31, TURNTABLE_RAY_32, TURNTABLE_RAY_33, TURNTABLE_RAY_34, TURNTABLE_RAY_35, TURNTABLE_RAY_36, TURNTABLE_RAY_37, TURNTABLE_RAY_38, TURNTABLE_RAY_39, TURNTABLE_RAY_40, TURNTABLE_RAY_41, TURNTABLE_RAY_42, TURNTABLE_RAY_43, TURNTABLE_RAY_44, TURNTABLE_RAY_45, TURNTABLE_RAY_46, TURNTABLE_RAY_47, TURNTABLE_RAY_48, TURNTABLE_RAY_49, TURNTABLE_RAY_50, TURNTABLE_RAY_51, TURNTABLE_RAY_52, TURNTABLE_RAY_53, TURNTABLE_RAY_54, TURNTABLE_RAY_55, TURNTABLE_RAY_56, TURNTABLE_RAY_57, TURNTABLE_RAY_58, TURNTABLE_RAY_59, TURNTABLE_RAY_60, TURNTABLE_RAY_61, TURNTABLE_RAY_62, TURNTABLE_RAY_63};
    }

    // *****************************************************************
    //    SHAPE_POINT support
    // *****************************************************************
    /**
     * Find the 0-9 index with respect to SHAPE_POINT_0
     * of a given enum entry.  Throws {@link IllegalArgumentException} if
     * the given enum value isn't one of the SHAPE_POINT_n entries.
     * <p>
     * Ideally, this would be replaced by shape code that works
     * directly with the enum values as a step toward using objects
     * to implement hit points.
     * @return (Temporary) 0-9 index of the enum element
     */
    protected int shapePointIndex() {
        int result = this.ordinal() - HitPointType.SHAPE_POINT_0.ordinal();
        if (result < 0) {
            throw new IllegalArgumentException(this.toString() + "is not a valid SHAPE_POINT");
        }
        if (result > 9) {
            throw new IllegalArgumentException(this.toString() + "is not a valid SHAPE_POINT");
        }
        return result;
    }

    /**
     * Return a specific SHAPE_POINT from its 0-9 index.
     * Throws {@link IllegalArgumentException} if
     * the given index value isn't valid for the SHAPE_POINT entries.
     * <p>
     * Ideally, this would be replaced by shape code that works
     * directly with the enum values as a step toward using objects
     * to implement hit points.
     * @param i (Temporary) 0-9 index of the enum element
     * @return Requested enum element
     */
    protected static HitPointType shapePointIndexedValue(int i) {
        if (i < 0 || i > 9) {
            throw new IllegalArgumentException(i + "is not a valid SHAPE_POINT index");
        }
        return HitPointType.values()[(SHAPE_POINT_0.ordinal() + i)];
    }

    /**
     * Return an array of the valid SHAPE_POINT enum values.
     * Meant for interations over the set of points.  Order is
     * from 0 to 9.
     * @return (Temporary) Array containing SHAPE_POINT_0 through SHAPE_POINT_9
     */
    protected static HitPointType[] shapePointValues() {
        return new HitPointType[]{SHAPE_POINT_0, SHAPE_POINT_1, SHAPE_POINT_2, SHAPE_POINT_3, SHAPE_POINT_4, SHAPE_POINT_5, SHAPE_POINT_6, SHAPE_POINT_7, SHAPE_POINT_8, SHAPE_POINT_9};
    }

    protected static boolean isShapePointOffsetHitPointType(HitPointType t) {
        return (t.compareTo(SHAPE_POINT_0) >= 0) && (t.compareTo(SHAPE_POINT_9) <= 0);
    }
    // limited use, remove?
    public static final int NUM_SHAPE_POINTS = 10;

    // *****************************************************************
    //    BEZIER_CONTROL_POINT support
    // *****************************************************************
    /**
     * Find the 0-8 index with respect to BEZIER_CONTROL_POINT_0
     * of this enum entry. Throws {@link IllegalArgumentException} if
     * the enum value isn't one of the BEZIER_CONTROL_POINT_n entries.
     * <p>
     * Ideally, this would be replaced by bezier code that works
     * directly with the enum values as a step toward using objects
     * to implement hit points.
     * @return (Temporary) 0-8 index of this enum
     */
    protected int bezierPointIndex() {
        int result = this.ordinal() - HitPointType.BEZIER_CONTROL_POINT_0.ordinal();
        if (result < 0) {
            throw new IllegalArgumentException(this.toString() + "is not a valid BEZIER_CONTROL_POINT");
        }
        if (result > 8) {
            throw new IllegalArgumentException(this.toString() + "is not a valid BEZIER_CONTROL_POINT");
        }
        return result;
    }

    /**
     * Return a specific BEZIER_CONTROL_POINT from its 0-8 index.
     * Throws {@link IllegalArgumentException} if
     * the given index value isn't valid for the SHAPE_POINT entries.
     * <p>
     * Ideally, this would be replaced by shape code that works
     * directly with the enum values as a step toward using objects
     * to implement hit points.
     * @param i (Temporary) 0-8 index of the enum element
     * @return Requested enum element
     */
    protected static HitPointType bezierPointIndexedValue(int i) {
        if (i < 0 || i > 8) {
            throw new IllegalArgumentException(i + "is not a valid BEZIER_CONTROL_POINT index");
        }
        return HitPointType.values()[(BEZIER_CONTROL_POINT_0.ordinal() + i)];
    }
    // limited use, remove?
    public static final int NUM_BEZIER_CONTROL_POINTS = 9;
    
}
