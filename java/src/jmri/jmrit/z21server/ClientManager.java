package jmri.jmrit.z21server;

import java.beans.PropertyChangeEvent;
import jmri.DccThrottle;
import jmri.LocoAddress;
import jmri.ThrottleListener;
import jmri.Turnout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.beans.PropertyChangeListener;

/**
 * Register and unregister clients, set loco throttle
 * 
 * @author Jean-Yves Roda (C) 2023
 * @author Eckart Meyer (C) 2025 (enhancements, WlanMaus support)
 */

public class ClientManager implements ThrottleListener {

    private static ClientManager instance;
    private static final HashMap<InetAddress, AppClient> registeredClients = new HashMap<>();
    private static final HashMap<Integer, InetAddress> requestedThrottlesList = new HashMap<>(); //temporary store client InetAddress
    private PropertyChangeListener changeListener = null;
    private PropertyChangeListener clientListener = null; //the listener will be notified if a client is registered or unregistered
    public static float speedMultiplier = 1.0f / 128.0f;

    private final static Logger log = LoggerFactory.getLogger(ClientManager.class);

    private ClientManager() {
    }

/**
 * Return the one running instance of the client manager.
 * If there is no instance, create it.
 * 
 * @return the client manager instance
 */
    synchronized public static ClientManager getInstance() {
        if (instance == null) {
            instance = new ClientManager();
        }
        return instance;
    }
    
/**
 * Set the throttle change listener.
 * 
 * @param changeListener - the property change listener instance
 */
    public void setChangeListener(PropertyChangeListener changeListener) {
        this.changeListener = changeListener;
    }
    
/**
 * Set the client change listener.
 * The listener is called if a new is registered or a registered client is
 * unregistered.
 * 
 * @param clientListener - the property change listener instance
 */
    public void setClientListener(PropertyChangeListener clientListener) {
        this.clientListener = clientListener;
    }
    
/**
 * Get a hash map of the registered clients, indexed by their InetAddress
 * 
 * @return the hash map of registered clients
 */
    public HashMap<InetAddress, AppClient> getRegisteredClients() {
        return registeredClients;
    }
    
// Loco handling

/**
 * Register a client if not already registered and add a throttle for the given
 * loco address to the clients list of throttles.
 * 
 * @param clientAddress - InetAddress of the client
 * @param locoAddress - address of a loco
 */
    synchronized public void registerLocoIfNeeded(InetAddress clientAddress, int locoAddress) {
        if (!registeredClients.containsKey(clientAddress)) {
            AppClient client = new AppClient(clientAddress, changeListener);
            registeredClients.put(clientAddress, client);
            if (clientListener != null) {
                clientListener.propertyChange(new PropertyChangeEvent(this, "client-registered", null, null));
            }
        }
        if (registeredClients.get(clientAddress).getThrottleFromLocoAddress(locoAddress) == null) {
            // save loco address and client address temporary, so that notifyThrottleFound() knows the client for the Throttle
            requestedThrottlesList.put(locoAddress, clientAddress);
            jmri.InstanceManager.throttleManagerInstance().requestThrottle(locoAddress, ClientManager.getInstance()); //results in notifyThrottleFound() (hopefully)
        }
    }

/**
 * Set a JMRI throttle to new speed and direction.
 * Called when a Z21 client's user changes speed and/or direction.
 * 
 * @param clientAddress - the client's InetAddress
 * @param locoAddress - the loco address
 * @param speed - the speed to set
 * @param forward - true of forward, false if reverse
 */
    synchronized public void setLocoSpeedAndDirection(InetAddress clientAddress, int locoAddress, int speed, boolean forward) {
        AppClient client = registeredClients.get(clientAddress);
        if (client != null) {
            DccThrottle throttle = client.getThrottleFromLocoAddress(locoAddress);
            if (throttle != null) {
                if (throttle.getIsForward() != forward) throttle.setIsForward(forward);
                throttle.setSpeedSetting(speed * speedMultiplier);
                setActiveThrottle(client, throttle);
            } else {
                log.info("Unable to find throttle for loco {} from client {}", locoAddress, clientAddress);
            }
        } else {
            log.info("App client {} is not registered", clientAddress);
        }
    }

/**
 * Set a JMRI throttle to new function state.
 * Called when a Z21 client's user changes function status.
 * 
 * @param clientAddress - the client's InetAddress
 * @param locoAddress - the loco address
 * @param functionNumber - the function number to set
 * @param functionState - the new state of the function
 */
    synchronized public void setLocoFunction(InetAddress clientAddress, int locoAddress, int functionNumber, int functionState) {
        AppClient client = registeredClients.get(clientAddress);
        if (client != null) {
            DccThrottle throttle = client.getThrottleFromLocoAddress(locoAddress);
            if (throttle != null) {
                boolean bOn = (functionState & 0x01) == 0x01;
                if ( (functionState & 0x03) == 0x02) {
                    log.trace("Toggle! old state: {}", throttle.getFunction(functionNumber));
                    bOn = !throttle.getFunction(functionNumber);
                }
                log.trace("set function {} to value: {}", functionNumber, bOn);
                throttle.setFunction(functionNumber, bOn);
                setActiveThrottle(client, throttle);
            } else {
                log.info("Unable to find throttle for loco {} from client {}", locoAddress, clientAddress);
            }
        } else {
            log.info("App client {} is not registered", clientAddress);
        }
    }
    
/**
 * Return a Z21 LAN_X_LOCO_INFO packet for a given client and loco address
 * 
 * @param address - client InetAddress
 * @param locoAddress - the loco address
 * @return Z21 LAN_X_LOCO_INFO packet
 */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "PZLA_PREFER_ZERO_LENGTH_ARRAYS",
    justification = "Messages can be of any length, null is used to indicate absence of message for caller")
    synchronized public byte[] getLocoStatusMessage(InetAddress address, Integer locoAddress) {
        if (registeredClients.containsKey(address)) {
            AppClient client = registeredClients.get(address);
            return client.getLocoStatusMessage(locoAddress);
        } else {
            return null;
        }
    }
    
