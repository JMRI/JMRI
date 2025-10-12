package jmri.jmrix.bidib;

import java.util.BitSet;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.SpeedStepMode;
import jmri.DccLocoAddress;
import jmri.LocoAddress;
import jmri.jmrix.AbstractThrottle;
//import jmri.Throttle;

import org.bidib.jbidibc.messages.enums.DirectionEnum;
import org.bidib.jbidibc.core.DefaultMessageListener;
import org.bidib.jbidibc.messages.DriveState;
import org.bidib.jbidibc.core.MessageListener;
import org.bidib.jbidibc.messages.Node;
import org.bidib.jbidibc.messages.enums.CsQueryTypeEnum;
import org.bidib.jbidibc.messages.enums.DriveAcknowledge;
import org.bidib.jbidibc.messages.enums.SpeedStepsEnum;
import org.bidib.jbidibc.messages.message.CommandStationQueryMessage;
import org.bidib.jbidibc.messages.message.CommandStationDriveMessage;
import org.bidib.jbidibc.messages.utils.NodeUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * An implementation of DccThrottle with code specific to an BiDiB connection.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Eckart Meyer Copyright (C) 2019-2023
 */
public class BiDiBThrottle extends AbstractThrottle {
    
    /* Unfortunately one of the recent changes removes the possibility to set the
     * current status of the functions as received from BiDiB, because
     * AbstractThrottle now uses a private array FUNCTION_BOOLEAN_ARRAY[].
     * Using set provided setFx functions would send out the new status again.
     *
     * So we have no choice and have to duplicate this array here and also
     * some of the functions :-(
     */
    
    
    private final BitSet activeFunctions;// = new BitSet(29); //0..28
    private final BitSet functions;// = new BitSet(29);
    private float oldSpeed = 0.0f;

    private BiDiBTrafficController tc = null;
    MessageListener messageListener = null;
    protected Node node = null;
    
    // sendDeregister is a little hack to enable the user to set the loco to sleep
    // i.e. remove it from the DCC memory of the command station. The loco
    // won't be updated then until another MSG_CS_DRIVE message for that
    // loco will arrive.
    private boolean sendDeregister = false;

    /**
     * Constructor.
     * @param memo system connection memo to use
     * @param locoAddress DCC loco locoAddress
     */
//    @SuppressWarnings("OverridableMethodCallInConstructor")
    public BiDiBThrottle(BiDiBSystemConnectionMemo memo, DccLocoAddress locoAddress) {
        super(memo);
        this.tc = memo.getBiDiBTrafficController();
        node = tc.getFirstCommandStationNode();
        log.trace("++ctor");
//        setSpeedStepMode(SpeedStepMode128);
        setSpeedStepMode(SpeedStepMode.NMRA_DCC_128);

        // cache settings. It would be better to read the actual state or at least cache this somethere
        this.speedSetting = 0;
/*
        this.f0 = false;
        this.f1 = false;
        this.f2 = false;
        this.f3 = false;
        this.f4 = false;
        this.f5 = false;
        this.f6 = false;
        this.f7 = false;
        this.f8 = false;
        this.f9 = false;
        this.f10 = false;
        this.f11 = false;
        this.f12 = false;
        this.f13 = false;
        this.f14 = false;
        this.f15 = false;
        this.f16 = false;
        this.f17 = false;
        this.f18 = false;
        this.f19 = false;
        this.f20 = false;
        this.f21 = false;
        this.f22 = false;
        this.f23 = false;
        this.f24 = false;
        this.f25 = false;
        this.f26 = false;
        this.f27 = false;
        this.f28 = false;
*/
        this.locoAddress = locoAddress;
        this.isForward = true;

        // jbidibc wants the functions as a BitSet ...
        activeFunctions = new BitSet(29); //0..28
        functions = new BitSet(29);
        for (int bitIndex = 0; bitIndex < activeFunctions.size(); bitIndex++) {
            //log.trace("init function {}", bitIndex);
            activeFunctions.set(bitIndex, true); //all functions enabled for now... no way to ask the loco as far as I can see
            functions.set(bitIndex, false); //all off
        }
        
        createThrottleListener();
        
        //requestStateDelayed();
        requestState();
    }

