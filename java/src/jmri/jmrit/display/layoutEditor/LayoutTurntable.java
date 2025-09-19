package jmri.jmrit.display.layoutEditor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.*;
import jmri.util.MathUtil;

/**
 * A LayoutTurntable is a representation used by LayoutEditor to display a
 * turntable.
 * <p>
 * A LayoutTurntable has a variable number of connection points, called
 * RayTracks, each radiating from the center of the turntable. Each of these
 * points should be connected to a TrackSegment.
 * <p>
 * Each radiating segment (RayTrack) gets its Block information from its
 * connected track segment.
 * <p>
 * Each radiating segment (RayTrack) has a unique connection index. The
 * connection index is set when the RayTrack is created, and cannot be changed.
 * This connection index is used to maintain the identity of the radiating
 * segment to its connected Track Segment as ray tracks are added and deleted by
 * the user.
 * <p>
 * The radius of the turntable circle is variable by the user.
 * <p>
 * Each radiating segment (RayTrack) connecting point is a fixed distance from
 * the center of the turntable. The user may vary the angle of the radiating
 * segment. Angles are measured from the vertical (12 o'clock) position in a
 * clockwise manner. For example, 30 degrees is 1 o'clock, 60 degrees is 2
 * o'clock, 90 degrees is 3 o'clock, etc.
 * <p>
 * Each radiating segment is drawn from its connection point to the turntable
 * circle in the direction of the turntable center.
 *
 * @author Dave Duchamp Copyright (c) 2007
 * @author George Warner Copyright (c) 2017-2018
 */
public class LayoutTurntable extends LayoutTrack {

     /**
     * Constructor method
     *
     * @param id           the name for the turntable
     * @param models what layout editor panel to put it in
     */
     public LayoutTurntable(@Nonnull String id, @Nonnull LayoutEditor models) {
         super(id, models);

        // Check Dispatcher options to see if we should create a virtual signal mast.
        // A mast is only needed if the user has configured Dispatcher to use Signal Masts.
        jmri.jmrit.dispatcher.OptionsFile optionsFile = jmri.InstanceManager.getDefault(jmri.jmrit.dispatcher.OptionsFile.class);
        log.info("signaltype {}", String.valueOf(optionsFile.getSignalType()));
        // The integer '1' corresponds to the "Signal Masts" selection in the Dispatcher options.
        if (optionsFile == null || optionsFile.getSignalType() != 1) {
            log.debug("Skipping virtual signal mast creation for turntable '{}' as Dispatcher is not configured for Signal Masts.", getName());
            return; // Do not create a mast
        }

         radius = 25.0; // initial default, change asap.

         SignalMastManager smm = jmri.InstanceManager.getDefault(jmri.SignalMastManager.class);
         String mastUserName = "Turntable Mast " + id;

         // First, check if a mast for this turntable already exists.
         // This handles the case where the panel is being loaded from a file.
         this.virtualSignalMast = smm.getByUserName(mastUserName);

         // If no mast was found, create a new one.
         // This handles the case where a new turntable is being created in the editor.
         if (this.virtualSignalMast == null) {
             log.debug("No existing mast found for turntable '{}', creating a new one.", id);
             // Define the signal system to use, arbitrary use BR-2003
             String signalSystem = "BR-2003:3";

             // Get the next available unique number for a VirtualSignalMast
             java.text.DecimalFormat paddedNumber = new java.text.DecimalFormat("0000");
             int nextMastNum = jmri.implementation.VirtualSignalMast.getLastRef() + 1;

             // Construct the valid, unique system name in the required format.
             String mastSystemName = "IF$vsm:" + signalSystem + "($" + paddedNumber.format(nextMastNum) + ")";

             this.virtualSignalMast = smm.provideSignalMast(mastSystemName);

             // Check for successful creation
             if (this.virtualSignalMast == null) {
                 log.error("Failed to create virtual signal mast for turntable {}", id);
                 return; // Stop here if creation failed.
             }
             this.virtualSignalMast.setUserName(mastUserName);
         } else {
             log.debug("Found existing mast '{}' for turntable '{}'.", this.virtualSignalMast.getSystemName(), id);
         }

        // Store a direct reference to this turntable object on the mast itself.
        // This allows other parts of the system (like SignalMastLogic) to easily find the owner.
        if (this.virtualSignalMast != null) {
            this.virtualSignalMast.setProperty("ownerTurntable", this);
        }
     }