/**
 * Set the active (last used) throttle of a client.
 * 
 * @param client - the client's AppClient instance
 * @param throttle - the throttle instance
 */
    private void setActiveThrottle(AppClient client, DccThrottle throttle) {
        if (client.getActiveThrottle() != throttle) {
            client.setActiveThrottle(throttle);
            if (clientListener != null) {
                clientListener.propertyChange(new PropertyChangeEvent(this, "active-throttle", null, null));
            }
        }
    }
    
// Turnout handling
    
/**
 * Set a JMRI component to new state.
 * The component may be a JMRI turnout, light, route, signal mast, signal head or sensor,
 * depending on a property entry for the component containing the Z21 turnout number.
 * 
 * Called when a Z21 client's user changes state of a turnout.
 * 
 * @param clientAddress - client's InetAddress
 * @param turnoutNumber - the Z21 turnout number, starting from 1 as seen on the WlanMaus display (in the Z21 protocol turnouts start with 0).
 * @param state - true if turnout should be THROWN, false if CLOSED.
 */
    synchronized public void setTurnout(InetAddress clientAddress, int turnoutNumber, boolean state) {
        // state: 
        // false: set turnout closed
        // true: set turnout thrown
        int turnoutState = state ? Turnout.THROWN : Turnout.CLOSED;
        TurnoutNumberMapHandler.getInstance().setStateForNumber(turnoutNumber + 1, turnoutState);
    }
    
/**
 * Get a Z21 LAN_X_TURNOUT_INFO packet to be sent to the client fpr a given turnout number.
 * 
 * @param address - client's InetAdress
 * @param turnoutNumber - the Z21 Turnout Number
 * @return a Z21 LAN_X_TURNOUT_INFO packet
 */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "PZLA_PREFER_ZERO_LENGTH_ARRAYS",
    justification = "Messages can be of any length, null is used to indicate absence of message for caller")
    synchronized public byte[] getTurnoutStatusMessage(InetAddress address, Integer turnoutNumber) {
        int state = TurnoutNumberMapHandler.getInstance().getStateForNumber(turnoutNumber + 1);
        if (state >= 0) {
            // return LAN_X_TURNOUT_INFO packet
            // state in byte 7, bits 0 and 1 - WlanMaus displays the state according to byte 7
            // 0x02 - turnout closed (straight, main line)
            // 0x01 - turnout thrown (diverging line)
            // 0x00 - unknown (WlanMaus displays both legs in the turnout symbol)
            byte[] turnoutPacket =  new byte[9];
            turnoutPacket[0] = (byte) 0x09;
            turnoutPacket[1] = (byte) 0x00;
            turnoutPacket[2] = (byte) 0x40;
            turnoutPacket[3] = (byte) 0x00;
            turnoutPacket[4] = (byte) 0x43;
            turnoutPacket[5] = (byte) (turnoutNumber >> 8); //MSB
            turnoutPacket[6] = (byte) (turnoutNumber & 0xFF); //LSB
            turnoutPacket[7] = (byte) 0x00; //preset UNKNOWN
            if (state == Turnout.CLOSED) {
                turnoutPacket[7] = (byte) 0x02;
            }
            if (state == Turnout.THROWN) {
                turnoutPacket[7] = (byte) 0x01;
            }
            turnoutPacket[8] = ClientManager.xor(turnoutPacket);
            return turnoutPacket;
        }
        return null;
    }
    
