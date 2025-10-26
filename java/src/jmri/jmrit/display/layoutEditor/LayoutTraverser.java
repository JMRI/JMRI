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
 * A LayoutTraverser is a representation used by LayoutEditor to display a
 * traverser.
 * <p>
 * A LayoutTraverser has a variable number of connection points, called
 * SlotTracks. Each of these points should be connected to a TrackSegment.
 * <p>
 * Each slot gets its Block information from its
 * connected track segment.
 * <p>
 * Each slot has a unique connection index. The
 * connection index is set when the SlotTrack is created, and cannot be changed.
 * This connection index is used to maintain the identity of the radiating
 * segment to its connected Track Segment as slots are added and deleted by
 * the user.
 * <p>
 * The length and width of the traverser is variable by the user.
 *
 * @author Dave Duchamp Copyright (c) 2007
 * @author George Warner Copyright (c) 2017-2018
 * @author Dave Sand Copyright (c) 2024
 */
public class LayoutTraverser extends LayoutTrack {

    public LayoutTraverser(@Nonnull String id, @Nonnull LayoutEditor models) {
        super(id, models);
        recalculateDimensions();
    }

    // defined constants
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;
    private double slotOffset = 25.0;

    // operational instance variables (not saved between sessions)
    private NamedBeanHandle<LayoutBlock> namedLayoutBlock = null;

    private boolean dispatcherManaged = false;
    private boolean turnoutControlled = false;
    private double deckLength = 100.0;
    private double deckWidth = 50.0;
    private int orientation = HORIZONTAL;
    private boolean mainline = false;
    private int lastKnownIndex = -1;

    private int signalIconPlacement = 0; // 0: Do Not Place, 1: Left, 2: Right

    private NamedBeanHandle<SignalMast> bufferSignalMast;
    private NamedBeanHandle<SignalMast> exitSignalMast;

    // persistent instance variables (saved between sessions)

    public final List<SlotTrack> slotList = new ArrayList<>(); // list of Slot Track objects

    /**
     * Get a string that represents this object. This should only be used for
     * debugging.
     *
     * @return the string
     */
    @Override
    @Nonnull
    public String toString() {
        return "LayoutTraverser " + getName();
    }

    //
    // Accessor methods
    //
    public double getSlotOffset() { return slotOffset; }
    public void setSlotOffset(double offset) {
        if (this.slotOffset != offset) {
            this.slotOffset = offset;
            renumberSlots();
        }
    }

