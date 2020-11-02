/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.jmrit.display.modulesEditor;

import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

import jmri.jmrit.display.layoutEditor.*;
import static jmri.jmrit.display.layoutEditor.PositionablePoint.PointType.EDGE_CONNECTOR;
import jmri.util.MathUtil;

/**
 *
 * @author geowar
 */
public class LEModule {

    private final LayoutEditor layoutEditor;

    public LEModule(LayoutEditor layoutEditor) {
        this.layoutEditor = layoutEditor;
    }

    public LayoutEditor getLayoutEditor() {
        return layoutEditor;
    }

    /*
     * accessors for location
     */
    private Point2D location = MathUtil.zeroPoint2D();

    public Point2D getLocation() {
        return location;
    }

    public void setLocation(Point2D location) {
        this.location = location;
    }

    /*
     * accessors for rotation
     */
    private double rotationRAD = Math.toRadians(15.0);

    public double getRotationDEG() {
        return Math.toDegrees(rotationRAD);
    }

    public void setRotationDEG(double rotationDEG) {
        rotationRAD = Math.toRadians(rotationDEG);
    }

    public double getRotationRAD() {
        return rotationRAD;
    }

    public void setRotationRAD(double rotationRAD) {
        this.rotationRAD = rotationRAD;
    }

    /**
     *
     * @return this modules outline
     */
    public List<Point2D> getOutline() {
        List<Point2D> results = new ArrayList<>();

        // add tracks
        for (LayoutTrackView layoutTrackView : layoutEditor.getLayoutTrackViews()) {
            Rectangle2D r = layoutTrackView.getBounds();
            results.add(new Point2D.Double(r.getMinX(), r.getMinY()));
            results.add(new Point2D.Double(r.getMinX(), r.getMaxY()));
            results.add(new Point2D.Double(r.getMaxX(), r.getMinY()));
            results.add(new Point2D.Double(r.getMaxX(), r.getMaxY()));
        }
        // add shapes
        for (LayoutShape layoutShape : layoutEditor.getLayoutShapes()) {
            Rectangle2D r = layoutShape.getBounds();
            results.add(new Point2D.Double(r.getMinX(), r.getMinY()));
            results.add(new Point2D.Double(r.getMinX(), r.getMaxY()));
            results.add(new Point2D.Double(r.getMaxX(), r.getMinY()));
            results.add(new Point2D.Double(r.getMaxX(), r.getMaxY()));
        }
        // convert to convex polygon outline
        results = MathUtil.convexHull(results);

        // re-add the orginal point to close the path
        if (!results.isEmpty()) {
            results.add(results.get(0));
        }

        //move to origin, rotate and move to location
        Point2D center = MathUtil.midPoint(results);
        //Point2D center = MathUtil.midPoint(getLayoutEditor().getPanelBounds());
        for (int i = 0; i < results.size(); i++) {
            Point2D p = results.get(i);
            p = MathUtil.subtract(p, center);
            p = MathUtil.rotateRAD(p, rotationRAD);
            p = MathUtil.add(p, location);
            results.set(i, p);
        }
        return results;
    }   // getOutline

    /**
     * draw this module
     *
     * @param g2 the graphics context
     */
    public void draw(Graphics2D g2) {
        BasicStroke narrow = new BasicStroke(1.0F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        BasicStroke wide = new BasicStroke(2.0F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

        // save original transform
        AffineTransform saveAT = g2.getTransform();

        List<Point2D> points = getOutline();
        Point2D midPoly = MathUtil.midPoint(points);

        g2.setStroke(narrow);
        GeneralPath path = new GeneralPath();
        for (Point2D point : getOutline()) {
            if (path.getCurrentPoint() == null) {
                path.moveTo(point.getX(), point.getY());
            } else {
                path.lineTo(point.getX(), point.getY());
            }
        }
        g2.setColor(Color.blue);
        g2.draw(path);

        // Perform transformation
        Point2D center = MathUtil.midPoint(getLayoutEditor().getPanelBounds());
        //log.warn("center: {}, location: {}", center, location);

        g2.setColor(Color.red);
        g2.fillOval((int) center.getX() - 3, (int) center.getY() - 3, 7, 7);

        g2.setColor(Color.magenta);
        g2.fillOval((int) midPoly.getX() - 3, (int) midPoly.getY() - 3, 7, 7);

//        Point2D center = MathUtil.subtract(midPoly, location);
//        g2.translate(midPoly.getX(), midPoly.getY());
//        g2.translate(-midPoly.getX(), -midPoly.getY());
//        g2.translate(-center.getX(), -center.getY());
//        g2.translate(location.getX(), location.getY());
        g2.translate(-location.getX(), -location.getY());
//        g2.rotate(rotationRAD);

        g2.setColor(Color.darkGray);
        getLayoutEditor().getLayoutTrackViews().forEach((tsv) -> {
            LayoutTrack lt = tsv.getLayoutTrack();
            if (lt instanceof PositionablePoint) {
                PositionablePoint pp = (PositionablePoint) lt;
                if (pp.getType() == EDGE_CONNECTOR) {
//                    Point2D c = tsv.getCoordsCenter();
//                    g2.drawLine((int) c.getX() - 3, (int) c.getY() - 3, (int) c.getX() + 3, (int) c.getY() + 3);
//                    g2.drawLine((int) c.getX() - 3, (int) c.getY() + 3, (int) c.getX() + 3, (int) c.getY() - 3);
                    tsv.drawEditControlsPublic(g2);
                }
            } else if (lt.isMainline()) {
                g2.setStroke(wide);
                tsv.drawOne(g2, true, false);
            } else {
                g2.setStroke(narrow);
                tsv.drawOne(g2, false, false);
            }
        });

//        g2.setColor(Color.red);
//        g2.fillOval((int) center.getX() - 3, (int) center.getY() - 3, 7, 7);
//
//        g2.setColor(Color.magenta);
//        g2.fillOval((int) midPoly.getX() - 3, (int) midPoly.getY() - 3, 7, 7);

        // Restore original transform
        g2.setTransform(saveAT);

        g2.setColor(Color.green);
        g2.drawOval((int) location.getX() - 3, (int) location.getY() - 3, 7, 7);
    }

    // initialize logging
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LEModule.class);
}
