package jmri.jmrit.display.layoutEditor;

import java.util.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.*;

import javax.annotation.*;

/**
 * Scaffold implementing {@link LayoutModels} interface for testing use.
 *
 * @author Bob Jacobsen Copyright: (c) 2020
 */
public class LayoutModelsScaffold implements LayoutModels {


    public boolean isDirty() {
        return panelChanged;
    }
    public void setDirty() {
        setDirty(true);
    }
    public void setDirty(boolean b) {
        panelChanged = b;
    }
    boolean panelChanged;

    public void redrawPanel() {
    }
    
    // ====================================
    // Access to related navigation objects
    // ====================================

    private  LayoutEditorAuxTools auxTools = null;

    @Nonnull
    public LayoutEditorAuxTools getLEAuxTools() {
        if (auxTools == null) {
            auxTools = new LayoutEditorAuxTools(this);
        }
        return auxTools;
    }
    
    // ====================================
    // Access to (lists of) model objects
    // ====================================

    public @Nonnull
    Stream<LayoutTrack> getLayoutTracksOfClass(Class<? extends LayoutTrack> layoutTrackClass) {
        return getLayoutTracks().stream()
                .filter(layoutTrackClass::isInstance)
                .map(layoutTrackClass::cast);
    }

    public @Nonnull
    Stream<LayoutTrackView> getLayoutTrackViewsOfClass(Class<? extends LayoutTrackView> layoutTrackViewClass) {
        return getLayoutTrackViews().stream()
                .filter(layoutTrackViewClass::isInstance)
                .map(layoutTrackViewClass::cast);
    }

