package jmri.jmrix.can.cbus;

import jmri.DccLocoAddress;
import jmri.LocoAddress;
import jmri.SpeedStepMode;
import jmri.jmrix.AbstractThrottle;
import jmri.jmrix.can.CanSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of DccThrottle via AbstractThrottle with code specific to a
 * CBUS connection.
 * <p>
 * Speed in the Throttle interfaces and AbstractThrottle is a float, but in CBUS
 * is an int with values from 0 to 127.
 *
 * @author Andrew Crosland Copyright (C) 2009
 */
public class CbusThrottle extends AbstractThrottle {

    private CbusCommandStation cs = null;
    private int _handle = -1;
    private DccLocoAddress dccAddress = null;
    private boolean _isStolen;
    private int _recoveryAttempts;

    /**
     * Constructor
     *
     * @param memo System Connection
     * @param address The address this throttle relates to.
     * @param handle the Session ID for the Throttle
     */
    public CbusThrottle(CanSystemConnectionMemo memo, LocoAddress address, int handle) {
        super(memo);
        log.debug("creating new CbusThrottle address {} handle {}",address,handle);
        if (!( address instanceof DccLocoAddress )  ){
            log.error("{} is not a DccLocoAddress",address);
            return;
        }
        log.debug("Throttle created");
        cs = (CbusCommandStation) adapterMemo.get(jmri.CommandStation.class);
        _handle = handle;
        _isStolen = false;
        _recoveryAttempts = 0;

        // cache settings
        this.speedSetting = 0;
        // Functions 0-28 default to false
        this.dccAddress = (DccLocoAddress) address;
        this.isForward = true;

//        switch(slot.decoderType())
//        {
//            case CbusConstants.DEC_MODE_128:
//            case CbusConstants.DEC_MODE_128A: this.speedIncrement = 1; break;
//            case CbusConstants.DEC_MODE_28:
//            case CbusConstants.DEC_MODE_28A:
//            case CbusConstants.DEC_MODE_28TRI: this.speedIncrement = 4; break;
//            case CbusConstants.DEC_MODE_14: this.speedIncrement = 8; break;
//        }
        // Only 128 speed step supported at the moment
        this.speedStepMode = SpeedStepMode.NMRA_DCC_128;

        // start periodically sending keep alives, to keep this
        // attached
        log.debug("Start Throttle refresh");
        startRefresh();

    }

    /**
     * Set initial throttle values as taken from PLOC reply from hardware
     *
     * @param speed including direction flag
     * @param f0f4 Functions 0-4
     * @param f5f8 Functions 5-8
     * @param f9f12 Functions 9-12
     */
    protected void throttleInit(int speed, int f0f4, int f5f8, int f9f12) {
        log.debug("Setting throttle initial values");
        updateSpeedSetting( speed & 0x7f );
        updateIsForward ( (speed & 0x80) == 0x80 );
        updateFunctionGroup1(f0f4);
        updateFunctionGroup2(f5f8);
        updateFunctionGroup3(f9f12);
    }

    /**
     * setSpeedStepMode - set the speed step value.
     * <p>
     * Overridden to capture mode changes to be forwarded to the hardware.
     * New throttles default to 128 step mode
     *
     * @param Mode the current speed step mode - default should be 128
     *              speed step mode in most cases
     */
    @Override
    public void setSpeedStepMode(SpeedStepMode Mode) {
        int mode;
        speedStepMode = Mode;
        super.setSpeedStepMode(speedStepMode);
        switch (speedStepMode) {
            case NMRA_DCC_28:
                mode = CbusConstants.CBUS_SS_28;
                break;
            case NMRA_DCC_14:
                mode = CbusConstants.CBUS_SS_14;
                break;
            default:
                mode = CbusConstants.CBUS_SS_128;
                break;
        }
        cs.setSpeedSteps(_handle, mode);
    }

