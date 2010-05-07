
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
 *	'I'dle (defaults to this if it falls through the tree) !! Needs to change to nothing on default
 *          idle needs to be called specifically
 *
 *	@author Brett Hoffman   Copyright (C) 2009, 2010
 *      @author Created by Brett Hoffman on: 8/23/09.
 *	@version $Revision: 1.4 $
 */

import java.lang.reflect.Method;
import jmri.DccThrottle;
import jmri.jmrit.throttle.ThrottleFrame;
import jmri.jmrit.throttle.AddressListener;
import jmri.jmrit.throttle.AddressPanel;
import jmri.jmrit.throttle.ControlPanel;

import javax.swing.JSlider;
import java.util.ArrayList;


public class ThrottleController implements AddressListener{

    private DccThrottle throttle;
    private int speedIncrement;
    AddressPanel addressPanel;
    ControlPanel controlPanel;
    JSlider speedSlider;
    float speedMultiplier;
    private boolean isAddressSet;
    public boolean confirm = false;
    private ArrayList<ThrottleControllerListener> listeners;

/**
 *  Constructor.
 *  Point a local variable to the different panels needed for control.
 *  @param throttleFrame The ThrottleFrame this ThrottleController will control.
 */
    public ThrottleController(ThrottleFrame throttleFrame){

        addressPanel = throttleFrame.getAddressPanel();
        controlPanel = throttleFrame.getControlPanel();
        speedSlider = controlPanel.getSpeedSlider();
        speedMultiplier = speedSlider.getMaximum()/126;

        addressPanel.addAddressListener(this);
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
     * Receive notification that a new address has been selected.
     * @param newAddress The address that is now selected.
     */
    public void notifyAddressChosen(int newAddress, boolean isLong){
    }

    /**
     * Receive notification that an address has been released/dispatched
     * @param address The address released/dispatched
     */
    public void notifyAddressReleased(int address, boolean isLong){
        isAddressSet = false;
        throttle = null;
        for (int i = 0; i < listeners.size(); i++) {
            ThrottleControllerListener l = listeners.get(i);
            l.notifyControllerAddressReleased(this);
            if (log.isDebugEnabled()) log.debug("Notify TCListener address released: " + l.getClass());
        }
    }
    
    /**
     * Recieve notification that a DccThrottle has been found and is in use.
     * Set speedIncrement for this throttle.
     * @param throttle The throttle which has been found
     */
    public void notifyAddressThrottleFound(DccThrottle throttle){
	if (throttle != null) {
            this.throttle = throttle;
            speedIncrement = (int)throttle.getSpeedIncrement();
            isAddressSet = true;
        }else {
            log.error("*throttle is null!*");
        }
        for (int i = 0; i < listeners.size(); i++) {
            ThrottleControllerListener l = listeners.get(i);
            l.notifyControllerAddressFound(this);
            if (log.isDebugEnabled()) log.debug("Notify TCListener address found: " + l.getClass());
        }
        if (speedIncrement == 0) {	//	handles LN Simulator
            speedIncrement = 1;
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
                        addressPanel.releaseAddress();
                        break;

                case 'd':	//	Dispatch
                        addressPanel.dispatchAddress();
                        break;

                case 'L':	//	Set a Long address.
                        addressPanel.dispatchAddress();
                        int addr = Integer.parseInt(inPackage.substring(1));
                        setAddress(addr, true);
                        break;

                case 'S':	//	Set a Short address.
                        addressPanel.dispatchAddress();
                        addr = Integer.parseInt(inPackage.substring(1));
                        setAddress(addr, false);
                        break;

                case 'I':
                    idle();
                    break;

                default:	//	Idle
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




//  Device is quitting or has lost connection
    public void shutdownThrottle(){

        try{
        if (isAddressSet){
            controlPanel.setSpeedValues(speedIncrement, 0);
            addressPanel.dispatchAddress();
            addressPanel.removeAddressListener(this);
        }
        }catch (NullPointerException e){
            log.warn("No throttle frame to shutdown");
        }
    }

//  ControlPanel methods

    private void setSpeed(int rawSpeed){

        int newSpeed = (int)(rawSpeed*speedMultiplier);

        if (log.isDebugEnabled()) log.debug("raw"+rawSpeed+" MAX"+speedSlider.getMaximum()+" NewSpd"+newSpeed);
        controlPanel.setSpeedValues(speedIncrement, newSpeed);
    }


    private void setDirection(boolean isForward){
        controlPanel.setForwardDirection(isForward);
    }

    private void eStop(){
        controlPanel.stop();
    }

    private void idle(){
        controlPanel.setSpeedValues(speedIncrement, 0);
    }


//  AddressPanel methods

/**
 * Move the address values along to the AddressPanel
 * @param number
 * @param isLong
 */
    private void setAddress(int number, boolean isLong){
	
	addressPanel.setAddress(number, isLong);

    }

//	FunctionPanel methods
    private void handleFunction(String inPackage){
        //	get the function # sent from device
        String receivedFunction = inPackage.substring(2);
        Boolean state = false;
        
        if (inPackage.charAt(1) == '1'){	//	Function Button down
            if(log.isDebugEnabled()) log.debug("Trying to set function " + receivedFunction);
            //	Toggle button state:
            try{
                Method getF = throttle.getClass().getMethod("getF"+receivedFunction,(Class[])null);

                Class partypes[] = {Boolean.TYPE};
                Method setF = throttle.getClass().getMethod("setF"+receivedFunction, partypes);
                
                state = (Boolean)getF.invoke(throttle, (Object[])null);
                Object data[] = {new Boolean(!state)};

                setF.invoke(throttle, data);
            
            
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
                throttle.setF2(false);
                return;
            }

            //	Do nothing if lockable, turn off if momentary
            try{
                Method getFMom = throttle.getClass().getMethod("getF"+receivedFunction+"Momentary",(Class[])null);

                Class partypes[] = {Boolean.TYPE};
                Method setF = throttle.getClass().getMethod("setF"+receivedFunction, partypes);
                
                if ((Boolean)getFMom.invoke(throttle, (Object[])null)){
                    Object data[] = {new Boolean(false)};

                    setF.invoke(throttle, data);
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
