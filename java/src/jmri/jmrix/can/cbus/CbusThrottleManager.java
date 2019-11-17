package jmri.jmrix.can.cbus;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TimerTask;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.LocoAddress;
import jmri.SpeedStepMode;
import jmri.ThrottleListener;
import jmri.ThrottleListener.DecisionType;
import jmri.jmrit.throttle.ThrottlesPreferences;
import jmri.jmrix.AbstractThrottleManager;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CBUS implementation of a ThrottleManager.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Andrew Crosland Copyright (C) 2009
 * @author Steve Young Copyright (C) 2019
 */
public class CbusThrottleManager extends AbstractThrottleManager implements CanListener {

    private boolean _handleExpected = false;
    private boolean _handleExpectedSecondLevelRequest = false;
    private int _intAddr;
    private DccLocoAddress _dccAddr;
    protected int THROTTLE_TIMEOUT = 5000;
    private JDialog canErrorDialog;
    private JDialog invalidErrorDialog;

    private final HashMap<Integer, CbusThrottle> softThrottles = new HashMap<>(CbusConstants.CBUS_MAX_SLOTS);

    public CbusThrottleManager(CanSystemConnectionMemo memo) {
        super(memo);
        tc = memo.getTrafficController();
        addTc(tc);
    }
    
    public void dispose() {
        tc.removeCanListener(this);
        stopThrottleRequestTimer();
    }

    private final TrafficController tc;

    /**
     * CBUS allows Throttle sharing, both internally within JMRI and externally by command stations
     * <p>
     * {@inheritDoc}
     */
    @Override
    protected boolean singleUse() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void requestThrottleSetup(LocoAddress address, boolean control) {
        startThrottleRequestTimer(false);
        requestThrottleSetup(address, DecisionType.STEAL_OR_SHARE);
    }
    
    /**
     * As this method is called by both throttle recovery and normal throttle creation,
     * methods calling need to start their own timeoouts to ensure the correct
     * error message is displayed.
     */
    private void requestThrottleSetup(LocoAddress address, DecisionType decision) {
        if ( !( address instanceof DccLocoAddress)) {
            log.error("{} is not a DccLocoAddress",address);
            return;
        }
        _dccAddr = (DccLocoAddress) address;
        _intAddr = _dccAddr.getNumber();

        // The CBUS protocol requires that we request a session from the command
        // station. Throttle object will be notified by Command Station
        log.debug("Requesting {} session for loco {}",decision,_dccAddr);
        if (_dccAddr.isLongAddress()) {
            _intAddr = _intAddr | 0xC000;
        }
        CanMessage msg;
        
        switch (decision) {
            case STEAL_OR_SHARE:
                // 1st line request
                // Request a session for this throttle normally
                _handleExpectedSecondLevelRequest = false;
                msg = new CanMessage(3, tc.getCanid());
                msg.setOpCode(CbusConstants.CBUS_RLOC);
                msg.setElement(1, _intAddr / 256);
                msg.setElement(2, _intAddr & 0xff);
                break;
            case STEAL:
                // 2nd line request
                // Request a Steal session
                _handleExpectedSecondLevelRequest = true;
                msg = new CanMessage(4, tc.getCanid());
                msg.setOpCode(CbusConstants.CBUS_GLOC);
                msg.setElement(1, _intAddr / 256);
                msg.setElement(2, _intAddr & 0xff);
                msg.setElement(3, 0x01); // bit 0 flag set
                break;
            case SHARE:
                // 2nd line request
                // Request a Share session
                _handleExpectedSecondLevelRequest = true;
                msg = new CanMessage(4, tc.getCanid());
                msg.setOpCode(CbusConstants.CBUS_GLOC);
                msg.setElement(1, _intAddr / 256);
                msg.setElement(2, _intAddr & 0xff);
                msg.setElement(3, 0x02); // bit 1 flag set
                break;
            default:
                log.error("decision type {} unknown to CbusThrottleManager",decision);
                return;
        }
        
        // send the request to layout
        _handleExpected = true;
        tc.sendCanMessage(msg, this);
    }

