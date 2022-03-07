package jmri.jmrit.vsdecoder;

import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import jmri.jmrit.display.layoutEditor.*;
import jmri.util.MathUtil;

import javax.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Navigation through a LayoutEditor panel to set the sound position.
 *
 * Almost all code from George Warner's LENavigator.
 * ------------------------------------------------
 * Added direction change feature with new methods
 * setReturnTrack(T), setReturnLastTrack(T) and
 * a Block check.
 *
 * Concept for direction change, e.g.:
 *  EndBumper ---- TrackSegment ------ Anchor
 *  lastTrack      returnTrack     returnLastTrack
 *
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * for more details.
 *
 * @author Klaus Killinger Copyright (C) 2022
 */
public class VSDNavigation {

    private VSDecoder d;

    private boolean use_blocks = VSDecoderManager.instance().getVSDecoderPreferences().getUseBlocksSetting();

    // constructor
    VSDNavigation(VSDecoder vsd) {
        d = vsd;
    }

    // layout track specific methods
    boolean navigatePositionalPoint() {
        boolean result = true; // always go to next track
        PositionablePoint pp = (PositionablePoint) d.getLayoutTrack();
        PositionablePoint.PointType type = pp.getType();
        switch (type) {
            case ANCHOR: {
                if (pp.getConnect1().equals(d.getLastTrack())) {
                    d.setLayoutTrack(pp.getConnect2());
                    d.setReturnTrack(d.getLayoutTrack());
                } else if (pp.getConnect2().equals(d.getLastTrack())) {
                    d.setLayoutTrack(pp.getConnect1());
                    d.setReturnTrack(d.getLayoutTrack());
                } else { // OOPS! we're lost!
                    result = false;
                    break;
                }
                d.setLastTrack(pp);
                break;
            }
            default:
            case END_BUMPER: {
                d.setReturnTrack(pp.getConnect1());
                d.distanceOnTrack = d.getReturnDistance();
                d.setDistance(0);
                result = false;
                break;
            }
            case EDGE_CONNECTOR: {
                TrackSegment ts2 = null;
                if (pp.getLinkedPoint() != null) {
                    ts2 = pp.getLinkedPoint().getConnect1();
                    d.setModels(pp.getLinkedEditor()); // change the panel
                    d.setLayoutTrack(ts2);
                    d.setReturnTrack(d.getLayoutTrack());
                    if (pp.getLinkedPoint().equals(ts2.getConnect1())) {
                        d.setLastTrack(ts2.getConnect1());
                    } else if (pp.getLinkedPoint().equals(ts2.getConnect2())) {
                        d.setLastTrack(ts2.getConnect2());
                    } else {
                        log.warn(" EdgeConnector lost");
                    }
                } else {
                    log.warn(" EdgeConnector is not linked");
                    d.setReturnTrack(d.getLastTrack());
                    d.distanceOnTrack = d.getReturnDistance();
                    d.setDistance(0);
                    result = false;
                }
                break;
            }
        }
        return result;
    }

