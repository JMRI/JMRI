package jmri.jmrit.display.layoutEditor;

import java.awt.geom.Point2D;
import java.util.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.JmriException;
import jmri.Turnout;
import jmri.tracktiles.NotATile;
import jmri.tracktiles.TrackTile;

/**
 * Abstract base class for all layout track objects (PositionablePoint,
 * TrackSegment, LayoutTurnout, LayoutSlip, LevelXing and LayoutTurntable)
 * <p>
 * This is the connectivity/topology information for the layout; the
 * display information, including screen geometry, is held in {@link LayoutTrackView} subclasses.
 * <ul>
 *   <li>One or more connections, consisting of a LayoutTrack name and {@link HitPointType}
 *   <li>Mainline status
 *   <li>Associated
 *      <ul>
 *          <li>Blocks
 *          <li>Signal heads and masts
 *          <li>Sensors
 *          <li>Turnout controls
 *      </ul>
 * </ul>
 *
 * @author Dave Duchamp Copyright (C) 2009
 * @author George Warner Copyright (c) 2017-2020
 * @author Bob Jacobsen Copyright (c)  2020
 */
abstract public class LayoutTrack {

    // final protected LayoutModels models;  // preferred
    final protected LayoutEditor models; // temporary type

    /**
     * Constructor method.
     * @param ident track ID.
     * @param models main layout editor.
     */
    // public LayoutTrack(@Nonnull String ident, @Nonnull LayoutModels models) { // preferred
    public LayoutTrack(@Nonnull String ident, @Nonnull LayoutEditor models) { // temporary
        this.ident = ident;
        this.models = models;
    }

    /**
     * Get the track ID.
     * @return track ident.
     */
    @Nonnull
    final public String getId() {
        return ident;
    }

    @Nonnull
    final public String getName() {
        return ident;
    }

    /**
     * Get the type of this item.
     * @return the type
     */
    public abstract String getTypeName();

    private String ident = "";
    private TrackTile trackTile = NotATile.getInstance();

    final protected void setIdent(@Nonnull String ident) {
        this.ident = ident;
    }

    /**
     * Get the associated TrackTile for this layout track.
     * 
     * @return the TrackTile, or NotATile if none is associated
     */
    @Nonnull
    public TrackTile getTrackTile() {
        return trackTile;
    }

    /**
     * Set the associated TrackTile for this layout track.
     * 
     * @param trackTile the TrackTile to associate, or null to set NotATile
     */
    public void setTrackTile(TrackTile trackTile) {
        this.trackTile = (trackTile != null) ? trackTile : NotATile.getInstance();
    }

    /**
     * Check if this track element has an associated tile with geometry data.
     * 
     * @return true if this track has tile geometry information
     */
    public boolean isTiled() {
        return !(trackTile instanceof jmri.tracktiles.NotATile || trackTile instanceof jmri.tracktiles.UnknownTile);
    }

    /**
     * Get the list of anchor point identifiers for this track element.
     * 
     * @return list of anchor point names (e.g., ["A", "B"] for track segments)
     */
    @Nonnull
    public abstract List<String> getAnchorPoints();

    /**
     * Get the list of path identifiers for this track element.
     * 
     * @return list of path names (e.g., ["AB"] for track segments)
     */
    @Nonnull
    public abstract List<String> getPathIdentifiers();

    /**
     * Calculate the orientation angle at the specified anchor point.
     * This is the angle of track continuation from this anchor point.
     * 
     * @param anchor the anchor point identifier
     * @param layoutEditor the layout editor for coordinate access
     * @return the orientation angle in degrees (0-360), or -1 if calculation fails
     */
    public double getOrientationAtAnchor(@Nonnull String anchor, @Nonnull LayoutEditor layoutEditor) {
        if (!isTiled()) {
            return -1.0;
        }

        List<String> anchors = getAnchorPoints();
        if (anchors.size() != 2) {
            return -1.0; // Only handle two-anchor tracks for now
        }

        Point2D pointA = getAnchorCoordinates(anchors.get(0), layoutEditor);
        Point2D pointB = getAnchorCoordinates(anchors.get(1), layoutEditor);
        
        if (pointA == null || pointB == null) {
            return -1.0;
        }

        jmri.tracktiles.TrackTile tile = getTrackTile();
        String jmriType = tile.getJmriType();
        
        if ("curved".equals(jmriType)) {
            double arcDegrees = tile.getArc();
            
            if (arcDegrees <= 0) {
                // Invalid curve data - fall back to straight
                return calculateStraightTrackOrientation(pointA, pointB);
            }
            
            return calculateCurvedTrackOrientation(pointA, pointB, arcDegrees, anchor, anchors);
        }

        // Straight path - return the same orientation for both anchors
        return calculateStraightTrackOrientation(pointA, pointB);
    }