    /**
     * Convert a CBUS speed integer to a float speed value
     * @param lSpeed -1 to 127
     * @return float value -1 to 1
     */
    protected float floatSpeed(int lSpeed) {
        switch (lSpeed) {
            case 0:
                return 0.f;
            case 1:
                return -1.f;   // estop
            default:
                return ((lSpeed - 1) / 126.f);
        }
    }

    /**
     * Send the CBUS message to set the state of locomotive direction and
     * functions F0, F1, F2, F3, F4
     */
    @Override
    protected void sendFunctionGroup1() {
        int new_fn = ((getF0() ? CbusConstants.CBUS_F0 : 0)
                | (getF1() ? CbusConstants.CBUS_F1 : 0)
                | (getF2() ? CbusConstants.CBUS_F2 : 0)
                | (getF3() ? CbusConstants.CBUS_F3 : 0)
                | (getF4() ? CbusConstants.CBUS_F4 : 0));
        cs.setFunctions(1, _handle, new_fn);
    }

    /**
     * Send the CBUS message to set the state of functions F5, F6, F7, F8
     */
    @Override
    protected void sendFunctionGroup2() {
        int new_fn = ((getF5() ? CbusConstants.CBUS_F5 : 0)
                | (getF6() ? CbusConstants.CBUS_F6 : 0)
                | (getF7() ? CbusConstants.CBUS_F7 : 0)
                | (getF8() ? CbusConstants.CBUS_F8 : 0));
        cs.setFunctions(2, _handle, new_fn);
    }

    /**
     * Send the CBUS message to set the state of functions F9, F10, F11, F12
     */
    @Override
    protected void sendFunctionGroup3() {
        int new_fn = ((getF9() ? CbusConstants.CBUS_F9 : 0)
                | (getF10() ? CbusConstants.CBUS_F10 : 0)
                | (getF11() ? CbusConstants.CBUS_F11 : 0)
                | (getF12() ? CbusConstants.CBUS_F12 : 0));
        cs.setFunctions(3, _handle, new_fn);
    }

    /**
     * Send the CBUS message to set the state of functions F13, F14, F15, F16,
     * F17, F18, F19, F20
     */
    @Override
    protected void sendFunctionGroup4() {
        int new_fn = ((getF13() ? CbusConstants.CBUS_F13 : 0)
                | (getF14() ? CbusConstants.CBUS_F14 : 0)
                | (getF15() ? CbusConstants.CBUS_F15 : 0)
                | (getF16() ? CbusConstants.CBUS_F16 : 0)
                | (getF17() ? CbusConstants.CBUS_F17 : 0)
                | (getF18() ? CbusConstants.CBUS_F18 : 0)
                | (getF19() ? CbusConstants.CBUS_F19 : 0)
                | (getF20() ? CbusConstants.CBUS_F20 : 0));
        cs.setFunctions(4, _handle, new_fn);
    }

    /**
     * Send the CBUS message to set the state of functions F21, F22, F23, F24,
     * F25, F26, F27, F28
     */
    @Override
    protected void sendFunctionGroup5() {
        int new_fn = ((getF21() ? CbusConstants.CBUS_F21 : 0)
                | (getF22() ? CbusConstants.CBUS_F22 : 0)
                | (getF23() ? CbusConstants.CBUS_F23 : 0)
                | (getF24() ? CbusConstants.CBUS_F24 : 0)
                | (getF25() ? CbusConstants.CBUS_F25 : 0)
                | (getF26() ? CbusConstants.CBUS_F26 : 0)
                | (getF27() ? CbusConstants.CBUS_F27 : 0)
                | (getF28() ? CbusConstants.CBUS_F28 : 0));
        cs.setFunctions(5, _handle, new_fn);
    }

