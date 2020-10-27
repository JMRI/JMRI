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

    /*
    The following are convience getters to simplify accessing these
    properties of the track segment being drawn by this view
     */
    private boolean isDashed() {
        return trackSegment.isDashed();
    }

    private boolean isFlip() {
        return trackSegment.isFlip();
    }

    private boolean isArc() {
        return trackSegment.isArc();
    }

    private boolean isCircle() {
        return trackSegment.isCircle();
    }

    private boolean isBezier() {
        return trackSegment.isBezier();
    }

    private ArrayList<Point2D> getBezierControlPoints() {
        return trackSegment.getBezierControlPoints();
    }

    private LayoutBlock getLayoutBlock() {
        return trackSegment.getLayoutBlock();
    }

    private void calculateTrackSegmentAngle() {
        trackSegment.calculateTrackSegmentAngle();
    }

    private Point2D getCentreSeg() {
        return trackSegment.getCentreSeg();
    }

    private double getCX() {
        return trackSegment.getCX();
    }

    private double getCY() {
        return trackSegment.getCY();
    }

    private double getCH() {
        return trackSegment.getCH();
    }

    private double getCW() {
        return trackSegment.getCW();
    }

    private double getStartAdj() {
        return trackSegment.getStartAdj();
    }

    private double getTmpAngle() {
        return trackSegment.getTmpAngle();
    }

    private void trackRedrawn() {
        trackSegment.trackRedrawn();
    }

    private Point2D[] getBezierPoints() {
        return trackSegment.getBezierPoints();
    }

    private HitPointType getType1() {
        return trackSegment.getType1();
    }

    private HitPointType getType2() {
        return trackSegment.getType2();
    }

    private LayoutTrack getConnect1() {
        return trackSegment.getConnect1();
    }

    private LayoutTrack getConnect2() {
        return trackSegment.getConnect2();
    }

    private boolean isShowConstructionLines() {
        return trackSegment.isShowConstructionLines();
    }

    private Point2D getCoordsCenterCircle() {
        return trackSegment.getCoordsCenterCircle();
    }

    private Ellipse2D trackEditControlCircleAt(@Nonnull Point2D inPoint) {
        return trackSegment.trackEditControlCircleAt(inPoint);
    }

    private Rectangle2D layoutEditorControlRectAt(@Nonnull Point2D inPoint) {
        return trackSegment.layoutEditor.layoutEditorControlRectAt(inPoint);
    }

    private int getArrowStyle() {
        return trackSegment.getArrowStyle();
    }

    private int getArrowLineWidth() {
        return trackSegment.getArrowLineWidth();
    }

    private int getArrowLength() {
        return trackSegment.getArrowLength();
    }

    private int getArrowGap() {
        return trackSegment.getArrowGap();
    }

    private Color getArrowColor() {
        return trackSegment.getArrowColor();
    }

    private boolean isArrowEndStart() {
        return trackSegment.isArrowEndStart();
    }

    private boolean isArrowEndStop() {
        return trackSegment.isArrowEndStop();
    }

    private boolean isArrowDirIn() {
        return trackSegment.isArrowDirIn();
    }

    private boolean isArrowDirOut() {
        return trackSegment.isArrowDirOut();
    }

    private boolean isBridgeSideRight() {
        return trackSegment.isBridgeSideRight();
    }

    private boolean isBridgeSideLeft() {
        return trackSegment.isBridgeSideLeft();
    }

    private boolean isBridgeHasEntry() {
        return trackSegment.isBridgeHasEntry();
    }

    private boolean isBridgeHasExit() {
        return trackSegment.isBridgeHasExit();
    }

    private int getBridgeApproachWidth() {
        return trackSegment.getBridgeApproachWidth();
    }

    private int getBridgeDeckWidth() {
        return trackSegment.getBridgeDeckWidth();
    }

    private int getBridgeLineWidth() {
        return trackSegment.getBridgeLineWidth();
    }

    private Color getBridgeColor() {
        return trackSegment.getBridgeColor();
    }

    private boolean isBumperEndStart() {
        return trackSegment.isBumperEndStart();
    }

    private boolean isBumperEndStop() {
        return trackSegment.isBumperEndStop();
    }

    private int getBumperLineWidth() {
        return trackSegment.getBumperLineWidth();
    }

    private Color getBumperColor() {
        return trackSegment.getBumperColor();
    }

    private int getBumperLength() {
        return trackSegment.getBumperLength();
    }

    private boolean isBumperFlipped() {
        return trackSegment.isBumperFlipped();
    }

    private boolean isTunnelSideRight() {
        return trackSegment.isTunnelSideRight();
    }

    private boolean isTunnelSideLeft() {
        return trackSegment.isTunnelSideLeft();
    }

    private int getTunnelFloorWidth() {
        return trackSegment.getTunnelFloorWidth();
    }

    private int getTunnelLineWidth() {
        return trackSegment.getTunnelLineWidth();
    }

    private Color getTunnelColor() {
        return trackSegment.getTunnelColor();
    }

    private int getTunnelEntranceWidth() {
        return trackSegment.getTunnelEntranceWidth();
    }

    private boolean isTunnelHasEntry() {
        return trackSegment.isTunnelHasEntry();
    }

    private boolean isTunnelHasExit() {
        return trackSegment.isTunnelHasExit();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void draw1(Graphics2D g2, boolean isMain, boolean isBlock) {
        //if (getName().equals("T15")) {
        //   log.debug("STOP");
        //}
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
                calculateTrackSegmentAngle();
                g2.draw(new Arc2D.Double(
                        getCX(), getCY(), getCW(), getCH(),
                        getStartAdj(), getTmpAngle(),
                        Arc2D.OPEN));
                trackRedrawn();
            } else if (isBezier()) {
                Point2D[] points = getBezierPoints();
                MathUtil.drawBezier(g2, points);
            } else {
                Point2D end1 = LayoutEditor.getCoords(getConnect1(), getType1());
                Point2D end2 = LayoutEditor.getCoords(getConnect2(), getType2());

                g2.draw(new Line2D.Double(end1, end2));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
                calculateTrackSegmentAngle();
                Rectangle2D cRectangle2D = new Rectangle2D.Double(
                        getCX(), getCY(), getCW(), getCH());
                Rectangle2D tRectangle2D = MathUtil.inset(cRectangle2D, -railDisplacement);
                double startAdj = getStartAdj(), tmpAngle = getTmpAngle();
                g2.draw(new Arc2D.Double(tRectangle2D.getX(), tRectangle2D.getY(),
                        tRectangle2D.getWidth(), tRectangle2D.getHeight(),
                        startAdj, tmpAngle, Arc2D.OPEN));
                tRectangle2D = MathUtil.inset(cRectangle2D, +railDisplacement);
                g2.draw(new Arc2D.Double(tRectangle2D.getX(), tRectangle2D.getY(),
                        tRectangle2D.getWidth(), tRectangle2D.getHeight(),
                        startAdj, tmpAngle, Arc2D.OPEN));
                trackRedrawn();
            } else if (isBezier()) {
                Point2D[] points = getBezierPoints();
                MathUtil.drawBezier(g2, points, -railDisplacement);
                MathUtil.drawBezier(g2, points, +railDisplacement);
            } else {
                Point2D end1 = LayoutEditor.getCoords(getConnect1(), getType1());
                Point2D end2 = LayoutEditor.getCoords(getConnect2(), getType2());

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
        if (isShowConstructionLines()) {
            Point2D ep1 = LayoutEditor.getCoords(getConnect1(), getType1());
            Point2D ep2 = LayoutEditor.getCoords(getConnect2(), getType2());
            if (isCircle()) {
                // draw radiuses
                Point2D circleCenterPoint = getCoordsCenterCircle();
                g2.draw(new Line2D.Double(circleCenterPoint, ep1));
                g2.draw(new Line2D.Double(circleCenterPoint, ep2));
                // Draw a circle and square at the circles centre, that
                // allows the user to change the angle by dragging the mouse.
                g2.draw(trackEditControlCircleAt(circleCenterPoint));
                g2.draw(layoutEditorControlRectAt(circleCenterPoint));
            } else if (isBezier()) {
                // draw construction lines and control circles
                Point2D lastPt = ep1;
                for (Point2D bcp : getBezierControlPoints()) {
                    g2.draw(new Line2D.Double(lastPt, bcp));
                    lastPt = bcp;
                    g2.draw(layoutEditorControlRectAt(bcp));
                }
                g2.draw(new Line2D.Double(lastPt, ep2));
            }
        }
        g2.draw(trackEditControlCircleAt(getCentreSeg()));
    }   // drawEditControls

    @Override
    protected void drawTurnoutControls(Graphics2D g2) {
        // TrackSegments don't have turnout controls...
        // nothing to see here... move along...
    }

    private Point2D ep1, ep2;
    Point2D p1, p2, p3, p4, p5, p6, p7;
    Point2D p1P, p2P, p3P, p4P, p5P, p6P, p7P;
    double startAngleRAD, stopAngleRAD;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void drawDecorations(Graphics2D g2) {
//   if (getName().equals("T5")) {
//       log.debug("STOP");
//   }

// get end points and calculate start/stop angles (in radians)
        ep1 = LayoutEditor.getCoords(getConnect1(), getType1());
        ep2 = LayoutEditor.getCoords(getConnect2(), getType2());
        p1P = ep1;
        p2P = ep2;

        if (isArc()) {
            calculateTrackSegmentAngle();
            double startAngleDEG = getStartAdj(), extentAngleDEG = getTmpAngle();
            startAngleRAD = (Math.PI / 2.D) - Math.toRadians(startAngleDEG);
            stopAngleRAD = (Math.PI / 2.D) - Math.toRadians(startAngleDEG + extentAngleDEG);
            if (isFlip()) {
                startAngleRAD += Math.PI;
                stopAngleRAD += Math.PI;
            } else {
                double temp = startAngleRAD;
                startAngleRAD = stopAngleRAD;
                stopAngleRAD = temp;
            }
        } else if (isBezier()) {
            ArrayList<Point2D> bezierControlPoints = getBezierControlPoints();
            Point2D cp0 = bezierControlPoints.get(0);
            Point2D cpN = bezierControlPoints.get(bezierControlPoints.size() - 1);
            startAngleRAD = (Math.PI / 2.D) - MathUtil.computeAngleRAD(cp0, ep1);
            stopAngleRAD = (Math.PI / 2.D) - MathUtil.computeAngleRAD(ep2, cpN);
        } else {
            startAngleRAD = (Math.PI / 2.D) - MathUtil.computeAngleRAD(ep2, ep1);
            stopAngleRAD = startAngleRAD;
        }

        drawArrowDecorations(g2);
        drawBridgeDecorations(g2);
        drawBumperDecorations(g2);
        drawTunnelDecorations(g2);
    }   // drawDecorations

    //
    // arrow decorations
    //
    private void drawArrowDecorations(Graphics2D g2) {
        if (getArrowStyle() > 0) {
            g2.setStroke(new BasicStroke(getArrowLineWidth(),
                    BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.F));
            g2.setColor(getArrowColor());

            // draw the start arrows
            int offset = 1;
            if (isArrowEndStart()) {
                if (isArrowDirIn()) {
                    offset = drawArrow(g2, ep1, Math.PI + startAngleRAD, false, offset);
                }
                if (isArrowDirOut()) {
                    /* offset = */ drawArrow(g2, ep1, Math.PI + startAngleRAD, true, offset);
                }
            }

            // draw the stop arrows
            offset = 1;
            if (isArrowEndStop()) {
                if (isArrowDirIn()) {
                    offset = drawArrow(g2, ep2, stopAngleRAD, false, offset);
                }
                if (isArrowDirOut()) {
                    /* offset = */ drawArrow(g2, ep2, stopAngleRAD, true, offset);
                }
            }
        }
    }

    //
    // bridge decorations
    //
    private void drawBridgeDecorations(Graphics2D g2) {
        boolean bridgeSideRight = isBridgeSideRight();
        boolean bridgeSideLeft = isBridgeSideLeft();

        if (bridgeSideLeft || bridgeSideRight) {
            float halfWidth = getBridgeDeckWidth() / 2.F;

            g2.setStroke(new BasicStroke(getBridgeLineWidth(),
                    BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.F));
            g2.setColor(getBridgeColor());

            if (isArc()) {
                calculateTrackSegmentAngle();
                Rectangle2D cRectangle2D = new Rectangle2D.Double(
                        getCX(), getCY(), getCW(), getCH());
                double startAdj = getStartAdj(), tmpAngle = getTmpAngle();
                if (bridgeSideLeft) {
                    Rectangle2D tRectangle2D = MathUtil.inset(cRectangle2D, -halfWidth);
                    g2.draw(new Arc2D.Double(tRectangle2D.getX(), tRectangle2D.getY(),
                            tRectangle2D.getWidth(), tRectangle2D.getHeight(),
                            startAdj, tmpAngle, Arc2D.OPEN));
                }
                if (bridgeSideRight) {
                    Rectangle2D tRectangle2D = MathUtil.inset(cRectangle2D, +halfWidth);
                    g2.draw(new Arc2D.Double(tRectangle2D.getX(), tRectangle2D.getY(),
                            tRectangle2D.getWidth(), tRectangle2D.getHeight(),
                            startAdj, tmpAngle, Arc2D.OPEN));
                }
            } else if (isBezier()) {
                Point2D[] points = getBezierPoints();
                if (bridgeSideLeft) {
                    MathUtil.drawBezier(g2, points, -halfWidth);
                }
                if (bridgeSideRight) {
                    MathUtil.drawBezier(g2, points, +halfWidth);
                }
            } else {
                Point2D delta = MathUtil.subtract(ep2, ep1);
                Point2D vector = MathUtil.normalize(delta, halfWidth);
                vector = MathUtil.orthogonal(vector);

                if (bridgeSideRight) {
                    Point2D ep1R = MathUtil.add(ep1, vector);
                    Point2D ep2R = MathUtil.add(ep2, vector);
                    g2.draw(new Line2D.Double(ep1R, ep2R));
                }

                if (bridgeSideLeft) {
                    Point2D ep1L = MathUtil.subtract(ep1, vector);
                    Point2D ep2L = MathUtil.subtract(ep2, vector);
                    g2.draw(new Line2D.Double(ep1L, ep2L));
                }
            }   // if isArc() {} else if isBezier() {} else...

            if (isFlip()) {
                boolean temp = bridgeSideRight;
                bridgeSideRight = bridgeSideLeft;
                bridgeSideLeft = temp;
            }
            int bridgeApproachWidth = getBridgeApproachWidth();

            if (isBridgeHasEntry()) {
                if (bridgeSideRight) {
                    p1 = new Point2D.Double(-bridgeApproachWidth, +bridgeApproachWidth + halfWidth);
                    p2 = new Point2D.Double(0.0, +halfWidth);
                    p1P = MathUtil.add(MathUtil.rotateRAD(p1, startAngleRAD), ep1);
                    p2P = MathUtil.add(MathUtil.rotateRAD(p2, startAngleRAD), ep1);
                    g2.draw(new Line2D.Double(p1P, p2P));
                }
                if (bridgeSideLeft) {
                    p1 = new Point2D.Double(-bridgeApproachWidth, -bridgeApproachWidth - halfWidth);
                    p2 = new Point2D.Double(0.0, -halfWidth);
                    p1P = MathUtil.add(MathUtil.rotateRAD(p1, startAngleRAD), ep1);
                    p2P = MathUtil.add(MathUtil.rotateRAD(p2, startAngleRAD), ep1);
                    g2.draw(new Line2D.Double(p1P, p2P));
                }
            }
            if (isBridgeHasExit()) {
                if (bridgeSideRight) {
                    p1 = new Point2D.Double(+bridgeApproachWidth, +bridgeApproachWidth + halfWidth);
                    p2 = new Point2D.Double(0.0, +halfWidth);
                    p1P = MathUtil.add(MathUtil.rotateRAD(p1, stopAngleRAD), ep2);
                    p2P = MathUtil.add(MathUtil.rotateRAD(p2, stopAngleRAD), ep2);
                    g2.draw(new Line2D.Double(p1P, p2P));
                }
                if (bridgeSideLeft) {
                    p1 = new Point2D.Double(+bridgeApproachWidth, -bridgeApproachWidth - halfWidth);
                    p2 = new Point2D.Double(0.0, -halfWidth);
                    p1P = MathUtil.add(MathUtil.rotateRAD(p1, stopAngleRAD), ep2);
                    p2P = MathUtil.add(MathUtil.rotateRAD(p2, stopAngleRAD), ep2);
                    g2.draw(new Line2D.Double(p1P, p2P));
                }
            }
        }
    }

    //
    // end bumper decorations
    //
    private void drawBumperDecorations(Graphics2D g2) {
        if (isBumperEndStart() || isBumperEndStop()) {
            g2.setStroke(new BasicStroke(getBumperLineWidth(),
                    BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.F));
            g2.setColor(getBumperColor());

            float halfLength = getBumperLength() / 2.F;

            if (isBumperFlipped()) {
                double temp = startAngleRAD;
                startAngleRAD = stopAngleRAD;
                stopAngleRAD = temp;
            }

            // common points
            p1 = new Point2D.Double(0.F, -halfLength);
            p2 = new Point2D.Double(0.F, +halfLength);

            if (isBumperEndStart()) {
                p1P = MathUtil.add(MathUtil.rotateRAD(p1, startAngleRAD), ep1);
                p2P = MathUtil.add(MathUtil.rotateRAD(p2, startAngleRAD), ep1);
                // draw cross tie
                g2.draw(new Line2D.Double(p1P, p2P));
            }
            if (isBumperEndStop()) {
                p1P = MathUtil.add(MathUtil.rotateRAD(p1, stopAngleRAD), ep2);
                p2P = MathUtil.add(MathUtil.rotateRAD(p2, stopAngleRAD), ep2);
                // draw cross tie
                g2.draw(new Line2D.Double(p1P, p2P));
            }
        }   // if (bumperEndStart || bumperEndStop)
    }

    //
    // tunnel decorations
    //
    private void drawTunnelDecorations(Graphics2D g2) {
        boolean tunnelSideRight = isTunnelSideRight();
        boolean tunnelSideLeft = isTunnelSideLeft();

        if (tunnelSideRight || tunnelSideLeft) {
            float halfWidth = getTunnelFloorWidth() / 2.F;
            g2.setStroke(new BasicStroke(getTunnelLineWidth(),
                    BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10.F,
                    new float[]{6.F, 4.F}, 0));
            g2.setColor(getTunnelColor());

            if (isArc()) {
                calculateTrackSegmentAngle();
                Rectangle2D cRectangle2D = new Rectangle2D.Double(
                        getCX(), getCY(), getCW(), getCH());
                double startAngleDEG = getStartAdj(),
                        extentAngleDEG = getTmpAngle();
                if (tunnelSideRight) {
                    Rectangle2D tRectangle2D = MathUtil.inset(cRectangle2D, +halfWidth);
                    g2.draw(new Arc2D.Double(tRectangle2D.getX(), tRectangle2D.getY(),
                            tRectangle2D.getWidth(), tRectangle2D.getHeight(),
                            startAngleDEG, extentAngleDEG, Arc2D.OPEN));
                }
                if (tunnelSideLeft) {
                    Rectangle2D tRectangle2D = MathUtil.inset(cRectangle2D, -halfWidth);
                    g2.draw(new Arc2D.Double(tRectangle2D.getX(), tRectangle2D.getY(),
                            tRectangle2D.getWidth(), tRectangle2D.getHeight(),
                            startAngleDEG, extentAngleDEG, Arc2D.OPEN));
                }
                trackRedrawn();
            } else if (isBezier()) {
                Point2D[] points = getBezierPoints();
                if (tunnelSideRight) {
                    MathUtil.drawBezier(g2, points, +halfWidth);
                }
                if (tunnelSideLeft) {
                    MathUtil.drawBezier(g2, points, -halfWidth);
                }
            } else {
                Point2D delta = MathUtil.subtract(ep2, ep1);
                Point2D vector = MathUtil.normalize(delta, halfWidth);
                vector = MathUtil.orthogonal(vector);

                if (tunnelSideRight) {
                    Point2D ep1L = MathUtil.add(ep1, vector);
                    Point2D ep2L = MathUtil.add(ep2, vector);
                    g2.draw(new Line2D.Double(ep1L, ep2L));
                }
                if (tunnelSideLeft) {
                    Point2D ep1R = MathUtil.subtract(ep1, vector);
                    Point2D ep2R = MathUtil.subtract(ep2, vector);
                    g2.draw(new Line2D.Double(ep1R, ep2R));
                }
            }   // if isArc() {} else if isBezier() {} else...

            g2.setStroke(new BasicStroke(getTunnelLineWidth(),
                    BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.F));
            g2.setColor(getTunnelColor());

            // don't let tunnelEntranceWidth be less than tunnelFloorWidth + 6
            double tunnelEntranceWidth = Math.max(getTunnelEntranceWidth(),
                    getTunnelFloorWidth() + 6);

            double halfEntranceWidth = tunnelEntranceWidth / 2.0;
            double halfFloorWidth = getTunnelFloorWidth() / 2.0;
            double halfDiffWidth = halfEntranceWidth - halfFloorWidth;

            if (isFlip()) {
                boolean temp = tunnelSideRight;
                tunnelSideRight = tunnelSideLeft;
                tunnelSideLeft = temp;
            }

            if (isTunnelHasEntry()) {
                if (tunnelSideRight) {
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
                if (tunnelSideLeft) {
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
            if (isTunnelHasExit()) {
                if (tunnelSideRight) {
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
                if (tunnelSideLeft) {
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
        }
    }

    private int drawArrow(
            Graphics2D g2,
            Point2D ep,
            double angleRAD,
            boolean dirOut,
            int offset) {
        Point2D p1, p2, p3, p4, p5, p6;

        int arrowLength = getArrowLength();
        int arrowLineWidth = getArrowLineWidth();
        int arrowGap = getArrowGap();

        switch (getArrowStyle()) {
            default:
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
