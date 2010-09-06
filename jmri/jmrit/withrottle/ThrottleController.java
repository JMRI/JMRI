
package jmri.jmrit.withrottle;


//  WiThrottle
//
//  

/**
 *	ThrottleController.java
 *	Sends commands to appropriate throttle component.
 *
 *	Sorting codes for received string from client:
 *	'V'elocity followed by 0 - 126
 *      'X'stop
 *      'F'unction (1-button down, 0-button up) (0-28) e.g. F14 indicates function 4 button is pressed
 *                                              `       F04 indicates function 4 button is released
 *	di'R'ection (0=reverse, 1=forward)
 *	'L'ong address #, 'S'hort address #     e.g. L1234
 *      'r'elease, 'd'ispatch
 *      'C'consist lead address, e.g. CL1235
 *	'I'dle (defaults to this if it falls through the tree) !! Needs to change to nothing on default
 *          idle needs to be called specifically
 *
 *	@author Brett Hoffman   Copyright (C) 2009, 2010
 *      @author Created by Brett Hoffman on: 8/23/09.
 *	@version $Revision: 1.12 $
 */

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import jmri.DccThrottle;

import java.util.ArrayList;
import java.util.List;
import jmri.DccLocoAddress;
import jmri.ThrottleListener;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;


public class ThrottleController implements /*AddressListener,*/ ThrottleListener, PropertyChangeListener{

    private DccThrottle throttle;
    private DccThrottle functionThrottle;
    private DccLocoAddress leadAddress;
    private String whichThrottle;
    float speedMultiplier;
    private boolean isAddressSet;
    public boolean confirm = false;
    private ArrayList<ThrottleControllerListener> listeners;
    private ArrayList<ControllerInterface> controllerListeners;
    private boolean useLeadLocoF;
    ConsistFunctionController leadLocoF = null;

/**
 *  Constructor.
 *  Point a local variable to the different panels needed for control.
 */
    public ThrottleController(){
        speedMultiplier = 1.0f/126.0f;
    }

    public void setWhichThrottle(String s){
        whichThrottle = s;
    }

    public void addThrottleControllerListener(ThrottleControllerListener l) {
        if (listeners == null)
                listeners = new ArrayList<ThrottleControllerListener>(1);
        if (!listeners.contains(l))
                listeners.add(l);
    }

    public void removeThrottleControllerListener(ThrottleControllerListener l) {
        if (listeners == null)
                return;
        if (listeners.contains(l))
                listeners.remove(l);
    }
    
/**
 * Add a listener to handle:
 * listener.sendPacketToDevice(message);
 * @param listener
 */
    public void addControllerListener(ControllerInterface listener){
        if (controllerListeners == null)
                controllerListeners = new ArrayList<ControllerInterface>(1);
        if (!controllerListeners.contains(listener))
                controllerListeners.add(listener);
    }

    public void removeControllerListener(ControllerInterface listener){
        if (controllerListeners == null)
                return;
        if (controllerListeners.contains(listener))
                controllerListeners.remove(listener);
    }

    /**
     * Receive notification that a new address has been selected.
     * @param newAddress The address that is now selected.
     */
    public void notifyAddressChosen(int newAddress, boolean isLong){
    }

    /**
     * Receive notification that an address has been released/dispatched
     */
    public void addressRelease(/*int address, boolean isLong*/){
        isAddressSet = false;
        throttle.release();
        throttle.removePropertyChangeListener(this);
        throttle = null;
        sendAddress();
        for (int i = 0; i < listeners.size(); i++) {
            ThrottleControllerListener l = listeners.get(i);
            l.notifyControllerAddressReleased(this);
            if (log.isDebugEnabled()) log.debug("Notify TCListener address released: " + l.getClass());
        }
    }

