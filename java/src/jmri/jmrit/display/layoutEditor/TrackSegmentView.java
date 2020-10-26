package jmri.jmrit.display.layoutEditor;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;

import javax.annotation.*;

import jmri.util.*;

/**
 * MVC View component for the TrackSegment class.
 *
 * @author Bob Jacobsen Copyright (c) 2020
 *
 */
public class TrackSegmentView extends LayoutTrackView {

    /**
     * constructor method.
     *
     * @param track the track segment to view.
     */
    public TrackSegmentView(@Nonnull TrackSegment track) {
        super(track);
        this.trackSegment = track;
    }

    final private TrackSegment trackSegment;

    public boolean isDashed() {
        return trackSegment.isDashed();
    }
    public boolean isArc() {
        return trackSegment.isArc();
    }
    public boolean isCircle() {
        return trackSegment.isCircle();
    }
    public boolean isBezier() {
        return trackSegment.isBezier();
    }
    public LayoutBlock getLayoutBlock() {
        return trackSegment.getLayoutBlock();
    }

    /**
     * {@inheritDoc}
     */
    //@Override
    protected void draw1(Graphics2D g2, boolean isMain, boolean isBlock) {
//   if (getName().equals("T15")) {
//       log.debug("STOP");
//   }
        if (!isBlock && isDashed() && getLayoutBlock() != null) {
            // Skip the dashed rail layer, the block layer will display the dashed track
            // This removes random rail fragments from between the block dashes
            return;
        }
        if (isMain == isMainline()) {
            if (isBlock) {
                setColorForTrackBlock(g2, getLayoutBlock());
            }
            if (isArc()) {
                trackSegment.calculateTrackSegmentAngle();
                g2.draw(new Arc2D.Double(trackSegment.getCX(), trackSegment.getCY(),
                        trackSegment.getCW(), trackSegment.getCH(),
                        trackSegment.getStartAdj(), trackSegment.getTmpAngle(),
                        Arc2D.OPEN));
                trackSegment.trackRedrawn();
            } else if (trackSegment.isBezier()) {
                Point2D[] points = trackSegment.getBezierPoints();
                MathUtil.drawBezier(g2, points);
            } else {
                Point2D end1 = LayoutEditor.getCoords(trackSegment.getConnect1(), trackSegment.getType1());
                Point2D end2 = LayoutEditor.getCoords(trackSegment.getConnect2(), trackSegment.getType2());

                g2.draw(new Line2D.Double(end1, end2));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    //@Override
    protected void draw2(Graphics2D g2, boolean isMain, float railDisplacement) {
//   if (getName().equals("T5")) {
//       log.debug("STOP");
//   }
        if (isDashed() && getLayoutBlock() != null) {
            // Skip the dashed rail layer, the block layer will display the dashed track
            // This removes random rail fragments from between the block dashes
            return;
        }
        if (isMain == isMainline()) {
            if (isArc()) {
                trackSegment.calculateTrackSegmentAngle();
                Rectangle2D cRectangle2D = new Rectangle2D.Double(
                        trackSegment.getCX(), trackSegment.getCY(),
                        trackSegment.getCW(), trackSegment.getCH());
                Rectangle2D tRectangle2D = MathUtil.inset(cRectangle2D, -railDisplacement);
                double startAdj = trackSegment.getStartAdj(), tmpAngle = trackSegment.getTmpAngle();
                g2.draw(new Arc2D.Double(tRectangle2D.getX(), tRectangle2D.getY(),
                        tRectangle2D.getWidth(), tRectangle2D.getHeight(),
                        startAdj, tmpAngle, Arc2D.OPEN));
                tRectangle2D = MathUtil.inset(cRectangle2D, +railDisplacement);
                g2.draw(new Arc2D.Double(tRectangle2D.getX(), tRectangle2D.getY(),
                        tRectangle2D.getWidth(), tRectangle2D.getHeight(),
                        startAdj, tmpAngle, Arc2D.OPEN));
                trackSegment.trackRedrawn();
            } else if (isBezier()) {
                Point2D[] points = trackSegment.getBezierPoints();
                MathUtil.drawBezier(g2, points, -railDisplacement);
                MathUtil.drawBezier(g2, points, +railDisplacement);
            } else {
                Point2D end1 = LayoutEditor.getCoords(trackSegment.getConnect1(), trackSegment.getType1());
                Point2D end2 = LayoutEditor.getCoords(trackSegment.getConnect2(), trackSegment.getType2());

                Point2D delta = MathUtil.subtract(end2, end1);
                Point2D vector = MathUtil.normalize(delta, railDisplacement);
                vector = MathUtil.orthogonal(vector);

                Point2D ep1L = MathUtil.add(end1, vector);
                Point2D ep2L = MathUtil.add(end2, vector);
                g2.draw(new Line2D.Double(ep1L, ep2L));

                Point2D ep1R = MathUtil.subtract(end1, vector);
                Point2D ep2R = MathUtil.subtract(end2, vector);
                g2.draw(new Line2D.Double(ep1R, ep2R));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void highlightUnconnected(Graphics2D g2, HitPointType selectedType) {
        // TrackSegments are always connected
        // nothing to see here... move along...
    }

    @Override
    protected void drawEditControls(Graphics2D g2) {
        g2.setColor(Color.black);
        if (trackSegment.isShowConstructionLines()) {
            Point2D ep1 = LayoutEditor.getCoords(trackSegment.getConnect1(), trackSegment.getType1());
            Point2D ep2 = LayoutEditor.getCoords(trackSegment.getConnect2(), trackSegment.getType2());
            if (isCircle()) {
                // draw radiuses
                Point2D circleCenterPoint = trackSegment.getCoordsCenterCircle();
                g2.draw(new Line2D.Double(circleCenterPoint, ep1));
                g2.draw(new Line2D.Double(circleCenterPoint, ep2));
                // Draw a circle and square at the circles centre, that
                // allows the user to change the angle by dragging the mouse.
                g2.draw(trackSegment.trackEditControlCircleAt(circleCenterPoint));
                g2.draw(trackSegment.layoutEditor.layoutEditorControlRectAt(circleCenterPoint));
            } else if (isBezier()) {
                // draw construction lines and control circles
                Point2D lastPt = ep1;
                for (Point2D bcp : trackSegment.getBezierControlPoints()) {
                    g2.draw(new Line2D.Double(lastPt, bcp));
                    lastPt = bcp;
                    g2.draw(trackSegment.layoutEditor.layoutEditorControlRectAt(bcp));
                }
                g2.draw(new Line2D.Double(lastPt, ep2));
            }
        }
        g2.draw(trackSegment.trackEditControlCircleAt(trackSegment.getCentreSeg()));
    }   // drawEditControls

    @Override
    protected void drawTurnoutControls(Graphics2D g2) {
        // TrackSegments don't have turnout controls...
        // nothing to see here... move along...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void drawDecorations(Graphics2D g2) {
//   if (getName().equals("T5")) {
//       log.debug("STOP");
//   }

// get end points and calculate start/stop angles (in radians)
        Point2D ep1 = LayoutEditor.getCoords(trackSegment.getConnect1(), trackSegment.getType1());
        Point2D ep2 = LayoutEditor.getCoords(trackSegment.getConnect2(), trackSegment.getType2());
        Point2D p1, p2, p3, p4, p5, p6, p7;
        Point2D p1P = ep1, p2P = ep2, p3P, p4P, p5P, p6P, p7P;
        double startAngleRAD, stopAngleRAD;
        if (isArc()) {
            trackSegment.calculateTrackSegmentAngle();
            double startAngleDEG = trackSegment.getStartAdj(), extentAngleDEG = trackSegment.getTmpAngle();
            startAngleRAD = (Math.PI / 2.D) - Math.toRadians(startAngleDEG);
            stopAngleRAD = (Math.PI / 2.D) - Math.toRadians(startAngleDEG + extentAngleDEG);
            if (trackSegment.isFlip()) {
                startAngleRAD += Math.PI;
                stopAngleRAD += Math.PI;
            } else {
                double temp = startAngleRAD;
                startAngleRAD = stopAngleRAD;
                stopAngleRAD = temp;
            }
        } else if (isBezier()) {
            ArrayList<Point2D> bezierControlPoints = trackSegment.getBezierControlPoints();
            Point2D cp0 = bezierControlPoints.get(0);
            Point2D cpN = bezierControlPoints.get(bezierControlPoints.size() - 1);
            startAngleRAD = (Math.PI / 2.D) - MathUtil.computeAngleRAD(cp0, ep1);
            stopAngleRAD = (Math.PI / 2.D) - MathUtil.computeAngleRAD(ep2, cpN);
        } else {
            startAngleRAD = (Math.PI / 2.D) - MathUtil.computeAngleRAD(ep2, ep1);
            stopAngleRAD = startAngleRAD;
        }

//
// arrow decorations
//
        if (trackSegment.getArrowStyle() > 0) {
            g2.setStroke(new BasicStroke(trackSegment.getArrowLineWidth(),
                    BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.F));
            g2.setColor(trackSegment.getArrowColor());

            // draw the start arrows
            int offset = 1;
            if (trackSegment.isArrowEndStart()) {
                if (trackSegment.isArrowDirIn()) {
                    offset = drawArrow(g2, ep1, Math.PI + startAngleRAD, false, offset);
                }
                if (trackSegment.isArrowDirOut()) {
                    offset = drawArrow(g2, ep1, Math.PI + startAngleRAD, true, offset);
                }
            }

            // draw the stop arrows
            offset = 1;
            if (trackSegment.isArrowEndStop()) {
                if (trackSegment.isArrowDirIn()) {
                    offset = drawArrow(g2, ep2, stopAngleRAD, false, offset);
                }
                if (trackSegment.isArrowDirOut()) {
                    offset = drawArrow(g2, ep2, stopAngleRAD, true, offset);
                }
            }
        }   // arrow decoration

//
// bridge decorations
//
        if (trackSegment.isBridgeSideLeft() || trackSegment.isBridgeSideRight()) {
            float halfWidth = trackSegment.getBridgeDeckWidth() / 2.F;

            g2.setStroke(new BasicStroke(trackSegment.getBridgeLineWidth(),
                    BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.F));
            g2.setColor(trackSegment.getBridgeColor());

            if (isArc()) {
                trackSegment.calculateTrackSegmentAngle();
                Rectangle2D cRectangle2D = new Rectangle2D.Double(
                        trackSegment.getCX(), trackSegment.getCY(),
                        trackSegment.getCW(), trackSegment.getCH());
                double startAdj = trackSegment.getStartAdj(), tmpAngle = trackSegment.getTmpAngle();
                if (trackSegment.isBridgeSideLeft()) {
                    Rectangle2D tRectangle2D = MathUtil.inset(cRectangle2D, -halfWidth);
                    g2.draw(new Arc2D.Double(tRectangle2D.getX(), tRectangle2D.getY(),
                            tRectangle2D.getWidth(), tRectangle2D.getHeight(),
                            startAdj, tmpAngle, Arc2D.OPEN));
                }
                if (trackSegment.isBridgeSideRight()) {
                    Rectangle2D tRectangle2D = MathUtil.inset(cRectangle2D, +halfWidth);
                    g2.draw(new Arc2D.Double(tRectangle2D.getX(), tRectangle2D.getY(),
                            tRectangle2D.getWidth(), tRectangle2D.getHeight(),
                            startAdj, tmpAngle, Arc2D.OPEN));
                }
            } else if (isBezier()) {
                Point2D[] points = trackSegment.getBezierPoints();
                if (trackSegment.isBridgeSideLeft()) {
                    MathUtil.drawBezier(g2, points, -halfWidth);
                }
                if (trackSegment.isBridgeSideRight()) {
                    MathUtil.drawBezier(g2, points, +halfWidth);
                }
            } else {
                Point2D delta = MathUtil.subtract(ep2, ep1);
                Point2D vector = MathUtil.normalize(delta, halfWidth);
                vector = MathUtil.orthogonal(vector);

                if (trackSegment.isBridgeSideRight()) {
                    Point2D ep1R = MathUtil.add(ep1, vector);
                    Point2D ep2R = MathUtil.add(ep2, vector);
                    g2.draw(new Line2D.Double(ep1R, ep2R));
                }

                if (trackSegment.isBridgeSideLeft()) {
                    Point2D ep1L = MathUtil.subtract(ep1, vector);
                    Point2D ep2L = MathUtil.subtract(ep2, vector);
                    g2.draw(new Line2D.Double(ep1L, ep2L));
                }
            }   // if isArc() {} else if isBezier() {} else...

            if (trackSegment.isFlip()) {
                boolean temp = trackSegment.isBridgeSideRight();
                trackSegment.setBridgeSideRight(trackSegment.isBridgeSideLeft());
                trackSegment.setBridgeSideLeft(temp);
            }
            int bridgeApproachWidth = trackSegment.getBridgeApproachWidth();

            if (trackSegment.isBridgeHasEntry()) {
                if (trackSegment.isBridgeSideRight()) {
                    p1 = new Point2D.Double(-bridgeApproachWidth, +bridgeApproachWidth + halfWidth);
                    p2 = new Point2D.Double(0.0, +halfWidth);
                    p1P = MathUtil.add(MathUtil.rotateRAD(p1, startAngleRAD), ep1);
                    p2P = MathUtil.add(MathUtil.rotateRAD(p2, startAngleRAD), ep1);
                    g2.draw(new Line2D.Double(p1P, p2P));
                }
                if (trackSegment.isBridgeSideLeft()) {
                    p1 = new Point2D.Double(-bridgeApproachWidth, -bridgeApproachWidth - halfWidth);
                    p2 = new Point2D.Double(0.0, -halfWidth);
                    p1P = MathUtil.add(MathUtil.rotateRAD(p1, startAngleRAD), ep1);
                    p2P = MathUtil.add(MathUtil.rotateRAD(p2, startAngleRAD), ep1);
                    g2.draw(new Line2D.Double(p1P, p2P));
                }
            }
            if (trackSegment.isBridgeHasExit()) {
                if (trackSegment.isBridgeSideRight()) {
                    p1 = new Point2D.Double(+bridgeApproachWidth, +bridgeApproachWidth + halfWidth);
                    p2 = new Point2D.Double(0.0, +halfWidth);
                    p1P = MathUtil.add(MathUtil.rotateRAD(p1, stopAngleRAD), ep2);
                    p2P = MathUtil.add(MathUtil.rotateRAD(p2, stopAngleRAD), ep2);
                    g2.draw(new Line2D.Double(p1P, p2P));
                }
                if (trackSegment.isBridgeSideLeft()) {
                    p1 = new Point2D.Double(+bridgeApproachWidth, -bridgeApproachWidth - halfWidth);
                    p2 = new Point2D.Double(0.0, -halfWidth);
                    p1P = MathUtil.add(MathUtil.rotateRAD(p1, stopAngleRAD), ep2);
                    p2P = MathUtil.add(MathUtil.rotateRAD(p2, stopAngleRAD), ep2);
                    g2.draw(new Line2D.Double(p1P, p2P));
                }
            }

            // if necessary flip these back
            if (trackSegment.isFlip()) {
                boolean temp = trackSegment.isBridgeSideRight();
                trackSegment.setBridgeSideRight(trackSegment.isBridgeSideLeft());
                trackSegment.setBridgeSideLeft(temp);
            }
        }

        //
        // end bumper decorations
        //
        if (trackSegment.isBumperEndStart() || trackSegment.isBumperEndStop()) {
            g2.setStroke(new BasicStroke(trackSegment.getBumperLineWidth(),
                    BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.F));
            g2.setColor(trackSegment.getBumperColor());

            float halfLength = trackSegment.getBumperLength() / 2.F;

            if (trackSegment.isBumperFlipped()) {
                double temp = startAngleRAD;
                startAngleRAD = stopAngleRAD;
                stopAngleRAD = temp;
            }

            // common points
            p1 = new Point2D.Double(0.F, -halfLength);
            p2 = new Point2D.Double(0.F, +halfLength);

            if (trackSegment.isBumperEndStart()) {
                p1P = MathUtil.add(MathUtil.rotateRAD(p1, startAngleRAD), ep1);
                p2P = MathUtil.add(MathUtil.rotateRAD(p2, startAngleRAD), ep1);
                // draw cross tie
                g2.draw(new Line2D.Double(p1P, p2P));
            }
            if (trackSegment.isBumperEndStop()) {
                p1P = MathUtil.add(MathUtil.rotateRAD(p1, stopAngleRAD), ep2);
                p2P = MathUtil.add(MathUtil.rotateRAD(p2, stopAngleRAD), ep2);
                // draw cross tie
                g2.draw(new Line2D.Double(p1P, p2P));
            }
        }   // if (bumperEndStart || bumperEndStop)

//
// tunnel decorations
//
        if (trackSegment.isTunnelSideRight() || trackSegment.isTunnelSideLeft()) {
            float halfWidth = trackSegment.getTunnelFloorWidth() / 2.F;
            g2.setStroke(new BasicStroke(trackSegment.getTunnelLineWidth(),
                    BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10.F,
                    new float[]{6.F, 4.F}, 0));
            g2.setColor(trackSegment.getTunnelColor());

            if (isArc()) {
                trackSegment.calculateTrackSegmentAngle();
                Rectangle2D cRectangle2D = new Rectangle2D.Double(
                        trackSegment.getCX(), trackSegment.getCY(),
                        trackSegment.getCW(), trackSegment.getCH());
                double startAngleDEG = trackSegment.getStartAdj(),
                        extentAngleDEG = trackSegment.getTmpAngle();
                if (trackSegment.isTunnelSideRight()) {
                    Rectangle2D tRectangle2D = MathUtil.inset(cRectangle2D, +halfWidth);
                    g2.draw(new Arc2D.Double(tRectangle2D.getX(), tRectangle2D.getY(),
                            tRectangle2D.getWidth(), tRectangle2D.getHeight(),
                            startAngleDEG, extentAngleDEG, Arc2D.OPEN));
                }
                if (trackSegment.isTunnelSideLeft()) {
                    Rectangle2D tRectangle2D = MathUtil.inset(cRectangle2D, -halfWidth);
                    g2.draw(new Arc2D.Double(tRectangle2D.getX(), tRectangle2D.getY(),
                            tRectangle2D.getWidth(), tRectangle2D.getHeight(),
                            startAngleDEG, extentAngleDEG, Arc2D.OPEN));
                }
                trackSegment.trackRedrawn();
            } else if (isBezier()) {
                Point2D[] points = trackSegment.getBezierPoints();
                if (trackSegment.isTunnelSideRight()) {
                    MathUtil.drawBezier(g2, points, +halfWidth);
                }
                if (trackSegment.isTunnelSideLeft()) {
                    MathUtil.drawBezier(g2, points, -halfWidth);
                }
            } else {
                Point2D delta = MathUtil.subtract(ep2, ep1);
                Point2D vector = MathUtil.normalize(delta, halfWidth);
                vector = MathUtil.orthogonal(vector);

                if (trackSegment.isTunnelSideRight()) {
                    Point2D ep1L = MathUtil.add(ep1, vector);
                    Point2D ep2L = MathUtil.add(ep2, vector);
                    g2.draw(new Line2D.Double(ep1L, ep2L));
                }
                if (trackSegment.isTunnelSideLeft()) {
                    Point2D ep1R = MathUtil.subtract(ep1, vector);
                    Point2D ep2R = MathUtil.subtract(ep2, vector);
                    g2.draw(new Line2D.Double(ep1R, ep2R));
                }
            }   // if isArc() {} else if isBezier() {} else...

            g2.setStroke(new BasicStroke(trackSegment.getTunnelLineWidth(),
                    BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.F));
            g2.setColor(trackSegment.getTunnelColor());

            // don't let tunnelEntranceWidth be less than tunnelFloorWidth + 6
            double tunnelEntranceWidth = Math.max(trackSegment.getTunnelEntranceWidth(),
                    trackSegment.getTunnelFloorWidth() + 6);

            double halfEntranceWidth = tunnelEntranceWidth / 2.0;
            double halfFloorWidth = trackSegment.getTunnelFloorWidth() / 2.0;
            double halfDiffWidth = halfEntranceWidth - halfFloorWidth;

            if (trackSegment.isFlip()) {
                boolean temp = trackSegment.isTunnelSideRight();
                trackSegment.setTunnelSideRight(trackSegment.isTunnelSideLeft());
                trackSegment.setTunnelSideRight(temp);
            }

            if (trackSegment.isTunnelHasEntry()) {
                if (trackSegment.isTunnelSideRight()) {
                    p1 = new Point2D.Double(0.0, 0.0);
                    p2 = new Point2D.Double(0.0, +halfFloorWidth);
                    p3 = new Point2D.Double(0.0, +halfEntranceWidth);
                    p4 = new Point2D.Double(-halfEntranceWidth - halfFloorWidth, +halfEntranceWidth);
                    p5 = new Point2D.Double(-halfEntranceWidth - halfFloorWidth, +halfEntranceWidth - halfDiffWidth);
                    p6 = new Point2D.Double(-halfFloorWidth, +halfEntranceWidth - halfDiffWidth);
                    p7 = new Point2D.Double(-halfDiffWidth, 0.0);

                    p1P = MathUtil.add(MathUtil.rotateRAD(p1, startAngleRAD), ep1);
                    p2P = MathUtil.add(MathUtil.rotateRAD(p2, startAngleRAD), ep1);
                    p3P = MathUtil.add(MathUtil.rotateRAD(p3, startAngleRAD), ep1);
                    p4P = MathUtil.add(MathUtil.rotateRAD(p4, startAngleRAD), ep1);
                    p5P = MathUtil.add(MathUtil.rotateRAD(p5, startAngleRAD), ep1);
                    p6P = MathUtil.add(MathUtil.rotateRAD(p6, startAngleRAD), ep1);
                    p7P = MathUtil.add(MathUtil.rotateRAD(p7, startAngleRAD), ep1);

                    GeneralPath path = new GeneralPath();
                    path.moveTo(p1P.getX(), p1P.getY());
                    path.lineTo(p2P.getX(), p2P.getY());
                    path.quadTo(p3P.getX(), p3P.getY(), p4P.getX(), p4P.getY());
                    path.lineTo(p5P.getX(), p5P.getY());
                    path.quadTo(p6P.getX(), p6P.getY(), p7P.getX(), p7P.getY());
                    path.closePath();
                    g2.draw(path);
                }
                if (trackSegment.isTunnelSideLeft()) {
                    p1 = new Point2D.Double(0.0, 0.0);
                    p2 = new Point2D.Double(0.0, -halfFloorWidth);
                    p3 = new Point2D.Double(0.0, -halfEntranceWidth);
                    p4 = new Point2D.Double(-halfEntranceWidth - halfFloorWidth, -halfEntranceWidth);
                    p5 = new Point2D.Double(-halfEntranceWidth - halfFloorWidth, -halfEntranceWidth + halfDiffWidth);
                    p6 = new Point2D.Double(-halfFloorWidth, -halfEntranceWidth + halfDiffWidth);
                    p7 = new Point2D.Double(-halfDiffWidth, 0.0);

                    p1P = MathUtil.add(MathUtil.rotateRAD(p1, startAngleRAD), ep1);
                    p2P = MathUtil.add(MathUtil.rotateRAD(p2, startAngleRAD), ep1);
                    p3P = MathUtil.add(MathUtil.rotateRAD(p3, startAngleRAD), ep1);
                    p4P = MathUtil.add(MathUtil.rotateRAD(p4, startAngleRAD), ep1);
                    p5P = MathUtil.add(MathUtil.rotateRAD(p5, startAngleRAD), ep1);
                    p6P = MathUtil.add(MathUtil.rotateRAD(p6, startAngleRAD), ep1);
                    p7P = MathUtil.add(MathUtil.rotateRAD(p7, startAngleRAD), ep1);

                    GeneralPath path = new GeneralPath();
                    path.moveTo(p1P.getX(), p1P.getY());
                    path.lineTo(p2P.getX(), p2P.getY());
                    path.quadTo(p3P.getX(), p3P.getY(), p4P.getX(), p4P.getY());
                    path.lineTo(p5P.getX(), p5P.getY());
                    path.quadTo(p6P.getX(), p6P.getY(), p7P.getX(), p7P.getY());
                    path.closePath();
                    g2.draw(path);
                }
            }
            if (trackSegment.isTunnelHasExit()) {
                if (trackSegment.isTunnelSideRight()) {
                    p1 = new Point2D.Double(0.0, 0.0);
                    p2 = new Point2D.Double(0.0, +halfFloorWidth);
                    p3 = new Point2D.Double(0.0, +halfEntranceWidth);
                    p4 = new Point2D.Double(halfEntranceWidth + halfFloorWidth, +halfEntranceWidth);
                    p5 = new Point2D.Double(halfEntranceWidth + halfFloorWidth, +halfEntranceWidth - halfDiffWidth);
                    p6 = new Point2D.Double(halfFloorWidth, +halfEntranceWidth - halfDiffWidth);
                    p7 = new Point2D.Double(halfDiffWidth, 0.0);

                    p1P = MathUtil.add(MathUtil.rotateRAD(p1, stopAngleRAD), ep2);
                    p2P = MathUtil.add(MathUtil.rotateRAD(p2, stopAngleRAD), ep2);
                    p3P = MathUtil.add(MathUtil.rotateRAD(p3, stopAngleRAD), ep2);
                    p4P = MathUtil.add(MathUtil.rotateRAD(p4, stopAngleRAD), ep2);
                    p5P = MathUtil.add(MathUtil.rotateRAD(p5, stopAngleRAD), ep2);
                    p6P = MathUtil.add(MathUtil.rotateRAD(p6, stopAngleRAD), ep2);
                    p7P = MathUtil.add(MathUtil.rotateRAD(p7, stopAngleRAD), ep2);

                    GeneralPath path = new GeneralPath();
                    path.moveTo(p1P.getX(), p1P.getY());
                    path.lineTo(p2P.getX(), p2P.getY());
                    path.quadTo(p3P.getX(), p3P.getY(), p4P.getX(), p4P.getY());
                    path.lineTo(p5P.getX(), p5P.getY());
                    path.quadTo(p6P.getX(), p6P.getY(), p7P.getX(), p7P.getY());
                    path.closePath();
                    g2.draw(path);
                }
                if (trackSegment.isTunnelSideLeft()) {
                    p1 = new Point2D.Double(0.0, 0.0);
                    p2 = new Point2D.Double(0.0, -halfFloorWidth);
                    p3 = new Point2D.Double(0.0, -halfEntranceWidth);
                    p4 = new Point2D.Double(halfEntranceWidth + halfFloorWidth, -halfEntranceWidth);
                    p5 = new Point2D.Double(halfEntranceWidth + halfFloorWidth, -halfEntranceWidth + halfDiffWidth);
                    p6 = new Point2D.Double(halfFloorWidth, -halfEntranceWidth + halfDiffWidth);
                    p7 = new Point2D.Double(halfDiffWidth, 0.0);

                    p1P = MathUtil.add(MathUtil.rotateRAD(p1, stopAngleRAD), ep2);
                    p2P = MathUtil.add(MathUtil.rotateRAD(p2, stopAngleRAD), ep2);
                    p3P = MathUtil.add(MathUtil.rotateRAD(p3, stopAngleRAD), ep2);
                    p4P = MathUtil.add(MathUtil.rotateRAD(p4, stopAngleRAD), ep2);
                    p5P = MathUtil.add(MathUtil.rotateRAD(p5, stopAngleRAD), ep2);
                    p6P = MathUtil.add(MathUtil.rotateRAD(p6, stopAngleRAD), ep2);
                    p7P = MathUtil.add(MathUtil.rotateRAD(p7, stopAngleRAD), ep2);

                    GeneralPath path = new GeneralPath();
                    path.moveTo(p1P.getX(), p1P.getY());
                    path.lineTo(p2P.getX(), p2P.getY());
                    path.quadTo(p3P.getX(), p3P.getY(), p4P.getX(), p4P.getY());
                    path.lineTo(p5P.getX(), p5P.getY());
                    path.quadTo(p6P.getX(), p6P.getY(), p7P.getX(), p7P.getY());
                    path.closePath();
                    g2.draw(path);
                }
            }

            // if necessary, put these back
            if (trackSegment.isFlip()) {
                boolean temp = trackSegment.isTunnelSideRight();
                trackSegment.setTunnelSideRight(trackSegment.isTunnelSideLeft());
                trackSegment.setTunnelSideRight(temp);
            }
        }
    }   // drawDecorations

    private int drawArrow(
            Graphics2D g2,
            Point2D ep,
            double angleRAD,
            boolean dirOut,
            int offset) {
        Point2D p1, p2, p3, p4, p5, p6;

        int arrowLength = trackSegment.getArrowLength();
        int arrowLineWidth = trackSegment.getArrowLineWidth();
        int arrowGap = trackSegment.getArrowGap();

        switch (trackSegment.getArrowStyle()) {
            default: {
                trackSegment.setArrowStyle(0);
                break;
            }
            case 0: {
                break;
            }
            case 1: {
                if (dirOut) {
                    p1 = new Point2D.Double(offset, -arrowLength);
                    p2 = new Point2D.Double(offset + arrowLength, 0.0);
                    p3 = new Point2D.Double(offset, +arrowLength);
                } else {
                    p1 = new Point2D.Double(offset + arrowLength, -arrowLength);
                    p2 = new Point2D.Double(offset, 0.0);
                    p3 = new Point2D.Double(offset + arrowLength, +arrowLength);
                }
                p1 = MathUtil.add(MathUtil.rotateRAD(p1, angleRAD), ep);
                p2 = MathUtil.add(MathUtil.rotateRAD(p2, angleRAD), ep);
                p3 = MathUtil.add(MathUtil.rotateRAD(p3, angleRAD), ep);

                g2.draw(new Line2D.Double(p1, p2));
                g2.draw(new Line2D.Double(p2, p3));
                offset += arrowLength + arrowGap;
                break;
            }
            case 2: {
                if (dirOut) {
                    p1 = new Point2D.Double(offset, -arrowLength);
                    p2 = new Point2D.Double(offset + arrowLength, 0.0);
                    p3 = new Point2D.Double(offset, +arrowLength);
                    p4 = new Point2D.Double(offset + arrowLineWidth + arrowGap, -arrowLength);
                    p5 = new Point2D.Double(offset + arrowLineWidth + arrowGap + arrowLength, 0.0);
                    p6 = new Point2D.Double(offset + arrowLineWidth + arrowGap, +arrowLength);
                } else {
                    p1 = new Point2D.Double(offset + arrowLength, -arrowLength);
                    p2 = new Point2D.Double(offset, 0.0);
                    p3 = new Point2D.Double(offset + arrowLength, +arrowLength);
                    p4 = new Point2D.Double(offset + arrowLineWidth + arrowGap + arrowLength, -arrowLength);
                    p5 = new Point2D.Double(offset + arrowLineWidth + arrowGap, 0.0);
                    p6 = new Point2D.Double(offset + arrowLineWidth + arrowGap + arrowLength, +arrowLength);
                }
                p1 = MathUtil.add(MathUtil.rotateRAD(p1, angleRAD), ep);
                p2 = MathUtil.add(MathUtil.rotateRAD(p2, angleRAD), ep);
                p3 = MathUtil.add(MathUtil.rotateRAD(p3, angleRAD), ep);
                p4 = MathUtil.add(MathUtil.rotateRAD(p4, angleRAD), ep);
                p5 = MathUtil.add(MathUtil.rotateRAD(p5, angleRAD), ep);
                p6 = MathUtil.add(MathUtil.rotateRAD(p6, angleRAD), ep);

                g2.draw(new Line2D.Double(p1, p2));
                g2.draw(new Line2D.Double(p2, p3));
                g2.draw(new Line2D.Double(p4, p5));
                g2.draw(new Line2D.Double(p5, p6));
                offset += arrowLength + (2 * (arrowLineWidth + arrowGap));
                break;
            }
            case 3: {
                if (dirOut) {
                    p1 = new Point2D.Double(offset, -arrowLength);
                    p2 = new Point2D.Double(offset + arrowLength, 0.0);
                    p3 = new Point2D.Double(offset, +arrowLength);
                } else {
                    p1 = new Point2D.Double(offset + arrowLength, -arrowLength);
                    p2 = new Point2D.Double(offset, 0.0);
                    p3 = new Point2D.Double(offset + arrowLength, +arrowLength);
                }
                p1 = MathUtil.add(MathUtil.rotateRAD(p1, angleRAD), ep);
                p2 = MathUtil.add(MathUtil.rotateRAD(p2, angleRAD), ep);
                p3 = MathUtil.add(MathUtil.rotateRAD(p3, angleRAD), ep);

                GeneralPath path = new GeneralPath();
                path.moveTo(p1.getX(), p1.getY());
                path.lineTo(p2.getX(), p2.getY());
                path.lineTo(p3.getX(), p3.getY());
                path.closePath();
                if (arrowLineWidth > 1) {
                    g2.fill(path);
                } else {
                    g2.draw(path);
                }
                offset += arrowLength + arrowGap;
                break;
            }
            case 4: {
                if (dirOut) {
                    p1 = new Point2D.Double(offset, 0.0);
                    p2 = new Point2D.Double(offset + (2 * arrowLength), -arrowLength);
                    p3 = new Point2D.Double(offset + (3 * arrowLength), 0.0);
                    p4 = new Point2D.Double(offset + (2 * arrowLength), +arrowLength);
                } else {
                    p1 = new Point2D.Double(offset, 0.0);
                    p2 = new Point2D.Double(offset + (4 * arrowLength), -arrowLength);
                    p3 = new Point2D.Double(offset + (3 * arrowLength), 0.0);
                    p4 = new Point2D.Double(offset + (4 * arrowLength), +arrowLength);
                }
                p1 = MathUtil.add(MathUtil.rotateRAD(p1, angleRAD), ep);
                p2 = MathUtil.add(MathUtil.rotateRAD(p2, angleRAD), ep);
                p3 = MathUtil.add(MathUtil.rotateRAD(p3, angleRAD), ep);
                p4 = MathUtil.add(MathUtil.rotateRAD(p4, angleRAD), ep);

                g2.draw(new Line2D.Double(p1, p3));
                g2.draw(new Line2D.Double(p2, p3));
                g2.draw(new Line2D.Double(p3, p4));

                offset += (3 * arrowLength) + arrowGap;
                break;
            }
            case 5: {
                if (dirOut) {
                    p1 = new Point2D.Double(offset, 0.0);
                    p2 = new Point2D.Double(offset + (2 * arrowLength), -arrowLength);
                    p3 = new Point2D.Double(offset + (3 * arrowLength), 0.0);
                    p4 = new Point2D.Double(offset + (2 * arrowLength), +arrowLength);
                } else {
                    p1 = new Point2D.Double(offset, 0.0);
                    p2 = new Point2D.Double(offset + (4 * arrowLength), -arrowLength);
                    p3 = new Point2D.Double(offset + (3 * arrowLength), 0.0);
                    p4 = new Point2D.Double(offset + (4 * arrowLength), +arrowLength);
                }
                p1 = MathUtil.add(MathUtil.rotateRAD(p1, angleRAD), ep);
                p2 = MathUtil.add(MathUtil.rotateRAD(p2, angleRAD), ep);
                p3 = MathUtil.add(MathUtil.rotateRAD(p3, angleRAD), ep);
                p4 = MathUtil.add(MathUtil.rotateRAD(p4, angleRAD), ep);

                GeneralPath path = new GeneralPath();
                path.moveTo(p4.getX(), p4.getY());
                path.lineTo(p2.getX(), p2.getY());
                path.lineTo(p3.getX(), p3.getY());
                path.closePath();
                if (arrowLineWidth > 1) {
                    g2.fill(path);
                } else {
                    g2.draw(path);
                }
                g2.draw(new Line2D.Double(p1, p3));

                offset += (3 * arrowLength) + arrowGap;
                break;
            }
        }
        return offset;
    }   // drawArrow

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrackSegmentView.class);
}