    boolean navigateTrackSegment() {
        boolean result = false;
        // LayoutTrack block and reported block must be equal
        if (use_blocks && ((TrackSegment) d.getLayoutTrack()).getLayoutBlock().getBlock() != VSDecoderManager.instance().currentBlock.get(d)) {
            // not in the block
            d.setDistance(0);
            return result;
        }

        double distanceOnTrack = d.getDistance() + d.distanceOnTrack;
        d.nextLayoutTrack = null;

        TrackSegmentView tsv = d.getModels().getTrackSegmentView((TrackSegment) d.getLayoutTrack());
        if (tsv.isArc()) {
            // tsv.calculateTrackSegmentAngle(); // ... has protected access in TrackSegmentView
            // when do we need this? After a panel change?
            Point2D radius2D = new Point2D.Double(tsv.getCW() / 2, tsv.getCH() / 2);
            double radius = (radius2D.getX() + radius2D.getY()) / 2;
            Point2D centre = tsv.getCentre();
            /*
             * Note: Angles go CCW from south to east to north to west, etc.
             * For JMRI angles subtract from 90 to get east, south, west, north
             */
            //double startAdjDEG = tsv.getStartAdj(); // klk The value of the local variable startAdjDEG is not really used
            double tmpAngleDEG = tsv.getTmpAngle();

            double distance = 2 * radius * Math.PI * tmpAngleDEG / 360;
            d.setReturnDistance(distance);
            if (distanceOnTrack < distance) { // it's on this track
                Point2D p1 = d.getModels().getCoords(tsv.getConnect1(), tsv.getType1());
                Point2D p2 = d.getModels().getCoords(tsv.getConnect2(), tsv.getType2());
                if (!tsv.isCircle()) {
                    centre = MathUtil.midPoint(p1, p2);
                    Point2D centreSeg = tsv.getCentreSeg();
                    double newX = (centre.getX() < centreSeg.getX()) ? Math.min(p1.getX(), p2.getX()) : Math.max(p1.getX(), p2.getX());
                    double newY = (centre.getY() < centreSeg.getY()) ? Math.min(p1.getY(), p2.getY()) : Math.max(p1.getY(), p2.getY());
                    centre = new Point2D.Double(newX, newY);
                }
                double angle1DEG = MathUtil.computeAngleDEG(p1, centre) - 90;
                double angle2DEG = MathUtil.computeAngleDEG(p2, centre) - 90;
                Point2D centreSeg = tsv.getCentreSeg();
                double angle3DEG = MathUtil.computeAngleDEG(centreSeg, centre) - 90;
                double angleDeltaDEG = MathUtil.wrapPM360(2 * (angle3DEG - angle1DEG));
                double ratio = distanceOnTrack / distance;
                Point2D delta = new Point2D.Double(radius, 0);
                double angleDEG = 0;
                if (tsv.getConnect1().equals(d.getLastTrack())) {
                    // entering from this end...
                    d.nextLayoutTrack = tsv.getConnect2();
                    d.setReturnLastTrack(tsv.getConnect2());
                    angleDEG = angle1DEG;
                    angleDeltaDEG = MathUtil.lerp(0, angleDeltaDEG, ratio);
                } else if (tsv.getConnect2().equals(d.getLastTrack())) {
                    // entering from the other end...
                    d.nextLayoutTrack = tsv.getConnect1();
                    d.setReturnLastTrack(tsv.getConnect1());
                    //startAdjDEG += tmpAngleDEG; // SpotBugs: Dead store to startAdjDEG
                    angleDEG = angle2DEG;
                    angleDeltaDEG = MathUtil.lerp(0, -angleDeltaDEG, ratio);
                } else { // OOPS! we're lost!
                    log.info(" lost");
                    result = false;
                    angleDeltaDEG = 0;
                }
                double dirDeltaDEG = Math.signum(angleDeltaDEG) * -90;

                double newAngleDeg = -(angleDEG + angleDeltaDEG);
                // Compute location
                delta = MathUtil.rotateDEG(delta, newAngleDeg);
                if (!tsv.isCircle()) {
                    delta = MathUtil.multiply(delta, radius2D.getX() / radius, radius2D.getY() / radius);
                }
                d.setLocation(MathUtil.add(centre, delta));
                d.setDirectionDEG(newAngleDeg + dirDeltaDEG);
                d.setDistance(0);
            } else { // it's not on this track
                d.nextLayoutTrack = tsv.getConnect2();
                if (tsv.getConnect2().equals(d.getLastTrack())) {
                    // entering from the other end...
                    d.nextLayoutTrack = tsv.getConnect1();
                }
                d.setDistance(distanceOnTrack - distance);
                distanceOnTrack = 0;
                result = true;
            }
            d.distanceOnTrack = distanceOnTrack;
        } else if (tsv.isBezier()) {
            //Point2D[] points = tsv.getBezierPoints(); // getBezierPoints() has private access in TrackSegmentView!
            // Alternative
            Point2D ep1 = d.getModels().getCoords(tsv.getConnect1(), tsv.getType1());
            Point2D ep2 = d.getModels().getCoords(tsv.getConnect2(), tsv.getType2());
            int cnt = tsv.getBezierControlPoints().size() + 2;
            Point2D[] points = new Point2D[cnt];
            points[0] = ep1;
            for (int idx = 0; idx < cnt - 2; idx++) {
                points[idx + 1] = tsv.getBezierControlPoints().get(idx);
            }
            points[cnt - 1] = ep2;

            double distance = MathUtil.drawBezier(null, points);
            d.setReturnDistance(distance);
            if (distanceOnTrack < distance) { // it's on this track
                d.nextLayoutTrack = tsv.getConnect2();
                d.setReturnLastTrack(tsv.getConnect2());
                // if entering from the other end...
                if (tsv.getConnect2().equals(d.getLastTrack())) {
                    points = MathUtil.reverse(points);     //..reverse the points
                    d.nextLayoutTrack = tsv.getConnect1(); // and change the next LayoutTrack
                    d.setReturnLastTrack(tsv.getConnect1());
                }
                GeneralPath path = MathUtil.getBezierPath(points);
                PathIterator i = path.getPathIterator(null);
                List<Point2D> pathPoints = new ArrayList<>();
                while (!i.isDone()) {
                    float[] data = new float[6];
                    switch (i.currentSegment(data)) {
                        case PathIterator.SEG_MOVETO:
                        case PathIterator.SEG_LINETO: {
                            pathPoints.add(new Point2D.Double(data[0], data[1]));
                            break;
                        }
                        default: {
                            log.error("Unknown path segment type: {}.", i.currentSegment(data));
                            //$FALL-THROUGH$
                      //  case PathIterator.SEG_QUADTO:
                      //  case PathIterator.SEG_CUBICTO:
                      //  case PathIterator.SEG_CLOSE: {
                            // OOPS! we're lost!
                            log.info(" bezier lost");
                            result = false;
                            break;
                        }
                    }
                    i.next();
                } // while (!i.isDone())
                return navigate(pathPoints, d.nextLayoutTrack);
            } else { // it's not on this track
                d.nextLayoutTrack = tsv.getConnect2();
                if (tsv.getConnect2().equals(d.getLastTrack())) {
                    d.nextLayoutTrack = tsv.getConnect1();
                }
                d.setDistance(distanceOnTrack - distance);
                distanceOnTrack = 0;
                result = true;
            }
            d.distanceOnTrack = distanceOnTrack;
        } else {
            Point2D p1 = d.getModels().getCoords(tsv.getConnect1(), tsv.getType1());
            Point2D p2 = d.getModels().getCoords(tsv.getConnect2(), tsv.getType2());
            double distance = MathUtil.distance(p1, p2);
            d.setReturnDistance(distance);
            if (distanceOnTrack < distance) {
                // it's on this track
                if (tsv.getConnect1().equals(d.getLastTrack())) {
                    d.nextLayoutTrack = tsv.getConnect2();
                    d.setReturnLastTrack(tsv.getConnect2());
                } else if (tsv.getConnect2().equals(d.getLastTrack())) {
                    // if entering from the other end then swap end points
                    d.nextLayoutTrack = tsv.getConnect1();
                    d.setReturnLastTrack(tsv.getConnect1());
                    // swap
                    Point2D temp = p1;
                    p1 = p2;
                    p2 = temp;
                } else { // OOPS! we're lost!
                    result = false;
                }
                double ratio = distanceOnTrack / distance;
                d.setLocation(MathUtil.lerp(p1, p2, ratio));
                d.setDirectionRAD((Math.PI / 2) - MathUtil.computeAngleRAD(p2, p1));
                d.setDistance(0);
            } else { // it's not on this track
                if (tsv.getConnect1().equals(d.getLastTrack())) {
                    d.nextLayoutTrack = tsv.getConnect2();
                } else if (tsv.getConnect2().equals(d.getLastTrack())) {
                    d.nextLayoutTrack = tsv.getConnect1();
                }
                d.setDistance(distanceOnTrack - distance);
                distanceOnTrack = 0;
                result = true;
            }
            d.distanceOnTrack = distanceOnTrack;
        }

        if (result) { // not on this track
            // go to next track
            LayoutTrack last = d.getLayoutTrack();
            if (d.nextLayoutTrack != null) {
                d.setLayoutTrack(d.nextLayoutTrack);
            } else { // OOPS! we're lost!
                result = false;
            }
            if (result) {
                d.setLastTrack(last);
                d.setReturnTrack(d.getLayoutTrack());
                d.setReturnLastTrack(d.getLayoutTrack());
            }
        }
        d.savedSound.setTunnel(tsv.isTunnelSideRight() || tsv.isTunnelSideLeft() || tsv.isTunnelHasEntry() || tsv.isTunnelHasExit() ? true : false); // set the tunnel status
        return result;
    }

