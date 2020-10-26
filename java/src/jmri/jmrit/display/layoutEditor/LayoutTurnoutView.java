package jmri.jmrit.display.layoutEditor;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.*;

import javax.annotation.*;

import jmri.*;
import static jmri.jmrit.display.layoutEditor.LayoutTurnout.INCONSISTENT;
import jmri.util.*;

import jmri.jmrit.display.layoutEditor.LayoutTurnout.TurnoutType;
import static jmri.jmrit.display.layoutEditor.LayoutTurnout.UNKNOWN;

/**
 * MVC View component for the LayoutTurnout class.
 *
 * @author Bob Jacobsen  Copyright (c) 2020
 *
 */
public class LayoutTurnoutView extends LayoutTrackView {

    /**
     * Constructor method.
     * @param turnout the layout turnout to create the view for.
     */
    public LayoutTurnoutView(@Nonnull LayoutTurnout turnout) {
        super(turnout);
        this.turnout = turnout;
    }

    final protected LayoutTurnout turnout;

    // These now reflect to code in the base class; eventually this heirarchy will
    // expand and the code will be brought here

    protected boolean isDisabled() {
        return turnout.isDisabled();
    }
    public Point2D getCoordsA() {
        return turnout.getCoordsA();
    }
    public Point2D getCoordsB() {
        return turnout.getCoordsB();
    }
    public Point2D getCoordsC() {
        return turnout.getCoordsC();
    }
    public Point2D getCoordsD() {
        return turnout.getCoordsD();
    }
    public boolean isMainlineA() {
        return turnout.isMainlineA();
    }
    public boolean isMainlineB() {
        return turnout.isMainlineB();
    }
    public boolean isMainlineC() {
        return turnout.isMainlineC();
    }
    public boolean isMainlineD() {
        return turnout.isMainlineD();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void draw1(Graphics2D g2, boolean isMain, boolean isBlock) {
        if (isBlock && turnout.getLayoutBlock() == null) {
            // Skip the block layer if there is no block assigned.
            return;
        }

        Point2D pA = getCoordsA();
        Point2D pB = getCoordsB();
        Point2D pC = getCoordsC();
        Point2D pD = getCoordsD();

        boolean mainlineA = isMainlineA();
        boolean mainlineB = isMainlineB();
        boolean mainlineC = isMainlineC();
        boolean mainlineD = isMainlineD();

        boolean drawUnselectedLeg = turnout.layoutEditor.isTurnoutDrawUnselectedLeg();

        Color color = g2.getColor();

        // if this isn't a block line all these will be the same color
        Color colorA = color;
        Color colorB = color;
        Color colorC = color;
        Color colorD = color;

        if (isBlock) {
            LayoutBlock lb = turnout.getLayoutBlock();
            colorA = (lb == null) ? color : lb.getBlockColor();
            lb = turnout.getLayoutBlockB();
            colorB = (lb == null) ? color : lb.getBlockColor();
            lb = turnout.getLayoutBlockC();
            colorC = (lb == null) ? color : lb.getBlockColor();
            lb = turnout.getLayoutBlockD();
            colorD = (lb == null) ? color : lb.getBlockColor();
        }

        // middles
        Point2D pM = getCoordsCenter();
        Point2D pABM = MathUtil.midPoint(pA, pB);
        Point2D pAM = MathUtil.lerp(pA, pABM, 5.0 / 8.0);
        Point2D pAMP = MathUtil.midPoint(pAM, pABM);
        Point2D pBM = MathUtil.lerp(pB, pABM, 5.0 / 8.0);
        Point2D pBMP = MathUtil.midPoint(pBM, pABM);

        Point2D pCDM = MathUtil.midPoint(pC, pD);
        Point2D pCM = MathUtil.lerp(pC, pCDM, 5.0 / 8.0);
        Point2D pCMP = MathUtil.midPoint(pCM, pCDM);
        Point2D pDM = MathUtil.lerp(pD, pCDM, 5.0 / 8.0);
        Point2D pDMP = MathUtil.midPoint(pDM, pCDM);

        Point2D pAF = MathUtil.midPoint(pAM, pM);
        Point2D pBF = MathUtil.midPoint(pBM, pM);
        Point2D pCF = MathUtil.midPoint(pCM, pM);
        Point2D pDF = MathUtil.midPoint(pDM, pM);

        int state = UNKNOWN;
        if (turnout.layoutEditor.isAnimating()) {
            state = turnout.getState();
        }

        TurnoutType turnoutType = turnout.getTurnoutType();
        if (turnoutType == TurnoutType.DOUBLE_XOVER) {
            if (state != Turnout.THROWN && state != INCONSISTENT) { // unknown or continuing path - not crossed over
                if (isMain == mainlineA) {
                    g2.setColor(colorA);
                    g2.draw(new Line2D.Double(pA, pABM));
                    if (!isBlock || drawUnselectedLeg) {
                        g2.draw(new Line2D.Double(pAF, pM));
                    }
                }
                if (isMain == mainlineB) {
                    g2.setColor(colorB);
                    g2.draw(new Line2D.Double(pB, pABM));
                    if (!isBlock || drawUnselectedLeg) {
                        g2.draw(new Line2D.Double(pBF, pM));
                    }
                }
                if (isMain == mainlineC) {
                    g2.setColor(colorC);
                    g2.draw(new Line2D.Double(pC, pCDM));
                    if (!isBlock || drawUnselectedLeg) {
                        g2.draw(new Line2D.Double(pCF, pM));
                    }
                }
                if (isMain == mainlineD) {
                    g2.setColor(colorD);
                    g2.draw(new Line2D.Double(pD, pCDM));
                    if (!isBlock || drawUnselectedLeg) {
                        g2.draw(new Line2D.Double(pDF, pM));
                    }
                }
            }
            if (state != Turnout.CLOSED && state != INCONSISTENT) { // unknown or diverting path - crossed over
                if (isMain == mainlineA) {
                    g2.setColor(colorA);
                    g2.draw(new Line2D.Double(pA, pAM));
                    g2.draw(new Line2D.Double(pAM, pM));
                    if (!isBlock || drawUnselectedLeg) {
                        g2.draw(new Line2D.Double(pAMP, pABM));
                    }
                }
                if (isMain == mainlineB) {
                    g2.setColor(colorB);
                    g2.draw(new Line2D.Double(pB, pBM));
                    g2.draw(new Line2D.Double(pBM, pM));
                    if (!isBlock || drawUnselectedLeg) {
                        g2.draw(new Line2D.Double(pBMP, pABM));
                    }
                }
                if (isMain == mainlineC) {
                    g2.setColor(colorC);
                    g2.draw(new Line2D.Double(pC, pCM));
                    g2.draw(new Line2D.Double(pCM, pM));
                    if (!isBlock || drawUnselectedLeg) {
                        g2.draw(new Line2D.Double(pCMP, pCDM));
                    }
                }
                if (isMain == mainlineD) {
                    g2.setColor(colorD);
                    g2.draw(new Line2D.Double(pD, pDM));
                    g2.draw(new Line2D.Double(pDM, pM));
                    if (!isBlock || drawUnselectedLeg) {
                        g2.draw(new Line2D.Double(pDMP, pCDM));
                    }
                }
            }
            if (state == INCONSISTENT) {
                if (isMain == mainlineA) {
                    g2.setColor(colorA);
                    g2.draw(new Line2D.Double(pA, pAM));
                }
                if (isMain == mainlineB) {
                    g2.setColor(colorB);
                    g2.draw(new Line2D.Double(pB, pBM));
                }
                if (isMain == mainlineC) {
                    g2.setColor(colorC);
                    g2.draw(new Line2D.Double(pC, pCM));
                }
                if (isMain == mainlineD) {
                    g2.setColor(colorD);
                    g2.draw(new Line2D.Double(pD, pDM));
                }
                if (!isBlock || drawUnselectedLeg) {
                    if (isMain == mainlineA) {
                        g2.setColor(colorA);
                        g2.draw(new Line2D.Double(pAF, pM));
                    }
                    if (isMain == mainlineC) {
                        g2.setColor(colorC);
                        g2.draw(new Line2D.Double(pCF, pM));
                    }
                    if (isMain == mainlineB) {
                        g2.setColor(colorB);
                        g2.draw(new Line2D.Double(pBF, pM));
                    }
                    if (isMain == mainlineD) {
                        g2.setColor(colorD);
                        g2.draw(new Line2D.Double(pDF, pM));
                    }
                }
            }
        } else if ((turnoutType == TurnoutType.RH_XOVER)
                || (turnoutType == TurnoutType.LH_XOVER)) {    // draw (rh & lh) cross overs
            pAF = MathUtil.midPoint(pABM, pM);
            pBF = MathUtil.midPoint(pABM, pM);
            pCF = MathUtil.midPoint(pCDM, pM);
            pDF = MathUtil.midPoint(pCDM, pM);
            if (state != Turnout.THROWN && state != INCONSISTENT) { // unknown or continuing path - not crossed over
                if (isMain == mainlineA) {
                    g2.setColor(colorA);
                    g2.draw(new Line2D.Double(pA, pABM));
                }
                if (isMain == mainlineB) {
                    g2.setColor(colorB);
                    g2.draw(new Line2D.Double(pABM, pB));
                }
                if (isMain == mainlineC) {
                    g2.setColor(colorC);
                    g2.draw(new Line2D.Double(pC, pCDM));
                }
                if (isMain == mainlineD) {
                    g2.setColor(colorD);
                    g2.draw(new Line2D.Double(pCDM, pD));
                }
                if (!isBlock || drawUnselectedLeg) {
                    if (turnoutType == TurnoutType.RH_XOVER) {
                        if (isMain == mainlineA) {
                            g2.setColor(colorA);
                            g2.draw(new Line2D.Double(pAF, pM));
                        }
                        if (isMain == mainlineC) {
                            g2.setColor(colorC);
                            g2.draw(new Line2D.Double(pCF, pM));
                        }
                    } else if (turnoutType == TurnoutType.LH_XOVER) {
                        if (isMain == mainlineB) {
                            g2.setColor(colorB);
                            g2.draw(new Line2D.Double(pBF, pM));
                        }
                        if (isMain == mainlineD) {
                            g2.setColor(colorD);
                            g2.draw(new Line2D.Double(pDF, pM));
                        }
                    }
                }
            }
            if (state != Turnout.CLOSED && state != INCONSISTENT) { // unknown or diverting path - crossed over
                if (turnoutType == TurnoutType.RH_XOVER) {
                    if (isMain == mainlineA) {
                        g2.setColor(colorA);
                        g2.draw(new Line2D.Double(pA, pABM));
                        g2.draw(new Line2D.Double(pABM, pM));
                    }
                    if (!isBlock || drawUnselectedLeg) {
                        if (isMain == mainlineB) {
                            g2.setColor(colorB);
                            g2.draw(new Line2D.Double(pBM, pB));
                        }
                    }
                    if (isMain == mainlineC) {
                        g2.setColor(colorC);
                        g2.draw(new Line2D.Double(pC, pCDM));
                        g2.draw(new Line2D.Double(pCDM, pM));
                    }
                    if (!isBlock || drawUnselectedLeg) {
                        if (isMain == mainlineD) {
                            g2.setColor(colorD);
                            g2.draw(new Line2D.Double(pDM, pD));
                        }
                    }
                } else if (turnoutType == TurnoutType.LH_XOVER) {
                    if (!isBlock || drawUnselectedLeg) {
                        if (isMain == mainlineA) {
                            g2.setColor(colorA);
                            g2.draw(new Line2D.Double(pA, pAM));
                        }
                    }
                    if (isMain == mainlineB) {
                        g2.setColor(colorB);
                        g2.draw(new Line2D.Double(pB, pABM));
                        g2.draw(new Line2D.Double(pABM, pM));
                    }
                    if (!isBlock || drawUnselectedLeg) {
                        if (isMain == mainlineC) {
                            g2.setColor(colorC);
                            g2.draw(new Line2D.Double(pC, pCM));
                        }
                    }
                    if (isMain == mainlineD) {
                        g2.setColor(colorD);
                        g2.draw(new Line2D.Double(pD, pCDM));
                        g2.draw(new Line2D.Double(pCDM, pM));
                    }
                }
            }
            if (state == INCONSISTENT) {
                if (isMain == mainlineA) {
                    g2.setColor(colorA);
                    g2.draw(new Line2D.Double(pA, pAM));
                }
                if (isMain == mainlineB) {
                    g2.setColor(colorB);
                    g2.draw(new Line2D.Double(pB, pBM));
                }
                if (isMain == mainlineC) {
                    g2.setColor(colorC);
                    g2.draw(new Line2D.Double(pC, pCM));
                }
                if (isMain == mainlineD) {
                    g2.setColor(colorD);
                    g2.draw(new Line2D.Double(pD, pDM));
                }
                if (!isBlock || drawUnselectedLeg) {
                    if (turnoutType == TurnoutType.RH_XOVER) {
                        if (isMain == mainlineA) {
                            g2.setColor(colorA);
                            g2.draw(new Line2D.Double(pAF, pM));
                        }
                        if (isMain == mainlineC) {
                            g2.setColor(colorC);
                            g2.draw(new Line2D.Double(pCF, pM));
                        }
                    } else if (turnoutType == TurnoutType.LH_XOVER) {
                        if (isMain == mainlineB) {
                            g2.setColor(colorB);
                            g2.draw(new Line2D.Double(pBF, pM));
                        }
                        if (isMain == mainlineD) {
                            g2.setColor(colorD);
                            g2.draw(new Line2D.Double(pDF, pM));
                        }
                    }
                }
            }
        } else if (turnout.isTurnoutTypeSlip()) {
            log.error("{}.draw1(...); slips should be being drawn by LayoutSlip sub-class", getName());
        } else {    // LH, RH, or WYE Turnouts
            // draw A<===>center
            if (isMain == mainlineA) {
                g2.setColor(colorA);
                g2.draw(new Line2D.Double(pA, pM));
            }

            if (state == UNKNOWN || (turnout.continuingSense == state && state != INCONSISTENT)) { // unknown or continuing path
                // draw center<===>B
                if (isMain == mainlineB) {
                    g2.setColor(colorB);
                    g2.draw(new Line2D.Double(pM, pB));
                }
            } else if (!isBlock || drawUnselectedLeg) {
                // draw center<--=>B
                if (isMain == mainlineB) {
                    g2.setColor(colorB);
                    g2.draw(new Line2D.Double(MathUtil.twoThirdsPoint(pM, pB), pB));
                }
            }

            if (state == UNKNOWN || (turnout.continuingSense != state && state != INCONSISTENT)) { // unknown or diverting path
                // draw center<===>C
                if (isMain == mainlineC) {
                    g2.setColor(colorC);
                    g2.draw(new Line2D.Double(pM, pC));
                }
            } else if (!isBlock || drawUnselectedLeg) {
                // draw center<--=>C
                if (isMain == mainlineC) {
                    g2.setColor(colorC);
                    g2.draw(new Line2D.Double(MathUtil.twoThirdsPoint(pM, pC), pC));
                }
            }
        }
    }   // draw1

    /**
     * {@inheritDoc}
     */
    @Override
    protected void draw2(Graphics2D g2, boolean isMain, float railDisplacement) {
        TurnoutType turnoutType = turnout.getTurnoutType();

        Point2D pA = getCoordsA();
        Point2D pB = getCoordsB();
        Point2D pC = getCoordsC();
        Point2D pD = getCoordsD();
        Point2D pM = getCoordsCenter();

        Point2D vAM = MathUtil.normalize(MathUtil.subtract(pM, pA));
        Point2D vAMo = MathUtil.orthogonal(MathUtil.normalize(vAM, railDisplacement));

        Point2D pAL = MathUtil.subtract(pA, vAMo);
        Point2D pAR = MathUtil.add(pA, vAMo);

        Point2D vBM = MathUtil.normalize(MathUtil.subtract(pB, pM));
        double dirBM_DEG = MathUtil.computeAngleDEG(vBM);
        Point2D vBMo = MathUtil.normalize(MathUtil.orthogonal(vBM), railDisplacement);
        Point2D pBL = MathUtil.subtract(pB, vBMo);
        Point2D pBR = MathUtil.add(pB, vBMo);
        Point2D pMR = MathUtil.add(pM, vBMo);

        Point2D vCM = MathUtil.normalize(MathUtil.subtract(pC, pM));
        double dirCM_DEG = MathUtil.computeAngleDEG(vCM);

        Point2D vCMo = MathUtil.normalize(MathUtil.orthogonal(vCM), railDisplacement);
        Point2D pCL = MathUtil.subtract(pC, vCMo);
        Point2D pCR = MathUtil.add(pC, vCMo);
        Point2D pML = MathUtil.subtract(pM, vBMo);

        double deltaBMC_DEG = MathUtil.absDiffAngleDEG(dirBM_DEG, dirCM_DEG);
        double deltaBMC_RAD = Math.toRadians(deltaBMC_DEG);

        double hypotF = railDisplacement / Math.sin(deltaBMC_RAD / 2.0);

        Point2D vDisF = MathUtil.normalize(MathUtil.add(vAM, vCM), hypotF);
        if (turnoutType == TurnoutType.WYE_TURNOUT) {
            vDisF = MathUtil.normalize(vAM, hypotF);
        }
        Point2D pF = MathUtil.add(pM, vDisF);

        Point2D pFR = MathUtil.add(pF, MathUtil.multiply(vBMo, 2.0));
        Point2D pFL = MathUtil.subtract(pF, MathUtil.multiply(vCMo, 2.0));

        // Point2D pFPR = MathUtil.add(pF, MathUtil.normalize(vBMo, 2.0));
        // Point2D pFPL = MathUtil.subtract(pF, MathUtil.normalize(vCMo, 2.0));
        Point2D vDisAP = MathUtil.normalize(vAM, hypotF);
        Point2D pAP = MathUtil.subtract(pM, vDisAP);
        Point2D pAPR = MathUtil.add(pAP, vAMo);
        Point2D pAPL = MathUtil.subtract(pAP, vAMo);

        // Point2D vSo = MathUtil.normalize(vAMo, 2.0);
        // Point2D pSL = MathUtil.add(pAPL, vSo);
        // Point2D pSR = MathUtil.subtract(pAPR, vSo);
        boolean mainlineA = isMainlineA();
        boolean mainlineB = isMainlineB();
        boolean mainlineC = isMainlineC();
        boolean mainlineD = isMainlineD();

        int state = UNKNOWN;
        if (turnout.layoutEditor.isAnimating()) {
            state = turnout.getState();
        }

        switch (turnoutType) {
            case RH_TURNOUT: {
                if (isMain == mainlineA) {
                    g2.draw(new Line2D.Double(pAL, pML));
                    g2.draw(new Line2D.Double(pAR, pAPR));
                }
                if (isMain == mainlineB) {
                    g2.draw(new Line2D.Double(pML, pBL));
                    g2.draw(new Line2D.Double(pF, pBR));
                    if (turnout.continuingSense == state) {  // unknown or diverting path
//                         g2.draw(new Line2D.Double(pSR, pFPR));
//                     } else {
                        g2.draw(new Line2D.Double(pAPR, pF));
                    }
                }
                if (isMain == mainlineC) {
                    g2.draw(new Line2D.Double(pF, pCL));
                    g2.draw(new Line2D.Double(pFR, pCR));
                    GeneralPath path = new GeneralPath();
                    path.moveTo(pAPR.getX(), pAPR.getY());
                    path.quadTo(pMR.getX(), pMR.getY(), pFR.getX(), pFR.getY());
                    path.lineTo(pCR.getX(), pCR.getY());
                    g2.draw(path);
                    if (turnout.continuingSense != state) {  // unknown or diverting path
                        path = new GeneralPath();
                        path.moveTo(pAPL.getX(), pAPL.getY());
                        path.quadTo(pML.getX(), pML.getY(), pF.getX(), pF.getY());
                        g2.draw(path);
//                     } else {
//                         path = new GeneralPath();
//                         path.moveTo(pSL.getX(), pSL.getY());
//                         path.quadTo(pML.getX(), pML.getY(), pFPL.getX(), pFPL.getY());
//                         g2.draw(path);
                    }
                }
                break;
            }   // case RH_TURNOUT

            case LH_TURNOUT: {
                if (isMain == mainlineA) {
                    g2.draw(new Line2D.Double(pAR, pMR));
                    g2.draw(new Line2D.Double(pAL, pAPL));
                }
                if (isMain == mainlineB) {
                    g2.draw(new Line2D.Double(pMR, pBR));
                    g2.draw(new Line2D.Double(pF, pBL));
                    if (turnout.continuingSense == state) {  // straight path
//                         g2.draw(new Line2D.Double(pSL, pFPL));  Offset problem
//                     } else {
                        g2.draw(new Line2D.Double(pAPL, pF));
                    }
                }
                if (isMain == mainlineC) {
                    g2.draw(new Line2D.Double(pF, pCR));
                    GeneralPath path = new GeneralPath();
                    path.moveTo(pAPL.getX(), pAPL.getY());
                    path.quadTo(pML.getX(), pML.getY(), pFL.getX(), pFL.getY());
                    path.lineTo(pCL.getX(), pCL.getY());
                    g2.draw(path);
                    if (turnout.continuingSense != state) {  // unknown or diverting path
                        path = new GeneralPath();
                        path.moveTo(pAPR.getX(), pAPR.getY());
                        path.quadTo(pMR.getX(), pMR.getY(), pF.getX(), pF.getY());
                        g2.draw(path);
//                     } else {
//                         path = new GeneralPath();
//                         path.moveTo(pSR.getX(), pSR.getY());
//                         path.quadTo(pMR.getX(), pMR.getY(), pFPR.getX(), pFPR.getY());
//                         g2.draw(path);
                    }
                }
                break;
            }   // case LH_TURNOUT

            case WYE_TURNOUT: {
                if (isMain == mainlineA) {
                    g2.draw(new Line2D.Double(pAL, pAPL));
                    g2.draw(new Line2D.Double(pAR, pAPR));
                }
                if (isMain == mainlineB) {
                    g2.draw(new Line2D.Double(pF, pBL));
                    GeneralPath path = new GeneralPath();
                    path.moveTo(pAPR.getX(), pAPR.getY());
                    path.quadTo(pMR.getX(), pMR.getY(), pFR.getX(), pFR.getY());
                    path.lineTo(pBR.getX(), pBR.getY());
                    g2.draw(path);
                    if (turnout.continuingSense != state) {  // unknown or diverting path
                        path = new GeneralPath();
                        path.moveTo(pAPR.getX(), pAPR.getY());
                        path.quadTo(pMR.getX(), pMR.getY(), pF.getX(), pF.getY());
                        g2.draw(path);
//                     } else {
//                         path = new GeneralPath();
//                         path.moveTo(pSR.getX(), pSR.getY());
//                         path.quadTo(pMR.getX(), pMR.getY(), pFPR.getX(), pFPR.getY());
//                  bad    g2.draw(path);
                    }
                }
                if (isMain == mainlineC) {
                    pML = MathUtil.subtract(pM, vCMo);
                    GeneralPath path = new GeneralPath();
                    path.moveTo(pAPL.getX(), pAPL.getY());
                    path.quadTo(pML.getX(), pML.getY(), pFL.getX(), pFL.getY());
                    path.lineTo(pCL.getX(), pCL.getY());
                    g2.draw(path);
                    g2.draw(new Line2D.Double(pF, pCR));
                    if (turnout.continuingSense != state) {  // unknown or diverting path
//                         path = new GeneralPath();
//                         path.moveTo(pSL.getX(), pSL.getY());
//                         path.quadTo(pML.getX(), pML.getY(), pFPL.getX(), pFPL.getY());
//           bad              g2.draw(path);
                    } else {
                        path = new GeneralPath();
                        path.moveTo(pAPL.getX(), pAPL.getY());
                        path.quadTo(pML.getX(), pML.getY(), pF.getX(), pF.getY());
                        g2.draw(path);
                    }
                }
                break;
            }   // case WYE_TURNOUT

            case DOUBLE_XOVER: {
                // A, B, C, D end points (left and right)
                Point2D vAB = MathUtil.normalize(MathUtil.subtract(pB, pA), railDisplacement);
                double dirAB_DEG = MathUtil.computeAngleDEG(vAB);
                Point2D vABo = MathUtil.orthogonal(MathUtil.normalize(vAB, railDisplacement));
                pAL = MathUtil.subtract(pA, vABo);
                pAR = MathUtil.add(pA, vABo);
                pBL = MathUtil.subtract(pB, vABo);
                pBR = MathUtil.add(pB, vABo);
                Point2D vCD = MathUtil.normalize(MathUtil.subtract(pD, pC), railDisplacement);
                Point2D vCDo = MathUtil.orthogonal(MathUtil.normalize(vCD, railDisplacement));
                pCL = MathUtil.add(pC, vCDo);
                pCR = MathUtil.subtract(pC, vCDo);
                Point2D pDL = MathUtil.add(pD, vCDo);
                Point2D pDR = MathUtil.subtract(pD, vCDo);

                // AB, CD mid points (left and right)
                Point2D pABM = MathUtil.midPoint(pA, pB);
                Point2D pABL = MathUtil.midPoint(pAL, pBL);
                Point2D pABR = MathUtil.midPoint(pAR, pBR);
                Point2D pCDM = MathUtil.midPoint(pC, pD);
                Point2D pCDL = MathUtil.midPoint(pCL, pDL);
                Point2D pCDR = MathUtil.midPoint(pCR, pDR);

                // A, B, C, D mid points
                double halfParallelDistance = MathUtil.distance(pABM, pCDM) / 2.0;
                Point2D pAM = MathUtil.subtract(pABM, MathUtil.normalize(vAB, halfParallelDistance));
                Point2D pAML = MathUtil.subtract(pAM, vABo);
                Point2D pAMR = MathUtil.add(pAM, vABo);
                Point2D pBM = MathUtil.add(pABM, MathUtil.normalize(vAB, halfParallelDistance));
                Point2D pBML = MathUtil.subtract(pBM, vABo);
                Point2D pBMR = MathUtil.add(pBM, vABo);
                Point2D pCM = MathUtil.subtract(pCDM, MathUtil.normalize(vCD, halfParallelDistance));
                Point2D pCML = MathUtil.subtract(pCM, vABo);
                Point2D pCMR = MathUtil.add(pCM, vABo);
                Point2D pDM = MathUtil.add(pCDM, MathUtil.normalize(vCD, halfParallelDistance));
                Point2D pDML = MathUtil.subtract(pDM, vABo);
                Point2D pDMR = MathUtil.add(pDM, vABo);

                // crossing points
                Point2D vACM = MathUtil.normalize(MathUtil.subtract(pCM, pAM), railDisplacement);
                Point2D vACMo = MathUtil.orthogonal(vACM);
                Point2D vBDM = MathUtil.normalize(MathUtil.subtract(pDM, pBM), railDisplacement);
                Point2D vBDMo = MathUtil.orthogonal(vBDM);
                Point2D pBDR = MathUtil.add(pM, vACM);
                Point2D pBDL = MathUtil.subtract(pM, vACM);

                // crossing diamond point (no gaps)
                Point2D pVR = MathUtil.add(pBDL, vBDM);
                Point2D pKL = MathUtil.subtract(pBDL, vBDM);
                Point2D pKR = MathUtil.add(pBDR, vBDM);
                Point2D pVL = MathUtil.subtract(pBDR, vBDM);

                // crossing diamond points (with gaps)
                Point2D vACM2 = MathUtil.normalize(vACM, 2.0);
                Point2D vBDM2 = MathUtil.normalize(vBDM, 2.0);
                // (syntax of "pKLtC" is "point LK toward C", etc.)
                Point2D pKLtC = MathUtil.add(pKL, vACM2);
                Point2D pKLtD = MathUtil.add(pKL, vBDM2);
                Point2D pVLtA = MathUtil.subtract(pVL, vACM2);
                Point2D pVLtD = MathUtil.add(pVL, vBDM2);
                Point2D pKRtA = MathUtil.subtract(pKR, vACM2);
                Point2D pKRtB = MathUtil.subtract(pKR, vBDM2);
                Point2D pVRtB = MathUtil.subtract(pVR, vBDM2);
                Point2D pVRtC = MathUtil.add(pVR, vACM2);

                // A, B, C, D frog points
                vCM = MathUtil.normalize(MathUtil.subtract(pCM, pM));
                dirCM_DEG = MathUtil.computeAngleDEG(vCM);
                double deltaBAC_DEG = MathUtil.absDiffAngleDEG(dirAB_DEG, dirCM_DEG);
                double deltaBAC_RAD = Math.toRadians(deltaBAC_DEG);
                hypotF = railDisplacement / Math.sin(deltaBAC_RAD / 2.0);
                Point2D vACF = MathUtil.normalize(MathUtil.add(vACM, vAB), hypotF);
                Point2D pAFL = MathUtil.add(pAM, vACF);
                Point2D pCFR = MathUtil.subtract(pCM, vACF);
                Point2D vBDF = MathUtil.normalize(MathUtil.add(vBDM, vCD), hypotF);
                Point2D pBFL = MathUtil.add(pBM, vBDF);
                Point2D pDFR = MathUtil.subtract(pDM, vBDF);

                // A, B, C, D frog points
                Point2D pAFR = MathUtil.add(MathUtil.add(pAFL, vACMo), vACMo);
                Point2D pBFR = MathUtil.subtract(MathUtil.subtract(pBFL, vBDMo), vBDMo);
                Point2D pCFL = MathUtil.subtract(MathUtil.subtract(pCFR, vACMo), vACMo);
                Point2D pDFL = MathUtil.add(MathUtil.add(pDFR, vBDMo), vBDMo);

                // end of switch rails (closed)
                Point2D vABF = MathUtil.normalize(vAB, hypotF);
                pAP = MathUtil.subtract(pAM, vABF);
                pAPL = MathUtil.subtract(pAP, vABo);
                pAPR = MathUtil.add(pAP, vABo);
                Point2D pBP = MathUtil.add(pBM, vABF);
                Point2D pBPL = MathUtil.subtract(pBP, vABo);
                Point2D pBPR = MathUtil.add(pBP, vABo);

                Point2D vCDF = MathUtil.normalize(vCD, hypotF);
                Point2D pCP = MathUtil.subtract(pCM, vCDF);
                Point2D pCPL = MathUtil.add(pCP, vCDo);
                Point2D pCPR = MathUtil.subtract(pCP, vCDo);
                Point2D pDP = MathUtil.add(pDM, vCDF);
                Point2D pDPL = MathUtil.add(pDP, vCDo);
                Point2D pDPR = MathUtil.subtract(pDP, vCDo);

                // end of switch rails (open)
                Point2D vS = MathUtil.normalize(vABo, 2.0);
                Point2D pASL = MathUtil.add(pAPL, vS);
                // Point2D pASR = MathUtil.subtract(pAPR, vS);
                Point2D pBSL = MathUtil.add(pBPL, vS);
                // Point2D pBSR = MathUtil.subtract(pBPR, vS);
                Point2D pCSR = MathUtil.subtract(pCPR, vS);
                // Point2D pCSL = MathUtil.add(pCPL, vS);
                Point2D pDSR = MathUtil.subtract(pDPR, vS);
                // Point2D pDSL = MathUtil.add(pDPL, vS);

                // end of switch rails (open at frogs)
                Point2D pAFS = MathUtil.subtract(pAFL, vS);
                Point2D pBFS = MathUtil.subtract(pBFL, vS);
                Point2D pCFS = MathUtil.add(pCFR, vS);
                Point2D pDFS = MathUtil.add(pDFR, vS);

                // vSo = MathUtil.orthogonal(vS);
                // Point2D pAFSR = MathUtil.add(pAFL, vSo);
                // Point2D pBFSR = MathUtil.subtract(pBFL, vSo);
                // Point2D pCFSL = MathUtil.subtract(pCFR, vSo);
                // Point2D pDFSL = MathUtil.add(pDFR, vSo);
                if (isMain == mainlineA) {
                    g2.draw(new Line2D.Double(pAL, pABL));
                    g2.draw(new Line2D.Double(pVRtB, pKLtD));
                    g2.draw(new Line2D.Double(pAFL, pABR));
                    g2.draw(new Line2D.Double(pAFL, pKL));
                    GeneralPath path = new GeneralPath();
                    path.moveTo(pAR.getX(), pAR.getY());
                    path.lineTo(pAPR.getX(), pAPR.getY());
                    path.quadTo(pAMR.getX(), pAMR.getY(), pAFR.getX(), pAFR.getY());
                    path.lineTo(pVR.getX(), pVR.getY());
                    g2.draw(path);
                    if (state != Turnout.CLOSED) {  // unknown or diverting path
                        path = new GeneralPath();
                        path.moveTo(pAPL.getX(), pAPL.getY());
                        path.quadTo(pAML.getX(), pAML.getY(), pAFL.getX(), pAFL.getY());
                        g2.draw(path);
//                         g2.draw(new Line2D.Double(pASR, pAFSR));
                    } else {                        // continuing path
                        g2.draw(new Line2D.Double(pAPR, pAFL));
                        path = new GeneralPath();
                        path.moveTo(pASL.getX(), pASL.getY());
                        path.quadTo(pAML.getX(), pAML.getY(), pAFS.getX(), pAFS.getY());
//                         g2.draw(path);
                    }
                }
                if (isMain == mainlineB) {
                    g2.draw(new Line2D.Double(pABL, pBL));
                    g2.draw(new Line2D.Double(pKLtC, pVLtA));
                    g2.draw(new Line2D.Double(pBFL, pABR));
                    g2.draw(new Line2D.Double(pBFL, pKL));
                    GeneralPath path = new GeneralPath();
                    path.moveTo(pBR.getX(), pBR.getY());
                    path.lineTo(pBPR.getX(), pBPR.getY());
                    path.quadTo(pBMR.getX(), pBMR.getY(), pBFR.getX(), pBFR.getY());
                    path.lineTo(pVL.getX(), pVL.getY());
                    g2.draw(path);
                    if (state != Turnout.CLOSED) {  // unknown or diverting path
                        path = new GeneralPath();
                        path.moveTo(pBPL.getX(), pBPL.getY());
                        path.quadTo(pBML.getX(), pBML.getY(), pBFL.getX(), pBFL.getY());
                        g2.draw(path);
//                         g2.draw(new Line2D.Double(pBSR, pBFSR));
                    } else {
                        g2.draw(new Line2D.Double(pBPR, pBFL));
                        path = new GeneralPath();
                        path.moveTo(pBSL.getX(), pBSL.getY());
                        path.quadTo(pBML.getX(), pBML.getY(), pBFS.getX(), pBFS.getY());
//                         g2.draw(path);
                    }
                }
                if (isMain == mainlineC) {
                    g2.draw(new Line2D.Double(pCR, pCDR));
                    g2.draw(new Line2D.Double(pKRtB, pVLtD));
                    g2.draw(new Line2D.Double(pCFR, pCDL));
                    g2.draw(new Line2D.Double(pCFR, pKR));
                    GeneralPath path = new GeneralPath();
                    path.moveTo(pCL.getX(), pCL.getY());
                    path.lineTo(pCPL.getX(), pCPL.getY());
                    path.quadTo(pCML.getX(), pCML.getY(), pCFL.getX(), pCFL.getY());
                    path.lineTo(pVL.getX(), pVL.getY());
                    g2.draw(path);
                    if (state != Turnout.CLOSED) {  // unknown or diverting path
                        path = new GeneralPath();
                        path.moveTo(pCPR.getX(), pCPR.getY());
                        path.quadTo(pCMR.getX(), pCMR.getY(), pCFR.getX(), pCFR.getY());
                        g2.draw(path);
//                         g2.draw(new Line2D.Double(pCSL, pCFSL));
                    } else {
                        g2.draw(new Line2D.Double(pCPL, pCFR));
                        path = new GeneralPath();
                        path.moveTo(pCSR.getX(), pCSR.getY());
                        path.quadTo(pCMR.getX(), pCMR.getY(), pCFS.getX(), pCFS.getY());
//                         g2.draw(path);
                    }
                }
                if (isMain == mainlineD) {
                    g2.draw(new Line2D.Double(pCDR, pDR));
                    g2.draw(new Line2D.Double(pKRtA, pVRtC));
                    g2.draw(new Line2D.Double(pDFR, pCDL));
                    g2.draw(new Line2D.Double(pDFR, pKR));
                    GeneralPath path = new GeneralPath();
                    path.moveTo(pDL.getX(), pDL.getY());
                    path.lineTo(pDPL.getX(), pDPL.getY());
                    path.quadTo(pDML.getX(), pDML.getY(), pDFL.getX(), pDFL.getY());
                    path.lineTo(pVR.getX(), pVR.getY());
                    g2.draw(path);
                    if (state != Turnout.CLOSED) {  // unknown or diverting path
                        path = new GeneralPath();
                        path.moveTo(pDPR.getX(), pDPR.getY());
                        path.quadTo(pDMR.getX(), pDMR.getY(), pDFR.getX(), pDFR.getY());
                        g2.draw(path);
//                         g2.draw(new Line2D.Double(pDSL, pDFSL));
                    } else {
                        g2.draw(new Line2D.Double(pDPL, pDFR));
                        path = new GeneralPath();
                        path.moveTo(pDSR.getX(), pDSR.getY());
                        path.quadTo(pDMR.getX(), pDMR.getY(), pDFS.getX(), pDFS.getY());
//                         g2.draw(path);
                    }
                }
                break;
            }   // case DOUBLE_XOVER

            case RH_XOVER: {
                // A, B, C, D end points (left and right)
                Point2D vAB = MathUtil.normalize(MathUtil.subtract(pB, pA), railDisplacement);
                double dirAB_DEG = MathUtil.computeAngleDEG(vAB);
                Point2D vABo = MathUtil.orthogonal(MathUtil.normalize(vAB, railDisplacement));
                pAL = MathUtil.subtract(pA, vABo);
                pAR = MathUtil.add(pA, vABo);
                pBL = MathUtil.subtract(pB, vABo);
                pBR = MathUtil.add(pB, vABo);
                Point2D vCD = MathUtil.normalize(MathUtil.subtract(pD, pC), railDisplacement);
                Point2D vCDo = MathUtil.orthogonal(MathUtil.normalize(vCD, railDisplacement));
                pCL = MathUtil.add(pC, vCDo);
                pCR = MathUtil.subtract(pC, vCDo);
                Point2D pDL = MathUtil.add(pD, vCDo);
                Point2D pDR = MathUtil.subtract(pD, vCDo);

                // AB and CD mid points
                Point2D pABM = MathUtil.midPoint(pA, pB);
                Point2D pABL = MathUtil.subtract(pABM, vABo);
                Point2D pABR = MathUtil.add(pABM, vABo);
                Point2D pCDM = MathUtil.midPoint(pC, pD);
                Point2D pCDL = MathUtil.subtract(pCDM, vABo);
                Point2D pCDR = MathUtil.add(pCDM, vABo);

                // directions
                Point2D vAC = MathUtil.normalize(MathUtil.subtract(pCDM, pABM), railDisplacement);
                Point2D vACo = MathUtil.orthogonal(MathUtil.normalize(vAC, railDisplacement));
                double dirAC_DEG = MathUtil.computeAngleDEG(vAC);
                double deltaBAC_DEG = MathUtil.absDiffAngleDEG(dirAB_DEG, dirAC_DEG);
                double deltaBAC_RAD = Math.toRadians(deltaBAC_DEG);

                // AC mid points
                Point2D pACL = MathUtil.subtract(pM, vACo);
                Point2D pACR = MathUtil.add(pM, vACo);

                // frogs
                hypotF = railDisplacement / Math.sin(deltaBAC_RAD / 2.0);
                Point2D vF = MathUtil.normalize(MathUtil.add(vAB, vAC), hypotF);
                Point2D pABF = MathUtil.add(pABM, vF);
                Point2D pCDF = MathUtil.subtract(pCDM, vF);

                // frog primes
                Point2D pABFP = MathUtil.add(MathUtil.add(pABF, vACo), vACo);
                Point2D pCDFP = MathUtil.subtract(MathUtil.subtract(pCDF, vACo), vACo);

                // end of switch rails (closed)
                Point2D vABF = MathUtil.normalize(vAB, hypotF);
                pAP = MathUtil.subtract(pABM, vABF);
                pAPL = MathUtil.subtract(pAP, vABo);
                pAPR = MathUtil.add(pAP, vABo);
                Point2D pCP = MathUtil.add(pCDM, vABF);
                Point2D pCPL = MathUtil.add(pCP, vCDo);
                Point2D pCPR = MathUtil.subtract(pCP, vCDo);

                // end of switch rails (open)
                Point2D vS = MathUtil.normalize(vAB, 2.0);
                Point2D vSo = MathUtil.orthogonal(vS);
                Point2D pASL = MathUtil.add(pAPL, vSo);
                // Point2D pASR = MathUtil.subtract(pAPR, vSo);
                // Point2D pCSL = MathUtil.add(pCPL, vSo);
                Point2D pCSR = MathUtil.subtract(pCPR, vSo);

                // end of switch rails (open at frogs)
                Point2D pABFS = MathUtil.subtract(pABF, vSo);
                // Point2D pABFSP = MathUtil.subtract(pABF, vS);
                Point2D pCDFS = MathUtil.add(pCDF, vSo);
                // Point2D pCDFSP = MathUtil.add(pCDF, vS);

                if (isMain == mainlineA) {
                    g2.draw(new Line2D.Double(pAL, pABL));
                    GeneralPath path = new GeneralPath();
                    path.moveTo(pAR.getX(), pAR.getY());
                    path.lineTo(pAPR.getX(), pAPR.getY());
                    path.quadTo(pABR.getX(), pABR.getY(), pABFP.getX(), pABFP.getY());
                    path.lineTo(pACR.getX(), pACR.getY());
                    g2.draw(path);
                    g2.draw(new Line2D.Double(pABF, pACL));
                    if (state != Turnout.CLOSED) {  // unknown or diverting path
                        path = new GeneralPath();
                        path.moveTo(pAPL.getX(), pAPL.getY());
                        path.quadTo(pABL.getX(), pABL.getY(), pABF.getX(), pABF.getY());
                        g2.draw(path);
//                         g2.draw(new Line2D.Double(pASR, pABFSP));
                    } else {                        // continuing path
                        g2.draw(new Line2D.Double(pAPR, pABF));
                        path = new GeneralPath();
                        path.moveTo(pASL.getX(), pASL.getY());
                        path.quadTo(pABL.getX(), pABL.getY(), pABFS.getX(), pABFS.getY());
//                         g2.draw(path);
                    }
                }
                if (isMain == mainlineB) {
                    g2.draw(new Line2D.Double(pABL, pBL));
                    g2.draw(new Line2D.Double(pABF, pBR));
                }
                if (isMain == mainlineC) {
                    g2.draw(new Line2D.Double(pCR, pCDR));
                    GeneralPath path = new GeneralPath();
                    path.moveTo(pCL.getX(), pCL.getY());
                    path.lineTo(pCPL.getX(), pCPL.getY());
                    path.quadTo(pCDL.getX(), pCDL.getY(), pCDFP.getX(), pCDFP.getY());
                    path.lineTo(pACL.getX(), pACL.getY());
                    g2.draw(path);
                    g2.draw(new Line2D.Double(pCDF, pACR));
                    if (state != Turnout.CLOSED) {  // unknown or diverting path
                        path = new GeneralPath();
                        path.moveTo(pCPR.getX(), pCPR.getY());
                        path.quadTo(pCDR.getX(), pCDR.getY(), pCDF.getX(), pCDF.getY());
                        g2.draw(path);
//                         g2.draw(new Line2D.Double(pCSL, pCDFSP));
                    } else {                        // continuing path
                        g2.draw(new Line2D.Double(pCPL, pCDF));
                        path = new GeneralPath();
                        path.moveTo(pCSR.getX(), pCSR.getY());
                        path.quadTo(pCDR.getX(), pCDR.getY(), pCDFS.getX(), pCDFS.getY());
//                         g2.draw(path);
                    }
                }
                if (isMain == mainlineD) {
                    g2.draw(new Line2D.Double(pCDR, pDR));
                    g2.draw(new Line2D.Double(pCDF, pDL));
                }
                break;
            }   // case RH_XOVER

            case LH_XOVER: {
                // B, A, D, C end points (left and right)
                Point2D vBA = MathUtil.normalize(MathUtil.subtract(pA, pB), railDisplacement);
                double dirBA_DEG = MathUtil.computeAngleDEG(vBA);
                Point2D vBAo = MathUtil.orthogonal(MathUtil.normalize(vBA, railDisplacement));
                pBL = MathUtil.add(pB, vBAo);
                pBR = MathUtil.subtract(pB, vBAo);
                pAL = MathUtil.add(pA, vBAo);
                pAR = MathUtil.subtract(pA, vBAo);
                Point2D vDC = MathUtil.normalize(MathUtil.subtract(pC, pD), railDisplacement);
                Point2D vDCo = MathUtil.orthogonal(MathUtil.normalize(vDC, railDisplacement));
                Point2D pDL = MathUtil.subtract(pD, vDCo);
                Point2D pDR = MathUtil.add(pD, vDCo);
                pCL = MathUtil.subtract(pC, vDCo);
                pCR = MathUtil.add(pC, vDCo);

                // BA and DC mid points
                Point2D pBAM = MathUtil.midPoint(pB, pA);
                Point2D pBAL = MathUtil.add(pBAM, vBAo);
                Point2D pBAR = MathUtil.subtract(pBAM, vBAo);
                Point2D pDCM = MathUtil.midPoint(pD, pC);
                Point2D pDCL = MathUtil.add(pDCM, vBAo);
                Point2D pDCR = MathUtil.subtract(pDCM, vBAo);

                // directions
                Point2D vBD = MathUtil.normalize(MathUtil.subtract(pDCM, pBAM), railDisplacement);
                Point2D vBDo = MathUtil.orthogonal(MathUtil.normalize(vBD, railDisplacement));
                double dirBD_DEG = MathUtil.computeAngleDEG(vBD);
                double deltaABD_DEG = MathUtil.absDiffAngleDEG(dirBA_DEG, dirBD_DEG);
                double deltaABD_RAD = Math.toRadians(deltaABD_DEG);

                // BD mid points
                Point2D pBDL = MathUtil.add(pM, vBDo);
                Point2D pBDR = MathUtil.subtract(pM, vBDo);

                // frogs
                hypotF = railDisplacement / Math.sin(deltaABD_RAD / 2.0);
                Point2D vF = MathUtil.normalize(MathUtil.add(vBA, vBD), hypotF);
                Point2D pBFL = MathUtil.add(pBAM, vF);
                Point2D pBF = MathUtil.subtract(pBFL, vBDo);
                Point2D pBFR = MathUtil.subtract(pBF, vBDo);
                Point2D pDFR = MathUtil.subtract(pDCM, vF);
                Point2D pDF = MathUtil.add(pDFR, vBDo);
                Point2D pDFL = MathUtil.add(pDF, vBDo);

                // end of switch rails (closed)
                Point2D vBAF = MathUtil.normalize(vBA, hypotF);
                Point2D pBP = MathUtil.subtract(pBAM, vBAF);
                Point2D pBPL = MathUtil.add(pBP, vBAo);
                Point2D pBPR = MathUtil.subtract(pBP, vBAo);
                Point2D pDP = MathUtil.add(pDCM, vBAF);
                Point2D pDPL = MathUtil.subtract(pDP, vDCo);
                Point2D pDPR = MathUtil.add(pDP, vDCo);

                // end of switch rails (open)
                Point2D vS = MathUtil.normalize(vBA, 2.0);
                Point2D vSo = MathUtil.orthogonal(vS);
                Point2D pBSL = MathUtil.subtract(pBPL, vSo);
                // Point2D pBSR = MathUtil.add(pBPR, vSo);
                // Point2D pDSL = MathUtil.subtract(pDPL, vSo);
                Point2D pDSR = MathUtil.add(pDPR, vSo);

                // end of switch rails (open at frogs)
                Point2D pBAFS = MathUtil.add(pBFL, vSo);
                // Point2D pBAFSP = MathUtil.subtract(pBFL, vS);
                Point2D pDCFS = MathUtil.subtract(pDFR, vSo);
                // Point2D pDCFSP = MathUtil.add(pDFR, vS);

                if (isMain == mainlineA) {
                    g2.draw(new Line2D.Double(pBAL, pAL));
                    g2.draw(new Line2D.Double(pBFL, pAR));
                }
                if (isMain == mainlineB) {
                    g2.draw(new Line2D.Double(pBL, pBAL));
                    GeneralPath path = new GeneralPath();
                    path.moveTo(pBR.getX(), pBR.getY());
                    path.lineTo(pBPR.getX(), pBPR.getY());
                    path.quadTo(pBAR.getX(), pBAR.getY(), pBFR.getX(), pBFR.getY());
                    path.lineTo(pBDR.getX(), pBDR.getY());
                    g2.draw(path);
                    g2.draw(new Line2D.Double(pBFL, pBDL));
                    if (state != Turnout.CLOSED) {  // unknown or diverting path
                        path = new GeneralPath();
                        path.moveTo(pBPL.getX(), pBPL.getY());
                        path.quadTo(pBAL.getX(), pBAL.getY(), pBFL.getX(), pBFL.getY());
                        g2.draw(path);
//                         g2.draw(new Line2D.Double(pBSR, pBAFSP));
                    } else {                        // continuing path
                        g2.draw(new Line2D.Double(pBPR, pBFL));
                        path = new GeneralPath();
                        path.moveTo(pBSL.getX(), pBSL.getY());
                        path.quadTo(pBAL.getX(), pBAL.getY(), pBAFS.getX(), pBAFS.getY());
//                         g2.draw(path);
                    }
                }
                if (isMain == mainlineC) {
                    g2.draw(new Line2D.Double(pDCR, pCR));
                    g2.draw(new Line2D.Double(pDFR, pCL));
                }
                if (isMain == mainlineD) {
                    g2.draw(new Line2D.Double(pDR, pDCR));
                    GeneralPath path = new GeneralPath();
                    path.moveTo(pDL.getX(), pDL.getY());
                    path.lineTo(pDPL.getX(), pDPL.getY());
                    path.quadTo(pDCL.getX(), pDCL.getY(), pDFL.getX(), pDFL.getY());
                    path.lineTo(pBDL.getX(), pBDL.getY());
                    g2.draw(path);
                    g2.draw(new Line2D.Double(pDFR, pBDR));
                    if (state != Turnout.CLOSED) {  // unknown or diverting path
                        path = new GeneralPath();
                        path.moveTo(pDPR.getX(), pDPR.getY());
                        path.quadTo(pDCR.getX(), pDCR.getY(), pDFR.getX(), pDFR.getY());
                        g2.draw(path);
//                         g2.draw(new Line2D.Double(pDSL, pDCFSP));
                    } else {                        // continuing path
                        g2.draw(new Line2D.Double(pDPL, pDFR));
                        path = new GeneralPath();
                        path.moveTo(pDSR.getX(), pDSR.getY());
                        path.quadTo(pDCR.getX(), pDCR.getY(), pDCFS.getX(), pDCFS.getY());
//                         g2.draw(path);
                    }
                }
                break;
            }   // case LH_XOVER
            case SINGLE_SLIP:
            case DOUBLE_SLIP: {
                log.error("{}.draw2(...); slips should be being drawn by LayoutSlip sub-class", getName());
                break;
            }
            default: {
                // this should never happen... but...
                log.error("{}.draw2(...); Unknown turnout type {}", getName(), turnoutType);
                break;
            }
        }
    }   // draw2

    /**
     * {@inheritDoc}
     */
    @Override
    protected void highlightUnconnected(Graphics2D g2, HitPointType specificType) {
        if (((specificType == HitPointType.NONE) || (specificType == HitPointType.TURNOUT_A))
                && (turnout.getConnectA() == null)) {
            g2.fill(turnout.trackControlCircleAt(getCoordsA()));
        }

        if (((specificType == HitPointType.NONE) || (specificType == HitPointType.TURNOUT_B))
                && (turnout.getConnectB() == null)) {
            g2.fill(turnout.trackControlCircleAt(getCoordsB()));
        }

        if (((specificType == HitPointType.NONE) || (specificType == HitPointType.TURNOUT_C))
                && (turnout.getConnectC() == null)) {
            g2.fill(turnout.trackControlCircleAt(getCoordsC()));
        }
        if (turnout.isTurnoutTypeXover()) {
            if (((specificType == HitPointType.NONE) || (specificType == HitPointType.TURNOUT_D))
                    && (turnout.getConnectD() == null)) {
                g2.fill(turnout.trackControlCircleAt(getCoordsD()));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void drawTurnoutControls(Graphics2D g2) {
        if (!turnout.disabled && !(turnout.disableWhenOccupied && turnout.isOccupied())) {
            Color foregroundColor = g2.getColor();
            // if turnout is not continuing state
            if (turnout.getState() != turnout.continuingSense) {
                // then switch to background color
                g2.setColor(g2.getBackground());
            }
            if (turnout.layoutEditor.isTurnoutFillControlCircles()) {
                g2.fill(turnout.trackControlCircleAt(getCoordsCenter()));
            } else {
                g2.draw(turnout.trackControlCircleAt(getCoordsCenter()));
            }
            // if turnout is not continuing state
            if (turnout.getState() != turnout.continuingSense) {
                // then restore foreground color
                g2.setColor(foregroundColor);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void drawEditControls(Graphics2D g2) {
        Point2D pt = getCoordsA();
        if (turnout.isTurnoutTypeXover() || turnout.isTurnoutTypeSlip()) {
            if (turnout.getConnectA() == null) {
                g2.setColor(Color.magenta);
            } else {
                g2.setColor(Color.blue);
            }
        } else {
            if (turnout.getConnectA() == null) {
                g2.setColor(Color.red);
            } else {
                g2.setColor(Color.green);
            }
        }
        g2.draw(turnout.layoutEditor.layoutEditorControlRectAt(pt));

        pt = getCoordsB();
        if (turnout.getConnectB() == null) {
            g2.setColor(Color.red);
        } else {
            g2.setColor(Color.green);
        }
        g2.draw(turnout.layoutEditor.layoutEditorControlRectAt(pt));

        pt = getCoordsC();
        if (turnout.getConnectC() == null) {
            g2.setColor(Color.red);
        } else {
            g2.setColor(Color.green);
        }
        g2.draw(turnout.layoutEditor.layoutEditorControlRectAt(pt));

        if (turnout.isTurnoutTypeXover() || turnout.isTurnoutTypeSlip()) {
            pt = getCoordsD();
            if (turnout.getConnectD() == null) {
                g2.setColor(Color.red);
            } else {
                g2.setColor(Color.green);
            }
            g2.draw(turnout.layoutEditor.layoutEditorControlRectAt(pt));
        }
    }

    /**
     * Draw track decorations.
     * <p>
     * This type of track has none, so this method is empty.
     */
    @Override
    protected void drawDecorations(Graphics2D g2) {
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutTurnoutView.class);
}