    public void addressDispatch(/*int address, boolean isLong*/){
        isAddressSet = false;
        throttle.dispatch();
        throttle.removePropertyChangeListener(this);
        throttle = null;
        sendAddress();
        for (int i = 0; i < listeners.size(); i++) {
            ThrottleControllerListener l = listeners.get(i);
            l.notifyControllerAddressReleased(this);
            if (log.isDebugEnabled()) log.debug("Notify TCListener address dispatched: " + l.getClass());
        }
    }
    
    /**
     * Recieve notification that a DccThrottle has been found and is in use.
     * 
     * @param t The throttle which has been found
     */
//    public void notifyAddressThrottleFound(DccThrottle throttle){
    public void notifyThrottleFound(DccThrottle t) {
	if (t != null) {
            throttle = t;
            setFunctionThrottle(throttle);
            throttle.addPropertyChangeListener(this);
            isAddressSet = true;
        }else {
            log.error("*throttle is null!*");
            return;
        }
        for (int i = 0; i < listeners.size(); i++) {
            ThrottleControllerListener l = listeners.get(i);
            l.notifyControllerAddressFound(this);
            if (log.isDebugEnabled()) log.debug("Notify TCListener address found: " + l.getClass());
        }
        
        sendAddress();

        sendFunctionLabels(throttle);

        sendAllFunctionStates(whichThrottle);

    }

/*
 * Current Format:  RPF}|{whichThrottle]\[eventName}|{newValue
 * This format may be used to send multiple function status, for initial values.
 *
 * Event may be from regular throttle or consist throttle, but is handled the same.
 */
    public void propertyChange(PropertyChangeEvent event) {
        String eventName = event.getPropertyName();
        log.debug("property change: " + eventName);
        if (eventName.startsWith("F")){
            
            if (eventName.contains("Momentary")){
                return;
            }
            StringBuilder message = new StringBuilder("RPF}|{" + whichThrottle);
            message.append("]\\[" + eventName + "}|{" + event.getNewValue());
            
            for (ControllerInterface listener : controllerListeners){
                listener.sendPacketToDevice(message.toString());
            }
        }
        
    }

