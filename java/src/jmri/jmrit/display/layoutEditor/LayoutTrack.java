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

    public Point2D center = new Point2D.Double(50.0, 50.0);

    protected boolean hidden = false;

    protected Color defaultTrackColor = Color.black;

    /**
     * constructor method
     */
    public LayoutTrack() {
    }
    
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