    /**
     * Static method to identify a SignalMast that is a turntable mast
     * based on its user name.
     * @param mast The signal mast to check.
     * @return true if the mast's user name indicates it belongs to a turntable.
     */
    public static boolean isTurntableMast(jmri.SignalMast mast) {
        if (mast == null) {
            return false;
        }
        // Identify a turntable mast by its user name, which is set reliably
        // by the LayoutTurntable constructor.
        return mast.getUserName() != null && mast.getUserName().contains("Turntable Mast");
    }

    // defined constants
    // operational instance variables (not saved between sessions)
    private NamedBeanHandle<LayoutBlock> namedLayoutBlock = null;

    private boolean turnoutControlled = false;
    private double radius = 25.0;
    private int lastKnownIndex = -1;
	private SignalMast virtualSignalMast = null;

    // persistent instance variables (saved between sessions)

    // temporary: this is referenced directly from LayoutTurntable, which
    // should be using _functional_ accessors here.
    public final List<RayTrack> rayTrackList = new ArrayList<>(); // list of Ray Track objects

    /**
     * Get a string that represents this object. This should only be used for
     * debugging.
     *
     * @return the string
     */
    @Override
    @Nonnull
    public String toString() {
        return "LayoutTurntable " + getName();
    }

    //
    // Accessor methods
    //
    /**
     * Get the radius for this turntable.
     *
     * @return the radius for this turntable
     */
    public double getRadius() {
        return radius;
    }

    /**
     * Set the radius for this turntable.
     *
     * @param r the radius for this turntable
     */
    public void setRadius(double r) {
        radius = r;
    }

    /**
     * @return the layout block name
     */
    @Nonnull
    public String getBlockName() {
        String result = null;
        if (namedLayoutBlock != null) {
            result = namedLayoutBlock.getName();
        }
        return ((result == null) ? "" : result);
    }

    /**
     * @return the layout block
     */
    @CheckForNull
    public LayoutBlock getLayoutBlock() {
        return (namedLayoutBlock != null) ? namedLayoutBlock.getBean() : null;
    }

    /**
     * Set up a LayoutBlock for this LayoutTurntable.
     *
     * @param newLayoutBlock the LayoutBlock to set
     */
    public void setLayoutBlock(@CheckForNull LayoutBlock newLayoutBlock) {
        LayoutBlock layoutBlock = getLayoutBlock();
        if (layoutBlock != newLayoutBlock) {
            /// block has changed, if old block exists, decrement use
            if (layoutBlock != null) {
                layoutBlock.decrementUse();
            }
            if (newLayoutBlock != null) {
                String newName = newLayoutBlock.getUserName();
                if ((newName != null) && !newName.isEmpty()) {
                    namedLayoutBlock = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(newName, newLayoutBlock);
					newLayoutBlock.incrementUse();
                } else {
                    namedLayoutBlock = null;
                }
            } else {
                namedLayoutBlock = null;
            }
			if (layoutBlock != null) { layoutBlock.updatePaths(); }
            if (newLayoutBlock != null) { newLayoutBlock.updatePaths(); }
            for (RayTrack ray : rayTrackList) {
                TrackSegment segment = ray.getConnect();
                if (segment != null) {
                    LayoutBlock rayBlock = segment.getLayoutBlock();
                    if (rayBlock != null && rayBlock != layoutBlock && rayBlock != newLayoutBlock) {
                        rayBlock.updatePaths();
                    }
                }
            }												 
        }
    }

    /**
     * Set up a LayoutBlock for this LayoutTurntable.
     *
     * @param name the name of the new LayoutBlock
     */
    public void setLayoutBlockByName(@CheckForNull String name) {
        if ((name != null) && !name.isEmpty()) {
            setLayoutBlock(models.provideLayoutBlock(name));
        }
    }

    /**
     * Add a ray at the specified angle.
     *
     * @param angle the angle
     * @return the RayTrack
     */
    public RayTrack addRay(double angle) {
        RayTrack rt = new RayTrack(angle, getNewIndex());
        rayTrackList.add(rt);
        return rt;
    }

    private int getNewIndex() {
        int index = -1;
        if (rayTrackList.isEmpty()) {
            return 0;
        }

        boolean found = true;
        while (found) {
            index++;
            found = false; // assume failure (pessimist!)
            for (RayTrack rt : rayTrackList) {
                if (index == rt.getConnectionIndex()) {
                    found = true;
                }
            }
        }
        return index;
    }

    // the following method is only for use in loading layout turntables
    public void addRayTrack(double angle, int index, String name) {
        RayTrack rt = new RayTrack(angle, index);
        /// if (ray!=null) {
        rayTrackList.add(rt);
        rt.connectName = name;
        //}
    }