    public void sendFunctionLabels(DccThrottle t){
        StringBuilder functionString = new StringBuilder();
        RosterEntry rosterLoco = null;
        if (t.getLocoAddress() != null){
            List<RosterEntry> l = Roster.instance().matchingList(null, null, ""+((DccLocoAddress)t.getLocoAddress()).getNumber(), null, null, null, null);
            if (l.size()>0){
                if (log.isDebugEnabled()) log.debug("Roster Loco found: "+ l.get(0).getDccAddress());
                rosterLoco = l.get(0);
            }else return;
        }

        if (whichThrottle.equalsIgnoreCase("S")) {
            functionString.append("RS29}|{" + getCurrentAddressString());
        }else{
            //  I know, it should have been 'RT' but this was before there were two throttles.
            functionString.append("RF29}|{" + getCurrentAddressString());
        }

        int i;
        for (i = 0; i<29; i++){
            functionString.append("]\\[");
            if ((rosterLoco.getFunctionLabel(i) != null) && (rosterLoco != null)) functionString.append(rosterLoco.getFunctionLabel(i));
        }
        for (ControllerInterface listener : controllerListeners){
            listener.sendPacketToDevice(functionString.toString());
        }
    }
    
/**
 * send all function states, primarily for initial status
 * Current Format:  RPF}|{whichThrottle]\[function}|{state]\[function}|{state...
 * @param whichThrottle identify first or second throttle
 */
    public void sendAllFunctionStates(String whichThrottle){
        
        log.debug("Sending state of all functions");
        StringBuilder message = new StringBuilder("RPF}|{" + whichThrottle);

        try{
            for (int cnt = 0; cnt < 29; cnt++){
                Method getF = throttle.getClass().getMethod("getF"+cnt,(Class[])null);
                message.append("]\\[F" + cnt + "}|{" + getF.invoke(throttle, (Object[])null));
            }

        }catch (NoSuchMethodException ea){
            log.warn(ea);
            return;
        }catch (IllegalAccessException eb){
            log.warn(eb);
            return;
        }catch (java.lang.reflect.InvocationTargetException ec){
            log.warn(ec);
            return;
        }
        
        for (ControllerInterface listener : controllerListeners){
            listener.sendPacketToDevice(message.toString());
        }
        
    }

/**
 * Figure out what the received command means, where it has to go,
 * and translate to a jmri method.
 * @param inPackage The package minus its prefix which steered it here.
 * @return true to keep reading in run loop.
 */
    public boolean sort(String inPackage){
        if (isAddressSet){

            try{
            switch (inPackage.charAt(0)) {
                case 'V':	//	Velocity
                        setSpeed(Integer.parseInt(inPackage.substring(1)));

                        break;

                case 'X':
                        eStop();

                        break;

                case 'F':	//	Function
                    
                        handleFunction(inPackage);
                    
                        break;

                case 'R':	//	Direction
                        setDirection(!inPackage.endsWith("0")); // 0 sets to reverse, all others forward
                        break;

                case 'r':	//	Release
                       // addressPanel.releaseAddress();
                        addressRelease();
                        clearLeadLoco();
                        break;

                case 'd':	//	Dispatch
                        //addressPanel.dispatchAddress();
                        addressDispatch();
                        clearLeadLoco();
                        break;

                case 'L':	//	Set a Long address.
                        //addressPanel.dispatchAddress();
                        addressRelease();
                        clearLeadLoco();
                        int addr = Integer.parseInt(inPackage.substring(1));
                        setAddress(addr, true);
                        break;

                case 'S':	//	Set a Short address.
                        //addressPanel.dispatchAddress();
                        addressRelease();
                        clearLeadLoco();
                        addr = Integer.parseInt(inPackage.substring(1));
                        setAddress(addr, false);
                        break;
                    
                case 'C':
                    setLocoForConsistFunctions(inPackage.substring(1));

                    break;

                case 'I':
                    idle();
                    break;

            }
            }catch (NullPointerException e){
                log.warn("No throttle frame to receive: " + inPackage);
                return false;
            }
        }else{
            switch (inPackage.charAt(0)) {
                case 'L':	//	Set a Long address.
                        int addr = Integer.parseInt(inPackage.substring(1));
                        setAddress(addr, true);
                        break;

                case 'S':	//	Set a Short address.
                        addr = Integer.parseInt(inPackage.substring(1));
                        setAddress(addr, false);
                        break;

                case 'C':
                    setLocoForConsistFunctions(inPackage.substring(1));

                    break;

                default:
                        break;
            }
        }
        if (inPackage.charAt(0) == 'Q') {//	If device has Quit.
            shutdownThrottle();
            return false;
        }
        return true;

    }

    private void clearLeadLoco(){
        if (useLeadLocoF){
            functionThrottle.removePropertyChangeListener(this);
            leadLocoF.dispose();
            if (throttle != null){
                setFunctionThrottle(throttle);
            }
            
            leadLocoF = null;
            useLeadLocoF = false;
        }
    }
    
    public void setFunctionThrottle(DccThrottle t){
        functionThrottle = t;
        functionThrottle.addPropertyChangeListener(this);
    }

    public void setLocoForConsistFunctions(String inPackage){
        /*
         *      This is used to control speed an direction on the
         *      consist address, but have functions mapped to lead.
         *      Consist address must be set first!
         */

        leadAddress = new DccLocoAddress(Integer.parseInt(inPackage.substring(1)), (inPackage.charAt(0) != 'S'));
        //if (inPackage.charAt(1) == 'S'){
        if (log.isDebugEnabled()) log.debug("Setting lead loco address: "+leadAddress.toString() +
                                            ", for consist: " + getCurrentAddressString());
        clearLeadLoco();
        leadLocoF = new ConsistFunctionController(this);
        useLeadLocoF = leadLocoF.requestThrottle(leadAddress);

        if (!useLeadLocoF) {
            log.warn("Lead loco address not available.");
            leadLocoF = null;
        }
    }


//  Device is quitting or has lost connection
    public void shutdownThrottle(){

        try{
        if (isAddressSet){
            throttle.setSpeedSetting(0);
            addressRelease();
        }
        }catch (NullPointerException e){
            log.warn("No throttle frame to shutdown");
        }
        clearLeadLoco();
    }

/**
 * handle the conversion from rawSpeed to the float value needed in
 * the DccThrottle
 * @param rawSpeed  Value sent from mobile device, range 0 - 126
 */
    private void setSpeed(int rawSpeed){

        float newSpeed = (rawSpeed*speedMultiplier);

        if (log.isDebugEnabled()) log.debug("raw: "+rawSpeed+", NewSpd: "+newSpeed);
        throttle.setSpeedSetting(newSpeed);
    }


