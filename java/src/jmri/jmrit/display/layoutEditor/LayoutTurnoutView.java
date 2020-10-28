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
 * @author Bob Jacobsen Copyright (c) 2020
 *
 */
public class LayoutTurnoutView extends LayoutTrackView {

    /**
     * Constructor method.
     *
     * @param turnout the layout turnout to create the view for.
     */
    public LayoutTurnoutView(@Nonnull LayoutTurnout turnout) {
        super(turnout);
        this.turnout = turnout;
    }

    final protected LayoutTurnout turnout;

    /*
    * these are convience getters for properties of LayoutTurnouts
     */
    protected LayoutEditor getLayoutEditor() {
        return turnout.layoutEditor;
    }

    protected TurnoutType getTurnoutType() {
        return turnout.getTurnoutType();
    }

    protected boolean isTurnoutTypeTurnout() {
        return turnout.isTurnoutTypeTurnout();
    }

    protected boolean isTurnoutTypeXover() {
        return turnout.isTurnoutTypeXover();
    }

    protected boolean isTurnoutTypeSlip() {
        return turnout.isTurnoutTypeSlip();
    }

    protected boolean hasEnteringSingleTrack() {
        return turnout.hasEnteringSingleTrack();
    }

    protected boolean hasEnteringDoubleTrack() {
        return turnout.hasEnteringDoubleTrack();
    }

    protected boolean isDisabled() {
        return turnout.isDisabled();
    }

    protected boolean isDisabledWhenOccupied() {
        return turnout.isDisabledWhenOccupied();
    }

    protected boolean isOccupied() {
        return turnout.isOccupied();
    }

    protected int getContinuingSense() {
        return turnout.getContinuingSense();
    }

    protected LayoutTrack getConnectA() {
        return turnout.getConnectA();
    }

    protected LayoutTrack getConnectB() {
        return turnout.getConnectB();
    }

    protected LayoutTrack getConnectC() {
        return turnout.getConnectC();
    }

    protected LayoutTrack getConnectD() {
        return turnout.getConnectD();
    }

    protected Point2D getCoordsA() {
        return turnout.getCoordsA();
    }

    protected Point2D getCoordsB() {
        return turnout.getCoordsB();
    }

    protected Point2D getCoordsC() {
        return turnout.getCoordsC();
    }

    protected Point2D getCoordsD() {
        return turnout.getCoordsD();
    }

    protected boolean isMainlineA() {
        return turnout.isMainlineA();
    }

    protected boolean isMainlineB() {
        return turnout.isMainlineB();
    }

    protected boolean isMainlineC() {
        return turnout.isMainlineC();
    }

    protected boolean isMainlineD() {
        return turnout.isMainlineD();
    }

    protected LayoutBlock getLayoutBlock() {
        return turnout.getLayoutBlock();
    }

    protected LayoutBlock getLayoutBlockB() {
        return turnout.getLayoutBlockB();
    }

    protected LayoutBlock getLayoutBlockC() {
        return turnout.getLayoutBlockC();
    }

    protected LayoutBlock getLayoutBlockD() {
        return turnout.getLayoutBlockD();
    }

    protected int getState() {
        return turnout.getState();
    }

