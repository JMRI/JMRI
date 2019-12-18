package jmri.jmrit.entryexit;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import jmri.*;
import jmri.jmrit.dispatcher.ActiveTrain;
import jmri.jmrit.display.layoutEditor.ConnectivityUtil;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.jmrit.display.layoutEditor.LayoutBlockConnectivityTools;
import jmri.jmrit.display.layoutEditor.LayoutSlip;
import jmri.jmrit.display.layoutEditor.LayoutTrackExpectedState;
import jmri.jmrit.display.layoutEditor.LayoutTurnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DestinationPoints extends jmri.implementation.AbstractNamedBean {

    @Override
    public String getBeanType() {
        return Bundle.getMessage("BeanNameDestination");  // NOI18N
    }

    transient PointDetails point = null;
    Boolean uniDirection = true;
    int entryExitType = EntryExitPairs.SETUPTURNOUTSONLY;//SETUPSIGNALMASTLOGIC;
    boolean enabled = true;
    boolean activeEntryExit = false;
    List<LayoutBlock> routeDetails = new ArrayList<>();
    LayoutBlock destination;
    boolean disposed = false;

    transient EntryExitPairs manager = jmri.InstanceManager.getDefault(jmri.jmrit.entryexit.EntryExitPairs.class);

    transient jmri.SignalMastLogic sml;

    final static int NXMESSAGEBOXCLEARTIMEOUT = 30;

    boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean boo) {
        enabled = boo;

        // Modify source signal mast held state
        Sensor sourceSensor = src.getPoint().getSensor();
        if (sourceSensor == null) {
            return;
        }
        SignalMast sourceMast = src.getPoint().getSignalMast();
        if (sourceMast == null) {
            return;
        }
        if (enabled) {
            sourceMast.setHeld(true);
        } else {
            // All destinations for the source must be disabled before the mast hold can be released
            for (PointDetails pd : src.getDestinationPoints()) {
                if (src.getDestForPoint(pd).isEnabled()) {
                    return;
                }
            }
            sourceMast.setHeld(false);
        }
    }

    transient Source src = null;

    DestinationPoints(PointDetails point, String id, Source src) {
        super(id != null ? id : "IN:" + UUID.randomUUID().toString());
        this.src = src;
        this.point = point;
        setUserName(src.getPoint().getDisplayName() + " to " + this.point.getDisplayName());

        propertyBlockListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent e) {
                blockStateUpdated(e);
            }
        };
    }

    String getUniqueId() {
        return getSystemName();
    }

    public PointDetails getDestPoint() {
        return point;
    }

    /**
     * @since 4.17.4
     * Making the source object available for scripting in Jython.
     */
    public Source getSource() {
        return src ;
    }

    boolean getUniDirection() {
        return uniDirection;
    }

    void setUniDirection(boolean uni) {
        uniDirection = uni;
    }

    NamedBean getSignal() {
        return point.getSignal();
    }

    void setRouteTo(boolean set) {
        if (set && getEntryExitType() == EntryExitPairs.FULLINTERLOCK) {
            point.setRouteTo(true);
            point.setNXButtonState(EntryExitPairs.NXBUTTONACTIVE);
        } else {
            point.setRouteTo(false);
            point.setNXButtonState(EntryExitPairs.NXBUTTONINACTIVE);
        }
    }

    void setRouteFrom(boolean set) {
        if (set && getEntryExitType() == EntryExitPairs.FULLINTERLOCK) {
            src.pd.setRouteFrom(true);
            src.pd.setNXButtonState(EntryExitPairs.NXBUTTONACTIVE);
        } else {
            src.pd.setRouteFrom(false);
            src.pd.setNXButtonState(EntryExitPairs.NXBUTTONINACTIVE);
        }
    }

    boolean isRouteToPointSet() {
        return point.isRouteToPointSet();
    }

    LayoutBlock getFacing() {
        return point.getFacing();
    }

    List<LayoutBlock> getProtecting() {
        return point.getProtecting();
    }

    int getEntryExitType() {
        return entryExitType;
    }

    void setEntryExitType(int type) {
        entryExitType = type;
        if ((type != EntryExitPairs.SETUPTURNOUTSONLY) && (getSignal() != null) && point.getSignal() != null) {
            uniDirection = true;
        }
    }

    @SuppressFBWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED",
            justification = "No auto serialization")
    transient protected PropertyChangeListener propertyBlockListener;

    protected void blockStateUpdated(PropertyChangeEvent e) {
        Block blk = (Block) e.getSource();
        if (e.getPropertyName().equals("state")) {  // NOI18N
            if (log.isDebugEnabled()) {
                log.debug(getUserName() + "  We have a change of state on the block " + blk.getDisplayName());  // NOI18N
            }
            int now = ((Integer) e.getNewValue()).intValue();

            if (now == Block.OCCUPIED) {
                LayoutBlock lBlock = InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).getLayoutBlock(blk);
                //If the block was previously active or inactive then we will
                //reset the useExtraColor, but not if it was previously unknown or inconsistent.
                lBlock.setUseExtraColor(false);
                blk.removePropertyChangeListener(propertyBlockListener); //was this
                removeBlockFromRoute(lBlock);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("state was " + now + " and did not go through reset");  // NOI18N
                }
            }
        }
    }

    Object lastSeenActiveBlockObject;

    synchronized void removeBlockFromRoute(LayoutBlock lBlock) {

        if (routeDetails != null) {
            if (routeDetails.indexOf(lBlock) == -1) {
                if (src.getStart() == lBlock) {
                    log.debug("Start block went active");  // NOI18N
                    lastSeenActiveBlockObject = src.getStart().getBlock().getValue();
                    lBlock.getBlock().removePropertyChangeListener(propertyBlockListener);
                    return;
                } else {
                    log.error("Block " + lBlock.getDisplayName() + " went active but it is not part of our NX path");  // NOI18N
                }
            }
            if (routeDetails.indexOf(lBlock) != 0) {
                log.debug("A block has been skipped will set the value of the active block to that of the original one");  // NOI18N
                lBlock.getBlock().setValue(lastSeenActiveBlockObject);
                if (routeDetails.indexOf(lBlock) != -1) {
                    while (routeDetails.indexOf(lBlock) != 0) {
                        LayoutBlock tbr = routeDetails.get(0);
                        log.debug("Block skipped " + tbr.getDisplayName() + " and removed from list");  // NOI18N
                        tbr.getBlock().removePropertyChangeListener(propertyBlockListener);
                        tbr.setUseExtraColor(false);
                        routeDetails.remove(0);
                    }
                }
            }
            if (routeDetails.contains(lBlock)) {
                routeDetails.remove(lBlock);
                setRouteFrom(false);
                src.pd.setNXButtonState(EntryExitPairs.NXBUTTONINACTIVE);
                if (sml != null && getEntryExitType() == EntryExitPairs.FULLINTERLOCK) {
                    sml.getSourceMast().setHeld(true);
                    SignalMast mast = (SignalMast) getSignal();
                    if (sml.getStoreState(mast) == jmri.SignalMastLogic.STORENONE) {
                        sml.removeDestination(mast);
                    }
                }
            } else {
                log.error("Block " + lBlock.getDisplayName() + " that went Occupied was not in the routeDetails list");  // NOI18N
            }
            if (log.isDebugEnabled()) {
                log.debug("Route details contents " + routeDetails);  // NOI18N
                for (int i = 0; i < routeDetails.size(); i++) {
                    log.debug("      " + routeDetails.get(i).getDisplayName());
                }
            }
            if ((routeDetails.size() == 1) && (routeDetails.contains(destination))) {
                routeDetails.get(0).getBlock().removePropertyChangeListener(propertyBlockListener);  // was set against block sensor
                routeDetails.remove(destination);
            }
        }
        lastSeenActiveBlockObject = lBlock.getBlock().getValue();

        if ((routeDetails == null) || (routeDetails.size() == 0)) {
            //At this point the route has cleared down/the last remaining block are now active.
            routeDetails = null;
            setRouteTo(false);
            setRouteFrom(false);
            setActiveEntryExit(false);
            lastSeenActiveBlockObject = null;
        }
    }

    //For a clear down we need to add a message, if it is a cancel, manual clear down or I didn't mean it.
    void setRoute(boolean state) {
        if (log.isDebugEnabled()) {
            log.debug("Set route " + src.getPoint().getDisplayName());  // NOI18N
        }
        if (disposed) {
            log.error("Set route called even though interlock has been disposed of");  // NOI18N
            return;
        }

        if (routeDetails == null) {
            log.error("No route to set or clear down");  // NOI18N
            setActiveEntryExit(false);
            setRouteTo(false);
            setRouteFrom(false);
            if ((getSignal() instanceof SignalMast) && (getEntryExitType() != EntryExitPairs.FULLINTERLOCK)) {
                SignalMast mast = (SignalMast) getSignal();
                mast.setHeld(false);
            }
            synchronized (this) {
                destination = null;
            }
            return;
        }
        if (!state) {
            switch (manager.getClearDownOption()) {
                case EntryExitPairs.PROMPTUSER:
                    cancelClearOptionBox();
                    break;
                case EntryExitPairs.AUTOCANCEL:
                    cancelClearInterlock(EntryExitPairs.CANCELROUTE);
                    break;
                case EntryExitPairs.AUTOCLEAR:
                    cancelClearInterlock(EntryExitPairs.CLEARROUTE);
                    break;
                case EntryExitPairs.AUTOSTACK:
                    cancelClearInterlock(EntryExitPairs.STACKROUTE);
                    break;
                default:
                    cancelClearOptionBox();
                    break;
            }
            if (log.isDebugEnabled()) {
                log.debug("Exit " + src.getPoint().getDisplayName());
            }
            return;
        }
        if (manager.isRouteStacked(this, false)) {
            manager.cancelStackedRoute(this, false);
        }
        /* We put the setting of the route into a seperate thread and put a glass pane in front of the layout editor.
         The swing thread for flashing the icons will carry on without interuption. */
        final List<Color> realColorStd = new ArrayList<Color>();
        final List<Color> realColorXtra = new ArrayList<Color>();
        final List<LayoutBlock> routeBlocks = new ArrayList<>();
        if (manager.useDifferentColorWhenSetting()) {
            boolean first = true;
            for (LayoutBlock lbk : routeDetails) {
                if (first) {
                    // Don't change the color for the facing block
                    first = false;
                    continue;
                }
                routeBlocks.add(lbk);
                realColorXtra.add(lbk.getBlockExtraColor());
                realColorStd.add(lbk.getBlockTrackColor());
                lbk.setBlockExtraColor(manager.getSettingRouteColor());
                lbk.setBlockTrackColor(manager.getSettingRouteColor());
            }
            //Force a redraw, to reflect color change
            src.getPoint().getPanel().redrawPanel();
        }
        ActiveTrain tmpat = null;
        if (manager.getDispatcherIntegration() && jmri.InstanceManager.getNullableDefault(jmri.jmrit.dispatcher.DispatcherFrame.class) != null) {
            jmri.jmrit.dispatcher.DispatcherFrame df = jmri.InstanceManager.getDefault(jmri.jmrit.dispatcher.DispatcherFrame.class);
            for (ActiveTrain atl : df.getActiveTrainsList()) {
                if (atl.getEndBlock() == src.getStart().getBlock()) {
                    if (atl.getLastAllocatedSection() == atl.getEndBlockSection()) {
                        if (!atl.getReverseAtEnd() && !atl.getResetWhenDone()) {
                            tmpat = atl;
                            break;
                        }
                        log.warn("Interlock will not be added to existing Active Train as it is set for back and forth operation");  // NOI18N
                    }
                }
            }
        }
        final ActiveTrain at = tmpat;
        Runnable setRouteRun = new Runnable() {
            @Override
            public void run() {
                src.getPoint().getPanel().getGlassPane().setVisible(true);

                try {
                    Hashtable<Turnout, Integer> turnoutSettings = new Hashtable<Turnout, Integer>();

                    ConnectivityUtil connection = new ConnectivityUtil(point.getPanel());
//                     log.info("@@ New ConnectivityUtil = '{}'", point.getPanel().getLayoutName());

                    // This for loop was after the if statement
                    // Last block in the route is the one that we are protecting at the last sensor/signalmast
                    for (int i = 0; i < routeDetails.size(); i++) {
                        //if we are not using the dispatcher and the signal logic is dynamic, then set the turnouts
                        if (at == null && isSignalLogicDynamic()) {
                            if (i > 0) {
                                List<LayoutTrackExpectedState<LayoutTurnout>> turnoutlist;
                                int nxtBlk = i + 1;
                                int preBlk = i - 1;
                                if (i < routeDetails.size() - 1) {
                                    turnoutlist = connection.getTurnoutList(routeDetails.get(i).getBlock(), routeDetails.get(preBlk).getBlock(), routeDetails.get(nxtBlk).getBlock());
                                    for (int x = 0; x < turnoutlist.size(); x++) {
                                        if (turnoutlist.get(x).getObject() instanceof LayoutSlip) {
                                            int slipState = turnoutlist.get(x).getExpectedState();
                                            LayoutSlip ls = (LayoutSlip) turnoutlist.get(x).getObject();
                                            int taState = ls.getTurnoutState(slipState);
                                            turnoutSettings.put(ls.getTurnout(), taState);

                                            int tbState = ls.getTurnoutBState(slipState);
                                            ls.getTurnoutB().setCommandedState(tbState);
                                            turnoutSettings.put(ls.getTurnoutB(), tbState);
                                        } else {
                                            String t = turnoutlist.get(x).getObject().getTurnoutName();
                                            Turnout turnout = InstanceManager.turnoutManagerInstance().getTurnout(t);
                                            if (turnout != null) {
                                                turnoutSettings.put(turnout, turnoutlist.get(x).getExpectedState());
                                                if (turnoutlist.get(x).getObject().getSecondTurnout() != null) {
                                                    turnoutSettings.put(turnoutlist.get(x).getObject().getSecondTurnout(),
                                                            turnoutlist.get(x).getExpectedState());
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if ((getEntryExitType() == EntryExitPairs.FULLINTERLOCK)) {
                            routeDetails.get(i).getBlock().addPropertyChangeListener(propertyBlockListener); // was set against occupancy sensor
                            if (i > 0) {
                                routeDetails.get(i).setUseExtraColor(true);
                            }
                        } else {
                            routeDetails.get(i).getBlock().removePropertyChangeListener(propertyBlockListener); // was set against occupancy sensor
                        }
                    }
                    if (at == null) {
                        if (!isSignalLogicDynamic()) {
                            jmri.SignalMastLogic tmSml = InstanceManager.getDefault(jmri.SignalMastLogicManager.class).getSignalMastLogic((SignalMast) src.sourceSignal);
                            for (Turnout t : tmSml.getAutoTurnouts((SignalMast) getSignal())) {
                                turnoutSettings.put(t, tmSml.getAutoTurnoutState(t, (SignalMast) getSignal()));
                            }
                        }
                        for (Map.Entry< Turnout, Integer> entry : turnoutSettings.entrySet()) {
                            entry.getKey().setCommandedState(entry.getValue());
//                             log.info("**> Set turnout '{}'", entry.getKey().getDisplayName());
                            Runnable r = new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Thread.sleep(250 + manager.turnoutSetDelay);
                                    } catch (InterruptedException ex) {
                                        Thread.currentThread().interrupt();
                                    }
                                }
                            };
                            Thread thr = new Thread(r, "Entry Exit Route: Turnout Setting");  // NOI18N
                            thr.start();
                            try {
                                thr.join();
                            } catch (InterruptedException ex) {
                                //            log.info("interrupted at join " + ex);
                            }
                        }
                    }
                    src.getPoint().getPanel().redrawPanel();
                    if (getEntryExitType() != EntryExitPairs.SETUPTURNOUTSONLY) {
                        if (getEntryExitType() == EntryExitPairs.FULLINTERLOCK) {
                            //If our start block is already active we will set it as our lastSeenActiveBlock.
                            if (src.getStart().getState() == Block.OCCUPIED) {
                                src.getStart().removePropertyChangeListener(propertyBlockListener);
                                lastSeenActiveBlockObject = src.getStart().getBlock().getValue();
                                log.debug("Last seen value " + lastSeenActiveBlockObject);
                            }
                        }
                        if ((src.sourceSignal instanceof SignalMast) && (getSignal() instanceof SignalMast)) {
                            SignalMast smSource = (SignalMast) src.sourceSignal;
                            SignalMast smDest = (SignalMast) getSignal();
                            synchronized (this) {
                                sml = InstanceManager.getDefault(jmri.SignalMastLogicManager.class).newSignalMastLogic(smSource);
                                if (!sml.isDestinationValid(smDest)) {
                                    //if no signalmastlogic existed then created it, but set it not to be stored.
                                    sml.setDestinationMast(smDest);
                                    sml.setStore(jmri.SignalMastLogic.STORENONE, smDest);
                                }
                            }

                            //Remove the first block as it is our start block
                            routeDetails.remove(0);

                            synchronized (this) {
                                releaseMast(smSource, turnoutSettings);
                                //Only change the block and turnout details if this a temp signalmast logic
                                if (sml.getStoreState(smDest) == jmri.SignalMastLogic.STORENONE) {
                                    LinkedHashMap<Block, Integer> blks = new LinkedHashMap<Block, Integer>();
                                    for (int i = 0; i < routeDetails.size(); i++) {
                                        if (routeDetails.get(i).getBlock().getState() == Block.UNKNOWN) {
                                            routeDetails.get(i).getBlock().setState(Block.UNOCCUPIED);
                                        }
                                        blks.put(routeDetails.get(i).getBlock(), Block.UNOCCUPIED);
                                    }
                                    sml.setAutoBlocks(blks, smDest);
                                    sml.setAutoTurnouts(turnoutSettings, smDest);
                                    sml.initialise(smDest);
                                }
                            }
                            smSource.addPropertyChangeListener(new PropertyChangeListener() {
                                @Override
                                public void propertyChange(PropertyChangeEvent e) {
                                    SignalMast source = (SignalMast) e.getSource();
                                    source.removePropertyChangeListener(this);
                                    setRouteFrom(true);
                                    setRouteTo(true);
                                }
                            });
                            src.pd.extendedtime = true;
                            point.extendedtime = true;
                        } else {
                            if (src.sourceSignal instanceof SignalMast) {
                                SignalMast mast = (SignalMast) src.sourceSignal;
                                releaseMast(mast, turnoutSettings);
                            } else if (src.sourceSignal instanceof SignalHead) {
                                SignalHead head = (SignalHead) src.sourceSignal;
                                head.setHeld(false);
                            }
                            setRouteFrom(true);
                            setRouteTo(true);
                        }
                    }
                    if (manager.useDifferentColorWhenSetting()) {
                        //final List<Color> realColorXtra = realColorXtra;
                        javax.swing.Timer resetColorBack = new javax.swing.Timer(manager.getSettingTimer(), new java.awt.event.ActionListener() {
                            @Override
                            public void actionPerformed(java.awt.event.ActionEvent e) {
                                for (int i = 0; i < routeBlocks.size(); i++) {
                                    LayoutBlock lbk = routeBlocks.get(i);
                                    lbk.setBlockExtraColor(realColorXtra.get(i));
                                    lbk.setBlockTrackColor(realColorStd.get(i));
                                }
                                src.getPoint().getPanel().redrawPanel();
                            }
                        });
                        resetColorBack.setRepeats(false);
                        resetColorBack.start();
                    }

                    if (at != null) {
                        jmri.Section sec = null;
                        if (sml != null && sml.getAssociatedSection((SignalMast) getSignal()) != null) {
                            sec = sml.getAssociatedSection((SignalMast) getSignal());
                        } else {
                            sec = InstanceManager.getDefault(jmri.SectionManager.class).createNewSection(src.getPoint().getDisplayName() + ":" + point.getDisplayName());
                            if (sec == null) {
                                //A Section already exists, lets grab it and check that it is one used with the Interlocking, if so carry on using that.
                                sec = InstanceManager.getDefault(jmri.SectionManager.class).getSection(src.getPoint().getDisplayName() + ":" + point.getDisplayName());
                            } else {
                                sec.setSectionType(jmri.Section.DYNAMICADHOC);
                            }
                            if (sec.getSectionType() == jmri.Section.DYNAMICADHOC) {
                                sec.removeAllBlocksFromSection();
                                for (LayoutBlock key : routeDetails) {
                                    if (key != src.getStart()) {
                                        sec.addBlock(key.getBlock());
                                    }
                                }
                                String dir = jmri.Path.decodeDirection(src.getStart().getNeighbourDirection(routeDetails.get(0).getBlock()));
                                jmri.EntryPoint ep = new jmri.EntryPoint(routeDetails.get(0).getBlock(), src.getStart().getBlock(), dir);
                                ep.setTypeForward();
                                sec.addToForwardList(ep);

                                LayoutBlock proDestLBlock = point.getProtecting().get(0);
                                if (proDestLBlock != null) {
                                    dir = jmri.Path.decodeDirection(proDestLBlock.getNeighbourDirection(point.getFacing()));
                                    ep = new jmri.EntryPoint(point.getFacing().getBlock(), proDestLBlock.getBlock(), dir);
                                    ep.setTypeReverse();
                                    sec.addToReverseList(ep);
                                }
                            }
                        }
                        jmri.InstanceManager.getDefault(jmri.jmrit.dispatcher.DispatcherFrame.class).extendActiveTrainsPath(sec, at, src.getPoint().getPanel());
                    }

                    src.pd.setNXButtonState(EntryExitPairs.NXBUTTONINACTIVE);
                    point.setNXButtonState(EntryExitPairs.NXBUTTONINACTIVE);
                } catch (RuntimeException ex) {
                    log.error("An error occurred while setting the route", ex);  // NOI18N
                    src.pd.setNXButtonState(EntryExitPairs.NXBUTTONINACTIVE);
                    point.setNXButtonState(EntryExitPairs.NXBUTTONINACTIVE);
                    if (manager.useDifferentColorWhenSetting()) {
                        for (int i = 0; i < routeBlocks.size(); i++) {
                            LayoutBlock lbk = routeBlocks.get(i);
                            lbk.setBlockExtraColor(realColorXtra.get(i));
                            lbk.setBlockTrackColor(realColorStd.get(i));
                        }
                    }
                    src.getPoint().getPanel().redrawPanel();
                }
                src.getPoint().getPanel().getGlassPane().setVisible(false);
                //src.setMenuEnabled(true);
            }
        };
        Thread thrMain = new Thread(setRouteRun, "Entry Exit Set Route");  // NOI18N
        thrMain.start();
        try {
            thrMain.join();
        } catch (InterruptedException e) {
            log.error("Interuption exception " + e.toString());  // NOI18N
        }
        if (log.isDebugEnabled()) {
            log.debug("finish route " + src.getPoint().getDisplayName());  // NOI18N
        }
    }

    /**
     * Remove the hold on the mast when all of the turnouts have completed moving.
     * This only applies to turnouts using ONESENSOR feedback.  TWOSENSOR has an
     * intermediate inconsistent state which prevents erroneous signal aspects.
     * The maximum wait time is 10 seconds.
     *
     * @since 4.11.1
     * @param mast The signal mast that will be released.
     * @param turnoutSettings The turnouts that are being set for the current NX route.
     */
    private void releaseMast(SignalMast mast, Hashtable<Turnout, Integer> turnoutSettings) {
        Hashtable<Turnout, Integer> turnoutList = new Hashtable<>(turnoutSettings);
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 20; i > 0; i--) {
                        int active = 0;
                        for (Map.Entry< Turnout, Integer> entry : turnoutList.entrySet()) {
                            Turnout tout = entry.getKey();
                            if (tout.getFeedbackMode() == Turnout.ONESENSOR) {
                                // Check state
                                if (tout.getKnownState() != tout.getCommandedState()) {
                                    active += 1;
                                }
                            }
                        }
                        if (active == 0) {
                            break;
                        }
                        Thread.sleep(500);
                    }
                    log.debug("Release mast: {}", mast.getDisplayName());
                    mast.setHeld(false);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        };
        Thread thr = new Thread(r, "Entry Exit Route: Release Mast");  // NOI18N
        thr.start();
    }

    private boolean isSignalLogicDynamic() {
        if ((src.sourceSignal instanceof SignalMast) && (getSignal() instanceof SignalMast)) {
            SignalMast smSource = (SignalMast) src.sourceSignal;
            SignalMast smDest = (SignalMast) getSignal();
            if (InstanceManager.getDefault(jmri.SignalMastLogicManager.class).getSignalMastLogic(smSource) != null
                    && InstanceManager.getDefault(jmri.SignalMastLogicManager.class).getSignalMastLogic(smSource).getStoreState(smDest) != jmri.SignalMastLogic.STORENONE) {
                return false;
            }
        }
        return true;

    }

    private JFrame cancelClearFrame;
    transient private Thread threadAutoClearFrame = null;
    JButton jButton_Stack = new JButton(Bundle.getMessage("Stack"));  // NOI18N

    void cancelClearOptionBox() {
        if (cancelClearFrame == null) {
            JButton jButton_Clear = new JButton(Bundle.getMessage("ClearDown"));  // NOI18N
            JButton jButton_Cancel = new JButton(Bundle.getMessage("ButtonCancel"));  // NOI18N

            JButton jButton_Exit = new JButton(Bundle.getMessage("Exit"));  // NOI18N
            JLabel jLabel = new JLabel(Bundle.getMessage("InterlockPrompt"));  // NOI18N
            JLabel jIcon = new JLabel(javax.swing.UIManager.getIcon("OptionPane.questionIcon"));  // NOI18N
            cancelClearFrame = new JFrame(Bundle.getMessage("Interlock"));  // NOI18N
            Container cont = cancelClearFrame.getContentPane();
            JPanel qPanel = new JPanel();
            qPanel.add(jIcon);
            qPanel.add(jLabel);
            cont.add(qPanel, BorderLayout.CENTER);
            JPanel buttonsPanel = new JPanel();
            buttonsPanel.add(jButton_Cancel);
            buttonsPanel.add(jButton_Clear);
            buttonsPanel.add(jButton_Stack);
            buttonsPanel.add(jButton_Exit);
            cont.add(buttonsPanel, BorderLayout.SOUTH);
            cancelClearFrame.pack();

            jButton_Clear.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    cancelClearFrame.setVisible(false);
                    threadAutoClearFrame.interrupt();
                    cancelClearInterlock(EntryExitPairs.CLEARROUTE);
                }
            });
            jButton_Cancel.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    cancelClearFrame.setVisible(false);
                    threadAutoClearFrame.interrupt();
                    cancelClearInterlock(EntryExitPairs.CANCELROUTE);
                }
            });
            jButton_Stack.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    cancelClearFrame.setVisible(false);
                    threadAutoClearFrame.interrupt();
                    cancelClearInterlock(EntryExitPairs.STACKROUTE);
                }
            });
            jButton_Exit.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    cancelClearFrame.setVisible(false);
                    threadAutoClearFrame.interrupt();
                    cancelClearInterlock(EntryExitPairs.EXITROUTE);
                    firePropertyChange("noChange", null, null);  // NOI18N
                }
            });
            src.getPoint().getPanel().setGlassPane(manager.getGlassPane());

        }
        cancelClearFrame.setTitle(getUserName());
        if (manager.isRouteStacked(this, false)) {
            jButton_Stack.setEnabled(false);
        } else {
            jButton_Stack.setEnabled(true);
        }

        if (cancelClearFrame.isVisible()) {
            return;
        }
        src.pd.extendedtime = true;
        point.extendedtime = true;

        class MessageTimeOut implements Runnable {

            MessageTimeOut() {
            }

            @Override
            public void run() {
                try {
                    //Set a timmer before this window is automatically closed to 30 seconds
                    Thread.sleep(NXMESSAGEBOXCLEARTIMEOUT * 1000);
                    cancelClearFrame.setVisible(false);
                    cancelClearInterlock(EntryExitPairs.EXITROUTE);
                } catch (InterruptedException ex) {
                    log.debug("Flash timer cancelled");  // NOI18N
                }
            }
        }
        MessageTimeOut mt = new MessageTimeOut();
        threadAutoClearFrame = new Thread(mt, "NX Button Clear Message Timeout ");  // NOI18N
        threadAutoClearFrame.start();
        cancelClearFrame.setAlwaysOnTop(true);
        src.getPoint().getPanel().getGlassPane().setVisible(true);
        int w = cancelClearFrame.getSize().width;
        int h = cancelClearFrame.getSize().height;
        int x = (int) src.getPoint().getPanel().getLocation().getX() + ((src.getPoint().getPanel().getSize().width - w) / 2);
        int y = (int) src.getPoint().getPanel().getLocation().getY() + ((src.getPoint().getPanel().getSize().height - h) / 2);
        cancelClearFrame.setLocation(x, y);
        cancelClearFrame.setVisible(true);
    }

    void cancelClearInterlock(int cancelClear) {
        if ((cancelClear == EntryExitPairs.EXITROUTE) || (cancelClear == EntryExitPairs.STACKROUTE)) {
            src.pd.setNXButtonState(EntryExitPairs.NXBUTTONINACTIVE);
            point.setNXButtonState(EntryExitPairs.NXBUTTONINACTIVE);
            src.getPoint().getPanel().getGlassPane().setVisible(false);
            if (cancelClear == EntryExitPairs.STACKROUTE) {
                manager.stackNXRoute(this, false);
            }
            return;
        }

        if (cancelClear == EntryExitPairs.CANCELROUTE) {
            if (manager.getDispatcherIntegration() && jmri.InstanceManager.getNullableDefault(jmri.jmrit.dispatcher.DispatcherFrame.class) != null) {
                jmri.jmrit.dispatcher.DispatcherFrame df = jmri.InstanceManager.getDefault(jmri.jmrit.dispatcher.DispatcherFrame.class);
                ActiveTrain at = null;
                for (ActiveTrain atl : df.getActiveTrainsList()) {
                    if (atl.getEndBlock() == point.getFacing().getBlock()) {
                        if (atl.getLastAllocatedSection() == atl.getEndBlockSection()) {
                            at = atl;
                            break;
                        }
                    }
                }
                if (at != null) {
                    jmri.Section sec = null;
                    synchronized (this) {
                        if (sml != null && sml.getAssociatedSection((SignalMast) getSignal()) != null) {
                            sec = sml.getAssociatedSection((SignalMast) getSignal());
                        } else {
                            sec = InstanceManager.getDefault(jmri.SectionManager.class).getSection(src.getPoint().getDisplayName() + ":" + point.getDisplayName());
                        }
                    }
                    if (sec != null) {
                        if (!df.removeFromActiveTrainPath(sec, at, src.getPoint().getPanel())) {
                            log.error("Unable to remove allocation from dispathcer, leave interlock in place");  // NOI18N
                            src.pd.cancelNXButtonTimeOut();
                            point.cancelNXButtonTimeOut();
                            src.getPoint().getPanel().getGlassPane().setVisible(false);
                            return;
                        }
                        if (sec.getSectionType() == jmri.Section.DYNAMICADHOC) {
                            sec.removeAllBlocksFromSection();
                        }
                    }
                }
            }
        }
        src.setMenuEnabled(false);
        if (src.sourceSignal instanceof SignalMast) {
            SignalMast mast = (SignalMast) src.sourceSignal;
            mast.setAspect(mast.getAppearanceMap().getSpecificAppearance(jmri.SignalAppearanceMap.DANGER));
            mast.setHeld(true);
        } else if (src.sourceSignal instanceof SignalHead) {
            SignalHead head = (SignalHead) src.sourceSignal;
            head.setHeld(true);
        } else {
            log.debug("No signal found");  // NOI18N
        }

        //Get rid of the signal mast logic to the destination mast.
        synchronized (this) {
            if ((getSignal() instanceof SignalMast) && (sml != null)) {
                SignalMast mast = (SignalMast) getSignal();
                if (sml.getStoreState(mast) == jmri.SignalMastLogic.STORENONE) {
                    sml.removeDestination(mast);
                }
            }
            sml = null;
        }

        if (routeDetails == null) {
            return;
        }

        for (LayoutBlock blk : routeDetails) {
            if ((getEntryExitType() == EntryExitPairs.FULLINTERLOCK)) {
                blk.setUseExtraColor(false);
            }
            blk.getBlock().removePropertyChangeListener(propertyBlockListener); // was set against occupancy sensor
        }

        if (cancelClear == EntryExitPairs.CLEARROUTE) {
            if (routeDetails.size() == 0) {
                if (log.isDebugEnabled()) {
                    log.debug(getUserName() + "  all blocks have automatically been cleared down");  // NOI18N
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug(getUserName() + "  No blocks were cleared down " + routeDetails.size());  // NOI18N
                }
                try {
                    if (log.isDebugEnabled()) {
                        log.debug(getUserName() + "  set first block as active so that we can manually clear this down " + routeDetails.get(0).getBlock().getUserName());  // NOI18N
                    }
                    if (routeDetails.get(0).getOccupancySensor() != null) {
                        routeDetails.get(0).getOccupancySensor().setState(Sensor.ACTIVE);
                    } else {
                        routeDetails.get(0).getBlock().goingActive();
                    }

                    if (src.getStart().getOccupancySensor() != null) {
                        src.getStart().getOccupancySensor().setState(Sensor.INACTIVE);
                    } else {
                        src.getStart().getBlock().goingInactive();
                    }
                } catch (java.lang.NullPointerException e) {
                    log.error("error in clear route A " + e);  // NOI18N
                } catch (JmriException e) {
                    log.error("error in clear route A " + e);  // NOI18N
                }
                if (log.isDebugEnabled()) {
                    log.debug(getUserName() + "  Going to clear routeDetails down " + routeDetails.size());  // NOI18N
                    for (int i = 0; i < routeDetails.size(); i++) {
                        log.debug("Block at " + i + " " + routeDetails.get(i).getDisplayName());
                    }
                }
                if (routeDetails.size() > 1) {
                    //We will remove the propertychange listeners on the sensors as we will now manually clear things down.
                    //Should we just be usrc.pdating the block status and not the sensor
                    for (int i = 1; i < routeDetails.size() - 1; i++) {
                        if (log.isDebugEnabled()) {
                            log.debug(getUserName() + " in loop Set active " + routeDetails.get(i).getDisplayName() + " " + routeDetails.get(i).getBlock().getSystemName());  // NOI18N
                        }
                        try {
                            if (routeDetails.get(i).getOccupancySensor() != null) {
                                routeDetails.get(i).getOccupancySensor().setState(Sensor.ACTIVE);
                            } else {
                                routeDetails.get(i).getBlock().goingActive();
                            }

                            if (log.isDebugEnabled()) {
                                log.debug(getUserName() + " in loop Set inactive " + routeDetails.get(i - 1).getDisplayName() + " " + routeDetails.get(i - 1).getBlock().getSystemName());  // NOI18N
                            }
                            if (routeDetails.get(i - 1).getOccupancySensor() != null) {
                                routeDetails.get(i - 1).getOccupancySensor().setState(Sensor.INACTIVE);
                            } else {
                                routeDetails.get(i - 1).getBlock().goingInactive();
                            }
                        } catch (NullPointerException | JmriException e) {
                            log.error("error in clear route b ", e);  // NOI18N
                        }
                        // NOI18N

                    }
                    try {
                        if (log.isDebugEnabled()) {
                            log.debug(getUserName() + " out of loop Set active " + routeDetails.get(routeDetails.size() - 1).getDisplayName() + " " + routeDetails.get(routeDetails.size() - 1).getBlock().getSystemName());  // NOI18N
                        }
                        //Get the last block an set it active.
                        if (routeDetails.get(routeDetails.size() - 1).getOccupancySensor() != null) {
                            routeDetails.get(routeDetails.size() - 1).getOccupancySensor().setState(Sensor.ACTIVE);
                        } else {
                            routeDetails.get(routeDetails.size() - 1).getBlock().goingActive();
                        }
                        if (log.isDebugEnabled()) {
                            log.debug(getUserName() + " out of loop Set inactive " + routeDetails.get(routeDetails.size() - 2).getUserName() + " " + routeDetails.get(routeDetails.size() - 2).getBlock().getSystemName());  // NOI18N
                        }
                        if (routeDetails.get(routeDetails.size() - 2).getOccupancySensor() != null) {
                            routeDetails.get(routeDetails.size() - 2).getOccupancySensor().setState(Sensor.INACTIVE);
                        } else {
                            routeDetails.get(routeDetails.size() - 2).getBlock().goingInactive();
                        }
                    } catch (java.lang.NullPointerException e) {
                        log.error("error in clear route c " + e);  // NOI18N
                    } catch (java.lang.ArrayIndexOutOfBoundsException e) {
                        log.error("error in clear route c " + e);  // NOI18N
                    } catch (JmriException e) {
                        log.error("error in clear route c " + e);  // NOI18N
                    }
                }
            }
        }
        setActiveEntryExit(false);
        setRouteFrom(false);
        setRouteTo(false);
        routeDetails = null;
        synchronized (this) {
            lastSeenActiveBlockObject = null;
        }
        src.pd.cancelNXButtonTimeOut();
        point.cancelNXButtonTimeOut();
        src.getPoint().getPanel().getGlassPane().setVisible(false);

    }

    public void setInterlockRoute(boolean reverseDirection) {
        if (activeEntryExit) {
            return;
        }
        activeBean(reverseDirection, false);
    }

    void activeBean(boolean reverseDirection) {
        activeBean(reverseDirection, true);
    }

    synchronized void activeBean(boolean reverseDirection, boolean showMessage) {
        if (!isEnabled()) {
            JOptionPane.showMessageDialog(null, Bundle.getMessage("RouteDisabled", getDisplayName()));  // NOI18N
            src.pd.setNXButtonState(EntryExitPairs.NXBUTTONINACTIVE);
            point.setNXButtonState(EntryExitPairs.NXBUTTONINACTIVE);
            return;
        }
        if (activeEntryExit) {
            // log.debug(getUserName() + "  Our route is active so this would go for a clear down but we need to check that the we can clear it down" + activeEndPoint);
            if (!isEnabled()) {
                log.debug("A disabled entry exit has been called will bomb out");  // NOI18N
                return;
            }
            log.debug(getUserName() + "  We have a valid match on our end point so we can clear down");  // NOI18N
            //setRouteTo(false);
            //src.pd.setRouteFrom(false);
            setRoute(false);
        } else {
            if (isRouteToPointSet()) {
                log.debug(getUserName() + "  route to this point is set therefore can not set another to it " /*+ destPoint.src.getPoint().getID()*/);  // NOI18N
                if (showMessage && !manager.isRouteStacked(this, false)) {
                    handleNoCurrentRoute(reverseDirection, "Route already set to the destination point");  // NOI18N
                }
                src.pd.setNXButtonState(EntryExitPairs.NXBUTTONINACTIVE);
                point.setNXButtonState(EntryExitPairs.NXBUTTONINACTIVE);
                return;
            } else {
                LayoutBlock startlBlock = src.getStart();
                class BestPath {

                    LayoutBlock srcProtecting = null;
                    LayoutBlock srcStart = null;
                    LayoutBlock destination = null;

                    BestPath(LayoutBlock startPro, LayoutBlock sourceProtecting, LayoutBlock destinationBlock, List<LayoutBlock> blocks) {
                        srcStart = startPro;
                        srcProtecting = sourceProtecting;
                        destination = destinationBlock;
                        listOfBlocks = blocks;
                    }

                    LayoutBlock getStartBlock() {
                        return srcStart;
                    }

                    LayoutBlock getProtectingBlock() {
                        return srcProtecting;
                    }

                    LayoutBlock getDestinationBlock() {
                        return destination;
                    }

                    List<LayoutBlock> listOfBlocks = new ArrayList<>(0);
                    String errorMessage = "";

                    List<LayoutBlock> getListOfBlocks() {
                        return listOfBlocks;
                    }

                    void setErrorMessage(String msg) {
                        errorMessage = msg;
                    }

                    String getErrorMessage() {
                        return errorMessage;
                    }
                }
                List<BestPath> pathList = new ArrayList<BestPath>(2);
                LayoutBlock protectLBlock;
                LayoutBlock destinationLBlock;
                //Need to work out around here the best one.
                for (LayoutBlock srcProLBlock : src.getSourceProtecting()) {
                    protectLBlock = srcProLBlock;
                    if (!reverseDirection) {
                        //We have a problem, the destination point is already setup with a route, therefore we would need to
                        //check some how that a route hasn't been set to it.
                        destinationLBlock = getFacing();
                        List<LayoutBlock> blocks = new ArrayList<>();
                        String errorMessage = null;
                        try {
                            blocks = InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).getLayoutBlockConnectivityTools().getLayoutBlocks(startlBlock, destinationLBlock, protectLBlock, false, 0x00/*jmri.jmrit.display.layoutEditor.LayoutBlockManager.MASTTOMAST*/);
                        } catch (Exception e) {
                            errorMessage = e.getMessage();
                            //can be considered normal if no free route is found
                        }
                        BestPath toadd = new BestPath(startlBlock, protectLBlock, destinationLBlock, blocks);
                        toadd.setErrorMessage(errorMessage);
                        pathList.add(toadd);
                    } else {
                        startlBlock = srcProLBlock;
                        protectLBlock = getFacing();

                        destinationLBlock = src.getStart();
                        if (log.isDebugEnabled()) {
                            log.debug("reverse set destination is set going for " + startlBlock.getDisplayName() + " " + destinationLBlock.getDisplayName() + " " + protectLBlock.getDisplayName());  // NOI18N
                        }
                        try {
                            LayoutBlock srcPro = src.getSourceProtecting().get(0);  //Don't care what block the facing is protecting
                            //Need to add a check for the lengths of the returned lists, then choose the most appropriate
                            if (!InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).getLayoutBlockConnectivityTools().checkValidDest(startlBlock, protectLBlock, srcPro, src.getStart(), LayoutBlockConnectivityTools.SENSORTOSENSOR)) {
                                startlBlock = getFacing();
                                protectLBlock = srcProLBlock;
                                if (log.isDebugEnabled()) {
                                    log.debug("That didn't work so try  " + startlBlock.getDisplayName() + " " + destinationLBlock.getDisplayName() + " " + protectLBlock.getDisplayName());  // NOI18N
                                }
                                if (!InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).getLayoutBlockConnectivityTools().checkValidDest(startlBlock, protectLBlock, srcPro, src.getStart(), LayoutBlockConnectivityTools.SENSORTOSENSOR)) {
                                    log.error("No route found");  // NOI18N
                                    JOptionPane.showMessageDialog(null, "No Valid path found");  // NOI18N
                                    src.pd.setNXButtonState(EntryExitPairs.NXBUTTONINACTIVE);
                                    point.setNXButtonState(EntryExitPairs.NXBUTTONINACTIVE);
                                    return;
                                } else {
                                    List<LayoutBlock> blocks = new ArrayList<>();
                                    String errorMessage = null;
                                    try {
                                        blocks = InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).getLayoutBlockConnectivityTools().getLayoutBlocks(startlBlock, destinationLBlock, protectLBlock, false, 0x00/*jmri.jmrit.display.layoutEditor.LayoutBlockManager.MASTTOMAST*/);
                                    } catch (Exception e) {
                                        errorMessage = e.getMessage();
                                        //can be considered normal if no free route is found
                                    }
                                    BestPath toadd = new BestPath(startlBlock, protectLBlock, destinationLBlock, blocks);
                                    toadd.setErrorMessage(errorMessage);
                                    pathList.add(toadd);
                                }
                            } else if (InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).getLayoutBlockConnectivityTools().checkValidDest(getFacing(), srcProLBlock, srcPro, src.getStart(), LayoutBlockConnectivityTools.SENSORTOSENSOR)) {
                                //Both paths are valid, so will go for setting the shortest
                                int distance = startlBlock.getBlockHopCount(destinationLBlock.getBlock(), protectLBlock.getBlock());
                                int distance2 = getFacing().getBlockHopCount(destinationLBlock.getBlock(), srcProLBlock.getBlock());
                                if (distance > distance2) {
                                    //The alternative route is shorter we shall use that
                                    startlBlock = getFacing();
                                    protectLBlock = srcProLBlock;
                                }
                                List<LayoutBlock> blocks = new ArrayList<>();
                                String errorMessage = "";
                                try {
                                    blocks = InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).getLayoutBlockConnectivityTools().getLayoutBlocks(startlBlock, destinationLBlock, protectLBlock, false, jmri.jmrit.display.layoutEditor.LayoutBlockConnectivityTools.NONE);
                                } catch (Exception e) {
                                    //can be considered normal if no free route is found
                                    errorMessage = e.getMessage();
                                }
                                BestPath toadd = new BestPath(startlBlock, protectLBlock, destinationLBlock, blocks);
                                toadd.setErrorMessage(errorMessage);
                                pathList.add(toadd);
                            } else {
                                List<LayoutBlock> blocks = new ArrayList<>();
                                String errorMessage = "";
                                try {
                                    blocks = InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).getLayoutBlockConnectivityTools().getLayoutBlocks(startlBlock, destinationLBlock, protectLBlock, false, jmri.jmrit.display.layoutEditor.LayoutBlockConnectivityTools.NONE);
                                } catch (Exception e) {
                                    //can be considered normal if no free route is found
                                    errorMessage = e.getMessage();
                                }
                                BestPath toadd = new BestPath(startlBlock, protectLBlock, destinationLBlock, blocks);
                                toadd.setErrorMessage(errorMessage);
                                pathList.add(toadd);
                            }
                        } catch (jmri.JmriException ex) {
                            log.error("Exception " + ex.getMessage());  // NOI18N
                            if (showMessage) {
                                JOptionPane.showMessageDialog(null, ex.getMessage());
                            }
                            src.pd.setNXButtonState(EntryExitPairs.NXBUTTONINACTIVE);
                            point.setNXButtonState(EntryExitPairs.NXBUTTONINACTIVE);
                            return;
                        }
                    }
                }
                if (pathList.isEmpty()) {
                    log.debug("Path list empty so exiting");  // NOI18N
                    return;
                }
                BestPath pathToUse = null;
                if (pathList.size() == 1) {
                    if (!pathList.get(0).getListOfBlocks().isEmpty()) {
                        pathToUse = pathList.get(0);
                    }
                } else {
                    /*Need to filter out the remaining routes, in theory this should only ever be two.
                     We simply pick at this stage the one with the least number of blocks as being preferred.
                     This could be expanded at some stage to look at either the length or the metric*/
                    int noOfBlocks = 0;
                    for (BestPath bp : pathList) {
                        if (!bp.getListOfBlocks().isEmpty()) {
                            if (noOfBlocks == 0 || bp.getListOfBlocks().size() < noOfBlocks) {
                                noOfBlocks = bp.getListOfBlocks().size();
                                pathToUse = bp;
                            }
                        }
                    }
                }
                if (pathToUse == null) {
                    //No valid paths found so will quit
                    if (pathList.get(0).getListOfBlocks().isEmpty()) {
                        if (showMessage) {
                            //Considered normal if not a valid through path, provide an option to stack
                            handleNoCurrentRoute(reverseDirection, pathList.get(0).getErrorMessage());
                            src.pd.setNXButtonState(EntryExitPairs.NXBUTTONINACTIVE);
                            point.setNXButtonState(EntryExitPairs.NXBUTTONINACTIVE);
                        }
                        return;
                    }
                    pathToUse = pathList.get(0);
                }
                startlBlock = pathToUse.getStartBlock();
                protectLBlock = pathToUse.getProtectingBlock();
                destinationLBlock = pathToUse.getDestinationBlock();
                routeDetails = pathToUse.getListOfBlocks();

                if (log.isDebugEnabled()) {
                    log.debug("Path chosen start = {}, dest = {}, protect = {}", startlBlock.getDisplayName(),  // NOI18N
                            destinationLBlock.getDisplayName(), protectLBlock.getDisplayName());
                }
                synchronized (this) {
                    destination = destinationLBlock;
                }

                if (log.isDebugEnabled()) {
                    log.debug("Route details:");
                    for (LayoutBlock blk : routeDetails) {
                        log.debug("  {}", blk.getDisplayName());
                    }
                }

                if (getEntryExitType() == EntryExitPairs.FULLINTERLOCK) {
                    setActiveEntryExit(true);
                }
                setRoute(true);
            }
        }
    }

    void handleNoCurrentRoute(boolean reverse, String message) {
        Object[] options = {Bundle.getMessage("ButtonYes"), // NOI18N
            Bundle.getMessage("ButtonNo")};  // NOI18N
        int n = JOptionPane.showOptionDialog(null,
                message + "\n" + Bundle.getMessage("StackRouteAsk"), Bundle.getMessage("RouteNotClear"), // NOI18N
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[1]);
        if (n == 0) {
            manager.stackNXRoute(this, reverse);
            firePropertyChange("stacked", null, null);  // NOI18N
        } else {
            firePropertyChange("failed", null, null);  // NOI18N
        }
    }

    @Override
    public void dispose() {
        enabled = false;
        setActiveEntryExit(false);
        cancelClearInterlock(EntryExitPairs.CANCELROUTE);
        setRouteFrom(false);
        setRouteTo(false);
        point.removeDestination(this);
        synchronized (this) {
            lastSeenActiveBlockObject = null;
        }
        disposed = true;
    }

    @Override
    public int getState() {
        if (activeEntryExit) {
            return 0x02;
        }
        return 0x04;
    }

    public boolean isActive() {
        return activeEntryExit;
    }

    @Override
    public void setState(int state) {
    }

    void setActiveEntryExit(boolean boo) {
        int oldvalue = getState();
        activeEntryExit = boo;
        src.setMenuEnabled(boo);
        firePropertyChange("active", oldvalue, getState());  // NOI18N

    }

    private final static Logger log = LoggerFactory.getLogger(DestinationPoints.class);

}
