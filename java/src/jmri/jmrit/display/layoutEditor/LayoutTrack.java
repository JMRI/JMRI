package jmri.jmrit.display.layoutEditor;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ResourceBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for all layout track objects
 *
 * @author George Warner Copyright (c) 2017
 */
public class LayoutTrack {

    // Defined text resource
    ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.layoutEditor.LayoutEditorBundle");

    // size of point boxes
    public static final double SIZE = 1.0;
    public static final double SIZE2 = SIZE * 2.;  // must be twice SIZE

    // connection types
    public static final int NONE = 0;
    public static final int POS_POINT = 1;
    public static final int TURNOUT_A = 2;  // throat for RH, LH, and WYE turnouts
    public static final int TURNOUT_B = 3;  // continuing route for RH or LH turnouts
    public static final int TURNOUT_C = 4;  // diverging route for RH or LH turnouts
    public static final int TURNOUT_D = 5;  // double-crossover or single crossover only
    public static final int LEVEL_XING_A = 6;
    public static final int LEVEL_XING_B = 7;
    public static final int LEVEL_XING_C = 8;
    public static final int LEVEL_XING_D = 9;
    public static final int TRACK = 10;
    public static final int TURNOUT_CENTER = 11; // non-connection points should be last
    public static final int LEVEL_XING_CENTER = 12;
    public static final int TURNTABLE_CENTER = 13;
    public static final int LAYOUT_POS_LABEL = 14;
    public static final int LAYOUT_POS_JCOMP = 15;
    public static final int MULTI_SENSOR = 16;
    public static final int MARKER = 17;
    public static final int TRACK_CIRCLE_CENTRE = 18;
    public static final int SLIP_CENTER = 20; //
    public static final int SLIP_A = 21; // offset for slip connection points
    public static final int SLIP_B = 22; // offset for slip connection points
    public static final int SLIP_C = 23; // offset for slip connection points
    public static final int SLIP_D = 24; // offset for slip connection points
    public static final int SLIP_LEFT = 25;
    public static final int SLIP_RIGHT = 26;
    public static final int TURNTABLE_RAY_OFFSET = 50; // offset for turntable connection points

    // dashed line parameters
    //private static int minNumDashes = 3;
    //private static double maxDashLength = 10;

    public Point2D center = new Point2D.Double(50.0, 50.0);

    protected boolean hidden = false;

    protected static Color defaultTrackColor = Color.black;

    /**
     * constructor method
     */
    public LayoutTrack() {
    }

    /**
     * accessor method
     */

    public static void setDefaultTrackColor(Color color) {
        defaultTrackColor = color;
    }

    /**
     * useful math methods (should be class extension for Double & Point2D)
     */

   //return a Double between a & b for t:0 ==> a and t:1 ==> b
    public static Double lerp(Double a, Double b, Double t) {
          return ((1.0 - t) * a) + (t * b);
    }

    //return a Point2D between a & b for t:0 ==> a and t:1 ==> b
    public static Point2D lerp(Point2D p1, Point2D p2, Double interpolant) {
        return new Point2D.Double(lerp(p1.getX(), p2.getX(), interpolant),
                lerp(p1.getY(), p2.getY(), interpolant));
    }

    // return a Point2D at the mid point between p1 & p2
    public static Point2D midpoint(Point2D p1, Point2D p2) {
        return lerp(p1, p2, 0.5);
    }

    // return a Point2D one third of the way from p1 to p2
    public static Point2D third(Point2D p1, Point2D p2) {
        return lerp(p1, p2, 1.0/3.0);
    }

    // return a Point2D one forth of the way from p1 to p2
    public static Point2D fourth(Point2D p1, Point2D p2) {
        return lerp(p1, p2, 1.0/4.0);
    }

    //
    // Wrap a double between two values (for example +/- 180 or 0-360 degrees)
    // Note: THIS IS NOT A PIN OR TRUNCATE; VALUES WRAP AROUND BETWEEN MIN & MAX
    // (And yes, this works correctly with negative numbers)
    //
    public Double wrap(Double inValue, Double inMin, Double inMax) {
        Double valueRange = inMax - inMin;
        return inMin + ((((inValue - inMin) % valueRange) + valueRange) % valueRange);
    }	// wrap

    // wrap an double between +/-180
    public Double wrapPM180(Double inValue) {
        return wrap(inValue, -180.0, +180.0);
    }

    // wrap an double between +/-360
    public Double wrapPM360(Double inValue) {
        return wrap(inValue, -360.0, +360.0);
    }

    // wrap an double between 0-360
    public Double wrap360(Double inValue) {
        return wrap(inValue, 0.0, +360.0);
    }

    // wrap an angle between 0-360
    public double normalizeAngle(double a) {
        return wrap360(a);
    }

    // return the absolute difference (0-180) between two angles
    public double diffAngle(double a, double b) {
        return Math.abs(wrapPM180(a - b));
    }

    //NOTE: not public because "center" is a member variable
    protected Point2D rotatePoint(Point2D p, double sineRot, double cosineRot) {
        double cX = center.getX();
        double cY = center.getY();
        double deltaX = p.getX() - cX;
        double deltaY = p.getY() - cY;
        double x = cX + cosineRot * deltaX - sineRot * deltaY;
        double y = cY + sineRot * deltaX + cosineRot * deltaY;
        return new Point2D.Double(x, y);
    }

    private final static Logger log = LoggerFactory.getLogger(LayoutTrack.class.getName());
}
