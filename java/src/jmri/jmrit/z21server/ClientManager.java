package jmri.jmrit.z21server;

import jmri.DccThrottle;
import jmri.LocoAddress;
import jmri.ThrottleListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.beans.PropertyChangeListener;

public class ClientManager implements ThrottleListener {

    private static ClientManager instance;
    private static final HashMap<InetAddress, AppClient> registeredClients = new HashMap<>();
    private static final HashMap<Integer, InetAddress> requestedThrottlesList = new HashMap<>(); //temporary store client InetAddress
    private PropertyChangeListener changeListener = null;
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
    
    public HashMap<InetAddress, AppClient> getRegisteredClients() {
        return registeredClients;
    }

    synchronized public void registerLocoIfNeeded(InetAddress clientAddress, int locoAddress) {
        if (!registeredClients.containsKey(clientAddress)) {
            AppClient client = new AppClient(clientAddress, changeListener);
            registeredClients.put(clientAddress, client);
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
            } else {
                log.info("Unable to find throttle for loco {} from client {}", locoAddress, clientAddress);
            }
        } else {
            log.info("App client {} is not registered", clientAddress);
        }
    }

    synchronized public void heartbeat(InetAddress clientAddress) {
        AppClient client = registeredClients.get(clientAddress);
        if (client != null) client.heartbeat();
    }

    synchronized public void handleExpiredClients() {
        HashMap<InetAddress, AppClient> tempMap = new HashMap<>(registeredClients); // to avoid concurrent modification
        for (AppClient c : tempMap.values()) {
            if (c.isTimestampExpired()) {
                log.info("Remove expired client [{}]",c.getAddress());
                c.clear();
                registeredClients.remove(c.getAddress());

                // the list should definitly be empty, so just in case...
                for (Iterator<HashMap.Entry<Integer, InetAddress>> it = requestedThrottlesList.entrySet().iterator(); it.hasNext(); ) {
                    HashMap.Entry<Integer, InetAddress> e = it.next();
                    if (e.getValue().equals(c.getAddress())) {
                        log.error("The list requestedThrottlesList should be empty, but is not. Remove {}", e);
                        it.remove();
                    }
                }
             }
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
