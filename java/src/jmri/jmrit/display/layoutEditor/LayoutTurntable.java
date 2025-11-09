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

        radius = 25.0; // initial default, change asap.
    }

    // defined constants
    // operational instance variables (not saved between sessions)
    private NamedBeanHandle<LayoutBlock> namedLayoutBlock = null;

    private boolean dispatcherManaged = false;
    private boolean turnoutControlled = false;
    private double radius = 25.0;
    private int knownIndex = -1;
    private int commandedIndex = -1;

    private int signalIconPlacement = 0; // 0: Do Not Place, 1: Left, 2: Right

    private NamedBeanHandle<SignalMast> bufferSignalMast;
    private NamedBeanHandle<SignalMast> exitSignalMast;

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

    public boolean isDispatcherManaged() {
        return dispatcherManaged;
    }

    public void setDispatcherManaged(boolean managed) {
        dispatcherManaged = managed;
    }

    public int getSignalIconPlacement() {
        return signalIconPlacement;
    }

    public void setSignalIconPlacement(int placement) {
        this.signalIconPlacement = placement;
    }

    public SignalMast getBufferMast() {
        if (bufferSignalMast == null) {
            return null;
        }
        return bufferSignalMast.getBean();
    }

    public String getBufferSignalMastName() {
        if (bufferSignalMast == null) {
            return "";
        }
        return bufferSignalMast.getName();
    }

    public void setBufferSignalMast(String name) {
        if (name == null || name.isEmpty()) {
            bufferSignalMast = null;
            return;
        }
        SignalMast mast = InstanceManager.getDefault(SignalMastManager.class).getSignalMast(name);
        if (mast != null) {
            bufferSignalMast = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(name, mast);
        } else {
            bufferSignalMast = null;
        }
    }

    public SignalMast getExitSignalMast() {
        if (exitSignalMast == null) {
            return null;
        }
        return exitSignalMast.getBean();
    }

    public String getExitSignalMastName() {
        if (exitSignalMast == null) {
            return "";
        }
        return exitSignalMast.getName();
    }

    public void setExitSignalMast(String name) {
        if (name == null || name.isEmpty()) {
            exitSignalMast = null;
            return;
        }
        SignalMast mast = InstanceManager.getDefault(SignalMastManager.class).getSignalMast(name);
        if (mast != null) {
            exitSignalMast = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(name, mast);
        } else {
            exitSignalMast = null;
        }
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
                } else {
                    namedLayoutBlock = null;
                }
            } else {
                namedLayoutBlock = null;
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
    public String tExitSignalMastName = "";
    public String tBufferSignalMastName = "";

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

        if (tBufferSignalMastName != null && !tBufferSignalMastName.isEmpty()) {
            setBufferSignalMast(tBufferSignalMastName);
        }
        tBufferSignalMastName = null;
        if (tExitSignalMastName != null && !tExitSignalMastName.isEmpty()) {
            setExitSignalMast(tExitSignalMastName);
        }
        tExitSignalMastName = null;

        rayTrackList.forEach((rt) -> {
            rt.setConnect(p.getFinder().findTrackSegmentByName(rt.connectName));
            if (rt.approachMastName != null && !rt.approachMastName.isEmpty()) {
                rt.setApproachMast(rt.approachMastName);
            }
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
     *
     * @param index the index
     */
    public void setPosition(int index) {
        if (isTurnoutControlled()) {
            boolean found = false; // assume failure (pessimist!)
            for (RayTrack rt : rayTrackList) {
                if (rt.getConnectionIndex() == index) {
                    commandedIndex = index;
                    rt.setPosition();
                    models.redrawPanel();
                    models.setDirty();
                    found = true;
                    break;
                }
            }
            if (!found) {
                log.error("{}.setPosition({}); Attempt to set the position on a non-existant ray track",
                        getName(), index);
            }
        }
    }

    /**
     * Get the turntable position.
     *
     * @return the turntable position
     */
    public int getPosition() {
        return knownIndex;
    }

    public int getCommandedPosition() {
        return commandedIndex;
    }

    /**
     * Delete this ray track.
     *
     * @param rayTrack the ray track
     */
    public void deleteRay(@Nonnull RayTrack rayTrack) {
        TrackSegment t = null;
        if (rayTrackList == null) {
            log.error("{}.deleteRay(null); rayTrack is null", getName()); // NOI18N
        } else {
            t = rayTrack.getConnect();
            getRayTrackList().remove(rayTrack);
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

    /**
     * Checks if the given mast is an approach mast for any ray on this turntable.
     * @param mast The SignalMast to check.
     * @return true if it is an approach mast for one of the rays.
     */
    public boolean isApproachMast(SignalMast mast) {
        if (mast == null) {
            return false;
        }
        for (RayTrack ray : rayTrackList) {
            if (mast.equals(ray.getApproachMast())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the given block is one of the ray blocks for this turntable.
     * @param block The Block to check.
     * @return true if it is a block for one of the rays.
     */
    public boolean isRayBlock(Block block) {
        if (block == null) {
            return false;
        }
        for (RayTrack ray : rayTrackList) {
            TrackSegment ts = ray.getConnect();
            if (ts != null && ts.getLayoutBlock() != null && block.equals(ts.getLayoutBlock().getBlock())) {
                return true;
            }
        }
        return false;
    }


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
        private NamedBeanHandle<SignalMast> approachMast;

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
         * Get the approach signal mast for this ray.
         * @return The signal mast, or null.
         */
        public SignalMast getApproachMast() {
            if (approachMast == null) {
                return null;
            }
            return approachMast.getBean();
        }

        public String getApproachMastName() {
            if (approachMast == null) {
                return "";
            }
            return approachMast.getName();
        }

        /**
         * Set the approach signal mast for this ray by name.
         * @param name The name of the signal mast.
         */
        public void setApproachMast(String name) {
            if (name == null || name.isEmpty()) {
                approachMast = null;
                return;
            }
            SignalMast mast = InstanceManager.getDefault(SignalMastManager.class).getSignalMast(name);
            if (mast != null) {
                approachMast = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(name, mast);
            } else {
                approachMast = null;
            }
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
        public String approachMastName = "";

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
                mTurnoutListener = (PropertyChangeEvent e) -> { // Lambda expression for listener
                    if ("KnownState".equals(e.getPropertyName())) {
                        int turnoutState = (Integer) e.getNewValue();
                        if (turnoutState == Turnout.THROWN) {
                            // This ray is now the active one. Update the turntable's known position.
                            knownIndex = connectionIndex;
                        } else if (turnoutState == Turnout.CLOSED) {
                            // If the currently known active ray is now closed, turntable is un-aligned.
                            if (knownIndex == connectionIndex) {
                                knownIndex = -1;
                            }
                        }
                        models.redrawPanel();
                        models.setDirty();
                    } else if ("CommandedState".equals(e.getPropertyName())) {
                        if ((Integer) e.getNewValue() == Turnout.THROWN) {
                            commandedIndex = connectionIndex;
                            models.redrawPanel();
                            models.setDirty();

                            // This is the "Smart Listener" logic.
                            // If this ray was commanded THROWN, ensure all other rays are commanded CLOSED.
                            if ((Integer) e.getNewValue() == Turnout.THROWN) {
                                log.debug("Turntable Ray {} commanded THROWN, ensuring other rays are CLOSED.", connectionIndex);
                                for (RayTrack otherRay : LayoutTurntable.this.rayTrackList) {
                                    // Check this isn't the current ray and that it has a turnout assigned.
                                    if (otherRay.getConnectionIndex() != connectionIndex && otherRay.getTurnout() != null) {
                                        // Only send the command if it's not already CLOSED to avoid loops.
                                        if (otherRay.getTurnout().getCommandedState() != Turnout.CLOSED) {
                                            otherRay.getTurnout().setCommandedState(Turnout.CLOSED);
                                        }
                                    }
                                }
                            }
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
            if (turnout != null) {
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
                if (disableWhenOccupied && isOccupied()) { // isOccupied is on RayTrack, so check must be here
                    log.debug("Can not setPosition of turntable ray when it is occupied");
                } else {
                    // This method is now only called by manual clicks on the panel.
                    // The listener above handles the interlocking for all command sources.
                    getTurnout().setCommandedState(Turnout.THROWN);
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
            if (knownIndex == connectionIndex) {
                knownIndex = -1;
            }
        }
    }   // class RayTrack

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reCheckBlockBoundary() {
        // nothing to see here... move along...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @CheckForNull
    protected List<LayoutConnectivity> getLayoutConnectivity() {
        // nothing to see here... move along...
        return null;
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
     * Checks if the path represented by the blocks crosses this turntable.
     * @param block1 A block in the path.
     * @param block2 Another block in the path.
     * @return true if the path crosses this turntable.
     */
    public boolean isTurntableBoundary(Block block1, Block block2) {
        if (getLayoutBlock() == null) {
            return false;
        }
        Block turntableBlock = getLayoutBlock().getBlock();
        if (turntableBlock == null) {
            return false;
        }

        // Case 1: Moving to/from the turntable block itself.
        if ((block1 == turntableBlock && isRayBlock(block2)) ||
            (block2 == turntableBlock && isRayBlock(block1))) {
            return true;
        }

        // Case 2: Moving between two ray blocks (crossing over the turntable).
        if (isRayBlock(block1) && isRayBlock(block2)) {
            return true;
        }
        return false;
    }

    /**
     * Gets the list of turnouts and their required states to align the turntable
     * for a path defined by the given blocks.
     *
     * @param curBlock  The current block in the train's path.
     * @param prevBlock The previous block in the train's path.
     * @param nextBlock The next block in the train's path.
     * @return A list of LayoutTrackExpectedState objects for the turnouts.
     */
    public List<LayoutTrackExpectedState<LayoutTurnout>> getTurnoutList(Block curBlock, Block prevBlock, Block nextBlock) {
        List<LayoutTrackExpectedState<LayoutTurnout>> turnoutList = new ArrayList<>();
        if (!isTurnoutControlled()) {
            return turnoutList;
        }

        Block turntableBlock = (getLayoutBlock() != null) ? getLayoutBlock().getBlock() : null;
        if (turntableBlock == null) {
            return turnoutList;
        }

        int targetRay = -1;

        // Determine which ray needs to be aligned.
        if (prevBlock == turntableBlock) {
            // Train is leaving the turntable, so align to the destination ray.
            targetRay = getRayForBlock(curBlock);
        } else if (curBlock == turntableBlock) {
            // Train is entering the turntable, so align to the approaching ray.
            targetRay = getRayForBlock(prevBlock);
        }

        if (targetRay != -1) {
            Turnout t = getRayTurnout(targetRay);
            if (t != null) {
                // Create a temporary LayoutTurnout wrapper for the dispatcher.
                // This object is not on a panel and is for logic purposes only.
                // We give the dispatcher our real turnout, and our "Smart Listener"
                // on CommandedState will handle the interlocking.
                LayoutLHTurnout tempLayoutTurnout = new LayoutLHTurnout("TURNTABLE_WRAPPER_" + getId(), models) { // NOI18N
                    @Override
                    public Turnout getTurnout() {
                        // Return the real turnout.
                        return t;
                    }
                };
                // We must associate the temp layout turnout with the real turnout to satisfy the dispatcher framework
                tempLayoutTurnout.setTurnout(t.getSystemName());
                int requiredState = Turnout.THROWN;

                log.debug("Adding turntable turnout {} to list with required state {}", t.getDisplayName(), requiredState);
                turnoutList.add(new LayoutTrackExpectedState<>(tempLayoutTurnout, requiredState));
            }
        }
        return turnoutList;
    }

    private int getRayForBlock(Block block) {
        if (block == null) return -1;
        for (int i = 0; i < getNumberRays(); i++) {
            TrackSegment ts = getRayConnectOrdered(i);
            if (ts != null && ts.getLayoutBlock() != null && ts.getLayoutBlock().getBlock() == block) {
                return i;
            }
        }
        return -1;
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
        // turntables don't have blocks...
        // nothing to see here, move along...
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