    boolean navigateLayoutTurnout() {
        boolean result = false;
        if (use_blocks && ((LayoutTurnout) d.getLayoutTrack()).getLayoutBlock().getBlock() != VSDecoderManager.instance().currentBlock.get(d)) {
            // we are not in the block
            d.setDistance(0);
            return result;
        }

        double distanceOnTrack = d.getDistance() + d.distanceOnTrack;

        LayoutTurnoutView tv = d.getModels().getLayoutTurnoutView((LayoutTurnout) d.getLayoutTrack());
        Point2D pM = tv.getCoordsCenter();
        Point2D pA = tv.getCoordsA();
        Point2D pB = tv.getCoordsB();
        Point2D pC = tv.getCoordsC();
        Point2D pD = tv.getCoordsD();

        int state = LayoutTurnout.UNKNOWN; // 1
        if (d.getModels().isAnimating()) {
            state = tv.getState(); // turnout closed: 2, turnout thrown: 4
        }
        if ((state != jmri.Turnout.CLOSED) && (state != jmri.Turnout.THROWN)) {
            log.info("have to stop - state: {}", state); // state UNKNOWN
            result = false;
        }

        d.nextLayoutTrack = null;

        switch (tv.getTurnoutType()) {
            case RH_TURNOUT:
            case LH_TURNOUT:
            case WYE_TURNOUT: {
                Point2D pStart = null;
                Point2D pEnd = null;

                if (tv.getConnectA().equals(d.getLastTrack())) {
                    pStart = pA;
                    if (state == jmri.Turnout.CLOSED) {
                        pEnd = pB;
                        d.nextLayoutTrack = tv.getConnectB();
                    } else if (state == jmri.Turnout.THROWN) {
                        pEnd = pC;
                        d.nextLayoutTrack = tv.getConnectC();
                    }
                } else if (tv.getConnectB().equals(d.getLastTrack())) {
                    if (state == jmri.Turnout.CLOSED) {
                        pStart = pB;
                        pEnd = pA;
                        d.nextLayoutTrack = tv.getConnectA();
                    }
                } else if (tv.getConnectC().equals(d.getLastTrack())) {
                    if (state == jmri.Turnout.THROWN) {
                        pStart = pC;
                        pEnd = pA;
                        d.nextLayoutTrack = tv.getConnectA();
                    }
                } else { // OOPS! we're lost!
                    result = false;
                }
                if (d.nextLayoutTrack != null) {
                    d.setReturnLastTrack(d.nextLayoutTrack);
                    d.setReturnTrack(d.getLayoutTrack());
                    d.setDistance(0);
                }

                if (pStart != null) {
                    double distanceStart = MathUtil.distance(pStart, pM);
                    d.setReturnDistance(distanceStart);
                    if (distanceOnTrack < distanceStart) { // it's on startleg
                        double ratio = distanceOnTrack / distanceStart;
                        d.setLocation(MathUtil.lerp(pStart, pM, ratio));
                        d.setDirectionRAD((Math.PI / 2) - MathUtil.computeAngleRAD(pM, pStart));
                        d.setDistance(0);
                    } else if (pEnd != null) { // it's not on startleg
                        double distanceEnd = MathUtil.distance(pM, pEnd);
                        d.setReturnDistance(distanceEnd);
                        if ((distanceOnTrack - distanceStart) < distanceEnd) { // it's on end leg
                            double ratio = (distanceOnTrack - distanceStart) / distanceEnd;
                            d.setLocation(MathUtil.lerp(pM, pEnd, ratio));
                            d.setDirectionRAD((Math.PI / 2) - MathUtil.computeAngleRAD(pEnd, pM));
                            d.setDistance(0);
                        } else { // it's not on end leg / this track
                            d.setDistance(distanceOnTrack - (distanceStart + distanceEnd));
                            distanceOnTrack = 0;
                            result = true;
                        }
                    } else { // OOPS! we're lost!
                        log.info(" Turnout has unknown state");
                        result = false;
                        distanceOnTrack = distanceStart;
                        d.setDistance(0);
                        d.setReturnDistance(0);
                        d.setReturnTrack(d.getLastTrack());
                    }
                } else { // OOPS! we're lost!
                    log.info(" Turnout caused a stop"); // correct position or change direction
                    result = false;
                    distanceOnTrack = 0;
                    d.setDistance(0);
                    d.setReturnDistance(0);
                    d.setReturnTrack(d.getLastTrack());
                }
                break;
            }

            case RH_XOVER:
            case LH_XOVER:
            case DOUBLE_XOVER: {
                List<Point2D> points = new ArrayList<>();

                // middles
                Point2D pABM = MathUtil.midPoint(pA, pB);
                Point2D pAM = pABM, pBM = pABM;

                Point2D pCDM = MathUtil.midPoint(pC, pD);
                Point2D pCM = pCDM, pDM = pCDM;

                if (tv.getTurnoutType() == LayoutTurnout.TurnoutType.DOUBLE_XOVER) {
                    pAM = MathUtil.lerp(pA, pABM, 5.0 / 8.0);
                    pBM = MathUtil.lerp(pB, pABM, 5.0 / 8.0);
                    pCM = MathUtil.lerp(pC, pCDM, 5.0 / 8.0);
                    pDM = MathUtil.lerp(pD, pCDM, 5.0 / 8.0);
                }

                if (tv.getConnectA().equals(d.getLastTrack())) {
                    if (state == jmri.Turnout.CLOSED) {
                        points.add(pA);
                        points.add(pB);
                        d.nextLayoutTrack = tv.getConnectB();
                    } else if ((tv.getTurnoutType() != LayoutTurnout.TurnoutType.LH_XOVER) && (state == jmri.Turnout.THROWN)) {
                        points.add(pA);
                        points.add(pAM);
                        points.add(pCM);
                        points.add(pC);
                        d.nextLayoutTrack = tv.getConnectC();
                    }
                } else if (tv.getConnectB().equals(d.getLastTrack())) {
                    if (state == jmri.Turnout.CLOSED) {
                        points.add(pB);
                        points.add(pA);
                        d.nextLayoutTrack = tv.getConnectA();
                    } else if ((tv.getTurnoutType() != LayoutTurnout.TurnoutType.RH_XOVER) && (state == jmri.Turnout.THROWN)) {
                        points.add(pB);
                        points.add(pBM);
                        points.add(pDM);
                        points.add(pD);
                        d.nextLayoutTrack = tv.getConnectD();
                    }
                } else if (tv.getConnectC().equals(d.getLastTrack())) {
                    if (state == jmri.Turnout.CLOSED) {
                        points.add(pC);
                        points.add(pD);
                        d.nextLayoutTrack = tv.getConnectD();
                    } else if ((tv.getTurnoutType() != LayoutTurnout.TurnoutType.LH_XOVER) && (state == jmri.Turnout.THROWN)) {
                        points.add(pC);
                        points.add(pCM);
                        points.add(pAM);
                        points.add(pA);
                        d.nextLayoutTrack = tv.getConnectA();
                    }
                } else if (tv.getConnectD().equals(d.getLastTrack())) {
                    if (state == jmri.Turnout.CLOSED) {
                        points.add(pD);
                        points.add(pC);
                        d.nextLayoutTrack = tv.getConnectC();
                    } else if ((tv.getTurnoutType() != LayoutTurnout.TurnoutType.RH_XOVER) && (state == jmri.Turnout.THROWN)) {
                        points.add(pD);
                        points.add(pDM);
                        points.add(pBM);
                        points.add(pB);
                        d.nextLayoutTrack = tv.getConnectB();
                    }
                } else { // OOPS! we're lost!
                    result = false;
                }

                if (d.nextLayoutTrack != null) {
                    d.setReturnLastTrack(d.nextLayoutTrack);
                    d.setReturnTrack(d.getLayoutTrack());
                }
                return navigate(points, d.nextLayoutTrack);
            }

            case SINGLE_SLIP:
            case DOUBLE_SLIP: {
                log.warn("TurnoutView {}.navigate(...); slips should be being handled by LayoutSlip sub-class", tv.getName());
                break;
            }
            default: { // OOPS! we're lost!
                result = false;
                break;
            }
        }
        d.distanceOnTrack = distanceOnTrack;

        if (result) { // not on this track
            // go to next track
            LayoutTrack last = d.getLayoutTrack();
            if (d.nextLayoutTrack != null) {
                d.setLayoutTrack(d.nextLayoutTrack);
            } else { // OOPS! we're lost!
                result = false;
            }
            if (result) {
                d.setLastTrack(last);
                d.setReturnTrack(d.getLayoutTrack());
                d.setReturnLastTrack(d.getLayoutTrack());
            }
        }
        return result;
    }