// client handling

/**
 * Send a heartbeat() to the AppClient instance.
 * 
 * @param clientAddress - the client's InetAdress
 */
    synchronized public void heartbeat(InetAddress clientAddress) {
        AppClient client = registeredClients.get(clientAddress);
        if (client != null) client.heartbeat();
    }

 /**
  * Check all clients if they have not sent anything for a time peroid (60 seconds).
  * If the client has expired, remove it from the list.
  * 
  * @param removeAll - if true, remove all clients regardless of their expiry time.
  */
    synchronized public void handleExpiredClients(boolean removeAll) {
        HashMap<InetAddress, AppClient> tempMap = new HashMap<>(registeredClients); // to avoid concurrent modification
        for (AppClient c : tempMap.values()) {
            if (c.isTimestampExpired()  ||  removeAll) {
                log.debug("Remove expired client [{}]",c.getAddress());
                unregisterClient(c.getAddress());
             }
        }
    }
    
/**
 * Unregister a client.
 * Clean up the AppClient instance to remove listeners from throttles,
 * Remove client from hash map,
 * Call client listener to inform about removing the client
 * 
 * @param clientAddress - client's InetAddress
 */
    synchronized public void unregisterClient(InetAddress clientAddress) {
        log.info("Remove client [{}]", clientAddress);
        if (registeredClients.containsKey(clientAddress)) {
            registeredClients.get(clientAddress).clear();
        }
        registeredClients.remove(clientAddress);
        if (clientListener != null) {
            clientListener.propertyChange(new PropertyChangeEvent(this, "client-unregistered", null, null));
        }

        // the list should definitly be empty, so just in case...
        for (Iterator<HashMap.Entry<Integer, InetAddress>> it = requestedThrottlesList.entrySet().iterator(); it.hasNext(); ) {
            HashMap.Entry<Integer, InetAddress> e = it.next();
            if (e.getValue().equals(clientAddress)) {
                log.error("The list requestedThrottlesList should be empty, but is not. Remove {}", e);
                it.remove();
            }
        }
    }

// ThrottleListener implementation
    
/**
 * Called from the throttle manager when a requested throttle for a given loco address was found.
 * The thottle is then added to the list of throttles in the AppClient instance.
 * 
 * @param t - the (new) throttle bound to the loco.
 */
    @Override
    synchronized public void notifyThrottleFound(DccThrottle t) {
        int locoAddress = t.getLocoAddress().getNumber();
        // add the new throttle to the AppClient instance, which is identified by the clients InetAddress
        InetAddress client = requestedThrottlesList.get(locoAddress);
        if (client != null) {
            registeredClients.get(client).addThrottle(locoAddress, t);
            requestedThrottlesList.remove(locoAddress); //not needed any more, remove entry
        }
    }

/**
 * Called from the throttle manager when no throttle can be created for a loco address.
 * 
 * @param address - loco address
 * @param reason - a message from the throttle manager
 */
    @Override
    synchronized public void notifyFailedThrottleRequest(LocoAddress address, String reason) {
        log.info("Unable to get Throttle for loco address {}, reason : {}", address.getNumber(), reason);
        requestedThrottlesList.remove(address.getNumber());
    }

/**
 * Called from the throttle manager to ask if the throttle should be shared or the previous should be disconnected.
 * For now, we always use shared throttles.
 * 
 * @param address - loco address
 * @param question - STEAL, SHARE or both
 */
    @Override
    synchronized public void notifyDecisionRequired(LocoAddress address, DecisionType question) {
        jmri.InstanceManager.throttleManagerInstance().responseThrottleDecision(address, ClientManager.getInstance(), ThrottleListener.DecisionType.SHARE);
    }

    
/**
 * Helper to construct the Z21 protocol XOR byte
 * 
 * @param packet - Z21 packet
 * @return the XOR byte
 */
    public static byte xor(byte[] packet) {
        byte xor = (byte) (packet[0] ^ packet[1]);
        for (int i = 2; i < (packet.length - 1); i++) {
            xor = (byte) (xor ^ packet[i]);
        }
        return xor;
    }

}
