package jmri.jmrit.display.layoutEditor;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.*;

/**
 * TrackSegment is a segment of track on a layout linking two nodes of the
 * layout. A node may be a LayoutTurnout, a LevelXing or a PositionablePoint.
 * <p>
 * PositionablePoints have 1 or 2 connection points. LayoutTurnouts have 3 or 4
 * (crossovers) connection points, designated A, B, C, and D. LevelXing's have 4
 * connection points, designated A, B, C, and D.
 * <p>
 * TrackSegments carry the connectivity information between the three types of
 * nodes. Track Segments serve as the lines in a graph which shows layout
 * connectivity. For the connectivity graph to be valid, all connections between
 * nodes must be via TrackSegments.
 * <p>
 * TrackSegments carry Block information, as do LayoutTurnouts and LevelXings.
 * <p>
 * Arrows and bumpers are visual, presentation aspects handled in the View.
 *
 * @author Dave Duchamp Copyright (p) 2004-2009
 * @author George Warner Copyright (c) 2017-2019
 */
public class TrackSegment extends LayoutTrack {

    public TrackSegment(@Nonnull String id,
            @CheckForNull LayoutTrack c1, HitPointType t1,
            @CheckForNull LayoutTrack c2, HitPointType t2,
            boolean main,
            @Nonnull LayoutEditor models) {
        super(id, models);

        // validate input
        if ((c1 == null) || (c2 == null)) {
            log.error("Invalid object in TrackSegment constructor call - {}", id);
        }

        if (HitPointType.isConnectionHitType(t1)) {
            connect1 = c1;
            type1 = t1;
        } else {
            log.error("Invalid connect type 1 ('{}') in TrackSegment constructor - {}", t1, id);
        }
        if (HitPointType.isConnectionHitType(t2)) {
            connect2 = c2;
            type2 = t2;
        } else {
            log.error("Invalid connect type 2 ('{}') in TrackSegment constructor - {}", t2, id);
        }

        mainline = main;
    }

    // alternate constructor for loading layout editor panels
    public TrackSegment(@Nonnull String id,
            @CheckForNull String c1Name, HitPointType t1,
            @CheckForNull String c2Name, HitPointType t2,
            boolean main,
            @Nonnull LayoutEditor models) {
        super(id, models);

        tConnect1Name = c1Name;
        type1 = t1;
        tConnect2Name = c2Name;
        type2 = t2;

        mainline = main;
    }


    // defined constants
    // operational instance variables (not saved between sessions)
    private NamedBeanHandle<LayoutBlock> namedLayoutBlock = null;

    // persistent instances variables (saved between sessions)
    protected LayoutTrack connect1 = null;
    protected HitPointType type1 = HitPointType.NONE;
    protected LayoutTrack connect2 = null;
    protected HitPointType type2 = HitPointType.NONE;
    private boolean mainline = false;

    /**
     * Get debugging string for the TrackSegment.
     *
     * @return text showing id and connections of this segment
     */
    @Override
    public String toString() {
        return "TrackSegment " + getName()
                + " c1:{" + getConnect1Name() + " (" + type1 + ")},"
                + " c2:{" + getConnect2Name() + " (" + type2 + ")}";

    }

    /*
    * Accessor methods
     */
    @Nonnull
    public String getBlockName() {
        String result = null;
        if (namedLayoutBlock != null) {
            result = namedLayoutBlock.getName();
        }
        return ((result == null) ? "" : result);
    }

    public HitPointType getType1() {
        return type1;
    }

    public HitPointType getType2() {
        return type2;
    }

    public LayoutTrack getConnect1() {
        return connect1;
    }

    public LayoutTrack getConnect2() {
        return connect2;
    }

    /**
     * set a new connection 1
     *
     * @param connectTrack   the track we want to connect to
     * @param connectionType where on that track we want to be connected
     */
    protected void setNewConnect1(@CheckForNull LayoutTrack connectTrack, HitPointType connectionType) {
        connect1 = connectTrack;
        type1 = connectionType;
    }

    /**
     * set a new connection 2
     *
     * @param connectTrack   the track we want to connect to
     * @param connectionType where on that track we want to be connected
     */
    protected void setNewConnect2(@CheckForNull LayoutTrack connectTrack, HitPointType connectionType) {
        connect2 = connectTrack;
        type2 = connectionType;
    }