    protected Ellipse2D trackControlCircleAt(@Nonnull Point2D inPoint) {
        return turnout.trackControlCircleAt(inPoint);
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
        //Point2D pD = getCoordsD();

        boolean mainlineA = isMainlineA();
        boolean mainlineB = isMainlineB();
        boolean mainlineC = isMainlineC();
        //boolean mainlineD = isMainlineD();

        boolean drawUnselectedLeg = getLayoutEditor().isTurnoutDrawUnselectedLeg();

        Color color = g2.getColor();

        // if this isn't a block line all these will be the same color
        Color colorA = color;
        Color colorB = color;
        Color colorC = color;
        //Color colorD = color;

        if (isBlock) {
            LayoutBlock lb = getLayoutBlock();
            colorA = (lb == null) ? color : lb.getBlockColor();
            lb = getLayoutBlockB();
            colorB = (lb == null) ? color : lb.getBlockColor();
            lb = getLayoutBlockC();
            colorC = (lb == null) ? color : lb.getBlockColor();
            //lb = getLayoutBlockD();
            //colorD = (lb == null) ? color : lb.getBlockColor();
        }

        // middles
        Point2D pM = getCoordsCenter();
        //Point2D pABM = MathUtil.midPoint(pA, pB);
        //Point2D pAM = MathUtil.lerp(pA, pABM, 5.0 / 8.0);
        //Point2D pAMP = MathUtil.midPoint(pAM, pABM);
        //Point2D pBM = MathUtil.lerp(pB, pABM, 5.0 / 8.0);
        //Point2D pBMP = MathUtil.midPoint(pBM, pABM);

        //Point2D pCDM = MathUtil.midPoint(pC, pD);
        //Point2D pCM = MathUtil.lerp(pC, pCDM, 5.0 / 8.0);
        //Point2D pCMP = MathUtil.midPoint(pCM, pCDM);
        //Point2D pDM = MathUtil.lerp(pD, pCDM, 5.0 / 8.0);
        //Point2D pDMP = MathUtil.midPoint(pDM, pCDM);

        //Point2D pAF = MathUtil.midPoint(pAM, pM);
        //Point2D pBF = MathUtil.midPoint(pBM, pM);
        //Point2D pCF = MathUtil.midPoint(pCM, pM);
        //Point2D pDF = MathUtil.midPoint(pDM, pM);

        int state = UNKNOWN;
        if (getLayoutEditor().isAnimating()) {
            state = getState();
        }

        //TurnoutType turnoutType = getTurnoutType();
        // LH, RH, or WYE Turnouts

        // draw A<===>center
        if (isMain == mainlineA) {
            g2.setColor(colorA);
            g2.draw(new Line2D.Double(pA, pM));
        }

        if (state == UNKNOWN || (getContinuingSense() == state && state != INCONSISTENT)) { // unknown or continuing path
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

        if (state == UNKNOWN || (getContinuingSense() != state && state != INCONSISTENT)) { // unknown or diverting path
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
    }   // draw1

    /**
     * {@inheritDoc}
     */
    @Override
    protected void draw2(Graphics2D g2, boolean isMain, float railDisplacement) {
        TurnoutType turnoutType = getTurnoutType();

        Point2D pA = getCoordsA();
        Point2D pB = getCoordsB();
        Point2D pC = getCoordsC();
        //Point2D pD = getCoordsD();
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
        //boolean mainlineD = isMainlineD();

        int state = UNKNOWN;
        if (getLayoutEditor().isAnimating()) {
            state = getState();
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
                    if (getContinuingSense() == state) {  // unknown or diverting path
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
                    if (getContinuingSense() != state) {  // unknown or diverting path
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
                    if (getContinuingSense() == state) {  // straight path
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
                    if (getContinuingSense() != state) {  // unknown or diverting path
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
                    if (getContinuingSense() != state) {  // unknown or diverting path
                        path = new GeneralPath();
                        path.moveTo(pAPR.getX(), pAPR.getY());
                        path.quadTo(pMR.getX(), pMR.getY(), pF.getX(), pF.getY());
                        g2.draw(path);
                        //} else {
                        //    path = new GeneralPath();
                        //    path.moveTo(pSR.getX(), pSR.getY());
                        //    path.quadTo(pMR.getX(), pMR.getY(), pFPR.getX(), pFPR.getY());
                        //    g2.draw(path);
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
                    if (getContinuingSense() != state) {  // unknown or diverting path
                        //path = new GeneralPath();
                        //path.moveTo(pSL.getX(), pSL.getY());
                        //path.quadTo(pML.getX(), pML.getY(), pFPL.getX(), pFPL.getY());
                        //g2.draw(path);
                    } else {
                        path = new GeneralPath();
                        path.moveTo(pAPL.getX(), pAPL.getY());
                        path.quadTo(pML.getX(), pML.getY(), pF.getX(), pF.getY());
                        g2.draw(path);
                    }
                }
                break;
            }   // case WYE_TURNOUT
            default: {
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
                && (getConnectA() == null)) {
            g2.fill(trackControlCircleAt(getCoordsA()));
        }

        if (((specificType == HitPointType.NONE) || (specificType == HitPointType.TURNOUT_B))
                && (getConnectB() == null)) {
            g2.fill(trackControlCircleAt(getCoordsB()));
        }

        if (((specificType == HitPointType.NONE) || (specificType == HitPointType.TURNOUT_C))
                && (getConnectC() == null)) {
            g2.fill(trackControlCircleAt(getCoordsC()));
        }
        if (isTurnoutTypeXover()) {
            if (((specificType == HitPointType.NONE) || (specificType == HitPointType.TURNOUT_D))
                    && (getConnectD() == null)) {
                g2.fill(trackControlCircleAt(getCoordsD()));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void drawTurnoutControls(Graphics2D g2) {
        if (!isDisabled() && !(isDisabledWhenOccupied() && isOccupied())) {
            Color foregroundColor = g2.getColor();
            // if turnout is not continuing state
            if (getState() != getContinuingSense()) {
                // then switch to background color
                g2.setColor(g2.getBackground());
            }
            if (getLayoutEditor().isTurnoutFillControlCircles()) {
                g2.fill(trackControlCircleAt(getCoordsCenter()));
            } else {
                g2.draw(trackControlCircleAt(getCoordsCenter()));
            }
            // if turnout is not continuing state
            if (getState() != getContinuingSense()) {
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
        if (hasEnteringDoubleTrack()) {
            if (getConnectA() == null) {
                g2.setColor(Color.magenta);
            } else {
                g2.setColor(Color.blue);
            }
        } else {
            if (getConnectA() == null) {
                g2.setColor(Color.red);
            } else {
                g2.setColor(Color.green);
            }
        }
        g2.draw(getLayoutEditor().layoutEditorControlRectAt(pt));

        pt = getCoordsB();
        if (getConnectB() == null) {
            g2.setColor(Color.red);
        } else {
            g2.setColor(Color.green);
        }
        g2.draw(getLayoutEditor().layoutEditorControlRectAt(pt));

        pt = getCoordsC();
        if (getConnectC() == null) {
            g2.setColor(Color.red);
        } else {
            g2.setColor(Color.green);
        }
        g2.draw(getLayoutEditor().layoutEditorControlRectAt(pt));

        if (hasEnteringDoubleTrack()) {
            pt = getCoordsD();
            if (getConnectD() == null) {
                g2.setColor(Color.red);
            } else {
                g2.setColor(Color.green);
            }
            g2.draw(getLayoutEditor().layoutEditorControlRectAt(pt));
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
