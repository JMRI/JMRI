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
 * MVC View component for the LayoutDoubleXOver class.
 *
 * @author Bob Jacobsen Copyright (c) 2020
 *
 */
public class LayoutDoubleXOverView extends LayoutXOverView {

    /**
     * Constructor method.
     *
     * @param xover the layout double crossover to view.
     */
    public LayoutDoubleXOverView(@Nonnull LayoutDoubleXOver xover) {
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
        if (getLayoutEditor().isAnimating()) {
            state = getState();
        }

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

        Point2D vBM = MathUtil.normalize(MathUtil.subtract(pB, pM));
        double dirBM_DEG = MathUtil.computeAngleDEG(vBM);
        //Point2D vBMo = MathUtil.normalize(MathUtil.orthogonal(vBM), railDisplacement);
        //Point2D pBL = MathUtil.subtract(pB, vBMo);
        //Point2D pBR = MathUtil.add(pB, vBMo);
        //Point2D pMR = MathUtil.add(pM, vBMo);

        Point2D vCM = MathUtil.normalize(MathUtil.subtract(pC, pM));
        double dirCM_DEG = MathUtil.computeAngleDEG(vCM);

        //Point2D vCMo = MathUtil.normalize(MathUtil.orthogonal(vCM), railDisplacement);
        //Point2D pCL = MathUtil.subtract(pC, vCMo);
        //Point2D pCR = MathUtil.add(pC, vCMo);
        //Point2D pML = MathUtil.subtract(pM, vBMo);

        double deltaBMC_DEG = MathUtil.absDiffAngleDEG(dirBM_DEG, dirCM_DEG);
        double deltaBMC_RAD = Math.toRadians(deltaBMC_DEG);

        double hypotF = railDisplacement / Math.sin(deltaBMC_RAD / 2.0);

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
        Point2D pAP = MathUtil.subtract(pAM, vABF);
        Point2D pAPL = MathUtil.subtract(pAP, vABo);
        Point2D pAPR = MathUtil.add(pAP, vABo);
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
        //Point2D vS = MathUtil.normalize(vABo, 2.0);
        //Point2D pASL = MathUtil.add(pAPL, vS);
        //Point2D pASR = MathUtil.subtract(pAPR, vS);
        //Point2D pBSL = MathUtil.add(pBPL, vS);
        //Point2D pBSR = MathUtil.subtract(pBPR, vS);
        //Point2D pCSR = MathUtil.subtract(pCPR, vS);
        //Point2D pCSL = MathUtil.add(pCPL, vS);
        //Point2D pDSR = MathUtil.subtract(pDPR, vS);
        //Point2D pDSL = MathUtil.add(pDPL, vS);

        // end of switch rails (open at frogs)
        //Point2D pAFS = MathUtil.subtract(pAFL, vS);
        //Point2D pBFS = MathUtil.subtract(pBFL, vS);
        //Point2D pCFS = MathUtil.add(pCFR, vS);
        //Point2D pDFS = MathUtil.add(pDFR, vS);

        // vSo = MathUtil.orthogonal(vS);
        //Point2D pAFSR = MathUtil.add(pAFL, vSo);
        //Point2D pBFSR = MathUtil.subtract(pBFL, vSo);
        //Point2D pCFSL = MathUtil.subtract(pCFR, vSo);
        //Point2D pDFSL = MathUtil.add(pDFR, vSo);
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
                //g2.draw(new Line2D.Double(pASR, pAFSR));
            } else {                        // continuing path
                g2.draw(new Line2D.Double(pAPR, pAFL));
                //path = new GeneralPath();
                //path.moveTo(pASL.getX(), pASL.getY());
                //path.quadTo(pAML.getX(), pAML.getY(), pAFS.getX(), pAFS.getY());
                //g2.draw(path);
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
                //g2.draw(new Line2D.Double(pBSR, pBFSR));
            } else {
                g2.draw(new Line2D.Double(pBPR, pBFL));
                //path = new GeneralPath();
                //path.moveTo(pBSL.getX(), pBSL.getY());
                //path.quadTo(pBML.getX(), pBML.getY(), pBFS.getX(), pBFS.getY());
                //g2.draw(path);
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
                //g2.draw(new Line2D.Double(pCSL, pCFSL));
            } else {
                g2.draw(new Line2D.Double(pCPL, pCFR));
                //path = new GeneralPath();
                //path.moveTo(pCSR.getX(), pCSR.getY());
                //path.quadTo(pCMR.getX(), pCMR.getY(), pCFS.getX(), pCFS.getY());
                //g2.draw(path);
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
                //g2.draw(new Line2D.Double(pDSL, pDFSL));
            } else {
                g2.draw(new Line2D.Double(pDPL, pDFR));
                //path = new GeneralPath();
                //path.moveTo(pDSR.getX(), pDSR.getY());
                //path.quadTo(pDMR.getX(), pDMR.getY(), pDFS.getX(), pDFS.getY());
                //g2.draw(path);
            }
        }
    }   // draw2

    //private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutDoubleXOverView.class);
}
