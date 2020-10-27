package jmri.jmrit.display.layoutEditor;

import java.awt.*;
import java.awt.geom.*;

import javax.annotation.*;

import jmri.util.*;

/**
 * MVC View component for the LayoutTurntable class.
 *
 * @author Bob Jacobsen  Copyright (c) 2020
 *
 */
public class LayoutTurntableView extends LayoutTrackView {

    /**
     * Constructor method.
     * @param turntable the layout turntable to create view for.
     */
    public LayoutTurntableView(@Nonnull LayoutTurntable turntable) {
        super(turntable);
        this.turntable = turntable;
    }

    final private LayoutTurntable turntable;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void draw1(Graphics2D g2, boolean isMain, boolean isBlock) {
        float trackWidth = 2.F;
        float halfTrackWidth = trackWidth / 2.f;
        double radius = turntable.getRadius(), diameter = 2.f * radius;

        if (isBlock && isMain) {
            double radius2 = Math.max(radius / 4.f, trackWidth * 2);
            double diameter2 = radius2 * 2.f;
            Stroke stroke = g2.getStroke();
            Color color = g2.getColor();
            // draw turntable circle - default track color, side track width
            g2.setStroke(new BasicStroke(trackWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
            g2.setColor(turntable.layoutEditor.getDefaultTrackColorColor());
            g2.draw(new Ellipse2D.Double(getCoordsCenter().getX() - radius, getCoordsCenter().getY() - radius, diameter, diameter));
            g2.draw(new Ellipse2D.Double(getCoordsCenter().getX() - radius2, getCoordsCenter().getY() - radius2, diameter2, diameter2));
            g2.setStroke(stroke);
            g2.setColor(color);
        }

        // draw ray tracks
        for (int j = 0; j < turntable.getNumberRays(); j++) {
            boolean main = false;
            Color color = null;
            TrackSegment ts = turntable.getRayConnectOrdered(j);
            if (ts != null) {
                main = ts.isMainline();
            }

            if (isBlock) {
                if (ts == null) {
                    g2.setColor(turntable.layoutEditor.getDefaultTrackColorColor());
                } else {
                    LayoutBlock lb = ts.getLayoutBlock();
                    if (lb != null) {
                        color = g2.getColor();
                        setColorForTrackBlock(g2, lb);
                    }
                }
            }

            Point2D pt2 = turntable.getRayCoordsOrdered(j);
            Point2D delta = MathUtil.normalize(MathUtil.subtract(pt2, getCoordsCenter()), radius);
            Point2D pt1 = MathUtil.add(getCoordsCenter(), delta);
            if (main == isMain) {
                g2.draw(new Line2D.Double(pt1, pt2));
            }
            if (isMain && turntable.isTurnoutControlled() && (turntable.getPosition() == j)) {
                if (isBlock) {
                    LayoutBlock lb = turntable.getLayoutBlock();
                    if (lb != null) {
                        color = (color == null) ? g2.getColor() : color;
                        setColorForTrackBlock(g2, lb);
                    } else {
                        g2.setColor(turntable.layoutEditor.getDefaultTrackColorColor());
                    }
                }
                delta = MathUtil.normalize(delta, radius - halfTrackWidth);
                pt1 = MathUtil.subtract(getCoordsCenter(), delta);
                g2.draw(new Line2D.Double(pt1, pt2));
            }
            if (color != null) {
                g2.setColor(color); /// restore previous color
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void draw2(Graphics2D g2, boolean isMain, float railDisplacement) {
        float trackWidth = 2.F;
        float halfTrackWidth = trackWidth / 2.f;

        // draw ray tracks
        for (int j = 0; j < turntable.getNumberRays(); j++) {
            boolean main = false;
//            Color c = null;
            TrackSegment ts = turntable.getRayConnectOrdered(j);
            if (ts != null) {
                main = ts.isMainline();
//                LayoutBlock lb = ts.getLayoutBlock();
//                if (lb != null) {
//                    c = g2.getColor();
//                    setColorForTrackBlock(g2, lb);
//                }
            }
            Point2D pt2 = turntable.getRayCoordsOrdered(j);
            Point2D vDelta = MathUtil.normalize(MathUtil.subtract(pt2, getCoordsCenter()), turntable.getRadius());
            Point2D vDeltaO = MathUtil.normalize(MathUtil.orthogonal(vDelta), railDisplacement);
            Point2D pt1 = MathUtil.add(getCoordsCenter(), vDelta);
            Point2D pt1L = MathUtil.subtract(pt1, vDeltaO);
            Point2D pt1R = MathUtil.add(pt1, vDeltaO);
            Point2D pt2L = MathUtil.subtract(pt2, vDeltaO);
            Point2D pt2R = MathUtil.add(pt2, vDeltaO);
            if (main == isMain) {
                g2.draw(new Line2D.Double(pt1L, pt2L));
                g2.draw(new Line2D.Double(pt1R, pt2R));
            }
            if (isMain && turntable.isTurnoutControlled() && (turntable.getPosition() == j)) {
//                LayoutBlock lb = getLayoutBlock();
//                if (lb != null) {
//                    c = g2.getColor();
//                    setColorForTrackBlock(g2, lb);
//                } else {
//                    g2.setColor(layoutEditor.getDefaultTrackColorColor());
//                }
                vDelta = MathUtil.normalize(vDelta, turntable.getRadius() - halfTrackWidth);
                pt1 = MathUtil.subtract(getCoordsCenter(), vDelta);
                pt1L = MathUtil.subtract(pt1, vDeltaO);
                pt1R = MathUtil.add(pt1, vDeltaO);
                g2.draw(new Line2D.Double(pt1L, pt2L));
                g2.draw(new Line2D.Double(pt1R, pt2R));
            }
//            if (c != null) {
//                g2.setColor(c); /// restore previous color
//            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void highlightUnconnected(Graphics2D g2, HitPointType specificType) {
        for (int j = 0; j < turntable.getNumberRays(); j++) {
            if (  (specificType == HitPointType.NONE)
                    || (specificType == (HitPointType.turntableTrackIndexedValue(j)))
                )
            {
                if (turntable.getRayConnectOrdered(j) == null) {
                    Point2D pt = turntable.getRayCoordsOrdered(j);
                    g2.fill(turntable.trackControlCircleAt(pt));
                }
            }
        }
    }

    /**
     * Draw this turntable's controls.
     *
     * @param g2 the graphics port to draw to
     */
    @Override
    protected void drawTurnoutControls(Graphics2D g2) {
        if (turntable.isTurnoutControlled()) {
            // draw control circles at all but current position ray tracks
            for (int j = 0; j < turntable.getNumberRays(); j++) {
                if (turntable.getPosition() != j) {
                    LayoutTurntable.RayTrack rt = turntable.getRayTrackList().get(j);
                    if (!rt.isDisabled() && !(rt.isDisabledWhenOccupied() && rt.isOccupied())) {
                        Point2D pt = turntable.getRayCoordsOrdered(j);
                        g2.draw(turntable.trackControlCircleAt(pt));
                    }
                }
            }
        }
    }

    /**
     * Draw this turntable's edit controls.
     *
     * @param g2 the graphics port to draw to
     */
    @Override
    protected void drawEditControls(Graphics2D g2) {
        Point2D pt = getCoordsCenter();
        g2.setColor(turntable.layoutEditor.getDefaultTrackColorColor());
        g2.draw(turntable.trackControlCircleAt(pt));

        for (int j = 0; j < turntable.getNumberRays(); j++) {
            pt = turntable.getRayCoordsOrdered(j);

            if (turntable.getRayConnectOrdered(j) == null) {
                g2.setColor(Color.red);
            } else {
                g2.setColor(Color.green);
            }
            g2.draw(turntable.layoutEditor.layoutEditorControlRectAt(pt));
        }
    }

    /**
     * Draw track decorations.
     *
     * This type of track has none, so this method is empty.
     */
    @Override
    protected void drawDecorations(Graphics2D g2) {}

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutTurntableView.class);
}
