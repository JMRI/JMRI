
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
 *	'I'dle (defaults to this if it falls through the tree)
 *
 *	@author Brett Hoffman   Copyright (C) 2009
 *      @author Created by Brett Hoffman on: 8/23/09.
 *	@version $Revision: 1.2 $
 */

import jmri.DccThrottle;
import jmri.jmrit.throttle.ThrottleFrame;
import jmri.jmrit.throttle.AddressListener;
import jmri.jmrit.throttle.AddressPanel;
import jmri.jmrit.throttle.ControlPanel;
import jmri.jmrit.throttle.FunctionPanel;
import jmri.jmrit.throttle.FunctionButton;

import javax.swing.JSlider;
import java.util.ArrayList;


public class ThrottleController implements AddressListener{

	private int speedIncrement;
	AddressPanel addressPanel;
	ControlPanel controlPanel;
	FunctionPanel functionPanel;
	private FunctionButton functionButton[];
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
        functionPanel = throttleFrame.getFunctionPanel();
        //	Function buttons will only show actions if their frame is the active one.
        try{
                functionPanel.setSelected(true);
        }catch (java.beans.PropertyVetoException e) {
                //	Oh well, we tried.
        }
        functionButton = functionPanel.getFunctionButtons();
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
        int receivedFunction = Integer.parseInt(inPackage.substring(2));

        if (inPackage.charAt(1) == '1'){	//	Function Button down
            //	Toggle button state:
            if(log.isDebugEnabled()) log.debug("Trying to set function " + receivedFunction);
            functionButton[receivedFunction].changeState(!functionButton[receivedFunction].getState());
        }else {	//	Function Button up
            //	Do nothing if lockable, turn off if momentary
            if(!functionButton[receivedFunction].getIsLockable()){
                if(log.isDebugEnabled()) log.debug("is not lockable " + receivedFunction);
                functionButton[receivedFunction].changeState(false);
            }
        }
        if(log.isDebugEnabled()){
            if (functionButton[receivedFunction].getState()){
                log.debug(inPackage + " ON");
            }else log.debug(inPackage + " OFF");
        }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ThrottleController.class.getName());

}
