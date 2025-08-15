package jmri.jmrit.display.layoutEditor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import jmri.*;
import jmri.util.MathUtil;
import java.util.HashMap;
import java.util.Set;

/**
 * A LayoutTurntable is a representation used by LayoutEditor to display a
 * turntable. This is the Model class.
 *
 * @author Dave Duchamp Copyright (c) 2007
 * @author George Warner Copyright (c) 2017-2018
 */
public class LayoutTurntable extends LayoutTrack {

    public LayoutTurntable(@Nonnull String id, @Nonnull LayoutEditor models) {
        super(id, models);
        radius = 25.0;
        String mastSystemName = "IV-LT:" + id;
        this.virtualSignalMast = new TurntableSignalMast(mastSystemName);
        jmri.InstanceManager.getDefault(jmri.SignalMastManager.class).register(this.virtualSignalMast);
        String mastUserName = "Turntable Mast " + id;
        this.virtualSignalMast.setUserName(mastUserName);
    }

    private NamedBeanHandle<LayoutBlock> namedLayoutBlock = null;
    private boolean turnoutControlled = false;
    private double radius = 25.0;
    private int lastKnownIndex = -1;
    private SignalMast virtualSignalMast = null;
    public final List<RayTrack> rayTrackList = new ArrayList<>();

    @Override
    @Nonnull
    public String toString() { return "LayoutTurntable " + getName(); }
    public double getRadius() { return radius; }
    public void setRadius(double r) { radius = r; }
    @Nonnull
    public String getBlockName() {
        String result = null;
        if (namedLayoutBlock != null) { result = namedLayoutBlock.getName(); }
        return ((result == null) ? "" : result);
    }
    @CheckForNull
    public LayoutBlock getLayoutBlock() { return (namedLayoutBlock != null) ? namedLayoutBlock.getBean() : null; }