    // NOTE: LayoutSlip uses the checkForNonContiguousBlocks
    //      and collectContiguousTracksNamesInBlockNamed methods
    //      inherited from LayoutTurnout
    boolean navigateLayoutSlip() {
        if (use_blocks && ((LayoutSlip) d.getLayoutTrack()).getLayoutBlock().getBlock() != VSDecoderManager.instance().currentBlock.get(d)) {
            // we are not in the block
            d.setDistance(0);
            return false;
        }

        boolean result = true; // assume success (optimist!)

        LayoutSlipView ltv = d.getModels().getLayoutSlipView((LayoutSlip) d.getLayoutTrack());

        Point2D pA = ltv.getCoordsA();
        Point2D pB = ltv.getCoordsB();
        Point2D pC = ltv.getCoordsC();
        Point2D pD = ltv.getCoordsD();

        d.nextLayoutTrack = null;

        List<Point2D> points = new ArrayList<>();

        // thirds
        double third = 1.0 / 3.0;
        Point2D pACT = MathUtil.lerp(pA, pC, third);
        Point2D pBDT = MathUtil.lerp(pB, pD, third);
        Point2D pCAT = MathUtil.lerp(pC, pA, third);
        Point2D pDBT = MathUtil.lerp(pD, pB, third);

        int slipState = ltv.getSlipState();

        boolean slip_lost = false;

        if (ltv.getConnectA().equals(d.getLastTrack())) {
            if (slipState == LayoutTurnout.STATE_AC) {
                points.add(pA);
                points.add(pC);
                d.nextLayoutTrack = ltv.getConnectC();
            } else if (slipState == LayoutTurnout.STATE_AD) {
                points.add(pA);
                points.add(pACT);
                points.add(pDBT);
                points.add(pD);
                d.nextLayoutTrack = ltv.getConnectD();
            } else { // OOPS! we're lost!
                result = false;
                slip_lost = true;
            }
        } else if (ltv.getConnectB().equals(d.getLastTrack())) {
            if (slipState == LayoutTurnout.STATE_BD) {
                points.add(pB);
                points.add(pD);
                d.nextLayoutTrack = ltv.getConnectD();
            } else if (slipState == LayoutTurnout.STATE_BC) {
                points.add(pB);
                points.add(pBDT);
                points.add(pCAT);
                points.add(pC);
                d.nextLayoutTrack = ltv.getConnectC();
            } else { // OOPS! we're lost!
                result = false;
                slip_lost = true;
            }
        } else if (ltv.getConnectC().equals(d.getLastTrack())) {
            if (slipState == LayoutTurnout.STATE_AC) {
                points.add(pC);
                points.add(pA);
                d.nextLayoutTrack = ltv.getConnectA();
            } else if (slipState == LayoutTurnout.STATE_BC) {
                points.add(pC);
                points.add(pCAT);
                points.add(pBDT);
                points.add(pB);
                d.nextLayoutTrack = ltv.getConnectB();
            } else { // OOPS! we're lost!
                result = false;
                slip_lost = true;
            }
        } else if (ltv.getConnectD().equals(d.getLastTrack())) {
            if (slipState == LayoutTurnout.STATE_BD) {
                points.add(pD);
                points.add(pB);
                d.nextLayoutTrack = ltv.getConnectB();
            } else if (slipState == LayoutTurnout.STATE_AD) {
                points.add(pD);
                points.add(pDBT);
                points.add(pACT);
                points.add(pA);
                d.nextLayoutTrack = ltv.getConnectA();
            } else { // OOPS! we're lost!
                result = false;
                slip_lost = true;
            }
        } else { // OOPS! we're lost!
            result = false;
        }
        if (d.nextLayoutTrack != null) {
            d.setReturnLastTrack(d.nextLayoutTrack);
            d.setReturnTrack(d.getLayoutTrack());
        }
        if (slip_lost) {
            log.info(" Turnout state not good");
            d.setDistance(0);
            d.setReturnDistance(0);
        }

        if (result) {
            result = navigate(points, d.nextLayoutTrack);
        }
        return result;
    }

