package jmri.jmrit.display.layoutEditor;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.util.*;

/*
* This is an intermediate component used to put the Layout Editor
* into the component layers hierarchy so that objects can be drawn
* in front of or behind layout editor objects.
*
* @author George Warner Copyright (c) 2017-2018
 */
class LayoutEditorComponent extends JComponent {

    private final LayoutEditor layoutEditor;

    // Antialiasing rendering
    protected static final RenderingHints antialiasing = new RenderingHints(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);

    protected LayoutEditorComponent(@Nonnull final LayoutEditor LayoutEditor) {
        super();
        this.layoutEditor = LayoutEditor;
    }

    /*
    * {@inheritDoc}
     */
    @Override
    public void paint(Graphics g) {
        if (g instanceof Graphics2D) {
            // layoutEditor.draw((Graphics2D) g);

            Graphics2D g2 = (Graphics2D) g;

            if (clipBounds != null) {
                if (!clipBounds.isEmpty()) {
                    if ((clipBounds.getWidth() > 0) && (clipBounds.getHeight() > 0)) {
                        if (!clipBounds.equals(g2.getClipBounds())) {
                            //log.debug("LEComponent.paint(); clipBounds: {}, oldClipBounds: {}",
                            //        clipBounds, g2.getClipBounds());
                            g2.setClip(clipBounds);
                        }
                    }
                }
            }
            // Optional antialising, to eliminate (reduce) staircase on diagonal lines
            if (layoutEditor.getAntialiasingOn()) {
                g2.setRenderingHints(antialiasing);
            }

            // drawPositionableLabelBorder(g2);
            // things that only get drawn in edit mode
            if (layoutEditor.isEditable()) {
                if (layoutEditor.getDrawGrid()) {
                    drawPanelGrid(g2);
                }
                drawLayoutTracksHidden(g2);
            }

            drawShapes(g2, true);
            drawTrackSegmentsDashed(g2);
            drawLayoutTracksBallast(g2);
            drawLayoutTracksTies(g2);
            drawLayoutTracksRails(g2);
            drawLayoutTracksBlockLines(g2);

            drawPositionablePoints(g2, false);
            drawPositionablePoints(g2, true);

            drawShapes(g2, false);

            drawDecorations(g2);

            // things that only get drawn in edit mode
            if (layoutEditor.isEditable()) {
                drawLayoutTrackEditControls(g2);
                drawShapeEditControls(g2);

                drawMemoryRects(g2);
                drawBlockContentsRects(g2);

                if (layoutEditor.allControlling()) {
                    drawTurnoutControls(g2);
                }
                drawSelectionRect(g2);
                highLightSelection(g2);

                drawTrackSegmentInProgress(g2);
                drawShapeInProgress(g2);

                if (layoutEditor.isDrawLayoutTracksLabel()) {
                    drawLayoutTracksLabel(g2);
                }
                if (jmri.jmrit.vsdecoder.VSDecoderManager.instance().getVSDecoderPreferences().getShowTrainSymbolSetting()) {
                    for (jmri.jmrit.vsdecoder.VSDecoder vsd : jmri.jmrit.vsdecoder.VSDecoderManager.instance().getVSDecoderList()) {
                        vsd.draw(g2);
                    }
                }
            } else if (layoutEditor.getTurnoutCircles()) {
                if (layoutEditor.allControlling()) {
                    drawTurnoutControls(g2);
                }
            }
        } else {
            log.error("LayoutEditor drawing requires Graphics2D");
        }
    }