    /**
     * Replace old track connection with new track connection.
     *
     * @param oldTrack the old track connection.
     * @param newTrack the new track connection.
     * @param newType  the hit point type.
     * @return true if successful.
     */
    public boolean replaceTrackConnection(@CheckForNull LayoutTrack oldTrack, @CheckForNull LayoutTrack newTrack, HitPointType newType) {
        boolean result = false; // assume failure (pessimist!)
        // trying to replace old track with null?
        if (newTrack == null) {
            result = true;  // assume success (optimist!)
            //(yes) remove old connection
            if (oldTrack != null) {
                if (connect1 == oldTrack) {
                    connect1 = null;
                    type1 = HitPointType.NONE;
                } else if (connect2 == oldTrack) {
                    connect2 = null;
                    type2 = HitPointType.NONE;
                } else {
                    log.error("{}.replaceTrackConnection({}, null, {}); Attempt to remove invalid track connection",
                            getName(), oldTrack.getName(), newType);
                    result = false;
                }
            } else {
                log.warn("{}.replaceTrackConnection(null, null, {}); Can't replace null track connection with null",
                        getName(), newType);
                result = false;
            }
        } else // already connected to newTrack?
        if ((connect1 != newTrack) && (connect2 != newTrack)) {
            //(no) find a connection we can connect to
            result = true;  // assume success (optimist!)
            if (connect1 == oldTrack) {
                connect1 = newTrack;
                type1 = newType;
            } else if (connect2 == oldTrack) {
                connect2 = newTrack;
                type2 = newType;
            } else {
                log.error("{}.replaceTrackConnection({}, {}, {}); Attempt to replace invalid track connection",
                        getName(), (oldTrack == null) ? "null" : oldTrack.getName(), newTrack.getName(), newType);
                result = false;
            }
        }
        return result;
    }

    /**
     * @return true if track segment is a main line
     */
    @Override
    public boolean isMainline() {
        return mainline;
    }

    public void setMainline(boolean main) {
        if (mainline != main) {
            mainline = main;
            models.redrawPanel();
            models.setDirty();
        }
    }

    public LayoutBlock getLayoutBlock() {
        return (namedLayoutBlock != null) ? namedLayoutBlock.getBean() : null;
    }

    public String getConnect1Name() {
        return getConnectName(connect1, type1);
    }

    public String getConnect2Name() {
        return getConnectName(connect2, type2);
    }

