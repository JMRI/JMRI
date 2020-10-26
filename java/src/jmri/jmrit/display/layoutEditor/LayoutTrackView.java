package jmri.jmrit.display.layoutEditor;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.*;
import java.util.*;

import javax.annotation.*;

/**
 * MVC View component for the LayoutTrack hierarchy.
 *
 * @author Bob Jacobsen  Copyright (c) 2020
 *
 */
abstract public class LayoutTrackView {

    /**
     * Constructor method.
     * @param track the layout track to view.
     */
    public LayoutTrackView(@Nonnull LayoutTrack track) {
         this.layoutTrack = track;
    }

    // temporary method to get a correct-type *View or subclass.
    // Eventually, this will go away once *View's are created
    // in a type-specific way with their underlying model objects
    // @Deprecated // should be made not necessary
    @CheckForNull
    static public LayoutTrackView makeTrackView(@Nonnull LayoutTrack trk) {

        if (trk instanceof LayoutTurnout) {

            if (trk instanceof LayoutRHTurnout) { return new LayoutRHTurnoutView((LayoutRHTurnout)trk); }
            if (trk instanceof LayoutLHTurnout) { return new LayoutLHTurnoutView((LayoutLHTurnout)trk); }
            if (trk instanceof LayoutWye) { return new LayoutWyeView((LayoutWye)trk); }

            if (trk instanceof LayoutXOver) {
                if (trk instanceof LayoutRHXOver) { return new LayoutRHXOverView((LayoutRHXOver)trk); }
                if (trk instanceof LayoutLHXOver) { return new LayoutLHXOverView((LayoutLHXOver)trk); }
                if (trk instanceof LayoutDoubleXOver) { return new LayoutDoubleXOverView((LayoutDoubleXOver)trk); }

                return new LayoutXOverView((LayoutXOver)trk);
            }

            if (trk instanceof LayoutSlip) {
                if (trk instanceof LayoutSingleSlip) { return new LayoutSingleSlipView((LayoutSingleSlip)trk); }
                if (trk instanceof LayoutDoubleSlip) { return new LayoutDoubleSlipView((LayoutDoubleSlip)trk); }

                return new LayoutSlipView((LayoutSlip)trk);
            }

            return new LayoutTurnoutView((LayoutTurnout)trk);
        }
        if (trk instanceof TrackSegment) { return new TrackSegmentView((TrackSegment)trk); }
        if (trk instanceof PositionablePoint) { return new PositionablePointView((PositionablePoint)trk); }
        if (trk instanceof LevelXing) { return new LevelXingView((LevelXing)trk); }
        if (trk instanceof LayoutTurntable) { return new LayoutTurntableView((LayoutTurntable)trk); }

        log.error("makeTrackView did not match type of {}", trk, new Exception("traceback"));
        return null;
    }

    final private LayoutTrack layoutTrack;

    // These now reflect to code in the base class; eventually this heirarchy will
    // expand and the code will be brought here

    public boolean hasDecorations() {
        return layoutTrack.hasDecorations();
    }
    final public Point2D getCoordsCenter() { // final for efficiency
        return layoutTrack.getCoordsCenter();
    }
    @Nonnull
    final public String getId() {
        return layoutTrack.getId();
    }
    @Nonnull
    final public String getName() {
        return layoutTrack.getName();
    }
    public Map<String, String> getDecorations() {
        return layoutTrack.getDecorations();
    }
    public boolean isMainline() {
        return layoutTrack.isMainline();
    }

    public boolean isHidden() {
        return layoutTrack.isHidden();
    }

    /**
     * draw one line (Ballast, ties, center or 3rd rail, block lines)
     *
     * @param g2      the graphics context
     * @param isMain  true if drawing mainlines
     * @param isBlock true if drawing block lines
     */
    abstract protected void draw1(Graphics2D g2, boolean isMain, boolean isBlock);

    /**
     * draw two lines (rails)
     *
     * @param g2               the graphics context
     * @param isMain           true if drawing mainlines
     * @param railDisplacement the offset from center to draw the lines
     */
    abstract protected void draw2(Graphics2D g2, boolean isMain, float railDisplacement);

    /**
     * draw hidden track
     *
     * @param g2 the graphics context
     */
    // abstract protected void drawHidden(Graphics2D g2);
    // note: placeholder until I get this implemented in all sub-classes
    // TODO: replace with abstract declaration (above)
    final protected void drawHidden(Graphics2D g2) {
        // nothing to do here... move along...
    }


    /**
     * highlight unconnected connections
     *
     * @param g2           the graphics context
     * @param specificType the specific connection to draw (or NONE for all)
     */
    abstract protected void highlightUnconnected(Graphics2D g2, HitPointType specificType);

    // optional parameter specificType = NONE
    final protected void highlightUnconnected(Graphics2D g2) {
        highlightUnconnected(g2, HitPointType.NONE);
    }

    /**
     * draw the edit controls
     *
     * @param g2 the graphics context
     */
    abstract protected void drawEditControls(Graphics2D g2);

    /**
     * Draw the turnout controls
     *
     * @param g2 the graphics context
     */
    abstract protected void drawTurnoutControls(Graphics2D g2);

    /**
     * Draw the track decorations
     *
     * @param g2 the graphics context
     */
    abstract protected void drawDecorations(Graphics2D g2);

    final protected Color setColorForTrackBlock(Graphics2D g2,
            @CheckForNull LayoutBlock layoutBlock, boolean forceBlockTrackColor) {
        Color result = layoutTrack.getColorForTrackBlock(layoutBlock, forceBlockTrackColor);
        g2.setColor(result);
        return result;
    }

    // optional parameter forceTrack = false
    final protected Color setColorForTrackBlock(Graphics2D g2, @CheckForNull LayoutBlock lb) {
        return setColorForTrackBlock(g2, lb, false);
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutTrackView.class);
}