    private void drawPanelGrid(Graphics2D g2) {
        int wideMod = layoutEditor.gContext.getGridSize() * layoutEditor.gContext.getGridSize2nd();
        int wideMin = layoutEditor.gContext.getGridSize() / 2;

        // granulize puts these on getGridSize() increments
        int minX = 0;
        int minY = 0;
        int maxX = (int) MathUtil.granulize(layoutEditor.gContext.getLayoutWidth(), layoutEditor.gContext.getGridSize());
        int maxY = (int) MathUtil.granulize(layoutEditor.gContext.getLayoutHeight(), layoutEditor.gContext.getGridSize());

        log.debug("drawPanelGrid: minX: {}, minY: {}, maxX: {}, maxY: {}", minX, minY, maxX, maxY);

        Point2D startPt, stopPt;
        BasicStroke narrow = new BasicStroke(1.0F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        BasicStroke wide = new BasicStroke(2.0F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

        g2.setColor(Color.gray);
        g2.setStroke(narrow);

        // draw horizontal lines
        for (int y = minY; y <= maxY; y += layoutEditor.gContext.getGridSize()) {
            startPt = new Point2D.Double(minX, y);
            stopPt = new Point2D.Double(maxX, y);

            if ((y % wideMod) < wideMin) {
                g2.setStroke(wide);
                g2.draw(new Line2D.Double(startPt, stopPt));
                g2.setStroke(narrow);
            } else {
                g2.draw(new Line2D.Double(startPt, stopPt));
            }
        }

        // draw vertical lines
        for (int x = minX; x <= maxX; x += layoutEditor.gContext.getGridSize()) {
            startPt = new Point2D.Double(x, minY);
            stopPt = new Point2D.Double(x, maxY);

            if ((x % wideMod) < wideMin) {
                g2.setStroke(wide);
                g2.draw(new Line2D.Double(startPt, stopPt));
                g2.setStroke(narrow);
            } else {
                g2.draw(new Line2D.Double(startPt, stopPt));
            }
        }
    }

    //
    //  draw hidden layout tracks
    //
    private void drawLayoutTracksHidden(Graphics2D g2) {
        LayoutTrackDrawingOptions ltdo = layoutEditor.getLayoutTrackDrawingOptions();
        Stroke stroke = new BasicStroke(1.F);
        Stroke dashedStroke = new BasicStroke(1.F,
                BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10.F,
                new float[]{6.F, 4.F}, 0);

        // setup for drawing hidden sideline rails
        g2.setColor(ltdo.getSideRailColor());
        g2.setStroke(stroke);
        boolean main = false, block = false, hidden = true, dashed = false;
        draw1(g2, main, block, hidden, dashed);
        g2.setStroke(dashedStroke);
        draw1(g2, main, block, hidden, dashed = true);

        // setup for drawing mainline rails
        main = true;
        g2.setColor(ltdo.getMainRailColor());
        g2.setStroke(stroke);
        draw1(g2, main, block, hidden, dashed = false);
        g2.setStroke(dashedStroke);
        dashed = true;
        draw1(g2, main, block, hidden, dashed);
    }

    //
    //  draw dashed track segments
    //
    private void drawTrackSegmentsDashed(Graphics2D g2) {
        LayoutTrackDrawingOptions ltdo = layoutEditor.getLayoutTrackDrawingOptions();
        boolean main = false, block = false, hidden = false, dashed = true;

        if (ltdo.getSideRailCount() > 0) {
            // setup for drawing dashed sideline rails
            int railWidth = ltdo.getSideRailWidth();
            float[] dashArray = new float[]{6.F + railWidth, 4.F + railWidth};
            g2.setStroke(new BasicStroke(
                    railWidth,
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                    10.F, dashArray, 0));
            g2.setColor(ltdo.getSideRailColor());
            if ((ltdo.getSideRailCount() & 1) == 1) {
                draw1(g2, main, block, hidden, dashed);
            }
            if (ltdo.getSideRailCount() >= 2) {
                float railDisplacement = railWidth + (ltdo.getSideRailGap() / 2.F);
                draw2(g2, main, railDisplacement, dashed);
            }
        }

        if (ltdo.getMainRailCount() > 0) {
            // setup for drawing dashed mainline rails
            main = true;
            int railWidth = ltdo.getMainRailWidth();
            float[] dashArray = new float[]{6.F + railWidth, 4.F + railWidth};
            g2.setStroke(new BasicStroke(
                    railWidth,
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                    10.F, dashArray, 0));
            g2.setColor(ltdo.getMainRailColor());
            if ((ltdo.getMainRailCount() & 1) == 1) {
                draw1(g2, main, block, hidden, dashed);
            }
            if (ltdo.getMainRailCount() >= 2) {
                float railDisplacement = railWidth + (ltdo.getSideRailGap() / 2.F);
                draw2(g2, main, railDisplacement, dashed);
            }
        }
    }   // drawTrackSegmentsDashed

    //
    // draw layout track ballast
    //
    private void drawLayoutTracksBallast(Graphics2D g2) {
        LayoutTrackDrawingOptions ltdo = layoutEditor.getLayoutTrackDrawingOptions();
        boolean main = false, block = false, hidden = false, dashed = false;

        // setup for drawing sideline ballast
        int ballastWidth = ltdo.getSideBallastWidth();
        if (ballastWidth > 0) {
            g2.setStroke(new BasicStroke(ballastWidth,
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(ltdo.getSideBallastColor());
            draw1(g2, main, block, hidden, dashed);
        }

        // setup for drawing mainline ballast
        ballastWidth = ltdo.getMainBallastWidth();
        if (ballastWidth > 0) {
            g2.setStroke(new BasicStroke(ballastWidth,
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(ltdo.getMainBallastColor());
            main = true;
            draw1(g2, main, block, hidden, dashed);
        }
    }

    //
    // draw layout track ties
    //
    private void drawLayoutTracksTies(Graphics2D g2) {
        LayoutTrackDrawingOptions ltdo = layoutEditor.getLayoutTrackDrawingOptions();

        // setup for drawing sideline ties
        int tieLength = ltdo.getSideTieLength();
        int tieWidth = ltdo.getSideTieWidth();
        int tieGap = ltdo.getSideTieGap();
        if ((tieLength > 0) && (tieWidth > 0) && (tieGap > 0)) {
            g2.setStroke(new BasicStroke(tieLength,
                    BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10.F,
                    new float[]{tieWidth, tieGap}, 0));
            g2.setColor(ltdo.getSideTieColor());
            draw1(g2, false);  // main = false
        }

        // setup for drawing mainline ties
        tieLength = ltdo.getMainTieLength();
        tieWidth = ltdo.getMainTieWidth();
        tieGap = ltdo.getMainTieGap();
        if ((tieLength > 0) && (tieWidth > 0) && (tieGap > 0)) {
            g2.setStroke(new BasicStroke(tieLength,
                    BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10.F,
                    new float[]{tieWidth, tieGap}, 0));
            g2.setColor(ltdo.getMainTieColor());
            draw1(g2, true); // main = true
        }
    }

    //
    // draw layout track rails
    //
    private void drawLayoutTracksRails(Graphics2D g2) {
        LayoutTrackDrawingOptions ltdo = layoutEditor.getLayoutTrackDrawingOptions();
        int railWidth = ltdo.getSideRailWidth();
        Color railColor = ltdo.getSideRailColor();

        boolean main = false, block = false, hidden = false, dashed = false;

        if (ltdo.getSideRailCount() > 1) {
            // setup for drawing sideline rails
            float railDisplacement = railWidth + (ltdo.getSideRailGap() / 2.F);
            g2.setStroke(new BasicStroke(railWidth,
                    BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
            g2.setColor(railColor);
            draw2(g2, main, railDisplacement);
        }

        if ((ltdo.getSideRailCount() & 1) == 1) {
            // setup for drawing sideline rails
            g2.setStroke(new BasicStroke(
                    railWidth,
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(railColor);
            draw1(g2, main, block, hidden, dashed);
        }

        main = true;

        railWidth = ltdo.getMainRailWidth();
        railColor = ltdo.getMainRailColor();
        if (ltdo.getMainRailCount() > 1) {
            // setup for drawing mainline rails
            float railDisplacement = railWidth + (ltdo.getMainRailGap() / 2.F);
            g2.setStroke(new BasicStroke(railWidth,
                    BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
            g2.setColor(railColor);
            draw2(g2, main, railDisplacement);
        }
        if ((ltdo.getMainRailCount() & 1) == 1) {
            // setup for drawing mainline rails
            g2.setStroke(new BasicStroke(
                    railWidth,
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(railColor);
            dashed = false;
            draw1(g2, main, block, hidden, dashed);
        }
    }   // drawLayoutTracksRails

    //
    // draw layout track block lines
    //
    private void drawLayoutTracksBlockLines(Graphics2D g2) {
        LayoutTrackDrawingOptions ltdo = layoutEditor.getLayoutTrackDrawingOptions();

        // setup for drawing sideline block lines
        int blockLineWidth = ltdo.getSideBlockLineWidth();
        float[] dashArray = new float[]{6.F + blockLineWidth, 4.F + blockLineWidth};

        Stroke blockLineStroke;
        int dashPercentageX10 = ltdo.getSideBlockLineDashPercentageX10();
        if (dashPercentageX10 > 0) {
            float[] blockLineDashArray = new float[]{
                dashPercentageX10 + blockLineWidth,
                10.F - dashPercentageX10 + blockLineWidth};
            blockLineStroke = new BasicStroke(
                    blockLineWidth,
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                    10.F, blockLineDashArray, 0);
            g2.setStroke(blockLineStroke);
        } else {
            blockLineStroke = new BasicStroke(
                    blockLineWidth,
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            g2.setStroke(new BasicStroke(
                    blockLineWidth,
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                    10.F, dashArray, 0));
        }

        // note: color is set in layout track's draw1 when isBlock is true
        boolean main = false, block = true, hidden = false, dashed = true;
        draw1(g2, main, block, hidden, dashed);
        g2.setStroke(blockLineStroke);
        draw1(g2, main, block, hidden, dashed = false);

        // setup for drawing mainline block lines
        blockLineWidth = ltdo.getMainBlockLineWidth();
        dashArray = new float[]{6.F + blockLineWidth, 4.F + blockLineWidth};

        dashPercentageX10 = ltdo.getMainBlockLineDashPercentageX10();
        if (dashPercentageX10 > 0) {
            float[] blockLineDashArray = new float[]{
                dashPercentageX10 + blockLineWidth,
                10 - dashPercentageX10 + blockLineWidth};
            blockLineStroke = new BasicStroke(
                    blockLineWidth,
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                    10.F, blockLineDashArray, 0);
            g2.setStroke(blockLineStroke);
        } else {
            blockLineStroke = new BasicStroke(
                    blockLineWidth,
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            g2.setStroke(new BasicStroke(
                    blockLineWidth,
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                    10.F, dashArray, 0));
        }
        // note: color is set in layout track's draw1 when isBlock is true
        draw1(g2, main = true, block, hidden, dashed = true);
        g2.setStroke(blockLineStroke);
        dashed = false;
        draw1(g2, main, block, hidden, dashed);
    }

    // isDashed defaults to false
    private void draw1(Graphics2D g2,
            boolean isMain,
            boolean isBlock,
            boolean isHidden) {
        draw1(g2, isMain, isBlock, isHidden, false);
    }

    // isHidden defaults to false
    private void draw1(Graphics2D g2,
            boolean isMain,
            boolean isBlock) {
        draw1(g2, isMain, isBlock, false);
    }

    // isBlock defaults to false
    private void draw1(Graphics2D g2, boolean isMain) {
        draw1(g2, isMain, false);
    }

    // draw single line (ballast, ties & block lines)
    private void draw1(Graphics2D g2,
            boolean isMain,
            boolean isBlock,
            boolean isHidden,
            boolean isDashed) {
        for (LayoutTrackView layoutTrackView : layoutEditor.getLayoutTrackViews()) {
            if (!(layoutTrackView instanceof PositionablePointView)) {
                if (isHidden == layoutTrackView.isHidden()) {
                    if ((layoutTrackView instanceof TrackSegmentView)) {
                        if (((TrackSegmentView) layoutTrackView).isDashed() == isDashed) {
                            layoutTrackView.draw1(g2, isMain, isBlock);
                        }
                    } else if (!isDashed) {
                        layoutTrackView.draw1(g2, isMain, isBlock);
                    }
                }
            }
        }
    }

    // draw positionable points
    private void drawPositionablePoints(Graphics2D g2, boolean isMain) {
        for (PositionablePointView positionablePointView : layoutEditor.getPositionablePointViews()) {
            positionablePointView.draw1(g2, isMain, false);
        }
    }

    // isDashed defaults to false
    private void draw2(Graphics2D g2, boolean isMain, float railDisplacement) {
        draw2(g2, isMain, railDisplacement, false);
    }

    // draw parallel lines (rails)
    private void draw2(Graphics2D g2, boolean isMain,
            float railDisplacement, boolean isDashed) {
        for (LayoutTrackView layoutTrackView : layoutEditor.getLayoutTrackViews()) {
            if ((layoutTrackView instanceof TrackSegmentView)) {
                if (((TrackSegmentView) layoutTrackView).isDashed() == isDashed) {
                    layoutTrackView.draw2(g2, isMain, railDisplacement);
                }
            } else if (!isDashed) {
                layoutTrackView.draw2(g2, isMain, railDisplacement);
            }
        }
    }

    // draw decorations
    private void drawDecorations(Graphics2D g2) {
        layoutEditor.getLayoutTrackViews().forEach((tr) -> tr.drawDecorations(g2));
    }

    // draw shapes
    private void drawShapes(Graphics2D g2, boolean isBackground) {
        layoutEditor.getLayoutShapes().forEach((s) -> {
            if (isBackground == (s.getLevel() < 3)) {
                s.draw(g2);
            }
        });
    }

    // draw track segment (in progress)
    private void drawTrackSegmentInProgress(Graphics2D g2) {
        // check for segment in progress
        if (layoutEditor.isEditable() && (layoutEditor.beginTrack != null) && layoutEditor.getLayoutEditorToolBarPanel().trackButton.isSelected()) {
            g2.setColor(layoutEditor.defaultTrackColor);
            g2.setStroke(new BasicStroke(layoutEditor.gContext.getSidelineTrackWidth(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
            g2.draw(new Line2D.Double(layoutEditor.beginLocation, layoutEditor.currentLocation));

            // highlight unconnected endpoints of all tracks
            Color highlightColor = ColorUtil.setAlpha(Color.red, 0.25);
            Color connectColor = ColorUtil.setAlpha(Color.green, 0.5);
            g2.setColor(highlightColor);
            g2.setStroke(new BasicStroke(1.0F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (LayoutTrack lt : layoutEditor.getLayoutTracks()) {
                if (lt != layoutEditor.beginTrack) {
                    LayoutTrackView ltv = layoutEditor.getLayoutTrackView(lt);
                    if (lt == layoutEditor.foundTrack) {
                        ltv.highlightUnconnected(g2);
                        g2.setColor(connectColor);
                        ltv.highlightUnconnected(g2, layoutEditor.foundHitPointType);
                        g2.setColor(highlightColor);
                    } else {
                        ltv.highlightUnconnected(g2);
                    }
                }
            }
        }
    }

    // draw shape (in progress)
    private void drawShapeInProgress(Graphics2D g2) {
        // check for segment in progress
        if (layoutEditor.getLayoutEditorToolBarPanel().shapeButton.isSelected()) {
            // log.warn("drawShapeInProgress: selectedObject: " + selectedObject);
            if ((layoutEditor.selectedObject != null)) {
                g2.setColor(Color.DARK_GRAY);
                g2.setStroke(new BasicStroke(3.0F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
                g2.draw(new Line2D.Double(layoutEditor.beginLocation, layoutEditor.currentLocation));
            }
        }
    }

    // draw layout track edit controls
    private void drawLayoutTrackEditControls(Graphics2D g2) {
        g2.setStroke(new BasicStroke(1.0F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));

        layoutEditor.getLayoutTrackViews().forEach((tr) -> tr.drawEditControls(g2));
    }

    private void drawShapeEditControls(Graphics2D g2) {
        g2.setStroke(new BasicStroke(1.0F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        layoutEditor.getLayoutShapes().forEach((s) -> s.drawEditControls(g2));
    }

    // draw layout turnout controls
    private void drawTurnoutControls(Graphics2D g2) {
        g2.setStroke(new BasicStroke(1.0F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        g2.setColor(layoutEditor.turnoutCircleColor);
        g2.setBackground(layoutEditor.turnoutCircleThrownColor);

        // loop over all turnouts
        boolean editable = layoutEditor.isEditable();
        layoutEditor.getLayoutTrackViews().forEach((tr) -> {
            if (tr instanceof LayoutTurnoutView) {  //<== this includes LayoutSlips
                LayoutTurnoutView lt = (LayoutTurnoutView) tr;
                if (editable || !(lt.isHidden() || lt.isDisabled())) {
                    lt.drawTurnoutControls(g2);
                }
            } else if (tr instanceof LayoutTurntableView) {
                LayoutTurntableView lt = (LayoutTurntableView) tr;
                if (editable || !lt.isHidden()) {
                    lt.drawTurnoutControls(g2);
                }
            }
        });
    }

    private void drawSelectionRect(Graphics2D g2) {
        if (layoutEditor.selectionActive && (layoutEditor.selectionWidth != 0.0) && (layoutEditor.selectionHeight != 0.0)) {
            // The Editor super class draws a dashed red selection rectangle...
            // We're going to also draw a non-dashed yellow selection rectangle...
            // This could be code stripped if the super-class implementation is "good enough"
            Stroke stroke = g2.getStroke();
            Color color = g2.getColor();

            g2.setColor(new Color(204, 207, 88));
            g2.setStroke(new BasicStroke(3.0F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));

            g2.draw(layoutEditor.getSelectionRect());    // this sets _selectRect also

            g2.setColor(color);
            g2.setStroke(stroke);
        } else {
            layoutEditor.setSelectRect(null); // and clear it to turn it off
        }
    }

    private void drawMemoryRects(Graphics2D g2) {
        g2.setColor(layoutEditor.defaultTrackColor);
        g2.setStroke(new BasicStroke(1.0F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));

        layoutEditor.getMemoryLabelList().forEach((l) -> g2.draw(new Rectangle2D.Double(l.getX(), l.getY(), l.getSize().width, l.getSize().height)));
    }

    private void drawBlockContentsRects(Graphics2D g2) {
        g2.setColor(layoutEditor.defaultTrackColor);
        g2.setStroke(new BasicStroke(1.0F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));

        layoutEditor.getBlockContentsLabelList().forEach((l) -> g2.draw(new Rectangle2D.Double(l.getX(), l.getY(), l.getSize().width, l.getSize().height)));
    }

    private void highLightSelection(Graphics2D g) {
        Stroke stroke = g.getStroke();
        Color color = g.getColor();
        g.setColor(new Color(204, 207, 88));
        g.setStroke(new BasicStroke(2.0f));

        layoutEditor.getPositionalSelection().forEach((c) -> g.drawRect(c.getX(), c.getY(), c.maxWidth(), c.maxHeight()));

        layoutEditor._layoutTrackSelection.stream().map((lt) -> {
            LayoutTrackView ltv = layoutEditor.getLayoutTrackView(lt);
            Rectangle2D r = ltv.getBounds();
            if (r.isEmpty()) {
                r = MathUtil.inset(r, -4.0);
            }
            //r = MathUtil.centerRectangleOnPoint(r, ltv.getCoordsCenter());
            return r;
        }).forEachOrdered(g::draw);

        layoutEditor._layoutShapeSelection.stream().map((ls) -> {
            Rectangle2D r = ls.getBounds();
            if (r.isEmpty()) {
                r = MathUtil.inset(r, -4.0);
            }
            //r = MathUtil.centerRectangleOnPoint(r, ls.getCoordsCenter());
            return r;
        }).forEachOrdered(g::draw);

        g.setColor(color);
        g.setStroke(stroke);
    }

    private void drawLayoutTracksLabel(Graphics2D g) {
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
        g.setColor(Color.red);
        for (LayoutTrackView layoutTrackView : layoutEditor.getLayoutTrackViews()) {
            layoutTrackView.drawLayoutTrackText(g);
        }
    }

    /*
    * {@inheritDoc}
     */
    @Override
    public Rectangle getBounds() {
        JComponent targetPanel = layoutEditor.getTargetPanel();
        Rectangle2D targetBounds = targetPanel.getBounds();
        Container parent = getParent();
        if (parent != null) {   // if there is a parent...
            Rectangle2D parentBounds = parent.getBounds();

            // convert our origin to parent coordinates
            Point2D origin = new Point2D.Double(
                    targetBounds.getX() - parentBounds.getX(),
                    targetBounds.getY() - parentBounds.getY());

            return new Rectangle((int) origin.getX(), (int) origin.getY(),
                    (int) targetBounds.getWidth(), (int) targetBounds.getHeight());
        } else {
            return MathUtil.rectangle2DToRectangle(targetBounds);
        }
    }

    /*
    * {@inheritDoc}
     */
    @Override
    public Rectangle getBounds(Rectangle rv) {
        rv.setBounds(getBounds());
        return rv;
    }

    /*
    * {@inheritDoc}
     */
    @Override
    public int getX() {
        Rectangle bounds = getBounds();
        return (int) bounds.getX();
    }

    /*
    * {@inheritDoc}
     */
    @Override
    public int getY() {
        Rectangle bounds = getBounds();
        return (int) bounds.getY();
    }

    /*
    * {@inheritDoc}
     */
    @Override
    public int getWidth() {
        Rectangle bounds = getBounds();
        return (int) bounds.getWidth();
    }

    /*
    * {@inheritDoc}
     */
    @Override
    public int getHeight() {
        Rectangle bounds = getBounds();
        return (int) bounds.getHeight();
    }

    private Rectangle2D clipBounds = null;

    public void setClip(Rectangle2D clipBounds) {
        this.clipBounds = clipBounds;
    }

    // initialize logging
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutEditorComponent.class);
}
