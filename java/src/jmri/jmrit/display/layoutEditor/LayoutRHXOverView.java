package jmri.jmrit.display.layoutEditor;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.*;

import javax.annotation.*;

import jmri.Turnout;
import static jmri.jmrit.display.layoutEditor.LayoutTurnout.INCONSISTENT;
import static jmri.jmrit.display.layoutEditor.LayoutTurnout.UNKNOWN;
import jmri.util.MathUtil;

/**
 * MVC View component for the LayoutRHXOver class.
 *
 * @author Bob Jacobsen Copyright (c) 2020
 *
 */
public class LayoutRHXOverView extends LayoutXOverView {

    /**
     * Constructor method.
     *
     * @param xover the layout right hand crossover to view.
     */
    public LayoutRHXOverView(@Nonnull LayoutRHXOver xover) {
        super(xover);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void draw1(Graphics2D g2, boolean isMain, boolean isBlock) {
        if (isBlock && getLayoutBlock() == null) {
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

        boolean drawUnselectedLeg = getLayoutEditor().isTurnoutDrawUnselectedLeg();

        Color color = g2.getColor();

        // if this isn't a block line all these will be the same color
        Color colorA = color;
        Color colorB = color;
        Color colorC = color;
        Color colorD = color;

        if (isBlock) {
            LayoutBlock lb = getLayoutBlock();
            colorA = (lb == null) ? color : lb.getBlockColor();
            lb = getLayoutBlockB();
            colorB = (lb == null) ? color : lb.getBlockColor();
            lb = getLayoutBlockC();
            colorC = (lb == null) ? color : lb.getBlockColor();
            lb = getLayoutBlockD();
            colorD = (lb == null) ? color : lb.getBlockColor();
        }

        // middles
        Point2D pM = getCoordsCenter();
        Point2D pABM = MathUtil.midPoint(pA, pB);
        Point2D pAM = MathUtil.lerp(pA, pABM, 5.0 / 8.0);
        //Point2D pAMP = MathUtil.midPoint(pAM, pABM);
        Point2D pBM = MathUtil.lerp(pB, pABM, 5.0 / 8.0);
        //Point2D pBMP = MathUtil.midPoint(pBM, pABM);

        Point2D pCDM = MathUtil.midPoint(pC, pD);
        Point2D pCM = MathUtil.lerp(pC, pCDM, 5.0 / 8.0);
        //Point2D pCMP = MathUtil.midPoint(pCM, pCDM);
        Point2D pDM = MathUtil.lerp(pD, pCDM, 5.0 / 8.0);
        //Point2D pDMP = MathUtil.midPoint(pDM, pCDM);

//        Point2D pAF = MathUtil.midPoint(pAM, pM);
//        Point2D pBF = MathUtil.midPoint(pBM, pM);
//        Point2D pCF = MathUtil.midPoint(pCM, pM);
//        Point2D pDF = MathUtil.midPoint(pDM, pM);

        int state = UNKNOWN;
        if (getLayoutEditor().isAnimating()) {
            state = getState();
        }

        LayoutTurnout.TurnoutType turnoutType = getTurnoutType();
        // draw (rh & lh) cross overs
        Point2D pAF = MathUtil.midPoint(pABM, pM);
        Point2D pBF = MathUtil.midPoint(pABM, pM);
        Point2D pCF = MathUtil.midPoint(pCDM, pM);
        Point2D pDF = MathUtil.midPoint(pCDM, pM);
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
                if (isMain == mainlineA) {
                    g2.setColor(colorA);
                    g2.draw(new Line2D.Double(pAF, pM));
                }
                if (isMain == mainlineC) {
                    g2.setColor(colorC);
                    g2.draw(new Line2D.Double(pCF, pM));
                }
            }
        }
        if (state != Turnout.CLOSED && state != INCONSISTENT) { // unknown or diverting path - crossed over
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
                if (turnoutType == LayoutTurnout.TurnoutType.RH_XOVER) {
                    if (isMain == mainlineA) {
                        g2.setColor(colorA);
                        g2.draw(new Line2D.Double(pAF, pM));
                    }
                    if (isMain == mainlineC) {
                        g2.setColor(colorC);
                        g2.draw(new Line2D.Double(pCF, pM));
                    }
                } else if (turnoutType == LayoutTurnout.TurnoutType.LH_XOVER) {
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
    }   // draw1

    /**
     * {@inheritDoc}
     */
    @Override
    protected void draw2(Graphics2D g2, boolean isMain, float railDisplacement) {
        //LayoutTurnout.TurnoutType turnoutType = getTurnoutType();

        Point2D pA = getCoordsA();
        Point2D pB = getCoordsB();
        Point2D pC = getCoordsC();
        Point2D pD = getCoordsD();
        Point2D pM = getCoordsCenter();

        //Point2D vAM = MathUtil.normalize(MathUtil.subtract(pM, pA));
        //Point2D vAMo = MathUtil.orthogonal(MathUtil.normalize(vAM, railDisplacement));

        //Point2D pAL = MathUtil.subtract(pA, vAMo);
        //Point2D pAR = MathUtil.add(pA, vAMo);

        //Point2D vBM = MathUtil.normalize(MathUtil.subtract(pB, pM));
        //double dirBM_DEG = MathUtil.computeAngleDEG(vBM);
        //Point2D vBMo = MathUtil.normalize(MathUtil.orthogonal(vBM), railDisplacement);
        //Point2D pBL = MathUtil.subtract(pB, vBMo);
        //Point2D pBR = MathUtil.add(pB, vBMo);
        //Point2D pMR = MathUtil.add(pM, vBMo);

        //Point2D vCM = MathUtil.normalize(MathUtil.subtract(pC, pM));
        //double dirCM_DEG = MathUtil.computeAngleDEG(vCM);

        //Point2D vCMo = MathUtil.normalize(MathUtil.orthogonal(vCM), railDisplacement);
        //Point2D pCL = MathUtil.subtract(pC, vCMo);
        //Point2D pCR = MathUtil.add(pC, vCMo);
        //Point2D pML = MathUtil.subtract(pM, vBMo);

        //double deltaBMC_DEG = MathUtil.absDiffAngleDEG(dirBM_DEG, dirCM_DEG);
        //double deltaBMC_RAD = Math.toRadians(deltaBMC_DEG);

        //double hypotF = railDisplacement / Math.sin(deltaBMC_RAD / 2.0);

        //Point2D vDisF = MathUtil.normalize(MathUtil.add(vAM, vCM), hypotF);
        //if (turnoutType == LayoutTurnout.TurnoutType.WYE_TURNOUT) {
        //    vDisF = MathUtil.normalize(vAM, hypotF);
        //}
        //Point2D pF = MathUtil.add(pM, vDisF);

        //Point2D pFR = MathUtil.add(pF, MathUtil.multiply(vBMo, 2.0));
        //Point2D pFL = MathUtil.subtract(pF, MathUtil.multiply(vCMo, 2.0));

        //Point2D pFPR = MathUtil.add(pF, MathUtil.normalize(vBMo, 2.0));
        //Point2D pFPL = MathUtil.subtract(pF, MathUtil.normalize(vCMo, 2.0));
        //Point2D vDisAP = MathUtil.normalize(vAM, hypotF);
        //Point2D pAP = MathUtil.subtract(pM, vDisAP);
        //Point2D pAPR = MathUtil.add(pAP, vAMo);
        //Point2D pAPL = MathUtil.subtract(pAP, vAMo);

        //Point2D vSo = MathUtil.normalize(vAMo, 2.0);
        //Point2D pSL = MathUtil.add(pAPL, vSo);
        //Point2D pSR = MathUtil.subtract(pAPR, vSo);

        boolean mainlineA = isMainlineA();
        boolean mainlineB = isMainlineB();
        boolean mainlineC = isMainlineC();
        boolean mainlineD = isMainlineD();

        int state = UNKNOWN;
        if (getLayoutEditor().isAnimating()) {
            state = getState();
        }

        // A, B, C, D end points (left and right)
        Point2D vAB = MathUtil.normalize(MathUtil.subtract(pB, pA), railDisplacement);
        double dirAB_DEG = MathUtil.computeAngleDEG(vAB);
        Point2D vABo = MathUtil.orthogonal(MathUtil.normalize(vAB, railDisplacement));
        Point2D pAL = MathUtil.subtract(pA, vABo);
        Point2D pAR = MathUtil.add(pA, vABo);
        Point2D pBL = MathUtil.subtract(pB, vABo);
        Point2D pBR = MathUtil.add(pB, vABo);
        Point2D vCD = MathUtil.normalize(MathUtil.subtract(pD, pC), railDisplacement);
        Point2D vCDo = MathUtil.orthogonal(MathUtil.normalize(vCD, railDisplacement));
        Point2D pCL = MathUtil.add(pC, vCDo);
        Point2D pCR = MathUtil.subtract(pC, vCDo);
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
        double hypotF = railDisplacement / Math.sin(deltaBAC_RAD / 2.0);
        Point2D vF = MathUtil.normalize(MathUtil.add(vAB, vAC), hypotF);
        Point2D pABF = MathUtil.add(pABM, vF);
        Point2D pCDF = MathUtil.subtract(pCDM, vF);

        // frog primes
        Point2D pABFP = MathUtil.add(MathUtil.add(pABF, vACo), vACo);
        Point2D pCDFP = MathUtil.subtract(MathUtil.subtract(pCDF, vACo), vACo);

        // end of switch rails (closed)
        Point2D vABF = MathUtil.normalize(vAB, hypotF);
        Point2D pAP = MathUtil.subtract(pABM, vABF);
        Point2D pAPL = MathUtil.subtract(pAP, vABo);
        Point2D pAPR = MathUtil.add(pAP, vABo);
        Point2D pCP = MathUtil.add(pCDM, vABF);
        Point2D pCPL = MathUtil.add(pCP, vCDo);
        Point2D pCPR = MathUtil.subtract(pCP, vCDo);

        // end of switch rails (open)
        //Point2D vS = MathUtil.normalize(vAB, 2.0);
        //Point2D vSo = MathUtil.orthogonal(vS);
        //Point2D pASL = MathUtil.add(pAPL, vSo);
        //Point2D pASR = MathUtil.subtract(pAPR, vSo);
        //Point2D pCSL = MathUtil.add(pCPL, vSo);
        //Point2D pCSR = MathUtil.subtract(pCPR, vSo);

        // end of switch rails (open at frogs)
        //Point2D pABFS = MathUtil.subtract(pABF, vSo);
        //Point2D pABFSP = MathUtil.subtract(pABF, vS);
        //Point2D pCDFS = MathUtil.add(pCDF, vSo);
        //Point2D pCDFSP = MathUtil.add(pCDF, vS);

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
                //g2.draw(new Line2D.Double(pASR, pABFSP));
            } else {                        // continuing path
                g2.draw(new Line2D.Double(pAPR, pABF));
                //path = new GeneralPath();
                //path.moveTo(pASL.getX(), pASL.getY());
                //path.quadTo(pABL.getX(), pABL.getY(), pABFS.getX(), pABFS.getY());
                //g2.draw(path);
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
                //g2.draw(new Line2D.Double(pCSL, pCDFSP));
            } else {                        // continuing path
                g2.draw(new Line2D.Double(pCPL, pCDF));
                //path = new GeneralPath();
                //path.moveTo(pCSR.getX(), pCSR.getY());
                //path.quadTo(pCDR.getX(), pCDR.getY(), pCDFS.getX(), pCDFS.getY());
                //g2.draw(path);
            }
        }
        if (isMain == mainlineD) {
            g2.draw(new Line2D.Double(pCDR, pDR));
            g2.draw(new Line2D.Double(pCDF, pDL));
        }
    }   // draw2

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutRHXOverView.class);
}
