package jmri.jmrit.withrottle;

//  WiThrottle
//
//
/**
 * ThrottleController.java Sends commands to appropriate throttle component.
 * <p>
 * Original version sorting codes for received string from client: 'V'elocity
 * followed by 0 - 126 'X'stop 'F'unction (1-button down, 0-button up) (0-28)
 * e.g. F14 indicates function 4 button is pressed ` F04 indicates function 4
 * button is released di'R'ection (0=reverse, 1=forward) 'L'ong address #,
 * 'S'hort address # e.g. L1234 'r'elease, 'd'ispatch 'C'consist lead address,
 * e.g. CL1235 'I'dle Idle needs to be called specifically 'Q'uit
 * <p>
 * Anything using added codes needs to verify version number for compatibility.
 * Added in v1.7: 'E'ntry from roster, e.g. ESpiffy Loco 'c'consist lead from
 * roster ID, e.g. cSpiffy Loco
 * <p>
 * Added in v2.0: If sent through MultiThrottle 'M' in DeviceServer, earlier
 * versions will automatically ignore these. ('M' code did not exist prior to
 * v2.0, so it will not forward to here) If sent through a 'T' or 'S', need to
 * verify version number for compatibility. 'f' set a function directly.
 * 's'peedStepMode - 1-128, 2-28, 4-27, 8-14 re'q'uest information, add the
 * following: 'V' getSpeedSetting 'R' getIsForward 's' getSpeedStepMode 'm'
 * getF#Momentary for all functions
 *
 *
 * @author Brett Hoffman Copyright (C) 2009, 2010, 2011
 * @author Created by Brett Hoffman on: 8/23/09.
 */
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.Queue;
import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.LocoAddress;
import jmri.SpeedStepMode;
import jmri.ThrottleListener;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThrottleController implements ThrottleListener, PropertyChangeListener {

    DccThrottle throttle;
    DccThrottle functionThrottle;
    RosterEntry rosterLoco = null;
    DccLocoAddress leadAddress;
    char whichThrottle;
    float speedMultiplier;
    protected Queue<Float> lastSentSpeed;
    protected float newSpeed;
    boolean isAddressSet;
    protected ArrayList<ThrottleControllerListener> listeners;
    protected ArrayList<ControllerInterface> controllerListeners;
    boolean useLeadLocoF;
    ConsistFunctionController leadLocoF = null;
    String locoKey = "";

    final boolean isMomF2 = InstanceManager.getDefault(WiThrottlePreferences.class).isUseMomF2();

    public ThrottleController() {
        speedMultiplier = 1.0f / 126.0f;
        lastSentSpeed = new LinkedList<Float>();
    }

    public ThrottleController(char whichThrottleChar, ThrottleControllerListener tcl, ControllerInterface cl) {
        this();
        setWhichThrottle(whichThrottleChar);
        addThrottleControllerListener(tcl);
        addControllerListener(cl);
    }

    public void setWhichThrottle(char c) {
        whichThrottle = c;
    }

    public void addThrottleControllerListener(ThrottleControllerListener l) {
        if (listeners == null) {
            listeners = new ArrayList<>(1);
        }
        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    public void removeThrottleControllerListener(ThrottleControllerListener l) {
        if (listeners == null) {
            return;
        }
        if (listeners.contains(l)) {
            listeners.remove(l);
        }
    }

    /**
     * Add a listener to handle: listener.sendPacketToDevice(message);
     *
     * @param listener handle of listener to add
     *
     */
    public void addControllerListener(ControllerInterface listener) {
        if (controllerListeners == null) {
            controllerListeners = new ArrayList<>(1);
        }
        if (!controllerListeners.contains(listener)) {
            controllerListeners.add(listener);
        }
    }

    public void removeControllerListener(ControllerInterface listener) {
        if (controllerListeners == null) {
            return;
        }
        if (controllerListeners.contains(listener)) {
            controllerListeners.remove(listener);
        }
    }

    /**
     * Receive notification that an address has been released/dispatched
     */
    public void addressRelease() {
        isAddressSet = false;
        jmri.InstanceManager.throttleManagerInstance().releaseThrottle(throttle, this);
        throttle.removePropertyChangeListener(this);
        throttle = null;
        rosterLoco = null;
        sendAddress();
        clearLeadLoco();
        for (int i = 0; i < listeners.size(); i++) {
            ThrottleControllerListener l = listeners.get(i);
            l.notifyControllerAddressReleased(this);
            log.debug("Notify TCListener address released: {}", l.getClass());
        }
    }

    public void addressDispatch() {
        isAddressSet = false;
        jmri.InstanceManager.throttleManagerInstance().dispatchThrottle(throttle, this);
        throttle.removePropertyChangeListener(this);
        throttle = null;
        rosterLoco = null;
        sendAddress();
        clearLeadLoco();
        for (int i = 0; i < listeners.size(); i++) {
            ThrottleControllerListener l = listeners.get(i);
            l.notifyControllerAddressReleased(this);
            log.debug("Notify TCListener address dispatched: {}", l.getClass());
        }
    }

    /**
     * Receive notification that a DccThrottle has been found and is in use.
     *
     * @param t The throttle which has been found
     */
    @Override
    public void notifyThrottleFound(DccThrottle t) {
        if (isAddressSet) {
            log.debug("Throttle: {} is already set. (Found is: {})", getCurrentAddressString(), t.getLocoAddress());
            return;
        }
        if (t != null) {
            throttle = t;
            setFunctionThrottle(throttle);
            throttle.addPropertyChangeListener(this);
            isAddressSet = true;
            log.debug("DccThrottle found for: {}", throttle.getLocoAddress());
        } else {
            log.error("*throttle is null!*");
            return;
        }
        for (int i = 0; i < listeners.size(); i++) {
            ThrottleControllerListener l = listeners.get(i);
            l.notifyControllerAddressFound(this);
            log.debug("Notify TCListener address found: {}", l.getClass());
        }

        if (rosterLoco == null) {
            rosterLoco = findRosterEntry(throttle);
        }

        syncThrottleFunctions(throttle, rosterLoco);

        sendAddress();

        sendFunctionLabels(rosterLoco);

        sendAllFunctionStates(throttle);

        sendCurrentSpeed(throttle);

        sendCurrentDirection(throttle);

        sendSpeedStepMode(throttle);

    }

    @Override
    public void notifyFailedThrottleRequest(LocoAddress address, String reason) {
        log.warn("Throttle request failed for {} because {}.", address, reason);
        for (ThrottleControllerListener l : listeners) {
            l.notifyControllerAddressDeclined(this, (DccLocoAddress) address, reason);
            log.debug("Notify TCListener address declined in-use: {}", l.getClass());
        }
    }
    
    /**
     * {@inheritDoc}
     * @deprecated since 4.15.7; use #notifyDecisionRequired
     */
    @Override
    @Deprecated
    public void notifyStealThrottleRequired(jmri.LocoAddress address) {
        notifyDecisionRequired(address, DecisionType.STEAL);
    }

    /**
     * calls notifyFailedThrottleRequest, Steal Required
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void notifyDecisionRequired(jmri.LocoAddress address, DecisionType question) {
        notifyFailedThrottleRequest(address, "Steal Required");
    }


    /*
     * Current Format:  RPF}|{whichThrottle]\[eventName}|{newValue
     * This format may be used to send multiple function status, for initial values.
     *
     * Event may be from regular throttle or consist throttle, but is handled the same.
     *
     * Bound params: SpeedSteps, IsForward, SpeedSetting, F##, F##Momentary
     */
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        String eventName = event.getPropertyName();
        log.debug("property change: {}", eventName);

        if (eventName.startsWith("F")) {

            if (eventName.contains("Momentary")) {
                return;
            }
            StringBuilder message = new StringBuilder("RPF}|{");
            message.append(whichThrottle);
            message.append("]\\[");
            message.append(eventName);
            message.append("}|{");
            message.append(event.getNewValue());

            for (ControllerInterface listener : controllerListeners) {
                listener.sendPacketToDevice(message.toString());
            }
        }

    }

    public RosterEntry findRosterEntry(DccThrottle t) {
        RosterEntry re = null;
        if (t.getLocoAddress() != null) {
            List<RosterEntry> l = Roster.getDefault().matchingList(null, null, "" + ((DccLocoAddress) t.getLocoAddress()).getNumber(), null, null, null, null);
            if (l.size() > 0) {
                log.debug("Roster Loco found: {}", l.get(0).getDccAddress());
                re = l.get(0);
            }
        }
        return re;
    }

    public void syncThrottleFunctions(DccThrottle t, RosterEntry re) {
        if (re != null) {
            for (int funcNum = 0; funcNum < 29; funcNum++) {
                try {

                    Class<?> partypes[] = {Boolean.TYPE};
                    Method setMomentary = t.getClass().getMethod("setF" + funcNum + "Momentary", partypes);
                    Object data[] = {!(re.getFunctionLockable(funcNum))};

                    setMomentary.invoke(t, data);

                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ea) {
                    log.warn(ea.getLocalizedMessage(), ea);
                }
            }
        }

    }

    public void sendFunctionLabels(RosterEntry re) {

        if (re != null) {
            StringBuilder functionString = new StringBuilder();
            if (whichThrottle == 'S') {
                functionString.append("RS29}|{");
            } else {
                //  I know, it should have been 'RT' but this was before there were two throttles.
                functionString.append("RF29}|{");
            }
            functionString.append(getCurrentAddressString());

            int i;
            for (i = 0; i < 29; i++) {
                functionString.append("]\\[");
                if ((re.getFunctionLabel(i) != null)) {
                    functionString.append(re.getFunctionLabel(i));
                }
            }
            for (ControllerInterface listener : controllerListeners) {
                listener.sendPacketToDevice(functionString.toString());
            }
        }

    }

    /**
     * send all function states, primarily for initial status Current Format:
     * RPF}|{whichThrottle]\[function}|{state]\[function}|{state...
     *
     * @param t throttle to send functions to
     */
    public void sendAllFunctionStates(DccThrottle t) {

        log.debug("Sending state of all functions");
        StringBuilder message = new StringBuilder(buildFStatesHeader());

        try {
            for (int cnt = 0; cnt < 29; cnt++) {
                Method getF = t.getClass().getMethod("getF" + cnt, (Class[]) null);
                message.append("]\\[F");
                message.append(cnt);
                message.append("}|{");
                message.append(getF.invoke(t, (Object[]) null));
            }

        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ea) {
            log.warn(ea.getLocalizedMessage(), ea);
            return;
        }

        for (ControllerInterface listener : controllerListeners) {
            listener.sendPacketToDevice(message.toString());
        }

    }

    protected String buildFStatesHeader() {
        return ("RPF}|{" + whichThrottle);
    }

    synchronized protected void sendCurrentSpeed(DccThrottle t) {
    }

    protected void sendCurrentDirection(DccThrottle t) {
    }

    protected void sendSpeedStepMode(DccThrottle t) {
    }

    protected void sendAllMomentaryStates(DccThrottle t) {
    }

    /**
     * Figure out what the received command means, where it has to go, and
     * translate to a jmri method.
     *
     * @param inPackage The package minus its prefix which steered it here.
     * @return true to keep reading in run loop.
     */
    public boolean sort(String inPackage) {
        if (inPackage.charAt(0) == 'Q') {// If device has Quit.
            shutdownThrottle();
            return false;
        }
        if (isAddressSet) {

            try {
                switch (inPackage.charAt(0)) {
                    case 'V': // Velocity
                        setSpeed(Integer.parseInt(inPackage.substring(1)));

                        break;

                    case 'X':
                        eStop();

                        break;

                    case 'F': // Function

                        handleFunction(inPackage);

                        break;

                    case 'f': //v>=2.0 Force function

                        forceFunction(inPackage.substring(1));

                        break;

                    case 'R': // Direction
                        setDirection(!inPackage.endsWith("0")); // 0 sets to reverse, all others forward
                        break;

                    case 'r': // Release
                        addressRelease();
                        break;

                    case 'd': // Dispatch
                        addressDispatch();
                        break;

                    case 'L': // Set a Long address.
                        addressRelease();
                        int addr = Integer.parseInt(inPackage.substring(1));
                        setAddress(addr, true);
                        break;

                    case 'S': // Set a Short address.
                        addressRelease();
                        addr = Integer.parseInt(inPackage.substring(1));
                        setAddress(addr, false);
                        break;

                    case 'E':       //v>=1.7    Address from RosterEntry
                        addressRelease();
                        requestEntryFromID(inPackage.substring(1));
                        break;

                    case 'C':
                        setLocoForConsistFunctions(inPackage.substring(1));

                        break;

                    case 'c':       //v>=1.7      Consist Lead from RosterEntry
                        setRosterLocoForConsistFunctions(inPackage.substring(1));
                        break;

                    case 'I':
                        idle();
                        break;

                    case 's':       //v>=2.0
                        handleSpeedStepMode(decodeSpeedStepMode(inPackage.substring(1)));
                        break;

                    case 'm':       //v>=2.0
                        handleMomentary(inPackage.substring(1));
                        break;

                    case 'q':       //v>=2.0
                        handleRequest(inPackage.substring(1));
                        break;
                    default:
                        log.warn("Unhandled code: {}", inPackage.charAt(0));
                        break;
                }
            } catch (NullPointerException e) {
                log.warn("No throttle frame to receive: {}", inPackage);
                return false;
            }
            try {    //  Some layout connections cannot handle rapid inputs
                Thread.sleep(20);
            } catch (java.lang.InterruptedException ex) {
            }
        } else {  //  Address not set
            switch (inPackage.charAt(0)) {
                case 'L': // Set a Long address.
                    int addr = Integer.parseInt(inPackage.substring(1));
                    setAddress(addr, true);
                    break;

                case 'S': // Set a Short address.
                    addr = Integer.parseInt(inPackage.substring(1));
                    setAddress(addr, false);
                    break;

                case 'E':       //v>=1.7      Address from RosterEntry
                    requestEntryFromID(inPackage.substring(1));
                    break;

                case 'C':
                    setLocoForConsistFunctions(inPackage.substring(1));

                    break;

                case 'c':       //v>=1.7      Consist Lead from RosterEntry
                    setRosterLocoForConsistFunctions(inPackage.substring(1));
                    break;

                default:
                    break;
            }
        }
        return true;

    }

    private void clearLeadLoco() {
        if (useLeadLocoF) {
            leadLocoF.dispose();
            functionThrottle.removePropertyChangeListener(this);
            if (throttle != null) {
                setFunctionThrottle(throttle);
            }

            leadLocoF = null;
            useLeadLocoF = false;
        }
    }

    public void setFunctionThrottle(DccThrottle t) {
        functionThrottle = t;
        functionThrottle.addPropertyChangeListener(this);
    }

    public void setLocoForConsistFunctions(String inPackage) {
        /*
         *      This is used to control speed and direction on the
         *      consist address, but have functions mapped to lead.
         *      Consist address must be set first!
         */

        leadAddress = new DccLocoAddress(Integer.parseInt(inPackage.substring(1)), (inPackage.charAt(0) != 'S'));
        log.debug("Setting lead loco address: {}, for consist: {}", leadAddress, getCurrentAddressString());
        clearLeadLoco();
        leadLocoF = new ConsistFunctionController(this);
        useLeadLocoF = leadLocoF.requestThrottle(leadAddress);

        if (!useLeadLocoF) {
            log.warn("Lead loco address not available.");
            leadLocoF = null;
        }
    }

    public void setRosterLocoForConsistFunctions(String id) {
        RosterEntry re;
        List<RosterEntry> l = Roster.getDefault().matchingList(null, null, null, null, null, null, id);
        if (l.size() > 0) {
            log.debug("Consist Lead Roster Loco found: {} for ID: {}", l.get(0).getDccAddress(), id);
            re = l.get(0);
            clearLeadLoco();
            leadLocoF = new ConsistFunctionController(this, re);
            useLeadLocoF = leadLocoF.requestThrottle(re.getDccLocoAddress());

            if (!useLeadLocoF) {
                log.warn("Lead loco address not available.");
                leadLocoF = null;
            }
        } else {
            log.debug("No Roster Loco found for: {}", id);
        }
    }

//  Device is quitting or has lost connection
    public void shutdownThrottle() {

        try {
            if (isAddressSet) {
                throttle.setSpeedSetting(0);
                addressRelease();
            }
        } catch (NullPointerException e) {
            log.warn("No throttle to shutdown");
        }
        clearLeadLoco();
    }

    /**
     * handle the conversion from rawSpeed to the float value needed in the
     * DccThrottle
     *
     * @param rawSpeed Value sent from mobile device, range 0 - 126
     */
    synchronized protected void setSpeed(int rawSpeed) {

        float newSpeed = (rawSpeed * speedMultiplier);

        log.debug("raw: {}, NewSpd: {}", rawSpeed, newSpeed);
        while(lastSentSpeed.offer(Float.valueOf(newSpeed))==false){
              log.debug("failed attempting to add speed to queue");
        }
        throttle.setSpeedSetting(newSpeed);
    }

    protected void setDirection(boolean isForward) {
        log.debug("set direction to: {}", (isForward ? "Fwd" : "Rev"));
        throttle.setIsForward(isForward);
    }

    protected void eStop() {
        throttle.setSpeedSetting(-1);
    }

    protected void idle() {
        throttle.setSpeedSetting(0);
    }

    protected void setAddress(int number, boolean isLong) {
        log.debug("setAddress: {}, isLong: {}", number, isLong);
        if (rosterLoco != null) {
            jmri.InstanceManager.throttleManagerInstance().requestThrottle(rosterLoco, this, true);
        } else {
            jmri.InstanceManager.throttleManagerInstance().requestThrottle(new DccLocoAddress(number, isLong), this, true);
            
        }
    }

    public void requestEntryFromID(String id) {
        RosterEntry re;
        List<RosterEntry> l = Roster.getDefault().matchingList(null, null, null, null, null, null, id);
        if (l.size() > 0) {
            log.debug("Roster Loco found: {} for ID: {}", l.get(0).getDccAddress(), id);
            re = l.get(0);
            rosterLoco = re;
            setAddress(Integer.parseInt(re.getDccAddress()), re.isLongAddress());
        } else {
            log.debug("No Roster Loco found for: {}", id);
        }
    }

    public DccThrottle getThrottle() {
        return throttle;
    }

    public DccThrottle getFunctionThrottle() {
        return functionThrottle;
    }

    public DccLocoAddress getCurrentAddress() {
        return (DccLocoAddress) throttle.getLocoAddress();
    }

    /**
     * Get the string representation of this throttles address. Returns 'Not
     * Set' if no address in use.
     *
     * @return string value of throttle address
     */
    public String getCurrentAddressString() {
        if (isAddressSet) {
            return ((DccLocoAddress) throttle.getLocoAddress()).toString();
        } else {
            return "Not Set";
        }
    }

    /**
     * Get the string representation of this Roster ID. Returns empty string 
     * if no address in use.
     * since 4.15.4
     *
     * @return string value of throttle Roster ID
     */
    public String getCurrentRosterIdString() {
        if (rosterLoco != null) {
            return rosterLoco.getId() ;
        } else {
            return " ";
        }
    }

    public void sendAddress() {
        for (ControllerInterface listener : controllerListeners) {
            listener.sendPacketToDevice(whichThrottle + getCurrentAddressString());
        }
    }

// Function methods
    protected void handleFunction(String inPackage) {
        // get the function # sent from device
        String receivedFunction = inPackage.substring(2);
        Boolean state;

        if (inPackage.charAt(1) == '1') { // Function Button down
            log.debug("Trying to set function {}", receivedFunction);
            // Toggle button state:
            try {
                Method getF = functionThrottle.getClass().getMethod("getF" + receivedFunction, (Class[]) null);

                Class<?> partypes[] = {Boolean.TYPE};
                Method setF = functionThrottle.getClass().getMethod("setF" + receivedFunction, partypes);

                state = (Boolean) getF.invoke(functionThrottle, (Object[]) null);
                Object data[] = {!state};

                setF.invoke(functionThrottle, data);

                log.debug("Throttle: {}, Function: {}, set state: {}", functionThrottle.getLocoAddress(), receivedFunction, !state);

            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ea) {
                log.warn(ea.getLocalizedMessage(), ea);
            }

        } else { // Function Button up

            //  F2 is momentary for horn, unless prefs are set to follow roster entry
            if ((isMomF2) && (receivedFunction.equals("2"))) {
                functionThrottle.setF2(false);
                return;
            }

            // Do nothing if lockable, turn off if momentary
            try {
                Method getFMom = functionThrottle.getClass().getMethod("getF" + receivedFunction + "Momentary", (Class[]) null);

                Class<?> partypes[] = {Boolean.TYPE};
                Method setF = functionThrottle.getClass().getMethod("setF" + receivedFunction, partypes);

                if ((Boolean) getFMom.invoke(functionThrottle, (Object[]) null)) {
                    Object data[] = {false};

                    setF.invoke(functionThrottle, data);
                    log.debug("Throttle: {}, Momentary Function: {}, set false", functionThrottle.getLocoAddress(), receivedFunction);
                }

            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ea) {
                log.warn(ea.getLocalizedMessage(), ea);
            }

        }

    }

    protected void forceFunction(String inPackage) {
        String receivedFunction = inPackage.substring(1);
        Object data[] = new Object[1];

        if (inPackage.charAt(0) == '1') { // Set function on
            data[0] = true;
            log.debug("Trying to set function {} to ON", receivedFunction);
        } else {
            data[0] = false;
            log.debug("Trying to set function {} to OFF", receivedFunction);
        }
        try {
            Class<?> partypes[] = {Boolean.TYPE};
            Method setF = throttle.getClass().getMethod("setF" + receivedFunction, partypes);

            setF.invoke(throttle, data);

        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ea) {
            log.warn(ea.getLocalizedMessage(), ea);
        }

    }

    protected void handleSpeedStepMode(SpeedStepMode newMode) {
        throttle.setSpeedStepMode(newMode);
    }

    protected void handleMomentary(String inPackage) {
        String receivedFunction = inPackage.substring(1);
        Object data[] = new Object[1];

        if (inPackage.charAt(0) == '1') { // Set Momentary TRUE
            data[0] = true;
            log.debug("Trying to set function {} to Momentary", receivedFunction);
        } else {
            data[0] = false;
            log.debug("Trying to set function {} to Locking", receivedFunction);
        }
        try {
            Class<?> partypes[] = {Boolean.TYPE};
            Method setF = throttle.getClass().getMethod("setF" + receivedFunction + "Momentary", partypes);

            setF.invoke(throttle, data);

        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ea) {
            log.warn(ea.getLocalizedMessage(), ea);
        }
    }

    protected void handleRequest(String inPackage) {
        switch (inPackage.charAt(0)) {
            case 'V': {
                if(lastSentSpeed.isEmpty()){
                   // send the current speed only
                   // if we aren't waiting for the back end
                   // to update the speed.
                   sendCurrentSpeed(throttle);
                }
                break;
            }
            case 'R': {
                sendCurrentDirection(throttle);
                break;
            }
            case 's': {
                sendSpeedStepMode(throttle);
                break;
            }
            case 'm': {
                sendAllMomentaryStates(throttle);
                break;
            }
            default:
                log.warn("Unhandled code: {}", inPackage.charAt(0));
                break;
        }

    }


    private static SpeedStepMode decodeSpeedStepMode(String mode) {
        // NOTE: old speed step modes use the original numeric values
        // from when speed step modes were in DccThrottle. If the input does not match
        // any of the old modes, decode based on the new speed step names.
        if(mode.equals("1"))  {
            return SpeedStepMode.NMRA_DCC_128;
        } else if(mode.equals("2")) {
            return SpeedStepMode.NMRA_DCC_28;
        } else if(mode.equals("4")) {
            return SpeedStepMode.NMRA_DCC_27;
        } else if(mode.equals("8")) {
            return SpeedStepMode.NMRA_DCC_14;
        } else if(mode.equals("16")) {
            return SpeedStepMode.MOTOROLA_28;
        }
        return SpeedStepMode.getByName(mode);
    }

    private final static Logger log = LoggerFactory.getLogger(ThrottleController.class);

}
