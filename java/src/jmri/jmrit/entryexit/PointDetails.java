package jmri.jmrit.entryexit;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.Sensor;
import jmri.SignalHead;
import jmri.SignalMast;
import jmri.jmrit.display.PanelMenu;
import jmri.jmrit.display.SensorIcon;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.layoutEditor.LayoutSlip;
import jmri.jmrit.display.layoutEditor.LayoutTurnout;
import jmri.jmrit.display.layoutEditor.LevelXing;
import jmri.jmrit.display.layoutEditor.PositionablePoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PointDetails {

    //May want to look at putting a listener on the refLoc to listen to updates to blocks, signals and sensors attached to it
    LayoutEditor panel = null;
    LayoutBlock facing;
    List<LayoutBlock> protectingBlocks;
    private NamedBean refObj;
    private Object refLoc;
    private Sensor sensor;
    private SignalMast signalmast;
    private SignalHead signalhead;

    static int nxButtonTimeout = 10;

    Source sourceRoute;
    transient Hashtable<DestinationPoints, Source> destinations = new Hashtable<DestinationPoints, Source>(5);

    public PointDetails(LayoutBlock facing, List<LayoutBlock> protecting) {
        this.facing = facing;
        this.protectingBlocks = protecting;
    }

    public LayoutBlock getFacing() {
        return facing;
    }

    public List<LayoutBlock> getProtecting() {
        return protectingBlocks;
    }

    //This might be better off a ref to the source pointdetail.
    boolean routeToSet = false;

    void setRouteTo(boolean boo) {
        routeToSet = boo;
    }

    boolean routeFromSet = false;

    void setRouteFrom(boolean boo) {
        routeFromSet = boo;
    }

    public void setPanel(LayoutEditor panel) {
        this.panel = panel;
        // find the panel that actually contains this sensor, default to the supplied panel
        for (LayoutEditor layout : InstanceManager.getDefault(PanelMenu.class).getLayoutEditorPanelList()) {
            for (SensorIcon si : layout.sensorList) {
                if (sensor == si.getNamedBean()) {
                    this.panel = layout;
                    return;
                }
            }
        }
    }

    void setSensor(Sensor sen) {
        if (sensor == sen) {
            return;
        }
        if (sensor != null) {
            sensor.removePropertyChangeListener(nxButtonListener);
        }
        sensor = sen;
        if (sensor != null) {
            sensor.addPropertyChangeListener(nxButtonListener);
        }
    }

    void addSensorList() {
        sensor.addPropertyChangeListener(nxButtonListener);
    }

    void removeSensorList() {
        sensor.removePropertyChangeListener(nxButtonListener);
    }

    //Sensor getSensor() { return sensor; }
    protected PropertyChangeListener nxButtonListener = new PropertyChangeListener() {
        //First off if we were inactive, and now active
        @Override
        public void propertyChange(PropertyChangeEvent e) {
            nxButtonStateChange(e);
        }
    };

    private void nxButtonStateChange(PropertyChangeEvent e) {
        if (!e.getPropertyName().equals("KnownState")) {  // NOI18N
            return;
        }
        int now = ((Integer) e.getNewValue()).intValue();
        int old = ((Integer) e.getOldValue()).intValue();

        if ((old == Sensor.UNKNOWN) || (old == Sensor.INCONSISTENT)) {
            setButtonState(EntryExitPairs.NXBUTTONINACTIVE);
            return;
        }

        DestinationPoints destPoint = null;

        for (Entry<DestinationPoints, Source> dp : destinations.entrySet()) {
            destPoint = dp.getKey();
            if (destPoint.isEnabled() && dp.getValue().getPoint().getNXState() == EntryExitPairs.NXBUTTONSELECTED) {
                setButtonState(EntryExitPairs.NXBUTTONSELECTED);
                destPoint.activeBean(false);
                return;
            }
        }

        if (sourceRoute != null) {
            if (now == Sensor.ACTIVE && getNXState() == EntryExitPairs.NXBUTTONINACTIVE) {
                setButtonState(EntryExitPairs.NXBUTTONSELECTED);
                for (Entry<PointDetails, DestinationPoints> en : sourceRoute.pointToDest.entrySet()) {
                    //Sensor sen = getSensorFromPoint(en.getKey().getPoint());
                    //Set a time out on the source sensor, so that if its state hasn't been changed, then we will clear it out.
                    if (en.getValue().isEnabled() && !en.getValue().getUniDirection()) {
                        if (en.getKey().getNXState() == EntryExitPairs.NXBUTTONSELECTED) {
                            sourceRoute.activeBean(en.getValue(), true);
                        }
                    }
                }
            } else if (now == Sensor.INACTIVE && getNXState() == EntryExitPairs.NXBUTTONSELECTED) {
                //sensor inactive, nxbutton state was selected, going to set back to inactive - ie user cancelled button
                setButtonState(EntryExitPairs.NXBUTTONINACTIVE);
            } else if (now == Sensor.INACTIVE && getNXState() == EntryExitPairs.NXBUTTONACTIVE) {
                //Sensor gone inactive, while nxbutton was selected - potential start of user either clear route or setting another
                setButtonState(EntryExitPairs.NXBUTTONSELECTED);
                for (Entry<PointDetails, DestinationPoints> en : sourceRoute.pointToDest.entrySet()) {
                    //Sensor sen = getSensorFromPoint(en.getKey().getPoint());
                    //Set a time out on the source sensor, so that if its state hasn't been changed, then we will clear it out.
                    if (en.getValue().isEnabled() && !en.getValue().getUniDirection()) {
                        if (en.getKey().getNXState() == EntryExitPairs.NXBUTTONSELECTED) {
                            sourceRoute.activeBean(en.getValue(), false);
                        }
                    }
                }
            }
        } else if (destPoint != null) {
            //Button set as a destination but has no source, it has had a change in state
            if (now == Sensor.ACTIVE) {
                //State now is Active will set flashing
                setButtonState(EntryExitPairs.NXBUTTONSELECTED);
            } else if (getNXState() == EntryExitPairs.NXBUTTONACTIVE) {
                //Sensor gone inactive while it was previosly active
                setButtonState(EntryExitPairs.NXBUTTONSELECTED);
            } else if (getNXState() == EntryExitPairs.NXBUTTONSELECTED) {
                //Sensor gone inactive while it was previously selected therefore will cancel
                setButtonState(EntryExitPairs.NXBUTTONINACTIVE);
            }
        }
        jmri.InstanceManager.getDefault(jmri.jmrit.entryexit.EntryExitPairs.class).setMultiPointRoute(this, panel);
    }

    void setSignalMast(SignalMast mast) {
        signalmast = mast;
    }

    void setSource(Source src) {
        if (sourceRoute == src) {
            return;
        }
        sourceRoute = src;
    }

    void setDestination(DestinationPoints srcdp, Source src) {
        if (!destinations.containsKey(srcdp)) {
            destinations.put(srcdp, src);
        }
    }

    void removeDestination(DestinationPoints srcdp) {
        destinations.remove(srcdp);
        if (sourceRoute == null && destinations.size() == 0) {
            stopFlashSensor();
            sensor.removePropertyChangeListener(nxButtonListener);
            setSensor(null);
        }
    }

    void removeSource(Source src) {
        sourceRoute = null;
        if (destinations.size() == 0) {
            stopFlashSensor();
            setSensor(null);
        }
    }

    private int nxButtonState = EntryExitPairs.NXBUTTONINACTIVE;

    void setButtonState(int state) {
        setNXButtonState(state);
    }

    void setNXState(int state) {
        if (state == nxButtonState) {
            return;
        }
        if (state == EntryExitPairs.NXBUTTONSELECTED) {
            nxButtonTimeOut();
            flashSensor();
        } else {
            cancelNXButtonTimeOut();
            stopFlashSensor();
        }
        nxButtonState = state;
    }

    public int getNXState() {
        return nxButtonState;
    }

    SignalMast getSignalMast() {
        return signalmast;
    }

    void setSignalHead(SignalHead head) {
        signalhead = head;
    }

    SignalHead getSignalHead() {
        return signalhead;
    }

    public LayoutEditor getPanel() {
        return panel;
    }

    public void setRefObject(NamedBean refObs) {
        List<LayoutEditor> panels = InstanceManager.getDefault(jmri.jmrit.display.PanelMenu.class).
                getLayoutEditorPanelList();
        for (LayoutEditor pnl : panels) {
            if (refLoc == null) {
                setRefObjectByPanel(refObs, pnl);
            }
        }
    }

    public void setRefObjectByPanel(NamedBean refObs, LayoutEditor pnl) {
        refObj = refObs;
        if (pnl != null && refObj != null) {
            if (refObj instanceof SignalMast || refObj instanceof Sensor) {
                //String mast = ((SignalMast)refObj).getUserName();
                refLoc = pnl.getFinder().findPositionablePointByEastBoundBean(refObj);
                if (refLoc == null) {
                    refLoc = pnl.getFinder().findPositionablePointByWestBoundBean(refObj);
                }
                if (refLoc == null) {
                    refLoc = pnl.getFinder().findLayoutTurnoutByBean(refObj);
                }
                if (refLoc == null) {
                    refLoc = pnl.getFinder().findLevelXingByBean(refObj);
                }
                if (refLoc == null) {
                    refLoc = pnl.getFinder().findLayoutSlipByBean(refObj);
                }
                if (refObj instanceof Sensor) {
                    setSensor((Sensor) refObj);
                }
            } else if (refObj instanceof SignalHead) {
                String signal = ((SignalHead) refObj).getDisplayName();
                refLoc = pnl.getFinder().findPositionablePointByEastBoundSignal(signal);
                if (refLoc == null) {
                    refLoc = pnl.getFinder().findPositionablePointByWestBoundSignal(signal);
                }
            }
        }
//        if (refLoc != null) {
//            if (refLoc instanceof PositionablePoint) {
//                //((PositionablePoint)refLoc).addPropertyChangeListener(this);
//            } else if (refLoc instanceof LayoutTurnout) {  //<== this includes LayoutSlips
//                //((LayoutTurnout)refLoc).addPropertyChangeListener(this);
//            } else if (refLoc instanceof LevelXing) {
//                //((LevelXing)refLoc).addPropertyChangeListener(this);
//            }
//        }
        //With this set ref we can probably add a listener to it, so that we can detect when a change to the point details takes place
    }

    public NamedBean getRefObject() {
        return refObj;
    }

    public Object getRefLocation() {
        return refLoc;
    }

    //LayoutEditor getLayoutEditor() { return panel; }
    public boolean isRouteToPointSet() {
        return routeToSet;
    }

    public boolean isRouteFromPointSet() {
        return routeFromSet;
    }

    public String getDisplayName() {
        if (sensor != null) {
            String description = sensor.getDisplayName();
            if (signalmast != null) {
                description = description + " (" + signalmast.getDisplayName() + ")";
            }
            return description;
        }

        if (refObj instanceof SignalMast) {
            return ((SignalMast) refObj).getDisplayName();
        } else if (refObj instanceof Sensor) {
            return ((Sensor) refObj).getDisplayName();
        } else if (refObj instanceof SignalHead) {
            return ((SignalHead) refObj).getDisplayName();
        }
        return "no display name";  // NOI18N
    }

    transient Thread nxButtonTimeOutThr;

    void nxButtonTimeOut() {
        if ((nxButtonTimeOutThr != null) && (nxButtonTimeOutThr.isAlive())) {
            return;
        }
        extendedtime = true;
        class ButtonTimeOut implements Runnable {

            ButtonTimeOut() {
            }

            @Override
            public void run() {
                try {
                    //Stage one default timer for the button if no other button has been pressed
                    Thread.sleep(nxButtonTimeout * 1000L);
                    //Stage two if an extended time out has been requested
                    if (extendedtime) {
                        Thread.sleep(60000);  //timeout after a minute waiting for the sml to set.
                    }
                } catch (InterruptedException ex) {
                    log.debug("Flash timer cancelled");  // NOI18N
                }
                setNXButtonState(EntryExitPairs.NXBUTTONINACTIVE);
            }
        }
        ButtonTimeOut t = new ButtonTimeOut();
        nxButtonTimeOutThr = new Thread(t, "NX Button Timeout " + getSensor().getDisplayName());  // NOI18N

        nxButtonTimeOutThr.start();
    }

    void cancelNXButtonTimeOut() {
        if (nxButtonTimeOutThr != null) {
            nxButtonTimeOutThr.interrupt();
        }

    }

    boolean extendedtime = false;

    public void flashSensor() {
        for (SensorIcon si : getPanel().sensorList) {
            if (si.getSensor() == getSensor()) {
                si.flashSensor(2, Sensor.ACTIVE, Sensor.INACTIVE);
            }
        }
    }

    public void stopFlashSensor() {
        for (SensorIcon si : getPanel().sensorList) {
            if (si.getSensor() == getSensor()) {
                si.stopFlash();
            }
        }
    }

    synchronized public void setNXButtonState(int state) {
        if (getSensor() == null) {
            return;
        }
        if (state == EntryExitPairs.NXBUTTONINACTIVE) {
            //If a route is set to or from out point then we need to leave/set the sensor to ACTIVE
            if (isRouteToPointSet()) {
                state = EntryExitPairs.NXBUTTONACTIVE;
            } else if (isRouteFromPointSet()) {
                state = EntryExitPairs.NXBUTTONACTIVE;
            }
        }
        setNXState(state);
        int sensorState = Sensor.UNKNOWN;
        switch (state) {
            case EntryExitPairs.NXBUTTONINACTIVE:
                sensorState = Sensor.INACTIVE;
                break;
            case EntryExitPairs.NXBUTTONACTIVE:
                sensorState = Sensor.ACTIVE;
                break;
            case EntryExitPairs.NXBUTTONSELECTED:
                sensorState = Sensor.ACTIVE;
                break;
            default:
                sensorState = Sensor.UNKNOWN;
                break;
        }

        //Might need to clear listeners at the stage and then reapply them after.
        if (getSensor().getKnownState() != sensorState) {
            removeSensorList();
            try {
                getSensor().setKnownState(sensorState);
            } catch (jmri.JmriException ex) {
                log.error(ex.getLocalizedMessage(), ex);
            }
            addSensorList();
        }
    }

    /**
     * @since 4.17.6
     * Making the source object available for scripting in Jython.
     */
    public Sensor getSensor() {
        if (getRefObject() == null) {
            return null;
        }
        if ((getPanel() != null) && (!getPanel().isEditable()) && (sensor != null)) {
            return sensor;
        }

        if (getRefObject() instanceof Sensor) {
            setSensor((Sensor) getRefObject());
            return (Sensor) getRefObject();
        }
        Object objLoc = getRefLocation();
        Object objRef = getRefObject();
        SignalHead head = null;
        SignalMast mast = null;
        String username = "";
        String systemname = "";
        Sensor foundSensor = null;
        if (objRef instanceof SignalMast) {
            mast = (SignalMast) objRef;
        }
        if (objRef instanceof SignalHead) {
            head = (SignalHead) objRef;
            username = head.getUserName();
            systemname = head.getSystemName();
        }
        jmri.SensorManager sm = InstanceManager.sensorManagerInstance();
        if (objLoc instanceof PositionablePoint) {
            PositionablePoint p = (PositionablePoint) objLoc;
            if (mast != null) {
                if (p.getEastBoundSignalMast() == objRef) {
                    foundSensor = p.getEastBoundSensor();
                } else if (p.getWestBoundSignalMast() == objRef) {
                    foundSensor = p.getWestBoundSensor();
                }
            } else if (head != null) {
                if ((p.getEastBoundSignal().equals(username))
                        || p.getEastBoundSignal().equals(systemname)) {
                    foundSensor = p.getEastBoundSensor();
                } else if ((p.getWestBoundSignal().equals(username))
                        || p.getWestBoundSignal().equals(systemname)) {
                    foundSensor = p.getWestBoundSensor();
                }
            }
        } else if (objLoc instanceof LayoutSlip) {
            LayoutSlip sl = (LayoutSlip) objLoc;
            if (mast != null) {
                if (sl.getSignalAMast() == objRef) {
                    foundSensor = sl.getSensorA();
                } else if (sl.getSignalBMast() == objRef) {
                    foundSensor = sl.getSensorB();
                } else if (sl.getSignalCMast() == objRef) {
                    foundSensor = sl.getSensorC();
                } else if (sl.getSignalDMast() == objRef) {
                    foundSensor = sl.getSensorD();
                }
            }
            if (head != null) {
                if ((sl.getSignalA1Name().equals(username)) || (sl.getSignalA1Name().equals(systemname))) {
                    foundSensor = sm.getSensor(sl.getSensorAName());
                } else if ((sl.getSignalB1Name().equals(username)) || (sl.getSignalB1Name().equals(systemname))) {
                    foundSensor = sm.getSensor(sl.getSensorBName());
                } else if ((sl.getSignalC1Name().equals(username)) || (sl.getSignalC1Name().equals(systemname))) {
                    foundSensor = sm.getSensor(sl.getSensorCName());
                } else if ((sl.getSignalD1Name().equals(username)) || (sl.getSignalD1Name().equals(systemname))) {
                    foundSensor = sm.getSensor(sl.getSensorDName());
                }
            }
        } else //note: you have to do this after LayoutSlip
        // because LayoutSlip extends LayoutTurnout
        // (So a LayoutSlip would be an instance of LayoutTurnout.)
        if (objLoc instanceof LayoutTurnout) {  //<== this includes LayoutSlips
            LayoutTurnout t = (LayoutTurnout) objLoc;
            if (mast != null) {
                if (t.getSignalAMast() == objRef) {
                    foundSensor = t.getSensorA();
                } else if (t.getSignalBMast() == objRef) {
                    foundSensor = t.getSensorB();
                } else if (t.getSignalCMast() == objRef) {
                    foundSensor = t.getSensorC();
                } else if (t.getSignalDMast() == objRef) {
                    foundSensor = t.getSensorD();
                }
            }
            if (head != null) {
                if ((t.getSignalA1Name().equals(username)) || (t.getSignalA1Name().equals(systemname))) {
                    foundSensor = t.getSensorA();
                } else if ((t.getSignalA2Name().equals(username)) || (t.getSignalA2Name().equals(systemname))) {
                    foundSensor = t.getSensorA();
                } else if ((t.getSignalA3Name().equals(username)) || (t.getSignalA3Name().equals(systemname))) {
                    foundSensor = t.getSensorA();
                } else if ((t.getSignalB1Name().equals(username)) || (t.getSignalB1Name().equals(systemname))) {
                    foundSensor = t.getSensorB();
                } else if ((t.getSignalB2Name().equals(username)) || (t.getSignalB2Name().equals(systemname))) {
                    foundSensor = t.getSensorB();
                } else if ((t.getSignalC1Name().equals(username)) || (t.getSignalC1Name().equals(systemname))) {
                    foundSensor = t.getSensorC();
                } else if ((t.getSignalC2Name().equals(username)) || (t.getSignalC2Name().equals(systemname))) {
                    foundSensor = t.getSensorC();
                } else if ((t.getSignalD1Name().equals(username)) || (t.getSignalD1Name().equals(systemname))) {
                    foundSensor = t.getSensorD();
                } else if ((t.getSignalD2Name().equals(username)) || (t.getSignalD2Name().equals(systemname))) {
                    foundSensor = t.getSensorD();
                }
            }
        } else if (objLoc instanceof LevelXing) {
            LevelXing x = (LevelXing) objLoc;
            if (mast != null) {
                if (x.getSignalAMast() == objRef) {
                    foundSensor = x.getSensorA();
                } else if (x.getSignalBMast() == objRef) {
                    foundSensor = x.getSensorB();
                } else if (x.getSignalCMast() == objRef) {
                    foundSensor = x.getSensorC();
                } else if (x.getSignalDMast() == objRef) {
                    foundSensor = x.getSensorD();
                }
            }
            if (head != null) {
                if ((x.getSignalAName().equals(username)) || (x.getSignalAName().equals(systemname))) {
                    foundSensor = x.getSensorA();
                } else if ((x.getSignalBName().equals(username)) || (x.getSignalBName().equals(systemname))) {
                    foundSensor = x.getSensorB();
                } else if ((x.getSignalCName().equals(username)) || (x.getSignalCName().equals(systemname))) {
                    foundSensor = x.getSensorC();
                } else if ((x.getSignalDName().equals(username)) || (x.getSignalDName().equals(systemname))) {
                    foundSensor = x.getSensorD();
                }
            }
        }
        setSensor(foundSensor);
        return foundSensor;
    }

    NamedBean getSignal() {
        if ((getPanel() != null) && (!getPanel().isEditable()) && (getSignalMast() != null)) {
            return getSignalMast();
        }
        if ((getPanel() != null) && (!getPanel().isEditable()) && (getSignalHead() != null)) {
            return getSignalHead();
        }
        jmri.SignalHeadManager sh = InstanceManager.getDefault(jmri.SignalHeadManager.class);
        NamedBean signal = null;

        if (getRefObject() == null) {
            log.error("Signal not found at point");  // NOI18N
            return null;
        } else if (getRefObject() instanceof SignalMast) {
            signal = getRefObject();
            setSignalMast(((SignalMast) getRefObject()));
            return signal;
        } else if (getRefObject() instanceof SignalHead) {
            signal = getRefObject();
            setSignalHead(((SignalHead) getRefObject()));
            return signal;
        }

        Sensor sen = (Sensor) getRefObject();
        log.debug("  Looking at sensor '{}' on panel '{}' at '{}'",
                sen.getDisplayName(), getPanel().getLayoutName(), getRefLocation());
        if (getRefLocation() instanceof PositionablePoint) {
            PositionablePoint p = (PositionablePoint) getRefLocation();
            if (p.getEastBoundSensor() == sen) {
                if (p.getEastBoundSignalMast() != null) {
                    signal = p.getEastBoundSignalMast();
                } else if (!p.getEastBoundSignal().equals("")) {
                    signal = sh.getSignalHead(p.getEastBoundSignal());
                }
            } else if (p.getWestBoundSensor() == sen) {
                if (p.getWestBoundSignalMast() != null) {
                    signal = p.getWestBoundSignalMast();
                } else if (!p.getWestBoundSignal().equals("")) {
                    signal = sh.getSignalHead(p.getWestBoundSignal());
                }
            }
        } else if (getRefLocation() instanceof LayoutSlip) {
            LayoutSlip t = (LayoutSlip) getRefLocation();
            if (t.getSensorA() == sen) {
                if (t.getSignalAMast() != null) {
                    signal = t.getSignalAMast();
                } else if (!t.getSignalA1Name().equals("")) {
                    signal = sh.getSignalHead(t.getSignalA1Name());
                }
            } else if (t.getSensorB() == sen) {
                if (t.getSignalBMast() != null) {
                    signal = t.getSignalBMast();
                } else if (!t.getSignalB1Name().equals("")) {
                    signal = sh.getSignalHead(t.getSignalB1Name());
                }
            } else if (t.getSensorC() == sen) {
                if (t.getSignalCMast() != null) {
                    signal = t.getSignalCMast();
                } else if (!t.getSignalC1Name().equals("")) {
                    signal = sh.getSignalHead(t.getSignalC1Name());
                }
            } else if (t.getSensorD() == sen) {
                if (t.getSignalDMast() != null) {
                    signal = t.getSignalDMast();
                } else if (!t.getSignalD1Name().equals("")) {
                    signal = sh.getSignalHead(t.getSignalD1Name());
                }
            }
        } else //note: you have to do this after LayoutSlip
        // because LayoutSlip extends LayoutTurnout
        // (So a LayoutSlip would be an instance of LayoutTurnout.)
        if (getRefLocation() instanceof LayoutTurnout) {  //<== this includes LayoutSlips
            LayoutTurnout t = (LayoutTurnout) getRefLocation();
            if (t.getSensorA() == sen) {
                if (t.getSignalAMast() != null) {
                    signal = t.getSignalAMast();
                } else if (!t.getSignalA1Name().equals("")) {
                    signal = sh.getSignalHead(t.getSignalA1Name());
                }
            } else if (t.getSensorB() == sen) {
                if (t.getSignalBMast() != null) {
                    signal = t.getSignalBMast();
                } else if (!t.getSignalB1Name().equals("")) {
                    signal = sh.getSignalHead(t.getSignalB1Name());
                }
            } else if (t.getSensorC() == sen) {
                if (t.getSignalCMast() != null) {
                    signal = t.getSignalCMast();
                } else if (!t.getSignalC1Name().equals("")) {
                    signal = sh.getSignalHead(t.getSignalC1Name());
                }
            } else if (t.getSensorD() == sen) {
                if (t.getSignalDMast() != null) {
                    signal = t.getSignalDMast();
                } else if (!t.getSignalD1Name().equals("")) {
                    signal = sh.getSignalHead(t.getSignalD1Name());
                }
            }
        } else if (getRefLocation() instanceof LevelXing) {
            LevelXing x = (LevelXing) getRefLocation();
            if (x.getSensorA() == sen) {
                if (x.getSignalAMast() != null) {
                    signal = x.getSignalAMast();
                } else if (!x.getSignalAName().equals("")) {
                    signal = sh.getSignalHead(x.getSignalAName());
                }
            } else if (x.getSensorB() == sen) {
                if (x.getSignalBMast() != null) {
                    signal = x.getSignalBMast();
                } else if (!x.getSignalBName().equals("")) {
                    signal = sh.getSignalHead(x.getSignalBName());
                }
            } else if (x.getSensorC() == sen) {
                if (x.getSignalCMast() != null) {
                    signal = x.getSignalCMast();
                } else if (!x.getSignalCName().equals("")) {
                    signal = sh.getSignalHead(x.getSignalCName());
                }
            } else if (x.getSensorD() == sen) {
                if (x.getSignalDMast() != null) {
                    signal = x.getSignalDMast();
                } else if (!x.getSignalDName().equals("")) {
                    signal = sh.getSignalHead(x.getSignalDName());
                }
            }
        }

        if (signal instanceof SignalMast) {
            setSignalMast(((SignalMast) signal));
        } else if (signal instanceof SignalHead) {
            setSignalHead(((SignalHead) signal));
        }
        return signal;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }

        if (!(getClass() == obj.getClass())) {
            return false;
        } else {
            PointDetails tmp = (PointDetails) obj;
            if (tmp.getFacing() != this.facing) {
                return false;
            }
            if (!tmp.getProtecting().equals(this.protectingBlocks)) {
                return false;
            }
            if (tmp.getPanel() != this.panel) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.panel != null ? this.panel.hashCode() : 0);
        hash = 37 * hash + (this.facing != null ? this.facing.hashCode() : 0);
        hash = 37 * hash + (this.protectingBlocks != null ? this.protectingBlocks.hashCode() : 0);
        return hash;
    }

    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    protected void firePropertyChange(String p, Object old, Object n) {
        pcs.firePropertyChange(p, old, n);
    }

    private final static Logger log = LoggerFactory.getLogger(PointDetails.class);
}