    /**
     * Update the state of locomotive functions F0, F1, F2, F3, F4 in response
     * to a message from the hardware
     * @param fns int value Fn 0-4
     */
    protected void updateFunctionGroup1(int fns) {
        updateFunction( 0, (fns & CbusConstants.CBUS_F0) == CbusConstants.CBUS_F0 );
        updateFunction( 1, (fns & CbusConstants.CBUS_F1) == CbusConstants.CBUS_F1);
        updateFunction( 2, (fns & CbusConstants.CBUS_F2) == CbusConstants.CBUS_F2);
        updateFunction( 3, (fns & CbusConstants.CBUS_F3) == CbusConstants.CBUS_F3);
        updateFunction( 4, (fns & CbusConstants.CBUS_F4) == CbusConstants.CBUS_F4);
    }

    /**
     * Update the state of locomotive functions F5, F6, F7, F8 in response to a
     * message from the hardware
     * @param fns int value Fn 5-8
     */
    protected void updateFunctionGroup2(int fns) {
        updateFunction( 5, (fns & CbusConstants.CBUS_F5) == CbusConstants.CBUS_F5);
        updateFunction( 6, (fns & CbusConstants.CBUS_F6) == CbusConstants.CBUS_F6);
        updateFunction( 7, (fns & CbusConstants.CBUS_F7) == CbusConstants.CBUS_F7);
        updateFunction( 8, (fns & CbusConstants.CBUS_F8) == CbusConstants.CBUS_F8);
    }

    /**
     * Update the state of locomotive functions F9, F10, F11, F12 in response to
     * a message from the hardware
     * @param fns int value Fn 9-12
     */
    protected void updateFunctionGroup3(int fns) {
        updateFunction( 9, (fns & CbusConstants.CBUS_F9) == CbusConstants.CBUS_F9);
        updateFunction( 10, (fns & CbusConstants.CBUS_F10) == CbusConstants.CBUS_F10);
        updateFunction( 11, (fns & CbusConstants.CBUS_F11) == CbusConstants.CBUS_F11);
        updateFunction( 12, (fns & CbusConstants.CBUS_F12) == CbusConstants.CBUS_F12);
    }

    /**
     * Update the state of locomotive functions F13, F14, F15, F16, F17, F18,
     * F19, F20 in response to a message from the hardware
     * @param fns int value Fn 13-20
     */
    protected void updateFunctionGroup4(int fns) {
        
        updateFunction( 13 , ((fns & CbusConstants.CBUS_F13) == CbusConstants.CBUS_F13));
        updateFunction( 14 , ((fns & CbusConstants.CBUS_F14) == CbusConstants.CBUS_F14));
        updateFunction( 15 , ((fns & CbusConstants.CBUS_F15) == CbusConstants.CBUS_F15));
        updateFunction( 16 , ((fns & CbusConstants.CBUS_F16) == CbusConstants.CBUS_F16));
        updateFunction( 17 , ((fns & CbusConstants.CBUS_F17) == CbusConstants.CBUS_F17));
        updateFunction( 18 , ((fns & CbusConstants.CBUS_F18) == CbusConstants.CBUS_F18));
        updateFunction( 19 , ((fns & CbusConstants.CBUS_F19) == CbusConstants.CBUS_F19));
        updateFunction( 20 , ((fns & CbusConstants.CBUS_F20) == CbusConstants.CBUS_F20));
    }

    /**
     * Update the state of locomotive functions F21, F22, F23, F24, F25, F26,
     * F27, F28 in response to a message from the hardware
     * @param fns int value Fn 21-28
     */
    protected void updateFunctionGroup5(int fns) {
        updateFunction( 21 , ((fns & CbusConstants.CBUS_F21) == CbusConstants.CBUS_F21));
        updateFunction( 22 , ((fns & CbusConstants.CBUS_F22) == CbusConstants.CBUS_F22));
        updateFunction( 23 , ((fns & CbusConstants.CBUS_F23) == CbusConstants.CBUS_F23));
        updateFunction( 24 , ((fns & CbusConstants.CBUS_F24) == CbusConstants.CBUS_F24));
        updateFunction( 25 , ((fns & CbusConstants.CBUS_F25) == CbusConstants.CBUS_F25));
        updateFunction( 26 , ((fns & CbusConstants.CBUS_F26) == CbusConstants.CBUS_F26));
        updateFunction( 27 , ((fns & CbusConstants.CBUS_F27) == CbusConstants.CBUS_F27));
        updateFunction( 28 , ((fns & CbusConstants.CBUS_F28) == CbusConstants.CBUS_F28));
    }

