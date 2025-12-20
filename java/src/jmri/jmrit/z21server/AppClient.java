package jmri.jmrit.z21server;

import jmri.DccThrottle;

import java.net.InetAddress;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import static jmri.jmrit.z21server.ClientManager.speedMultiplier;

import java.util.List;
import jmri.DccLocoAddress;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents a connected and registered client, e.g. a Z21 app or a WlanMaus
 * JMRI throttles a bound to this client.
 * 
 * @author Jean-Yves Roda (C) 2023
 * @author Eckart Meyer (C) 2025 (enhancements, WlanMaus support)
 */

public class AppClient implements PropertyChangeListener  {

    private final static Logger log = LoggerFactory.getLogger(AppClient.class);
    private final InetAddress address;
    private final HashMap<Integer, DccThrottle> throttles; //list of throttles the client uses
    private final PropertyChangeListener changeListener; //a throttle change event will be forwarded to this listener
    private DccThrottle activeThrottle = null; //last modified throttle
    private RosterEntry activeRosterEntry = null; //cached roster entry for activeThrottle

    private Date timestamp;

    private static final int packetLenght = 14;


/**
 * Constructor.
 * 
 * @param address of the connected client
 * @param changeListener to be called if one of the throttles has changed
 */
    public AppClient(InetAddress address, PropertyChangeListener changeListener) {
        this.address = address;
        this.changeListener = changeListener;
        throttles = new HashMap<>();
        heartbeat();
    }

/**
 * Add a throttle to the clients list of throttles.
 * The throttle instance is created by the caller.
 * 
 * @param locoAddress - the loco address
 * @param throttle - the throttle to be added
 */
    public void addThrottle(int locoAddress, DccThrottle throttle) {
        if (!throttles.containsKey(locoAddress)) {
            throttles.put(locoAddress, throttle);
            throttle.addPropertyChangeListener(this);
        }
        log.trace("addThrottle: list: {}", throttles.keySet());
    }
    
/**
 * Get the last used throttle
 * 
 * @return last used throttle
 */
    public DccThrottle getActiveThrottle() {
        return activeThrottle;
    }
    
/**
 * Get the roster ID for the last used throttle
 * 
 * @return roster ID as String
 */
    public String getActiveRosterIdString() {
        return (activeRosterEntry != null) ? activeRosterEntry.getId() : null;
    }
    
/**
 * Set last used throttle
 * 
 * @param t is the throttle
 */
    public void setActiveThrottle(DccThrottle t) {
        activeThrottle = t;
        activeRosterEntry = findRosterEntry(t);
    }
    
/**
 * Remove the listener from all throttles and clear the list of throttles.
 */
    public void clear() {
        log.trace("clear: list: {}", throttles.keySet());
        for (DccThrottle t: throttles.values()) {
            t.removePropertyChangeListener(this);
        }
        throttles.clear();
    }
    
/**
 * Get a throttle by loco address
 * 
 * @param locoAddress - the loco address
 * @return the throttle
 */
    public DccThrottle getThrottleFromLocoAddress(int locoAddress) {
        if (throttles.containsKey(locoAddress)) {
            return throttles.get(locoAddress);
        } else {
            return null;
        }
    }

/**
 * Get clients IP address
 * 
 * @return the InetAddress
 */
    public InetAddress getAddress() {
        return address;
    }

/**
 * The heartbeat for client expire
 */
    public void heartbeat() {
        timestamp = new Date();
    }

/**
 * Check if the client has not been seen (see heartbeat()) for at least 60 seconds.
 * 
 * @return true if not seen for more than 60 seconds
 */
    public boolean isTimestampExpired() {
        Duration duration = Duration.between(timestamp.toInstant(), new Date().toInstant());
        log.trace("Duration without heartbeat: {}", duration);
        return (duration.toSeconds() >= 60);
        //return (duration.toSeconds() >= 5); //debug
        /* Per Z21 Spec, clients are deemed lost after one minute of inactivity. */
    }

/**
 * Return a Z21 LAN_X_LOCO_INFO packet for a given loco address
 * 
 * @param locoAddress - the loco address
 * @return Z21 LAN_X_LOCO_INFO packet
 */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "PZLA_PREFER_ZERO_LENGTH_ARRAYS",
       justification = "Messages can be of any length, null is used to indicate absence of message for caller")
    public byte[] getLocoStatusMessage(Integer locoAddress) {
        if (throttles.containsKey(locoAddress)) {
            return buildLocoPacket(throttles.get(locoAddress));
        } else {
            return null;
        }
    }

/**
 * Listener for throttle events.
 * Will call the changeListener (in MainServer) with the Z21 LAN_X_LOCO_INFO packet as new value.
 * 
 * @param pce - throttle change event
 */
    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        if (changeListener != null) {
            log.trace("AppClient: Throttle change event: loco: {}, {}", ((DccThrottle)pce.getSource()).getLocoAddress(), pce);
            changeListener.propertyChange(new PropertyChangeEvent(pce.getSource(), "throttle-change", null, buildLocoPacket( ((DccThrottle)pce.getSource()) )));
        }
    }
    
/**
 * Find the roster entry from a given throttle instance.
 * 
 * @param t - the throttle instance
 * @return the roster entry
 */
    public static RosterEntry findRosterEntry(DccThrottle t) {
        RosterEntry re = null;
        if (t.getLocoAddress() != null) {
            List<RosterEntry> l = Roster.getDefault().matchingList(null, null, "" + ((DccLocoAddress) t.getLocoAddress()).getNumber(), null, null, null, null);
            if (l.size() > 0) {
                log.debug("Roster Loco found: {}", l.get(0).getDccAddress());
                re = l.get(0);
            }
        }
        return re;
    }
    
/**
 * Build a Z21 LAN_X_LOCO_INFO packet from a given throttle instance
 * 
 * @param t - the throttle instance
 * @return the Z21 LAN_X_LOCO_INFO packet
 */
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
