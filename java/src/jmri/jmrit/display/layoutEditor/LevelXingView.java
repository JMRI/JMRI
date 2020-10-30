package jmri.jmrit.display.layoutEditor;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.*;
import static java.lang.Math.PI;

import javax.annotation.*;

import jmri.util.*;

/**
 * MVC View component for the LevelXing class
 *
 * @author Bob Jacobsen  Copyright (c) 2020
 *
 */
public class LevelXingView extends LayoutTrackView {

    /**
     * Constructor method.
     * @param xing the level crossing.
     */
    public LevelXingView(@Nonnull LevelXing xing) {
        super(xing);
        this.xing = xing;
    }

    final private LevelXing xing;

    /**
     * Draw this level crossing.
     *
     * @param g2 the graphics port to draw to
     */
    @Override
    protected void draw1(Graphics2D g2, boolean isMain, boolean isBlock) {
        if (isMain == xing.isMainlineAC()) {
            if (isBlock) {
                setColorForTrackBlock(g2, xing.getLayoutBlockAC());
            }
            g2.draw(new Line2D.Double(xing.getCoordsA(), xing.getCoordsC()));
        }
        if (isMain == xing.isMainlineBD()) {
            if (isBlock) {
                setColorForTrackBlock(g2, xing.getLayoutBlockBD());
            }
            g2.draw(new Line2D.Double(xing.getCoordsB(), xing.getCoordsD()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void draw2(Graphics2D g2, boolean isMain, float railDisplacement) {
        Point2D pA = xing.getCoordsA();
        Point2D pB = xing.getCoordsB();
        Point2D pC = xing.getCoordsC();
        Point2D pD = xing.getCoordsD();
        Point2D pM = getCoordsCenter();

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

        double hypotK = railDisplacement / Math.cos((PI - deltaRAD) / 2.0);
        double hypotV = railDisplacement / Math.cos(deltaRAD / 2.0);

        log.debug("dir AC: {}, BD: {}, diff: {}", dirAC_DEG, dirBD_DEG, deltaDEG);

        Point2D vDisK = MathUtil.normalize(MathUtil.add(vAC, vBD), hypotK);
        Point2D vDisV = MathUtil.normalize(MathUtil.orthogonal(vDisK), hypotV);
        Point2D pKL = MathUtil.subtract(pM, vDisK);
        Point2D pKR = MathUtil.add(pM, vDisK);
        Point2D pVL = MathUtil.subtract(pM, vDisV);
        Point2D pVR = MathUtil.add(pM, vDisV);

        if (isMain == xing.isMainlineAC()) {
            // this is the *2.0 vector (rail gap) for the AC diamond parts
            Point2D vAC2 = MathUtil.normalize(vAC, 2.0);
            // KL toward C, VR toward A, VL toward C and KR toward A
            Point2D pKLtC = MathUtil.add(pKL, vAC2);
            Point2D pVRtA = MathUtil.subtract(pVR, vAC2);
            Point2D pVLtC = MathUtil.add(pVL, vAC2);
            Point2D pKRtA = MathUtil.subtract(pKR, vAC2);

            // draw right AC rail: AR====KL == VR====CR
            g2.draw(new Line2D.Double(pAR, pKL));
            g2.draw(new Line2D.Double(pKLtC, pVRtA));
            g2.draw(new Line2D.Double(pVR, pCR));

            // draw left AC rail: AL====VL == KR====CL
            g2.draw(new Line2D.Double(pAL, pVL));
            g2.draw(new Line2D.Double(pVLtC, pKRtA));
            g2.draw(new Line2D.Double(pKR, pCL));
        }
        if (isMain == xing.isMainlineBD()) {
            // this is the *2.0 vector (rail gap) for the BD diamond parts
            Point2D vBD2 = MathUtil.normalize(vBD, 2.0);
            // VR toward D, KR toward B, KL toward D and VL toward B
            Point2D pVRtD = MathUtil.add(pVR, vBD2);
            Point2D pKRtB = MathUtil.subtract(pKR, vBD2);
            Point2D pKLtD = MathUtil.add(pKL, vBD2);
            Point2D pVLtB = MathUtil.subtract(pVL, vBD2);

            // draw right BD rail: BR====VR == KR====DR
            g2.draw(new Line2D.Double(pBR, pVR));
            g2.draw(new Line2D.Double(pVRtD, pKRtB));
            g2.draw(new Line2D.Double(pKR, pDR));

            // draw left BD rail: BL====KL == VL====DL
            g2.draw(new Line2D.Double(pBL, pKL));
            g2.draw(new Line2D.Double(pKLtD, pVLtB));
            g2.draw(new Line2D.Double(pVL, pDL));
        }
    }

    /**
     * Draw track decorations.
     *
     * This type of track has none, so this method is empty.
     */
    @Override
    protected void drawDecorations(Graphics2D g2) {}

    /**
     * {@inheritDoc}
     */
    @Override
    protected void highlightUnconnected(Graphics2D g2, HitPointType specificType) {
        if (((specificType == HitPointType.NONE) || (specificType == HitPointType.LEVEL_XING_A))
                && (xing.getConnectA() == null)) {
            g2.fill(xing.trackControlCircleAt(xing.getCoordsA()));
        }

        if (((specificType == HitPointType.NONE) || (specificType == HitPointType.LEVEL_XING_B))
                && (xing.getConnectB() == null)) {
            g2.fill(xing.trackControlCircleAt(xing.getCoordsB()));
        }

        if (((specificType == HitPointType.NONE) || (specificType == HitPointType.LEVEL_XING_C))
                && (xing.getConnectC() == null)) {
            g2.fill(xing.trackControlCircleAt(xing.getCoordsC()));
        }

        if (((specificType == HitPointType.NONE) || (specificType == HitPointType.LEVEL_XING_D))
                && (xing.getConnectD() == null)) {
            g2.fill(xing.trackControlCircleAt(xing.getCoordsD()));
        }
    }

    @Override
    protected void drawEditControls(Graphics2D g2) {
        g2.setColor(xing.layoutEditor.getDefaultTrackColorColor());
        g2.draw(xing.trackEditControlCircleAt(getCoordsCenter()));

        if (xing.getConnectA() == null) {
            g2.setColor(Color.magenta);
        } else {
            g2.setColor(Color.blue);
        }
        g2.draw(xing.layoutEditor.layoutEditorControlRectAt(xing.getCoordsA()));

        if (xing.getConnectB() == null) {
            g2.setColor(Color.red);
        } else {
            g2.setColor(Color.green);
        }
        g2.draw(xing.layoutEditor.layoutEditorControlRectAt(xing.getCoordsB()));

        if (xing.getConnectC() == null) {
            g2.setColor(Color.red);
        } else {
            g2.setColor(Color.green);
        }
        g2.draw(xing.layoutEditor.layoutEditorControlRectAt(xing.getCoordsC()));

        if (xing.getConnectD() == null) {
            g2.setColor(Color.red);
        } else {
            g2.setColor(Color.green);
        }
        g2.draw(xing.layoutEditor.layoutEditorControlRectAt(xing.getCoordsD()));
    }

    @Override
    protected void drawTurnoutControls(Graphics2D g2) {
        // LevelXings don't have turnout controls...
        // nothing to see here... move along...
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LevelXingView.class);
}
