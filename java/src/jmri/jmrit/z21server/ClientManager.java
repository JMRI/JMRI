package jmri.jmrit.z21server;

import jmri.DccThrottle;
import jmri.LocoAddress;
import jmri.ThrottleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.HashMap;

public class ClientManager implements ThrottleListener {

    private static ClientManager instance;
    private static final HashMap<InetAddress, AppClient> registeredClients = new HashMap<>();
    private static final HashMap<Integer, InetAddress> requestedThrottlesList = new HashMap<>();
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

    synchronized public void registerLocoIfNeeded(InetAddress clientAddress, int locoAddress) {
        if (!registeredClients.containsKey(clientAddress)) {
            AppClient client = new AppClient(clientAddress);
            registeredClients.put(clientAddress, client);
        }
        if (registeredClients.get(clientAddress).getThrottleFromLocoAddress(locoAddress) == null) {
            jmri.InstanceManager.throttleManagerInstance().requestThrottle(locoAddress, ClientManager.getInstance());
            requestedThrottlesList.put(locoAddress, clientAddress);
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

    synchronized public void setLocoFunction(InetAddress clientAddress, int locoAddress, int functionNumber, boolean bOn) {
        AppClient client = registeredClients.get(clientAddress);
        if (client != null) {
            DccThrottle throttle = client.getThrottleFromLocoAddress(locoAddress);
            if (throttle != null) {
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
        var tempMap = new HashMap<>(registeredClients); // to avoid concurrent modification
        for (AppClient c : tempMap.values()) {
            if (c.isTimestampExpired()) registeredClients.remove(c.getAddress());
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
        InetAddress client = requestedThrottlesList.get(locoAddress);
        if (client != null) {
            registeredClients.get(client).addThrottle(locoAddress, t);
            requestedThrottlesList.remove(locoAddress);
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