    boolean navigateLevelXing() {
        boolean result = false;
        jmri.Block block2 = null;
        LevelXing lx = (LevelXing) d.getLayoutTrack();
        if (lx.getConnectA().equals(d.getLastTrack()) || lx.getConnectC().equals(d.getLastTrack())) {
            block2 = lx.getLayoutBlockAC().getBlock();
        } else if (lx.getConnectB().equals(d.getLastTrack()) || lx.getConnectD().equals(d.getLastTrack())) {
            block2 = lx.getLayoutBlockBD().getBlock();
        }
        if (use_blocks && block2 != VSDecoderManager.instance().currentBlock.get(d)) {
            // not in the block (blocks do not match)
            d.setDistance(0);
            return result;
        }

        double distanceOnTrack = d.getDistance() + d.distanceOnTrack;

        LevelXingView lxv = d.getModels().getLevelXingView((LevelXing) d.getLayoutTrack());
        Point2D pA = lxv.getCoordsA();
        Point2D pB = lxv.getCoordsB();
        Point2D pC = lxv.getCoordsC();
        Point2D pD = lxv.getCoordsD();
        Point2D p1 = null;
        Point2D p2 = null;

        d.nextLayoutTrack = null;

        if (lxv.getConnectA().equals(d.getLastTrack())) {
            p1 = pA;
            p2 = pC;
            d.nextLayoutTrack = lxv.getConnectC();
        } else if (lxv.getConnectB().equals(d.getLastTrack())) {
            p1 = pB;
            p2 = pD;
            d.nextLayoutTrack = lxv.getConnectD();
        } else if (lxv.getConnectC().equals(d.getLastTrack())) {
            p1 = pC;
            p2 = pA;
            d.nextLayoutTrack = lxv.getConnectA();
        } else if (lxv.getConnectD().equals(d.getLastTrack())) {
            p1 = pD;
            p2 = pB;
            d.nextLayoutTrack = lxv.getConnectB();
            result = false;
        }
        if (d.nextLayoutTrack != null) {
            d.setReturnLastTrack(d.nextLayoutTrack);
            d.setReturnTrack(d.getLayoutTrack());
        }

        if (p1 != null) {
            double distance = MathUtil.distance(p1, p2);
            d.setReturnDistance(distance);
            if (distanceOnTrack < distance) {
                // it's on this track
                double ratio = distanceOnTrack / distance;
                d.setLocation(MathUtil.lerp(p1, p2, ratio));
                d.setDirectionRAD((Math.PI / 2) - MathUtil.computeAngleRAD(p2, p1));
                d.setDistance(0);
            } else { // it's not on this track
                d.setDistance(distanceOnTrack - distance);
                distanceOnTrack = 0;
                result = true;
            }
            d.distanceOnTrack = distanceOnTrack;
        }

        if (result) { // not on this track
            // go to next track
            LayoutTrack last = d.getLayoutTrack();
            if (d.nextLayoutTrack != null) {
                d.setLayoutTrack(d.nextLayoutTrack);
            } else { // OOPS! we're lost!
                result = false;
            }
            if (result) {
                d.setLastTrack(last);
                d.setReturnTrack(d.getLayoutTrack());
                d.setReturnLastTrack(d.getLayoutTrack());
            }
        }
        return result;
    }

