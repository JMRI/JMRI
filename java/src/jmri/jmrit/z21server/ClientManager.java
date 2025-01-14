package jmri.jmrit.z21server;

import java.beans.PropertyChangeEvent;
import jmri.DccThrottle;
import jmri.LocoAddress;
import jmri.ThrottleListener;
import jmri.Turnout;
import jmri.TurnoutManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.beans.PropertyChangeListener;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

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

    synchronized public static ClientManager getInstance() {
        if (instance == null) {
            instance = new ClientManager();
        }
        return instance;
    }
    
    public void setChangeListener(PropertyChangeListener changeListener) {
        this.changeListener = changeListener;
    }
    
    public void setClientListener(PropertyChangeListener clientListener) {
        this.clientListener = clientListener;
    }
    
    public HashMap<InetAddress, AppClient> getRegisteredClients() {
        return registeredClients;
    }
    
// Loco handling

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
    
    private void setActiveThrottle(AppClient client, DccThrottle throttle) {
        if (client.getActiveThrottle() != throttle) {
            client.setActiveThrottle(throttle);
            if (clientListener != null) {
                clientListener.propertyChange(new PropertyChangeEvent(this, "active-throttle", null, null));
            }
        }
    }
    
// Turnout handling
    
    synchronized public void setTurnout(InetAddress clientAddress, int turnoutNumber, boolean state) {
        Turnout t = getTurnoutFromNumber(turnoutNumber);
        if (t != null) {
            try {
                int turnoutState = state ? Turnout.THROWN : Turnout.CLOSED;
                log.debug("set state to {}", turnoutState);
                t.setState(turnoutState);
            }
            catch (Exception e) {
                log.warn("Cannot switch the turnout", e); // NOSONAR
            }
        }
    }
    
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "PZLA_PREFER_ZERO_LENGTH_ARRAYS",
    justification = "Messages can be of any length, null is used to indicate absence of message for caller")
    synchronized public byte[] getTurnoutStatusMessage(InetAddress address, Integer turnoutNumber) {
        Turnout t = getTurnoutFromNumber(turnoutNumber);
        if (t != null) {
            // send LAN_X_TURNOUT_INFO packet
            byte[] turnoutPacket =  new byte[9];
            turnoutPacket[0] = (byte) 0x09;
            turnoutPacket[1] = (byte) 0x00;
            turnoutPacket[2] = (byte) 0x40;
            turnoutPacket[3] = (byte) 0x00;
            turnoutPacket[4] = (byte) 0x43;
            turnoutPacket[5] = (byte) (turnoutNumber >> 8); //MSB
            turnoutPacket[6] = (byte) (turnoutNumber & 0xFF); //LSB
            turnoutPacket[7] = (byte) 0x00; //preset UNKNOWN
            if (t.getState() == Turnout.CLOSED) {
                turnoutPacket[7] = (byte) 0x02;
            }
            if (t.getState() == Turnout.THROWN) {
                turnoutPacket[7] = (byte) 0x01;
            }
            turnoutPacket[8] = ClientManager.xor(turnoutPacket);
            return turnoutPacket;
        }
        return null;
    }

    public Turnout getTurnoutFromNumber(int turnoutNumber) {
        // Find first turnout where the comment field contains the requested number in the format #n !!
        // This is quick and very dirty and should be replaced by somewhat more intelligent.
        // But it is a pragmatic solution for now.
        TurnoutManager turnouts = jmri.InstanceManager.getNullableDefault(jmri.TurnoutManager.class);
        if (turnouts != null) {
            Pattern numPattern = Pattern.compile(".*?#(\\d+).*");
            for (Turnout t : turnouts.getNamedBeanSet()) {
                if (t.getComment() != null) {
                    try {
                        Matcher m = numPattern.matcher(t.getComment());
                        if (m.matches()) {
                            log.trace("check turnout {}, comment: {}, number: {}", t.getUserName(), t.getComment(), m.group(1));
                            int num = Integer.parseInt(m.group(1));
                            if (num == (turnoutNumber + 1)) {
                                return t;
                            }                        
                        }
                    }
                    catch (Exception e) {} //silently ignore
                }
            }
        }
        return null;
    }
    
// client handling

    synchronized public void heartbeat(InetAddress clientAddress) {
        AppClient client = registeredClients.get(clientAddress);
        if (client != null) client.heartbeat();
    }

    synchronized public void handleExpiredClients(boolean removeAll) {
        HashMap<InetAddress, AppClient> tempMap = new HashMap<>(registeredClients); // to avoid concurrent modification
        for (AppClient c : tempMap.values()) {
            if (c.isTimestampExpired()  ||  removeAll) {
                log.debug("Remove expired client [{}]",c.getAddress());
                unregisterClient(c.getAddress());
             }
        }
    }
    
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

    @Override
    synchronized public void notifyFailedThrottleRequest(LocoAddress address, String reason) {
        log.info("Unable to get Throttle for loco address {}, reason : {}", address.getNumber(), reason);
        requestedThrottlesList.remove(address.getNumber());
    }

    @Override
    synchronized public void notifyDecisionRequired(LocoAddress address, DecisionType question) {
        jmri.InstanceManager.throttleManagerInstance().responseThrottleDecision(address, ClientManager.getInstance(), ThrottleListener.DecisionType.SHARE);
    }

    
    public static byte xor(byte[] packet) {
        byte xor = (byte) (packet[0] ^ packet[1]);
        for (int i = 2; i < (packet.length - 1); i++) {
            xor = (byte) (xor ^ packet[i]);
        }
        return xor;
    }

}
