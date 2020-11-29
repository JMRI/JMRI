/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.jmrit.display.layoutEditor;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.Timer;

import jmri.util.MathUtil;

/**
 *
 * @author geowar
 */
public class LENavigate {

    private LayoutEditor layoutEditor;  // which layout editor we're on
    private LayoutTrack lastTrack;      // the layout track we were on previously
    private LayoutTrack layoutTrack;    // which layout track we're on
    private HitPointType hitPointType;  // the hitpoint we entered from
    private Double fps;                 // frames per second
    private Double speed;               // how far to travel per second
    private Double distance;            // how far to travel
    private Point2D location;           // where we are (in (x1 zoom) screen coordinates)
    private Double direction;           // direction we're headed

    /**
     * Constructor method.
     *
     * @param layoutEditor main layout editor.
     */
    public LENavigate(@Nonnull LENavigate navInfo) {
        this(navInfo.getLayoutEditor(),
                navInfo.getLastTrack(),
                navInfo.getLayoutTrack(),
                navInfo.getHitPointType(),
                navInfo.getFPS(),
                navInfo.getSpeed(),
                navInfo.getDistance());
    }

    public LENavigate(@Nonnull LayoutEditor layoutEditor,
            @Nullable LayoutTrack lastTrack,
            @Nonnull LayoutTrack layoutTrack,
            @Nonnull HitPointType hitPointType,
            Double fps,
            Double speed,
            Double distance
    ) {
        this.layoutEditor = layoutEditor;
        this.lastTrack = null;
        this.layoutTrack = layoutTrack;
        this.hitPointType = hitPointType;
        this.fps = fps;
        this.speed = speed;
        this.distance = distance;
        this.location = MathUtil.zeroPoint2D;
        this.direction = 0.0;
    }

    /*
     * accessors
     */
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

    public Double getFPS() {
        return fps;
    }

    public void setFPS(Double fps) {
        this.fps = fps;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public Point2D getLocation() {
        return location;
    }

    public void setLocation(Point2D location) {
        this.location = location;
    }

    public Double getDirection() {
        return direction;
    }

    public void setDirection(Double direction) {
        this.direction = direction;
    }

    /*
     * public methods
     */
    public void start() {
        setDistance(0.0);
        ActionListener timerActions = (ActionEvent ae) -> {
            setDistance(getDistance() + (getSpeed() / getFPS()));
            log.error("Navigation timer distance: {}", getDistance());
            if (getDistance() > 0.0) {
                LENavigate newNavInfo = navigate();
                newNavInfo.start();
            } else {
                log.error("Fixed Navigation timer stoping");
            }
            ((Timer) ae.getSource()).stop();
        };
        Timer timer = new Timer((int)(1000 / getFPS()), timerActions);
        timer.setRepeats(false);
        log.error("Fixed Navigation timer starting");
        timer.start();
    }

    public LENavigate navigate() {
        return layoutTrack.navigate(this);
    }

    public void draw(Graphics g) {
        if (g instanceof Graphics2D) {
            Graphics2D g2 = (Graphics2D) g;
            // draw something here!
        }
    }
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LENavigate.class);
}
