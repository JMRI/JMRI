package jmri.jmrit.display.layoutEditor;

import java.util.*;

import javax.annotation.Nonnull;

import jmri.JmriException;
import jmri.Turnout;

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

    private String ident = "";

    final protected void setIdent(@Nonnull String ident) {
        this.ident = ident;
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