    private String getConnectName(@CheckForNull LayoutTrack layoutTrack, HitPointType type) {
        return (layoutTrack == null) ? null : layoutTrack.getName();
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation returns null because {@link #getConnect1} and
     * {@link #getConnect2} should be used instead.
     */
    // only implemented here to suppress "does not override abstract method " error in compiler
    @Override
    public LayoutTrack getConnection(HitPointType connectionType) throws jmri.JmriException {
        // nothing to see here, move along
        throw new jmri.JmriException("Use getConnect1() or getConnect2() instead.");
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation does nothing because {@link #setNewConnect1} and
     * {@link #setNewConnect2} should be used instead.
     */
    // only implemented here to suppress "does not override abstract method " error in compiler
    @Override
    public void setConnection(HitPointType connectionType, @CheckForNull LayoutTrack o, HitPointType type) throws jmri.JmriException {
        // nothing to see here, move along
        throw new jmri.JmriException("Use setConnect1() or setConnect2() instead.");
    }

    public void setConnect1(@CheckForNull LayoutTrack o, HitPointType type) {
        type1 = type;
        connect1 = o;
    }

    public void setConnect2(@CheckForNull LayoutTrack o, HitPointType type) {
        type2 = type;
        connect2 = o;
    }

    /**
     * Set up a LayoutBlock for this Track Segment.
     *
     * @param newLayoutBlock the LayoutBlock to set
     */
    public void setLayoutBlock(@CheckForNull LayoutBlock newLayoutBlock) {
        LayoutBlock layoutBlock = getLayoutBlock();
        if (layoutBlock != newLayoutBlock) {
            //block has changed, if old block exists, decrement use
            if (layoutBlock != null) {
                layoutBlock.decrementUse();
            }
            namedLayoutBlock = null;
            if (newLayoutBlock != null) {
                String newName = newLayoutBlock.getUserName();
                if ((newName != null) && !newName.isEmpty()) {
                    namedLayoutBlock = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(newName, newLayoutBlock);
                }
            }
        }
    }

    /**
     * Set up a LayoutBlock for this Track Segment.
     *
     * @param name the name of the new LayoutBlock
     */
    public void setLayoutBlockByName(@CheckForNull String name) {
        if ((name != null) && !name.isEmpty()) {
            LayoutBlock b = models.provideLayoutBlock(name);
            if (b != null) {
                namedLayoutBlock = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(name, b);
            } else {
                namedLayoutBlock = null;
            }
        } else {
            namedLayoutBlock = null;
        }
    }

    // initialization instance variables (used when loading a LayoutEditor)
    public String tConnect1Name = "";
    public String tConnect2Name = "";

    public String tLayoutBlockName = "";

    /**
     * Initialization method. The above variables are initialized by
     * PositionablePointXml, then the following method is called after the
     * entire LayoutEditor is loaded to set the specific TrackSegment objects.
     */
    @SuppressWarnings("deprecation")
    // NOTE: findObjectByTypeAndName is @Deprecated;
    // we're using it here for backwards compatibility until it can be removed
    @Override
    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification = "Null check performed before using return value")
    public void setObjects(LayoutEditor p) {

        LayoutBlock lb;
        if (!tLayoutBlockName.isEmpty()) {
            lb = p.provideLayoutBlock(tLayoutBlockName);
            if (lb != null) {
                namedLayoutBlock = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(lb.getUserName(), lb);
                lb.incrementUse();
            } else {
                log.error("{}.setObjects(...); bad blockname '{}' in tracksegment {}",
                        getName(), tLayoutBlockName, getName());
                namedLayoutBlock = null;
            }
            tLayoutBlockName = null; //release this memory
        }

        connect1 = p.getFinder().findObjectByName(tConnect1Name);
        connect2 = p.getFinder().findObjectByName(tConnect2Name);
    }

    public void updateBlockInfo() {
        LayoutBlock layoutBlock = getLayoutBlock();
        if (layoutBlock != null) {
            layoutBlock.updatePaths();
        }
        LayoutBlock b1 = getBlock(connect1, type1);
        if ((b1 != null) && (b1 != layoutBlock)) {
            b1.updatePaths();
        }
        LayoutBlock b2 = getBlock(connect2, type2);
        if ((b2 != null) && (b2 != layoutBlock) && (b2 != b1)) {
            b2.updatePaths();
        }

        getConnect1().reCheckBlockBoundary();
        getConnect2().reCheckBlockBoundary();
    }

    private LayoutBlock getBlock(LayoutTrack connect, HitPointType type) {
        LayoutBlock result = null;
        if (connect != null) {
            if (type == HitPointType.POS_POINT) {
                PositionablePoint p = (PositionablePoint) connect;
                if (p.getConnect1() != this) {
                    if (p.getConnect1() != null) {
                        result = p.getConnect1().getLayoutBlock();
                    }
                } else {
                    if (p.getConnect2() != null) {
                        result = p.getConnect2().getLayoutBlock();
                    }
                }
            } else {
                result = models.getAffectedBlock(connect, type);
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canRemove() {
        List<String> itemList = new ArrayList<>();

        HitPointType type1 = getType1();
        LayoutTrack conn1 = getConnect1();
        itemList.addAll(getPointReferences(type1, conn1));

        HitPointType type2 = getType2();
        LayoutTrack conn2 = getConnect2();
        itemList.addAll(getPointReferences(type2, conn2));

        if (!itemList.isEmpty()) {
            models.displayRemoveWarning(this, itemList, "TrackSegment");  // NOI18N
        }
        return itemList.isEmpty();
    }

    public ArrayList<String> getPointReferences(HitPointType type, LayoutTrack conn) {
        ArrayList<String> result = new ArrayList<>();

        if (type == HitPointType.POS_POINT && conn instanceof PositionablePoint) {
            PositionablePoint pt = (PositionablePoint) conn;
            if (!pt.getEastBoundSignal().isEmpty()) {
                result.add(pt.getEastBoundSignal());
            }
            if (!pt.getWestBoundSignal().isEmpty()) {
                result.add(pt.getWestBoundSignal());
            }
            if (!pt.getEastBoundSignalMastName().isEmpty()) {
                result.add(pt.getEastBoundSignalMastName());
            }
            if (!pt.getWestBoundSignalMastName().isEmpty()) {
                result.add(pt.getWestBoundSignalMastName());
            }
            if (!pt.getEastBoundSensorName().isEmpty()) {
                result.add(pt.getEastBoundSensorName());
            }
            if (!pt.getWestBoundSensorName().isEmpty()) {
                result.add(pt.getWestBoundSensorName());
            }
            if (pt.getType() == PositionablePoint.PointType.EDGE_CONNECTOR && pt.getLinkedPoint() != null) {
                result.add(Bundle.getMessage("DeleteECisActive"));   // NOI18N
            }
        }

        if (HitPointType.isTurnoutHitType(type) && conn instanceof LayoutTurnout) {
            LayoutTurnout lt = (LayoutTurnout) conn;
            switch (type) {
                case TURNOUT_A: {
                    result = lt.getBeanReferences("A");  // NOI18N
                    break;
                }
                case TURNOUT_B: {
                    result = lt.getBeanReferences("B");  // NOI18N
                    break;
                }
                case TURNOUT_C: {
                    result = lt.getBeanReferences("C");  // NOI18N
                    break;
                }
                case TURNOUT_D: {
                    result = lt.getBeanReferences("D");  // NOI18N
                    break;
                }
                default: {
                    log.error("Unexpected HitPointType: {}", type);
                }
            }
        }

        if (HitPointType.isLevelXingHitType(type) && conn instanceof LevelXing) {
            LevelXing lx = (LevelXing) conn;
            switch (type) {
                case LEVEL_XING_A: {
                    result = lx.getBeanReferences("A");  // NOI18N
                    break;
                }
                case LEVEL_XING_B: {
                    result = lx.getBeanReferences("B");  // NOI18N
                    break;
                }
                case LEVEL_XING_C: {
                    result = lx.getBeanReferences("C");  // NOI18N
                    break;
                }
                case LEVEL_XING_D: {
                    result = lx.getBeanReferences("D");  // NOI18N
                    break;
                }
                default: {
                    log.error("Unexpected HitPointType: {}", type);
                }
            }
        }

        if (HitPointType.isSlipHitType(type) && conn instanceof LayoutSlip) {
            LayoutSlip ls = (LayoutSlip) conn;
            switch (type) {
                case SLIP_A: {
                    result = ls.getBeanReferences("A");  // NOI18N
                    break;
                }
                case SLIP_B: {
                    result = ls.getBeanReferences("B");  // NOI18N
                    break;
                }
                case SLIP_C: {
                    result = ls.getBeanReferences("C");  // NOI18N
                    break;
                }
                case SLIP_D: {
                    result = ls.getBeanReferences("D");  // NOI18N
                    break;
                }
                default: {
                    log.error("Unexpected HitPointType: {}", type);
                }
            }
        }

        return result;
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
     * Get state. "active" means that the object is still displayed, and should
     * be stored.
     *
     * @return true if still displayed, else false.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * temporary fill of abstract from above
     */
    @Override
    public void reCheckBlockBoundary() {
        log.info("reCheckBlockBoundary is temporary, but was invoked", new Exception("traceback"));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected List<LayoutConnectivity> getLayoutConnectivity() {
        List<LayoutConnectivity> results = new ArrayList<>();

        LayoutConnectivity lc = null;
        LayoutBlock lb1 = getLayoutBlock(), lb2 = null;
        // ensure that block is assigned
        if (lb1 != null) {
            // check first connection for turnout
            if (HitPointType.isTurnoutHitType(type1)) {
                // have connection to a turnout, is block different
                LayoutTurnout lt = (LayoutTurnout) getConnect1();
                lb2 = lt.getLayoutBlock();
                if (lt.hasEnteringDoubleTrack()) {
                    // not RH, LH, or WYE turnout - other blocks possible
                    if ((type1 == HitPointType.TURNOUT_B) && (lt.getLayoutBlockB() != null)) {
                        lb2 = lt.getLayoutBlockB();
                    }
                    if ((type1 == HitPointType.TURNOUT_C) && (lt.getLayoutBlockC() != null)) {
                        lb2 = lt.getLayoutBlockC();
                    }
                    if ((type1 == HitPointType.TURNOUT_D) && (lt.getLayoutBlockD() != null)) {
                        lb2 = lt.getLayoutBlockD();
                    }
                }
                if ((lb2 != null) && (lb1 != lb2)) {
                    // have a block boundary, create a LayoutConnectivity
                    log.debug("Block boundary  (''{}''<->''{}'') found at {}", lb1, lb2, this);
                    lc = new LayoutConnectivity(lb1, lb2);
                    lc.setConnections(this, lt, type1, null);
                    lc.setDirection(models.computeDirection(
                                        getConnect2(), type2,
                                        getConnect1(), type1 ) );
                    results.add(lc);
                }
            } else if (HitPointType.isLevelXingHitType(type1)) {
                // have connection to a level crossing
                LevelXing lx = (LevelXing) getConnect1();
                if ((type1 == HitPointType.LEVEL_XING_A) || (type1 == HitPointType.LEVEL_XING_C)) {
                    lb2 = lx.getLayoutBlockAC();
                } else {
                    lb2 = lx.getLayoutBlockBD();
                }
                if ((lb2 != null) && (lb1 != lb2)) {
                    // have a block boundary, create a LayoutConnectivity
                    log.debug("Block boundary  (''{}''<->''{}'') found at {}", lb1, lb2, this);
                    lc = new LayoutConnectivity(lb1, lb2);
                    lc.setConnections(this, lx, type1, null);
                    lc.setDirection(models.computeDirection(
                                        getConnect2(), type2,
                                        getConnect1(), type1 ) );
                    results.add(lc);
                }
            } else if (HitPointType.isSlipHitType(type1)) {
                // have connection to a slip crossing
                LayoutSlip ls = (LayoutSlip) getConnect1();
                lb2 = ls.getLayoutBlock();
                if ((lb2 != null) && (lb1 != lb2)) {
                    // have a block boundary, create a LayoutConnectivity
                    log.debug("Block boundary  (''{}''<->''{}'') found at {}", lb1, lb2, this);
                    lc = new LayoutConnectivity(lb1, lb2);
                    lc.setConnections(this, ls, type1, null);
                    lc.setDirection(models.computeDirection(
                                        getConnect2(), type2,
                                        getConnect1(), type1 ) );
                    results.add(lc);
                }
            }
            // check second connection for turnout
            if (HitPointType.isTurnoutHitType(type2)) {
                // have connection to a turnout
                LayoutTurnout lt = (LayoutTurnout) getConnect2();
                lb2 = lt.getLayoutBlock();
                if (lt.hasEnteringDoubleTrack()) {
                    // not RH, LH, or WYE turnout - other blocks possible
                    if ((type2 == HitPointType.TURNOUT_B) && (lt.getLayoutBlockB() != null)) {
                        lb2 = lt.getLayoutBlockB();
                    }
                    if ((type2 == HitPointType.TURNOUT_C) && (lt.getLayoutBlockC() != null)) {
                        lb2 = lt.getLayoutBlockC();
                    }
                    if ((type2 == HitPointType.TURNOUT_D) && (lt.getLayoutBlockD() != null)) {
                        lb2 = lt.getLayoutBlockD();
                    }
                }
                if ((lb2 != null) && (lb1 != lb2)) {
                    // have a block boundary, create a LayoutConnectivity
                    log.debug("Block boundary  (''{}''<->''{}'') found at {}", lb1, lb2, this);
                    lc = new LayoutConnectivity(lb1, lb2);
                    lc.setConnections(this, lt, type2, null);
                    lc.setDirection(models.computeDirection(
                                        getConnect1(), type1,
                                        getConnect2(), type2 ) );
                    results.add(lc);
                }
            } else if (HitPointType.isLevelXingHitType(type2)) {
                // have connection to a level crossing
                LevelXing lx = (LevelXing) getConnect2();
                if ((type2 == HitPointType.LEVEL_XING_A) || (type2 == HitPointType.LEVEL_XING_C)) {
                    lb2 = lx.getLayoutBlockAC();
                } else {
                    lb2 = lx.getLayoutBlockBD();
                }
                if ((lb2 != null) && (lb1 != lb2)) {
                    // have a block boundary, create a LayoutConnectivity
                    log.debug("Block boundary  (''{}''<->''{}'') found at {}", lb1, lb2, this);
                    lc = new LayoutConnectivity(lb1, lb2);
                    lc.setConnections(this, lx, type2, null);
                    lc.setDirection(models.computeDirection(
                                        getConnect1(), type1,
                                        getConnect2(), type2 ) );
                    results.add(lc);
                }
            } else if (HitPointType.isSlipHitType(type2)) {
                // have connection to a slip crossing
                LayoutSlip ls = (LayoutSlip) getConnect2();
                lb2 = ls.getLayoutBlock();
                if ((lb2 != null) && (lb1 != lb2)) {
                    // have a block boundary, create a LayoutConnectivity
                    log.debug("Block boundary  (''{}''<->''{}'') found at {}", lb1, lb2, this);
                    lc = new LayoutConnectivity(lb1, lb2);
                    lc.setConnections(this, ls, type2, null);
                    lc.setDirection(models.computeDirection(
                                        getConnect1(), type1,
                                        getConnect2(), type2 ) );
                    results.add(lc);
                }
            }
        }   // if (lb1 != null)
        return results;
    }   // getLayoutConnectivity()

    /**
     * {@inheritDoc}
     */
    @Override
    public List<HitPointType> checkForFreeConnections() {
        return new ArrayList<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean checkForUnAssignedBlocks() {
        return (getLayoutBlock() != null);
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
        * #3) else add a new set (with this block/track) to
        *     blockNamesToTrackNameSetMap and
        * #4) collect all the connections in this block
        * <p>
        *     Basically, we're maintaining contiguous track sets for each block found
        *     (in blockNamesToTrackNameSetMap)
         */
        List<Set<String>> TrackNameSets = null;
        Set<String> TrackNameSet = null;    // assume not found (pessimist!)
        String blockName = getBlockName();
        if (!blockName.isEmpty()) {
            TrackNameSets = blockNamesToTrackNameSetsMap.get(blockName);
            if (TrackNameSets != null) { //(#1)
                for (Set<String> checkTrackNameSet : TrackNameSets) {
                    if (checkTrackNameSet.contains(getName())) { //(#2)
                        TrackNameSet = checkTrackNameSet;
                        break;
                    }
                }
            } else {    //(#3)
                log.debug("*New block (''{}'') trackNameSets", blockName);
                TrackNameSets = new ArrayList<>();
                blockNamesToTrackNameSetsMap.put(blockName, TrackNameSets);
            }
            if (TrackNameSet == null) {
                TrackNameSet = new LinkedHashSet<>();
                TrackNameSets.add(TrackNameSet);
            }
            if (TrackNameSet.add(getName())) {
                log.debug("*    Add track ''{}'' to TrackNameSets for block ''{}''", getName(), blockName);
            }
            //(#4)
            if (connect1 != null) {
                connect1.collectContiguousTracksNamesInBlockNamed(blockName, TrackNameSet);
            }
            if (connect2 != null) { //(#4)
                connect2.collectContiguousTracksNamesInBlockNamed(blockName, TrackNameSet);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void collectContiguousTracksNamesInBlockNamed(@Nonnull String blockName,
            @Nonnull Set<String> TrackNameSet) {
        if (!TrackNameSet.contains(getName())) {
            // is this the blockName we're looking for?
            if (getBlockName().equals(blockName)) {
                // if we are added to the TrackNameSet
                if (TrackNameSet.add(getName())) {
                    log.debug("*    Add track ''{}''for block ''{}''", getName(), blockName);
                }
                // these should never be null... but just in case...
                // it's time to play... flood your neighbours!
                if (connect1 != null) {
                    connect1.collectContiguousTracksNamesInBlockNamed(blockName, TrackNameSet);
                }
                if (connect2 != null) {
                    connect2.collectContiguousTracksNamesInBlockNamed(blockName, TrackNameSet);
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

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrackSegment.class);
}
