package jmri.jmrit.z21server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.Arrays;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.Iterator;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.PowerManager;
import jmri.jmrit.throttle.ThrottleFrame;
import jmri.jmrit.throttle.ThrottleFrameManager;
import jmri.DccThrottle;

/**
 * Handle X-BUS Protokoll (header type 0x40).
 * Only function to handle a loco throttle have been implemented.
 * 
 * @author Jean-Yves Roda (C) 2023
 * @author Eckart Meyer (C) 2025 (enhancements, WlanMaus support)
 */

public class Service40 {
    private static final String moduleIdent = "[Service 40] ";
    private static PropertyChangeListener changeListener = null;

    private final static Logger log = LoggerFactory.getLogger(Service40.class);

/**
 * Set a listener to be called on track power manager events.
 * The listener is called with the Z21 LAN_X_BC_TRACK_POWER_ON/OFF packet to
 * be sent to the client.
 * 
 * Note that throttle changes are handled in the AppClient class.
 * 
 * @param cl - listener class
 */
    public static void setChangeListener(PropertyChangeListener cl) {
        changeListener = cl;
        PowerManager powerMgr = InstanceManager.getNullableDefault(PowerManager.class);
        if (powerMgr != null) {
            powerMgr.addPropertyChangeListener( (PropertyChangeEvent pce) -> {
                if (changeListener != null) {
                    log.trace("Service40: power change event: {}", pce);
                    changeListener.propertyChange(new PropertyChangeEvent(pce.getSource(), "trackpower-change", null, buildTrackPowerPacket()));
                }
            });
        }
    }

/**
 * Handle a X-Bus command.
 * 
 * @param data - the Z21 packet bytes without data length and header.
 * @param clientAddress - the sending client's InetAddress
 * @return a response packet to be sent to the client or null if nothing is to sent (yet).
 */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "PZLA_PREFER_ZERO_LENGTH_ARRAYS",
    justification = "Messages can be of any length, null is used to indicate absence of message for caller")
    public static byte[] handleService(byte[] data, InetAddress clientAddress) {
        int command = data[0];
        switch (command){
            case (byte)0x21:
                return handleHeader21(data[1]);
            case (byte)0xE3:
                return handleHeaderE3(Arrays.copyOfRange(data, 1, 4), clientAddress);
            case (byte)0xE4:
                return handleHeaderE4(Arrays.copyOfRange(data, 1, 5), clientAddress);
            case (byte)0x43:
                return handleHeader43(Arrays.copyOfRange(data, 1, 3), clientAddress);
            case (byte)0x53:
                return handleHeader53(Arrays.copyOfRange(data, 1, 4), clientAddress);
            case (byte)0x80:
                return handleHeader80();
            case (byte)0xF1:
                return handleHeaderF1();
            default:
                log.debug("{} Header {} not yet supported", moduleIdent, Integer.toHexString(command & 0xFF));
                break;
        }
        return null;
    }

/**
 * Handle a LAN_X_GET_* commands.
 * 
 * @param db0 - X-Bus subcommand
 * @return a response packet to be sent to the client or null if nothing is to sent (yet).
 */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "PZLA_PREFER_ZERO_LENGTH_ARRAYS",
    justification = "Messages can be of any length, null is used to indicate absence of message for caller")
    private static byte[] handleHeader21(int db0){
        switch (db0){
            case 0x21:
                // Get z21 version
                break;
            case 0x24:
                // Get z21 status
                byte[] answer = new byte[8];
                answer[0] = (byte) 0x08;
                answer[1] = (byte) 0x00;
                answer[2] = (byte) 0x40;
                answer[3] = (byte) 0x00;
                answer[4] = (byte) 0x62;
                answer[5] = (byte) 0x22;
                answer[6] = (byte) 0x00;
                PowerManager powerMgr = InstanceManager.getNullableDefault(PowerManager.class);
                if (powerMgr != null) {
                    if (powerMgr.getPower() != PowerManager.ON) {
                        answer[6] |= 0x02;
                    }
                }
                answer[7] = ClientManager.xor(answer);
                return answer;
            case (byte) 0x80:
                log.info("{} Set track power to off", moduleIdent);
                return setTrackPower(false);
            case (byte) 0x81:
                log.info("{} Set track power to on", moduleIdent);
                return setTrackPower(true);
            default:
                break;
        }
        return null;
    }
    
/**
 * Set track power on to JMRI.
 * 
 * @param state - true to switch ON, false to switch OFF
 * @return a response packet to be sent to the client or null if nothing is to sent (yet).
 */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "PZLA_PREFER_ZERO_LENGTH_ARRAYS",
    justification = "Messages can be of any length, null is used to indicate absence of message for caller")
    private static byte[] setTrackPower(boolean state) {
        PowerManager powerMgr = InstanceManager.getNullableDefault(PowerManager.class);
        if (powerMgr != null) {
            try {
                powerMgr.setPower(state ? PowerManager.ON : PowerManager.OFF);
            } catch (JmriException ex) {
                log.error("Cannot set power from z21");
                return buildTrackPowerPacket(); //return power off
            }
        }
        // response packet is sent from the property change event
        //return buildTrackPowerPacket();
        return null;
    }

