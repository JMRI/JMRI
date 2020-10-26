package jmri.jmrit.display.layoutEditor;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.*;

import javax.annotation.*;

import jmri.Turnout;
import static jmri.Turnout.CLOSED;
import static jmri.Turnout.THROWN;
import static jmri.jmrit.display.layoutEditor.LayoutTurnout.STATE_AC;
import static jmri.jmrit.display.layoutEditor.LayoutTurnout.STATE_AD;
import static jmri.jmrit.display.layoutEditor.LayoutTurnout.STATE_BC;
import static jmri.jmrit.display.layoutEditor.LayoutTurnout.STATE_BD;
import static jmri.jmrit.display.layoutEditor.LayoutTurnout.UNKNOWN;
import jmri.util.*;

/**
 * MVC View component for the LayoutSlip class.
 *
 * @author Bob Jacobsen Copyright (c) 2020
 *
 */
public class LayoutSlipView extends LayoutTurnoutView {

    /**
     * Constructor method.
     *
     * @param slip the layout sip to create view for.
     */
    public LayoutSlipView(@Nonnull LayoutSlip slip) {
        super(slip);
        this.slip = slip;
    }

    final private LayoutSlip slip;

    @Override
    protected void draw1(Graphics2D g2, boolean drawMain, boolean isBlock) {
        Point2D pA = slip.getCoordsA();
        Point2D pB = slip.getCoordsB();
        Point2D pC = slip.getCoordsC();
        Point2D pD = slip.getCoordsD();

        boolean mainlineA = isMainlineA();
        boolean mainlineB = isMainlineB();
        boolean mainlineC = isMainlineC();
        boolean mainlineD = isMainlineD();

        boolean drawUnselectedLeg = slip.layoutEditor.isTurnoutDrawUnselectedLeg()
                || ((LayoutSlip) turnout).isTurnoutInconsistent();

        int slipState = ((LayoutSlip) turnout).getSlipState();

        Color color = g2.getColor();

        // if this isn't a block line all these will be the same color
        Color colorA = color, colorB = color, colorC = color, colorD = color;

        if (isBlock) {
            LayoutBlock layoutBlockA = slip.getLayoutBlock();
            colorA = (layoutBlockA != null) ? layoutBlockA.getBlockTrackColor() : color;
            LayoutBlock layoutBlockB = slip.getLayoutBlockB();
            colorB = (layoutBlockB != null) ? layoutBlockB.getBlockTrackColor() : color;
            LayoutBlock layoutBlockC = slip.getLayoutBlockC();
            colorC = (layoutBlockC != null) ? layoutBlockC.getBlockTrackColor() : color;
            LayoutBlock layoutBlockD = slip.getLayoutBlockD();
            colorD = (layoutBlockD != null) ? layoutBlockD.getBlockTrackColor() : color;

            if (slipState == STATE_AC) {
                colorA = (layoutBlockA != null) ? layoutBlockA.getBlockColor() : color;
                colorC = (layoutBlockC != null) ? layoutBlockC.getBlockColor() : color;
            } else if (slipState == STATE_BD) {
                colorB = (layoutBlockB != null) ? layoutBlockB.getBlockColor() : color;
                colorD = (layoutBlockD != null) ? layoutBlockD.getBlockColor() : color;
            } else if (slipState == STATE_AD) {
                colorA = (layoutBlockA != null) ? layoutBlockA.getBlockColor() : color;
                colorD = (layoutBlockD != null) ? layoutBlockD.getBlockColor() : color;
            } else if (slipState == STATE_BC) {
                colorB = (layoutBlockB != null) ? layoutBlockB.getBlockColor() : color;
                colorC = (layoutBlockC != null) ? layoutBlockC.getBlockColor() : color;
            }
        }
        Point2D oneForthPointAC = MathUtil.oneFourthPoint(pA, pC);
        Point2D oneThirdPointAC = MathUtil.oneThirdPoint(pA, pC);
        Point2D midPointAC = MathUtil.midPoint(pA, pC);
        Point2D twoThirdsPointAC = MathUtil.twoThirdsPoint(pA, pC);
        Point2D threeFourthsPointAC = MathUtil.threeFourthsPoint(pA, pC);

        Point2D oneForthPointBD = MathUtil.oneFourthPoint(pB, pD);
        Point2D oneThirdPointBD = MathUtil.oneThirdPoint(pB, pD);
        Point2D midPointBD = MathUtil.midPoint(pB, pD);
        Point2D twoThirdsPointBD = MathUtil.twoThirdsPoint(pB, pD);
        Point2D threeFourthsPointBD = MathUtil.threeFourthsPoint(pB, pD);

        Point2D midPointAD = MathUtil.midPoint(oneThirdPointAC, twoThirdsPointBD);
        Point2D midPointBC = MathUtil.midPoint(oneThirdPointBD, twoThirdsPointAC);

        if (slipState == STATE_AD) {
            // draw A<===>D
            if (drawMain == mainlineA) {
                g2.setColor(colorA);
                g2.draw(new Line2D.Double(pA, oneThirdPointAC));
                g2.draw(new Line2D.Double(oneThirdPointAC, midPointAD));
            }
            if (drawMain == mainlineD) {
                g2.setColor(colorD);
                g2.draw(new Line2D.Double(midPointAD, twoThirdsPointBD));
                g2.draw(new Line2D.Double(twoThirdsPointBD, pD));
            }
        } else if (slipState == STATE_AC) {
            // draw A<===>C
            if (drawMain == mainlineA) {
                g2.setColor(colorA);
                g2.draw(new Line2D.Double(pA, oneThirdPointAC));
                g2.draw(new Line2D.Double(oneThirdPointAC, midPointAC));
            }
            if (drawMain == mainlineC) {
                g2.setColor(colorC);
                g2.draw(new Line2D.Double(midPointAC, twoThirdsPointAC));
                g2.draw(new Line2D.Double(twoThirdsPointAC, pC));
            }
        } else if (slipState == STATE_BD) {
            // draw B<===>D
            if (drawMain == mainlineB) {
                g2.setColor(colorB);
                g2.draw(new Line2D.Double(pB, oneThirdPointBD));
                g2.draw(new Line2D.Double(oneThirdPointBD, midPointBD));
            }
            if (drawMain == mainlineD) {
                g2.setColor(colorD);
                g2.draw(new Line2D.Double(midPointBD, twoThirdsPointBD));
                g2.draw(new Line2D.Double(twoThirdsPointBD, pD));
            }
        } else if ((turnout instanceof LayoutDoubleSlip) && (slipState == STATE_BC)) {
            // draw B<===>C
            if (drawMain == mainlineB) {
                g2.setColor(colorB);
                g2.draw(new Line2D.Double(pB, oneThirdPointBD));
                g2.draw(new Line2D.Double(oneThirdPointBD, midPointBC));
            }
            if (drawMain == mainlineC) {
                g2.setColor(colorC);
                g2.draw(new Line2D.Double(midPointBC, twoThirdsPointAC));
                g2.draw(new Line2D.Double(twoThirdsPointAC, pC));
            }
        }

        if (!isBlock || drawUnselectedLeg) {
            if (slipState == STATE_AC) {
                if (drawMain == mainlineB) {
                    g2.setColor(colorB);
                    g2.draw(new Line2D.Double(pB, oneForthPointBD));
                }
                if (drawMain == mainlineD) {
                    g2.setColor(colorD);
                    g2.draw(new Line2D.Double(threeFourthsPointBD, pD));
                }
            } else if (slipState == STATE_BD) {
                if (drawMain == mainlineA) {
                    g2.setColor(colorA);
                    g2.draw(new Line2D.Double(pA, oneForthPointAC));
                }
                if (drawMain == mainlineC) {
                    g2.setColor(colorC);
                    g2.draw(new Line2D.Double(threeFourthsPointAC, pC));
                }
            } else if (slipState == STATE_AD) {
                if (drawMain == mainlineB) {
                    g2.setColor(colorB);
                    g2.draw(new Line2D.Double(pB, oneForthPointBD));
                }
                if (drawMain == mainlineC) {
                    g2.setColor(colorC);
                    g2.draw(new Line2D.Double(threeFourthsPointAC, pC));
                }
            } else if (slipState == STATE_BC) {
                if (drawMain == mainlineA) {
                    g2.setColor(colorA);
                    g2.draw(new Line2D.Double(pA, oneForthPointAC));
                }
                if (drawMain == mainlineD) {
                    g2.setColor(colorD);
                    g2.draw(new Line2D.Double(threeFourthsPointBD, pD));
                }
            } else {
                if (drawMain == mainlineA) {
                    g2.setColor(colorA);
                    g2.draw(new Line2D.Double(pA, oneForthPointAC));
                }
                if (drawMain == mainlineB) {
                    g2.setColor(colorB);
                    g2.draw(new Line2D.Double(pB, oneForthPointBD));
                }
                if (drawMain == mainlineC) {
                    g2.setColor(colorC);
                    g2.draw(new Line2D.Double(threeFourthsPointAC, pC));
                }
                if (drawMain == mainlineD) {
                    g2.setColor(colorD);
                    g2.draw(new Line2D.Double(threeFourthsPointBD, pD));
                }
            }
        }
    }   // draw1

