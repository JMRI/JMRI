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
        cs = (CbusCommandStation) adapterMemo.get(jmri.CommandStation.class);
        _handle = handle;
        _isStolen = false;
        _recoveryAttempts = 0;

        // cache settings
        synchronized(this) {
            this.speedSetting = 0;
        }
        // Functions 0-28 default to false
        this.dccAddress = (DccLocoAddress) address;
        this.isForward = true;
        this.speedStepMode = SpeedStepMode.NMRA_DCC_128;

        // start periodically sending keep alives, to keep this attached
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
        updateFunctionGroup(1,f0f4);
        updateFunctionGroup(2,f5f8);
        updateFunctionGroup(3,f9f12);
    }

    /**
     * setSpeedStepMode - set the speed step value.
     * <p>
     * Overridden to capture mode changes to be forwarded to the hardware.
     * New throttles default to 128 step mode.
     * CBUS Command stations also default to 128SS so this does not
     * need to be sent if unchanged.
     *
     * @param Mode the current speed step mode - default should be 128
     *              speed step mode in most cases
     */
    @Override
    public void setSpeedStepMode(SpeedStepMode Mode) {
        if (speedStepMode==Mode){
            return;
        }
        super.setSpeedStepMode(Mode);
        int mode;
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
     * Send the CBUS message to set the state of functions F0, F1, F2, F3, F4.
     */
    @Override
    protected void sendFunctionGroup1() {
        sendFunctionGroup(1);
    }

    /**
     * Send the CBUS message to set the state of functions F5, F6, F7, F8.
     */
    @Override
    protected void sendFunctionGroup2() {
        sendFunctionGroup(2);
    }

    /**
     * Send the CBUS message to set the state of functions F9, F10, F11, F12.
     */
    @Override
    protected void sendFunctionGroup3() {
        sendFunctionGroup(3);
    }

    /**
     * Send the CBUS message to set the state of functions F13, F14, F15, F16,
     * F17, F18, F19, F20
     */
    @Override
    protected void sendFunctionGroup4() {
        sendFunctionGroup(4);
    }

    /**
     * Send the CBUS message to set the state of functions F21, F22, F23, F24,
     * F25, F26, F27, F28
     */
    @Override
    protected void sendFunctionGroup5() {
        sendFunctionGroup(5);
    }
    
    protected void sendFunctionGroup(int group) {
        int totVal = 0;
        for ( int i=0; i<=28; i++ ){
            if (FUNCTION_GROUPS[i]==group && getFunction(i)){
                totVal = totVal + CbusConstants.CBUS_FUNCTION_BITS[i];
            }
        }
        cs.setFunctions(group, _handle, totVal);
    }

    protected void updateFunctionGroup(int group, int fns) {
        for ( int i=0; i<=28; i++ ){
            if (FUNCTION_GROUPS[i]==group){
                updateFunction( i, (fns & CbusConstants.CBUS_FUNCTION_BITS[i]) == CbusConstants.CBUS_FUNCTION_BITS[i] );
            }
        }
    }

    /**
     * Set the speed.
     * <p>
     * This intentionally skips the emergency stop value of 1.
     *
     * @param speed Number from 0 to 1; less than zero is emergency stop
     */
    @Override
    public synchronized void setSpeedSetting(float speed) {
        log.debug("setSpeedSetting({}) ", speed);
        float oldSpeed = this.speedSetting;
        this.speedSetting = speed;
        if (speed < 0) {
            this.speedSetting = -1.f;
        }

        setDispatchActive(!(this.speedSetting <= 0));

        if (Math.abs(oldSpeed - this.speedSetting) > 0.0001) {
            sendToLayout();
            firePropertyChange(SPEEDSETTING, oldSpeed, this.speedSetting);
            record(this.speedSetting); // float
        }
    }
    
    // TODO - use intSpeed( speedSetting, SPEED_STEPS )
    // following a speed or direction change, sends to layout
    private void sendToLayout() {
        int new_spd;
        synchronized(this) {
            new_spd = intSpeed(this.speedSetting);
        }
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
    protected synchronized void updateSpeedSetting(int speed) {
        
        log.debug("Updated speed/dir for speed:{}",speed);
        
        float oldSpeed = this.speedSetting;
        this.speedSetting = floatSpeed(speed);
        if (speed < 0) {
            this.speedSetting = -1.f;
        }

        setDispatchActive(!(this.speedSetting <= 0));

        if (Math.abs(oldSpeed - this.speedSetting) > 0.0001) {
            firePropertyChange(SPEEDSETTING, oldSpeed, this.speedSetting);
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
            firePropertyChange(ISFORWARD, old, isForward);
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
            firePropertyChange("IsAvailable", isStolen, _isStolen); // PCL is opposite of local boolean
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