/**
 * Build a LAN_X_BC_TRACK_POWER_ON or LAN_X_BC_TRACK_POWER_OFF packet.
 * @return the packet
 */
    private static byte[] buildTrackPowerPacket() {
        // LAN_X_BC_TRACK_POWER_ON/OFF
        byte[] trackPowerPacket =  new byte[7];
        trackPowerPacket[0] = (byte) 0x07;
        trackPowerPacket[1] = (byte) 0x00;
        trackPowerPacket[2] = (byte) 0x40;
        trackPowerPacket[3] = (byte) 0x00;
        trackPowerPacket[4] = (byte) 0x61;
        trackPowerPacket[5] = (byte) 0x00; //preset power off
        PowerManager powerMgr = InstanceManager.getNullableDefault(PowerManager.class);
        if (powerMgr != null) {
            trackPowerPacket[5] = (byte) (powerMgr.getPower() == PowerManager.ON ? 0x01 : 0x00);
        }
        trackPowerPacket[6] = ClientManager.xor(trackPowerPacket);
        return trackPowerPacket;
    }

    
/**
 * Handle a LAN_X_GET_LOCO_INFO command
 * 
 * @param data - the Z21 packet bytes without data length, header and X-header
 * @param clientAddress - the sending client's InetAddress
 * @return a response packet to be sent to the client or null if nothing is to sent (yet).
 */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "PZLA_PREFER_ZERO_LENGTH_ARRAYS",
    justification = "Messages can be of any length, null is used to indicate absence of message for caller")
    private static byte[] handleHeaderE3(byte[] data, InetAddress clientAddress) {
        int db0 = data[0];
        if (db0 == (byte)0xF0) {
            // Get loco status command
            int locomotiveAddress = (((data[1] & 0xFF) & 0x3F) << 8) + (data[2] & 0xFF);
            log.debug("{} Get loco no {} status", moduleIdent, locomotiveAddress);

            ClientManager.getInstance().registerLocoIfNeeded(clientAddress, locomotiveAddress);

            return ClientManager.getInstance().getLocoStatusMessage(clientAddress, locomotiveAddress);

        } else {
            log.debug("{} Header E3 with function {} is not supported", moduleIdent,  Integer.toHexString(db0));
        }
        return null;
    }

/**
 * Handle LAN_X_SET_LOCO_* commands
 * 
 * @param data - the Z21 packet bytes without data length, header and X-header
 * @param clientAddress - the sending client's InetAddress
 * @return a response packet to be sent to the client or null if nothing is to sent (yet).
 */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "PZLA_PREFER_ZERO_LENGTH_ARRAYS",
    justification = "Messages can be of any length, null is used to indicate absence of message for caller")
    private static byte[] handleHeaderE4(byte[] data, InetAddress clientAddress) {
        if (data[0] == 0x13) {
            // handle LAN_X_SET_LOCO_DRIVE - 128 steps only, others are not supported
            int locomotiveAddress = (((data[1] & 0xFF) & 0x3F) << 8) + (data[2] & 0xFF);
            int rawSpeedData = data[3] & 0xFF;
            boolean bForward = ((rawSpeedData & 0x80) >> 7) == 1;
            int actualSpeed = rawSpeedData & 0x7F;
            log.debug("Set loco no {} direction {} with speed {}",locomotiveAddress, (bForward ? "FWD" : "RWD"), actualSpeed);

            ClientManager.getInstance().setLocoSpeedAndDirection(clientAddress, locomotiveAddress, actualSpeed, bForward);

            // response packet is sent from the property change event
            //return ClientManager.getInstance().getLocoStatusMessage(clientAddress, locomotiveAddress);
        }
        else if (data[0] == (byte)0xF8) {
            // handle LAN_X_SET_LOCO_FUNCTION
            int locomotiveAddress = (((data[1] & 0xFF) & 0x3F) << 8) + (data[2] & 0xFF);
            // function switch type: 0x00 = OFF, 0x01 = ON, 0x20 = TOGGLE
            // Z21 app always sends ON or OFF, WLANmaus always TOGGLE
            // TOGGLE is done in clientManager.setLocoFunction().
            int functionSwitchType = ((data[3] & 0xFF) & 0xC0) >> 6;
            int functionNumber = (data[3] & 0xFF) & 0x3F;
            if (log.isDebugEnabled()) {
                String cmd = ((functionSwitchType & 0x01) == 0x01) ? "ON" : "OFF";
                if ((functionSwitchType & 0x03) == 0x02) {
                    cmd = "TOGGLE";
                }
                log.debug("Set loco no {} function no {}: {}", locomotiveAddress, functionNumber, cmd);
            }

            ClientManager.getInstance().setLocoFunction(clientAddress, locomotiveAddress, functionNumber, functionSwitchType);

            // response packet is sent from the property change event
            //return ClientManager.getInstance().getLocoStatusMessage(clientAddress, locomotiveAddress);
        }
        return null;
    }
    
