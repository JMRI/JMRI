package jmri.jmrit.z21server;


import jmri.DccThrottle;

import java.net.InetAddress;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import static jmri.jmrit.z21server.ClientManager.speedMultiplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppClient implements PropertyChangeListener  {

    private final static Logger log = LoggerFactory.getLogger(AppClient.class);
    private final InetAddress address;
    private final HashMap<Integer, DccThrottle> throttles;
    private final PropertyChangeListener changeListener;

    private Date timestamp;

    private static final int packetLenght = 14;


    public AppClient(InetAddress address, PropertyChangeListener changeListener) {
        this.address = address;
        this.changeListener = changeListener;
        throttles = new HashMap<>();
        heartbeat();
    }

    public void addThrottle(int locoAddress, DccThrottle throttle) {
        if (!throttles.containsKey(locoAddress)) {
            throttles.put(locoAddress, throttle);
            throttle.addPropertyChangeListener(this);
        }
    }
    
    public void clear() {
        for (DccThrottle t: throttles.values()) {
            t.removePropertyChangeListener(this);
        }
    }
    
    public DccThrottle getThrottleFromLocoAddress(int locoAddress) {
        if (throttles.containsKey(locoAddress)) {
            return throttles.get(locoAddress);
        } else {
            return null;
        }
    }

    public InetAddress getAddress() {
        return address;
    }

    public void heartbeat() {
        timestamp = new Date();
    }

    public boolean isTimestampExpired() {
        Duration duration = Duration.between(timestamp.toInstant(), new Date().toInstant());
        log.trace("Duration without heartbeat: {}", duration);
        //return (duration.toSeconds() >= 60);
        return (duration.toSeconds() >= 5); //debug
        /* Per Z21 Spec, clients are deemed lost after one minute of inactivity. */
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "PZLA_PREFER_ZERO_LENGTH_ARRAYS",
       justification = "Messages can be of any length, null is used to indicate absence of message for caller")
    public byte[] getLocoStatusMessage(Integer locoAddress) {
        if (throttles.containsKey(locoAddress)) {
            return buildLocoPacket(throttles.get(locoAddress));
        } else {
            return null;
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        if (changeListener != null) {
            log.trace("AppClient: Throttle change event: loco: {}, {}", ((DccThrottle)pce.getSource()).getLocoAddress(), pce);
            changeListener.propertyChange(new PropertyChangeEvent(pce.getSource(), "throttle-change", null, buildLocoPacket( ((DccThrottle)pce.getSource()) )));
        }
    }
    
    private byte[] buildLocoPacket(DccThrottle t) {
        byte[] locoPacket =  new byte[packetLenght];

        // Header
        locoPacket[0] = (byte) (7 + 7);
        locoPacket[1] = (byte) 0x00;
        locoPacket[2] = (byte) 0x40;
        locoPacket[3] = (byte) 0x00;
        locoPacket[4] = (byte) 0xEF;
        // Loco address
        int locoAddress = t.getLocoAddress().getNumber();
        locoPacket[5] = (byte) (locoAddress >> 8);
        locoPacket[6] = (byte) locoAddress;
        // set upper two bits of loco address MSB if loco address >= 128
        // see Z21 spec.
        if (locoAddress >= 128) {
            locoPacket[5] |= 0xC0;
        }
        //Loco drive and speed data
        locoPacket[7] = (byte) 0x04;
        float speed = t.getSpeedSetting();
        int packetspeed = Math.round(speed / speedMultiplier);
        if (speed < 0) packetspeed = 0;
        if (packetspeed > 128) packetspeed = 128;
        locoPacket[8] = (byte) ((t.getIsForward() ? (byte) 0x80 : 0) + ((byte) packetspeed));
        // Loco functions data
        locoPacket[9] = (byte) ((byte)
                (t.getFunction(0) ? 0x10 : 0) +
                (t.getFunction(4) ? 0x08 : 0) +
                (t.getFunction(3) ? 0x04 : 0) +
                (t.getFunction(2) ? 0x02 : 0) +
                (t.getFunction(1) ? 0x01 : 0)
        );
        locoPacket[10] = (byte) ((byte)
                (t.getFunction(12) ? 0x80 : 0) +
                (t.getFunction(11) ? 0x40 : 0) +
                (t.getFunction(10) ? 0x20 : 0) +
                (t.getFunction(9) ? 0x10 : 0) +
                (t.getFunction(8) ? 0x08 : 0) +
                (t.getFunction(7) ? 0x04 : 0) +
                (t.getFunction(6) ? 0x02 : 0) +
                (t.getFunction(5) ? 0x01 : 0)
        );
        locoPacket[11] = (byte) ((byte)
                (t.getFunction(20) ? 0x80 : 0) +
                (t.getFunction(19) ? 0x40 : 0) +
                (t.getFunction(18) ? 0x20 : 0) +
                (t.getFunction(17) ? 0x10 : 0) +
                (t.getFunction(16) ? 0x08 : 0) +
                (t.getFunction(15) ? 0x04 : 0) +
                (t.getFunction(14) ? 0x02 : 0) +
                (t.getFunction(13) ? 0x01 : 0)
        );
        locoPacket[12] = (byte) ((byte)
                (t.getFunction(28) ? 0x80 : 0) +
                (t.getFunction(27) ? 0x40 : 0) +
                (t.getFunction(26) ? 0x20 : 0) +
                (t.getFunction(25) ? 0x10 : 0) +
                (t.getFunction(24) ? 0x08 : 0) +
                (t.getFunction(23) ? 0x04 : 0) +
                (t.getFunction(22) ? 0x02 : 0) +
                (t.getFunction(21) ? 0x01 : 0)
        );
        locoPacket[13] = ClientManager.xor(locoPacket);

        return locoPacket;
    }


}