    private void setDirection(boolean isForward){
        throttle.setIsForward(isForward);
    }

    private void eStop(){
        throttle.setSpeedSetting(-1);
    }

    private void idle(){
        throttle.setSpeedSetting(0);
    }


    private void setAddress(int number, boolean isLong){

        jmri.InstanceManager.throttleManagerInstance().requestThrottle(number, isLong, this);

    }

    public DccThrottle getThrottle(){
        return throttle;
    }

    public DccThrottle getFunctionThrottle(){
        return functionThrottle;
    }


    public DccLocoAddress getCurrentAddress(){
        return (DccLocoAddress)throttle.getLocoAddress();
    }

    /**
     * Get the string representation of this throttles address.
     * Returns 'Not Set' if no address in use.
     */
    public String getCurrentAddressString(){
        if (isAddressSet){
            return ((DccLocoAddress)throttle.getLocoAddress()).toString();
        }else {
            return "Not Set";
        }
    }

    public void sendAddress(){
        for (ControllerInterface listener : controllerListeners){
            listener.sendPacketToDevice(whichThrottle + getCurrentAddressString());
        }
    }

//	Function methods
    private void handleFunction(String inPackage){
        //	get the function # sent from device
        String receivedFunction = inPackage.substring(2);
        Boolean state = false;
        
        if (inPackage.charAt(1) == '1'){	//	Function Button down
            if(log.isDebugEnabled()) log.debug("Trying to set function " + receivedFunction);
            //	Toggle button state:
            try{
                Method getF = functionThrottle.getClass().getMethod("getF"+receivedFunction,(Class[])null);

                Class partypes[] = {Boolean.TYPE};
                Method setF = functionThrottle.getClass().getMethod("setF"+receivedFunction, partypes);
                
                state = (Boolean)getF.invoke(functionThrottle, (Object[])null);
                Object data[] = {new Boolean(!state)};

                setF.invoke(functionThrottle, data);
            
            
            }catch (NoSuchMethodException ea){
                log.warn(ea);
            }catch (IllegalAccessException eb){
                log.warn(eb);
            }catch (java.lang.reflect.InvocationTargetException ec){
                log.warn(ec);
            }
            
        }else {	//	Function Button up

            //  F2 is momentary for horn
            //  Need to figure out what to do, Should this be in prefs?

            if (receivedFunction.equals("2")){
                functionThrottle.setF2(false);
                return;
            }

            //	Do nothing if lockable, turn off if momentary
            try{
                Method getFMom = functionThrottle.getClass().getMethod("getF"+receivedFunction+"Momentary",(Class[])null);

                Class partypes[] = {Boolean.TYPE};
                Method setF = functionThrottle.getClass().getMethod("setF"+receivedFunction, partypes);
                
                if ((Boolean)getFMom.invoke(functionThrottle, (Object[])null)){
                    Object data[] = {new Boolean(false)};

                    setF.invoke(functionThrottle, data);
                }
            
            }catch (NoSuchMethodException ea){
                log.warn(ea);
            }catch (IllegalAccessException eb){
                log.warn(eb);
            }catch (java.lang.reflect.InvocationTargetException ec){
                log.warn(ec);
            }
            
        }

    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ThrottleController.class.getName());

}