    DccLocoAddress locoAddress;
    
    
    /**
     * Request the state of a loco from BiDiB
     */
    public void requestState() {
        log.debug("request csState for addr {}", locoAddress);
        tc.sendBiDiBMessage(
                new CommandStationQueryMessage(CsQueryTypeEnum.LOCO_LIST, this.locoAddress.getNumber()), node); //send to command station node
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LocoAddress getLocoAddress() {
        return locoAddress;
    }

    /**
     * Send the message to set the state of functions F0, F1, F2, F3, F4.
     */
    @Override
    protected void sendFunctionGroup1() {
        log.trace("sendFunctionGroup1");
        sendDriveCommand(false);
    }

    /**
     * Send the message to set the state of functions F5, F6, F7, F8.
     */
    @Override
    protected void sendFunctionGroup2() {
        log.trace("sendFunctionGroup2");
        sendDriveCommand(false);
    }

    /**
     * Send the message to set the state of functions F9, F10, F11, F12.
     */
    @Override
    protected void sendFunctionGroup3() {
        log.trace("sendFunctionGroup3");
        sendDriveCommand(false);
    }

    /**
     * Send the message to set the state of functions F13, F14, F15, F16, F17,
     * F18, F19, F20
     */
    @Override
    protected void sendFunctionGroup4() {
        log.trace("sendFunctionGroup4");
        sendDriveCommand(false);
    }

    /**
     * Send the message to set the state of functions F21, F22, F23, F24, F25,
     * F26, F27, F28
     */
    @Override
    protected void sendFunctionGroup5() {
        log.trace("sendFunctionGroup5");
        sendDriveCommand(false);
    }

    /**
     * Set the speed {@literal &} direction.
     *
     * @param speed Number from 0 to 1; less than zero is emergency stop
     */
    @SuppressFBWarnings(value = "FE_FLOATING_POINT_EQUALITY") // OK to compare floating point, notify on any change
    @Override
    public void setSpeedSetting(float speed) {
        synchronized(this) {
            oldSpeed = this.speedSetting;
            this.speedSetting = speed; //sendDriveCommand needs it - TODO: should be redesigned
        
            if (sendDriveCommand(true)) {
                if (log.isDebugEnabled()) {
                    log.debug("setSpeedSetting= {}",speed);
                }
                this.speedSetting = oldSpeed; //super.setSpeedSetting needs the old speed here and then sets the new one. As sayed, this should be redesigned
                super.setSpeedSetting(speed);
            }
            else {
                this.speedSetting = oldSpeed;
                //notifyPropertyChangeListener("SpeedSetting", null, oldSpeed);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setIsForward(boolean forward) {
        boolean old = isForward;
        isForward = forward; //see above
        
        if (sendDriveCommand(false)) {
            if (log.isDebugEnabled()) {
                log.debug("setIsForward= {}", forward);
            }
            if (old != forward) {
                isForward = old;
                super.setIsForward(forward);
            }
        }
        else {
            isForward = old;
            //notifyPropertyChangeListener("IsForward", null, old);
        }
    }
    
    /**
     * Internal send method for this class.
     * Allocates speed and function data and constructs a BiDiB message
     * 
     * @param isSpeedSet false if not yet
     * @return true if successful
     */
    protected boolean sendDriveCommand(boolean isSpeedSet) {
        int addr;
        SpeedStepsEnum mode;
        Integer speed;
        
        synchronized(this) {
            if (!isSpeedSet  &&  this.speedSetting < 0) {
                this.speedSetting = 0; //remove estop condition when changing something other than speed
            }
            // BiDiB has only one message to set speed, direction and all functions
            addr = locoAddress.getNumber();
            switch(this.speedStepMode) {
                case NMRA_DCC_14:
                    mode = SpeedStepsEnum.DCC14; break;
                case NMRA_DCC_28:
                    mode = SpeedStepsEnum.DCC28; break;
                default:
                    mode = SpeedStepsEnum.DCC128; break;
            }
            speed = intSpeed(speedSetting);
        }
        DirectionEnum dir = isForward ? DirectionEnum.FORWARD : DirectionEnum.BACKWARD;
/* old - before v5.1.2
        functions.set(0, getF0());
        functions.set(1, getF1());
        functions.set(2, getF2());
        functions.set(3, getF3());
        functions.set(4, getF4());
        functions.set(5, getF5());
        functions.set(6, getF6());
        functions.set(7, getF7());
        functions.set(8, getF8());
        functions.set(9, getF9());
        functions.set(10, getF10());
        functions.set(11, getF11());
        functions.set(12, getF12());
        functions.set(13, getF13());
        functions.set(14, getF14());
        functions.set(15, getF15());
        functions.set(16, getF16());
        functions.set(17, getF17());
        functions.set(18, getF18());
        functions.set(19, getF19());
        functions.set(20, getF20());
        functions.set(21, getF21());
        functions.set(22, getF22());
        functions.set(23, getF23());
        functions.set(24, getF24());
        functions.set(25, getF25());
        functions.set(26, getF26());
        functions.set(27, getF27());
        functions.set(28, getF28());
*/
        for (int i = 0; i <= 28; i++) {
            functions.set(i, getFunction(i));
        }
        
        BitSet curActiveFunctions = (BitSet)activeFunctions.clone();

        if (sendDeregister) {
            sendDeregister = false;
            //functions.clear();
            curActiveFunctions.clear();
            speed = null;
            log.info("deregister loco reuqested ({})", addr);            
        }


        log.debug("sendBiDiBMessage: addr: {}, mode: {}, direction: {}, speed: {}, active functions: {}, enabled functions: {}",
                addr, mode, dir, speed, curActiveFunctions.toByteArray(), functions.toByteArray());
        
//direct message variant, fully async
        tc.sendBiDiBMessage(
                new CommandStationDriveMessage(addr, mode, speed, dir, curActiveFunctions, functions),
                node); //send to command station node

        return true;
    }

/// just to see what happens... seems that those methods won't be called by JMRI
//        @Override
//    public void dispatch(ThrottleListener l) {
//        log.debug("BiDiBThrottle.dispatch: {}", l);
//        super.dispatch(l);
//    }
//
//    @Override
//    public void release(ThrottleListener l) {
//        log.debug("BiDiBThrottle.release: {}", l);
//        super.release(l);
//    }
///////////////////////////

    protected void receiveFunctions(byte[] functions) {
        
        updateFunction(0, (functions[0] & 0x10) != 0);
        updateFunction(1, (functions[0] & 0x01) != 0);
        updateFunction(2, (functions[0] & 0x02) != 0);
        updateFunction(3, (functions[0] & 0x04) != 0);
        updateFunction(4, (functions[0] & 0x08) != 0);

        updateFunction(5,  (functions[1] & 0x01) != 0);
        updateFunction(6,  (functions[1] & 0x02) != 0);
        updateFunction(7,  (functions[1] & 0x04) != 0);
        updateFunction(8,  (functions[1] & 0x08) != 0);
        updateFunction(9,  (functions[1] & 0x10) != 0);
        updateFunction(10, (functions[1] & 0x20) != 0);
        updateFunction(11, (functions[1] & 0x40) != 0);
        updateFunction(12, (functions[1] & 0x80) != 0);

        updateFunction(13, (functions[2] & 0x01) != 0);
        updateFunction(14, (functions[2] & 0x02) != 0);
        updateFunction(15, (functions[2] & 0x04) != 0);
        updateFunction(16, (functions[2] & 0x08) != 0);
        updateFunction(17, (functions[2] & 0x10) != 0);
        updateFunction(18, (functions[2] & 0x20) != 0);
        updateFunction(19, (functions[2] & 0x40) != 0);
        updateFunction(20, (functions[2] & 0x80) != 0);

        updateFunction(21, (functions[3] & 0x01) != 0);
        updateFunction(22, (functions[3] & 0x02) != 0);
        updateFunction(23, (functions[3] & 0x04) != 0);
        updateFunction(24, (functions[3] & 0x08) != 0);
        updateFunction(25, (functions[3] & 0x10) != 0);
        updateFunction(26, (functions[3] & 0x20) != 0);
        updateFunction(27, (functions[3] & 0x40) != 0);
        updateFunction(28, (functions[3] & 0x80) != 0);

/*
        not possible any more since 4.19.5 - updateFunction is now used, see above
        this.f0 = receiveFunction(Throttle.F0, this.f0, functions[0] & 0x10);
        this.f1 = receiveFunction(Throttle.F1, this.f1, functions[0] & 0x01);
        this.f2 = receiveFunction(Throttle.F2, this.f2, functions[0] & 0x02);
        this.f3 = receiveFunction(Throttle.F3, this.f3, functions[0] & 0x04);
        this.f4 = receiveFunction(Throttle.F4, this.f4, functions[0] & 0x08);
        
        this.f5 = receiveFunction(Throttle.F5, this.f5, functions[1] & 0x01);
        this.f6 = receiveFunction(Throttle.F6, this.f6, functions[1] & 0x02);
        this.f7 = receiveFunction(Throttle.F7, this.f7, functions[1] & 0x04);
        this.f8 = receiveFunction(Throttle.F8, this.f8, functions[1] & 0x08);
        this.f9 = receiveFunction(Throttle.F9, this.f9, functions[1] & 0x10);
        this.f10 = receiveFunction(Throttle.F10, this.f10, functions[1] & 0x20);
        this.f11 = receiveFunction(Throttle.F11, this.f11, functions[1] & 0x40);
        this.f12 = receiveFunction(Throttle.F12, this.f12, functions[1] & 0x80);
        
        this.f13 = receiveFunction(Throttle.F13, this.f13, functions[2] & 0x01);
        this.f14 = receiveFunction(Throttle.F14, this.f14, functions[2] & 0x02);
        this.f15 = receiveFunction(Throttle.F15, this.f15, functions[2] & 0x04);
        this.f16 = receiveFunction(Throttle.F16, this.f16, functions[2] & 0x08);
        this.f17 = receiveFunction(Throttle.F17, this.f17, functions[2] & 0x10);
        this.f18 = receiveFunction(Throttle.F18, this.f18, functions[2] & 0x20);
        this.f19 = receiveFunction(Throttle.F19, this.f19, functions[2] & 0x40);
        this.f20 = receiveFunction(Throttle.F20, this.f20, functions[2] & 0x80);
        
        this.f21 = receiveFunction(Throttle.F21, this.f21, functions[3] & 0x01);
        this.f22 = receiveFunction(Throttle.F22, this.f22, functions[3] & 0x02);
        this.f23 = receiveFunction(Throttle.F23, this.f23, functions[3] & 0x04);
        this.f24 = receiveFunction(Throttle.F24, this.f24, functions[3] & 0x08);
        this.f25 = receiveFunction(Throttle.F25, this.f25, functions[3] & 0x10);
        this.f26 = receiveFunction(Throttle.F26, this.f26, functions[3] & 0x20);
        this.f27 = receiveFunction(Throttle.F27, this.f27, functions[3] & 0x40);
        this.f28 = receiveFunction(Throttle.F28, this.f28, functions[3] & 0x80);
*/
    }
 /*
    protected boolean receiveFunction(String property, boolean curStat, int newStat) {
        boolean old = curStat;
        curStat = (newStat != 0);
        log.trace("  set fn: property: {}, old: {}, new: {}", property, old, curStat);
        if (old != curStat) {
            notifyPropertyChangeListener(property, old, curStat);
        }
        return (newStat != 0);
    }
 */
    
    protected void receiveSpeedSetting(int speed) {
        synchronized(this) {
            oldSpeed = this.speedSetting;
            float newSpeed = floatSpeed(speed, 127);
            log.trace("  set speed: old: {}, new: {} {}", oldSpeed, newSpeed, speed);
            super.setSpeedSetting(newSpeed);
        }
    }
    
    protected void receiveIsForward(boolean forward) {
        boolean old = isForward;
        log.trace("  set isForward: old: {}, new: {}", old, forward);
        if (old != forward) {
            //isForward = forward;
        //notifyPropertyChangeListener("IsForward", old, forward);//TODO: use firePropertyChange or super.setIsForward
        super.setIsForward(forward);
        }
    }
    
    /**
     * Convert speed step value to floating value.
     * This is the oppsite of AbstractThrottle.intSpeed(speed, steps)
     * 
     * @param speed as integer from 1...steps
     * @param steps number if speed steps
     * @return speed as floating number from 0.0 to 1.0
     */
    public float floatSpeed(int speed, int steps) {
        // test that speed is 1 for emergency stop
        if (speed == 1) {
            return -1.0f; // emergency stop
        }
        else if (speed == 0) {
            return 0.0f;
        }
        float value = (float)(speed - 1) / (float)(steps - 1);
        log.trace("speed: {}, steps: {}, float value: {}", speed, steps, value);
        if (value > 1.0) {
            return 1.0f;
        }
        else if (value < 0.0) {
            return 0.0f;
        }
        return value;
    }

    protected void driveReceive(byte[] address, DriveState driveState) {
        if (NodeUtils.isAddressEqual(node.getAddr(), address)  &&  locoAddress.getNumber() == driveState.getAddress()) {
            log.debug("THROTTLE csDrive was signalled, node addr: {}, loco addr: {}, state: {}",
                    address, driveState.getAddress(), driveState);
            // set speed
            receiveSpeedSetting(driveState.getSpeed());
            receiveIsForward(driveState.getDirection() == DirectionEnum.FORWARD);
            receiveFunctions(driveState.getFunctions());
        }
    }    

    private void createThrottleListener() {
        messageListener = new DefaultMessageListener() {
            @Override
            public void csDriveAcknowledge(byte[] address, int messageNum, int dccAddress, DriveAcknowledge state, Integer acknowledgedMessageNumber) { //new
//            public void csDriveAcknowledge(byte[] address, int dccAddress, DriveAcknowledge state) { //12.5
                //log.trace("csDriveAcknowledge: node addr: {}, Lok addr: {}, Ack: {}", address, dccAddress, state, acknowledgedMessageNumber);
                //log.trace("csDriveAcknowledge: Ack: {}, Lok addr: {}, node: {}", state, dccAddress, node);
                if (NodeUtils.isAddressEqual(node.getAddr(), address)  &&  locoAddress.getNumber() == dccAddress) {
                    log.trace("THROTTLE: drive ackn was signalled, acknowledge: {}, dccAddress: {}, node: {}", state, dccAddress, node);
                    if (state == DriveAcknowledge.NOT_ACKNOWLEDGED) {
                        log.warn("setDrive was not acknowledged on node: {}, Lok addr: {}", address, dccAddress);
                    }
                }
            }
            @Override
//            public void csDriveState(byte[] address, DriveState driveState) {
            public void csDriveState(byte[] address, int messageNum, int opCode, DriveState driveState) {
                log.trace("csDriveState: node addr: {}, opCode: {}, DriveState: {}", address, opCode, driveState);
                //log.trace("              node addr: {}, locoAddress: {}", node.getAddr(), locoAddress.getNumber());
                if (NodeUtils.isAddressEqual(node.getAddr(), address)  &&  locoAddress.getNumber() == driveState.getAddress()) {
                    //log.debug("THROTTLE: Drive State was signalled, DriveState: {}, node: {}", driveState, node);
                    driveReceive(address, driveState);
                }
            }
            @Override
            public void csDriveManual(byte[] address, int messageNum, DriveState driveState) {
                //log.trace("csDriveManual: node addr: {}, DriveState: {}", address, driveState);
                if (NodeUtils.isAddressEqual(node.getAddr(), address)  &&  locoAddress.getNumber() == driveState.getAddress()) {
                    log.trace("THROTTLE: Drive Manual was signalled, DriveState: {}, node: {}", driveState, node);
                    driveReceive(address, driveState);
                }
            }
        };
        tc.addMessageListener(messageListener);        
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void throttleDispose() {
        log.trace("dispose throttle addr {}", locoAddress);
        synchronized(this) {
            if (this.speedSetting < 0) {
                sendDeregister = true;
                this.speedSetting = 0;
                sendDriveCommand(false); //will send a DCC deregister message
            }
        }
        //tc.removeMessageListener(messageListener); //TEMP 
        active = false;
        finishRecord();
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(BiDiBThrottle.class);

}