    public void setLayoutBlock(@CheckForNull LayoutBlock newLayoutBlock) {
        LayoutBlock oldBlock = getLayoutBlock();
        if (oldBlock != newLayoutBlock) {
            if (oldBlock != null) { oldBlock.decrementUse(); }
            if (newLayoutBlock != null) {
                namedLayoutBlock = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class)
                                    .getNamedBeanHandle(newLayoutBlock.getUserName(), newLayoutBlock);
                newLayoutBlock.incrementUse();
            } else {
                namedLayoutBlock = null;
            }
            if (oldBlock != null) { oldBlock.updatePaths(); }
            if (newLayoutBlock != null) { newLayoutBlock.updatePaths(); }
            for (RayTrack ray : rayTrackList) {
                TrackSegment segment = ray.getConnect();
                if (segment != null) {
                    LayoutBlock rayBlock = segment.getLayoutBlock();
                    if (rayBlock != null && rayBlock != oldBlock && rayBlock != newLayoutBlock) {
                        rayBlock.updatePaths();
                    }
                }
            }
        }
    }

    public void setLayoutBlockByName(@CheckForNull String name) {
        if ((name != null) && !name.isEmpty()) {
            setLayoutBlock(models.provideLayoutBlock(name));
        }
    }

    public RayTrack addRay(double angle) {
        RayTrack rt = new RayTrack(angle, getNewIndex());
        rayTrackList.add(rt);
        return rt;
    }

    private int getNewIndex() {
        int index = -1;
        if (rayTrackList.isEmpty()) { return 0; }
        boolean found = true;
        while (found) {
            index++;
            found = false;
            for (RayTrack rt : rayTrackList) {
                if (index == rt.getConnectionIndex()) {
                    found = true;
                }
            }
        }
        return index;
    }

    public void addRayTrack(double angle, int index, String name) {
        RayTrack rt = new RayTrack(angle, index);
        rayTrackList.add(rt);
        rt.connectName = name;
    }

    @CheckForNull
    public TrackSegment getRayConnectIndexed(int index) {
        for (RayTrack rt : rayTrackList) {
            if (rt.getConnectionIndex() == index) {
                return rt.getConnect();
            }
        }
        return null;
    }

    @CheckForNull
    public TrackSegment getRayConnectOrdered(int i) {
        if (i < rayTrackList.size()) {
            RayTrack rt = rayTrackList.get(i);
            if (rt != null) { return rt.getConnect(); }
        }
        return null;
    }

    public void setRayConnect(@CheckForNull TrackSegment ts, int index) {
        for (RayTrack rt : rayTrackList) {
            if (rt.getConnectionIndex() == index) {
                rt.setConnect(ts);
                break;
            }
        }
    }

    @Nonnull
    public List<RayTrack> getRayTrackList() { return rayTrackList; }
    public int getNumberRays() { return rayTrackList.size(); }
    public int getRayIndex(int i) {
        if (i < rayTrackList.size()) { return rayTrackList.get(i).getConnectionIndex(); }
        return 0;
    }
    public double getRayAngle(int i) {
        if (i < rayTrackList.size()) { return rayTrackList.get(i).getAngle(); }
        return 0.0;
    }

    public boolean isRayDisabled(int index) {
        for (RayTrack rt : rayTrackList) {
            if (rt.getConnectionIndex() == index) {
                return rt.isRayDisabled();
            }
        }
        return false;
    }

    public void setRayDisabled(int index, boolean boo) {
        for (RayTrack rt : rayTrackList) {
            if (rt.getConnectionIndex() == index) {
                rt.setRayDisabled(boo);
                return;
            }
        }
    }

    public boolean isRayDisabledWhenOccupied(int index) {
        for (RayTrack rt : rayTrackList) {
            if (rt.getConnectionIndex() == index) {
                return rt.isRayDisabledWhenOccupied();
            }
        }
        return false;
    }

    public void setRayDisableWhenOccupied(int index, boolean boo) {
        for (RayTrack rt : rayTrackList) {
            if (rt.getConnectionIndex() == index) {
                rt.setRayDisabledWhenOccupied(boo);
                return;
            }
        }
    }

    public void setRayTurnout(int index, @CheckForNull String turnoutName, int state) {
        for (RayTrack rt : rayTrackList) {
            if (rt.getConnectionIndex() == index) {
                rt.setTurnout(turnoutName, state);
                return;
            }
        }
        log.error("{}.setRayTurnout({}, {}, {}); Attempt to add Turnout control to a non-existant ray track",
                getName(), index, turnoutName, state);
    }

    @CheckForNull
    public String getRayTurnoutName(int i) {
        if (i < rayTrackList.size()) { return rayTrackList.get(i).getTurnoutName(); }
        return null;
    }

    @CheckForNull
    public Turnout getRayTurnout(int i) {
        if (i < rayTrackList.size()) { return rayTrackList.get(i).getTurnout(); }
        return null;
    }

    public int getRayTurnoutState(int i) {
        if (i < rayTrackList.size()) { return rayTrackList.get(i).getTurnoutState(); }
        return 0;
    }

    @Override
    public LayoutTrack getConnection(HitPointType connectionType) throws jmri.JmriException {
        if (HitPointType.isTurntableRayHitType(connectionType)) {
            return getRayConnectIndexed(connectionType.turntableTrackIndex());
        }
        throw new jmri.JmriException("Invalid connection type for LayoutTurntable");
    }

    @Override
    public void setConnection(HitPointType connectionType, @CheckForNull LayoutTrack o, HitPointType type) throws jmri.JmriException {
        if (HitPointType.isTurntableRayHitType(connectionType)) {
            if (o == null || o instanceof TrackSegment) {
                setRayConnect((TrackSegment) o, connectionType.turntableTrackIndex());
            } else {
                throw new jmri.JmriException("Invalid object type for turntable connection");
            }
        } else {
            throw new jmri.JmriException("Invalid connection type for LayoutTurntable");
        }
    }

    @Override
    public boolean isMainline() { return false; }
    public String tLayoutBlockName = "";

    @Override
    public void setObjects(@Nonnull LayoutEditor p) {
        if (tLayoutBlockName != null && !tLayoutBlockName.isEmpty()) {
            setLayoutBlockByName(tLayoutBlockName);
        }
        tLayoutBlockName = null;
        rayTrackList.forEach((rt) -> rt.setConnect(p.getFinder().findTrackSegmentByName(rt.connectName)));
        reCheckBlockBoundary();
    }

    public boolean isTurnoutControlled() { return turnoutControlled; }
    public void setTurnoutControlled(boolean boo) { turnoutControlled = boo; }

    public void setPosition(int index) {
        if (isTurnoutControlled()) {
            for (RayTrack rt : rayTrackList) {
                if (rt.getConnectionIndex() == index) {
                    lastKnownIndex = index;
                    rt.setPosition();
                    models.redrawPanel();
                    models.setDirty();
                    return;
                }
            }
            log.error("{}.setPosition({}); Attempt to set the position on a non-existant ray track", getName(), index);
        }
    }

    public int getPosition() { return lastKnownIndex; }

    public void deleteRay(@Nonnull RayTrack rayTrack) {
        TrackSegment t = rayTrack.getConnect();
        rayTrackList.remove(rayTrack);
        rayTrack.dispose();
        if (t != null) { models.removeTrackSegment(t); }
        models.redrawPanel();
        models.setDirty();
    }

    public void remove() {
        active = false;
        if (virtualSignalMast != null) {
            jmri.InstanceManager.getDefault(jmri.SignalMastManager.class).deregister(virtualSignalMast);
            virtualSignalMast = null;
        }
    }

    private boolean active = true;
    public boolean isActive() { return active; }

    @CheckForNull
    public SignalMast getVirtualSignalMast() { return virtualSignalMast; }

    public class RayTrack {
        public RayTrack(double angle, int index) {
            rayAngle = MathUtil.wrapPM360(angle);
            connectionIndex = index;
        }
        private double rayAngle = 0.0;
        private TrackSegment connect = null;
        private int connectionIndex = -1;
        public String connectName = "";
        private NamedBeanHandle<Turnout> namedTurnout;
        private int turnoutState;
        private PropertyChangeListener mTurnoutListener;
        private boolean rayDisabled = false;
        private boolean rayDisabledWhenOccupied = false;

        public TrackSegment getConnect() { return connect; }
        public void setConnect(TrackSegment ts) { connect = ts; }
        public double getAngle() { return rayAngle; }
        public void setAngle(double an) { rayAngle = MathUtil.wrapPM360(an); }
        public int getConnectionIndex() { return connectionIndex; }
        public boolean isRayDisabled() { return rayDisabled; }
        public void setRayDisabled(boolean boo) { rayDisabled = boo; }
        public boolean isRayDisabledWhenOccupied() { return rayDisabledWhenOccupied; }
        public void setRayDisabledWhenOccupied(boolean boo) { rayDisabledWhenOccupied = boo; }
        
        public void setTurnout(@CheckForNull String turnoutName, int state) {
            Turnout turnout = null;
            if (mTurnoutListener == null) {
                mTurnoutListener = (PropertyChangeEvent e) -> {
                    if (getTurnout().getKnownState() == turnoutState) {
                        lastKnownIndex = connectionIndex;
                        models.redrawPanel();
                        models.setDirty();
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
            if (turnout == null) namedTurnout = null;
            if (this.turnoutState != state) this.turnoutState = state;
        }
        public void setPosition() {
            if (namedTurnout != null) {
                getTurnout().setCommandedState(turnoutState);
            }
        }
        public Turnout getTurnout() {
            if (namedTurnout == null) return null;
            return namedTurnout.getBean();
        }
        @CheckForNull
        public String getTurnoutName() {
            if (namedTurnout == null) return null;
            return namedTurnout.getName();
        }
        public int getTurnoutState() { return turnoutState; }
        void dispose() {
            if (getTurnout() != null) {
                getTurnout().removePropertyChangeListener(mTurnoutListener);
            }
            if (lastKnownIndex == connectionIndex) lastKnownIndex = -1;
        }
    }

    @Override
    protected void reCheckBlockBoundary() {
        LayoutBlock block = getLayoutBlock();
        if (block != null) {
            block.updatePaths();
            block.addAllThroughPaths();
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

    @Override
    @CheckForNull
    protected List<LayoutConnectivity> getLayoutConnectivity() {
        return java.util.Collections.emptyList();
    }

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

    @Override
    public boolean checkForUnAssignedBlocks() { return true; }

    @Override
    public void checkForNonContiguousBlocks(@Nonnull HashMap<String, List<Set<String>>> blockNamesToTrackNameSetsMap) {
        Map<LayoutTrack, String> blocksAndTracksMap = new HashMap<>();
        for (int k = 0; k < getNumberRays(); k++) {
            TrackSegment ts = getRayConnectOrdered(k);
            if (ts != null) {
                String blockName = ts.getBlockName();
                blocksAndTracksMap.put(ts, blockName);
            }
        }
        for (Map.Entry<LayoutTrack, String> entry : blocksAndTracksMap.entrySet()) {
            LayoutTrack theConnect = entry.getKey();
            String theBlockName = entry.getValue();
            Set<String> TrackNameSet = null;
            List<Set<String>> TrackNameSets = blockNamesToTrackNameSetsMap.get(theBlockName);
            if (TrackNameSets != null) {
                for (Set<String> checkTrackNameSet : TrackNameSets) {
                    if (checkTrackNameSet.contains(getName())) {
                        TrackNameSet = checkTrackNameSet;
                        break;
                    }
                }
            } else {
                TrackNameSets = new ArrayList<>();
                blockNamesToTrackNameSetsMap.put(theBlockName, TrackNameSets);
            }
            if (TrackNameSet == null) {
                TrackNameSet = new LinkedHashSet<>();
                TrackNameSets.add(TrackNameSet);
            }
            TrackNameSet.add(getName());
            theConnect.collectContiguousTracksNamesInBlockNamed(theBlockName, TrackNameSet);
        }
    }

    @Override
    public void collectContiguousTracksNamesInBlockNamed(@Nonnull String blockName, @Nonnull Set<String> TrackNameSet) {
        if (!TrackNameSet.contains(getName())) {
            for (int k = 0; k < getNumberRays(); k++) {
                TrackSegment ts = getRayConnectOrdered(k);
                if (ts != null) {
                    String blk = ts.getBlockName();
                    if ((!blk.isEmpty()) && (blk.equals(blockName))) {
                        TrackNameSet.add(getName());
                        ts.collectContiguousTracksNamesInBlockNamed(blockName, TrackNameSet);
                    }
                }
            }
        }
    }

    @Override
    public void setAllLayoutBlocks(LayoutBlock layoutBlock) {
        setLayoutBlock(layoutBlock);
    }

    @Override
    public boolean canRemove() { return true; }

    @Override
    public String getTypeName() { return Bundle.getMessage("TypeName_Turntable"); }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutTurntable.class);
}