    /**
     * Get the connection for the ray with this index.
     *
     * @param index the index
     * @return the connection for the ray with this value of getConnectionIndex
     */
    @CheckForNull
    public TrackSegment getRayConnectIndexed(int index) {
        TrackSegment result = null;
        for (RayTrack rt : rayTrackList) {
            if (rt.getConnectionIndex() == index) {
                result = rt.getConnect();
                break;
            }
        }
        return result;
    }

    /**
     * Get the connection for the ray at the index in the rayTrackList.
     *
     * @param i the index in the rayTrackList
     * @return the connection for the ray at that index in the rayTrackList or null
     */
    @CheckForNull
    public TrackSegment getRayConnectOrdered(int i) {
        TrackSegment result = null;

        if (i < rayTrackList.size()) {
            RayTrack rt = rayTrackList.get(i);
            if (rt != null) {
                result = rt.getConnect();
            }
        }
        return result;
    }

    /**
     * Set the connection for the ray at the index in the rayTrackList.
     *
     * @param ts    the connection
     * @param index the index in the rayTrackList
     */
    public void setRayConnect(@CheckForNull TrackSegment ts, int index) {
        for (RayTrack rt : rayTrackList) {
            if (rt.getConnectionIndex() == index) {
                rt.setConnect(ts);
                break;
            }
        }
    }

    // should only be used by xml save code
    @Nonnull
    public List<RayTrack> getRayTrackList() {
        return rayTrackList;
    }

    /**
     * Get the number of rays on turntable.
     *
     * @return the number of rays
     */
    public int getNumberRays() {
        return rayTrackList.size();
    }

    /**
     * Get the index for the ray at this position in the rayTrackList.
     *
     * @param i the position in the rayTrackList
     * @return the index
     */
    public int getRayIndex(int i) {
        int result = 0;
        if (i < rayTrackList.size()) {
            RayTrack rt = rayTrackList.get(i);
            result = rt.getConnectionIndex();
        }
        return result;
    }

    /**
     * Get the angle for the ray at this position in the rayTrackList.
     *
     * @param i the position in the rayTrackList
     * @return the angle
     */
    public double getRayAngle(int i) {
        double result = 0.0;
        if (i < rayTrackList.size()) {
            RayTrack rt = rayTrackList.get(i);
            result = rt.getAngle();
        }
        return result;
    }

    /**
     * Check if a given LayoutBlock is connected to one of the turntable rays.
     * @param block The LayoutBlock to check.
     * @return true if the block is a ray block, false otherwise.
     */
    public boolean isRayBlock(LayoutBlock block) {
        if (block == null) {
            return false;
        }
        for (int i = 0; i < getNumberRays(); i++) {
            TrackSegment rayConnect = getRayConnectOrdered(i);
            if (rayConnect != null && rayConnect.getLayoutBlock() == block) {
                return true;
            }
        }
        return false;
    }
    /**
     * Get the connection index for a given LayoutBlock that is a ray track.
     * @param block The LayoutBlock to check.
     * @return the connection index, or -1 if not found.
     */
    public int getRayIndexForBlock(LayoutBlock block) {
        if (block == null) {
            return -1;
        }
        for (RayTrack rt : rayTrackList) {
            TrackSegment rayConnect = rt.getConnect();
            if (rayConnect != null && rayConnect.getLayoutBlock() == block) {
                return rt.getConnectionIndex();
            }
        }
        return -1;
    }
    /**
     * Get the position control Turnout for a specific ray index.
     * @param index The connection index of the ray.
     * @return The associated Turnout, or null if none exists.
     */
    public Turnout getTurnoutForRay(int index) {
        for (RayTrack rt : rayTrackList) {
            if (rt.getConnectionIndex() == index) {
                return rt.getTurnout();
            }
        }
        return null;
    }

    /**
     * Set the turnout and state for the ray with this index.
     *
     * @param index       the index
     * @param turnoutName the turnout name
     * @param state       the state
     */
    public void setRayTurnout(int index, @CheckForNull String turnoutName, int state) {
        boolean found = false; // assume failure (pessimist!)
        for (RayTrack rt : rayTrackList) {
            if (rt.getConnectionIndex() == index) {
                rt.setTurnout(turnoutName, state);
                found = true;
                break;
            }
        }
        if (!found) {
            log.error("{}.setRayTurnout({}, {}, {}); Attempt to add Turnout control to a non-existant ray track",
                    getName(), index, turnoutName, state);
        }
    }