    /**
     * stopAll()
     *
     * <p>
     * Called when track stopped message received. Sets all JMRI managed
     * throttles to speed zero
     */
    private void stopAll() {
        // Get set of handles for JMRI managed throttles and
        // iterate over them setting the speed of each throttle to 0
        // log.info("stopAll() setting all speeds to emergency stop");
        Iterator<Integer> itr = softThrottles.keySet().iterator();
        while (itr.hasNext()) {
            CbusThrottle throttle = softThrottles.get(itr.next());
            throttle.setSpeedSetting(-1.0f);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void message(CanMessage m) {
        if ( m.isExtended() || m.isRtr() ) {
            return;
        }
        int opc = m.getElement(0);
        int handle;
        Iterator<Integer> itr;
        switch (opc) {
            case CbusConstants.CBUS_ESTOP:
            case CbusConstants.CBUS_RESTP:
                stopAll();
                break;
            case CbusConstants.CBUS_KLOC: // Kill loco
                log.debug("Kill loco message");
                // Find a throttle corresponding to the handle
                itr = softThrottles.keySet().iterator();
                handle = m.getElement(1);
                while (itr.hasNext()) {
                        CbusThrottle throttle = softThrottles.get(itr.next());
                        if (throttle.getHandle() == handle) {
                            // make sure timer stopped
                            throttle.throttleDispose();
                            // Remove the Throttle from the managed list
                            itr.remove();
                        }
                    }
                break;
            case CbusConstants.CBUS_DSPD:
                // only if emergency stop
                if ((m.getElement(2) & 0x7f) == 1 ){
                    // Find a throttle corresponding to the handle
                    itr = softThrottles.keySet().iterator();
                    handle = m.getElement(1);
                    while (itr.hasNext()) {
                        CbusThrottle throttle = softThrottles.get(itr.next());
                        if (throttle.getHandle() == handle) {
                            // Set the throttle session to match the DSPD packet
                            throttle.updateSpeedSetting(m.getElement(2) & 0x7f);
                            throttle.updateIsForward((m.getElement(2) & 0x80) == 0x80);
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

    private boolean isCanErrorDialogVisible(){
        return canErrorDialog!=null && canErrorDialog.isVisible();
    }
    
    private boolean isInvalidErrorDialogVisible(){
        return invalidErrorDialog!=null && invalidErrorDialog.isVisible();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reply(CanReply m) {
        if ( m.extendedOrRtr() ) {
            return;
        }
        int opc = m.getElement(0);
        int rcvdIntAddr;
        boolean rcvdIsLong;
        int handle = m.getElement(1);
        
        DccLocoAddress rcvdDccAddr;
        String errStr = "";
        Iterator<Integer> itr;

        switch (opc) {
            case CbusConstants.CBUS_PLOC:
                rcvdIntAddr = (m.getElement(2) & 0x3f) * 256 + m.getElement(3);
                rcvdIsLong = (m.getElement(2) & 0xc0) != 0;
                rcvdDccAddr = new DccLocoAddress(rcvdIntAddr, rcvdIsLong);
                log.debug("Throttle manager received PLOC with session {} for address {}",m.getElement(1),rcvdIntAddr);
                if ((_handleExpected) 
                    && rcvdDccAddr.equals(_dccAddr)) {
                    log.debug("PLOC was expected");
                    // We're expecting an engine report and it matches our address
                    stopThrottleRequestTimer();
                    handle = m.getElement(1);
                    
                    // check if the PLOC has come from a throttle session cancel notification
                    itr = softThrottles.keySet().iterator();
                    while (itr.hasNext()) {
                        CbusThrottle throttle = softThrottles.get(itr.next());
                        if (throttle.isStolen()) {
                            log.debug("setting handle from {} to {}",throttle.getHandle(),handle);
                            throttle.setHandle(handle);
                            // uses timeout to help prevent steal loops
                           // jmri.util.ThreadingUtil.runOnLayoutDelayed( () -> {
                                throttle.setStolen(false); // sends the reactivation PCL
                           // },500 );
                            throttle.throttleInit(m.getElement(4), m.getElement(5), m.getElement(6), m.getElement(7));
                            _handleExpected = false;
                            return;
                        }
                    }
                    
                    // Initialise new throttle from PLOC data to allow taking over moving trains
                    CbusThrottle throttle;
                    throttle = new CbusThrottle((CanSystemConnectionMemo) adapterMemo, rcvdDccAddr, handle);
                    notifyThrottleKnown(throttle, rcvdDccAddr);
                    throttle.throttleInit(m.getElement(4), m.getElement(5), m.getElement(6), m.getElement(7));
                    softThrottles.put(handle, throttle);
                    _handleExpected = false;
                }
                break;

            case CbusConstants.CBUS_ERR:
                rcvdIntAddr = (m.getElement(1) & 0x3f) * 256 + m.getElement(2);
                rcvdIsLong = (m.getElement(1) & 0xc0) != 0;
                rcvdDccAddr = new DccLocoAddress(rcvdIntAddr, rcvdIsLong);
                int errCode = m.getElement(3);
                
                log.debug("Throttle manager received ERR {} for loco {}", errStr, rcvdDccAddr);
                
                switch (errCode) {
                    case CbusConstants.ERR_LOCO_STACK_FULL:
                    case CbusConstants.ERR_LOCO_ADDRESS_TAKEN:
                        if ( errCode == CbusConstants.ERR_LOCO_STACK_FULL ){
                            errStr = Bundle.getMessage("ERR_LOCO_STACK_FULL") + " " + rcvdIntAddr;
                        }
                        else if ( errCode == CbusConstants.ERR_LOCO_ADDRESS_TAKEN ){
                            errStr = Bundle.getMessage("ERR_LOCO_ADDRESS_TAKEN",rcvdIntAddr);
                        }
                        
                        log.debug("handlexpected {} _dccAddr {} got {} ",_handleExpected,_dccAddr,rcvdDccAddr);
                        
                        if ((_handleExpected) && rcvdDccAddr.equals(_dccAddr)) {
                            
                            // We were expecting an engine report and it matches our address
                            log.debug("Failed throttle request due to ERR");
                            _handleExpected = false;
                            stopThrottleRequestTimer();
                            
                            // if this is the result of a share or steal request,
                            // we need to stop here and inform the ThrottleListener
                            if ( _handleExpectedSecondLevelRequest ){
                                failedThrottleRequest(_dccAddr, errStr);
                                return;
                            }
                            
                            // so this is the message from the 1st normal request
                            // now we check the command station,
                            // and notify the ThrottleListener ()
                            
                            boolean steal = false;
                            boolean share = false;
                            
                            CbusCommandStation cs = (CbusCommandStation) jmri.InstanceManager.getNullableDefault(jmri.CommandStation.class);
        
                            if ( cs != null ) {
                                log.debug("cs says can steal {}, can share {}", cs.isStealAvailable(),cs.isShareAvailable() );
                                steal = cs.isStealAvailable();
                                share = cs.isShareAvailable();
                            }
                            
                            if ( !steal && !share ){
                                failedThrottleRequest(_dccAddr, errStr);
                            }
                            else if ( steal && share ){
                                notifyDecisionRequest(_dccAddr,DecisionType.STEAL_OR_SHARE);
                            }
                            else if ( steal ){
                                notifyDecisionRequest(_dccAddr,DecisionType.STEAL);
                            }
                            else if ( share ){
                                notifyDecisionRequest(_dccAddr,DecisionType.SHARE);
                            }
                        } else {
                            log.debug("ERR address not matched");
                        }
                        break;

                    case CbusConstants.ERR_SESSION_NOT_PRESENT:
                        // most likely called via a command station being reset or
                        // coming back online
                        errStr = Bundle.getMessage("ERR_SESSION_NOT_PRESENT",handle) ;
                        log.warn(errStr);
                        
                        if ((_handleExpected) && rcvdDccAddr.equals(_dccAddr)) {
                            // We were expecting an engine report and it matches our address
                            _handleExpected = false;
                            failedThrottleRequest(_dccAddr, Bundle.getMessage("CBUS_ERROR") + errStr);
                            log.warn("Session not present when expecting a session number");
                        }
                        
                        // check if it's a JMRI throttle session,
                        // Inform the throttle associated with this session handle, if any
                        itr = softThrottles.keySet().iterator();
                        while (itr.hasNext()) {
                            CbusThrottle throttle = softThrottles.get(itr.next());
                            if (throttle.getHandle() == handle) {
                                log.warn("Cancelling JMRI Throttle Session {} for loco {}",
                                    handle,
                                    throttle.getLocoAddress().toString()
                                );
                                
                                attemptRecoverThrottle(throttle);
                                break;
                            }
                        }
                        
                        break;
                    case CbusConstants.ERR_CONSIST_EMPTY:
                        errStr = Bundle.getMessage("ERR_CONSIST_EMPTY") + " " + handle;
                        log.warn(errStr);
                        break;
                    case CbusConstants.ERR_LOCO_NOT_FOUND:
                        log.warn(Bundle.getMessage("ERR_LOCO_NOT_FOUND") + " {}", handle);
                        break;
                    case CbusConstants.ERR_CAN_BUS_ERROR:
                        log.error(Bundle.getMessage("ERR_CAN_BUS_ERROR"));
                        if (!java.awt.GraphicsEnvironment.isHeadless() && !isCanErrorDialogVisible()){
                            jmri.util.ThreadingUtil.runOnGUI(() -> {
                                JOptionPane pane = new JOptionPane(Bundle.getMessage("ERR_CAN_BUS_ERROR"));
                                pane.setMessageType(JOptionPane.ERROR_MESSAGE);
                                canErrorDialog = pane.createDialog(null, Bundle.getMessage("CBUS_ERROR"));
                                canErrorDialog.setModal(false);
                                canErrorDialog.setVisible(true);
                            });
                        }
                        return;
                    case CbusConstants.ERR_INVALID_REQUEST:
                        log.error(Bundle.getMessage("ERR_INVALID_REQUEST"));
                        if (!java.awt.GraphicsEnvironment.isHeadless() && !isInvalidErrorDialogVisible()){
                            jmri.util.ThreadingUtil.runOnGUI(() -> {
                                JOptionPane pane = new JOptionPane(Bundle.getMessage("ERR_INVALID_REQUEST"));
                                pane.setMessageType(JOptionPane.ERROR_MESSAGE);
                                invalidErrorDialog = pane.createDialog(null, Bundle.getMessage("CBUS_ERROR"));
                                invalidErrorDialog.setModal(false);
                                invalidErrorDialog.setVisible(true);
                            });
                        }
                        return;
                    case CbusConstants.ERR_SESSION_CANCELLED:
                        // There will be a session cancelled error for the other throttle(s)
                        // when you are stealing, but as you don't yet have a session id, it
                        // won't match so you will ignore it, then a PLOC will come with that
                        // session id and your requested loco number which is giving it to you.
                        
                        log.debug(Bundle.getMessage("ERR_SESSION_CANCELLED",handle));

                        // Inform the throttle associated with this session handle, if any
                        itr = softThrottles.keySet().iterator();
                        while (itr.hasNext()) {
                            CbusThrottle throttle = softThrottles.get(itr.next());
                            if (throttle.getHandle() == handle) {
                                if (throttle.isStolen()){ // already actioned
                                    log.debug("external steal already actioned, returning");
                                    return;
                                }
                                log.warn("External Steal / Cancel for loco {} Session {} ",throttle.getLocoAddress(), handle );
                                attemptRecoverThrottle(throttle);
                                break;
                            }
                        }
                        break;
                    default:
                        log.error(Bundle.getMessage("ERR_UNKNOWN") + " error code: {}", errCode);
                        break;
                }
                break;

            case CbusConstants.CBUS_DSPD:
                // Find a throttle corresponding to the handle
                itr = softThrottles.keySet().iterator();
                while (itr.hasNext()) {
                    CbusThrottle throttle = softThrottles.get(itr.next());
                    if (throttle.getHandle() == handle) {
                        // Set the throttle session to match the DSPD packet received
                        throttle.updateSpeedSetting(m.getElement(2) & 0x7f);
                        throttle.updateIsForward((m.getElement(2) & 0x80) == 0x80);
                        // if something external to JMRI is sharing a session
                        // dispatch is invalid
                        throttle.setDispatchActive(false);
                    }
                }
                break;

            case CbusConstants.CBUS_DFUN:
                // Find a throttle corresponding to the handle
                itr = softThrottles.keySet().iterator();
                while (itr.hasNext()) {
                    CbusThrottle throttle = softThrottles.get(itr.next());
                    if (throttle.getHandle() == handle) {
                        
                        // if something external to JMRI is sharing a session
                        // dispatch is invalid
                        throttle.setDispatchActive(false);
                        
                        // Set the throttle session to match the DFUN packet received
                        // log.debug("DFUN group: " + m.getElement(2) + " Fns: " + m.getElement(3) + " for session: " + m.getElement(1));
                        switch (m.getElement(2)) {
                            case 1:
                                throttle.updateFunctionGroup1(m.getElement(3));
                                break;
                            case 2:
                                throttle.updateFunctionGroup2(m.getElement(3));
                                break;
                            case 3:
                                throttle.updateFunctionGroup3(m.getElement(3));
                                break;
                            case 4:
                                throttle.updateFunctionGroup4(m.getElement(3));
                                break;
                            case 5:
                                throttle.updateFunctionGroup5(m.getElement(3));
                                break;
                            default:
                                log.error("Unrecognised function group");
                                break;
                        }
                    }
                }
                break;

            case CbusConstants.CBUS_DFNON:
            case CbusConstants.CBUS_DFNOF:
                // Find a throttle corresponding to the handle
                itr = softThrottles.keySet().iterator();
                while (itr.hasNext()) {
                    CbusThrottle throttle = softThrottles.get(itr.next());
                    if (throttle.getHandle() == handle) {
                        
                        // if something external to JMRI is sharing a session
                        // dispatch is invalid
                        throttle.setDispatchActive(false);
                        throttle.updateFunction(m.getElement(2), (opc == CbusConstants.CBUS_DFNON));
                    }
                }
                break;

            case CbusConstants.CBUS_ESTOP:
            case CbusConstants.CBUS_RESTP:
                stopAll();
                break;
            case CbusConstants.CBUS_DKEEP:
                itr = softThrottles.keySet().iterator();
                while (itr.hasNext()) {
                    CbusThrottle throttle = softThrottles.get(itr.next());
                    if (throttle.getHandle() == handle) {
                        // if something external to JMRI is sharing a session
                        // dispatch is invalid
                        throttle.setDispatchActive(false);
                    }
                }
                break;
            
            default:
                break;
        }
    }
    
    /**
     * Attempts Throttle Recovery when a session has been lost
     */
    private void attemptRecoverThrottle(CbusThrottle throttle){
        
        log.debug("start of recovery, current throttle stolen {} session {} num recovr attempts {} hashmap size {}",
            throttle.isStolen(),throttle.getHandle(), throttle.getNumRecoverAttempts(),
            softThrottles.size() );
        
        int oldhandle=throttle.getHandle();
        
        throttle.increaseNumRecoverAttempts();
        
        if ( throttle.getNumRecoverAttempts()>10 ){ // catch runaways
            _handleExpected = false;
            throttle.throttleDispose(); // stop throttle keep-alive messages, send PCL ThrottleConnected false
            showSessionCancelDialogue(throttle.getLocoAddress());
            softThrottles.remove(oldhandle); // remove from local list
            forceDisposeThrottle( throttle.getLocoAddress() ); // remove from JMRI share list
            
        }
        
        throttle.setStolen(true);
        throttle.setHandle(-1);
        
        boolean steal = false;
        boolean share = false;
        
        CbusCommandStation cs = (CbusCommandStation) jmri.InstanceManager.getNullableDefault(jmri.CommandStation.class);
        if ( cs != null ) {
            log.debug("cs says can steal {}, can share {}", cs.isStealAvailable(),cs.isShareAvailable() );
            steal = cs.isStealAvailable();
            share = cs.isShareAvailable();
        }
        if (jmri.InstanceManager.getNullableDefault(ThrottlesPreferences.class) == null) {
            log.debug("Creating new ThrottlesPreference Instance");
            jmri.InstanceManager.store(new ThrottlesPreferences(), ThrottlesPreferences.class);
        }
        ThrottlesPreferences tp = jmri.InstanceManager.getDefault(ThrottlesPreferences.class);
        
        if (share && tp.isSilentShare()){
            // share is available on command station AND silent share preference checked
            log.info("Requesting Silent Share loco {} to regain a session",throttle.getLocoAddress());
            jmri.util.ThreadingUtil.runOnLayoutDelayed( () -> {
                startThrottleRequestTimer(true);
                requestThrottleSetup(throttle.getLocoAddress(), DecisionType.SHARE);
            },1000);
        }
        else if (steal && tp.isSilentSteal()){
            // steal is available on command station AND silent steal preference checked
            log.info("Requesting Silent Steal loco {} to regain a session",throttle.getLocoAddress());
            jmri.util.ThreadingUtil.runOnLayoutDelayed( () -> {
                startThrottleRequestTimer(true);
                requestThrottleSetup(throttle.getLocoAddress(), DecisionType.STEAL);
            },1000);
        } else {
            throttle.throttleDispose(); // stop throttle keep-alive messages, send PCL ThrottleConnected false
            showSessionCancelDialogue(throttle.getLocoAddress());
            softThrottles.remove(oldhandle); // remove from local list
            forceDisposeThrottle( throttle.getLocoAddress() ); // remove from JMRI share list
        }
    }

    /**
     * CBUS has a dynamic Dispatch function, defaulting to false
     * {@inheritDoc}
     */
    @Override
    public boolean hasDispatchFunction() {
        return false;
    }

    /**
     * Any address is potentially a long address
     */
    @Override
    public boolean canBeLongAddress(int address) {
        return address > 0;
    }

    /**
     * Address 127 and below is a short address
     *
     */
    @Override
    public boolean canBeShortAddress(int address) {
        return address < 128;
    }

    /**
     * Short and long address spaces overlap and are not unique
     */
    @Override
    public boolean addressTypeUnique() {
        return false;
    }

    /*
     * Local method for deciding short/long address
     */
    static boolean isLongAddress(int num) {
        return (num >= 128);
    }
    
    /**
     * Hardware has a stealing implementation
     * {@inheritDoc}
     */
    @Override
    public boolean enablePrefSilentStealOption() {
        return true;
    }
    
    /**
     * Hardware has a sharing implementation
     * {@inheritDoc}
     */
    @Override
    public boolean enablePrefSilentShareOption() {
        return true;
    }
    
    /**
     * CBUS Hardware will make its own decision on preferred option
     * <p>
     * This is the default for scripts that do NOT have a GUI for asking what to do when
     * a steal / share decision is required.
     * {@inheritDoc}
     */
    @Override
    protected void makeHardwareDecision(LocoAddress address,DecisionType question){
        // no need to check if share / steal currently enabled on command station,
        // this has already been done to produce the correct question
        switch (question) {
            case STEAL:
                // share has been disabled in command station
                responseThrottleDecision(address, null, DecisionType.STEAL );
                break;
            case SHARE:
                // steal has been disabled in command station
                responseThrottleDecision(address, null, DecisionType.SHARE );
                break;
            case STEAL_OR_SHARE:
                if (jmri.InstanceManager.getNullableDefault(ThrottlesPreferences.class) == null) {
                    log.debug("Creating new ThrottlesPreference Instance");
                    jmri.InstanceManager.store(new ThrottlesPreferences(), ThrottlesPreferences.class);
                }   ThrottlesPreferences tp = jmri.InstanceManager.getDefault(ThrottlesPreferences.class);
                if ( tp.isSilentSteal() ){
                    responseThrottleDecision(address, null, DecisionType.STEAL );
                }
                else {
                    responseThrottleDecision(address, null, DecisionType.SHARE );
                }   break;
            default:
                log.error("Question type {} unknown",question);
                break;
        }
    }

    /**
     * Send a request to steal or share a requested throttle.
     * <p>
     * {@inheritDoc}
     *
     */
    @Override
    public void responseThrottleDecision(LocoAddress address, ThrottleListener l, DecisionType decision) {
        log.debug("Received {} response for Loco {}, listener {}",decision,address,l);
        startThrottleRequestTimer(false);
        requestThrottleSetup(address,decision);
    }
    
    private TimerTask throttleRequestTimer;
    
    /**
     * Start timer to wait for command station to respond to RLOC or GLOC
     */
    private void startThrottleRequestTimer(boolean isRecovery) {
        throttleRequestTimer = new TimerTask() {
            @Override
            public void run() {
                timeout(isRecovery);
            }
        };
        jmri.util.TimerUtil.schedule(throttleRequestTimer, ( THROTTLE_TIMEOUT ) );
    }
    
    private void stopThrottleRequestTimer(){
        if (throttleRequestTimer!=null){
            throttleRequestTimer.cancel();
        }
        throttleRequestTimer = null;
    }

    /**
     * Internal routine to notify failed throttle request a timeout
     */
    private void timeout(boolean isRecovery) {
        log.debug("Throttle request (RLOC or PLOC) timed out");
        stopThrottleRequestTimer();
        if (isRecovery){
            log.warn("Session recovery not possible for {}",_dccAddr);
            forceDisposeThrottle( _dccAddr ); // remove from JMRI share list
            
            Iterator<Integer> itr = softThrottles.keySet().iterator();
            while (itr.hasNext()) {
                CbusThrottle throttle = softThrottles.get(itr.next());
                if (throttle.getLocoAddress() == _dccAddr) {
                    throttle.throttleDispose();
                    showSessionCancelDialogue(_dccAddr);
                    softThrottles.remove(throttle.getHandle());
                }
            }
        }
        else { // not in recovery, normal request timeout, is command station connected?
            failedThrottleRequest(_dccAddr, Bundle.getMessage("ERR_THROTTLE_TIMEOUT"));
        }
    }

    /**
     * MERG CBUS Throttle sessions default to 128 SS.
     * This can be changed by a subsequent message from Throttle to CS,
     * or by message from Command Station to CbusThrottle.
     * Supported modes are 128, 28 and 14.
     * {@inheritDoc }
     */
    @Override
    public EnumSet<SpeedStepMode> supportedSpeedModes() {
        return EnumSet.of(SpeedStepMode.NMRA_DCC_128
                , SpeedStepMode.NMRA_DCC_28
                , SpeedStepMode.NMRA_DCC_14);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean disposeThrottle(DccThrottle t, jmri.ThrottleListener l) {
        log.debug("disposeThrottle called for " + t);
        if (t instanceof CbusThrottle) {
            log.debug("Cbus Dispose calling abstract Throttle manager dispose");
            if (super.disposeThrottle(t, l)) {
                
                CbusThrottle lnt = (CbusThrottle) t;
                lnt.releaseFromCommandStation();
                lnt.throttleDispose();
                // forceDisposeThrottle( (DccLocoAddress) lnt.getLocoAddress() );
                log.debug("called throttleDispose");
                return true;
            }
        }
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void updateNumUsers( LocoAddress la, int numUsers ){
        log.debug("throttle {} notification that num. users is now {}",la,numUsers);
        Iterator<Integer> itr = softThrottles.keySet().iterator();
        while (itr.hasNext()) {
            CbusThrottle throttle = softThrottles.get(itr.next());
            if (throttle.getLocoAddress() == la ) {
                if ( ( numUsers == 1 ) && throttle.getSpeedSetting() > 0 ) {
                    throttle.setDispatchActive(true);
                    return;
                }
                throttle.setDispatchActive(false);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancelThrottleRequest(LocoAddress address, ThrottleListener l) {
        
        // calling super removes the ThrottleListener from the callback list,
        // The listener which has just sent the cancel doesn't need notification
        // of the cancel but other listeners might
        super.cancelThrottleRequest(address, l);
        failedThrottleRequest(address, "Throttle Request " + address + " Cancelled.");
    }
    
    private final static Logger log = LoggerFactory.getLogger(CbusThrottleManager.class);
}
