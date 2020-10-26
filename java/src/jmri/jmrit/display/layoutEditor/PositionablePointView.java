package jmri.jmrit.display.layoutEditor;

import java.awt.Color;
import java.awt.Graphics2D;

import javax.annotation.*;

import jmri.jmrit.display.layoutEditor.PositionablePoint.PointType;
import static jmri.jmrit.display.layoutEditor.PositionablePoint.PointType.ANCHOR;
import static jmri.jmrit.display.layoutEditor.PositionablePoint.PointType.EDGE_CONNECTOR;
import static jmri.jmrit.display.layoutEditor.PositionablePoint.PointType.END_BUMPER;

/**
 * MVC View component for the PositionablePoint class.
 *
 * @author Bob Jacobsen Copyright (c) 2020
 *
 */
public class PositionablePointView extends LayoutTrackView {

    /**
     * constructor method.
     *
     * @param point the positionable point.
     */
    public PositionablePointView(@Nonnull PositionablePoint point) {
        super(point);
        this.positionablePoint = point;
    }

    final private PositionablePoint positionablePoint;

    /**
     * Draw track decorations.
     * <p>
     * This type of track has none, so this method is empty.
     */
    @Override
    protected void drawDecorations(Graphics2D g2) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void draw1(Graphics2D g2, boolean isMain, boolean isBlock) {
        //nothing to do here... move along...
    }   // draw1

    /**
     * {@inheritDoc}
     */
    @Override
    protected void draw2(Graphics2D g2, boolean isMain, float railDisplacement) {
        //nothing to do here... move along...
    }

    /**
     * {@inheritDoc}
     */
    //@Override
    protected void highlightUnconnected(Graphics2D g2, HitPointType specificType) {
        if ((specificType == HitPointType.NONE) || (specificType == HitPointType.POS_POINT)) {
            if ((positionablePoint.getConnect1() == null)
                    || ((positionablePoint.getType() == ANCHOR)
                    && (positionablePoint.getConnect2() == null))) {
                g2.fill(positionablePoint.trackControlCircleAt(getCoordsCenter()));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void drawEditControls(Graphics2D g2) {
        TrackSegment ts1 = positionablePoint.getConnect1();
        if (ts1 == null) {
            g2.setColor(Color.red);
        } else {
            TrackSegment ts2 = null;
            PointType pointType = positionablePoint.getType();
            if (pointType == ANCHOR) {
                ts2 = positionablePoint.getConnect2();
            } else if (pointType == EDGE_CONNECTOR) {
                PositionablePoint linkedPoint = positionablePoint.getLinkedPoint();
                if (linkedPoint != null) {
                    ts2 = linkedPoint.getConnect1();
                }
            }
            if ((pointType != END_BUMPER) && (ts2 == null)) {
                g2.setColor(Color.yellow);
            } else {
                g2.setColor(Color.green);
            }
        }
        g2.draw(positionablePoint.layoutEditor.layoutEditorControlRectAt(getCoordsCenter()));
    }   // drawEditControls

    /**
     * {@inheritDoc}
     */
    //@Override
    protected void drawTurnoutControls(Graphics2D g2) {
        // PositionablePoints don't have turnout controls...
        // nothing to see here... move along...
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PositionablePointView.class);
}