    /**
     * Get the name of the turnout for the ray at this index.
     *
     * @param i the index
     * @return name of the turnout for the ray at this index
     */
    @CheckForNull
    public String getRayTurnoutName(int i) {
        String result = null;
        if (i < rayTrackList.size()) {
            RayTrack rt = rayTrackList.get(i);
            result = rt.getTurnoutName();
        }
        return result;
    }

    /**
     * Get the turnout for the ray at this index.
     *
     * @param i the index
     * @return the turnout for the ray at this index
     */
    @CheckForNull
    public Turnout getRayTurnout(int i) {
        Turnout result = null;
        if (i < rayTrackList.size()) {
            RayTrack rt = rayTrackList.get(i);
            result = rt.getTurnout();
        }
        return result;
    }

    /**
     * Get the state of the turnout for the ray at this index.
     *
     * @param i the index
     * @return state of the turnout for the ray at this index
     */
    public int getRayTurnoutState(int i) {
        int result = 0;
        if (i < rayTrackList.size()) {
            RayTrack rt = rayTrackList.get(i);
            result = rt.getTurnoutState();
        }
        return result;
    }

    /**
     * Get if the ray at this index is disabled.
     *
     * @param i the index
     * @return true if disabled
     */
    public boolean isRayDisabled(int i) {
        boolean result = false;    // assume not disabled
        if (i < rayTrackList.size()) {
            RayTrack rt = rayTrackList.get(i);
            result = rt.isDisabled();
        }
        return result;
    }

    /**
     * Set the disabled state of the ray at this index.
     *
     * @param i   the index
     * @param boo the state
     */
    public void setRayDisabled(int i, boolean boo) {
        if (i < rayTrackList.size()) {
            RayTrack rt = rayTrackList.get(i);
            rt.setDisabled(boo);
        }
    }

    /**
     * Get the disabled when occupied state of the ray at this index.
     *
     * @param i the index
     * @return the state
     */
    public boolean isRayDisabledWhenOccupied(int i) {
        boolean result = false;    // assume not disabled when occupied
        if (i < rayTrackList.size()) {
            RayTrack rt = rayTrackList.get(i);
            result = rt.isDisabledWhenOccupied();
        }
        return result;
    }