    /**
     * Calculate orientation for a straight track. Both ends have the same orientation.
     * Returns the direction of the track line itself (A→B direction).
     */
    protected double calculateStraightTrackOrientation(Point2D pointA, Point2D pointB) {
        double trackDirection = Math.toDegrees(Math.atan2(
            pointB.getY() - pointA.getY(),
            pointB.getX() - pointA.getX()));
        return (trackDirection % 360 + 360) % 360;
    }

    /**
     * Calculate orientation for a curved track.
     * For curved tracks:
     * - First connector has the same orientation as the straight track direction
     * - Second connector deviates by the arc amount
     */
    protected double calculateCurvedTrackOrientation(Point2D pointA, Point2D pointB, double arcDegrees, String anchor, List<String> anchors) {
        double baseOrientation = calculateStraightTrackOrientation(pointA, pointB);
        
        if (anchors.get(0).equals(anchor)) {
            // First connector maintains the base orientation
            return baseOrientation;
        } else {
            // Second connector deviates by the arc amount
            // TODO: Determine curve direction from tile data to know if it's +arc or -arc
            double deviation = arcDegrees;
            double orientation = baseOrientation + deviation;
            return (orientation % 360 + 360) % 360;
        }
    }

    /**
     * Calculate the length of the specified path.
     * For straight paths, returns the tile length.
     * For curved paths, returns the calculated arc length.
     * 
     * @param pathId the path identifier
     * @return the path length in millimeters, or 0.0 if not available
     */
    public double getPathLength(@Nonnull String pathId) {
        if (!isTiled()) {
            return 0.0;
        }
        
        // Validate that the pathId is valid for this track type
        if (!getPathIdentifiers().contains(pathId)) {
            return 0.0;
        }
        
        // Default implementation uses tile geometry calculation
        // TODO: For multi-path tiles (turnouts), this may need path-specific calculations
        return LayoutTileGeometry.calculatePathLength(getTrackTile());
    }

    /**
     * Find the path identifier that connects to the specified anchor point.
     * 
     * @param anchor the anchor point identifier
     * @return the path identifier, or null if not found
     */
    @CheckForNull
    protected abstract String findPathForAnchor(@Nonnull String anchor);

    /**
     * Get the coordinates of the specified anchor point.
     * 
     * @param anchor the anchor point identifier
     * @param layoutEditor the layout editor for coordinate access
     * @return the anchor point coordinates, or null if not found
     */
    @CheckForNull
    protected abstract java.awt.geom.Point2D getAnchorCoordinates(@Nonnull String anchor, @Nonnull LayoutEditor layoutEditor);

    /**
     * Get the coordinates of the other endpoint of the specified path.
     * 
     * @param pathId the path identifier
     * @param anchor the anchor point we're measuring from
     * @param layoutEditor the layout editor for coordinate access
     * @return the other endpoint coordinates, or null if not found
     */
    @CheckForNull
    protected abstract java.awt.geom.Point2D getOtherEndpoint(@Nonnull String pathId, @Nonnull String anchor, @Nonnull LayoutEditor layoutEditor);

    /**
     * Get the tile path object for the specified path identifier.
     * 
     * @param pathId the path identifier
     * @return the TrackTilePath object, or null if not found
     */
    @CheckForNull
    protected jmri.tracktiles.TrackTilePath getPathFromTile(@Nonnull String pathId) {
        if (!isTiled()) {
            return null;
        }
        
        for (jmri.tracktiles.TrackTilePath path : trackTile.getPaths()) {
            // Match by route name or state
            if (pathId.equals(path.getRoute()) || 
                (pathId.length() == 2 && pathId.equals(path.getDirection()))) {
                return path;
            }
        }
        return null;
    }

    abstract public boolean isMainline();

    /*
    * non-accessor methods
     */
    /**
     * get turnout state string
     *
     * @param turnoutState of the turnout
     * @return the turnout state string
     */
    final public String getTurnoutStateString(int turnoutState) {
        String result = "";
        if (turnoutState == Turnout.CLOSED) {
            result = Bundle.getMessage("TurnoutStateClosed");
        } else if (turnoutState == Turnout.THROWN) {
            result = Bundle.getMessage("TurnoutStateThrown");
        } else {
            result = Bundle.getMessage("BeanStateUnknown");
        }
        return result;
    }

