package jmri.jmrit.display.layoutEditor;

import java.util.List;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

/**
 * This interface serves as a manager for the overall layout model via
 * collections of i.e. LayoutTurnout, LayoutBlock,
 * PositionablePoint, Track Segment, LayoutSlip and LevelXing objects
 * along with their corresponding *View objects.
 * (Having *View objects here, which are specific to a panel, may
 * only be here as a temporary measure)
 * <p>
 * Provides a temporary setDirty()/isDirty() and redrawPanel() mechanism
 * for marking changes. That may have to grow and/or change. For
 * example, redrawPanel()  (which could be renamed) might fire listeners to cause repaints.
 * 
 *
 * @see LayoutEditorFindItems
 * @see LayoutEditorAuxTools
 *
 * @author Bob Jacobsen Copyright: (c) 2020
 */
public interface LayoutModels {

    /**
     * Check the dirty state
     *
     * @return true if contents of models have changed,
     */
    public boolean isDirty();

    public void setDirty();

    /**
     * A change has happen that might not need to be stored,
     * but should cause the presentation to be updated.
     */
    public void redrawPanel();

    // ====================================
    // Access to related navigation objects
    // ====================================

    @Nonnull
    public LayoutEditorAuxTools getLEAuxTools();
        

    // ====================================
    // Access to (lists of) model objects
    // ====================================

    //
    // General access. (temporary) Is this actually preferred to all those specific ones?
    //

    @Nonnull
    Stream<LayoutTrack> getLayoutTracksOfClass(Class<? extends LayoutTrack> layoutTrackClass);

    //
    // General access. (temporary) Is this actually preferred to all those specific ones?
    //
    
    @Nonnull
    Stream<LayoutTrackView> getLayoutTrackViewsOfClass(Class<? extends LayoutTrackView> layoutTrackViewClass);
    
    @Nonnull
    List<PositionablePointView> getPositionablePointViews();

    @Nonnull
    List<PositionablePoint> getPositionablePoints();

    @Nonnull
    List<LayoutSlip> getLayoutSlips();

    @Nonnull
    List<TrackSegmentView> getTrackSegmentViews();

    @Nonnull
    List<TrackSegment> getTrackSegments();

    @Nonnull
    List<LayoutTurnout> getLayoutTurnouts();

    @Nonnull
    List<LayoutTurntable> getLayoutTurntables();

    @Nonnull
    List<LevelXing> getLevelXings();

    @Nonnull
    List<LevelXingView> getLevelXingViews();

    /**
     * Read-only access to the list of LayoutTrack family objects.
     * The returned list will throw UnsupportedOperationException
     * if you attempt to modify it.
     * @return unmodifiable copy of layout track list.
     */
    @Nonnull
    List<LayoutTrack> getLayoutTracks();

    /**
     * Read-only access to the list of LayoutTrackView family objects.
     * The returned list will throw UnsupportedOperationException
     * if you attempt to modify it.
     * @return unmodifiable copy of track views.
     */
    @Nonnull
    List<LayoutTrackView> getLayoutTrackViews();

    // temporary
    LayoutTrackView getLayoutTrackView(LayoutTrack trk);
    
    // temporary
    LevelXingView getLevelXingView(LevelXing xing);
    
    // temporary
    LayoutTurnoutView getLayoutTurnoutView(LayoutTurnout to);
    
    // temporary
    LayoutTurntableView getLayoutTurntableView(LayoutTurntable to);
        
    // temporary
    TrackSegmentView getTrackSegmentView(TrackSegment to);

    // temporary
    PositionablePointView getPositionablePointView(PositionablePoint to);
        
    /**
     * Add a LayoutTrack and LayoutTrackView to the list of 
     * LayoutTrack family objects.
     * @param trk to be stored
     * @param v corresponding view
     */
    void addLayoutTrack(@Nonnull LayoutTrack trk, @Nonnull LayoutTrackView v);

    /**
     * If item present, delete from the list of LayoutTracks
     * and force a dirty redraw.
     * @param trk the layout track to remove.
     */
    void removeLayoutTrack(@Nonnull LayoutTrack trk);
    
    @Nonnull
    List<LayoutTurnout> getLayoutTurnoutsAndSlips();

    @Nonnull
    List<LayoutShape> getLayoutShapes();

    /**
     * Compute octagonal direction of vector from p1 to p2.
     * <p>
     * The octagonal (8) directions are: North, North-East, East,
     * South-East, South, South-West, West and North-West; see
     * {@link jmri.Path} for more on this.
     *
     * <p>
     * This method must eventually be in terms _other_ than
     * the screen geometry of the associated LayoutTrackView objects, 
     * as it's meant to be the track connectivity direction not the
     * on the screen implementation.
     *
     * @param trk1 track at "from" end
     * @param h1 the hit point for "from" end
     * @param trk2 the track at the "to" end
     * @param h2 the hit at the "to" end
     * @return the octagonal direction from p1 to p2
     */
    public int computeDirection(@Nonnull LayoutTrack trk1, @Nonnull HitPointType h1, 
                                @Nonnull LayoutTrack trk2, @Nonnull HitPointType h2);

    public int computeDirectionToCenter( @Nonnull LayoutTrack trk1, @Nonnull HitPointType h1, @Nonnull PositionablePoint p);
    
    public int computeDirectionFromCenter( @Nonnull PositionablePoint p, @Nonnull LayoutTrack trk1, @Nonnull HitPointType h1);

    default public int computeDirectionAB( @Nonnull LayoutTurnout track) {
        LayoutTurnoutView tv = getLayoutTurnoutView(track);
        return jmri.Path.computeDirection(tv.getCoordsA(), tv.getCoordsB());
    }

    default public int computeDirectionAC( @Nonnull LayoutTurnout track) {
        LayoutTurnoutView tv = getLayoutTurnoutView(track);
        return jmri.Path.computeDirection(tv.getCoordsA(), tv.getCoordsC());
    }

    default public int computeDirectionAD( @Nonnull LayoutTurnout track) {
        LayoutTurnoutView tv = getLayoutTurnoutView(track);
        return jmri.Path.computeDirection(tv.getCoordsA(), tv.getCoordsD());
    }

    default public int computeDirectionBC( @Nonnull LayoutTurnout track) {
        LayoutTurnoutView tv = getLayoutTurnoutView(track);
        return jmri.Path.computeDirection(tv.getCoordsB(), tv.getCoordsC());
    }

    default public int computeDirectionBD( @Nonnull LayoutTurnout track) {
        LayoutTurnoutView tv = getLayoutTurnoutView(track);
        return jmri.Path.computeDirection(tv.getCoordsB(), tv.getCoordsD());
    }

    default public int computeDirectionCD( @Nonnull LayoutTurnout track) {
        LayoutTurnoutView tv = getLayoutTurnoutView(track);
        return jmri.Path.computeDirection(tv.getCoordsC(), tv.getCoordsD());
    }

    /**
     * Invoked to display a warning about removal.
     * Lists the attached items that prevent removing the layout track item.
     * <p>
     * The default implementation refers this to a View object for displaying a Dialog.
     *
     * @param track The involved track
     * @param itemList A list of the attached heads, masts and/or sensors.
     * @param typeKey  The object type such as Turnout, Level Crossing, etc.
     */
    default public void displayRemoveWarning(LayoutTrack track, List<String> itemList, String typeKey) {
        getLayoutTrackView(track).displayRemoveWarningDialog(itemList, typeKey);
    }


}