    /**
     * {@inheritDoc}
     */
    @Override
    protected void draw2(Graphics2D g2, boolean drawMain, float railDisplacement) {
        Point2D pA = slip.getCoordsA();
        Point2D pB = slip.getCoordsB();
        Point2D pC = slip.getCoordsC();
        Point2D pD = slip.getCoordsD();
        Point2D pM = getCoordsCenter();

        //boolean drawUnselectedLeg = slip.layoutEditor.isTurnoutDrawUnselectedLeg()
        //        || ((LayoutSlip) turnout).isTurnoutInconsistent();

        Point2D vAC = MathUtil.normalize(MathUtil.subtract(pC, pA), railDisplacement);
        double dirAC_DEG = MathUtil.computeAngleDEG(pA, pC);
        Point2D vACo = MathUtil.orthogonal(vAC);
        Point2D pAL = MathUtil.subtract(pA, vACo);
        Point2D pAR = MathUtil.add(pA, vACo);
        Point2D pCL = MathUtil.subtract(pC, vACo);
        Point2D pCR = MathUtil.add(pC, vACo);

        Point2D vBD = MathUtil.normalize(MathUtil.subtract(pD, pB), railDisplacement);
        double dirBD_DEG = MathUtil.computeAngleDEG(pB, pD);
        Point2D vBDo = MathUtil.orthogonal(vBD);
        Point2D pBL = MathUtil.subtract(pB, vBDo);
        Point2D pBR = MathUtil.add(pB, vBDo);
        Point2D pDL = MathUtil.subtract(pD, vBDo);
        Point2D pDR = MathUtil.add(pD, vBDo);

        double deltaDEG = MathUtil.absDiffAngleDEG(dirAC_DEG, dirBD_DEG);
        double deltaRAD = Math.toRadians(deltaDEG);

        double hypotV = railDisplacement / Math.cos((Math.PI - deltaRAD) / 2.0);
        double hypotK = railDisplacement / Math.cos(deltaRAD / 2.0);

        log.debug("dir AC: {}, BD: {}, diff: {}", dirAC_DEG, dirBD_DEG, deltaDEG);

        Point2D vDisK = MathUtil.normalize(MathUtil.subtract(vAC, vBD), hypotK);
        Point2D vDisV = MathUtil.normalize(MathUtil.orthogonal(vDisK), hypotV);
        Point2D pKL = MathUtil.subtract(pM, vDisK);
        Point2D pKR = MathUtil.add(pM, vDisK);
        Point2D pVL = MathUtil.add(pM, vDisV);
        Point2D pVR = MathUtil.subtract(pM, vDisV);

        // this is the vector (rail gaps) for the diamond parts
        double railGap = 2.0 / Math.sin(deltaRAD);
        Point2D vAC2 = MathUtil.normalize(vAC, railGap);
        Point2D vBD2 = MathUtil.normalize(vBD, railGap);
        // KR and VR toward A, KL and VL toward C
        Point2D pKRtA = MathUtil.subtract(pKR, vAC2);
        Point2D pVRtA = MathUtil.subtract(pVR, vAC2);
        Point2D pKLtC = MathUtil.add(pKL, vAC2);
        Point2D pVLtC = MathUtil.add(pVL, vAC2);

        // VR and KL toward B, KR and VL toward D
        Point2D pVRtB = MathUtil.subtract(pVR, vBD2);
        Point2D pKLtB = MathUtil.subtract(pKL, vBD2);
        Point2D pKRtD = MathUtil.add(pKR, vBD2);
        Point2D pVLtD = MathUtil.add(pVL, vBD2);

        // outer (closed) switch points
        Point2D pAPL = MathUtil.add(pAL, MathUtil.subtract(pVL, pAR));
        Point2D pBPR = MathUtil.add(pBR, MathUtil.subtract(pVL, pBL));
        Point2D pCPR = MathUtil.add(pCR, MathUtil.subtract(pVR, pCL));
        Point2D pDPL = MathUtil.add(pDL, MathUtil.subtract(pVR, pDR));

        // this is the vector (rail gaps) for the inner (open) switch points
        Point2D vACo2 = MathUtil.normalize(vACo, 2.0);
        Point2D vBDo2 = MathUtil.normalize(vBDo, 2.0);
        Point2D pASL = MathUtil.add(pAPL, vACo2);
        Point2D pBSR = MathUtil.subtract(pBPR, vBDo2);
        Point2D pCSR = MathUtil.subtract(pCPR, vACo2);
        Point2D pDSL = MathUtil.add(pDPL, vBDo2);

        Point2D pVLP = MathUtil.add(pVLtD, vAC2);
        Point2D pVRP = MathUtil.subtract(pVRtA, vBD2);

        Point2D pKLH = MathUtil.midPoint(pM, pKL);
        Point2D pKRH = MathUtil.midPoint(pM, pKR);

        boolean mainlineA = isMainlineA();
        boolean mainlineB = isMainlineB();
        boolean mainlineC = isMainlineC();
        boolean mainlineD = isMainlineD();

        if (drawMain == mainlineA) {
            g2.draw(new Line2D.Double(pAR, pVL));
            g2.draw(new Line2D.Double(pVLtD, pKLtB));
            GeneralPath path = new GeneralPath();
            path.moveTo(pAL.getX(), pAL.getY());
            path.lineTo(pAPL.getX(), pAPL.getY());
            path.quadTo(pKL.getX(), pKL.getY(), pDPL.getX(), pDPL.getY());
            g2.draw(path);
        }
        if (drawMain == mainlineB) {
            g2.draw(new Line2D.Double(pBL, pVL));
            g2.draw(new Line2D.Double(pVLtC, pKRtA));
            if (turnout instanceof LayoutDoubleSlip) {
                GeneralPath path = new GeneralPath();
                path.moveTo(pBR.getX(), pBR.getY());
                path.lineTo(pBPR.getX(), pBPR.getY());
                path.quadTo(pKR.getX(), pKR.getY(), pCPR.getX(), pCPR.getY());
                g2.draw(path);
            } else {
                g2.draw(new Line2D.Double(pBR, pKR));
            }
        }
        if (drawMain == mainlineC) {
            g2.draw(new Line2D.Double(pCL, pVR));
            g2.draw(new Line2D.Double(pVRtB, pKRtD));
            if (turnout instanceof LayoutDoubleSlip) {
                GeneralPath path = new GeneralPath();
                path.moveTo(pCR.getX(), pCR.getY());
                path.lineTo(pCPR.getX(), pCPR.getY());
                path.quadTo(pKR.getX(), pKR.getY(), pBPR.getX(), pBPR.getY());
                g2.draw(path);
            } else {
                g2.draw(new Line2D.Double(pCR, pKR));
            }
        }
        if (drawMain == mainlineD) {
            g2.draw(new Line2D.Double(pDR, pVR));
            g2.draw(new Line2D.Double(pVRtA, pKLtC));
            GeneralPath path = new GeneralPath();
            path.moveTo(pDL.getX(), pDL.getY());
            path.lineTo(pDPL.getX(), pDPL.getY());
            path.quadTo(pKL.getX(), pKL.getY(), pAPL.getX(), pAPL.getY());
            g2.draw(path);
        }

        int slipState = ((LayoutSlip) turnout).getSlipState();
        if (slipState == STATE_AD) {
            if (drawMain == mainlineA) {
                g2.draw(new Line2D.Double(pASL, pKL));
                g2.draw(new Line2D.Double(pVLP, pKLH));
            }
            if (drawMain == mainlineB) {
                g2.draw(new Line2D.Double(pBPR, pKR));
                g2.draw(new Line2D.Double(pVLtC, pKRH));
            }
            if (drawMain == mainlineC) {
                g2.draw(new Line2D.Double(pCPR, pKR));
                g2.draw(new Line2D.Double(pVRtB, pKRH));
            }
            if (drawMain == mainlineD) {
                g2.draw(new Line2D.Double(pDSL, pKL));
                g2.draw(new Line2D.Double(pVRP, pKLH));
            }
        } else if (slipState == STATE_AC) {
            if (drawMain == mainlineA) {
                g2.draw(new Line2D.Double(pAPL, pKL));
                g2.draw(new Line2D.Double(pVLtD, pKLH));
            }
            if (drawMain == mainlineB) {
                g2.draw(new Line2D.Double(pBSR, pKR));
                g2.draw(new Line2D.Double(pVLP, pKRH));
            }
            if (drawMain == mainlineC) {
                g2.draw(new Line2D.Double(pCPR, pKR));
                g2.draw(new Line2D.Double(pVRtB, pKRH));
            }
            if (drawMain == mainlineD) {
                g2.draw(new Line2D.Double(pDSL, pKL));
                g2.draw(new Line2D.Double(pVRP, pKLH));
            }
        } else if (slipState == STATE_BD) {
            if (drawMain == mainlineA) {
                g2.draw(new Line2D.Double(pASL, pKL));
                g2.draw(new Line2D.Double(pVLP, pKLH));
            }
            if (drawMain == mainlineB) {
                g2.draw(new Line2D.Double(pBPR, pKR));
                g2.draw(new Line2D.Double(pVLtC, pKRH));
            }
            if (drawMain == mainlineC) {
                g2.draw(new Line2D.Double(pCSR, pKR));
                g2.draw(new Line2D.Double(pVRP, pKRH));
            }
            if (drawMain == mainlineD) {
                g2.draw(new Line2D.Double(pDPL, pKL));
                g2.draw(new Line2D.Double(pVRtA, pKLH));
            }
        } else if ((turnout instanceof LayoutDoubleSlip)
                && (slipState == STATE_BC)) {
            if (drawMain == mainlineA) {
                g2.draw(new Line2D.Double(pAPL, pKL));
                g2.draw(new Line2D.Double(pVLtD, pKLH));
            }
            if (drawMain == mainlineB) {
                g2.draw(new Line2D.Double(pBSR, pKR));
                g2.draw(new Line2D.Double(pVLP, pKRH));
            }
            if (drawMain == mainlineC) {
                g2.draw(new Line2D.Double(pCSR, pKR));
                g2.draw(new Line2D.Double(pVRP, pKRH));
            }
            if (drawMain == mainlineD) {
                g2.draw(new Line2D.Double(pDPL, pKL));
                g2.draw(new Line2D.Double(pVRtA, pKLH));
            }
        }
        //else if (drawUnselectedLeg) {
        //    if (drawMain == mainlineA) {
        //        g2.draw(new Line2D.Double(pAPL, pKL));
        //        g2.draw(new Line2D.Double(pVLtD, pKLH));
        //    }
        //    if (drawMain == mainlineB) {
        //        g2.draw(new Line2D.Double(pBPR, pKR));
        //        g2.draw(new Line2D.Double(pVLtC, pKRH));
        //    }
        //    if (drawMain == mainlineC) {
        //        g2.draw(new Line2D.Double(pCPR, pKR));
        //        g2.draw(new Line2D.Double(pVRtB, pKRH));
        //    }
        //    if (drawMain == mainlineD) {
        //        g2.draw(new Line2D.Double(pDPL, pKL));
        //        g2.draw(new Line2D.Double(pVRtA, pKLH));
        //    }
        //}
    }   // draw2