    /**
     * Set the disabled when occupied state of the ray at this index.
     *
     * @param i   the index
     * @param boo the state
     */
    public void setRayDisabledWhenOccupied(int i, boolean boo) {
        if (i < rayTrackList.size()) {
            RayTrack rt = rayTrackList.get(i);
            rt.setDisabledWhenOccupied(boo);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LayoutTrack getConnection(HitPointType connectionType) throws jmri.JmriException {
        LayoutTrack result = null;
        if (HitPointType.isTurntableRayHitType(connectionType)) {
            result = getRayConnectIndexed(connectionType.turntableTrackIndex());
        } else {
            String errString = MessageFormat.format("{0}.getCoordsForConnectionType({1}); Invalid connection type",
                    getName(), connectionType); // NOI18N
            log.error("will throw {}", errString); // NOI18N
            throw new jmri.JmriException(errString);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setConnection(HitPointType connectionType, @CheckForNull LayoutTrack o, HitPointType type) throws jmri.JmriException {
        if ((type != HitPointType.TRACK) && (type != HitPointType.NONE)) {
            String errString = MessageFormat.format("{0}.setConnection({1}, {2}, {3}); Invalid type",
                    getName(), connectionType, (o == null) ? "null" : o.getName(), type); // NOI18N
            log.error("will throw {}", errString); // NOI18N
            throw new jmri.JmriException(errString);
        }
        if (HitPointType.isTurntableRayHitType(connectionType)) {
            if ((o == null) || (o instanceof TrackSegment)) {
                setRayConnect((TrackSegment) o, connectionType.turntableTrackIndex());
            } else {
                String errString = MessageFormat.format("{0}.setConnection({1}, {2}, {3}); Invalid object: {4}",
                        getName(), connectionType, o.getName(),
                        type, o.getClass().getName()); // NOI18N
                log.error("will throw {}", errString); // NOI18N
                throw new jmri.JmriException(errString);
            }
        } else {
            String errString = MessageFormat.format("{0}.setConnection({1}, {2}, {3}); Invalid connection type",
                    getName(), connectionType, (o == null) ? "null" : o.getName(), type); // NOI18N
            log.error("will throw {}", errString); // NOI18N
            throw new jmri.JmriException(errString);
        }
    }

    /**
     * Test if ray with this index is a mainline track or not.
     * <p>
     * Defaults to false (not mainline) if connecting track segment is missing.
     *
     * @param index the index
     * @return true if connecting track segment is mainline
     */
    public boolean isMainlineIndexed(int index) {
        boolean result = false; // assume failure (pessimist!)

        for (RayTrack rt : rayTrackList) {
            if (rt.getConnectionIndex() == index) {
                TrackSegment ts = rt.getConnect();
                if (ts != null) {
                    result = ts.isMainline();
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Test if ray at this index is a mainline track or not.
     * <p>
     * Defaults to false (not mainline) if connecting track segment is missing
     *
     * @param i the index
     * @return true if connecting track segment is mainline
     */
    public boolean isMainlineOrdered(int i) {
        boolean result = false; // assume failure (pessimist!)
        if (i < rayTrackList.size()) {
            RayTrack rt = rayTrackList.get(i);
            if (rt != null) {
                TrackSegment ts = rt.getConnect();
                if (ts != null) {
                    result = ts.isMainline();
                }
            }
        }
        return result;
    }

    @Override
    public boolean isMainline() {
        return false;
    }


    public String tLayoutBlockName = "";

    /**
     * Initialization method The name of each track segment connected to a ray
     * track is initialized by by LayoutTurntableXml, then the following method
     * is called after the entire LayoutEditor is loaded to set the specific
     * TrackSegment objects.
     *
     * @param p the layout editor
     */
    @Override
    public void setObjects(@Nonnull LayoutEditor p) {
        if (tLayoutBlockName != null && !tLayoutBlockName.isEmpty()) {
            setLayoutBlockByName(tLayoutBlockName);
        }
        tLayoutBlockName = null; /// release this memory

        rayTrackList.forEach((rt) -> {
            rt.setConnect(p.getFinder().findTrackSegmentByName(rt.connectName));
        });
    }

    /**
     * Is this turntable turnout controlled?
     *
     * @return true if so
     */
    public boolean isTurnoutControlled() {
        return turnoutControlled;
    }

    /**
     * Set if this turntable is turnout controlled.
     *
     * @param boo set true if so
     */
    public void setTurnoutControlled(boolean boo) {
        turnoutControlled = boo;
    }

    /**
     * Set turntable position to the ray with this index.
	 * Set the turnout to thrown if at the ray with this index else closed
     *
     * @param index the index
     */
    public void setPosition(int index) {
        log.debug("DIAGNOSTIC: Turntable '{}' setPosition({}) called.", getName(), index);

        if (isTurnoutControlled()) {
            boolean rayExists = false;
            for (RayTrack rt : rayTrackList) {
                if (rt.getConnectionIndex() == index) {
                    rayExists = true;
                }
                Turnout t = rt.getTurnout();
                if (t != null) {
                    if (rt.getConnectionIndex() == index) {
                        log.debug("DIAGNOSTIC:   - Commanding turnout '{}' for ray {} to THROWN.", t.getSystemName(), index);
                        t.setCommandedState(Turnout.THROWN);
                    } else {
                        // Only log this if we are actively changing it, to reduce log spam.
                        if (t.getCommandedState() != Turnout.CLOSED) log.debug("DIAGNOSTIC:   - Commanding turnout '{}' to CLOSED.", t.getSystemName());
                        t.setCommandedState(Turnout.CLOSED);
                    }
                }
            }

            if (rayExists) {
                lastKnownIndex = index;
                models.redrawPanel();
                models.setDirty();
            } else {
                log.error("{}.setPosition({}); Attempt to set the position on a non-existant ray track", getName(), index);

            }
        } else {
            log.debug("DIAGNOSTIC: Turntable '{}' setPosition({}) ignored because it is not turnout-controlled.", getName(), index);
        }
    }

    /**
     * Get the turntable position.
     *
     * @return the turntable position
     */
    public int getPosition() {
        return lastKnownIndex;
    }

    /**
     * Delete this ray track.
     *
     * @param rayTrack the ray track
     */
    public void deleteRay(@Nonnull RayTrack rayTrack) {
        TrackSegment t = null;
        if (rayTrackList == null) {
            log.error("{}.deleteRay(null); rayTrack is null", getName());
        } else {
            t = rayTrack.getConnect();
            getRayTrackList().remove(rayTrack.getConnectionIndex());
            rayTrack.dispose();
        }
        if (t != null) {
            models.removeTrackSegment(t);
        }

        // update the panel
        models.redrawPanel();
        models.setDirty();
    }

    /**
     * Remove this object from display and persistance.
     */
    public void remove() {
        // remove from persistance by flagging inactive
        active = false;
        if (virtualSignalMast != null) {
            jmri.InstanceManager.getDefault(jmri.SignalMastManager.class).deregister(virtualSignalMast);
            virtualSignalMast = null;
        }
    }

    private boolean active = true;

    /**
     * Get if turntable is active.
     * "active" means that the object is still displayed, and should be stored.
     * @return true if active, else false.
     */
    public boolean isActive() {
        return active;
    }
	
	@CheckForNull
    public SignalMast getVirtualSignalMast() { return virtualSignalMast; }

    public class RayTrack {

        /**
         * constructor for RayTracks
         *
         * @param angle its angle
         * @param index its index
         */
        public RayTrack(double angle, int index) {
            rayAngle = MathUtil.wrapPM360(angle);
            connect = null;
            connectionIndex = index;

            disabled = false;
            disableWhenOccupied = false;
        }

        // persistant instance variables
        private double rayAngle = 0.0;
        private TrackSegment connect = null;
        private int connectionIndex = -1;

        private boolean disabled = false;
        private boolean disableWhenOccupied = false;

        //
        // Accessor routines
        //
        /**
         * Set ray track disabled.
         *
         * @param boo set true to disable
         */
        public void setDisabled(boolean boo) {
            if (disabled != boo) {
                disabled = boo;
                if (models != null) {
                    models.redrawPanel();
                }
            }
        }

        /**
         * Is this ray track disabled?
         *
         * @return true if so
         */
        public boolean isDisabled() {
            return disabled;
        }

        /**
         * Set ray track disabled if occupied.
         *
         * @param boo set true to disable if occupied
         */
        public void setDisabledWhenOccupied(boolean boo) {
            if (disableWhenOccupied != boo) {
                disableWhenOccupied = boo;
                if (models != null) {
                    models.redrawPanel();
                }
            }
        }

        /**
         * Is ray track disabled if occupied?
         *
         * @return true if so
         */
        public boolean isDisabledWhenOccupied() {
            return disableWhenOccupied;
        }

        /**
         * get the track segment connected to this ray
         *
         * @return the track segment connected to this ray
         */
        // @CheckForNull termporary until we know whether this really can be null or not
        public TrackSegment getConnect() {
            return connect;
        }

        /**
         * Set the track segment connected to this ray.
         *
         * @param ts the track segment to connect to this ray
         */
        public void setConnect(TrackSegment ts) {
            connect = ts;
        }

        /**
         * Get the angle for this ray.
         *
         * @return the angle for this ray
         */
        public double getAngle() {
            return rayAngle;
        }

        /**
         * Set the angle for this ray.
         *
         * @param an the angle for this ray
         */
        public void setAngle(double an) {
            rayAngle = MathUtil.wrapPM360(an);
        }

        /**
         * Get the connection index for this ray.
         *
         * @return the connection index for this ray
         */
        public int getConnectionIndex() {
            return connectionIndex;
        }

        /**
         * Is this ray occupied?
         *
         * @return true if occupied
         */
        public boolean isOccupied() {  // temporary - accessed by View - is this topology or visualization?
            boolean result = false; // assume not
            if (connect != null) {  // does it have a connection? (yes)
                LayoutBlock lb = connect.getLayoutBlock();
                if (lb != null) {   // does the connection have a block? (yes)
                    // is the block occupied?
                    result = (lb.getOccupancy() == LayoutBlock.OCCUPIED);
                }
            }
            return result;
        }

        // initialization instance variable (used when loading a LayoutEditor)
        public String connectName = "";

        private NamedBeanHandle<Turnout> namedTurnout;
        // Turnout t;
        private int turnoutState;
        private PropertyChangeListener mTurnoutListener;

        /**
         * Set the turnout and state for this ray track.
         *
         * @param turnoutName the turnout name
         * @param state       its state
         */
        @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value="RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE",
                justification="2nd check of turnoutName is considered redundant by SpotBugs, but required by ecj") // temporary
        public void setTurnout(@Nonnull String turnoutName, int state) {
            Turnout turnout = null;
            if (mTurnoutListener == null) {
                mTurnoutListener = (PropertyChangeEvent e) -> { // if a ray turnout is thrown, set all others to closed
                    if (e.getPropertyName().equals(Turnout.PROPERTY_KNOWN_STATE)) {
                        // If this ray's turnout has been thrown, it means the user wants to align to this ray.
                        log.debug("DIAGNOSTIC: RayTrack listener for turnout '{}' (ray {}) detected property change to {}.",
                                getTurnoutName(), connectionIndex, e.getNewValue());
                        // We call setPosition() on the parent LayoutTurntable to enforce the interlocking.
                        if ((Integer) e.getNewValue() == turnoutState) { // turnoutState is THROWN
                            log.debug("DIAGNOSTIC:   - State matches target (THROWN). Calling setPosition({}).", connectionIndex);
                            LayoutTurntable.this.setPosition(connectionIndex);
                        }
                    }
                };
            }
            if (turnoutName != null) {
                turnout = jmri.InstanceManager.turnoutManagerInstance().getTurnout(turnoutName);
            }
            if (namedTurnout != null && namedTurnout.getBean() != turnout) {
                namedTurnout.getBean().removePropertyChangeListener(mTurnoutListener);
            }
            if (turnout != null && (namedTurnout == null || namedTurnout.getBean() != turnout)) {
                if (turnoutName != null && !turnoutName.isEmpty()) {
                    namedTurnout = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(turnoutName, turnout);
                    turnout.addPropertyChangeListener(mTurnoutListener, turnoutName, "Layout Editor Turntable");
                }
            }
            if (turnout == null) {
                namedTurnout = null;
            }

            if (this.turnoutState != state) {
                this.turnoutState = state;
            }
        }

        /**
         * Set the position for this ray track.
         */
        public void setPosition() {
            if (namedTurnout != null) {
                if (disableWhenOccupied && isOccupied()) {
                    log.debug("Can not setPosition of turntable ray when it is occupied");
                } else {
                    getTurnout().setCommandedState(turnoutState);
                }
            }
        }

        /**
         * Get the turnout for this ray track.
         *
         * @return the turnout or null
         */
        // @CheckForNull temporary until we have central paradigm for null
        public Turnout getTurnout() {
            if (namedTurnout == null) {
                return null;
            }
            return namedTurnout.getBean();
        }

        /**
         * Get the turnout name for the ray track.
         *
         * @return the turnout name
         */
        @CheckForNull
        public String getTurnoutName() {
            if (namedTurnout == null) {
                return null;
            }
            return namedTurnout.getName();
        }

        /**
         * Get the state for the turnout for this ray track.
         *
         * @return the state
         */
        public int getTurnoutState() {
            return turnoutState;
        }

        /**
         * Dispose of this ray track.
         */
        void dispose() {
            if (getTurnout() != null) {
                getTurnout().removePropertyChangeListener(mTurnoutListener);
            }
            if (lastKnownIndex == connectionIndex) {
                lastKnownIndex = -1;
            }
        }
    }   // class RayTrack

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reCheckBlockBoundary() {
                LayoutBlock block = getLayoutBlock();
        if (block != null) {
            block.updatePaths();
        }
        for (RayTrack ray : rayTrackList) {
            TrackSegment segment = ray.getConnect();
            if (segment != null) {
                LayoutBlock rayBlock = segment.getLayoutBlock();
                if (rayBlock != null && rayBlock != block) {
                    rayBlock.updatePaths();
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @CheckForNull
    protected List<LayoutConnectivity> getLayoutConnectivity() {
        final List<LayoutConnectivity> c = new ArrayList<>();
        LayoutBlock turntableBlock = getLayoutBlock();
        log.error("TURNTABLE_CONNECTIVITY for " + getName() + ": Turntable block is '" + (turntableBlock != null ? turntableBlock.getDisplayName() : "null") + "'");

        if (turntableBlock == null) {
            log.info("TURNTABLE_CONNECTIVITY for " + getName() + ": No turntable block, returning empty list.");
            return c;
        }

        for (RayTrack ray : rayTrackList) {
            TrackSegment connectedSegment = ray.getConnect();
            log.info("TURNTABLE_CONNECTIVITY for " + getName() + ": Processing ray " + ray.getConnectionIndex());
            if (connectedSegment != null) {
                LayoutBlock connectedBlock = connectedSegment.getLayoutBlock();
                log.info("TURNTABLE_CONNECTIVITY for " + getName() + ": Ray " + ray.getConnectionIndex() + " connects to block " + (connectedBlock != null ? connectedBlock.getDisplayName() : "null"));
                if (connectedBlock != null && connectedBlock != turntableBlock) {
                    log.info("TURNTABLE_CONNECTIVITY for " + getName() + ": Adding connection between " + connectedBlock.getDisplayName() + " and " + turntableBlock.getDisplayName());
                    c.add(new LayoutConnectivity(connectedBlock, turntableBlock));
                    c.add(new LayoutConnectivity(turntableBlock, connectedBlock));
                } else {
                    log.info("TURNTABLE_CONNECTIVITY for " + getName() + ": Skipping connection for ray " + ray.getConnectionIndex() + " because connected block is null or same as turntable block.");
                }
            } else {
                log.info("TURNTABLE_CONNECTIVITY for " + getName() + ": Ray " + ray.getConnectionIndex() + " has no connected segment.");
            }
        }
        log.info("TURNTABLE_CONNECTIVITY for " + getName() + ": Returning " + c.size() + " connectivity items.");
        return c;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public List<HitPointType> checkForFreeConnections() {
        List<HitPointType> result = new ArrayList<>();

        for (int k = 0; k < getNumberRays(); k++) {
            if (getRayConnectOrdered(k) == null) {
                result.add(HitPointType.turntableTrackIndexedValue(k));
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean checkForUnAssignedBlocks() {
        // Layout turnouts get their block information from the
        // track segments attached to their rays so...
        // nothing to see here... move along...
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkForNonContiguousBlocks(
            @Nonnull HashMap<String, List<Set<String>>> blockNamesToTrackNameSetsMap) {
        /*
        * For each (non-null) blocks of this track do:
        * #1) If it's got an entry in the blockNamesToTrackNameSetMap then
        * #2) If this track is already in the TrackNameSet for this block
        *     then return (done!)
        * #3) else add a new set (with this block// track) to
        *     blockNamesToTrackNameSetMap and check all the connections in this
        *     block (by calling the 2nd method below)
        * <p>
        *     Basically, we're maintaining contiguous track sets for each block found
        *     (in blockNamesToTrackNameSetMap)
         */

        // We're using a map here because it is convient to
        // use it to pair up blocks and connections
        Map<LayoutTrack, String> blocksAndTracksMap = new HashMap<>();
        for (int k = 0; k < getNumberRays(); k++) {
            TrackSegment ts = getRayConnectOrdered(k);
            if (ts != null) {
                String blockName = ts.getBlockName();
                blocksAndTracksMap.put(ts, blockName);
            }
        }

        List<Set<String>> TrackNameSets;
        Set<String> TrackNameSet;
        for (Map.Entry<LayoutTrack, String> entry : blocksAndTracksMap.entrySet()) {
            LayoutTrack theConnect = entry.getKey();
            String theBlockName = entry.getValue();

            TrackNameSet = null;    // assume not found (pessimist!)
            TrackNameSets = blockNamesToTrackNameSetsMap.get(theBlockName);
            if (TrackNameSets != null) { // (#1)
                for (Set<String> checkTrackNameSet : TrackNameSets) {
                    if (checkTrackNameSet.contains(getName())) { // (#2)
                        TrackNameSet = checkTrackNameSet;
                        break;
                    }
                }
            } else {    // (#3)
                log.debug("*New block (''{}'') trackNameSets", theBlockName);
                TrackNameSets = new ArrayList<>();
                blockNamesToTrackNameSetsMap.put(theBlockName, TrackNameSets);
            }
            if (TrackNameSet == null) {
                TrackNameSet = new LinkedHashSet<>();
                TrackNameSets.add(TrackNameSet);
            }
            if (TrackNameSet.add(getName())) {
                log.debug("*    Add track ''{}'' to trackNameSet for block ''{}''", getName(), theBlockName);
            }
            theConnect.collectContiguousTracksNamesInBlockNamed(theBlockName, TrackNameSet);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void collectContiguousTracksNamesInBlockNamed(@Nonnull String blockName,
            @Nonnull Set<String> TrackNameSet) {
        if (!TrackNameSet.contains(getName())) {
            // for all the rays with matching blocks in this turnout
            //  #1) if its track segment's block is in this block
            //  #2)     add turntable to TrackNameSet (if not already there)
            //  #3)     if the track segment isn't in the TrackNameSet
            //  #4)         flood it
            for (int k = 0; k < getNumberRays(); k++) {
                TrackSegment ts = getRayConnectOrdered(k);
                if (ts != null) {
                    String blk = ts.getBlockName();
                    if ((!blk.isEmpty()) && (blk.equals(blockName))) { // (#1)
                        // if we are added to the TrackNameSet
                        if (TrackNameSet.add(getName())) {
                            log.debug("*    Add track ''{}'' for block ''{}''", getName(), blockName);
                        }
                        // it's time to play... flood your neighbours!
                        ts.collectContiguousTracksNamesInBlockNamed(blockName,
                                TrackNameSet); // (#4)
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAllLayoutBlocks(LayoutBlock layoutBlock) {
        setLayoutBlock(layoutBlock);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canRemove() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTypeName() {
        return Bundle.getMessage("TypeName_Turntable");
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutTurntable.class);

}