    /**
     * Set the speed.
     * <p>
     * This intentionally skips the emergency stop value of 1.
     *
     * @param speed Number from 0 to 1; less than zero is emergency stop
     */
    @Override
    public void setSpeedSetting(float speed) {
        log.debug("setSpeedSetting({}) ", speed);
        float oldSpeed = this.speedSetting;
        this.speedSetting = speed;
        if (speed < 0) {
            this.speedSetting = -1.f;
        }
        
        if ( this.speedSetting <= 0 ) {
                setDispatchActive(false);
        }
        else {
            setDispatchActive(true);
        }

        if (Math.abs(oldSpeed - this.speedSetting) > 0.0001) {
            sendToLayout();
            notifyPropertyChangeListener(SPEEDSETTING, oldSpeed, this.speedSetting);
            record(this.speedSetting); // float
        }
    }
    
    // following a speed or direction change, sends to layout
    private void sendToLayout(){
        int new_spd = intSpeed(this.speedSetting);
        if (this.isForward) {
            new_spd = new_spd | 0x80;
        }
        log.debug("Sending speed/dir for speed: {}",new_spd);
        // reset timeout
        mRefreshTimer.stop();
        mRefreshTimer.setRepeats(true);
        mRefreshTimer.start();
        if (cs != null ) {
            cs.setSpeedDir(_handle, new_spd);
        }
    }

    /**
     * Update the throttles speed setting without sending to hardware. Used to
     * support CBUS sharing by taking speed received <b>from</b> the hardware in
     * an OPC_DSPD message.
     * <p>
     * No compensation required for a direction flag
     * @param speed integer speed value
     */
    protected void updateSpeedSetting(int speed) {
        
        log.debug("Updated speed/dir for speed:{}",speed);
        
        float oldSpeed = this.speedSetting;
        this.speedSetting = floatSpeed(speed);
        if (speed < 0) {
            this.speedSetting = -1.f;
        }
        
        if ( this.speedSetting <= 0 ) {
                setDispatchActive(false);
        }
        else {
            setDispatchActive(true);
        }

        if (Math.abs(oldSpeed - this.speedSetting) > 0.0001) {
            notifyPropertyChangeListener(SPEEDSETTING, oldSpeed, this.speedSetting);
            record(this.speedSetting); // float
        }
    }

    /**
     * Set the direction and reset speed.
     * Forwards to the layout
     * {@inheritDoc}
     */
    @Override
    public void setIsForward(boolean forward) {
        boolean old = this.isForward;
        this.isForward = forward;
        if (old != this.isForward) {
            sendToLayout();
            notifyPropertyChangeListener(ISFORWARD, old, isForward);
        }
    }
    
    /**
     * Update the throttles direction without sending to hardware.Used to
     * support CBUS sharing by taking direction received <b>from</b> the
     * hardware in an OPC_DSPD message.
     * @param forward True if Forward, else False
     */
    protected void updateIsForward(boolean forward){
        super.setIsForward(forward);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getLocoAddress().toString();
    }

    /**
     * Return the handle for this throttle
     *
     * @return integer session handle
     */
    protected int getHandle() {
        return _handle;
    }
    
    /**
     * Set the handle for this throttle
     * <p>
     * This is normally done on Throttle Construction but certain
     * operations, eg. recovering from an external steal
     * may need to change this.
     * @param newHandle session handle
     */
    protected void setHandle(int newHandle){
        _handle = newHandle;
    }