    /**
     * Check for active block boundaries.
     * <p>
     * If any connection point of a layout track object has attached objects,
     * such as signal masts, signal heads or NX sensors, the layout track object
     * cannot be deleted.
     *
     * @return true if the layout track object can be deleted.
     */
    abstract public boolean canRemove();

    /**
     * Initialization method for LayoutTrack sub-classes. The following method
     * is called for each instance after the entire LayoutEditor is loaded to
     * set the specific objects for that instance
     *
     * @param le the layout editor
     */
    abstract public void setObjects(@Nonnull LayoutEditor le);

    /**
     * get the LayoutTrack connected at the specified connection type
     *
     * @param connectionType where on us to get the connection
     * @return the LayoutTrack connected at the specified connection type
     * @throws JmriException - if the connectionType is invalid
     */
    abstract public LayoutTrack getConnection(HitPointType connectionType) throws JmriException;

    /**
     * set the LayoutTrack connected at the specified connection type
     *
     * @param connectionType where on us to set the connection
     * @param o              the LayoutTrack that is to be connected
     * @param type           where on the LayoutTrack we are connected
     * @throws JmriException - if connectionType or type are invalid
     */
    abstract public void setConnection(HitPointType connectionType, LayoutTrack o, HitPointType type) throws JmriException;

    /**
     * abstract method... subclasses should implement _IF_ they need to recheck
     * their block boundaries
     */
    abstract protected void reCheckBlockBoundary();

    /**
     * get the layout connectivity for this track
     *
     * @return the list of Layout Connectivity objects
     */
    abstract protected List<LayoutConnectivity> getLayoutConnectivity();

    /**
     * return true if this connection type is disconnected
     *
     * @param connectionType the connection type to test
     * @return true if the connection for this connection type is free
     */
    public boolean isDisconnected(HitPointType connectionType) {
        boolean result = false;
        if (HitPointType.isConnectionHitType(connectionType)) {
            try {
                result = (null == getConnection(connectionType));
            } catch (JmriException e) {
                // this should never happen because isConnectionType() above would have caught an invalid connectionType.
                log.error("Unexpected exception", e);
            }
        }
        return result;
    }

    /**
     * return a list of the available connections for this layout track
     *
     * @return the list of available connections
     */
     // note: used by LayoutEditorChecks.setupCheckUnConnectedTracksMenu()
     //
     // This could have just returned a boolean but I thought a list might be
     // more useful (eventually... not currently being used; we just check to see
     // if it's not empty.)
    @Nonnull
    abstract public List<HitPointType> checkForFreeConnections();

    /**
     * determine if all the appropriate blocks have been assigned to this track
     *
     * @return true if all appropriate blocks have been assigned
     */
     // note: used by LayoutEditorChecks.setupCheckUnBlockedTracksMenu()
     //
    abstract public boolean checkForUnAssignedBlocks();

    /**
     * check this track and its neighbors for non-contiguous blocks
     * <p>
     * For each (non-null) blocks of this track do: #1) If it's got an entry in
     * the blockNamesToTrackNameSetMap then #2) If this track is not in one of
     * the TrackNameSets for this block #3) add a new set (with this
     * block/track) to blockNamesToTrackNameSetMap and #4) check all the
     * connections in this block (by calling the 2nd method below)
     * <p>
     * Basically, we're maintaining contiguous track sets for each block found
     * (in blockNamesToTrackNameSetMap)
     *
     * @param blockNamesToTrackNameSetMaps hashmap of key:block names to lists
     *                                     of track name sets for those blocks
     */
     // note: used by LayoutEditorChecks.setupCheckNonContiguousBlocksMenu()
     //
    abstract public void checkForNonContiguousBlocks(
            @Nonnull HashMap<String, List<Set<String>>> blockNamesToTrackNameSetMaps);

    /**
     * recursive routine to check for all contiguous tracks in this blockName
     *
     * @param blockName    the block that we're checking for
     * @param TrackNameSet the set of track names in this block
     */
    abstract public void collectContiguousTracksNamesInBlockNamed(
            @Nonnull String blockName,
            @Nonnull Set<String> TrackNameSet);

    /**
     * Assign all the layout blocks in this track
     *
     * @param layoutBlock to this layout block (used by the Tools menu's "Assign
     *                    block to selection" item)
     */
    abstract public void setAllLayoutBlocks(LayoutBlock layoutBlock);

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutTrack.class);
}
