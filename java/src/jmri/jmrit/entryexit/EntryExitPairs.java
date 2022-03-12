package jmri.jmrit.entryexit;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.*;
import java.util.Map.Entry;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import jmri.*;
import jmri.beans.VetoableChangeSupport;
import jmri.jmrit.display.EditorManager;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.jmrit.display.layoutEditor.LayoutBlockConnectivityTools;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrix.internal.InternalSystemConnectionMemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements an Entry Exit based method of setting turnouts, setting up signal
 * logic and allocating blocks through a path based on the Layout Editor.
 * <p>
 * The route is based upon having a sensor assigned at a known location on the
 * panel (set at the boundary of two different blocks) through to a sensor at a
 * remote location on the same panel. Using the layout block routing, a path can
 * then be set between the two sensors so long as one exists and no
 * section of track is set occupied. If available an alternative route will be
 * used when the direct path is occupied (blocked).
 * <p>
 * Initial implementation only handles the setting up of turnouts on a path.
 *
 * @author Kevin Dickerson Copyright (C) 2011
 */
public class EntryExitPairs extends VetoableChangeSupport implements Manager<DestinationPoints>, jmri.InstanceManagerAutoDefault,
        PropertyChangeListener {

    public LayoutBlockConnectivityTools.Metric routingMethod = LayoutBlockConnectivityTools.Metric.METRIC;

    public final static int NXBUTTONSELECTED = 0x08;
    public final static int NXBUTTONACTIVE = Sensor.ACTIVE;
    public final static int NXBUTTONINACTIVE = Sensor.INACTIVE;
    private final SystemConnectionMemo memo;
    private final Map<String, Boolean> silencedProperties = new HashMap<>();

    private int settingTimer = 2000;

    public int getSettingTimer() {
        return settingTimer;
    }

    public void setSettingTimer(int i) {
        settingTimer = i;
    }

    private Color settingRouteColor = null;

    public boolean useDifferentColorWhenSetting() {
        return (settingRouteColor != null);
    }

    public Color getSettingRouteColor() {
        return settingRouteColor;
    }

    public void setSettingRouteColor(Color col) {
        settingRouteColor = col;
    }

    /**
     * Constant value to represent that the entryExit will only set up the
     * turnouts between two different points.
     */
    public final static int SETUPTURNOUTSONLY = 0x00;

    /**
     * Constant value to represent that the entryExit will set up the turnouts
     * between two different points and configure the Signal Mast Logic to use
     * the correct blocks.
     */
    public final static int SETUPSIGNALMASTLOGIC = 0x01;

    /**
     * Constant value to represent that the entryExit will do full interlocking.
     * It will set the turnouts and "reserve" the blocks.
     */
    public final static int FULLINTERLOCK = 0x02;

    boolean allocateToDispatcher = false;

    public final static int PROMPTUSER = 0x00;
    public final static int AUTOCLEAR = 0x01;
    public final static int AUTOCANCEL = 0x02;
    public final static int AUTOSTACK = 0x03;

    public final static int OVERLAP_CANCEL = 0x01;
    public final static int OVERLAP_STACK = 0x02;

    int routeClearOption = PROMPTUSER;
    int routeOverlapOption = PROMPTUSER;
    String memoryOption = "";     // Optional memory variable to receive allocation messages
    int memoryClearDelay = 0;     // Delay before clearing memory, 0 for clearing disabled

    static JPanel glassPane = new JPanel();

    /**
     * Delay between issuing Turnout commands
     */
    public int turnoutSetDelay = 0;

    /**
     * Constructor for creating an EntryExitPairs object and create a transparent JPanel for it.
     */
    public EntryExitPairs() {
        memo = InstanceManager.getDefault(InternalSystemConnectionMemo.class);
        InstanceManager.getOptionalDefault(ConfigureManager.class).ifPresent(cm -> cm.registerUser(this));
        InstanceManager.getDefault(LayoutBlockManager.class).addPropertyChangeListener(propertyBlockManagerListener);

        glassPane.setOpaque(false);
        glassPane.setLayout(null);
        glassPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                e.consume();
            }
        });
    }

    public void setDispatcherIntegration(boolean boo) {
        allocateToDispatcher = boo;
    }

    public boolean getDispatcherIntegration() {
        return allocateToDispatcher;
    }

    /**
     * Get the transparent JPanel for this EntryExitPairs.
     * @return JPanel overlay
     */
    public JPanel getGlassPane() {
        return glassPane;
    }

    HashMap<PointDetails, Source> nxpair = new HashMap<>();

    public void addNXSourcePoint(LayoutBlock facing, List<LayoutBlock> protecting, NamedBean loc, LayoutEditor panel) {
        PointDetails point = providePoint(facing, protecting, panel);
        point.setRefObject(loc);
    }

    public void addNXSourcePoint(NamedBean source) {
        PointDetails point = null;
        for (LayoutEditor editor : InstanceManager.getDefault(EditorManager.class).getAll(LayoutEditor.class)) {
            point = providePoint(source, editor);
        }
        if (point == null) {
            log.error("Unable to find a location on any panel for item {}", source.getDisplayName());  // NOI18N
        }
    }

    public void addNXSourcePoint(NamedBean source, LayoutEditor panel) {
        if (source == null) {
            log.error("source bean supplied is null");  // NOI18N
            return;
        }
        if (panel == null) {
            log.error("panel supplied is null");  // NOI18N
            return;
        }
        PointDetails point;
        point = providePoint(source, panel);
        if (point == null) {
            log.error("Unable to find a location on the panel {} for item {}", panel.getLayoutName(), source.getDisplayName());  // NOI18N
        }
    }

    public Object getEndPointLocation(NamedBean source, LayoutEditor panel) {
        if (source == null) {
            log.error("Source bean past is null");  // NOI18N
            return null;
        }
        if (panel == null) {
            log.error("panel passed is null");  // NOI18N
            return null;
        }
        PointDetails sourcePoint = getPointDetails(source, panel);
        if (sourcePoint == null) {
            log.error("Point is not located");  // NOI18N
            return null;
        }
        return sourcePoint.getRefLocation();
    }

    /** {@inheritDoc} */
    @Override
    public int getXMLOrder() {
        return ENTRYEXIT;
    }

    /** {@inheritDoc} */
    @Override
    public DestinationPoints getBySystemName(String systemName) {
        for (Source e : nxpair.values()) {
            DestinationPoints pd = e.getByUniqueId(systemName);
            if (pd != null) {
                return pd;
            }
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public DestinationPoints getByUserName(@Nonnull String userName) {
        for (Source e : nxpair.values()) {
            DestinationPoints pd = e.getByUserName(userName);
            if (pd != null) {
                return pd;
            }
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public DestinationPoints getNamedBean(@Nonnull String name) {
        DestinationPoints b = getByUserName(name);
        if (b != null) {
            return b;
        }
        return getBySystemName(name);
    }

    /** {@inheritDoc} */
    @Nonnull
    @Override
    public SystemConnectionMemo getMemo() {
        return memo;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public String getSystemPrefix() {
        return memo.getSystemPrefix();
    }

    /** {@inheritDoc} */
    @Override
    public char typeLetter() {
        throw new UnsupportedOperationException("Not supported yet.");  // NOI18N
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public String makeSystemName(@Nonnull String s) {
        throw new UnsupportedOperationException("Not supported yet.");  // NOI18N
    }

    /** {@inheritDoc} */
    @Override
    @CheckReturnValue
    public int getObjectCount() {
        return getNamedBeanSet().size();
    }

    /**
     * Implemented to support the Conditional combo box name list
     * @since 4.9.3
     * @return a list of Destination Point beans
     */
    @Override
    @Nonnull
    public SortedSet<DestinationPoints> getNamedBeanSet() {
        TreeSet<DestinationPoints> beanList = new TreeSet<>(new jmri.util.NamedBeanComparator<>());
        for (Source e : nxpair.values()) {
            List<String> uidList = e.getDestinationUniqueId();
            for (String uid : uidList) {
                beanList.add(e.getByUniqueId(uid));
            }
        }
        return beanList;
    }

    /** {@inheritDoc} */
    @Override
    public void register(@Nonnull DestinationPoints n) {
        throw new UnsupportedOperationException("Not supported yet.");  // NOI18N
    }

    /** {@inheritDoc} */
    @Override
    public void deregister(@Nonnull DestinationPoints n) {
        throw new UnsupportedOperationException("Not supported yet.");  // NOI18N
    }

    public void setClearDownOption(int i) {
        routeClearOption = i;
    }

    public int getClearDownOption() {
        return routeClearOption;
    }

    public void setOverlapOption(int i) {
        routeOverlapOption = i;
    }

    public int getOverlapOption() {
        return routeOverlapOption;
    }

    public void setMemoryOption(String memoryName) {
        memoryOption = memoryName;
    }

    public String getMemoryOption() {
        return memoryOption;
    }

    public void setMemoryClearDelay(int secs) {
        memoryClearDelay = secs;
    }

    public int getMemoryClearDelay() {
        return memoryClearDelay;
    }

    /** {@inheritDoc} */
    @Override
    public void dispose() {
    }

    /**
     * Generate the point details, given a known source and a
     * Layout Editor panel.
     *
     * @param source Origin of movement
     * @param panel  A Layout Editor panel
     * @return A PointDetails object
     */
    public PointDetails providePoint(NamedBean source, LayoutEditor panel) {
        PointDetails sourcePoint = getPointDetails(source, panel);
        if (sourcePoint == null) {
            LayoutBlock facing = InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).getFacingBlockByNamedBean(source, null);
            List<LayoutBlock> protecting = InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).getProtectingBlocksByNamedBean(source, null);
//             log.info("facing = {}, protecting = {}", facing, protecting);
            if (facing == null && protecting.size() == 0) {
                log.error("Unable to find facing and protecting blocks");  // NOI18N
                return null;
            }
            sourcePoint = providePoint(facing, protecting, panel);
            sourcePoint.setRefObject(source);
        }
        return sourcePoint;
    }

    /**
     * Return a list of all source (origin) points on a given
     * Layout Editor panel.
     *
     * @param panel  A Layout Editor panel
     * @return A list of source objects
     */
    public List<Object> getSourceList(LayoutEditor panel) {
        List<Object> list = new ArrayList<>();

        for (Entry<PointDetails, Source> e : nxpair.entrySet()) {
            Object obj = (e.getKey()).getRefObject();
            LayoutEditor pan = (e.getKey()).getPanel();
            if (pan == panel) {
                if (!list.contains(obj)) {
                    list.add(obj);
                }
            } // end while
        }
        return list;
    }

    public Source getSourceForPoint(PointDetails pd) {
        return nxpair.get(pd);
    }

    public int getNxPairNumbers(LayoutEditor panel) {
        int total = 0;
        for (Entry<PointDetails, Source> e : nxpair.entrySet()) {
            PointDetails key = e.getKey();
            LayoutEditor pan = key.getPanel();
            if (pan == panel) {
                total = total + e.getValue().getNumberOfDestinations();
            } // end while
        }

        return total;
    }

    /**
     * Set the route between the two points represented by the Destination Point name.
     *
     * @since 4.11.1
     * @param nxPair The system or user name of the destination point.
     */
    public void setSingleSegmentRoute(String nxPair) {
        DestinationPoints dp = getNamedBean(nxPair);
        if (dp != null) {
            String destUUID = dp.getUniqueId();
            nxpair.forEach((pd, src) -> {
                for (String srcUUID : src.getDestinationUniqueId()) {
                    if (destUUID.equals(srcUUID)) {
                        log.debug("Found the correct source: src = {}, dest = {}",
                                pd.getSensor().getDisplayName(), dp.getDestPoint().getSensor().getDisplayName());
                        setMultiPointRoute(pd, dp.getDestPoint());
                        return;
                    }
                }
            });
        }
    }

    public void setMultiPointRoute(PointDetails requestpd, LayoutEditor panel) {
        for (PointDetails pd : pointDetails) {
            if (pd != requestpd) {
                if (pd.getNXState() == NXBUTTONSELECTED) {
                    setMultiPointRoute(pd, requestpd);
                    return;
                }
            }
        }
    }

    private void setMultiPointRoute(PointDetails fromPd, PointDetails toPd) {
        boolean cleardown = false;
        if (fromPd.isRouteFromPointSet() && toPd.isRouteToPointSet()) {
            cleardown = true;
        }
        for (LayoutBlock pro : fromPd.getProtecting()) {
            try {
                jmri.jmrit.display.layoutEditor.LayoutBlockManager lbm = InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class);
                LayoutBlock toProt = null;
                if (!toPd.getProtecting().isEmpty()) {
                    toProt = toPd.getProtecting().get(0);
                }
                boolean result = lbm.getLayoutBlockConnectivityTools().checkValidDest(fromPd.getFacing(), pro, toPd.getFacing(), toProt, LayoutBlockConnectivityTools.Routing.SENSORTOSENSOR);
                if (result) {
                    List<LayoutBlock> blkList = lbm.getLayoutBlockConnectivityTools().getLayoutBlocks(fromPd.getFacing(), toPd.getFacing(), pro, cleardown, LayoutBlockConnectivityTools.Routing.NONE);
                    if (!blkList.isEmpty()) {
                        if (log.isDebugEnabled()) {
                            for (LayoutBlock blk : blkList) {
                                log.debug("blk = {}", blk.getDisplayName());
                            }
                        }
                        List<jmri.NamedBean> beanList = lbm.getLayoutBlockConnectivityTools().getBeansInPath(blkList, null, jmri.Sensor.class);
                        PointDetails fromPoint = fromPd;
                        refCounter++;
                        if (!beanList.isEmpty()) {
                            if (log.isDebugEnabled()) {
                                for (NamedBean xnb : beanList) {
                                    log.debug("xnb = {}", xnb.getDisplayName());
                                }
                            }
                            for (int i = 1; i < beanList.size(); i++) {
                                NamedBean nb = beanList.get(i);
                                PointDetails cur = getPointDetails(nb, fromPd.getPanel());
                                Source s = nxpair.get(fromPoint);
                                if (s != null) {
                                    routesToSet.add(new SourceToDest(s, s.getDestForPoint(cur), false, refCounter));
                                }
                                fromPoint = cur;
                            }
                        }
                        Source s = nxpair.get(fromPoint);
                        if (s != null) {
                            if (s.getDestForPoint(toPd) != null) {
                                routesToSet.add(new SourceToDest(s, s.getDestForPoint(toPd), false, refCounter));
                            }
                        }
                        processRoutesToSet();
                        return;
                    }
                }
            } catch (jmri.JmriException e) {
                //Can be considered normal if route is blocked
            }
        }
        fromPd.setNXButtonState(NXBUTTONINACTIVE);
        toPd.setNXButtonState(NXBUTTONINACTIVE);
    }

    int refCounter = 0;

    /**
     * List holding SourceToDest sets of routes between two points.
     */
    List<SourceToDest> routesToSet = new ArrayList<>();

    /**
     * Class to store NX sets consisting of a source point, a destination point,
     * a direction and a reference.
     */
    static class SourceToDest {

        Source s = null;
        DestinationPoints dp = null;
        boolean direction = false;
        int ref = -1;

        /**
         * Constructor for a SourceToDest element.
         *
         * @param s   a source point
         * @param dp  a destination point
         * @param dir a direction
         * @param ref Integer used as reference
         */
        SourceToDest(Source s, DestinationPoints dp, boolean dir, int ref) {
            this.s = s;
            this.dp = dp;
            this.direction = dir;
            this.ref = ref;
        }
    }

    int currentDealing = 0;

    /**
     * Activate each SourceToDest set in routesToSet
     */
    synchronized void processRoutesToSet() {
        if (log.isDebugEnabled()) {
            for (SourceToDest sd : routesToSet) {
                String dpName = (sd.dp == null) ? "- null -" : sd.dp.getDestPoint().getSensor().getDisplayName();
                log.debug("processRoutesToSet: {} -- {} -- {}", sd.s.getPoint().getSensor().getDisplayName(), dpName, sd.ref);
            }
        }

        if (routesToSet.isEmpty()) {
            return;
        }
        Source s = routesToSet.get(0).s;
        DestinationPoints dp = routesToSet.get(0).dp;
        boolean dir = routesToSet.get(0).direction;
        currentDealing = routesToSet.get(0).ref;
        routesToSet.remove(0);

        dp.addPropertyChangeListener(propertyDestinationListener);
        s.activeBean(dp, dir);
    }

    /**
     * Remove remaining SourceToDest sets in routesToSet
     */
    synchronized void removeRemainingRoute() {
        List<SourceToDest> toRemove = new ArrayList<>();
        for (SourceToDest rts : routesToSet) {
            if (rts.ref == currentDealing) {
                toRemove.add(rts);
                rts.dp.getDestPoint().setNXButtonState(NXBUTTONINACTIVE);
            }
        }
        for (SourceToDest rts : toRemove) {
            routesToSet.remove(rts);
        }
    }

    protected PropertyChangeListener propertyDestinationListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent e) {
            ((DestinationPoints) e.getSource()).removePropertyChangeListener(this);
            if (e.getPropertyName().equals("active")) {
                processRoutesToSet();
            } else if (e.getPropertyName().equals("stacked") || e.getPropertyName().equals("failed") || e.getPropertyName().equals("noChange")) {  // NOI18N
                removeRemainingRoute();
            }
        }
    };

    List<Object> destinationList = new ArrayList<>();

    // Need to sort out the presentation of the name here rather than using the point ID.
    // This is used for the creation and display of information in the table.
    // The presentation of the name might have to be done at the table level.
    public List<Object> getNxSource(LayoutEditor panel) {
        List<Object> source = new ArrayList<>();
        destinationList = new ArrayList<>();

        for (Entry<PointDetails, Source> e : nxpair.entrySet()) {
            PointDetails key = e.getKey();
            LayoutEditor pan = key.getPanel();
            if (pan == panel) {
                List<PointDetails> dest = nxpair.get(key).getDestinationPoints();
                for (int i = 0; i < dest.size(); i++) {
                    destinationList.add(dest.get(i).getRefObject());
                    source.add(key.getRefObject());
                }
            }
        }
        return source;
    }

    public List<Object> getNxDestination() {
        return destinationList;
    }

    public List<LayoutEditor> getSourcePanelList() {
        List<LayoutEditor> list = new ArrayList<>();

        for (Entry<PointDetails, Source> e : nxpair.entrySet()) {
            PointDetails key = e.getKey();
            LayoutEditor pan = key.getPanel();
            if (!list.contains(pan)) {
                list.add(pan);
            }
        }
        return list;
    }

    /**
     * Return a point if it already exists, or create a new one if not.
     */
    private PointDetails providePoint(LayoutBlock source, List<LayoutBlock> protecting, LayoutEditor panel) {
        PointDetails sourcePoint = getPointDetails(source, protecting, panel);
        if (sourcePoint == null) {
            sourcePoint = new PointDetails(source, protecting);
            sourcePoint.setPanel(panel);
        }
        return sourcePoint;
    }

    /**
     * @since 4.17.4
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        firePropertyChange("active", evt.getOldValue(), evt.getNewValue());
    }


    public void addNXDestination(NamedBean source, NamedBean destination, LayoutEditor panel) {
        addNXDestination(source, destination, panel, null);
    }

    /**
     * @since 4.17.4
     * Register in Property Change Listener.
     * @param source the source bean.
     * @param destination the destination bean.
     * @param panel the layout editor panel.
     * @param id the points details id.
     */
    public void addNXDestination(NamedBean source, NamedBean destination, LayoutEditor panel, String id) {
        if (source == null) {
            log.error("no source Object provided");  // NOI18N
            return;
        }
        if (destination == null) {
            log.error("no destination Object provided");  // NOI18N
            return;
        }
        PointDetails sourcePoint = providePoint(source, panel);
        if (sourcePoint == null) {
            log.error("source point for {} not created addNXDes", source.getDisplayName());  // NOI18N
            return;
        }

        sourcePoint.setPanel(panel);
        sourcePoint.setRefObject(source);
        PointDetails destPoint = providePoint(destination, panel);
        if (destPoint != null) {
            destPoint.setPanel(panel);
            destPoint.setRefObject(destination);
            destPoint.getSignal();
            if (!nxpair.containsKey(sourcePoint)) {
                Source sp = new Source(sourcePoint);
                nxpair.put(sourcePoint, sp);
                sp.removePropertyChangeListener(this);
                sp.addPropertyChangeListener(this);
            }
            nxpair.get(sourcePoint).addDestination(destPoint, id);
        }

        firePropertyChange("length", null, null);  // NOI18N
    }

    public List<Object> getDestinationList(Object obj, LayoutEditor panel) {
        List<Object> list = new ArrayList<>();
        if (nxpair.containsKey(getPointDetails(obj, panel))) {
            List<PointDetails> from = nxpair.get(getPointDetails(obj, panel)).getDestinationPoints();
            for (int i = 0; i < from.size(); i++) {
                list.add(from.get(i).getRefObject());
            }
        }
        return list;
    }

    public void removeNXSensor(Sensor sensor) {
        log.info("panel maintenance has resulting in the request to remove a sensor: {}", sensor.getDisplayName());
    }

    // ============ NX Pair Delete Methods ============
    // The request will be for all NX Pairs containing a sensor or
    // a specific entry and exit sensor pair.

    /**
     * Entry point to delete all of the NX pairs for a specific sensor.
     * 1) Build a list of affected NX pairs.
     * 2) Check for Conditional references.
     * 3) If no references, do the delete process with user approval.
     * <p>
     * @since 4.11.2
     * @param sensor The sensor whose pairs should be deleted.
     * @return true if the delete was successful. False if prevented by
     * Conditional references or user choice.
     */
    public boolean deleteNxPair(NamedBean sensor) {
        if (sensor == null) {
            log.error("deleteNxPair: sensor is null");  // NOI18N
            return false;
        }
        createDeletePairList(sensor);
        if (checkNxPairs()) {
            // No Conditional references.
            if (confirmDeletePairs()) {
                deleteNxPairs();
                return true;
            }
        }
        return false;
    }

    /**
     * Entry point to delete a specific NX pair.
     *
     * @since 4.11.2
     * @param entrySensor The sensor that acts as the entry point.
     * @param exitSensor The sensor that acts as the exit point.
     * @param panel The layout editor panel that contains the entry sensor.
     * @return true if the delete was successful. False if there are Conditional references.
     */
    public boolean deleteNxPair(NamedBean entrySensor, NamedBean exitSensor, LayoutEditor panel) {
        if (entrySensor == null || exitSensor == null || panel == null) {
            log.error("deleteNxPair: One or more null inputs");  // NOI18N
            return false;
        }
        deletePairList.clear();
        deletePairList.add(new DeletePair(entrySensor, exitSensor, panel));
        if (checkNxPairs()) {
            // No Conditional references.
            deleteNxPairs();  // Delete with no prompt
            return true;
        }
        return false;
    }

    /**
     * Find Logix Conditionals that have Variables or Actions for the affected NX Pairs
     * If any are found, display a dialog box listing the Conditionals and return false.
     * <p>
     * @since 4.11.2
     * @return true if there are no references.
     */
    private boolean checkNxPairs() {
        jmri.LogixManager mgr = InstanceManager.getDefault(jmri.LogixManager.class);
        List<String> conditionalReferences = new ArrayList<>();
        for (DeletePair dPair : deletePairList) {
            if (dPair.dp == null) {
                continue;
            }
            for (jmri.Logix lgx : mgr.getNamedBeanSet()) {
                for (int i = 0; i < lgx.getNumConditionals(); i++) {
                    String cdlName = lgx.getConditionalByNumberOrder(i);
                    jmri.implementation.DefaultConditional cdl = (jmri.implementation.DefaultConditional) lgx.getConditional(cdlName);
                    String cdlUserName = cdl.getUserName();
                    if (cdlUserName == null) {
                        cdlUserName = "";
                    }
                    for (jmri.ConditionalVariable var : cdl.getStateVariableList()) {
                        if (var.getBean() == dPair.dp) {
                            String refName = (cdlUserName.equals("")) ? cdlName : cdlName + "  ( " + cdlUserName + " )";
                            if (!conditionalReferences.contains(refName)) {
                                conditionalReferences.add(refName);
                            }
                        }
                    }
                    for (jmri.ConditionalAction act : cdl.getActionList()) {
                        if (act.getBean() == dPair.dp) {
                            String refName = (cdlUserName.equals("")) ? cdlName : cdlName + "  ( " + cdlUserName + " )";
                            if (!conditionalReferences.contains(refName)) {
                                conditionalReferences.add(refName);
                            }
                        }
                    }
                }
            }
        }
        if (conditionalReferences.isEmpty()) {
            return true;
        }

        conditionalReferences.sort(null);
        StringBuilder msg = new StringBuilder(Bundle.getMessage("DeleteReferences"));
        for (String ref : conditionalReferences) {
            msg.append("\n    " + ref);  // NOI18N
        }
        JOptionPane.showMessageDialog(null,
                msg.toString(),
                Bundle.getMessage("WarningTitle"),  // NOI18N
                JOptionPane.WARNING_MESSAGE);

        return false;
    }

    /**
     * Display a list of pending deletes and ask for confirmation.
     * @since 4.11.2
     * @return true if deletion confirmation is Yes.
     */
    private boolean confirmDeletePairs() {
        if (deletePairList.size() > 0) {
            StringBuilder msg = new StringBuilder(Bundle.getMessage("DeletePairs"));  // NOI18N
            for (DeletePair dPair : deletePairList) {
                if (dPair.dp != null) {
                    msg.append("\n    " + dPair.dp.getDisplayName());  // NOI18N
                }
            }
            msg.append("\n" + Bundle.getMessage("DeleteContinue"));  // NOI18N
            int resp = JOptionPane.showConfirmDialog(null,
                    msg.toString(),
                    Bundle.getMessage("WarningTitle"),  // NOI18N
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (resp != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Delete the pairs in the delete pair list.
     * @since 4.11.2
     * @since 4.17.4
     * Remove from Change Listener.
     */
    private void deleteNxPairs() {
        for (DeletePair dp : deletePairList) {
            PointDetails sourcePoint = getPointDetails(dp.src, dp.pnl);
            PointDetails destPoint = getPointDetails(dp.dest, dp.pnl);
            nxpair.get(sourcePoint).removeDestination(destPoint);
            firePropertyChange("length", null, null);  // NOI18N
            if (nxpair.get(sourcePoint).getDestinationPoints().isEmpty()) {
                nxpair.get(sourcePoint).removePropertyChangeListener(this);
                nxpair.remove(sourcePoint);
            }
        }
    }

    /**
     * List of NX pairs that are scheduled for deletion.
     * @since 4.11.2
     */
    List<DeletePair> deletePairList = new ArrayList<>();

    /**
     * Class to store NX pair components.
     * @since 4.11.2
     */
    class DeletePair {
        NamedBean src = null;
        NamedBean dest = null;
        LayoutEditor pnl = null;
        DestinationPoints dp = null;

        /**
         * Constructor for a DeletePair row.
         *
         * @param src  Source sensor bean
         * @param dest Ddestination sensor bean
         * @param pnl  The LayoutEditor panel for the source bean
         */
        DeletePair(NamedBean src, NamedBean dest, LayoutEditor pnl) {
            this.src = src;
            this.dest = dest;
            this.pnl = pnl;

            // Get the actual destination point, if any.
            PointDetails sourcePoint = getPointDetails(src, pnl);
            PointDetails destPoint = getPointDetails(dest, pnl);
            if (sourcePoint != null && destPoint != null) {
                if (nxpair.containsKey(sourcePoint)) {
                    this.dp = nxpair.get(sourcePoint).getDestForPoint(destPoint);
                }
            }
        }
    }

    /**
     * Rebuild the delete pair list based on the supplied sensor.
     * Find all of the NX pairs that use this sensor as either a source or
     * destination.  They will be candidates for deletion.
     * <p>
     * @since 4.11.2
     * @param sensor The sensor being deleted,
     */
    void createDeletePairList(NamedBean sensor) {
        deletePairList.clear();
        nxpair.forEach((pdSrc, src) -> {
            Sensor sBean = pdSrc.getSensor();
            LayoutEditor sPanel = pdSrc.getPanel();
            for (PointDetails pdDest : src.getDestinationPoints()) {
                Sensor dBean = pdDest.getSensor();
                if (sensor == sBean || sensor == dBean) {
                    log.debug("Delete pair: {} to {}, panel = {}",  // NOI18N
                            sBean.getDisplayName(), dBean.getDisplayName(), sPanel.getLayoutName());
                    deletePairList.add(new DeletePair(sBean, dBean, sPanel));
                }
            }
        });
    }

    // ============ End NX Pair Delete Methods ============

    /**
     * Create a list of sensors that have the layout block as either
     * facing or protecting.
     * Called by {@link jmri.jmrit.display.layoutEditor.LayoutEditorDialogs.LayoutTrackEditor#hasNxSensorPairs}.
     * @since 4.11.2
     * @param layoutBlock The layout block to be checked.
     * @return the a list of sensors affected by the layout block or an empty list.
     */
    public List<String> layoutBlockSensors(@Nonnull LayoutBlock layoutBlock) {
        log.debug("layoutBlockSensors: {}", layoutBlock.getDisplayName());
        List<String> blockSensors = new ArrayList<>();
        nxpair.forEach((pdSrc, src) -> {
            Sensor sBean = pdSrc.getSensor();
            for (LayoutBlock sProtect : pdSrc.getProtecting()) {
                if (layoutBlock == pdSrc.getFacing() || layoutBlock == sProtect) {
                    log.debug("  Source = '{}', Facing = '{}', Protecting = '{}'         ",
                            sBean.getDisplayName(), pdSrc.getFacing().getDisplayName(), sProtect.getDisplayName());
                    blockSensors.add(sBean.getDisplayName());
                }
            }

            for (PointDetails pdDest : src.getDestinationPoints()) {
                Sensor dBean = pdDest.getSensor();
                for (LayoutBlock dProtect : pdDest.getProtecting()) {
                    if (layoutBlock == pdDest.getFacing() || layoutBlock == dProtect) {
                        log.debug("    Destination = '{}', Facing = '{}', Protecting = '{}'     ",
                                dBean.getDisplayName(), pdDest.getFacing().getDisplayName(), dProtect.getDisplayName());
                        blockSensors.add(dBean.getDisplayName());
                    }
                }
            }
        });
        return blockSensors;
    }

    public boolean isDestinationValid(Object source, Object dest, LayoutEditor panel) {
        if (nxpair.containsKey(getPointDetails(source, panel))) {
            return nxpair.get(getPointDetails(source, panel)).isDestinationValid(getPointDetails(dest, panel));
        }
        return false;
    }

    public boolean isUniDirection(Object source, LayoutEditor panel, Object dest) {
        if (nxpair.containsKey(getPointDetails(source, panel))) {
            return nxpair.get(getPointDetails(source, panel)).getUniDirection(dest, panel);
        }
        return false;
    }

    public void setUniDirection(Object source, LayoutEditor panel, Object dest, boolean set) {
        if (nxpair.containsKey(getPointDetails(source, panel))) {
            nxpair.get(getPointDetails(source, panel)).setUniDirection(dest, panel, set);
        }
    }

    public boolean canBeBiDirectional(Object source, LayoutEditor panel, Object dest) {
        if (nxpair.containsKey(getPointDetails(source, panel))) {
            return nxpair.get(getPointDetails(source, panel)).canBeBiDirection(dest, panel);
        }
        return false;
    }

    public boolean isEnabled(Object source, LayoutEditor panel, Object dest) {
        if (nxpair.containsKey(getPointDetails(source, panel))) {
            return nxpair.get(getPointDetails(source, panel)).isEnabled(dest, panel);
        }
        return false;
    }

    public void setEnabled(Object source, LayoutEditor panel, Object dest, boolean set) {
        if (nxpair.containsKey(getPointDetails(source, panel))) {
            nxpair.get(getPointDetails(source, panel)).setEnabled(dest, panel, set);
        }
    }

    public void setEntryExitType(Object source, LayoutEditor panel, Object dest, int set) {
        if (nxpair.containsKey(getPointDetails(source, panel))) {
            nxpair.get(getPointDetails(source, panel)).setEntryExitType(dest, panel, set);
        }
    }

    public int getEntryExitType(Object source, LayoutEditor panel, Object dest) {
        if (nxpair.containsKey(getPointDetails(source, panel))) {
            return nxpair.get(getPointDetails(source, panel)).getEntryExitType(dest, panel);
        }
        return 0x00;
    }

    public String getUniqueId(Object source, LayoutEditor panel, Object dest) {
        if (nxpair.containsKey(getPointDetails(source, panel))) {
            return nxpair.get(getPointDetails(source, panel)).getUniqueId(dest, panel);
        }
        return null;
    }

    public List<String> getEntryExitList() {
        List<String> destlist = new ArrayList<>();
        for (Source e : nxpair.values()) {
            destlist.addAll(e.getDestinationUniqueId());
        }
        return destlist;
    }

    // protecting helps us to determine which direction we are going.
    // validateOnly flag is used, if all we are doing is simply checking to see if the source/destpoints are valid
    // when creating the pairs in the user GUI
    public boolean isPathActive(Object sourceObj, Object destObj, LayoutEditor panel) {
        PointDetails pd = getPointDetails(sourceObj, panel);
        if (nxpair.containsKey(pd)) {
            Source source = nxpair.get(pd);
            return source.isRouteActive(getPointDetails(destObj, panel));
        }
        return false;
    }

    public void cancelInterlock(Object source, LayoutEditor panel, Object dest) {
        if (nxpair.containsKey(getPointDetails(source, panel))) {
            nxpair.get(getPointDetails(source, panel)).cancelInterlock(dest, panel);
        }

    }

    jmri.SignalMastLogicManager smlm = InstanceManager.getDefault(jmri.SignalMastLogicManager.class);

    public final static int CANCELROUTE = 0;
    public final static int CLEARROUTE = 1;
    public final static int EXITROUTE = 2;
    public final static int STACKROUTE = 4;

   /**
     * Return a point from a given LE Panel.
     *
     * @param obj The point object
     * @param panel The Layout Editor panel on which the point was placed
     * @return the point object, null if the point is not found
     */
    public PointDetails getPointDetails(Object obj, LayoutEditor panel) {
        for (int i = 0; i < pointDetails.size(); i++) {
            if ((pointDetails.get(i).getRefObject() == obj)) {
                return pointDetails.get(i);

            }
        }
        return null;
    }

    /**
     * Return either an existing point stored in pointDetails, or create a new one as required.
     *
     * @param source The Layout Block functioning as the source (origin)
     * @param destination A (list of) Layout Blocks functioning as destinations
     * @param panel The Layout Editor panel on which the point is to be placed
     * @return the point object
     */
    PointDetails getPointDetails(LayoutBlock source, List<LayoutBlock> destination, LayoutEditor panel) {
        PointDetails newPoint = new PointDetails(source, destination);
        newPoint.setPanel(panel);
        for (int i = 0; i < pointDetails.size(); i++) {
            if (pointDetails.get(i).equals(newPoint)) {
                return pointDetails.get(i);
            }
        }
        //Not found so will add
        pointDetails.add(newPoint);
        return newPoint;
    }

    //No point can have multiple copies of what is the same thing.
    static List<PointDetails> pointDetails = new ArrayList<PointDetails>();

    /**
     * Get the name of a destinationPoint on a LE Panel.
     *
     * @param obj the point object
     * @param panel The Layout Editor panel on which it is expected to be placed
     * @return the name of the point
     */
    public String getPointAsString(NamedBean obj, LayoutEditor panel) {
        if (obj == null) {
            return "null";  // NOI18N
        }
        PointDetails valid = getPointDetails(obj, panel);  //was just plain getPoint
        if (valid != null) {
            return valid.getDisplayName();
        }
        return "empty";  // NOI18N
    }

    List<StackDetails> stackList = new ArrayList<>();

    /**
     * If a route is requested but is currently blocked, ask user
     * if it should be added to stackList.
     *
     * @param dp DestinationPoints object
     * @param reverse true for a reversed running direction, mostly false
     */
    synchronized public void stackNXRoute(DestinationPoints dp, boolean reverse) {
        if (isRouteStacked(dp, reverse)) {
            return;
        }
        stackList.add(new StackDetails(dp, reverse));
        checkTimer.start();
        if (stackPanel == null) {
            stackPanel = new StackNXPanel();
        }
        if (stackDialog == null) {
            stackDialog = new JDialog();
            stackDialog.setTitle(Bundle.getMessage("WindowTitleStackRoutes"));  // NOI18N
            stackDialog.add(stackPanel);
        }
        stackPanel.updateGUI();

        stackDialog.pack();
        stackDialog.setModal(false);
        stackDialog.setVisible(true);
    }

    StackNXPanel stackPanel = null;
    JDialog stackDialog = null;

    /**
     * Get a list of all stacked routes from stackList.
     *
     * @return an List containing destinationPoint elements
     */
    public List<DestinationPoints> getStackedInterlocks() {
        List<DestinationPoints> dpList = new ArrayList<>();
        for (StackDetails st : stackList) {
            dpList.add(st.getDestinationPoint());
        }
        return dpList;
    }

    /**
     * Query if a stacked route is in stackList.
     *
     * @param dp DestinationPoints object
     * @param reverse true for a reversed running direction, mostly false
     * @return true if dp is in stackList
     */
    public boolean isRouteStacked(DestinationPoints dp, boolean reverse) {
        Iterator<StackDetails> iter = stackList.iterator();
        while (iter.hasNext()) {
            StackDetails st = iter.next();
            if (st.getDestinationPoint() == dp && st.getReverse() == reverse) {
                return true;
            }
        }
        return false;
    }

    /**
     * Remove a stacked route from stackList.
     *
     * @param dp DestinationPoints object
     * @param reverse true for a reversed running direction, mostly false
     */
    synchronized public void cancelStackedRoute(DestinationPoints dp, boolean reverse) {
        Iterator<StackDetails> iter = stackList.iterator();
        while (iter.hasNext()) {
            StackDetails st = iter.next();
            if (st.getDestinationPoint() == dp && st.getReverse() == reverse) {
                iter.remove();
            }
        }
        stackPanel.updateGUI();
        if (stackList.isEmpty()) {
            stackDialog.setVisible(false);
            checkTimer.stop();
        }
    }

    /**
     * Class to collect (stack) routes when they are requested but blocked.
     */
    static class StackDetails {

        DestinationPoints dp;
        boolean reverse;

        StackDetails(DestinationPoints dp, boolean reverse) {
            this.dp = dp;
            this.reverse = reverse;
        }

        boolean getReverse() {
            return reverse;
        }

        DestinationPoints getDestinationPoint() {
            return dp;
        }
    }

    javax.swing.Timer checkTimer = new javax.swing.Timer(10000, (java.awt.event.ActionEvent e) -> {
        checkRoute();
    });

    /**
     * Step through stackList and activate the first stacked route in line
     * if it is no longer blocked.
     */
    synchronized void checkRoute() {
        checkTimer.stop();
        StackDetails[] tmp = new StackDetails[stackList.size()];
        stackList.toArray(tmp);

        for (StackDetails st : tmp) {
            if (!st.getDestinationPoint().isActive()) {
                // If the route is not already active, then check.
                // If the route does get set, then the setting process will remove the route from the stack.
                st.getDestinationPoint().setInterlockRoute(st.getReverse());
            }
        }

        if (!stackList.isEmpty()) {
            checkTimer.start();
        } else {
            stackDialog.setVisible(false);
        }
    }

    public void removePropertyChangeListener(PropertyChangeListener list, NamedBean obj, LayoutEditor panel) {
        if (obj == null) {
            return;
        }
        PointDetails valid = getPointDetails(obj, panel);
        if (valid != null) {
            valid.removePropertyChangeListener(list);
        }
    }

    boolean runWhenStabilised = false;
    LayoutEditor toUseWhenStable;
    int interlockTypeToUseWhenStable;

    /**
     * Discover all possible valid source and destination Signal Mast Logic pairs
     * on all Layout Editor panels.
     *
     * @param editor The Layout Editor panel
     * @param interlockType Integer value representing the type of interlocking, one of
     *                      SETUPTURNOUTSONLY, SETUPSIGNALMASTLOGIC or FULLINTERLOCK
     * @throws JmriException when an error occurs during discovery
     */
    public void automaticallyDiscoverEntryExitPairs(LayoutEditor editor, int interlockType) throws JmriException {
        //This is almost a duplicate of that in the DefaultSignalMastLogicManager
        runWhenStabilised = false;
        jmri.jmrit.display.layoutEditor.LayoutBlockManager lbm = InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class);
        if (!lbm.isAdvancedRoutingEnabled()) {
            throw new JmriException("advanced routing not enabled");  // NOI18N
        }
        if (!lbm.routingStablised()) {
            runWhenStabilised = true;
            toUseWhenStable = editor;
            interlockTypeToUseWhenStable = interlockType;
            log.debug("Layout block routing has not yet stabilised, discovery will happen once it has");  // NOI18N
            return;
        }
        HashMap<NamedBean, List<NamedBean>> validPaths = lbm.getLayoutBlockConnectivityTools().
                discoverValidBeanPairs(null, Sensor.class, LayoutBlockConnectivityTools.Routing.SENSORTOSENSOR);
        EntryExitPairs eep = this;
        for (Entry<NamedBean, List<NamedBean>> entry : validPaths.entrySet()) {
            NamedBean key = entry.getKey();
            List<NamedBean> validDestMast = validPaths.get(key);
            if (validDestMast.size() > 0) {
                eep.addNXSourcePoint(key, editor);
                for (int i = 0; i < validDestMast.size(); i++) {
                    if (!eep.isDestinationValid(key, validDestMast.get(i), editor)) {
                        eep.addNXDestination(key, validDestMast.get(i), editor);
                        eep.setEntryExitType(key, editor, validDestMast.get(i), interlockType);
                    }
                }
            }
        }

        firePropertyChange("autoGenerateComplete", null, null);  // NOI18N
    }

    protected PropertyChangeListener propertyBlockManagerListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent e) {
            if (e.getPropertyName().equals("topology")) {  // NOI18N
                //boolean newValue = new Boolean.parseBoolean(String.valueOf(e.getNewValue()));
                boolean newValue = (Boolean) e.getNewValue();
                if (newValue) {
                    if (runWhenStabilised) {
                        try {
                            automaticallyDiscoverEntryExitPairs(toUseWhenStable, interlockTypeToUseWhenStable);
                        } catch (JmriException je) {
                            //Considered normal if routing not enabled
                        }
                    }
                }
            }
        }
    };

    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {

    }

    @Override
    public void deleteBean(@Nonnull DestinationPoints bean, @Nonnull String property) throws PropertyVetoException {

    }

    @Override
    @Nonnull
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameTransits" : "BeanNameTransit");  // NOI18N
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<DestinationPoints> getNamedBeanClass() {
        return DestinationPoints.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @OverridingMethodsMustInvokeSuper
    public void setPropertyChangesSilenced(@Nonnull String propertyName, boolean silenced) {
        if (!"beans".equals(propertyName)) {
            throw new IllegalArgumentException("Property " + propertyName + " cannot be silenced.");
        }
        silencedProperties.put(propertyName, silenced);
        if (propertyName.equals("beans") && !silenced) {
            fireIndexedPropertyChange("beans", getNamedBeanSet().size(), null, null);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void addDataListener(ManagerDataListener<DestinationPoints> e) {
        if (e != null) listeners.add(e);
    }

    /** {@inheritDoc} */
    @Override
    public void removeDataListener(ManagerDataListener<DestinationPoints> e) {
        if (e != null) listeners.remove(e);
    }

    final List<ManagerDataListener<DestinationPoints>> listeners = new ArrayList<>();

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(EntryExitPairs.class);

}
