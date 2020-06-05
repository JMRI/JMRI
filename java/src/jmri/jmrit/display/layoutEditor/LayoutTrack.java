package jmri.jmrit.display.layoutEditor;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;

import javax.annotation.Nonnull;
import javax.swing.JPopupMenu;

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

     // final protected LayoutModels layoutEditor;  // preferred
    final protected LayoutEditor layoutEditor; // temporarily

    /**
     * Constructor method.
     * @param ident track ID.
     * @param layoutEditor main layout editor.
     */
    // public LayoutTrack(@Nonnull String ident, @Nonnull LayoutModels layoutEditor) { // preferred
    public LayoutTrack(@Nonnull String ident, @Nonnull LayoutEditor layoutEditor) { // temporary
        this.ident = ident;
        this.layoutEditor = layoutEditor;
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
    
    /**
     * Set center coordinates
     *
     * @return the center coordinates
     */
    final public Point2D getCoordsCenter() {
        log.debug("getCoordsCenter should have called in view instead of object (temporary)",
                jmri.util.Log4JUtil.shortenStacktrace(new Exception("temporary traceback")));
        return layoutEditor.getLayoutTrackView(this).getCoordsCenter();
    }

    /**
     * Set center coordinates.
     * <p>
     * Some subtypes may reimplement this is "center" is a more complicated
     * idea, i.e. for Bezier curves
     * @param p the coordinates to set
     */
    final public void setCoordsCenter(@Nonnull Point2D p) { // temporary until coords always in Views
        log.error("setCoordsCenter should have called in view instead of object (temporary)",
                jmri.util.Log4JUtil.shortenStacktrace(new Exception("temporary traceback")));
        layoutEditor.getLayoutTrackView(this).setCoordsCenter(p);
    }

    /**
     * @return true if this track segment has decorations
     */
    final public boolean hasDecorations() {
        log.error("LayoutTrack.hasDecorations() should have been called through View");
        return layoutEditor.getLayoutTrackView(this).hasDecorations();
    }

    protected static final int NUM_ARROW_TYPES = 6;  // temporary: why here? now view?

    abstract public boolean isMainline();

    /**
     * highlight unconnected connections
     *
     * @param g2           the graphics context
     * @param specificType the specific connection to draw (or NONE for all)
     */
    final protected void highlightUnconnected(Graphics2D g2, HitPointType specificType) {
        throw new IllegalArgumentException("should have called in Object instead of View (temporary)");
        
    }

    // optional parameter specificType = NONE
    final protected void highlightUnconnected(Graphics2D g2) {
        log.error("highlightUnconnected should have been called in view instead of object (temporary)");
        layoutEditor.getLayoutTrackView(this).highlightUnconnected(g2);
    }


    /**
     * Get the hidden state of the track element.
     *
     * @return true if hidden; false otherwise
     */
    final public boolean isHidden() {
        log.info("isHidden should have called in view instead of object (temporary)",
                jmri.util.Log4JUtil.shortenStacktrace(new Exception("temporary traceback")));
        return layoutEditor.getLayoutTrackView(this).isHidden();
    }
 
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
     * Display the attached items that prevent removing the layout track item.
     *
     * @param itemList A list of the attached heads, masts and/or sensors.
     * @param typeKey  The object type such as Turnout, Level Crossing, etc.
     */
    final public void displayRemoveWarningDialog(List<String> itemList, String typeKey) {
        log.error("displayRemoveWarningDialog should have been called in view instead of object (temporary)");
        layoutEditor.getLayoutTrackView(this).displayRemoveWarningDialog(itemList, typeKey);
    }

    /**
     * Initialization method for LayoutTrack sub-classes. The following method
     * is called for each instance after the entire LayoutEditor is loaded to
     * set the specific objects for that instance
     *
     * @param le the layout editor
     */
    abstract public void setObjects(@Nonnull LayoutEditor le);

    /**
     * scale this LayoutTrack's coordinates by the x and y factors
     *
     * @param xFactor the amount to scale X coordinates
     * @param yFactor the amount to scale Y coordinates
     */
    final public void scaleCoords(double xFactor, double yFactor) {
        log.error("scaleCoords should have been called in view instead of object (temporary)");
        layoutEditor.getLayoutTrackView(this).scaleCoords(xFactor, yFactor);
    }

    /**
     * rotate this LayoutTrack's coordinates by angleDEG's
     *
     * @param angleDEG the amount to rotate in degrees
     */
    final public void rotateCoords(double angleDEG){
        log.error("rotateCoords should have been called in view instead of object (temporary)");
        layoutEditor.getLayoutTrackView(this).rotateCoords(angleDEG);
    }

    /**
     * find the hit (location) type for a point
     *
     * @param hitPoint           the point
     * @param useRectangles      whether to use (larger) rectangles or (smaller)
     *                           circles for hit testing
     * @param requireUnconnected whether to only return hit types for free
     *                           connections
     * @return the location type for the point (or NONE)
     * @since 7.4.3
     */
    final protected HitPointType findHitPointType(@Nonnull Point2D hitPoint, boolean useRectangles, boolean requireUnconnected) {
        log.info("findHitPointType should have called View instead of temporary");
        return layoutEditor.getLayoutTrackView(this).findHitPointType(hitPoint, useRectangles, requireUnconnected);
    }


    // optional requireUnconnected parameter defaults to false
    final protected HitPointType findHitPointType(@Nonnull Point2D p, boolean useRectangles) {
        throw new IllegalArgumentException("should have called in Object instead of View (temporary)");
    }

    /**
     * @return the bounds of this track
     */
    final public Rectangle2D getBounds() {
        // final here to force pass over to View tree (method is temporary here)
        log.error("LayoutTrack.getBounds should have been called through View");
        return layoutEditor.getLayoutTrackView(this).getBounds();
    }

    /**
     * show the popup menu for this layout track
     *
     * @param mouseEvent the mouse down event that triggered this popup
     * @return the popup menu for this layout track
     */
    @Nonnull
    final protected JPopupMenu showPopup(@Nonnull MouseEvent mouseEvent) {
        log.error("LayoutTrack.showPopup(mE) should have been called through View");
        return layoutEditor.getLayoutTrackView(this).showPopup(mouseEvent);
    }

    /**
     * show the popup menu for this layout track
     *
     * @return the popup menu for this layout track
     */
    @Nonnull
    final protected JPopupMenu showPopup() {
        log.error("LayoutTrack.showPopup() should have been called through View");
        return layoutEditor.getLayoutTrackView(this).showPopup();
    }

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
