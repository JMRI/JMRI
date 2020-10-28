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
 * MVC View component for the LayoutLHXOver class.
 *
 * @author Bob Jacobsen Copyright (c) 2020
 *
 */
public class LayoutLHXOverView extends LayoutXOverView {

    /**
     * Constructor method.
     *
     * @param xover the layout left hand crossover to view.
     */
    public LayoutLHXOverView(@Nonnull LayoutLHXOver xover) {
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

        //Point2D pAF = MathUtil.midPoint(pAM, pM);
        //Point2D pBF = MathUtil.midPoint(pBM, pM);
        //Point2D pCF = MathUtil.midPoint(pCM, pM);
        //Point2D pDF = MathUtil.midPoint(pDM, pM);

        int state = UNKNOWN;
        if (getLayoutEditor().isAnimating()) {
            state = getState();
        }

        //LayoutTurnout.TurnoutType turnoutType = getTurnoutType();
        // draw (rh & lh) cross overs
        //Point2D pAF = MathUtil.midPoint(pABM, pM);
        Point2D pBF = MathUtil.midPoint(pABM, pM);
        //Point2D pCF = MathUtil.midPoint(pCDM, pM);
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
        if (state != Turnout.CLOSED && state != INCONSISTENT) { // unknown or diverting path - crossed over
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

        // B, A, D, C end points (left and right)
        Point2D vBA = MathUtil.normalize(MathUtil.subtract(pA, pB), railDisplacement);
        double dirBA_DEG = MathUtil.computeAngleDEG(vBA);
        Point2D vBAo = MathUtil.orthogonal(MathUtil.normalize(vBA, railDisplacement));
        Point2D pBL = MathUtil.add(pB, vBAo);
        Point2D pBR = MathUtil.subtract(pB, vBAo);
        Point2D pAL = MathUtil.add(pA, vBAo);
        Point2D pAR = MathUtil.subtract(pA, vBAo);
        Point2D vDC = MathUtil.normalize(MathUtil.subtract(pC, pD), railDisplacement);
        Point2D vDCo = MathUtil.orthogonal(MathUtil.normalize(vDC, railDisplacement));
        Point2D pDL = MathUtil.subtract(pD, vDCo);
        Point2D pDR = MathUtil.add(pD, vDCo);
        Point2D pCL = MathUtil.subtract(pC, vDCo);
        Point2D pCR = MathUtil.add(pC, vDCo);

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
        double hypotF = railDisplacement / Math.sin(deltaABD_RAD / 2.0);
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
        //Point2D vS = MathUtil.normalize(vBA, 2.0);
        //Point2D vSo = MathUtil.orthogonal(vS);
        //Point2D pBSL = MathUtil.subtract(pBPL, vSo);
        //Point2D pBSR = MathUtil.add(pBPR, vSo);
        //Point2D pDSL = MathUtil.subtract(pDPL, vSo);
        //Point2D pDSR = MathUtil.add(pDPR, vSo);

        // end of switch rails (open at frogs)
        //Point2D pBAFS = MathUtil.add(pBFL, vSo);
        //Point2D pBAFSP = MathUtil.subtract(pBFL, vS);
        //Point2D pDCFS = MathUtil.subtract(pDFR, vSo);
        //Point2D pDCFSP = MathUtil.add(pDFR, vS);

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
                //g2.draw(new Line2D.Double(pBSR, pBAFSP));
            } else {                        // continuing path
                g2.draw(new Line2D.Double(pBPR, pBFL));
                //path = new GeneralPath();
                //path.moveTo(pBSL.getX(), pBSL.getY());
                //path.quadTo(pBAL.getX(), pBAL.getY(), pBAFS.getX(), pBAFS.getY());
                //g2.draw(path);
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
                //g2.draw(new Line2D.Double(pDSL, pDCFSP));
            } else {                        // continuing path
                g2.draw(new Line2D.Double(pDPL, pDFR));
                //path = new GeneralPath();
                //path.moveTo(pDSR.getX(), pDSR.getY());
                //path.quadTo(pDCR.getX(), pDCR.getY(), pDCFS.getX(), pDCFS.getY());
                //g2.draw(path);
            }
        }
    }   // draw2

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutLHXOverView.class);
}