    /**
     * Set Throttle Stolen Flag
     * <p>
     * This is false on Throttle Construction but certain
     * operations may need to change this, eg. an external steal.
     * <p>
     * Sends IsAvailable Property Change Notification
     * @param isStolen true if Throttle has been stolen, else false
     */
    protected void setStolen(boolean isStolen){
        if (isStolen != _isStolen){
            notifyPropertyChangeListener("IsAvailable", isStolen, _isStolen); // PCL is opposite of local boolean
            _isStolen = isStolen;
        }
        if (isStolen){ // stop keep-alive messages
            if ( mRefreshTimer != null ) {
                mRefreshTimer.stop();
            }
            mRefreshTimer = null;
        }
        else {
            startRefresh(); // resume keep-alive messages
        }
    }
    
    /**
     * Get Throttle Stolen Flag
     * <p>
     * This is false on Throttle Construction but certain
     * operations may need to change this, eg. an external steal.
     * @return true if Throttle has been stolen, else false
     */
    protected boolean isStolen(){
        return _isStolen;
    }

    /**
     * Get the number of external steal recovery attempts
     * @return Number of attempts since last reset
     */
    protected int getNumRecoverAttempts(){
        return _recoveryAttempts;
    }

    /**
     * Increase a count of external steal recovery attempts
     */
    protected void increaseNumRecoverAttempts(){
        _recoveryAttempts++;
    }
    
    /**
     * Reset count of recovery attempts
     */
    protected void resetNumRecoverAttempts(){
        _recoveryAttempts = 0;
    }

    /**
     * Release session from a command station
     * ie. throttle with clean full dispose called from releaseThrottle
     */
    protected void releaseFromCommandStation(){
        if ( cs != null ) {
            cs.releaseSession(_handle);
        }
    }

    /**
     * Dispose when finished with this object. After this, further usage of this
     * Throttle object will result in a JmriException.
     */
    @Override
    public void throttleDispose() {
        log.debug("dispose");
        
        finishRecord();
        
        notifyThrottleDisconnect();
        
        // stop timeout
        if ( mRefreshTimer != null ) {
            mRefreshTimer.stop();
        }
        mRefreshTimer = null;
        cs = null;
        _handle = -1;
        
    }

    private javax.swing.Timer mRefreshTimer;

    // CBUS command stations expect DSPD per sesison every 4s
    protected final void startRefresh() {
        mRefreshTimer = new javax.swing.Timer(4000, (java.awt.event.ActionEvent e) -> {
            keepAlive();
        });
        mRefreshTimer.setRepeats(true);     // refresh until stopped by dispose
        mRefreshTimer.start();
    }

    /**
     * Internal routine to resend the speed on a timeout
     */
    synchronized private void keepAlive() {
        if (cs != null) { // cs can be null if in process of terminating?
            cs.sendKeepAlive(_handle);

            // reset timeout
            mRefreshTimer.stop();
            mRefreshTimer.setRepeats(true);     // refresh until stopped by dispose
            mRefreshTimer.start();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LocoAddress getLocoAddress() {
        return dccAddress;
    }
    
    /**
     * Adds extra check for num of this JMRI throttle users before notifying
     * and makes sure these always get sent as a pair
     * the abstracts only send to ThrottleListeners if value has been changed
     * @param newval set true if dispatch can be enabled, else false
     */
    protected void setDispatchActive( boolean newval){
        
        // feature disabled if command station not listed in CBUS node table,
        // could be CANCMD v3 or in test
        if ( cs == null ) {
            return;
        }
        if ( cs.getMasterCommandStation() == null ) {
            return;
        }
        
        if (newval == true){
            int numThrottles = jmri.InstanceManager.throttleManagerInstance().getThrottleUsageCount(dccAddress);
            log.debug("numThrottles {}",numThrottles);
            if ( numThrottles < 2 ){
                notifyThrottleReleaseEnabled(false);
                notifyThrottleDispatchEnabled(true);
                return;
            }
        }
        notifyThrottleReleaseEnabled(true);
        notifyThrottleDispatchEnabled(false);
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(CbusThrottle.class);

}
