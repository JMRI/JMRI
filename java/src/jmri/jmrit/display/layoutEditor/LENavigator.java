/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.jmrit.display.layoutEditor;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.*;
import java.util.*;

import javax.annotation.*;

import jmri.util.MathUtil;

/**
 *
 * @author geowar
 */
public class LENavigator {

    private String name;                // the name of this navigator
    private LayoutEditor layoutEditor;  // which layout editor we're on
    private LayoutTrack lastTrack;      // the layout track we were on previously
    private LayoutTrack layoutTrack;    // which layout track we're on
    private HitPointType hitPointType;  // the hitpoint we entered from
    private double acceleration;        // how fast to accelerate per second
    private double speed;               // how far to travel per second
    private double maxSpeed;            // top speed
    private double distance;            // how far to travel this frame
    private double distanceOnTrack;     // how far we've traveled on this track
    private Point2D location;           // where we are (in (x1 zoom) screen coordinates)
    private double directionRAD;        // directionRAD we're headed (in radians)

    private LENavigator previous;
    private LENavigator next;

    /**
     * Constructor method.
     *
     * @param layoutEditor main layout editor.
     */
    public LENavigator(@Nonnull LENavigator navInfo) {
        this(navInfo.getLayoutEditor(),
                navInfo.getLastTrack(),
                navInfo.getLayoutTrack(),
                navInfo.getHitPointType(),
                navInfo.getSpeed());
    }

    public LENavigator(@Nonnull LayoutEditor layoutEditor,
            @Nullable LayoutTrack lastTrack,
            @Nonnull LayoutTrack layoutTrack,
            @Nonnull HitPointType hitPointType,
            double speed
    ) {
        this.layoutEditor = layoutEditor;
        this.lastTrack = lastTrack;
        this.layoutTrack = layoutTrack;
        this.hitPointType = hitPointType;
        //
        this.maxSpeed = speed;
        this.distance = 0.0;
        this.distanceOnTrack = 0.0;
        this.location = layoutEditor.getCoords(layoutTrack, hitPointType);
        this.directionRAD = 0.0;

        this.previous = null;
        this.next = null;

        layoutEditor.addNavigator(this);
    }

    /*
     * accessors
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LayoutEditor getLayoutEditor() {
        return layoutEditor;
    }

    public void setLayoutEditor(LayoutEditor layoutEditor) {
        this.layoutEditor = layoutEditor;
    }

    public LayoutTrack getLastTrack() {
        return lastTrack;
    }

    public void setLastTrack(LayoutTrack lastTrack) {
        this.lastTrack = lastTrack;
    }

    public LayoutTrack getLayoutTrack() {
        return layoutTrack;
    }

    public void setLayoutTrack(LayoutTrack layoutTrack) {
        this.layoutTrack = layoutTrack;
    }

    public HitPointType getHitPointType() {
        return hitPointType;
    }

    public void setHitPointType(HitPointType hitPointType) {
        this.hitPointType = hitPointType;
    }

    public double getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(double acceleration) {
        this.acceleration = acceleration;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getDistanceOnTrack() {
        return distanceOnTrack;
    }

    public void setDistanceOnTrack(double distance) {
        this.distanceOnTrack = distance;
    }

    public Point2D getLocation() {
        return location;
    }

    public void setLocation(Point2D location) {
        this.location = location;
    }

    public double getDirectionDEG() {
        return Math.toDegrees(directionRAD);
    }

    public void setDirectionDEG(double directionDEG) {
        this.directionRAD = Math.toRadians(directionDEG);
    }

    public double getDirectionRAD() {
        return directionRAD;
    }

    public void setDirectionRAD(double directionRAD) {
        this.directionRAD = directionRAD;
    }

    public LENavigator getPrevious() {
        return previous;
    }

    public void setPrevious(LENavigator previous) {
        this.previous = previous;
    }

    public LENavigator getNext() {
        return next;
    }

    public void setNext(LENavigator next) {
        this.next = next;
    }

    /*
     * public methods
     */
    @Override
    public String toString() {
        return getName();
    }

    /**
     * navigate
     */
    public void navigate() {
        do {
            log.warn(String.format(
                    "#  %s on track: %s at {%.0f, %.0f}, dir: %.0f, distance: %.0f",
                    getName(), layoutTrack.getName(),
                    getLocation().getX(), getLocation().getY(),
                    getDirectionDEG(), getDistance()));
        } while (layoutTrack.navigate(this));
    }

    public void draw(@Nonnull Graphics2D g2) {
        // save original transform
//        AffineTransform saveAT = g2.getTransform();

        // Perform transformation to draw layout tracks
//        g2.translate(location.getX(), location.getY());
//        g2.rotate(directionRAD);
        //g2.translate(-location.getX(), -location.getY());
        List<Point2D> points = new ArrayList<>();
        if (getPrevious() == null) {
            points.add(MathUtil.add(location, new Point2D.Double(+16.0, -8.0)));
            points.add(MathUtil.add(location, new Point2D.Double(+24.0, +0.0)));
            points.add(MathUtil.add(location, new Point2D.Double(+16.0, +8.0)));
            points.add(MathUtil.add(location, new Point2D.Double(-16.0, +8.0)));
            points.add(MathUtil.add(location, new Point2D.Double(-16.0, -8.0)));
        } else {
            points.add(MathUtil.add(location, new Point2D.Double(+16.0, -8.0)));
            points.add(MathUtil.add(location, new Point2D.Double(+16.0, +8.0)));
            points.add(MathUtil.add(location, new Point2D.Double(-16.0, +8.0)));
            points.add(MathUtil.add(location, new Point2D.Double(-16.0, -8.0)));
        }
        GeneralPath path = new GeneralPath();
        for (Point2D p : points) {
            p.setLocation(MathUtil.rotateRAD(p, location, directionRAD));
            if (path.getCurrentPoint() == null) {   // if this is the 1st point
                path.moveTo(p.getX(), p.getY());
            } else {
                path.lineTo(p.getX(), p.getY());
            }
        }
        path.closePath();

        g2.setColor(Color.yellow);
        g2.fill(path);
        g2.setColor(Color.black);
        g2.draw(path);

        // Restore original transform
//        g2.setTransform(saveAT);
    }
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LENavigator.class);
}