/**
 * Handle LAN_X_GET_TURNOUT_INFO command.
 * Note: JMRI has no concept of turnout numbers as with the Z21 protocol.
 * So this would only work of mapping tables have already been set by user.
 * 
 * @param data - the Z21 packet bytes without data length, header and X-header
 * @param clientAddress - the sending client's InetAddress
 * @return a response packet to be sent to the client or null if nothing is to sent (yet).
 */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "PZLA_PREFER_ZERO_LENGTH_ARRAYS",
    justification = "Messages can be of any length, null is used to indicate absence of message for caller")
    private static byte[] handleHeader43(byte[] data, InetAddress clientAddress) {
        // Get turnout status command
        int turnoutNumber = ((data[0] & 0xFF) << 8) + (data[1] & 0xFF);
        log.debug("{} Get turnout no {} status", moduleIdent, turnoutNumber);
        return ClientManager.getInstance().getTurnoutStatusMessage(clientAddress, turnoutNumber);
    }

/**
 * Handle LAN_X_SET_TURNOUT command.
 * Note: JMRI has no concept of turnout numbers as with the Z21 protocol.
 * So this would only work of mapping tables have already been set by user.
 * 
 * @param data - the Z21 packet bytes without data length, header and X-header
 * @param clientAddress - the sending client's InetAddress
 * @return a response packet to be sent to the client or null if nothing is to sent (yet).
 */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "PZLA_PREFER_ZERO_LENGTH_ARRAYS",
    justification = "Messages can be of any length, null is used to indicate absence of message for caller")
    private static byte[] handleHeader53(byte[] data, InetAddress clientAddress) {
        // Set turnout
        // WlanMaus sends in bit 0 of data[2]:
        // 0x00 - Turnout thrown button pressed (diverging, unstraight, not main line)
        // 0x01 / Turnout closed button pressed (straight, main line)
        int turnoutNumber = ((data[0] & 0xFF) << 8) + (data[1] & 0xFF);
        log.debug("{} Set turnout no {} to state {}", moduleIdent, turnoutNumber, data[2] & 0xFF);
        if ( (data[2] & 0x08) == 0x08) { //only use "activation", ignore "deactivation"
            ClientManager.getInstance().setTurnout(clientAddress, turnoutNumber, (data[2] & 0x1) == 0x00);
        }
        return ClientManager.getInstance().getTurnoutStatusMessage(clientAddress, turnoutNumber);
    }

/**
 * Handle LAN_X_SET_STOP command.
 * Stop the locos for all throttles found in JMRI.
 * 
 * @return a response packet to be sent to the client or null if nothing is to sent (yet).
 */
    private static byte[] handleHeader80() {
        log.info("{} Stop all locos", moduleIdent);
        Iterator<ThrottleFrame> tpi = InstanceManager.getDefault(ThrottleFrameManager.class).getThrottlesListPanel().getTableModel().iterator();
        while (tpi.hasNext()) {
            DccThrottle t = tpi.next().getAddressPanel().getThrottle();
            if (t != null) {
                t.setSpeedSetting(-1);
            }
        }
        // send LAN_X_BC_STOPPED packet
        byte[] stoppedPacket =  new byte[7];
        stoppedPacket[0] = (byte) 0x07;
        stoppedPacket[1] = (byte) 0x00;
        stoppedPacket[2] = (byte) 0x40;
        stoppedPacket[3] = (byte) 0x00;
        stoppedPacket[4] = (byte) 0x81;
        stoppedPacket[5] = (byte) 0x00;
        stoppedPacket[6] = ClientManager.xor(stoppedPacket);
        return stoppedPacket;
    }

/**
 * Handle LAN_X_GET_FIRMWARE_VERSION command.
 * Of course, since we are not a Z21 command station, the version number
 * does not make sense. But for the case that the client behaves different
 * for Z21 command station software version, we just return the
 * currently newest version 1.43 (January 2025).
 * 
 * @return a response packet to be sent to the client or null if nothing is to sent (yet).
 */
    private static byte[] handleHeaderF1() {
        log.info("{} Get Firmware Version", moduleIdent);
        
        // send Firmware Version Packet - always return 1.43
        byte[] fwVersionPacket =  new byte[9];
        fwVersionPacket[0] = (byte) 0x09;
        fwVersionPacket[1] = (byte) 0x00;
        fwVersionPacket[2] = (byte) 0x40;
        fwVersionPacket[3] = (byte) 0x00;
        fwVersionPacket[4] = (byte) 0xF3;
        fwVersionPacket[5] = (byte) 0x0A;
        fwVersionPacket[6] = (byte) 0x01;
        fwVersionPacket[7] = (byte) 0x43;
        fwVersionPacket[8] = ClientManager.xor(fwVersionPacket);
        return fwVersionPacket;
    }
}