    public @Nonnull
    List<PositionablePointView> getPositionablePointViews() {
        return getLayoutTrackViewsOfClass(PositionablePointView.class)
                .map(PositionablePointView.class::cast)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public @Nonnull
    List<PositionablePoint> getPositionablePoints() {
        return getLayoutTracksOfClass(PositionablePoint.class)
                .map(PositionablePoint.class::cast)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public @Nonnull
    List<LayoutSlip> getLayoutSlips() {
        return getLayoutTracksOfClass(LayoutSlip.class)
                .map(LayoutSlip.class::cast)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public @Nonnull
    List<TrackSegmentView> getTrackSegmentViews() {
        return getLayoutTrackViewsOfClass(TrackSegmentView.class)
                .map(TrackSegmentView.class::cast)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public @Nonnull
    List<TrackSegment> getTrackSegments() {
        return getLayoutTracksOfClass(TrackSegment.class)
                .map(TrackSegment.class::cast)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public @Nonnull
    List<LayoutTurnout> getLayoutTurnouts() {
        return getLayoutTracks().stream() // next line excludes LayoutSlips
                .filter((o) -> (!(o instanceof LayoutSlip) && (o instanceof LayoutTurnout)))
                .map(LayoutTurnout.class::cast)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public @Nonnull
    List<LayoutTurntable> getLayoutTurntables() {
        return getLayoutTracksOfClass(LayoutTurntable.class)
                .map(LayoutTurntable.class::cast)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public @Nonnull
    List<LevelXing> getLevelXings() {
        return getLayoutTracksOfClass(LevelXing.class)
                .map(LevelXing.class::cast)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public @Nonnull
    List<LevelXingView> getLevelXingViews() {
        return getLayoutTrackViewsOfClass(LevelXingView.class)
                .map(LevelXingView.class::cast)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Read-only access to the list of LayoutTrack family objects.
     * The returned list will throw UnsupportedOperationException
     * if you attempt to modify it.
     * @return unmodifiable copy of layout track list.
     */
    @Nonnull
    public List<LayoutTrack> getLayoutTracks() {
        return Collections.unmodifiableList(layoutTrackList);
    }

    /**
     * Read-only access to the list of LayoutTrackView family objects.
     * The returned list will throw UnsupportedOperationException
     * if you attempt to modify it.
     * @return unmodifiable copy of track views.
     */
    @Nonnull
    public List<LayoutTrackView> getLayoutTrackViews() {
        return Collections.unmodifiableList(layoutTrackViewList);
    }

    private final List<LayoutShape> layoutShapes = new ArrayList<>(); 

    private final List<LayoutTrack> layoutTrackList = new ArrayList<>();
    private final List<LayoutTrackView> layoutTrackViewList = new ArrayList<>();
    private final Map<LayoutTrack, LayoutTrackView> trkToView = new HashMap<>();
    private final Map<LayoutTrackView, LayoutTrack> viewToTrk = new HashMap<>();

    // temporary
    public LayoutTrackView getLayoutTrackView(LayoutTrack trk) {
        LayoutTrackView lv = trkToView.get(trk);
        if (lv == null) {
            log.warn("No View found for {} class {}", trk, trk.getClass());
            throw new IllegalArgumentException("No View found: "+trk.getClass());
        }
        return lv;
    }
    // temporary
    public LevelXingView getLevelXingView(LevelXing xing) {
        LayoutTrackView lv = trkToView.get(xing);
        if (lv == null) {
            log.warn("No View found for {} class {}", xing, xing.getClass());
            throw new IllegalArgumentException("No View found: "+xing.getClass());
        }
        if (lv instanceof LevelXingView) return (LevelXingView) lv;
        else log.error("wrong type {} {} found {}", xing, xing.getClass(), lv);
        throw new IllegalArgumentException("Wrong type: "+xing.getClass());
    }
    // temporary
    public LayoutTurnoutView getLayoutTurnoutView(LayoutTurnout to) {
        LayoutTrackView lv = trkToView.get(to);
        if (lv == null) {
            log.warn("No View found for {} class {}", to, to.getClass());
            throw new IllegalArgumentException("No View found: "+to);
        }
        if (lv instanceof LayoutTurnoutView) return (LayoutTurnoutView) lv;
        else log.error("wrong type {} {} found {}", to, to.getClass(), lv);
        throw new IllegalArgumentException("Wrong type: "+to.getClass());
    }
    
    // temporary
    public LayoutTurntableView getLayoutTurntableView(LayoutTurntable to) {
        LayoutTrackView lv = trkToView.get(to);
        if (lv == null) {
            log.warn("No View found for {} class {}", to, to.getClass());
            throw new IllegalArgumentException("No matching View found: "+to);
        }
        if (lv instanceof LayoutTurntableView) return (LayoutTurntableView) lv;
        else log.error("wrong type {} {} found {}", to, to.getClass(), lv);
        throw new IllegalArgumentException("Wrong type: "+to.getClass());
    }
        
    // temporary
    public TrackSegmentView getTrackSegmentView(TrackSegment to) {
        LayoutTrackView lv = trkToView.get(to);
        if (lv == null) {
            log.warn("No View found for {} class {}", to, to.getClass());
            throw new IllegalArgumentException("No matching View found: "+to);
        }
        if (lv instanceof TrackSegmentView) return (TrackSegmentView) lv;
        else log.error("wrong type {} {} found {}", to, to.getClass(), lv);
        throw new IllegalArgumentException("Wrong type: "+to.getClass());
    }
        
    // temporary
    public PositionablePointView getPositionablePointView(PositionablePoint to) {
        LayoutTrackView lv = trkToView.get(to);
        if (lv == null) {
            log.warn("No View found for {} class {}", to, to.getClass());
            throw new IllegalArgumentException("No matching View found: "+to);
        }
        if (lv instanceof PositionablePointView) return (PositionablePointView) lv;
        else log.error("wrong type {} {} found {}", to, to.getClass(), lv);
        throw new IllegalArgumentException("Wrong type: "+to.getClass());
    }
        
    /**
     * Add a LayoutTrack and LayoutTrackView to the list of 
     * LayoutTrack family objects.
     */
    public void addLayoutTrack(@Nonnull LayoutTrack trk, @Nonnull LayoutTrackView v) {
        log.trace("addLayoutTrack {}", trk);
        if (layoutTrackList.contains(trk)) log.warn("LayoutTrack {} already being maintained", trk.getName());
        layoutTrackList.add(trk);
        
        layoutTrackViewList.add(v);
        trkToView.put(trk, v);
        viewToTrk.put(v, trk);
    }

    /**
     * If item present, delete from the list of LayoutTracks
     * and force a dirty redraw.
     * @param trk the layout track to remove.
     */
    public void removeLayoutTrack(@Nonnull LayoutTrack trk) {
        log.trace("removeLayoutTrack {}", trk);
        layoutTrackList.remove(trk);
        LayoutTrackView v = trkToView.get(trk);
        layoutTrackViewList.remove(v);
        trkToView.remove(trk);
        viewToTrk.remove(v);
    }
    
    @Nonnull
    public List<LayoutTurnout> getLayoutTurnoutsAndSlips() {
        return getLayoutTracksOfClass(LayoutTurnout.class
        )
                .map(LayoutTurnout.class::cast)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Nonnull
    public List<LayoutShape> getLayoutShapes() {
        return layoutShapes;
    }

    public int computeDirection(@Nonnull LayoutTrack trk1, @Nonnull HitPointType h1, 
                                @Nonnull LayoutTrack trk2, @Nonnull HitPointType h2) {
        return jmri.Path.EAST;  // fixed result for testing
    }

    @Override
    public int computeDirectionToCenter( @Nonnull LayoutTrack trk1, @Nonnull HitPointType h1, @Nonnull PositionablePoint p) {
        return jmri.Path.EAST;  // fixed result for testing
    }
    
    @Override
    public int computeDirectionFromCenter( @Nonnull PositionablePoint p, @Nonnull LayoutTrack trk1, @Nonnull HitPointType h1) {
        return jmri.Path.EAST; // fixed result for testing    
    }

    // initialize logging
    private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutEditor.class);

}
