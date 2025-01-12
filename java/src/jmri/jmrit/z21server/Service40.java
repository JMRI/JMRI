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
            case (byte)0x80:
                return handleHeader80();
            default:
                log.debug("{} Header {} not yet supported", moduleIdent, Integer.toHexString(command & 0xFF));
                break;
        }
        return null;
    }

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
        // response packet is now sent from the property change event
        //return buildTrackPowerPacket();
        return null;
    }

    private static byte[] buildTrackPowerPacket() {
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

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "PZLA_PREFER_ZERO_LENGTH_ARRAYS",
    justification = "Messages can be of any length, null is used to indicate absence of message for caller")
    private static byte[] handleHeaderE4(byte[] data, InetAddress clientAddress) {
        if (data[0] == 0x13) {
            int locomotiveAddress = (((data[1] & 0xFF) & 0x3F) << 8) + (data[2] & 0xFF);
            int rawSpeedData = data[3] & 0xFF;
            boolean bForward = ((rawSpeedData & 0x80) >> 7) == 1;
            int actualSpeed = rawSpeedData & 0x7F;
            log.debug("Set loco no {} direction {} with speed {}",locomotiveAddress, (bForward ? "FWD" : "RWD"), actualSpeed);

            ClientManager.getInstance().setLocoSpeedAndDirection(clientAddress, locomotiveAddress, actualSpeed, bForward);

            // response packet is now sent from the property change event
            //return ClientManager.getInstance().getLocoStatusMessage(clientAddress, locomotiveAddress);
        }
        else if (data[0] == (byte)0xF8) {
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

            // response packet is now sent from the property change event
            //return ClientManager.getInstance().getLocoStatusMessage(clientAddress, locomotiveAddress);
        }
        return null;
    }
    
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

}