    /**
     * {@inheritDoc}
     */
    @Override
    protected void highlightUnconnected(Graphics2D g2, HitPointType specificType) {
        if (((specificType == HitPointType.NONE) || (specificType == HitPointType.SLIP_A))
                && (slip.getConnectA() == null)) {
            g2.fill(slip.trackControlCircleAt(getCoordsA()));
        }

        if (((specificType == HitPointType.NONE) || (specificType == HitPointType.SLIP_B))
                && (slip.getConnectB() == null)) {
            g2.fill(slip.trackControlCircleAt(getCoordsB()));
        }

        if (((specificType == HitPointType.NONE) || (specificType == HitPointType.SLIP_C))
                && (slip.getConnectC() == null)) {
            g2.fill(slip.trackControlCircleAt(getCoordsC()));
        }

        if (((specificType == HitPointType.NONE) || (specificType == HitPointType.SLIP_D))
                && (slip.getConnectD() == null)) {
            g2.fill(slip.trackControlCircleAt(getCoordsD()));
        }
    }

    @Override
    protected void drawTurnoutControls(Graphics2D g2) {
        if (!slip.disabled && !(slip.disableWhenOccupied && slip.isOccupied())) {
            // TODO: query user base if this is "acceptable" (can obstruct state)
            if (false) {
                int stateA = UNKNOWN;
                Turnout toA = slip.getTurnout();
                if (toA != null) {
                    stateA = toA.getKnownState();
                }

                Color foregroundColor = g2.getColor();
                Color backgroundColor = g2.getBackground();

                if (stateA == THROWN) {
                    g2.setColor(backgroundColor);
                } else if (stateA != CLOSED) {
                    g2.setColor(Color.GRAY);
                }
                Point2D rightCircleCenter = slip.getCoordsRight();
                if (slip.layoutEditor.isTurnoutFillControlCircles()) {
                    g2.fill(slip.trackControlCircleAt(rightCircleCenter));
                } else {
                    g2.draw(slip.trackControlCircleAt(rightCircleCenter));
                }
                if (stateA != CLOSED) {
                    g2.setColor(foregroundColor);
                }

                int stateB = UNKNOWN;
                Turnout toB = slip.getTurnoutB();
                if (toB != null) {
                    stateB = toB.getKnownState();
                }

                if (stateB == THROWN) {
                    g2.setColor(backgroundColor);
                } else if (stateB != CLOSED) {
                    g2.setColor(Color.GRAY);
                }
                // drawHidden left/right turnout control circles
                Point2D leftCircleCenter = slip.getCoordsLeft();
                if (slip.layoutEditor.isTurnoutFillControlCircles()) {
                    g2.fill(slip.trackControlCircleAt(leftCircleCenter));
                } else {
                    g2.draw(slip.trackControlCircleAt(leftCircleCenter));
                }
                if (stateB != CLOSED) {
                    g2.setColor(foregroundColor);
                }
            } else {
                Point2D rightCircleCenter = slip.getCoordsRight();
                g2.draw(slip.trackControlCircleAt(rightCircleCenter));
                Point2D leftCircleCenter = slip.getCoordsLeft();
                g2.draw(slip.trackControlCircleAt(leftCircleCenter));
            }
        }
    } // drawTurnoutControls

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutSlipView.class);
}