    public double getDeckLength() { return deckLength; }
    private void setDeckLength(double l) { deckLength = l; }
    public double getDeckWidth() { return deckWidth; }
    public void setDeckWidth(double w) {
        if (deckWidth != w) {
            deckWidth = w;
            models.redrawPanel();
            models.setDirty();
        }
    }
    public int getOrientation() { return orientation; }
    public void setOrientation(int o) {
        if (orientation != o) {
            orientation = o;
        }
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
     * Set up a LayoutBlock for this LayoutTraverser.
     *
     * @param newLayoutBlock the LayoutBlock to set
     */
    public void setLayoutBlock(@CheckForNull LayoutBlock newLayoutBlock) {
        LayoutBlock layoutBlock = getLayoutBlock(); // This is for the traverser itself
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
     * Set up a LayoutBlock for this LayoutTraverser.
     *
     * @param name the name of the new LayoutBlock
     */
    public void setLayoutBlockByName(@CheckForNull String name) {
        if ((name != null) && !name.isEmpty()) {
            setLayoutBlock(models.provideLayoutBlock(name));
        }
    }

    public void addSlotPair() {
        addSlot(0); // Side A
        addSlot(0); // Side B
        renumberSlots();
    }

    private SlotTrack addSlot(double offset) {
        SlotTrack rt = new SlotTrack(offset, getNewIndex());
        slotList.add(rt);
        return rt;
    }

    private int getNewIndex() {
        int index = -1;
        if (slotList.isEmpty()) {
            return 0;
        }

        boolean found = true;
        while (found) {
            index++;
            found = false; // assume failure (pessimist!)
            for (SlotTrack rt : slotList) {
                if (index == rt.getConnectionIndex()) {
                    found = true;
                }
            }
        }
        return index;
    }

    // the following method is only for use in loading layout traversers
    public void addSlotTrack(double offset, int index, String name) { // Note: This was likely private or package-private
        SlotTrack rt = new SlotTrack(offset, index);
        slotList.add(rt);
        rt.connectName = name;
    }

    /**
     * Get the connection for the slot with this index.
     *
     * @param index the index
     * @return the connection for the slot with this value of getConnectionIndex
     */
    @CheckForNull
    public TrackSegment getSlotConnectIndexed(int index) {
        TrackSegment result = null;
        for (SlotTrack rt : slotList) {
            if (rt.getConnectionIndex() == index) {
                result = rt.getConnect();
                break;
            }
        }
        return result;
    }

    /**
     * Get the connection for the slot at the index in the slotList.
     *
     * @param i the index in the slotList
     * @return the connection for the slot at that index in the slotList or null
     */
    @CheckForNull
    public TrackSegment getSlotConnectOrdered(int i) {
        TrackSegment result = null;

        if (i < slotList.size()) {
            SlotTrack rt = slotList.get(i);
            if (rt != null) {
                result = rt.getConnect();
            }
        }
        return result;
    }

    /**
     * Set the connection for the slot at the index in the slotList.
     *
     * @param ts    the connection
     * @param index the index in the slotList
     */
    public void setSlotConnect(@CheckForNull TrackSegment ts, int index) {
        for (SlotTrack rt : slotList) {
            if (rt.getConnectionIndex() == index) {
                rt.setConnect(ts);
                break;
            }
        }
    }

    // should only be used by xml save code
    @Nonnull
    public List<SlotTrack> getSlotList() {
        return slotList;
    }

    /**
     * Get the number of slots on traverser.
     *
     * @return the number of slots
     */
    public int getNumberSlots() {
        return slotList.size();
    }

    /**
     * Get the index for the slot at this position in the slotList.
     *
     * @param i the position in the slotList
     * @return the index
     */
    public int getSlotIndex(int i) {
        int result = 0;
        if (i < slotList.size()) {
            SlotTrack rt = slotList.get(i);
            result = rt.getConnectionIndex();
        }
        return result;
    }

    /**
     * Get the offset for the slot at this position in the slotList.
     *
     * @param i the position in the slotList
     * @return the offset
     */
    public double getSlotOffsetValue(int i) {
        double result = 0.0;
        if (i < slotList.size()) {
            SlotTrack rt = slotList.get(i);
            result = rt.getOffset();
        }
        return result;
    }

    /**
     * Set the turnout and state for the slot with this index.
     *
     * @param index       the index
     * @param turnoutName the turnout name
     * @param state       the state
     */
    public void setSlotTurnout(int index, @CheckForNull String turnoutName, int state) {
        boolean found = false; // assume failure (pessimist!)
        for (SlotTrack rt : slotList) {
            if (rt.getConnectionIndex() == index) {
                rt.setTurnout(turnoutName, state);
                found = true;
                break;
            }
        }
        if (!found) {
            log.error("{}.setSlotTurnout({}, {}, {}); Attempt to add Turnout control to a non-existant slot track",
                    getName(), index, turnoutName, state);
        }
    }

    /**
     * Get the name of the turnout for the slot at this index.
     *
     * @param i the index
     * @return name of the turnout for the slot at this index
     */
    @CheckForNull
    public String getSlotTurnoutName(int i) {
        String result = null;
        if (i < slotList.size()) {
            SlotTrack rt = slotList.get(i);
            result = rt.getTurnoutName();
        }
        return result;
    }

    /**
     * Get the turnout for the slot at this index.
     *
     * @param i the index
     * @return the turnout for the slot at this index
     */
    @CheckForNull
    public Turnout getSlotTurnout(int i) {
        Turnout result = null;
        if (i < slotList.size()) {
            SlotTrack rt = slotList.get(i);
            result = rt.getTurnout();
        }
        return result;
    }

    /**
     * Get the state of the turnout for the slot at this index.
     *
     * @param i the index
     * @return state of the turnout for the slot at this index
     */
    public int getSlotTurnoutState(int i) {
        int result = 0;
        if (i < slotList.size()) {
            SlotTrack rt = slotList.get(i);
            result = rt.getTurnoutState();
        }
        return result;
    }

    /**
     * Get if the slot at this index is disabled.
     *
     * @param i the index
     * @return true if disabled
     */
    public boolean isSlotDisabled(int i) {
        boolean result = false;    // assume not disabled
        if (i < slotList.size()) {
            SlotTrack rt = slotList.get(i);
            result = rt.isDisabled();
        }
        return result;
    }

    /**
     * Set the disabled state of the slot at this index.
     *
     * @param i   the index
     * @param boo the state
     */
    public void setSlotDisabled(int i, boolean boo) {
        if (i < slotList.size()) {
            SlotTrack rt = slotList.get(i);
            rt.setDisabled(boo);
        }
    }

    /**
     * Get the disabled when occupied state of the slot at this index.
     *
     * @param i the index
     * @return the state
     */
    public boolean isSlotDisabledWhenOccupied(int i) {
        boolean result = false;    // assume not disabled when occupied
        if (i < slotList.size()) {
            SlotTrack rt = slotList.get(i);
            result = rt.isDisabledWhenOccupied();
        }
        return result;
    }

    /**
     * Set the disabled when occupied state of the slot at this index.
     *
     * @param i   the index
     * @param boo the state
     */
    public void setSlotDisabledWhenOccupied(int i, boolean boo) {
        if (i < slotList.size()) {
            SlotTrack rt = slotList.get(i);
            rt.setDisabledWhenOccupied(boo);
        }
    }
	
	/**
     * {@inheritDoc}
     */
    @Override
    public LayoutTrack getConnection(HitPointType connectionType) throws jmri.JmriException {
        LayoutTrack result = null;
        if (HitPointType.isTraverserSlotHitType(connectionType)) {
            result = getSlotConnectIndexed(connectionType.traverserTrackIndex());
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
        if (HitPointType.isTraverserSlotHitType(connectionType)) {
            if ((o == null) || (o instanceof TrackSegment)) {
                setSlotConnect((TrackSegment) o, connectionType.traverserTrackIndex());
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
     * Test if slot with this index is a mainline track or not.
     * <p>
     * Defaults to false (not mainline) if connecting track segment is missing.
     *
     * @param index the index
     * @return true if connecting track segment is mainline
     */
    public boolean isMainlineIndexed(int index) {
        boolean result = false; // assume failure (pessimist!)

        for (SlotTrack rt : slotList) {
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
     * Test if slot at this index is a mainline track or not.
     * <p>
     * Defaults to false (not mainline) if connecting track segment is missing
     *
     * @param i the index
     * @return true if connecting track segment is mainline
     */
    public boolean isMainlineOrdered(int i) {
        boolean result = false; // assume failure (pessimist!)
        if (i < slotList.size()) {
            SlotTrack rt = slotList.get(i);
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
        return mainline;
    }

    /**
     * Set the mainline status of the traverser bridge itself.
     * @param main true if the bridge is mainline, false otherwise.
     */
    public void setMainline(boolean main) {
        if (mainline != main) {
            mainline = main;
            models.redrawPanel();
            models.setDirty();
        }
    }

    public String tLayoutBlockName = "";
    public String tExitSignalMastName = "";
    public String tBufferSignalMastName = "";

    /**
     * Initialization method The name of each track segment connected to a slot
     * track is initialized by by LayoutTraverserXml, then the following method
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

        slotList.forEach((rt) -> {
            log.info("calling setConnect for rt {}", rt);
            rt.setConnect(p.getFinder().findTrackSegmentByName(rt.connectName));
            if (rt.approachMastName != null && !rt.approachMastName.isEmpty()) {
                rt.setApproachMast(rt.approachMastName);
            }
            log.info("called setConnect for rt {}", rt);
        });

        // Recalculate dimensions now that all slots are loaded
        recalculateDimensions();
    }

    /**
     * Is this traverser turnout controlled?
     *
     * @return true if so
     */
    public boolean isTurnoutControlled() {
        return turnoutControlled;
    }

    /**
     * Set if this traverser is turnout controlled.
     *
     * @param boo set true if so
     */
    public void setTurnoutControlled(boolean boo) {
        turnoutControlled = boo;
    }

    /**
     * Set traverser position to the slot with this index.
     *
     * @param index the index
     */
    public void setPosition(int index) {
        if (isTurnoutControlled()) {
            boolean found = false; // assume failure (pessimist!)
            for (SlotTrack rt : slotList) {
                if (rt.getConnectionIndex() == index) {
                    lastKnownIndex = index;
                    rt.setPosition();
                    models.redrawPanel();
                    models.setDirty();
                    found = true;
                    break;
                }
            }
            if (!found) {
                log.error("{}.setPosition({}); Attempt to set the position on a non-existant slot track",
                        getName(), index);
            }
        }
    }

    /**
     * Get the traverser position.
     *
     * @return the traverser position
     */
    public int getPosition() {
        return lastKnownIndex;
    }

    public void deleteTrackPair(int pairIndex) {
        if (pairIndex < 0 || (pairIndex * 2 + 1) >= slotList.size()) {
            return;
        }
        SlotTrack slotB = slotList.get(pairIndex * 2 + 1);
        SlotTrack slotA = slotList.get(pairIndex * 2);
        slotList.remove(slotB);
        slotList.remove(slotA);
        slotB.dispose();
        slotA.dispose();
        renumberSlots();
    }

    private void renumberSlots() {
        int numPairs = getNumberSlots() / 2;
        double totalWidth = (numPairs > 1) ? (numPairs - 1) * slotOffset : 0;
        double firstOffset = -totalWidth / 2.0;

        for (int i = 0; i < numPairs; i++) {
            double offset = firstOffset + (i * slotOffset);
            slotList.get(i * 2).setOffset(offset);
            slotList.get(i * 2 + 1).setOffset(offset);
        }
        recalculateDimensions();
    }

    private void recalculateDimensions() {
        int numPairs = getNumberSlots() / 2;
        double newLength = (numPairs > 0) ? slotOffset + (slotOffset * numPairs) : slotOffset;
        setDeckLength(newLength);
    }

    public void moveSlotPairUp(int pairIndex) {
        if (pairIndex > 0) {
            Collections.swap(slotList, pairIndex * 2, (pairIndex - 1) * 2);
            Collections.swap(slotList, pairIndex * 2 + 1, (pairIndex - 1) * 2 + 1);
            renumberSlots();
        }
    }

    public void moveSlotPairDown(int pairIndex) {
        if (pairIndex < (getNumberSlots() / 2) - 1) {
            Collections.swap(slotList, pairIndex * 2, (pairIndex + 1) * 2);
            Collections.swap(slotList, pairIndex * 2 + 1, (pairIndex + 1) * 2 + 1);
            renumberSlots();
        }
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
     * Get if traverser is active.
     * "active" means that the object is still displayed, and should be stored.
     * @return true if active, else false.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Checks if the given mast is an approach mast for any slot on this traverser.
     * @param mast The SignalMast to check.
     * @return true if it is an approach mast for one of the slots.
     */
    public boolean isApproachMast(SignalMast mast) {
        if (mast == null) {
            return false;
        }
        for (SlotTrack ray : slotList) {
            if (mast.equals(ray.getApproachMast())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the given block is one of the slot blocks for this traverser.
     * @param block The Block to check.
     * @return true if it is a block for one of the slots.
     */
    public boolean isSlotBlock(Block block) {
        if (block == null) {
            return false;
        }
        for (SlotTrack ray : slotList) {
            TrackSegment ts = ray.getConnect();
            if (ts != null && ts.getLayoutBlock() != null && block.equals(ts.getLayoutBlock().getBlock())) {
                return true;
            }
        }
        return false;
    }


    public class SlotTrack {

        /**
         * constructor for SlotTracks
         *
         * @param offset its offset
         * @param index its index
         */
        public SlotTrack(double offset, int index) {
            this.offset = offset;
            connect = null;
            connectionIndex = index;

            disabled = false;
            disableWhenOccupied = false;
        }

        // persistant instance variables
        private double offset = 0.0;
        private TrackSegment connect = null;
        private int connectionIndex = -1;

        private boolean disabled = false;
        private boolean disableWhenOccupied = false;
        private NamedBeanHandle<SignalMast> approachMast;

        //
        // Accessor routines
        //
        /**
         * Set slot track disabled.
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
         * Is this slot track disabled?
         *
         * @return true if so
         */
        public boolean isDisabled() {
            return disabled;
        }

        /**
         * Set slot track disabled if occupied.
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
         * Is slot track disabled if occupied?
         *
         * @return true if so
         */
        public boolean isDisabledWhenOccupied() {
            return disableWhenOccupied;
        }

        /**
         * get the track segment connected to this slot
         *
         * @return the track segment connected to this slot
         */
        public TrackSegment getConnect() {
            // This should be a simple getter, just like in LayoutTurntable.RayTrack.
            // All connection resolution happens in LayoutTraverser.setObjects().
            return connect;
        }

        /**
         * Set the track segment connected to this slot.
         *
         * @param ts the track segment to connect to this slot
         */
        public void setConnect(TrackSegment ts) {
            // This should ONLY set the live object reference, just like in LayoutTurntable.RayTrack.
            // The public 'connectName' field is managed separately by the editor and XML loader.
            connect = ts;
        }

        /**
         * Get the offset for this slot.
         *
         * @return the offset for this slot
         */
        public double getOffset() {
            return offset;
        }

        /**
         * Set the offset for this slot.
         *
         * @param o the offset for this slot
         */
        public void setOffset(double o) {
            this.offset = o;
        }

        /**
         * Get the connection index for this slot.
         *
         * @return the connection index for this slot
         */
        public int getConnectionIndex() {
            return connectionIndex;
        }

        /**
         * Get the approach signal mast for this slot.
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
         * Set the approach signal mast for this slot by name.
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
         * Is this slot occupied?
         *
         * @return true if occupied
         */
        public boolean isOccupied() {
            boolean result = false; // assume not
            if (connect != null) {
                LayoutBlock lb = connect.getLayoutBlock();
                if (lb != null) {
                    result = (lb.getOccupancy() == LayoutBlock.OCCUPIED);
                }
            }
            return result;
        }

        // initialization instance variable (used when loading a LayoutEditor)
        public String connectName = "";
        public String approachMastName = "";

        private NamedBeanHandle<Turnout> namedTurnout;
        private int turnoutState;
        private PropertyChangeListener mTurnoutListener;

        /**
         * Set the turnout and state for this slot track.
         *
         * @param turnoutName the turnout name
         * @param state       its state
         */
        @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value="RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE",
                justification="2nd check of turnoutName is considered redundant by SpotBugs, but required by ecj") // temporary
        public void setTurnout(@Nonnull String turnoutName, int state) {
            Turnout turnout = null;
            if (mTurnoutListener == null) {
                mTurnoutListener = (PropertyChangeEvent e) -> {
                    int turnoutState = getTurnout().getKnownState();
                    if (turnoutState == Turnout.THROWN) {
                        // This slot is now the active one.
                        // Update the traverser's position indicator.
                        if (lastKnownIndex != connectionIndex) {
                            lastKnownIndex = connectionIndex;
                            models.redrawPanel();
                            models.setDirty();
                        }

                        // Command all other slot turnouts to CLOSED.
                        for (SlotTrack otherRay : LayoutTraverser.this.slotList) {
                            if (otherRay != this && otherRay.getTurnout() != null) {
                                // Check state before commanding to prevent potential listener loops
                                if (otherRay.getTurnout().getCommandedState() != Turnout.CLOSED) {
                                    otherRay.getTurnout().setCommandedState(Turnout.CLOSED);
                                }
                            }
                        }
                    } else if (turnoutState == Turnout.CLOSED) {
                        // This turnout is now closed. Check if all are closed.
                        boolean allClosed = true;
                        for (SlotTrack otherRay : LayoutTraverser.this.slotList) {
                            if (otherRay.getTurnout() != null && otherRay.getTurnout().getKnownState() != Turnout.CLOSED) {
                                allClosed = false;
                                break;
                            }
                        }
                        if (allClosed && lastKnownIndex != -1) {
                            lastKnownIndex = -1; // All turnouts are closed, blank the bridge
                            models.redrawPanel();
                            models.setDirty();
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
                    turnout.addPropertyChangeListener(mTurnoutListener, turnoutName, "Layout Editor Traverser");
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
         * Set the position for this slot track.
         */
        public void setPosition() {
            if (namedTurnout != null) {
                if (disableWhenOccupied && isOccupied()) { // isOccupied is on SlotTrack, so check must be here
                    log.debug("Can not setPosition of traverser slot when it is occupied");
                } else {
                    // The listener attached to the turnout will handle de-selecting other slots
                    // by setting their turnouts to CLOSED.
                    getTurnout().setCommandedState(Turnout.THROWN);
                }
            }
        }

        /**
         * Get the turnout for this slot track.
         *
         * @return the turnout or null
         */
        public Turnout getTurnout() {
            if (namedTurnout == null) {
                return null;
            }
            return namedTurnout.getBean();
        }

        /**
         * Get the turnout name for the slot track.
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
         * Get the state for the turnout for this slot track.
         *
         * @return the state
         */
        public int getTurnoutState() {
            return turnoutState;
        }

        /**
         * Dispose of this slot track.
         */
        void dispose() {
            if (getTurnout() != null) {
                getTurnout().removePropertyChangeListener(mTurnoutListener);
            }
            if (lastKnownIndex == connectionIndex) {
                lastKnownIndex = -1;
            }
        }
    }   // class SlotTrack

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

        for (int k = 0; k < getNumberSlots(); k++) {
            if (getSlotConnectOrdered(k) == null) {
                result.add(HitPointType.traverserTrackIndexedValue(k));
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
        // track segments attached to their slots so...
        // nothing to see here... move along...
        return true;
    }

    /**
     * Checks if the path represented by the blocks crosses this traverser.
     * @param block1 A block in the path.
     * @param block2 Another block in the path.
     * @return true if the path crosses this traverser.
     */
    public boolean isTraverserBoundary(Block block1, Block block2) {
        if (getLayoutBlock() == null) {
            return false;
        }
        Block traverserBlock = getLayoutBlock().getBlock();
        if (traverserBlock == null) {
            return false;
        }

        // Case 1: Moving to/from the traverser block itself.
        if ((block1 == traverserBlock && isSlotBlock(block2)) ||
            (block2 == traverserBlock && isSlotBlock(block1))) {
            return true;
        }

        // Case 2: Moving between two slot blocks (crossing over the traverser).
        if (isSlotBlock(block1) && isSlotBlock(block2)) {
            return true;
        }
        return false;
    }

    /**
     * Gets the list of turnouts and their required states to align the traverser
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

        Block traverserBlock = (getLayoutBlock() != null) ? getLayoutBlock().getBlock() : null;
        if (traverserBlock == null) {
            return turnoutList;
        }

        int targetRay = -1;

        // Determine which slot needs to be aligned.
        if (prevBlock == traverserBlock) {
            // Train is leaving the traverser, so align to the destination slot.
            targetRay = getSlotForBlock(curBlock);
        } else if (curBlock == traverserBlock) {
            // Train is entering the traverser, so align to the approaching slot.
            targetRay = getSlotForBlock(prevBlock);
        }

        if (targetRay != -1) {
            Turnout t = getSlotTurnout(targetRay);
            if (t != null) {
                // Create a temporary LayoutTurnout wrapper for the dispatcher.
                // This object is not on a panel and is for logic purposes only.
                LayoutLHTurnout tempLayoutTurnout = new LayoutLHTurnout("TRAVERSER_WRAPPER_" + t.getSystemName(), models);
                tempLayoutTurnout.setTurnout(t.getSystemName());
                int requiredState = Turnout.THROWN;

                log.debug("Adding traverser turnout {} to list with required state {}", t.getDisplayName(), requiredState);
                turnoutList.add(new LayoutTrackExpectedState<>(tempLayoutTurnout, requiredState));
            }
        }
        return turnoutList;
    }

    private int getSlotForBlock(Block block) {
        if (block == null) return -1;
        for (int i = 0; i < getNumberSlots(); i++) {
            TrackSegment ts = getSlotConnectOrdered(i);
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
        for (int k = 0; k < getNumberSlots(); k++) {
            TrackSegment ts = getSlotConnectOrdered(k);
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
                log.debug("*New block ('{}') trackNameSets", theBlockName);
                TrackNameSets = new ArrayList<>();
                blockNamesToTrackNameSetsMap.put(theBlockName, TrackNameSets);
            }
            if (TrackNameSet == null) {
                TrackNameSet = new LinkedHashSet<>();
                TrackNameSets.add(TrackNameSet);
            }
            if (TrackNameSet.add(getName())) {
                log.debug("*    Add track '{}' to trackNameSet for block '{}'", getName(), theBlockName);
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
            // for all the slots with matching blocks in this turnout
            //  #1) if its track segment's block is in this block
            //  #2)     add traverser to TrackNameSet (if not already there)
            //  #3)     if the track segment isn't in the TrackNameSet
            //  #4)         flood it
            for (int k = 0; k < getNumberSlots(); k++) {
                TrackSegment ts = getSlotConnectOrdered(k);
                if (ts != null) {
                    String blk = ts.getBlockName();
                    if ((!blk.isEmpty()) && (blk.equals(blockName))) { // (#1)
                        // if we are added to the TrackNameSet
                        if (TrackNameSet.add(getName())) {
                            log.debug("*    Add track '{}' for block '{}'", getName(), blockName);
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
        // traversers don't have blocks...
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
        return Bundle.getMessage("TypeName_Traverser");
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutTraverser.class);

}