    private boolean navigate(List<Point2D> points, @Nullable LayoutTrack nextLayoutTrack) {
        boolean result = false;
        double distanceOnTrack = d.getDistance() + d.distanceOnTrack;
        boolean nextLegFlag = true;
        Point2D lastPoint = null;
        double trackDistance = 0;
        for (Point2D p : points) {
            if (lastPoint != null) {
                double distance = MathUtil.distance(lastPoint, p);
                trackDistance += distance;
                if (distanceOnTrack < trackDistance) { // it's on this leg
                    d.setLocation(MathUtil.lerp(p, lastPoint, (trackDistance - distanceOnTrack) / distance));
                    d.setDirectionRAD((Math.PI / 2) - MathUtil.computeAngleRAD(p, lastPoint));
                    nextLegFlag = false;
                    break;
                }
            }
            lastPoint = p;
        }
        if (nextLegFlag) { // it's not on this track
            d.setDistance(distanceOnTrack - trackDistance);
            distanceOnTrack = 0;
            result = true;
        } else { // it's on this track
            d.setDistance(0);
        }
        d.distanceOnTrack = distanceOnTrack;
        if (result) { // not on this track
            // go to next track
            LayoutTrack last = d.getLayoutTrack();
            if (nextLayoutTrack != null) {
                d.setLayoutTrack(nextLayoutTrack);
            } else { // OOPS! we're lost!
                result = false;
            }
            if (result) {
                d.setLastTrack(last);
                d.setReturnTrack(d.getLayoutTrack());
                d.setReturnLastTrack(d.getLayoutTrack());
            }
        }
        return result;
    }

    private static final Logger log = LoggerFactory.getLogger(VSDNavigation.class);

}
